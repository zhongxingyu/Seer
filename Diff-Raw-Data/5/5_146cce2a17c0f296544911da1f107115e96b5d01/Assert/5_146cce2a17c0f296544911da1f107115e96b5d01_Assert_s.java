 package com.willfaught;
 
 import java.util.Iterator;
 
 public class Assert
 {
     private Assert()
     {
     }
     
     private static String suite;
     private static String test;
     private static Stack<String> sections = new ArrayList<String>();
     private static int failures;
     private static int assertions;
     private static boolean verbose;
     
     public static int assertions()
     {
         return assertions;
     }
     
     public static void assertExpected(String name, Object expected, Object actual)
     {
         String s = name + ": expected=\"" + string(expected) + "\", actual=\"" + string(actual) + "\"";
         if (equals(expected, actual))
         {
             pass(s);
         }
         else
         {
             fail(s);
         }
     }
     
     public static void assertNotExpected(String name, Object notExpected, Object actual)
     {
         String s = name + ": not expected=\"" + string(notExpected) + "\", actual=\"" + string(actual) + "\"";
         if (equals(notExpected, actual))
         {
             fail(s);
         }
         else
         {
             pass(s);
         }
     }
     
     public static void assertEqual(String name, Object first, Object second)
     {
        String s = name + ": not equal: first=\"" + string(first) + "\", second=\"" + string(second) + "\"";
         if (equals(first, second))
         {
             pass(s);
         }
         else
         {
             fail(s);
         }
     }
     
     public static void assertNotEqual(String name, Object first, Object second)
     {
        String s = name + ": equal: first=\"" + string(first) + "\", second=\"" + string(second) + "\"";
         if (equals(first, second))
         {
             fail(s);
         }
         else
         {
             pass(s);
         }
     }
     
     public static void assertTrue(String name, boolean condition)
     {
         String s = name + ": expected=true, actual=false";
         if (condition)
         {
             pass(s);
         }
         else
         {
             fail(s);
         }
     }
     
     public static void assertFalse(String name, boolean condition)
     {
         String s = name + ": expected=false, actual=true";
         if (condition)
         {
             fail(s);
         }
         else
         {
             pass(s);
         }
     }
     
     public static void begin(String name)
     {
         if (name == null)
         {
             throw new IllegalArgumentException();
         }
         sections.push(name);
     }
     
     public static void suite(String name)
     {
         if (name == null)
         {
             throw new IllegalArgumentException();
         }
         suite = name;
     }
     
     public static void end()
     {
         sections.pop();
     }
     
     private static boolean equals(Object expected, Object actual)
     {
         if (expected instanceof int[])
         {
             if (actual instanceof int[])
             {
                 return java.util.Arrays.equals((int[])expected, (int[])actual);
             }
             return false;
         }
         return expected.equals(actual);
     }
     
     public static void fail()
     {
         log("Failure: " + prefix());
         ++assertions;
         ++failures;
     }
     
     public static void fail(String message)
     {
         log("Failure: " + prefix() + ": " + message);
         ++assertions;
         ++failures;
     }
     
     public static void fail(Exception e)
     {
         fail("unexpected=" + e);
     }
     
     public static void fail(Class c)
     {
         fail("expected=" + c.getCanonicalName());
     }
     
     public static void fail(Class expected, Exception actual)
     {
         fail("expected=" + expected.getCanonicalName() + ", actual=" + actual);
     }
     
     public static int failures()
     {
         return failures;
     }
     
     public static void log(String message)
     {
         System.err.println(message);
     }
     
     private static void pass(String message)
     {
         if (verbose)
         {
             log("Success: " + prefix() + ": " + message);
         }
         ++assertions;
     }
     
     private static String prefix()
     {
         String prefix = suite + "." + test;
         Iterator<String> iterator = sections.iterator();
         while (iterator.hasNext())
         {
             prefix += ": " + iterator.next();
         }
         return prefix;
     }
     
     private static String string(Object object)
     {
         if (object instanceof int[])
         {
             return java.util.Arrays.toString((int[])object);
         }
         return object.toString();
     }
     
     public static void test(String name)
     {
         if (name == null)
         {
             throw new IllegalArgumentException();
         }
         test = name;
         if (!sections.empty())
         {
             throw new IllegalStateException();
         }
     }
     
     public static void verbose(boolean verbose)
     {
         Assert.verbose = verbose;
     }
 }
