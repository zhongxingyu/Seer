 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.lang.reflect.Method;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 
 public class Client {
 
     private int port;
     private Scanner scan = new Scanner(System.in);
 
     public Client(int port){
         this.port = port;
     }
 
     private String getClassChoice(String[] options){
         boolean invalid = true;
         String input = "";
         while(invalid){
             try{
                 System.out.println("Type the name of your class choice: ");
                 input = scan.next();
                 for(String option : options){
                     if(input.toLowerCase().trim().equals(option.toLowerCase())){
                         invalid = false;
                     }
                 }
                 if(invalid)
                     System.out.println("check for misspellings");
             }
             catch(Exception e){
                 e.printStackTrace();
             }
         }
         return input;    }
 
     public void Connect(){
         try{
 
             Socket socket = new Socket("localhost",port);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
 
             boolean notDone = true;
             boolean classChosen =false;
             boolean methodChosen = false;
             while(notDone){
 
                 if(!classChosen){
                     String[] classOptions = in.readLine().split(" ");
 
                     String classPrompt = "Choose a class: ";
                     for(String classOption: classOptions){
                         classPrompt += classOption + ", ";
                     }
                     classPrompt = classPrompt.substring(0, classPrompt.length()-2);
 
                     System.out.println(classPrompt);
 
                     out.println(getClassChoice(classOptions));
                     out.flush();
                     classChosen = true;
                 }
 
                 if(!methodChosen){
 
                     List<Operation> operationList = new ArrayList<Operation>();
                     String options = in.readLine();
                     String[] methods = options.split(";");
                     for(String method : methods){
                         String[] signature = method.split(":");
                         String[] params = null;
                         if(signature.length > 1){
                             params = signature[1].split(",");
 
                         }
                         operationList.add(new Operation(signature[0].toLowerCase(), params));
                     }
                     String prompt = "Choose Operation: ";
 
                     for(Operation o : operationList){
                         prompt += o.getName() + ",";
                     }
                     prompt += " or Exit";
 
                     System.out.println(prompt);
                     boolean invalid = true;
                     String operation ="";
                     while(invalid){
                         try{
                             operation = scan.nextLine();
                             if(operationExists(operationList, operation) || operation.equals("exit")){
                                 invalid = false;
                             }
 
                         }catch(Exception e ){
                             e.printStackTrace();
                         }
 
                     }
                     operation = operation.toLowerCase();
                     if(operationExists(operationList, operation)){
                         System.out.println("You chose " + operation + ", now insert its parameters");
                         Operation oper = getOperation(operationList, operation);
                         String request = operation + "|||";
                         List<Class> params = oper.getParams();
                         if(params.size() > 0 ){
                             for(int i = 0; i < params.size(); i++){
                                 request +=  getInput(params.get(i).getName());
                                 if(i < params.size()-1)
                                     request += ",";
                             }
 
                         }
                         out.println(request);
                         out.flush();
                         System.out.println("Answer: " + in.readLine());
                         methodChosen = false;
                         classChosen = false;
                     }
                     else if(operation.equals("exit")){
                         notDone = false;
                         out.close();
                         in.close();
                         socket.close();
                     }
                 }
             }
         }catch(Exception e){
             e.printStackTrace();
         }
 
 
     }
 
     public Operation getOperation(List<Operation> operationList, String operation){
         for(Operation o : operationList)
             if(o.getName().equals(operation))
                 return o;
         return null;
     }
 
     public boolean operationExists(List<Operation> operationList, String operation){
         boolean found = false;
 
         for(int i = 0; i < operationList.size() && !found; i++){
             found = operationList.get(i).getName().equals(operation);
         }
 
         return found;
     }
 
     public String getInput(String type){
         boolean invalid = true;
         String input = "";
         while(invalid){
             System.out.println("Input a " + type + ".");
             if(type.equals("java.lang.String")){
                input += scan.nextLine() + "::" + type;
                 invalid = false;
             }
             else{
                 try{
                     Class c = Class.forName(type);
                     Method m = c.getMethod("valueOf",String.class);
                    input += m.invoke(null,scan.nextLine()) + "::" + type;
                     invalid = false;
 
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
             }
 
 
         }
         return input;
     }
 }
