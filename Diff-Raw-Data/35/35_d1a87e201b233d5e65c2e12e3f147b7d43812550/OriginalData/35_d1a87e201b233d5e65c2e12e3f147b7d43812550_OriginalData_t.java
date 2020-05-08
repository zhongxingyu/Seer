 package kdd.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 //Table 1 in the paper
 public class OriginalData {
 
     public static int ATTRIBUTE_STATUS_FLEXIBLE = 0;
     public static int ATTRIBUTE_STATUS_STABLE = 1;
     public static int ATTRIBUTE_STATUS_DECISION = 2;
 
     private static OriginalData instance = null;
 
     private Map<String, Integer> attributesMap = new HashMap<String, Integer>();
     private Map<String, Integer> nameMap = new HashMap<String, Integer>();
 
     private String[][] dataSet = new String[][] {};
 
     private String desiredValue = "";
 
     private OriginalData() {
         // Nothing here.
     }
 
     private OriginalData(String[][] dataSet) {
         this.attributesMap.clear();
         this.nameMap.clear();
         this.dataSet = dataSet;
 
         if (dataSet != null) {
             for (int i = 1; i < dataSet[0].length; i++) {
                 this.attributesMap.put(dataSet[0][i], i);
             }
             for (int i = 2; i < dataSet.length; i++) {
                 this.nameMap.put(dataSet[i][0], i);
             }
         }
     }
 
     public static OriginalData initialize(String[][] dataSet) {
         OriginalData data = new OriginalData(dataSet);
         OriginalData.instance = data;
         return data;
     }
 
     public static OriginalData getInstance() {
         return OriginalData.instance;
     }
 
     public Object[][] getTableData() {
         String[][] data = new String[this.dataSet.length - 2][];
         for (int i = 0; i < data.length; i++) {
             data[i] = this.dataSet[i + 2];
         }
         return data;
     }
 
     public String[] getTableColumnNames() {
         String[] names = new String[this.dataSet[0].length];
         for (int i = 0; i < names.length; i++) {
             if (this.dataSet[1][i] != null && !"".equals(this.dataSet[1][i])) {
                 names[i] = this.dataSet[0][i] + "(" + this.dataSet[1][i] + ")";
             }
         }
         return names;
     }
 
     public String getReductPair(String reduct) {
         for (int i = 0; i < this.dataSet.length; i++) {
             for (int j = 0; j < this.dataSet[0].length; j++) {
                 if (this.dataSet[i][j].equals(reduct)) {
                     if ("s".equals(this.dataSet[1][j])) {
                         return new String(this.dataSet[0][j] + " = " + reduct);
                     } else {
                         return new String(this.dataSet[0][j] + " to " + reduct);
                     }
                 }
             }
         }
         return null;
     }
 
     public List<String> getRecommendationRules(List<String> reduct) {
         List<String> rules = new ArrayList<String>();
         for (int i = 0; i < reduct.size(); i++) {
             String reductPair = getReductPair(reduct.get(i));
             if (reductPair != null) {
                 rules.add(reductPair);
             }
         }
         return rules;
     }
 
     public boolean isFoundStableWithReduct(String name, List<String> reduct) {
         int row = this.nameMap.get(name);
         for (int j = 0; j < reduct.size(); j++) {
             for (int i = 1; i < this.dataSet[0].length; i++) {
                 if (this.dataSet[row][i].equals(reduct.get(j))) {
                     if ("s".equals(this.dataSet[1][i])) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     public boolean isFoundWithReduct(String name, List<String> reduct) {
         int row = this.nameMap.get(name);
         for (int j = 0; j < reduct.size(); j++) {
             boolean isFound = false;
             for (int i = 1; i < this.dataSet[0].length; i++) {
                 if (this.dataSet[row][i].equals(reduct.get(j))) {
                     if ("s".equals(this.dataSet[row][i])) {
                         return true;
                     }
                     isFound = true;
                     break;
                 }
             }
 
             if (!isFound) {
                 return false;
             }
         }
         return true;
     }
 
     public String getValue(String name, String attribute) {
         int iName = this.nameMap.get(name);
         int iAttribute = this.attributesMap.get(attribute);
         String str = this.dataSet[iName][iAttribute].trim();
         if ("".equals(str)) {
             return null;
         } else {
             return str;
         }
     }
 
     public DiscernableData caLculateDiscernableData() {
         List<String> desiredList = new ArrayList<String>();
         List<String> unDesiredList = new ArrayList<String>();
 
         int length = this.dataSet[0].length;
         for (int i = 2; i < this.dataSet.length; i++) {
             if (this.desiredValue.equals(this.dataSet[i][length - 1])) {
                 desiredList.add(this.dataSet[i][0]);
             } else {
                 unDesiredList.add(this.dataSet[i][0]);
             }
         }
 
         List<String>[][] discernableData = new ArrayList[unDesiredList.size()][desiredList.size()];
 
         for (int i = 0; i < unDesiredList.size(); i++) {
             for (int j = 0; j < desiredList.size(); j++) {
                 for (int k = 1; k < this.dataSet[0].length - 1; k++) {
                     String str1 = getValue(desiredList.get(j), this.dataSet[0][k]);
                     String str2 = getValue(unDesiredList.get(i), this.dataSet[0][k]);
 
                    if (str1 == null) {
                         if (discernableData[i][j] == null) {
                             discernableData[i][j] = new ArrayList<String>();
                            discernableData[i][j].add(str1);
                        }
                    } else if (!str1.equals(str2)) {
                        if (discernableData[i][j] == null || discernableData[i][j].get(0) == null) {
                            discernableData[i][j] = new ArrayList<String>();
                         }
                         discernableData[i][j].add(str1);
                     }
                 }
             }
         }
 
         return DiscernableData.initialize(desiredList.toArray(new String[desiredList.size()]),
                 unDesiredList.toArray(new String[unDesiredList.size()]), discernableData);
     }
 
     public String getDesiredValue() {
         return this.desiredValue;
     }
 
     public void setDesiredValue(String desiredValue) {
         this.desiredValue = desiredValue;
     }
 }
