# maven 项目的启动方式
- 1.直接在IDE 工具里点击运行 
- 2.控制台进入项目目，输入命令来启动，如图所示 
```
mvn spring-boot:run

```
![spring boot 控制台启动方式1](https://github.com/maoqitian/MaoMdPhoto/raw/master/spring%20boot%20%E9%A1%B9%E7%9B%AE%E5%90%AF%E5%8A%A8/spring%20boot%20%E6%8E%A7%E5%88%B6%E5%8F%B0%E5%90%AF%E5%8A%A8%E6%96%B9%E5%BC%8F1.png)  
      
      
  - 3.第三种方式同样控制台下使用命令，如图所示
  
      ```
      先编译项目在项目的target目录生成项目 jar 包
      
      mvn install
      
      然后通过java命令来启动项目
      
      java -jar xxx(项目名)-0.0.1-SNAPSHOT.jar
      
      该命令也可加入参数表明启动的是正式包还是测试包
      
      java -jar xxx(项目名)-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
      

      ```
![编译spring boot jar 包](https://github.com/maoqitian/MaoMdPhoto/raw/master/spring%20boot%20%E9%A1%B9%E7%9B%AE%E5%90%AF%E5%8A%A8/%E7%BC%96%E8%AF%91spring%20boot%20jar%20%E5%8C%85.png)
![通过命令生成jar包](https://github.com/maoqitian/MaoMdPhoto/raw/master/spring%20boot%20%E9%A1%B9%E7%9B%AE%E5%90%AF%E5%8A%A8/%E9%80%9A%E8%BF%87%E5%91%BD%E4%BB%A4%E7%94%9F%E6%88%90%E7%9A%84%20.jar%E5%8C%85.png)
![通过java命令启动jar文件启动项目](https://github.com/maoqitian/MaoMdPhoto/raw/master/spring%20boot%20%E9%A1%B9%E7%9B%AE%E5%90%AF%E5%8A%A8/%E9%80%9A%E8%BF%87java%E5%91%BD%E4%BB%A4%E5%90%AF%E5%8A%A8jar%E6%96%87%E4%BB%B6%E5%90%AF%E5%8A%A8%E9%A1%B9%E7%9B%AE.png)
    
    
    
# Spring Boot
## Controller 的使用
- 注解的使用
   

   注解 | 含义
   ---|---
   @Controller | 处理http请求 
   @RestController | Spring4 之后新加入的注解，等于@Controller + @ResponseBody结合使用
   @RequestMapping | 配置url 映射
   
- 注意：单独 使用Controller则需要使用模板提供 html 文件 比如下面的 index文件，使用官方的thymeleaf
   
   
    注解 | 含义
   ---|---
   @PathVariable | 获取url的数据，直接“/”后面加入要请求的参数获取
   @RequestParam |获取请求参数的值，比较传统，请求地址后加入 ？id=xxx
   @PatchMapping | 组合注解
   
   
```
//@RestController = @Controller + @ResponseBody
//@Controller //单独 使用Controller则需要使用模板提供 html 文件 比如下面的 index文件，使用官方的thymeleaf
//@ResponseBody
@RestController
@RequestMapping(value = "first") //整个controller类设置一个url
public class HelloWorldController {

    //@RequestMapping(value = "/hello",method = RequestMethod.GET)
    @GetMapping(value = "/hello")
    //设置默认值 1
    public String index(@RequestParam(value = "id",required = false,defaultValue = "1") Integer myID){
       return "id:" + myID;
       //return "index";
    }
}
```

## 数据库操作（mysql数据库）
- 使用JPA组件和mysql组件
    
    ```
        <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
		</dependency>

    ```
- 配置数据库，使用yml文件
    
    ```
    application.yml 文件
    spring:
     profiles:
     active: dev #配置当前打包的环境
     datasource:
     driver-class-name: com.mysql.jdbc.Driver #数据库驱动
     url: jdbc:mysql://127.0.0.1:3306/student #数据库地址和操作的表
     username: root #数据库登录用户名
     password: maoqitian  #登录密码
    jpa:
     hibernate:
       ddl-auto: update # create 表示自动创建表,每次重启都会删除旧表，重新生成新的表 update 则只是刷新不会重建
     show-sql: true  #日志显示 sql 语句
     
     开发环境 application-dev.yml
     server:
      port: 8082
      servlet:
       context-path: /mao
     
     生产环境 application-prod.yml
      server:
       port: 8082
      servlet:
       context-path: /mao

    ```
- 创建一个数据库服务接口继承JpaRepository，传入需要操作的对象    
    
    ```
    /**
    * @Author: mao.qitian
    * @Date: 2018/8/2 0002 17:56
    * Student Dao
    */
    public interface StudentRepository extends JpaRepository<Student,Integer>{

    /**
     * @Author maoqitian
     * @Description 通过年龄查找学生
     * @Date 2018/8/3 0003 11:14
     * @Param [age]
     * @return 学生列表
     **/
    public List<Student> findByAge(Integer age);

    /**
     * @Author maoqitian
     * @Description 通过名字查找学生
     * @Date 2018/8/3 0003 11:16
     * @Param [name]
     * @return 查找到的学生
     **/
    public List<Student> findByName(String name);
    }

    ```
- Controller中注入就可以使用它来操作数据库返回我们需要的信息，其他的操作spring-boot已经帮我们实现了
    ```
     @Autowired
     private StudentRepository studentRepository;

    ```


## AOP 面向切面编程
  
- 将通用逻辑从业务逻辑中分离出来
```
     <!--AOP 依赖-->
     <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-aop</artifactId>
		</dependency>

``` 
- 网络请求统一日志处理
     
     ```
     /**
      * @Author: mao.qitian
      * @Date: 2018/8/6 0006 9:51
      * @Description: 使用AOP 统一处理请求日志打印
      */

      @Aspect
      @Component // 该注解为注入spring-boot 容器
      public class HttpAspect {

      private static final Logger logger= LoggerFactory.getLogger(HttpAspect.class);

      /**
       * @Author maoqitian
       * @Description 公共的处理方法，定位拦截的类和方法
       * @Date 2018/8/6 0006 10:04
       * @Param []
       * @return void
       **/
       @Pointcut("execution(public *  com.mao.springdemo.demo.controller.StudentController.*(..))")
       //所以方法都有拦截http网络日志
       public void log(){
       }


        @Before("log()")
        public void doBefore(JoinPoint joinPoint){
        ServletRequestAttributes servletRequestAttributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        //IP
        logger.info("url={}",request.getRemoteAddr());
        //url
        logger.info("url={}",request.getRequestURL());
        //method
        logger.info("method={}",request.getMethod());
        //请求类的方法
        logger.info("class_method={}",joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName());
        //参数
        logger.info("args={}",joinPoint.getArgs().toString());
        }

        @After("log()")
        public void doAfter(){
        logger.info("方法走完");
        }

        //打印返回参数
        @AfterReturning(returning = "object",pointcut = "log()")
        public void doAfterReturning(Object object){
         //logger.info("response={}",object.toString());
        }
      }

     ```

## 数据返回格式、异常处理统一处理
  
- 首先创建我们需要返回的数据统一格式类对象
    
    ```
    /**
    * @Author: mao.qitian
    * @Date: 2018/8/6 0006 15:55
    * @Description: 统一 返回 对象
    */
    public class Result<T>{

    /*返回数据*/
    private T data;
    /*返回信息*/
    private String message;
    /*错误码*/
    private int code;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
      }
    }

    ```
- 创建枚举类，对返回的数据信息和状态码做统一配置处理
    
    ```
      /**
       * @Author: mao.qitian
       * @Date: 2018/8/6 0006 17:31
       * @Description: 自定义返回信息枚举
       */
       public enum ResultEnum {
        UNKNOW_ERROR(-1,"未知错误"),
        FAIL(1,"数据获取失败"),
        SUCCESS(0,"数据获取成功"),
        PRIMARY_SCHOOL(100,"你是小学生，无法进行注册"),
        MIDDLE_SCHOOL(101,"你是中学生，无法进行注册")
        ;


        private Integer code;

        private String message;

        ResultEnum(Integer code, String message){
        this.code=code;
        this.message=message;
        }

        public Integer getCode() {
        return code;
        }

        public String getMessage() {
         return message;
         }
        }
  
    ```
- 出现异常，我们也希望能打印一样的数据格式，所以我们可以写自己的异常类，并且继承运行时异常，这样spring-boot才能捕获异常
    
    ```
    /**
     * @Author: mao.qitian
     * @Date: 2018/8/6 0006 16:26
     * @Description: 自定义异常类
     */
    public class StudentException extends RuntimeException{

    private int code;
    public StudentException(ResultEnum resultEnum){
        super(resultEnum.getMessage());
        this.code= resultEnum.getCode();
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
      }
    }

    ```
- 捕获异常类
    ```
    /**
     * @Author: mao.qitian@gxxmt.com
     * @Date: 2018/8/6 0006 16:19
     * @Description: 异常捕获类
     */
    @ControllerAdvice
    public class MyExceptionHandle  {

    private static final Logger logger= LoggerFactory.getLogger(HttpAspect.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result handle(Exception e){
        if(e instanceof StudentException){
           StudentException studentException=(StudentException)e;
           return ResultUtils.error(studentException.getMessage(),ResultEnum.FAIL);
        }else {
            logger.error("【系统异常】",e);
            return ResultUtils.error("未知错误",ResultEnum.UNKNOW_ERROR);
        }

      }
    }
    ```
- 最后是数据统一返回的工具类
     
    ```
    /**
      * @Author: mao.qitian@gxxmt.com
      * @Date: 2018/8/6 0006 16:03
      * @Description: 数据返回处理工具类
      */
     public class ResultUtils {


      public static Result success(Object obj, ResultEnum resultEnum){
        Result result=new Result();
        result.setCode(resultEnum.getCode());
        result.setMessage(resultEnum.getMessage());
        result.setData(obj);
        return result;
      }

      //没有返回数据的情况
      public static Result success(){
       return success(null,ResultEnum.SUCCESS);
      }


      public static Result error(String message,ResultEnum resultEnum){
        Result result=new Result();
        result.setMessage(message);
        result.setCode(resultEnum.getCode());
        return result;
      }
     }
    ```
- 如何使用
          
    ```
    成功的返回 
     ResultUtils.success(studentRepository.save(student), ResultEnum.SUCCESS);
    
    获取数据失败
    ResultUtils.error(bindingResult.getFieldError().getDefaultMessage(),ResultEnum.FAIL);
    
    服务器出现异常直接使用自定义异常类抛出
    throw new StudentException(ResultEnum.PRIMARY_SCHOOL);

    ```


    





   
   
