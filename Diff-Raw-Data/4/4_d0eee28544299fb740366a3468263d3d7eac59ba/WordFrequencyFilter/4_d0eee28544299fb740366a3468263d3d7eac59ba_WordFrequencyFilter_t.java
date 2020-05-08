 package filters;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import weka.core.Attribute;
 import general.Email;
 
 public class WordFrequencyFilter extends Filter{
 
 	private static final long serialVersionUID = 1148119665295273L;
 
 	private String attPrefix;
 	private HashMap<String, Integer> indexMap;
 	
 	/**
 	 * Constructor
 	 * @param atts List of attributes, one for each important word
 	 * @param options Only one option representing the attribute name prefix, which is appended before the word
 	 */
 	public WordFrequencyFilter(ArrayList<Attribute> atts, String[] options) {
 		super(atts, options);
 		attPrefix = options[0];
 		Iterator<Attribute> itr = attributes.iterator();
 		indexMap = new HashMap<String, Integer>();
 		int index=0;
 
 		while(itr.hasNext()) indexMap.put(getWord(itr.next().name()), index++);
 	}
 
 	/**
 	 * extracts the word string from the attribute name, because the name of the
 	 * attribute is on the form "prefix_word" --> "wff_word"
 	 * @param attName Attribute name
 	 */
 	private String getWord(String attName){
 		//TODO : change this function according to the convention in which will name the wordFrequency attributes
 		return attName.substring(attPrefix.length());
 	}
 	
 	/***
 	 * counts the frequency of important words and stores the count in the vals array
 	 * @param vals Frequency array to update
 	 * @param indexMap Map between the word and its index in the vals array 
 	 * @param email Email to count frequency
 	 */
 	private void calcFrequencies(double[] vals, HashMap<String, Integer> indexMap, Email email){
 		String[] toks;
 		//TODO : change the splitRegex according the words we will agree to consider
 		String splitRegex = "[^a-zA-Z]+"; //split on non-characters (one or more)
 		//calc wf from subject
 		toks = email.getFrom().trim().split(splitRegex);
 		for(int i=0; i<toks.length; i++)
 			if(indexMap.containsKey(toks[i])) 
 				vals[indexMap.get(toks[i])]++;
 		
 		//calc wf from content
 		toks = email.getContent().trim().split(splitRegex);
 		for(int i=0; i<toks.length; i++)
 			if(indexMap.containsKey(toks[i]))
 				vals[indexMap.get(toks[i])]++;
 	}
 	
 	@Override
 	public double[] getAttValue(Email email){
 		double[] vals = new double[attributes.size()];
 		calcFrequencies(vals, indexMap, email);
		int sz = email.getSize();
		if(sz>0)
			for(int i=0; i<vals.length; i++) vals[i]/= sz;
 		return vals;
 	}
 }
