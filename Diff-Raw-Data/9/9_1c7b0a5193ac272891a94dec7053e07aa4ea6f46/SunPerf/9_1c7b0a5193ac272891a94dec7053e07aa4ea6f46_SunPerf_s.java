 package ru.smartislav.microclock.clocklike;
 
 import java.lang.reflect.Method;
 
 public final class SunPerf {
   public static final ClockLike instance = obtainInstance();
 
   private static ClockLike obtainInstance() {
     try {
       Class<?> klass = Class.forName("ru.smartislav.microclock.clocklike.SunPerfProvider");
      Method getMethod = klass.getMethod("obtainInstance");
       return (ClockLike)getMethod.invoke(null);
     } catch (Throwable ignored) {
     }
 
     return null;
   }
 }
