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

## AbstractApplicationContext
对于以上接口一次基础的实现。

### refresh()
#### prepareRefresh()
1. 设置一些状态位
2. 额外预留出属性初始化的位置方法 `initPropertySources()` 方法
3. 校验环境所必须的属性 `getEnvironment().validateRequiredProperties()`
4. 对监听器以及事件相关集合进行初始化

#### obtainFreshBeanFactory
`protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;`
AbstractRefreshableApplicationContext.refreshBeanFactory();
1. 清理已有的 BeanFactory
2. 创建新的 DefaultListableBeanFactory 进行替换
   1. 创建时会涉及到定制化 beanFactory，通过 `customizeBeanFactory()` 方法，有需要的话可以重写该方法
   2. 预留加载 BeanDefinition 的方法 `loadBeanDefinitions()`，该方法是如何读取加载 BeanDefinition 的主要实现；例如从 XML 文件中读取 Bean 的定义 

AbstractXmlApplicationContext.loadBeanDefinitions()：
最终调用了实现 `BeanDefinitionReader` 接口的 `XmlBeanDefintionRedaer` 类进行加载 bean definition 最终在方法 `doLoadBeanDefinitions` 实现。
doLoadBeanDefinitions() 方法中，`doLoadDocument()` 解析 xml 文件行程 java 对象文档；`registerBeanDefinitions()` 中调用 `DefaultBeanDefinitionDocumentReader.doRegisterBeanDefinitions()` 方法来注册 xml 中的 bean definition。
 
即在 refreshBeanFactory() 方法中就已经完成对了 Xml 中 bean definition 相关的读取和注册等

### prepareBeanFactory()
一句话，对已经通过 obtainFreshBeanFactory() 方法获得的 BeanFactory 进行一些配置 
1. BeanExpressionResolver
2. PropertyEditor
3. ApplicationContextAwareProcessor
4. ApplicationListenerDetector(这是一个 BeanPostProcessor)
5. LoadTimeWeaver
6. Environment、SystemProperties、SystemEnvironment、ApplicationStartup

### postProcessBeanFactory()
后置处理 Bean Factory 默认为空可以自定义

### invokeBeanFactoryPostProcessors()
PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors()
BeanFactoryPostProcessor、BeanDefinitionRegistryPostProcessor
BeanFactoryPostProcessor 来源于两个地方，一个是通过 beanFactory.addBeanPostProcessor() 方法直接添加的，另一个是通过注册成 spring bean 的方式然后通过 beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class） 方法来得到的。

### registerBeanPostProcessors()
添加 spring bean 中的 BeanPostProcessor
对比 invokeBeanFactoryPostProcessors 的处理逻辑，BeanPostProcessor 多单独判断了一个 MergedBeanDefinitionPostProcessor 且会讲将此 MergedBeanDefinitionPostProcessor 的实现放置到最后进行处理（即优先级最低）
最后添加了对于 ApplicationListenerDetector 来需找判断 Bean 是否是 ApplicationListener。

### initMessageSource()
判断 bean 中是否有叫做 messageSource MessageSource 对象，有的话就使用，没有的话就创建一个 DelegatingMessageSource。

### initApplicationEventMulticaster()
ApplicationEventPublisher 是将请求委托给了 ApplicationEventMulticaster。所以本方法是对此对象的初始化。
bean 中有 ApplicationEventMulticaster 则使用，没有则创建 SimpleApplicationEventMuticaster。

### onRefresh()
留空，允许子类进行一些定制操作

### registerListeners()
将 ApplicationListener 添加到 ApplicationEventMulticaster 中去；ApplicationListener 来源有两块：
1. 从 applicationListeners 集合中获取
2. 从 实现了 ApplicationListener 接口的 Bean 中获取

将 earlyApplicationEvents 中保存的事件进行广播

### finishBeanFactoryInitialization()
最终为了实例化单例 bean（不包括懒加载的 bean）。

1. 判断 bean 中是否有 ConversionService，有的话单独设置到 beanFactory 中
2. EmbeddedValueResolver
3. LoadTimeWeaverAware

preInstantiateSingletons():
isFactoryBean() 判断是否实现 FactoryBean 接口
getBean() 是获取 bean 实例化的重要实现！！！
SmartInitializingSingleton 当单例 bean 实例化完成后执行的回调方法

### finishRefresh()
1. 清除资源缓存
2. 出发 LifecycleProcessor 接口 onRefresh() 方法实现进行回调
3. 广播 ContextRefreshedEvent 事件




### 构造器

向上跟踪构造器：
`ClassPathXmlApplicationContext -> AbstractXmlApplicationContext -> AbstractRefreshableConfigApplicationContext -> AbstractRefreshableApplicationContext -> AbstractApplicationContext`

AbstractApplicationContext 构造器：

getResourcePatternResolver()：
setParent():

### setConfigLocations()

resolvePath():

getEnvironment():

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
