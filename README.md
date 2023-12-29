# Spring

## ClassPathXmlApplicationContext

![img.png](static/ClassPathXmlApplicationDiagram.png)

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
