package com.whu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class WhuBeanPostProcessor implements BeanPostProcessor {
    /**
     * 方法postProcessBeforeInitialization作用为：在bean对象执行初始化方法之前执行
     *
     * @param bean
     * @param beanName
     * @return java.lang.Object
     * @throws
     * @author Lee
     * * @param: bean
     * @param: beanName
     * @version
     * @date 11/23/2023
     * @time 7:06 PM
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        System.out.println("初始化bean之前执行的方法" + beanName);
        return bean;
    }

    /**
     * 方法postProcessAfterInitialization作用为：在bean对象执行初始化方法之后执行
     *
     * @param bean
     * @param beanName
     * @return java.lang.Object
     * @throws
     * @author Lee
     * * @param: bean
     * @param: beanName
     * @version
     * @date 11/23/2023
     * @time 7:06 PM
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        System.out.println("初始化bean之后执行的方法" + beanName);
        //判断是否需要执行AOP切点逻辑
        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(WhuBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");//找AOP切点，执行切点的逻辑
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
            //如果是userService，直接把生成的动态代理对象直接返回
        }
        return bean;
    }
}
