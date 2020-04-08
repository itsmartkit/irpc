package com.itsmartkit.binding;

import com.itsmartkit.annotation.Reference;
import com.itsmartkit.client.proxy.RpcProxy;
import com.itsmartkit.registry.ServiceDiscovery;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author cyj
 * @name ServiceBeanPostProcessor
 * @description TODO
 * @date 2020/4/8 15:16
 * Version 1.0
 */
@Component
public class ServiceBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private Environment env;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        ServiceDiscovery discovery = new ServiceDiscovery(env.getProperty("irpc.registry.address"));
        for (Field field : fields) {
            Reference annotation = field.getAnnotation(Reference.class);
            if (annotation != null) {
                String version = annotation.version();
                RpcProxy proxy = new RpcProxy(discovery, version);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy.create(field.getType()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
    }
}