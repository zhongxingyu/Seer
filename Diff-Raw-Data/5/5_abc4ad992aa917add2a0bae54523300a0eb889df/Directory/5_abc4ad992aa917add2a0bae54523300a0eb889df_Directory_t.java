 /*
  * Copyright (C) 2009-2010 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.util.Map;
 
 /**
  *
  * @author mcculley
  */
 public interface Directory {
 
     /**
      * Retrieves a Map of Employees with employee numbers as the keys in the form of Integer, Long, and String.  Note that there are
      * multiple entries for each employee in the returned Map.
      *
      * @return a Map of Employees with various forms of the employee number as the key
      */
     Map<Object, Employee> getByNumber();
 
     /**
     * Retrieves a Map of Employees with usernames as the keys.
      *
      * @return a Map of Employees with the username as the key
      */
     Map<String, Employee> getByUserName();
 
     /**
     * Synchronize this directory against the backing store.
      */
     void reload();
 
 }
