 package org.automation.dojo;
 
 import org.automation.dojo.web.bugs.Bug;
 import org.automation.dojo.web.scenario.BasicScenario;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * @author serhiy.zelenin
  */
 public class ShopBugsQueue implements BugsQueue {
     public Bug nextBugFor(BasicScenario scenario) {    // TODO test this
         ArrayList<Bug> possibleBugs = new ArrayList<Bug>(scenario.getPossibleBugs());
         possibleBugs.add(Bug.NULL_BUG);
         int bugIndex = new Random().nextInt(possibleBugs.size());
        return possibleBugs.get(bugIndex);
     }
 }
