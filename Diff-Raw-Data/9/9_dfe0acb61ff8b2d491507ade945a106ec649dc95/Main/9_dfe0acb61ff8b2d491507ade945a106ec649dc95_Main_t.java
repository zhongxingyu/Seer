 package lambda;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Scanner;
 
 import lambda.ast.Lambda;
 import lambda.ast.MacroExpander;
 import lambda.ast.parser.ParserException;
 import lambda.conversion.Converter;
 import lambda.system.CommandDelegate;
 import lambda.system.CommandProcessor;
 import util.nullable.NullableBool;
 import util.nullable.NullableInt;
 
 public class Main
 {
 	private static final Environment env = Environment.getEnvironment();
 	private static final CommandProcessor commands = new CommandProcessor();
 
 	private static char readChar()
 	{
 		char c = ' ';
 		Scanner scan = new Scanner(System.in);
 		if (scan.hasNext())
 		{
 			String line = scan.nextLine().trim();
 			if (line.length() > 0)
 			{
 				c = Character.toLowerCase(line.charAt(0));
 			}
 		}
 		return c;
 	}
 
 	private static Lambda parse(String text)
 	{
 		try
 		{
 			return Lambda.parse(text);
 		}
 		catch (ParserException e)
 		{
 			String s = "";
 			for (int i = 0; i < e.column; i++)
 			{
 				s = s + ' ';
 			}
 			System.out.println(text);
 			System.out.println(s + '^');
 			System.out.println("- " + e.getMessage());
 		}
 		return null;
 	}
 
 	private static void defineMacro(String name, String expr)
 	{
 		Lambda lambda = parse(expr);
 		if (lambda != null)
 		{
 			env.defineMacro(name, lambda);
 			String s = String.format("- <%s> is defined as %s", name, lambda);
 			System.out.println(s);
 		}
 	}
 
 	private static void readMacroDefinition(String s, int line)
 	{
 		if (s.contains("="))
 		{
 			String[] v = s.split("\\s*=\\s*");
 			if (v.length == 2)
 			{
 				defineMacro(v[0], v[1]);
 			}
 			else
 			{
 				System.out.println("- Invalid macro definition: " + s + " at line " + line);
 			}
 		}
 		else
 		{
 			System.out.println("- Line " + line + " is not a macro definition");
 		}
 	}
 
 	private static void evalLine(String line)
 	{
 		if (line.contains("="))
 		{
 			readMacroDefinition(line, 1);
 		}
 		else
 		{
 			Lambda lambda = parse(line);
 			if (lambda == null)
 			{
 				return;
 			}
 
 			System.out.println(lambda);
 
 			LambdaInterpreter interpreter = new LambdaInterpreter(lambda);
 
 			int continueSteps = env.getInt(Environment.KEY_CONTINUE_STEPS, 500);
 			int step = 1;
 			boolean interrupted = false;
 
 			while (interpreter.step(env))
 			{
 				if (env.getBoolean(Environment.KEY_TRACE))
 				{
 					String s = interpreter.getLambda().toString();
 					if ((env.getBoolean(Environment.KEY_SHORT)) && (s.length() > 75))
 					{
 						s = s.substring(0, 35) + " ... " + s.substring(s.length() - 35, s.length());
 					}
 					System.out.printf("%3d: ", step);
 					System.out.println("--> " + s);
 				}
 
 				if (continueSteps > 0 && step % continueSteps == 0)
 				{
 					char c;
 					do
 					{
 						System.out.printf("- (%d steps done) continue?(y/n): ", step);
 						c = readChar();
 					}
 					while (c != 'y' && c != 'n');
 
 					if (c != 'y')
 					{
 						interrupted = true;
 						break;
 					}
 				}
 				step++;
 			}
 			MacroExpander expander = new MacroExpander(env);
 			System.out.print("--> " + expander.expand(interpreter.getLambda()));
 			if (!interrupted)
 			{
 				System.out.print("    ");
 				if (interpreter.isCyclic())
 				{
 					System.out.print("(cyclic reduction)");
 				}
 				else
 				{
 					System.out.print("(normal form)");
 				}
 			}
 			System.out.println();
 
 			if (env.getBoolean(Environment.KEY_DATA_CONV))
 			{
 				NullableInt natValue = Converter.toNat(interpreter.getLambda());
 				if (natValue.hasValue())
 				{
 					System.out.println("  = " + natValue + " (as nat)");
 				}
 				NullableBool boolValue = Converter.toBool(interpreter.getLambda());
 				if (boolValue.hasValue())
 				{
 					System.out.println("  = " + boolValue + " (as bool)");
 				}
 			}
 		}
 	}
 
 	private static void loadFile(String path)
 	{
 		if (!path.endsWith(".lm.txt"))
 		{
 			path = path + ".lm.txt";
 		}
 
 		try
 		{
 			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
 
 			System.out.println("- Load '" + path + "'");
 
 			int lineNumber = 1;
 			String line;
 			while ((line = reader.readLine()) != null)
 			{
 				int c = line.indexOf('#');
 				if (c != -1) line = line.substring(0, c);
 				line = line.trim();
 				if (!line.isEmpty())
 				{
 					readMacroDefinition(line, lineNumber);
 				}
 				lineNumber++;
 			}
 			reader.close();
 		}
 		catch (FileNotFoundException e)
 		{
 			System.out.println("- Unable to open file \"" + path + "\"");
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private static void initializeCommands()
 	{
 		commands.add(":l", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				if (params.length == 0)
 				{
 					File cd = new File(".");
 					for (File file : cd.listFiles())
 					{
 						if (file.isFile() && file.getName().endsWith(".lm.txt"))
 						{
 							loadFile(file.getName());
 						}
 					}
 				}
 				else
 				{
 					for (String fileName : params)
 					{
 						loadFile(fileName);
 					}
 				}
 			}
 		});
 		commands.add(":f", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				if (params.length >= 1)
 				{
 					String expr = "";
 					for (String s : params)
 					{
 						expr = expr + s + " ";
 					}
 					MacroExpander expander = new MacroExpander(env);
 					try
 					{
						System.out.println(expander.expand(Lambda.parse(expr), true));
 					}
 					catch (ParserException e)
 					{
 						System.out.println(e.getMessage());
 					}
 				}
 			}
 		});
 		commands.add(":s", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				if (params.length >= 1)
 				{
 					try
 					{
 						int n = Integer.parseInt(params[0]);
 						if (n >= 0)
 						{
 							env.set(Environment.KEY_CONTINUE_STEPS, n);
 							System.out.print("- set continuation steps to ");
 							if (n != 0)
 							{
 								System.out.println(n);
 							}
 							else
 							{
 								System.out.println("infinite");
 							}
 						}
 						else
 						{
 							System.out.println("- Negative number is not allowed");
 						}
 					}
 					catch (NumberFormatException e)
 					{
 						System.out.println("- Illegal number format: " + params[0]);
 					}
 				}
 				else
 				{
 					System.out.println("- continuation steps is " + env.getInt(Environment.KEY_CONTINUE_STEPS, 500));
 				}
 			}
 		});
 		commands.add(":t", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				if (params.length >= 1)
 				{
 					String s = params[0].toLowerCase();
 					if ((s.equals("on")) || (s.equals("off")))
 					{
 						boolean b = params[0].equals("on");
 						env.set(Environment.KEY_TRACE, b);
 						System.out.println("- set trace " + (b ? "on" : "off"));
 					}
 					else
 					{
 						System.out.println("- type 'on' or 'off' to set trace mode");
 					}
 				}
 				else
 				{
 					System.out.println("- trace is " + (env.getBoolean(Environment.KEY_TRACE) ? "on" : "off"));
 				}
 			}
 		});
 		commands.add(":clear", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				env.clearMacros();
 				System.out.println("- macros were cleared.");
 			}
 		});
 		commands.add(":m", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				env.dumpMacros();
 			}
 		});
 		commands.add(":pwd", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				System.out.println(new File("").getAbsolutePath());
 			}
 		});
 		commands.add(":short", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				boolean b = !env.getBoolean(Environment.KEY_SHORT);
 				env.set(Environment.KEY_SHORT, b);
 				System.out.println("- set short mode " + (b ? "on" : "off"));
 			}
 		});
 		commands.add(":conv", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				boolean b = !env.getBoolean(Environment.KEY_DATA_CONV);
 				env.set(Environment.KEY_DATA_CONV, b);
 				System.out.println("- set data conversion mode " + (b ? "on" : "off"));
 			}
 		});
 		commands.add(":?", new CommandDelegate()
 		{
 			public void commandInvoked(String[] params)
 			{
 				System.out.println("- :?            - show this help.");
 				System.out.println("- :f <expr>     - expand macros.");
 				System.out.println("- :l <name>     - load macro definitions from a text file.");
 				System.out.println("- :l            - load *.lm.txt files in current directory.");
 				System.out.println("- :s <n>        - set the number of continuation steps.");
 				System.out.println("- :s            - show current number of continuation steps.");
 				System.out.println("- :t (on|off)   - set trace mode. ");
 				System.out.println("- :t            - show current trace mode. ");
 				System.out.println("- :m            - list defined macros.");
 				System.out.println("- :clear        - clear all macros.");
 				System.out.println("- :pwd          - print working directory.");
 				System.out.println("- :short        - toggle short print mode.");
 				System.out.println("- :conv         - toggle data conversion mode.");
 				System.out.println("- :q            - quit interpreter.");
 			}
 		});
 	}
 
 	private static void repl()
 	{
 		System.out.println("Lambda * Magica version " + Environment.APPLICATION_VERSION + " (" + Environment.RELEASE_DATE + ")");
 		System.out.println("- Type :? to see command help");
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 		while (true)
 		{
 			System.out.print("> ");
 			String line;
 			try
 			{
 				line = reader.readLine();
 			}
 			catch (IOException e)
 			{
 				System.err.println("- " + e.getMessage());
 				break;
 			}
 
 			if (line == null) break;
 
 			line = line.trim();
 
 			if (line.isEmpty()) continue;
 
 			if (line.charAt(0) == ':')
 			{
 				if (line.startsWith(":q"))
 				{
 					break;
 				}
 				commands.invokeCommand(line);
 			}
 			else
 			{
 				evalLine(line);
 			}
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		initializeCommands();
 		repl();
 	}
 }
 
