package com.nepxion.discovery.guide.zuul.impl;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.strategy.zuul.tracer.DefaultZuulStrategyTracer;
import com.netflix.zuul.context.RequestContext;

// 自定义调用链和灰度调用链输出到Zipkin
public class MyZuulStrategyZipkinTracer extends DefaultZuulStrategyTracer {
    private static final Logger LOG = LoggerFactory.getLogger(MyZuulStrategyZipkinTracer.class);

    @Autowired
    private Tracer tracer;

    private Span span;

    @Override
    public void trace(RequestContext context) {
        super.trace(context);

        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(DiscoveryConstant.GATEWAY_TYPE);
        span = spanBuilder.start();

        // 自定义调用链
        span.setTag(Tags.COMPONENT.getKey(), DiscoveryConstant.DISCOVERY_NAME);
        span.setTag("mobile", strategyContextHolder.getHeader("mobile"));
        span.setTag("user", strategyContextHolder.getHeader("user"));

        // 灰度路由调用链
        span.setTag(DiscoveryConstant.N_D_SERVICE_GROUP, strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_GROUP));
        span.setTag(DiscoveryConstant.N_D_SERVICE_TYPE, strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_TYPE));
        span.setTag(DiscoveryConstant.N_D_SERVICE_ID, strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_ID));
        span.setTag(DiscoveryConstant.N_D_SERVICE_ADDRESS, strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_ADDRESS));
        span.setTag(DiscoveryConstant.N_D_SERVICE_VERSION, strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_VERSION));
        span.setTag(DiscoveryConstant.N_D_SERVICE_REGION, strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_REGION));

        LOG.info("全链路灰度调用链输出到Zipkin");
    }

    @Override
    public void release(RequestContext context) {
        if (tracer != null && span != null) {
            span.finish();
        }

        LOG.info("全链路灰度调用链Zipkin上下文清除");
    }
}