 package org.babblelang.engine.impl.natives.java;
 
 import org.babblelang.engine.impl.Resolver;
 
 public class JavaObject implements Resolver {
     private final JavaClass _class;
     private final Object value;
 
     public JavaObject(JavaClass _class, Object value) {
         this._class = _class;
         this.value = value;
     }
 
     public boolean isDeclared(String key) {
         return _class.isDeclared(key);
     }
 
     public Object get(String key) {
         JavaMethod result = (JavaMethod) _class.get(key);
        return new BoundJavaMethod(result, value);
     }
 }
