 package kkckkc.jsourcepad.model;
 
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.model.bundle.EnvironmentProvider;
 import kkckkc.jsourcepad.model.settings.AdvancedSettings;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import kkckkc.jsourcepad.util.io.UISupportCallback;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.model.TextInterval;
 import kkckkc.utils.Pair;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CompletionManager implements ChangeListener {
     private static Pattern WORD_PATTERN;
 
     private BufferImpl buffer;
 
     private List<String> completions;
     private int currentCompletion;
     private Interval previousCompletion;
 
     private boolean isNewCompletion = true;
 
     private int completeStartPosition = -1;
 
     private boolean enabled = true;
     private Set<String> completionSet;
     private BundleManager bundleManager;
 
     public CompletionManager(BufferImpl buffer) {
         this.buffer = buffer;
         this.bundleManager = Application.get().getBundleManager();
 
         synchronized (CompletionManager.class) {
             if (WORD_PATTERN == null) {
                 AdvancedSettings advancedSettings = Application.get().getSettingsManager().get(AdvancedSettings.class);
                 WORD_PATTERN = Pattern.compile(advancedSettings.getWordPattern());
             }
         }
     }
 
     public void completeNext() {
         complete();
 
         if (! isNewCompletion) {
             currentCompletion++;
             if (currentCompletion >= completions.size()) {
                 currentCompletion = 0;
             }
         }
 
         insertCompletion();
     }
 
     public void completePrevious() {
         complete();
 
         if (! isNewCompletion) {
             currentCompletion--;
             if (currentCompletion < 0) {
                 currentCompletion = completions.size() - 1;
             }
         }
 
         insertCompletion();
     }
 
     private void insertCompletion() {
         if (completions.isEmpty()) return;
 
         this.enabled = false;
 
         if (previousCompletion != null) {
             buffer.setSelection(Interval.createEmpty(this.completeStartPosition));
             buffer.remove(previousCompletion);
         }
 
         String text = completions.get(currentCompletion);
         String word = getPrefix();
 
         text = text.substring(word.length());
 
         int pos = buffer.getInsertionPoint().getPosition();
         buffer.insertText(pos, text, null);
 
         previousCompletion = Interval.createWithLength(pos, text.length());
 
         this.enabled = true;
     }
 
     private String getPrefix() {
         TextInterval interval = buffer.getCurrentWord();
        if (interval == null) return "";
         return buffer.getText(new Interval(interval.getStart(),
                 Math.min(buffer.getInsertionPoint().getPosition(), interval.getEnd())));
     }
 
     private void complete() {
         if (completions != null) {
             isNewCompletion = false;
             return;
         }
 
         InsertionPoint ip = buffer.getInsertionPoint();
 
         this.completeStartPosition = ip.getPosition();
 
         this.isNewCompletion = true;
         this.currentCompletion = 0;
         this.previousCompletion = null;
         completionSet = Sets.newHashSet();
         completions = Lists.newArrayList();
 
         Scope scope = ip.getScope();
         String word = getPrefix();
 
        if ("".equals(word)) return;

         completeStaticCompletions(scope, word);
         completeUsingCommand(scope);
         completeUsingWordsInDocument(scope, word);
     }
 
     private void completeUsingWordsInDocument(Scope scope, String word) {
         Integer disableDefault = (Integer) bundleManager.getPreference("disableDefaultCompletion", scope);
         if (disableDefault == null || disableDefault != 1) {
             Map<String, Integer> wordToPositionDistanceMap = Maps.newHashMap();
             String s = buffer.getCompleteDocument().getText();
             Matcher matcher = WORD_PATTERN.matcher(s);
             while (matcher.find()) {
                 String w = matcher.group();
 
                 if (Strings.isNullOrEmpty(w)) continue;
                 if (! w.startsWith(word)) continue;
 
                 int position = matcher.start();
                 int distance = Math.abs(position - completeStartPosition);
 
                 if (position == completeStartPosition - word.length()) continue;
 
                 if (wordToPositionDistanceMap.containsKey(w)) {
                     int curDistance = wordToPositionDistanceMap.get(w);
                     if (distance < curDistance) {
                         wordToPositionDistanceMap.put(w, distance);
                     }
                 } else {
                     wordToPositionDistanceMap.put(w, distance);
                 }
             }
 
             List<Pair<String, Integer>> tempList = Lists.newArrayList();
             for (Map.Entry<String, Integer> entry : wordToPositionDistanceMap.entrySet()) {
                 tempList.add(new Pair<String, Integer>(entry.getKey(), entry.getValue()));
             }
 
             Collections.sort(tempList, new Comparator<Pair<String, Integer>>() {
                 @Override
                 public int compare(Pair<String, Integer> object1, Pair<String, Integer> object2) {
                     return object1.getSecond().compareTo(object2.getSecond());
                 }
             });
 
             for (Pair<String, Integer> p : tempList) {
                 s = p.getFirst();
                 if (! completionSet.contains(s)) {
                     completionSet.add(s);
                     completions.add(s);
                 }
             }
         }
     }
 
     private void completeUsingCommand(Scope scope) {
         String completionCommand = (String) bundleManager.getPreference("completionCommand", scope);
         if (completionCommand != null) {
             ScriptExecutor scriptExecutor = new ScriptExecutor(completionCommand, Application.get().getThreadPool());
             scriptExecutor.setShowStderr(false);
 
             try {
                 Window window = buffer.getDoc().getDocList().getWindow();
                 ScriptExecutor.Execution ex = scriptExecutor.execute(
                         new UISupportCallback(window),
                         new StringReader(""),
                         EnvironmentProvider.getEnvironment(window, null));
                 ex.waitForCompletion();
 
                 for (String line : Splitter.on("\n").omitEmptyStrings().trimResults().split(ex.getStdout())) {
                     if (! completionSet.contains(line)) {
                         completionSet.add(line);
                         completions.add(line);
                     }
                 }
 
             } catch (IOException e) {
                 throw new RuntimeException(e);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             } catch (ExecutionException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     private void completeStaticCompletions(Scope scope, String word) {
         Object o = bundleManager.getPreference("completions", scope);
         if (o != null && o instanceof List) {
             for (String s : (List<String>) o) {
                 if (s.startsWith(word)) {
                     if (! completionSet.contains(s)) {
                         completionSet.add(s);
                         completions.add(s);
                     }
                 }
             }
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent e) {
         if (this.enabled) {
             this.completions = null;
         }
     }
 }
