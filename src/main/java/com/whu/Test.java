package com.whu;

import com.spring.WhuApplicationContext;
import com.whu.service.UserService;

public class Test {
    public static void main(String[] args) {
        //启动Spring容器
        WhuApplicationContext applicationContext = new WhuApplicationContext(AppConfig.class);//通过spring容器扫描路径，去扫描相应的类，然后将类进行实例化，然后放到spring容器中
//        System.out.println(applicationContext.getBean("userService"));

//        Object userService = applicationContext.getBean("userService");//如果是单例bean，则每次都是同一个对象
//        Object userService1 = applicationContext.getBean("userService");//如果是prototype原型bean，则每次都是新的对象

        UserService userService = (UserService) applicationContext.getBean("userService");//通过spring容器去获得bean对象,大部分情况下通过class实例化出来的对象和bean对象是同一个对象，但是有一个特殊情况，就是AOP，AOP会对bean对象进行增强，增强之后的对象和原来的对象不是同一个对象
        System.out.println("test");
        userService.test();//1.先执行代理对象的逻辑 2.再执行目标对象的逻辑
//        System.out.println(userService1);
        //单例bean和原型bean的区别，实现逻辑是什么
        //单例bean的实现逻辑如何做到每次getBean都是同一个对象，其实在getBean方法中，如果是单例bean，则直接从单例池中获取，如果是原型bean，则每次都是新的对象
        //单例bean的底层使用了一个map对象，key是beanName，value是bean对象，map<beanName,beanObject>,这个map就是单例池
    }
}
