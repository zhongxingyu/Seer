 package functions;
 
 import backEnd.Turtle;
 
 public class Left extends TurtleFunction {
     
     public Left (Turtle turtle) {
         super(turtle);
     }
    
     @Override

     public void execute(String input) {
     	String[] args = input.split("\\s+");
     	int myAngle = Integer.parseInt(args[1]);
         getTurtle().setHeading(myAngle);
     }
 
 

    @Override
    public String getOutput (String input) {
        return "";
    }

 }
 
