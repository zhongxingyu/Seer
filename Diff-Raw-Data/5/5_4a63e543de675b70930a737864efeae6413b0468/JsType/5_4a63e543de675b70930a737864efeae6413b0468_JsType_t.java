 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.jasdoc.doc;
 
 /**
  * JavaScript type.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public class JsType
 {
     /** The ANY type. */
     public static final JsType ANY = new JsType("{*}");
 
     /** The VOID type. */
     public static final JsType VOID = new JsType("{void}");
 
     /** The type expression. */
     private final String expression;
 
     /**
      * Constructor.
      *
      * @param expression
      *            The type expression.
      */
     public JsType(final String expression)
     {
        if (expression.startsWith("{") && expression.endsWith("}"))
            this.expression = expression.substring(1, expression.length() - 1);
        else
            this.expression = expression;
     }
 
     /**
      * Returns the type expression.
      *
      * @return The type expression.
      */
     public String getExpression()
     {
         return this.expression;
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString()
     {
         return getExpression();
     }
 
     /**
      * Checks if this type is void.
      *
      * @return True if type is void, false if not.
      */
     public boolean isVoid()
     {
         return this == VOID;
     }
 }
