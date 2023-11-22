package com.spring;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class WhuApplicationContext {

    private final Class configClass;

    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();//单例池
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();//beanDefinitionMap,存放beanDefinition,扫描到的所有的类定义都存在这个map中


    //构造Spring容器的底层实现
    //单例池的底层实现
    public WhuApplicationContext(Class configClass) {//使用spring容器类的时候要构造一个容器，要传入一个配置类
        this.configClass = configClass;

        // 解析配置类，解析其中提供的配置类，主要解析这个类上面或者类中方法里面是否有一些Spring提供的注解
        // ComponentScan注解 -> 扫描路径 -> 扫描 -> BeanDefinition -> BeanDefinitionMap

        scan(configClass);

        for (BeanDefinition value : beanDefinitionMap.values()) {
            if (value.getScope().equals("singleton")) {
                //如果是单例bean，则在容器启动的时候就要创建好bean对象，然后放入单例池中
                Object bean = createBean(value);//创建bean对象
                singletonObjects.put(value.getClazz().getName(), bean);
            }
        }


    }

    public Object createBean(BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.newInstance();
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
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
                    System.out.println(classPath);
                    try {
                        Class<?> aClass = classLoader.loadClass(classPath);
                        if (aClass.isAnnotationPresent(Component.class)) {
                            //如果有component注解，则表示当前这个类是一个bean
                            //class-->bean 如果是一个bean，则需要创建一个bean对象（不一定，要根据bean的作用域进行判断，bean 的作用域有单例也有原型）
                            //解析类，判断当前bean是单例还是原型，如果是单例，则将bean对象放入单例池中，如果是原型，则不放入单例池中
                            //如果每次都解析非常麻烦，所以引出了beanDefinition，每个类都有scope和相应的beanName来定义对应的bean对象，将解析的类封装成beanDefinition，然后将beanDefinition放入beanDefinitionMap中

                            Component component = aClass.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();//获取beanName

                            BeanDefinition beanDefinition = new BeanDefinition();//生成beanDefinition
                            beanDefinition.setClazz(aClass);

                            if (aClass.isAnnotationPresent(Scope.class)) {
                                Scope scope = aClass.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scope.value());//有scope注解，那么表示当前bean是原型bean
                            } else {
                                beanDefinition.setScope("singleton");//没有scope注解，那么表示默认为单例bean
                            }
                            beanDefinitionMap.put(beanName, beanDefinition);//将beanDefinition放入beanDefinitionMap中
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public Object getBean(String beanName) {
        //如何根据beanName获取bean对象，如何判断当前bean是单例还是原型，都需要去解析类，解析类中是否有scope注解，如果有，则判断是单例还是原型
        //首先从beanDefinitionMap中去查询beanName，如果有，则表示当前bean是一个beanDefinition，然后根据beanDefinition中的scope属性来判断是单例还是原型
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);//获取beanDefinition对象
            if (beanDefinition.getScope().equals("singleton")) {//如果是单例的，则从单例池中获取
                Object o = singletonObjects.get(beanName);
                if (o == null) {//如果单例池中没有，则创建一个bean对象，然后放入单例池中
                    Object bean = createBean(beanDefinition);
                    return bean;
//                    //创建bean对象
//                    Class clazz = beanDefinition.getClazz();
//                    try {
//                        Object instance = clazz.newInstance();
//                        singletonObjects.put(beanName, instance);//放入单例池中
//                        return instance;
//                    } catch (InstantiationException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    return o;
                }

            } else {//不是单例bean，而是prototype bean,每一次都要去创建一个新的bean对象
                //创建bean对象
                Class clazz = beanDefinition.getClazz();
                try {
                    Object instance = clazz.newInstance();
                    return instance;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        } else {
            throw new NullPointerException();//如果不存在，简单处理抛出异常
        }
        return null;
    }
}
