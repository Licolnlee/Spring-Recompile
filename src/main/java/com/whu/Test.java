package com.whu;

import com.spring.WhuApplicationContext;

public class Test {
    public static void main(String[] args) {
        WhuApplicationContext applicationContext = new WhuApplicationContext(AppConfig.class);

        Object userService = applicationContext.getBean("userService");//如果是单例bean，则每次都是同一个对象
        Object userService1 = applicationContext.getBean("userService");//如果是prototype原型bean，则每次都是新的对象
    }
}
