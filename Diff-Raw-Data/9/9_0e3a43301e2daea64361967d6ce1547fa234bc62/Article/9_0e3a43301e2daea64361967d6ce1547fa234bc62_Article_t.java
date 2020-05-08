 package application;
 import java.text.BreakIterator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
 import edu.stanford.nlp.trees.*;
 
 /**
  * This Class define article object that stores
  * article source and  list of relations extracted from each sentence
  */
 
 public class Article {
 
 	private String url;
 	private String body;
 	private ArrayList<Relation> relations;
 	
 	public Article(String url, String body) {
 		this.url = url;
 		this.body = body;
 		relations = new ArrayList<Relation>();
 	}
 
 	/**
 	 * @return source url of the article
 	 */
 	public String getURL() {
 		return url;
 	}
 	
 	/**
 	 * @return list of relations extracted from the sentence
 	 */
 	public ArrayList<Relation> getRelations() {
 		return relations;
 	}
 	
 	/**
 	 * wall through each sentence in the article, get full parse tree.
 	 * then iterate through every sequential pair of noun phrases e1 and e2
 	 * label their relationship and populate the relations list
 	 */
 	public ArrayList<Relation> extractRelations(LexicalizedParser lp) {
 		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
 		iterator.setText(body);
 		int start = iterator.first();
 		for (int end = iterator.next();
 				end != BreakIterator.DONE;
 				start = end, end = iterator.next()) {
 			/* list of NP in the sentence */
 			ArrayList<Tree> NPList;
 			/* two NP will be used to extract relations */
 			Tree e1, e2;
 			int e1Index, e2Index;
 			
 			// parse sentence
 			String sentence = "Jaguar, the luxury auto maker sold 1,214 cars in the U.S.A. when Tom sat on the chair";
 //			String sentence = body.substring(start,end);
 			String nerSentence = Processor.ner.runNER(sentence);
 			String taggedSentence = Processor.tagger.tagSentence(sentence);
 			Tree parse = lp.apply(sentence);
 			
 			// generateNPList
 			NPList = generateNPList(parse);
 
 			// walk through NP list, select all e1 & e2 paris and construct relations
 			if (NPList.size() < 2) 
 				continue;
 			for (e1Index = 0; e1Index < NPList.size() - 1; ++e1Index) {
 				for (e2Index = e1Index + 1; e2Index < NPList.size(); ++e2Index) {
 					Tree NP1 = NPList.get(e1Index);
 					Tree NP2 = NPList.get(e2Index);
 					relations.add(new Relation(url, NP1, NP2, sentence, taggedSentence, nerSentence, 
 							parse, (e2Index - e1Index), true));
 				}
 			}
 		}
 
 		return relations;
 	}
 	
 	/**
 	 * Walk through parsed tree and populate list of NP
 	 * @param parse full parsed tree
 	 * @return list of noun phrases in the parsed tree
 	 */
 	private ArrayList<Tree> generateNPList(Tree parse) {
 		ArrayList<Tree> NPList = new ArrayList<Tree>();
 		Iterator<Tree> itr = parse.iterator();
 		while(itr.hasNext()) {
 			Tree currNode = itr.next();
 			if (currNode.label().value().equals("NP"))
 				NPList.add(currNode);
 		}
 		return NPList;
 	}
 	
 	/* debug use, try out tree structure */
 	private void printTree(Tree parse) {
 		parse.pennPrint();
 	}
 	
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("url: ");
 		sb.append(url);
 		sb.append("\n");
 		sb.append("body: ");
 		sb.append(body);
 		sb.append("\n");
 		return sb.toString();
 	}
 }
