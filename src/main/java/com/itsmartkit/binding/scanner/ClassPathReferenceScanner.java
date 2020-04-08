package com.itsmartkit.binding.scanner;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

/**
 * @author cyj
 * @name ClassPathReferenceScanner
 * @description TODO
 * @date 2020/4/8 12:47
 * Version 1.0
 */
public class ClassPathReferenceScanner extends ClassPathBeanDefinitionScanner {
    public ClassPathReferenceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        processBeanDefinitions(beanDefinitions);
        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return super.isCandidateComponent(beanDefinition);
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holders : beanDefinitions) {
            definition = (GenericBeanDefinition) holders.getBeanDefinition();
        }
    }
}