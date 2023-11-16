package com.spring;

import java.io.File;
import java.net.URL;

public class WhuApplicationContext {

    private final Class configClass;

    //构造Spring容器的底层实现
    public WhuApplicationContext(Class configClass) {//使用spring容器类的时候要构造一个容器，要传入一个配置类
        this.configClass = configClass;

        // 解析配置类，解析其中提供的配置类，主要解析这个类上面或者类中方法里面是否有一些Spring提供的注解
        // ComponentScan注解 -> 扫描路径 -> 扫描 -> BeanDefinition -> BeanDefinitionMap

        ComponentScan componentScan = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScan.value().replace(".", "/");//扫描路径
        //扫描，看看是不是加了component注解
        //类加载器：bootstrap-->jre/lib(应用程序的启动类加载器)
        //类加载器：ext-->jre/ext/lib(应用程序的扩展类加载器)
        //类加载器：app-->classpath 我们在运行程序时，其实是java.exe --classpath 相应路径下的class文件(应用程序类加载器)
        ClassLoader classLoader = WhuApplicationContext.class.getClassLoader();//app的类加载器,这个类加载器可以加载classpath下的类
        URL resource = classLoader.getResource("com/whu/service");//获取资源的路径，通过定义classloader下对应资源路径，获取相应的资源
        File file = new File(resource.getFile());//获取文件
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
//                System.out.println(file1);
                String fileName = file1.getAbsolutePath();//获取文件的绝对路径
                if (fileName.endsWith(".class")) {//判断是否是class文件

                    String classPath = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class")).replace("\\", ".");//获取文件的class路径
                    try {
                        Class<?> aClass = classLoader.loadClass(classPath);
                        if (aClass.isAnnotationPresent(Component.class)) {
                            //如果有component注解，则表示当前这个类是一个bean
                            //class-->bean 如果是一个bean，则需要创建一个bean对象（不一定，要根据bean的作用域进行判断，bean 的作用域有单例也有原型）
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
        }


    }

    public Object getBean(String beanName) {
        return null;
    }
}
