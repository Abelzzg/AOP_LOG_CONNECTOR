/**
 * Copyright (C) 2014 android10.org. All rights reserved.
 *
 * @author Fernando Cejas (the android10 coder)
 */
package com.zzg.logservice.aspect;

import com.zzg.logservice.internal.StopWatch;
import com.zzg.logservice.utils.LogReceiverUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Aspect representing the cross cutting-concern: Method and Constructor Tracing.
 */
@Aspect
public class TimeAspect {

    private static final String POINTCUT_METHOD =
            "execution(* android.app.Activity.onCreate(..))";
    private static final String POINTCUT_METHOD1 =
            "execution(* android.app.Fragment.onCreateView(..))";


    @Pointcut(POINTCUT_METHOD)
    public void methodAnnotatedWithTimeTrace() {
    }

    @Pointcut(POINTCUT_METHOD1)
    public void methodAnnotatedWithTimeTrace1() {
    }

    @Around("methodAnnotatedWithTimeTrace() || methodAnnotatedWithTimeTrace1()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getName();
        String methodName = methodSignature.getName();
        LogReceiverUtils.generateBehaviorEventlog(buildLogMessage(className,methodName));
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        LogReceiverUtils.generateDuringEventlog(className, methodName, String.valueOf(stopWatch.getTotalTimeMillis()));
        return result;
    }

    /**
     * Create a log message.
     *
     * @param methodName     A string with the method name.
     * @return A string representing message.
     */
    private static String buildLogMessage(String className, String methodName) {
        StringBuilder message = new StringBuilder();
        message.append(className);
        message.append(" --> ");
        message.append(methodName);
        return message.toString();
    }
}
