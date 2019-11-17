package com.nepxion.discovery.guide.service.feign;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import io.opentracing.contrib.concurrent.TracedRunnable;
import io.opentracing.util.GlobalTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.guide.service.middleware.MiddlewareOperation;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "discovery-guide-service-a")
public class AFeignImpl extends AbstractFeignImpl implements AFeign {
    private static final Logger LOG = LoggerFactory.getLogger(AFeignImpl.class);

    @Autowired
    private BFeign bFeign;

    @Autowired
    private MiddlewareOperation middlewareOperation;

    @Override
    @SentinelResource(value = "sentinel-resource", blockHandler = "handleBlock", fallback = "handleFallback")
    public String invoke(@PathVariable(value = "value") String value) {
        value = doInvoke(value);
        value = bFeign.invoke(value);

        LOG.info("调用路径：{}", value);

        middlewareOperation.operate();

        return value;
    }

    @Override
    @SentinelResource(value = "sentinel-resource", blockHandler = "handleBlock", fallback = "handleFallback")
    public String invokeAsync(@PathVariable(value = "value") String value) {
        Runnable invokeRunnable = new Runnable() {
            @Override
            public void run() {
                bFeign.invoke(value);

                LOG.info("异步调用...");
            }
        };
        TracedRunnable tracedRunnable = new TracedRunnable(invokeRunnable, GlobalTracer.get());
        new Thread(tracedRunnable).start();

        return "Invoke Async";
    }

    public String handleBlock(String value, BlockException e) {
        return value + "-> A server sentinel block, cause=" + e.getClass().getName() + ", rule=" + e.getRule() + ", limitApp=" + e.getRuleLimitApp();
    }

    public String handleFallback(String value) {
        return value + "-> A server sentinel fallback";
    }
}