package com.spring;

public class WhuApplicationContext {

    private final Class configClass;

    public WhuApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类
        // ComponentScan注解 -> 扫描路径 -> 扫描 -> BeanDefinition -> BeanDefinitionMap
    }

    public Object getBean(String beanName) {
        return null;
    }
}
