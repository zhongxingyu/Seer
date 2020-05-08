 /* ====================================================================
  *   Copyright 2003-2005 Fabrizio Giustina.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * ====================================================================
  */
 package net.sf.commonclipse;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 
 
 /**
  * Generator for toString() methods.
  * @author fgiust
  * @version $Revision$ ($Author$)
  */
 public final class ToStringGenerator extends Generator
 {
 
     /**
      * Class name for the ToString buider.
      */
     private static final String BUILDER_CLASS = "org.apache.commons.lang.builder.ToStringBuilder"; //$NON-NLS-1$
 
     /**
      * Singleton for ToStringGenerator.
      */
     private static Generator instance = new ToStringGenerator();
 
     /**
      * Use getInstance() to obtain an instance of ToStringGenerator.
      */
     private ToStringGenerator()
     {
     }
 
     /**
      * Returns the ToStringGenerator instance.
      * @return instance of ToStringGenerator
      */
     public static Generator getInstance()
     {
         return instance;
     }
 
     /**
      * @see net.sf.commonclipse.Generator#getMethodName()
      */
     @Override
     protected String getMethodName()
     {
         return "toString"; //$NON-NLS-1$
     }
 
     /**
      * @see net.sf.commonclipse.Generator#addImports(org.eclipse.jdt.core.IType)
      */
     @Override
     protected void addImports(IType type) throws JavaModelException
     {
         type.getCompilationUnit().createImport(BUILDER_CLASS, null, null);
 
         if (CCPluginPreferences.getPreferences().useCustomToStringStyle())
         {
             type.getCompilationUnit().createImport(
                 CCPluginPreferences.getPreferences().getToStringStyleQualifiedClass(),
                 null,
                 null);
         }
     }
 
     /**
      * @see net.sf.commonclipse.Generator#createMethod(org.eclipse.jdt.core.IType)
      */
     @Override
     protected String createMethod(IType type) throws JavaModelException
     {
 
         StringBuffer buffer = new StringBuffer();
 
         buffer.append(getJavadoc());
 
         buffer.append("    public String toString()\n    {\n        return new ToStringBuilder(this"); //$NON-NLS-1$
 
         if (CCPluginPreferences.getPreferences().useCustomToStringStyle())
         {
             buffer.append(", "); //$NON-NLS-1$
             buffer.append(CCPluginPreferences.getPreferences().getToStringStyleClassAndConstant());
         }
 
         buffer.append(")\n"); //$NON-NLS-1$
 
         if (CCPluginPreferences.getPreferences().appendSuperToToString())
         {
             buffer.append(".appendSuper(super.toString())\n"); //$NON-NLS-1$
         }
 
         if (CCPluginPreferences.getPreferences().useJavabeanToString())
         {
             buffer.append(buildAppenderListFromBean(type));
         }
         else
         {
             buffer.append(buildAppenderList(type));
         }
 
         buffer.append(".toString();\n}"); //$NON-NLS-1$
 
         return buffer.toString();
     }
 
     /**
      * Iterates on javabean properties and calls getFieldAppender for all of them.
      * @param type IType
      * @return String
      * @throws JavaModelException exception in analyzing properties
      */
     private String buildAppenderListFromBean(IType type) throws JavaModelException
     {
         // temporary map of methods to avoid duplicated entry
         Map<String, IMethod> getterMethods = buildMethodsMap(type);
 
         // fields to match method names
         Map<String, IField> fields = buildFieldMap(type);
 
         // now iterates on generated method list and create the toString method body
         Iterator<Entry<String, IMethod>> iterator = getterMethods.entrySet().iterator();
 
         StringBuffer buffer = new StringBuffer();
 
         while (iterator.hasNext())
         {
             IMethod method = iterator.next().getValue();
             String methodName = method.getElementName();
             String propertyName = getJavabeanProperyName(methodName);
 
             // check if propertyName is excluded
             if (!isExcluded(propertyName))
             {
                 // does a field with the same name exist?
                IField matchingField = (IField) fields.get(propertyName);
 
                 if (matchingField != null)
                 {
                     if (propertyName.equals(matchingField.getElementName()))
                     {
                         // if we have a fields with the same name and type of the property, use it instead
                         // of the getter method for the toString()
                         buffer.append(getFieldAppender(propertyName, propertyName));
                         continue;
                     }
                 }
 
                 // else add the getter method to the toString
                 buffer.append(getFieldAppender(propertyName, methodName + "()")); //$NON-NLS-1$
             }
         }
         return buffer.toString();
     }
 
     /**
      * Returns a Map containing all the javabean getter methods in this type and its supertypes.
      * @param type IType
      * @return Map containing method names - IMethod objects
      * @throws JavaModelException exception in analyzing type
      */
     private Map<String, IMethod> buildMethodsMap(IType type) throws JavaModelException
     {
         Map<String, IMethod> getterMethods = new HashMap<String, IMethod>();
 
         // iterates on hierarchy, looking for properties also if defined in superclasses
         ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
         IType[] types = hierarchy.getAllClasses();
 
         for (int j = 0; j < types.length; j++)
         {
             IMethod[] superMethods = types[j].getMethods();
 
             for (int x = 0; x < superMethods.length; x++)
             {
                 IMethod method = superMethods[x];
 
                 if (isJavabeanGetter(method))
                 {
                     getterMethods.put(method.getElementName(), method);
                 }
             }
         }
 
         return getterMethods;
     }
 
     /**
      * Returns the javabean property name from the getter method name.
      * @param methodName getter method name
      * @return javabean property name (ex. "test" for "getTest()")
      */
     private String getJavabeanProperyName(String methodName)
     {
 
         String propertyName;
         if (methodName.startsWith("get")) //$NON-NLS-1$
         {
             propertyName = methodName.substring(3, methodName.length());
         }
         else
         {
             // should be a boolean property isXXX
             propertyName = methodName.substring(2, methodName.length());
         }
 
         if (propertyName.length() > 1 && Character.isLowerCase(propertyName.charAt(1)))
         {
             propertyName = Character.toLowerCase(propertyName.charAt(0))
                 + propertyName.substring(1, propertyName.length());
         }
         else if (propertyName.length() == 1)
         {
             propertyName = propertyName.toLowerCase();
         }
         return propertyName;
     }
 
     /**
      * Checks if the given method is a javabean getter (start with get or is for boolean properties, no param).
      * @param method IMethod
      * @return <code>true</code> if the method is a javabean property accessor
      * @throws JavaModelException exception thrown in analyzing method
      */
     private boolean isJavabeanGetter(IMethod method) throws JavaModelException
     {
 
         if (method.getNumberOfParameters() == 0 && Flags.isPublic(method.getFlags()))
         {
             String methodName = method.getElementName();
 
             if (methodName.length() > 3 && methodName.startsWith("get")) //$NON-NLS-1$
             {
                 return true;
             }
             else if (methodName.length() > 2 && methodName.startsWith("is") //$NON-NLS-1$
                 && ("Z".equals(method.getReturnType()) //$NON-NLS-1$
                 || "QBoolean;".equals(method.getReturnType()))) //$NON-NLS-1$
             {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Generates the method javadoc.
      * @return String javadoc
      */
     private String getJavadoc()
     {
         return "/**\n * @see java.lang.Object#toString()\n */\n"; //$NON-NLS-1$
     }
 
     /**
      * @see net.sf.commonclipse.Generator#getExistingMethod(org.eclipse.jdt.core.IType)
      */
     @Override
     protected IMethod getExistingMethod(IType type)
     {
         return type.getMethod(getMethodName(), new String[0]);
     }
 
     /**
      * @see net.sf.commonclipse.Generator#getFieldAppender(java.lang.String, java.lang.String)
      */
     @Override
     protected String getFieldAppender(String fieldName, String accessor)
     {
         return ".append(\"" + fieldName + "\", this." + accessor + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     }
 
 }
