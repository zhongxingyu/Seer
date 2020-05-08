 package lobecompiler;
 
 import java.util.Stack;
 
 import earcompiler.EARCompiler;
 import earcompiler.EARException;
 
 public class LOBECompiler {
 
 	private LOBESymbolTable mSymbols;
 	public String mOutput;
 	private Variable[] mWorkingMemory;
 	private Stack<Variable> mIfs;
 	private Stack<Conditional> mWhileConds;
 	private Stack<Variable> mWhileCells;
 
 	public LOBECompiler() {
 		reset();
 	}
 	
 	/**
 	 * Resets state to that of a new compiler.
 	 */
 	private void reset() {
 		mSymbols = new LOBESymbolTable();
 		initWorkingMemory(10);
 		mOutput = "";
 		mIfs = new Stack<Variable>();
 		mWhileConds = new Stack<Conditional>();
 		mWhileCells = new Stack<Variable>();
 	}
 	
 	public String compile(String LOBECode) throws InvalidParameterException {
 		reset();
 		LOBEParser parser = new LOBEParser();
 		LOBEInstruction[] instructions = parser.parseAll(LOBECode);
 		for (LOBEInstruction instruction : instructions) {
 			execute(instruction);
 		}
 		return mOutput;
 	}
 
 	public void execute(LOBEInstruction instruction)
 			throws InvalidParameterException {
 		if (instruction.mCommand == LOBECommand.PRINT) {
 			if (instruction.mArguments.length != 1) {
 				throw new InvalidParameterException(
 						"PRINT takes exactly one parameter.");
 			}
 			Value argval = instruction.mArguments[0].evaluate(this);
 			Variable target;
 			if (argval instanceof Variable) {
 				target = (Variable)argval;
 			}
 			else
 			{
 				target = mSymbols.getNewInternalVariable(this);
 				mOutput += "MOV " + argval.getRef(this) + " " + target.getRef(this) + "\n";				
 			}			
 			mOutput += "OUT " + target.getRef(this) + "\n";			
 			
 		} 
 		else if (instruction.mCommand == LOBECommand.SET) {
 			if (instruction.mArguments.length != 2) {
 				throw new InvalidParameterException(
 						"SET takes exactly two parameters.");
 			}
 			if (!(instruction.mArguments[0] instanceof Variable)) {
 				throw new InvalidParameterException(
 						"The first argument to SET must be a variable.");
 			}			
 			Variable target = (Variable) instruction.mArguments[0];
 			Value source = instruction.mArguments[1].evaluate(this);
 			if (!mSymbols.containsKey(target)) {
 				mSymbols.addVariable(target);
 			}
 			String maybeAt = (source instanceof Variable) ? "@" : "";
 			mOutput += "COPY " + maybeAt + source.getRef(this) 
 					+ " " + target.getRef(this) 
 					+ " " + mWorkingMemory[0].getRef(this) 
 					+ "\n\n";
 		} else if (instruction.mCommand == LOBECommand.IF) {
 			if (instruction.mArguments.length != 1) {
 				throw new InvalidParameterException(
 						"IF takes exactly one parameter.");
 			}
 			Value argVal = instruction.mArguments[0].evaluate(this);
 			Variable ifVar = mSymbols.getNewInternalVariable(this);
 			String maybeAt = (ifVar instanceof Variable) ? "@" : "";
 			mOutput += "COPY " + maybeAt + argVal.getRef(this)
 					+ " " + ifVar.getRef(this) 
 					+ " " + mWorkingMemory[0].getRef(this) 
 					+ "\n";
 			mIfs.add(ifVar);
 			mSymbols.lockVariable(ifVar);
 			mOutput += "WHILE " + ifVar.getRef(this) + "\n\n";
 		} else if (instruction.mCommand == LOBECommand.ENDIF) {
 			if (instruction.mArguments.length != 0) {
 				throw new InvalidParameterException(
 						"ENDIF takes no parameters.");
 			}
 			Variable ifVar = mIfs.pop();			
 			mOutput += "ZERO " + ifVar.getRef(this) + "\n";
 			mOutput += "ENDWHILE\n\n";
 			mSymbols.unlockVariable(ifVar);
 		} else if (instruction.mCommand == LOBECommand.WHILE) {
 			if (instruction.mArguments.length != 1) {
 				throw new InvalidParameterException(
 						"WHILE takes exactly one parameter.");
 			} 
 			else if (!(instruction.mArguments[0] instanceof Conditional)) {
 				System.out.println(instruction.mArguments);
 				throw new InvalidParameterException(
 						"WHILE's argument should be a conditonal..");
 			}
 			Conditional whileCond = (Conditional) instruction.mArguments[0];
 			mWhileConds.add(whileCond);
 			Variable whileVar = whileCond.evaluate(this);
 			mWhileCells.add(whileVar);
 			mOutput += "WHILE " + whileVar.getRef(this) + "\n\n";
 		} 
 		else if (instruction.mCommand == LOBECommand.ENDWHILE) {
 			if (instruction.mArguments.length != 0) {
 				throw new InvalidParameterException(
 						"ENDWHILE takes no parameters.");
 			}
 			Conditional whileCond = mWhileConds.pop();
 			Variable whileVar = whileCond.evaluate(this);
 			Variable whileToVar = mWhileCells.pop();
 			mOutput += "COPY @" + whileVar.getRef(this)
 					+ " " + whileToVar.getRef(this)
 					+ " " + mWorkingMemory[0].getRef(this) 
 					+ "\n";
 			mOutput += "ENDWHILE\n\n";
 		} else {
 			throw new InvalidOperationTokenException("Invalid command "
 					+ instruction.mCommand);
 		}
 		mSymbols.clearInternalVars();
 	}
 
 	private void initWorkingMemory(int size) {
 		mWorkingMemory = new Variable[size];
 		for (int i = 0; i < size; i++) {
 			Variable v = new Variable("!w" + i);
 			mSymbols.put(v, -i);
 			mWorkingMemory[i] = v;
 		}
 	}
 
 	public String getRef(Constant c) {
 		return Integer.toString(c.getValue());
 	}
 
 	public String getRef(Variable v) {
 		return Integer.toString(getPointer(v));
 	}
 
 	public int getPointer(Variable v) {
 		return mSymbols.get(v);
 	}
 	
 	public Variable backup(Variable v, Variable target, Variable workingCell) {
 		mOutput += "COPY @" + v.getRef(this) + " " 
 	                        + target.getRef(this) + " " 
 				            + workingCell.getRef(this) + "\n";
 		return target;
 	}
 
 	public Value evaluate(Operator op, Value val1, Value val2) {
 		String opName;
 		Variable targetVar = mSymbols.getNewInternalVariable(this);
 		Value result;
 		
 		// We require values to be either variables or constants.
 		assert(val1 instanceof Variable || val1 instanceof Constant);
 		assert(val2 instanceof Variable || val2 instanceof Constant);
 		
 		switch (op) {
 			case ADD:
 				if (val2 instanceof Variable) {
 					// 2nd arg is destroyed by ADD, so use a temp copy.
 					val2 = backup((Variable)val2, targetVar, mWorkingMemory[0]);					
 				}				
 				if (val1 instanceof Variable) {
 					if (val2 instanceof Constant) {
 						// Can't use a constant as the second argument, so back 1st arg up
 						// and switch the order of the arguments.
						Value temp = backup((Variable)val1, targetVar, mWorkingMemory[0]);
 						val1 = val2;
 						val2 = temp;
 					}
 					else {
 						// Back up the first argument.						
 						val1 = backup((Variable)val1, 
 								      mSymbols.getNewInternalVariable(this),
 								      mWorkingMemory[0]);
 					}				
 				}
 				if (val1 instanceof Variable && val2 instanceof Constant) {
 					// 2nd arg is a constant - this isn't allowed in EAR but since 1st arg is a 
 					// variable we can swap them over.
 					Value temp = val1;
 					val1 = val2;
 					val2 = temp;					
 				}
 				if (val1 instanceof Constant && val2 instanceof Constant) {
 					// Both arguments are constants - we just return their sum as a constant.
 					int num1 = ((Constant)val1).getValue();
 					int num2 = ((Constant)val1).getValue();
 					result = new Constant(num1 + num2);
 				}
 				else {
 					String maybeAt = (val1 instanceof Variable) ? "@" : "";
 					mOutput += "ADD " + maybeAt + val1.getRef(this) +
 				                  " " + val2.getRef(this) +
 				                  "\n";
 					result = val2;
 				}
 				break;
 			case SUB:
 				if (val2 instanceof Variable) {
 					// 2nd arg is destroyed by SUB, so use a temp copy.
 					val2 = backup((Variable)val2, targetVar, mWorkingMemory[0]);					
 				}				
 				if (val1 instanceof Variable) {
 					if (val2 instanceof Constant) {
 						// Can't use a constant as the second argument, so back 1st arg up
 						// and switch the order of the arguments.
						Value temp = backup((Variable)val1, targetVar, mWorkingMemory[0]);
 						val1 = val2;
 						val2 = temp;
 					}
 					else {
 						// Back up the first argument.						
 						val1 = backup((Variable)val1, 
 								      mSymbols.getNewInternalVariable(this),
 								      mWorkingMemory[0]);
 					}				
 				}
 				if (val1 instanceof Variable && val2 instanceof Constant) {
 					// 2nd arg is a constant - this isn't allowed in EAR but since 1st arg is a 
 					// variable we can swap them over.
 					Value temp = val1;
 					val1 = val2;
 					val2 = temp;					
 				}
 				if (val1 instanceof Constant && val2 instanceof Constant) {
 					// Both arguments are constants - we just return the answer as a constant.
 					int num1 = ((Constant)val1).getValue();
 					int num2 = ((Constant)val1).getValue();
 					result = new Constant(num1 - num2);
 				}
 				else {
 					String maybeAt = (val1 instanceof Variable) ? "@" : "";
 					mOutput += "SUB " + maybeAt + val1.getRef(this) +
 				                  " " + val2.getRef(this) +
 				                  "\n";
 					result = val2;
 				}
 				break;
 			case MUL:
 				if (val1 instanceof Variable) {
 					// 1st arg is destroyed by SUB, so use a temp copy.
 					val1 = backup((Variable)val1, mWorkingMemory[0], mWorkingMemory[2]);					
 				}
 				if (val2 instanceof Variable) {
 					// 2nd arg is destroyed by SUB, so use a temp copy.
 					val2 = backup((Variable)val2, mWorkingMemory[1], mWorkingMemory[2]);					
 				}
 				if (val1 instanceof Constant && val2 instanceof Constant) {
 					// Both arguments are constants - we just return the product as a constant.
 					int num1 = ((Constant)val1).getValue();
 					int num2 = ((Constant)val1).getValue();
 					result = new Constant(num1 * num2);
 				}
 				else {
 					String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
 					String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
 					mOutput += "MUL " + maybeAt1 + val1.getRef(this) +
 				                  " " + maybeAt2 + val2.getRef(this) +
 				                   " " + targetVar.getRef(this) +
 				                   " " + mWorkingMemory[2].getRef(this) +
 				                   "\n";
 					result = targetVar;
 				}
 				break;
 			default:
 				throw new InvalidOperationTokenException("Unknown operation token "
 						+ op.name());		
 		}		
 		return result;
 	}
 
 	public Variable evaluate(Predicate pred, Value val1, Value val2) {
 		String arg1Name = val1.getRef(this);
 		String arg2Name = val2.getRef(this);
 		String resultCell;
 
 		String earCommand = "";
 		String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
 		String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
 		earCommand += "COPY " + maybeAt1 + arg1Name + " [[0]] [[6]] \n";
 		earCommand += "COPY " + maybeAt2 + arg2Name + " [[1]] [[6]] \n";
 
 		if (pred == Predicate.EQ) {
 			earCommand += "MOV 1 [[4]]\n" + 
 		                  "WHILE [[0]]\n" + 
 					      "MOV 0 [[4]]\n" +
 					      "COPY @[[1]] [[2]] [[5]]\n" + 
 					      "MOV 1 [[3]]\n" +
 					      "WHILE [[2]]\n" + 
 					      "MOV 0 [[3]]\n" + 
 					      "MOV 1 [[4]]\n" +
 					      "SUB 1 [[0]] [[1]]\n" + 
 					      "ZERO [[2]]\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[3]]\n" + 
 					      "ZERO [[0]]\n" + 
 					      "MOV 1 [[1]]\n" +
 					      "ZERO [[3]]\n" + 
 					      "ENDWHILE\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[1]]\n" + 
 					      "MOV 0 [[4]]\n" + 
 					      "ZERO [[1]]\n" +
 					      "ENDWHILE\n";
 			resultCell = "[[4]]";
 		} 
 		else if (pred == Predicate.NEQ) {
 			earCommand += "MOV 0 [[4]]\n" + 
 		                  "WHILE [[0]]\n" + 
 					      "MOV 1 [[4]]\n" +
 					      "COPY @[[1]] [[2]] [[5]]\n" + 
 					      "MOV 1 [[3]]\n" +
 					      "WHILE [[2]]\n" + 
 					      "MOV 0 [[3]]\n" + 
 					      "MOV 0 [[4]]\n" +
 					      "SUB 1 [[0]] [[1]]\n" + 
 					      "ZERO [[2]]\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[3]]\n" + 
 					      "ZERO [[0]]\n" + 
 					      "MOV 1 [[1]]\n" +
 					      "ZERO [[3]]\n" + 
 					      "ENDWHILE\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[1]]\n" + 
 					      "MOV 1 [[4]]\n" + 
 					      "ZERO [[1]]\n" +
 					      "ENDWHILE\n";
 			resultCell = "[[4]]";
 		} 
 		else if (pred == Predicate.LEQ) {
 			earCommand += "MOV 1 [[4]]\n" + 
 		                  "WHILE [[0]]\n" + 
 					      "MOV 0 [[4]]\n" +
 					      "COPY @[[1]] [[2]] [[5]]\n" + 
 					      "MOV 1 [[3]]\n" +
 					      "WHILE [[2]]\n" + 
 					      "MOV 0 [[3]]\n" + 
 					      "MOV 1 [[4]]\n" +
 					      "SUB 1 [[0]] [[1]]\n" + 
 					      "ZERO [[2]]\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[3]]\n" + 
 					      "ZERO [[0]]\n" + 
 					      "ZERO [[1]]\n" +
 					      "ZERO [[3]]\n" + 
 					      "ENDWHILE\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[1]]\n" + 
 					      "MOV 1 [[4]]\n" + 
 					      "ZERO [[1]]\n" +
 					      "ENDWHILE\n";
 			resultCell = "[[4]]";
 		} 
 		else if (pred == Predicate.LT) {
 			earCommand += "MOV 0 [[4]]\n" + 
 		                  "WHILE [[0]]\n" + 
 					      "MOV 0 [[4]]\n" +
 					      "COPY @[[1]] [[2]] [[5]]\n" + 
 					      "MOV 1 [[3]]\n" +
 					      "WHILE [[2]]\n" + 
 					      "MOV 0 [[3]]\n" + 
 					      "MOV 1 [[4]]\n" +
 					      "SUB 1 [[0]] [[1]]\n" + 
 					      "ZERO [[2]]\n" + 
 					      "ENDWHILE\n" +
 					      "WHILE [[3]]\n" + 
 					      "ZERO [[0]]\n" + 
 					      "ZERO [[1]]\n" +
 					      "ZERO [[3]]\n" + 
 					      "ZERO [[4]]\n" + 
 					      "ENDWHILE\n" +
 					      "MOV 0 [[4]]\n" +
 					      "ENDWHILE\n" + 
 					      "WHILE [[1]]\n" + 
 					      "MOV 1 [[4]]\n" +
 					      "ZERO [[1]]\n" + 
 					      "ENDWHILE\n";
 			resultCell = "[[4]]";
 		} else {
 			resultCell = "";
 			throw new InvalidOperationTokenException("Unknown operation token "
 					+ pred.name());
 		}
 
 		Variable targetVar = mSymbols.getNewInternalVariable(this);
 		String targetVarName = targetVar.getRef(this);
 
 		earCommand += "COPY @" + resultCell + " " + targetVarName + " [[5]]\n";
 
 		for (int i = 0; i < 10; i++) {
 			String s = Integer.toString(i);
 			earCommand = earCommand.replaceAll("\\[\\[" + s + "\\]\\]",
 					mWorkingMemory[i].getRef(this));
 		}
 
 		mOutput += earCommand;
 
 		return targetVar; // TODO;
 	}
 }
