 package zhenghui.jvm;
 
 import zhenghui.jvm.parse.BaseInfoParse;
 import zhenghui.jvm.parse.ConstantPoolParse;
 import zhenghui.jvm.parse.ParseResult;
 
 /**
  *
  * User: zhenghui
  * Date: 13-1-10
  * Time: 2:19
  *
  */
 public class Main {
 
     public static void main(String[] args) throws Exception {
         ClassLoader cl = new ClassLoader();
         String code = cl.loadClass("d:\\Test.class");
        //Ϣ
         BaseInfoParse baseInfoParse = new BaseInfoParse(code);
         for(String str : baseInfoParse.parseBaseInfo()){
             System.out.println(str);
         }
        //
         ConstantPoolParse constantPoolParse = new ConstantPoolParse(code);
         ParseResult result = constantPoolParse.pareContantPool();
         for(String str : result.getStrs()){
             System.out.println(str);
         }
        //ʱ
         result = baseInfoParse.parseAccessFlags(result.getHandle());
         for(String str : result.getStrs()){
             System.out.println(str);
         }
     }
 }
