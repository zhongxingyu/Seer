 package com.adagio;
 
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.modelcc.io.ModelReader;
 import org.modelcc.io.java.JavaModelReader;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.metamodel.Model;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserFactory;
 
 import com.adagio.io.lilypond.LilyPondMusicPieceWriter;
 import com.adagio.language.MusicPiece;
 
 public class ADAgioCLI {
 
 	public static void main(String [] args){
 		int i = 0;
 
 		try {
 			
 			System.out.println();
 			
 			if(args.length == 0){
 				System.out.println(".adg file is required: ");
 				System.out.println("java -jar ADAgio.jar <file.adg>");
 				System.out.println("java -jar ADAgio.jar <file1.adg> ... <fileN.adg>");
 				System.exit(0);
 			}
 			
 			ModelReader reader = new JavaModelReader(MusicPiece.class);
 			
 			// Read the language model.
 			Model model = reader.read();
 
 			Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
 			ignore.add(new RegExpPatternRecognizer("//[^\n]*(\n|$)"));
 			ignore.add(new RegExpPatternRecognizer("( |\n|\t|\r)+"));
 
 			// Generate a parser from the model.
 			@SuppressWarnings("unchecked")
 			Parser<MusicPiece> parser = ParserFactory.create(model,ignore);
 			
 
 			//Read the Music Lore information and mix it with input.
 			InputStream inStream = ADAgioCLI.class.getResourceAsStream("MusicTheory.mth");
 			if(inStream == null){
 				System.err.println("Error reading MusicTheory");
 			}
 
 			for(String current: args){
 				if( i == 9){
 					System.out.println();
 				}
 				i++;
 				
 				System.out.println("Processing " +  relativePath(current) + "...");
 				
 				String [] preprocessedFiles = AdagioPreprocessor.preprocess("MusicTheory.mth", current);
 				String finalInput = AdagioLinker.link(preprocessedFiles[0], preprocessedFiles[1]);
				String outFileName = current.replace(".adg", ".ly");
 				
 				// Parse an input string.
 				MusicPiece result = parser.parse(finalInput);
 
 				PrintWriter out = (new PrintWriter(outFileName));
 				LilyPondMusicPieceWriter.writeMusicPiece(result,out);
 				out.close();
 
 				System.out.println("Generated " + relativePath(outFileName) + "\n");
 				System.gc();
 			}	
 
 		} catch (Exception e) {
 			System.err.println("Syntax error");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 	}
 	
 	public static String relativePath(String path){
 		
 		if(path.lastIndexOf("\\") > -1 && path.lastIndexOf("\\") < path.length()){
 			return path.substring(path.lastIndexOf("\\")+1, path.length());
 		}
 		else if(path.lastIndexOf("/") > -1 && path.lastIndexOf("/") < path.length()){
 			return path.substring(path.lastIndexOf("/")+1, path.length());
 		}
 		
 		return path;
 	}
 
 }
