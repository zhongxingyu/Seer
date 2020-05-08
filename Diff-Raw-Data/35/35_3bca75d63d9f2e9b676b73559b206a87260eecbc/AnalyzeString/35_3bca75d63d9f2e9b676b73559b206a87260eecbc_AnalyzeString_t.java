 package org.swows.function;
 
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.swows.node.Skolemizer;
 import org.swows.vocabulary.SWI;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.sparql.expr.NodeValue;
 import com.hp.hpl.jena.sparql.function.FunctionEnv;
 import com.hp.hpl.jena.sparql.graph.GraphFactory;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public class AnalyzeString extends GraphReturningFunction {
 	
 	private static final Node
 		MATCH_CLASS =	Node.createURI( Factory.getBaseURI() + "match"),
 		NON_MATCH_CLASS =	Node.createURI( Factory.getBaseURI() + "non-match"),
 		FIRST_PROPERTY =	Node.createURI( Factory.getBaseURI() + "first"),
 		FIRST_MATCH_PROPERTY =	Node.createURI( Factory.getBaseURI() + "first-match"),
 		FIRST_NON_MATCH_PROPERTY =	Node.createURI( Factory.getBaseURI() + "first-non-match"), 
 		NEXT_PROPERTY =	Node.createURI( Factory.getBaseURI() + "next"),
 		NEXT_MATCH_PROPERTY =	Node.createURI( Factory.getBaseURI() + "next-match"),
 		NEXT_NON_MATCH_PROPERTY =	Node.createURI( Factory.getBaseURI() + "next-non-match"), 
 		FIRST_GROUP_PROPERTY =	Node.createURI( Factory.getBaseURI() + "first-group"),
 		NEXT_GROUP_PROPERTY =	Node.createURI( Factory.getBaseURI() + "next-group"),
		GROUP_NON_MATCH_CLASS =	Node.createURI( Factory.getBaseURI() + "group-non-match"),
 		STRING_PROPERTY =	Node.createURI( Factory.getBaseURI() + "string");
 
 	@Override
 	public int getMinArgNum() {
 		return 2; // input, pattern
 	}
 
 	@Override
 	public int getMaxArgNum() {
 //		return 2; // input, pattern
 		return 3; // input, pattern, flags
 	}
 
 	@Override
 	public Graph exec(final List<NodeValue> args, final FunctionEnv env) {
         NodeValue inputNV = args.get(0);
         NodeValue patternNV = args.get(1);
         int flags = 0;
         if (args.size() > 2) {
             NodeValue flagsNV = args.get(2);
             String flagsStr = flagsNV.asString();
             if (flagsStr.contains("m"))
             	flags |= Pattern.MULTILINE;
             if (flagsStr.contains("s"))
             	flags |= Pattern.DOTALL;
             if (flagsStr.contains("i"))
             	flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
             if (flagsStr.contains("x"))
             	flags |= Pattern.COMMENTS;
             if (flagsStr.contains("d"))
             	flags |= Pattern.UNIX_LINES;
         }
         final String inputStr = inputNV.asString();
         String patternStr = patternNV.asString();
         Pattern pattern = Pattern.compile(patternStr, flags);
         final Matcher matcher = pattern.matcher(inputStr);
         final Graph newGraph = GraphFactory.createGraphMem();
 		final Node root = SWI.GraphRoot.asNode();
 		new Runnable() {
 			
 			Node last = null, lastMatching = null, lastNonMatching = null;
 			int lastMatchedPos = 0;
 			int nodeCount = 0;
 
 			private Node createMatching(MatchResult matchResult) {
 				Node node = Skolemizer.getInstance().getNode(env, args, nodeCount++);
 				newGraph.add(new Triple(node, RDF.type.asNode(), MATCH_CLASS));
 				if (last != null)
 					newGraph.add(new Triple(last, NEXT_PROPERTY, node));
 				else
 					newGraph.add(new Triple(root, FIRST_PROPERTY, node));
 				if (lastMatching != null)
 					newGraph.add(new Triple(lastMatching, NEXT_MATCH_PROPERTY, node));
 				else
 					newGraph.add(new Triple(root, FIRST_MATCH_PROPERTY, node));
 				newGraph.add(
 						new Triple(
 								node,
 								STRING_PROPERTY,
 								Node.createLiteral(matchResult.group()) ));
 				last = node;
 				lastMatching = node;
 				Node lastGroup = null;
 				for (int i = 1; i <= matchResult.groupCount(); i++) {
 					Node group = Skolemizer.getInstance().getNode(env, args, nodeCount++);
 					if (lastGroup != null)
 						newGraph.add(new Triple(lastGroup, NEXT_GROUP_PROPERTY, group));
 					else
 						newGraph.add(new Triple(node, FIRST_GROUP_PROPERTY, group));
					String match = matchResult.group(i);
					if (match != null)
						newGraph.add(
								new Triple(
										group,
										STRING_PROPERTY,
										Node.createLiteral(match) ));
					else
						newGraph.add(
								new Triple(
										group,
										RDF.type.asNode(),
										GROUP_NON_MATCH_CLASS ));
					lastGroup = group;
 				}
 				return node;
 			}
 			
 			private Node createNonMatching(String token) {
 				Node node = Skolemizer.getInstance().getNode(env, args, nodeCount++);
 				newGraph.add(new Triple(node, RDF.type.asNode(), NON_MATCH_CLASS));
 				if (last != null)
 					newGraph.add(new Triple(last, NEXT_PROPERTY, node));
 				else
 					newGraph.add(new Triple(root, FIRST_PROPERTY, node));
 				if (lastNonMatching != null)
 					newGraph.add(new Triple(lastNonMatching, NEXT_NON_MATCH_PROPERTY, node));
 				else
 					newGraph.add(new Triple(root, FIRST_NON_MATCH_PROPERTY, node));
 				newGraph.add(
 						new Triple(
 								node,
 								STRING_PROPERTY,
 								Node.createLiteral(token) ));
 				last = node;
 				lastNonMatching = node;
 				return node;
 			}
 			
 			@Override
 			public void run() {
 				while (matcher.find()) {
 					if (matcher.start() > lastMatchedPos) {
 						createNonMatching( inputStr.substring(lastMatchedPos, matcher.start()) );
 					}
 					createMatching( matcher.toMatchResult() );
 					lastMatchedPos = matcher.end();
 				}
 				if (inputStr.length() > lastMatchedPos) {
 					createNonMatching( inputStr.substring(lastMatchedPos) );
 				}
 			}
 		}.run();
 		return newGraph;
 	}
 
 	@Override
 	public Graph exec(List<NodeValue> args) {
 		// The other exec() is doing the job!
 		return null;
 	}
 
 }
