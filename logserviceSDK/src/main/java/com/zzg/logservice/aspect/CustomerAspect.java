package com.zzg.logservice.aspect;

import com.zzg.logservice.annotation.CustomerTrace;
import com.zzg.logservice.utils.LogReceiverUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Aspect representing the cross cutting-concern: Method and Constructor Tracing.
 */
@Aspect
public class CustomerAspect {

  private static final String POINTCUT_METHOD =
      "execution(@com.yeepay.logservice.annotation.CustomerTrace * *(..))";


  private static final String POINTCUT_CONSTRUCTOR =
      "execution(@com.yeepay.logservice.annotation.CustomerTrace *.new(..))";
  private String eventName;
  private String eventId;

  @Pointcut(POINTCUT_METHOD)
  public void methodAnnotatedWithCustomerTrace() {}

  @Pointcut(POINTCUT_CONSTRUCTOR)
  public void constructorAnnotatedCustomerTrace() {}

  @Before("methodAnnotatedWithCustomerTrace() || constructorAnnotatedCustomerTrace()")
  public void weaveJoinPoint(JoinPoint joinPoint)  {

    Method[] declaredMethods = joinPoint.getTarget().getClass().getDeclaredMethods();
    for (Method method : declaredMethods) {
      CustomerTrace customerTrace = method.getAnnotation(CustomerTrace.class);
      if(customerTrace != null) {
        eventName= customerTrace.eventName();
        eventId=customerTrace.eventId();
        LogReceiverUtils.generateCountEventlog(eventName,eventId);
      }
    }
  }

  /**
   * Create a log message.
   *
   * @param methodName A string with the method name.
   * @param methodDuration Duration of the method in milliseconds.
   * @return A string representing message.
   */
  private static String buildLogMessage(String methodName, long methodDuration) {
    StringBuilder message = new StringBuilder();
    message.append("Yeepay --> ");
    message.append(methodName);
    message.append(" --> ");
    message.append("[");
    message.append(methodDuration);
    message.append("ms");
    message.append("]");

    return message.toString();
  }
}
