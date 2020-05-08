 package org.sonar.ide.api;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Evgeny Mandrikov
  * @since 0.2
  */
 public final class SourceCodeDiff {
 
  public static final int NOT_FOUND = -1;

   private Map<Integer, Integer> diff = new HashMap<Integer, Integer>();
 
   public SourceCodeDiff() {
   }
 
  /**
   * @param oldLine line in Sonar server (starting from 1)
   * @param newLine line in working copy (starting from 0), -1 if not found
   */
   public void map(int oldLine, int newLine) {
    if (newLine != NOT_FOUND) {
      diff.put(oldLine, newLine);
    }
   }
 
   /**
   * @param oldLine line in Sonar server (starting from 1)
   * @return line in working copy (starting from 0), -1 if not found
    */
   public Integer newLine(int oldLine) {
     if (diff.containsKey(oldLine)) {
       return diff.get(oldLine);
     }
    return NOT_FOUND;
   }
 
   @Override
   public String toString() {
     return diff.toString();
   }
 }
