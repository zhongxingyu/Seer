 package com.elezeta.gnlp;
 
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Collection;
 import java.util.Map;
 
 import org.modelcc.IModel;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserException;
 import org.modelcc.probabilistic.ProbabilityValue;
 import org.modelcc.tools.FieldFinder;
 
 import com.elezeta.gnlp.languages.explicit.ExplicitParserFactory;
 import com.elezeta.gnlp.model.Sentence;
 
 public class GNLPCLI {
 
 	private static DecimalFormat df;
 	
 	public static void main(String[] args) {
 		df = new DecimalFormat("#.0000");
 		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
 		dfs.setGroupingSeparator(',');
 		dfs.setDecimalSeparator('.');
 		df.setDecimalFormatSymbols(dfs);
 
 		if (args.length == 0) {
 			System.out.println("GNLP (General Natural Language Parser) - Command-Line Interface");
 			System.out.println("Copyright (c) 2013, Luis Quesada - https://github.com/lquesada");
 		    System.out.println("");
 		    System.out.println("Usage: java -jar GNLP.jar <language> <sentence>");
 		    System.out.println("");
 		    System.err.println("Example: java -jar GNLP.jar explicit \"It(Pronoun) is(Verb) raining(Verb)\"");
 		    System.out.println("");
 		    System.out.println("Supported languages:");
 		    System.out.println("");
 		    System.out.println("- explicit");
 		    System.out.println("");
 		    System.out.println("  Specify language elements directly: ");
 		    System.out.println("    CoordinatingConjunction, SubordinatingConjunction, Verb, Adverb,");
 		    System.out.println("    Preposition, Determiner, CommonNoun, ProperNoun, Pronoun");
 		    System.out.println("");
 		    System.out.println("  Example sentence: ");
 		    System.out.println("");
 		    System.out.println("  I(CommonNoun|ProperNoun) saw(CommonNoun|Verb) a(Determiner) tree(CommonNoun)");
 		    System.exit(0);
 		}
 		if (args.length == 1) {
 			System.err.println("Please, provide both the language identifier and the sentence to parse.");
 		    System.err.println("");
 		    System.err.println("Example: java -jar GNLP.jar explicit \"It(Pronoun) is(Verb) raining(Verb)\"");
 		    System.exit(1);
 		}
 		if (args.length > 2) {
 			System.err.println("Please, provide the sentence as a single argument. Use quotes if necessary.");
 		    System.err.println("");
 		    System.err.println("Example: java -jar GNLP.jar explicit \"It(Pronoun) is(Verb) raining(Verb)\"");
 		    System.exit(1);
 		}
 		Parser<Sentence> parser = null;
 		if (args[0].equals("explicit")) {
 			try {
 				parser = ExplicitParserFactory.generateParser();
 			} catch (Exception e) {
 				System.err.println("Exception while generating parser.");
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		else {
 			System.err.println("Invalid language: "+args[0]);
 		    System.exit(1);
 		}
 		if (parser != null) {
 			String inputString = args[1];
 			try {
 				Collection<Sentence> interpretations = parser.parseAll(inputString);
 				System.out.println("Found "+interpretations.size()+" interpretations.");
 				
 				for (Sentence sentence : interpretations) {
 					System.out.println(" ");
 					show(sentence,inputString,parser);
 				}
 			} catch (ParserException e) {
 				System.err.println("No valid parse trees found.");
 			    System.exit(2);
 			}
 		}
 	}
 
 	private static void show(Object object,String inputString,Parser parser) {
 		show(object,0,inputString,parser);
 	}
 
     private static void show(Object object,int indent,String inputString,Parser parser) {
     	boolean array = false;
     	Class clazz = object.getClass();
     	if (clazz.isArray()) {
     		array = true;
     		clazz = clazz.getComponentType();
     	}
 		if (IModel.class.isAssignableFrom(clazz)) {
 			Map<String,Object> metadata = parser.getParsingMetadata(object);
 			if (!array) {
 				int startIndex = (Integer)metadata.get("startIndex");
 				int endIndex = (Integer)metadata.get("endIndex")+1;
 				double probability = ((ProbabilityValue)metadata.get("probability")).getNumericValue();
 				System.out.println(getIndent(indent)+"["+df.format(probability)+"] - "+object.getClass().getSimpleName()+": "+inputString.substring(startIndex,endIndex));
 				for (Field field : FieldFinder.getAllFields(object.getClass())) {
 					field.setAccessible(true);
 					try {
 						Object content = field.get(object);
 						if (content != null)
 							show(content,indent+2,inputString,parser);
 					} catch (Exception e) {
 						System.err.println("Cannot show field contents.");
 						e.printStackTrace();
 					}
 				}
 			}
 			else {
				System.out.println(getIndent(indent)+"[-.----] - "+object.getClass().getSimpleName()+"*");
 				for (Object content : (Object[])object) {
 					if (content != null)
						show(content,indent+2,inputString,parser);
 				}
 				
 			}
 		}
 		
 
 	}
 
 	private static String getIndent(int indent) {
 		String ret = "";
 		for (int i = 0;i < indent;i++)
 			ret += ' ';
 		return ret;
 	}
 }
 	    
