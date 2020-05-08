 package phase2;
 
 import java.util.*;
 
 public class TableWalker
 {
 	private Stack<String> stack;
 	private String begin;
 	private LinkedHashMap<String, LinkedHashMap<String, String>> table;
 	private LinkedList<Token> tokens;
 	
 	public TableWalker(List<String> inputFile, String begin, 
 			LinkedHashMap<String, LinkedHashMap<String, String>> table)
 	{
 		this.begin = begin;
 		this.table = table;
 		this.tokens = createTokens(inputFile);
 		stack = new Stack<String>();
 	}
 	
 	public boolean parse()
 	{
 		String currType = "", currValue = "", currStack = "", rule = "";
 		boolean result = false;
 		tokens.addLast(new Token("$", "$"));
 		stack.push("$");
 		stack.push(begin);
 
 		while (!stack.isEmpty())	
 		{
 			currType = tokens.getFirst().getId();
 			currValue = tokens.getFirst().getValue();
 			currStack = stack.pop();
 			/*System.out.println(stack);
 			System.out.println(currStack);
 			System.out.println(currType + " " + currValue);
 			System.out.println("=================================");*/
 
 			
 			
 			if (currStack.equals("<epsilon>"))
 			{
 				currStack = stack.pop();
 			}
 			
 			if (table.containsKey(currStack))
 			{
 				if (table.get(currStack).containsKey(currType))
 				{
 					rule = table.get(currStack).get(currType);
 				}
 				else
 				{
 					rule = table.get(currStack).get(currValue);
 				}
 				//System.out.println("Rule: " + rule);
				pushStack(stack, rule);
 				
 			}
 			else 
 			{
 				if (currStack.equals(currValue) || currStack.equals(currType))
 				{
 					tokens.removeFirst();
 				}
 				else
 				{
 					System.out.println("Form not valid");
 					result =  false;
 					break;
 				}
 			}
 			
 		}
 		
 		//System.out.println("FINAL TOKENS: " + tokens);
 		if (stack.isEmpty() && tokens.isEmpty()) //|| tokens.isEmpty() && stack.size() == 1)
 			result = true;
 		
 		return result;
 	}
 	
 	private void pushStack(Stack<String> stack, String rule)
 	{
 		String[] split = rule.split(" ");
 		for (int i = split.length-1; i >= 0; --i)
 		{
 			if (!split[i].equals("") && !split[i].equals("<epsilon>"))
 			{
 				stack.push(split[i]);
 				//System.out.println("STACK PUSH: " + split[i]);
 			}
 		}
 	}
 	
 	private LinkedList<Token> createTokens(List<String> input){
 		LinkedList<Token> toRet = new LinkedList<Token>();
 		for(String s : input){
 			String[] temp = s.split(" ", 2);
 			toRet.add(new Token(temp[0], temp[1]));
 			//System.out.println("TOKENS: " + temp[0] + " " + temp[1]);
 		}
 		/*for(Token t : toRet){
 			System.out.println(t.toString());
 		}*/
 		return toRet;
 	}
 }
