 package team.splunk.csc480.researcher;
 
 import team.splunk.csc480.data.DataItem;
 import team.splunk.csc480.handler.ThreatHandler.*;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
// Singlton may not be thread safe?
 public class KnowledgeBase {
    private static KnowledgeBase instance;
    private static ArrayList<String> knowledge;
 
    private KnowledgeBase() {
      knowledge = new ArrayList<String>();
    }
 
    public static KnowledgeBase getKnowledgeBase() {
      if (instance == null) {
        instance = new KnowledgeBase();
      }
      return instance;
    }
 
    /**
     *  Add a new piece of knowledge to the knowledge base.
     */
     public synchronized void learn(String newKnowledge) {
        if (!knowledge.contains(newKnowledge)) {
           knowledge.add(newKnowledge);
        }
     }
 
    /**
     *  Determine if an existing piece of a knowledge matches the rule specified.
     *
     *  @param regex the rule to match.
     */
     public boolean remember(String regex) {
        Pattern attack = Pattern.compile(regex);
        Matcher itemMatch;
 
        for (String currentItem : knowledge) {
           itemMatch = attack.matcher(currentItem);
           String match = itemMatch.find() ? itemMatch.group(0) : "";
           if (!match.equals(""))
              return true;
        }
        return false;
     }
 }
