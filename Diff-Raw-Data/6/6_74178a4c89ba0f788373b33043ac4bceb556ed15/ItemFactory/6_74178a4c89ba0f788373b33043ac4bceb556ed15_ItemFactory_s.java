 package net.virtualvoid.android.browser;
 
 import java.io.File;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class ItemFactory {
     static abstract class MappedItemList<T> implements ItemList{
         private CharSequence name;
 
         public MappedItemList(CharSequence name) {
             this.name = name;
         }
 
         protected abstract T getOriginal(int position);
         protected abstract Item map(T object);
         @Override
         public Item get(int position) {
             return map(getOriginal(position));
         }
         @Override
         public CharSequence getName() {
             return name;
         }
 
     }
     static abstract class MappedListItemList<T> extends MappedItemList<T>{
         private List<T> items;
 
         public MappedListItemList(String name, List<T> items) {
             super(name);
             this.items = items;
         }
 
         @Override
         public int size() {
             return items.size();
         }
         @Override
         protected T getOriginal(int index) {
             return items.get(index);
         }
     }
     static abstract class MappedArrayItemList<T> extends MappedItemList<T>{
         private T[] items;
 
         public MappedArrayItemList(String name, T[] items) {
             super(name);
             this.items = items;
         }
         @Override
         public int size() {
             return items.length;
         }
         @Override
         protected T getOriginal(int arg0) {
             return items[arg0];
         }
     }
     private static ItemList fromArray(final String name,final Item...is){
         return new ItemList(){
             @Override
             public Item get(int position) {
                 return is[position];
             }
             @Override
             public CharSequence getName() {
                 return name;
             }
             @Override
             public int size() {
                 return is.length;
             }
         };
     }
     private static ItemList join(final String name,final ItemList list1,final ItemList list2){
         return new ItemList(){
             @Override
             public Item get(int position) {
                 int len1 = list1.size();
                 if (position < len1)
                     return list1.get(position);
                 else
                     return list2.get(position-len1);
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public int size() {
                 return list1.size()+list2.size();
             }
         };
     }
 
     private static Item materialize(final MetaItem metaItem,final Object o){
         return new Item(){
             @Override
             public Object get() {
                 return metaItem.get(o);
             }
             @Override
             public CharSequence getName() {
                 return metaItem.getName();
             }
             @Override
             public Class<?> getReturnType() {
                 return metaItem.getReturnType();
             }
         };
     }
     private static ItemList materialize(final MetaItemList metaList,final Object o){
         return new ItemList(){
             @Override
             public Item get(int position) {
                 return materialize(metaList.get(position), o);
             }
             @Override
             public CharSequence getName() {
                 return metaList.getName();
             }
             @Override
             public int size() {
                 return metaList.size();
             }
         };
     }
     private final static ItemList emptyList = new ItemList(){
         @Override
         public String getName() {
             return "<empty>";
         }
         @Override
         public int size() {
             return 0;
         }
         @Override
         public Item get(int position) {
             throw new NoSuchElementException("no such position "+position);
         }
     };
     private static ItemList singleton(final String name,final Object o){
         final Item item = single(name,o);
         return o == null ? emptyList : new ItemList(){
             @Override
             public Item get(int position) {
                 if (position == 0)
                     return item;
                 else
                     throw new NoSuchElementException("At position "+position);
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public int size() {
                 return 1;
             }
         };
     }
     private static Item single(final String name,final Object o){
         return new Item(){
             @Override
             public Object get() {
                 return o;
             }
             @Override
             public String getName() {
                 return name;
             }
             @Override
             public Class<?> getReturnType() {
                 return o.getClass();
             }
         };
     }
     private static ItemList elementsOfArray(final Object array){
         final int len = Array.getLength(array);
         final Item[] items = new Item[len];
         return new ItemList(){
             @Override
             public Item get(final int index) {
                 Item item = items[index];
                 if (item == null){
                     item = new Item(){
                         @Override
                         public Object get() {
                             return Array.get(array, index);
                         }
                         @Override
                         public String getName() {
                             return Integer.toString(index);
                         }
                         @Override
                         public Class<?> getReturnType() {
                             Object val = get();
                             return val!=null ? val.getClass() : array.getClass().getComponentType();
                         }
                     };
                     items[index] = item;
                 }
                 return item;
             }
             @Override
             public String getName() {
                 return "Elements";
             }
             @Override
             public int size() {
                 return len;
             }
         };
     }
     private static ItemList elementsOfMap(final Map<?,?> map){
         ArrayList<Object> keys = new ArrayList<Object>();
         for (Object o:map.keySet())
             keys.add(o);
         return new MappedListItemList<Object>("Values",keys){
             @Override
             protected Item map(final Object key) {
                 return new Item(){
                     @Override
                     public Object get() {
                         return map.get(key);
                     }
                     @Override
                     public String getName() {
                        return key.toString();
                     }
                     @Override
                     public Class<?> getReturnType() {
                         Object val = get();
                         return val!=null ? val.getClass() : Object.class;
                     }
                 };
             }
         };
     }
     private static final int MAX_ELEMENTS = 100;
     private static ItemList elementsOfIterable(final Iterable<?> i){
         final ArrayList<Object> res = new ArrayList<Object>(MAX_ELEMENTS/2);
 
         int num = 0;
         for (Object o:i)
             if (num < MAX_ELEMENTS){
                 res.add(o);
                 num++;
             }
             else
                 break;
         final int numElements = num;
         return join("Elements"
                    ,singleton("Size",num >= MAX_ELEMENTS?">= "+MAX_ELEMENTS:num)
                    ,new ItemList(){
                         @Override
                         public Item get(final int position) {
                             return new Item(){
                                 @Override
                                 public Object get() {
                                     return res.get(position);
                                 }
                                 @Override
                                 public String getName() {
                                     return Integer.toString(position);
                                 }
                                 @Override
                                 public Class<?> getReturnType() {
                                     Object val = get();
                                     return val!=null ? val.getClass() : Object.class;
                                 }
                             };
                         }
                         @Override
                         public String getName() {
                             return "Elements";
                         }
                         @Override
                         public int size() {
                             return numElements;
                         }
                    });
     }
     private static ItemList contentsOfDirectory(final File dir){
         return join(
                 "Contents",
                 singleton("..",dir.getParentFile())
                 ,new MappedArrayItemList<File>("Contents",dir.listFiles()){
                     @Override
                     protected Item map(final File f) {
                         return new Item(){
                             @Override
                             public Object get() {
                                 return f;
                             }
                             @Override
                             public String getName() {
                                 return f.getName();
                             }
                             @Override
                             public Class<?> getReturnType() {
                                 return File.class;
                             }
                         };
                     }
                 });
     }
     private static ItemList tablesOfDb(final String dbPath){
         final SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
         final Cursor cursor = db.rawQuery("select name from sqlite_master where type='table'", null);
         return new MappedItemList<String>("Tables"){
             @Override
             protected String getOriginal(int position) {
                 cursor.moveToPosition(position);
                 return cursor.getString(0);
             }
             @Override
             protected Item map(final String table) {
                 return new Item(){
                     @Override
                     public Object get() {
                         return db.rawQuery("select * from "+table,null);
                     }
                     @Override
                     public CharSequence getName() {
                         return table;
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return Cursor.class;
                     }
                 };
             }
             @Override
             public int size() {
                 return cursor.getCount();
             }
         };
     }
     private static ItemList resultsOfQuery(final Cursor cursor){
         return new ItemList(){
             @Override
             public Item get(final int position) {
                 return new Item(){
                     @Override
                     public Object get() {
                         cursor.moveToPosition(position);
                         return DBTools.rowAsMap(cursor);
                     }
                     @Override
                     public CharSequence getName() {
                         return Integer.toString(position);
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return Map.class;
                     }
                 };
             }
             @Override
             public CharSequence getName() {
                 return "Results";
             }
             @Override
             public int size() {
                 return cursor.getCount();
             }
         };
     }
     private static MappedListItemList<PackageInfo> packagesFromPM(final PackageManager pm) {
         return new MappedListItemList<PackageInfo>("Installed Packages",pm.getInstalledPackages(0xffffffff)){
             @Override
             protected Item map(final PackageInfo info) {
                 return new Item(){
                     @Override
                     public Object get() {
                         return info;
                     }
                     @Override
                     public CharSequence getName() {
                         return pm.getApplicationLabel(info.applicationInfo);
                     }
                     @Override
                     public Class<?> getReturnType() {
                         return ApplicationInfo.class;
                     }
                 };
             }
         };
     }
     private static ItemList informationFor(final Object o){
         return fromArray("This"
                     ,new Item(){
                         @Override
                         public Object get() {
                             return o.toString();
                         }
                         @Override
                         public CharSequence getName() {
                             return "String representation";
                         }
                         @Override
                         public Class<?> getReturnType() {
                             return String.class;
                         }
                     }
                     ,new Item(){
                         @Override
                         public Object get() {
                             return o.getClass();
                         }
                         @Override
                         public CharSequence getName() {
                             return "Class";
                         }
                         @Override
                         public Class<?> getReturnType() {
                             return Class.class;
                         }
                     });
     }
     static abstract class MappedArray implements ItemLists{
         List<MetaItemList> metaLists;
         public MappedArray(Class<?> clazz) {
             this.metaLists = MetaItemFactory.metaItemsFor(clazz);
         }
         @Override
         public int size() {
             return metaLists.size();
         }
         @Override
         public ItemList get(int pos){
             final MetaItemList metaList = metaLists.get(pos);
             return new ItemList(){
                 @Override
                 public CharSequence getName() {
                     return "Map with "+metaList.getName();
                 }
                 @Override
                 public int size() {
                     return metaList.size();
                 }
                 public Item get(int position) {
                     final MetaItem item = metaList.get(position);
                     return new Item(){
                         @Override
                         public Object get() {
                             return new MappedArray(item.getReturnType()){
                                 @Override
                                 protected Object mappedValueAt(int position) {
                                    return item.get(MappedArray.this.mappedValueAt(position));
                                 }
                                 @Override
                                 protected int numValues() {
                                     return MappedArray.this.numValues();
                                 }
                                 @Override
                                 protected Object originalValueAt(int pos) {
                                     return MappedArray.this.originalValueAt(pos);
                                 }
                             };
                         }
                         @Override
                         public CharSequence getName() {
                             return item.getName();
                         }
                         @Override
                         public Class<?> getReturnType() {
                             return MappedArray.class;
                         }
                     };
                 }
             };
         }
 
         protected abstract Object mappedValueAt(int pos);
         protected abstract Object originalValueAt(int pos);
         protected abstract int numValues();
         @Override
         public String toString() {
             StringBuffer buffer = new StringBuffer();
             int num = numValues();
             int length = Math.min(num,MAX_ARRAY_ELEMENTS);
             for (int i = 0;i<length;i++){
                 if (buffer.length() != 0)
                     buffer.append("<br/>");
                 buffer.append(ItemFactory.toString(originalValueAt(i)))
                     .append(" → ")
                     .append(ItemFactory.toString(mappedValueAt(i)));
             }
             int moreValues = num - length;
             if (moreValues > 0)
                 buffer.append("\n... <i>(+ ")
                     .append(moreValues)
                     .append(" more")
                     .append(")</i>");
             return buffer.toString();
         }
         public Object[] getArray(){
             int length = numValues();
             Object []res = new Object[length];
             for (int i=0;i<length;i++)
                 res[i] = mappedValueAt(i);
             return res;
         }
         public Map<Object,Object> getMap(){
             HashMap<Object, Object> res = new HashMap<Object, Object>();
 
             int length = numValues();
 
             for (int i=0;i<length;i++)
                 res.put(originalValueAt(i),mappedValueAt(i));
             return res;
         }
     }
     private static ItemLists mappedArray(final Object o){
         if (o instanceof Object[]){
             final Object[] array = (Object[]) o;
             return new MappedArray(array.getClass().getComponentType()){
                 @Override
                 protected Object mappedValueAt(int pos) {
                     return array[pos];
                 }
                 @Override
                 protected int numValues() {
                     return array.length;
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return array[pos];
                 }
             };
         }
         else
             return new MappedArray(o.getClass().getComponentType()){
                 @Override
                 protected Object mappedValueAt(int pos) {
                     return Array.get(o, pos);
                 }
                 @Override
                 protected int numValues() {
                     return Array.getLength(o);
                 }
                 @Override
                 protected Object originalValueAt(int pos) {
                     return Array.get(o, pos);
                 }
             };
     }
     private static void add(ArrayList<ItemList> list,ItemList il){
         if (il.size() > 0)
             list.add(il);
     }
     public static ArrayList<ItemList> itemsFor(Object o){
         ArrayList<ItemList> res = new ArrayList<ItemList>();
 
         add(res,informationFor(o));
 
         if (o.getClass().isArray()){
             add(res,elementsOfArray(o));
             add(res,fromArray("Actions", single("Mapped array",mappedArray(o))));
         }
         else if (o instanceof Map)
             add(res,elementsOfMap((Map<?, ?>) o));
         else if (o instanceof File && ((File) o).isDirectory())
             add(res,contentsOfDirectory((File) o));
         else if (o instanceof Iterable)
             add(res,elementsOfIterable((Iterable<?>) o));
         else if (o instanceof PackageManager)
             add(res,packagesFromPM((PackageManager) o));
         else if (o instanceof File && ((File)o).getName().endsWith(".db"))
             add(res,tablesOfDb(((File) o).getPath()));
         else if (o instanceof Cursor)
             add(res,resultsOfQuery((Cursor) o));
 
         if (o instanceof ItemList)
             add(res,(ItemList) o);
         if (o instanceof ItemLists){
             ItemLists lists = (ItemLists) o;
             for (int i=0;i<lists.size();i++)
                 add(res,lists.get(i));
         }
 
         for (MetaItemList meta:MetaItemFactory.metaItemsFor(o.getClass()))
             add(res,materialize(meta,o));
 
         return res;
     }
 
     private static String capitalized(String str){
         return str.substring(0,1).toUpperCase()+str.substring(1);
     }
     private static Method getMethod(Class<?> clazz,String name){
         try {
             return clazz.getMethod(name);
         } catch (SecurityException e) {
             throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
             return null;
         }
     }
     private static Object getFieldOf(String field,Object o){
         try {
             Class<? extends Object> clazz = o.getClass();
             Method m = getMethod(clazz,"get"+capitalized(field));
             if (m == null)
                 m = getMethod(clazz,field);
 
             if (m != null)
                 return m.invoke(o);
             else{
                 Field f = clazz.getDeclaredField(field);
                 f.setAccessible(true);
                 return f.get(o);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     private static String eval(String path,Object o){
         String[] parts = path.split("\\.");
 
         for(String part:parts)
             o = getFieldOf(part,o);
         return toString(o);
     }
     private static Pattern splish = Pattern.compile("#(\\w+(?:\\.\\w+)*)");
     private static String format(String format,Object o){
         Matcher m = splish.matcher(format);
         StringBuffer res = new StringBuffer();
         while(m.find())
             m.appendReplacement(res, eval(m.group(1),o));
 
         m.appendTail(res);
         return res.toString();
     }
     private static final int MAX_ARRAY_ELEMENTS = 5;
     private static String arrayToString(Object o){
         StringBuffer buffer = new StringBuffer();
         buffer.append('[');
 
         int len = Array.getLength(o);
         for (int i=0;i<Math.min(len,MAX_ARRAY_ELEMENTS);i++){
             if (i != 0)
                 buffer.append(", ");
             buffer.append(toString(Array.get(o,i)));
         }
         int remaining = len - MAX_ARRAY_ELEMENTS;
         if (remaining > 0)
             buffer.append(", ... (<i>").append(remaining).append(" more</i>)");
 
         buffer.append(']');
         return buffer.toString();
     }
     public static String toString(Object o){
         if (o == null)
             return "null";
         else {
             Class<? extends Object> clazz = o.getClass();
             Textual text = clazz.getAnnotation(Textual.class);
             if (text != null)
                 return format(text.value(),o);
             else if (clazz.isArray())
                 return arrayToString(o);
             else
                 return String.valueOf(o);
         }
     }
 }
