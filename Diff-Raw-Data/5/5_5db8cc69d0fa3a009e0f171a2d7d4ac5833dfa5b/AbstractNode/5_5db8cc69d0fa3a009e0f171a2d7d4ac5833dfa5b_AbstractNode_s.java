 package edu.x3m.kas.core.structures;
 
 
 /**
  *
  * @author Hans
  */
public class AbstractNode implements Comparable<SimpleNode> {
 
 
     private static final int DEFAULT_COUNT = 1;
     private static final int DEFAULT_CODE_SIZE = 4;
     private static final int DEFAULT_BALANCE_INDEX = 256;
     //
     protected int count = DEFAULT_COUNT;
     protected StringBuilder code = new StringBuilder (DEFAULT_CODE_SIZE);
 
 
 
     public void append (char c) {
         code.append (c);
     }
 
 
 
     public int getCount () {
         return count * DEFAULT_BALANCE_INDEX;
     }
 
 
 
     public StringBuilder getCode () {
         return code;
     }
 
 
 
     public void inc () {
         count++;
     }
 
 
 
     @Override
    public int compareTo (SimpleNode o) {
         return getCount () - o.getCount ();
     }
 }
