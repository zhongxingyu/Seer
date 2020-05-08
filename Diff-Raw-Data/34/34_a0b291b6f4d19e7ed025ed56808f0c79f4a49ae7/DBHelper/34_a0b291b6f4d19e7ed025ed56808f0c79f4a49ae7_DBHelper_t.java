 /**
  * User: cyokin
  * Date: 9/25/13
  * Time: 9:42 PM
  * DBHelper
  */
 package LinkFuture.Core.DBHelper;
 import LinkFuture.Core.DBHelper.Model.*;
 import LinkFuture.Core.Debugger;
 import LinkFuture.Core.Utility;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class DBHelper implements AutoCloseable {
     public static String DefaultDriverClassName =  "com.mysql.jdbc.Driver";
     public static Connection getConnection(String connectionUrl,String userName,String password) throws SQLException, ClassNotFoundException {
         Class.forName(DefaultDriverClassName);
         return DriverManager.getConnection(connectionUrl,userName, password);
     }
     private Connection conn;
     private static final Map<String, SPInfo> CachedSPMeta = new HashMap<>();
     private static final Map<String, HashMap<String,FieldInfo>> CachedClassInfo = new HashMap<>();
 
     public Map<String, SPParameterInfo> parameterList = new HashMap<>();
     private Map<String,Object> outputParameterList = new HashMap<>();
     public String dbUrl;
     public SPInfo SPMetaInfo = null;
     public CallableStatement statement;
     private boolean autoClose = true;
 
     //region DBHelper Structure
     public DBHelper(Connection conn) throws SQLException, ClassNotFoundException {
         //don't not close connection if connect come from others
         this.autoClose = false;
         this.init(conn);
     }
     public DBHelper(String connectionUrl,String userName,String password) throws SQLException, ClassNotFoundException {
         this.init(getConnection(connectionUrl,userName,password));
     }
     // get connection from Tomcat JNDI connection pool,http://tomcat.apache.org/tomcat-7.0-doc/jndi-datasource-examples-howto.html
     // initialContextLookUp = "java:/comp/env/jdbc/dsname"
     public DBHelper(String initialContextLookUp) throws NamingException, SQLException {
         InitialContext cxt = new InitialContext();
         DataSource ds = (DataSource) cxt.lookup(initialContextLookUp );
         this.init(ds.getConnection());
     }
     //endregion
 
     private void init(Connection conn) throws SQLException {
         this.conn = conn;
         this.dbUrl = conn.getMetaData().getURL();
     }
 
     //region Add Parameter
     public void addParameter(String name, Object value)
     {
         addParameter(name,value,null);
     }
     public void addOutParameter(String name)
     {
         addParameter(name,null, ParameterTypeInfo.procedureColumnOut);
     }
     private void addParameter(String name, Object value, ParameterTypeInfo type)
     {
         if(type==null) type = ParameterTypeInfo.procedureColumnIn;
         SPParameterInfo parameter = new SPParameterInfo();
         parameter.parameterName = name;
         parameter.ParameterValue = value;
         parameter.parameterTypeInfo = type;
         parameterList.put(name.toLowerCase(),parameter);
     }
     private void ReadOutputParameterList() throws SQLException
     {
         if(this.SPMetaInfo.parameterList.size()>0)
         {
             for (SPParameterInfo param:this.SPMetaInfo.parameterList){
                 if(param.parameterTypeInfo == ParameterTypeInfo.procedureColumnOut )
                 {
                     this.outputParameterList.put(param.parameterName,this.statement.getObject(param.parameterName));
                 }
             }
         }
     }
     //endregion
 
     //region Execute
     public ArrayList<ArrayList<?>> executeSP(String commandText,Class<?>... outputType) throws InstantiationException, SQLException, IllegalAccessException {
         this.setCommand(commandText,CommandTypeInfo.StoredProcedure);
         boolean hadResults =this.Execute();
         ArrayList<ArrayList<?>> result = new ArrayList<>();
         int resultIndex =0;
         while(hadResults)
         {
             ResultSet rs = this.statement.getResultSet();
             if(resultIndex < outputType.length)
             {
                 ArrayList<?> item = this.read(rs,outputType[resultIndex]);
                 result.add(item);
             }
             resultIndex++;
             hadResults = this.statement.getMoreResults();
         }
         this.statement.close();
         return result;
     }
 
     //region read data to xml string
     public String executeSPToXml(String commandText) throws SQLException, ParserConfigurationException, IOException {
         this.setCommand(commandText,CommandTypeInfo.StoredProcedure);
         boolean hadResults =this.Execute();
         if(!hadResults)
         {
             return null;
         }
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.newDocument();
         Element result = doc.createElement("Result");
         result.setAttribute("TimeStamp",new java.util.Date().toString());
         doc.appendChild(result);
         while(hadResults)
         {
             ResultSet rs = this.statement.getResultSet();
             result.appendChild(this.read(rs,doc));
             hadResults = this.statement.getMoreResults();
         }
         for (String key:this.outputParameterList.keySet())
         {
             //TODO: need find way to encode xml node name
             result.setAttribute(key.replace("$",""),this.outputParameterList.get(key).toString());
         }
         return Utility.xmlNodeToString(doc);
     }
     private Element read(ResultSet rs,Document doc) throws SQLException, ParserConfigurationException, IOException {
         ResultSetMetaData rsmd = rs.getMetaData();
         TableInfo resultMetaInfo = findResultsColumnInfo(rsmd);
         Element table = doc.createElement("Table");
         while (rs.next()) {
             Element row =doc.createElement("Row");
             ArrayList<Element> columnList = fillObject(rs,resultMetaInfo,doc);
             for (Element node:columnList)
             {
                 row.appendChild(node);
             }
             table.appendChild(row);
         }
         return table;
     }
     private ArrayList<Element> fillObject(ResultSet rs,TableInfo resultMetaInfo,Document doc) throws SQLException {
         ArrayList<Element> list = new ArrayList<>();
         for (int i = 1; i <= resultMetaInfo.columnList.size(); i++) {
             ColumnInfo column =   resultMetaInfo.columnList.get(i -1);
             Element newNode = doc.createElement(column.columnName);
             newNode.appendChild(doc.createTextNode(rs.getString(i)));
             list.add(newNode);
         }
         return  list;
     }
     //endregion
 
     public<T> ArrayList<T> executeSP(String commandText,Class<T> outputType)  throws InstantiationException, SQLException, IllegalAccessException {
         this.setCommand(commandText,CommandTypeInfo.StoredProcedure);
         boolean hadResults =this.Execute();
         ArrayList<T> result = null;
         if(hadResults)
         {
             ResultSet rs = this.statement.getResultSet();
             result = this.read(rs,outputType);
         }
         this.statement.close();
         return result;
     }
 
     public  Map<String,Object> getOutputParameterList(){
         return this.outputParameterList;
     }
 
     public<T> ArrayList<T> executeSQL(String sql,Class<T> outputType) throws SQLException, IllegalAccessException, InstantiationException {
         Statement stmt= this.conn.createStatement();
         ResultSet rs =stmt.executeQuery(sql);
         ArrayList<T> result = read(rs,outputType);
         stmt.close();
         return result;
     }
    public int executeSQL(String sql) throws SQLException, IllegalAccessException, InstantiationException {
        Statement stmt= this.conn.createStatement();
        return stmt.executeUpdate(sql);
        //stmt.close();
    }
     //endregion
 
     public SPInfo findSPInfo(String spName) throws SQLException {
     /*
      http://technet.microsoft.com/en-us/library/ms378416.aspx
      AS mySQL not support default in SP, so just ignore it
      PROCEDURE_NAME: The name of the stored procedure.
      COLUMN_NAME:  The name of the column.
      COLUMN_TYPE:  The type of the column. It can be one of the following values:
          procedureColumnUnknown (0)
          procedureColumnIn (1)
          procedureColumnInOut (2)
          procedureColumnOut (4)
          procedureColumnReturn (5)
          procedureColumnResult (3)
     DATA_TYPE: java.sql.Types (smallint)
     TYPE_NAME: The name of the sql data type. (String)
     NULLABLE:  Indicates if the column can contain a null value. It can be one of the following values:
          procedureNoNulls (0)
          procedureNullable (1)
          procedureNullableUnknown (2)
     COLUMN_DEF: The default value of the column.
      */
         String key = (dbUrl + "_" + spName).toLowerCase();
         if(!CachedSPMeta.containsKey(key))
         {
             Debugger.traceln("Read SP Info:" + key);
             SPInfo spMeta = new SPInfo();
             spMeta.dbName = dbUrl;
             spMeta.spName = spName;
             DatabaseMetaData dbmd = this.conn.getMetaData();
             ResultSet rs = dbmd.getProcedureColumns(null, null, spName, null);
             while (rs.next()) {
                 SPParameterInfo parameter = new SPParameterInfo();
                 parameter.parameterName = rs.getString("COLUMN_NAME");
                 parameter.parameterTypeInfo = ParameterTypeInfo.convert(rs.getInt("COLUMN_TYPE"));
                 parameter.sqlTypes = rs.getInt("DATA_TYPE");
                 spMeta.parameterList.add(parameter);
             }
             spMeta.sqlCall =  buildSPCallString(spMeta);
             CachedSPMeta.put(key,spMeta);
         }
         return CachedSPMeta.get(key);
     }
 
     public void close(){
         try {
             if(this.statement!=null && !this.statement.isClosed())
             {
                 this.statement.close();
                 this.statement = null;
             }
             if(this.autoClose && this.conn!=null && !this.conn.isClosed())
             {
                 this.conn.close();
             }
         } catch (SQLException e) {
             Debugger.trace(e);
         }
     }
 
     //region Private Method
     private void setCommand(String commandText,CommandTypeInfo commandType) throws SQLException {
         if(commandType == CommandTypeInfo.StoredProcedure)
         {
             this.SPMetaInfo = this.findSPInfo(commandText);
         }
     }
 
     private boolean Execute() throws SQLException {
         this.statement= this.conn.prepareCall(this.SPMetaInfo.sqlCall);
         //fill
         fillParameter(this.SPMetaInfo, this.statement);
         //exec
         boolean isSuccess =  this.statement.execute();
         //read output data
         if(isSuccess)
         {
             this.ReadOutputParameterList();
         }
         return isSuccess;
     }
 
     private <T> ArrayList<T> read(ResultSet rs,Class<T> type) throws InstantiationException, SQLException, IllegalAccessException {
         ResultSetMetaData rsmd = rs.getMetaData();
         TableInfo resultMetaInfo = findResultsColumnInfo(rsmd);
         ArrayList<T> result = new ArrayList<>();
         while (rs.next()) {
             T newRow = this.fillObject(rs,resultMetaInfo,type);
             result.add(newRow);
         }
         return result;
     }
 
     private String buildSPCallString(SPInfo spMeta) {
         String paramTemplete = "";
         if(spMeta.parameterList.size()>0)
         {
             StringBuilder sb = new StringBuilder();
             sb.append("(");
             for (SPParameterInfo ignored :spMeta.parameterList){
                 sb.append("?,");
             }
             //delete last comma ","
             sb.deleteCharAt(sb.length()-1);
             sb.append(")");
             paramTemplete = sb.toString();
         }
         return String.format("{call %s%s}",spMeta.spName,paramTemplete);
     }
 
     private TableInfo findResultsColumnInfo(ResultSetMetaData rsMeta) throws SQLException {
         int numberOfColumns = rsMeta.getColumnCount();
         TableInfo tableMeta = new TableInfo();
         for (int i = 1; i <= numberOfColumns; i++) {
             ColumnInfo columnMeta = new ColumnInfo();
             columnMeta.columnName = rsMeta.getColumnName(i);
             columnMeta.javaClassName =rsMeta.getColumnClassName(i);
             columnMeta.sqlTypeName = rsMeta.getColumnTypeName(i);
             columnMeta.sqlType = rsMeta.getColumnType(i);
             tableMeta.columnList.add(columnMeta);
         }
         return tableMeta;
     }
 
     public static <T> HashMap<String,FieldInfo> findClassFieldInfo(Class<T> type){
         String classKey = type.getName();
         if(!CachedClassInfo.containsKey(classKey))
         {
             HashMap<String,FieldInfo> fieldList = new HashMap<>();
             for (Field field:type.getDeclaredFields())
             {
                 //ignore directly;
                 IgnoreDBColumnAttribute ignoreAttribute = field.getAnnotation(IgnoreDBColumnAttribute.class);
                 if(ignoreAttribute!=null)
                 {
                     continue;
                 }
                 DBColumnAttribute columnAttribute = field.getAnnotation(DBColumnAttribute.class);
                 FieldInfo fieldMeta = new FieldInfo();
                 fieldMeta.field = field;
                 if(columnAttribute!=null)
                 {
                     String columnName = columnAttribute.columnName().length()==0?field.getName():columnAttribute.columnName();
                     if(columnAttribute.isEntity())
                     {
                         //find sub field, save into Memory
                         findClassFieldInfo(field.getClass());
                     }
                     fieldMeta.columnAttribute = columnAttribute;
                     fieldList.put(columnName.toLowerCase(),fieldMeta);
                 }
                 else
                 {
                     fieldList.put(field.getName().toLowerCase(),fieldMeta);
                 }
             }
             CachedClassInfo.put(classKey,fieldList);
         }
         return  CachedClassInfo.get(classKey) ;
     }
 
     private void fillParameter(SPInfo spMeta, CallableStatement stmt) throws SQLException {
         if(spMeta.parameterList.size()>0)
         {
             for (SPParameterInfo param:spMeta.parameterList){
                 String paramKey = param.parameterName.toLowerCase();
                 if(parameterList.containsKey(paramKey))
                 {
                     if(param.parameterTypeInfo == ParameterTypeInfo.procedureColumnOut )
                     {
                         stmt.registerOutParameter(param.parameterName, parameterList.get(paramKey).sqlTypes);
                     }
                     else
                     {
                         stmt.setObject(param.parameterName, parameterList.get(paramKey).ParameterValue);
                     }
                 }
             }
         }
     }
 
     private<T> T fillObject(ResultSet rs,TableInfo resultMetaInfo,Class<T> type) throws InstantiationException, SQLException, IllegalAccessException {
         T newInstance = type.newInstance();
         HashMap<String,FieldInfo> fieldList = findClassFieldInfo(type) ;
         for (int i = 1; i <= resultMetaInfo.columnList.size(); i++) {
             ColumnInfo column =   resultMetaInfo.columnList.get(i -1);
             String fieldKey = column.columnName.toLowerCase();
             if(fieldList.containsKey(fieldKey))
             {
                 FieldInfo fieldMeta = fieldList.get(fieldKey);
                 fieldMeta.field.setAccessible(true);
                 fieldMeta.field.set(newInstance,rs.getObject(i));
             }
         }
         //fill data to sub entity
         for (String fieldKey:fieldList.keySet())
         {
               FieldInfo fieldMeta = fieldList.get(fieldKey);
               if(fieldMeta.columnAttribute!=null && fieldMeta.columnAttribute.isEntity())
               {
                   fieldMeta.field.setAccessible(true);
                   Object subInstance = fillObject(rs,resultMetaInfo,fieldMeta.field.getType());
                   fieldMeta.field.set(newInstance,subInstance);
               }
         }
         return newInstance;
     }
 
     //endregion
 }
 
 
 
 
 
