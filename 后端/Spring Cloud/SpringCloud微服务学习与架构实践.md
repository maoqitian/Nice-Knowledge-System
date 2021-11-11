# 微服务架构
![SpringCloud架构想法](https://github.com/maoqitian/MaoMdPhoto/raw/master/SpringCloud/SpringCloud%E6%9E%B6%E6%9E%84%E6%83%B3%E6%B3%95.jpg)
##  Dubbo 微服务架构（阿里系）
- 目前没做更多了解

## Spring Cloud 微服务架构核心组件

### Spring Cloud 版本

- https://www.cnblogs.com/xingzc/p/9414208.html

### 注册中心： nacos（阿里）、Consul、Eureka (Server Client)
#### Eureka
- 简单搭建 POM应用依赖包

```
<!--eureka 依赖包-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <!--配置Spring Cloud Config Client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
```
- 在工程的启动类EurekaApplication加上@EnableEurekaServer注解开启eureka Server的功能

```
@SpringBootApplication
@EnableEurekaServer //开启注册中心服务
public class EurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }

}
```
- 远程配置仓库创建 eureka-config.yml文件

```
spring:
  profiles:
    active:
    - dev
---
server:
  port: 8805
spring:
  profiles: dev     #开发环境
  application:
    name: eureka-dev
  rabbitmq: # 消息中间件 rabbitmq 配置
    host: 172.16.112.17
    port: 5672 # 注意该端口为 amqp 端口 而不是 http 访问端口
    username: admin
    password: Gxxmt    
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false # 实例是否在eureka服务器上注册自己的信息以供其他服务发现，默认为true
    fetch-registry: false # 此客户端是否获取eureka服务器注册表上的注册信息，默认为true
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
---
server:
  port: 8805
spring:
  profiles: test     #开发环境
  application:
    name: eureka-test
  rabbitmq: # 消息中间件 rabbitmq 配置
    host: xxxxx
    port: 5672 # 注意该端口为 amqp 端口 而不是 http 访问端口
    username: admin
    password: Gxxmt    
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false # 实例是否在eureka服务器上注册自己的信息以供其他服务发现，默认为true
    fetch-registry: false # 此客户端是否获取eureka服务器注册表上的注册信息，默认为true
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```
- 创建bootstrap.yml文件
```
spring:
  cloud:
    config:
      name: eureka-config #需要从github上读取的资源名称，注意没有yml后缀名
      profile: dev   #本次访问的配置项
      label: master
      uri: http://localhost:8666  #本微服务启动后先去找配置中心服务，通过SpringCloudConfig获取GitLab的服务地址
```
- **注意：bootstrap.yml的profile具体值是什么，从而确定它能从git上取得什么样的配置**
- 创建application.yml文件
```
spring:
  application:
    name: eureka-server
```
- 启动服务注册中心就启动了

### 服务：Ribbon、Feign

- Ribbon即提供服务，也消费服务，更优雅使用暴露接口则使用Feign

####
- Spring Cloud Ribbon是一个基于Http和TCP的客服端负载均衡工具，它是基于Netflix Ribbon实现的。与Eureka配合使用时，Ribbon可自动从Eureka Server (注册中心)获取服务提供者地址列表，并基于负载均衡算法，通过在客户端中配置ribbonServerList来设置服务端列表去轮询访问以达到均衡负载的作用。

#### 声明式服务Feign实例
- 当我们要调用一个服务时，需要知道服务名和api地址，这样才能进行服务调用，服务少时，这样写觉得没有什么问题，但当服务一多，接口参数很多时，上面的写法就显得不够优雅了。所以，接下来，来说说一种更好更优雅的调用服务的方式：Feign
- Feign是Netflix开发的声明式、模块化的HTTP客户端。Feign可帮助我们更好更快的便捷、优雅地调用HTTP API。

### 网关：Zuul、Gateway（英文为入口的意思）
- RPC 的全称是 Remote Procedure Call 是一种进程间通信方式。
它允许程序调用另一个地址空间（通常是共享网络的另一台机器上）的过程或函数，而不用程序员显式编码这个远程调用的细节。即无论是调用本地接口/服务的还是远程的接口/服务，本质上编写的调用代码基本相同。
比如两台服务器A，B，一个应用部署在A服务器上，想要调用B服务器上应用提供的函数或者方法，由于不在一个内存空间，不能直接调用，这时候需要通过就可以应用RPC框架的实现来解决。

### Gateway初级尝试
- Gateway概念predicates和filters
- predicates:Predicate来自于java8的接口。Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将Predicate组合成其他复杂的逻辑（比如：与，或，非）。可以用于接口请求参数校验、判断新老数据是否有变化需要进行更新操作。add--与、or--或、negate--非。

#### Gateway使用
- POM 配置
```
<!--配置Client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--网关组件-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
```
- 在工程的启动类GatewayApplication加上@EnableDiscoveryClient注解开启eureka client的功能

```
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
```
-工程的配置文件（application.yml）配置应用名、端口、向注册中心注册的地址、每个服务的路由配置，以及暴露actuator的所有端口

```
server:
  port: 8081
spring:
  application:
    name: sc-gateway-service
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true # gateway开启服务注册和发现的功能
          lowerCaseServiceId: true # 将请求路径上的服务名配置为小写（因为服务注册的时候，向注册中心注册时将服务名转成大写的了）
      routes: # 灵活配置路由对应地址
        - id: api-feign
          uri: lb://API-FEIGN # 注册中心对应的服务名称
          predicates:
            - Path=/feign/**  # 配置的路径地址
          filters:
            - StripPrefix=1
        - id: ribbon-provider
          uri: lb://RIBBON-PROVIDER
          predicates:
            - Path=/ribbon/**
          filters:
            - StripPrefix=1
        - id: ribbon-consumer
          uri: lb://RIBBON-CONSUMER
          predicates:
            - Path=/ribbonconsumer/**
          filters:
            - StripPrefix=1
eureka:
  instance:
    leaseRenewalIntervalInSeconds: 5 #表示eureka client间隔多久去拉取服务注册信息，默认为30秒,网关应该尽快拉取信息
    health-check-url-path: /actuator/health
  client:
    service-url:
      registryFetchIntervalSeconds: 5
      defaultZone: http://localhost:8805/eureka/  #注册中心
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```
### 熔断处理：Hystrix

### 流量卫兵：Sentiel（阿里）
- 服务之间会有相互依赖关系，例如服务A做到了1秒上万个QPS（（Query Per Second）每秒查询率），但这时候服务B并无法满足1秒上万个QPS，那么如何保证服务A在高频调用服务B时，服务B仍能正常工作呢？一种比较常见的情况是，服务A调用服务B时，服务B因无法满足高频调用出现响应时间过长的情况，导致服务A也出现响应过长的情况，进而产生连锁反应影响整个依赖链上的所有应用，这时候就需要熔断和降级的方法。Sentinel通过并发线程数进行限制和响应时间对资源进行降级两种手段来对服务进行熔断或降级。

### 鉴权认证：AuthServer

### 配置中心 Spring Cloud Config

- SpringCloud Config为微服务架构中的微服务提供集中化的外部配置支持，配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部配置
- 不同环境不同配置，动态化的配置更新，分环境部署比如dev/test/prod/beta/release 

#### Spring Cloud Config Server

- 首先需要在gitLab或者github 中创建仓库和对应的服务配置文件，配置了开发环境和测试环境对应端口号不同，注册中心服务也不同

```
spring:
  profiles:
    active:
    - dev
---
server: 
  port: 8201 
spring:
  profiles: dev
  application: 
    name: microservicecloud-config-client
eureka: 
  client: 
    service-url: 
      defaultZone: http://eureka-dev.com:7001/eureka/   
---
server: 
  port: 8202 
spring:
  profiles: test
  application: 
    name: microservicecloud-config-client
eureka: 
  client: 
    service-url: 
      defaultZone: http://eureka-test.com:7002/eureka/

#  请保存为UTF-8格式
```
- POM 配置
```
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
        </dependency>
        <!--配置Server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
```
- 在工程的启动类ConfigServer加@EnableConfigServer注解，开启配置中心服务端 Config server的功能

```
@SpringBootApplication
@EnableConfigServer
public class ConfigServer {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }

}
```
- 工程的配置文件（application.yml）配置应用名、端口、向注册中心注册的地址、git服务地址及用户密码，以及暴露actuator的所有端口

```
server:
  port: 8666
spring:
  application:
    name: microservicecloud-config # 应用名称
  cloud:
    config:
      server:
        git: # git 仓库配置
          uri: xxxxxxx
          search-paths: microservicecloud-config    # 路径名称
          default-label: master  #分支名称
          username: xxxxx
          password: xxxxxx
          #ignoreLocalSshSettings: true
          #skip-ssl-validation: true
          #hostKey: someHostKey
          #hostKeyAlgorithm: id-rsa
          #private-key: |

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8805/eureka/ #注册中心
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```
- 到此启动项目我们配置中心服务端就能够启动了，如何获取配置？接下来看看配置读取规则
- {application}映射到客户端的“spring.application.name”;

- {profile}映射到客户端上的“spring.profiles.active”（逗号分隔列表）; 和

- {label}这是一个服务器端功能，标记“版本”的一组配置文件。
- HTTP服务具有以下格式的资源：

```
/{application}/{profile}[/{label}]

eg: http://localhost:8666/application/test/master

/{application}-{profile}.yml 

eg：http://localhost:8666/application/dev

/{label}/{application}-{profile}.yml

eg ：http://localhost:8666/master/application-test.yml
```
#### Spring Cloud Config Client

- 配置Client 启动之前确保配置Server 已经提供配置服务，并且在Git配置文件仓库中有对应的Client 服务配置文件，以下以Gateway配置文件为例，Git仓库中创建gateway-config.yml文件

```
spring:
  profiles:
    active:
    - dev
---
server:
  port: 8081
spring:
  profiles: dev     #开发环境
  application:
    name: sc-gateway-service-dev
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true # gateway开启服务注册和发现的功能
          lowerCaseServiceId: true # 将请求路径上的服务名配置为小写（因为服务注册的时候，向注册中心注册时将服务名转成大写的了）
      routes: # 灵活配置路由对应地址
        - id: api-feign
          uri: lb://API-FEIGN # 注册中心对应的服务名称
          predicates:
            - Path=/feign/**  # 配置的路径地址
          filters:
            - StripPrefix=1
        - id: ribbon-provider
          uri: lb://RIBBON-PROVIDER
          predicates:
            - Path=/ribbon/**
          filters:
            - StripPrefix=1
        - id: ribbon-consumer
          uri: lb://RIBBON-CONSUMER
          predicates:
            - Path=/ribbonconsumer/**
          filters:
            - StripPrefix=1
eureka:
  instance:
    leaseRenewalIntervalInSeconds: 5 #表示eureka client间隔多久去拉取服务注册信息，默认为30秒,网关应该尽快拉取信息
    health-check-url-path: /actuator/health
  client:
    service-url:
      registryFetchIntervalSeconds: 5
      defaultZone: http://localhost:8805/eureka/  #注册中心
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS    
---
server:
  port: 8181
spring:
  profiles: test   #测试环境
  application:
    name: sc-gateway-service-test
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true # gateway开启服务注册和发现的功能
          lowerCaseServiceId: true # 将请求路径上的服务名配置为小写（因为服务注册的时候，向注册中心注册时将服务名转成大写的了）
      routes: # 灵活配置路由对应地址
        - id: api-feign
          uri: lb://API-FEIGN # 注册中心对应的服务名称
          predicates:
            - Path=/feign/**  # 配置的路径地址
          filters:
            - StripPrefix=1
        - id: ribbon-provider
          uri: lb://RIBBON-PROVIDER
          predicates:
            - Path=/ribbon/**
          filters:
            - StripPrefix=1
        - id: ribbon-consumer
          uri: lb://RIBBON-CONSUMER
          predicates:
            - Path=/ribbonconsumer/**
          filters:
            - StripPrefix=1
eureka:
  instance:
    leaseRenewalIntervalInSeconds: 5 #表示eureka client间隔多久去拉取服务注册信息，默认为30秒,网关应该尽快拉取信息
    health-check-url-path: /actuator/health
  client:
    service-url:
      registryFetchIntervalSeconds: 5
      defaultZone: http://localhost:8805/eureka/  #注册中心
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS    
#  请保存为UTF-8格式
```
- Client引入依赖包
```
<!--配置Client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
```
#####  bootstrap.yml 和 Application.yml
- applicaiton.yml是**用户级的资源配置项**，bootstrap.yml是**系统级的，优先级更加高**
- Spring Cloud会创建一个`Bootstrap Context`，作为Spring应用的`Application Context`的父上下文。初始化的时候，`Bootstrap Context`负责从外部源加载配置属性并解析配置。这两个上下文共享一个从外部获取的`Environment`。`Bootstrap`属性有高优先级，默认情况下，它们不会被本地配置覆盖。 `Bootstrap context`和`Application Context`有着不同的约定，
所以新增了一个`bootstrap.yml`文件，保证`Bootstrap Context`和`Application Context`配置的分离。
- 以下继续以Gateway配置文件为例。创建bootstrap.yml文件（注意目前是配置Client），bootstrap.yml里面的profile值是什么，决定从git上读取什么对应环境的配置
- 创建bootstrap.yml文件
```
spring:
  cloud:
    config:
      name: gateway-config #需要从github上读取的资源名称，注意没有yml后缀名
      profile: dev   #本次访问的配置项
      label: master
      uri: http://localhost:8666  #本微服务启动后先去找配置中心服务，通过SpringCloudConfig获取GitLab的服务地址
```
- **注意：bootstrap.yml的profile具体值是什么，从而确定它能从git上取得什么样的配置**
- 创建application.yml文件
```
spring:
  application:
    name: sc-gateway-service
```
- 在工程的启动类GatewayApplication加上@EnableDiscoveryClient注解开启eureka client的功能

```
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
```
- 到此启动配置Client，如果服务正常说明Gateway服务最为配置Client从配置服务端获取配置信息成功。其他服务同理可以作为配置Client从配置服务端获取配置信息。

### Spring Cloud Bus 事件总线配合Spring Cloud Config Server动态刷新配置
- 上一小节我们已经实现了配置文件的统一管理（git 仓库），但是，每次修改配置文件后，还需要重新启动应用才能加载到修改后的配置文件，这还没有达到我们的目的，我们最终想要的是，修改完配置文件后，不需要重启我们的应用，就可以重新加载到修改后的配置文件，其实 Spring Cloud 已经为我们提供了这样的支持，那就是 Spring Cloud Bus 组件。
#### Spring Cloud Config + Spring Cloud Bus 实现配置刷新
- 将Config Server也加入到消息总线中，并使用Config Server的/bus/refresh端点来实现配置的刷新。
- 在Config Server的POM中引入依赖包
```
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--<dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
        </dependency>-->
        <!--配置Server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <!--spring-cloud-bus-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
```
- 工程的配置文件（application.yml）加入消息中间件rabbitmq配置

```
spring:
  rabbitmq: # 消息中间件 rabbitmq 配置
    host: 172.16.112.17
    port: 5672 # 注意该端口为 amqp 端口 而不是 http 访问端口
    username: admin
    password: Gxxmt
```
- Client Spring Cloud Bus 配置，每一个服务服务都需要加上spring-cloud-starter-bus-amqp依赖包，并在在bootstrap.yml加入对应消息中间件的配置（rabbitmq），为了让消息中间件的队列中能有对应的服务信息，为刷新做准备

```
# POM中引入依赖包
 <!--spring-cloud-bus-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
        
# bootstrap.yml
spring:
  rabbitmq: # 消息中间件 rabbitmq 配置
    host: 172.16.112.17
    port: 5672 # 注意该端口为 amqp 端口 而不是 http 访问端口
    username: admin
    password: Gxxmt
```
- 接着我们更新git配置仓库的文件然后手动发送post请求刷新配置，在命令行中输入curl -X POST http://localhost:8666/actuator/bus-refresh 来执行配置的刷新
#### 部分刷新
- 如果需要在对应类中动态获取配置更新，需要加上注解 @RefreshScope 
- 某些场景下（例如灰度发布），我们可能只想刷新部分微服务的配置，此时可通过/bus/refresh端点的destination参数来定位要刷新的应用程序。例如：/bus/refresh?destination=customers:9000，这样消息总线上的微服务实例就会根据destination参数的值来判断是否需要要刷新。其中，customers:9000指的是各个微服务的ApplicationContext ID（注册中心服务注册的application）。destination参数也可以用来定位特定的微服务。例如：/bus/refresh?destination=customers:** ，这样就可以触发customers微服务所有实例的配置刷新。
- 如下代码我们在网关服务中获取配置中的一个字段值，并指定刷新
```
@SpringBootApplication
@EnableDiscoveryClient
@RestController
@RefreshScope //加入该注解才能获取 spring cloud bus 通知动态的更新
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Value("${test}")
    private String hello;

    @RequestMapping("test")
    public HashMap hello() {
        HashMap<String,String> hashMap=new HashMap<String,String>();
        hashMap.put("test",hello);
        return hashMap;
    }
}

刷新请求 curl -X POST http://localhost:8666/actuator/bus-refresh?destination=gateway-service-dev:8081
```
- 整体流程示意图

![SpringCloudConfig-Bus-config](https://github.com/maoqitian/MaoMdPhoto/raw/master/SpringCloud/SpringCloudConfig-Bus-config.jpg)

#### gitlab 自动刷新配置 webhook 
- 前面我们更新Git中的配置都是手动触发更新，我们希望只要提交git配置更新，就能够自动刷新，这里可以使用Gitlab或者github的webhook功能、




## 管理和监控SpringBoot应用程序(Spring Boot Admin)

- Spring Boot Admin是一个开源社区项目，用于管理和监控SpringBoot应用程序。 应用程序作为Spring Boot Admin Client向为Spring Boot Admin Server注册（通过HTTP）或使用SpringCloud注册中心（例如Eureka，Consul）发现。 UI是的AngularJs应用程序，展示Spring Boot Admin Client的Actuator端点上的一些监控。
###  Spring Boot Admin Server
- 引入依赖包 spring-boot-admin-starter-server 2.1.4版本比2.1.3版本文档并解决了bug：reactor.retry.RetryExhaustedException：de.codecentric.boot.admin.server.eventstore.OptimisticLockingException：Verison 1 被 1 替换

```
<!--spring-boot-admin server 包-->
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-server</artifactId>
            <version>2.1.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
```

- 工程的配置文件（application.yml）配置应用名、端口、向注册中心注册的地址，以及暴露actuator的所有端口

```
spring:
  application:
    name: admin-server
server:
  port: 8806
eureka:
  client:
    registryFetchIntervalSeconds: 5 
    service-url:
      defaultZone: http://localhost:8805/eureka/  #注册中心
  instance:
    leaseRenewalIntervalInSeconds: 10 #表示eureka client间隔多久去拉取服务注册信息，默认为30秒,网关应该尽快拉取信息
    health-check-url-path: /actuator/health

management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

- 在工程的启动类AdminServerApplication加上@EnableAdminServer注解，开启admin server的功能，加上@EnableDiscoveryClient注解开启eureka client的功能
```
@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
public class ServerAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerAdminApplication.class, args);
    }

}
```


### Spring Boot Admin Client

- 除了注册中心，把所有的Spring Cloud核心组件都可以写成Spring Boot Admin Client
#### Spring Boot Admin Client 集成实现
- 所需组件包，2.1.0采用webflux，引入webflux的起步依赖，引入eureka-client的起步依赖，并引用actuator的起步依赖
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```
- 工程的配置文件（application.yml）配置应用名、端口、向注册中心注册的地址，以及暴露actuator的所有端口
```
server:
  port: 8840
spring:
  application:
    name: api-feign #feign服务消费者,对外 api
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8805/eureka/ #注册中心
management: #系统暴露eureka-server的actuator的所有端口
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS

#开启Hystrix熔断服务
feign:
  hystrix:
    enabled: true
```
- 在服务启动类中加入发现服务注解@EnableDiscoveryClient
```
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
```
#### Spring Boot Admin Server 集成spring security登录模块
- 集成依赖包
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
```
- 工程的配置文件（application.yml）配置用户名密码，并在服务向注册中心注册时带上metadata-map的信息

```
spring:
  application:
    name: admin-server
  security: #配置spring security的用户名和密码
    user:
      name: admin
      password: admin
