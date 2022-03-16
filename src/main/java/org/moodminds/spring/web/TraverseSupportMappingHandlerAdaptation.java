package org.moodminds.spring.web;

import org.moodminds.lang.Emittable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitterReturnValueHandler;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * The {@link Emittable} request mapping adaptation configuration bean.
 */
@Configuration
public class TraverseSupportMappingHandlerAdaptation implements InitializingBean {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    /**
     * Construct the configuration object with the specified {@link RequestMappingHandlerAdapter}.
     *
     * @param requestMappingHandlerAdapter the specified {@link RequestMappingHandlerAdapter}
     */
    public TraverseSupportMappingHandlerAdaptation(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    /**
     * Register the {@link Emittable} value handler in the {@link RequestMappingHandlerAdapter}.
     */
    @Override
    public void afterPropertiesSet() {
        ofNullable(this.requestMappingHandlerAdapter.getReturnValueHandlers()).ifPresent(handlers -> handlers.stream()
                .filter(h -> h instanceof ResponseBodyEmitterReturnValueHandler)
                .map(h -> (ResponseBodyEmitterReturnValueHandler) h)
                .findFirst().ifPresent(emitterHandler -> {
                    List<HandlerMethodReturnValueHandler> handlersCustomized = new ArrayList<>(handlers.size() + 1);
                    handlersCustomized.add(new ResponseBodyTraverseSupportReturnValueHandler(emitterHandler));
                    handlersCustomized.addAll(handlers);
                    requestMappingHandlerAdapter.setReturnValueHandlers(handlersCustomized);
                }));
    }
}
