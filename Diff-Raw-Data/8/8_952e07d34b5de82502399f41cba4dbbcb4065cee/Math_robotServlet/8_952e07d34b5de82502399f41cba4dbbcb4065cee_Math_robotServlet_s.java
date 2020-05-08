 package org.zachary.mathrobot;
 
 import java.util.*;
 import com.google.wave.api.*;
 
 @SuppressWarnings("serial")
 public class Math_robotServlet extends AbstractRobotServlet {
 
   private String HELPINFO;
   
   private void initialize() {
     StringBuilder sb = new StringBuilder();
     sb.append("Math Robot help\n\n");
     sb.append("List of commands, and all begin with !mathr [command]\n");
     sb.append(" help - This help information\n");
     sb.append(" isprime [int]  - Check to see if an integer number is prime, up to 2^31\n");
     sb.append(" gcd [int] [int] - Find the Greatest Common Divisor of two integers");
     HELPINFO = sb.toString();
   }
   
   @Override
   public void processEvents(RobotMessageBundle events) {
     initialize();  
     
     if (events.wasSelfAdded()) {
     Wavelet wavelet = events.getWavelet();
     Blip blip = wavelet.appendBlip();
     TextView textView = blip.getDocument();
     textView.append("Math Robot is listening.\n");
     textView.append("For help, respond with \"!mathr help\"");
     return;
     }
 
     List<Event> submittedEvents = events.getBlipSubmittedEvents();
     for (Event e : submittedEvents ) {
     Blip blip = e.getBlip();
     if (!blip.getCreator().equals("math-robot")) {
       processMath(blip);
     }
     }
   }
   
   void processMath(Blip blip) {
     TextView tV = blip.getDocument();
     String blipString = tV.getText();
     blipString = blipString.trim();
     
     if (blipString.startsWith("!mathr")) {
       blipResponse(tV, processMathRequest(blipString));
     }
   }
   
   private void blipResponse(TextView tv, String response) {
     Blip responseBlip = tv.appendInlineBlip();
     TextView newTV = responseBlip.getDocument();
     newTV.append(response);
   }
   
   private String processMathRequest(String command) {
     String first = command.split("\n")[0];
     String[] tokens = first.split("\\s+");
     
     StringBuilder response = new StringBuilder();
     
     if(tokens.length == 1) {
       response.append("command missing");
     } 
     else if (tokens[1].equals("help")) {
       response.append(HELPINFO);
     } 
     else if (tokens[1].equals("gcd") && tokens.length > 3) {
       response.append(getGCD(tokens[2], tokens[3]));
     }
     else if (tokens[1].equals("isprime") && tokens.length > 2) {
       response.append(isPrime(tokens[2]));
     }
     else {
       response.append("Invalid command");
     }
     return response.toString(); 
   }
   
   private String isPrime(String num) {
     StringBuilder response = new StringBuilder();
     try {
       Integer prime = Integer.parseInt(num);
 
       if (prime < 1)
         throw new NumberFormatException();
       
       boolean isprime = true;
       Integer pSqrt = (new Double(Math.sqrt(prime))).intValue();
       List<Integer> pList = Sieve.sieve_of_eratosthenes(pSqrt);
       
       for (Integer i : pList) {
         if (prime % i == 0) {
           isprime = false;
           break; 
         }
       }
       
       if (isprime || prime == 2) {
         response.append(num + " is prime");
       }
       else {
         response.append(num + " is not prime");          
       }
     }
     catch (NumberFormatException e) {
       response.append(num + " is not a positive integer between 1 and 2^31.");      
     }
     return response.toString();
   }
 
   private String getGCD(String num1, String num2) {
     StringBuilder response = new StringBuilder();
     Integer n1 = null;
     Integer n2 = null;
     try {
       n1 = Integer.parseInt(num1);
     }
     catch (NumberFormatException e) {
       response.append(num1 + " is not a positive integer between 1 and 2^31.");      
     }
     try {
       n2 = Integer.parseInt(num2);
     }
     catch (NumberFormatException e) {
       response.append(num2 + " is not a positive integer between 1 and 2^31.");      
     }
     
    int n3 = 1;
     
     if (n1 > n2)
       n3 = GCD(n1,n2);
     else
       n3 = GCD(n2,n1);
     
     response.append("The GCD of " + n1 + " and " + n2 + " is " + n3);
     return response.toString();
   }
   
   private int GCD(int a, int b)
   {
      if (b==0) return a;
      return GCD(b,a%b);
   }
 }
