 /*
  * Copyright 2012 and beyond, Juan Luis Paz
  *
  * This file is part of Uaithne.
  *
  * Uaithne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Uaithne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Uaithne. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.uaithne.generator.processors.myBatis;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.processing.RoundEnvironment;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.MirroredTypeException;
 import javax.tools.Diagnostic;
 import javax.tools.FileObject;
 import javax.tools.StandardLocation;
 import org.uaithne.annotations.Comparator;
 import org.uaithne.annotations.myBatis.MyBatisCustomSqlStatementId;
 import org.uaithne.annotations.myBatis.MyBatisTypeHandler;
 import org.uaithne.annotations.sql.CustomSqlQuery;
 import org.uaithne.annotations.sql.JdbcType;
 import org.uaithne.annotations.sql.UseJdbcType;
 import org.uaithne.generator.commons.*;
 import org.uaithne.generator.processors.sql.SqlMappersProcessor;
 
 public abstract class MyBatisMappersProcessor extends SqlMappersProcessor {
 
     //<editor-fold defaultstate="collapsed" desc="Process">
     public void process(RoundEnvironment re, Element element) {
         TypeElement classElement = (TypeElement) element;
 
         ExecutorModuleInfo module = getGenerationInfo().getExecutorModuleByRealName(classElement);
         if (module == null) {
             processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "For create a myBatis mapper the annotated class must be an operation module", element);
             return;
         }
 
         String packageName = NamesGenerator.createGenericPackageName(classElement, subPackage());
         String name = mapperPrefix() + module.getNameUpper() + "Mapper";
 
         String namespace;
         if (packageName == null || packageName.isEmpty()) {
             namespace = name;
         } else {
             namespace = packageName + "." + name;
         }
 
         Writer writer = null;
         boolean hasUnimplementedOperations = false;
         try {
             FileObject fo = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, name + ".xml", element);
             writer = fo.openWriter();
             writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
             writer.write("<!DOCTYPE mapper PUBLIC '-//mybatis.org//DTD Mapper 3.0//EN' 'http://mybatis.org/dtd/mybatis-3-mapper.dtd'>\n");
             writer.write("<mapper namespace='");
             writer.write(namespace);
             writer.write("'>\n\n");
             for (OperationInfo operation : module.getOperations()) {
                 processOperation(operation, namespace, writer);
                 hasUnimplementedOperations = hasUnimplementedOperations || operation.isManually();
             }
             writer.write("</mapper>");
         } catch (IOException ex) {
             Logger.getLogger(MyBatisMappersProcessor.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if (writer != null) {
                     writer.close();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(MyBatisMappersProcessor.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         processMyBatisExecutorTemplate(module, packageName, namespace, name, hasUnimplementedOperations, element);
     }
 
     public boolean processMyBatisExecutorTemplate(ExecutorModuleInfo module, String packageName, String namespace, String name, boolean hasUnimplementedOperations, Element element) {
         GenerationInfo generationInfo = getGenerationInfo();
 
         HashMap<String, Object> data = createDefaultData();
         data.put("module", module);
         data.put("useAliasInOrderBy", useAliasInOrderBy());
 
         HashSet<String> imports = new HashSet<String>();
         DataTypeInfo.PAGE_INFO_DATA_TYPE.appendImports(packageName, imports);
         Utils.appendImportIfRequired(imports, packageName, "java.util.List");
         Utils.appendImportIfRequired(imports, packageName, module.getOperationPackage() + "." + module.getNameUpper() + "AbstractExecutor");
         module.appendNotMannuallyDefinitionImports(packageName, imports);
         if (module.isContainOrderedOperations()) {
             Utils.appendImportIfRequired(imports, packageName, "java.util.HashMap");
         }
         Utils.appendImportIfRequired(imports, packageName, "java.util.ArrayList");
 
         boolean generateGetSession = generationInfo.getSharedMyBatisPackage() != null;
         if (generateGetSession) {
             Utils.appendImportIfRequired(imports, packageName, generationInfo.getSharedMyBatisPackageDot() + "SqlSessionProvider");
         }
 
         data.put("imports", imports);
         data.put("namespace", namespace);
         data.put("abstractExecutorName", module.getNameUpper() + "AbstractExecutor");
         data.put("abstractClass", hasUnimplementedOperations || !generateGetSession);
         data.put("generateGetSession", generateGetSession);
         return processClassTemplate("operations/myBatis/MyBatisExecutor.ftl", packageName, name, data, element);
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Process operation">
     public void processOperation(OperationInfo operation, String namespace, Writer writer) throws IOException {
         if (operation.isManually()) {
             return;
         }
         MyBatisCustomSqlStatementId myBatisCustomSqlStatement = operation.getAnnotation(MyBatisCustomSqlStatementId.class);
         if (myBatisCustomSqlStatement != null) {
             String statementId = myBatisCustomSqlStatement.value();
             if (statementId.trim().isEmpty()) {
                 processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You must provide a valid mybatis statement id", operation.getElement());
             }
             operation.getExtraInfo().put("myBatisStatementId", statementId);
             if (operation.getOperationKind() == OperationKind.SELECT_PAGE) {
                 String countStatementId = myBatisCustomSqlStatement.countStatementId();
                 if (countStatementId.trim().isEmpty()) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You must provide a valid mybatis statement id for count the numbers of the rows", operation.getElement());
                 }
                 operation.getExtraInfo().put("myBatisCountStatementId", countStatementId);
             } else {
                 String countStatementId = myBatisCustomSqlStatement.countStatementId();
                 if (!countStatementId.isEmpty()) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "For this kind of operation is not allowed provide a mybatis statement id for count the numbers of the rows", operation.getElement());
                 }
             }
             return;
         }
         EntityInfo entity = operation.getEntity();
         if (entity != null) {
             entity = entity.getCombined();
         }
 
         switch (operation.getOperationKind()) {
             case CUSTOM: {
             }
             break;
             case SELECT_ONE: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName());
                 String[] query = getSelectOneQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedName(),
                             operation.getRealReturnDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case SELECT_MANY: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName());
                 String[] query = getSelectManyQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedName(),
                             operation.getRealOneItemReturnDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case SELECT_PAGE: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName() + "Page");
                 operation.getExtraInfo().put("myBatisCountStatementId", namespace + "." + operation.getMethodName() + "Count");
                 String[] query = getSelectPageQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName() + "Page",
                             operation.getDataType().getQualifiedName(),
                             operation.getRealOneItemReturnDataType().getQualifiedName(),
                             query);
                 }
                 query = getSelectPageCountQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName() + "Count",
                             operation.getDataType().getQualifiedName(),
                             DataTypeInfo.PAGE_INFO_DATA_TYPE.getQualifiedName(),
                             query);
                 }
             }
             break;
             case DELETE_BY_ID: {
                 String[] query = getEntityDeleteByIdQuery(entity);
                 if (query != null) {
                     writeDelete(writer,
                             operation.getMethodName(),
                             entity.getFirstIdField().getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case INSERT: {
                 String[] query = getEntityLastInsertedIdQuery(entity);
                 if (query != null) {
                     writeSelectWithoutParameter(writer,
                             "lastInsertedIdFor" + entity.getDataType().getSimpleName(),
                             operation.getRealReturnDataType().getQualifiedName(),
                             query);
                 }
                 query = getEntityInsertQuery(entity);
                 if (query != null) {
                     writeInsert(writer,
                             "insert" + entity.getDataType().getSimpleName(),
                             entity.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case JUST_INSERT: {
             }
             break;
             case SAVE: {
             }
             break;
             case JUST_SAVE: {
             }
             break;
             case SELECT_BY_ID: {
                 String[] query = getEntitySelectByIdQuery(entity);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName(),
                             entity.getFirstIdField().getDataType().getQualifiedName(),
                             operation.getRealReturnDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case UPDATE: {
                 String[] query = getEntityUpdateQuery(entity);
                 if (query != null) {
                     writeUpdate(writer,
                             "update" + entity.getDataType().getSimpleName(),
                             entity.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case CUSTOM_INSERT: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName());
                 String[] query = getCustomInsertQuery(operation);
                 if (query != null) {
                     writeInsert(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case CUSTOM_UPDATE: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName());
                 String[] query = getCustomUpdateQuery(operation);
                 if (query != null) {
                     writeUpdate(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case CUSTOM_DELETE: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName());
                 String[] query = getCustomDeleteQuery(operation);
                 if (query != null) {
                     writeDelete(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case CUSTOM_INSERT_WITH_ID: {
                 operation.getExtraInfo().put("myBatisStatementId", namespace + "." + operation.getMethodName());
                 String[] query = getCustomInsertQuery(operation);
                 if (query != null) {
                     writeInsert(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
             case MERGE: {
                 String[] query = getEntityMergeQuery(entity);
                 if (query != null) {
                     writeUpdate(writer,
                             "merge" + entity.getDataType().getSimpleName(),
                             entity.getDataType().getQualifiedName(),
                             query);
                 }
             }
             break;
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Write xml entries">
     public void writeUpdate(Writer writer, String id, String parameterType, String[] lines) throws IOException {
         writer.write("    <update id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("'>\n");
         if (lines != null) {
             for (int i = 0; i < lines.length; i++) {
                 writer.write("        ");
                 writer.write(lines[i]);
                 writer.write("\n");
             }
         }
         writer.write("    </update>\n\n");
     }
 
     public void writeSelect(Writer writer, String id, String parameterType, String resultType, String[] lines) throws IOException {
         writer.write("    <select id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("' resultType='");
         writer.write(resultType);
         writer.write("'>\n");
         if (lines != null) {
             for (int i = 0; i < lines.length; i++) {
                 writer.write("        ");
                 writer.write(lines[i]);
                 writer.write("\n");
             }
         }
         writer.write("    </select>\n\n");
     }
 
     public void writeSelectWithoutParameter(Writer writer, String id, String resultType, String[] lines) throws IOException {
         writer.write("    <select id='");
         writer.write(id);
         writer.write("' resultType='");
         writer.write(resultType);
         writer.write("'>\n");
         if (lines != null) {
             for (int i = 0; i < lines.length; i++) {
                 writer.write("        ");
                 writer.write(lines[i]);
                 writer.write("\n");
             }
         }
         writer.write("    </select>\n\n");
     }
 
     public void writeInsert(Writer writer, String id, String parameterType, String[] lines) throws IOException {
         writer.write("    <insert id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("'>\n");
         if (lines != null) {
             for (int i = 0; i < lines.length; i++) {
                 writer.write("        ");
                 writer.write(lines[i]);
                 writer.write("\n");
             }
         }
         writer.write("    </insert>\n\n");
     }
 
     public void writeDelete(Writer writer, String id, String parameterType, String[] lines) throws IOException {
         writer.write("    <delete id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("'>\n");
         if (lines != null) {
             for (int i = 0; i < lines.length; i++) {
                 writer.write("        ");
                 writer.write(lines[i]);
                 writer.write("\n");
             }
         }
         writer.write("    </delete>\n\n");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Order by">
     @Override
     public void appendOrderBy(StringBuilder result, FieldInfo orderBy, CustomSqlQuery customQuery) {
         if (customQuery != null) {
             if (hasQueryValue(customQuery.orderBy())) {
                 result.append("\norder by");
                 appendToQueryln(result, customQuery.beforeOrderByExpression(), "    ");
                 appendToQueryln(result, customQuery.orderBy(), "    ");
                 appendToQueryln(result, customQuery.afterOrderByExpression(), "    ");
             } else if (hasQueryValue(customQuery.beforeOrderByExpression()) || hasQueryValue(customQuery.afterOrderByExpression())) {
                 result.append("\norder by");
                 appendToQueryln(result, customQuery.beforeOrderByExpression(), "    ");
                 if (orderBy != null) {
                     if (orderBy.isOptional()) {
                         result.append("\n    <if test='");
                         result.append(orderBy.getName());
                         result.append(" != null'> ${");
                         result.append(orderBy.getName());
                         result.append("} </if>");
                     } else {
                         result.append("\n    ${");
                         result.append(orderBy.getName());
                         result.append("}");
                     }
                 }
                 appendToQueryln(result, customQuery.afterOrderByExpression(), "    ");
             } else if (orderBy != null) {
                 if (orderBy.isOptional()) {
                     result.append("\n<if test='");
                     result.append(orderBy.getName());
                     result.append(" != null'> order by ${");
                     result.append(orderBy.getName());
                     result.append("} </if>");
                 } else {
                     result.append("\norder by ${");
                     result.append(orderBy.getName());
                     result.append("}");
                 }
             }
         } else if (orderBy != null) {
             if (orderBy.isOptional()) {
                 result.append("\n<if test='");
                 result.append(orderBy.getName());
                 result.append(" != null'> order by ${");
                 result.append(orderBy.getName());
                 result.append("} </if>");
             } else {
                 result.append("\norder by ${");
                 result.append(orderBy.getName());
                 result.append("}");
             }
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Parameter">
     @Override
     public String getParameterValue(FieldInfo field) {
         return "#{" + field.getName() + getJdbcType(field) + getTypeHandler(field) + "}";
     }
 
     public String getJdbcType(FieldInfo field) {
         UseJdbcType ujt = field.getAnnotation(UseJdbcType.class);
         JdbcType jdbcType;
         if (ujt != null) {
             jdbcType = ujt.value();
         } else {
             jdbcType = null;
         }
         if (jdbcType != null) {
             return ",jdbcType=" + jdbcType.name();
         }
 
         String name = field.getDataType().getSimpleName();
         if ("String".equals(name)) {
             return ",jdbcType=VARCHAR";
         } else if ("Date".equals(name)) {
             return ",jdbcType=TIMESTAMP";
         } else if ("Time".equals(name)) {
             return ",jdbcType=TIME";
         } else if ("Timestamp".equals(name)) {
             return ",jdbcType=TIMESTAMP";
         } else if ("List<String>".equals(name)) {
             return ",jdbcType=VARCHAR";
         } else if ("ArrayList<String>".equals(name)) {
             return ",jdbcType=VARCHAR";
         } else if ("List<Date>".equals(name)) {
             return ",jdbcType=TIMESTAMP";
         } else if ("ArrayList<Date>".equals(name)) {
             return ",jdbcType=TIMESTAMP";
         } else if ("List<Time>".equals(name)) {
             return ",jdbcType=TIME";
         } else if ("ArrayList<Time>".equals(name)) {
             return ",jdbcType=TIME";
         } else if ("List<Timestamp>".equals(name)) {
             return ",jdbcType=TIMESTAMP";
         } else if ("ArrayList<Timestamp>".equals(name)) {
             return ",jdbcType=TIMESTAMP";
         } else {
             return ",jdbcType=NUMERIC";
         }
     }
 
     public String getTypeHandler(FieldInfo field) {
         MyBatisTypeHandler th = field.getAnnotation(MyBatisTypeHandler.class);
 
         if (th == null) {
             return "";
         }
 
         DataTypeInfo typeHandler;
         try {
             typeHandler = NamesGenerator.createResultDataType(th.value());
         } catch (MirroredTypeException ex) {
             // See: http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
             typeHandler = NamesGenerator.createDataTypeFor(ex.getTypeMirror());
         }
         if (typeHandler == null) {
             processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the type handler", field.getElement());
             return "";
         }
 
         if (typeHandler == null) {
             return "";
         }
 
         return ",typeHandler=" + typeHandler.getQualifiedName();
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Where">
     @Override
     public void appendStartWhere(StringBuilder result) {
         result.append("<where>");
     }
 
     @Override
     public void appendEndWhere(StringBuilder result) {
         result.append("</where>");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Set">
     @Override
     public void appendStartSet(StringBuilder result) {
         result.append("<set>");
     }
 
     @Override
     public void appendEndSet(StringBuilder result) {
         result.append("</set>");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="If (not) null">
     @Override
     public void appendConditionStartIfNull(StringBuilder result, FieldInfo field) {
         result.append("<if test='");
         result.append(field.getName());
         result.append(" == null'>");
     }
 
     @Override
     public void appendConditionStartIfNotNull(StringBuilder result, FieldInfo field) {
         result.append("<if test='");
         result.append(field.getName());
         result.append(" != null'>");
     }
 
     @Override
     public void appendConditionEndIf(StringBuilder result) {
         result.append("</if>");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Comparators">
     @Override
     public String translateComparator(Comparator comparator) {
         if (comparator == null) {
             return null;
         }
         switch (comparator) {
             case EQUAL:      return "[[column]] = [[value]]";
             case NOT_EQUAL:  return "[[column]] &lt;&gt; [[value]]";
             case EQUAL_NULLABLE:         return "<if test='[[name]] != null'> [[column]] = [[value]] </if> <if test='[[name]] == null'> [[column]] is null </if>";
             case NOT_EQUAL_NULLABLE:     return "<if test='[[name]] != null'> [[column]] &lt;&gt; [[value]] </if> <if test='[[name]] == null'> [[column]] is not null </if>";
             case SMALLER:    return "[[column]] &lt; [[value]]";
             case LARGER:     return "[[column]] &gt; [[value]]";
             case SMALL_AS:   return "[[column]] &lt;= [[value]]";
             case LARGER_AS:  return "[[column]] &gt;= [[value]]";
             case IN:         return "[[column]] in <foreach close=')' collection='[[name]]' item='_item_[[name]]' open='(' separator=','> #{_item_[[name]][[jdbcType]][[typeHandler]]} </foreach>";
             case NOT_IN:     return "[[column]] not in <foreach close=')' collection='[[name]]' item='_item_[[name]]' open='(' separator=','> #{_item_[[name]][[jdbcType]][[typeHandler]]} </foreach>";
             case LIKE:       return "[[column]] like [[value]]";
             case NOT_LIKE:   return "[[column]] not like [[value]]";
             case LIKE_INSENSITIVE:       return "lower([[column]]) like lower([[value]])";
             case NOT_LIKE_INSENSITIVE:   return "lower([[column]]) not like lower([[value]])";
             case START_WITH:     return "[[column]] like ([[value]] || '%')";
             case NOT_START_WITH: return "[[column]] not like ([[value]] || '%')";
             case END_WITH:       return "[[column]] like ('%' || [[value]]";
             case NOT_END_WITH:   return "[[column]] not like ('%' || [[value]]";
             case START_WITH_INSENSITIVE:     return "lower([[column]]) like (lower([[value]]) || '%')";
             case NOT_START_WITH_INSENSITIVE: return "lower([[column]]) not like (lower([[value]]) || '%')";
             case END_WITH_INSENSITIVE:       return "lower([[column]]) like ('%' || lower([[value]])";
             case NOT_END_WITH_INSENSITIVE:   return "lower([[column]]) not like ('%' || lower([[value]])";
            case CONTAINS:       return "[[column]] like ('%' || [[value]] || '%')'";
            case NOT_CONTAINS:   return "[[column]] not like ('%' || [[value]] || '%')'";
             case CONTAINS_INSENSITIVE:       return "lower([[column]]) like ('%' || lower([[value]]) || '%')";
             case NOT_CONTAINS_INSENSITIVE:   return "lower([[column]]) not like ('%' || lower([[value]]) || '%')";
             default:
                 throw new IllegalArgumentException("Unimplemented comparator " + comparator);
         }
     }
 
     @Override
     public String getConditionElementValue(String rule, FieldInfo field, CustomSqlQuery customQuery) {
         if (rule == null) {
             return null;
         } else if ("column".equals(rule) || "COLUMN".equals(rule)) {
             return getColumnNameForWhere(field, customQuery);
         } else if ("name".equals(rule) || "NAME".equals(rule)) {
             return field.getName();
         } else if ("value".equals(rule) || "VALUE".equals(rule)) {
             return getParameterValue(field);
         } else if ("jdbcType".equals(rule) || "JDBC_TYPE".equals(rule)) {
             return getJdbcType(field);
         } else if ("typeHandler".equals(rule) || "TYPE_HANDLER".equals(rule)) {
             return getTypeHandler(field);
         } else {
             return null;
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Configuration">
     public abstract boolean useAliasInOrderBy();
     public abstract String subPackage();
     public abstract String mapperPrefix();
     //</editor-fold>
 }
