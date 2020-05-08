 /* =========================== MiniVM.java ===============================
 
 The Mini language is a modified small subset of Java/C. 
 A Mini program consists of a single possibly recursive function.
 The language has no declarations (implicit type is integer).
 
 MiniVM.java is a compiler-interpreter for Mini written in Java.
 The compiler generates code for a virtual machine, which is a modified 
 small subset of the Java VM (integer code instead of byte code).
 
 The one-pass compiler is implemented by a top-down recursive descent
 parser calling the methods of lexical analysis and code generation.
 The parser routines correspond to the grammar rules in EBNF notation.
 The regular right parts of EBNF are suitable to postfix generation.
 Lexical analysis takes advantage of the Java class StreamTokenizer.
 
 ====================== source language syntax (EBNF) =====================
 
 Program    = Function
 Function   = identifier "(" identifier ")" Block
 Block      = "{" [Statements] "}"
 Statements = Statement Statements
 Statement  = identifier "=" Expression ";" |
              "if" Condition Statement "else" Statement |
              "while" Condition Statement |
              "return" Expression ";" |
             "print" Expression ";"
              Block |
              ";"
 Condition  = "(" Expression ("=="|"!="|">"|"<") Expression ")"
 Expression = Term {("+"|"-") Term}
 Term       = Factor {("*"|"/") Factor}
 Factor     = number |
              identifier |
              "(" Expression ")" |
              identifier "(" Expression ")" 
 
 ================================ VM code =================================
 
  0    do nothing
  1 c  push constant c onto stack
  2 v  load variable v onto stack 
  3 v  store stack value into variable v
  4    add two top elements of stack, replace by result 
  5    subtract ...
  6    multiply ...
  7    divide ...
  8 a  jump to a if the two top elements of stack are equal
  9 a  jump if ... not equal
 10 a  jump if ... less or equal
 11 a  jump if ... greater or equal
 12 a  unconditional jump to a
 13 a  jump to subroutine start address a
 14    return from function
 15    stop execution
 16    print variable value
 
 ================================ example =================================
 
 source file "fac.mini":
 -----------------------
 fac(n) {
     if (n == 0)
         return 1;
     else
         return n * fac(n-1);
 }
 
 run:
 ----
 <Java Compiler> MiniVM.java
 <Java VM> MiniVM fac.mini 8
 
 VMCode: 13 3 15 2 1 1 0 9 14 1 1 14 12 25 2 1 2 1 1 1 5 13 3 6 14 0 
 Result: 40320
 
 ======================================================================= */
 
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

 /**
  *	Demonstrate the Mini VM by loading a script then execute it 
  */
 public class MiniVM {
 	static int code_max = 1000;
 	static int stack_max = 10000;
 
 	public static void main(String args[]) {
 		
 		if(args.length == 0){
 			System.out.println("Usage : MiniVM <script.mini> [arg]");
 			System.exit(1);
 		}
 		
 		try {
 			SymTab symbols = new SymTab();
 			Lexer lex = new Lexer(args[0], symbols);// init lexer
 			BinCode cg = new BinCode();
 			cg.init(code_max); 						// init generator
 			Parser parser = new Parser();
 			parser.program(lex, cg);				// call parser + assemble code
 			cg.show(); 								// show bytecode
 			VM vm = new VM(cg.getCode(), symbols, stack_max);// init VM
 			vm.setTrace(true);
 			if(args.length > 1){
 				int x = Integer.parseInt(args[1]);		// input data
 				int y = vm.exec(x); 					// call VM
 				System.out.println("Result: " + y);		// print result
 			} else {
 				vm.exec();
 				System.out.println("Done.");
 			}
 		} 
 		catch (Error e) {
 			System.out.println("error " + e.getMessage());
 		}
 	}
 }
 
 class Error extends Exception {
 	private static final long serialVersionUID = -7442464307199425656L;
 
 	public Error(String msg) {
 		super(msg);
 	}
 }
 
 //////////////////////////////////////////////////////////////////////////
 
 /**
  * Lexer Tokens
  */
 enum Token 
 {
     T_num (1), // number
     T_id  (2), // identifier
     T_eql (3), // ==
     T_neq (4), // !=
     T_grt ('>'),
     T_les ('<'),
     T_add ('+'),
     T_sub ('-'),
     T_mul ('*'),
     T_div ('/'),
     T_lbr ('('),
     T_rbr (')'),
     T_clb ('{'),
     T_crb ('}'),
     T_com (','),
     T_sem (';'),
     T_ass ('='),
     T_eof ('$'),
 
     kw0      (10),
     T_if     (10),
     T_else   (11),
     T_while  (12),
     T_return (13),
     T_print  (14),
     
     T_unknown (99);
     
     Token(int val){
 		this.value = val;
 	}
 
 	public int getValue(){
 		return value;
 	}
 
 	public static Token fromInt(int i) {
 		Token tok = intToTokenMap.get(Integer.valueOf(i));
 	    if (tok == null) 
 	        return T_unknown;
 	    return tok;
 	}
 
 	private final int value;
 	private static final Map<Integer, Token> intToTokenMap = new HashMap<Integer, Token>();
 
 	static {
 	    for (Token tok : Token.values()) {
 	        intToTokenMap.put(tok.value, tok);
 	    }
 	}
 }
 
 /**
  *	VM opcodes 
  */
 enum OpCode
 {
 	M_nop		(0, false, "do nothing"), 
 	M_push		(1, true, "push constant"),
     M_load		(2, true, "push local var"),
     M_pop		(3, true, "pop"),
     M_add		(4, false, "pop x2, add, push"),
     M_sub		(5, false, "pop x2, sub, push"),
     M_mul		(6, false, "pop x2, mul, push"),
     M_div		(7, false, "pop x2, div, push"),
     M_if_cmpeq	(8, true, "pop x2, je"),
     M_if_cmpne	(9, true, "pop x2, jne"),
     M_if_cmple	(10, true, "pop x2, jle"),
     M_if_cmpge	(11, true, "pop x2, jge"),
     M_goto		(12, true, "goto"),
     M_jsr		(13, true, "gosub"),
     M_ret		(14, false, "return"),
     M_halt		(15, false, "stop execution"),
     M_pval		(16, false, "print value");
 
 	OpCode(int val, boolean arg, String desc){
 		this.value = val;
 		this.arg = arg;
 		this.desc = desc;
 	}
 
 	public int getValue(){
 		return value;
 	}
 	
 	public boolean hasArg(){
 		return arg;
 	}
 	
 	public String getDesc(){
 		return desc;
 	}
 	
 	public static OpCode fromInt(int i) {
 		OpCode op = intToOpcodeMap.get(Integer.valueOf(i));
 	    //if (type == null) 
 	    //    return Code.UNKNOWN;
 		assert(op != null);
 	    return op;
 	}
 
 	private final int value;
 	private final boolean arg;
 	private final String desc;
 	private static final Map<Integer, OpCode> intToOpcodeMap = new HashMap<Integer, OpCode>();
 
 	static {
 	    for (OpCode op : OpCode.values()) {
 	        intToOpcodeMap.put(op.value, op);
 	    }
 	}
 }
 
 //////////////////////////////////////////////////////////////////////////
 
 /**
  *  naive implementation of a symbol table 
  *  (should be replaced by Dictionary / HashTable)
  *
  */
 class SymTab {
 	private String t[] = new String[100]; // table array
 	private int countSym = 0; // number of variables
 
 	int enter(String s) {
 		int i;
 		//look for symbol
 		for (i = 0; i < countSym && !s.equals(t[i]); i++) {
 		}
 		if (i == countSym) {
 			//if we reached the end of table, add the new symbol
 			t[i] = s;
 			countSym++;
 		}
 		return i;	//return its value
 	}
 	
 	/**
 	 * get the size of the symbol table
 	 * @return
 	 */
 	public int getCount(){
 		return countSym;
 	}
 }
 
 /**
  * lexical analysis
  */
 class Lexer {
 	private int num_val;	// attribute of number
 	private int id_val;		// attribute of id (index of symbol table)
 	final static String kw[] = { "if", "else", "while", "return", "print" };
 	private Reader source;
 	private StreamTokenizer st;
 	private int tok;		// tokenizer token
 	private SymTab symbols;
 
 	/**
 	 * Initialize the parser
 	 * 
 	 * @param file_name
 	 *            the script to parse
 	 * @throws Error
 	 */
 	Lexer(String file_name, SymTab symbols) throws Error {
 		try {
 			source = new BufferedReader(new FileReader(file_name));
 		} catch (FileNotFoundException e) {
 			throw new Error("file not found" + " " + e.getMessage());
 		}
 		st = new StreamTokenizer(source);
 		st.ordinaryChar('/');
 		st.ordinaryChar('-');
 		this.symbols = symbols;
 	}
 
 	/**
 	 * return the next token from stream
 	 * @return a Token
 	 * @throws Error
 	 */
 	public Token scan() throws Error {
 		try {
 			tok = st.nextToken();
 			switch (tok) {
 			case StreamTokenizer.TT_EOF:
 				return Token.T_eof;
 			case StreamTokenizer.TT_NUMBER:
 				num_val = (int) st.nval;
 				return Token.T_num;
 			case StreamTokenizer.TT_WORD:
 				int i = look_kw(st.sval);
 				if (i >= 0)
 					return Token.fromInt(Token.kw0.getValue() + i);
 				else {
 					id_val = symbols.enter(st.sval);
 					return Token.T_id;
 				}
 			default:
 				char c = (char) tok;
 				switch (c) {
 				case '=':
 					if ((char) st.nextToken() == '=')
 						return Token.T_eql;
 					else
 						st.pushBack();
 					break;
 				case '!':
 					if ((char) st.nextToken() == '=')
 						return Token.T_neq;
 					else
 						st.pushBack();
 					break;
 				}
 			}
 		} catch (IOException e) {
 			throw new Error("IO" + " " + e.getMessage());
 		}
 		return Token.fromInt(tok);
 	}
 
 	/**
 	 * naive implementation of a keyword table
 	 * @param s as string to check
 	 * @return the index of keyword or -1 if not found
 	 */
 	private static int look_kw(String s) {
 		int i;
 		for (i = 0; i < kw.length && !s.equals(kw[i]); i++) {
 		}
 		if (i < kw.length)
 			return i;
 		else
 			return -1;
 	}
 	
 	/**
 	 * get the current identifier
 	 * @return
 	 */
 	public int getID(){
 		return id_val;
 	}
 	
 	/**
 	 * get the current numeric value
 	 * @return
 	 */
 	public int getNumeric(){
 		return num_val;
 	}
 }
 
 //////////////////////////////////////////////////////////////////////////
 
 /**
  *  Parser and code emitter
  */
 class Parser {
 	private static Token curToken;
 	private Lexer lx;
 	private BinCode bin;
 
 	/**
 	 * Entry point for grammar
 	 * 
 	 * @param lex a Lexer object that provides tokens
 	 * @param bin 
 	 * @throws Error
 	 */
 	void program(Lexer lex, BinCode bin) throws Error {
 		this.lx = lex;
 		this.bin = bin;
 
 		next();
 		bin.start();
 		function();
 	}
 
 	private void function() throws Error {
 		match(Token.T_id);
 		match(Token.T_lbr);
 		match(Token.T_id);
 		match(Token.T_rbr);
 		block();
 		//bin.instr(OpCode.M_nop); //TODO: why ??
 	}
 
 	private void block() throws Error {
 		match(Token.T_clb);
 		statements();
 		match(Token.T_crb);
 	}
 
 	private void statements() throws Error {
 		if (curToken != Token.T_crb) {
 			statement();
 			statements();
 		}
 	}
 
 	private void statement() throws Error {
 		int p1, p2;
 		switch (curToken) {
 			case T_id:
 				int adr = lx.getID();
 				next();
 				match(Token.T_ass);
 				expression();
 				bin.instr(OpCode.M_pop, adr);
 				match(Token.T_sem);
 				break;
 			case T_if:
 				next();
 				condition();
 				p1 = bin.getCurOffset() - 1;
 				statement();
 				bin.instr(OpCode.M_goto, 0);
 				p2 = bin.getCurOffset() - 1;
 				match(Token.T_else);
 				bin.setjump(p1);
 				statement();
 				bin.setjump(p2);
 				break;
 			case T_while:
 				next();
 				p1 = bin.getCurOffset();
 				condition();
 				p2 = bin.getCurOffset() - 1;
 				statement();
 				bin.instr(OpCode.M_goto, p1);
 				bin.setjump(p2);
 				break;
 			case T_return:
 				next();
 				expression();
 				bin.instr(OpCode.M_ret);
 				match(Token.T_sem);
 				break;
 			case T_print:
 				next();
 				expression();
 				if (curToken == Token.T_num){
 					bin.instr(OpCode.M_push, lx.getNumeric());
 				}else if (curToken == Token.T_id){
 					int id = lx.getID();
 					bin.instr(OpCode.M_load, id);
 				}
 				bin.instr(OpCode.M_pval);
 				match(Token.T_sem);
 				break;
 			case T_clb:
 				block();
 				break;
 			case T_sem:
 				next();
 				break;
 			default:
 				throw new Error("statement " + curToken);
 		}
 	}
 
 	private void condition() throws Error {
 		match(Token.T_lbr);
 		expression();
 		Token rop = curToken;
 		next();
 		expression();
 		match(Token.T_rbr);
 		switch (rop) {
 		case T_eql:
 			bin.instr(OpCode.M_if_cmpne, 0);
 			break;
 		case T_neq:
 			bin.instr(OpCode.M_if_cmpeq, 0);
 			break;
 		case T_grt:
 			bin.instr(OpCode.M_if_cmple, 0);
 			break;
 		case T_les:
 			bin.instr(OpCode.M_if_cmpge, 0);
 			break;
 		default:
 			throw new Error("condition " + curToken);
 		}
 	}
 
 	private void expression() throws Error {
 		term();
 		while (curToken == Token.T_add || curToken == Token.T_sub) {
 			switch (curToken) {
 			case T_add:
 				next();
 				term();
 				bin.instr(OpCode.M_add);
 				break;
 			case T_sub:
 				next();
 				term();
 				bin.instr(OpCode.M_sub);
 				break;
 			}
 		}
 	}
 
 	private void term() throws Error {
 		factor();
 		while (curToken == Token.T_mul || curToken == Token.T_div) {
 			switch (curToken) {
 			case T_mul:
 				next();
 				term();
 				bin.instr(OpCode.M_mul);
 				break;
 			case T_div:
 				next();
 				term();
 				bin.instr(OpCode.M_div);
 				break;
 			}
 		}
 	}
 
 	private void factor() throws Error {
 		switch (curToken) {
 		case T_num:
 			bin.instr(OpCode.M_push, lx.getNumeric());
 			next();
 			break;
 		case T_id:
 			int id = lx.getID();
 			next();
 			if (curToken != Token.T_lbr)
 				bin.instr(OpCode.M_load, id);
 			else {
 				next();
 				expression();
 				match(Token.T_rbr);
 				bin.instr(OpCode.M_jsr, bin.getStartAddr());
 			}
 			break;
 		case T_lbr:
 			next();
 			expression();
 			match(Token.T_rbr);
 			break;
 		default:
 			throw new Error("expression " + curToken);
 		}
 	}
 
 	/**
 	 * get the next token from lexer and store it as current
 	 * @throws Error
 	 */
 	private void next() throws Error {
 		curToken = lx.scan();
 	}
 
 	/** if the curent token matches the given one, get the next
 	 * else we have a syntax error
 	 * @param t a Token to check for
 	 * @throws Error
 	 */
 	private void match(Token t) throws Error {
 		if (curToken == t)
 			next();
 		else
 			throw new Error("syntax " + curToken);
 	}
 }
 
 //////////////////////////////////////////////////////////////////////////
 
 /**
  *  Helper class to store compiled code
  *
  */
 class BinCode {
 	private int code[]; // target code
 	private int pc; // program counter
 	private int start_adr; // start address
 
 	public void init(int code_max) {
 		code = new int[code_max];
 		pc = 0;
 	}
 
 	public int getCurOffset() {
 		return pc;
 	}
 
 	public int getStartAddr() {
 		return start_adr;
 	}
 
 	public int[] getCode() {
 		return code;
 	}
 
 	public void start() {
 		instr(OpCode.M_jsr, 3);
 		instr(OpCode.M_halt);
 		start_adr = pc;
 	}
 
 	/**
 	 * Assemble a single opcode
 	 * 
 	 * @param instruction
 	 *            the opcode
 	 */
 	public void instr(OpCode instruction) {
 		code[pc] = instruction.getValue();
 		pc = pc + 1;
 	}
 
 	/**
 	 * Assemble an instruction + operand
 	 * 
 	 * @param instruction
 	 *            the opcode
 	 * @param operand
 	 *            the argument to instruction
 	 */
 	public void instr(OpCode instruction, int operand) {
 		code[pc] = instruction.getValue();
 		code[pc + 1] = operand;
 		pc = pc + 2;
 	}
 
 	public void setjump(int adr) {
 		code[adr] = pc;
 	}
 
 	/**
 	 * Dump the compiled program
 	 */
 	public void show() {
 		System.out.print("VMCode:\n");
 		for (int i = 0; i < pc; i++) {
 			OpCode oc = OpCode.fromInt(code[i]);
 			if (oc.hasArg())
 				System.out.println(String.format(
 						"%04d  %02d %02d  %-12s%-6d%s", i, code[i],
 						code[i + 1], oc.toString(), code[++i], oc.getDesc()));
 			else
 				System.out.println(String.format("%04d  %02d     %-18s%s", i,
 						code[i], oc.toString(), oc.getDesc()));
 		}
 
 		System.out.println();
 	}
 }
 
 /**
  * 
  * virtual machine engine
  * 
 */
 class VM {
 	private int p[]; // program code
 	private int ip; // instruction pointer
 	private int s[]; // stack
 	private int sp; // stack pointer
 	private int fp; // frame pointer
 	private int fs; // frame size
 	
 	private boolean bTrace = false;
 
 	VM(int code[], SymTab symbols, int stack_max) {
 		init(code, symbols, stack_max);
 	}
 	
 	public void setTrace(boolean trace){
 		this.bTrace = trace;
 	}
 
 	void init(int code[], SymTab symbols, int stack_max) {
 		p = code;
 		ip = 0;
 		s = new int[stack_max];
 		sp = 0;
 		fp = 0;
 		fs = symbols.getCount();
 	}
 
 	/**
 	 * Start the VM, the argument is pushed onto the stack
 	 * 
 	 * @param arg an initial value that is pushed into the stack
 	 * @return the last value on the stack when program stops
 	 * @throws Error
 	 */
 	int exec(int arg) throws Error {
 		s[0] = arg;
 		sp++;
 		exec();
 		return s[0];
 	}
 	
 	/**
 	 * Start the VM at initial address
 	 * 
 	 * @throws Error
 	 */
 	void exec() throws Error {
 		OpCode op;
 //		if(bTrace){
 //			//System.out.print("Symbols : ");
 //			//for(int s: symb)
 //			System.out.print("                     ");
 //			dumpStack();
 //			System.out.println();
 //		}
 			
 		//while ((op = OpCode.fromInt(p[ip])) != OpCode.M_halt) {
 		do {
 			op = OpCode.fromInt(p[ip]);
 			if(bTrace){
 				System.out.print("                     ");
 				dumpStack();
 				System.out.println();
 				System.out.println(String.format("%04d  %-15s", ip, op.toString() + (op.hasArg() ? String.format(" %02d", p[ip+1]) : "")));
 			}
 			switch (op) {
 				case M_nop:
 					ip++;
 					break;
 				case M_push:
 					s[sp] = p[ip + 1];
 					sp++;
 					ip = ip + 2;
 					break;
 				case M_load:
 					s[sp] = s[fp + p[ip + 1]];
 					sp++;
 					ip = ip + 2;
 					break;
 				case M_pop:
 					s[fp + p[ip + 1]] = s[sp - 1];
 					sp--;
 					ip = ip + 2;
 					break;
 				case M_add:
 					sp--;
 					s[sp - 1] = s[sp - 1] + s[sp];
 					ip++;
 					break;
 				case M_sub:
 					sp--;
 					s[sp - 1] = s[sp - 1] - s[sp];
 					ip++;
 					break;
 				case M_mul:
 					sp--;
 					s[sp - 1] = s[sp - 1] * s[sp];
 					ip++;
 					break;
 				case M_div:
 					sp--;
 					s[sp - 1] = s[sp - 1] / s[sp];
 					ip++;
 					break;
 				case M_if_cmpeq:
 					sp = sp - 2;
 					ip = s[sp] == s[sp + 1] ? p[ip + 1] : ip + 2;
 					break;
 				case M_if_cmpne:
 					sp = sp - 2;
 					ip = s[sp] != s[sp + 1] ? p[ip + 1] : ip + 2;
 					break;
 				case M_if_cmple:
 					sp = sp - 2;
 					ip = s[sp] <= s[sp + 1] ? p[ip + 1] : ip + 2;
 					break;
 				case M_if_cmpge:
 					sp = sp - 2;
 					ip = s[sp] >= s[sp + 1] ? p[ip + 1] : ip + 2;
 					break;
 				case M_goto:
 					ip = p[ip + 1];
 					break;
 				case M_jsr:
 					s[sp] = ip + 2; 		// save return address
 					s[sp + 1] = fp; 		// save fp
 					fp = sp + 2; 			// set fp
 					sp = fp + fs; 			// set sp
 					s[fp + 1] = s[fp - 3];	// copy argument
 					ip = p[ip + 1];			// goto start address
 					break;
 				case M_ret:
 					s[fp - 3] = s[sp - 1];	// copy return value
 					sp = fp - 2;			// reset sp
 					fp = s[sp + 1];			// reset fp
 					ip = s[sp];				// goto return address
 					break;
 				case M_pval:
 					System.out.println(s[sp-1]);
 					sp--;
 					ip++;
 					break;
 				case M_halt:
 					//nothing, just print stack if needed
 					break;
 				default:
 					throw new Error("illegal vm code " + p[ip]);
 			} 
 		} while (op != OpCode.M_halt);
 //		if(bTrace){
 //			System.out.print("                     ");
 //			dumpStack();
 //			System.out.println();
 //		}
 	}
 
 	/**
 	 * Debugging helper : list the stack
 	 */
 	private void dumpStack() {
 		for(int adr = 0; adr < sp; adr++){
 			//stack element separator : to show frame pointer & local vars
 			if(adr == fp)
 				System.out.print('|');
 			else if(adr == fp + fs)
 				System.out.print('.');
 			else
 				System.out.print(' ');
 			System.out.print(String.format("%02d", s[adr]));
 		}
 	}
 }
