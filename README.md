# Spring-Recompile

学习了老师的 Spring 原理课程之后，独立完成写了个仿 Spring 轮子项目，实现功能包括：
1. @ComponentScan 组件扫描
2. bean 完整的生命周期过程（创建-依赖注入-初始化-销毁），包括 Bean后处理器（BeanPostProcessor）的调用、各种 Aware 接口回调、执行销毁方法。
3. 使用三级缓存解决属性注入和 set 方法注入的循环依赖问题，@Lazy 注解、ObjectFactory 解决构造方法注入的循环依赖问题
4. 完成了 5 种通知类型（@Before、@AfterReturning、@After、@AfterThrowing、@Around）的解析，对符合切点的目标对象进行代理增强。 应用在目标方法上的多个通知会链式调用执行，且实现了通知的调用顺序控制。

用到的设计模式：代理（JDK 生成动态代理）、责任链（通知的链式调用）、适配器（适配各种销毁方法的调用）、单例（比较器）、工厂（ObjectFactory）