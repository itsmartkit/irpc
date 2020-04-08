package com.itsmartkit.registry;

import com.itsmartkit.common.Constants;
import com.itsmartkit.util.ZooKeeperUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cyj
 * @name ServiceRegistry
 * @description TODO
 * @date 2020/4/7 14:01
 * Version 1.0
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private ZooKeeper zk;

    // 注册地址
    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String serverAddress, String service) {
        if (zk == null) {
            zk = ZooKeeperUtil.getZkConn(registryAddress);
        }
        try {
            Stat stat = zk.exists(Constants.IRPC_PATH + Constants.PROVIDER_PATH + Constants.PATH_SEPARATOR + service, false);
            if (stat == null) {
                zk.create(Constants.IRPC_PATH + Constants.PROVIDER_PATH + Constants.PATH_SEPARATOR + service,
                        new byte[0],
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            zk.create(Constants.IRPC_PATH + Constants.PROVIDER_PATH + Constants.PATH_SEPARATOR + service + Constants.PATH_SEPARATOR + serverAddress,
                    new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("注册服务[{}]失败", serverAddress + Constants.GROUP_SEPARATOR + service , e);
        }
    }

}