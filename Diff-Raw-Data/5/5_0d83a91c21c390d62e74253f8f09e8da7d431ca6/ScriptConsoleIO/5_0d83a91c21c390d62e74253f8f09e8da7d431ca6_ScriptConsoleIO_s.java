 package org.eclipse.dltk.console;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class ScriptConsoleIO implements IScriptConsoleIO {
 	private static final String INTERPRETER = "interpreter";
 
 	private static final String SHELL = "shell";
 
 	private InputStream input;
 
 	private OutputStream output;
 
 	private String id;
 
 	protected static String readFixed(int len, InputStream input)
 			throws IOException {
 		byte[] buffer = new byte[len];
 		int from = 0;
 		while (from < buffer.length) {
 			int n = input.read(buffer, from, buffer.length - from);
 			if (n == -1) {
 				return null;
 			}
 			from += n;
 		}
 		return new String(buffer);
 	}
 
 	protected static int readLength(InputStream input) throws IOException {
 		String strLen = readFixed(10, input);
 		if (strLen == null) {
 			return -1;
 		}
 
 		return Integer.parseInt(strLen);
 	}
 
 	protected static String readResponse(InputStream input) throws IOException {
 		int len = readLength(input);
 		if (len == -1) {
 			return null;
 		}
 
 		String xml = readFixed(len, input);
 
 		if (xml == null) {
 			return null;
 		}
 
 		return xml;
 	}
 
 	public ScriptConsoleIO(InputStream input, OutputStream output)
 			throws IOException {
 		if (input == null || output == null) {
 			throw new IllegalArgumentException();
 		}
 		
 		this.input = input;
 		this.output = output;
 
 		this.id = ScriptConsoleXmlHelper.parseInfoXml(readResponse(input));
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public ShellResponse execShell(String command, String[] args)
 			throws IOException {
 
 		output.write((SHELL + "\n").getBytes());
 		output.write((command + "\n").getBytes());
 
 		for (int i = 0; i < args.length; ++i) {
 			output.write((args[i] + "\n").getBytes());
 		}
 
 		output.flush();
		return ScriptConsoleXmlHelper.parseShellXml(readResponse(input));
 	}
 
 	public InterpreterResponse execInterpreter(String command)
 			throws IOException {
 		output.write((INTERPRETER + "\n").getBytes());
 		output.write((command + "\n").getBytes());
 		output.flush();
 
 		return ScriptConsoleXmlHelper.parseInterpreterXml(readResponse(input));
 	}
 
 	public void close() throws IOException {
 		input.close();
 		output.close();
 	}
 }
