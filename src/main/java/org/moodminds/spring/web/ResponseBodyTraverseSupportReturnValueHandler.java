package org.moodminds.spring.web;

import org.moodminds.lang.Emittable;
import org.moodminds.lang.Traversable;
import org.moodminds.lang.TraverseSupport;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitterReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.moodminds.util.Cast.cast;
import static org.springframework.core.ResolvableType.forMethodParameter;

/**
 * The {@link Emittable} and {@link Traversable} registration
 * in the {@link ResponseBodyEmitterReturnValueHandler} configuration bean.
 */
public class ResponseBodyTraverseSupportReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final ResponseBodyEmitterReturnValueHandler emitterReturnValueHandler;

    /**
     * Construct the configuration object with the specified {@link ResponseBodyEmitterReturnValueHandler}.
     *
     * @param emitterReturnValueHandler the specified {@link ResponseBodyEmitterReturnValueHandler}
     */
    public ResponseBodyTraverseSupportReturnValueHandler(ResponseBodyEmitterReturnValueHandler emitterReturnValueHandler) {
        this.emitterReturnValueHandler = emitterReturnValueHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> bodyType = ResponseEntity.class.isAssignableFrom(returnType.getParameterType()) ?
                forMethodParameter(returnType).getGeneric().resolve() :
                returnType.getParameterType();
        return Emittable.class.equals(bodyType) || Traversable.class.equals(bodyType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        ResponseBodyEmitter emitter = new SseEmitter();
        boolean isResponseEntity = returnValue instanceof ResponseEntity;
        ResponseEntity<? extends TraverseSupport<?, ?>> responseEntity = isResponseEntity ? cast(returnValue) : null;
        TraverseSupport<?, ?> traverseSupport = isResponseEntity ? responseEntity.getBody() : (TraverseSupport<?, ?>) returnValue;
        emitterReturnValueHandler.handleReturnValue(traverseSupport == null ? null : isResponseEntity
                        ? new ResponseEntity<>(emitter, responseEntity.getHeaders(), responseEntity.getStatusCode())
                        : emitter, returnType, mavContainer, webRequest);
        if (traverseSupport != null) newSingleThreadExecutor().execute(() -> {
            try {
                traverseSupport.traverse(t -> t.each(event -> emitter.send(requireNonNull(event))));
                emitter.complete();
            } catch(Throwable e) {
                emitter.completeWithError(e);
            }
        });
    }
}
