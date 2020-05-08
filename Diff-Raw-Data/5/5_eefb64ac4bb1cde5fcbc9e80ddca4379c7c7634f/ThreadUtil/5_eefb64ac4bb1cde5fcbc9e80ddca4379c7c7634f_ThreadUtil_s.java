 
 package com.quartercode.quarterbukkit.api.thread;
 
 /**
  * This class is for handle the Bukkit-Thread-Errors.
  * It will manage the called method with their actions.
  */
 public class ThreadUtil {
 
     private static Thread bukkitThread;
 
     /**
      * Initalizes the {@link Thread} management.
      * This can only be called one time.
      */
     public static void initalizeThread() {
 
         if (bukkitThread != null) {
             throw new IllegalStateException("bukkitThread already initalized");
         }
 
         bukkitThread = Thread.currentThread();
     }
 
     /**
      * Returns the initalized Bukkit-Main-{@link Thread}.
      * 
      * @return The Bukkit-Main-{@link Thread}.
      */
     public static Thread getBukkitThrad() {
 
         return bukkitThread;
     }
 
     /**
      * Checks if the current {@link Thread} is the Bukkit-Main-{@link Thread}.
      * 
      * @return If the current {@link Thread} is valid for Bukkit-API-functions.
      */
     public static boolean isInBukkitThread() {
 
         return isInBukkitThread(Thread.currentThread());
     }
 
     /**
      * Checks if a {@link Thread} is the Bukkit-Main-{@link Thread}.
      * 
      * @param thread The {@link Thread} to check.
      * @return If the {@link Thread} is valid for Bukkit-API-functions.
      */
     public static boolean isInBukkitThread(final Thread thread) {
 
         if (bukkitThread == null || thread.getId() == bukkitThread.getId()) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Checks if the current {@link Thread} is valid for Bukkit-API-functions and throws an {@link IllegalThreadStateException} if not.
      */
     public static void check() {
 
         if (!isInBukkitThread()) {
             final StackTraceElement[] stackTrace = new Exception().getStackTrace();
            throw new IllegalThreadStateException("You can call " + stackTrace[stackTrace.length] + " only in the Bukkit Main-Thread");
         }
     }
 
     /**
      * Checks if a {@link Thread} is valid for Bukkit-API-functions and throws an {@link IllegalThreadStateException} if not.
      * 
      * @param thread The {@link Thread} to check.
      */
     public static void check(final Thread thread) {
 
         if (!isInBukkitThread(thread)) {
             final StackTraceElement[] stackTrace = new Exception().getStackTrace();
            throw new IllegalThreadStateException("You can call " + stackTrace[stackTrace.length] + " only in the Bukkit Main-Thread");
         }
     }
 
     private ThreadUtil() {
 
     }
 
 }
