package com.whu;

import com.spring.WhuApplicationContext;

public class Test {
    public static void main(String[] args) {
        WhuApplicationContext applicationContext = new WhuApplicationContext(AppConfig.class);

        Object userService = applicationContext.getBean("userService");//如果是单例bean，则每次都是同一个对象
        Object userService1 = applicationContext.getBean("userService");//如果是prototype原型bean，则每次都是新的对象

        //单例bean和原型bean的区别，实现逻辑是什么
        //单例bean的实现逻辑如何做到每次getBean都是同一个对象，其实在getBean方法中，如果是单例bean，则直接从单例池中获取，如果是原型bean，则每次都是新的对象
        //单例bean的底层使用了一个map对象，key是beanName，value是bean对象，map<beanName,beanObject>,这个map就是单例池
    }
}
