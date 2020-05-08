 package swp_compiler_ss13.fuc.backend;
 
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.types.Type;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This class allows for the generation of an LLVM IR module.
  * Each methode that begins with "add" generates LLVM IR code
  * and writes it to the <code>PrintWriter</code> <code>out</code>.
  *
  */
 public class Module
 {
 	/**
 	 * A list of all the string literals that
 	 * exist in this module where each element
 	 * describes a string literal's number with its
 	 * position in the list and that string literal's
 	 * length with the value of the element.
 	 *
 	 */
 	private ArrayList<Integer> stringLiterals;
 
 	/**
 	 * This maps a variable's name (not its
 	 * identifier, which is %{name}) to the
 	 * number of times it has been used in
 	 * the module already.
 	 * This is necessary because LLVM's IR
 	 * allows only for static single assignment
 	 * (SSA), wich means we have to store the
 	 * variable's actual value as the destination
 	 * of a static pointer and generate a new
 	 * 'use' variable (%{name}.{use_number})
 	 * for every time the variable is read
 	 * from or written to and route the pointer
 	 * access through that use variable.
 	 *
 	 */
 	private Map<String,Integer> variableUseCount;
 
 	/**
 	 * The <code>PrintWriter</code> which
 	 * the generated LLVM IR is written with.
 	 *
 	 */
 	private PrintWriter out;
 
 	/**
 	 * Creates a new <code>Module</code> instance.
 	 *
 	 * @param out <code>PrintWriter</code> used
 	 * to write the LLVM IR
 	 */
 	public Module(PrintWriter out)
 	{
 		reset(out);
 	}
 
 	/**
 	 * Completely resets this <code>Module</code>,
 	 * so it can be reused.
 	 *
 	 * @param out <code>PrintWriter</code> used
 	 * to write the LLVM IR
 	 */
 	public void reset(PrintWriter out)
 	{
 		stringLiterals = new ArrayList<Integer>();
 		variableUseCount = new HashMap<String,Integer>();
 
 		this.out = out;
 	}
 
 	/**
 	 * Prefix the LLVM IR with two spaces
 	 * (as it may only exist inside the main
 	 * function for now) and write it with <code>out</code>
 	 *
 	 * @param code a <code>String</code> value
 	 */
 	private void gen(String code)
 	{
 		out.println("  " + code);
 	}
 
 	/**
 	 * Get a string literal's identifier by its
 	 * id (i.e. its position in the list of string literals).
 	 *
 	 * @param id the string literal's id
 	 * @return the string literal's identifier
 	 */
 	private static String getStringLiteralIdentifier(int id)
 	{
 		return "%.string_" + String.valueOf(id);
 	}
 
 	/**
 	 * Get a string literal's type by its
 	 * id (i.e. [i8 * {length}], where the length
 	 * is the string literal's length).
 	 *
 	 * @param id an <code>int</code> value
 	 * @return a <code>String</code> value
 	 */
 	private String getStringLiteralType(int id)
 	{
 		return "[" + String.valueOf(stringLiterals.get(id)) + " x i8]";
 	}
 
 	/**
 	 * Get the LLVM IR type corresponding to
 	 * a type from the three address code.
 	 *
 	 * @param type the type from the three address code
 	 * @return the corresponding LLVM IR type
 	 */
 	private String getIRType(Type.Kind type)
 	{
 		String irType = "";
 		switch(type)
 		{
 			case LONG:
 				/* Longs are 64bit signed integers */
 				irType = "i64";
 				break;
 			case DOUBLE:
 				/* Doubles are 64bit IEEE floats */
 				irType = "double";
 				break;
 			case BOOLEAN:
 				/* Booleans are 8bit signed integers */
 				irType = "i8";
 				break;
 			case STRING:
 				/* Strings are each a pointer to a string literal
 				   (which itself is a pointer to a memory segment
 				   of signed integers).*/
 				irType = "i8*";
 				break;
 		}
 
 		return irType;
 	}
 
 	/**
 	 * Gets the LLVM IR instruction for a binary TAC operator
 	 *
 	 * @param operator the binary TAC operator
 	 * @return the corresponding LLVM IR instruction
 	 */
 	private String getIRBinaryInstruction(Quadruple.Operator operator)
 	{
 		String irInst = "";
 
 		switch(operator)
 		{
 			case ADD_LONG:
 				irInst = "add";
 				break;
 			case ADD_DOUBLE:
 				irInst = "fadd";
 				break;
 			case SUB_LONG:
 				irInst = "sub";
 				break;
 			case SUB_DOUBLE:
 				irInst = "fsub";
 				break;
 			case MUL_LONG:
 				irInst = "mul";
 				break;
 			case MUL_DOUBLE:
 				irInst = "fmul";
 				break;
 			case DIV_LONG:
 				irInst = "sdiv";
 				break;
 			case DIV_DOUBLE:
 				irInst = "fdiv";
 				break;
 		}
 
 		return irInst;
 	}
 
 	/**
 	 * Gets a unique use identifier for
 	 * a variable; no two calls will return
 	 * the same use identifier.
 	 *
 	 * @param variable the variable's name
 	 * @return a free use identifier for the variable
 	 */
 	private String getUseIdentifierForVariable(String variable)
 	{
 		int ssa_suffix = variableUseCount.get(variable);
 		variableUseCount.put(variable, ssa_suffix + 1);
 		return "%" + variable + "." + String.valueOf(ssa_suffix);
 	}
 
 	/**
	 * Convert a three adress code boolean
	 * to an LLVM IR boolean.
	 * All other public functions expect booleans
	 * to be in the LLVM IR format.
	 *
	 * @param constant a TAC boolean
	 * @return the converted LLVM IR boolean
	 */
	public static String toIRBoolean(String bool)
	{
		if(bool.equals("#FALSE"))
		{
			return "#0";
		}
		else if(bool.equals("#TRUE"))
		{
			return "#1";
		}
		else
		{
			return bool;
		}
	}

	/**
 	 * Generates a new string literal from a <code>String</code>
 	 * value and returns its id (i.e. its position in the
 	 * list of string literals <code>stringLiterals</code>).
 	 *
 	 * @param literal the string to use
 	 * @return the new string literal's id
 	 */
 	private Integer addStringLiteral(String literal)
 	{
 		int length = literal.length() - 2;
 		int id = stringLiterals.size();
 		stringLiterals.add(length);
 
 		String type = getStringLiteralType(id);
 
 		String identifier = getStringLiteralIdentifier(id);
 
 		gen(identifier + " = alloca " + type);
 		gen("store " + type + " c" + literal + ", " + type + "* " + identifier);
 
 		return id;
 	}
 
 	/**
 	 * Sets a variable (which must be of the string primitive type)
 	 * to contain a pointer to a string literal.
 	 *
 	 * @param variable the variable's name
 	 * @param literalID the string literal's id
 	 */
 	private void addLoadStringLiteral(String variable, int literalID)
 	{
 		String variableIdentifier = "%" + variable;
 		String variableUseIdentifier = getUseIdentifierForVariable(variable);
 		String literalIdentifier = getStringLiteralIdentifier(literalID);
 		String literalType = getStringLiteralType(literalID);
 
 		gen(variableUseIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");
 		gen("store i8* " + variableUseIdentifier + ", i8** " + variableIdentifier);
 	}
 
 	/**
 	 * Adds a new variable and return its identifier.
 	 *
 	 * @param type the new variable's type
 	 * @param variable the new variable's name
 	 * @return the variable's identifier
 	 */
 	private String addNewVariable(Type.Kind type, String variable)
 	{
 		String irType = getIRType(type);
 		String variableIdentifier = "%" + variable;
 		variableUseCount.put(variable, 0);
 
 		gen(variableIdentifier + " = alloca " + irType);
 		return variableIdentifier;
 	}
 
 	/**
 	 * Adds a new variable and optionally sets its
 	 * initial value if given.
 	 *
 	 * @param type the new variable's type
 	 * @param variable the new variable's name
 	 * @param initializer the new variable's initial value
 	 */
 	public void addPrimitiveDeclare(Type.Kind type, String variable, String initializer)
 	{
 		addNewVariable(type, variable);
 
 		if(!initializer.equals(Quadruple.EmptyArgument))
 		{
 			addPrimitiveAssign(type, variable, initializer);
 		}
 	}
 
 	/**
 	 * Assigns either a constant value, or the value of
 	 * one variable (<code>src</code>) to another variable
 	 * (<code>dst</code>). Only assignments for identical
 	 * types are allowed.
 	 *
 	 * @param type the type of the assignment
 	 * @param dst the destination variable's name
 	 * @param src the source constant or the source variable's name
 	 */
 	public void addPrimitiveAssign(Type.Kind type, String dst, String src)
 	{
 		boolean constantSrc = false;
 		if(src.charAt(0) == '#')
 		{
 			src = src.substring(1);
 			constantSrc = true;
 		}
 
 		String irType = getIRType(type);
 		String dstIdentifier = "%" + dst;
 
 		if(constantSrc)
 		{
 			if(type == Type.Kind.STRING)
 			{
 				int id = addStringLiteral(src);
 				addLoadStringLiteral(dst, id);
 			}
 			else
 			{
 				gen("store " + irType + " " + src + ", " + irType + "* " + dstIdentifier);
 			}
 		}
 		else
 		{
 			String srcUseIdentifier = getUseIdentifierForVariable(src);
 			String srcIdentifier = "%" + src;
 			gen(srcUseIdentifier + " = load " + irType + "* " + srcIdentifier);
 			gen("store " + irType + " " + srcUseIdentifier + ", " + irType + "* " + dstIdentifier);
 		}
 	}
 
 	/**
 	 * Assigns the value of one variable (<code>src</code>)
 	 * to another variable (<code>dst</code>), where their types
 	 * must only be convertible, not identical.
 	 *
 	 * @param srcType the source variable's type
 	 * @param src the source variable's name
 	 * @param dstType the destination variable's type
 	 * @param dst the destination variable's name
 	 */
 	public void addPrimitiveConversion(Type.Kind srcType, String src, Type.Kind dstType, String dst)
 	{
 		String srcUseIdentifier = getUseIdentifierForVariable(src);
 		String dstUseIdentifier = getUseIdentifierForVariable(dst);
 		String srcIdentifier = "%" + src;
 		String dstIdentifier = "%" + dst;
 
 		if((srcType == Type.Kind.LONG) && (dstType == Type.Kind.DOUBLE))
 		{
 			gen(srcUseIdentifier + " = load i64* " + srcIdentifier);
 			gen(dstUseIdentifier + " = sitofp i64 " + srcUseIdentifier + " to double");
 			gen("store double " + dstUseIdentifier + ", double* " + dstIdentifier);
 		}
 		else if((srcType == Type.Kind.DOUBLE) && (dstType == Type.Kind.LONG))
 		{
 			gen(srcUseIdentifier + " = load double* " + srcIdentifier);
 			gen(dstUseIdentifier + " = fptosi double " + srcUseIdentifier + " to i64");
 			gen("store i64 " + dstUseIdentifier + ", i64* " + dstIdentifier);
 		}
 	}
 
 	/**
 	 * Adds a genric binary operation of two sources - each can
 	 * either be a constant or a variable - the result
 	 * of which will be stored in a variable (<code>dst</code>).
 	 * All types must be identical.
 	 *
 	 * @param op the binary operation to add
 	 * @param type the type of the binary operation
 	 * @param lhs the constant or name of the variable on the left hand side
 	 * @param rhs the constant or name of the variable on the right hand side
 	 * @param dst the destination variable's name
 	 */
 	public void addPrimitiveBinaryInstruction(Quadruple.Operator op, Type.Kind type, String lhs, String rhs, String dst)
 	{
 		String irType = getIRType(type);
 		String irInst = getIRBinaryInstruction(op);
 
 		if(lhs.charAt(0) == '#')
 		{
 			lhs = lhs.substring(1);
 		}
 		else
 		{
 			String lhsIdentifier = "%" + lhs;
 			lhs = getUseIdentifierForVariable(lhs);
 			gen(lhs + " = load " + irType + "* " + lhsIdentifier);
 		}
 
 		if(rhs.charAt(0) == '#')
 		{
 			rhs = rhs.substring(1);
 		}
 		else
 		{
 			String rhsIdentifier = "%" + rhs;
 			rhs = getUseIdentifierForVariable(rhs);
 			gen(rhs + " = load " + irType + "* " + rhsIdentifier);
 		}
 
 		String dstUseIdentifier = getUseIdentifierForVariable(dst);
 		String dstIdentifier = "%" + dst;
 
 		gen(dstUseIdentifier + " = " + irInst + " " + irType + " " + lhs + ", " + rhs);
 		gen("store " + irType + " " + dstUseIdentifier + ", " + irType + "* " + dstIdentifier);
 	}
 
 	/**
 	 * Adds the return instruction for the
 	 * main method.
 	 *
 	 * @param value the value to return (exit code)
 	 */
 	public void addMainReturn(String value)
 	{
 		if(value.charAt(0) == '#')
 		{
 			gen("ret i64 " + value.substring(1));
 		}
 		else
 		{
 			String valueUseIdentifier = getUseIdentifierForVariable(value);
 			String valueIdentifier = "%" + value;
 			gen(valueUseIdentifier + " = load i64* " + valueIdentifier);
 			gen("ret i64 " + valueUseIdentifier);
 		}
 	}
 }
