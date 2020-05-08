 package de.unisiegen.informatik.bs.alvis.compiler;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.Token;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 
 import de.uni_siegen.informatik.bs.alvic.AbstractTLexer;
 import de.uni_siegen.informatik.bs.alvic.Compiler;
 import de.uni_siegen.informatik.bs.alvic.TParser;
 import de.unisiegen.informatik.bs.alvis.io.files.FileCopy;
 import de.unisiegen.informatik.bs.alvis.io.logger.Logger;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 /**
  * @author mays
  * @author Colin Benner
  * @author Eduard Boos
  * 
  */
 public class CompilerAccess {
 	/**
 	 * The compiler used to do all the real work.
 	 */
 	private Compiler compiler;
 
 	/**
 	 * These are the types the user may use.
 	 */
 	private Collection<PCObject> types;
 
 	/**
 	 * These are the packages available to the user. They will automatically be
 	 * imported by the compiler.
 	 */
 	private Collection<String> packages;
 
 	/**
 	 * This is the absolute path to the file that is to be compiled.
 	 */
 	private String algorithmPath = null;
 
 	/**
 	 * This is the generated Java code.
 	 */
 	private String javaCode;
 
 	private static CompilerAccess instance;
 
 	/**
 	 * This is used to translate token names to the corresponding characters.
 	 */
 	private static Map<String, List<String>> translateCompletion = null;
 
 	/**
 	 * Add possible results for a given key.
 	 * 
 	 * @param key
 	 *            The token name to translate.
 	 * @param arg
 	 *            The possible characters.
 	 */
 	private static void add(String key, String... arg) {
 		translateCompletion.put(key, Arrays.asList(arg));
 	}
 
 	/**
 	 * Fill the translateCompletion map.
 	 */
 	private void loadTranslation() {
 		translateCompletion = new HashMap<String, List<String>>();
 		add("MAIN", "main ");
 		add("IF", "if (");
 		add("FOR", "for ");
 		add("WHILE", "while (");
 		add("IN", "in ");
 		add("RETURN", "return");
 
 		add("SIGN", "+", "-");
 		add("BANG", "!");
 
 		add("EQUAL", "=");
 		add("PLUS", "+");
 		add("MINUS", "-");
 		add("STAR", "*");
 		add("SLASH", "/");
 		add("PERCENT", "%");
 		add("AMPAMP", "&&");
 		add("PIPEPIPE", "||");
 		add("EQEQ", "==");
 		add("BANGEQ", "!=");
 		add("LESS", "<");
 		add("GREATER", ">");
 		add("LESSEQ", "<=");
 		add("GREATEREQ", ">=");
 
 		add("LPAREN", "(");
 		add("RPAREN", ")");
 		add("LARRAY", "[");
 		add("RARRAY", "]");
 
 		add("SEMICOLON", ";");
 		add("COMMA", ",");
 		add("COLON", ":");
 		add("DOT", ".");
 
 		add("SCOPEL", "begin", "{");
 		add("SCOPER", "end", "}");
 
 		add("NULL", "null");
 		add("BOOL", "false", "true");
 		add("INFTY", "infty");
 
 		translateCompletion.put("TYPE",
 				new ArrayList<String>(AbstractTLexer.getTypes()));
 	}
 
 	public static CompilerAccess getDefault() {
 		if (instance == null)
 			instance = new CompilerAccess();
 		return instance;
 	}
 
 	/**
 	 * Compile the source found at algorithmPath.
 	 * 
 	 * @return The File representing the file containing the generated Java
 	 *         code.
 	 * @throws RecognitionException
 	 * @throws IOException
 	 */
 	public File compile() throws RecognitionException, IOException {
 		return compile(algorithmPath, true);
 	}
 
 	/**
 	 * Use the compiler to compile the source code found in the given file.
 	 * Before calling this method you should provide the names of all packages
 	 * and classes that the user may use (using the setDatatypes and
 	 * setDatatypePackages methods).
 	 * 
 	 * @param path
 	 *            path to the source code that
 	 * @return path to the generated .java file if it exists, null otherwise
 	 * 
 	 * @throws IOException
 	 */
 	public File compile(String path) throws IOException {
 		return compile(path, false);
 
 	}
 
 	/**
 	 * Use the compiler to compile the source code found in the given file.
 	 * Before calling this method you should provide the names of all packages
 	 * and classes that the user may use (using the setDatatypes and
 	 * setDatatypePackages methods).
 	 * 
 	 * @param path
 	 *            path to the source code that we want to compile.
 	 * @param isAbsolutePath
 	 *            true if the path is absolute.
 	 * @return path to the generated .java file if it exists, null otherwise
 	 * 
 	 * @throws IOException
 	 */
 	public File compile(String path, Boolean isAbsolutePath) throws IOException {
 		if (isAbsolutePath)
 			algorithmPath = path;
 		else
 			algorithmPath = currentPath() + path;
 
 		compileString(readFile(algorithmPath));
 
 		if (null == javaCode)
 			return null;
 
 		return writeJavaCode(getWorkspacePath(algorithmPath), "Algorithm");
 	}
 
 	/**
 	 * Write the generated Java code to a file. The file will be placed in the
 	 * given directory and will be called 'algorithmName + ".java"'.
 	 * 
 	 * @param directory
 	 *            The directory in which the file will be created.
 	 * @param algorithmName
 	 *            The name of the class used for the algorithm.
 	 * @return the file with the Java source code.
 	 * @throws IOException
 	 */
 	public File writeJavaCode(File directory, String algorithmName)
 			throws IOException {
 		File result = null;
 		BufferedWriter out = null;
 		FileWriter fstream;
 		try {
 			result = new File(directory, algorithmName + ".java");
 			fstream = new FileWriter(result);
 			out = new BufferedWriter(fstream);
 			out.write(javaCode.replaceAll("#ALGORITHM_NAME#", algorithmName));
 		} finally {
 			if (out != null)
 				out.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Given the pseudo code to compile create a string containing the generated
 	 * Java code.
 	 * 
 	 * @param code
 	 *            The pseudo code to be compiled.
 	 * @return the Java code the compiler generated.
 	 * @throws IOException
 	 */
 	public String compileString(String code) throws IOException {
 		compiler = new Compiler(types, packages);
 		loadTranslation();
 		return javaCode = compiler.compile(code);
 	}
 	
 	public String generateLatex(String code) throws IOException {
 		compiler = new Compiler(types, packages);
 		return compiler.compile(code, "LaTeX.stg");
 	}
 
 	/**
 	 * Run lexer, parser and the type checker on the code given.
 	 * 
 	 * @param code
 	 *            The code to check.
 	 * @return list of the exceptions created when lexing, parsing and type
 	 *         checking the code.
 	 */
 	public List<RecognitionException> checkCode(String code) {
 		compiler = new Compiler(types, packages);
 		return compiler.check(code);
 	}
 
 	/**
 	 * @return path to working directory.
 	 */
 	private String currentPath() {
 		return Platform.getInstanceLocation().getURL().getPath();
 	}
 
 	/**
 	 * Get parent directory of the file given by its path.
 	 * 
 	 * @param fileWithPath
 	 *            The file of which we want to get the parent directory.
 	 * @return the path to the parent directory.
 	 */
 	private File getWorkspacePath(String fileWithPath) {
 		return new File(fileWithPath).getAbsoluteFile().getParentFile();
 	}
 
 	/**
 	 * Read a file given by its path into a String.
 	 * 
 	 * @param fileName
 	 *            the file to read
 	 * @return the contents of the file
 	 * @throws IOException
 	 */
 	private String readFile(String fileName) throws IOException {
 		BufferedReader fstream = new BufferedReader(new FileReader(fileName));
 		String result = "";
 
 		while (fstream.ready())
 			result += fstream.readLine() + System.getProperty("line.separator");
 
 		return result;
 	}
 
 	/**
 	 * Translates the token names given into the PseudoCode representation.
 	 * 
 	 * @param possibleTokens
 	 *            the token to translate
 	 * @return the List of translated Strings.
 	 */
 	private List<String> translateAutocompletionString(
 			List<String> possibleTokens) {
 		ArrayList<String> translatedCompletions = new ArrayList<String>();
 		for (String toTranslate : possibleTokens) {
 			List<String> translations = translateCompletion.get(toTranslate);
 			if (null == translations)
 				translatedCompletions.add(toTranslate);
 			else
 				for (String t : translations)
 					translatedCompletions.add(t);
 		}
 
 		return translatedCompletions;
 	}
 
 	/**
 	 * Finds the previous Token to the position given.
 	 * 
 	 * @param the
 	 *            line of the Position
 	 * @param charPositionInLine
 	 *            the offset in the line.
 	 * @return the previous token to the position given. Null if the given
 	 *         position is the first Token.
 	 */
 	private Token findPreviousToken(int line, int charPositionInLine) {
 		Token currentToken = compiler.getLexer().getTokenByNumbers(line,
 				charPositionInLine);
 		if (currentToken != null
 				&& ((currentToken.getLine() < line || (currentToken
 						.getCharPositionInLine() + currentToken.getText()
 						.length()) < charPositionInLine))) {
 			currentToken = null;
 		}
 		Token previousToken = null;
 		/*
 		 * currentToken is null, when Whitespace is currentChar --> find
 		 * previous Token
 		 */
 		if (currentToken == null) {
 			List<Token> tokens = compiler.getLexer().getTokens();
 			for (Token token : tokens) {
 				if (line > token.getLine()
 						|| ((line == token.getLine()) && charPositionInLine >= token
 								.getCharPositionInLine())) {
 					currentToken = token;
 				} else {
 					return currentToken;
 				}
 			}
 		} else {
 			int previousTokenIndex = currentToken.getTokenIndex() - 1;
 			// channel = 99 indicates a whitespace or comment token
 			while (previousToken == null || previousToken.getChannel() == 99) {
 				if (previousTokenIndex < 0) {
 					break;
 				} else {
 					previousToken = compiler.getLexer().getTokens()
 							.get(previousTokenIndex);
 					previousTokenIndex--;
 				}
 			}
 		}
 		/* previousToken is null when, the token to Complete is the first token */
 		return previousToken;
 	}
 
 	/**
 	 * Returns the ArrayList<String[]> containing all variables declared until
 	 * this line at charPositionInLine.
 	 * 
 	 * @param line
 	 *            the line
 	 * @param charPositionInLine
 	 *            the offset in the line given
 	 * @return the ArrayList<String[]> containing all variables declared until
 	 *         the position given by the parameters. The String Array contains
 	 *         the Type at index 0 and the variable name at index 1;
 	 */
 	private ArrayList<String[]> getDefinedVariables(int line,
 			int charPositionInLine) {
 		/* HashMap contains varName -> Type entry */
 		// HashMap<String,String> variables = new HashMap<String,String>();
 		ArrayList<String[]> variables = new ArrayList<String[]>();
 		List<Token> tokenList = getTokens();
 		for (Token token : tokenList) {
 			if (token.getType() != -1
 					&& getTokenName(token.getType()).equals("TYPE")) {
 				if (token.getLine() <= line
 						&& token.getCharPositionInLine() <= charPositionInLine) {
 					/*
 					 * This Token can be start of variable definition and fits
 					 * the condition
 					 */
 					int tokenIndex = token.getTokenIndex();
 					if ((tokenIndex) <= tokenList.size())
 						;
 					Token nextToken = tokenList.get(tokenIndex + 1);
 					String nextTokenName = getTokenName(nextToken.getType());
 					if (nextTokenName.equals("ID")) {
 						String[] variableSet = { nextToken.getText(),
 								token.getText() };
 						variables.add(variableSet);
 					}
 				}
 			}
 		}
 		return variables;
 	}
 
 	/**
 	 * @return exceptions produced when lexing, parsing and type checking the
 	 *         code.
 	 */
 	public List<RecognitionException> getExceptions() {
 		return compiler.getExceptions();
 	}
 
 	/**
 	 * This method copies the Dummy Algorithm file next to the PCAlgorithm file
 	 * that is written by the user. To get the path of the created file see
 	 * getAlgorithmPath().
 	 * 
 	 * @param pathToAlgorithm
 	 *            relative to Alvis-workspace e.g.: "project/src/Algorithm.algo"
 	 * @return Name of the Java Algorithm file
 	 */
 	public File compileThisDummy(String pathToAlgorithm) {
 		String SLASH = File.separator;
 
 		// the path were the translated java file is.
 		String pathWhereTheJavaIs = "";
 		try {
 			pathWhereTheJavaIs = FileLocator
 					.getBundleFile(Activator.getDefault().getBundle())
 					.getCanonicalPath().toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// The compiled algorithm
 		File source = new File(pathWhereTheJavaIs + SLASH + "Algorithm.java");
 
 		// Get the path to algorithm and separate path and filename
 		String algoWorkSpacePath = new File(pathToAlgorithm).getParent();
 		algorithmPath = currentPath() + algoWorkSpacePath + SLASH;
 		File destination = new File(algorithmPath + "Algorithm.java");
 
 		// Copy compiled file into the workspace
 		FileCopy fileCopy = new FileCopy();
 		fileCopy.copy(source, destination);
 
 		return destination;
 	}
 
 	/**
 	 * @return a list of all type names available for the user.
 	 */
 	public Collection<String> getDatatypes() {
 		return compiler.getDatatypes();
 	}
 
 	/**
 	 * Tell the compiler which types are allowed.
 	 * 
 	 * @param datatypes
 	 *            the types
 	 */
 	public void setDatatypes(Collection<PCObject> datatypes) {
 		this.types = datatypes;
 	}
 
 	/**
 	 * Tell the compiler which packages the user can use. I.e. what packages the
 	 * compiler is supposed to import.
 	 * 
 	 * @param datatypePackages
 	 *            the packages
 	 */
 	public void setDatatypePackages(Collection<String> datatypePackages) {
 		this.packages = datatypePackages;
 	}
 
 	/**
 	 * Tell the lexer to read the input stream so it can provide auto completion
 	 * and help for syntax highlighting.
 	 */
 	public void reLex() {
 		compiler.getLexer().scan();
 	}
 
 	/**
 	 * Computes the possible autoCompletion for the line and charPositionInLine
 	 * given. Returns a List containing all available completions as
 	 * CompletionInformation.
 	 * 
 	 * @param line
 	 *            the line
 	 * @param charPositionInLine
 	 *            the offset in the line given
 	 * @return the List of CompletionInformation available at this line at
 	 *         charPositionInLine.
 	 */
 	public List<CompletionInformation> tryAutoCompletion(int line,
 			int charPositionInLine) {
 
 		Token tokenToComplete = compiler.getLexer().getTokenByNumbers(line,
 				charPositionInLine);
 		if (tokenToComplete != null
 				&& ((tokenToComplete.getLine() < line || (tokenToComplete
 						.getCharPositionInLine() + tokenToComplete.getText()
 						.length()) < charPositionInLine))) {
 			tokenToComplete = null;
 		}
 
 		int prefixLength = 0;
 		String prefix = "";
 		if (tokenToComplete != null) {
 			/*
 			 * getPrefix and prefixLength and override line and
 			 * charPositionInLine
 			 */
 			prefixLength = charPositionInLine
 					- tokenToComplete.getCharPositionInLine();
 			prefix = tokenToComplete.getText().substring(0, prefixLength);
 			line = tokenToComplete.getLine();
 			charPositionInLine = tokenToComplete.getCharPositionInLine();
 		}
 		Token previousToken = null;
 		if (tokenToComplete != null
 				&& (tokenToComplete.getText().equals(".")
 						|| tokenToComplete.getText().equals("(") || tokenToComplete
 						.getText().equals("{"))) {
 			previousToken = tokenToComplete;
 			prefix = "";
 			prefixLength = 0;
 			charPositionInLine = tokenToComplete.getCharPositionInLine()
 					+ tokenToComplete.getText().length();
 		} else {
 			previousToken = findPreviousToken(line, charPositionInLine);
 		}
 		List<CompletionInformation> availableProposals = new ArrayList<CompletionInformation>();
 
 		if (previousToken == null) {
 			/** current token is first token */
 			List<Token> tokens = compiler.getLexer().getTokens();
 			boolean containsMain = false;
 			for (Token currToken : tokens) {
 				if (getTokenName(currToken.getType()) != null
 						&& getTokenName(currToken.getType()).equals("MAIN")) {
 					containsMain = true;
 				}
 			}
 			if (!containsMain) {
 				prefixLength = charPositionInLine;
				prefix = tokenToComplete.getText().substring(0, prefixLength);
				line = tokenToComplete.getLine();
				charPositionInLine = tokenToComplete.getCharPositionInLine();
 				availableProposals.add(new CompletionInformation("main", line,
 						charPositionInLine, prefixLength));
 			}
 		} else {
 			/*
 			 * previousToken was set --> get possibleFollowing Tokens and create
 			 * code-Completion Information
 			 */
 			List<String> possibleTokens = compiler.getParser()
 					.possibleFollowingTokens(TParser.class,
 							getTokenName(previousToken.getType()));
 			/* Remove the tokens which should not be completed */
 			possibleTokens.remove("DOT");
 
 			List<String> viableCompletionStrings = translateAutocompletionString(possibleTokens);
 			/* Some cases have to be handled separately */
 
 			/* Handle SEMICOLON Token */
 			if (getTokenName(previousToken.getType()).equals("SEMICOLON")) {
 				/* add all variable names which are available at this position */
 				ArrayList<String[]> definedVariables = getDefinedVariables(
 						line, charPositionInLine);
 
 				Iterator<String[]> iterator = definedVariables.iterator();
 				while (iterator.hasNext()) {
 					String[] varSet = iterator.next();
 					if (varSet.length == 2) {
 						viableCompletionStrings.add(varSet[0]);
 					}
 				}
 				viableCompletionStrings.addAll(AbstractTLexer.getTypes());
 			}
 			/* EndOf Handle SEMICOLON token */
 
 			/* Handle ID Token */
 			if (possibleTokens.contains("ID")) {
 				if (getTokenName(previousToken.getType()).equals("DOT")) {
 					/* getting full prefix(until whitespace is found */
 					int currentTokenIndex = previousToken.getTokenIndex() - 1;
 					Token currentToken = compiler.getLexer().getTokens()
 							.get(currentTokenIndex);
 					String stillPrefix = "ID";
 					Stack<String> idToTest = new Stack<String>();
 					while (currentTokenIndex > 0
 							&& getTokenName(currentToken.getType()).equals(
 									stillPrefix)) {
 						if (stillPrefix.equals("ID")) {
 							idToTest.push(currentToken.getText());
 							stillPrefix = "DOT";
 						} else {
 							stillPrefix = "ID";
 						}
 						currentTokenIndex = currentTokenIndex - 1;
 						currentToken = compiler.getLexer().getTokens()
 								.get(currentTokenIndex);
 					}
 
 					List<Token> identifiers = getIdentifiers();
 					if (!idToTest.isEmpty()) {
 						String firstID = idToTest.pop();
 						/* getting first index of id */
 						int idIndex = -1;
 						for (int i = 0; i < identifiers.size(); i++) {
 							Token token = identifiers.get(i);
 							if (token.getText().equals(firstID)) {
 								idIndex = i;
 								break;
 							}
 						}
 						if (idIndex != -1) {
 							Token varToken = identifiers.get(idIndex);
 							Token varType = getTokens().get(
 									varToken.getTokenIndex() - 1);
 							if (getTokenName(varType.getType()).equals("TYPE")) {
 								for (PCObject dataType : this.types) {
 									try {
 										String type = (dataType.getClass())
 												.getMethod("getTypeName", null)
 												.invoke(null, null).toString();
 										if (type.equals(varType.getText())) {
 											viableCompletionStrings
 													.addAll(dataType
 															.getMembers());
 											/*
 											 * add () to mark methods as methods
 											 */
 											List<String> methods = dataType
 													.getMethods();
 											for (String method : methods) {
 												viableCompletionStrings
 														.add(method + "()");
 											}
 										}
 									} catch (IllegalArgumentException e) {
 										Logger.getInstance()
 												.log("alvis.compiler->CompilerAccess",
 														Logger.ERROR,
 														"IlligelArgumentException in tryAutoCompletion : "
 																+ e.getMessage());
 									} catch (SecurityException e) {
 										Logger.getInstance()
 												.log("alvis.compiler->CompilerAccess",
 														Logger.ERROR,
 														"IlligelArgumentException in tryAutoCompletion : "
 																+ e.getMessage());
 									} catch (IllegalAccessException e) {
 										Logger.getInstance()
 												.log("alvis.compiler->CompilerAccess",
 														Logger.ERROR,
 														"IlligelArgumentException in tryAutoCompletion : "
 																+ e.getMessage());
 									} catch (InvocationTargetException e) {
 										Logger.getInstance()
 												.log("alvis.compiler->CompilerAccess",
 														Logger.ERROR,
 														"IlligelArgumentException in tryAutoCompletion : "
 																+ e.getMessage());
 									} catch (NoSuchMethodException e) {
 										Logger.getInstance()
 												.log("alvis.compiler->CompilerAccess",
 														Logger.ERROR,
 														"IlligelArgumentException in tryAutoCompletion : "
 																+ e.getMessage());
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			/* EndOF Handle ID */
 
 			/*
 			 * create a CompletionInformation Object if prefix fits the
 			 * viableCompletionString
 			 */
 			for (String completionString : viableCompletionStrings) {
 				if (completionString.startsWith(prefix)) {
 					CompletionInformation completionInfomation = new CompletionInformation(
 							completionString, line, charPositionInLine,
 							prefixLength);
 					availableProposals.add(completionInfomation);
 				}
 			}
 		}
 		return availableProposals;
 
 	}
 
 	/**
 	 * @return List of Tokens that are forbidden because they are Java keywords.
 	 */
 	public List<Token> getForbidden() {
 		return compiler.getLexer().getForbidden();
 	}
 
 	/**
 	 * @return List of all identifiers used in the code
 	 */
 	public List<Token> getIdentifiers() {
 		return compiler.getLexer().getIdentifiers();
 	}
 
 	/**
 	 * @return List of all keywords the compiler recognizes (e.g. 'if', 'else',
 	 *         ...)
 	 */
 	public List<Token> getKeywords() {
 		return compiler.getLexer().getKeywords();
 	}
 
 	/**
 	 * Create a list of all the tokens in the given source code that mark the
 	 * beginning of a block.
 	 * 
 	 * @return List of tokens that mark the beginning of a block
 	 */
 	public List<Token> beginBlock() {
 		return compiler.getLexer().beginBlock();
 	}
 
 	/**
 	 * Create a list of all the tokens in the given source code that mark the
 	 * end of a block.
 	 * 
 	 * @return List of tokens that mark the end of a block
 	 */
 	public List<Token> endBlock() {
 		return compiler.getLexer().endBlock();
 	}
 
 	/**
 	 * @return List of all available keywords.
 	 */
 	public List<String> allKeywords() {
 
 		return AbstractTLexer.allKeywords();
 	}
 
 	/**
 	 * Return a list of all the Java keywords that the pseudo code does not use.
 	 * 
 	 * @return List of forbidden words
 	 */
 	public List<String> allForbidden() {
 		return AbstractTLexer.allForbidden();
 	}
 
 	/**
 	 * @return List of all tokens created by the lexer.
 	 */
 	public List<Token> getTokens() {
 		return compiler.getLexer().getTokens();
 	}
 
 	/**
 	 * Translate the internal number of a token type to its name.
 	 * 
 	 * @param tokenNumber
 	 *            The number to translate.
 	 * @return The name of the token type with that number.
 	 */
 	public String getTokenName(int tokenNumber) {
 		return compiler.getParser().getTokenName(tokenNumber);
 	}
 
 	/**
 	 * Method for checking whether the compiler is informed correctly about
 	 * available packages and types.
 	 */
 	@SuppressWarnings("unchecked")
 	public void testDatatypes() {
 		try {
 			System.out.println("Compiler shows its datatypes:");
 			for (PCObject obj : types) {
 				System.out.println(obj.getClass());
 				List<String> tmp = ((List<String>) obj.getClass()
 						.getMethod("getMembers").invoke(obj));
 				System.out.println("available attributes:"
 						+ (tmp == null ? "null" : tmp));
 				tmp = ((List<String>) obj.getClass().getMethod("getMethods")
 						.invoke(obj));
 				System.out.println("available methods:"
 						+ (tmp == null ? "null" : tmp));
 			}
 			System.out.println("Compiler shows its packages:");
 			for (String obj : packages) {
 				System.out.println(obj);
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 	}
 }
