package com.itsmartkit.factory.bean;

import com.itsmartkit.client.proxy.RpcProxy;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author cyj
 * @name ServiceFactoryBean
 * @description TODO
 * @date 2020/4/8 12:59
 * Version 1.0
 */
public class ServiceFactoryBean<T> implements FactoryBean<T> {


    Class<T> serviceInterface;

    public ServiceFactoryBean(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    public T getObject() throws Exception {

        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}