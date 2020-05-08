 package titocc.gui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.List;
 import java.util.Scanner;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 import titocc.compiler.Compiler;
 import titocc.compiler.Parser;
 import titocc.compiler.elements.TranslationUnit;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.Token;
 import titocc.tokenizer.Tokenizer;
 
 /**
  * Source code file that may have a file on the disk associated with it. Provides functionality to
  * compile the file and write/read the contents to/from disk.
  */
 public class SourceFile
 {
 	/**
 	 * Document object that contains the actual text data.
 	 */
 	private final PlainDocument document;
 
 	/**
 	 * File object associated with this source file.
 	 */
 	private File file;
 
 	/**
 	 * Constructs a new SourceFile object.
 	 */
 	public SourceFile()
 	{
 		document = new PlainDocument();
 	}
 
 	/**
 	 * Returns the Document object associated with the source file to be used with JTextArea etc.
 	 *
 	 * @return the Document object
 	 */
 	public Document getDocument()
 	{
 		return document;
 	}
 
 	/**
 	 * Returns the File object for the source file.
 	 *
 	 * @return the File object or null if the file has not been saved to disk
 	 */
 	public File getFile()
 	{
 		return file;
 	}
 
 	/**
 	 * Returns the name of the file without the directory path.
 	 *
 	 * @return filename
 	 */
 	public String getName()
 	{
 		return file != null ? file.getName() : "main.c";
 	}
 
 	/**
 	 * Returns textual contents of the source file.
 	 *
 	 * @return the source file contents as text
 	 */
 	public String getText()
 	{
 		String text = "";
 		try {
 			text = document.getText(0, document.getLength());
 		} catch (BadLocationException e) {
 		}
 		return text;
 	}
 
 	/**
 	 * Compiles the source file and optionally creates the output file.
 	 *
 	 * @param log logger for compiler messages and errors
 	 * @param writer Writer object used for compiler output
 	 * @param createOutputFile if true, additionally a .k91 output file will be created
 	 * @throws IOException if writing the output file fails
 	 */
 	public void compile(MessageLog log, Writer writer, boolean createOutputFile) throws IOException
 	{
 		log.logMessage("Compiling file " + getName() + ".");
 		StringWriter assemblyCode = null;
 		String sourceCode = getText();
 		Tokenizer tokenizer = new Tokenizer(new StringReader(sourceCode));
 		try {
 			List<Token> tokens = tokenizer.tokenize();
 			log.logMessage("Tokenization completed successfully.");
 
 			TranslationUnit trUnit = Parser.parse(tokens);
 			//writer.append(";PARSER OUTPUT: " + trUnit.toString() + "\n");
 			log.logMessage("Parsing completed successfully.");
 
 			Compiler compiler = new Compiler(trUnit);
 			assemblyCode = new StringWriter();
 			compiler.compile(assemblyCode);
 			log.logMessage("Compilation completed successfully.");
 			writer.write(assemblyCode.toString());
 		} catch (SyntaxException e) {
 			int line = e.getPosition().line + 1;
 			int character = e.getPosition().column + 1;
 			log.logMessage("Compiler error (line " + line + ", ch " + character + "): "
 					+ e.getMessage());
 		} catch (Exception e) {
			log.logMessage("Compilation failed: " + e.getMessage());
 		}
 
 		if (createOutputFile && assemblyCode != null && file != null)
 			writeOutputFile(assemblyCode.toString());
 	}
 
 	/**
 	 * Writes the source code file to disk.
 	 *
 	 * @param file file in which the contents will be written to
 	 * @throws IOException if writing fails
 	 */
 	public void writeToFile(File file) throws IOException
 	{
 		FileWriter writer = new FileWriter(file);
 		try {
 			String text = getText();
 			writer.write(text);
 			writer.close();
 			this.file = file;
 		} finally {
 			writer.close();
 		}
 	}
 
 	/**
 	 * Creates a new source file with default contents.
 	 */
 	public void createNewFile()
 	{
 		try {
 			document.remove(0, document.getLength());
 			document.insertString(0, "int main()\n{\n\treturn 0;\n}", null);
 		} catch (BadLocationException e) {
 		}
 		file = null;
 	}
 
 	/**
 	 * Reads the source file contents from disk.
 	 *
 	 * @param file file from which the contents will be read from
 	 * @throws FileNotFoundException if file doesn't exist
 	 */
 	public void readFromFile(File file) throws FileNotFoundException
 	{
 		try {
 			document.remove(0, document.getLength());
 			document.insertString(0, new Scanner(file).useDelimiter("\\A").next(), null);
 			this.file = file;
 		} catch (BadLocationException e) {
 		}
 	}
 
 	/**
 	 * Writes output file to disk.
 	 *
 	 * @param code output file contents
 	 * @throws IOException if write fails
 	 */
 	private void writeOutputFile(String code) throws IOException
 	{
 		FileWriter fileWriter = new FileWriter(new File(getOutputFileName()));
 		try {
 			fileWriter.write(code);
 		} finally {
 			fileWriter.close();
 		}
 	}
 
 	/**
 	 * Generates the name of the output file by replacing the extension with .k91 extension.
 	 *
 	 * @return name of the output file.
 	 */
 	private String getOutputFileName()
 	{
 		String filename = file.getAbsolutePath();
 		int idx = filename.lastIndexOf('.');
 		if (idx != -1)
 			filename = filename.substring(0, idx);
 		filename = filename + ".k91";
 		return filename;
 	}
 }
