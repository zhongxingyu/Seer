 /**
  * Copyright (c) 2009-2011 Zauber S.A. <http://www.zaubersoftware.com/>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.zaubersoftware.leviathan.api.engine.impl.pipe;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.UnhandledException;
 import org.apache.commons.lang.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ar.com.zauber.commons.dao.Closure;
 
 import com.zaubersoftware.leviathan.api.engine.ExceptionHandler;
 import com.zaubersoftware.leviathan.api.engine.Pipe;
 import com.zaubersoftware.leviathan.api.engine.impl.PipeExceptionResolver;
 
 /**
  * A {@link Pipe} that builds the flow of pipes and dispatch exception to the registered {@link ExceptionHandler}s
  *
  * @param <I>
  * @param <O>
  * @author Guido Marucci Blas
  * @since Aug 12, 2011
  */
 public final class FlowBuilderPipe<I, O> implements Pipe<I, O> {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private final Iterable<Pipe<?, ?>> pipes;
 
     private ExceptionHandler defaultExceptionHandler = new ExceptionHandler() {
         @Override
         public void handle(final Throwable trowable) {
             FlowBuilderPipe.this.logger.error("No one is willing to handle this exception. It will blow up!!!!!", 
                     trowable);
             throw new UnhandledException(trowable);
         }
     };
 
     private final Map<Class<? extends Throwable>, ExceptionHandler> handlers =
         new HashMap<Class<? extends Throwable>, ExceptionHandler>();
     private final Map<Pipe<?, ?>, PipeExceptionResolver> pipeExceptionResolvers =
         new HashMap<Pipe<?, ?>, PipeExceptionResolver>();
 
     /**
      * Creates the FlowBuilderPipe.
      *
      * @param pipes The pipe to be assembled into a {@link Closure}
      * @param handlers The exception handlers.
      */
     public FlowBuilderPipe(
             final Collection<Pipe<?, ?>> pipes,
             final Map<Class<? extends Throwable>, ExceptionHandler> handlers,
             final Map<Pipe<?, ?>, PipeExceptionResolver> pipeExceptionResolvers) {
         Validate.noNullElements(pipes, "The pipes cannot be nul nor contain null elements");
         Validate.notEmpty(pipes, "Pipes cannot be empty");
 
         this.pipes = pipes;
         if (handlers != null) {
             this.handlers.putAll(handlers);
         }
         if (this.pipeExceptionResolvers != null && pipeExceptionResolvers != null) {
             this.pipeExceptionResolvers.putAll(pipeExceptionResolvers);
         }
     }
 
     /**
      * Creates the CompositePipe.
      *
      */
     public FlowBuilderPipe(final Collection<Pipe<?, ?>> pipes) {
         this(pipes, null, null);
     }
 
     /**
      * Creates the CompositePipe.
      *
      */
     public FlowBuilderPipe(final Collection<Pipe<?, ?>> pipes, 
                            final Map<Class<? extends Throwable>, ExceptionHandler> handlers) {
         this(pipes, handlers, null);
     }
 
     @Override
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public O execute(final I input) {
         Object ret = input;
         for (final Pipe pipe : this.pipes) {
             try {
                 ret = pipe.execute(ret);
             } catch(final Throwable e) {
                 this.logger.error("There was an error in the pipe chain execution", e);
 
                 // Serching for exception handlers for this pipe if not delegates to engine exception handlers
                 if (this.pipeExceptionResolvers.containsKey(pipe)) {
                     final PipeExceptionResolver resolver = this.pipeExceptionResolvers.get(pipe);
                     if (resolver.getHandlers().containsKey(e.getClass())) {
                         resolver.getHandlers().get(e.getClass()).handle(e);
                         return null;
                     } else if (resolver.hasDefaultHandler()) {
                         resolver.getDefaultHandler().handle(e);
                         return null;
                    } else if (this.handlers.containsKey(e.getClass())) {
                        this.handlers.get(e.getClass()).handle(e);
                        this.logger.info("The pipe's flow has been stopped");
                        return null;
                    } else {
                        this.defaultExceptionHandler.handle(e);
                        return null;
                     }
                 } else if (this.handlers.containsKey(e.getClass())) {
                     this.handlers.get(e.getClass()).handle(e);
                     this.logger.info("The pipe's flow has been stopped");
                     return null;
                 } else {
                     this.defaultExceptionHandler.handle(e);
                     return null;
                 }
             }
         }
         return (O) ret;
     }
 
     /**
      * Sets the defaultExceptionHandler.
      *
      * @param defaultExceptionHandler <code>ExceptionHandler<Throwable></code> with the defaultExceptionHandler.
      */
     public void setDefaultExceptionHandler(final ExceptionHandler defaultExceptionHandler) {
         Validate.notNull(defaultExceptionHandler, "The default exception handler cannot be null");
         this.defaultExceptionHandler = defaultExceptionHandler;
     }
 
 }
