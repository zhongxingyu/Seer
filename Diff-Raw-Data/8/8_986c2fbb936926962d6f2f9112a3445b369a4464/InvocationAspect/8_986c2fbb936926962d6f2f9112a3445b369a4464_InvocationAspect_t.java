 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.woozoo73.ht;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.AfterThrowing;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 
 import com.github.woozoo73.ht.filter.ClassFilter;
 import com.github.woozoo73.ht.filter.Filter;
 import com.github.woozoo73.ht.format.DefaultFormat;
 import com.github.woozoo73.ht.format.Format;
 import com.github.woozoo73.ht.writer.LogWriter;
 import com.github.woozoo73.ht.writer.Writer;
 
 /**
  * Invocation aspect.
  * 
  * @author woozoo73
  */
 @Aspect
 public class InvocationAspect {
 
 	private Writer writer;
 
 	private Filter filter;
 
 	public InvocationAspect() {
 		initWriter();
 		initFilter();
 	}
 
 	private void initWriter() {
 		try {
 			String formatName = System.getProperty("ht.format", DefaultFormat.class.getName());
 			Format format = (Format) Class.forName(formatName).newInstance();
 
 			String witerName = System.getProperty("ht.writer", LogWriter.class.getName());
 			Writer writer = (Writer) Class.forName(witerName).newInstance();
 			writer.setFormat(format);
 
 			this.writer = writer;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void initFilter() {
 		try {
 			String filterName = System.getProperty("ht.filter", ClassFilter.class.getName());
 			this.filter = (Filter) Class.forName(filterName).newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Pointcut("!within(com.github.woozoo73.ht..*) && !cflow(call(String *.toString())) && !within(java.sql..*+) && !cflow(call(* java.sql..*+.*(..)))")
 	public void excludePointcut() {
 	}
 
 	@Pointcut("excludePointcut() && execution(* *.*(..))")
 	public void executionPointcut() {
 	}
 
 	@Pointcut("excludePointcut() && initialization(*.new(..))")
 	public void initializationPointcut() {
 	}
 
 	@Pointcut("executionPointcut() || initializationPointcut()")
 	public void profilePointcut() {
 	}
 
 	@Before("profilePointcut()")
	public void profileBefore(JoinPoint joinPoint) {
 		Invocation endpointInvocation = Context.getEndpointInvocation();
 		if (endpointInvocation == null && !filter.accept(joinPoint)) {
			return;
 		}
 
 		Invocation invocation = new Invocation();
 		invocation.setJoinPoint(joinPoint);
 		invocation.setJoinPointInfo(new JoinPointInfo(joinPoint));
 
 		if (endpointInvocation == null) {
 			Context.setEndpointInvocation(invocation);
 		}
 
 		Invocation currentInvocation = Context.peekFromInvocationStack();
 		if (currentInvocation != null) {
 			currentInvocation.add(invocation);
 		}
 
 		Context.addToInvocationStack(invocation);
 
 		invocation.start();
 	}
 
 	@After("profilePointcut()")
 	public void profileAfter(JoinPoint joinPoint) {
 		Invocation invocation = getInvocation(joinPoint);
 		if (invocation == null) {
 			return;
 		}
 
 		invocation.stop();
 
 		Context.popFromInvocationStack();
 
 		if (invocation.equalsJoinPoint(Context.getEndpointInvocation())) {
 			Invocation i = Context.dump();
 
 			writer.write(i);
 		}
 	}
 
 	@AfterThrowing(pointcut = "profilePointcut()", throwing = "t")
 	public void profileAfterThrowing(JoinPoint joinPoint, Throwable t) {
 		Invocation invocation = getInvocation(joinPoint);
 		if (invocation == null) {
 			return;
 		}
 
 		invocation.setThrowableInfo(new ObjectInfo(t));
 	}
 
 	@AfterReturning(pointcut = "profilePointcut()", returning = "r")
 	public void profileAfterReturning(JoinPoint joinPoint, Object r) {
 		Invocation invocation = getInvocation(joinPoint);
 		if (invocation == null) {
 			return;
 		}
 
 		invocation.setReturnValueInfo(new ObjectInfo(r));
 	}
 
 	private Invocation getInvocation(JoinPoint joinPoint) {
 		Invocation endpointInvocation = Context.getEndpointInvocation();
 		if (endpointInvocation == null) {
 			return null;
 		}
 
 		Invocation invocation = endpointInvocation.getInvocationByJoinPoint(joinPoint);
 		if (invocation == null) {
 			return null;
 		}
 
 		return invocation;
 	}
 
 }
