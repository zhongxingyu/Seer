 package com.undeadscythes.genebase.exception;
 
 /**
 * Thrown when no {@link com.undeadscythes.genebase.gedcom.RecordType} can be
 * found that matches a given string.
  *
  * @author UndeadScythes
  */
 public class NoValidTypeException extends RuntimeException {
     private static final long serialVersionUID = 1L;
 
     /**
      * Pass the name of the type that was not found.
      */
     public NoValidTypeException(final String type) {
         super("No valid type with name " + type + ".");
     }
 }