server:
  port: 8806
eureka:
  instance:
    metadata-map: # 在服务注册时带上metadata-map的信息,spring security的用户名和密码
      user.name: ${spring.security.user.name}
      user.password: ${spring.security.user.password}
```
- 登录重定向配置类

```
/**
 * @author maoqitian
 * @Description Server admin 登录重定向配置类 输入 账号密码
 * @create 2019-03-29 16:59
 */
@Configuration
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

    private final String adminContextPath;

    public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter( "redirectTo" );

        http.authorizeRequests()
                .antMatchers( adminContextPath + "/assets/**" ).permitAll()
                .antMatchers( adminContextPath + "/login" ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage( adminContextPath + "/login" ).successHandler( successHandler ).and()
                .logout().logoutUrl( adminContextPath + "/logout" ).and()
                .httpBasic().and()
                .csrf().disable();
    }
}
```
#### Spring Boot Admin Server 集成邮件服务
- 加入对应依赖
```
 <!--邮件服务-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```
- 工程的配置文件（application.yml）配置

```
spring:
  application:
    name: admin-server
  security: #配置spring security的用户名和密码
    user:
      name: admin
      password: admin
  mail: # 设置邮件通知
    # QQ
    host: smtp.qq.com
    username: 532977849@qq.com
    password: sdnzlxasnlrzbice # 该密码不是邮箱密码 而是对应的 SMTP服务获取一个16个字符的密码
    port: 587 # 网易 163 不用对应端口号
    default-encoding: UTF-8
    properties:
      # 设置是否需要认证，如果为true,那么用户名和密码就必须的，
      # 如果设置false，可以不设置用户名和密码，当然也得看你的对接的平台是否支持无密码进行访问的。
        mail.smtp.auth: true
      # STARTTLS[1]  是对纯文本通信协议的扩展。它提供一种方式将纯文本连接升级为加密连接（TLS或SSL），而不是另外使用一个端口作加密通信。
        mail.smtp.starttls.enable: true
        mail.smtp.starttls.required: true
        mail.smtp.socketFactory.port: 465
        mail.smtp.socketFactory.class: javax.net.ssl.SSLSocketFactory
        mail.smtp.socketFactory.fallback: false
  boot:
    admin:
      notify:
        mail:
          to: mao.qitian@gxxmt.com,maoqitian068@163.com # 发送给谁
          from: 532977849@qq.com  # 是谁发送出去的
