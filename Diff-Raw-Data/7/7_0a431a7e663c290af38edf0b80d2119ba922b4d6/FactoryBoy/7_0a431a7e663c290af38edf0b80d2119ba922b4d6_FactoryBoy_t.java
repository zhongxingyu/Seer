 package org.factoryboy.core;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * 主工厂基类.
  *
  * @param <T>
  */
 public abstract class FactoryBoy<T> {
 
     protected List<Mold<T>> _moldList = new ArrayList<Mold<T>>();
 
     protected List<T> _preBuildList = new ArrayList<T>();
 
     public abstract T defaultObject();
 
     protected T _object;
     private Integer sequence;
 
     private T newObject() {
         this.sequence = nextSequence();
        System.out.println("this.sequence = " + this.sequence);
        this._object = defaultObject();
         return _object;
     }
 
     public synchronized Integer currentSequenct() {
         if (this.sequence == null) {
             this.sequence = nextSequence();
         }
         return this.sequence;
     }
 
     public synchronized Integer nextSequence() {
         if (sequence == null) {
             sequence = 0;
         }
        return ++sequence;
     }
 
     private T _build() {
         T t = newObject();
         for (Mold<T> mold : _moldList) {
             mold.build(t);
         }
         return t;
     }
 
     /**
      * 生成单个对象.
      */
     public T build() {
         T t = _build();
         _preBuildList.clear();
         return t;
     }
 
     public void popBuild(int number) {
         System.out.println("_preBuildList(" + _preBuildList.size() + ") old = " + _preBuildList);
         for (int i = 0; i < number; i++) {
             _preBuildList.add(_build());
         }
         System.out.println("_preBuildList(" + _preBuildList.size() + ") = " + _preBuildList);
     }
 
     public void popBuild() {
         popBuild(1);
     }
 
     public List<T> getList() {
         List<T> list = new ArrayList<T>();
         list.addAll(_preBuildList);
         _preBuildList.clear();
         return list;
     }
 
     public List<T> build(int number) {
         List<T> list = new ArrayList<T>();
         list.addAll(_preBuildList);
         for (int i = 0; i < number; i++) {
             list.add(_build());
         }
         _preBuildList.clear();
         return list;
     }
 
     public T create() {
         T t = build();
         // TODO: 执行数据库插入操作
         return t;
     }
 
     public List<T> create(int number) {
         List<T> list = new ArrayList<T>();
         for (int i = 0; i < number; i++) {
             list.add(create());
         }
         return list;
     }
 
     /**
      * 返回最后一个值对象.
      *
      * @return
      */
     public T last() {
         return null; //TODO
     }
 
     public T lastOrBuild() {
         return null; //TODO
     }
 
     public Set<T> buildSet(int number) {
         Set<T> set = new HashSet<T>();
         set.addAll(_preBuildList);
         for (int i = 0; i < number; i++) {
             set.add(_build());
         }
         _preBuildList.clear();
         return set;
     }
 
     /**
      * 安装模具.
      */
     public <MF extends FactoryBoy<T>> MF add(MF modelFactory, Mold<T> mold) {
         modelFactory._moldList.add(mold);
         return modelFactory;
     }
 
     // ------------------ 动态设置属性 ----------------------
     public <MF extends FactoryBoy<T>, V> MF set(final String property, final SequenceValue<V> sequenceValue) {
         return (MF) add(this, new Mold<T>() {
             @Override
             public void build(T t) {
                 setObjectProperty(t, property, withValue(sequenceValue));
             }
         });
     }
 
     public <MF extends FactoryBoy<T>, V> MF set(final String property, final V value) {
         return (MF) add(this, new Mold<T>() {
             @Override
             public void build(T t) {
                 setObjectProperty(t, property, value);
             }
         });
     }
 
     // ------------------ 生成动态值的static import method ---------------------------------
     public static SequenceValue<String> format(final String format) {
         return new SequenceValue<String>() {
             @Override
             public String get() {
                 return String.format(format, getFactoryBoy().currentSequenct());
             }
         };
     }
 
     public static SequenceValue<Integer> increaseFrom(final Integer baseValue) {
         return new SequenceValue<Integer>() {
             @Override
             public Integer get() {
                 return baseValue + getFactoryBoy().currentSequenct();
             }
         };
     }
 
     public static SequenceValue<Integer> decreaseFrom(final Integer baseValue) {
         return new SequenceValue<Integer>() {
             @Override
             public Integer get() {
                 return baseValue - getFactoryBoy().currentSequenct();
             }
         };
     }
 
 
     public static SequenceValue<Long> increaseFrom(final Long baseValue) {
         return new SequenceValue<Long>() {
             @Override
             public Long get() {
                 return baseValue + getFactoryBoy().currentSequenct();
             }
         };
     }
 
     public static SequenceValue<Long> decreaseFrom(final Long baseValue) {
         return new SequenceValue<Long>() {
             @Override
             public Long get() {
                 return baseValue - getFactoryBoy().currentSequenct();
             }
         };
     }
 
     public static SequenceValue<BigDecimal> increaseFrom(final BigDecimal baseValue) {
         return new SequenceValue<BigDecimal>() {
             @Override
             public BigDecimal get() {
                 return baseValue.add(new BigDecimal(getFactoryBoy().currentSequenct()));
             }
         };
     }
 
     public static SequenceValue<BigDecimal> decreaseFrom(final BigDecimal baseValue) {
         return new SequenceValue<BigDecimal>() {
             @Override
             public BigDecimal get() {
                 return baseValue.subtract(new BigDecimal(getFactoryBoy().currentSequenct()));
             }
         };
     }
 
     public <V extends SequenceValue<?>> V withThis(V seqValue) {
         seqValue.setFactoryBoy(this);
         return seqValue;
     }
 
 
     public <SV extends SequenceValue<V>, V> V withValue(SV seqValue) {
         seqValue.setFactoryBoy(this);
         return seqValue.get();
     }
 
 
     // 考虑把所有static方法放一个单独类，包括DateHelper
     // 使用 Mockito.doThrow(new Exception()).when(instance).methodName(); 看上去也不能级联，不优雅
 
     public static String contentFrom(String fileName) {
         // TODO
         return null;
     }
 
     /**
      * java反射bean的set方法
      *
      * @param objectClass
      * @param fieldName
      * @return
      */
     @SuppressWarnings("unchecked")
     public static Method getSetMethod(Class objectClass, String fieldName) {
         try {
             Class[] parameterTypes = new Class[1];
             Field field = objectClass.getDeclaredField(fieldName);
             parameterTypes[0] = field.getType();
             StringBuilder sb = new StringBuilder();
             sb.append("set");
             sb.append(fieldName.substring(0, 1).toUpperCase());
             sb.append(fieldName.substring(1));
             return objectClass.getMethod(sb.toString(), parameterTypes);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * 执行set方法
      *
      * @param o         执行对象
      * @param fieldName 属性
      * @param value     值
      */
     public static void setObjectProperty(Object o, String fieldName, Object value) {
         Method method = getSetMethod(o.getClass(), fieldName);
         try {
             method.invoke(o, new Object[]{value});
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 }
