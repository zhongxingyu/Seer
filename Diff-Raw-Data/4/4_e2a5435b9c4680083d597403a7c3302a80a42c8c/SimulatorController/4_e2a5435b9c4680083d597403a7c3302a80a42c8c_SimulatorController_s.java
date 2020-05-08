 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package statemachinedesigner;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author Henry
  */
 public class SimulatorController {
 
     SimulatorController(SimulatorFrame aThis) {
         _view = aThis;
         _seenStates = new HashMap<String, String>();
         _finalStates = new HashMap<String, String>();
         _unexploredStates = new LinkedList<String>();
         _inputSet = new LinkedList<String>();
 
         _input = "";
     }
 
     public String startSimulation(String text) {
         String designInput = text.replaceAll("\\n", " ").toUpperCase().trim() + " ";//new lines essentially denote spaces, add space at end by convention
         String[] tokens = designInput.split("\\s");
         for (int i = 0; i < tokens.length; i++) {
             if (tokens[i].matches("[pP]{1}[\\d]+[']?")) {
 //                System.out.println(text + " contains a promoter site: " + tokens[i]);
             } else if (tokens[i].matches("[iI]{1}[\\d]+[']?")) {
 //                System.out.println(text + " contains a invertase recognition site: " + tokens[i]);
             } else if (tokens[i].matches("[@]{1}[\\d]+[']?")) {
 //                System.out.println(text + " contains a invertase coding region: " + tokens[i]);
             } else if (tokens[i].matches("[xX]{1}[']?")) {
 //                System.out.println(text + " contains a terminator region: " + tokens[i]);
             } else if (tokens[i].matches("[rR]{1}[\\d]+[']?")) {
 //                System.out.println(text + " contains a reporter coding region: " + tokens[i]);
             } else {
 //                System.out.println("construct contains invalid component: " + tokens[i]);
                 return "construct contains invalid component: " + tokens[i];
             }
         }
         _inputSet = new LinkedList<String>();
         _seenStates = new HashMap<String, String>();
         _finalStates = new HashMap<String, String>();
         _unexploredStates = new LinkedList<String>();
         _currentState = designInput;
         _unexploredStates.offer(designInput);
         _seenStates.put("", designInput);
         _input = "";
         _inputSet.offer("");
         runSimulation();
         return "running simulation";
 //        return null;
     }
 
     private void runSimulation() {
 
         while (!_unexploredStates.isEmpty()) {
             _currentState = _unexploredStates.poll();
             _input = _inputSet.poll();
             System.out.println("current state: " + _currentState);
             ArrayList<Integer> activePromoters = findActivePromoters(_currentState);
             System.out.print("Active promoters: ");
             for (Integer i : activePromoters) {
                 System.out.print(i + " ");
 
             }
             System.out.println();
             for (Integer i : activePromoters) {
                 activatePromoter(i);
             }
         }
         System.out.println("===========RESULTS==========");
         Collection coll = _finalStates.keySet();
         LinkedList listy = new LinkedList<String>();
         listy.addAll(coll);
         java.util.Collections.sort(listy);
         for (Object s : listy) {
            System.out.println((String) _finalStates.get(s));
             System.out.println((String) s);
         }
     }
 
     private ArrayList<Integer> findActivePromoters(String s) {
         String designString = s.replaceAll("\\n", " ").trim();//new lines essentially denote spaces
         String[] tokens = designString.split("\\s");
         ArrayList<Integer> toReturn = new ArrayList<Integer>();
         for (int i = 0; i < tokens.length; i++) {
             if (tokens[i].matches("[pP]{1}[\\d]+")) {
                 try {
                     toReturn.add(Integer.parseInt(tokens[i].substring(1)));
                 } catch (NumberFormatException e) {
                     System.out.println("error parsing for valid promoter sites");
                     e.printStackTrace();
                 }
             }
         }
         return toReturn;
     }
 
     private void activatePromoter(int promoterNumber) {
         _promoterNumber = promoterNumber;
 
 //        String numberString = Integer.toString(promoterNumber);
 //        String temp = "";
 //        for (int i = 0; i < numberString.length(); i++) {
 //            temp = temp + "[" + numberString.substring(i, i + 1) + "]{1}";
 //        }
 
 
         if (_input.indexOf(Integer.toString(promoterNumber)) < 0) { //don't allow repeat use of promoters
             System.out.println("Activating promoter: " + promoterNumber);
             ArrayList<Integer> activatedInvertase = new ArrayList<Integer>();
             ArrayList<Integer> starts = new ArrayList<Integer>();
             ArrayList<Integer> ends = new ArrayList<Integer>();
             int start = _currentState.indexOf("P" + promoterNumber + " ") + ("p" + promoterNumber + 1).length();
             int end = _currentState.indexOf("X ", start);
             while (start > -1) {
                 starts.add(start);
                 if (end < 0) {
                     end = _currentState.length();
                 }
                 ends.add(end);
 //                System.out.println("start: "+start+" stop: "+end);
                 start = _currentState.indexOf("P" + promoterNumber + " ", start + 1);
                 end = _currentState.indexOf("X ", start);
             }
             if (starts.size() > 0 && ends.size() > 0) {
                 for (int i = 0; i < Math.min(starts.size(), ends.size()); i++) {
                     String regionOfInterest = _currentState.substring(starts.get(i), ends.get(i));
                     System.out.println("active region: " + regionOfInterest);
                     Pattern p = Pattern.compile("[rR]{1}[\\d]+");
                     Matcher m = p.matcher(regionOfInterest);
                     while (m.find()) {
                         System.out.println("expressed reporter: " + m.group());
                         _finalStates.put(_input + " " + _promoterNumber, _currentState);
 
                     }
                     p = Pattern.compile("[@]{1}[\\d]+[^']{1}");
                     m = p.matcher(regionOfInterest);
 
                     while (m.find()) {
 //                activateInvertase(Integer.parseInt(m.group().substring(1).trim()));
                         activatedInvertase.add(Integer.parseInt(m.group().substring(1).trim()));
                     }
 
 
 
                 }
             }
 
             String newState = activateInvertase(activatedInvertase);
             System.out.println("inputs used: " + _input + " " + _promoterNumber);
             System.out.println("new state:     " + newState);
             if (!_seenStates.containsKey(_input + " " + _promoterNumber)) {
                 System.out.println("added new state");
                 _seenStates.put(_input + " " + _promoterNumber, newState);
                 _unexploredStates.add(newState);
                 _inputSet.add(_input + " " + _promoterNumber);
 
 
             }
         }
 
     }
 
     private String activateInvertase(ArrayList<Integer> activeSites) {
         String newState = _currentState;
         for (int invertaseNumber : activeSites) {
             System.out.println("Activating invertase: " + invertaseNumber);
             String numberString = Integer.toString(invertaseNumber);
             String temp = "";
             for (int i = 0; i < numberString.length(); i++) {
                 temp = temp + "[" + numberString.substring(i, i + 1) + "]{1}";
             }
             Pattern p = Pattern.compile("[iI]{1}" + temp + "[\\s]{1}");
             Matcher m = p.matcher(newState);
             ArrayList<Integer> starts = new ArrayList<Integer>();
             ArrayList<Integer> ends = new ArrayList<Integer>();
             while (m.find()) {
                 starts.add(m.end());
             }
             p = Pattern.compile("[iI]{1}" + temp + "[']{1}");
             m = p.matcher(newState);
             while (m.find()) {
                 ends.add(m.start());
             }
             if (Math.min(starts.size(), ends.size()) > 0) {
                 for (int i = 0; i < Math.min(starts.size(), ends.size()); i++) {
                     String first = newState.substring(0, starts.get(i) - 1);
                     String second = newState.substring(starts.get(i) - 1, ends.get(i) - 1);
                     String last = newState.substring(ends.get(i) - 1);
                     temp = "";
                     String[] tokens = second.split("\\s");
                     for (int j = 0; j < tokens.length; j++) {
                         if (tokens[j].matches("[^']+[']{1}")) {
                             temp = tokens[j].substring(0, tokens[j].length() - 1) + " " + temp;
                         } else if (tokens[j].matches("[^']+")) {
                             temp = tokens[j] + "'" + " " + temp;
                         }
                     }
                     if (first.endsWith(" ")) {
                         if (temp.startsWith(" ")) {
                             temp = temp.substring(1);
                         }
                     } else {
                         if (!temp.startsWith(" ")) {
                             temp = " " + temp;
                         }
                     }
                     if (last.startsWith(" ")) {
                         if (temp.endsWith(" ")) {
                             last = last.substring(1);
                         }
                     } else {
                         if (!temp.endsWith(" ")) {
                             temp = temp + " ";
                         }
                     }
                     newState = first + temp + last;
                 }
             }
         }
         return newState;
 
     }
 
     private void activateInvertase(int invertaseNumber) {
         System.out.println("Activating invertase: " + invertaseNumber);
         String numberString = Integer.toString(invertaseNumber);
         String temp = "";
         for (int i = 0; i < numberString.length(); i++) {
             temp = temp + "[" + numberString.substring(i, i + 1) + "]{1}";
         }
         Pattern p = Pattern.compile("[iI]{1}" + temp + "[\\s]{1}");
         Matcher m = p.matcher(_currentState);
         ArrayList<Integer> starts = new ArrayList<Integer>();
         ArrayList<Integer> ends = new ArrayList<Integer>();
         while (m.find()) {
 //            System.out.println("found start: " + m.group());
             starts.add(m.end());
 
         }
 //        p = Pattern.compile("[iI]{1}[\\d]+[']{1}");
 
         p = Pattern.compile("[iI]{1}" + temp + "[']{1}");
         m = p.matcher(_currentState);
         while (m.find()) {
 //            System.out.println("found end: " + m.group());
             ends.add(m.start());
 
         }
         if (Math.min(starts.size(), ends.size()) > 0) {
             for (int i = 0; i < Math.min(starts.size(), ends.size()); i++) {
 //                System.out.println("start:" + starts.get(i) + " end " + ends.get(i));
                 String first = _currentState.substring(0, starts.get(i) - 1);
                 String second = _currentState.substring(starts.get(i) - 1, ends.get(i) - 1);
                 String last = _currentState.substring(ends.get(i) - 1);
 //                System.out.println("I'm going to flip:" + second);
                 temp = "";
                 String[] tokens = second.split("\\s");
                 for (int j = 0; j < tokens.length; j++) {
                     if (tokens[j].matches("[^']+[']{1}")) {
                         temp = tokens[j].substring(0, tokens[j].length() - 1) + " " + temp;
                     } else if (tokens[j].matches("[^']+")) {
                         temp = tokens[j] + "'" + " " + temp;
 
                     }
                 }
 //                System.out.println("After flipping:" + temp);
                 if (first.endsWith(" ")) {
                     if (temp.startsWith(" ")) {
                         temp = temp.substring(1);
                     }
                 } else {
                     if (!temp.startsWith(" ")) {
                         temp = " " + temp;
                     }
                 }
                 if (last.startsWith(" ")) {
                     if (temp.endsWith(" ")) {
                         last = last.substring(1);
                     }
                 } else {
                     if (!temp.endsWith(" ")) {
                         temp = temp + " ";
                     }
                 }
                 String newState = first + temp + last;
                 System.out.println("inputs used: " + _input);
                 System.out.println("new state:     " + newState);
                 if (!_seenStates.containsKey(_input + " " + _promoterNumber)) {//inputs must be unique
                     _seenStates.put(_input + " " + _promoterNumber, newState);
                     _unexploredStates.add(newState);
                     _inputSet.add(_input + " " + _promoterNumber);
 
                 }
             }
         }
 
 
     }
     private SimulatorFrame _view;
     private HashMap<String, String> _seenStates;
     private HashMap<String, String> _finalStates;
     private LinkedList<String> _unexploredStates;
     private String _currentState;
     private String _input;
     private LinkedList<String> _inputSet;
     private int _promoterNumber;
 }
