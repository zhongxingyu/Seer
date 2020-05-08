 package at.rovo.rdf;
 
 import java.util.Map;
 import java.util.Set;
 
 /**
  * <p>{@link InferenceRule} provides the basic mechanism to extract new
  * knowledge from already existing rules inside the triple store. It 
  * therefore provides two methods:</p>
  * <ul>
  * <li>{@link #getQueries()}</li>
  * <li>{@link #makeTriples(Map)}</li>
  * </ul>
  * <p>The first method defines the queries which are needed to extract new knowledge, 
  * while the latter one defines new rules which get added to the knowledge base</p>
  * 
  * <p>A typical example of inferencing is the membership of a person to a 
  * certain country and the continent. The knowledge base for example may contain 
  * a rule that Peter is German, Sandra is Austrian and Paul is US citizen. Based 
  * on the knowledge of the belonging from a person to a certain state some further
  * rules can be derived. As Germany and Austria are located in Europe and USA 
  * in Northern America, Peter and Sandra may be considered as European while
  * Paul is (North) American.</p>
  * 
  * <p>Imagine the triplestore contains the following knowledge:</p>
  * <code>[(Paul, nationality, USA), (USA, part_of, North America), 
  * (Peter, nationality, Germany), (Austria, part_of, Europe), 
  * (Germany, part_of, Europe), (Sandra, nationality, Austria)]</code>
  * <p>With an implementation of {@link #getQueries()} as follows all persons that
  * live in a country and the country is part of a continent should be considered for
  * the inferencing algorithm.</p>
  * <pre> {@code public Set<Set<String>> getQueries() 
  * {
 *    {@code Set<Set<String>> query = new LinkedHashSet<Set<String>>();
 *    {@code Set<String> query1 = new LinkedHashSet&lt;String>();}
  *    {@code query1.add("?person, nationality, ?country");}
  *    {@code query1.add("?country, part_of, ?continent");}
  *     
  *    {@code query.add(query1);}
  *    {@code return query;}
  * }</pre>
  * <p>{@link #makeTriples(Map)} defines how the new rules are added to the knowledge 
  * base.</p>
  * <pre> {@code public Set<Triple> makeTriples(Map<String, String> binding) 
  * {
  *    {@code Triple t = new Triple(binding.get("?person") ,"on_continent", binding.get("?continent"));}
  *    {@code Set<Triple> ret = new LinkedHashSet<Triple>();}
  *    {@code ret.add(t);}
  *    {@code return ret;}
  * }</pre>
  * <p>On adding the {@link InferenceRule} to via {@link Graph#applyInference(InferenceRule)} 
  * to the knowledge base the inferencing process starts. After the inferencing mechanism the 
  * knowledge base contains looks like this:</p>
  * <code>[(Paul, on_continent, North America), (Paul, nationality, USA), 
  * (USA, part_of, North America), (Peter, on_continent, Europe), (Peter, nationality, Germany), 
  * (Austria, part_of, Europe), (Germany, part_of, Europe), (Sandra, on_continent, Europe), 
  * (Sandra, nationality, Austria)]</code>
  * 
  * @author Roman Vottner
  */
 public interface InferenceRule
 {
 	/**
 	 * <p>This method contains rules that define which triples should be used
 	 * to extract new knowledge.</p>
 	 * 
 	 * @return Returns a Set of queries which define the triples that should be used to
 	 *         derive new knowledge from
 	 */
 	public Set<Set<String>> getQueries();
 	
 	/**
 	 * <p>Defines new rules that should be added to the knowledge base in case
 	 * some rules were found that provide new insights.</p>
 	 * 
 	 * @param binding Contains a mapping of variable-names and their value
 	 * @return Returns a {@link Set} which includes the template of new rules to add
 	 */
 	public Set<Triple> makeTriples(Map<String, String> binding);
 }
