 package no.citrus.localprioritization;
 
 import japa.parser.JavaParser;
 import japa.parser.ParseException;
 import japa.parser.ast.CompilationUnit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class CompilationUnitProvider {
 
 	public static List<CompilationUnit> getCompilationUnits(List<File> files) throws ParseException, IOException {
 		List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
 		for(File f : files){
			try {
				compilationUnits.add(JavaParser.parse(f));
			} catch (Exception e) {
			}
 		}
 		return compilationUnits;
 	}
 
 }
