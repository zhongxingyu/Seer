 /*******************************************************************************
  * Copyright (c) 2011 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     Mike Norman - June 10 2011, created DDL parser package
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.util;
 
 //javase imports
 import java.io.Reader;
 import java.io.StringReader;
 import java.sql.CallableStatement;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ServiceLoader;
 import java.util.Stack;
 import java.util.TreeSet;
 
 //DDL parser imports
 import org.eclipse.persistence.tools.oracleddl.metadata.CompositeDatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.DatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.FieldType;
 import org.eclipse.persistence.tools.oracleddl.metadata.FunctionType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ObjectTableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ObjectType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLCursorType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLPackageType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLRecordType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ProcedureType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ROWTYPEType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TYPEType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.UnresolvedType;
 import org.eclipse.persistence.tools.oracleddl.metadata.VArrayType;
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.UnresolvedTypesVisitor;
 import org.eclipse.persistence.tools.oracleddl.parser.DDLParser;
 import org.eclipse.persistence.tools.oracleddl.parser.ParseException;
 
 public class DatabaseTypeBuilder {
   
     //misc. string constants
     public static final String BEGIN = "BEGIN";
     public static final String END = "END";
     public static final String FORWARD_SLASH = "/";
     public static final String NOT ="NOT ";
     public static final String OR =" OR ";
     public static final String RESULT ="RESULT";
     public static final String PERCENT = "%";
     public static final String NEW_LINE = "\n";
     public static final String TOPLEVEL = "TOPLEVEL";
     public static final String ROWTYPE_MACRO = PERCENT + "ROWTYPE";
     public static final String TYPE_MACRO = PERCENT + "TYPE";
     public static final String DOT = ".";
     public static final String TRANSFORM_PREFIX =
         "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'";
     public static final String DBMS_METADATA_GET_DDL_STMT_PREFIX =
         "SELECT DBMS_METADATA.GET_DDL('";
     public static final String DBMS_METADATA_GET_DDL_STMT1 =
         "', AO.OBJECT_NAME) AS " + RESULT + " FROM ALL_OBJECTS AO WHERE ";
     public static final String EXCLUDED_ADMIN_SCHEMAS =
         "'*SYS*|XDB|*ORD*|DBSNMP|ANONYMOUS|OUTLN|MGMT_VIEW|SI_INFORMTN_SCHEMA|WK_TEST|WKPROXY'";
     public static final String DBMS_METADATA_GET_DDL_STMT_STMT2 =
         "REGEXP_LIKE(OWNER,?) AND";
     public static final String DBMS_METADATA_GET_DDL_STMT_STMT3 =
         " AO.STATUS = 'VALID' AND AO.OBJECT_TYPE = ? AND";
     public static final String DBMS_METADATA_GET_DDL_STMT_SUFFIX =
         " AO.OBJECT_NAME LIKE ?";
     //OBJECT_TYPE codes from ALL_OBJECTS view - we are only interested in top-level types:
     public static final int OBJECT_TYPE_UNKNOWN_CODE = -1;
     public static final String ALL_OBJECTS_OBJECT_TYPE_FIELD = "OBJECT_TYPE";
     public static final String OBJECT_TYPE_FUNCTION = "FUNCTION";
     public static final int OBJECT_TYPE_FUNCTION_CODE = 1;
     public static final String OBJECT_TYPE_PACKAGE = "PACKAGE";
     public static final int OBJECT_TYPE_PACKAGE_CODE = 2;
     public static final String OBJECT_TYPE_PROCEDURE = "PROCEDURE";
     public static final int OBJECT_TYPE_PROCEDURE_CODE = 3;
     public static final String OBJECT_TYPE_TABLE = "TABLE";
     public static final int OBJECT_TYPE_TABLE_CODE = 4;
     public static final String OBJECT_TYPE_TYPE = "TYPE";
     public static final int OBJECT_TYPE_TYPE_CODE = 5;
     public static final String GET_OBJECT_TYPE_STMT =
         "SELECT DECODE(AO." + ALL_OBJECTS_OBJECT_TYPE_FIELD +
             ", '" + OBJECT_TYPE_FUNCTION + "', " + OBJECT_TYPE_FUNCTION_CODE +
             ", '" + OBJECT_TYPE_PACKAGE + "', " + OBJECT_TYPE_PACKAGE_CODE +
             ", '" + OBJECT_TYPE_PROCEDURE + "', " + OBJECT_TYPE_PROCEDURE_CODE +
             ", '" + OBJECT_TYPE_TABLE + "', " + OBJECT_TYPE_TABLE_CODE +
             ", '" + OBJECT_TYPE_TYPE + "', " + OBJECT_TYPE_TYPE_CODE +
             "," + OBJECT_TYPE_UNKNOWN_CODE + ") AS OBJECT_TYPE FROM ALL_OBJECTS AO WHERE " +
                 "(STATUS = 'VALID' AND OWNER LIKE ? AND OBJECT_NAME = ?)";
 
     static DBMSMetadataSessionTransforms TRANSFORMS_FACTORY;
     static {
         ServiceLoader<DBMSMetadataSessionTransforms> transformsFactories =
             ServiceLoader.load(DBMSMetadataSessionTransforms.class);
         Iterator<DBMSMetadataSessionTransforms> i = transformsFactories.iterator();
         //we are only expecting one transforms factory - any additional are ignored
         if (i.hasNext()) {
             TRANSFORMS_FACTORY = i.next();
         }
         else {
             TRANSFORMS_FACTORY = null;
         }
     }
 
     protected boolean transformsSet = false;
 
     public DatabaseTypeBuilder() {
         super();
     }
 
     public List<TableType> buildTables(Connection conn, String schemaPattern,
         String tablePattern) throws ParseException {
         return buildTables(conn, schemaPattern, tablePattern, true);
     }
     protected List<TableType> buildTables(Connection conn, String schemaPattern,
         String tablePattern, boolean resolveTypes) throws ParseException {
         List<String> schemaPatterns = new ArrayList<String>();
         schemaPatterns.add(schemaPattern);
         List<String> tablePatterns = new ArrayList<String>();
         tablePatterns.add(tablePattern);
         return buildTables(conn, schemaPatterns, tablePatterns, resolveTypes);
     }
     public List<TableType> buildTables(Connection conn, List<String> schemaPatterns,
         List<String> tablePatterns) throws ParseException {
         return buildTables(conn, schemaPatterns, tablePatterns, true);
     }
     protected List<TableType> buildTables(Connection conn, List<String> schemaPatterns,
         List<String> tablePatterns, boolean resolveTypes) throws ParseException {
         List<TableType> tableTypes = null;
         List<String> copyOfSchemaPatterns = new ArrayList<String>();
         List<String> copyOfTablePatterns = new ArrayList<String>();
         String getDDlStmt = buildDDLStmt(OBJECT_TYPE_TABLE, schemaPatterns, tablePatterns,
             copyOfSchemaPatterns, copyOfTablePatterns);
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, OBJECT_TYPE_TABLE, getDDlStmt, copyOfSchemaPatterns,
                 copyOfTablePatterns);
             if (ddls != null) {
                 //need 'set' semantics to ensure no duplicates; using TreeSet also sorts
                 TreeSet<String> distinctDDLs = new TreeSet<String>();
                 distinctDDLs.addAll(ddls);
                 tableTypes = new ArrayList<TableType>();
                 for (String ddl : distinctDDLs) {
                     DDLParser parser = newDDLParser(ddl, copyOfSchemaPatterns);
                     TableType tableType = parser.parseTable();
                     if (tableType != null) {
                         tableTypes.add(tableType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(tableType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 resolvedTypes(conn, tableType.getSchema(), parser,
                                     unresolvedTypesVisitor.getUnresolvedTypes(), tableType);
                             }
                         }
                     }
                 }
             }
         }
         return tableTypes;
     }
 
     public List<PLSQLPackageType> buildPackages(Connection conn, String schemaPattern,
         String packagePattern) throws ParseException {
         return buildPackages(conn, schemaPattern, packagePattern, true);
     }
     protected List<PLSQLPackageType> buildPackages(Connection conn, String schemaPattern,
         String packagePattern, boolean resolveTypes) throws ParseException {
         List<String> schemaPatterns = new ArrayList<String>();
         schemaPatterns.add(schemaPattern);
         List<String> packagePatterns = new ArrayList<String>();
         packagePatterns.add(packagePattern);
         return buildPackages(conn, schemaPatterns, packagePatterns, resolveTypes);
     }
     public List<PLSQLPackageType> buildPackages(Connection conn, List<String> schemaPatterns,
         List<String> packagePatterns) throws ParseException {
         return buildPackages(conn, schemaPatterns, packagePatterns, true);
     }
     protected List<PLSQLPackageType> buildPackages(Connection conn, List<String> schemaPatterns,
         List<String> packagePatterns, boolean resolveTypes) throws ParseException {
         List<PLSQLPackageType> packageTypes = null;
         List<String> copyOfSchemaPatterns = new ArrayList<String>();
         List<String> copyOfPackagePatterns = new ArrayList<String>();
         String getDDlStmt = buildDDLStmt(OBJECT_TYPE_PACKAGE, schemaPatterns, packagePatterns,
             copyOfSchemaPatterns, copyOfPackagePatterns);
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, OBJECT_TYPE_PACKAGE, getDDlStmt, copyOfSchemaPatterns,
                 copyOfPackagePatterns);
             if (ddls != null) {
                 //need 'set' semantics to ensure no duplicates
                 TreeSet<String> distinctDDLs = new TreeSet<String>();
                 distinctDDLs.addAll(ddls);
                 packageTypes = new ArrayList<PLSQLPackageType>();
                 for (String ddl : distinctDDLs) {
                     DDLParser parser = newDDLParser(ddl, copyOfSchemaPatterns);
                     PLSQLPackageType packageType = parser.parsePLSQLPackage();
                     if (packageType != null) {
                         packageTypes.add(packageType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(packageType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 resolvedTypes(conn, packageType.getSchema(), parser, 
                                         unresolvedTypesVisitor.getUnresolvedTypes(), packageType, packageTypes);
                             }
                         }
                     }
                 }
             }
         }
         return packageTypes;
     }
 
     public List<ProcedureType> buildProcedures(Connection conn, String schemaPattern,
         String procedurePattern) throws ParseException {
         return buildProcedures(conn, schemaPattern, procedurePattern, true);
     }
     protected List<ProcedureType> buildProcedures(Connection conn, String schemaPattern,
         String procedurePattern, boolean resolveTypes) throws ParseException {
         List<String> schemaPatterns = new ArrayList<String>();
         schemaPatterns.add(schemaPattern);
         List<String> procedurePatterns = new ArrayList<String>();
         procedurePatterns.add(procedurePattern);
         return buildProcedures(conn, schemaPatterns, procedurePatterns, resolveTypes);
     }
     public List<ProcedureType> buildProcedures(Connection conn, List<String> schemaPatterns,
         List<String> procedurePatterns) throws ParseException {
         return buildProcedures(conn, schemaPatterns, procedurePatterns, true);
     }
     protected List<ProcedureType> buildProcedures(Connection conn, List<String> schemaPatterns,
         List<String> procedurePatterns, boolean resolveTypes) throws ParseException {
         List<ProcedureType> procedureTypes = null;
         List<String> copyOfSchemaPatterns = new ArrayList<String>();
         List<String> copyOfProcedurePatterns = new ArrayList<String>();
         String getDDlStmt = buildDDLStmt(OBJECT_TYPE_PROCEDURE, schemaPatterns, procedurePatterns,
             copyOfSchemaPatterns, copyOfProcedurePatterns);
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, OBJECT_TYPE_PROCEDURE, getDDlStmt, copyOfSchemaPatterns,
                 copyOfProcedurePatterns);
             if (ddls != null) {
                 //need 'set' semantics to ensure no duplicates
                 TreeSet<String> distinctDDLs = new TreeSet<String>();
                 distinctDDLs.addAll(ddls);
                 procedureTypes = new ArrayList<ProcedureType>();
                 for (String ddl : distinctDDLs) {
                     DDLParser parser = newDDLParser(ddl, copyOfSchemaPatterns);
                     ProcedureType procedureType = parser.parseTopLevelProcedure();
                     if (procedureType != null) {
                         procedureTypes.add(procedureType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(procedureType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 resolvedTypes(conn, procedureType.getSchema(), parser,
                                     unresolvedTypesVisitor.getUnresolvedTypes(), procedureType);
                             }
                         }
                     }
                 }
             }
         }
         return procedureTypes;
     }
 
     public List<FunctionType> buildFunctions(Connection conn, String schemaPattern,
         String functionPattern) throws ParseException {
         return buildFunctions(conn, schemaPattern, functionPattern, true);
     }
     protected List<FunctionType> buildFunctions(Connection conn, String schemaPattern,
         String functionPattern, boolean resolveTypes) throws ParseException {
         List<String> schemaPatterns = new ArrayList<String>();
         schemaPatterns.add(schemaPattern);
         List<String> functionPatterns = new ArrayList<String>();
         functionPatterns.add(functionPattern);
         return buildFunctions(conn, schemaPatterns, functionPatterns, resolveTypes);
     }
     public List<FunctionType> buildFunctions(Connection conn, List<String> schemaPatterns,
         List<String> functionPatterns) throws ParseException {
         return buildFunctions(conn, schemaPatterns, functionPatterns, true);
     }
     protected List<FunctionType> buildFunctions(Connection conn, List<String> schemaPatterns,
         List<String> functionPatterns, boolean resolveTypes) throws ParseException {
         List<FunctionType> functionsTypes = null;
         List<String> copyOfSchemaPatterns = new ArrayList<String>();
         List<String> copyOfFunctionPatterns = new ArrayList<String>();
         String getDDlStmt = buildDDLStmt(OBJECT_TYPE_FUNCTION, schemaPatterns, functionPatterns,
             copyOfSchemaPatterns, copyOfFunctionPatterns);
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, OBJECT_TYPE_FUNCTION, getDDlStmt, copyOfSchemaPatterns,
                 copyOfFunctionPatterns);
             if (ddls != null) {
                 //need 'set' semantics to ensure no duplicates
                 TreeSet<String> distinctDDLs = new TreeSet<String>();
                 distinctDDLs.addAll(ddls);
                 functionsTypes = new ArrayList<FunctionType>();
                 for (String ddl : distinctDDLs) {
                     DDLParser parser = newDDLParser(ddl, copyOfSchemaPatterns);
                     FunctionType functionType = parser.parseTopLevelFunction();
                     if (functionType != null) {
                         functionsTypes.add(functionType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(functionType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 resolvedTypes(conn, functionType.getSchema(), parser,
                                     unresolvedTypesVisitor.getUnresolvedTypes(), functionType);
                             }
                         }
                     }
                 }
             }
         }
         return functionsTypes;
     }
 
     public List<CompositeDatabaseType> buildTypes(Connection conn, String schemaPattern,
         String namePattern) throws ParseException {
         return buildTypes(conn, schemaPattern, namePattern, true);
     }
     protected List<CompositeDatabaseType> buildTypes(Connection conn, String schemaPattern,
         String namePattern, boolean resolveTypes) throws ParseException {
         List<String> schemaPatterns = new ArrayList<String>();
         schemaPatterns.add(schemaPattern);
         List<String> namePatterns = new ArrayList<String>();
         namePatterns.add(namePattern);
         return buildTypes(conn, schemaPatterns, namePatterns, resolveTypes);
     }
     public List<CompositeDatabaseType> buildTypes(Connection conn, List<String> schemaPatterns,
         List<String> namePatterns) throws ParseException {
         return buildTypes(conn, schemaPatterns, namePatterns, true);
     }
     protected List<CompositeDatabaseType> buildTypes(Connection conn, List<String> schemaPatterns,
         List<String> namePatterns, boolean resolveTypes) throws ParseException {
         List<CompositeDatabaseType> databaseTypes = null;
         List<String> copyOfSchemaPatterns = new ArrayList<String>();
         List<String> copyOfNamePatterns = new ArrayList<String>();
         String getDDlStmt = buildDDLStmt(OBJECT_TYPE_TYPE, schemaPatterns, namePatterns,
             copyOfSchemaPatterns, copyOfNamePatterns);
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, OBJECT_TYPE_TYPE, getDDlStmt, copyOfSchemaPatterns,
                 copyOfNamePatterns);
             if (ddls != null) {
                 //need 'set' semantics to ensure no duplicates
                 TreeSet<String> distinctDDLs = new TreeSet<String>();
                 distinctDDLs.addAll(ddls);
                 databaseTypes = new ArrayList<CompositeDatabaseType>();
                 for (String ddl : distinctDDLs) {
                     DDLParser parser = newDDLParser(ddl, copyOfSchemaPatterns);
                     CompositeDatabaseType databaseType = parser.parseType();
                     if (databaseType != null) {
                         databaseTypes.add(databaseType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(databaseType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 String schemaPattern = null;
                                 if (databaseType.isObjectTableType()) {
                                     schemaPattern = ((ObjectTableType)databaseType).getSchema();
                                 }
                                 else if (databaseType.isObjectType()) {
                                     schemaPattern = ((ObjectType)databaseType).getSchema();
                                 }
                                 else if (databaseType.isVArrayType()) {
                                     schemaPattern = ((VArrayType)databaseType).getSchema();
                                 }
                                 else {
                                     schemaPattern = PERCENT;
                                 }
                                 resolvedTypes(conn, schemaPattern, parser,
                                     unresolvedTypesVisitor.getUnresolvedTypes(), databaseType);
                             }
                         }
                     }
                 }
             }
         }
         return databaseTypes;
     }
 
     protected String buildDDLStmt(String objectType, List<String> schemaPatterns,
         List<String> namePatterns, List<String> copyOfSchemaPatterns,
         List<String> copyOfNamePatterns) {
         StringBuilder sb = new StringBuilder();
         sb.append(DBMS_METADATA_GET_DDL_STMT_PREFIX);
         sb.append(objectType);
         sb.append(DBMS_METADATA_GET_DDL_STMT1);
         for (int i = 0, len = schemaPatterns.size(); i < len; i++) {
             String schemaPattern = schemaPatterns.get(i);
             String schemaPatternU = schemaPattern == null ? null : schemaPattern.toUpperCase();
             String namePattern = namePatterns.get(i);
             String namePatternU = namePattern == null ? null : namePattern.toUpperCase();
             copyOfNamePatterns.add(namePatternU);
             sb.append('(');
             if (schemaPatternExcludesAdminSchemas(schemaPatternU)) {
                 sb.append(NOT);
                 copyOfSchemaPatterns.add(EXCLUDED_ADMIN_SCHEMAS);
             }
             else {
                 copyOfSchemaPatterns.add(schemaPatternU);
             }
             sb.append(DBMS_METADATA_GET_DDL_STMT_STMT2);
             sb.append(DBMS_METADATA_GET_DDL_STMT_STMT3);
             sb.append(DBMS_METADATA_GET_DDL_STMT_SUFFIX);
             sb.append(')');
             if (i < len -1) {
                 sb.append(OR);
             }
         }
         return sb.toString();
     }
 
     protected DDLParser newDDLParser(String ddl, List<String> schemaPatterns) {
         DDLParser parser = new DDLParser(new StringReader(ddl));
         parser.setTypesRepository(new DatabaseTypesRepository());
         parser.setSchemaPatterns(schemaPatterns);
         return parser;
     }
 
     protected List<String> getDDLs(Connection conn, String typeSpec, String getDDlStmt,
         List<String> schemaPatterns, List<String> typeNamePatterns) {
         List<String> ddls = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try {
             pStmt = conn.prepareStatement(getDDlStmt);
             int j = 0;
             for (int i = 0, len = schemaPatterns.size(); i < len; i++) {
                 pStmt.setString(++j, schemaPatterns.get(i));
                 pStmt.setString(++j, typeSpec);
                 pStmt.setString(++j, typeNamePatterns.get(i));
             }
             rs = pStmt.executeQuery();
             if (rs.next()) {
                 ddls = new ArrayList<String>();
                 do {
                     Clob clob = rs.getClob(RESULT);
                     String ddl = null;
                     if (clob != null) {
                         Reader is = clob.getCharacterStream();
                         StringBuffer sb = new StringBuffer();
                         int length = (int)clob.length();
                         if (length > 0) {
                             char[] buffer = new char[length];
                             // Read stream and append to StringBuffer.
                             try {
                                 while (is.read(buffer) != -1) {
                                     sb.append(buffer);
                                 }
                             }
                             catch (Exception e) {
                                 //e.printStackTrace();
                             }
                             ddl = sb.toString().trim();
                         }
                     }
                     if (ddl != null) {
                         if (ddl.endsWith(FORWARD_SLASH)) {
                             ddl = (String)ddl.subSequence(0, ddl.length()-1);
                         }
                         ddls.add(ddl);
                     }
                 } while (rs.next());
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         finally {
             if (rs != null) {
                 try {
                     rs.close();
                 }
                 catch (SQLException e) {
                     // ignore
                 }
             }
             if (pStmt != null) {
                 try {
                     pStmt.close();
                 }
                 catch (SQLException e) {
                     // ignore
                 }
             }
         }
         return ddls;
     }
 
     /**
      * Attempt to resolve any types that the DDLParser could not resolve.
      *  
      */
     protected void resolvedTypes(Connection conn, String schemaPattern, DDLParser parser, List<UnresolvedType> unresolvedTypes, DatabaseType databaseType) throws ParseException {
         resolvedTypes(conn, schemaPattern, parser, unresolvedTypes, databaseType, null);
     }
     
     /**
      * Attempt to resolve any types that the DDLParser could not resolve.
      *  
      */
     protected void resolvedTypes(Connection conn, String schemaPattern, DDLParser parser,
         List<UnresolvedType> unresolvedTypes, DatabaseType databaseType, List<PLSQLPackageType> processedPackages) throws ParseException {
         
         // need to process non '%' named items 1st such that we can resolve the '%' ones later
         List<String> percentNameList = new ArrayList<String>();
         List<String> nonPercentNameList = new ArrayList<String>();
         for (UnresolvedType uType : unresolvedTypes) {
             CompositeDatabaseType owningType = uType.getOwningType();
             if (owningType != null && (owningType.isTYPEType() || owningType.isROWTYPEType())) {
                 if (!percentNameList.contains(uType.getTypeName())) {
                     percentNameList.add(uType.getTypeName());
                 }
             } else {
                 if (!nonPercentNameList.contains(uType.getTypeName())) {
                     nonPercentNameList.add(uType.getTypeName());
                 }
             }
         }
         // sort so dotted entries are last
         Collections.sort(percentNameList);
         Collections.sort(nonPercentNameList);
         // reverse so entries are put on the stack in the correct order
         Collections.reverse(percentNameList);
         Collections.reverse(nonPercentNameList);
         // iterate over the collections and add unresolved types based on type name
         List<UnresolvedType> percentList = new ArrayList<UnresolvedType>();
         for (String tname : percentNameList) {
             for (UnresolvedType uType : unresolvedTypes) {
                 if (uType.getTypeName().equals(tname)) {
                     percentList.add(uType);
                     break;
                 }
             }
         }
         List<UnresolvedType> nonPercentList = new ArrayList<UnresolvedType>();
         for (String tname : nonPercentNameList) {
             for (UnresolvedType uType : unresolvedTypes) {
                 if (uType.getTypeName().equals(tname)) {
                     nonPercentList.add(uType);
                     break;
                 }
             }
         }
         Stack<UnresolvedType> stac = new Stack<UnresolvedType>();
         // need to process non % entries first
         for (UnresolvedType uPercentType : percentList) {
             if (!stac.contains(uPercentType)) {
                 stac.push(uPercentType);
             }
         }
         for (UnresolvedType uNonPercentType : nonPercentList) {
             if (!stac.contains(uNonPercentType)) {
                 stac.push(uNonPercentType);
             }
         }
 
         boolean done = false;
         DatabaseTypesRepository typesRepository = parser.getTypesRepository();
         while (!done) {
             CompositeDatabaseType resolvedType = null;
             UnresolvedType uType = stac.pop();
             String typeName = uType.getTypeName();
             CompositeDatabaseType owningType = uType.getOwningType();
             int dotIdx = typeName.indexOf(DOT);
             String typeName1 = typeName;
             String typeName2 = null;
             // handle dotted scenario, i.e. "PackageName.TypeName" or "SchemaName.TypeName"
             if (dotIdx != -1) {
                 typeName1 = typeName.substring(0, dotIdx);
                 typeName2 = typeName.substring(dotIdx+1, typeName.length());
 
                 // handle second dotted scenario, i.e. "SchemaName.TableName.ColumnName"
                 dotIdx = typeName2.indexOf(DOT);
                 if (dotIdx != -1) {
                     String tmpStr = typeName2;
                     typeName1 = tmpStr.substring(0, dotIdx);
                     typeName2 = tmpStr.substring(dotIdx+1, tmpStr.length());
                 }
             }
             // check type repository first
             resolvedType = (CompositeDatabaseType)typesRepository.getDatabaseType(typeName);
             if (resolvedType == null) {
                 if (owningType.isROWTYPEType()) {
                     ROWTYPEType rType = (ROWTYPEType)owningType;
                     String tableName = rType.getTypeName();
                     if (tableName.contains(ROWTYPE_MACRO)) {
                         int idx = tableName.indexOf(ROWTYPE_MACRO);
                         tableName = tableName.substring(0,idx);
                     }
                     resolvedType = (CompositeDatabaseType)typesRepository.getDatabaseType(tableName);
                     if (resolvedType == null) {
                         TableType tableType = null;
                         List<TableType> tables = buildTables(conn, schemaPattern, tableName, false);
                         if (tables != null && tables.size() > 0) {
                             tableType = tables.get(0);
                             typesRepository.setDatabaseType(tableType.getTableName(), tableType);
                             rType.setEnclosedType(tableType);
                             typesRepository.setDatabaseType(rType.getTypeName(), rType);
                         }
                         resolvedType = tableType;
                         // always a chance that tableType has some unresolved column type
                         if (tableType != null && !tableType.isResolved()) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(tableType);
                             for (UnresolvedType u2Type : unresolvedTypesVisitor.getUnresolvedTypes()) {
                                 if (!stac.contains(u2Type)) {
                                     stac.push(u2Type);
                                 }
                             }
                         } else {
                             // may be dealing with a table from a different schema
                         }
                     } else {
                         uType.getOwningType().setEnclosedType(resolvedType);
                     }
                 } else if (owningType.isTYPEType()) {
                     TYPEType tType = (TYPEType)owningType;
                     DatabaseType foundType = findField(typeName1, databaseType);
                     if (foundType != null) {
                         if (typeName2 != null) {
                             foundType = findField(typeName2, foundType);
                         }
                         if (foundType != null) {
                             tType.setEnclosedType(foundType);
                             // may need to handle TYPEType's that go 'into' a  local variable
                             resolvedType = (CompositeDatabaseType)foundType;
                         }
                     }
                 }
                 if (resolvedType == null) {
                     int objectTypeCode = getObjectType(conn, schemaPattern, typeName1);
                     switch (objectTypeCode) {
                         case OBJECT_TYPE_FUNCTION_CODE :
                             List<FunctionType> functions = buildFunctions(conn, schemaPattern, typeName1, false);
                             if (functions != null && functions.size() > 0) {
                                 resolvedType = functions.get(0); // only care about first one
                             }
                             break;
                         case OBJECT_TYPE_PACKAGE_CODE :
                             PLSQLPackageType plsqlPkg = null;
                             // we may have already processed this package
                             if (processedPackages != null) {
                                 for (PLSQLPackageType pkg : processedPackages) {
                                     if (pkg.getPackageName().equals(typeName1)) {
                                         plsqlPkg = pkg;
                                     }
                                 }
                             }
                             if (plsqlPkg == null) {
                                 List<PLSQLPackageType> packages = buildPackages(conn, schemaPattern, typeName1, false);
                                 if (packages != null && packages.size() > 0) {
                                     plsqlPkg = packages.get(0);  // only care about first one
                                     processedPackages.add(plsqlPkg);
                                 }
                             }
                             if (plsqlPkg != null) {
                                 // we may have a dotted scenario, i.e. 'PackageName.TypeName'
                                 if (typeName2 == null) {
                                     resolvedType = plsqlPkg;
                                 } else {
                                     DatabaseType dbType = findField(typeName2, plsqlPkg);
                                     if (dbType != null && dbType.isComposite()) {
                                         resolvedType = (CompositeDatabaseType) dbType;
                                     }
                                 }
                             }
                             break;
                         case OBJECT_TYPE_PROCEDURE_CODE :
                             List<ProcedureType> procedures = buildProcedures(conn, schemaPattern, typeName1, false);
                             if (procedures != null && procedures.size() > 0) {
                                 resolvedType = procedures.get(0); // only care about first one
                             }
                             break;
                         case OBJECT_TYPE_TABLE_CODE :
                             List<TableType> tables = buildTables(conn, schemaPattern, typeName1, false);
                             if (tables != null && tables.size() > 0) {
                                 TableType tableType = tables.get(0); // only care about first one
                                 resolvedType = tableType;
                                 if (typeName2 != null) {
                                     DatabaseType foundType = findField(typeName2, resolvedType);
                                     if (foundType != null) {
                                         resolvedType = (CompositeDatabaseType)foundType;
                                     }
                                 }
                                 // always a chance that tableType has some unresolved column type
                                 if (!tableType.isResolved()) {
                                     UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                                     unresolvedTypesVisitor.visit(tableType);
                                     for (UnresolvedType u2Type : unresolvedTypesVisitor.getUnresolvedTypes()) {
                                         if (!stac.contains(u2Type)) {
                                             stac.push(u2Type);
                                         }
                                     }
                                 }
                             }
                             break;
                         case OBJECT_TYPE_TYPE_CODE :
                             List<CompositeDatabaseType> types = buildTypes(conn, schemaPattern, typeName1, false);
                             if (types != null && types.size() > 0) {
                                 resolvedType = types.get(0); // only care about first one
                                 if (typeName2 != null) {
                                     DatabaseType foundType = findField(typeName2, resolvedType);
                                     if (foundType != null) {
                                         resolvedType = (CompositeDatabaseType)foundType;
                                     }
                                 }
                             }
                             break;
                         case OBJECT_TYPE_UNKNOWN_CODE :
                         default :
                             break;
                     }
                 }
             }
             if (resolvedType != null) {
                 if (owningType.isPLSQLRecordType() && !(resolvedType.isFieldType())) {
                     PLSQLRecordType recordType = (PLSQLRecordType)owningType;
                     //special fixing-up for unresolved types in records
                     for (FieldType field : recordType.getFields()) {
                         if (!field.isResolved()) {
                             if (field.getEnclosedType().getTypeName().equals(resolvedType.getTypeName())) {
                                 field.setEnclosedType(resolvedType);
                             }
                         }
                     }
                 } else {
                     owningType.setEnclosedType(resolvedType);
                 }
                 typesRepository.setDatabaseType(resolvedType.getTypeName(), resolvedType);
                 
                 // update any other types in the unresolved list that reference the resolved type
                 for (UnresolvedType unresolvedType : unresolvedTypes) {
                     if (unresolvedType.getTypeName().equals(typeName)) {
                         if (unresolvedType.getOwningType().getEnclosedType() instanceof UnresolvedType) {
                             unresolvedType.getOwningType().setEnclosedType(resolvedType);
                         }
                     }
                 }
                 
                 // always a chance that resolvedType refers to something that is un-resolved
                 if (!resolvedType.isResolved()) {
                     UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                     unresolvedTypesVisitor.visit(resolvedType);
                     for (UnresolvedType u2Type : unresolvedTypesVisitor.getUnresolvedTypes()) {
                         if (!stac.contains(u2Type)) {
                             stac.push(u2Type);
                         }
                     }
                 }
             }
 
             if (stac.isEmpty()) {
                 done = true;
             }
         }
     }
 
     /**
      * Attempt to determine the database type (Function, Package, Table, etc.) for a 
      * given a Schema and Type name.
      * 
      * Types are translated to integer values as follows:
      * <ul>
      * <li>FUNCTION = 1</li>
      * <li>PACKAGE = 2</li>
      * <li>PROCEDURE = 3</li>
      * <li>TABLE = 4</li>
      * <li>TYPE = 5</li>
      * </ul>
      */
     protected int getObjectType(Connection conn, String schema,  String typeName) {
         int objectType = -1;
         String schemaPattern = schema == null ? PERCENT : schema;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try {
             pStmt = conn.prepareStatement(GET_OBJECT_TYPE_STMT);
             pStmt.setString(1, schemaPattern);
             pStmt.setString(2, typeName.toUpperCase());
             boolean worked = pStmt.execute();
             if (worked) {
                 rs = pStmt.getResultSet();
                 boolean b = rs.next();
                 if (b) {
                     objectType = rs.getInt(ALL_OBJECTS_OBJECT_TYPE_FIELD);
                 }
             }
         } catch (SQLException e) {
             // ignore
         }
         finally {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     // ignore
                 }
             }
             if (pStmt != null) {
                 try {
                     pStmt.close();
                 } catch (SQLException e) {
                     // ignore
                 }
             }
         }
         return objectType;
     }
 
     public Properties getTransformProperties() throws DatabaseTypeBuilderException  {
         if (TRANSFORMS_FACTORY == null) {
             throw DatabaseTypeBuilderException.noTransformsFactories();
         }
         Properties transformProperties = TRANSFORMS_FACTORY.getTransformProperties();
         if (transformProperties == null) {
             throw DatabaseTypeBuilderException.noTransformsProperties();
         }
         return transformProperties;
     }
 
     protected boolean setDbmsMetadataSessionTransforms(Connection conn) {
         if (transformsSet) {
             return true;
         }
         boolean worked = true;
         CallableStatement cStmt = null;
         try {
             Properties transformProperties = getTransformProperties();
             StringBuilder sb = new StringBuilder(BEGIN);
             for (Map.Entry<Object, Object> me : transformProperties.entrySet()) {
                 sb.append(NEW_LINE);
                 sb.append(TRANSFORM_PREFIX);
                 sb.append(me.getKey());
                 sb.append("',");
                 sb.append(me.getValue());
                 sb.append(");");
             }
             sb.append(NEW_LINE);
             sb.append(END);
             sb.append(";");
             cStmt = conn.prepareCall(sb.toString());
             cStmt.execute();
         }
         catch (Exception e) {
            worked = false;
         }
         finally {
             try {
                 cStmt.close();
             }
             catch (SQLException e) {
                 // ignore
             }
         }
         if (worked) {
             transformsSet = true;
         }
         return worked;
     }
 
     static boolean schemaPatternExcludesAdminSchemas(String schemaPattern) {
         return (schemaPattern == null || schemaPattern.length() == 0 ||
             TOPLEVEL.equals(schemaPattern) || PERCENT.equals(schemaPattern));
     }
 
     /**
      * Attempt to find a field matching 'fieldName' in a given DatabaseType.
      * 
      */
     static DatabaseType findField(String fieldName, DatabaseType targetType) {
         // remove '%' from field name
         int pctIdx = fieldName.indexOf(PERCENT);
         if (pctIdx != -1) {
             fieldName = fieldName.substring(0, pctIdx);
         }
 
         if (targetType.isPLSQLRecordType()) {
             PLSQLRecordType plsqlRecordType = (PLSQLRecordType)targetType;
             for (FieldType fieldType : plsqlRecordType.getFields()) {
                 if (fieldType.getFieldName().equals(fieldName)) {
                     return fieldType;
                 }
             }
         }
         else if (targetType.isTableType()) {
             TableType tableType = (TableType)targetType;
             for (FieldType columnType : tableType.getColumns()) {
                if (columnType.getFieldName().equalsIgnoreCase(fieldName)) {
                     return columnType;
                 }
             }
         }
         else if (targetType.isObjectType()) {
             ObjectType objectType = (ObjectType)targetType;
             for (FieldType fieldType : objectType.getFields()) {
                 if (fieldType.getFieldName().equals(fieldName)) {
                     return fieldType;
                 }
             }
         }
         else if (targetType instanceof PLSQLPackageType) {
             PLSQLPackageType packageType = (PLSQLPackageType) targetType;
             // check cursors
             for (PLSQLCursorType cursorType : packageType.getCursors()) {
                 if (cursorType.getCursorName().equals(fieldName)) {
                     return cursorType;
                 }
                 
             }
             // check types
             for (PLSQLType plsqlType : packageType.getTypes()) {
                 if (plsqlType.getTypeName().equals(fieldName)) {
                     return plsqlType;
                 }
             }
             // check procedures
             for (ProcedureType procType : packageType.getProcedures()) {
                 if (procType.getProcedureName().equals(fieldName)) {
                     return procType; 
                 }
             }
             // check local variables
             for (FieldType fieldType : packageType.getLocalVariables()) {
                 if (fieldType.getFieldName().equals(fieldName)) {
                     return fieldType;
                 }
             }
         }
         // we may be dealing with a Field type, and need to navigate to the last enclosed type
         else if (targetType.isFieldType()) {
             FieldType fldType = (FieldType) targetType;
             DatabaseType eType = fldType.getEnclosedType();
             if (eType.isComposite()) {
                 CompositeDatabaseType cType = (CompositeDatabaseType) eType;
                 while (cType.getEnclosedType() != null) {
                     eType = cType.getEnclosedType();
                     if (eType.isComposite()) {
                         cType = (CompositeDatabaseType) eType;
                     } else {
                         break;
                     }
                 }
             }
             return findField(fieldName, eType);
         }
         return null;
     }
 }
