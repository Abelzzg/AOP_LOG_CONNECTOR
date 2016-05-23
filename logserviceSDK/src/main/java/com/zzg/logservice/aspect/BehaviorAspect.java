/**
 * Copyright (C) 2014 android10.org. All rights reserved.
 *
 * @author Fernando Cejas (the android10 coder)
 */
package com.zzg.logservice.aspect;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;

import com.zzg.logservice.utils.LogReceiverUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Aspect representing the cross cutting-concern: Method and Constructor Tracing.
 */
@Aspect
public class BehaviorAspect {

    private static final String POINTCUT_METHOD1 =
            "execution(* android.view.View.OnClickListener .*(..))";

    private static final String POINTCUT_METHOD0 =
            "execution(* android.view.View.OnLongClickListener .*(..))";

    private static final String POINTCUT_METHOD2 =
            "execution(* android.app.Activity.onResume(..))";

    private static final String POINTCUT_METHOD3 =
            "execution(* android.app.Activity.onPause(..))";

    private static final String POINTCUT_METHOD4 =
            "execution(* android.support.v4.app.Fragment.onResume(..))";

    private static final String POINTCUT_METHOD5 =
            "execution(* android.support.v4.app.Fragment.onPause(..))";


    @Pointcut(POINTCUT_METHOD1)
    public void methodAnnotatedWithCLickBehaviorTrace() {
    }

    @Pointcut(POINTCUT_METHOD0)
    public void methodAnnotatedWithLongClickBehaviorTrace() {
    }

    @Pointcut(POINTCUT_METHOD2)
    public void methodAnnotatedWithBehaviorTraceOnResume() {
    }

    @Pointcut(POINTCUT_METHOD3)
    public void methodAnnotatedWithBehaviorTraceOnPause() {
    }

    @Pointcut(POINTCUT_METHOD4)
    public void methodAnnotatedWithBehaviorTraceFragmentOnResume() {
    }

    @Pointcut(POINTCUT_METHOD5)
    public void methodAnnotatedWithBehaviorTraceFragmentOnPause() {
    }


    @Before("methodAnnotatedWithBehaviorTraceOnResume() || methodAnnotatedWithBehaviorTraceOnPause() || methodAnnotatedWithBehaviorTraceFragmentOnResume || methodAnnotatedWithBehaviorTraceFragmentOnPause")
    public void weaveJoinPointForActivty(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getName();
        String methodName = methodSignature.getName();
        LogReceiverUtils.generateBehaviorEventlog(buildLogMessage(className, methodName));
    }

    @Before("methodAnnotatedWithCLickBehaviorTrace() ")
    public void weaveJoinPointForClick(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        View view = (View) args[0];
        while (view.getId() == -1) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                view = (View) parent;
            }
        }
        String id = view.getResources().getResourceName(view.getId());
//        id = id.substring(id.lastIndexOf("/") + 1);
//        LogReceiverUtils.generateBehaviorEventlog(buildLogMessage(id, "click"));
    }

    @Before("methodAnnotatedWithLongClickBehaviorTrace() ")
    public void weaveJoinPointForLongClick(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        View view = (View) args[0];
        while (view.getId() == -1) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                view = (View) parent;
            }
        }
        String id = view.getResources().getResourceName(view.getId());
//        id = id.substring(id.lastIndexOf("/") + 1);
//        LogReceiverUtils.generateBehaviorEventlog(buildLogMessage(id, "long_click"));
    }

    /**
     * Create a log message.
     *
     * @param methodName A string with the method name.
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
