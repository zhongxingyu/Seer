 package swp_compiler_ss13.fuc.gui.ide;
 
 import java.awt.Component;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.WriterAppender;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.backend.Backend;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.common.parser.Parser;
 import swp_compiler_ss13.common.semanticAnalysis.SemanticAnalyser;
import swp_compiler_ss13.fuc.backend.LLVMExecutor;
import swp_compiler_ss13.fuc.backend.LLVMExecutor.ExecutionResult;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeButton;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeMenu;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeStatusLabel;
 import swp_compiler_ss13.fuc.gui.ide.data.FucIdeTab;
 import swp_compiler_ss13.fuc.gui.ide.mvc.Controller;
 import swp_compiler_ss13.fuc.gui.ide.mvc.Position;
 
 /**
  * The FUC IDE Controllre
  * 
  * @author "Frank Zechert"
  * @version 1
  */
 public class FucIdeController {
 	/**
 	 * The model
 	 */
 	private FucIdeModel model;
 	/**
 	 * The view
 	 */
 	private FucIdeView view;
 	private WriterAppender appender;
 
 	private static Logger logger = Logger.getLogger(FucIdeController.class);
 
 	/**
 	 * Instantiate a new instance of the controller
 	 */
 	public FucIdeController() {
 
 		this.redirectSystemStreams();
 		this.model = new FucIdeModel(this);
 		this.view = new FucIdeView(this);
 
 		this.initComponents();
 		this.setUpInitialState();
 
 		this.view.setVisible(true);
 	}
 
 	private void redirectSystemStreams() {
 		final StringWriter consoleWriter = new StringWriter();
 		this.appender = new WriterAppender(new PatternLayout("%d{ISO8601} %p - %m%n"), consoleWriter);
 		this.appender.setName("GUI_APPENDER");
 		this.appender.setThreshold(org.apache.log4j.Level.INFO);
 		Logger.getRootLogger().addAppender(this.appender);
 
 		new Thread() {
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						SwingUtilities.invokeAndWait(new Runnable() {
 							@Override
 							public void run() {
 								FucIdeController.this.updateTextPane(consoleWriter.toString());
 								consoleWriter.getBuffer().setLength(0);
 							}
 						});
 
 						Thread.sleep(500);
 					} catch (InvocationTargetException | InterruptedException e) {
 						// ignore
 					}
 				}
 			}
 		}.start();
 	}
 
 	protected void updateTextPane(String valueOf) {
 		if (this.view != null) {
 			this.view.updateTextPane(valueOf);
 		}
 	}
 
 	/**
 	 * Init the compiler copmonents
 	 */
 	private void initComponents() {
 		List<Lexer> lexers = this.model.getLexers();
 		for (Lexer lexer : lexers) {
 			this.view.addComponentRadioMenuItem(lexer);
 		}
 		List<Parser> parsers = this.model.getParsers();
 		for (Parser parser : parsers) {
 			this.view.addComponentRadioMenuItem(parser);
 		}
 		List<SemanticAnalyser> semanticAnalyzers = this.model.getSemanticAnalysers();
 		for (SemanticAnalyser sa : semanticAnalyzers) {
 			this.view.addComponentRadioMenuItem(sa);
 		}
 		List<IntermediateCodeGenerator> irgs = this.model.getIntermediateCodeGenerators();
 		for (IntermediateCodeGenerator irg : irgs) {
 			this.view.addComponentRadioMenuItem(irg);
 		}
 		List<Backend> backends = this.model.getBackends();
 		for (Backend backend : backends) {
 			this.view.addComponentRadioMenuItem(backend);
 		}
 
 		if (lexers.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							Lexer.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (parsers.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							Parser.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (semanticAnalyzers.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							SemanticAnalyser.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (irgs.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							IntermediateCodeGenerator.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		if (backends.size() == 0) {
 			String error = String
 					.format("No implementation for %s was found in the classpath.\nThe compiler will not work.",
 							Backend.class);
 			new FucIdeCriticalError(this.view, error, false);
 		}
 
 		List<Controller> cl = this.model.getGUIControllers();
 		for (Controller c : cl) {
 			logger.info("Initializing gui component " + c.getClass().getName());
 			c.init(this.model);
 			boolean notify = false;
 			notify = notify || c.getModel().setSourceCode("");
 			notify = notify || c.getModel().setTokens(null);
 			notify = notify || c.getModel().setAST(null);
 			notify = notify || c.getModel().setTAC(null);
 			notify = notify || c.getModel().setTargetCode(null);
 			if (notify) {
 				logger.info("notifying the controller " + c.getClass().getName() + " about model changes");
 				c.notifyModelChanged();
 			}
 			this.model.addTab(c);
 			this.notifyModelTab();
 		}
 	}
 
 	public void notifyModelAddedMenu() {
 		FucIdeMenu[] menus = this.model.getMenus().toArray(new FucIdeMenu[] {});
 		Arrays.sort(menus);
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				FucIdeController.this.view.clearMenus();
 			}
 		});
 		for (final FucIdeMenu menu : menus) {
 
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addMenu(menu);
 				}
 			});
 		}
 	}
 
 	public void notifyModelAddedButton() {
 		FucIdeButton[] buttons = this.model.getButtons().toArray(new FucIdeButton[] {});
 		Arrays.sort(buttons);
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				FucIdeController.this.view.clearButtons();
 			}
 		});
 		for (final FucIdeButton button : buttons) {
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addButton(button);
 				}
 			});
 		}
 	}
 
 	public void notifyModelAddedLabel() {
 		FucIdeStatusLabel[] labels = this.model.getLabels().toArray(new FucIdeStatusLabel[] {});
 		Arrays.sort(labels);
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				FucIdeController.this.view.clearLabels();
 			}
 		});
 		for (final FucIdeStatusLabel label : labels) {
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addLabel(label);
 				}
 			});
 		}
 	}
 
 	public void notifyModelTab() {
 		FucIdeTab[] tabs = this.model.getTabs().toArray(new FucIdeTab[] {});
 		Arrays.sort(tabs);
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 
 				FucIdeController.this.view.clearTabs();
 			}
 		});
 		for (final FucIdeTab tab : tabs) {
 			logger.info("adding tab " + tab.getName());
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					FucIdeController.this.view.addTab(tab.getName(), tab.getComponent());
 				}
 			});
 		}
 
 	}
 
 	private void setUpInitialState() {
 		for (final FucIdeTab t : this.model.getTabs()) {
 			if (this.view.isFirstTab(t)) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						FucIdeController.this.updateStatus(t.getPosition());
 						FucIdeController.this.view.showFirstTab();
 					}
 
 				});
 				break;
 			}
 		}
 	}
 
 	private void updateStatus(Position position) {
 		logger.info("Showing the tab for position " + position);
 		FucIdeMenu[] menus = this.model.getMenus().toArray(new FucIdeMenu[] {});
 		Arrays.sort(menus);
 
 		this.view.clearMenus();
 		for (FucIdeMenu menu : menus) {
 			if (menu.isAlwaysVisible() || menu.getPosition() == position) {
 				this.view.addMenu(menu);
 				break;
 			}
 		}
 
 		FucIdeButton[] buttons = this.model.getButtons().toArray(new FucIdeButton[] {});
 		Arrays.sort(buttons);
 
 		this.view.clearButtons();
 		for (FucIdeButton button : buttons) {
 			if (button.isAlwaysVisible() || button.getPosition() == position) {
 				this.view.addButton(button);
 			}
 		}
 
 		FucIdeStatusLabel[] labels = this.model.getLabels().toArray(new FucIdeStatusLabel[] {});
 		Arrays.sort(labels);
 
 		this.view.clearLabels();
 		for (FucIdeStatusLabel label : labels) {
 			if (label.isAlwaysVisible() || label.getPosition() == position) {
 				this.view.addLabel(label);
 			}
 		}
 
 		this.view.invalidate();
 
 	}
 
 	public void tabChanged() {
 		for (final FucIdeTab t : this.model.getTabs()) {
 			if (this.view.isCurrentTab(t)) {
 				this.model.setActiveTab(t);
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						FucIdeController.this.updateStatus(t.getPosition());
 					}
 				});
 				break;
 			}
 		}
 
 		this.view.invalidate();
 	}
 
 	public static void main(String[] args) {
 		new FucIdeController();
 	}
 
 	public void onRunPressed() {
 		this.run(false);
 	}
 
 	public void onExecPressed() {
 		if (this.model.getExecutable() == null) {
 			new FucIdeCriticalError(this.view, "There is nothing to execute. Please press 'compile' before 'execute'!",
 					true);
 		}
 
 		String output = this.runProgram(this.model.getExecutable(), false);
 		this.notifyExecutableChanged(output);
 	}
 
 	public void onLexerSelected(Lexer lexer) {
 		logger.info("Lexer component active: " + lexer.getClass().getName());
 		this.model.setActiveLexer(lexer);
 	}
 
 	public void onParserSelected(Parser parser) {
 		logger.info("Parser component active: " + parser.getClass().getName());
 		this.model.setActiveParser(parser);
 	}
 
 	public void onAnalyzerSelected(SemanticAnalyser analyzer) {
 		logger.info("SemanticAnalyzer component active: " + analyzer.getClass().getName());
 		this.model.setActiveAnalyzer(analyzer);
 	}
 
 	public void onIRGSelected(IntermediateCodeGenerator irgen) {
 		logger.info("IntermediateCodeGenerator component active: " + irgen.getClass().getName());
 		this.model.setActiveIRG(irgen);
 	}
 
 	public void onBackendSelected(Backend backend) {
 		logger.info("Backend component active: " + backend.getClass().getName());
 		this.model.setActiveBackend(backend);
 	}
 
 	public void notifySourceCodeChanged() {
 		logger.info("Source code was changed");
 
 		this.model.setExecutable(null);
 		this.view.disableExecuteButton();
 
 		for (Controller c : this.model.getGUIControllers()) {
 			if (c.getModel().setSourceCode(this.model.getSourceCode())) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	public void notifyTokensChanged(List<Token> tokens) {
 		logger.info("token list was changed");
 		for (Controller c : this.model.getGUIControllers()) {
 			if (c.getModel().setTokens(tokens)) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	public void notifyASTChanged(AST ast) {
 		logger.info("ast was changed");
 		for (Controller c : this.model.getGUIControllers()) {
 			if (c.getModel().setAST(ast)) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	public void notifyTACChanged(List<Quadruple> tac) {
 		logger.info("tac was changed");
 		for (Controller c : this.model.getGUIControllers()) {
 			if (c.getModel().setTAC(tac)) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	public void notifyTargetChanged(Map<String, InputStream> target) {
 		logger.info("target was changed");
 		for (Controller c : this.model.getGUIControllers()) {
 
 			Map<String, InputStream> clonedTarget = this.deepCloneTargetMap(target);
 
 			if (c.getModel().setTargetCode(clonedTarget)) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	private Map<String, InputStream> deepCloneTargetMap(Map<String, InputStream> target) {
 		HashMap<String, InputStream> clonedMap = new HashMap<>();
 		for (Entry<String, InputStream> entry : target.entrySet()) {
 			InputStream[] copy = this.cloneInputStream(entry.getValue());
 			InputStream copy1 = copy[0];
 			InputStream copy2 = copy[1];
 			target.put(entry.getKey(), copy1);
 			clonedMap.put(entry.getKey(), copy2);
 		}
 		return clonedMap;
 	}
 
 	public void notifyExecutableChanged(String output) {
 		logger.info("executable output was changed");
 		for (Controller c : this.model.getGUIControllers()) {
 			if (c.getModel().setProgramResult(output)) {
 				c.notifyModelChanged();
 			}
 		}
 	}
 
 	public Component getView() {
 		return this.view;
 	}
 
 	public void run(boolean silent) {
 		String sourceCode = this.model.getSourceCode();
 		ReportLogImpl reportlog = new ReportLogImpl();
 		this.model.setReportLog(reportlog);
 
 		List<Token> tokens = this.runLexer(sourceCode, silent, reportlog);
 		this.notifyTokensChanged(tokens);
 		if (tokens != null && !this.reportLogContainsErrors(reportlog)) {
 			AST ast = this.runParser(tokens, silent, reportlog);
 			this.notifyASTChanged(ast);
 			if (ast != null && !this.reportLogContainsErrors(reportlog)) {
 				AST checkedAST = this.runSemanticAnalysis(ast, silent, reportlog);
 				this.notifyASTChanged(ast);
 				if (checkedAST != null && !this.reportLogContainsErrors(reportlog)) {
 					List<Quadruple> tac = this.runIntermediateCodeGenerator(checkedAST, silent, reportlog);
 					this.notifyTACChanged(tac);
 					if (tac != null && !this.reportLogContainsErrors(reportlog)) {
 						Map<String, InputStream> target = this.runBackend(tac, silent, reportlog);
 						this.notifyTargetChanged(target);
 						this.model.setExecutable(target);
 						this.view.enableExecuteButton();
 					}
 				}
 			}
 		}
 		if (!silent) {
 			this.setLogEntries(reportlog);
 		}
 	}
 
 	private boolean reportLogContainsErrors(ReportLogImpl reportlog) {
 		return reportlog.getErrors().size() > 0;
 	}
 
 	public List<Token> runLexer(String sourceCode, boolean silent) {
 		ReportLogImpl reportlog = new ReportLogImpl();
 		this.model.setReportLog(reportlog);
 		List<Token> tokens = this.runLexer(sourceCode, silent, reportlog);
 		this.notifyTokensChanged(tokens);
 		if (!silent) {
 			this.setLogEntries(reportlog);
 		}
 		return tokens;
 	}
 
 	public AST runParser(List<Token> tokens, boolean silent) {
 		ReportLogImpl reportlog = new ReportLogImpl();
 		this.model.setReportLog(reportlog);
 		AST ast = this.runParser(tokens, silent, reportlog);
 		this.notifyASTChanged(ast);
 		if (!silent) {
 			this.setLogEntries(reportlog);
 		}
 		return ast;
 	}
 
 	public AST runSemanticAnalysis(AST ast, boolean silent) {
 		ReportLogImpl reportlog = new ReportLogImpl();
 		this.model.setReportLog(reportlog);
 		AST cast = this.runSemanticAnalysis(ast, silent, reportlog);
 		this.notifyASTChanged(cast);
 		if (!silent) {
 			this.setLogEntries(reportlog);
 		}
 		return cast;
 	}
 
 	public Map<String, InputStream> runBackend(List<Quadruple> tac, boolean silent) {
 		ReportLogImpl reportlog = new ReportLogImpl();
 		this.model.setReportLog(reportlog);
 		Map<String, InputStream> target = this.runBackend(tac, silent, reportlog);
 		this.notifyTargetChanged(target);
 		if (!silent) {
 			this.setLogEntries(reportlog);
 		}
 		return target;
 	}
 
 	public List<Quadruple> runIntermediateCodeGenerator(AST ast, boolean silent) {
 		ReportLogImpl reportlog = new ReportLogImpl();
 		this.model.setReportLog(reportlog);
 		List<Quadruple> tac = this.runIntermediateCodeGenerator(ast, silent, reportlog);
 		this.notifyTACChanged(tac);
 		if (!silent) {
 			this.setLogEntries(reportlog);
 		}
 		return tac;
 	}
 
 	public List<Token> runLexer(String sourceCode, boolean silent, ReportLogImpl log) {
 		if (sourceCode == null) {
 			sourceCode = "";
 		}
 		Lexer lexer = this.model.getActiveLexer();
 		InputStream stream = new ByteArrayInputStream(sourceCode.getBytes());
 
 		try {
 			lexer.setSourceStream(stream);
 			List<Token> tokenList = new LinkedList<>();
 			while (true) {
 				Token token = lexer.getNextToken();
 				tokenList.add(token);
 				if (token.getTokenType() == TokenType.EOF) {
 					break;
 				}
 			}
 			return tokenList;
 		} catch (Throwable th) {
 			if (!silent) {
 				new FucIdeCriticalError(this.view, th, true);
 			}
 		}
 		return null;
 	}
 
 	public AST runParser(List<Token> tokens, boolean silent, ReportLogImpl log) {
 		Parser parser = this.model.getActiveParser();
 		parser.setReportLog(log);
 		parser.setLexer(new MockLexer(tokens));
 
 		try {
 			AST ast = parser.getParsedAST();
 			return ast;
 		} catch (Throwable th) {
 			if (!silent) {
 				new FucIdeCriticalError(this.view, th, true);
 			}
 		}
 		return null;
 	}
 
 	public AST runSemanticAnalysis(AST ast, boolean silent, ReportLogImpl log) {
 		SemanticAnalyser sa = this.model.getActiveAnalyzer();
 		sa.setReportLog(log);
 		try {
 			AST checkedAst = sa.analyse(ast);
 			return checkedAst;
 		} catch (Throwable th) {
 			if (!silent) {
 				new FucIdeCriticalError(this.view, th, true);
 			}
 		}
 		return null;
 	}
 
 	public Map<String, InputStream> runBackend(List<Quadruple> tac, boolean silent, ReportLogImpl log) {
 		Backend backend = this.model.getActiveBackend();
 		try {
 			Map<String, InputStream> target = backend.generateTargetCode("main", tac);
 			return target;
 		} catch (Throwable th) {
 			if (!silent) {
 				new FucIdeCriticalError(this.view, th, true);
 			}
 		}
 		return null;
 	}
 
 	public List<Quadruple> runIntermediateCodeGenerator(AST ast, boolean silent, ReportLogImpl log) {
 		IntermediateCodeGenerator irg = this.model.getActiveIRG();
 		try {
 			List<Quadruple> tac = irg.generateIntermediateCode(ast);
 			return tac;
 		} catch (Throwable th) {
 			if (!silent) {
 				new FucIdeCriticalError(this.view, th, true);
 			}
 		}
 		return null;
 	}
 
 	public String runProgram(Map<String, InputStream> program, boolean silent) {
 		try {
 			StringBuilder output = new StringBuilder();
 			for (Entry<String, InputStream> file : program.entrySet()) {
 				String filename = file.getKey();
 				InputStream filecontent = file.getValue();
 				InputStream[] copy = this.cloneInputStream(filecontent);
 				InputStream copy1 = copy[0];
 				InputStream copy2 = copy[1];
 				program.put(filename, copy1);
 
 				if (filename.endsWith(".ll")) {
					ExecutionResult r = LLVMExecutor.runIR(copy2);
 					output.append("Executing ").append(filename).append(":\n\n");
 					output.append(r.output);
 					output.append("\nExit Code: ");
 					output.append(r.exitCode);
 					output.append("\n\n\n");
 				}
 
 				if (filename.endsWith(".class")) {
 					throw new UnsupportedOperationException(
 							"Execution of Code is currently only supported for LLVM. Please use LLVM Backend!");
 				}
 			}
 
 			return output.toString();
 		} catch (Throwable th) {
 			if (silent) {
 				return null;
 			}
 			if (th instanceof IOException) {
 				new FucIdeCriticalError(
 						this.view,
 						"It seems you are missing the program 'lli'. 'lli' is required to execute LLVM Code.\nPlease install 'lli' or check your path if it is already installed!",
 						true);
 			}
 			else {
 				new FucIdeCriticalError(this.view, th, true);
 			}
 			return null;
 		}
 	}
 
 	private InputStream[] cloneInputStream(InputStream input) {
 		try {
 			ByteArrayOutputStream bout = new ByteArrayOutputStream();
 			byte buffer[] = new byte[1024];
 			int len;
 			while ((len = input.read(buffer)) > 0) {
 				bout.write(buffer, 0, len);
 			}
 			bout.flush();
 			return new InputStream[] { new ByteArrayInputStream(bout.toByteArray()),
 					new ByteArrayInputStream(bout.toByteArray()) };
 		} catch (IOException e) {
 			new FucIdeCriticalError(this.view, e, true);
 		}
 		return null;
 	}
 
 	private void setLogEntries(final ReportLogImpl reportlog) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				FucIdeController.this.view.displayErrorLog(reportlog);
 			}
 		});
 	}
 
 	public void showTab(final Controller controller) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				FucIdeController.this.view.showTab(controller);
 			}
 		});
 	}
 
 	public void onLogLevelSelected(org.apache.log4j.Level level) {
 		this.appender.setThreshold(level);
 		logger.info("LegLevel now set to " + level);
 	}
 }
