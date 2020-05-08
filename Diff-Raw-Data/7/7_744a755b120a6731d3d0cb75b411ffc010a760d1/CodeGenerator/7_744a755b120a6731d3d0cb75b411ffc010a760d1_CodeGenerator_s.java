 package compiler.back.codeGen;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import compiler.DLX;
 import compiler.back.regAloc.RealRegister;
 import compiler.back.regAloc.RealRegisterPool;
 import compiler.back.regAloc.VirtualRegister;
 import compiler.ir.cfg.*;
 import compiler.ir.instructions.*;
 
 public class CodeGenerator {
 
 //	private static final int MAX_REG = 27;
 	private static final boolean DEBUG = true;
 	private HashMap<Integer, String> DEBUGMESG;
 	
 	
 	private static final int FrameP = 28;
 	private static final int StackP = 29;
 	private static final int GlobalV = 30;
 	private static final int ReturnA = 31;
 
 	// Mnemonic-to-Opcode mapping
 	static final String mnemo[] = { "ADD", "SUB", "MUL", "DIV", "MOD", "CMP",
 			"ERR", "ERR", "OR", "AND", "BIC", "XOR", "LSH", "ASH", "CHK",
 			"ERR", "ADDI", "SUBI", "MULI", "DIVI", "MODI", "CMPI", "ERRI",
 			"ERRI", "ORI", "ANDI", "BICI", "XORI", "LSHI", "ASHI", "CHKI",
 			"ERR", "LDW", "LDX", "POP", "ERR", "STW", "STX", "PSH", "ERR",
 			"BEQ", "BNE", "BLT", "BGE", "BLE", "BGT", "BSR", "ERR", "JSR",
 			"RET", "RDI", "WRD", "WRH", "WRL", "ERR", "ERR", "ERR", "ERR",
 			"ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR",
 			"ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR",
 			"ERR", "ERR", "ERR", "ERR" };
 	static final int ADD = 0;
 	static final int SUB = 1;
 	static final int MUL = 2;
 	static final int DIV = 3;
 	static final int MOD = 4;
 	static final int CMP = 5;
 	static final int OR = 8;
 	static final int AND = 9;
 	static final int BIC = 10;
 	static final int XOR = 11;
 	static final int LSH = 12;
 	static final int ASH = 13;
 	static final int CHK = 14;
 	static final int ADDI = 16;
 	static final int SUBI = 17;
 	static final int MULI = 18;
 	static final int DIVI = 19;
 	static final int MODI = 20;
 	static final int CMPI = 21;
 	static final int ORI = 24;
 	static final int ANDI = 25;
 	static final int BICI = 26;
 	static final int XORI = 27;
 	static final int LSHI = 28;
 	static final int ASHI = 29;
 	static final int CHKI = 30;
 	static final int LDW = 32;
 	static final int LDX = 33;
 	static final int POP = 34;
 	static final int STW = 36;
 	static final int STX = 37;
 	static final int PSH = 38;
 	static final int BEQ = 40;
 	static final int BNE = 41;
 	static final int BLT = 42;
 	static final int BGE = 43;
 	static final int BLE = 44;
 	static final int BGT = 45;
 	static final int BSR = 46;
 	static final int JSR = 48;
 	static final int RET = 49;
 	static final int RDI = 50;
 	static final int WRD = 51;
 	static final int WRH = 52;
 	static final int WRL = 53;
 	static final int ERR = 63;
 
 	private List<CFG> CFGs;
 
 	private CFG mainCFG;
 	private CFG currentCFG;
 
 	private int pc;
 	private ArrayList<Integer> nativeCode;
 	private String outFile;
 	private Stack<Integer> fixup;
 	private Stack<Integer> functionFixup;
 	private Stack<CFG> functionsToFixup;
 
 	public CodeGenerator(List<CFG> CFGs) {
 		this.CFGs = CFGs;
 	}
 
 	public void generateCode(String outFile) {
 		this.outFile = outFile;
 		setupProgram();
 		setupGlobals();
 
 		int start = pc;// Start after var setup
 
 		if (CFGs.size() > 1) {
 			UnCondBraFwd(0);
 		}
 
 		processFunc();
 
 		if (CFGs.size() > 1) {
 			Fixup(start);
 		}
 
 		processMain();
 
 		fixupFunctions();
 
 		writeBin(outFile);
 	}
 
 	private void processFunc() {
 		for (CFG function : CFGs) {
 			// Process main separately
 			if (function.label.equals("main")) {
 				continue;
 			}
 			currentCFG = function;
 			function.setStartLine(pc);
 
 			Iterator<BasicBlock> blocks = function.topDownIterator();
 			while (blocks.hasNext()) {
 				BasicBlock currentBlock = (BasicBlock) blocks.next();
 				currentBlock.startLine = pc;// Easy loopbacks
 
 				preBlockProcessing(currentBlock);
 
 				// Process block's instructions
 				Iterator<Instruction> instructions = currentBlock
 						.getInstructionsIterator();
 				while (instructions.hasNext()) {
 					Instruction instruction = (Instruction) instructions.next();
 					produceCode(instruction);
 				}
 
 				postBlockProcessing(currentBlock);
 			}
 
 		}
 	}
 
 	private void processMain() {
 		currentCFG = mainCFG;
 		Iterator<BasicBlock> blocks = mainCFG.topDownIterator();
 		while (blocks.hasNext()) {
 			BasicBlock currentBlock = (BasicBlock) blocks.next();
 			currentBlock.startLine = pc;// Easy loopbacks
 
 			preBlockProcessing(currentBlock);
 
 			// Process block's instructions
 			Iterator<Instruction> instructions = currentBlock
 					.getInstructionsIterator();
 			while (instructions.hasNext()) {
 				Instruction instruction = (Instruction) instructions.next();
 				produceCode(instruction);
 			}
 
 			postBlockProcessing(currentBlock);
 		}
 
 		PutF2(RET, 0, 0, 0);// END MAIN!
 	}
 
 	private void preBlockProcessing(BasicBlock currentBlock) {
 		// IF fixup
 		if (currentBlock.label.equals("else")
 				&& !currentBlock.isInstructionsEmpty()) {
 			int elseBranch = fixup.pop();
 			UnCondBraFwd(0);
 			Fixup(elseBranch);
 		} else if (currentBlock.label.equals("else")
 				&& currentBlock.isInstructionsEmpty()) {
 			Fixup(fixup.pop());
 		}
 
 		if (currentBlock.label.equals("fi-join")) {
 			BasicBlock elseCheck = null;
 			for (BasicBlock block : currentBlock.pred) {
 				if (block.label.equals("else")) {
 					elseCheck = block;
 					break;
 				}
 			}
 			if (elseCheck != null && !elseCheck.isInstructionsEmpty()) {
 				FixAll(elseCheck.startLine);
 			}
 		}
 		
 		// Functions intro
 		if (currentBlock.label.equals("start")) {
 			AddDebug((currentCFG.isFunc()?"FUNCTION: ":"PROCEDURE: ")+currentCFG.label+"\n");
 			if (!currentCFG.label.equals("main")){
 				// Store Return Address
 				int paramNum = currentCFG.getParamNum();
 				PutF1(STW, ReturnA, FrameP, (paramNum + 2) * 4);
 			}
 		}
 
 	}
 
 	private void produceCode(Instruction instruction) {
 		// TODO remove when real allocator exists (reg %MAX_REG)+1
 		AddDebug(instruction.toString());
 		
 		if (instruction instanceof Immediate) {
 			Immediate ins = (Immediate) instruction;
 			PutF1(ADDI, useReg(ins.outputOp) , 0, ins.value);
 
 		} else if (instruction instanceof StoreValue) {
 			StoreValue ins = (StoreValue) instruction;
 			// Global
 			store(ins);
 			// TODO arrays, locals
 
 		} else if (instruction instanceof LoadValue) {
 			LoadValue ins = (LoadValue) instruction;
 			load(ins);
 
 		} else if (instruction instanceof ControlFlowInstr) {
             ControlFlowInstr ins = (ControlFlowInstr) instruction;
             CondNegBraFwd(ins);
 
         } else if (instruction instanceof Move) {
             Move ins = (Move) instruction;
         	List<VirtualRegister> operands = ins.getInputOperands();
         	
         	//TODO find bug
         	
             PutF1(ADDI, useReg(Instruction.resolve(ins).outputOp), useReg(operands.get(0)), 0);
 
         } else if (instruction instanceof Index) {
         	Index ins = (Index) instruction;
         	List<VirtualRegister> operands = ins.getInputOperands();
             PutF1(SUB, useReg(ins.outputOp), useReg(operands.get(0)), useReg(operands.get(1)));
 
         } else if (instruction instanceof Return) {
 			Return ins = (Return) instruction;
 			int paramNum = currentCFG.getParamNum();
 			boolean isFunc = currentCFG.isFunc();
 
 			if (isFunc) {
 				PutF1(STW, useReg(ins.inputOps.get(0)) , FrameP, 4);
 			}
 
 			// Load RA
 			PutF1(LDW, ReturnA, FrameP, (paramNum + 2) * 4);
 			// Go To RA
 			PutF1(RET, 0, 0, ReturnA);
 
 		} else if (instruction instanceof ArithmeticBinary) {
 			ArithmeticBinary ins = (ArithmeticBinary) instruction;
 			List<VirtualRegister> inputs = ins.getInputOperands();
 			// System.err.println(ins.left.getClass());
 			PutF2(opCode(ins), useReg(ins.outputOp) ,
 					useReg(inputs.get(0)) ,
 					useReg(inputs.get(1)) );
 
 		} else if (instruction instanceof Call) {
 			Call ins = (Call) instruction;
 
 			// Call other functions
 			CFG callee = null;
 			for (CFG func : CFGs) {
 				if (func.label.equals(ins.function.ident)) {
 					callee = func;
 					break;
 				}
 			}
 			if (callee != null) {
 				execFunction(ins, callee);
 			}
 
 			// Built in functions can be overridden
 			if (ins.function.ident.equalsIgnoreCase("outputnum")) {
 				PutF2(WRD, 0,
 						useReg(ins.args.get(0).outputOp) , 0);
 			} else if (ins.function.ident.equalsIgnoreCase("outputnewline")) {
 				PutF2(WRL, 0, 0, 0);
 			} else if (ins.function.ident.equalsIgnoreCase("inputnum")) {
 				PutF2(RDI, useReg(ins.outputOp) , 0, 0);
 			}
 
 		}
 
 	}
 
 	private void postBlockProcessing(BasicBlock currentBlock) {
 
 		// While loop fixup
 		BasicBlock whileStart = null;
 		for (BasicBlock block : currentBlock.succ) {
 			if (currentBlock.depth > block.depth) {
 				whileStart = block;
 				break;
 			}
 		}
 		if (whileStart != null) {
 			PutF1(BEQ, 0, 0, whileStart.startLine - pc);
 			Fixup(fixup.pop());
 		}
 
 		if (currentBlock.label.equals("exit") && !currentCFG.label.equals("main")) {
 			if (!currentCFG.isFunc()) {
 				int paramNum = currentCFG.getParamNum();
 				// Load RA
 				PutF1(LDW, ReturnA, FrameP, (paramNum + 2) * 4);
 				// Pop vars
 				PutF1(ADDI, StackP, FrameP, 0);
 				PutF2(RET, 0, 0, ReturnA);// END Procedure!
 			}
 		}
 	}
 
 	private void execFunction(Call ins, CFG callee) {
 		int paramNum = callee.getParamNum();
 		int varNum = callee.getVarNum();
 		int arraySize = callee.getArraysSize();
 
 		boolean isFunc = callee.isFunc();
 		// TODO WORK IN PROGRESS
 
 		
 		// Store old FP
 		Push(FrameP);
 
 		// Put RA space
 		Push(0);
 
 		// Put Param, reverse order
 		if (paramNum > 0) {
 			List<VirtualRegister> parms = ins.getInputOperands();
 			for( int i = parms.size()-1; i>=0;i--){
 			
 				// load word to mem
 				//TODO load params to mem
 				Push(useReg(parms.get(i)));
 			}
 		}
 
 		// Put RetVal on stack
 		Push(0);
 
 		// Save FP
 		PutF1(ADDI, FrameP, StackP, 0);
 
 		// Put vars
 		if (varNum > 0) {
 			for (int i = 0; i < varNum; i++) {
 				Push(0);
 			}
 		}
 
 		if (arraySize > 0) {
 			for (int i = 0; i < arraySize; i++) {
 				Push(0);
 			}
 		}
 
 		if (callee.getStartLine() < 0) {
 			// jump to function
 			PutF1(JSR, 0, 0, callee.getStartLine() * 4);
 		} else {
 			// delay pointers till function is processed
 			functionFixup.push(pc);
 			functionsToFixup.push(callee);
 			PutF1(JSR, 0, 0, 0);
 		}
 		
 		// Function Happens HERE
 
 		// pop vars
 		PutF1(ADDI, StackP, FrameP, 0);
 
 		// Load prev FP
 		PutF1(LDW, FrameP, FrameP, (paramNum + 3) * 4);
 
 		// IF func get return val
 		if (isFunc) {
 			// put ret val in x
 			Pop(useReg(ins.outputOp));
 		} else {
 			// remove empty return val
 			Pop();
 		}
 
 		// Pop Parms
 		// TODO shorten
 		if (paramNum > 0) {
 			for (int i = paramNum - 1; i >= 0; i--) {
 				Pop();
 			}
 		}
 
 		// POP RA
 		Pop();
 
 		// Restore oldFP
 		Pop(FrameP);
 	}
 
 	private void setupProgram() {
 		nativeCode = new ArrayList<Integer>();
 		fixup = new Stack<Integer>();
 		functionFixup = new Stack<Integer>();
 		functionsToFixup = new Stack<CFG>();
 		DEBUGMESG = new HashMap<Integer, String>();
 		pc = 0;
 		// Setup Frame Pointer at global value pointer
 		PutF1(ADDI, FrameP, GlobalV, 0);
 		PutF1(ADDI, StackP, GlobalV, 0);
 		for (CFG cfg : CFGs) {
 			if (cfg.label.equals("main")) {
 				mainCFG = cfg;
 			}
 		}
 	}
 
 	private void setupGlobals() {
 		// Zero out memory, could be reduced
 		// Setup Global Variables
 		for (int i = 0; i < mainCFG.getVarNum(); i++) {
 			// Allocate memory
 			PutF1(STW, 0, StackP, 0);
 			PutF1(ADDI, StackP, StackP, -4);
 		}
 		// Setup Global Arrays
 		for (int i = 0; i < mainCFG.getArraysSize(); i++) {
 			// Allocate memory
 			PutF1(STW, 0, StackP, 0);
 			PutF1(ADDI, StackP, StackP, -4);
 		}
 	}
 
 	private void fixupFunctions() {
 		while (!functionsToFixup.empty()) {
 			CFG fixee = functionsToFixup.pop();
 			int loc = functionFixup.pop();
 			nativeCode.set(loc,
 					JSR << 26 | 0 << 21 | 0 << 16 | fixee.getStartLine() * 4
 							& 0xffff);
 		}
 	}
 
 	private void load(LoadValue ins) {
 		if (ins.symbol == null && ins.address != null) {
 			// Load by address
 //			System.err.println("Check: " + ins);
 			List<VirtualRegister> address = ins.getInputOperands();
 			if (address == null || address.isEmpty()) {
 				return;
 			}
 //            PutF1(LDX, useReg(address.get(0)), GlobalV, useReg(ins.outputOp) );
             PutF1(LDX, useReg(ins.outputOp), useReg(address.get(0)), 0 );
 		} else if (currentCFG.containsVar(ins.symbol.ident) && !currentCFG.label.equals("main")) {
 			// Var
 			PutF1(LDW, useReg(ins.outputOp), FrameP, -GetVarAddress(ins.symbol.ident));
 		} else if (currentCFG.containsParam(ins.symbol.ident) && !currentCFG.label.equals("main")) {
 			// Param
 			PutF1(LDW, useReg(ins.outputOp), FrameP, 8 + GetParamAddress(ins.symbol.ident));
 		} else if (mainCFG.containsVar(ins.symbol.ident)) {
 			// Global Var
 			PutF1(LDW, useReg(ins.outputOp), GlobalV,
 					-GetVarAddress(ins.symbol.ident));
 		} else if (currentCFG.containsArray(ins.symbol.ident) && !currentCFG.label.equals("main")) {
 			// Array base address
 //			PutF1(ADDI, useReg(ins.outputOp), FrameP , 0 );
 //			PutF1(ADD, useReg(ins.outputOp), useReg(ins.outputOp), -GetArrayAddress(ins.symbol.ident) );
 	          PutF1(ADDI, useReg(ins.outputOp), FrameP, -GetArrayAddress(ins.symbol.ident) );
 		} else if (mainCFG.containsArray(ins.symbol.ident)) {
 			// Global Array base address
 //			PutF1(ADDI, useReg(ins.outputOp), GlobalV , 0 );
 //          PutF1(ADD, useReg(ins.outputOp), useReg(ins.outputOp), -GetArrayAddress(ins.symbol.ident) );
           PutF1(ADDI, useReg(ins.outputOp), GlobalV, -GetArrayAddress(ins.symbol.ident) );
 		}
 	}
 	
 	private int useReg(VirtualRegister vReg) {
 		// IF !rReg THEN return spill(rReg) ELSE return rReg.regNumber
 		if(vReg.rReg == null){
 			return spill(vReg);
 		}
 		return vReg.rReg.regNumber;
 	}
 
 	private int spill(VirtualRegister rReg) {
 		//TODO if loading then load from mem to temp registers
 		//TODO if storing then save to temp register and move to mem
 		//Read offset loc
 		RealRegister tempReg = new RealRegister(9);
 		System.err.println("spill: "+tempReg.regNumber);
 		return tempReg.regNumber;
 	}
 
 	private void store(StoreValue ins) {
 		if (ins.symbol == null) {
 			// Store by address
 			List<VirtualRegister> address = ins.getInputOperands();
 			if (address == null || address.isEmpty()) {
 				return;
 			}
 			
 			//TODO fix bug
 			System.err.println("store: " + ins);
 			
 			PutF1(STX, useReg(address.get(0)), useReg(address.get(1)), 0);
 		} else if (currentCFG.containsVar(ins.symbol.ident)
 				&& !currentCFG.label.equals("main")) {
 			// Var
 		    List<VirtualRegister> inputs = ins.getInputOperands();
 			PutF1(STW, useReg(inputs.get(0)), FrameP,
 					-GetVarAddress(ins.symbol.ident));
 		} else if (currentCFG.containsParam(ins.symbol.ident)
 				&& !currentCFG.label.equals("main")) {
 			// Param
 			PutF1(STW, useReg(ins.outputOp), FrameP,
 					8 + GetParamAddress(ins.symbol.ident));
 		} else if (mainCFG.containsVar(ins.symbol.ident)) {
 			// Global Var
 			//TODO resolve?
			PutF1(STW, useReg(ins.value.outputOp),
 					GlobalV, -GetVarAddress(ins.symbol.ident));
 		} else if (currentCFG.containsArray(ins.symbol.ident)
 				&& !currentCFG.label.equals("main")) {
 			// Array
 			//TODO get array base address; same for load
 			System.err.println("Array Store shouldn't happen");
 //			PutF1(STW, useReg(ins.outputOp), FrameP,  -GetArrayAddress(ins.symbol.ident) );
 		} else if (mainCFG.containsArray(ins.symbol.ident)) {
 			// Global Array
 			System.err.println("Global Array Store shouldn't happen");
 //			PutF1(STW, useReg(ins.outputOp), GlobalV, -GetArrayAddress(ins.symbol.ident) );
 		}
 	}
 
 	private int GetParamAddress(String ident) {
 		int ret = 0;
 		if (currentCFG.containsParam(ident)) {
 			ret = (currentCFG.getParam(ident)) * 4;
 		} else {
 			ret = Integer.MAX_VALUE;
 			Error("GetParamAddress: Var does not exist: " + ident);
 		}
 
 		return ret;
 	}
 
 	private int GetVarAddress(String ident) {
 		int ret = 0;
 
 		if (currentCFG != null && currentCFG.containsVar(ident)) {
 			ret = (currentCFG.getVar(ident)) * 4;
 		} else if (mainCFG.containsVar(ident)) {
 			ret = (mainCFG.getVar(ident)) * 4;
 		} else {
 			ret = Integer.MAX_VALUE;
 			Error("GetVarAddress: Var does not exist: " + ident);
 		}
 
 		return ret;
 	}
 
 	private int GetArrayAddress(String ident) {
 		int ret = 0;
 
 		if (currentCFG != null && currentCFG.containsArray(ident)) {
 			ret = (currentCFG.getArrayOffset(ident));
 		} else if (mainCFG.containsArray(ident)) {
 			ret = (mainCFG.getArrayOffset(ident));
 		} else {
 			ret = Integer.MAX_VALUE;
 			Error("GetVarAddress: Var does not exist: " + ident);
 		}
 
 		return ret;
 	}
 
 //	private int GetArrayAddress(String ident) {
 ////		int[] maxDim;
 ////		CFG arrayCFG = currentCFG;
 //		if (!currentCFG.containsArray(ident)) {
 ////			arrayCFG = mainCFG;
 //			System.err.println("GetArrayAddress can't find array");
 //		}
 //		return currentCFG.getArrayDims(ident);
 //		maxDim = arrayCFG.getArrayDims(ident);
 
 //		offset.setConst();
 //		offset.value = (arrayCFG.getArrayOffset(id) + arrayCFG.getVarNum()) * 4;
 //
 //		address = addAddress(0, maxDim, coord);
 ////		load(address);// add offset to this reg
 //
 //		// Negate address
 //		neg.setConst();
 //		neg.value = -4;
 //
 //		// TODO output code
 //		// Compute(Scanner.timesToken, address, neg);
 //		// add offset to the calculated address
 //		// Compute(Scanner.minusToken, address, offset);
 //
 //		return address;
 //	}
 
 //	private static Result addAddress(int dim, int[] maxDim, Result[] coord) {
 //		if (dim >= maxDim.length) {
 //			return new Result();
 //		}
 //
 //		Result address = new Result();
 //
 //		if (dim == maxDim.length - 1) {// last dim
 //			address = coord[dim];
 //
 //			return address;// just use regular value
 //		}
 //
 //		address = coord[dim];
 //
 //		Result tailDataSize = new Result();
 //		tailDataSize.setConst();
 //		tailDataSize.value = 1;
 //
 //		for (int i = dim + 1; i < maxDim.length; i++) {
 //			tailDataSize.value *= maxDim[i];
 //		}
 //
 //		Result subAddress = addAddress(dim + 1, maxDim, coord);
 //
 //		// TODO output code
 //		// Compute(Scanner.timesToken, address, tailDataSize);
 //
 //		// Compute(Scanner.plusToken, address, subAddress);
 //
 //		return address;
 //	}
 
 	private int opCodeImm(int op) {
 		if (op != ERR) {
 			return op + 16;
 		}
 		return ERR;
 	}
 
 	private static int negatedBranchOp(ControlFlowInstr instruction) {
 		if (instruction instanceof BranchEqual) {
 			return BNE;
 		} else if (instruction instanceof BranchGreater) {
 			return BLE;
 		} else if (instruction instanceof BranchGreaterEqual) {
 			return BLT;
 		} else if (instruction instanceof BranchLesser) {
 			return BGE;
 		} else if (instruction instanceof BranchLesserEqual) {
 			return BGT;
 		} else if (instruction instanceof BranchNotEqual) {
 			return BEQ;
 		}
 		return ERR;
 	}
 
 	private static int opCode(Instruction instruction) {
 		if (instruction instanceof Add) {
 			return ADD;
 		} else if (instruction instanceof Sub) {
 			return SUB;
 		} else if (instruction instanceof Mul) {
 			return MUL;
 		} else if (instruction instanceof Div) {
 			return DIV;
 		} else if (instruction instanceof Cmp) {
 			return CMP;
 		}
 		return ERR;
 	}
 
 	private void Push(int regNo) {
 		PutF1(STW, regNo, StackP, 0);
 		PutF1(ADDI, StackP, StackP, -4);
 	}
 
 	private void Pop(int regNo) {
 		PutF1(ADDI, StackP, StackP, 4);
 		PutF1(LDW, regNo, StackP, 0);
 	}
 
 	private void Pop() {
 		PutF1(ADDI, StackP, StackP, 4);
 	}
 
 	private void CondNegBraFwd(ControlFlowInstr ins) {
 		fixup.push(pc);
 		PutF1(negatedBranchOp(ins),
 				useReg(ins.inputOps.get(0)), 0, 0);
 	}
 
 	private void UnCondBraFwd(int loc) {
 		PutF1(BEQ, 0, 0, loc);// Build linked list by storing previous value
 		// fixup.push(pc - 1);
 	}
 
 	private void Fixup(int loc) {// TODO builtin pop
 		int part = (0xffff0000 + (pc - loc));
 		int fixed = (nativeCode.get(loc) | 0x0000ffff) & part;
 		nativeCode.set(loc, fixed);
 	}
 
 	private void FixAll(int loc) {
 		int next;
 		while (loc != 0) {
 			next = nativeCode.get(loc) & 0x0000ffff; // extract next element of
 														// linked list
 			Fixup(loc);
 			loc = next;
 		}
 	}
 
 	private void PutF1(int op, int a, int b, int c) {
 		nativeCode.add(pc++, op << 26 | a << 21 | b << 16 | c & 0xffff);
 	}
 
 	private void PutF2(int op, int a, int b, int c) {
 		nativeCode.add(pc++, op << 26 | a << 21 | b << 16 | c & 0x1f);
 	}
 
 	private void PutF3(int op, int c) {
 		nativeCode.add(pc++, op << 26 | c & 0xffffff);
 	}
 
 	public int[] getProgram() {
 		int[] ret = new int[nativeCode.size()];
 		for (int i = 0; i < nativeCode.size(); i++) {
 			ret[i] = nativeCode.get(i);
 		}
 		return ret;
 	}
 
 	private void writeBin(String outFile) {
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
 			for (int i = 0; i < nativeCode.size(); i++) {
 				if (i > 0) {
 					out.write("\n");
 				}
 				if (DEBUG && DEBUGMESG.containsKey(i)){
 					for (String line:DEBUGMESG.get(i).split(System.getProperty("line.separator")) ){
 						out.write("# "+line + "\n");
 					}
 				}
 				out.write(nativeCode.get(i).toString());
 				if (DEBUG){
 					out.write("\n# "+i+": "+DLX.disassemble(nativeCode.get(i)) );
 				}
 			}
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 
 	private void AddDebug(String string) {
 		// TODO Auto-generated method stub
 		if (!DEBUGMESG.containsKey(pc)){
 			DEBUGMESG.put(pc, "");
 		}
 		DEBUGMESG.put(pc, DEBUGMESG.get(pc) + string);
 	}
 	
 	public void Error(String errorMsg) {
 		System.err.println("PC = " + pc + " Compiler error: " + errorMsg);
 	}
 }
