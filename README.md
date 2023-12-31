# Spring

## ClassPathXmlApplicationContext

![img.png](static/ClassPathXmlApplicationDiagram.png)

### ResourceLoader 
获取 Resource 资源, 在这里面定义了 `classpath:` 路径前缀
* DefaultResourceLoader: 提供一个默认的资源加载实现, 额外定义来一套加载来源的流程; 以及资源缓存的支持
* ResourcePatternResolver: 增加获取资源集合的接口, 此处定义了 `classpath*:` 的匹配字符串

### BeanFactory
获取 Bean 相关
* HierarchicalBeanFactory: 增加形成层次(父级) BeanFactory 接口方法
* ListableBeanFactory: 支持 Bean 以及 BeanDefinition 的集合查询操作(按类型等), 即添加对于一次性获取多个 Bean 的方法

### EnvironmentCapable
获取环境对应的属性,这里的环境可以理解为 `Profile` 的概念

### ApplicationEventPublisher
支持发布事件方法

### ApplicationContext
继承接口 环境属性、 可遍历 BeanFactory、 层级 BeanFactory、 事件发布、Resource 资源解析器;

### Lifecycle
对于生命周期的简单控制: 启动以及关闭

### ConfigurableApplicationContext
![img.png](static/ConfigurableListBeanFactory.png)

对于其继承的 `ApplicationContext` `Lifecycle` `Closeable` 等接口相关所需的变量提供了设置(类似 setter getter)的方法.
Spring 中关键的 `BeanFactoryPostProcessor` `refresh()` 定义和使用首次出现在这里.
`getBeanFactory()` 方法限定使用返回的 BeanFactory 类型为 `ConfigurableListableBeanFactory` 及其子接口

### SingletonBeanRegistry
对于单例 Bean 的 setter 与 getter 方法定义支持

### ConfigurableBeanFactory
继承 `HierarchicalBeanFactory` `SingletonBeanRegistry` 接口，添加对相关的变量提供类似 setter 设置方法
对于 Bean SCOPE 的定义（singleton、prototype）字面量定义出现在这里
添加设置：
1. BeanExpressionResolver（EL 表达式）相关方法
2. TypeConverter 相关方法
3. BeanPostProcessor 相关方法
4. Alias 别名相关方法
5. 设置一个 Bean 依赖其他 Bean 的相关方法

### AutowireCapableBeanFactory
针对自动注入 (Autowire) 添加相关的支持方法(创建 Bean, 配置 Bean, 填充 Bean 等)

### ConfigurableListableBeanFactory
继承 `ListableBeanFactory` `AutowireCapableBeanFactory` `ConfigurableBeanFactory` 接口
添加设置:
1. 装配(忽略 指定类型注册)相关方法
2. 清除 Bean 缓存
3. 冻结配置
4. 提前实例化所有单例 Bean


### 构造器

```java
public ClassPathXmlApplicationContext(
        String[]configLocations,boolean refresh,@Nullable ApplicationContext parent)
    throws BeansException{

    super(parent);
    setConfigLocations(configLocations);
    if(refresh){
        refresh();
    }
}
```

向上跟踪构造器：
`ClassPathXmlApplicationContext -> AbstractXmlApplicationContext -> AbstractRefreshableConfigApplicationContext -> AbstractRefreshableApplicationContext -> AbstractApplicationContext`

AbstractApplicationContext 构造器：
```java
public AbstractApplicationContext(@Nullable ApplicationContext parent){
    this();
    setParent(parent);
}

public AbstractApplicationContext(){
    this.resourcePatternResolver=getResourcePatternResolver();
}
```

getResourcePatternResolver()：
```java
protected ResourcePatternResolver getResourcePatternResolver(){
    return new PathMatchingResourcePatternResolver(this);
}
```

setParent():
```java
public void setParent(@Nullable ApplicationContext parent) {
    this.parent = parent;
    if (parent != null) {
        Environment parentEnvironment = parent.getEnvironment();
        if (parentEnvironment instanceof ConfigurableEnvironment configurableEnvironment) {
            getEnvironment().merge(configurableEnvironment);
        }
    }
}
```

### setConfigLocations()

```java
public void setConfigLocations(@Nullable String...locations){
        if(locations!=null){
        Assert.noNullElements(locations,"Config locations must not be null");
        this.configLocations=new String[locations.length];
        for(int i=0;i<locations.length;i++){
        this.configLocations[i]=resolvePath(locations[i]).trim();
        }
        }
        else{
        this.configLocations=null;
        }
        }
```

resolvePath():

```java
protected String resolvePath(String path){
        return getEnvironment().resolveRequiredPlaceholders(path);
        }
```

getEnvironment():

```java
public ConfigurableEnvironment getEnvironment(){
        if(this.environment==null){
        this.environment=createEnvironment();
        }
        return this.environment;
        }
```

### StandardEnvironment

![img.png](static/StandardEnvironmentDiagram.png)
PropertyResolver：

1. 获取读取特定属性的值
2. 替换字符串中占位符为具体的值

ConfigurablePropertyResolver：

1. 在 PropertyResolver 读取属性操作上增加了 ConfigurableConversionService 对于属性值类型的转换操作
2. 支持配置占位符的是由哪些字符组成特征

Environment：

1. 在 PropertyResolver 读取属性上增加了环境概念的读取操作，即 Spring 中的 Profile

ConfigurableEnvironment：

1. 添加对于设置 Profile 的操作
2. 支持对多个 Environment 之间的合并操作
3. 添加从环境变量以及系统属性中获取键值对的操作，以及带有优先级和可变的属性源 MutableProperty

AbstractEnvironment
1. 对 ConfigurableEnvironment 的默认实现（ConfigurablePropertyResolver 和 ConfigurableEnvironment）
2. 扩展 customizePropertySources() 方法来支持配置增加属性来源

StandardEnvironment
1. 将 systemEnvironment 和 systemProperties 添加到 propertySources 中

### PropertySourcesPropertyResolver
![img.png](static/PropertySourcesPropertyResolverDiagram.png)

### refresh
```java
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

        // Prepare this context for refreshing.
        prepareRefresh();

        // Tell the subclass to refresh the internal bean factory.
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

        // Prepare the bean factory for use in this context.
        prepareBeanFactory(beanFactory);

        try {
            // Allows post-processing of the bean factory in context subclasses.
            postProcessBeanFactory(beanFactory);

            StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
            // Invoke factory processors registered as beans in the context.
            invokeBeanFactoryPostProcessors(beanFactory);

            // Register bean processors that intercept bean creation.
            registerBeanPostProcessors(beanFactory);
            beanPostProcess.end();

            // Initialize message source for this context.
            initMessageSource();

            // Initialize event multicaster for this context.
            initApplicationEventMulticaster();

            // Initialize other special beans in specific context subclasses.
            onRefresh();

            // Check for listener beans and register them.
            registerListeners();

            // Instantiate all remaining (non-lazy-init) singletons.
            finishBeanFactoryInitialization(beanFactory);

            // Last step: publish corresponding event.
            finishRefresh();
        }

        catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                        "cancelling refresh attempt: " + ex);
            }

            // Destroy already created singletons to avoid dangling resources.
            destroyBeans();

            // Reset 'active' flag.
            cancelRefresh(ex);

            // Propagate exception to caller.
            throw ex;
        }

        finally {
            // Reset common introspection caches in Spring's core, since we
            // might not ever need metadata for singleton beans anymore...
            resetCommonCaches();
            contextRefresh.end();
        }
    }
}
```
