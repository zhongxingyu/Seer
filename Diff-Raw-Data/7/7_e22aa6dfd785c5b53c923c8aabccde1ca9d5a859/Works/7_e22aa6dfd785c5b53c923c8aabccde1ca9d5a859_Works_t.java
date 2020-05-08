 package sorcer.ex2.requestor;
 
 import sorcer.ex2.provider.InvalidWork;
 import sorcer.ex2.provider.Work;
 import sorcer.service.Context;
 import sorcer.service.ContextException;
 
 import java.io.Serializable;
 
 public class Works implements Serializable {
 
     public static Work work1, work2, work3, work4;
 
     static {
         work1 = new Work() {
             public Context exec(Context cxt) throws InvalidWork, ContextException {
                 String p =  cxt.getPrefix();
                 int arg1 = (Integer) cxt.getValue(p+"requestor/operand/1");
                 int arg2 = (Integer) cxt.getValue(p+"requestor/operand/2");
                 cxt.putOutValue(p+"provider/result", arg1 + arg2);
                if (cxt.getReturnPath() != null) {
                    cxt.setReturnValue(arg1 + arg2);
                }
                 return cxt;
             }
         };
 
         work2 = new Work() {
             public Context exec(Context cxt) throws InvalidWork, ContextException {
                 String p =  cxt.getPrefix();
                 int arg1 = (Integer) cxt.getValue(p+"requestor/operand/1");
                 int arg2 = (Integer) cxt.getValue(p+"requestor/operand/2");
                 cxt.putOutValue(p+"provider/result", arg1 * arg2);
                if (cxt.getReturnPath() != null) {
                    cxt.setReturnValue(arg1 * arg2);
                }
                 return cxt;
             }
         };
 
         work3 = new Work() {
             public Context exec(Context cxt) throws InvalidWork, ContextException {
                 String p =  cxt.getPrefix();
                 int arg1 = (Integer) cxt.getValue(p+"requestor/operand/1");
                 int arg2 = (Integer) cxt.getValue(p+"requestor/operand/2");
                 cxt.putOutValue(p+"provider/result", arg1 - arg2);
                 return cxt;
             }
         };
 
         work4 = new Work() {
             public Context exec(Context context) throws InvalidWork, ContextException {
                 double arg1 = (Double)context.getValue("requestor/operand/1");
                 double arg2 = (Double)context.getValue("requestor/operand/2");
                 double arg3 = (Double)context.getValue("requestor/operand/3");
                 context.putOutValue("provider/result", (arg1 + arg2 + arg3)/3);
                 return context;
             }
         };
     }
 }
