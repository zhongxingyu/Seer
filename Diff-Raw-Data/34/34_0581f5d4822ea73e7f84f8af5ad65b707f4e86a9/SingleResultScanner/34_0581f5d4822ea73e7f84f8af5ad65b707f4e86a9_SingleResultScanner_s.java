 package com.nearinfinity.mysqlengine.scanner;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/21/12
  * Time: 9:05 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SingleResultScanner implements HBaseResultScanner {
     private ResultScanner scanner;
     private Result lastResult;
 
     public SingleResultScanner(ResultScanner scanner) {
         this.scanner = scanner;
     }
 
     @Override
     public Result next(byte[] valueToSkip) throws IOException {
         if (scanner == null) {
             return null;
         }
 
         Result result = this.scanner.next();
         this.lastResult = result;
         return result;
     }
 
     @Override
     public void close() {
         if (scanner != null) {
             this.scanner.close();
         }
     }
 
     @Override
     public Result getLastResult() {
         return this.lastResult;
     }
 }
