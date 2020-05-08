 /*
  * SQLRunner.java
  *
  * program to run sql scripts with parameters; similar syntax to sqlplus.
  *
  * Thu Feb  7 02:39:03 EST 2005
  * 
  * Copyright (c) 2005, Alex Sverdlov
  * 
  * This work is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 2, or higher.
  */
 
 package db;
 
 import java.util.*;
 import java.text.*;
 import java.sql.*;
 import java.io.*;
 import java.util.regex.*;
 import java.util.zip.*;
 
 /**
  * SQLRunner
  *
  * Java utility to run SQL, dump data to CSV, etc.
  *
  * @author Alex Sverdlov
  * @version 1.0.17
  */
 public class SQLRunner {
 
     /**
      * connections we keep around.
      */
     private static TreeMap<String,Connection> dbconnections = new TreeMap<String,Connection>();
 
     /**
      * files currently being processed (to avoid infinite recursion).
      */
     private static Stack<String> processingFiles = new Stack<String>();
 
     /**
      * timestamp format in the prompt; yyyyMMdd hh:mm:ss
      */
     private static Format timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS");
     
     /**
      * all property setting and getting should be done via setProperty and getProperty methods
      */
     private static Properties props = System.getProperties();
 
     /**
      * set property
      */
     public static void setProperty(String k,String v){
         props.setProperty(k,v);
     }
     public static void clearProperty(String k){
         props.remove(k);
     }
     /**
      * if not found, attempt to return _default, if again not found, return alt.
      */
     public static String getProperty(String n,String alt){
         return props.getProperty(n,props.getProperty(n+"_default",alt));
     }
 
     /**
      * get property with evaluated value.
      */
     public static String getEvalProperty(String n,String alt){
         return evalString(getProperty(n,alt));
     }
 
     /**
      * trim string from the right.
      */
     public static String rtrim(String s){
         if(s == null)
             return null;
         int i = s.length() - 1;
         if(i < 0)
             return "";
         while(i >= 0 && Character.isSpace(s.charAt(i)))
             i--;
         return s.substring(0,i+1);
     }
 
     /**
      * eval a string.
      */
     public static String evalString(String val) {
         StringBuffer sb = new StringBuffer();
         boolean found = false;
         if(val == null)
             return val;
         String[] ptrn = {
             "<\\?=\\s*\\$?(\\w+)\\s*\\?>",      // <?=$somevariable?> 
             "\\\\?\\&\\&?(\\w+)\\.?",           // &&tdate, &tdate, \&tdate
             "\\$\\{\\s*(\\w+)\\s*\\}\\.?",      // ${variable} 
             "\\$(\\w+)\\.?"                     // $variable 
         };
         for(String p : ptrn){
             sb.setLength(0);
             Matcher matcher = Pattern.compile(p,Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(val);
             while (matcher.find()) {
                 found = true;
                 String s = getEvalProperty(matcher.group(1),null);
                 if(s == null)
                     throw new IllegalStateException("unable to evaluate ["+val+"]. ('"+matcher.group(1)+"' is undefined).");
                 matcher.appendReplacement(sb,Matcher.quoteReplacement(s));
             }
             matcher.appendTail(sb);
             val = sb.toString();
         }
         return found ? evalString(val) : val;
     }
 
     /**
      * Run sql query
      */
     public static void sqlRun(String sql,PrintStream ps) throws Exception {
         long qtim = 0,otim = 0;
 
         setProperty("_current_connection_laststmnt_raw",sql.trim());
         sql = evalString(sql);
         setProperty("_current_connection_laststmnt",sql.trim());
 
         //
         // if nothing to run, return.
         //
         if(sql.trim().length() <= 0)
             return;
 
         if(getEvalProperty("env","off").equals("on"))
             props.list(System.out);
 
         qtim = System.currentTimeMillis();
         setProperty("_current_connection_laststmnt_startmillis",""+qtim);
 
         if(getEvalProperty("log","off").equals("on")){
             System.out.println("\n--SQL START: "+new java.util.Date(qtim));
             System.out.println("-----------------------------------");
             System.out.println( sql + ";" );
             System.out.println("-----------------------------------");
         }
 
         if(getEvalProperty("pretend","off").equals("on") || getEvalProperty("debug","off").equals("on")){
             if(getEvalProperty("log","off").equals("on"))
                 System.out.println("-- In pretend mode. Not running query.");
             return;
         }
 
         // connection key (catenated connection info).
         StringBuffer key = new StringBuffer();
 
         // if db name is "blah123", then use blah123_user for user, etc. (unless `user' is defined).
         for(String sufix : new String[]{"url","user","pass","driver"}){
             String val = getEvalProperty(sufix,null);
             if(val == null)
                 val = getEvalProperty(getEvalProperty("db","db")+"_"+sufix,null);
             
             // we -must- have url,user,pass,driver defined in environment.
             if(val == null)
                 throw new IllegalStateException("undefined db ("+sufix+") connection info");
 
             setProperty("_current_connection_"+sufix,val);
 
             // append to connection key (changing connection details will start a new connection).
             key.append(val+"|");
         }
         // note: default driver should probably be "sun.jdbc.odbc.JdbcOdbcDriver"
 
 
         // see if we have this connection saved
         Connection connection = dbconnections.get(key.toString());
         if(connection == null){
             //
             // init driver.
             // 
             Class.forName( getProperty("_current_connection_driver",null) ).newInstance();
         
             // 
             // connect.
             //
             connection = DriverManager.getConnection(
                 getProperty("_current_connection_url",null),
                 getProperty("_current_connection_user",null),
                 getProperty("_current_connection_pass",null)
             );
 
             // save this connection for later use.
             dbconnections.put(key.toString(),connection);
         }
 
         //
         // to be initialized below.
         ResultSet rs = null;
         String[] m = new String[1];
 
         //
         // if user is doing a metadata lookup.
         //
         if(strmatch(sql,"^\\s*show\\s+table(s)?\\s*;?\\s*$") != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             // get all tables (uses jdbc)
             rs = databaseMetaData.getTables(
                     getEvalProperty("catalog",null),
                     getEvalProperty("schema",null),null,
                     new String[]{"TABLE"}
                 );
         }else if(strmatch(sql,"^\\s*show\\s+view(s)?\\s*;?\\s*$") != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             // get all tables (uses jdbc)
             rs = databaseMetaData.getTables(
                     getEvalProperty("catalog",null),
                     getEvalProperty("schema",null),null,
                     new String[]{"VIEW"}
                 );
         }else if(strmatch(sql,"^\\s*show\\s+object(s)?\\s*;?\\s*$") != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             rs = databaseMetaData.getTables(
                     getEvalProperty("catalog",null),
                     getEvalProperty("schema",null),null,
                     new String[]{"VIEW","TABLE"}
                 );
         }else if(strmatch(sql,"^\\s*show\\s+catalog(s)?\\s*;?\\s*$") != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             rs = databaseMetaData.getCatalogs();
         }else if(strmatch(sql,"^\\s*show\\s+schema(s)?\\s*;?$") != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             rs = databaseMetaData.getSchemas();
         }else if(strmatch(sql,"^\\s*show\\s+table\\s+type(s)?\\s*;?\\s*$") != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             rs = databaseMetaData.getTableTypes();
         }else if((m = strmatch(sql,"^\\s*desc(ribe)?\\s+(\\S+)\\s*;?\\s*$")) != null){
             DatabaseMetaData databaseMetaData = connection.getMetaData();
             rs = databaseMetaData.getColumns(props.getProperty("catalog"),
                    props.getProperty("schema"),m[2].toUpperCase(),null);
         }else if(strmatch(sql,"^\\s*info\\s*;?$") != null){
             //
             // if in console mode, display database info.
             //
             if(ps != null){ 
                 DatabaseMetaData databaseMetaData = connection.getMetaData();
                 ps.println("Driver: "+databaseMetaData.getDriverName());
                 ps.println("Driver Version: "+databaseMetaData.getDriverVersion());
                 ps.println("JDBC Version: "+databaseMetaData.getJDBCMajorVersion()+"."+
                     databaseMetaData.getJDBCMinorVersion());
                 ps.println("Database: "+databaseMetaData.getDatabaseProductName());
                 ps.println("Database Version: "+databaseMetaData.getDatabaseProductVersion());
                 ps.println("URL: "+databaseMetaData.getURL());
                 ps.println("User: "+databaseMetaData.getUserName());
                 ps.println("SQLRunner Version: "+props.getProperty("version"));
             }
         }else{
             // user is doing a db query. 
 
             // for select statements, set autocommit to off.
             if(strmatch(sql,"^\\s*select") != null){
                 connection.setAutoCommit(false);
             }else{
                 // for non-select statements, set autocommit to variable name, defaulting to "on".
                 connection.setAutoCommit( getEvalProperty("autocommit","on").equals("on") );
             }
 
             // 
             // execute statement
             //
             Statement statement = connection.createStatement();
             statement.setFetchSize(1024*16);   // random large number.
             boolean rsout = false; 
 
             try {
                 rsout = statement.execute(sql);
             } catch(java.lang.Exception e){
                 if( // Postgres
                     e.getMessage().indexOf("Unable to interpret the update count in command completion tag") >= 0 ||
                     // Netezza
                     e.getMessage().indexOf("Unable to fathom update count") >= 0 ){
                     // in PostgreSQL 9.1 JDBC4 (build 901)
                     // ...and Netezza 
                     // the driver appears to be doing:
                     // update_count = Integer.parseInt(...); 
                     // so if inserting/updating > 2^31 records, we blow up for no good reason.
                     if(getEvalProperty("log","off").equals("on")){
                         System.out.println("-- SUPPRESSED JAVA EXCEPTION: "+e.getMessage());
                     }
                 }else{
                     throw e;
                 }
             }
 
             otim = System.currentTimeMillis();
             // record (just in case).
             setProperty("_current_connection_laststmnt_rsmillis",""+otim);                
             setProperty("_current_connection_laststmnt_rsseconds",""+(otim - qtim)/1000.0);
 
             if(getEvalProperty("log","off").equals("on")){
                 double d = ((otim - qtim) / 1000.0) / 60;
                 System.out.printf("\n--SQL END: "+new java.util.Date(otim)+"; %.2f min\n",d);
                 if(!rsout){
                     System.out.println("-- UpdateCount: "+statement.getUpdateCount());
                 }
                 System.out.println("-----------------------------------");
             }
 
             if(!rsout){
                 // 
                 // if no result set, then simply return.
                 //
                 if(getEvalProperty("console","off").equals("on")){
                     System.out.println("OK (no results)");
                 }
                 return ;
             }
             
             // 
             // get the results
             //
             rs = statement.getResultSet();
         }
 
         //
         // determine output format
         //
         if(getEvalProperty("outformat","delim").equals("delim")){
             if(rs != null)
                 resultSetOutputDelim(rs,ps);
         }else{
             // else ?
         }
 
 
         long etim = System.currentTimeMillis();
         
         // record (just in case).
         setProperty("_current_connection_laststmnt_millis",""+etim);
         setProperty("_current_connection_laststmnt_seconds",""+(etim - qtim)/1000.0);
          
         if(getEvalProperty("log","off").equals("on")){
             double d = ((etim - qtim) / 1000.0)/60;
             if(otim > 0){   // there was output
                 double e = ((etim - otim) / 1000.0)/60;
                 System.out.printf("\n--OUT END: "+new java.util.Date(etim)+"; OUT: %.2f min, TOTAL: %.2f min\n",e,d);
             }else{
                 System.out.printf("\n--OUT END: "+new java.util.Date(etim)+"; TOTAL: %.2f min\n",d);
             }
             System.out.println("-----------------------------------");
         }
     }
 
 
 /*
  *
  *  // todo database loads via native database utilities.
     Process p = Runtime.getRuntime().exec("some executable");
     p.waitFor();
  *
  *
  */
 
 
     /**
      * output data to a CSV file
      */
     public static void resultSetOutputDelim(ResultSet rs,PrintStream ps) throws Exception {
 
         // 
         // setup record delimiter for output
         //
         String delim = getEvalProperty("delim",",");
         String linesep = getEvalProperty("linesep","\n");
 
         
 
         delim = delim.replaceAll("\\\\t","\t");
         delim = delim.replaceAll("\\\\n","\n");
         delim = delim.replaceAll("\\\\r","\r");
 
         linesep = linesep.replaceAll("\\\\t","\t");
         linesep = linesep.replaceAll("\\\\n","\n");
         linesep = linesep.replaceAll("\\\\r","\r");
 
         // replace octals (e.g. \000, \1, etc.) 
         for(int i=0xFF;i>=0;i--){
             String octi = Integer.toOctalString(i);
             String octr = Character.toString((char)i);
             delim = delim.replaceAll("\\\\00"+octi,octr);
             linesep = linesep.replaceAll("\\\\00"+octi,octr);
             delim = delim.replaceAll("\\\\0"+octi,octr);
             linesep = linesep.replaceAll("\\\\0"+octi,octr);
             delim = delim.replaceAll("\\\\"+octi,octr);
             linesep = linesep.replaceAll("\\\\"+octi,octr);
         }
 
 
         // 
         // retrieve and figure out resultset metadata.
         //
         ResultSetMetaData meta = rs.getMetaData();
         int cols = meta.getColumnCount();
         boolean[] colIsDate = new boolean[cols+1];
         boolean[] colIsTimestamp = new boolean[cols+1];
         boolean[] colIsTime = new boolean[cols+1];
         boolean[] colIsDouble = new boolean[cols+1];        
         String[] colTypeName = new String[cols+1];
         String[] colTypeNameOrig = new String[cols+1];
         String[] colName = new String[cols+1];
         int[] colSiz = new int[cols+1];     // size
         int[] colPres = new int[cols+1];    // precision
         int[] colScal = new int[cols+1];    // scale 
         int[] colNul = new int[cols+1];     // is null
 
         // 
         // populate metadata arrays (for output).
         //    NOW~TIMESTAMP~26~26~6~1
         for(int i=1;i<=cols;i++){
             colName[i] = meta.getColumnName(i).toUpperCase();
             colTypeName[i] = meta.getColumnTypeName(i).toUpperCase();
             colTypeNameOrig[i] = meta.getColumnTypeName(i).toUpperCase();
             colSiz[i] = Math.max(meta.getColumnDisplaySize(i),0);
             colPres[i] = Math.max(meta.getPrecision(i),0);
             colScal[i] = Math.max(meta.getScale(i),0);
             colNul[i] = Math.max(meta.isNullable(i),0) > 0 ? 1 : 0;
             colIsDate[i] = meta.getColumnType(i) == Types.DATE ? true : false;
             if(meta.getColumnType(i) == Types.TIME){
                 colIsTime[i] = true;
             }else if(meta.getColumnType(i) == Types.TIMESTAMP || meta.getColumnTypeName(i).toUpperCase().startsWith("TIME")){
                 colIsTimestamp[i] = true;
             }
             colIsDouble[i] = meta.getScale(i) > 0 ? true : false;            
             String tm = colTypeName[i];
 
             // special case for Oracle (number without precision nor scale).
             if(colTypeName[i].equals("NUMBER") && colPres[i]==0 && colScal[i]==0){
                 colIsDouble[i]=true;
             }
 
             if(tm.matches(".*NUMERIC.*") || tm.matches(".*NUMBER.*")){
                 colTypeName[i] = "NUMBER";
                 colSiz[i] = 0;
             }else if(tm.matches(".*DECIMAL.*") || tm.matches(".*DOUBLE.*") || tm.matches(".*FLOAT.*") || tm.matches(".*REAL.*")){
                 // if(colScal[i] == 0)
                 //    colPres[i] = 0;
                 if(tm.matches(".*DECIMAL.*") && colScal[i] == 0)
                     colIsDouble[i] = false;
                 else
                     colIsDouble[i] = true;
                 colTypeName[i] = "NUMBER";
                 colSiz[i] = 0;
             }else if(tm.matches(".*BIGINT.*") || tm.matches(".*INTEGER.*") || tm.matches(".*SMALLINT.*") || tm.matches(".*TINYINT.*") || tm.matches(".*INT.*")){
                 colTypeName[i] = "NUMBER";
                 colSiz[i] = 0;
                 colScal[i] = 0;
             }else if(tm.matches(".*VARCHAR.*")){
                 colTypeName[i] = "VARCHAR";
                 colPres[i] = colScal[i] = 0;
             }
         }
         
         // 
         // all dates are output in YYYYMMDD format.
         //
         Format dateFormat = new SimpleDateFormat(System.getProperty("dateformat","yyyyMMdd"));
         Format timestampFormat = new SimpleDateFormat(System.getProperty("timestampformat","yyyyMMdd HH:mm:ss.SSS"));
         Format timeFormat = new SimpleDateFormat(System.getProperty("timeformat","HH:mm:ss.SSS"));
 
         String outstr = "";
         
         // 
         // do we display header (or full header) ?
         //  full header includes type, size, precision, scale, null 
         //  (ie: enough info to recreate table in another db).
         //
         if(getEvalProperty("head","off").equals("on") || getEvalProperty("fhead","off").equals("on")){
             StringBuffer b = new StringBuffer();
             boolean fullheader = getEvalProperty("fhead","off").equals("on");
             for(int i=1;i<=cols;i++){
                 b.append(colName[i]);
                 if(fullheader)
                     b.append('~'+colTypeName[i]+'~'+colSiz[i]+'~'+colPres[i]+'~'+colScal[i]+'~'+colNul[i]+'~'+colTypeNameOrig[i]);
                 b.append(delim);
             }
             b.setLength(b.length()-delim.length());
             outstr = b.toString();
             if(ps != null)
                 ps.print(outstr + linesep);
             setProperty("_current_connection_laststmnt_header",outstr);
         }
 
 
         // 
         // output loop
         //
         StringBuffer sb = new StringBuffer(10000);
         NumberFormat nf = NumberFormat.getInstance();
         nf.setGroupingUsed(false);
         nf.setMaximumFractionDigits(0xFF);
         nf.setMaximumIntegerDigits(0xFF);
         nf.setMinimumFractionDigits(0);
         nf.setMinimumIntegerDigits(1);
 
         long outcnt = 0;
 
         char delimChar = delim.charAt(0);
         char delimReplace = getEvalProperty("delimReplace"," ").charAt(0);
 
 
 
         Format[] formats = new Format[cols+1];
         for(int i=1;i<=cols;i++){
             if(System.getProperty(colName[i]+"_format") != null){
                 formats[i] = new SimpleDateFormat(System.getProperty(colName[i]+"_format"));
             } else if(colIsDate[i]){
                 formats[i] = dateFormat;
             } else if(colIsTime[i]){
                 formats[i] = timeFormat;
             } else if(colIsTimestamp[i]){
                 formats[i] = timestampFormat;
             }
         }
 
         String nullval = getEvalProperty("nullval","");
 
         while(rs.next()){
             outcnt++;
             for(int i=1;i<=cols;i++){
                 if(colIsDate[i]){        // if date, then format as YYYYMMDD
                     // java.sql.Date d = rs.getDate(i);
                     java.sql.Timestamp d = rs.getTimestamp(i);
                     if(!rs.wasNull())
                         sb.append(formats[i].format(d));
                     else sb.append(nullval);
                 } else if(colIsTimestamp[i]){        // if date, then format as YYYYMMDDHHMMSS.SSS
                     java.sql.Timestamp d = rs.getTimestamp(i);
                     if(!rs.wasNull())
                         sb.append(formats[i].format(d));
                     else sb.append(nullval);                        
                 } else if(colIsTime[i]){                // time column.
                     java.sql.Time d = rs.getTime(i);
                     if(!rs.wasNull())
                         sb.append(formats[i].format(d));
                     else sb.append(nullval);                        
                 } else if(colIsDouble[i]) {             // if number has decimal point.
                     double d = rs.getDouble(i);
                     if(!rs.wasNull())
                         sb.append(nf.format(d));
                     else sb.append(nullval);                        
                 } else {                        // treat everything else as string.
                     String s = rs.getString(i);
                     if(!rs.wasNull())
                         //sb.append(s);
                         sb.append(s.replace(delimChar,delimReplace).trim());    // trim output strings.
                     else sb.append(nullval);                        
                 }
                 sb.append(delim);
             }
             sb.setLength(sb.length()-delim.length());   // add new line.
             outstr = sb.toString();
             if(ps != null)
                 ps.print(outstr + linesep);                       // output
             sb.setLength(0);                    // reset buffer.
         }
 
         //
         // save the last result.
         //
         setProperty("last",outstr.trim());
         setProperty("_current_connection_laststmnt_lastrow",outstr.trim());
         setProperty("_current_connection_laststmnt_cnt",""+outcnt);
 
         if(getEvalProperty("console","off").equals("on")){
             System.out.println("OK ("+outcnt+" results)");
         }
     }
 
     /**
      * string match; similar to m/.../ in perl
      */
     public static String[] strmatch(String str,String regex){
         Matcher matcher = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(str);
         if(matcher.find()){
             //
             // found match! return array of all matched groups.
             //
             int n = matcher.groupCount();
             String[] s = new String[n+1];
             for(int i=0;i<=n;i++)
                 s[i] = matcher.group(i);
             return s;
         }
         return null;    // no match
     }
 
 
     /**
      * process file (if f is '-' then read stdin)
      */
     public static void cmdProc(String f,PrintStream ps) throws Exception {
         
         // 
         // read input; if '-' then, use standard input, otherwise name of file.
         //
         BufferedReader in = new BufferedReader(f.equals("-") ? new InputStreamReader(System.in) : new FileReader(f), 1024);
         String line;
         String[] m;
         String runbuf = "";
         String sb = "";
         
 
         int lineno = 0,linenosql=0, linsize = 0;
         boolean incomment = false;      // multi-line comment.
         for(;;){
             lineno++;
             linenosql++;
             
             // display a prompt.
             if(getEvalProperty("console","off").equals("on")){
                 String prompt = "";
                 if(sb.trim().length() > 0 ){
                     System.out.printf(" %3d> ",linenosql);
                 }else{
                     System.out.printf("SQL[%s]",timestampFormat.format(new java.util.Date(System.currentTimeMillis())));
                     if(getEvalProperty("lineno","off").equals("on"))
                         System.out.printf("("+lineno+")");
                     if(incomment)
                         System.out.print("--");
                     System.out.print("> ");
                 }
             }
 
             // 
             // read in the next line.
             //
             line = in.readLine();
             line = rtrim(line);
 
 
             // 
             // if EOF, exit.
             //
             if(line == null)
                 break;
             
 
             // size of line (is there anything there?)
             linsize = line.length();
 
             //
             // if we are in a multiline comment.
             //
             if(incomment){
                 //
                 // is comment ending on this line?
                 // 
                 if(line.indexOf("*/") >= 0){
                     // 
                     // cut away the comment. get out of a multiline comment.
                     //
                     line = line.substring( line.indexOf("*/") + 2);
                     incomment = false;
                 }else{
                     //
                     // ignore line; we're in a multi-line comment.
                     //
                     continue;
                 }
             }
 
             // 
             // remove all /* ... */ comments; these span 1 line (multilines are handled separately).
             //
             line = Pattern.compile("/\\*.*?\\*/",Pattern.DOTALL).matcher(line).replaceAll(" ");
 
             //
             // if line is starting a multiline comment.
             //
             if(line.indexOf("/*") >= 0){
                 line = line.substring(0,line.indexOf("/*"));
                 incomment = true;
             }
 
 
             // add support for "fake" sqlrunner syntax :-)
             if( getEvalProperty("nysesqlrunner","off").equals("on") ){
                 String orig_line = line;
                 // support for weird syntax.
                 line = Pattern.compile("--@@\\s*IMPORT\\s*\\('classpath:(.*?)'\\);",
                     Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(line).replaceAll("@$1");
                 line = Pattern.compile("--@@\\s*SET\\s*(.*)",
                     Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(line).replaceAll("def $1");
                 line = Pattern.compile("--@@\\s*ignore_sql_error\\s*=\\s*on\\s*;",
                     Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(line).replaceAll("set errors off;");
                 line = Pattern.compile("--@@\\s*ignore_sql_error\\s*=\\s*off\\s*;",
                     Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(line).replaceAll("set errors on;");
                 //if(!line.equals(orig_line) && getEvalProperty("log","off").equals("on") ){
                 //    System.out.println("\nreplaced: "+orig_line+ " with "+line);
                 //}
             }
 
             //
             // remove the --, #, and // comments.
             //
             line = Pattern.compile("(--|#|//).*").matcher(line).replaceAll("");
             
             line = rtrim(line);
 
             //
             // user wants to run current buffer.
             //
             if(line.equals( getEvalProperty("gocmd","/") )){
                 //
                 // runbuf becomes current buffer (if current buffer isn't empty).
                 //    
                 if(sb.trim().length() > 0)
                     runbuf = sb;
                 sb = "";
                 linenosql = 0;
                 try{
                     // run sql (spool to ps)
                     sqlRun(runbuf,ps);
                 }catch(Exception e){
                     // ignore errors on drops.
                     if(getEvalProperty("errors","on").equals("on") && strmatch(evalString(runbuf),"^\\s*DROP\\s") == null){
                         if(getEvalProperty("console","off").equals("on")){
                             // don't bomb out of console mode.
                             System.out.println("ERROR: "+e.getMessage());
                         }else{
                             throw new Exception(f+"("+lineno+"): "+e.getMessage(),e);
                         }
                     }
                 }
             }else if(linsize == 0){
                 //
                 // if empty line, setup run buffer to current buffer (if current buffer is not empty).
                 //
                 // if(sb.trim().length() > 0)
                 //    runbuf = sb;
                 //sb = "";
                 //linenosql = 0;
             }else if(sb.trim().length() > 0){
                 //
                 // if current buffer isn't empty, then add line to current buffer.
                 //
 
                 //
                 // if there's a ';' end of line, remove it.
                 //
                 if(line.endsWith(getEvalProperty("cmdseparator",";"))){
                     line = line.substring(0,line.length()-1).trim();
                     if(line.length() > 0)
                         sb += line + "\n";
                     
                     //
                     // TODO: refactor the code.
                     // runbuf becomes current buffer (if current buffer isn't empty).
                     //    
                     if(sb.trim().length() > 0)
                         runbuf = sb;
                     sb = "";
                     linenosql = 0;
                     try{
                         // run sql (spool to ps)
                         sqlRun(runbuf,ps);
                         runbuf="";
                     }catch(Exception e){
                         // ignore errors on drops.
                         if(getEvalProperty("errors","on").equals("on") && strmatch(evalString(runbuf),"^\\s*DROP\\s") == null){
                             if(getEvalProperty("console","off").equals("on")){
                                 // don't bomb out of console mode.
                                 System.out.println("ERROR: "+e.getMessage());
                             }else{
                                 throw new Exception(f+"("+lineno+"): "+e.getMessage(),e);
                             }
                         }
                     }
 
                 }else{
                     if(line.length() > 0)
                         sb += line + "\n";
                 }
            }else if(strmatch(line,"^\\s*set\\s+") != null){
                 // 
                 // handle the "set " command.
                 // this can be:  set blah glah on spool env log off
                 //  (ie: consistent with sqlplus, this sets glah blah to "on" and spool env log to "off")
                 //
 
                 //
                 // if there's a ';' end of line, remove it.
                 //            
                 if(line.endsWith(getEvalProperty("cmdseparator",";")))
                     line = line.substring(0,line.length()-1).trim();                
                 
                 Vector<String> v = new Vector<String>();
                 for(String s : line.split("\\s+")){
                     if(s.equalsIgnoreCase("on") || s.equalsIgnoreCase("off")){
                         for(String t : v)
                             setProperty(t,s.toLowerCase());
                         v.removeAllElements();
                     }else if(s.length() > 0){
                         v.add(s);
                     }
                 }
                 if(v.size() > 0){    // if set doesn't have an "on" or "off"
                     if(getEvalProperty("console","off").equals("on")){
                         // don't bomb out of console mode.
                         System.out.println("ERROR: Improperly formatted 'set' command");
                     }else{
                         throw new IllegalStateException(f+"("+lineno+"): Improperly formatted 'set' command.");
                     }
                 }
                 sb = "";
                 linenosql = 0;
            }else if(strmatch(line,"^\\s*(unset|undef(ine)?)\\s+") != null){
                 // 
                 // handle the "unset " command.
                 // this can be:  unset blah glah on spool env log
 
                 // if there's a ';' end of line, remove it.
                 if(line.endsWith(getEvalProperty("cmdseparator",";")))
                     line = line.substring(0,line.length()-1).trim();
                 
                 Vector<String> v = new Vector<String>();
                 for(String s : line.split("\\s+")){
                     if(s.length() > 0)
                         clearProperty(s);
                 }
                 sb = "";
                 linenosql = 0;
             }else if(
                 ((m = strmatch(line,"^\\s*def(ine)?\\s+(\\w+)\\s*=\\s*'([^']+)'\\s*;?\\s*$")) != null) ||
                 ((m = strmatch(line,"^\\s*def(ine)?\\s+(\\w+)\\s*=\\s*[\"]?([^\"]+)[\"]?\\s*;?\\s*$")) != null)
                 ){
                 // 
                 // handle the 'def ' command, ie:
                 //    def tdate='20071031';
                 // 
                 setProperty(m[2],evalString(m[3].trim()));
                 sb = "";
                 linenosql = 0;
             }else if(
                 ((m = strmatch(line,"^\\s*def(ine)?q\\s+(\\w+)\\s*=\\s*'([^']+)'\\s*;?\\s*$")) != null) ||
                 ((m = strmatch(line,"^\\s*def(ine)?q\\s+(\\w+)\\s*=\\s*[\"]?([^\"]+)[\"]?\\s*;?\\s*$")) != null) 
                 ){
                 // 
                 // handle the 'defq ' command, ie:
                 //    defq tdate='&&year.&&month.&&day';
                 // // will set value "as is"; meaning it can be evaluated later. 
                 setProperty(m[2],m[3].trim());
                 sb = "";
                 linenosql = 0;
             }else if(
                 ((m = strmatch(line,"^\\s*echo\\s*'([^']+)'\\s*;?\\s*$")) != null) ||
                 ((m = strmatch(line,"^\\s*echo\\s*[\"]?([^\"]+)[\"]?\\s*;?\\s*$")) != null) 
                 ){
                 //
                 // handle the 'echo ' command, ie:
                 //    echo "hello world"
                 // 
                 if(ps != null)
                     ps.println(evalString(m[1]));
                 sb = "";
                 linenosql = 0;
             }else if(
                 ((m = strmatch(line,"^\\s*spool\\s+'([^']+)'\\s*;?\\s*$")) != null) || 
                 ((m = strmatch(line,"^\\s*spool\\s+[\"]?([^\"]+)[\"]?\\s*;?\\s*$")) != null) 
                 ){
                 //
                 // setup spooling to a file (csv)
                 //
                 setProperty("spool",evalString(m[1].trim()));
                 sb = "";
                 linenosql = 0;
 
                 try{
                     // if ps is not stdout, close it.
                     if(ps != null && ps != System.out)
                         ps.close();
 
                     //
                     // setup spooling to a file (if spool isn't "off")
                     if(getEvalProperty("spool","stdout").equals("stdout")){
                         ps = System.out;
                     }else if(getEvalProperty("spool","stdout").equals("off") || getEvalProperty("spool","stdout").equals("null")){
                         ps = null;
                     }else {
                         ps = new PrintStream( 
                             getEvalProperty("spool","").endsWith(".gz") ?
                             new GZIPOutputStream( new FileOutputStream( System.getProperty("spool") ) ) :  
                             new FileOutputStream( System.getProperty("spool") )
                         );
                     }
                 }catch(Exception e){
                             if(getProperty("console","off").equals("on")){
                                 // don't bomb out of console mode.
                                 System.out.println("ERROR: "+e.getMessage());
                             }else{
                                 throw new Exception(f+"("+lineno+"): "+e.getMessage(),e);
                             }
                     
                 }
             }else if(
                 ((m = strmatch(line,"^\\s*@\\s*'([^']+)'\\s*;?\\s*$")) != null) || 
                 ((m = strmatch(line,"^\\s*@\\s*[\"]?([^\"]+)[\"]?\\s*;?\\s*$")) != null)
                 ){
                 //
                 // recursively process a file
                 //
                 line = evalString(m[1]);
                 sb = "";
                 linenosql = 0;
 
                 // check if we're already processing this file.
                 if(processingFiles.contains(line)){
                     throw new IllegalStateException("cannot recursively include: "+line);
                 }
 
                 try {
                     // push file to stack
                     processingFiles.push(line);
                     
                     // if .properties file, then load it into properties.
                     if(line.toLowerCase().endsWith(".properties")){
                         props.load(new FileInputStream(line));
                     }else{
                         setProperty("_"+line+"_startmillis",""+System.currentTimeMillis());
                         cmdProc(line,ps);
                     }
                 }catch(Exception e){
                     if(getEvalProperty("console","off").equals("on")){
                         // don't bomb out of console mode.
                         System.out.println("ERROR: "+e.getMessage());
                     }else{
                         throw new Exception(f+"("+lineno+"): "+e.getMessage(),e);
                     }
                 }finally{
                     setProperty("_"+line+"_endmillis",""+System.currentTimeMillis());
 
                     if(getEvalProperty("log","off").equals("on")){
                         long millis = 
                             Long.parseLong( getProperty("_"+line+"_startmillis","0") ) - 
                             Long.parseLong( getProperty("_"+line+"_endmillis","0"));
                         System.out.printf("\n-- File %s completed in: %.2f minutes.\n",line,(millis/1000.0)/60.0);
                     }
                     // remove file from stack.
                     processingFiles.pop();
                 }
 
             }else if(strmatch(line,"^\\s*exit\\s*;?\\s*$") != null){
 
                 setProperty("_TOTAL_endmillis",""+System.currentTimeMillis());
                 if(getEvalProperty("log","off").equals("on")){
                     long millis = 
                         Long.parseLong( getProperty("_TOTAL_endmillis","0") ) - 
                         Long.parseLong( getProperty("_TOTAL_startmillis","0"));
                     System.out.printf("\n-- Total SQLRunner execution time: %.2f minutes.\n",(millis/1000.0)/60.0);
                 }
 
                 //
                 // exits the program.
                 //
                 System.exit(0);
             }else if(strmatch(line,"^\\s*quit\\s*;?\\s*$") != null){
                 //
                 // ends processing of current file.
                 //
                 break;
             }else if(line.length() > 0){
                 //
                 // if not one of the commands, then starts new buffer.
                 //
 
                 //
                 // if there's a ';' end of line, remove it.
                 //
                 if(line.endsWith(getEvalProperty("cmdseparator",";"))){
                     line = line.substring(0,line.length()-1).trim();
 
                     if(line.length() > 0)
                         sb += line + "\n";
                     
                     //
                     // TODO: refactor the code.
                     // runbuf becomes current buffer (if current buffer isn't empty).
                     //    
                     if(sb.trim().length() > 0)
                         runbuf = sb;
                     sb = "";
                     linenosql = 0;
                     try{
                         // run sql (spool to ps)
                         sqlRun(runbuf,ps);
                         runbuf="";
                     }catch(Exception e){
                         // ignore errors on drops.
                         if(getEvalProperty("errors","on").equals("on") && strmatch(evalString(runbuf),"^\\s*DROP\\s") == null){
                             if(getEvalProperty("console","off").equals("on")){
                                 // don't bomb out of console mode.
                                 System.out.println("ERROR: "+e.getMessage());
                             }else{
                                 throw new Exception(f+"("+lineno+"): "+e.getMessage(),e);
                             }
                         }
                     }
 
                 }else{
                     sb += line + "\n";
                 }
             }
         }
 
 
         // if hit EOF, with no end of line, then assume that's a SQL statement.
         // ie: echo "select * blah where glah=323" | sqlrunner db=glah - 
         if(sb.trim().length() > 0){
             runbuf = sb;
             sb = "";
             linenosql = 0;
             try{
                 // run sql (spool to ps)
                 sqlRun(runbuf,ps);
                 runbuf="";
             }catch(Exception e){
                 // ignore errors on drops.
                 if(getEvalProperty("errors","on").equals("on") && strmatch(evalString(runbuf),"^\\s*DROP\\s") == null){
                     if(getEvalProperty("console","off").equals("on")){
                         // don't bomb out of console mode.
                         System.out.println("ERROR: "+e.getMessage());
                     }else{
                         throw new Exception(f+"("+lineno+"): "+e.getMessage(),e);
                     }
                 }
             }
         }
     }
 
     /**
      * loop through command line args... and run stuff that appears there.
      */
     public static void main(String[] args) throws Exception {
 
         setProperty("_TOTAL_startmillis",""+System.currentTimeMillis());
 
         // defaults that user can change.
         setProperty("env_default","off");
         setProperty("log_default","off");
         setProperty("console_default","off");
         setProperty("errors_default","on");
         setProperty("pretend_default","off");
         setProperty("debug_default","off");
         setProperty("spool_default","stdout");
         setProperty("cmdseparator_default",";");
         setProperty("gocmd_default","/");
         setProperty("delimReplace_default"," ");
         setProperty("delim_default",",");
         setProperty("linesep_default","\n");
         setProperty("outformat_default","delim");
         setProperty("db_default","db");
         setProperty("version","unknown");
         setProperty("amp","&");
         setProperty("tab","\t");
         setProperty("nullval","");
         // 
         setProperty("tns","&&amp.&&amp.&&db._tns");
         setProperty("url","&&amp.&&amp.&&db._url");
         setProperty("driver","&&amp.&&amp.&&db._driver");
         setProperty("user","&&amp.&&amp.&&db._user");
         setProperty("pass","&&amp.&&amp.&&db._pass");
         setProperty("dbname","&&amp.&&amp.&&db._dbname");
         setProperty("host","&&amp.&&amp.&&db._host");
         setProperty("port","&&amp.&&amp.&&db._port");
 
         //
         // use environment as default params
         // 
         Map<String, String> env = System.getenv();
         for (String envName : env.keySet()) {
             setProperty(envName,env.get(envName));
         }
 
         for(String cmd : args){
             //
             // if someone types name=value in command line, then set it as environment.
             // 
             String[] nv = cmd.split("=",2);
             setProperty(nv[0],(nv.length > 1 ? nv[1] : "on"));
             
             //
             // if .properties, then load it into system properties. 
             // else process it as input file.
             //
             if(cmd.endsWith(".properties")){      // this is a properties file.
                 props.load(new FileInputStream(cmd));
             }else if(cmd.toLowerCase().endsWith(".sql") || cmd.equals("-")){  // run this sql code
                 try {
                     setProperty("_"+cmd+"_startmillis",""+System.currentTimeMillis());
                     cmdProc(cmd,System.out);
                 } finally { 
                     setProperty("_"+cmd+"_endmillis",""+System.currentTimeMillis());
 
                     if(getEvalProperty("log","off").equals("on")){
                         long millis = 
                             Long.parseLong( getProperty("_"+cmd+"_endmillis","0") ) - 
                             Long.parseLong( getProperty("_"+cmd+"_startmillis","0"));
                         System.out.printf("\n-- File %s completed in: %.2f minutes.\n",cmd,(millis/1000.0)/60.0);
                     }
                 }
             }
         }
 
         setProperty("_TOTAL_endmillis",""+System.currentTimeMillis());
         if(getEvalProperty("log","off").equals("on")){
             long millis = 
                 Long.parseLong( getProperty("_TOTAL_endmillis","0") ) - 
                 Long.parseLong( getProperty("_TOTAL_startmillis","0"));
             System.out.printf("\n-- Total SQLRunner execution time: %.2f minutes.\n",(millis/1000.0)/60.0);
         }
     }
 }
 
