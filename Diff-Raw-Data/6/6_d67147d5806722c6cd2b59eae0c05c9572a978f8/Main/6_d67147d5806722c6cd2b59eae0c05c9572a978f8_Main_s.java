 package gradespeed;
 import java.io.*;
 import static java.lang.System.*;
 import java.util.*;
 import org.jsoup.*;
 import org.jsoup.Connection.*;
 import org.jsoup.nodes.*;
 import org.jsoup.select.*;
 
 public class Main {
 
     public static void main(String[] args) throws Exception{
         
 //        String code = "PGRpdiB1bnNlbGVjdGFibGU9Im9uIiBvbnNlbGVjdHN0YXJ0PSJyZXR1cm4gZmFsc2U7IiBzdHlsZT0iLW1vei11c2VyLXNlbGVjdDpub25lIj48UD48ZGl2IGNsYXNzPSJTdHVkZW50SGVhZGVyIj48c3BhbiBjbGFzcz0iU3R1ZGVudE5hbWUiPkxhbmtlbmF1LCBQYXRyaWNpbzwvc3Bhbj4gKEtsZWluIE9hayBIaWdoIFNjaG9vbCk8L2Rpdj48dGFibGUgYm9yZGVyPSIwIiBjZWxsc3BhY2luZz0iMCIgY2VsbHBhZGRpbmc9IjMiIGNsYXNzPSJEYXRhVGFibGUiPjx0ciBjbGFzcz0iVGFibGVIZWFkZXIiPjx0aCBhbGlnbj0ibGVmdCIgc2NvcGU9ImNvbCIVGVhY2hlcjwvdGgPHRoIGFsaWduPSJsZWZ0IiBzY29wZT0iY29sIj5Db3Vyc2U8L3RoPjx0aCBhbGlnbj0ibGVmdCIgc2NvcGU9ImNvbCIUGVyaW9kPC90aD48dGggYWxpZ249ImxlZnQiIHNjb3BlPSJjb2wiPkN5Y2xlIDE8L3RoPjx0aCBhbGlnbj0ibGVmdCIgc2NvcGU9ImNvbCIQ3ljbGUgMjwvdGgPHRoIGFsaWduPSJsZWZ0IiBzY29wZT0iY29sIj5DeWNsZSAzPC90aD48dGggYWxpZ249ImxlZnQiIHNjb3BlPSJjb2wiPkV4YW0gMTwvdGgPHRoIGFsaWduPSJsZWZ0IiBzY29wZT0iY29sIj5TZW0gMTwvdGgPHRoIGFsaWduPSJsZWZ0IiBzY29wZT0iY29sIj5DeWNsZSA0PC90aD48dGggYWxpZ249ImxlZnQiIHNjb3BlPSJj";
 //        out.println(decodeString(code));
         String code = "<div unselectable=\"on\" onselectstart=\"return false;\" style=\"-moz-user-select:none\"><P><div class=\"StudentHeader\"><span class=\"StudentName\">Lankenau, Patricio</span> (Klein Oak High School)</div><table border=\"0\" cellspacing=\"0\" cellpadding=\"3\" class=\"DataTable\"><tr class=\"TableHeader\"><th align=\"left\" scope=\"col\">Teacher</th><th align=\"left\" scope=\"col\">Course</th><th align=\"left\" scope=\"col\">Period</th><th align=\"left\" scope=\"col\">Cycle 1</th><th align=\"left\" scope=\"col\">Cycle 2</th><th align=\"left\" scope=\"col\">Cycle 3</th><th align=\"left\" scope=\"col\">Exam 1</th><th align=\"left\" scope=\"col\">Sem 1</th><th align=\"left\" scope=\"col\">Cycle 4</th><th align=\"left\" scope=\"col\">Cycle 5</th><th align=\"left\" scope=\"col\">Cycle 6</th><th align=\"left\" scope=\"col\">Exam 2</th><th align=\"left\" scope=\"col\">Sem 2</th></tr><tr class=\"DataRow\">"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:jmellen1@kleinisd.net\" class=\"EmailLink\">Mellen, J</a></th><td align=\"left\">COMPUTER SCI  HL -IB</td><td>1</td><td><a href=\"?data=MXw1OTY2MDR8MTEwMTN8MzQyOHwzfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Mellen, J Course COMPUTER SCI  HL -IB Period 1 Cycle 1 Grade 99\">99</a></td><td><a href=\"?data=Mnw1OTY2MDR8MTEwMTN8MzQyOHwzfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Mellen, J Course COMPUTER SCI  HL -IB Period 1 Cycle 2 Grade 95\">95</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Mellen, J Course COMPUTER SCI  HL -IB Period 1 Semester 1 Grade 97\">97</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr class=\"DataRowAlt\">"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:sparent1@kleinisd.net\" class=\"EmailLink\">Parent, S</a></th><td align=\"left\">HIST OF AMERICAS HL -IB</td><td>2</td><td><a href=\"?data=MXw1OTY2MDR8MTAzMjJ8MzQxMnwzfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Parent, S Course HIST OF AMERICAS HL -IB Period 2 Cycle 1 Grade 95\">95</a></td><td><a href=\"?data=Mnw1OTY2MDR8MTAzMjJ8MzQxMnwzfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Parent, S Course HIST OF AMERICAS HL -IB Period 2 Cycle 2 Grade 100\">100</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Parent, S Course HIST OF AMERICAS HL -IB Period 2 Semester 1 Grade 98\">98</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr class=\"DataRow\">"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:brice1@kleinisd.net\" class=\"EmailLink\">Rice, B</a></th><td align=\"left\">MATHEMATICS HL -IB</td><td>3</td><td><a href=\"?data=MXw1OTY2MDR8MTAxNzB8MzQyNHwxfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Rice, B Course MATHEMATICS HL -IB Period 3 Cycle 1 Grade 92\">92</a></td><td><a href=\"?data=Mnw1OTY2MDR8MTAxNzB8MzQyNHwxfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Rice, B Course MATHEMATICS HL -IB Period 3 Cycle 2 Grade 90\">90</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Rice, B Course MATHEMATICS HL -IB Period 3 Semester 1 Grade 91\">91</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr class=\"DataRowAlt\">"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:lgarner@kleinisd.net\" class=\"EmailLink\">Garner, L</a></th><td align=\"left\">ENGLISH 4 -IB</td><td>4</td><td><a href=\"?data=MXw1OTY2MDR8NTUxMHwzNDAyfDR8MTAxOTE1fDM%3d\" class=\"Grade\"  title=\"Teacher Garner, L Course ENGLISH 4 -IB Period 4 Cycle 1 Grade 93\">93</a></td><td><a href=\"?data=Mnw1OTY2MDR8NTUxMHwzNDAyfDR8MTAxOTE1fDM%3d\" class=\"Grade\"  title=\"Teacher Garner, L Course ENGLISH 4 -IB Period 4 Cycle 2 Grade 97\">97</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Garner, L Course ENGLISH 4 -IB Period 4 Semester 1 Grade 95\">95</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr class=\"DataRow\">"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:jmellen1@kleinisd.net\" class=\"EmailLink\">Mellen, J</a></th><td align=\"left\">IND STUDY EMERG TECH</td><td>5</td><td><a href=\"?data=MXw1OTY2MDR8MTEwMTN8NDkzNnwxfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Mellen, J Course IND STUDY EMERG TECH Period 5 Cycle 1 Grade 100\">100</a></td><td><a href=\"?data=Mnw1OTY2MDR8MTEwMTN8NDkzNnwxfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Mellen, J Course IND STUDY EMERG TECH Period 5 Cycle 2 Grade 100\">100</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Mellen, J Course IND STUDY EMERG TECH Period 5 Semester 1 Grade 100\">100</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr class=\"DataRowAlt\">'"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:aparent1@kleinisd.net\" class=\"EmailLink\">Parent, A</a></th><td align=\"left\">PHYSICS 2 -IB</td><td>6</td><td><a href=\"?data=MXw1OTY2MDR8MTE0ODh8MzQzOHwxfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Parent, A Course PHYSICS 2 -IB Period 6 Cycle 1 Grade 96\">96</a></td><td><a href=\"?data=Mnw1OTY2MDR8MTE0ODh8MzQzOHwxfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Parent, A Course PHYSICS 2 -IB Period 6 Cycle 2 Grade 99\">99</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Parent, A Course PHYSICS 2 -IB Period 6 Semester 1 Grade 98\">98</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr class=\"DataRow\">"
 +"<th align=\"left\" scope=\"row\" class=\"TeacherNameCell\"><a href=\"mailto:rtumlinson@kleinisd.net\" class=\"EmailLink\">Tumlinson, R</a></th><td align=\"left\">THEORY OF KNOWL A -IB</td><td>7</td><td><a href=\"?data=MXw1OTY2MDR8NTU2NHwzNDY4QXwyfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Tumlinson, R Course THEORY OF KNOWL A -IB Period 7 Cycle 1 Grade 93\">93</a></td><td><a href=\"?data=Mnw1OTY2MDR8NTU2NHwzNDY4QXwyfDEwMTkxNXwz\" class=\"Grade\"  title=\"Teacher Tumlinson, R Course THEORY OF KNOWL A -IB Period 7 Cycle 2 Grade 85\">85</a></td><td>&nbsp;</td><td>&nbsp;</td><td><span class=\"Grade\" title=\"Teacher Tumlinson, R Course THEORY OF KNOWL A -IB Period 7 Semester 1 Grade 89\">89</span></td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr></table>&nbsp;</P></div>";
 
         //extrapolate(code);
         String html = code;//getHTML();
 
         int c = getCycle(html);
         ArrayList<Grade> cycle = extrapolate(html, c);
         out.println(cycle.toString());
 
     }
     public static String getHTML() throws Exception{
         //Log in
         long start = System.currentTimeMillis();
         
         out.println("Logging in...");
        String password =                                                                                            "Cocacola1";
         Response res = Jsoup
             .connect("https://gradespeed.kleinisd.net/pc/Default.aspx")
            .data("txtUserName", "plankenau")
             .data("txtPassWord", password)
             .method(Method.POST)
             .execute();
 
         Document doc = res.parse();
         out.println("Logged in ("+((System.currentTimeMillis()-start)/1000F)+" secs)"); //;
 
         start = System.currentTimeMillis();
 
         //Keep logged in
         Map<String, String> cookies = res.cookies();
 
         Document doc2 = Jsoup
             .connect("https://gradespeed.kleinisd.net/pc/ParentStudentGrades.aspx")
             .cookies(cookies)
             .get();
 
         String codehtml = doc2.body().html();
 
         out.println("Fetched ("+((System.currentTimeMillis()-start)/1000F)+ " secs)");
 
         start = System.currentTimeMillis();
 
         String code = codehtml.split("var")[1];
         code = code.split("</script>")[0];
 
         code = code.replaceAll("[ ]", "");
         String var = code.substring(0,code.indexOf(";")); //might have to -1
         code = code.replaceAll(var+";","");
         code = code.replaceAll(var+"='';","");
         code = code.replaceAll(var+"="+var+"+", "");
         code = code.replace("';", "");
         code = code.replace("+'", "");
         //code = code.replaceAll(var,"VAR");
         code = code.replace("document.write(decodeString("+var+"));", "");
         code = code.replace("-->", "");
         code = code.replaceAll("\\n","");
         code = code.replaceAll("\\r","");
 
         String decoded = decode(code);
         out.println("Decoded ("+((System.currentTimeMillis()-start)/1000F)+ " secs)");
         return decoded;
         
     }
     public static void print(String what) throws Exception{
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("print.out")));
         out.println(what);
         out.flush();
         out.close();
     }
     public static void print(String code, String decoded) throws Exception{
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("coded.out")));
         PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter("decoded.out")));
         out.println(code);
         out.flush();
         out.close();
 
         out2.println(decoded);
         out2.flush();
         out2.close();
     }
     public static ArrayList<Grade> extrapolate(String html, int cycle){
         Document doc = Jsoup.parse(html);
         Elements links = doc.select("[href]");
         links = links.select("[title*=Cycle "+cycle+"]");
         ArrayList<Grade> grades = new ArrayList<Grade>(); //what this kolaveri? 
         for (Element a: links){
             Grade grade = new Grade(a.attr("title"));
             grades.add(grade);
             System.out.println(grades);
         }
         return grades;        
     }
     public static int getCycle(String html){
         Document doc = Jsoup.parse(html);
         Elements links = doc.select("[href]");
         links = links.select("[title*=Cycle]");
         ArrayList<Integer> cycles = new ArrayList<Integer>();
         for (Element e : links){
             String t = e.attr("title");
             cycles.add(Integer.parseInt(t.substring(t.indexOf("Cycle ")+6,t.indexOf(" Grade"))));
         }
         Collections.sort(cycles);
         return cycles.get(cycles.size()-1);
     }
     public static String decode(String input) {
     String keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
     String output = "";
     int chr1, chr2, chr3;
     int enc1, enc2, enc3, enc4;
     int i = 0;
 
     // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
     input = input.replaceAll("/[^A-Za-z0-9\\+\\/\\=]/g", "");
 
     do {
         try{
         enc1 = keyStr.indexOf(input.charAt(i++));
         enc2 = keyStr.indexOf(input.charAt(i++));
         enc3 = keyStr.indexOf(input.charAt(i++));
         enc4 = keyStr.indexOf(input.charAt(i++));
         
 
         chr1 = (enc1 << 2) | (enc2 >> 4);
         chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
         chr3 = ((enc3 & 3) << 6) | enc4;
 
 
         output += (char)(chr1);
 
         if (enc3 != 64) {
             output += (char)(chr2);
         }
         if (enc4 != 64) {
             output += (char)(chr3);
         }
 
         }catch (Exception e){
             err.println("ERROR i:"+i+" \n "+e);
         }
     }while (i<input.length()-input.length()%4);
     return output;
 }
 
 
 }
