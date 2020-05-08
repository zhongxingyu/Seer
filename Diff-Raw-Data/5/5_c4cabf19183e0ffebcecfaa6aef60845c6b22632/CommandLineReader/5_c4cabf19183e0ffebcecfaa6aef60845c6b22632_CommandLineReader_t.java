 package ch.uzh.ejb.bank.client;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 
 import org.jboss.security.auth.callback.UsernamePasswordHandler;
 
 import ch.uzh.ejb.bank.BankApplicationTestRemote;
 
 import jline.ArgumentCompletor;
 import jline.Completor;
 import jline.ConsoleReader;
 import jline.SimpleCompletor;
 
 public class CommandLineReader {
 
 	private ConsoleReader reader;
 	private BankApplicationTestRemote bankApplication;
 
 	public CommandLineReader() throws IOException, NamingException {
 		setupConsoleReader();
 		setupWebServiceBinding();
 	}
 
 	private void setupWebServiceBinding() throws NamingException {
 		Properties props = new Properties();
 		props.put("java.naming.factory.initial",
 				"org.jnp.interfaces.NamingContextFactory");
 		props.put("java.naming.provider.url", "localhost:1099");
 		InitialContext context = new InitialContext(props);
 		bankApplication = (BankApplicationTestRemote) context
 				.lookup("BankApplicationTestBean/remote");
 	}
 
 	private void setupConsoleReader() throws IOException {
 		reader = new ConsoleReader();
 		reader.setBellEnabled(false);
 		reader.setDebug(new PrintWriter(new FileWriter("writer.debug", true)));
 		List<Completor> completors = new LinkedList<Completor>();
 
 		completors
 				.add(new SimpleCompletor(new String[] { "foo", "bar", "baz" }));
 
 		reader.addCompletor(new ArgumentCompletor(completors));
 	}
 
 	public void start() throws IOException {
 		String line;
 		PrintWriter out = new PrintWriter(System.out);
 
 		while ((line = reader.readLine("bankservice> ")) != null) {
 //			out.println("==> " + line);
 //			out.flush();
 			String normalizedLine = line.toLowerCase().trim();
 
 			if (isExitCommand(normalizedLine)) {
 				break;
 			} else if (isBlankLine(normalizedLine)) {
 				continue;
 			} else {
 				parseLine(line.trim()); // no lower-casing intentional
 			}
 		}
 	}
 
 	private boolean isBlankLine(String normalizedLine) {
 		return normalizedLine.equals("");
 	}
 
 	private boolean isExitCommand(String line) {
 		return line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit");
 	}
 
 	private void parseLine(String line) {
 		StringTokenizer tokenizer = new StringTokenizer(line);
 		if (tokenizer.hasMoreTokens()) {
 			dispatchCommand(tokenizer);
 		}
 	}
 
 	private void dispatchCommand(StringTokenizer tokenizer) {
 		String firstToken = tokenizer.nextToken();
 		try {
 			if (firstToken.equals("login")) {
 				new LoginCommandHandler(this, tokenizer).execute();
			} else {
				System.err.println("[ERROR] Unknown command: "+firstToken);
 			}
 		} catch (Exception e) {
			System.err.println("[ERROR] Command unsuccessful: "+e.getMessage());
 		}
 	}
 
 	public static void login(String username, String password)
 			throws LoginException {
 		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
 				password.toCharArray());
 		LoginContext loginContext = new LoginContext("ba", handler);
 		loginContext.login();
 	}
 
 }
