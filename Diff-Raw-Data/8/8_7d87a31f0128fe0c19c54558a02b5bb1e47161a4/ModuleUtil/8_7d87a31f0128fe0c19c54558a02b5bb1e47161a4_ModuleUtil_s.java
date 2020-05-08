 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.tools.ant.BuildException;
 
 /**
  * <p>Various utilities that are used primarily to read module meta information
  * by Ant tasks that are used in targets invoked for a specific module.</p>
  *
  * @author @author D&#378;mitry La&#365;&#269;uk
  */
 public class ModuleUtil
 {
     // prohibits having instances of ModuleUtil
     private ModuleUtil()
     {
     }
     
     /**
      * <p>Returns {@code true} if the given object is an instance of {@link Module}
      * regardless of what class loader this class is loaded by. {@code null} is considered
      * as not an instance of {@code Module}.</p>
      * 
      * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
      * new types by a new class loader. {@link CallTargetForModules} executes the target configured
      * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
      * anonymous class loader then it is loaded each time the target is invoked for another module.
      * Therefore, the JVM considers the class of the module objects passed by
      * {@code CallTargetForModules} and the class {@code Module} available in this child project
      * as different types. This function handles {@code Module} objects that are loaded by
      * an arbitrary class loader.</p>
      * 
      * @param object the object to be tested.
      * 
      * @return {@code true} if <em>object</em> is non-{@code null} and its class
      *      is {@link Module}; {@code false} is returned otherwise.
      * 
      * @see #getPath(Object)
      * @see #getDependencies(Object)
      * @see #getAttributes(Object)
      */
     public static boolean isModule(final Object object)
     {
         if (object == null) {
             return false;
         }
         return object.getClass().getName().equals(Module.class.getName());
     }
     
     /**
      * <p>Works as an equivalent of {@link Module#getPath() getPath()} invoked for
      * <em>module</em> if the given object's class is {@link Module} regardless of
      * what class loader loaded it.</p>
      * 
      * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
      * new types by a new class loader. {@link CallTargetForModules} executes the target configured
      * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
      * anonymous class loader then it is loaded each time the target is invoked for another module.
      * Therefore, the JVM considers the class of the module objects passed by
      * {@code CallTargetForModules} and the class {@code Module} available in this child project
      * as different types. This function handles {@code Module} objects that are loaded by
      * an arbitrary class loader.</p>
      * 
      * @param module the module object. It must be non-{@code}.
      * 
      * @return the same result as if {@code module.getPath()} were invoked.
      * 
      * @throws NullPointerException if <em>module</em> is {@code null}.
      * @throws IllegalArgumentException if <em>module</em>'s class is not {@code Module}.
      * @throws BuildException if invocation of {@code module.getPath()} did not succeed.
      * 
      * @see #isModule(Object)
      * @see #getDependencies(Object)
      * @see #getAttributes(Object)
      */
     public static String getPath(final Object module)
     {
         validateModule(module);
         return (String) callFunction(module, "getPath");
     }
     
     /**
      * <p>Works as an equivalent of {@link Module#getDependencies() getDependencies()} invoked for
      * <em>module</em> if the given object's class is {@link Module} regardless of
      * what class loader loaded it.</p>
      * 
      * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
      * new types by a new class loader. {@link CallTargetForModules} executes the target configured
      * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
      * anonymous class loader then it is loaded each time the target is invoked for another module.
      * Therefore, the JVM considers the class of the module objects passed by
      * {@code CallTargetForModules} and the class {@code Module} available in this child project
      * as different types. This function handles {@code Module} objects that are loaded by
      * an arbitrary class loader.</p>
      * 
      * @param module the module object. It must be non-{@code}.
      * 
      * @return the same result as if {@code module.getDependencies()} were invoked.
      * 
      * @throws NullPointerException if <em>module</em> is {@code null}.
      * @throws IllegalArgumentException if <em>module</em>'s class is not {@code Module}.
      * @throws BuildException if invocation of {@code module.getPath()} did not succeed.
      * 
      * @see #isModule(Object)
      * @see #getPath(Object)
      * @see #getAttributes(Object)
      */
     public static Set<?> getDependencies(final Object module)
     {
         validateModule(module);
         return (Set<?>) callFunction(module, "getDependencies");
     }
     
     /**
      * <p>Works as an equivalent of {@link Module#getAttributes() getAttributes()} invoked for
      * <em>module</em> if the given object's class is {@link Module} regardless of
      * what class loader loaded it.</p>
      * 
      * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
      * new types by a new class loader. {@link CallTargetForModules} executes the target configured
      * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
      * anonymous class loader then it is loaded each time the target is invoked for another module.
      * Therefore, the JVM considers the class of the module objects passed by
      * {@code CallTargetForModules} and the class {@code Module} available in this child project
      * as different types. This function handles {@code Module} objects that are loaded by
      * an arbitrary class loader.</p>
      * 
      * <p>Note that if an attribute value's class is not in the class path of the current
      * class loader then an attempt to use it could still cause {@link ClassCastException}
      * exceptions.</p>
      * 
      * @param module the module object. It must be non-{@code}.
      * 
      * @return the same result as if {@code module.getAttributes()} were invoked.
      * 
      * @throws NullPointerException if <em>module</em> is {@code null}.
      * @throws IllegalArgumentException if <em>module</em>'s class is not {@code Module}.
      * @throws BuildException if invocation of {@code module.getPath()} did not succeed.
      * 
      * @see #isModule(Object)
      * @see #getPath(Object)
      * @see #getDependencies(Object)
      */
     public static Map<String, Object> getAttributes(final Object module)
     {
         validateModule(module);
         return (Map<String, Object>) callFunction(module, "getAttributes");
     }
     
     private static void validateModule(final Object module)
     {
         if (module == null) {
             throw new NullPointerException("module");
         }
         final String className = module.getClass().getName();
         if (!className.equals(Module.class.getName())) {
             throw new IllegalArgumentException(MessageFormat.format(
                     "Unsupported module type. Expected: ''{0}'', was: ''{1}''",
                     Module.class.getName(), className));
         }
     }
     
     private static Object callFunction(final Object module, final String functionName)
     {
         try {
             return module.getClass().getMethod(functionName).invoke(module);
         }
         catch (IllegalAccessException ex) {
            throw new BuildException("Unable to get module attributes.", ex);
         }
         catch (NoSuchMethodException ex) {
            throw new BuildException("Unable to get module attributes.", ex);
         }
         catch (InvocationTargetException ex) {
            throw new BuildException("Unable to get module attributes.", ex);
         }
     }
 }
