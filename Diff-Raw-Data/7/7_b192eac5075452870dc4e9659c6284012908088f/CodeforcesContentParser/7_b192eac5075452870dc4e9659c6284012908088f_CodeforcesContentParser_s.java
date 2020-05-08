 package ru.kirillova.search.database;
 
 import ru.chuprikov.search.gather.ProblemRawData;
 
 class CodeforcesContentParser implements ContentParser {
 
     private String getTextBody(String s, int index, int skipfirst) {
         StringBuilder str = new StringBuilder();
         int count = 0;
         for (int i = index; i < s.length(); ++i) {
             if (s.charAt(i) == '<') {
                 if (s.charAt(i + 1) == '/') {
                     if (skipfirst > 0) {
                         --skipfirst;
                     }
                     --count;
                 } else {
                     ++count;
                 }
                 for (; s.charAt(i) != '>'; ++i);
                 if (count == 0) {
                     return str.toString();
                 }
                 continue;
             }
             if (skipfirst == 0) {
                 str.append(s.charAt(i));
             }
         }
         return str.toString();
     }
 
     public ParsedProblem parseContent(ProblemRawData problem) {
         ParsedProblem p = new ParsedProblem(problem);
 
         String s = problem.getContent().substring(problem.getContent().indexOf("<div class=\"problem-statement\">"),
             problem.getContent().indexOf("<div class=\"sample-test\">"));
         String term = "<div class=\"title\">";
         p.title = getTextBody(s, s.indexOf(term), 0);
        term = "<DIV ID=\"problem_text\">";
         p.condition = getTextBody(s, s.indexOf(term), 0);
 
         return p;
     }
 }
