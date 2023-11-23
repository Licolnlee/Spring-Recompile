package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WhuApplicationContext {

    private final Class configClass;

    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();//单例池
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();//beanDefinitionMap,存放beanDefinition,扫描到的所有的类定义都存在这个map中

    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();// beanPostProcessorList,存放beanPostProcessor


    //构造Spring容器的底层实现
    //单例池的底层实现
    public WhuApplicationContext(Class configClass) {//使用spring容器类的时候要构造一个容器，要传入一个配置类
        this.configClass = configClass;

        // 解析配置类，解析其中提供的配置类，主要解析这个类上面或者类中方法里面是否有一些Spring提供的注解
        // ComponentScan注解 -> 扫描路径 -> 扫描 -> BeanDefinition -> BeanDefinitionMap

        scan(configClass);

        for (Map.Entry<String,BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition value = entry.getValue();
            if ("singleton".equals(value.getScope())) {
                //如果是单例bean，则在容器启动的时候就要创建好bean对象，然后放入单例池中
                Object bean = createBean(beanName, value);//创建bean对象
                singletonObjects.put(beanName, bean);
            }
        }


    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        //实例化之前做一些事情

        //创建实例化bean对象
        Class clazz = beanDefinition.getClazz();
        //实例化之后做一些事情
        try {
            Object instance = clazz.newInstance();
            //依赖注入，需要将当前bean中的所有的autowired注解的属性进行注入,需要对属性进行赋值
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    //如果当前属性有autowired注解，则需要进行注入
                    //需要注入的属性，需要从容器中获取,那么首先需要从spring容器中（单例池）根据属性类型或者属性名获取bean对象
                    Object bean = getBean(declaredField.getName());//根据属性名获取bean对象
                    declaredField.setAccessible(true);//设置属性可以被访问
                    declaredField.set(instance, bean);//给属性赋值
                }
            }

            //Aware回调
            //判断当前这个类或者这个实例是否实现了BeanNameAware接口，如果实现了，则需要调用setBeanName方法
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            //初始化之前做一些事情
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);//对当前的bean对象进行初始化前的额外加工处理
            }
            //初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();//初始化bean之后执行的方法,进行一些初始化操作，比如说一些初始化逻辑的执行如初始化数据库连接池
            }
            //初始化之后做一些事情
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);//对当前的bean对象进行初始化后的额外加工处理
            }



            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

                    String classPath = fileName.substring(fileName.indexOf("com".concat(File.separator)), fileName.indexOf(".class")).replace(File.separator, ".");//获取文件的class路径
//                    System.out.println(classPath);
                    try {
                        Class<?> aClass = classLoader.loadClass(classPath);
                        if (aClass.isAnnotationPresent(Component.class)) {


                            //如果有component注解，则表示当前这个类是一个bean
                            //class-->bean 如果是一个bean，则需要创建一个bean对象（不一定，要根据bean的作用域进行判断，bean 的作用域有单例也有原型）
                            //解析类，判断当前bean是单例还是原型，如果是单例，则将bean对象放入单例池中，如果是原型，则不放入单例池中
                            //如果每次都解析非常麻烦，所以引出了beanDefinition，每个类都有scope和相应的beanName来定义对应的bean对象，将解析的类封装成beanDefinition，然后将beanDefinition放入beanDefinitionMap中

                            //发现bean，但是这个bean比较特殊，发现是一个beanPostProcessor，那么需要将这个beanPostProcessor放入beanPostProcessorList中
                            if (BeanPostProcessor.class.isAssignableFrom(aClass)) {//判断当前这个类是否是BeanPostProcessor的子类或者子接口
                                BeanPostProcessor o = (BeanPostProcessor) aClass.getDeclaredConstructor().newInstance();//创建beanPostProcessor对象
                                //创建完之后，需要将beanPostProcessor对象放入beanPostProcessorList中
                                beanPostProcessorList.add(o);//将beanPostProcessor对象放入beanPostProcessorList中
                            }


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
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
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
                    Object bean = createBean(beanName, beanDefinition);
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
                return createBean(beanName, beanDefinition);
//                Class clazz = beanDefinition.getClazz();
//                try {
//                    Object instance = clazz.newInstance();
//                    return instance;
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }

            }
        } else {
            throw new NullPointerException();//如果不存在，简单处理抛出异常
        }
//        return null;
    }
}
