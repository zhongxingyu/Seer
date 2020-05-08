 import java.util.*;
 
 public class Proof {
 
 	// need variable to hold a theoremSet
 	// holds line number object showing current position
 	// setAssumption
 
 	//PROBLEM: ~q not stored correctly
 	//suggestion still not quite sure how parser works, but does the parser remember to skip ahead if it encounters '~'?
 	//appears not to right now
 	
 	
 	/*
 		General To-Do
 			
 			-Add calls to checkLineScope where appropriate
 			
 			-Add support for & and |
 			
 			-update line checker
 		
 			-check proper protection status for variables and methods
 			-Write a inference checker for:
 				//We need a hashtable for this expression in the global scope
 				-User defined expression (takes in two clones: predefined expression, input expression)
 					-Recursive Function that deconstructs both the pre-defined queue and the
 					input-queue and when an operand is found, the input-function operand is assigned
 					to the pre-defined fuction in a hashtable which is used to check for consistancy
 					with every subsequent comparison.
 					
 					pop a value off both and store them
 					
 					if both stored values are operators:
 						if the operators are equivilent
 							call recursively the function on both sides and return only if both'
 							are true
 						else return the false
 						
 					if the stored values are operands:
 						if the values are not in the hashtable
 							add to the hashtable using predefined expression value as the key
 							and the input expression as the vlaue
 						if the operand comparison is inconsistant with the hashtable 
 							return false	
 					return true
 		
 				//Clear the table
 
 				-Contradiction
 				-
 			-
 			-
 			-
 			-
 			-
 			-
 			-
 	*/
 
 	private TheoremSet myTheoremSet;
 	private LineNumber myLineNumber;
 	private Hashtable<String,LinkedList<String>> showTable;
 	private ArrayList<String> printList;
 	private Hashtable<String,String> operands;
 
 	public Proof (TheoremSet theorems) {
 		myTheoremSet=theorems;
 		myLineNumber=new LineNumber();
 		showTable=new Hashtable<String,LinkedList<String>>();
 		printList=new ArrayList<String>();
 		operands = new Hashtable<String,String>();
 		//takes in expression stuff from Theorems
 	}
 
 	public LineNumber nextLineNumber () {
 		//makes a new call on LineNumber based on arg x
 		return myLineNumber;
 	}
 
 	public void changeLineNumber(String x){
 
 	}
 
 	public void extendProof (String x) throws IllegalLineException, IllegalInferenceException{
 
 		//checks x for Line Errors, then throws if error
 		//calls expression on x, gets a tree object Expression y
 		//checks tree object expression y for inference errors and throws again
 		//steps line number forward one  by calling appropriate method in LineNumber  
 		//based on what expression y was called
 		//adds tree object to proofs theorem set with name lineNumber and expression y
 
 		//check x for Line Errors
 		String[] statement;
 		try{
 			statement = StringSplitter(x);
 			LineChecker(statement);
 			reasonDelagation(statement);
 		}catch (IllegalLineException e){
 			throw e;
 		}catch (IllegalInferenceException e){
 			throw e;
 		}
 	}
 
 	public String toString ( ) {
 		//not sure where this is used?
 		return "";
 	}
 
 	public boolean isComplete ( ) {
 		//stops looping in Proofchecker by returning true
 		//returns true when expression in line = expression inside
 		//return false;
 		System.out.println(myTheoremSet.myTheorems.toString());
 		return showTable.isEmpty();
 	}
 
 	//change name
 	public boolean inferenceChecker(ArrayList<LinkedList<String>> Queues)
 	{
 		//takes in any number of queues in an ArrayList and returns whether or not together they're
 		//valid inferences
 		
 		//Catches Empty Booleans
 		try
 		{
 		
 			//Iterate Over the Tree Expressions from Molles Tollens
 			for(LinkedList curQueue : Queues)
 			{
 				
 				//curExpr is a expression to be evaluated
 				if(curQueue.size() > 2)
 				{
 					//Gets Next Symbol
 					String curSymbol = (String) curQueue.pop();
 	
 					//Checks Implies
 					if(curSymbol.equals("=>"))
 					{
 						boolean leftBool = inferenceCheckerHelper(curQueue,Queues);
 						boolean rightBool = inferenceCheckerHelper(curQueue,Queues);
 						return implies(leftBool,rightBool);
 					}
 					if(curSymbol.equals("~"))
 					{
 						boolean restBool = inferenceCheckerHelper(curQueue,Queues);
 						return !restBool;
 					}
 				}
 			}
 			return false;
 		}
 		catch (IllegalInferenceException e)
 		{
 			return false;
 		}
 	}
 
 	public boolean inferenceCheckerHelper(LinkedList<String> curQueue,ArrayList<LinkedList<String>> Queues) throws IllegalInferenceException
 	{
 		String curSymbol = (String) curQueue.pop();
 
 		if(curSymbol.equals("=>"))
 		{
 			boolean bol1 = inferenceCheckerHelper(curQueue,Queues);
 			boolean bol2 = inferenceCheckerHelper(curQueue,Queues);
 			return implies(bol1,bol2);
 		}
 		
 		if(curSymbol.equals("~"))
 		{
 			boolean restBool = inferenceCheckerHelper(curQueue,Queues);
 			return !restBool;
 		}
 		
 		for(LinkedList<String> Q : Queues)
 		{
 			if(Q.size() == 1)
 			{
 				if(curSymbol.equals(Q.getFirst()))
 				{
 					return true;
 				}
 			}
 			
 			if(Q.size()==2)
 			{
 				if(curSymbol.equals(Q.get(1)))
 				{
 					return false;
 				}
 			}
 		}
 		
 		throw new IllegalInferenceException("Bad");
 	}
 
 	public static String[] StringSplitter(String x) throws IllegalLineException{
 		String[] rtn=x.split(" ");
 		if ((rtn.length<1)||rtn.length>4){
 			throw new IllegalLineException("***Too many Arguments:" + x);
 		}else for(int i=0;i<rtn.length;i++){
 			if (rtn[i]==null){
 				throw new IllegalLineException("***Use only one space between Arguments" + x);
 			}
 		}
 		return rtn;
 	}
 
 	public void reasonDelagation(String[] args) throws IllegalInferenceException
 	{
 		String command = args[0];
 		//System.out.println(args[0]);
 		//System.out.println(args[1]);
 
 		/*
 		Todo:
 			-
 		
 			-delegation for user defined theorems.
 				-Find what matches between whats in the proof and whats in the user defined theorem
 				-
 				-	
 				-
 		
 		
 		*/
 		if (command.equals("theorem"))
 		{
 			//add the argument to the theoremset with the key of the theorem name
 			//what if theorem is first, will that ruin show code?
 			this.storeprint(args);
 			this.myTheoremSet.put(args[1], new Expression(args[2]));
 		}
 		if (command.equals("show"))
 		{
 			Expression temp = new Expression(args[1]);
 			//Wrong yo, you can show a    (assuming you mean a blank expression, that can't be entered into the parser, would get trimmed)
 			//if (!temp.Queue.peek().equals("=>")){
 			//	throw new IllegalInferenceException("Expression must include =>: "+ args[1]);
 			//}
 
 			System.out.println(temp.Queue);
 			this.storeprint(args);
 			this.showTable.put(myLineNumber.toString(), temp.Queue);
 
 			/*
 			To do: 
 				-convert show to queue (with expression, which takes string) (DONE)
 				-Show must check that the value at the top of the queue is an implication
 				-store the converted show queue in a hashtable of shows (line number, queue)
 			*/
 			if(ProofChecker.iAmDebugging){
 				System.out.println(myLineNumber);
 				System.out.println(showTable.get(myLineNumber.toString()));
 				System.out.println((myTheoremSet.myTheorems.isEmpty()));
 			}
 			if(myLineNumber.toString().equals("1"))
 			{
 				myLineNumber.step();
 			}
 			else
 			{
 				myLineNumber.layerMinus();
 			}
 
 		}
 		if (command.equals("assume"))
 		{
 			if(!myLineNumber.toString().equals("2"))
 			{
 				if (!myLineNumber.readyAssume())
 				{
 					throw new IllegalInferenceException("Must Use assume After a show");
 				}
 			}
 			
 			LinkedList<String> temp = showTable.get(myLineNumber.currentSuper());
 			System.out.println(myLineNumber.currentSuper());
 
 			if (temp==null)
 			{
 				throw new IllegalInferenceException("Show was not made at LineNumber: "+temp);
 			}
 			else if(temp.toString().equals("[~, "+(new Expression(args[1])).Queue.toString().substring(1))
 					||("[~, "+temp.toString().substring(1)).equals((new Expression(args[1])).Queue.toString()))
 			{
 				myTheoremSet.put(myLineNumber.toString(), new Expression(args[1]));
 				myLineNumber.step();
 				return;
 			}
 			else if (!findAssumption(temp).toString().equals((new Expression(args[1])).Queue.toString()))
 			{
 				throw new IllegalInferenceException("Can Only Assume Left Side of => or ~ of Show: "+ args[1]);
 			}
 			/*
 			To do: 
 				-if current line number = 2 then continue (Done)
 				-else check that the last element in the line number array = 1
 				  Deprecated: (current line number, without last 2 chars = hashtable (key) points to the show expression currently being proved)
 				-else throw hands up theyre playing my song
 				-add to theoremset (done)
 			*/
 			this.storeprint(args);
 			myTheoremSet.put(myLineNumber.toString(), new Expression(args[1]));
 			myLineNumber.step();
 		}
 		if (command.equals("mp"))
 		{
 			/*
 			 * assume (a=>b)
 			 * assume ((a=>b)=>(b=>c))
 			 * mp 1 2 (b=>c)
 			 * 
 			 * */
 
 			if(mpChecker((LinkedList<String>) myTheoremSet.get(args[1]).clone(),
 							(LinkedList<String>) myTheoremSet.get(args[2]).clone(),
 							(LinkedList<String>) new Expression(args[3]).Queue.clone()))
 			{
 				this.storeprint(args);
 				this.myTheoremSet.put(this.myLineNumber.toString(), new Expression(args[3]));
 				myLineNumber.step();
 			}
 			else
 			{
 				throw new IllegalInferenceException("Invalid Inference");
 			}
 		}
 		if (command.equals("mt"))
 		{
 			
 			if(mtChecker((LinkedList<String>) myTheoremSet.get(args[1]).clone(),
 						(LinkedList<String>) myTheoremSet.get(args[2]).clone(),
 						(LinkedList<String>) new Expression(args[3]).Queue.clone()))
 			{
 				this.storeprint(args);
 				this.myTheoremSet.put(this.myLineNumber.toString(), new Expression(args[3]));
 				myLineNumber.step();
 			}
 			else
 			{
 				throw new IllegalInferenceException("Invalid Inference");
 			}
 		}
 		if (command.equals("co"))
 		{
 			if (contradiction((LinkedList<String>)this.myTheoremSet.get(args[1]).clone(), 
 							(LinkedList<String>)this.myTheoremSet.get(args[2]).clone()))
 			{
 				this.storeprint(args);
 				showTable.remove(myLineNumber.currentSuper());
 				this.myTheoremSet.put(myLineNumber.currentSuper(),new Expression(args[3]));
 				if(showTable.size()!=0)
 				{
 					myLineNumber.layerUp();
 				}
 			}
 			else
 			{
 				throw new IllegalInferenceException("Invalid Inference");
 			}
 
 		}
 		if (command.equals("ic"))
 		{
 			LinkedList<String> proveExpr = myTheoremSet.get(args[1]);
 			LinkedList<String> showExpr = (new Expression(args[2])).Queue;
 			if(showTable.get(myLineNumber.currentSuper()).toString().equals(showExpr.toString()))
 			{
 				if(findConsequent(showExpr).toString().equals(proveExpr.toString()))
 				{
 					this.storeprint(args);
 					showTable.remove(myLineNumber.currentSuper());
 					myTheoremSet.put(myLineNumber.currentSuper(), showExpr);
 					if(showTable.size()!=0)
 					{
 						myLineNumber.layerUp();
 					}
 				}
 				else
 				{
 					throw new IllegalInferenceException("Invalid Inference");
 				}
 			}
 			else
 			{
 				throw new IllegalInferenceException("Invalid Inference");
 			}
 		}
 		if (command.equals("repeat"))
 		{
 			LinkedList<String> proveExpr = showTable.get(myLineNumber.currentSuper());
 			LinkedList<String> showExpr = (new Expression(args[2])).Queue;
 			if (!proveExpr.equals(showExpr)){
 				throw new IllegalInferenceException("Repeated expression doesn't match unproven show:"+ args[2]);
 			}
 			showTable.remove(myLineNumber.currentSuper());
 			myTheoremSet.put(myLineNumber.currentSuper(), showExpr);
 			this.storeprint(args);
 			myTheoremSet.put(myLineNumber.toString(), myTheoremSet.myTheorems.get(args[1]));
 			myLineNumber.step();
 		}
 		if (command.equals("print"))
 		{
 			for (int i=0;i<printList.size();i++){
 				System.out.println(printList.get(i));
 			}
 		}
 		else if (this.myTheoremSet.get(command) != null)
 		{
 			if (checkTheoremEquivalence((LinkedList<String>) myTheoremSet.get(command).clone(), new Expression(args[1]).Queue))
 			{
 				this.myTheoremSet.put(this.myLineNumber.toString(), new Expression(args[1]));
 				this.myLineNumber.step();
 			}
 			else
 			{
 				throw new IllegalInferenceException(
 						"***Invalid Inference, the provided theorem " +
 						"is not equivalent to stored theorem of same name :" + command);
 			}
 		}
 	}
 	
 	public void storeprint(String[] args){
 		//adds to printList
 		String output= myLineNumber.toString();
 		for (int i = 0;i<args.length;i++){
 			output+=" "+args[i];
 		}
 		this.printList.add(output);
 	}
 
 	private boolean mpChecker(LinkedList<String>left,LinkedList<String>middle, LinkedList<String>consequent)
 	{
 		/*
 		 * assume (a=>b)
 		 * assume ((a=>b)=>(b=>c))
 		 * mp 1 2 (b=>c)
 		 * 
 		 * */
 		
 		LinkedList<String> fullExpression;
 		LinkedList<String> predicate;
 
 
 		if (left.size() > middle.size())
 		{
 			fullExpression = left;
 			predicate = middle;
 		}
 		else
 		{
 			fullExpression = middle;
 			predicate = left;
 		}
 
 		//System.out.println(fullExpression.toArray().toString());
 		//System.out.println(predicate.toArray().toString());
 
 		if (fullExpression.pop().equals("=>"))
 		{
 			String fullBuff = "";
 
 			//check predicate matches left side of full expression
 			for(int i=0; i < predicate.size();i++)
 			{
 				try
 				{
 					fullBuff = fullExpression.pop();
 					assert predicate.pop().equals(fullBuff);
 				}
 				catch (Exception e)
 				{
 					return false;
 				}
 			}
 
 			//check consequent matches right side of full expression
 			for(int i=0; i < consequent.size();i++)
 			{
 				try
 				{
 					fullBuff = fullExpression.pop();
 					assert consequent.pop().equals(fullBuff);
 				}
 				catch (Exception e)
 				{
 					return false;
 				}
 			}
 
 			try
 			{
 				assert fullExpression.size()==0;
 				return true;
 			}
 			catch (Exception e)
 			{
 				return false;
 			}
 			
 		}
 		return false;
 		
 	}
 
 	private boolean mtChecker(LinkedList<String>left,LinkedList<String>middle, LinkedList<String>consequent)
 	{
 		
 		/*
 		 * assume ~~~c
 		 * assume ~~(b=>c)
 		 * mt 1 2 ~~~b
 		 * 
 		 * */
 		
 		/*
 		 * Potential Problems:
 		 * 	- c
 		 * 	- ~(b=>c)
 		 *  - ~b
 		 *  
 		 *  assert size at bottom of method might break things
 		 *  
 		 * */
 		
 		filterTildas(left);
 		filterTildas(middle);
 		filterTildas(consequent);
 		
 		/*
 		System.out.println(left.toString());
 		System.out.println(middle.toString());
 		System.out.println(consequent.toString());
 		*/
 		
 		LinkedList<String> fullExpression;
 		LinkedList<String> predicate;
 
 		if (left.size() > middle.size())
 		{
 			fullExpression = left;
 			predicate = middle;
 		}
 		else
 		{
 			fullExpression = middle;
 			predicate = left;
 		}
 
 		//System.out.println(fullExpression.toArray().toString());
 		//System.out.println(predicate.toArray().toString());
 
 		if (fullExpression.pop().equals("=>"))
 		{
 			String fullBuff = "";
 
 			//check consequent matches left side of full expression, but has tilda
 			assert consequent.peek().equals("~");
 			consequent.pop();
 
 			for(int i=0; i < consequent.size();i++)
 			{
 
 				try
 				{
 					fullBuff = fullExpression.pop();
 					assert consequent.pop().equals(fullBuff);
 				}
 				catch (Exception e)
 				{
 					return false;
 				}
 			}
 
 			//check predicate matches right side of full expression, but has tilda
 			assert predicate.peek().equals("~");
 			predicate.pop();
 
 			for(int i=0; i < predicate.size();i++)
 			{
 
 				try
 				{
 					fullBuff = fullExpression.pop();
 					assert predicate.pop().equals(fullBuff);
 				}
 				catch (Exception e)
 				{
 					return false;
 				}
 			}
 			
 			try
 			{
 				assert fullExpression.size()==0;
 				return true;
 			}
 			catch (Exception e)
 			{
 				return false;
 			}
 			
 		}
 		return false;
 	}
 	
 	
 	private boolean checkTheoremEquivalence( LinkedList<String> storedTheoremQueue, LinkedList<String> inputTheoremQueue) {
 		//System.out.print(this.myTheoremSet.get("dn"));
 		
 		//Base Case
 			if (storedTheoremQueue.size() == 0 && inputTheoremQueue.size() == 0)
 			{
 				return true;
 			}
 			if (storedTheoremQueue.size() == 0 && inputTheoremQueue.size() != 0)
 			{
 				return false;
 			}
 			if (storedTheoremQueue.size() != 0 && inputTheoremQueue.size() == 0)
 			{
 				return false;
 			}
 		
 		if (storedTheoremQueue.peek().equals("=>")&&
 				inputTheoremQueue.peek().equals("=>"))
 		{
 			storedTheoremQueue.pop();
 			inputTheoremQueue.pop();
 			return checkTheoremEquivalence(storedTheoremQueue,inputTheoremQueue);
 		}
 		if (storedTheoremQueue.peek().equals("&")&&
 				inputTheoremQueue.peek().equals("&"))
 		{
 			storedTheoremQueue.pop();
 			inputTheoremQueue.pop();
 			return checkTheoremEquivalence(storedTheoremQueue,inputTheoremQueue);
 		}
 		if (storedTheoremQueue.peek().equals("|")&&
 				inputTheoremQueue.peek().equals("|"))
 		{
 			storedTheoremQueue.pop();
 			inputTheoremQueue.pop();
 			return checkTheoremEquivalence(storedTheoremQueue,inputTheoremQueue);
 		}
 		if (storedTheoremQueue.peek().equals("~")&&
 				inputTheoremQueue.peek().equals("~"))
 		{
 			storedTheoremQueue.pop();
 			inputTheoremQueue.pop();
 			return checkTheoremEquivalence(storedTheoremQueue,inputTheoremQueue);
 		}
 		else 
 		{
 			//Finds Left and Right Operand
 
 			String storedOperand = storedTheoremQueue.pop();
 			String inputOperand = inputTheoremQueue.pop();
 			
 			if(inputOperand.equals("&") || inputOperand.equals("|") || inputOperand.equals("=>") || inputOperand.equals("~"))
 			{
 				int numberOfOperations = 2;
 				if(inputOperand.equals("~"))
 				{
 					numberOfOperations--;
 				}
 				while(numberOfOperations != 0)
 				{
 
 					String currentStr = inputTheoremQueue.pop();
 					if(currentStr.equals("=>"))
 					{
 						numberOfOperations++;
 					}
 					if(currentStr.equals("&"))
 					{
 						numberOfOperations++;
 					}
 					if(currentStr.equals("|"))
 					{
 						numberOfOperations++;
 					}
 					else if(!currentStr.equals("~"))
 					{
 						numberOfOperations--;
 					}
 					inputOperand+=currentStr;
 				}
 			}
 			System.out.println(storedOperand);
 			System.out.println(inputOperand);
 			if (this.operands.get(storedOperand) != null)
 			{
 				if (operands.get(storedOperand).equals(inputOperand))
 				{
 					return checkTheoremEquivalence(storedTheoremQueue,inputTheoremQueue);
 				}
 				return false;
 			}
 			else
 			{
 				this.operands.put(storedOperand, inputOperand);
 			}
 			return checkTheoremEquivalence(storedTheoremQueue,inputTheoremQueue);
 		}
 		
 	}
 	
 
 	private void filterTildas(LinkedList<String> queue) {
 		if (queue.size() > 2)
 		{
 			if(queue.peekFirst().equals("~") && 
 					queue.get(1).equals("~")) 
 			{
 				queue.pop();
 				queue.pop();
 				filterTildas(queue);
 			}
 		}
 	}
 	
 	
 	public LinkedList<String> findAssumption(LinkedList<String> Queue)
 	{
 		/* Takes in Queue expressing and Expression
 		 * returns a Queue of the Left Operand of the Expressiong
 		 *  
 		 */
 		LinkedList<String> rtnQueue = new LinkedList<String>();
 		int numberOfOperands = 1;
 		int i=1;
 		while(numberOfOperands != 0)
 		{
 			String currentStr = Queue.get(i);
 			if(currentStr.equals("=>"))
 			{
 				numberOfOperands++;
 			}
 			else if(currentStr.equals("&"))
 			{
 				numberOfOperands++;
 			}
 			else if(currentStr.equals("|"))
 			{
 				numberOfOperands++;
 			}
 			else if(!currentStr.equals("~"))
 			{
 				numberOfOperands--;
 			}
 			rtnQueue.add(currentStr);
 			i++;
 		}
 		return rtnQueue;
 	}
 
 	public LinkedList<String> findConsequent(LinkedList<String> Queue)
 	{
 		int numberOfOperands = 1;
 		int i = 1;
 
 		while(numberOfOperands != 0)
 		{
 			String currentStr = Queue.get(i);
 			if(currentStr.equals("=>"))
 			{
 				numberOfOperands++;
 			}
 			else if(currentStr.equals("&"))
 			{
 				numberOfOperands++;
 			}
 			else if(currentStr.equals("|"))
 			{
 				numberOfOperands++;
 			}
 			else if(!currentStr.equals("~"))
 			{
 				numberOfOperands--;
 			}
 			i++;
 		}
 
 		LinkedList<String> rtnQueue = new LinkedList<String>();
 		numberOfOperands = 1;
 
 		while(numberOfOperands != 0)
 		{
 			String currentStr = Queue.get(i);
 			if(currentStr.equals("=>"))
 			{
 				numberOfOperands++;
 			}
 			else if(currentStr.equals("&"))
 			{
 				numberOfOperands++;
 			}
 			else if(currentStr.equals("|"))
 			{
 				numberOfOperands++;
 			}
 			else if(!currentStr.equals("~"))
 			{
 				numberOfOperands--;
 			}
 			rtnQueue.add(currentStr);
 			i++;
 		}
 
 		return rtnQueue;
 	}
 
 	
 	
 	/*
 	 * Check if there are numbers in the args of statement
 	 * 		if there are numbers, check to see if they are in scope
 	 * 			within scope = 
 	 * 
 	 * 				1. shit is in the hashtable
 	 * 
 	 * 				2. A legal line number reference matches the current line number everywhere 
 	 * 				but in the last place, which must be less than the corresponding component 
 	 * 				of the current line number. For example, if the current line number is 3.2.4, 
 	 * 				any of the following lines may be referenced: 1, 2, 3.1, 3.2.1, 3.2.2, or 3.2.3.
 	 * 
 	 * */
 	public boolean checkLineScope(String input) throws IllegalLineException
 	{	String test = input.substring(0,input.length()-1);
 		String temp = myLineNumber.toString();
 		if (!myTheoremSet.myTheorems.containsKey(input)){
 			throw new IllegalLineException("Line Number is not in scope: "+input);
 		}else if (input.length()>temp.length()){
 			throw new IllegalLineException("Line Number is too deep to be in scope: "+input);
 		}else if (Character.getNumericValue((input.charAt(input.length()-1)))>=Character.getNumericValue((temp.charAt(input.length()-1)))){
 			throw new IllegalLineException("Line Number is not in scope: "+input);
 		}else if (!temp.startsWith(test)&&input.length()!=1){
 			throw new IllegalLineException("Line Number is not in scope: "+input);
 		}
 		
 		return true;
 	}
 	
 	/*
 	 * To-do-
 	 * 		
 	 * 		1. MP, MT, CO, IC: Check that they reference lines in the scope
 	 * 		2. Add handling for user defined proofs (takes 1 arg + theorem name)
 	 * 		3. Check that assume is coming after a show
 	 * 		4. 
 	 * 
 	 * 
 	 * 
 	 * */
 	
 	
 	private boolean LineChecker(String[] statement) throws IllegalLineException, IllegalInferenceException {
 		//check for correct space placement
 		//checks for Line errors, returns true if isOK, returns false if error
 		int numArgs;
 		String command = statement[0];
 
 		try{
 
 			if (command.equals("print"))
 			{
 				numArgs = 1;
 			}
 			else if(command.equals("show")||
 					command.equals("assume"))
 
 					{numArgs = 2;
 
 			}
 			else if(command.equals("repeat")
 					||command.equals("ic")
 				    ||command.equals("theorem"))
 
 					{numArgs = 3;
 			}
 			else if(command.equals("mp")||
 					command.equals("mt")||
 					command.equals("co"))
 
 					{numArgs = 4;
 			}
 			else
 			{
 				if (this.myTheoremSet.get(command) == null)
 				{
 					throw new IllegalLineException("***Invalid Reason:" + command);
 				}
 				else
 				{
 					numArgs = 2;
 				}
 				
 			}
 
 			if (statement.length!=numArgs){
 				throw new IllegalLineException("Invalid Number of Args For:"+ statement [0]);
 			}
 
 			if (numArgs != 1)
 			{
 				ExpressionChecker(statement[numArgs-1]);
 
 				if (numArgs==3)
 				{
 					LineNumberChecker(statement[1]);
 					checkLineScope(statement[1]);
 				}
 				if (numArgs==4)
 				{
 					LineNumberChecker(statement[1]);
 					LineNumberChecker(statement[2]);
 					checkLineScope(statement[1]);
 					checkLineScope(statement[2]);
 				}
 			}
 
		}
 		if (command.equals("repeat")){
 			if(!new Expression(statement[2]).Queue.equals(myTheoremSet.myTheorems.get(statement[1]))){
 				throw new IllegalLineException("named Expression is not at given line: "+ statement[2]);
 			}
 		}
 		catch (IllegalLineException e)
 		{
 			throw e;
 		}
 
 		return true;
 	}
 
 	
 	public static void ExpressionChecker(String x) throws IllegalLineException{
 		//checks Expression for valid Parentheses, typos
 		//checks for nesting, operators within nesting, and syntax
 		int a=0;
 		int b=0;
 		char test=0;
 		int canHold=0;
 		int needRight=0;
 		boolean[][] dictionary=new boolean[][]{{false,false,true,false,true,false,false},
 							{true,true,false,true,false,false,true},
 							{false,false,true,false,true,false},
 							{true,true,false,true,false,false,true},
 							{true,true,false,true,false,false,true},
 							{false,false,true,false,true,false,false},
 							{false,false,false,false,false,true,false}};
 
 		for(int i=0;i<x.length();i++){
 			test=x.charAt(i);
 			a=indexer(test);
 			if (a==100){
 				throw new IllegalLineException("***Invalid Expression: "+x);
 			}
 			if (!dictionary[a][b]){
 				throw new IllegalLineException("***Invalid Expression: "+x);
 			}
 			if (test==')'){
 				needRight--;
 			}else if(test=='('){
 				needRight++;
 				canHold++;
 			}else if(test=='='){
 				canHold--;
 			}else if(test=='|'||test=='&'){
 				canHold--;
 			}else if(test=='~'){
 			}else if(!Character.isLetter(test)&&test!='>'){
 				throw new IllegalLineException("***Invalid Expression: "+x);
 			}
 			if (canHold<0||needRight<0){
 				throw new IllegalLineException("***Invalid Expression: "+x);
 			}
 			b=a;
 		}
 		if (canHold!=0||needRight!=0){
 			throw new IllegalLineException("***Invalid Expression: "+x);
 		}
 	}
 	public static int indexer(char x)throws IllegalLineException{
 		//takes in a char and returns int to be used for indexing with Expression Checker's dictionary
 		//throws IllegalLineException when the char is not of expected type
 		//the final return of 100 is never reached.
 		switch(x){
 			case '|':case'&': return 0;
 			case '(': return 1;
 			case ')': return 2;
 			case '~': return 3;
 			case '=': return 5;
 			case '>': return 6;
 			default: if(!Character.isLetter(x)){
 						throw new IllegalLineException("***Invalid Expression: "+x);
 					}else{
 						return 4;
 					}
 		}
 	}
 
 
 	public static void LineNumberChecker(String x)throws IllegalLineException{
 		//checks that x is just ints and .
 		if ((Character.toString(x.charAt(0)).equals('.'))&&(Character.toString(x.charAt(x.length()-1)).equals('.')))
 		{
 			throw new IllegalLineException("***Invalid Line Number"+x);
 		}
 		String[] test = x.split(".");
 		for (int i=0;i<test.length;i++){
 			try{
 				Integer.parseInt(test[i]);
 			}catch (NumberFormatException e){
 				throw new IllegalLineException("***Invalid Line Number:" + x);
 			} 
 		}
 	}
 
 
 
 	public boolean implies(boolean op1, boolean op2)
 	{
 		//truth tabel processing
 
 		if (op1==true && op2==true)
 		{
 			return true;
 		}
 		if (op1==true && op2==false)
 		{
 			return false;
 		}
 		return true;
 
 	}
 	
 	/*
 	 * To-do:
 	 * 
 	 * 		-Check which one starts with ~
 	 * 		-create a substring of that string, which excludes the ~
 	 * 		-make sure both are identical with .equals
 	 * 
 	 * */
 	
 	public boolean contradiction(LinkedList<String> first, LinkedList<String> second)
 	{
 		//System.out.println(second);
 		int numLeadingTildas;
 		
 		if (first.peek().equals("~") && second.peek().equals("~"))
 		{
 			first.pop();
 			second.pop();
 			return contradiction(first,second);
 		}
 		else if (first.peek().equals("~"))
 		{
 			first.pop();
 			return contradictionHelper(first, second);
 		}
 		else if (second.peek().equals("~"))
 		{
 			second.pop();
 			return contradictionHelper(first, second);
 		}
 		return false;
 	}
 
 	public boolean contradictionHelper(LinkedList<String> first, LinkedList<String> second)
 	{			
 		while(true)
 		{
 			String firstStr = first.pop();
 			String secondStr = second.pop();
 			if(!first.isEmpty())
 				while(firstStr.equals("~"))
 				{
 					if(first.peek().equals("~"))
 					{
 						first.pop();
 						firstStr = first.pop();
 					}
 					else
 					{
 						break;
 					}
 				}
 			if(!second.isEmpty())
 			{
 				while(secondStr.equals("~"))
 				{
 					if(second.peek().equals("~"))
 					{
 						second.pop();
 						secondStr = second.pop();
 					}
 					else
 					{
 						break;
 					}
 				}
 			}
 			if(!firstStr.equals(secondStr))
 			{
 				return false;
 			}
 			if(first.size() == 0 || second.size()==0)
 			{
 				if(first.size() == 0 && second.size()==0)
 				{
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 			}
 		}
 	}
 }
