 package vectorSpace;
 
 import java.util.HashMap;
 
 public class DocumentVector {
 	
 	private double[] vector;
 	private String ID;
 	private String[] words;
 	private double querySim;
 	private DocumentCollection docListener;
 	
 	public DocumentVector(String ID, double[] v){
 		setVector(v);
 		setID(ID);
 	}
 	
 	
 	public DocumentVector(String ID, String[] documentTerms){
 		words = documentTerms;
 		setID(ID);
 	}
 	
 	public DocumentVector(String ID, String[] documentTerms, HashMap<String, Integer> terms, HashMap<String, Double> idf){
 		words = documentTerms;
 		generateVector(terms, idf);
 		setID(ID);
 	}
 	
 	
 	public void generateVector(HashMap<String, Integer> terms, HashMap<String, Double> idf){
 		
 		
 	}
 	
 	
 	public void setQuerySim(double querySim){
		
 	}
 	
 	
 	public double getQuerySim(){
 		return querySim;
 	}
 	
 	private void setVector(double[] vector){
 		this.vector = vector;
 	}
 	
 	public double[] getVector(){
 		return vector;
 	}
 	
 	private void setID(String id){
 		ID = id;
 	}
 	
 	public String getID(){
 		return ID;
 	}
 	
 	public void addListener(DocumentCollection documentCollection){
 		this.docListener = documentCollection;
 	}
 
 }
