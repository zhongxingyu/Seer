 package com.espn.mule.devkit;
 
 import com.thoughtworks.paranamer.CachingParanamer;
 import com.thoughtworks.paranamer.Paranamer;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 /**
  *
  * @author arlethp1
  */
 public class Assist {
 
     private static Paranamer paranamer = new CachingParanamer();
 
     public static String[] getParameterNames(Method method) {
         Class[] types = method.getParameterTypes();
         String[] result = null;
         try {
             result = paranamer.lookupParameterNames(method); // throws ParameterNamesNotFoundException if not found
         } catch (Exception e) {
         }
         if (result == null || result.length != types.length) {
             result = new String[types.length];
             if (result.length == 1) {
                 String simpleName = types[0].getSimpleName();
                 if (types[0].isArray()) {
                     simpleName = types[0].getComponentType().getSimpleName();
                 }
                 result[0] = (simpleName + "_arg").toLowerCase();
             } else {
                 for (int i = 0; i < result.length; i++) {
                     String simpleName = types[i].getSimpleName();
                     if (types[i].isArray()) {
                         simpleName = types[i].getComponentType().getSimpleName();
                     }
                     result[i] = (simpleName + "_arg" + i).toLowerCase();
                 }
             }
         }
         return result;
     }
 
     @SuppressWarnings("unchecked")
     private static <T> Class<T> wrap(Class<T> c) {
         return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
     }
     private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS;
 
     static {
         Map<Class<?>, Class<?>> primMap = new HashMap<Class<?>, Class<?>>();
         primMap.put(boolean.class, Boolean.class);
         primMap.put(byte.class, Byte.class);
         primMap.put(char.class, Character.class);
         primMap.put(double.class, Double.class);
         primMap.put(float.class, Float.class);
         primMap.put(int.class, Integer.class);
         primMap.put(long.class, Long.class);
         primMap.put(short.class, Short.class);
         primMap.put(void.class, Void.class);
         primMap.put(Boolean.class, Boolean.class);
         primMap.put(Byte.class, Byte.class);
         primMap.put(Character.class, Character.class);
         primMap.put(Double.class, Double.class);
         primMap.put(Float.class, Float.class);
         primMap.put(Integer.class, Integer.class);
         primMap.put(Long.class, Long.class);
         primMap.put(Short.class, Short.class);
         primMap.put(Void.class, Void.class);
         primMap.put(String.class, String.class);
 
         PRIMITIVES_TO_WRAPPERS = Collections.unmodifiableMap(primMap);
     }
 
     public static String methodToMule(String methodName) {
         String result = methodName.replaceAll("(\\p{javaUpperCase}+)", "-$1");
         result = result.replaceAll("(\\p{javaUpperCase}{2,}+)", "$1-");
         if (result.startsWith("-")) {
             result = result.substring(1, result.length());
         }
         result = result.replaceAll("--", "-").toLowerCase();
         return result;
     }
 
     public static abstract class Handler {
 
         HashMap<String, StringBuilder> resultMap = new HashMap<String, StringBuilder>();
         ArrayList<String> names = new ArrayList<String>();
 
         public synchronized StringBuilder getStringBuilder(Method method) {
             String name = method.getName();
             StringBuilder sb = resultMap.get(name);
             if (sb == null) {
                 names.add(name);
                 sb = new StringBuilder();
                 this.resultMap.put(name, sb);
             }
             return sb;
         }
 
         public abstract void handle(Method m);
 
         @Override
         public String toString() {
             StringBuilder resultSb = new StringBuilder();
             for (String name : names) {
                 StringBuilder sb = resultMap.get(name);
                 resultSb.append(sb).append("\n");
             }
             return resultSb.toString();
         }
     }
     private static final HashSet<String> bogusMethodNameSet = new HashSet<String>();
 
     static {
         for (Method m : java.lang.Object.class.getMethods()) {
             bogusMethodNameSet.add(m.getName());
         }
         for (Method m : java.lang.Class.class.getMethods()) {
             bogusMethodNameSet.add(m.getName());
         }
     }
 
     public static void handlePublicMethods(Class c, List<Handler> handlers) {
         Method[] methods = c.getMethods();
         HashSet<String> nameset = new HashSet<String>(bogusMethodNameSet);
         for (Method m : methods) {
             if (nameset.add(m.getName())) {
                 for (Handler h : handlers) {
                     h.handle(m);
                 }
             }
         }
     }
 
     private static String buildClassName(boolean wrapPrimatives, Class r) {
         if (wrapPrimatives) {
             r = wrap(r);
         }
 
         String result;
 
         if (r.isArray()) {
             result = r.getComponentType().getName() + "[]";
         } else {
             result = r.getName();
         }
 
         return fixInnerClassNames(result);
     }
 
     private static String fixInnerClassNames(String name) {
         String[] parts = name.split("\\$");
         if (parts.length > 1) {
             String[] packageNames = parts[0].split("\\.");
             name = packageNames[packageNames.length - 1] + "." + parts[1];
         } else {
             String[] packageNames = parts[0].split("\\.");
             name = packageNames[packageNames.length - 1];
         }
         return name;
     }
 
     private static String[][] buildParamClassVarNames(boolean wrapPrimatives, Method m) {
         String[][] paramClassVarNames = null;
         ArrayList<String[]> paramClassVarNameList = new ArrayList<String[]>();
 
         Class[] paramTypes = m.getParameterTypes();
         String[] parameterNames = getParameterNames(m);
         if (paramTypes != null) {
             for (int i = 0; i < paramTypes.length; i++) {
                 String[] classVar = new String[]{
                     buildClassName(wrapPrimatives, paramTypes[i]),
                     parameterNames[i]
                 };
 
                 paramClassVarNameList.add(classVar);
             }
             paramClassVarNames = paramClassVarNameList.toArray(new String[paramClassVarNameList.size()][]);
         }
         return paramClassVarNames;
     }
 
     public static class JavadocItem {
 
         private String namespace;
         private String sampleXmlFileName;
         private String muleName;
         private String methodName;
         private String[][] paramClassVarNames;
         private String returnClassName;
         private String[] exceptions;
 
         public JavadocItem(String namespace,
                 String sampleXmlFilename,
                 String methodName,
                 String[][] paramClassVarNames,
                 String returnClassName,
                 String[] exceptions) {
             this.namespace = namespace;
             this.sampleXmlFileName = sampleXmlFilename;
             this.methodName = methodName;
             this.muleName = methodToMule(methodName);
             this.paramClassVarNames = paramClassVarNames;
             this.returnClassName = returnClassName;
             this.exceptions = exceptions;
         }
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
             sb.append("    /**\n");
             sb.append("     * ").append(methodName).append("\n");
             sb.append("     *\n");
             sb.append("     * {@sample.xml ../../../doc/").
                     append(this.sampleXmlFileName).append(" ").
                     append(this.namespace).append(":").append(muleName).append("}\n");
             if (paramClassVarNames != null) {
                 for (int i = 0; i < paramClassVarNames.length; i++) {
                     String[] classVarNames = paramClassVarNames[i];
                     sb.append("     * @param ");
                     sb.append(classVarNames[1]).append(" ");
                     sb.append(classVarNames[0]).append("\n");
                 }
             }
 
             if (this.returnClassName == null) {
                 //print nothing
             } else {
                 sb.append("     * @return ").append(this.returnClassName).append("\n");
             }
 
             if (exceptions != null && exceptions.length > 0) {
                 sb.append("     * @throws");
                 for (String c : exceptions) {
                     sb.append(" ").append(c);
                 }
             }
             sb.append("     */\n");
             return sb.toString();
         }
     }
 
     public static class JavadocHandler extends Handler {
 
         private String namespace;
         private String sampleXmlFileName;
         final ArrayList<JavadocItem> items = new ArrayList<JavadocItem>();
 
         public JavadocHandler(String namespace, String sampleXmlFileName) {
             this.namespace = namespace;
             this.sampleXmlFileName = sampleXmlFileName;
         }
 
         private JavadocItem buildJavadocItem(Method m) {
             String methodName = m.getName();
 
             String[][] paramClassVarNames = buildParamClassVarNames(false, m);
             String returnClassName = null;
             String[] exceptions = null;
 
             Class r = m.getReturnType();
             if (r.equals(Void.TYPE)) {
                 //print nothing
             } else {
                 returnClassName = r.getName();
             }
 
             Class[] exceptionClasses = m.getExceptionTypes();
             if (exceptionClasses.length > 0) {
                 exceptions = new String[exceptionClasses.length];
                 for (int i = 0; i < exceptionClasses.length; i++) {
                     Class c = exceptionClasses[i];
                     exceptions[i] = c.getName();
                 }
             }
             return new JavadocItem(this.namespace, this.sampleXmlFileName, methodName, paramClassVarNames, returnClassName, exceptions);
         }
 
         @Override
         public void handle(Method m) {
             StringBuilder sb = getStringBuilder(m);
             JavadocItem item;
             synchronized (this.items) {
                 item = buildJavadocItem(m);
                 this.items.add(item);
             }
             sb.append(item.toString());
         }
     }
 
     public static class MethodItem {
 
         private String variableName;
         private String staticClassName;
         private String muleName;
         private String methodName;
         private String[][] paramClassVarNames;
         private String returnClassName;
         private String[] exceptions;
 
         public MethodItem(String variableName,
                 String staticClassName,
                 String muleName,
                 String methodName,
                 String[][] paramClassVarNames,
                 String returnClassName,
                 String[] exceptions) {
             this.variableName = variableName;
             this.staticClassName = staticClassName;
             this.muleName = muleName;
             this.methodName = methodName;
             this.paramClassVarNames = paramClassVarNames;
             this.returnClassName = returnClassName;
             this.exceptions = exceptions;
         }
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
 
             sb.append("    @Processor\n");
             sb.append("    public ");
 
             if (returnClassName == null) {
                 sb.append("void");
             } else {
                 sb.append(returnClassName);
             }
             sb.append(" ").append(methodName).append("(");
 
             for (int i = 0; i < paramClassVarNames.length; i++) {
                 if (i > 0) {
                     sb.append(",");
                 }
                 sb.append(" ");
                 if (paramClassVarNames[i][0].endsWith("[]")) {
                     sb.append("List<");
                     sb.append(paramClassVarNames[i][0].substring(0, paramClassVarNames[i][0].length() - 2));
                     sb.append(">");
                 } else {
                     sb.append(paramClassVarNames[i][0]);
                 }
                 sb.append(" ").append(paramClassVarNames[i][1]);
             }
             if (paramClassVarNames.length > 0) {
                 sb.append(" ");
             }
 
             sb.append(")");
 
             if (exceptions != null && exceptions.length > 0) {
                 sb.append(" throws");
                 for (String c : exceptions) {
                     sb.append(" ").append(c);
                 }
             }
 
             sb.append(" {\n");
             sb.append("        ");
 
             if (returnClassName != null) {
                 sb.append("return ");
             }
 
             if (staticClassName != null) {
                 sb.append(staticClassName);
             } else {
                 sb.append(variableName);
             }
             sb.append(".").append(methodName).append("(");
 
             for (int i = 0; i < paramClassVarNames.length; i++) {
                 if (i > 0) {
                     sb.append(",");
                 }
                 if (paramClassVarNames[i][0].endsWith("[]")) {
                     String cn = paramClassVarNames[i][0].substring(0, paramClassVarNames[i][0].length() - 2);
                     sb.append(paramClassVarNames[i][1]).append(".toArray( new ").append(cn);
                     sb.append("[").append(paramClassVarNames[i][1]).append(".size()").append("] )");
                 } else {
                     sb.append(paramClassVarNames[i][1]);
                 }
             }
             sb.append(");\n");
             sb.append("    }\n");
 
             return sb.toString();
         }
     }
 
     public static class ConnectorMethodHandler extends Handler {
 
         private String variableName;
         private Class theClass;
         private final HashSet<Class> importPackages = new HashSet<Class>();
         final ArrayList<MethodItem> items = new ArrayList<MethodItem>();
 
         public ConnectorMethodHandler(Class theClass,
                 String variableName) {
             this.theClass = theClass;
             this.variableName = variableName;
             this.importPackages.add(this.theClass);
         }
 
         private MethodItem buildMethodItem(Method m) {
             String methodName = m.getName();
 
             Class[] paramClasses = m.getParameterTypes();
             if (paramClasses != null) {
                 for (Class c : paramClasses) {
                     this.importPackages.add(c);
                 }
             }
             String[][] paramClassVarNames = buildParamClassVarNames(true, m);
             String returnClassName = null;
             String[] exceptions = null;
 
             Class r = m.getReturnType();
             if (r.equals(Void.TYPE)) {
                 //print nothing so null
             } else {
                 this.importPackages.add(r);
                 returnClassName = buildClassName(false, r);
             }
 
             Class[] exceptionClasses = m.getExceptionTypes();
             if (exceptionClasses.length > 0) {
                 exceptions = new String[exceptionClasses.length];
                 for (int i = 0; i < exceptionClasses.length; i++) {
                     Class c = exceptionClasses[i];
                     this.importPackages.add(c);
                     exceptions[i] = c.getName();
                 }
             }
             String staticClassName = null;
             if (Modifier.isStatic(m.getModifiers())) {
                 staticClassName = theClass.getSimpleName();
             }
             String muleName = methodToMule(methodName);
             return new MethodItem(variableName,
                     staticClassName,
                     muleName,
                     methodName,
                     paramClassVarNames,
                     returnClassName,
                     exceptions);
         }
 
         @Override
         public void handle(Method m) {
             StringBuilder sb = getStringBuilder(m);
             MethodItem item;
             synchronized (this.items) {
                 item = buildMethodItem(m);
                 items.add(item);
             }
             sb.append(item.toString());
         }
 
         public String buildImports() {
             StringBuilder sb = new StringBuilder();
             TreeSet<String> resultSet = new TreeSet<String>();
 
             synchronized (this.importPackages) {
                 Class[] classes = this.importPackages.toArray(new Class[this.importPackages.size()]);
                 for (Class c : classes) {
                     if (c.getPackage() != null && !c.getPackage().getName().equals("java.lang")) {
                         resultSet.add(c.getName());
                     }
                 }
                 for (String i : resultSet) {
                     sb.append("import ").append(i.replaceAll("\\$", ".")).append(";\n");
                 }
             }
             sb.append("\n");
             return sb.toString();
         }
     }
 
     public static class ConnectorHandler extends Handler {
 
         JavadocHandler javadocHandler;
         ConnectorMethodHandler connectorMethodHandler;
 
         public ConnectorHandler(Class theClass, String variableName, String namespace, String sampleXmlFileName) {
             this.javadocHandler = new JavadocHandler(namespace, sampleXmlFileName);
             this.connectorMethodHandler = new ConnectorMethodHandler(theClass, variableName);
         }
 
         @Override
         public void handle(Method m) {
             this.javadocHandler.handle(m);
             this.connectorMethodHandler.handle(m);
         }
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
             sb.append(this.connectorMethodHandler.buildImports());
             for (String name : this.connectorMethodHandler.names) {
                 sb.append(this.javadocHandler.resultMap.get(name));
                 sb.append(this.connectorMethodHandler.resultMap.get(name));
                 sb.append("\n");
             }
             return sb.toString();
         }
     }
 
     public static class TestMuleConfigHandler extends Handler {
 
         private String namespace;
 
         public TestMuleConfigHandler(String namespace) {
             this.namespace = namespace;
         }
 
         @Override
         public void handle(Method m) {
             StringBuilder sb = getStringBuilder(m);
             sb.append("    <flow name=\"").append(m.getName()).append("Flow\">\n");
 
             sb.append("        <").append(namespace).append(":").append(methodToMule(m.getName()));
 
             Class[] paramTypes = m.getParameterTypes();
             String[] parameterNames = getParameterNames(m);
             for (int i = 0; i < paramTypes.length; i++) {
                 sb.append(" ").append(parameterNames[i]).append("=\"");
                 if (PRIMITIVES_TO_WRAPPERS.containsKey(paramTypes[i])) {
                     if (!paramTypes[i].equals(String.class)) {
                        sb.append("0");
                     }
                 } else {
                    sb.append("null");
                 }
                sb.append("\"");
             }
             sb.append("/>\n").append("    </flow>\n");
         }
     }
 
     public static class ConnectorSampleXmlHandler extends Handler {
 
         private String namespace;
 
         public ConnectorSampleXmlHandler(String namespace) {
             this.namespace = namespace;
         }
 
         @Override
         public void handle(Method m) {
             StringBuilder sb = getStringBuilder(m);
             String mname = methodToMule(m.getName());
             sb.append("\n<!-- BEGIN_INCLUDE(").append(namespace).append(":").append(mname).append(") -->\n");
             sb.append("    <").append(namespace).append(":").append(mname);
             Class[] paramTypes = m.getParameterTypes();
             String[] parameterNames = getParameterNames(m);
 
             ArrayList<Integer> arrayIndexes = new ArrayList<Integer>();
             for (int i = 0; i < paramTypes.length; i++) {
                 Class c = paramTypes[i];
                 if (c.isArray()) {
                     arrayIndexes.add(i);
                 }
             }
 
             //put normal params here
             for (int i = 0; i < paramTypes.length; i++) {
                 if (paramTypes[i].isArray() == false) {
                     if (PRIMITIVES_TO_WRAPPERS.containsKey(paramTypes[i])) {
                         sb.append(" ").append(parameterNames[i]).append("=\"#[map-payload:").append(parameterNames[i]).append("]\"");
                     } else {
                         //complex type add -ref bit
                         sb.append(" ").append(parameterNames[i]).append("-ref=\"#[map-payload:").append(parameterNames[i]).append("]\"");
                     }
                 }
             }
 
             if (arrayIndexes.size() > 0) {
                 for (Integer index : arrayIndexes) {
                     String listParamName = parameterNames[index];
                     sb.append(">\n");
                     sb.append("        <").append(namespace).append(":");
                     sb.append(listParamName).append(" ref=\"#[map-payload:").append(listParamName).append("]\" />\n");
                 }
                 sb.append("    </").append(namespace).append(":").append(mname).append(">\n");
             } else {
                 sb.append(" />\n");
             }
             sb.append("<!-- END_INCLUDE(").append(namespace).append(":").append(mname).append(") -->");
         }
     }
 
     public static String run(String classname, String sampleXmlFileName, String namespace, String variableName) throws Exception {
         StringBuilder resultSb = new StringBuilder();
 
         Class c = Class.forName(classname);
 
         ConnectorHandler connectorHandler = new ConnectorHandler(c, variableName, namespace, sampleXmlFileName);
 
         //ConnectorMethodHandler connectorMethodHandler = new ConnectorMethodHandler(c, variableName);
         ConnectorSampleXmlHandler connectorSampleXmlHandler = new ConnectorSampleXmlHandler(namespace);
         //JavadocHandler javadocHandler = new JavadocHandler(namespace, sampleXmlFileName);
         TestMuleConfigHandler testMuleConfigHandler = new TestMuleConfigHandler(namespace);
 
         ArrayList<Handler> handlers = new ArrayList<Handler>();
         //handlers.add(javadocHandler);
         //handlers.add(connectorMethodHandler);
         handlers.add(connectorHandler);
         handlers.add(connectorSampleXmlHandler);
         handlers.add(testMuleConfigHandler);
 
         handlePublicMethods(c, handlers);
 
         resultSb.append(testMuleConfigHandler);
 
         resultSb.append("\n\n\n");
         resultSb.append(" ---------------------------------------- ");
         resultSb.append("\n\n\n");
 
         resultSb.append(connectorSampleXmlHandler);
 
         resultSb.append("\n\n\n");
         resultSb.append(" ---------------------------------------- ");
         resultSb.append("\n\n\n");
 
         resultSb.append(connectorHandler);
 
         resultSb.append("\n\n\n");
         resultSb.append(" ---------------------------------------- ");
         resultSb.append("\n\n\n");
 
         return resultSb.toString();
     }
 
     public static void main(String[] args) throws Exception {
         String classname = "java.lang.Math";
         String sampleXmlFileName = "Math-connector.xml.sample";
         String namespace = "math";
         String variableName = "math";
 
         if (args != null && args.length == 4) {
             classname = args[0];
             sampleXmlFileName = args[1];
             namespace = args[2];
             variableName = args[3];
         }
 
         System.out.println(run(classname, sampleXmlFileName, namespace, variableName));
     }
 }
