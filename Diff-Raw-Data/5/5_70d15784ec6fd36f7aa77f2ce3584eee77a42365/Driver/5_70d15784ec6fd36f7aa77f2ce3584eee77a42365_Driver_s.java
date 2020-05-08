 package com.nearinfinity.mysqlengine;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 7/25/12
  * Time: 2:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Driver {
     public static void main(String[] args) throws IOException {
         new Driver().go(args);
     }
 
     public void go(String[] args) throws IOException {
         HBaseClient client = new HBaseClient("sql");
 
         if (args[0].equals("create")) {
             //create table_name column*
             String tableName = args[1];
             List<String> columns = new LinkedList<String>();
             for (int i = 2 ; i < args.length ; i++) {
                 columns.add(args[i]);
             }
 
             client.createTableFull(tableName, columns);
         }
         else if (args[0].equals("put")) {
             //put table_name column=value*
             String tableName = args[1];
            Map<String, ByteBuffer> values = new LinkedHashMap<String, ByteBuffer>();
             for (int i = 2; i < args.length ; i++) {
                 String [] tokens = args[i].split("=");
                values.put(tokens[0], ByteBuffer.wrap(tokens[1].getBytes()));
             }
 
             client.writeRow(tableName, values);
         }
         else if (args[0].equals("scan")) {
             //scan table_name
             String tableName = args[1];
 
             List<Map<String, byte[]>> rows = client.fullTableScan(tableName);
 
             StringBuilder sb = new StringBuilder();
             for (Map<String, byte[]> row : rows) {
                 sb.append("{ ");
                 for (String key : row.keySet()) {
                     sb.append("\'" + key + "\' => \'" + new String(row.get(key)) + "\', ");
                 }
                 sb.append("}\n");
             }
             System.out.println(sb);
         }
     }
 }
