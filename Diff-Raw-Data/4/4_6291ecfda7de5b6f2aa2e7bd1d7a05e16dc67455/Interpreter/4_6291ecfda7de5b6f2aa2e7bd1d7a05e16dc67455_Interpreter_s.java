 package watson.glen.pseudocode.interpreter;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import watson.glen.pseudocode.constructs.AccessModifier;
 import watson.glen.pseudocode.constructs.ClassConstruct;
 import watson.glen.pseudocode.constructs.Comment;
 import watson.glen.pseudocode.constructs.EnumConstruct;
 import watson.glen.pseudocode.constructs.FirstClassMember;
 import watson.glen.pseudocode.constructs.InterfaceConstruct;
 import watson.glen.pseudocode.constructs.LanguageConstruct;
 import watson.glen.pseudocode.constructs.Method;
 import watson.glen.pseudocode.constructs.MethodSignature;
 import watson.glen.pseudocode.constructs.VariableDeclaration;
 import watson.glen.pseudocode.interpreter.exception.MissingAccessModifierException;
 import watson.glen.pseudocode.interpreter.exception.NotAMethodSignatureException;
 import watson.glen.pseudocode.tokenizer.LineToken;
 import watson.glen.pseudocode.tokenizer.Token;
 
 public class Interpreter
 {
 	private final String TAB = "\t";
 	List<LanguageConstruct> constructs = new LinkedList<LanguageConstruct>();
 	private Level0State lvl0State;
 	private ClassConstruct lastClass;
 	private InterfaceConstruct lastInterface;
 	private EnumConstruct lastEnum;
 	
 	private Method lastMethod;
 	/*
 	//method signature regex
 	private static final String tabs = "("+TAB+")*";
 	private static final String accessModifier = "[\\+\\-#]";
 	private static final String generic ="<[a-zA-Z_][a-zA-Z0-9_]*>";
 	private static final String type ="([a-zA-Z_][a-zA-Z0-9_]*("+generic+")?)";
 	private static final String name = "([a-z_][a-zA-Z0-9_]*)";
 	
 	private static final String parameter = "("+name+" : "+type+")";
 	private static final String parameters = "("+parameter+", )*"+parameter;
 	private static final String parameterList = "\\(("+parameters+"|"+parameter+"?)\\)";
 	
 	private static final String methodSigRegex = tabs + accessModifier + " " + name + parameterList + " : " + type;
 	//end method signature regex
 */
 	
 	public List<LanguageConstruct> interpret(List<LineToken> lineTokens)
 	{
 		parseLineTokens(lineTokens);
 		return constructs;
 	}
 	
 	private void parseLineTokens(List<LineToken> lineTokens)
 	{
 		for(LineToken lineToken : lineTokens)
 		{
 			System.out.println("parsing: "+lineToken);
 			parseTokens(lineToken.getTokens());
 		}
 	}
 
 	private void parseTokens(List<Token> tokens)
 	{
 		Queue<Token> tokenQueue = toTokenQueue(tokens);
 		int indentionLevel = getIndendation(tokenQueue);
 		switch(indentionLevel)
 		{
 			case 0: //Class, Interface, Enum
 				parseLevel0(tokenQueue);
 				break;
 			case 1:
 				parseLevel1(tokenQueue);
 				break;
 			default:
 				parseLevelGreaterThan2(tokenQueue);
 				break;
 		}
 	}
 	
 	private int getIndendation(Queue<Token> tokens)
 	{
 		int indention = 0;
 		while(tokens.size() > 0 && tokens.peek().getValue().equals(TAB))
 		{
 			tokens.poll();
 			indention++;
 		}
 		return indention;
 	}
 	
 	private FirstClassMember getFirstClassMember()
 	{
 		switch(lvl0State)
 		{
 			case Class:
 				return lastClass;
 			case Interface:
 				return lastInterface;
 			case Enum:
 				return lastEnum;
 		}
 		return null;
 	}
 	
 	/* Level 0 */
 	private void parseLevel0(Queue<Token> tokens)
 	{
 		try
 		{
 			AccessModifier modifier = parseModifier(tokens);
 			String first = tokens.poll().getValue();
 			String name = tokens.poll().getValue();
 			switch(first)
 			{
 				case "class":
 					lvl0State = Level0State.Class;
 					lastClass = new ClassConstruct(modifier, name);
 					constructs.add(lastClass);
 					//TODO: parse extends/implements
 					break;
 				case "interface":
 					lvl0State = Level0State.Interface;
 					lastInterface = new InterfaceConstruct(modifier, name);
 					constructs.add(lastInterface);
 					//TODO: parse extends or implements?
 					break;
 				case "enum":
 					lvl0State = Level0State.Enum;
 					lastEnum = new EnumConstruct(modifier, name);
 					constructs.add(lastEnum);
 					break;
 				default:
 					lvl0State = null;
 			}
 		} catch (MissingAccessModifierException e)
 		{
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		
 		//if there are values here,
 		//	Init new class/interface/enum
 		//	parse the values
 		//	update lvl0State
 	}
 	
 	
 	/* Level 1*/
 	private void parseLevel1(Queue<Token> tokens)
 	{
 		switch(lvl0State)
 		{
 			case Class: //Instance variables, Method signatures,
 				try
 				{
 					parseClassInternals(tokens);
 				} catch (NotAMethodSignatureException e)
 				{
 					System.out.println(e.getMessage());
 					e.printStackTrace();
 				} catch (MissingAccessModifierException e)
 				{
 					System.out.println(e.getMessage());
 					e.printStackTrace();
 				}
 				break;
 			case Interface: //Method signatures
 				try
 				{
 					parseInterfaceMethodSignature(tokens);
 				} catch (NotAMethodSignatureException e)
 				{
 					System.out.println(e.getMessage());
 				} catch (MissingAccessModifierException e)
 				{
 					System.out.println(e.getMessage());
 					e.printStackTrace();
 				}
 				break;
 			case Enum: //Enum values
 				parseEnumValues(tokens);
 				break;
 			default:
 				assert false : lvl0State;
 		}
 	}
 
 	private void parseClassInternals(Queue<Token> tokens) throws NotAMethodSignatureException, MissingAccessModifierException
 	{
 		lastMethod = new Method(parseMethodSignature(tokens));
 		lastClass.getMethods().add(lastMethod);
 	}
 
 	private void parseInterfaceMethodSignature(Queue<Token> tokens) throws NotAMethodSignatureException, MissingAccessModifierException
 	{
 		parseMethodSignature(tokens);
 	}
 
 	private void parseEnumValues(Queue<Token> tokens)
 	{
 		while(tokens.size() > 0)
 		{
 			String value = tokens.poll().getValue();
 			lastEnum.getEnumNames().add(value);
 			if(tokens.size()>0 && tokens.peek().equals(","))
 				tokens.poll();
 		}
 	}
 	
 	/* Level >= 2 */
 	private void parseLevelGreaterThan2(Queue<Token> tokens)
 	{
 		switch(lvl0State)
 		{
 			case Class: //Actual code
 				lastMethod.getLines().add(parseComment(tokens));
 				break;
 			case Interface: //Umm, no?
 				//throw new 
 				break;
 			case Enum: //Nope
 				//throw new 
 				break;
 			default:
 				assert false : lvl0State;
 		}
 	}
 	
 	private Comment parseComment(Queue<Token> tokens)
 	{
 		StringBuilder sb = new StringBuilder();
 		Token token;
 		while((token = tokens.poll()) != null)
 		{
 			sb.append(token.getValue());
 		}
 		return new Comment(sb.toString());
 	}
 	
 	private MethodSignature parseMethodSignature(Queue<Token> tokens) throws NotAMethodSignatureException, MissingAccessModifierException
 	{
 		AccessModifier modifier = parseModifier(tokens);
 		boolean isStatic = parseStatic(tokens);
 		String methodName = parseMethodName(tokens);
 		List<VariableDeclaration> parameters = parseParameterList(tokens);
 		String returnType = parseType(tokens);
 		
		
 		MethodSignature sig = new MethodSignature(modifier, isStatic, returnType, methodName, parameters);
 		return sig;
 	}
 
 	private Queue<Token> toTokenQueue(List<Token> tokenList)
 	{
 		LinkedList<Token> llQueue = new LinkedList<>(tokenList);
 		return llQueue;
 	}
 	
 	private Queue<LineToken> toLineTokenQueue(List<LineToken> lineTokenList)
 	{
 		LinkedList<LineToken> llQueue = new LinkedList<>(lineTokenList);
 		return llQueue;
 	}
 	
 	private boolean parseStatic(Queue<Token> tokens)
 	{
 		if(tokens.size() > 0 && tokens.peek().getValue().equals("_"))
 		{
 			tokens.poll();
 			return true;
 		}
 		return false;
 	}
 
 	private String parseType(Queue<Token> tokens) throws NotAMethodSignatureException
 	{
 		if(tokens.size() == 0)
 			throw new NotAMethodSignatureException("No type given");
 		return tokens.poll().getValue();
 	}
 
 	private List<VariableDeclaration> parseParameterList(Queue<Token> tokens) throws NotAMethodSignatureException
 	{
 		if(tokens.size() == 0 || !tokens.poll().getValue().equals("("))
 			throw new NotAMethodSignatureException("No \"(\" after method name");
 		
 		if(tokens.size() == 0)
 			throw new NotAMethodSignatureException("No parameter list or closing paren \")\" after opening paren \"(\"");
 		boolean endOfParameterList = !tokens.peek().getValue().equals(")");
 		List<VariableDeclaration> varDeclarations = endOfParameterList ? parseParameters(tokens) : new ArrayList<VariableDeclaration>();
 		
 		if(tokens.size() == 0 || !tokens.poll().getValue().equals(")"))
 			throw new NotAMethodSignatureException("No closing \")\" in parameter list");
 		return varDeclarations;
 	}
 
 	private List<VariableDeclaration> parseParameters(Queue<Token> tokens) throws NotAMethodSignatureException
 	{
 		List<VariableDeclaration> varDeclarations = new LinkedList<>();
 		varDeclarations.add(parseParameter(tokens));
 		while(!tokens.peek().getValue().equals(")"))
 		{
 			if(!tokens.poll().getValue().equals(","))
 				throw new NotAMethodSignatureException("No \",\" in between parameters");
 			varDeclarations.add(parseParameter(tokens));
 		}
 		return varDeclarations;
 	}
 	
 	private VariableDeclaration parseParameter(Queue<Token> tokens) throws NotAMethodSignatureException
 	{
 		if(tokens.size() < 3)
 			throw new NotAMethodSignatureException("Invalid parameter list variable declaration");
 		
 		String variableName = tokens.poll().getValue();
 		if(!tokens.poll().getValue().equals(":"))
 			throw new NotAMethodSignatureException("No \":\" in parameter list variable declaration");
 		String type = tokens.poll().getValue();
 		return new VariableDeclaration(type, variableName);
 	}
 
 	private String parseMethodName(Queue<Token> tokens) throws NotAMethodSignatureException
 	{
 		if(tokens.size() < 1)
 			throw new NotAMethodSignatureException("No method name");
 		return tokens.poll().getValue();
 	}
 
 	private AccessModifier parseModifier(Queue<Token> tokens) throws MissingAccessModifierException
 	{
 		if(tokens.size() < 1)
 			throw new MissingAccessModifierException();
 		
 		AccessModifier modifier;
 		switch(tokens.poll().getValue())
 		{
 			case "+":
 				modifier = AccessModifier.publicModifier;
 				break;
 			case "-":
 				modifier = AccessModifier.privateModifier;
 				break;
 			case "#":
 				modifier = AccessModifier.protectedModifier;
 				break;
 			case "~":
 				modifier = AccessModifier.defaultModifier;
 				break;
 			default:
 				modifier = AccessModifier.publicModifier;
 				break;
 		}
 		return modifier;
 	}
 }
