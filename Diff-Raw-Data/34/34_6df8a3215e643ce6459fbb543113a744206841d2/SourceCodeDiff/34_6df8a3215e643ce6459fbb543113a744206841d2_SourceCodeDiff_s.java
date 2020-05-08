 package org.sonar.ide.api;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Evgeny Mandrikov
  * @since 0.2
  */
 public final class SourceCodeDiff {
 
   private Map<Integer, Integer> diff = new HashMap<Integer, Integer>();
 
   public SourceCodeDiff() {
   }
 
   public void map(int oldLine, int newLine) {
    diff.put(oldLine, newLine);
   }
 
   /**
   * @param oldLine (starting from 1)
   * @return new line (starting from 0), -1 if not found
    */
   public Integer newLine(int oldLine) {
     if (diff.containsKey(oldLine)) {
       return diff.get(oldLine);
     }
    return -1;
   }
 
   @Override
   public String toString() {
     return diff.toString();
   }
 }
