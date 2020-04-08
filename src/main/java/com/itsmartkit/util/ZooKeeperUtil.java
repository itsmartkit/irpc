package com.itsmartkit.util;

import com.itsmartkit.common.Constants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author cyj
 * @name ZooKeeperUtil
 * @description TODO
 * @date 2020/4/8 13:05
 * Version 1.0
 */
public class ZooKeeperUtil {

    private ZooKeeperUtil() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperUtil.class);

    private static CountDownLatch latch = new CountDownLatch(1);

    private static HashMap<String, ZooKeeper> zks = new HashMap<>();

    public static ZooKeeper getZkConn(String registryAddress) {
        if (zks.get(registryAddress) == null) {
            synchronized (zks) {
                if (zks.get(registryAddress) == null) {
                    try {
                        ZooKeeper zk = new ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
                            @Override
                            public void process(WatchedEvent watchedEvent) {
                                if (watchedEvent.getState()== Event.KeeperState.SyncConnected) {
                                    latch.countDown();
                                    LOGGER.info("连接zk服务[{}]成功！", registryAddress);
                                }
                            }
                        });
                        latch.await();
                        zks.put(registryAddress, zk);
                    } catch (Exception ex) {
                        LOGGER.error("连接zk服务[{}]失败", ex);
                    }
                }
            }
        }
        return zks.get(registryAddress);
    }
}