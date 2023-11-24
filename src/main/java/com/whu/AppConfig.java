package com.whu;

import com.spring.ComponentScan;

@ComponentScan("com.whu.service")
public class AppConfig {
    //配置类，相当于spring的配置文件，配置扫描路径，类中需要配置哪些bean，就相当于spring的配置文件中需要配置哪些bean
}
