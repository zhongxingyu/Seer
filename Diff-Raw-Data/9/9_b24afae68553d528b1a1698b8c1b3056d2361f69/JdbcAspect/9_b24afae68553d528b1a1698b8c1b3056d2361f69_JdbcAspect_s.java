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
 
 import java.sql.Statement;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.AfterThrowing;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 
 import com.github.woozoo73.ht.jdbc.JdbcContext;
 import com.github.woozoo73.ht.jdbc.JdbcInfo;
 import com.github.woozoo73.ht.jdbc.JdbcStatementInfo;
 
 @Aspect
 public class JdbcAspect {
 
 	@Pointcut("within(java.sql.Connection+) && execution(java.sql.PreparedStatement+ prepareStatement(String))")
 	public void prepareStatementPointcut() {
 	}
 
	@Pointcut("within(java.sql.Statement+) && (execution(java.sql.ResultSet+ executeQuery()) || execution(int executeUpdate()))")
 	public void executePointcut() {
 	}
 
 	@Pointcut("within(java.sql.PreparedStatement+) && execution(void java.sql.PreparedStatement+.set*(int, *))")
 	public void setParameterPointcut() {
 	}
 
 	@AfterReturning(pointcut = "prepareStatementPointcut()", returning = "r")
 	public void profilePrepareStatement(JoinPoint joinPoint, Object r) {
 		if (r == null) {
 			return;
 		}
 
 		Statement statement = (Statement) r;
 		JdbcStatementInfo statementInfo = new JdbcStatementInfo();
 		statementInfo.setSql((String) joinPoint.getArgs()[0]);
 
 		JdbcContext.put(statement, statementInfo);
 	}
 
 	@Before("executePointcut()")
 	public void prfileBeforeExecute(JoinPoint joinPoint) {
 		Statement statement = (Statement) joinPoint.getTarget();
 		JdbcStatementInfo statementInfo = JdbcContext.get(statement);
 
 		if (statementInfo == null) {
 			return;
 		}
 
 		statementInfo.setStart(System.nanoTime());
 	}
 
 	@After("executePointcut()")
 	public void prfileAfterExecute(JoinPoint joinPoint) {
 		Statement statement = (Statement) joinPoint.getTarget();
 		JdbcStatementInfo statementInfo = JdbcContext.get(statement);
 
 		if (statementInfo == null) {
 			return;
 		}
 
 		statementInfo.setEnd(System.nanoTime());
 		statementInfo.calculateDuration();
 
 		Invocation invocation = Context.peekFromInvocationStack();
 
 		if (invocation != null) {
 			JdbcInfo jdbcInfo = invocation.getJdbcInfo();
 			if (jdbcInfo == null) {
 				jdbcInfo = new JdbcInfo();
 				invocation.setJdbcInfo(jdbcInfo);
 			}
 
 			jdbcInfo.add(statementInfo);
 			jdbcInfo.fixData();
 		}
 	}
 
 	@AfterThrowing(pointcut = "executePointcut()", throwing = "t")
 	public void prfileAfterThrowingExecute(JoinPoint joinPoint, Throwable t) {
 		Statement statement = (Statement) joinPoint.getTarget();
 		JdbcStatementInfo statementInfo = JdbcContext.get(statement);
 
 		if (statementInfo == null) {
 			return;
 		}
 
 		statementInfo.setThrowableInfo(new ObjectInfo(t));
 	}
 
 	@Before("setParameterPointcut()")
 	public void profileSetParameter(JoinPoint joinPoint) {
 		Statement statement = (Statement) joinPoint.getTarget();
 		JdbcStatementInfo statementInfo = JdbcContext.get(statement);
 
 		Object[] args = joinPoint.getArgs();
 		if (args[0] == null) {
 			return;
 		}
 
 		Integer index = (Integer) args[0];
 		Object value = args[1];
 
 		if (statementInfo != null) {
 			statementInfo.setParameter(index, value);
 		}
 	}
 
 }
