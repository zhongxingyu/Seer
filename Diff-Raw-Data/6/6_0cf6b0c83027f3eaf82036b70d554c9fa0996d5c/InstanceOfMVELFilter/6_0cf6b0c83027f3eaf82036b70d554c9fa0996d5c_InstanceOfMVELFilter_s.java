 /* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
  *
  * Copyright (c) 2013 Edugility LLC.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  * The original copy of this license is available at
  * http://www.opensource.org/license/mit-license.html.
  */
 package com.edugility.objexj.engine;
 
 import java.util.Map;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mvel2.MVEL;
 
 /**
  * An {@link MVELFilter} that conveniently checks the {@linkplain
  * InstructionContext#read() current item} to see if it is an instance
  * of a specified {@link Class} before applying further <a
  * href="http://mvel.codehaus.org/">MVEL</a>-based filters to it.
  *
  * @param <T> the type of {@link Object} that can be {@linkplain
  * #accept(Object, Map) accepted}
  *
  * @author <a href="http://about.me/lairdnelson"
  * target="_parent">Laird Nelson</a>
  */
 public class InstanceOfMVELFilter<T> extends MVELFilter<T> {
 
   /**
    * The version of this class for {@linkplain Serializable
    * serialization purposes}.
    */
   private static final long serialVersionUID = 1L;
 
   /**
    * A {@link Pattern} for parsing a single {@link String} for the
    * operands logically taken by this {@link InstanceOfMVELFilter}.
    *
    * <p>This field is never {@code null}.</p>
    *
    * <p>The textual form of the {@link Pattern} is: {@code ^\\s*([^\\s]+)\\s*(.*)}.</p>
    *
    * @see java.util.regex.Pattern#compile(String)
    */
   private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*([^\\s]+)\\s*(.*)");
 
   /**
    * The {@link Class} whose {@link Class#isInstance(Object)} method
    * will be called.
    *
    * <p>This field is never {@code null}.</p>
    */
   private final Class<?> cls;
 
   /**
    * Determines whether an {@link Object} will be checked to see if
    * {@linkplain Object#getClass() its <code>Class</code>} is equal to
    * {@linkplain #InstanceOfMVELFilter(Class, String) the
    * <code>Class</code> supplied at construction time} or merely an
    * {@linkplain Class#isInstance(Object) an instance of it}.
    */
   private final boolean exact;
 
   /**
    * Creates a new {@link InstanceOfMVELFilter}.
    *
    * @param operands the operands to parse represented as a single
    * {@link String}; must not be {@code null}
    *
    * @exception IllegalArgumentException if {@code operands} is {@code
    * null} or if a {@link Class} could not be loaded because part of
    * the {@code operands} {@link String} was a class name that could
    * not be {@linkplain Instruction#loadClass(String) loaded}
    */
   public InstanceOfMVELFilter(final String operands) {
     super();
     if (operands == null) {
       throw new IllegalArgumentException("operands", new NullPointerException("operands"));
     }
     final Matcher m = OPERAND_PATTERN.matcher(operands);
     assert m != null;
     if (!m.find()) {
       throw new IllegalArgumentException("Bad operands: " + operands);
     }
     String className = m.group(1);
     assert className != null;
     this.exact = className.charAt(0) == '=';
     if (this.exact) {
       className = className.substring(1);
     }
 
     Class<?> c = null;
     try {
       c = this.loadClass(className);
     } catch (final ClassNotFoundException cnfe) {
       throw new IllegalArgumentException("Bad operands: " + operands, cnfe);
     } finally {
       this.cls = c;
     }
 
     final String mvel = m.group(2);
     if (mvel == null) {
       this.mvelExpression = null;
       this.mvelExpressionSource = null;
     } else {
       this.mvelExpression = MVEL.compileExpression(mvel);
       this.mvelExpressionSource = mvel;
     }
   }
 
   /**
    * Creates a new {@link InstanceOfMVELFilter}.
    *
    * @param className the name of a {@link Class} against which {@link
    * Object}s will be {@linkplain #accept(InstructionContext)
    * checked}; must not be {@code null}; may start with an {@code =}
    * character, in which case a class equality test will be used
    * (otherwise {@link Class#isInstance(Object)} is used)
    *
    * @param mvel an <a href="http://mvel.codehaus.org/">MVEL</a>
    * expression; may be {@code null}
    *
    * @exception IllegalArgumentException if {@code className} is
    * {@code null} or designates a {@link Class} that could not be
    * {@linkplain Instruction#loadClass(String) loaded}
    */
   public InstanceOfMVELFilter(String className, final String mvel) {
     super();
     if (className == null) {
       throw new IllegalArgumentException("className", new NullPointerException("className"));
     }
     this.exact = className.charAt(0) == '=';
     if (this.exact) {
       className = className.substring(1);
     }
     Class<?> c = null;
     try {
       c = this.loadClass(className);
     } catch (final ClassNotFoundException cnfe) {
       throw new IllegalArgumentException("className", cnfe);
     } finally {
       this.cls = c;
     }
     if (mvel == null) {
       this.mvelExpression = null;
       this.mvelExpressionSource = null;
     } else {
       this.mvelExpression = MVEL.compileExpression(mvel);
       this.mvelExpressionSource = mvel;
     }
   }
 
   /**
    * Invokes the {@link #InstanceOfMVELFilter(Class, boolean, String)}
    * constructor passing the supplied {@link Class}, {@code false} and
    * {@code null}, thereby creating a new {@link
    * InstanceOfMVELFilter}.
    *
    * @param c the {@link Class} to use for comparison tests; must not
    * be {@code null}
    *
    * @exception IllegalArgumentException if {@code c} is {@code null}
    */
   public InstanceOfMVELFilter(final Class<?> c) {
     this(c, false, null);
   }
 
   /**
    * Invokes the {@link #InstanceOfMVELFilter(Class, boolean, String)}
    * constructor passing the supplied {@link Class}, {@code false} and
    * the supplied <a href="http://mvel.codehaus.org/">MVEL</a> expression, thereby creating a new {@link
    * InstanceOfMVELFilter}.
    *
    * @param c the {@link Class} to use for comparison tests; must not
    * be {@code null}
    *
    * @param mvel the <a href="http://mvel.codehaus.org/">MVEL</a>
    * expression; may be {@code null}
    *
    * @exception IllegalArgumentException if {@code c} is {@code null}
    */
   public InstanceOfMVELFilter(final Class<?> c, final String mvel) {
     this(c, false, mvel);
   }
 
   /**
    * Creates a new {@link InstanceOfMVELFilter}.
    *
    * @param c the {@link Class} to use for comparison tests; must not
    * be {@code null}
    *
    * @param exact if {@code true}, then the {@link Class} comparison
    * test used will be {@linkplain Object#equals(Object) equality};
    * otherwise it will be {@linkplain Class#isInstance(Object)
    * <code>Class</code> membership}
    *
    * @param mvel the <a href="http://mvel.codehaus.org/">MVEL</a>
    * expression; may be {@code null}
    *
    * @exception IllegalArgumentException if {@code c} is {@code null}
    *
    * @see <a href="http://mvel.codehaus.org/">The MVEL expression
    * language</a>
    */
   public InstanceOfMVELFilter(final Class<?> c, final boolean exact, final String mvel) {
     super();
     if (c == null) {
       throw new IllegalArgumentException("c", new NullPointerException("c"));
     }
     this.cls = c;
     this.exact = exact;
     if (mvel == null) {
       this.mvelExpression = null;
       this.mvelExpressionSource = null;
     } else {
       this.mvelExpression = MVEL.compileExpression(mvel);
       this.mvelExpressionSource = mvel;
     }
   }
 
   /**
    * Returns {@code true} if {@link Class} equality will be the
    * comparison test used against input {@link Object}s.
    *
    * @return {@code true} if {@link Class} equality will be the
    * comparison test used against input {@link Object}s; {@code false}
    * if {@linkplain Class#isInstance(Object) class membership} will be
    * used instead
    */
   public boolean isExact() {
     return this.exact;
   }
 
   /**
    * Returns {@code true} if this {@link InstanceOfMVELFilter}
    * notionally accepts the supplied {@link InstructionContext}.
    *
    * @return {@code true} if the supplied {@link InstructionContext}
    * is non-{@code null}, {@linkplain InstructionContext#canRead() can
    * be read from}, and if the {@link #accept(Object, Map)} method
    * returns {@code true} as well
    *
    * @exception IllegalArgumentException if {@code context} is {@code
    * null}
    */
   @Override
   public boolean accept(final InstructionContext<? extends T> context) {
     if (context == null) {
       throw new IllegalArgumentException("context", new NullPointerException("context == null"));
     }
    if (this.mvelExpression == null) {
      return super.accept(context);
    }
     return this.cls != null && context.canRead() && this.accept(context.read(), context.getVariables());
   }
 
   /**
    * Returns {@code true} if the supplied {@code item} is non-{@code
    * null} and {@linkplain Object#getClass() has a <code>Class</code>}
    * that compares properly with the {@link Class} {@link
    * #InstanceOfMVELFilter(Class, boolean, String) supplied at
    * construction time}.
    *
    * <p>{@link Class} comparison is controlled by the return value of
    * the {@link #isExact()} method.  If it is {@code true}, then the
    * supplied {@code item} must {@linkplain Object#getClass() have a
    * <code>Class</code>} that is equal to {@linkplain
    * #InstanceOfMVELFilter(Class, String) the <code>Class</code>
    * supplied at construction time}.  If it is {@code false}, then the
    * supplied {@code item} must merely be an {@linkplain
    * Class#isInstance(Object) instance of} that {@link Class}.</p>
    *
    * @param item the {@link Object} to be accepted; may be {@code
    * null} in which case {@code false} will be returned
    *
    * @param variables a {@link Map} of variables that may be populated
    * by the <a href="http://mvel.codehaus.org/">MVEL</a> expression
    * {@linkplain #InstanceOfMVELFilter(Class, boolean, String)
    * supplied at construction time}
    *
    * @see #isExact()
    *
    * @see #InstanceOfMVELFilter(Class, boolean, String)
    */
   @Override
   public boolean accept(final T item, Map<Object, Object> variables) {
     return
      item != null && 
       this.cls != null &&
       this.isExact() ? item.getClass().equals(this.cls) : this.cls.isInstance(item) &&
       super.accept(item, variables);
   }
 
   /**
    * Returns a hashcode for this {@link InstanceOfMVELFilter}.
    *
    * @return a hashcode for this {@link InstanceOfMVELFilter}
    */
   @Override
   public int hashCode() {
     assert this.cls != null;
     return 37 * super.hashCode() + this.cls.hashCode() + (this.exact ? 1 : 0);
   }
 
   /**
    * Returns {@code true} if the supplied {@link Object} is equal to
    * this {@link InstanceOfMVELFilter}.
    *
    * @param other the {@link Object} to test; may be {@code null} in
    * which case {@code false} will be returned
    *
    * @return {@code true} if the supplied {@link Object} is non-{@code
    * null}, {@linkplain Object#getClass() has a <code>Class</code>}
    * that is equal to this {@link InstanceOfMVELFilter}'s {@link
    * Class}, has the same return value from its {@link #isExact()}
    * method, and has the same {@link Class} supplied to it at
    * {@linkplain #InstanceOfMVELFilter(Class, boolean, String)
    * construction time}; {@code false} otherwise
    */
   @Override
   public boolean equals(final Object other) {
     if (other == this) {
       return true;
     } else if (super.equals(other)) {
       final InstanceOfMVELFilter him = (InstanceOfMVELFilter)other;
       return this.exact == him.exact && this.cls.equals(him.cls);
     } else {
       return false;
     }
   }
 
   /**
    * Returns a non-{@code null} {@link String} representation of this
    * {@link InstanceOfMVELFilter}.
    *
    * @return a non-{@code null} {@link String} representation of this
    * {@link InstanceOfMVELFilter}
    */
   @Override
   public String toString() {
     final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
     sb.append(" ");
     if (this.isExact()) {
       sb.append("=");
     }
     sb.append(this.cls.getName());
     if (this.mvelExpressionSource != null) {
       sb.append(" ").append(this.mvelExpressionSource);
     }
     return sb.toString();
   }
 
 }
