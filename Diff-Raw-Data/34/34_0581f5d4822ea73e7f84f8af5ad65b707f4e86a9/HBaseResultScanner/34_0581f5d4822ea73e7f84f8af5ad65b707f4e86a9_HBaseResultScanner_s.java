 package com.nearinfinity.mysqlengine.scanner;
 
 import org.apache.hadoop.hbase.client.Result;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/21/12
  * Time: 7:49 AM
  * To change this template use File | Settings | File Templates.
  */
 public interface HBaseResultScanner {
     public Result next(byte[] valueToSkip) throws IOException;
 
     public void close();
 
     public Result getLastResult();
 }
