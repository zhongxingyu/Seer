 package com.pinyin.spliter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * provide Chinese pinyin dictionary<p>
  *  
  */
 public class PinyinDicUtil
 {
     private static final String dicFileName = "./pinyin.data";    
     
     private static Map<String, Set<String>> pinyinDic = new HashMap<String, Set<String>>();;
     
     static
     {
         URL url = PinyinDicUtil.class.getClassLoader().getResource(dicFileName);
         String dicFilePath = url.getPath();
 
         File dicFile = new File(dicFilePath);
         
         BufferedReader br = null;
         
         try
         {
             br = new BufferedReader(new InputStreamReader(new FileInputStream(dicFile), "UTF-8"));
             String line = null;
             while((line = br.readLine()) != null)
             {
                 String [] fieldValues = line.split(" ");
                 if(fieldValues.length == 2)
                 {
                     String pinyin = fieldValues[0];
                     String chineseChar = fieldValues[1];
                  
                     Set<String> chineseCharSet = pinyinDic.get(pinyin);
                     
                     if(chineseCharSet == null)
                     {
                         chineseCharSet = new HashSet<String>();
                         pinyinDic.put(pinyin, chineseCharSet);
                     }
                 
                     chineseCharSet.add(chineseChar);
                 }
                 
             }
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         finally
         {
             if(br != null)
             {
                 try
                 {
                     br.close();
                 }
                 catch (IOException e)
                 {                    
                 }
             }
         }
     }
 
     public static boolean isPinyin(String pinyin)
     {  
         boolean isPinyin = false;
         
         if(pinyinDic != null)
         {
             isPinyin = pinyinDic.containsKey(pinyin);
         }
         
         return isPinyin;
     }
     
     
     public static Set<String> getChineseCharSet(String pinyin)
     {
         Set<String> chineseCharSet = new HashSet<String>();       
        
         return chineseCharSet;
     }
 }
