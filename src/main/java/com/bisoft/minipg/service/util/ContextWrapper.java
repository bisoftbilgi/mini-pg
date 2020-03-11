package com.bisoft.minipg.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class ContextWrapper {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Autowired
    private ContextWrapper(ApplicationContext ac) {
        context = ac;
    }

    public <T> T getBean(String name, Class<T> requiredType) {
        return context.getBean(name, requiredType);
    }

    public <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }

    public Object getBean(String name) {
        return context.getBean(name);
    }

    public Object createBean(Class<?> beanClass) {
        return autowireCapableBeanFactory.createBean(beanClass, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
    }
}