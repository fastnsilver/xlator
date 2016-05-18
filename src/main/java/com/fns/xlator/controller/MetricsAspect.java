package com.fns.xlator.controller;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricsAspect {

    private static final String TOTAL_REQUESTS = "requests.total";

    @Autowired
    private MetricRegistry metricRegistry;

    @Around("com.fns.xlator.controller.Pointcuts.inTranslationController()")
    public Object collectMetrics(ProceedingJoinPoint pjp) throws Throwable {

        String curClass = pjp.getTarget().getClass().getName();

        // overall timer
        Timer timerOverall = metricRegistry.timer(MetricRegistry.name(curClass, "overall"));
        Timer.Context contextOverall = timerOverall.time();

        // timer for each individual method call
        Timer timer = metricRegistry.timer(MetricRegistry.name(curClass, pjp.getSignature().toLongString()));
        Timer.Context context = timer.time();

        metricRegistry.meter(MetricRegistry.name(curClass, TOTAL_REQUESTS, "rate")).mark();

        try  {
            Object result = pjp.proceed();
            metricRegistry.meter(MetricRegistry.name(curClass, TOTAL_REQUESTS, "success", "rate")).mark();
            return result;
        } catch (Throwable t){
            metricRegistry.meter(MetricRegistry.name(curClass, TOTAL_REQUESTS, "failure", "rate")).mark();
            throw t;
        } finally {
            contextOverall.stop();
            context.stop();
        }
    }

}
