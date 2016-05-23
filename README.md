# AOP_LOG_CONNECTOR

#应用分析--日志采集
##传统的应用信息采集

	数据采集一般都离不开埋点插桩，但是大多数采集功能都需要手动地插入代码，标识某段操作的起点和终点。例如信息流的下拉刷新，工程师可以在第一个触摸事件时插入开始标记，然后在屏幕更新后插入结束标记。这种方法和Android Systrace工具所提供的功能类似。
	
	友盟的收集方式，都是通过调用代码来实现：

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	MobclickAgent.onProfileSignIn("userID");
	
	MobclickAgent.onProfileSignIn("WB"，"userID");
	
	甚至连错误统计都需要开发人员去主动调用代码
	MobclickAgent.setCatchUncaughtExceptions(false); 
	
	public static void reportError(Context context, String error)   
	//或  
	public static void reportError(Context context, Throwable e)
	
	这种方法有诸多弊端：

- 开发者插桩的粒度决定了数据的详细程度，并导致这种方法只能检测可以预见的性能影响。比如，网络连接和响应的渲染通常会被列入数据采集需求中，但其中可能触发磁盘写入，而这个会严重拖慢速度，但你不一定会想起收集数据；
- Android应用经常使用多线程编程，用户交互的高度异步特点，导致很难彻底检测代码。你必须给应用中的子线程分配唯一识别符以保证数据的完整，而异步让采集器的回收变得困难；
- 最后，一般应用的开发速度很快，代码变更很频繁，这让插入的标记也必须经常变动，而且手动插入性能检测点非常耗时且容易出错。工程师的时间不应该花费在可以自动化的事情上。而且，在一个不断变化的代码库中，确保这类检测点的正确性需要做大量的工作。

![image](http://)

##在android采用AOP切面技术采集数据
参考了github上AOP在Android实现切面的项目[AOPforAndroid](http://fernandocejas.com/2014/08/03/aspect-oriented-programming-in-android/)。

这个项目旨在解决以上代码侵入和维护困难等问题，使用了aspect的一个gradle插件，和aspect用于向android框架织入切点的jar包，gradle配置如下：

	import com.android.build.gradle.LibraryPlugin
	import org.aspectj.bridge.IMessage
	import org.aspectj.bridge.MessageHandler
	import org.aspectj.tools.ajc.Main
	 
	buildscript {
	  repositories {
    	mavenCentral()
	  }
	  dependencies {
    	classpath 'com.android.tools.build:gradle:0.12.+'
    	classpath 'org.aspectj:aspectjtools:1.8.1'
	  }
	}
 
	apply plugin: 'android-library'
 
	repositories {
	  mavenCentral()
	}
	 
	dependencies {
	  compile 'org.aspectj:aspectjrt:1.8.1'
	}
	 
	android {
	  compileSdkVersion 19
	  buildToolsVersion '19.1.0'
	 
	  lintOptions {
	    abortOnError false
	  }
	}
 
	android.libraryVariants.all { variant ->
	  LibraryPlugin plugin = project.plugins.getPlugin(LibraryPlugin)
	  JavaCompile javaCompile = variant.javaCompile
	  javaCompile.doLast {
	    String[] args = ["-showWeaveInfo",
	                     "-1.5",
	                     "-inpath", javaCompile.destinationDir.toString(),
	                     "-aspectpath", javaCompile.classpath.asPath,
	                     "-d", javaCompile.destinationDir.toString(),
	                     "-classpath", javaCompile.classpath.asPath,
	                     "-bootclasspath", plugin.project.android.bootClasspath.join(File.pathSeparator)]
 
      MessageHandler handler = new MessageHandler(true);
      new Main().run(args, handler)
 
      def log = project.logger
      for (IMessage message : handler.getMessages(null, true)) {
        switch (message.getKind()) {
          case IMessage.ABORT:
          case IMessage.ERROR:
          case IMessage.FAIL:
            log.error message.message, message.thrown
            break;
          case IMessage.WARNING:
          case IMessage.INFO:
            log.info message.message, message.thrown
            break;
          case IMessage.DEBUG:
            log.debug message.message, message.thrown
            break;
        }
       }
	  }
	}

在项目中暂时只提供三种切面方式作为示例，大家可以根据自己项目需求定制自己的切面方式。

#####BehaviorAspect--用户行为


	private static final String POINTCUT_METHOD1 ="execution(* android.view.View.OnClickListener .*(..))";

	private static final String POINTCUT_METHOD0 ="execution(* android.view.View.OnLongClickListener .*(..))";

	private static final String POINTCUT_METHOD2 ="execution(* android.app.Activity.onResume(..))";

	private static final String POINTCUT_METHOD3 ="execution(* android.app.Activity.onPause(..))";


#####CustomerAspect--定制切面

定制切面，首先需要注册一个注解提供给开发人员使用：

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
	public @interface CustomerTrace {
	    String eventId();
	    String eventName();
	}

然后切点指向注解标注的方法：

	private static final String POINTCUT_METHOD = 	"execution(@com.abel.logservice.annotation.CustomerTrace * *(..))";

	private static final String POINTCUT_CONSTRUCTOR = 	"execution(@com.abel.logservice.annotation.CustomerTrace *.new(..))";

#####TimeAspect--应用耗时行为切面

应用耗时行为切面，环切切点的方法，记录方法调用使用的时间：

	private static final String POINTCUT_METHOD = "execution(* android.app.Activity.onCreate(..))";
            
	private static final String POINTCUT_METHOD1 = "execution(* android.app.Fragment.onCreateView(..))";


#####日志发送记录模板









