 package gutenberg.workers;
 
 import java.io.PrintWriter;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.HashMap;
 import java.util.List;
 
 
 public class DocumentWriter extends PrintWriter implements ITagLib {
     
     public DocumentWriter(Path writerPath) throws Exception {
         super(Files.newBufferedWriter(writerPath,
             StandardCharsets.UTF_8, StandardOpenOption.CREATE));
         dice = new XORRandom(4);
     }
         
     public void writeTemplate(Path template) throws Exception{
         List<String> file = Files.readAllLines(template, StandardCharsets.UTF_8);
         for (String line : file) {
             println(line);
         }
     }
     
     public void writeTemplate(Path template,  HashMap<String, String>params) throws Exception{
         
         List<String> file = Files.readAllLines(template, StandardCharsets.UTF_8);
         for (String line : file) {
             
             String trimmed = line.trim();
             if (trimmed.startsWith(comment)) { // => a comment
                 continue;
             } else if (trimmed.startsWith(ITagLib.printanswers)) {
                 continue;
             } else if (trimmed.startsWith(setCounter)) {
                 int counter = params.get(setCounter).equalsIgnoreCase("0")? 
                     0:dice.nextInt();
                 line = line.replace("{0}", String.format("{%d}", counter));
             } else if (trimmed.startsWith(insertQR)) {
                 String QRCode = String.format("{%s%s}", params.get(insertQR), 
                     ITagLib.pageNumber);
                 line = line.replace("{QRC}", QRCode);
             }
             println(line);
         }
     }
     
     public void writePreamble(String quiz) {
         println("\\documentclass[12pt,a4paper,justified]{tufte-exam}");
         println(String.format("%s{%s}", school, quiz));
        println(fancyfooter);
     }
     
     public void printAuthor(String author) { 
         println(String.format("%s{%s}",docAuthor, author));        
     }
     public void beginQuiz() {
         println(beginDocument);
         println(beginQuestions);
     }
 
     public void endQuiz() {
         println(endQuestions);
         println(endDocument);
     }
     
     public void newPage() {
         println(newpage);
     }
     
     public void printAnswers() {
         println(printanswers);
     }
     
     public void printRubric() {
         println(printRubric);
     }
 
     public void resetPageNumbering() {
         println("\\setcounter{page}{1}");
     }
 
     public void resetQuestionNumbering() {
         println("\\setcounter{question}{0}");
     }
     
     public void setCounter(int i) {
         println(String.format("\\setcounter{rolldice}{%d}", i));
     }
 
     public void insertBlankPage() {
         print("\\begingroup");
         print("\\centering For rough work only. Will NOT be graded \\\\");
         println("\\endgroup");
         println(insertQR + BLANK_PAGE_CODE);
         println(newpage);
     }
     
     private XORRandom dice;
     private String BLANK_PAGE_CODE = "{0}";
         
 }
