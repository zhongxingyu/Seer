 package uk.ac.susx.mlcl.parser;
 
 import org.apache.commons.lang3.StringUtils;
 import uk.ac.susx.mlcl.featureextraction.InvalidEntryException;
 import uk.ac.susx.mlcl.featureextraction.Sentence;
 import uk.ac.susx.mlcl.featureextraction.annotations.Annotations;
 
 import java.util.*;
 
 /**
  * Copyright
  * User: mmb28
  * Date: 16/01/2013
  * Time: 10:09
  * To change this template use File | Settings | File Templates.
  */
 public class PreprocessedConllParser extends StanPoSMaltDepParser {
 
 	public PreprocessedConllParser(String[] args) {
 		super.init(args);
 	}
 
 	@Override
 	protected Map<Object, Object> loadPreparsedEntry(String entry) {
 		//assumes entry is a single sentence
 		Map<Object, Object> sentences = new HashMap<Object, Object>();
 		sentences.put(0, entry);
 		return sentences;
 	}
 
 	@Override
 	protected List<Sentence> annotate(Map<Object, Object> map) {
 		//the map already contains a single sentence
 		String sentString = map.values().toArray()[0].toString();
 		if (config().isUseLowercaseEntries())
 			sentString = sentString.toLowerCase();
 		String[] tokens = sentString.split("\\r?\\n");
 		List<Sentence> toReturn = new ArrayList<Sentence>();
 
 		String[] newTokens = new String[tokens.length];
 		for (int i = 0; i < tokens.length; i++) {
 			String token = tokens[i];
 			String[] components = token.split("\t");
 			newTokens[i] = components[1] + getPosDelim() + components[2] + getPosDelim() + components[3];
 
 		}
 		Sentence sentObject = super.annotateWithTokenInfo(StringUtils.join(newTokens, getTokenDelim()));
 		if (sentObject.size() < 1) {
 			throw new InvalidEntryException("empty sentence!");
 		}
 		annotateWithDependencies(tokens, sentObject);
 		applyFeatureFactory(getFeatureFactory(), sentObject);
 		toReturn.add(sentObject);
 
 		return toReturn;
 	}
 
 	private Sentence annotateWithDependencies(String[] tokens, Sentence annotatedSent) {
 		List<Edge> edges = new LinkedList<Edge>();
 		for (int i = 0; i < tokens.length; i++) {
 			String[] components = tokens[i].split("\t");
 			int dependentIndex = Integer.parseInt(components[4]) - 1, headIndex = i;
 			String relation = components[5];
 
 			edges.add(new Edge(headIndex, dependentIndex, relation));
 		}
 
 		//code below adapted from super.annotateWithDependencies()
 		for (Edge edge : edges) {
 			int headIndex = edge.head, dependentIndex = edge.dep;
 			if (dependentIndex < 0) continue;//ignore the root dependency
 			String relation = edge.label;
 			String dependant = annotatedSent.get(dependentIndex).getAnnotation(Annotations.TokenAnnotation.class).toString();
 			String head = annotatedSent.get(headIndex).getAnnotation(Annotations.TokenAnnotation.class).toString();
 
 			if (dependentIndex > 0) {
 				ArrayList<String> feats = (ArrayList<String>) annotatedSent.
 				get(headIndex).
 				getAnnotation(Annotations.DependencyHeadListAnnotation.class).
 				get(relation);
 
 				if (config().hDepList() != null) {
 					if (feats == null) {
 						feats = new ArrayList<String>();
 						feats.add(dependant);
 						annotatedSent.get(headIndex).
 						getAnnotation(Annotations.DependencyHeadListAnnotation.class).
 						put(relation, feats);
 					} else {
 						feats.add(dependant);
 					}
 				}
 				if (config().depList() != null) {
 					feats = (ArrayList<String>) annotatedSent.
 					get(dependentIndex).
 					getAnnotation(Annotations.DependencyListAnnotation.class).
 					get(relation);
 
 					if (feats == null) {
 						feats = new ArrayList<String>();
 						feats.add(head);
 						annotatedSent.get(dependentIndex).
 						getAnnotation(Annotations.DependencyListAnnotation.class).
 						put(relation, feats);
 					} else {
						feats.add(dependant);
 					}
 				}
 			}
 
 		}
 		return annotatedSent;
 	}
 
 	private class Edge {
 		public int head, dep;
 		String label;
 
 		private Edge(int head, int dep, String label) {
 			this.head = head;
 			this.dep = dep;
 			this.label = label;
 		}
 	}
 
 	@Override
 	protected String newLineDelim() {
 		return ".*^\\s*$";
 	}
 
 	@Override
 	protected Map<Object, Object> rawTextParse(CharSequence text) {
 		throw new IllegalStateException("Raw text parsing not implemented for this type");
 	}
 
 	@Override
 	public void init(String[] args) {
 	}
 
 	public static void main(String[] args) {
 		PreprocessedConllParser sp = new PreprocessedConllParser(args);
 		sp.init(args);
 		long start = System.currentTimeMillis();
 		sp.parse();
 		System.out.println("Time (ms) = " + (System.currentTimeMillis() - start));
 	}
 }
