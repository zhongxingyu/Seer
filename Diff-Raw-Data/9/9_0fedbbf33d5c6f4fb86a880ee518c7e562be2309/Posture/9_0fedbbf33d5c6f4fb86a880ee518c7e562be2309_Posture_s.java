 package models;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 
 public class Posture {
 
     private int generalPosture, torsoFirst, torsoSecond, head;
     private int leftHandFirst, rightHandFirst, leftHandSecond, rightHandSecond;
     private int leftLegFirst, rightLegFirst, leftLegSecond, rightLegSecond;
     private int activity;
     public static final Map<String, Integer> domains = initDomains();
 
 
     public Posture(String fileName) throws FileNotFoundException {
 
         Scanner scanner = new Scanner(new File(fileName));
         scanner.useDelimiter(",|\\n");
 
         generalPosture = scanner.nextInt();
         scanner.next();
         torsoFirst = scanner.nextInt();
         torsoSecond = scanner.nextInt();
         scanner.next();
         head = scanner.nextInt();
         scanner.next();
         leftHandFirst = scanner.nextInt();
         rightHandFirst = scanner.nextInt();
         leftHandSecond = scanner.nextInt();
         rightHandSecond = scanner.nextInt();
         scanner.next();
         leftLegFirst = scanner.nextInt();
         rightLegFirst = scanner.nextInt();
         leftLegSecond = scanner.nextInt();
         rightLegSecond = scanner.nextInt();
         scanner.next();
 
         if (scanner.hasNextInt())
             activity = scanner.nextInt();
         else
             activity = -1;
 
         if (!checkDomains())
             throw new IllegalArgumentException("Invalid file format");
 
     }
 
 
     /**
      * Computes the total number of observable variables that can
      * be obtained from all the combinations of the classes whose names are given.
      *
      * @param postureClasses names of the posture classes
      * @return total number of observable variables
      */
     public static int computeNumObservableVariables(List<String> postureClasses) {
         int num = 1;
 
         for (String name : postureClasses) {
             if (domains.containsKey(name))
                 num *= domains.get(name);
             else
                 throw new IllegalArgumentException("Invalid Class Names");
         }
 
         return num;
     }
 
     /**
      * Compute the index of the observation represented by
      * the posture classes whose names are given.
      * <p/>
      * Practically, it maps an observation from a list of
      * posture classes to a unique index.
      *
      * @param postureClasses names of the posture classes
      * @return index of the observation
      */
     public int computeObservationIndex(List<String> postureClasses) {
         int index = 0, product;
         List<Integer> maxDomain = new ArrayList<Integer>();
         List<Integer> values = new ArrayList<Integer>();
         int n = postureClasses.size();
         Integer val;
 
 
         for (String name : postureClasses) {
             maxDomain.add(domains.get(name));
             val = unMapClassName(name);
             if (val == 0)
                 return -1;
             values.add(val);
         }
 
         for (int i = 0; i < n; i++) {
 
             product = 1;
             for (int j = i + 1; j < n; j++) {
                 product *= maxDomain.get(j);
             }
 
             index += (values.get(i) - 1) * product;
         }
 
         return index;
     }
 
     public int getActivity() {
         return activity;
     }
 
     private Integer unMapClassName(String name) {
 
         if (!domains.keySet().contains(name))
             throw new IllegalArgumentException();
 
         PostureClass postureClass = PostureClass.valueOf(name);
         switch (postureClass) {
 
             case generalPosture:
                 return generalPosture;
 
             case torsoFirst:
                 return torsoFirst;
 
             case torsoSecond:
                 return torsoSecond;
 
             case head:
                 return head;
 
             case leftHandFirst:
                 return leftHandFirst;
 
             case rightHandFirst:
                 return rightHandFirst;
 
             case leftHandSecond:
                 return leftHandSecond;
 
             case rightHandSecond:
                 return rightHandSecond;
 
             case leftLegFirst:
                 return leftLegFirst;
 
             case rightLegFirst:
                 return rightLegFirst;
 
             case leftLegSecond:
                 return leftLegSecond;
 
             case rightLegSecond:
                 return rightLegSecond;
 
             default:
                 throw new IllegalArgumentException();
         }
     }
 
     private boolean checkDomains() {
 
         if (generalPosture < 0 || torsoFirst < 0 || torsoSecond < 0 || head < 0 ||
                 leftHandFirst < 0 || rightHandFirst < 0 || leftHandSecond < 0 || rightHandSecond < 0 ||
                leftLegFirst < 0 || rightLegFirst < 0 || leftLegSecond < 0 || rightLegSecond < 0 || activity < 0)
             return false;
 
         if (generalPosture > domains.get("generalPosture"))
             return false;
         if (torsoFirst > domains.get("torsoFirst"))
             return false;
         if (torsoSecond > domains.get("torsoSecond"))
             return false;
         if (head > domains.get("head"))
             return false;
         if (leftHandFirst > domains.get("leftHandFirst"))
             return false;
         if (rightHandFirst > domains.get("rightHandFirst"))
             return false;
         if (leftHandSecond > domains.get("leftHandSecond"))
             return false;
         if (rightHandSecond > domains.get("rightHandSecond"))
             return false;
         if (leftLegFirst > domains.get("leftLegFirst"))
             return false;
         if (rightLegFirst > domains.get("rightLegFirst"))
             return false;
         if (leftLegSecond > domains.get("leftLegSecond"))
             return false;
         if (rightLegSecond > domains.get("rightLegSecond"))
             return false;
 
 
         return true;
     }
 
     private static Map<String, Integer> initDomains() {
 
         Map<String, Integer> domains = new HashMap<String, Integer>();
         domains.put("generalPosture", 3);
         domains.put("torsoFirst", 2);
         domains.put("torsoSecond", 5);
         domains.put("head", 5);
         domains.put("leftHandFirst", 5);
         domains.put("rightHandFirst", 5);
         domains.put("leftHandSecond", 2);
         domains.put("rightHandSecond", 2);
         domains.put("leftLegFirst", 4);
         domains.put("rightLegFirst", 4);
         domains.put("leftLegSecond", 2);
         domains.put("rightLegSecond", 2);
 
         return domains;
     }
 
     @Override
     public String toString() {
         return "models.Posture{" +
                 "generalPosture=" + generalPosture +
                 ", torsoFirst=" + torsoFirst +
                 ", torsoSecond=" + torsoSecond +
                 ", head=" + head +
                 ", leftHandFirst=" + leftHandFirst +
                 ", rightHandFirst=" + rightHandFirst +
                 ", leftHandSecond=" + leftHandSecond +
                 ", rightHandSecond=" + rightHandSecond +
                 ", leftLegFirst=" + leftLegFirst +
                 ", rightLegFirst=" + rightLegFirst +
                 ", leftLegSecond=" + leftLegSecond +
                 ", rightLegSecond=" + rightLegSecond +
                 ", activity=" + activity +
                 '}';
     }
 
 
 }
