 package functions;
 
 import backEnd.Model;
 
 public class Repeat extends Function{
     private Model myModel;
     
 
     public Repeat(Model model) {
         myModel = model;
 	}
     
     @Override
     public void execute(String input) {
     	myModel.processString(input);
     }
     
     
     public String getOutput(String input){
        	String[] args = input.split("\\s+");
     	int newReps = Integer.parseInt(args[1]) - 1;
     	if(newReps == 0){
     		return "";
     	}
     	args[1] = newReps + "";
     	String output = "";
     	for(int i = 0; i < args.length ; i++){
     		output += args[i];
     	}
     	return output;
     }
     
 
 }
