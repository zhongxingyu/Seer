 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.csm;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.modelcc.csm.language.Mapping;
 import org.modelcc.io.ModelReader;
 import org.modelcc.io.java.JavaModelReader;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.metamodel.Model;
 import org.modelcc.parser.CannotCreateParserException;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserFactory;
 
 import test.languages.arithmeticcalculator2.Expression2;
 
 /**
  * Simple Mapping Reader class.
  * @author elezeta
  * @serial
  */
 public final class CSM implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Clone a model and apply CSM
      * @param model model
      * @param input CSM text
      * @return constrained clone of the model.
      * @throws CannotApplyMappingException when the mapping cannot be applied
      */
     public static Model apply(Model original,String input) throws CannotApplyMappingException {
     	Model clone = original.clone();
  	
 		try {
 	        ModelReader jmr = new JavaModelReader(Mapping.class);
 	        Model m = jmr.read();
 	        new HashSet<PatternRecognizer>();
 	        Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
 	        ignore.add(new RegExpPatternRecognizer("[\\t\\ \\n\\r]+"));
 	        Parser<Mapping> parser = ParserFactory.create(m,ignore);
 	        Mapping mapping = parser.parse(input);
 	        mapping.apply(clone);
 		} catch (Exception e1) {
 			throw new CannotApplyMappingException(e1);
 		}
 
		return clone;
 	}
 
     /**
      * Apply CSM to a model
      * @param model model
      * @param input CSM text
      * @return constrained model.
      * @throws CannotApplyMappingException when the mapping cannot be applied
      */
     public Model apply(Model model,Reader input) throws CannotApplyMappingException {
     	String inputs = "";
         int n;
         Writer writer = new StringWriter();
         char[] buffer = new char[1024];
         try {
             while ((n = input.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
         }
         catch (Exception e) {
 
         }
         inputs = writer.toString();
         return apply(model,inputs);
     }
     
 }
