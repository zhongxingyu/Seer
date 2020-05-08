 // ============================================================================
 //
 // Copyright (C) 2006-2011 Talend Inc. - www.talend.com
 //
 // This source code is available under agreement available at
 // %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 //
 // You should have received a copy of the agreement
 // along with this program; if not, write to Talend SA
 // 9 rue Pages 92150 Suresnes, France
 //
 // ============================================================================
 package org.talend.core.model.metadata;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.talend.commons.exception.PersistenceException;
 import org.talend.core.GlobalServiceRegister;
 import org.talend.core.language.ECodeLanguage;
 import org.talend.core.language.LanguageManager;
 import org.talend.core.model.metadata.builder.connection.Connection;
 import org.talend.core.model.metadata.builder.connection.MetadataTable;
 import org.talend.core.model.metadata.builder.connection.SAPConnection;
 import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
 import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
 import org.talend.core.model.metadata.types.JavaTypesManager;
 import org.talend.core.model.metadata.types.TypesManager;
 import org.talend.core.model.process.INode;
 import org.talend.core.model.properties.ConnectionItem;
 import org.talend.core.model.properties.Item;
 import org.talend.core.model.repository.IRepositoryPrefConstants;
 import org.talend.core.model.repository.IRepositoryViewObject;
 import org.talend.core.model.routines.IRoutinesService;
 import org.talend.core.runtime.CoreRuntimePlugin;
 import org.talend.core.runtime.i18n.Messages;
 import org.talend.core.utils.KeywordsValidator;
 import org.talend.cwm.helper.ConnectionHelper;
 import org.talend.designer.core.IDesignerCoreService;
 import org.talend.repository.model.ERepositoryStatus;
 import org.talend.repository.model.IProxyRepositoryFactory;
 import org.talend.repository.model.RepositoryConstants;
 
 /**
  * ggu class global comment. Detailled comment
  */
 public final class MetadataToolHelper {
 
     private static final int MIN = 192;
 
     private static final int MAX = 255;
 
     public static EList getMetadataTableFromConnection(final Connection conn) {
         if (conn == null) {
             return null;
         }
         // return conn.getTables();
         if (conn instanceof SAPConnection) {
             final SAPConnection sapConnection = (SAPConnection) conn;
             final EList functions = sapConnection.getFuntions();
             if (functions != null && !functions.isEmpty()) {
                 final EList tables = new BasicEList();
                 for (int i = 0; i < functions.size(); i++) {
                    tables.addAll(((SAPFunctionUnit) functions.get(i)).getTables());
                 }
                 return tables;
             }
         } else {
             EList tables = new BasicEList();
             tables.addAll(ConnectionHelper.getTables(conn));
             return tables;
         }
         return null;
     }
 
     public static ConnectionItem getConnectionItemByItemId(String itemId, boolean deleted) {
         if (itemId == null || itemId.equals("")) { //$NON-NLS-1$
             return null;
         }
         final IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
         try {
             final IRepositoryViewObject lastVersion = proxyRepositoryFactory.getLastVersion(itemId);
             if (lastVersion != null) {
                 if (!deleted && proxyRepositoryFactory.getStatus(lastVersion) == ERepositoryStatus.DELETED) {
                     return null;
                 }
                 final Item item = lastVersion.getProperty().getItem();
                 if (item != null && item instanceof ConnectionItem) {
                     return (ConnectionItem) item;
                 }
             }
         } catch (PersistenceException e) {
             //
         }
         return null;
     }
 
     /**
      * qzhang Comment method "isBoolean".
      * 
      * @param value
      * @return
      */
     public static boolean isBoolean(final String value) {
         return value.equals(JavaTypesManager.BOOLEAN.getId());
     }
 
     /**
      * qzhang Comment method "isDirectory".
      * 
      * @param value
      * @return
      */
     public static boolean isDirectory(final String value) {
         ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
         if (codeLanguage == ECodeLanguage.JAVA) {
             return value.equals(JavaTypesManager.DIRECTORY.getId());
         } else {
             return value.equals(ContextParameterJavaTypeManager.PERL_DIRECTORY);
         }
     }
 
     public static boolean isList(final String value) {
         ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
         if (codeLanguage == ECodeLanguage.JAVA) {
             return value.equals(JavaTypesManager.VALUE_LIST.getId());
         } else {
             return value.equals(ContextParameterJavaTypeManager.PERL_VALUE_LIST);
         }
     }
 
     /**
      * qzhang Comment method "isDate".
      * 
      * @param value
      * @return
      */
     public static boolean isDate(final String value) {
         ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
         if (codeLanguage == ECodeLanguage.JAVA) {
             return value.equals(JavaTypesManager.DATE.getId());
         } else {
             return value.equals(ContextParameterJavaTypeManager.PERL_DAY);
         }
 
     }
 
     /**
      * qzhang Comment method "isFile".
      * 
      * @param value
      * @return
      */
     public static boolean isFile(final String value) {
         ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
         if (codeLanguage == ECodeLanguage.JAVA) {
             return value.equals(JavaTypesManager.FILE.getId());
         } else {
             return value.equals(ContextParameterJavaTypeManager.PERL_FILE);
         }
     }
 
     public static boolean isPassword(final String value) {
         ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
         if (codeLanguage == ECodeLanguage.JAVA) {
             return value.equals(JavaTypesManager.PASSWORD.getId());
         } else {
             return value.equals(ContextParameterJavaTypeManager.PERL_PASSWORD);
         }
 
     }
 
     public static boolean isValidSchemaName(String name) {
         if (name.contains(" ")) {
             return false;
         }
         return true;
     }
 
     public static boolean isValidColumnName(String name) {
         if (name == null || KeywordsValidator.isKeyword(name)) {
             return false;
         }
 
         return isAllowSpecificCharacters() || Pattern.matches(RepositoryConstants.COLUMN_NAME_PATTERN, name);
     }
 
     private static boolean isAllowSpecificCharacters() {
         if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
             return CoreRuntimePlugin.getInstance().getDesignerCoreService().getDesignerCorePreferenceStore()
                     .getBoolean(IRepositoryPrefConstants.ALLOW_SPECIFIC_CHARACTERS_FOR_SCHEMA_COLUMNS);
         }
         return false;
     }
 
     /**
      * 
      * qli Comment method "validateColumnName".
      * 
      * 
      */
     public static String validateColumnName(String columnName, int index) {
         String originalColumnName = new String(columnName);
         columnName = "";
         final String underLine = "_"; //$NON-NLS-1$
 
         boolean isKeyword = KeywordsValidator.isKeyword(columnName);
 
         if (!isKeyword) {
             boolean isAllowSpecific = isAllowSpecificCharacters();
 
             // match RepositoryConstants.COLUMN_NAME_VALIDATED
             for (int i = 0; i < originalColumnName.length(); i++) {
                 Character car = originalColumnName.charAt(i);
                 if (car.toString().getBytes().length == 1 || !isAllowSpecific) {
                     // first character should have only a-z or A-Z or _
                     // other characters should have only a-z or A-Z or _ or 0-9
                     if (((car >= 'a') && (car <= 'z')) || ((car >= 'A') && (car <= 'Z')) || car == '_'
                             || ((car >= '0') && (car <= '9') && (i != 0))) {
                         columnName += car;
                     } else {
                         columnName += underLine;
                     }
                 } else {
                     columnName += car;
                 }
             }
         }
         if (isKeyword
                 || org.apache.commons.lang.StringUtils.countMatches(columnName, underLine) >= (originalColumnName.length() / 2)) {
             columnName = "Column" + index; //$NON-NLS-1$
         }
 
         return columnName;
 
     }
 
     /**
      * 
      * hwang Comment method "validateTableName".
      * 
      * 
      */
     public static String validateTableName(String tableName) {
         String originalTableName = new String(tableName);
         tableName = "";
         final String underLine = "_"; //$NON-NLS-1$
 
         boolean isKeyword = KeywordsValidator.isKeyword(originalTableName);
 
         // boolean isAllowSpecific = isAllowSpecificCharacters();
 
         for (int i = 0; i < originalTableName.length(); i++) {
             Character car = originalTableName.charAt(i);
             if (car.toString().getBytes().length == 1) {
                 if (((car >= 'a') && (car <= 'z')) || ((car >= 'A') && (car <= 'Z')) || car == '_'
                         || ((car >= '0') && (car <= '9') && (i != 0))) {
                     tableName += car;
                 } else {
                     tableName += underLine;
                 }
             } else {
                 tableName += car;
             }
         }
         if (isKeyword) {
             return tableName + "_1";
         }
         return tableName;
 
     }
 
     /**
      * wzhang Comment method "validataValue".
      */
     public static String validateValue(String columnName) {
         if (columnName == null) {
             return null;
         }
         columnName = mapSpecialChar(columnName);
         final String underLine = "_"; //$NON-NLS-1$
         if (columnName.matches("^\\d.*")) { //$NON-NLS-1$
             columnName = underLine + columnName;
         }
 
         String testColumnName = columnName.replaceAll("[^a-zA-Z0-9_]", underLine); //$NON-NLS-1$
 
         if (org.apache.commons.lang.StringUtils.countMatches(testColumnName, underLine) < (columnName.length() / 2)) {
             return testColumnName;
         }
         return columnName;
     }
 
     /**
      * zli Comment method "validataValue".
      */
     public static String validateValueForDBType(String columnName) {
         if (columnName == null) {
             return null;
         }
         columnName = mapSpecialChar(columnName);
         final String underLine = "_"; //$NON-NLS-1$
         if (columnName.matches("^\\d.*")) { //$NON-NLS-1$
             columnName = underLine + columnName;
         }
 
         String testColumnName = columnName.replaceAll("[^a-zA-Z 0-9_]", underLine); //$NON-NLS-1$
 
         if (org.apache.commons.lang.StringUtils.countMatches(testColumnName, underLine) < (columnName.length() / 2)) {
             return testColumnName;
         }
         return columnName;
     }
 
     /**
      * 
      * qli Comment method "mapSpecialChar".
      * 
      * 
      */
     private static String mapSpecialChar(String columnName) {
         if (GlobalServiceRegister.getDefault().isServiceRegistered(IRoutinesService.class)) {
             IRoutinesService service = (IRoutinesService) GlobalServiceRegister.getDefault().getService(IRoutinesService.class);
             if (service != null) {
                 Vector map = service.getAccents();
                 map.setElementAt("AE", 4);//$NON-NLS-1$
                 map.setElementAt("OE", 22);//$NON-NLS-1$
                 map.setElementAt("UE", 28);//$NON-NLS-1$
                 map.setElementAt("ss", 31);//$NON-NLS-1$
                 map.setElementAt("ae", 36);//$NON-NLS-1$
                 map.setElementAt("oe", 54);//$NON-NLS-1$
                 map.setElementAt("ue", 60);//$NON-NLS-1$
                 Vector addedMap = new Vector();
                 for (int i = 257; i < 304; i++) {
                     addedMap.add(String.valueOf((char) i));
                 }
                 map.addAll(addedMap);
                 map.add("I");//$NON-NLS-1$
 
                 return initSpecificMapping(columnName, map);
             }
         }
         return columnName;
     }
 
     /**
      * 
      * qli Comment method "initSpecificMapping".
      * 
      * 
      */
     private static String initSpecificMapping(String columnName, Vector map) {
         for (int i = 0; i < columnName.toCharArray().length; i++) {
             int carVal = columnName.charAt(i);
             if (carVal >= MIN && carVal <= MIN + map.size()) {
                 String oldVal = String.valueOf(columnName.toCharArray()[i]);
                 String newVal = (String) map.get(carVal - MIN);
                 if (!(oldVal.equals(newVal))) {
                     columnName = columnName.replaceAll(oldVal, newVal);
                 }
             }
         }
 
         return columnName;
     }
 
     /**
      * wzhang Comment method "validateSchema".
      */
     public static void validateSchema(String value) {
         if (value == null) {
             MessageDialog.openError(Display.getCurrent().getActiveShell(),
                     Messages.getString("MetadataTool.nullValue"), Messages.getString("MetadataTool.nameNull")); //$NON-NLS-1$ //$NON-NLS-2$
             return;
         }
         if (!isValidSchemaName(value)) {
             MessageDialog.openError(Display.getCurrent().getActiveShell(),
                     Messages.getString("MetadataTool.invalid"), Messages.getString("MetadataTool.schemaInvalid")); //$NON-NLS-1$ //$NON-NLS-2$
             return;
         }
     }
 
     /**
      * 
      * wzhang Comment method "validateSchemaValue".
      * 
      * @param value
      * @param beanPosition
      * @param list
      * @return
      */
     public static String validateSchemaValue(String value, int beanPosition, List<String> list) {
         if (value == null) {
             return Messages.getString("MetadataTool.schemaNull"); //$NON-NLS-1$
         }
         if (!isValidSchemaName(value)) {
             return Messages.getString("MetadataTool.schemaIn"); //$NON-NLS-1$
         }
         int listSize = list.size();
         for (int i = 0; i < listSize; i++) {
             if (value.equals(list.get(i)) && i != beanPosition) {
                 return Messages.getString("MetadataTool.schemaExist"); //$NON-NLS-1$
             }
         }
         return null;
     }
 
     /**
      * wzhang Comment method "checkSchema".
      */
     public static void checkSchema(Shell shell, KeyEvent event) {
         if ((!Character.isIdentifierIgnorable(event.character)) && (event.character == ' ')) { //$NON-NLS-1$
             event.doit = false;
             MessageDialog.openError(shell,
                     Messages.getString("MetadataTool.invalidChar"), Messages.getString("MetadataTool.errorMessage")); //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
 
     public static void copyTable(IMetadataTable source, IMetadataTable target) {
         copyTable(source, target, null);
     }
 
     /**
      * @author wzhang Comment method "copyTable".
      * @param dbmsid
      * @param source
      * @param target
      */
     public static void copyTable(String dbmsId, IMetadataTable source, IMetadataTable target) {
         setDBType(source, dbmsId);
         copyTable(source, target);
     }
 
     /**
      * DOC wzhang Comment method "setDBType".
      */
     public static void setDBType(IMetadataTable metaTable, String dbmsid) {
         List<IMetadataColumn> listColumns = metaTable.getListColumns();
         for (IMetadataColumn column : listColumns) {
             String talendType = column.getTalendType();
             String type = column.getType();
             if (dbmsid != null) {
                 if (!TypesManager.checkDBType(dbmsid, talendType, type)) {
                     column.setType(TypesManager.getDBTypeFromTalendType(dbmsid, talendType));
                 }
             }
         }
     }
 
     public static void copyTable(IMetadataTable source, IMetadataTable target, String targetDbms) {
         if (source == null || target == null) {
             return;
         }
         List<IMetadataColumn> columnsToRemove = new ArrayList<IMetadataColumn>();
         List<String> readOnlycolumns = new ArrayList<String>();
         for (IMetadataColumn column : target.getListColumns()) {
             if (!column.isCustom()) {
                 columnsToRemove.add(column);
             }
             if (column.isReadOnly()) {
                 readOnlycolumns.add(column.getLabel());
             }
         }
         target.getListColumns().removeAll(columnsToRemove);
 
         List<IMetadataColumn> columnsTAdd = new ArrayList<IMetadataColumn>();
         for (IMetadataColumn column : source.getListColumns()) {
             IMetadataColumn targetColumn = target.getColumn(column.getLabel());
             IMetadataColumn newTargetColumn = column.clone();
             if (targetColumn == null) {
                 columnsTAdd.add(newTargetColumn);
                 newTargetColumn.setReadOnly(target.isReadOnly() || readOnlycolumns.contains(newTargetColumn.getLabel()));
             } else {
                 if (!targetColumn.isReadOnly()) {
                     target.getListColumns().remove(targetColumn);
                     newTargetColumn.setCustom(targetColumn.isCustom());
                     newTargetColumn.setCustomId(targetColumn.getCustomId());
                     columnsTAdd.add(newTargetColumn);
                 }
             }
         }
         target.getListColumns().addAll(columnsTAdd);
         target.sortCustomColumns();
         target.setLabel(source.getLabel());
     }
 
     /**
      * 
      * DOC qli Comment method "copyTable".
      * 
      * @param sourceColumns,target,targetDbms
      * @return
      */
     public static void copyTable(List<IMetadataColumn> sourceColumns, IMetadataTable target, String targetDbms) {
         if (sourceColumns == null || target == null) {
             return;
         }
         List<String> readOnlycolumns = new ArrayList<String>();
         for (IMetadataColumn column : target.getListColumns()) {
             if (column.isReadOnly()) {
                 readOnlycolumns.add(column.getLabel());
             }
         }
 
         List<IMetadataColumn> columnsTAdd = new ArrayList<IMetadataColumn>();
         for (IMetadataColumn column : sourceColumns) {
             IMetadataColumn targetColumn = target.getColumn(column.getLabel());
             IMetadataColumn newTargetColumn = column.clone();
             if (targetColumn == null) {
                 columnsTAdd.add(newTargetColumn);
                 newTargetColumn.setReadOnly(target.isReadOnly() || readOnlycolumns.contains(newTargetColumn.getLabel()));
             } else {
                 if (!targetColumn.isReadOnly()) {
                     target.getListColumns().remove(targetColumn);
                     newTargetColumn.setCustom(targetColumn.isCustom());
                     newTargetColumn.setCustomId(targetColumn.getCustomId());
                     columnsTAdd.add(newTargetColumn);
                 }
             }
         }
         target.getListColumns().addAll(columnsTAdd);
         target.sortCustomColumns();
     }
 
     public static MetadataTable getMetadataTableFromRepository(String metaRepositoryId) {
         org.talend.core.model.metadata.builder.connection.Connection connection;
 
         String[] names = metaRepositoryId.split(" - "); //$NON-NLS-1$
         if (names.length < 2) {
             return null;
         }
         String linkedRepository = names[0];
         String name2 = null;
         if (names.length == 2) {
             name2 = names[1];
         } else if (names.length > 2) {
             name2 = metaRepositoryId.substring(linkedRepository.length() + 3);
         }
 
         connection = getConnectionFromRepository(linkedRepository);
 
         if (connection != null) {
             if (connection instanceof SAPConnection) {
                 return getMetadataTableFromSAPFunction((SAPConnection) connection, name2);
             }
             Set tables = ConnectionHelper.getTables(connection);
             for (Object tableObj : tables) {
                 MetadataTable table = (MetadataTable) tableObj;
                 if (table.getLabel().equals(name2)) {
                     return table;
                 }
             }
         }
         return null;
     }
 
     private static MetadataTable getMetadataTableFromSAPFunction(SAPConnection connection, String name) {
         String functionName = null;
         String metadataName = null;
         String[] names = name.split(" - "); //$NON-NLS-1$
         if (names.length == 2) {
             functionName = names[0];
             metadataName = names[1];
         } else {
             return null;
         }
 
         for (Object obj : connection.getFuntions()) {
             SAPFunctionUnit function = (SAPFunctionUnit) obj;
             if (functionName.equals(function.getLabel())) {
                 for (Object object : function.getTables()) {
                     MetadataTable table = (MetadataTable) object;
                     if (metadataName.equals(table.getLabel())) {
                         return table;
                     }
                 }
             }
 
         }
         return null;
     }
 
     public static org.talend.core.model.metadata.builder.connection.Connection getConnectionFromRepository(String metaRepositoryid) {
         String connectionId = metaRepositoryid;
         // some calls can be done either with only the connection Id or with
         // informations from query or table
         String[] names = metaRepositoryid.split(" - "); //$NON-NLS-1$
         if (names.length == 2) {
             connectionId = names[0];
         }
 
         IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
         try {
             IRepositoryViewObject object = factory.getLastVersion(connectionId);
             if (object == null) {
                 return null;
             }
             if (factory.getStatus(object) != ERepositoryStatus.DELETED) {
                 return ((ConnectionItem) object.getProperty().getItem()).getConnection();
             }
         } catch (PersistenceException e) {
             throw new RuntimeException(e);
         }
         return null;
 
     }
 
     public static IMetadataTable getMetadataTableFromNodeLabel(INode node, String name) {
         if (node == null || name == null) {
             return null;
         }
         for (IMetadataTable metadata : node.getMetadataList()) {
             if (name.equals(metadata.getLabel())) {
                 return metadata;
             }
         }
         return null;
     }
 
     public static IMetadataTable getMetadataTableFromNodeTableName(INode node, String name) {
         if (node == null || name == null) {
             return null;
         }
         for (IMetadataTable metadata : node.getMetadataList()) {
             // if (name.equals(metadata.getTableName())) {
             if (name.equals(metadata.getLabel()) || name.equals(metadata.getTableName())) {
                 return metadata;
             }
         }
         return null;
     }
 }
