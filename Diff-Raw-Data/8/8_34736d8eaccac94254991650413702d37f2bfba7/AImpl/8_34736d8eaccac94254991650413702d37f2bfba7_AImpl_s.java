 package com.bitmechanic.barrister.conform;
 
 import com.bitmechanic.barrister.RpcException;
 import com.bitmechanic.barrister.RpcRequest;
 import com.bitmechanic.test.*;
 import java.util.List;
 import java.util.ArrayList;
 
 public class AImpl implements A {
 
     public Long add(Long a, Long b) throws RpcException {
         return a+b;
     }
     
     public Double add_all(Double[] nums) throws RpcException {
         double total = 0;
         for (Double d : nums) {
             total += d;
         }
         return total;
     }
     
     public Double sqrt(Double a) throws RpcException {
         return Math.sqrt(a);
     }
     
     public RepeatResponse repeat(RepeatRequest req1) throws RpcException {
         RepeatResponse r = new RepeatResponse();
         List<String> items = new ArrayList<String>();
         for (int i = 0; i < req1.getCount(); i++) {
             items.add(req1.getTo_repeat());
         }
         r.setItems(items.toArray(new String[0]));
         r.setCount(req1.getCount());
         r.setStatus(Status.ok);
         return r;
     }
     
     public HiResponse say_hi() throws RpcException {
         HiResponse r = new HiResponse();
         r.setHi("hi");
         return r;
     }
     
 }
