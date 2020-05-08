 package spaceinvaders;
 
 import java.util.HashMap;
 import java.util.Stack;
 /**
  *
  * @author Edmund
  */
 public class Thread {
     //Base scriptID, the one which is on the top and its "main".
     public final int baseScriptID;
     
     //Which script are we running, and what line are we on?
     private int scriptID;
     private int currentLineNumber;
     
     //Whether the Scriptable is in the middle of the indicated command
     private boolean inProgress;
     
     //Holds the memory
     //For Threads tied with Entities, set it equal to the Entity's 
     //memoryBox so that memory can be shared between Threads.
     //For Threads not tied with Entities, set it to an individual memoryBox
     private HashMap<String, Parameter> memoryBox;
     private HashMap<String, Parameter> temporaryVariables;
     
     //This is the mannequin which the Thread object holds onto
     private Scriptable linkedScriptable;
     
     //Is this thread ready to die?
     private boolean markedForDeletion;
     
     //For waiting, which totally SHOULD be a thread function
     private double waitMilliseconds;
     
     //Identifier, the name of which basically allows us to find the thread
     //and kill it or something. 
     private String name;
     
     //For jumping back to whence we came 
     //private
     private Stack functionStack;
     
     //Keep track of how many functions deep we are
     private int functionLayer;
     
     private String[] functionReturns;
     
     
     
     
     public Thread(int scriptID) 
     {
         markedForDeletion = false;
         
         setScriptID(scriptID);
         baseScriptID = scriptID;
         
         memoryBox = new HashMap<String, Parameter>();
         
         
         functionStack = new Stack();
         functionLayer = 0;
     }
     
     
     
     public void markForDeletion() 
     { 
         markedForDeletion = true;
     }
     
     public boolean isMarkedForDeletion()
     {
         return markedForDeletion;
     }
     
     //Accessors and mutators for the mannequin Scriptable
     public void setScriptable(Scriptable scriptableObj)
     {
         linkedScriptable = scriptableObj;
     }
    
     public Scriptable getScriptable()
     {
         return linkedScriptable;
     }   
     
     
     //Accessors and mutators for the line number on the script
     public void setLineNumber(int newLineNumber)
     {
         currentLineNumber = newLineNumber;
     }
    
     public int getCurrentLine()
     {
         return currentLineNumber;
     }
     
     //Accessors and mutators for ScriptID
     protected void setScriptID(int newScriptID)
     {
         scriptID = newScriptID;
     }
    
     public int getScriptID()
     {
         return scriptID;
     }
     
     //Running state, or basically, is the script in the middle of executing
     //some command
     public boolean isRunning()
     {
         return inProgress;
     }
     
     protected void setRunningState(boolean progress)
     {
         inProgress = progress;
     }
     
     //Accessors and mutators for the name
     public void setName(String newName)
     {
         name = newName;
     }
    
     //maybe make a "base thread" name
     public String getName()
     {
         return name;
     }
     
     //Waiting
     public void beginWait(double millisecondsToWait)
     {
         waitMilliseconds = millisecondsToWait;
         setRunningState(true);
     }
     
     public boolean continueWait(double delta)
     {
         //Update the temporary value with the delta time
         waitMilliseconds -= delta;
         
         if (waitMilliseconds < 0)
         {
             //Oh, so we're done waiting. Great.
             //System.out.println("Finished waiting!");
             setRunningState(false);
             return false;
         }
         
         //Alright, we still have milliseconds left to wait. Keep going
         return true;
     }
     
     //Memory/variable magic
     public void setVariable(String identifier, Parameter value) 
     {
        //Scripter, it's YOUR FAULT if it crashes because you fail to
        //initialize the variable! LOL
        
         //Basically, here's the rule. When we're in a function, all variables
         //end up being LOCAL. 
         if (functionLayer > 0)
         {
            temporaryVariables.put(identifier, null);
         }
         else
         {
             //Otherwise, place the variable in the main memory
            memoryBox.put(identifier, null);
         }
     }
     
     public void newVariable(String identifier) 
     {
         //Declaring a new Variable without a value yet...
         //That's just using setVariable, except with a null.
         setVariable(identifier, null);
     }
     
     public Parameter getVariable(String identifier) 
     {
         //When the caller wants to get the Parameter referred to by identifier,
         //we don't know if it's located in temporary memory or main memory.
         //Hence, just look for it in temporary first (if applicable)
         
         if (functionLayer > 0)
         {
             Parameter tryTempMemory = temporaryVariables.get(identifier);
             
             //NOTICE how local variables shadow the instance fields in main.
             if (tryTempMemory != null) {
                 //Ah, we found it. Return it.
                 return tryTempMemory;
             }
             else
             {
                 //It must be in main memory
                 return memoryBox.get(identifier);
             }
         }
         else 
         {
             //We're on the main, so don't bother with temporary memory
             return memoryBox.get(identifier);
         }
     }
     
     public void deleteVariable(String identifier)
     {
         memoryBox.remove(identifier);
     }
     
     public HashMap<String, Parameter> getMemoryBox()
     {
         return memoryBox;
     }
     
     public void setMemoryBox(HashMap<String, Parameter> newMemoryBox)
     {
         memoryBox = newMemoryBox;
     }
     
     public void setLocalMemoryBox(HashMap<String, Parameter> newTempVariables)
     {
         temporaryVariables = newTempVariables;
     }
 
     
     
     //Stack stuff! 
     public void makeReturnPoint()
     {
         //System.out.println("We are a creating a return point: " 
         //  + "at scriptID " + getScriptID() + ". " + 
         //  "The line we're saving is " + getCurrentLine() + 
         //  ".");
         returnPoint foo = new returnPoint(getScriptID(), 
                 getCurrentLine(), 
                 memoryBox,
                 temporaryVariables);
         
         functionStack.push(foo);
         
         //We need to go deeper
         functionLayer++;
     }
     
     public void restoreLastReturnPoint()
     {
         //Get the last save return point.
         returnPoint foo = (returnPoint)functionStack.pop();
         
         //Make sure you go to the line and ID which we left off...
         setScriptID(foo.getScriptID());
         setLineNumber(foo.getLastLine());
         
         //Retrieve the main memory and function memory which we left off...
         memoryBox = foo.getMemoryBox();
         temporaryVariables = foo.getTemporaryMemoryBox();
             
         //System.out.println(getName() + " is the name of the"
         //+ "current thread layer now! The one we jump back to...");
             
         //Non, je ne regrette rien
         functionLayer--;
     }
     
     public void setFunctionReturns(String[] toReturn)
     {
         functionReturns = toReturn;
     }
     
     public String[] getFunctionReturns()
     {
         return functionReturns;
     }
     
     private class returnPoint
     {
         private int scriptID;
         private int lastLine;
         private HashMap<String, Parameter> memoryBox;
         private HashMap<String, Parameter> temporaryVariables;
                 
         returnPoint(int newScriptID, int newCurrentLine, 
                 HashMap<String, Parameter> lastMemoryBox, 
                 HashMap<String, Parameter> lastTemporaryVarBox)
         {
             scriptID = newScriptID;
             lastLine = newCurrentLine;
             memoryBox = lastMemoryBox;
             temporaryVariables = lastTemporaryVarBox;
         }
         
         int getScriptID()
         {
             return scriptID;
         }
                 
         int getLastLine()
         {
             return lastLine;
         }
         
         HashMap<String, Parameter> getMemoryBox()
         {
             return memoryBox;
         }
         
         HashMap<String, Parameter> getTemporaryMemoryBox()
         {
             return temporaryVariables;
         }
         
     }
 }
