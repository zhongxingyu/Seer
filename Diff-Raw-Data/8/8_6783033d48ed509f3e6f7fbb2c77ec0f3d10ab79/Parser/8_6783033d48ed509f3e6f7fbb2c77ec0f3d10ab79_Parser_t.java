 package com.ingemark.perftest.script;
 
 import static java.lang.Character.isWhitespace;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.ingemark.perftest.RequestProvider;
 import com.ingemark.perftest.Script;
 
 public class Parser
 {
   static final Pattern
     headerRegex = Pattern.compile("\\s*(\\S+)(?:\\s+(.+))?"),
     kvRegex = Pattern.compile("\\s*(\\S+)\\s*[=:]\\s*(.+)$");
   final InputStream is;
   final List<RequestProvider> initReqs = new ArrayList<>(), testReqs = new ArrayList<>();
   List<RequestProvider> currReqs;
   int reqProviderIndex;
   final Map<String, String> config = new HashMap<>();
 
   public Parser(InputStream is) { this.is = is; }
 
   public Script parse() {
     try {
       final List<List<Line>> sections = slurp(is);
       for (List<Line> sec : sections) parseSection(sec);
       return new Script(initReqs, testReqs, config);
     } catch (IOException e) {throw new RuntimeException(e);}
   }
 
   void parseSection(List<Line> section) {
     if (section.isEmpty()) return;
     final Line header = section.get(0);
     final Matcher m = headerRegex.matcher(header.line);
     if (!m.matches()) throw new RuntimeException("Invalid section header at " + header);
     switch(m.group(1)) {
     case "CONFIG": parseConfig(section); break;
     case "REQUEST":
       currReqs.add(parseReqProvider(reqProviderIndex, m.group(2), section));
       if (isNotBlank(m.group(2))) reqProviderIndex++;
       break;
     case "ONCE":
       if (currReqs == null) currReqs = initReqs;
       else throw new RuntimeException(
           "ONCE section can only appear before any REQUEST section at " + header);
       break;
     case "REPEAT": currReqs = testReqs; break;
     }
   }
 
   void parseConfig(List<Line> lines) {
     for (Line l : skipBlankLines(lines)) {
       final Matcher m = kvRegex.matcher(l.line);
       if (m.matches()) throw new RuntimeException("Malformed configuration line at " + l);
       config.put(m.group(1), m.group(2));
     }
   }
 
   static RequestProvider parseReqProvider(int index, String name, List<Line> section) {
     final Iterator<Line> it = section.iterator();
     final Line header = it.next();
     if (!it.hasNext()) throw new RuntimeException("Empty request section at " + header);
     final Line req = it.next();
     final Matcher m = headerRegex.matcher(req.line);
     if (!m.matches()) throw new RuntimeException("Malformed request at " + req);
     if (it.hasNext() && !isBlank(it.next().line))
       throw new RuntimeException("Blank line must follow request at " + req);
     final String body;
     if (it.hasNext()) {
       final StringBuilder b = new StringBuilder(128);
       while (it.hasNext()) b.append(it.next().line).append("\n");
       body = b.toString();
     } else body = null;
     return new RequestProvider(index, name, m.group(1), m.group(2), body);
   }
 
   List<List<Line>> slurp(InputStream is) throws IOException {
     final List<List<Line>> ret = new ArrayList<>();
     try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
       List<Line> section = newSection(ret);
       int i = 0;
       for (String line; (line = r.readLine()) != null; i++) {
        if (line.matches("-{3,}")) section = newSection(ret);
         else section.add(new Line(i, line));
       }
     }
     return ret;
   }
   static List<Line> newSection(List<List<Line>> lol) {
     final List<Line> ret = new ArrayList<>();
     lol.add(ret);
     return ret;
   }
   static List<Line> skipBlankLines(List<Line> in) {
     final List<Line> ret = new ArrayList<>();
     for (Line l : in) if (isNotBlank(l.line)) ret.add(l);
     return ret;
   }
   static boolean isNotBlank(String str) {
     int strLen;
     if (str == null || (strLen = str.length()) == 0) return false;
     for (int i = 0; i < strLen; i++)
       if (!isWhitespace(str.charAt(i))) return true;
     return false;
   }
   static boolean isBlank(String str) { return !isNotBlank(str);
   }
   static class Line {
     final int ind;
     final String line;
     Line(int ind, String line) { this.ind = ind; this.line = line; }
     public String toString() { return "line " + ind + "\"" + line + "\""; }
   }
 }
