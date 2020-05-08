 package com.undeadscythes.gedcomstd;
 
 import java.util.*;
 
 /**
 * The type of relationship between two {@link com.undeadscythes.gedcomstd.record.Individual}s.
  *
  * @author UndeadScythes
  */
 public enum Relation {
     MOTHER,
     FATHER,
     BROTHER,
     SISTER,
     NONE;
 
     /**
      * {@link #MOTHER MOTHER}, {@link #FATHER FATHER}.
      */
     public static final List<Relation> PARENT = new ArrayList<Relation>(Arrays.asList(MOTHER, FATHER));
 
     /**
      * {@link #BROTHER BROTHER}, {@link #SISTER SISTER}.
      */
     public static final List<Relation> SIBLING = new ArrayList<Relation>(Arrays.asList(BROTHER, SISTER));
 }
