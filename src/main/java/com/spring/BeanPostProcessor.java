package com.spring;

public interface BeanPostProcessor {

    /**
     * 方法postProcessBeforeInitialization作用为：在bean对象执行初始化方法之前执行
     *
     * @return java.lang.Object
     * @throws
     * @author Lee
     * * @param: bean
     * @param: beanName
     * @version
     * @date 11/23/2023
     * @time 7:06 PM
     */
    Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception;

    /**
     * 方法postProcessAfterInitialization作用为：在bean对象执行初始化方法之后执行
     *
     * @return java.lang.Object
     * @throws
     * @author Lee
     * * @param: bean
     * @param: beanName
     * @version
     * @date 11/23/2023
     * @time 7:06 PM
     */
    Object postProcessAfterInitialization(Object bean, String beanName) throws Exception;
}
