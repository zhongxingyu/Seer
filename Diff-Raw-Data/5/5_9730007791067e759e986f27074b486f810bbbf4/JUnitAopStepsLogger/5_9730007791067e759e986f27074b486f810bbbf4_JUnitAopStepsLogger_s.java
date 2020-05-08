 /*
  * Copyright 2012 CoreMedia AG
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
 
 package net.joala.bdd.aop;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.hamcrest.Description;
 import org.hamcrest.SelfDescribing;
 import org.hamcrest.StringDescription;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * <p>
  * This logger is responsible to log JUnit-based methods using BDD-like naming conventions.
  * </p>
  * <p>
  * The naming conventions for pointcuts are methods which match:
  * </p>
  * <ul>
  * <li>{@code execution(* given_*(..))} or</li>
  * <li>{@code execution(* when_*(..))} or</li>
  * <li>{@code execution(* then_*(..))}</li>
  * </ul>
  * <p>
  * To activate this steps logger, ensure that:
  * </p>
  * <ul>
  * <li>your steps come from a Spring Bean (thus from a class injected to your test),</li>
  * <li>if you use an internal class it must not be final and must be public,</li>
  * <li>that you have a context configuration similar to:
  * <pre>{@code
  * <beans xmlns="http://www.springframework.org/schema/beans"
  *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  *        xmlns:context="http://www.springframework.org/schema/context"
  *        xsi:schemaLocation="http://www.springframework.org/schema/beans
  *            http://www.springframework.org/schema/beans/spring-beans.xsd
  *            http://www.springframework.org/schema/context
  *            http://www.springframework.org/schema/context/spring-context.xsd">
  *  <import resource="classpath:META-INF/joala/bdd/bdd-context.xml"/>
  *  <context:component-scan base-package="your.test.package"/>
  * </beans>
  * }</pre>
  * </li>
  * </ul>
  *
  * @since 6/1/12
  */
 @SuppressWarnings("JavaDoc")
 @Aspect
 public class JUnitAopStepsLogger {
   private static final Logger LOG = LoggerFactory.getLogger(JUnitAopStepsLogger.class);
 
   /**
    * <p>
    * Advisor to log steps.
    * </p>
    *
    * @param joinPoint where we are
    * @return the result of the step call
    * @throws Throwable in case of any error
    */
   @Around("execution(* given_*(..))||execution(* when_*(..))||execution(* then_*(..))")
  public Object logGivenWhenThen(@Nonnull final ProceedingJoinPoint joinPoint) throws Throwable {
     final Description stepDescription = new StringDescription();
     final String stepName = joinPoint.getSignature().getName();
     stepDescription.appendText(stepName.replace('_', ' ').trim());
     describeArguments(stepDescription, joinPoint.getArgs());
     final Object result;
     try {
       LOG.info("{}", stepDescription);
       result = joinPoint.proceed();
    } catch (Throwable throwable) {
       LOG.info("{} (FAILED)", stepDescription);
       throw throwable;
     }
     return result;
   }
 
   /**
    * <p>
    * Describe the step arguments (most likely references) if they are describable. Ignore them if
    * they are not self describable.
    * </p>
    *
    * @param stepDescription description to add description of arguments to
    * @param args            arguments to describe
    */
   private void describeArguments(@Nonnull final Description stepDescription, @Nonnull final Object[] args) {
     final List<Object> argsList = Arrays.asList(args);
     final Collection<Object> describableArgs = Collections2.filter(argsList, new IsSelfDescribing());
     final boolean hasDescribableArgs = !describableArgs.isEmpty();
     if (hasDescribableArgs) {
       stepDescription.appendText(" (");
     }
     for (Iterator<Object> argumentIterator = describableArgs.iterator(); argumentIterator.hasNext(); ) {
       final SelfDescribing describableArg = (SelfDescribing) argumentIterator.next();
       describableArg.describeTo(stepDescription);
       if (argumentIterator.hasNext()) {
         stepDescription.appendText(", ");
       }
     }
     if (hasDescribableArgs) {
       stepDescription.appendText(")");
     }
   }
 
   /**
    * <p>
    * Filter to locate self describing arguments.
    * </p>
    */
   private static class IsSelfDescribing implements Predicate<Object> {
     @Override
     public boolean apply(@Nullable final Object input) {
       return (input instanceof SelfDescribing);
     }
   }
 }
