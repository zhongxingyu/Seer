 package org.flite.cach3.aop;
 
 import net.spy.memcached.*;
 import org.apache.commons.lang.*;
 import org.apache.velocity.*;
 import org.apache.velocity.app.*;
 import org.aspectj.lang.*;
 import org.aspectj.lang.annotation.*;
 import org.flite.cach3.annotations.*;
 import org.flite.cach3.api.*;
 import org.slf4j.*;
 import org.springframework.core.*;
 import org.springframework.core.annotation.*;
 
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 
 /**
 Copyright (c) 2011 Flite, Inc
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
  */
 @Aspect
 @Order(Ordered.HIGHEST_PRECEDENCE / 2)
 public class ReadThroughSingleCacheAdvice extends CacheBase {
 	private static final Logger LOG = LoggerFactory.getLogger(ReadThroughSingleCacheAdvice.class);
 
 	@Pointcut("@annotation(org.flite.cach3.annotations.ReadThroughSingleCache)")
 	public void getSingle() {}
 
 	@Around("getSingle()")
 	public Object cacheSingle(final ProceedingJoinPoint pjp) throws Throwable {
         // If we've disabled the caching programmatically (or via properties file) just flow through.
         if (isCacheDisabled()) {
             LOG.debug("Caching is disabled.");
             return pjp.proceed();
         }
 
         final MemcachedClientIF cache = getMemcachedClient();
 		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
 		// but do not let it surface up past the AOP injection itself.
         final String baseKey;
 		final String cacheKey;
 		final ReadThroughSingleCache annotation;
         final AnnotationData annotationData;
         final Object[] args = pjp.getArgs();
 		try {
 			final Method methodToCache = getMethodToCache(pjp);
 			annotation = methodToCache.getAnnotation(ReadThroughSingleCache.class);
             annotationData =
                     AnnotationDataBuilder.buildAnnotationData(annotation,
                             ReadThroughSingleCache.class,
                             methodToCache.getName());
 			baseKey = generateBaseKeySingle(args, annotationData, methodToCache.toString());
             cacheKey = buildCacheKey(baseKey, annotationData);
 			final Object result = cache.get(cacheKey);
 			if (result != null) {
 				LOG.debug("Cache hit.");
 				return (result instanceof PertinentNegativeNull) ? null : result;
 			}
 		} catch (Throwable ex) {
 			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
 			return pjp.proceed();
 		}
 
 		final Object result = pjp.proceed();
 
 		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
 		// but do not let it surface up past the AOP injection itself.
 		try {
 			final Object submission = (result == null) ? new PertinentNegativeNull() : result;
 			cache.set(cacheKey, annotation.expiration(), submission);
 
             // Notify the observers that a cache interaction happened.
             final List<ReadThroughSingleCacheListener> listeners = getPertinentListeners(ReadThroughSingleCacheListener.class,annotationData.getNamespace());
             if (listeners != null && !listeners.isEmpty()) {
                 for (final ReadThroughSingleCacheListener listener : listeners) {
                     try {
                         listener.triggeredReadThroughSingleCache(annotationData.getNamespace(), annotationData.getKeyPrefix(), baseKey, result, args);
                     } catch (Exception ex) {
                         LOG.warn("Problem when triggering a listener.", ex);
                     }
                 }
             }
 		} catch (Throwable ex) {
 			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
 		}
 		return result;
 	}
 
 	protected String getObjectId(final Object keyObject) throws Exception {
 		final Method keyMethod = getKeyMethod(keyObject);
 		return generateObjectId(keyMethod, keyObject);
 	}
 
     protected String generateBaseKeySingle(final Object[] args,
                                            final AnnotationData annotationData,
                                            final String methodString) throws Exception {
         if (StringUtils.isBlank(annotationData.getKeyTemplate())) {
             return getObjectId(getIndexObject(annotationData.getKeyIndex(), args, methodString));
         }
 
         try {
             final VelocityContext context = factory.getNewExtendedContext();
             context.put("args", args);
 
             final StringWriter writer = new StringWriter(250);
             Velocity.evaluate(context, writer, this.getClass().getSimpleName(), annotationData.getKeyTemplate());
            return writer.toString();
         } catch (Exception ex) {
             if (LOG.isInfoEnabled()) {
                 final StringBuilder sb = new StringBuilder("\nArgs: ")
                         .append(args == null ? "null" : args.length).append("\n");
                 if (args != null && args.length > 0) {
                     for (int ix = 0; ix < args.length; ix++) {
                         sb.append("Arg[").append(ix).append("]: ").append(args[ix] == null ? "null" : args[ix].toString()).append("\n");
                     }
                 }
                 sb.append("Template: \"").append(annotationData.getKeyTemplate()).append("\"");
                 LOG.info(sb.toString(), ex);
             }
             throw ex;
         }
     }
 
 }
