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
                result[0] = (types[0].getSimpleName() + "_arg").toLowerCase();
             } else {
                 for (int i = 0; i < result.length; i++) {
                    result[i] = (types[0].getSimpleName() + "_arg" + i).toLowerCase();
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
 
     public static class JavadocHandler extends Handler {
 
         String namespace;
         String sampleXmlFileName;
 
         public JavadocHandler(String namespace, String sampleXmlFileName) {
             this.namespace = namespace;
             this.sampleXmlFileName = sampleXmlFileName;
         }
 
         @Override
         public void handle(Method m) {
             StringBuilder sb = getStringBuilder(m);
             sb.append("    /**\n");
             sb.append("     * ").append(m.getName()).append("\n");
             sb.append("     *\n");
             sb.append("     * {@sample.xml ../../../doc/").
                     append(this.sampleXmlFileName).append(" ").
                     append(this.namespace).append(":").append(methodToMule(m.getName())).append("}\n");
             Class[] paramTypes = m.getParameterTypes();
 
             String[] parameterNames = getParameterNames(m);
             if (paramTypes != null) {
                 for (int i = 0; i < paramTypes.length; i++) {
                     Class c = paramTypes[i];
                     sb.append("     * @param ");
                     sb.append(parameterNames[i]).append(" ");
                     sb.append(c.getName()).append("\n");
                 }
             }
 
             Class r = m.getReturnType();
             if (r.equals(Void.TYPE)) {
                 //print nothing
             } else {
                 sb.append("     * @return \n").append("     * ").append(r.getName()).append("\n");
             }
 
             Class[] exceptions = m.getExceptionTypes();
             if (exceptions.length > 0) {
                 sb.append("     * @throws");
                 for (Class c : exceptions) {
                     sb.append(" ").append(c.getName());
                 }
             }
             sb.append("     */\n");
         }
     }
 
     public static class ConnectorMethodHandler extends Handler {
 
         String variableName;
         Class theClass;
 
         public ConnectorMethodHandler(Class theClass,
                 String variableName) {
             this.theClass = theClass;
             this.variableName = variableName;
         }
 
         @Override
         public void handle(Method m) {
             StringBuilder sb = getStringBuilder(m);
             sb.append("    @Processor\n");
             sb.append("    public ");
             Class r = m.getReturnType();
             if (r.equals(Void.TYPE)) {
                 sb.append("void");
             } else if (r.isArray()) {
                 sb.append(r.getComponentType().getName()).append("[]");
             } else {
                 String str = r.getName();
                 String[] parts = str.split("\\$");
                 if (parts.length > 1) {
                     String[] pacClassNames = parts[0].split("\\.");
                     sb.append(pacClassNames[pacClassNames.length - 1]).append(".").append(parts[parts.length - 1]);
                 } else {
                     sb.append(r.getSimpleName());
                 }
             }
             sb.append(" ").append(m.getName()).append("(");
 
             Class[] paramTypes = m.getParameterTypes();
             String[] parameterNames = getParameterNames(m);
 
             for (int i = 0; i < paramTypes.length; i++) {
                 Class paramTypeClass = paramTypes[i];
 
                 if (i > 0) {
                     sb.append(",");
                 }
                 sb.append(" ");
                 if (paramTypeClass.isArray()) {
                     sb.append(paramTypeClass.getComponentType().getName());
                     sb.append("[]");
                 } else {
                     sb.append(wrap(paramTypeClass).getSimpleName());
                 }
                 sb.append(" ").append(parameterNames[i]);
             }
             if (paramTypes.length > 0) {
                 sb.append(" ");
             }
 
             sb.append(")");
 
             Class[] exceptions = m.getExceptionTypes();
             if (exceptions.length > 0) {
                 sb.append(" throws");
                 for (Class c : exceptions) {
                     sb.append(" ").append(c.getName());
                 }
             }
 
             sb.append(" {\n");
             sb.append("        ");
 
             if (!r.equals(Void.TYPE)) {
                 sb.append("return ");
             }
 
             if (Modifier.isStatic(m.getModifiers())) {
                 sb.append(this.theClass.getSimpleName());
             } else {
                 sb.append(variableName);
             }
             sb.append(".").append(m.getName()).append("(");
 
             for (int i = 0; i < paramTypes.length; i++) {
                 if (i > 0) {
                     sb.append(",");
                 }
                 sb.append(parameterNames[i]);
             }
             sb.append(");\n");
             sb.append("    }\n");
         }
     }
 
     public static class TestMuleConfigHandler extends Handler {
 
         String namespace;
 
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
                 sb.append(" ").append(parameterNames[i]).append("=\"\"");
             }
             sb.append("/>\n").append("    </flow>\n");
         }
     }
 
     public static class ConnectorSampleXmlHandler extends Handler {
 
         String namespace;
 
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
 
             for (int i = 0; i < paramTypes.length; i++) {
                 sb.append(" ").append(parameterNames[i]).append("=\"#[map-payload:").append(parameterNames[i]).append("]\"");
             }
             sb.append(" -->\n");
             sb.append("<!-- END_INCLUDE(").append(namespace).append(":").append(mname).append(") -->");
         }
     }
 
     public static String run(String classname, String sampleXmlFileName, String namespace, String variableName) throws Exception {
         StringBuilder resultSb = new StringBuilder();
 
         Class c = Class.forName(classname);
 
         ConnectorMethodHandler connectorMethodHandler = new ConnectorMethodHandler(c, variableName);
         ConnectorSampleXmlHandler connectorSampleXmlHandler = new ConnectorSampleXmlHandler(namespace);
         JavadocHandler javadocHandler = new JavadocHandler(namespace, sampleXmlFileName);
         TestMuleConfigHandler testMuleConfigHandler = new TestMuleConfigHandler(namespace);
 
         ArrayList<Handler> handlers = new ArrayList<Handler>();
         handlers.add(connectorMethodHandler);
         handlers.add(connectorSampleXmlHandler);
         handlers.add(javadocHandler);
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
 
         for (String name : connectorMethodHandler.names) {
             resultSb.append(javadocHandler.resultMap.get(name));
             resultSb.append(connectorMethodHandler.resultMap.get(name));
             resultSb.append("\n");
         }
 
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
