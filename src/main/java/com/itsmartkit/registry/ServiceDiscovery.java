package com.itsmartkit.registry;

import com.itsmartkit.common.Constants;
import com.itsmartkit.util.ZooKeeperUtil;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author cyj
 * @name ServiceDiscovery
 * @description TODO
 * @date 2020/4/7 14:44
 * Version 1.0
 */
public class ServiceDiscovery {

    private ZooKeeper zk;

    // 注册地址
    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        zk = ZooKeeperUtil.getZkConn(registryAddress);
    }

    private volatile List<String> services;

    public String discovery(String service) {
        if (services == null) {
            watchNode(service);
        }
        String host = "";
        int size = services.size();
        if(size > 0){
            if(size == 1){
                // 若只有一个地址，则获取该地址
                host = services.get(0);
            }else {
                // 若存在多个地址，则随机获取一个地址
                host = services.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return host;
    }

    private void watchNode(String service) {
        if (zk != null) {
            try {
                services = zk.getChildren(Constants.IRPC_PATH + Constants.PROVIDER_PATH + Constants.PATH_SEPARATOR + service, new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                            watchNode(service);
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}