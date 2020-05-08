 package com.chaosdev.textmodloader.util;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.chaosdev.textmodloader.TextMod;
 import com.chaosdev.textmodloader.methods.MethodExecuter;
 import com.chaosdev.textmodloader.util.codeblocktypes.CodeBlockType;
 import com.chaosdev.textmodloader.util.types.Type;
 
 public class CodeBlock
 {
 	public boolean					blockComment	= false;
 	
 	public CodeBlock				superCodeBlock;
 	public List<CodeBlock>			codeBlocks;
 	public List<String>				lines;
 	public Map<String, Variable>	variables		= new HashMap<String, Variable>();
 	
 	public Parser					parser;
 	
 	public CodeBlock(CodeBlock superBlock)
 	{
 		this(superBlock, new LinkedList<String>());
 	}
 	
 	public CodeBlock(CodeBlock superBlock, List<String> lines)
 	{
 		this.superCodeBlock = superBlock;
 		this.lines = lines;
 		this.codeBlocks = new LinkedList<CodeBlock>();
 		this.parser = new Parser(this);
 	}
 	
 	public Map<String, Variable> getVariables()
 	{
 		Map<String, Variable> var = new HashMap<String, Variable>();
 		var.putAll(variables);
 		if (superCodeBlock != null)
 			var.putAll(superCodeBlock.getVariables());
 		return var;
 	}
 	
 	public boolean isBlockStart(String line)
 	{
 		return !line.isEmpty() && !line.equals("\n") && !isComment(line) && !isMethod(line) && !isVariable(line) && CodeBlockType.getCodeBlockType(this, line) != null;
 	}
 	
 	public boolean isBlockEnd(String line)
 	{
 		return line.endsWith("}");
 	}
 	
 	public boolean isComment(String line)
 	{
 		return blockComment || line.startsWith("//");
 	}
 	
 	public void execute()
 	{
 		CodeBlock cb = null;
 		for (int i = 0; i < lines.size(); i++)
 		{
 			String line = lines.get(i).trim();
 			
 			if (line.trim().startsWith("/*"))
 				blockComment = true;
 			if (line.trim().endsWith("*/"))
 				blockComment = false;
 			
 			try
 			{
 				if (superCodeBlock != null && superCodeBlock instanceof SpecialCodeBlock && ((SpecialCodeBlock) superCodeBlock).getCodeBlockType().isBreakable() && line.equals("break;"))
					;
				cb = null;
 				
 				if (cb != null && !isBlockStart(line) && !line.startsWith("{") && !isBlockEnd(line))
 					cb.lines.add(line);
 				
 				if (isBlockStart(line))
 				{
 					cb = new SpecialCodeBlock(line, this);
					cb = cb;
 				}
 				else if (line.startsWith("{") && cb == null)
 				{
 					cb = new CodeBlock(this);
 				}
 				else if (isBlockEnd(line))
 				{
 					cb.lines.add(line.replace("}", "").trim());
 					cb.execute();
 					codeBlocks.add(cb);
 					cb = null;
 				}
 				
 				if (cb == null)
 				{
 					this.executeLine(line);
 				}
 			}
 			catch (Exception ex)
 			{
 				System.out.println("  Exception while executing line " + i + ": ");
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	public void executeLine(String line)
 	{
 		if (TextModHelper.isLineValid(line))
 		{
 			System.out.println("  Reading line: " + line);
 			if (isMethod(line)) // Methods
 			{
 				Method method = getMethod(line);
 				executeMethod(method);
 			}
 			else if (isVariable(line)) // Variables
 			{
 				Variable v = getVariable(line);
 				this.variables.put(v.name, v);
 				this.parser.update(this);
 				System.out.println("  Variable \'" + v.name + "\' of type \'" + v.type.toString() + "\' added with value \'" + v.value.toString() + "\'.");
 			}
 		}
 	}
 	
 	public Object executeMethod(Method method)
 	{
 		MethodExecuter executer = TextModHelper.getMethodExecuterFromName(method.name);
 		if (executer != null)
 			return executer.execute(method.parameters);
 		else
 			System.out.println("  No valid executer found for method name " + method.name);
 		return null;
 	}
 	
 	public Method getMethod(String line)
 	{
 		// Replaces the method identifier
 		line = line.replaceFirst("[>]", "").trim();
 		int i = line.indexOf(TextMod.METHOD_PARAMETERS_START_CHAR);
 		int j = line.lastIndexOf(TextMod.METHOD_PARAMETERS_END_CHAR);
 		if (i == -1 || j == -1)
 			return null;
 		String methodName = line.substring(0, i);
 		String parameters = line.substring(i + 1, j);
 		String[] aparameters = TextModHelper.createParameterList(parameters, TextMod.PARAMETER_SPLIT_CHAR.charAt(0));
 		for (int m = 0; m < aparameters.length; m++)
 		{
 			aparameters[m] = aparameters[m].trim();
 		}
 		Object[] aparameters2 = parser.parse(aparameters);
 		return new Method(methodName, aparameters2);
 	}
 	
 	public Variable getVariable(String line)
 	{
 		String[] split = TextModHelper.createParameterList(line.replace(";", ""), ' ');
 		Variable var = null;
 		if (isType(split[0])) // First part is a type declaration
 		{
 			Type type = parser.getType(split[0]);
 			String name = split[1];
 			Object value = parser.parse(split[3]);
 			var = new Variable(type, name, value);
 		}
 		else
 		// First part is an existing variable name
 		{
 			Variable var1 = variables.get(split[0]);
 			String operator = split[1];
 			Object value = parser.parse(split[2]);
 			var = operate(var1, operator, value);
 		}
 		return var;
 	}
 	
 	public boolean isVariable(String par1)
 	{
 		String first = par1;
 		if (par1.contains(" "))
 			first = par1.substring(0, par1.indexOf(" "));
 		if (variables.get(TextModHelper.changeName(first)) != null) // Already
 																	// existing
 																	// Variable
 			return true;
 		else if (isType(first))
 			return true;
 		return false;
 	}
 	
 	public boolean isMethod(String par1)
 	{
 		par1 = par1.replace(";", "");
 		if (par1.contains(TextMod.METHOD_PARAMETERS_START_CHAR) && par1.endsWith(TextMod.METHOD_PARAMETERS_END_CHAR))
 		{
 			MethodExecuter executer = TextModHelper.getMethodExecuterFromName(par1.substring(0, par1.indexOf(TextMod.METHOD_PARAMETERS_START_CHAR)));
 			return executer != null;
 		}
 		return false;
 	}
 	
 	public boolean isType(String par1)
 	{
 		return parser.getType(par1) != null;
 	}
 	
 	public Variable operate(Variable var1, String operator, Object value)
 	{
 		if (operator.equals("="))
 		{
 			var1.value = value;
 			return var1;
 		}
 		else if (operator.equals("+="))
 		{
 			if (var1.value instanceof String && value instanceof String)
 			{
 				var1.value = (String) var1.value + (String) value;
 				return var1;
 			}
 			try
 			{
 				double i = ((Double) var1.value);
 				double j = ((Double) value);
 				i += j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		else if (operator.equals("-="))
 		{
 			try
 			{
 				double i = ((Double) var1.value);
 				double j = ((Double) value);
 				i -= j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		else if (operator.equals("*="))
 		{
 			try
 			{
 				double i = ((Double) var1.value);
 				double j = ((Double) value);
 				i *= j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		else if (operator.equals("/="))
 		{
 			try
 			{
 				double i = ((Double) var1.value);
 				double j = ((Double) value);
 				i /= j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		else if (operator.equals("%="))
 		{
 			try
 			{
 				double i = ((Double) var1.value);
 				double j = ((Double) value);
 				i %= j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		else if (operator.equals("&="))
 		{
 			if (var1.value instanceof Boolean && value instanceof Boolean)
 			{
 				var1.value = (Boolean) var1.value & (Boolean) value;
 				return var1;
 			}
 			try
 			{
 				int i = ((Integer) var1.value);
 				int j = ((Integer) value);
 				i &= j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		else if (operator.equals("|="))
 		{
 			if (var1.value instanceof Boolean && value instanceof Boolean)
 			{
 				var1.value = (Boolean) var1.value | (Boolean) value;
 				return var1;
 			}
 			try
 			{
 				int i = ((Integer) var1.value);
 				int j = ((Integer) value);
 				i |= j;
 				var1.value = i;
 				return var1;
 			}
 			catch (ClassCastException cce)
 			{
 				return var1;
 			}
 		}
 		return var1;
 	}
 }