```


## 承载技术 
### 发布：Jenkins
- 本项目Jenkins相关文章
## 容器：Docker
- 本项目相关docker文章
## 容器管理
- 本项目k8s相关文章
## 统一出口负载均衡：Nginx
- 敬请期待


## 参考链接

- [白话SpringCloud | 第四章：服务消费者(RestTemple+Ribbon+Feign)](https://blog.lqdev.cn/2018/09/21/SpringCloud/chapter-four/#Ribbon%E5%AE%9E%E4%BE%8B) 
- https://gitee.com/momoriven/kagome-momo-open-source
- https://gitee.com/shuzheng/zheng
- [SpringCloud微服务框架搭建](https://www.jianshu.com/p/99e73105f201) 
- [官方链接](https://springcloud.cc/spring-cloud-config.html#_spring_cloud_config_server)
- [方志明博客 Spring Cloud 系列 ](https://www.fangzhipeng.com/spring-cloud.html)
- [nacos初探--作为配置中心](https://www.jianshu.com/p/16ff6d6db0cf)
- [拜托！面试请不要再问我Spring Cloud底层原理](https://juejin.im/post/5be13b83f265da6116393fc7)
- [基于Jenkins，docker实现自动化部署（持续交互）](https://www.cnblogs.com/bigben0123/p/7886092.html)
- [Springboot Oauth2 Server 搭建Oauth2认证服务](https://www.jianshu.com/p/b273d53f1c27)
