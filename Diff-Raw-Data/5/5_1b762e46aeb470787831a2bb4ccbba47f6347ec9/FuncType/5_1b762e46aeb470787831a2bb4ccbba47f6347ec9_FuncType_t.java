 import java.util.LinkedHashMap;
 
 public class FuncType extends Type
 {
  public Type returntype;
   public LinkedHashMap<String, Type> params;
   public FuncType()
   {
     params = new LinkedHashMap<String, Type>();
   }
}
