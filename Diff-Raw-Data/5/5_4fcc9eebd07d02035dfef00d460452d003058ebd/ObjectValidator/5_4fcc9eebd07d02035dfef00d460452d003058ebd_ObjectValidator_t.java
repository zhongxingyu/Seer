 package halo.util.validator;
 
 import halo.util.ClassInfo;
 import halo.util.ClassInfoFactory;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class ObjectValidator {
 
     private Object instance;
 
     private ValidatorCreator validatorCreator;
 
     private String tmp_key;
 
     private String tmp_message;
 
     private JsonObj jsonObj;
 
     private String filePath;
 
     private final Map<String, String> exprMap = new LinkedHashMap<String, String>();
 
     public <T> ObjectValidator(T instance) {
         super();
         this.instance = instance;
     }
 
     public <T> ObjectValidator(T instance, ValidatorCreator validatorCreator) {
         this(instance, validatorCreator, null);
     }
 
     public ObjectValidator(Object instance, String filePath) {
         this(instance, null, filePath);
     }
 
     public ObjectValidator(Object instance, ValidatorCreator validatorCreator,
             String filePath) {
         this.instance = instance;
         this.validatorCreator = validatorCreator;
         this.setFilePath(filePath);
         if (this.validatorCreator == null) {
             this.validatorCreator = ValidatorCreator
                     .getDefaultValidatorCreator();
         }
     }
 
     public <T> ErrResult exec() {
         @SuppressWarnings("unchecked")
         ClassInfo<T> classInfo = (ClassInfo<T>) ClassInfoFactory
                 .getClassInfo(instance.getClass());
         Set<Entry<String, String>> set = exprMap.entrySet();
         Field field;
         Object value;
         Validator validator;
         for (Entry<String, String> e : set) {
             field = classInfo.getField(e.getKey());
             try {
                 value = field.get(instance);
                 this.parseExpr(e.getValue());
                 validator = this.validatorCreator.getValidator(tmp_key);
                 if (!validator.exec(this.jsonObj, value)) {
                     return new ErrResult(e.getKey(), this.tmp_message);
                 }
             }
             catch (Exception e1) {
                 throw new RuntimeException(e1);
             }
         }
         return null;
     }
 
     public <T> List<ErrResult> execBatch() {
         List<ErrResult> list = new ArrayList<ErrResult>();
         @SuppressWarnings("unchecked")
         ClassInfo<T> classInfo = (ClassInfo<T>) ClassInfoFactory
                 .getClassInfo(instance.getClass());
         Set<Entry<String, String>> set = exprMap.entrySet();
         Field field;
         Object value;
         Validator validator;
         for (Entry<String, String> e : set) {
             field = classInfo.getField(e.getKey());
             if (field == null) {
                throw new RuntimeException("no field [ " + e.getKey()
                        + " ] in class[ " + classInfo.getClazz().getName()
                        + " ]");
             }
             try {
                 value = field.get(instance);
                 this.parseExpr(e.getValue());
                 validator = this.validatorCreator.getValidator(tmp_key);
                 if (validator == null) {
                     throw new RuntimeException("unknown validate [ "
                             + e.getValue() + " ]");
                 }
                 if (!validator.exec(this.jsonObj, value)) {
                     list.add(new ErrResult(e.getKey(), this.tmp_message));
                 }
             }
             catch (Exception e1) {
                 throw new RuntimeException(e1);
             }
         }
         return list;
     }
 
     private void parseExpr(String expr) {
         int idx = expr.indexOf("{");
         this.tmp_key = expr.substring(0, idx);
         String json = expr.substring(idx);
         this.jsonObj = JsonUtil.getJsonObj(json);
         this.tmp_message = this.jsonObj.getString("msg");
     }
 
     public void setFilePath(String filePath) {
         this.filePath = filePath;
         if (this.filePath != null) {
             if (this.filePath.startsWith("classpath:")) {
                 String p = this.filePath.substring(10);
                 if (p.startsWith("/")) {
                     p = p.substring(1);
                 }
                 this.filePath = this.getClass().getClassLoader()
                         .getResource("").getPath()
                         + p;
             }
             this.addExprFromFile(this.filePath);
         }
     }
 
     public String getFilePath() {
         return filePath;
     }
 
     public ValidatorCreator getValidatorCreator() {
         return validatorCreator;
     }
 
     public void addExprFromMap(Map<String, String> map) {
         exprMap.putAll(map);
     }
 
     public void addExprFromFile(String filePath) {
         this.addExprFromMap(ValidateFileParser.parseFile(filePath));
     }
 
     public void addExpr(String fieldName, String expr) {
         exprMap.put(fieldName, expr);
     }
 }
