 
 import java.awt.TextArea;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /* OpSystem Class
  * 
  */
 
 /**
  *
  * @author amabeli
  */
 public class OpSystem {
     
     private TextArea output;
     private int numResourceTypes=0;
     private String available="", tempProcessLabels="";
     private ArrayList resourceList = new ArrayList(),tempAllocation = new ArrayList();
     private ArrayList tempNewWorkList = new ArrayList(),tempNewAvailableList = new ArrayList();
     private ArrayList availableList = new ArrayList();
     private ArrayList<Integer> workList = new ArrayList();
     private ArrayList<Process> processList = new ArrayList();
     private ArrayList<String> processLabels = new   ArrayList();
     
     public OpSystem(TextArea output){
         this.output = output;
     }
     
     public void parseAndStoreData(String inputBlock){
         System.out.println("attempting to parse the input data");
         output.append(inputBlock+"\n\n\n");
         Scanner scanner = new Scanner(inputBlock);
         
         tempProcessLabels = scanner.nextLine();
 //        available = Integer.parseInt(scanner.nextLine());
         available = scanner.nextLine();
         output.append("Available in the system:\n");
         output.append(tempProcessLabels+"\n");
         output.append(available+"\n");
         System.out.println("Available length "+ available.length());
         numResourceTypes = (available.length()+1)/2;
         System.out.println("Resources "+ numResourceTypes);
         for(int i = 0; i<available.length(); i+=2){
             char s = available.charAt(i);
             int n = Integer.parseInt(""+s);
             availableList.add(n);
         }
         
         output.append(availableList+"\n");
         output.append("Processes\n"
                 + "        Allocation     Max     Need\n");
         
         
         
          while(scanner.hasNext()){
             ArrayList tempAllocation = new ArrayList();
             ArrayList tempMax = new ArrayList();
             ArrayList tempNeed = new ArrayList();
             String processLine = scanner.nextLine();
             
             //create allocation list
             for(int i = 0; i<processLine.length()/2; i+=2){
                 tempAllocation.add(Integer.parseInt(""+processLine.charAt(i)));
             }
             
             //create Max list
             for(int i = processLine.length()/2+1; i<processLine.length(); i+=2){
                 tempMax.add(Integer.parseInt(""+processLine.charAt(i)));
             }
             
             //create need list
             for(int i = 0; i<tempMax.size(); i++){
                 int x = (Integer)tempAllocation.get(i);
                 int y = (Integer)tempMax.get(i);
                 tempNeed.add(y-x);
             }
              processList.add(new Process(tempAllocation,tempMax, tempNeed));
         }
         for(int i=0;i<processList.size();i++){
             output.append("** "+i+":"+processList.get(i).toString());
         }
     }
     
     
     public boolean safety(){
         ArrayList workList = new ArrayList();
         boolean allgood = true;
         for(int i = 0; i<availableList.size(); i++){
             int tempNum = 0;
             tempNum = (Integer)availableList.get(i);
             workList.add(tempNum);
         }
         for(int j = 0; j<processList.size(); j++){
             for(int i=0; i<numResourceTypes; i++){
                 if((Integer)processList.get(j).getNeed().get(i) <= (Integer)workList.get(i) && processList.get(j).getIsFinished() == false){ 
                                 System.out.println(processList.get(j)+" good");
                             }else{
                                 allgood = false;
                             }
                 
             }
             if(allgood == true){
                 for(int i=0; i<numResourceTypes; i++){
                 if((Integer)processList.get(j).getNeed().get(i) <= (Integer)workList.get(i) && processList.get(j).getIsFinished() == false){ 
                     for(int k=0; k<numResourceTypes; k++){
                         int x = (Integer)tempAllocation.get(i);
                         System.out.println("x: "+x);
                         int y = (Integer)workList.get(i);
                         System.out.println("y: "+y);
                         tempNewWorkList.add(y+x);
                         
                         if(j == processList.size()-1){
                         j =-1;
                         }
                 }  
                 }
                 }
                 workList.clear();
                 
                 for(int i=0; i<numResourceTypes; i++){
                  workList.add(tempNewWorkList.get(i));
                  
              }
                 
             }
             
             
         }
         
         for(int i=0; i<numResourceTypes; i++){
         if(processList.get(i).getIsFinished() == false){return false;}
         }
         return true;
     }
     
    /* public void safetyCheck(){
         System.out.println("safetyCheck()\n");
         
         boolean goToStepFour = false;
         
         //ArrayList<Integer> workList = new ArrayList(); //Work = n, Work.size() = needList.size();
         //ArrayList<Boolean> finishList = new ArrayList(); //finishList.size() = processlist.size();
         
         //Step 1
         
         //worklist = resourceList
         //why is this if statement here???????????????????????????????? (almost never true)
         //if(needList.size() == resourceList.size()){
         System.out.println("Start Step 1");
             for(int i = 0; i < resourceList.size(); i++){
                 workList.add((Integer) resourceList.get(i));
             }
         //}
         
         //finishList[i] = false        
         for(int i = 0; i < processList.size(); i++){
             processList.get(i).setFinished(false);
         }
 
         //Step 2
         
         //i such that
         //for(){
         //if (finish[i] == false && need[i] <=(sep) work)
         // 
         //}
         //Separate less than or equal
         
         int badProcess = -1;
         int timesThroughLoop=0;
         System.out.println("Start Step 2");
         System.out.println("Before Step 2" + goToStepFour);
         for(int i = 0; i < processList.size(); i++){
             if(processList.get(i).getIsFinished() == true && isLessThanOrEqualTo() == true){
                 System.out.println("During step 2" + goToStepFour);
                 processList.get(badProcess).setFinished(true);
                 goToStepFour = true;
             }
             else{
                 System.out.println("Else 1 " + goToStepFour);
                 goToStepFour = false;
                 System.out.println("Else 2 " + goToStepFour);
                 badProcess = i;
 //                break;
             }
             if(i==processList.size()-1){
                 boolean allTrue = true;
                 
                     System.out.println(allTrue);
                 timesThroughLoop++;
                 if(timesThroughLoop<processList.size()){
                         
                         for(int j=0;j<processList.size();j++){
                         if(processList.get(i).getIsFinished()==false){
                             i=0;
                             allTrue=false;
                         }
                     }
                         System.out.println(" - "+allTrue+"\n");
                 }
                 System.out.println("i = "+i+"\nprocessList.size() = "+processList.size()+"\ntimesThroughLoop = "+timesThroughLoop+"\n\n");
                 if(allTrue && i == processList.size()-1){
                     
                     System.out.println("System is Safe!");
                 }
                 else{
                     
                     System.out.println("System is NOT Safe!");
                 }
             }
         }
         
         
         
         //Step 3
         
         //If in step 2, any [i] is true
         //work = work + allocation[i]
         //finish[i] = true
         //back to step 2
 //        System.out.println("Start Step 3");
 //        if(badProcess >= 0){
 //            for(int i = 0; i < processList.get(badProcess).getAllocationList().size(); i++){
 //                workList.add(processList.get(badProcess).getAllocationList().get(i));
 //                processList.get(badProcess).setIsFinished(true);
 //                //System.out.println("Before break");
 //                //break;
 //                
 //            }
 //            System.out.println("After break");
 //            safetyCheck();
 //        }
         
         
         
         //Step 4
         
         //If step 2 if statement after the for loop is false
         //for(){
         //if(all finish[i] == true){
         // Sopl -> safe state
         //}
         //}
         //else Sopl -> not safe
         System.out.println("" + goToStepFour);
         System.out.println("Start Step 4");
         if(goToStepFour == true){
             for(int i = 0; i < processList.size(); i++){
                 if(processList.get(i).getIsFinished() == true){
                     System.out.println("System is Safe!");
                 }
                 else{
                     System.out.println("System is not safe!");
                 }
             }
         }
         System.out.println("End Step 4");
         
     }//end safety check method
     */
     
     
     public void resourceRequest(String commandField){
         boolean success = true;
         System.out.println("Resource Request");
         System.out.println("Command Field "+ commandField);
         System.out.println("Char at 3:"+ commandField.charAt(2));
 //        ArrayList resourceList;
 //        resourceList = new ArrayList();
         for(int i=2; i<commandField.length(); i+=2){
             char s = commandField.charAt(i);
             String str = String.valueOf(s);
             int n = Integer.parseInt(str);
             resourceList.add(n);
         }
             int tempNum = (Integer)resourceList.get(0);
             System.out.println(tempNum);
             
             for(int i=0; i<numResourceTypes; i++){
                 tempNum = (Integer)resourceList.get(i+1);
                 int start = (Integer)resourceList.get(0);
                 System.out.println(tempNum +(Integer) processList.get(start).getAllocation().get(i) +" <= "+ (Integer)processList.get(start).getMax().get(i));
                 if(tempNum +(Integer) processList.get(start).getAllocation().get(i) <= (Integer)processList.get(start).getMax().get(i)){
                     System.out.println("Success");
                 }else{success = false;}
             }
             
             if(success = false){System.out.println("success = "+success);
             }else{
                 for(int i=0; i<numResourceTypes; i++){
                     System.out.print("boom ");
                 }
             }
     }
     
     public void addProcess(String commandField){
 //        ArrayList tempAllocation = new ArrayList();
         ArrayList tempMax = new ArrayList();
         ArrayList tempNeed = new ArrayList();
             
         //create Allocation list
             for(int i = 2; i<commandField.length()/2+1; i+=2){
                 char s = commandField.charAt(i);
                String str = String.valueOf(s);
                int n = Integer.parseInt(str);
                 tempAllocation.add(n);
             }
             
             //create Max list
             for(int i = commandField.length()/2+2; i<commandField.length(); i+=2){
                 if(commandField.charAt(i)==('A') || commandField.charAt(i)==(' ')){
                     System.out.println("wrong spot");
                 }else{System.out.println("right spot");}
                 char s = commandField.charAt(i);
                 String str = String.valueOf(s);
                 System.out.println("str "+str);
                 int n = Integer.parseInt(str);
                 tempMax.add(n);
             }
             
             //create need list
             for(int i =0; i<tempMax.size(); i++){
                 int x = (Integer)tempAllocation.get(i);
                 int y = (Integer)tempMax.get(i);
                 tempNeed.add(y-x);
             }
              processList.add(new Process(tempAllocation,tempMax, tempNeed));
              
              //remove from available
              for(int i=0; i<numResourceTypes; i++){
                 int x = (Integer)tempAllocation.get(i);
                 System.out.println("x: "+x);
                 int y = (Integer)availableList.get(i);
                 System.out.println("y: "+y);
                 tempNewAvailableList.add(y-x);
              }
              availableList.clear();
              for(int i=0; i<numResourceTypes; i++){
                  availableList.add(tempNewAvailableList.get(i));
                  
              }
              
              System.out.println(processList.get(processList.size()-1).toString());
              System.out.print(availableList.get(0));
              System.out.print(availableList.get(1));
              System.out.print(availableList.get(2));
              
              printHeaders();
         }
     
     public void delete(String commandField){
         System.out.println("delete");
         char s = commandField.charAt(2);
         String str = String.valueOf(s);
         int n = Integer.parseInt(str);
         
         
         for(int i=0; i<numResourceTypes; i++){
                 //ArrayList tempNewAvailableList = new ArrayList();
                 int x = (Integer)processList.get(n).getAllocation().get(i);
                 System.out.println("x: "+x);
                 //System.out.println("pre y: "+availableList.toString());
                 int y = (Integer)availableList.get(i);
                 System.out.println("y: "+y);
                 tempNewAvailableList.add(y+x);
              }
         processList.remove(n);
         availableList.clear();
              for(int i=0; i<numResourceTypes; i++){
                  availableList.add(tempNewAvailableList.get(i));
                  
              }
         
         printHeaders();
         
     }
     /*
     public boolean isLessThanOrEqualTo(){
         ArrayList need = new ArrayList();
         for(int i=0;i<processList.size();i++){
             need = processList.get(i).getNeed();
         }
 
 //        for(int i = 0; i < workList.size(); i++){
 //            if(need.get(i) <= workList.get(i)){
 //                return true;
 //            }
 //            else{
 //                //return false;
 //            }
 //        }
 //        
         return false;
     }*/
     
     public void printHeaders(){
         output.append("Available in the system:\n");
         output.append(tempProcessLabels+"\n");
         output.append(availableList+"\n");
         output.append("Processes\n"
                 + "           Allocation       Max          Need\n");
         for(int i=0;i<processList.size();i++){
             output.append("** "+i+":"+processList.get(i).toString());
         }
     }
     
     
 //    public void testParseAndStoreData(String testBlock){
 //        System.out.println("attempting to parse the test data");
 //        System.out.println(testBlock+"\n\n\n");
 //        Scanner scanner = new Scanner(testBlock);
 //        String tempProcessLabels = scanner.nextLine();
 //        availableVector = Integer.parseInt(scanner.nextLine());
 //        System.out.println("Available in the system:\n");
 //        System.out.println(tempProcessLabels+"\n");
 //        System.out.println(availableVector+"\n");
 //        System.out.println("Processes\n"
 //                + "        Allocation     Max     Need\n");
 //        while(scanner.hasNext()){
 //            String processLine = scanner.nextLine();
 //            int tempAllocation = Integer.parseInt(""+processLine.charAt(0)+processLine.charAt(1)+processLine.charAt(2));
 //            int tempMax = Integer.parseInt(""+processLine.charAt(3)+processLine.charAt(4)+processLine.charAt(5));
 ////            System.out.println("allocation-"+tempAllocation+" | max-"+tempMax+" | need-"+(tempMax-tempAllocation));
 //            processList.add(new Process(tempAllocation,tempMax, tempMax-tempAllocation));
 //        }
 //        for(int i=0;i<processList.size();i++){
 //            System.out.println("** "+i+":"+processList.get(i).toString());
 //        }
 //    }
     
 } //end of OpSystem class
 
 
 /*
  * 
  * 
  */
 
 /*
  * 
  * 
  */
 /*
 import java.awt.TextArea;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class OpSystem {
     private TextArea outputArea;
     private int numResourceTypes=0;
     private ArrayList resourceList = new ArrayList();
     ArrayList tempAllocation = new ArrayList();
     ArrayList tempNewWorkList = new ArrayList();
     ArrayList tempNewAvailableList = new ArrayList();
     private ArrayList<Process> processList = new ArrayList();
     private ArrayList<String> processLabels = new   ArrayList();
     private ArrayList availableList = new ArrayList();
     private String tempProcessLabels, tempAvailable;
     public OpSystem(TextArea outputArea){
         this.outputArea = outputArea;
         
     }
     
     
     public void parseAndStore(String inputArea){
         System.out.println("Start Parse and Store");
         outputArea.append(inputArea+"\n\n");
         
         Scanner scanner = new Scanner(inputArea);
         
         tempProcessLabels = scanner.nextLine();
         outputArea.append("Available in the system:\n");
         outputArea.append(tempProcessLabels+"\n");
         tempAvailable = scanner.nextLine();
         System.out.println("Available length "+ tempAvailable.length());
         numResourceTypes=(tempAvailable.length()+1)/2;
         System.out.println("Resources "+ numResourceTypes);
         for(int i = 0; i<tempAvailable.length(); i+=2){
             char s = tempAvailable.charAt(i);
             String str = String.valueOf(s);
             int n = Integer.parseInt(str);
             availableList.add(n);
         }
         outputArea.append(availableList+"\n");
         
         outputArea.append("Processes\n"
                 + "           Allocation       Max          Need\n");
         
         while(scanner.hasNext()){
             ArrayList tempAllocation = new ArrayList();
             ArrayList tempMax = new ArrayList();
             ArrayList tempNeed = new ArrayList();
             String processLine = scanner.nextLine();
             
             //create allocation list
             for(int i = 0; i<processLine.length()/2; i+=2){
                 char s = processLine.charAt(i);
                 String str = String.valueOf(s);
                 int n = Integer.parseInt(str);
                 tempAllocation.add(n);
             }
             
             //create Max list
             for(int i = processLine.length()/2+1; i<processLine.length(); i+=2){
                 char s = processLine.charAt(i);
                 String str = String.valueOf(s);
                 int n = Integer.parseInt(str);
                 tempMax.add(n);
             }
             
             //create need list
             for(int i =0; i<tempMax.size(); i++){
                 int x = (Integer)tempAllocation.get(i);
                 int y = (Integer)tempMax.get(i);
                 tempNeed.add(y-x);
             }
              processList.add(new Process(tempAllocation,tempMax, tempNeed));
         }
         for(int i=0;i<processList.size();i++){
             outputArea.append("** "+i+":"+processList.get(i).toString());
         }
     }
     public boolean safety(){
         ArrayList workList = new ArrayList();
         boolean allgood = true;
         for(int i = 0; i<availableList.size(); i++){
             int tempNum = 0;
             tempNum = (Integer)availableList.get(i);
             workList.add(tempNum);
         }
         for(int j = 0; j<processList.size(); j++){
             for(int i=0; i<numResourceTypes; i++){
                 if((Integer)processList.get(j).getNeed().get(i) <= (Integer)workList.get(i) && processList.get(j).getIsFinished() == false){ 
                                 System.out.println(processList.get(j)+" good");
                             }else{
                                 allgood = false;
                             }
                 
             }
             if(allgood == true){
                 for(int i=0; i<numResourceTypes; i++){
                 if((Integer)processList.get(j).getNeed().get(i) <= (Integer)workList.get(i) && processList.get(j).getIsFinished() == false){ 
                     for(int k=0; k<numResourceTypes; k++){
                         int x = (Integer)tempAllocation.get(i);
                         System.out.println("x: "+x);
                         int y = (Integer)workList.get(i);
                         System.out.println("y: "+y);
                         tempNewWorkList.add(y+x);
                         
                         if(j == processList.size()-1){
                         j =-1;
                         }
                 }  
                 }
                 }
                 workList.clear();
                 
                 for(int i=0; i<numResourceTypes; i++){
                  workList.add(tempNewWorkList.get(i));
                  
              }
                 
             }
             
             
         }
         
         for(int i=0; i<numResourceTypes; i++){
         if(processList.get(i).getIsFinished() == false){return false;}
         }
         return true;
     }
     
     
     public void resourceRequest(String commandField){
         boolean success = true;
         System.out.println("Resource Request");
         System.out.println("Command Field "+ commandField);
         System.out.println("Char at 3:"+ commandField.charAt(2));
 //        ArrayList resourceList;
 //        resourceList = new ArrayList();
         for(int i=2; i<commandField.length(); i+=2){
             char s = commandField.charAt(i);
             String str = String.valueOf(s);
             int n = Integer.parseInt(str);
             resourceList.add(n);
         }
             int tempNum = (Integer)resourceList.get(0);
             System.out.println(tempNum);
             
             for(int i=0; i<numResourceTypes; i++){
                 tempNum = (Integer)resourceList.get(i+1);
                 int start = (Integer)resourceList.get(0);
                 System.out.println(tempNum +(Integer) processList.get(start).getAllocation().get(i) +" <= "+ (Integer)processList.get(start).getMax().get(i));
                 if(tempNum +(Integer) processList.get(start).getAllocation().get(i) <= (Integer)processList.get(start).getMax().get(i)){
                     System.out.println("Success");
                 }else{success = false;}
             }
             
             if(success = false){System.out.println("success = "+success);
             }else{
                 for(int i=0; i<numResourceTypes; i++){
                     System.out.print("boom ");
                 }
             }
     }
     
     public void addProcess(String commandField){
 //        ArrayList tempAllocation = new ArrayList();
         ArrayList tempMax = new ArrayList();
         ArrayList tempNeed = new ArrayList();
             
         //create Allocation list
             for(int i = 2; i<commandField.length()/2+1; i+=2){
                 char s = commandField.charAt(i);
                 String str = String.valueOf(s);
                 int n = Integer.parseInt(str);
                 tempAllocation.add(n);
             }
             
             //create Max list
             for(int i = commandField.length()/2+2; i<commandField.length(); i+=2){
                 if(commandField.charAt(i)==('A') || commandField.charAt(i)==(' ')){
                     System.out.println("wrong spot");
                 }else{System.out.println("right spot");}
                 char s = commandField.charAt(i);
                 String str = String.valueOf(s);
                 System.out.println("str "+str);
                 int n = Integer.parseInt(str);
                 tempMax.add(n);
             }
             
             //create need list
             for(int i =0; i<tempMax.size(); i++){
                 int x = (Integer)tempAllocation.get(i);
                 int y = (Integer)tempMax.get(i);
                 tempNeed.add(y-x);
             }
              processList.add(new Process(tempAllocation,tempMax, tempNeed));
              
              //remove from available
              for(int i=0; i<numResourceTypes; i++){
                 int x = (Integer)tempAllocation.get(i);
                 System.out.println("x: "+x);
                 int y = (Integer)availableList.get(i);
                 System.out.println("y: "+y);
                 tempNewAvailableList.add(y-x);
              }
              availableList.clear();
              for(int i=0; i<numResourceTypes; i++){
                  availableList.add(tempNewAvailableList.get(i));
                  
              }
              
              System.out.println(processList.get(processList.size()-1).toString());
              System.out.print(availableList.get(0));
              System.out.print(availableList.get(1));
              System.out.print(availableList.get(2));
              
              printHeaders();
         }
     
     public void delete(String commandField){
         System.out.println("delete");
         char s = commandField.charAt(2);
         String str = String.valueOf(s);
         int n = Integer.parseInt(str);
         
         
         for(int i=0; i<numResourceTypes; i++){
                 //ArrayList tempNewAvailableList = new ArrayList();
                 int x = (Integer)processList.get(n).getAllocation().get(i);
                 System.out.println("x: "+x);
                 //System.out.println("pre y: "+availableList.toString());
                 int y = (Integer)availableList.get(i);
                 System.out.println("y: "+y);
                 tempNewAvailableList.add(y+x);
              }
         processList.remove(n);
         availableList.clear();
              for(int i=0; i<numResourceTypes; i++){
                  availableList.add(tempNewAvailableList.get(i));
                  
              }
         
         printHeaders();
         
     }
     
     public void printHeaders(){
         outputArea.append("Available in the system:\n");
         outputArea.append(tempProcessLabels+"\n");
         outputArea.append(availableList+"\n");
         outputArea.append("Processes\n"
                 + "           Allocation       Max          Need\n");
         for(int i=0;i<processList.size();i++){
             outputArea.append("** "+i+":"+processList.get(i).toString());
         }
     }
         
     }*/
 
 /*
  * public class OpSystem {
     //private data.
     private boolean error;
     private TextArea input, output;
     private int numberOfProcesses, numberOfTypes;
     private ArrayList<Process> processes;
     private ArrayList<Integer> availableResources;
     private ArrayList resourceTypes;
     //boolean isSafe = false;
     
     //OpSystem constructor.
     public OpSystem(TextArea input, TextArea output){
         this.input = input;
         this.output = output;
     }
        
     //Method that is called when the read data button is clicked.
     public void readData(){  
         try{
         Scanner sc = new Scanner(input.getText());
         try{
             numberOfProcesses = Integer.parseInt(sc.nextLine());
         }
         catch(Exception e){
             System.out.println("error parsing int");
             output.append("\n Error parsing int");
             error = true;
         }
         
         try{
             String temp = sc.nextLine();
             Scanner scanLabel = new Scanner(temp);
             resourceTypes = new ArrayList();
             while(scanLabel.hasNext()){
                 resourceTypes.add(scanLabel.next().trim());
             }
             numberOfTypes = resourceTypes.size();
             output.append("\n NumTypes: "+numberOfTypes);
         }
                 
         catch(Exception e){
             System.out.println("catch of parsing labels");
             output.append("\nError processing labels");
             error = true;
         }
         
         try{
             String tmp =sc.nextLine();
             Scanner labelValuesScanner = new Scanner(tmp);
             availableResources = new ArrayList<Integer>();
             for(int i = 0 ; i<numberOfTypes; i++){
                 availableResources.add(Integer.parseInt(labelValuesScanner.next().trim()));
             }
         }
         catch(Exception e){
             System.out.println("Error getting label values");
             output.append("\nError parsing integer labels");
             error = true;
         }
         
         try{
             Scanner processLine = sc;
             processes = new ArrayList<Process>(numberOfProcesses);
             while(processLine.hasNext()){
                 processes.add(new Process(processLine.nextLine(),numberOfTypes));
             }
         }
                 
         catch(Exception e){
             System.out.println("Error getting string passed to Process");
             output.append("\nError while constructing the process");
             error = true;
             }
         printToOutput();     
         }
         catch(Exception e){
             System.out.println("Read data error");
         }
     }
     
     //Method that is called when the check data button is clicked.
     public void checkData(){
         try{
         output.append("\nAvailable\n");
         for(int i=0; i< resourceTypes.size(); i++){
             output.append(resourceTypes.get(i) + " ");
         }
         
         ArrayList<Integer> tempAvailable = new ArrayList<Integer>(numberOfTypes);
         for(int i=0; i< numberOfTypes ; i++){
             tempAvailable.add(availableResources.get(i));  
         }
         output.append("\n");
         for(int i=0; i< tempAvailable.size(); i++){
             output.append(tempAvailable.get(i)+ " ");
             //output.append("\n");
         }
 
         
         printJustProcesses();
         seeIfSystemIsSafe();
         }
         catch(Exception e){
             System.out.println("Checking system error");
         }
     }
     
     //Method that checks to see if the system is safe.
     public void seeIfSystemIsSafe(){
         ArrayList<Integer> tempAvailableResources = new ArrayList<Integer>(numberOfTypes);
         for(int i =0; i< availableResources.size(); i++)
             tempAvailableResources.add(availableResources.get(i));
         ArrayList<Integer> tempNeed = new ArrayList<Integer>();
         if(!error)
         {
             for(int i = 0; i < processes.size(); i++){
                 if(processes.get(i).getIsFinished()== false){
                     tempNeed = processes.get(i).getNeed();
 
                     for(int j = 0; j<numberOfTypes; j++){
                        if(tempNeed.get(j) > tempAvailableResources.get(j)){
                            continue;
                        }
                                
                        else{
                            for(int k=0; k < numberOfTypes; k++){
                                int total = tempAvailableResources.get(k) + tempNeed.get(k);
                                tempAvailableResources.set(k, total);
                            }
                            
                            processes.get(i).setFinish(true);
                            i=0;
                        }       
                     }
                 }
             }
             
             boolean isSafe = false;
             for(int i=0; i< processes.size(); i++){
                 if(processes.get(i).getIsFinished() == false){
                     isSafe = false;
                     break;
                 }
                 else
                     isSafe = true;
             }
             if(isSafe == true){
                 output.append("\n\tThe system is currently safe.");
             }
             else{
                 output.append("\n\tThe system is not currently safe.");
             }
         }
     }
    
     //This is the method that is called when the resource request button is clicked.
     public void resourceRequest(String s){
         try{
         ArrayList<Integer> resourceRequestArray = new ArrayList<Integer>(numberOfTypes + 1);
         try{
             Scanner sc = new Scanner(s);
             for(int i=0; i < numberOfTypes + 1; i++){
                 resourceRequestArray.add(Integer.parseInt(sc.next()));
             }  
             output.append("\n\n resources requested  --  " + s+"\n");
         }
         catch(Exception e){
             System.out.println("Resourse request parsing error");
         }
         
         output.append("\nAvailable\n");
         for(int i=0; i< resourceTypes.size(); i++){
             output.append(resourceTypes.get(i) + " ");
         }
        
         ArrayList<Integer> tempAvailable = new ArrayList<Integer>(numberOfTypes);
         for(int i=0; i< numberOfTypes ; i++){
             tempAvailable.add(availableResources.get(i));  
         }
         output.append("\n");
         for(int i=0; i< tempAvailable.size(); i++){
             //appends the amount of resources available after the requested was subtracted from the available.
             output.append(tempAvailable.get(i) - resourceRequestArray.get(i+1)+ " ");
         }
        
         int processNum = resourceRequestArray.get(0);
         ArrayList<Integer> tempAllocation = new ArrayList<Integer>();
         tempAllocation = processes.get(processNum).getAllocation();
         boolean continueTo2 = false;
         boolean continueTo3 = false;
         for(int i = 0; i < numberOfTypes; i++)
        {
             if(tempAllocation.get(i) <= resourceRequestArray.get(i)){
                 continueTo2 = true;
             }
             else{
                 continueTo2 = false;
                 output.append("\nThe Process has exceeded its originally set maximum.");
                 break;
             }
         }
         if(continueTo2 == true){
             for(int i =0; i< numberOfTypes; i++){
                 if(resourceRequestArray.get(i+1) <= tempAvailable.get(i) ){
                     continueTo3 = true;
                 }
                 else{
                     continueTo3 = false;
                     output.append("\nNeeded resources are not yet available.");
                     break;
                 }          
             }
         }
        
         if(continueTo3 == true){
             for(int i = 0; i < numberOfTypes; i++){
                
                 int newAvail = (tempAvailable.get(i) - resourceRequestArray.get(i+1));
                 tempAvailable.set(i,newAvail);
                 int newallocation = (tempAllocation.get(i) + resourceRequestArray.get(i+1));
                 tempAllocation.set(i,newallocation);
             }
             processes.get(processNum).setAllocation(tempAllocation);
             processes.get(processNum).fillNeed();
             printJustProcesses();
             seeIfSystemIsSafe();
             }
         }
         catch(Exception e){
             System.out.println("Resourse request error");
         }
     }
     
     //Prints everything to output. Calls in the read data method.
     public void printToOutput(){  
         output.setText(null);
         output.append("\n\n\nNumber of processes = " + numberOfProcesses + " \n");
         output.append("Process labels =");
         for(int i = 0; i < resourceTypes.size(); i++){
             output.append(" " + resourceTypes.get(i).toString());
         }
        
         output.append("\nNumTypes = "+numberOfTypes);
         
         output.append("\nAvailable vector =");
         for(int i = 0; i <availableResources.size(); i++){
             output.append(" "+ availableResources.get(i).toString());
         }
        
         for(int i = 0; i < numberOfProcesses; i++){
             output.append("\nProcess "+i+ " = " + processes.get(i).returnProcesses());
         } 
         
         output.append("\n\n\nall data successfully read and initialized");
     }
    
     //This prints just the processes.
     private void printJustProcesses(){
         output.append("\n \nProcesses ");
         output.append("\n     Allocation \t                Max:                  Need:  \n");
         for(int i = 0; i< processes.size(); i++){
             output.append(i+")");
             output.append(processes.get(i).returnNeed()+ "\n");
         }
     }
         
 }
 
  */
