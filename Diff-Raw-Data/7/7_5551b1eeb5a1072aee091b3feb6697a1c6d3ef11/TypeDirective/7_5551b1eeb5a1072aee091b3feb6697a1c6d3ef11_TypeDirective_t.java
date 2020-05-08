 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 package org.webmacro.directive;
 
 import org.webmacro.*;
 import org.webmacro.engine.BuildContext;
 import org.webmacro.engine.BuildException;
import org.webmacro.engine.UndefinedMacro;
 import org.webmacro.engine.Variable;
 import org.webmacro.util.Settings;
 
 import java.io.IOException;
 
 /**
  * TypeDirective allows the programmer (or template designer) to ensure
  * objects placed into the <code>Context</code> are of the required class
  * type.<p>
  *
  * Syntax:<pre>
  *    #type [ required ] var-reference quoted-string
  * </pre>
  *
  * Examples:<pre>
  *    $MyName, if it exists in the context, <b>must</b> be a java.lang.String
  *    #type $MyName "java.lang.String"
  *
  *    $Today <b>must</b> exist in the context and <b>must</b> be a java.util.Date
  *    #type required $Today "java.util.Date"
  *
  *    $Addresses, if it exists in the context, <b>must</b> be an org.mycompany.til.Address array
  *    #type $Addresses "org.mycompany.util.Address[]"
  * </pre>
  *
  * TypeDirective simply ensures the class type of the <code>$Variable</code>
  * <code>isAssignableFrom</code> the specified classname.<p>
  *
  * If the variable is not flagged as being <i>required</i>, then its class type
  * is only checked if it exists in the context.  By default, variables are not
  * required to be in the context.<p>
  *
  * Special support for checking that a variable is an object array exists.
  * Simply append matching square brackets to the end of the classname.<p>
  *
  * If a Variable is <b>not</b> of the specified type, TypeDirective will
  * throw a <code>org.webmacro.PropertyException.InvalidTypeException</code>,
  * which one can catch in their servlet code if necessary.<p>
  *
  * TypeDirective is <b>enabled by default</b>, however, it can be disabled
  * via your custom <code>WebMacro.properties</code> file:<pre>
  *    TypeDirective.Enabled = true | false
  * </pre>
  *
  * @author  <a href=mailto:ebr@tcdi.com>Eric B. Ridge</a>
  * @version 1.0
  * @since post 0.97
  */
 public class TypeDirective extends Directive
 {
 
     public static final int TYPE_REQUIRED = 0;
     public static final int TYPE_OBJECT = 1;
     public static final int TYPE_CLASSNAME = 2;
 
     public static final ArgDescriptor[] _args = new ArgDescriptor[]{
         new OptionalGroup(1),
         new KeywordArg(TYPE_REQUIRED, "required"),
         new RValueArg(TYPE_OBJECT),
         new QuotedStringArg(TYPE_CLASSNAME),
     };
 
     public static final DirectiveDescriptor _dd =
             new DirectiveDescriptor("type", null, _args, null);
 
     /**
      * static method required by the WebMacro parser to provide
      * a descriptor about this directive
      */
     public static DirectiveDescriptor getDescriptor ()
     {
         return _dd;
     }
 
 
     /** the Context object we need to check the type of */
     private Variable _object;
 
     /** the Class instance that _object is requried to be */
     private Class _class;
 
     /** is the Variable required to be in the Context? */
     private boolean _required;
 
     /**
      * configure directive for this run and return 'this'
      */
     public Object build (DirectiveBuilder builder, BuildContext bc) throws BuildException
     {
 
         if (!isEnabled(builder.getName(), bc.getBroker()))
             return null;
 
         String classname = (String) builder.getArg(TYPE_CLASSNAME, bc);
         _object = (Variable) builder.getArg(TYPE_OBJECT, bc);
         _required = builder.getArg(TYPE_REQUIRED, bc) != null;
 
         try
         {
             _class = TypeDirective.getClass(classname);
         }
         catch (ClassNotFoundException cnfe)
         {
             throw new BuildException("TypeDirective cannot find the class "
                     + "/" + classname + "/");
         }
 
         return this;
     }
 
     /**
      * Ensure the class of the specified Variable reference
      * <code>isAssignableFrom</code> from the classname arg.
      *
      * @return <code>null</code>, always
      * @throws PropertyException.NoSuchVariableException if the
      *         specified variable arg evaluates to null
      */
     public Object evaluate (Context context) throws PropertyException
     {
         Object o = _object;
 
         // evaluate the _object reference down to its base object
        while (o instanceof Macro && o != UndefinedMacro.getInstance())
             o = ((Macro) o).evaluate(context);
 
        if (o == null || o == UndefinedMacro.getInstance())
         {
             // the Variable to check isn't in the Context.
             if (_required)
             {
                 // but it should be
                 throw new PropertyException
                         .NoSuchVariableException(_object.getName());
             }
             else
             {
                 // but it's not required to be there, so get out now
                 // can't check the type of a null object
                 return null;
             }
         }
 
         // check it and throw if requried class isn't compatible
         // with class of specified object
         if (!_class.isAssignableFrom(o.getClass()))
             throw new PropertyException.InvalidTypeException(_object.getName(),
                     _class);
 
         return null;
     }
 
     /**
      * The #type directive does not produce output
      */
     public void write (FastWriter fw, Context context) throws IOException, PropertyException
     {
         evaluate(context);
     }
 
     public void accept (TemplateVisitor v)
     {
         v.beginDirective(_dd.name);
         v.visitDirectiveArg("TypeContextObject", _object);
         v.visitDirectiveArg("TypeClassname", _class.getName());
         v.endDirective();
     }
 
     /**
      * Check the configuration and see if we're enabled or not.  By default, we
      * are <b>enabled</b>, even if the configuration key doesn't exist.
      */
     private final boolean isEnabled (String directiveName, Broker broker)
     {
         Settings s = broker.getSettings();
         return s.getBooleanSetting(directiveName + ".Enabled", true);
     }
 
     //
     // private, static methods
     //
 
     /**
      * Use specified class name to return its Class instance.  special support
      * for an alternate syntax for object arrays:<pre>
      *    java.util.Date[]   -- a 1d array of Date objects
      *    java.util.Data[][] -- a 2d array of Date objects
      * </pre>
      */
     private static final Class getClass (final String classname) throws ClassNotFoundException
     {
         Class clazz;
 
         if (classname.endsWith("[]"))
         {
             // an object array of some kind
             String newName = "[L" + classname.substring(0, classname.length() - 2);
             if (classname.endsWith("[][]")) // support 2d arrays
                 newName = "[" + newName.substring(0, newName.length() - 2);
             newName += ";";
 
             clazz = Class.forName(newName);
         }
         else
         {
             // a normal object
             clazz = Class.forName(classname);
         }
 
         return clazz;
     }
 }
