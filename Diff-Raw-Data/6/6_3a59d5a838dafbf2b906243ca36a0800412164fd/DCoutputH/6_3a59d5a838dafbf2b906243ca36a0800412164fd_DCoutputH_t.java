 package dcc;
 
 import dcc.sify.MultiString;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DCoutputH {
     
     boolean debug = false;
     Date time;
     DateFormat timed;
 
     public DCoutputH(){
         timed = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
 	time = new Date();
 	}
     
     public DCoutputH(boolean debugI){
         timed = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
 	time = new Date();
 	debug = debugI; 
     }
     public boolean println (String input){
 		System.out.println("[" + timed.format(time) + "] " + input);
                //TODO logging to file ...
 		return true;
 	}
     public void println (String pre,MultiString input){
		for (int c = 1;c-1<input.lines;c++){
                this.println(pre + (String) input.line.get(c));
                 }
     }
     public boolean println (String input,String mode){
 		switch (mode) {
                     case "N":
                         this.println(input);
                         break;
                     case "E1":
                         //for a little error
                         this.println("[E1] Whops ...");
                         this.println("[E1]=> " + input);
                         this.println("[E1] of course it's still working :)");
                         break;
                     case "E2":
                         this.println("[E2] Something went wrong ...");
                         this.println("[E2]=> " + input);
                         this.println("[E2] but it didn't crashed yet ;)");
                         break;
                     case "E3":
                         this.println("[E3] I have very bad news");
                         this.println("[E3]=> " + input);
                         this.println("[E3] <hardly continues it's work>");
                         break;
                     case "E4":
                         this.println("[E4] Oh, it hurts so much");
                         this.println("[E4]=> " + input);
                         this.println("[E4] i'm dying !");
                         this.println("BZZt Bzzt bzz...");
                         System.exit(2);
                     case "E5":
                         this.println("[E5] Critical error ocured:");
                         this.println("[E5]=> " + input);
                         this.println("[E5] Killing in 1 second");
                         this.println("!!!ERROR!!!");
                         System.exit(2);
                     case "D":
                         if(debug){this.println("[Debug]=> " + input);}
                         break;
                     case "debug":
                         if(debug){this.println("[Debug]=> " + input);}
                         break;
                     default:
                         this.println(input);
 
     }
                 return true;
 	}
     public boolean println (MultiString input,String mode){
 		switch (mode) {
                     case "N":
                         this.println("",input);
                         break;
                     case "E1":
                         //for a little error
                         this.println("[E1] Whops ...");
                         this.println("[E1]=> ",input);
                         this.println("[E1] of course it's still working :)");
                         break;
                     case "E2":
                         this.println("[E2] Something went wrong ...");
                         this.println("[E2]=> ",input);
                         this.println("[E2] but it didn't crashed yet ;)");
                         break;
                     case "E3":
                         this.println("[E3] I have very bad news");
                         this.println("[E3]=> ",input);
                         this.println("[E3] <hardly continues it's work>");
                         break;
                     case "E4":
                         this.println("[E4] Oh, it hurts so much");
                         this.println("[E4]=> ",input);
                         this.println("[E4] i'm dying !");
                         this.println("BZZt Bzzt bzz...");
                         System.exit(2);
                     case "E5":
                         this.println("[E5] Critical error ocured:");
                         this.println("[E5]=> ",input);
                         this.println("[E5] Killing in 1 second");
                         this.println("!!!ERROR!!!");
                         System.exit(2);
                     case "D":
                         if(debug){this.println("[Debug]=> ",input);}
                         break;
                     case "debug":
                         if(debug){this.println("[Debug]=> ",input);}
                         break;
                     default:
                         this.println("",input);
 
     }
                 return true;
 	}
 
     //So funny, INTeger input = INTput
     public boolean println(int INTput) {
                 System.out.println("[" + timed.format(time) + "] " + INTput);
 		
 		return true;
     }
 }
