package com.whu.service;

import com.spring.*;

@Component("userService")
@Scope("prototype")
public class UserServiceImpl implements InitializingBean, BeanNameAware, UserService {

    @Autowired
    private OrderService orderService;

    private String beanName;//bean的名字,它并不是一个bean对象，所以无法使用autowired注解,所以为了实现这个功能添加了beanNameAware接口来实现自动配置beanName属性,希望spring容器能够自动的赋值这个属性

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化");
    }
}
