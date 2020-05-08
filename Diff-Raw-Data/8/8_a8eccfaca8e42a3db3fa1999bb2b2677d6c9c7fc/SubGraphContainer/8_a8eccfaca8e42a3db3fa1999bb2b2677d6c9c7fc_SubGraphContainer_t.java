 package luposdate.operators;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import lupos.datastructures.bindings.Bindings;
 import lupos.datastructures.items.Item;
 import lupos.datastructures.queryresult.QueryResult;
 import lupos.endpoint.client.formatreader.MIMEFormatReader;
 import lupos.endpoint.client.formatreader.XMLFormatReader;
import lupos.engine.operators.OperatorIDTuple;
 import lupos.engine.operators.index.BasicIndex;
 import lupos.engine.operators.index.Dataset;
 import lupos.engine.operators.index.IndexCollection;
 import lupos.engine.operators.index.Indices;
 import lupos.engine.operators.tripleoperator.TriplePattern;
 import lupos.rdf.Prefix;
 import luposdate.operators.formatter.SubGraphContainerFormatter;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import p2p.P2PAdapter;
 
 /**
  * enthaelt die Operatoren, die alle an den Empfaengerknoten verschickt werden
  * sollen.
  * 
  */
 public class SubGraphContainer extends BasicIndex {
 
 	/** The root node of sub graph. */
 	IndexCollection						rootNodeOfSubGraph;
 
 	private Collection<TriplePattern>	hashableTriplePatterns;
 
 	private final P2PAdapter			p2pAdapter;
 
 	/**
 	 * Instantiates a new sub graph container.
 	 * 
 	 * @param rootNodeOfOuterGraph
 	 *            the root node of outer graph
 	 * @param rootNodeOfSubGraph
 	 *            the root node of sub graph
 	 */
 	public SubGraphContainer(P2PAdapter p2pAdapter,
 			IndexCollection rootNodeOfOuterGraph,
 			IndexCollection rootNodeOfSubGraph) {
 		super(rootNodeOfOuterGraph);
 
 		this.p2pAdapter = p2pAdapter;
 		this.rootNodeOfSubGraph = rootNodeOfSubGraph;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * lupos.engine.operators.index.BasicIndex#join(lupos.engine.operators.index
 	 * .Indices, lupos.datastructures.bindings.Bindings)
 	 */
 	@Override
 	public QueryResult join(Indices indices, Bindings bindings) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * wird aufgerufen, wenn der OP ausgefuehrt werden soll hier wird verschickt
 	 * und auf das Ergebnis gewartet und zurueckgegeben
 	 * 
 	 * 
 	 * @param queryResult
 	 *            the query result
 	 * @param operandID
 	 *            the operand id
 	 * @return the query result
 	 */
 
 	@Override
 	public QueryResult process(int opt, Dataset dataset) {
 		SubGraphContainerFormatter serialzer = new SubGraphContainerFormatter();
 
 		JSONObject serializedGraph;
 		try {
 			// Schritt 1: Serialisierung
 			serializedGraph = serialzer.serialize(rootNodeOfSubGraph, 0);
 			String key = generateKey();
 
 			// Versand des Teilbaumes und Empfang des Ergebnisses
 			String result = p2pAdapter.sendMessage(key,
 					serializedGraph.toString());
 
 			// Schritt 4: Deserialisierung
 			MIMEFormatReader deserializier = new XMLFormatReader();
 
 			InputStream is;
 			try {
 				is = new ByteArrayInputStream(result.getBytes("UTF-8"));
 				QueryResult queryResult = deserializier.getQueryResult(is);
 				System.out.println("schritt 4: " + queryResult);

				for (OperatorIDTuple succ : this.getSucceedingOperators()) {
					succ.processAll(queryResult);
				}
				// return queryResult;
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 
 			return null;
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		return null;
 	}
 
 	private String generateKey() {
 		StringBuilder key = new StringBuilder();
 		ArrayList<TriplePattern> hashbla = new ArrayList<TriplePattern>(
 				hashableTriplePatterns);
 		for (Item item : hashbla.get(0).getItems()) {
 			if (!item.isVariable()) {
 				key.append(item.getName());
 			}
 		}
 		return key.toString();
 	}
 
 	@Override
 	public String toString() {
 		return "SubGraphContainer";
 	}
 
 	@Override
 	public String toString(Prefix prefixInstance) {
 		return toString();
 	}
 
 	public void setHashableTriplePatterns(
 			Collection<TriplePattern> triplePattern) {
 		this.hashableTriplePatterns = triplePattern;
 	}
 
 }
