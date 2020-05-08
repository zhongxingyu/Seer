 package com.incandescent.closure;
 
 import com.google.javascript.jscomp.CheckLevel;
 import com.google.javascript.jscomp.JSError;
 import com.google.javascript.jscomp.PrintStreamErrorManager;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * Filters errors by source file
  */
 public class FilteredPrintStreamErrorManager extends PrintStreamErrorManager {
     protected List<Pattern> includePatterns = new ArrayList<Pattern>();
     protected List<Pattern> excludePatterns = new ArrayList<Pattern>();
 
     public FilteredPrintStreamErrorManager(PrintStream stream, String[] include, String[] exclude) {
         super(stream);
         addRegexes(include, includePatterns);
         addRegexes(exclude, excludePatterns);
     }
 
     protected void addRegexes(String[] regexes, List<Pattern> patterns) {
         if (regexes != null) {
             for (String s: regexes) {
                 patterns.add(Pattern.compile(s, Pattern.CASE_INSENSITIVE));
             }
         }
     }
 
     protected boolean match(List<Pattern> patterns, String str) {
         for (Pattern pattern: patterns) {
             if (pattern.matcher(str).matches()) return true;
         }
         return false;
     }
 
     @Override
     public void report(CheckLevel level, JSError error) {
         if (includePatterns.size() > 0) {
             if (!match(includePatterns, error.sourceName)) {
                 return;
             }
         }
         if (excludePatterns.size() > 0) {
             if (match(excludePatterns, error.sourceName)) {
                 return;
             }
         }
         super.report(level, error);
     }
 }
