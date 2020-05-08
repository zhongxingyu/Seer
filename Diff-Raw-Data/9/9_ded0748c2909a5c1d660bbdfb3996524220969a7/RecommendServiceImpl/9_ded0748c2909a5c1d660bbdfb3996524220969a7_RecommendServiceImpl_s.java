 package com.dianping.spotlight.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.io.FileUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class RecommendServiceImpl implements RecommendService {
 
     @Autowired
     private StatisticsService statisticsService;
 
     private static final String APP_PREFIX = "active";
 
     private static final int MAX_HOTKEY_SIZE = 20;
 
     @Override
     public RecommendResult recommend(List<String> lines) {
 
         //        List<String> lines = null;
         //        try {
         //            lines = FileUtils.readLines(inputSeqFile);
         //        } catch (IOException e) {
         //            e.printStackTrace();
         //            return null;
         //        }
 
         RecommendResult result = new RecommendResult();
         String appName = "";
         List<HotkeyRes> hotkeyRess = new ArrayList<HotkeyRes>();
         result.setRecommendKeys(hotkeyRess);
         int hotkeyUsedTimes = 0;
         int operateTimes = lines.size();
 
         for (String line : lines) {
             line = line.trim().toLowerCase();
             if (line.startsWith(APP_PREFIX)) {
                 int startIndex = line.lastIndexOf(".");
                 if (startIndex == line.length() - 1 || line.length() == APP_PREFIX.length()) {
                     appName = "";
                 } else if (startIndex == -1) {
                     appName = line.substring(APP_PREFIX.length() + 1).trim();
                 } else {
                     appName = line.substring(startIndex+1, line.length());
                 }
                 if (!appName.isEmpty()) {
                     break;
                 }
             }
         }
         if (appName.isEmpty()) {
         	System.out.println("app name is empty");
             return null;
         }
 
         Set<Hotkey> hotkeys = statisticsService.listHotkeys(appName); //所有的快捷键
         if (hotkeys == null || hotkeys.size() == 0) {
         	System.out.println("hotkeys is null");
             return null;
         } else if (hotkeys.size() > MAX_HOTKEY_SIZE) { //取一定数量的快捷键
             List<Hotkey> hotkeyList = new ArrayList<Hotkey>(hotkeys);
             Collections.sort(hotkeyList, new HotkeyCompartor());
             hotkeys = new HashSet<Hotkey>();
             for (int i = 0; i < MAX_HOTKEY_SIZE; i++) {
                 hotkeys.add(hotkeyList.get(i));
             }
         }
 
         Map<Hotkey, Hotkey> hotkeyToHotKeyMap = new HashMap<Hotkey, Hotkey>(); //快捷键的map
         for (Hotkey hotkey : hotkeys) {
             hotkeyToHotKeyMap.put(hotkey, hotkey);
         }
         
         Set<Hotkey> hotkeysUsed = new HashSet<Hotkey>(); //使用过的快捷键
 
         for (String line : lines) {
             line = line.trim().toLowerCase();
             Set<String> tokens = new HashSet<String>();
             for (String token : line.split(" ")) {
                 tokens.add(token);
             }
            Hotkey hotkeyUsed = new Hotkey();
            hotkeyUsed.setTokens(tokens);
             if (hotkeyToHotKeyMap.containsKey(hotkeyUsed)) {
                 HotkeyRes hotkeyRes = new HotkeyRes();
                 hotkeyRes.setHotkey(hotkeyToHotKeyMap.get(hotkeyUsed));
                 hotkeyRes.setUsed(true);
                 hotkeyRess.add(hotkeyRes);
                 hotkeysUsed.add( hotkeyToHotKeyMap.remove(hotkeyUsed));              
             }
             if (hotkeys.contains(hotkeyUsed)) {
                 hotkeyUsedTimes++;
             }
         }
         if (!hotkeyToHotKeyMap.isEmpty()) {
             for (Hotkey hotkey : hotkeyToHotKeyMap.keySet()) {
                 HotkeyRes hotkeyRes = new HotkeyRes();
                 hotkeyRes.setHotkey(hotkeyToHotKeyMap.get(hotkey));
                 hotkeyRes.setUsed(false);
                 hotkeyRess.add(hotkeyRes);
             }
         }
         Collections.sort(hotkeyRess, new HotkeyResCompartor());
 
         int score = hotkeyUsedScore(hotkeys.size(), hotkeysUsed.size(), 50);
         score = score + hotkeyPercentScore(hotkeyUsedTimes * 1.0 / operateTimes, 50, appName);
         
         result.setScore(score);
         result.setHigherThan(statisticsService.record(appName, hotkeysUsed, score));
         result.setAppName(appName);
         return result;
     }
 
     private int hotkeyUsedScore(int all, int used, int totalScore) 
     {
         return (int) 1.0 * totalScore * used / all;
     }
 
     private int hotkeyPercentScore(double percent, int totalScore, String appName) {
         double usageHigherThan = statisticsService.usageHigherThan(appName, percent);
        return (int) (totalScore * usageHigherThan);
     }
 
     class HotkeyResCompartor implements Comparator<HotkeyRes> {
         @Override
         public int compare(HotkeyRes o1, HotkeyRes o2) {
             HotkeyCompartor comparator = new HotkeyCompartor();
             return comparator.compare(o1.getHotkey(), o2.getHotkey());
         }
 
     }
 
     class HotkeyCompartor implements Comparator<Hotkey> {
         @Override
         public int compare(Hotkey o1, Hotkey o2) {
             return new Double(o2.getUsage()).compareTo(o1.getUsage());
         }
 
     }
 
     @PostConstruct
     @Override
     public void init() {
 
         statisticsService = new StatisticsServiceImpl();
         Store store = new DefaultStore();
         store.init();
         statisticsService.setStore(store);
     }
     
     public static void main(String []args) {
     	RecommendService service = new RecommendServiceImpl();
     	service.init();
     	File file = new File("/Users/marsqing/Documents/textfile.txt");
     	List<String> lines = null;
     	
     	try {
     		lines= FileUtils.readLines(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     	
     	System.out.println(service.recommend(lines));
     	try {
 			System.in.read();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     	System.exit(0);
     }
 
 }
