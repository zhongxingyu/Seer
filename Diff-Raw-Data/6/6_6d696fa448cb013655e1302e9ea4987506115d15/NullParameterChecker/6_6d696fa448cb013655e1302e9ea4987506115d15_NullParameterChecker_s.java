 /*
  * Copyright 2011 Ritz, Bruno <bruno.ritz@gmail.com>
  *
  * This file is part of S-Plan.
  *
  * S-Plan is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * S-Plan is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with S-Plan. If not, see
  * http://www.gnu.org/licenses/.
  */
 package org.splan.utils.validation;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.Signature;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.reflect.ConstructorSignature;
 import org.aspectj.lang.reflect.MethodSignature;
 import org.splan.utils.exceptions.InvalidArgumentException;
 import org.splan.utils.exceptions.InvalidArgumentException.Reason;
 
 /**
 * An aspect that enhances methods and constructors with <code>null</code> parameter checks. Each parameter that is
 * annotated with an <code>org.splan.utils.validation.NotNull</code> annotation will be enhanced with a check hat throws
 * an <code>IllegalArgumentException</code> if <code>null</code> is passed for that parameter.
  * <p>
  *
  * Due to some limitations of AspectJ, this aspect only checks the first five parameters for <code>NotNull</code>
  * annotations.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 @Aspect
 public class NullParameterChecker
 {
 	/**
 	 * Checks an argument for <code>null</code>. If the argument is <code>null</code> this method will throw an
 	 * <code>IllegalArgumentException</code> that indicates the problem.
 	 *
 	 * @param joinPoint
 	 *     The Join Point where the check is happening
 	 * @param value
 	 *     The actual value passed for the parameter
 	 * @param index
 	 *     The position of the parameter in the method signature
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is <code>null</code>
 	 */
 	private void checkArgument(JoinPoint joinPoint, Object value, int index)
 		throws IllegalArgumentException
 	{
 		if (value == null)
 		{
 			Signature sig = joinPoint.getSignature();
 			String paramName = null;
 
 			if (sig instanceof MethodSignature)
 			{
 				paramName = ((MethodSignature) sig).getParameterNames()[index];
 			}
 			else if (sig instanceof ConstructorSignature)
 			{
 				paramName = ((ConstructorSignature) sig).getParameterNames()[index];
 			}
 
 			throw new InvalidArgumentException(paramName, Reason.NULL);
 		}
 	}
 
 	/**
 	 * Checks for a <code>null</code> in any method or constructor call where the first parameter is marked to not be
 	 * accepting <code>null</code>.
 	 *
 	 * @param joinPoint
 	 *     The Join Point where the check is happening
 	 * @param o
 	 *     The actual value passed to the parameter
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>o</code> is <code>null</code>
 	 */
 	@Before("execution(* *(@org.splan.utils.validation.NotNull (*), ..)) && args(o, ..)"
 		+ "|| execution(*.new(@org.splan.utils.validation.NotNull (*), ..)) && args(o, ..)")
 	public void firstArg(JoinPoint joinPoint, Object o)
 		throws IllegalArgumentException
 	{
 		this.checkArgument(joinPoint, o, 0);
 	}
 
 	/**
 	 * Checks for a <code>null</code> in any method or constructor call where the second parameter is marked to not be
 	 * accepting <code>null</code>.
 	 *
 	 * @param joinPoint
 	 *     The Join Point where the check is happening
 	 * @param o
 	 *     The actual value passed to the parameter
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>o</code> is <code>null</code>
 	 */
 	@Before("execution(* *(*, @org.splan.utils.validation.NotNull (*), ..)) && args(*, o, ..)"
 		+ "|| execution(*.new(*, @org.splan.utils.validation.NotNull (*), ..)) && args(*, o, ..)")
 	public void secondArg(JoinPoint joinPoint, Object o)
 		throws IllegalArgumentException
 	{
 		this.checkArgument(joinPoint, o, 1);
 	}
 
 	/**
 	 * Checks for a <code>null</code> in any method or constructor call where the third parameter is marked to not be
 	 * accepting <code>null</code>.
 	 *
 	 * @param joinPoint
 	 *     The Join Point where the check is happening
 	 * @param o
 	 *     The actual value passed to the parameter
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>o</code> is <code>null</code>
 	 */
 	@Before("execution(* *(*, *, @org.splan.utils.validation.NotNull (*), ..)) && args(*, *, o, ..)"
 		+ "|| execution(*.new(*, *, @org.splan.utils.validation.NotNull (*), ..)) && args(*, *, o, ..)")
 	public void thirdArg(JoinPoint joinPoint, Object o)
 		throws IllegalArgumentException
 	{
 		this.checkArgument(joinPoint, o, 2);
 	}
 
 	/**
 	 * Checks for a <code>null</code> in any method or constructor call where the fourth parameter is marked to not be
 	 * accepting <code>null</code>.
 	 *
 	 * @param joinPoint
 	 *     The Join Point where the check is happening
 	 * @param o
 	 *     The actual value passed to the parameter
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>o</code> is <code>null</code>
 	 */
 	@Before("execution(* *(*, *, *,@org.splan.utils.validation.NotNull (*), ..)) && args(*, *, *, o, ..)"
 		+ "|| execution(*.new(*, *, *,@org.splan.utils.validation.NotNull (*), ..)) && args(*, *, *, o, ..)")
 	public void fourthArg(JoinPoint joinPoint, Object o)
 		throws IllegalArgumentException
 	{
 		this.checkArgument(joinPoint, o, 3);
 	}
 
 	/**
 	 * Checks for a <code>null</code> in any method or constructor call where the fifth parameter is marked to not be
 	 * accepting <code>null</code>.
 	 *
 	 * @param joinPoint
 	 *     The Join Point where the check is happening
 	 * @param o
 	 *     The actual value passed to the parameter
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>o</code> is <code>null</code>
 	 */
 	@Before("execution(* *(*, *, *, *, @org.splan.utils.validation.NotNull (*), ..)) && args(*, *, *, *, o, ..)"
 		+ "|| execution(*.new(*, *, *, *, @org.splan.utils.validation.NotNull (*), ..)) && args(*, *, *, *, o, ..)")
 	public void fifthArg(JoinPoint joinPoint, Object o)
 		throws IllegalArgumentException
 	{
 		this.checkArgument(joinPoint, o, 4);
 	}
 }
