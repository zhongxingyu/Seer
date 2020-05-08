 package Server.src;
 
 import java.io.*;
 import java.util.HashMap;
 
 public class Analyst {
 
     private BufferedReader in;
     private HashMap<String, String> fields;
 
     public Analyst(String Memo) {
         this(new BufferedReader(new CharArrayReader(Memo.toCharArray())));
     }
 
     public Analyst(InputStream _in) {
         this(new BufferedReader(new InputStreamReader(_in)));
     }
 
     public Analyst(BufferedReader _in) {
         in = _in;
         fields = new HashMap<String, String>();
     }
 
     public Memo getMemo() {
         return new Memo(fields);
     }
 
     public void parse() {
        try {
             waitForReader();
             parseVerbPathVersion();
             parse(in.readLine());
             parseContent();
         } catch(Exception e) {
             System.err.println("Reading a corrupted Memo.");
         }
     }
     
     private void waitForReader() throws IOException {
         while(!in.ready());
     }
 
     private void parse(String line) throws IOException {
         try {
             if(line.equals(""))
                 return;
             String[] fieldPair = line.split(": ");
             String value = "";
             for(int i = 1; i < fieldPair.length; i++) {
                 value += fieldPair[i];
             }
             fields.put(fieldPair[0], value);
             if(in.ready())
                 parse(in.readLine());
             else
                 return;
         } catch(NullPointerException e) {
             return;
         }
     }
 
     private void parseVerbPathVersion() throws Exception {
         if(in.ready()) {
             String readLine = in.readLine();
             if(readLine != null) {
                 String[] line = readLine.split(" ");
                 for(int i = 0; i < line.length; i++) {
                     if(line[i].equals("GET") || line[i].equals("HEAD") || line[i].equals("PUT") || line[i].equals("POST")) {
                         fields.put("Verb", line[i]);
                     } else if(line[i].startsWith("/")) {
                         fields.put("Path", line[i]);
                     } else if(line[i].equals("HTTP/1.0") || line[i].equals("HTTP/1.1")) {
                         fields.put("Version", line[i]);
                     }
                 }
             }
         }
     }
 
     private void parseContent() throws IOException {
         String data = "";
         while(in.ready()) {
            data += in.readLine() + "\r\n";
         }
         fields.put("Content", data);
     }
 
 }
