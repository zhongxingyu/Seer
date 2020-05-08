 package me.donnior.srape;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.google.common.base.Joiner;
 
 public class FieldBuilderImpl implements ScopedFieldBuilder{
 
     private String name;
     private Class<? extends SrapeEntity> clz;
     private boolean condition;
     private boolean hasConditon;
     private boolean hasName;
     private Object value;
     
     public FieldBuilderImpl(Object value) {
         this.value = value;
     }
 
     public ConditionalFieldBuilder withNameAndType(String name, Class<? extends SrapeEntity> entityClass) {
         this.name = name;
         this.clz = entityClass;
         this.hasName = true;
         return this;
     }
 
     public ConditionalFieldBuilder withName(String string) {
         return this.withNameAndType(string, null);        
     }
     
     public ConditionalFieldBuilder withType(Class<? extends SrapeEntity> entityClass) {
         this.hasName = false;
        return this.withNameAndType(null, entityClass);        
     }
     
     
     
     public String getName() {
         return name;
     }
     
     public Class<? extends SrapeEntity> getEntityClass(){
         return this.clz;
     }
     
     public boolean hasEntityType(){
         return this.clz != null;
     }
 
     public boolean isCollectionValue(){
         return this.value != null && 
                 this.value instanceof Collection;
 //               (this.value instanceof Collection || this.value.getClass().isArray());
 
     }
     
     public boolean isArrayData(){
         return this.value != null && (this.isCollectionValue() || this.value.getClass().isArray());
     }
     
     public void unless(boolean condition){
         this.when(!condition);
     }
     
     public void when(boolean condition){
         this.hasConditon = true;
         this.condition = condition;
     }
     
     public boolean conditionMatched(){
         return hasConditon ? condition : true;
     }
 
     public boolean hasCondition(){
         return hasConditon;
     }
     
     public boolean hasName(){
         return this.hasName;
     }
     
     public Object getValue() {
         return value;
     }
     
     public String toJson(){
//        return "{" + contentWithNameAndValue(this.name, this.value) +"}";
         Object name = this.name != null ? this.name :"";
        
         return contentWithNameAndValue(name, this.value, this.hasName);
     }
     private String contentWithNameAndValue(Object name, Object value, boolean hasName){
         if(hasName){
             return StringUtil.quote(name.toString()) + ":" + _value0(value);   
         } else {
             return _value0(value).toString();
         }
     }
     
     private Object _value0(Object value){
         if(value == null){
             //TODO deal with null
             return "null";
         }
         if(value instanceof Boolean){
             return value.toString();
         }
         if(value instanceof Number){
             return value;
         }
 
         if(value instanceof String){
 //            return StringUtil.quote(value.toString());
             return quote(value.toString()); //TODO is this enough? like string \" escaping?
         }
         
         if(!isArrayData() && this.clz != null){
             return buildEntity(this.value,this.clz);
         }
         
         if (value.getClass().isArray()) {
             StringBuilder sb = new StringBuilder();
             sb.append("[");
             int length = Array.getLength(value);
             List<Object> values = new ArrayList<Object>();
             for (int i = 0; i < length; i ++) {
                 Object arrayElement = Array.get(value, i);
                 if(hasEntityType()){
                     values.add(buildEntity(arrayElement, this.clz));
                 } else {
                     values.add(_value0(arrayElement));
                 }
             }
             sb.append(Joiner.on(",").join(values));
             sb.append("]");
             return sb.toString();
         }
         
         //TODO more complex need
         if(value instanceof Collection){
             
                 //TODO data with normal type, fall back to gson
                 StringBuilder sb = new StringBuilder();
                 sb.append("[");
                 
                 List<Object> values = new ArrayList<Object>();
                 Iterator<Object> it = ((Collection)value).iterator();
                 while(it.hasNext()){
 //                    values.add(_value0(it.next()));
                     if(hasEntityType()){
                         values.add(buildEntity(it.next(), this.clz));
                     } else {
                         values.add(_value0(it.next()));
                     }
                 }
                 sb.append(Joiner.on(",").join(values));
                 sb.append("]");
                 return sb.toString();
             
         } 
         
         if(value instanceof Map){
             //TODO map data
             Iterator<Entry<Object, Object>> it = ((Map)value).entrySet().iterator();
             StringBuilder sb = new StringBuilder();
             sb.append("{");
             List<String> collector = new ArrayList<String>();
             while(it.hasNext()){
                 Entry<Object, Object>  entry = it.next();
                 collector.add(contentWithNameAndValue(entry.getKey(),   entry.getValue(), true));
             }
             sb.append(Joiner.on(",").join(collector));
             sb.append("}");
             return sb.toString();
         } 
         
         return StringUtil.quote(value.toString());
     }
     
     
     private Object buildEntity(Object value, Class<? extends SrapeEntity> clz) {
         try {
             SrapeEntity entity = clz.newInstance();
             FieldsExpositionHolder holder = new FieldsExpositionHolder();
             entity.config(value, holder);
             return holder.build();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Is this field exposition a pure array data? means it value is data type and don't have a
      * explicit name. You can't use {@link #withName(String)} or {@link #withNameAndType(String, Class)}
      * to define a field exposition if you want to make it pure data, you can use {@link #withType(Class)}
      * or just ignore the 'with' clause. 
      * 
      * <br />
      * <br />
      * 
      * If this field exposition is pure array, it would be output as <pre><code>[1,2,3]</code></pre>
      * 
      * 
      * Otherwise it will be output as <pre><code>{"name": xxxx}</code></pre>
      * 
      * @return
      */
     public boolean isPureArrayData(){
         return this.isArrayData() && !this.hasName;
     }
     
     public static String quote(String string) {
         if (string == null || string.length() == 0) {
             return "\"\"";
         }
 
         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         String       t;
 
         sb.append('"');
         for (i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
                 sb.append('\\');
                 sb.append(c);
                 break;
             case '/':
 //                if (b == '<') {
                     sb.append('\\');
 //                }
                 sb.append(c);
                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\r");
                break;
             default:
                 if (c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         sb.append('"');
         return sb.toString();
     }
 }
