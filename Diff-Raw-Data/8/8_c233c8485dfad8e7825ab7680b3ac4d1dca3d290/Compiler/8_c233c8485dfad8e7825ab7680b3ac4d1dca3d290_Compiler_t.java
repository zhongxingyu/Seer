 package com.github.davidmoten.asn1;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import com.github.davidmoten.asn1.Asn1Parser.moduleDefinitions_return;
 
 public class Compiler {
 
 	private final List<File> files;
 
 	public Compiler(List<File> files) {
 		this.files = files;
 	}
 
 	public void compile() {
 		for (File file : files) {
 			try {
 				CharStream s = new ANTLRFileStream(file.getPath());
 
 				Asn1Lexer lexer = new Asn1Lexer(s);
 				CommonTokenStream tokens = new CommonTokenStream(lexer);
 
 				Asn1Parser parser = new Asn1Parser(tokens);
 				for (String token : parser.getTokenNames()) {
 					System.out.println(token);
 				}
				moduleDefinitions_return defs = parser.moduleDefinitions();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
			} catch (RecognitionException e) {
				throw new RuntimeException(e);
 			}
 		}
 	}
 }
