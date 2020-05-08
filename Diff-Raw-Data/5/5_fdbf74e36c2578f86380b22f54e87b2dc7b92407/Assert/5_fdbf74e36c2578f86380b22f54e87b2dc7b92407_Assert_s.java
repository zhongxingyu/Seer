 package com.willfaught;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 public class Assert
 {
     private static String suite;
 
     private static String test;
     private static ArrayList<String> sections = new ArrayList<String>();
     private static int failures;
     private static int assertions;
     private static boolean verbose;
 
     public static void assertEqual(String name, Object first, Object second)
     {
         String s = name + ": equal: first=\"" + string(first) + "\", second=\"" + string(second) + "\"";
         if (equal(first, second))
         {
             pass(s);
         }
         else
         {
             fail(s);
         }
     }
 
     public static void assertExpected(String name, Object expected, Object actual)
     {
         String s = name + ": expected=\"" + string(expected) + "\", actual=\"" + string(actual) + "\"";
         if (equal(expected, actual))
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
        String s = name + ": expected=\"false\", actual=\"true\"";
         if (condition)
         {
             fail(s);
         }
         else
         {
             pass(s);
         }
     }
 
     public static int assertions()
     {
         return assertions;
     }
 
     public static void assertNotEqual(String name, Object first, Object second)
     {
         String s = name + ": not equal: first=\"" + string(first) + "\", second=\"" + string(second) + "\"";
         if (equal(first, second))
         {
             fail(s);
         }
         else
         {
             pass(s);
         }
     }
 
     public static void assertNotExpected(String name, Object notExpected, Object actual)
     {
         String s = name + ": not expected=\"" + string(notExpected) + "\", actual=\"" + string(actual) + "\"";
         if (equal(notExpected, actual))
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
        String s = name + ": expected=\"true\", actual=\"false\"";
         if (condition)
         {
             pass(s);
         }
         else
         {
             fail(s);
         }
     }
 
     public static void begin(String name)
     {
         if (name == null)
         {
             throw new IllegalArgumentException();
         }
         sections.add(name);
         if (verbose)
         {
             log("Section: " + prefix());
         }
     }
 
     public static void end()
     {
         if (sections.isEmpty())
         {
             log("Error: " + prefix() + ": extra section end");
         }
         else
         {
             sections.remove(sections.size() - 1);
         }
     }
 
     private static boolean equal(Object expected, Object actual)
     {
         if (expected == null)
         {
             if (actual == null)
             {
                 return true;
             }
             return actual.equals(expected);
         }
         if (expected instanceof Object[])
         {
             if (actual instanceof Object[])
             {
                 return Arrays.equals((Object[])expected, (Object[])actual);
             }
             return false;
         }
         if (expected instanceof int[])
         {
             if (actual instanceof int[])
             {
                 return Arrays.equals((int[])expected, (int[])actual);
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
 
     public static void fail(Class<?> c)
     {
         fail("expected=" + c.getName());
     }
 
     public static void fail(Class<?> expected, Throwable actual)
     {
         fail("expected=" + expected.getName() + ", actual=" + actual);
     }
 
     public static void fail(String message)
     {
         log("Failure: " + prefix() + ": " + message);
         ++assertions;
         ++failures;
     }
 
     public static void fail(Throwable t)
     {
         fail("unexpected=" + t);
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
         StringBuilder prefix = new StringBuilder();
         prefix.append(suite + "." + test);
         Iterator<String> iterator = sections.iterator();
         while (iterator.hasNext())
         {
             prefix.append(": " + iterator.next());
         }
         return prefix.toString();
     }
 
     private static String string(Object o)
     {
         if (o == null)
         {
             return "null";
         }
         if (o instanceof Object[])
         {
             return Arrays.toString((Object[])o);
         }
         if (o instanceof int[])
         {
             return Arrays.toString((int[])o);
         }
         return o.toString();
     }
 
     public static void suite(String name)
     {
         if (name == null)
         {
             throw new IllegalArgumentException();
         }
         suite = name;
         if (verbose)
         {
             log("Suite: " + name);
         }
     }
 
     public static void test(String name)
     {
         if (name == null)
         {
             throw new IllegalArgumentException();
         }
         sections.clear();
         test = name;
         if (verbose)
         {
             log("Test: " + prefix());
         }
     }
 
     public static void verbose(boolean verbose)
     {
         Assert.verbose = verbose;
     }
 
     private Assert()
     {
     }
 }
