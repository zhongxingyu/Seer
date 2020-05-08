 package main;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Stack;
 import java.util.Arrays;
 
 public class NFACreator {
 	
 	private NFA nfa;
 	private int index;
 	private String def;
 	private String name;
 	private ArrayList<String> splitDef;
 	private HashSet<NFA> defined_classes;
 	private HashMap<String, String> regexTable;
 	private Stack<Integer> myStack = new Stack<Integer>();
     private HashSet<String> reg_exFirst, rexpFirst, rexpPFirst, rexp1First, rexp1PFirst,
             rexp2First, rexp2_tailFirst, rexp3First, char_classFirst, char_class1First, 
             char_set_listFirst, char_setFirst, char_set_tailFirst, exclude_setFirst, exclude_set_tailFirst;
 	
 	public NFACreator(String name, String def, HashMap<String, String> regexTable, HashSet<NFA> regexNFAs)
 	{
 		index = 0;
 		this.def = def;
 		this.defined_classes = regexNFAs;
 		this.regexTable = regexTable;
 		splitDef = new ArrayList<String>();
 		createSplitDef();
 		initializeFirsts();
 		this.name = name;
         
 		nfa = reg_ex();
 		nfa.setName(name);
 	}
 	
 	public NFA getNFA()
 	{
 		return nfa;
 	}
 
 	public NFA reg_ex()
 	{
 		if(!reg_exFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return epsilonNFA();
 		}
 		return rexp();
 	}
 	
 	public NFA rexp()
 	{
 		if(!rexpFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return epsilonNFA();
 		}
 		myStack.push(index);
 		NFA rexp1 = rexp1();
 		if(rexp1 != null)
 		{
 			NFA rexpP = rexpP(rexp1);
 			if(!rexpP.getName().equals("EPSILON"))
 			{
 				myStack.pop();
 				return rexpP;
 			}
 			else
 			{
 				myStack.pop();
 				return rexp1;
 			}
 		}
 		index = myStack.pop();
 		return null;
 	}
 	
 	public NFA rexpP(NFA in)
 	{
 		if(index >= splitDef.size())
 		{
 			return epsilonNFA();
 		}
 		if(!rexpPFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return in;
 		}
 		myStack.push(index);
 		if(splitDef.get(index).equals("|"))
 		{
 			nextCurr();
 			NFA rexp1 = rexp1();
 			if(rexp1 != null)
 			{
 				NFA temp = UNION(in, rexp1);
 				NFA toRet = rexpP(temp);
 				myStack.pop();
 				return toRet;
 			}
 			else
 			{
 				index = myStack.pop();
 				return epsilonNFA();
 			}
 		}
 		else
 		{
 			index = myStack.pop();
 			return in;
 		}
 	}
 	
 	public NFA rexp1()
 	{
 		if(!rexp1First.contains(splitDef.get(index).substring(0,1)))
 		{
 			return epsilonNFA();
 		}
 		myStack.push(index);
 		NFA rexp2 = rexp2();
 		NFA rexp1P = rexp1P();
 		if(rexp2 != null && rexp1P != null)
 		{
 			myStack.pop();
 			return concat(rexp2, rexp1P);
 		}
 		else
 		{
 			index = myStack.pop();
 			return null;
 		}
 	}
 	
 	public NFA rexp1P()
 	{
 		if(index >= splitDef.size())
 		{
 			return epsilonNFA();
 		}
 		if(!rexp1PFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return epsilonNFA();
 		}
 		myStack.push(index);
 		NFA rexp2 = rexp2();
 		NFA rexp1P = rexp1P();
 		if(rexp2 != null && rexp1P != null)
 		{
 			myStack.pop();
 			return concat(rexp2, rexp1P);
 		}
 		else
 		{
 			index = myStack.pop();
 			return epsilonNFA();
 		}
 	}
 	
 	public NFA rexp2()
 	{
 		if(!rexp2First.contains(splitDef.get(index).substring(0,1)))
 		{
 			return epsilonNFA();
 		}
 		myStack.push(index);
 		if(splitDef.get(index).equals("("))
 		{
 			nextCurr();
 			NFA rexp = rexp();
 			if(rexp() != null)
 			{
 				if(splitDef.get(index).equals(")"))
 				{
 					nextCurr();
 					NFA rexp2_tail = rexp2_tail(rexp);
 					if(!rexp2_tail.getName().equals("EPSILON"))
 					{
 						myStack.pop();
 						return rexp2_tail;
 					}
 					else
 					{
 						myStack.pop();
 						return rexp;
 					}
 				}
 			}
 		}
 		
 		index = myStack.pop();
 		myStack.push(index);
 		
 		if(RE_CHAR().contains(splitDef.get(index)))
 		{
 			NFA n = new NFA(name, splitDef.get(index));
 			nextCurr();
 			NFA rexp2_tail = rexp2_tail(n);
 			myStack.pop();
 			return rexp2_tail;
 		}
 		
 		index = myStack.pop();
 		myStack.push(index);
 		
 		NFA rexp3 = rexp3();
 		if(rexp3 != null)
 		{
 			myStack.pop();
 			return rexp3;
 		}
 		
 		index = myStack.pop();
 		return null;
 	}
 	
 	public NFA rexp2_tail(NFA in)
 	{
 		if(index >= splitDef.size())
 		{
 			return in;
 		}
 		if(!rexp2_tailFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return in;
 		}
 		myStack.push(index);
 		switch(splitDef.get(index)){
 			case "*": 
 				nextCurr();
 				myStack.pop();
 				return star(in);
 			case "+": 
 				nextCurr();
 				myStack.pop();
 				return plus(in);
 		}
 		
 		index = myStack.pop();
 		return in;
 	}
 	
 	public NFA rexp3()
 	{
 		if(index >= splitDef.size())
 		{
 			return epsilonNFA();
 		}
 		if(!rexp3First.contains(splitDef.get(index).substring(0,1)))
 		{
 			return epsilonNFA();
 		}
 		myStack.push(index);
 		String char_class = char_class();
 		if(char_class != null)
 		{
 			myStack.pop();
 			return new NFA(name, char_class);
 		}
 		else
 		{
 			return epsilonNFA();
 		}
 	}
 	
 	public String char_class()
 	{
 		if(!char_classFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return null;
 		}
 		myStack.push(index);
 		if(splitDef.get(index).matches("[.]"))
 		{
 			nextCurr();
 			myStack.pop();
 			return "[.]";
 		}
 		index = myStack.pop();
 		myStack.push(index);
 		if(splitDef.get(index).equals("["))
 		{
 			nextCurr();
 			String char_class1 = char_class1();
 			if(char_class1 != null)
 			{
 				myStack.pop();
 				return "[" + char_class1;
 			}
 		}
 		index = myStack.pop();
 		myStack.push(index);
 		String defined_class = defined_class(splitDef.get(index));
 		if(defined_class == null)
 		{
 			index = myStack.pop();
 		}
 		else
 		{
 			myStack.pop();
 		}
 		return defined_class; // null if nonexistent
 	}
 	
 	public String char_class1()
 	{
 		if(!char_class1First.contains(splitDef.get(index).substring(0,1)))
 		{
 			return null;
 		}
 		myStack.push(index);
 		String char_set_list = char_set_list();
 		if(char_set_list != null)
 		{
 			myStack.pop();
 			return char_set_list;
 		}
 		index = myStack.pop();
 		myStack.push(index);
 		String exclude_set = exclude_set();
 		if(exclude_set != null)
 		{
 			myStack.pop();
 			return exclude_set;
 		}
 		index = myStack.pop();
 		return null;
 	}
 	
 	public String char_set_list()
 	{
 		if(!char_set_listFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			System.out.println("BITCHES");
 			return null;
 		}
 		myStack.push(index);
 		String char_set = char_set();
 		if(char_set != null)
 		{
 			String char_set_list = char_set_list();
 			if(char_set_list != null)
 			{
 				myStack.pop();
 				return char_set + char_set_list;
 			}
 		}
 		index = myStack.pop();
 		myStack.push(index);
 		if(splitDef.get(index).equals("]"))
 		{
 			nextCurr();
 			myStack.pop();
 			return "]";
 		}
 		index = myStack.pop();
 		return null;
 	}
 	
 	public String char_set()
 	{
 		if(!char_setFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return null;
 		}
 		myStack.push(index);
 		String ret = "";
 		if(CLS_CHAR().contains(splitDef.get(index)))
 		{
 			ret += splitDef.get(index);
 			nextCurr();
 			String char_set_tail = char_set_tail();
 			myStack.pop();
 			return ret + char_set_tail;
 		}
 		else
 		{
 			index = myStack.pop();
 			return null;
 		}
 	}
 	
 	public String char_set_tail()
 	{
 		if(index >= splitDef.size())
 		{
 			return epsilon();
 		}
 		if(!char_set_tailFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return null;
 		}
 		myStack.push(index);
 		String ret = "";
 		if(splitDef.get(index).equals("-") && CLS_CHAR().contains(splitDef.get(index+1)))
 		{
 			ret += splitDef.get(index);
 			nextCurr();
 			ret += splitDef.get(index);
 			nextCurr();
 			myStack.pop();
 			return ret;
 		}	
 		else
 		{
 			myStack.pop();
 			return epsilon();
 		}
 	}
 	
 	public String exclude_set()
 	{
 		if(!exclude_setFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return null;
 		}
 		myStack.push(index);
 		String ret = "";
 		if(splitDef.get(index).equals("^"))
 		{
 			ret += splitDef.get(index);
 			nextCurr();
 			String char_set = char_set();
 			if(char_set != null)
 			{
 				ret += char_set;
 				if(splitDef.get(index).equals("]"))
 				{
 					ret += splitDef.get(index);
 					nextCurr();
 					if(splitDef.get(index).equals("I") && splitDef.get(index+1).equals("N"))
 					{
 						nextCurr();
 						nextCurr();
 						String exclude_set_tail = exclude_set_tail();
 						if(exclude_set_tail != null)
 						{
 							ret = exclude_set_tail.substring(1,exclude_set_tail.length()-1) + ret;
 							myStack.pop();
 							return ret;
 						}
 					}
 				}
 			}
 		}
 		index = myStack.pop();
 		return null;
 	}
 	
 	public String exclude_set_tail()
 	{
 		if(!exclude_set_tailFirst.contains(splitDef.get(index).substring(0,1)))
 		{
 			return null;
 		}
 		myStack.push(index);
 		if(splitDef.get(index).equals("["))
 		{
 			nextCurr();
 			String char_set = char_set();
 			if(char_set != null && splitDef.get(index).equals("]"))
 			{
 				nextCurr();
 				myStack.pop();
 				return "[" + char_set + "]";
 			}
 		}
 		index = myStack.pop();
 		myStack.push(index);
 		String defined_class = defined_class(splitDef.get(index));
 		if(defined_class == null)
 		{
 			index = myStack.pop();
 		}
 		else
 		{
 			myStack.pop();
 		}
 		return defined_class; // null if nonexistent
 	}
 	
 	public String defined_class(String name)
 	{
 		String s = regexTable.get(name);
         String[] splitString = (s.split(" "));
         String value = splitString[0];
         for(int j=1; j<splitString.length; j++){
         	if(splitString[j].equalsIgnoreCase("in")){
         		String tempVal = regexTable.get(splitString[j+1]); //Retrieves the regex for "in $DIGIT/$CHAR"
             	value = tempVal.substring(0,tempVal.length()-1) + value.substring(1); //Appends the previous regex with the new one
             	break;
             }
             else{
             	value = value + " " + splitString[j];
             }
         }
         s = value;
 		if(s != null)
 		{
 			nextCurr();
 		}
 		return s;
 	}
 	
 	public NFA defined_classNFA(String name)
 	{
 		for (NFA n : defined_classes)
 		{
 			if (n.getName().equals(name))
 			{
 				return n;
 			}
 		}
 		return null;
 	}
 	
 	public NFA concat(NFA n1, NFA n2)
 	{
 		if(n1 == null && n2 == null)
 		{
 			return null;
 		}
 		else if(n1.getName().equals("EPSILON") && n2.getName().equals("EPSILON"))
 		{
 			return n1;
 		}
 		else if(n1 == null || n1.getName().equals("EPSILON"))
 		{
 			return n2;
 		}
 		else if(n2 == null || n2.getName().equals("EPSILON"))
 		{
 			return n1;
 		}
 		n1.getAccept().setIsAccept(false);
 		n1.getAccept().addTransition("", n2.getStart());
 		n1.setAccept(n2.getAccept());
 		return n1;
 	}
 
 	public ArrayList<String> CLS_CHAR()
 	{
 		ArrayList<String> wat = new ArrayList<String>();
 		for (char c=32; c<=126; c++) {
 			if(c != '\\' && c != '^' && c != '-' && c != '[' && c != ']') {
 				wat.add(Character.toString(c));
 			}
 		}
 		String[] escapedChars = {"\\\\","\\^","\\-","\\[","\\]"};
 		for(int i=0; i<escapedChars.length; i++)
 		{
 			wat.add(escapedChars[i]);
 		}
 		//System.out.println(wat.toString());
 		return wat;
 	}
 	
 	public ArrayList<String> RE_CHAR()
 	{
 		ArrayList<String> wat = new ArrayList<String>();
 		for (char c=32; c<=126; c++) {
 			if(c != ' ' && c != '\\' && c != '*' && c != '+' && c != '?' && c != '|' && c != '[' &&
 					c != ']' && c != '(' && c != ')' && c != '.' && c != '\'' && c != '"') {
 				wat.add(Character.toString(c));
 			}
 		}
 		String[] escapedChars = {"\\ ", "\\\\","\\*","\\+","\\?","\\|","\\[","\\]","\\(","\\)","\\.","\\\'","\\\""};
 		for(int i=0; i<escapedChars.length; i++)
 		{
 			wat.add(escapedChars[i]);
 		}
 		//System.out.println(wat.toString());
 		return wat;
 	}
 	
 	public NFA UNION(NFA n1, NFA n2)
 	{
 		if((n1 == null && n2 == null) || (n1.getName().equals("EPSILON") && n2.getName().equals("EPSILON")))
 		{
 			return null;
 		}
 		else if(n1 == null || n1.getName().equals("EPSILON"))
 		{
 			return n2;
 		}
 		else if(n2 == null || n2.getName().equals("EPSILON"))
 		{
 			return n1;
 		}
 		State newStart = new State(false, new HashMap<String, List<State>>());
		newStart.addTransition("", n1.getAccept());
		newStart.addTransition("", n2.getAccept());
 		State newAccept = new State(true, new HashMap<String, List<State>>());
 		newAccept.setName(name);
 		n1.getAccept().setIsAccept(false);
 		n2.getAccept().setIsAccept(false);
 		n1.getAccept().addTransition("", newAccept);
 		n2.getAccept().addTransition("", newAccept);
 		n1.setAccept(newAccept);
 		n2.setAccept(newAccept);
 		n1.setStart(newStart);
 //		NFA uniNFA = new NFA("|");
 //		State startS = new State(false, new HashMap<String, State>());
 //		uniNFA.addTransition(startS, "|", new State(true, new HashMap<String, State>()));
 //		uniNFA.setStart(startS);
 		return n1;
 	}
 	
 	public NFA epsilonNFA()
 	{
 		NFA epsNFA = new NFA("EPSILON");
 		State startS = new State(false, new HashMap<String, List<State>>());
 		State acceptS = new State(true, new HashMap<String, List<State>>());
 		acceptS.setName("EPSILON");
 		epsNFA.addTransition(startS, "", acceptS);
 		epsNFA.setStart(startS);
 		epsNFA.setAccept(acceptS);
 		return epsNFA;
 	}
 	
 	public String epsilon()
 	{
 		return "";
 	}
 	
 	public NFA star(NFA n)
 	{
 		State newStart = new State(false, new HashMap<String, List<State>>());
 		newStart.addTransition("", n.getAccept());
 		n.getAccept().setIsAccept(false);
 		n.getAccept().addTransition("", n.getStart());
 		State newAccept = new State(true, new HashMap<String, List<State>>());
 		n.getAccept().addTransition("", newAccept);
 		n.setAccept(newAccept);
 		n.setStart(newStart);
 //		NFA starNFA = new NFA("star");
 //		State startS = new State(false, new HashMap<String, State>());
 //		starNFA.addTransition(startS, "*", new State(true, new HashMap<String, State>()));
 //		starNFA.setStart(startS);
 		return n;
 	}
 	
 	public NFA plus(NFA n)
 	{
 		State newStart = new State(false, new HashMap<String, List<State>>());
 		newStart.addTransition("", n.getStart());
 		n.getAccept().setIsAccept(false);
 		n.getAccept().addTransition("", n.getStart());
 		State newAccept = new State(true, new HashMap<String, List<State>>());
 		n.getAccept().addTransition("", newAccept);
 		n.setAccept(newAccept);
 		n.setStart(newStart);
 //		NFA plusNFA = new NFA("plus");
 //		State startS = new State(false, new HashMap<String, State>());
 //		plusNFA.addTransition(startS, "+", new State(true, new HashMap<String, State>()));
 //		plusNFA.setStart(startS);
 		return n;
 	}
 	
 //	public NFA dot()
 //	{
 //		NFA dotNFA = new NFA("dot");
 //		State startS = new State(false, new HashMap<String, List<State>>());
 //		dotNFA.addTransition(startS, ".", new State(true, new HashMap<String, List<State>>()));
 //		dotNFA.setStart(startS);
 //		return dotNFA;
 //	}
 //	
 	public void createSplitDef()
 	{
 		String curr;
 		for(int i=0; i<def.length(); i++)
 		{
 			curr = def.substring(i, i+1);
 			if(curr.equals(" "))
 			{
 				continue;
 			}
 			if(curr.equals("\\"))
 			{
 				i++;
 				curr += def.substring(i, i+1);
 			}
 			else if(curr.equals("$") && def.substring(i+1, i+2).matches("[a-zA-Z]"))
 			{
 				while(def.substring(i+1, i+2).matches("[a-zA-Z]"))
 				{
 					i++;
 					curr += def.substring(i, i+1);
 					if(i >= def.length()-1)
 					{
 						break;
 					}
 				}
 			}
 			splitDef.add(curr);
 		}
 		//System.out.println(splitDef.toString());
 	}
 	
 	public void nextCurr()
 	{
 		index++;
 		if(index >= splitDef.size())
 		{
 			
 		}
 	}
 	
 	public void initializeFirsts()
 	{
 		 // Sets up first sets
         String[] firstSet1 = {"(", ".", "[", "$"};
         reg_exFirst = new HashSet<String>(Arrays.asList(firstSet1));
         reg_exFirst.addAll(RE_CHAR());
         String[] firstSet2 = {"(", ".", "[", "$"};
         rexpFirst = new HashSet<String>(Arrays.asList(firstSet2));
         rexpFirst.addAll(RE_CHAR());
         String[] firstSet3 = {"|", ""};
         rexpPFirst = new HashSet<String>(Arrays.asList(firstSet3));
         String[] firstSet4 = {"(", ".", "[", "$"};
         rexp1First = new HashSet<String>(Arrays.asList(firstSet4));
         rexp1First.addAll(RE_CHAR());
         String[] firstSet5 = {"(", ".", "[", "$", ""};
         rexp1PFirst = new HashSet<String>(Arrays.asList(firstSet5));
         rexp1PFirst.addAll(RE_CHAR());
         String[] firstSet6 = {"(", ".", "[", "$"};
         rexp2First = new HashSet<String>(Arrays.asList(firstSet6));
         rexp2First.addAll(RE_CHAR());
         String[] firstSet7 = {"*", "+", ""};
         rexp2_tailFirst = new HashSet<String>(Arrays.asList(firstSet7));
         String[] firstSet8 = {".", "[", "$", ""};
         rexp3First = new HashSet<String>(Arrays.asList(firstSet8));
         String[] firstSet9 = {".", "[", "$"};
         char_classFirst = new HashSet<String>(Arrays.asList(firstSet9));
         String[] firstSet10 = {"]", "^"};
         char_class1First = new HashSet<String>(Arrays.asList(firstSet10));
         char_class1First.addAll(CLS_CHAR());
         String[] firstSet11 = {"]"};
         char_set_listFirst = new HashSet<String>(Arrays.asList(firstSet11));
         char_set_listFirst.addAll(CLS_CHAR());
         String[] firstSet12 = {};
         char_setFirst = new HashSet<String>(Arrays.asList(firstSet12));
         char_setFirst.addAll(CLS_CHAR());
         String[] firstSet13 = {"-", ""};
         char_set_tailFirst = new HashSet<String>(Arrays.asList(firstSet13));
         String[] firstSet14 = {"^"};
         exclude_setFirst = new HashSet<String>(Arrays.asList(firstSet14));
         String[] firstSet15 = {"[", "$"};
         exclude_set_tailFirst = new HashSet<String>(Arrays.asList(firstSet15));
 	}
 }
