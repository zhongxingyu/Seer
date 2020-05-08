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
		this.setVector(new double[terms.size()]);
		for(int i = 0; i < getVector().length; i++){
			vector[i] = 0;
		}
 		
		for(int i = 0; i < words.length; i++){
			if(terms.containsKey(words[i])){
				vector[terms.get(words[i])]++;
			}
		}
		for(String key : terms.keySet()){
			vector[terms.get(key)] = (vector[terms.get(key)]/words.length)*Math.log(idf.get(key));
		}
 		
 	}
 	
 	
 	public void setQuerySim(double querySim){
		this.querySim = querySim;
		try{
			this.docListener.addRating(this.ID, querySim);
			
		} catch (Exception e) {
			System.out.println("No document listener available");
		}
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
