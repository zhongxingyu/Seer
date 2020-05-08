 package org.dt.japper;
 
 import java.beans.PropertyDescriptor;
 import java.math.BigDecimal;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javassist.ClassClassPath;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.NotFoundException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Copyright (c) 2012, David Sykes and Tomasz Orzechowski 
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * - Neither the name David Sykes nor Tomasz Orzechowski may be used to endorse
  * or promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE. * @author Administrator
  * 
  * 
  */
 
 /**
  * A factory class that creates a new instance of {@link Mapper} to map a given result
  * set to objects of a given type.
  *
  * The factory generates a new class each time that will map the result-set of a specific
  * query to an instance of the given class.
  * 
  */
 public class MapperCodeGenerator {
 
   private static final Log log = LogFactory.getLog(Japper.class);
   
   @SuppressWarnings("unchecked")
   public static <T> Mapper<T> create(Class<T> resultType, ResultSetMetaData metaData) throws SQLException {
     CtClass impl = createClass();
 
     String methodBody = buildMapMethodBody(resultType, metaData);
     
     String source = new StringBuilder()
         .append("  public Object map(java.sql.ResultSet rs) {\n")
         .append(methodBody)
         .append("    return dest;\n")
         .append("  }")
         .toString();
     
     try {
       if (log.isDebugEnabled()) {
         log.debug("Generated method:\n"+source);
       }
       
       CtMethod mapImpl = CtNewMethod.make(source, impl);
       impl.addMethod(mapImpl);
       
       return (Mapper<T>) impl.toClass().newInstance();
     }
     catch (Exception ex) {
       throw new IllegalStateException("Could not create instance of generated mapper!", ex);
     }
   }
   
   private static CtClass createClass() {
     try {
       ClassPool classPool = getClassPool();
       CtClass intf = classPool.get(Mapper.class.getName());
       CtClass impl = classPool.makeClass(makeNewClassName());
       impl.addInterface(intf);
       return impl;
     }
     catch (NotFoundException nfEx) {
       throw new IllegalStateException("Very weird, Javassist can't find Mapper interface!", nfEx);
     }
   }
 
  private static final Object CLASS_POOL_MUTEX = new Object();
   private static final int CLASS_POOL_REUSE_COUNTER = 100;
  private static int classCounter = CLASS_POOL_REUSE_COUNTER+1;     // Ensure that the first time call to getClassPool() will create a new class pool instance
   
   private static ClassPool mapperClassPool = null;
   
   private static ClassPool getClassPool() {
    synchronized (CLASS_POOL_MUTEX) {
       classCounter++;
       if (classCounter > CLASS_POOL_REUSE_COUNTER) {
         classCounter = 1;
         mapperClassPool = new ClassPool(true);
         mapperClassPool.insertClassPath(new ClassClassPath(Mapper.class));
       }
       return mapperClassPool;
     }
   }
   
   /**
    * Build the method body to convert the query results described by metaData to instances
    * of resultType.
    * 
    * Graph Guards
    * resultType may have complex properties, i.e. a column in metaData gets mapped to a property of
    * a property of resultType, or a property of a property of a property of result type.
    * The constructor for resultType may not create an instance of the complex property, instead
    * relying on its clients to set an instance. In order to allow for this we need to put a check
    * in before access to any complex property in order to make sure we don't throw a 
    * NullPointerException.
    * Obviously, we only need to check each complex property once, and we need to make sure that we
    * build the full object graph in the right order. To achieve this we keep a list of the complex
    * properties we have accessed as we come across them.
    * The TreeMap allows us to keep this list sorted by depth as we build it.
    * Once we have been through each property we produce the code for each guard clause and insert
    * to near the top of the method body.
    * 
    * @param resultType the type we are mapping to
    * @param metaData the result set we are mapping from
    * @return the source code of the method body to perform the mapping
    * @throws SQLException
    */
   private static <T> String buildMapMethodBody(Class<T> resultType, ResultSetMetaData metaData) throws SQLException {
     StringBuilder source = new StringBuilder().append("    ").append(resultType.getName()).append(" dest = new ").append(resultType.getName()).append("();\n\n");
 
     int tempCounter = 0;
     Map<String, String> graphGuardMap = new TreeMap<String, String>(new GraphGuardComparator());
     StringBuilder setterSource = new StringBuilder();
     
     PropertyMatcher matcher = new PropertyMatcher(resultType);
     
     for (int i = 1; i <= metaData.getColumnCount(); i++) {
       PropertyDescriptor[] path = matcher.match(metaData.getColumnLabel(i), metaData.getTableName(i), metaData.getColumnName(i));
       if (path != null) {
         tempCounter = buildPropertySetter(setterSource, graphGuardMap, i, metaData, path, tempCounter);
       }
     }
     
     String graphGuards = buildGraphGuards(graphGuardMap);
     source.append(graphGuards).append(setterSource);
     
     return source.toString();
   }
   
 
   private static class GraphGuardComparator implements Comparator<String> {
     @Override
     public int compare(String g1, String g2) {
       int c1 = countDots(g1);
       int c2 = countDots(g2);
       if (c1 == c2) {
         return g1.compareTo(g2);
       }
       
       return (c1 < c2 ? -1 : 1); 
     }
     
     private int countDots(String s) {
       int count = 0;
       int index = -1;
       while ((index = s.indexOf('.', index)) >= 0) {
         count++;
         index++;
       }
       return count;
     }
   }
   
   private static String buildGraphGuards(Map<String, String> graphGuardMap) {
     StringBuilder guards = new StringBuilder();
     for (String reference : graphGuardMap.keySet()) {
       guards.append( graphGuardMap.get(reference) );
     }
     
     return guards.append("\n").toString();
   }
 
   private static int buildPropertySetter(StringBuilder source, Map<String,String> graphGuardMap, int columnIndex, ResultSetMetaData metaData, PropertyDescriptor[] path, int tempCounter) throws SQLException {
     PropertySetter ps = new PropertySetter(columnIndex, metaData, path, buildReference(path, graphGuardMap));
     
     if (ps.isBlob()) {
       source.append("    ").append(ps.reference).append('.').append(ps.writerMethod).append("( ").append(ps.readerMethod).append("(").append("rs, ").append(columnIndex).append(") );\n");
     }
     else if (!ps.isNullable() && !ps.isNeedsConversion()) {
       source.append("    ").append(ps.reference).append('.').append(ps.writerMethod).append("( rs.").append(ps.readerMethod).append("(").append(columnIndex).append(") );\n");
     }
     else {
       // read into temp variable
       String tempName = "t"+tempCounter++;
       
       source.append("    ").append(ps.readType.getName()).append(" ").append(tempName).append(" = rs.").append(ps.readerMethod).append("(").append(ps.columnIndex).append(");\n");
 
       if (ps.isNullable()) {
         // add NULL check
         source.append("    if (").append(tempName).append(" != null) {\n");
       }
       
       if (ps.isNeedsConversion()) {
         String sourceTempName = tempName;
         tempName = "c"+sourceTempName;
         source.append("    ").append(ps.writeType.getName()).append(" ").append(tempName).append(";\n");
         tempCounter = buildConversion(source, ps, tempName, sourceTempName, tempCounter);
       }
       
       // actually set the property value
       source.append("      ").append(ps.reference).append('.').append(ps.writerMethod).append("(").append(tempName).append(");\n");
       
       if (ps.isNullable()) {
         // complete NULL check and add else clause
         source.append("    }\n");
         source.append("    else {\n");
         source.append("      ").append(ps.reference).append('.').append(ps.writerMethod).append("(").append(getNullValue(ps)).append(");\n");
         source.append("    }\n");
       }
     }
     
     return tempCounter;
   }
 
   
   private static String getNullValue(PropertySetter ps) {
     if (ps.writeType.isPrimitive()) {
       if (ps.writeType.equals(boolean.class)) return "false";
       if (ps.writeType.equals(float.class) || ps.writeType.equals(double.class)) return "0.0";
       if (ps.writeType.equals(long.class)) return "0L";
       return "0";
     }
     
     return "null";
   }
   
   private static int buildConversion(StringBuilder source, PropertySetter ps, String tempName, String sourceTempName, int tempCounter) {
     if (ps.writeType.equals(String.class) && ps.readType.equals(String.class)) {
       /*
        * No actual type conversion, but end up here if the read column is CHAR type 
        * So we need to strip trailing space
        */
       source.append("    ").append(tempName).append(" = org.dt.japper.MapperUtils.trimRight(").append(sourceTempName).append(");\n");
       return tempCounter;
     }
     
     if (ps.writeType.equals(java.sql.Timestamp.class) && ps.readType.equals(java.sql.Date.class) ) {
       source.append("    ").append(tempName).append(" = new java.sql.Timestamp(").append(sourceTempName).append(".getTime());\n");
       return tempCounter;
     }
 
     if ( (ps.writeType.equals(long.class) || ps.writeType.equals(Long.class)) && (ps.readType.equals(java.sql.Timestamp.class) || ps.readType.equals(java.sql.Date.class)) ) {
       source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".getTime();\n");
       return tempCounter;
     }
 
     if (ps.writeType.equals(BigDecimal.class)) {
       if (ps.readType.equals(int.class) || ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
         source.append("    ").append(tempName).append(" = new java.math.BigDecimal(").append(sourceTempName).append(");\n");
         return tempCounter;
       }
     }
 
     if (ps.writeType.equals(int.class) || ps.writeType.equals(Integer.class)) {
       if (ps.readType.equals(float.class) || ps.readType.equals(double.class)) {
         source.append("    ").append(tempName).append(" = (int) ").append(sourceTempName).append(";\n");
         return tempCounter;
       }
       
       if (ps.readType.equals(BigDecimal.class)) {
         source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".intValue();\n");
         return tempCounter;
       }
     }
 
     if (ps.writeType.equals(float.class) || ps.writeType.equals(float.class)) {
       if (ps.readType.equals(int.class) || ps.readType.equals(double.class)) {
         source.append("    ").append(tempName).append(" = (float) ").append(sourceTempName).append(";\n");
         return tempCounter;
       }
       
       if (ps.readType.equals(BigDecimal.class)) {
         source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".floatValue();\n");
         return tempCounter;
       }
     }
 
     if (ps.writeType.equals(double.class) || ps.writeType.equals(double.class)) {
       if (ps.readType.equals(int.class) || ps.readType.equals(float.class)) {
         source.append("    ").append(tempName).append(" = (double) ").append(sourceTempName).append(";\n");
         return tempCounter;
       }
       
       if (ps.readType.equals(BigDecimal.class)) {
         source.append("    ").append(tempName).append(" = ").append(sourceTempName).append(".doubleValue();\n");
         return tempCounter;
       }
     }
     
     throw new IllegalArgumentException("Cannot convert from "+ps.readType.getName()+" to "+ps.writeType.getName());
   }
 
   
   
   
   private static String buildReference(PropertyDescriptor[] path, Map<String, String> graphGuardMap) {
     if (path.length == 1) return "dest";
     
     StringBuilder reference = new StringBuilder("dest");
     
     for (int i = 0; i < path.length-1; i++) {
       String referenceToHere = reference.toString();
       reference.append(".").append(path[i].getReadMethod().getName()).append("()");
       
       String guardReference = referenceToHere+"."+path[i].getName();
       if (!graphGuardMap.containsKey(guardReference)) {
         String guard = "    if ("+reference+" == null) "+referenceToHere+"."+path[i].getWriteMethod().getName()+"( new "+path[i].getPropertyType().getName()+"() );\n";
         graphGuardMap.put(guardReference, guard);
       }
     }
     
     return reference.toString();
   }
   
   private static int counter = 1;
   
   private static String makeNewClassName() {
     return "Mapper_"+counter++;
   }
   
   
   
   private static class PropertySetter {
     public final PropertyDescriptor descriptor;
     public final Class<?> writeType;
     public final String writerMethod;
     public final String reference;
     
     public final int columnIndex;
     public final int sqlType;
     public final boolean nullable;
     
     public final String readerMethod;
     public final Class<?> readType;
     
     public PropertySetter(int columnIndex, ResultSetMetaData metaData, PropertyDescriptor[] path, String reference) throws SQLException {
       this.descriptor = path[path.length-1];
       this.writeType = descriptor.getPropertyType();
       this.writerMethod = descriptor.getWriteMethod().getName();
       this.reference = reference;
       
       this.columnIndex = columnIndex;
       this.sqlType = metaData.getColumnType(columnIndex);
       this.nullable = ( metaData.isNullable(columnIndex) != ResultSetMetaData.columnNoNulls );
       
       switch(sqlType) {
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.CLOB:
           readerMethod = "getString";
           readType = String.class;
           break;
           
         case Types.TIMESTAMP:
         case -102:    // id of the Oracle TIMESTAMP WITH LOCAL TIME ZONE data type
           readerMethod = "getTimestamp";
           readType = Timestamp.class;
           break;
           
         case Types.DATE:
           readerMethod = "getDate";
           readType = java.sql.Date.class;
           break;
           
         case Types.INTEGER:
           if (writeType.isAssignableFrom(BigDecimal.class)) {
             readerMethod = "getBigDecimal";
             readType = BigDecimal.class;
           }
           else {
             readerMethod = "getInt";
             readType = int.class;
           }
           break;
           
         case Types.FLOAT:
           if (writeType.isAssignableFrom(BigDecimal.class)) {
             readerMethod = "getBigDecimal";
             readType = BigDecimal.class;
           }
           else {
             readerMethod = "getFloat";
             readType = float.class;
           }
           break;
           
         case Types.DOUBLE:
           if (writeType.isAssignableFrom(BigDecimal.class)) {
             readerMethod = "getBigDecimal";
             readType = BigDecimal.class;
           }
           else {
             readerMethod = "getDouble";
             readType = double.class;
           }
           break;
           
         case Types.NUMERIC:
           readerMethod = "getBigDecimal";
           readType = BigDecimal.class;
           break;
           
         case Types.BLOB:
           readerMethod = "org.dt.japper.lob.BlobReader.read";
           readType = byte[].class;
           break;
           
         default:
           readerMethod = null;
           readType = null;
       }
     }
     
     public boolean isNullable() {
       return !readType.isPrimitive() && nullable;
     }
     
     public boolean isBlob() { return sqlType == Types.BLOB; }
     
     public boolean isNeedsConversion() {
       return !writeType.isAssignableFrom(readType) || sqlType == Types.CHAR;
     }
   }
 }
