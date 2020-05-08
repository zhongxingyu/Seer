 package misc;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.RecognitionException;
 
 import ast.Program;
 
 import parser.FunctionCallNotAllowedException;
 import parser.IllegalTypeException;
 import parser.ParserInterface;
 import verifier.smtlib.z3.Z3;
 import verifier.VerifierInterface;
 import interpreter.AssertionFailureException;
 import interpreter.Interpreter;
 import interpreter.ProgramExecution;
 import interpreter.GlobalBreakpoint;
 import interpreter.StatementBreakpoint;
 
 public class ExecutionHandler {
 	private ProgramExecution execution;
 	private Interpreter interpreter;
 	private MessageSystem messagesystem;
 	private ParserInterface parser;
 	
 	private Program ast;
 	private ArrayList<GlobalBreakpoint> globalBreakpoints;
 	private String[] parameterValues;
 	private String[] assertionFailureMessage;
 	private boolean paused;
 	
 	public ExecutionHandler(MessageSystem messagesystem) {
 		this.messagesystem = messagesystem;
 		this.interpreter = new Interpreter();
 		this.parser = new ParserInterface();
 		this.globalBreakpoints = new ArrayList<GlobalBreakpoint>();
 	}
 
 	public void parse(String source) {
 		this.messagesystem.clear(MessageCategories.ERROR);
 		try {
 			this.ast = this.parser.parseProgram(source);
 		} catch (RecognitionException re) {
 			this.messagesystem.addMessage(MessageCategories.ERROR, 0, re.getMessage());
 		} catch(NullPointerException npe) {
 			npe.printStackTrace();
 			this.messagesystem.addMessage(MessageCategories.ERROR, -1, "AST creation not possible!");
 		} catch(IllegalTypeException ite) {
 			this.messagesystem.addMessage(MessageCategories.ERROR, ite.getErrorPosition().getLine(), ite.getMessage());
 		} catch(FunctionCallNotAllowedException fce) {
 			this.messagesystem.addMessage(MessageCategories.ERROR, fce.getErrorPosition().getLine(), fce.getMessage());
 		}
 		for(String error : parser.getErrors()) {
 			String[] parsedError = parseParserError(error);
 			this.messagesystem.addMessage(MessageCategories.ERROR, Integer.parseInt(parsedError[0]), parsedError[1]);
 		}
 	}
 	
 	public void run(ArrayList<StatementBreakpoint> sbreakpoints, 
 			ArrayList<GlobalBreakpoint> gbreakpoints) {
 		if (this.ast == null) {
 			return;
 		}
 		boolean finished = false;
 		boolean success = true;
 		while (!paused && !finished && success) {
 			if (this.execution != null && this.execution.getCurrentState().getCurrentStatement() == null) {
 				finished = true;
				break;
 			}
 			success = this.singleStep(sbreakpoints, gbreakpoints);
 			try {
 				if (this.execution != null && this.execution.checkBreakpoints() != null) {
 					paused = true;
 				}
 			}
 			catch (FunctionCallNotAllowedException ignored) {
 			}
 		}
 	}
 	
 	public boolean singleStep(ArrayList<StatementBreakpoint> sbreakpoints, 
 			ArrayList<GlobalBreakpoint> gbreakpoints) {
 		boolean success = false;
 		if (this.ast == null) {
 			return false;
 		}
 		try {
 			if (this.execution == null) {
 				if (sbreakpoints == null) sbreakpoints = new ArrayList<StatementBreakpoint>();
 				if (gbreakpoints == null) gbreakpoints = new ArrayList<GlobalBreakpoint>();
 				this.execution = new ProgramExecution(this.ast, this.interpreter, 
 						sbreakpoints, gbreakpoints, this.parameterValues);
 			}
 			if (this.execution.getCurrentState().getCurrentStatement() != null) {
 				this.interpreter.step(this.execution.getCurrentState());
 				success = true;
 			}
 		}
 		catch (AssertionFailureException e) {
 			this.assertionFailureMessage = new String[2];
 			success = false;
 			this.assertionFailureMessage[0] = "" + e.getPosition().getLine();
 			this.assertionFailureMessage[1] = e.getMessage();
 			this.destroyProgramExecution();
 	    }
 		return success;
 	}
 	
 	public void verify(String source) {
 		if(this.ast == null) {
 			this.parse(source);
 		}
 		this.messagesystem.clear(MessageCategories.VERIFYERROR);
 		if(!new File(Settings.getInstance().getVerifierPath()).exists()) {
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, 
 					"Please specify a path to the verifier in the settings.");
 			return;
 		}
 		VerifierInterface verifier = new VerifierInterface(new Z3(
 				Settings.getInstance().getVerifierPath() + " ${FILE} -m"
 				));
 		try {
 			verifier.verify(this.ast);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, "IOException");
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, e.getMessage());
 		} catch (RecognitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1 , e.getMessage());
 		} catch(NullPointerException e) {
 			e.printStackTrace();
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, "NullPointer");
 		}
 	}
 	
 	private String[] parseParserError(String error) {
 		String[] parsedError = new String[2];
 		Pattern p = Pattern.compile("^line (\\d+):(\\d+) (.*)$");
 		Matcher m = p.matcher(error); 
 		boolean isvalid = m.matches();
 		if(!isvalid) {
 			System.out.println("ERROR");
 			return new String[]{"-1", "ERROR"};
 		}
 		parsedError[0] = m.group(1);
 		parsedError[1] = m.group(3);
 		return parsedError;
 	}
 	
 	public void printAssertionFailureMessage() {
 		int position;
 		try {
 			position = Integer.parseInt(this.assertionFailureMessage[0]);
 		}
 		catch (NumberFormatException e) {
 			position = -1;
 		}
 		this.messagesystem.addMessage(MessageCategories.ERROR, position, 
 				this.assertionFailureMessage[1]);
 		this.assertionFailureMessage = null;
 	}
 	
 	public void clearAssertionFailureMessage() {
 		this.assertionFailureMessage = null;
 	}
 	
 	public void destroyProgramExecution() {
 		this.execution = null;
 	}
 	
 	public void setParameterValues(String[] values) {
 		this.parameterValues = values;
 	}
 
 	public void setAST(Program ast) {
 		this.ast = ast;
 	}
 	
 	public void setPaused(boolean paused) {
 		this.paused = paused;
 	}
 	
 	public ProgramExecution getProgramExecution() {
 		return this.execution;
 	}
 	
 	public String[] getParameterValues() {
 		return this.parameterValues;
 	}
 	
 	public MessageSystem getMessageSystem() {
 		return this.messagesystem;
 	}
 	
 	public String[] getAssertionFailureMessage() {
 		return this.assertionFailureMessage;
 	}
 	
 	public Program getAST() {
 		return this.ast;
 	}
 	
 	public ParserInterface getParserInterface() {
 		return this.parser;
 	}
 	
 	public ArrayList<GlobalBreakpoint> getGlobalBreakpoints() {
 		return this.globalBreakpoints;
 	}
 	
 	public boolean getPaused() {
 		return this.paused;
 	}
 }
