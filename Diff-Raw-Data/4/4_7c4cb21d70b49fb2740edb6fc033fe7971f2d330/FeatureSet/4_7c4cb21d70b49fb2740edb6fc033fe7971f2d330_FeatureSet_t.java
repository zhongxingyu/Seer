 package iago.features;
 
 
 import iago.Board;
 import iago.players.Player;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class FeatureSet extends ArrayList<Feature>{
 
 	private static final long serialVersionUID = -5179576268344247586L;
 	
 	protected ArrayList<Feature> Features;
 	private String playerID;
 	
 	public FeatureSet (ArrayList<Feature> Features, String playerID) {
 		this.Features = Features;
 		this.playerID = playerID;
 	}
 	public FeatureSet (String playerID) {
 		this.Features = new ArrayList<Feature>();
 		this.playerID = playerID;
 	}
 	public FeatureSet () {
 		this.Features = new ArrayList<Feature>();
 	}
 	
 	public FeatureSet(FeatureSet other) {
 	    this.Features = new ArrayList<Feature>(other.Features); //Changed this because the array copy constructor wasn't deep
 	    this.Features = new ArrayList<Feature>();
 	    for(Feature f : other.Features){
 	    	try {
 				this.Features.add((Feature) f.clone());
 			} catch (CloneNotSupportedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 	    this.playerID = other.playerID;
 	}
 	
     public boolean add (Feature f) {
 		Features.add(f);
 		return true;
 	}
 	/**
 	 * Will make all weights be between -1 and 1. You may or may not want this depending on your use of the features.
 	 * The sum of the weights is 1 but the ratio between them is maintained
 	 */
 	public void standardiseWeights(){
 		double weightSum = 0;
 		for (Feature f : this){
 			weightSum += Math.abs(f.getWeight());
 		}
 		for (Feature f : this){
 			f.setWeight(f.getWeight() / weightSum);
 		}
 	}
 
 	public double score (Board state, Player.PlayerType player){
 		double boardscore = 0;
 		//Evaluate victory condition
 		//TODO: think about ties
		boolean gameOver = (state.validMoves(player).size() == 0) && (state.validMoves(player.getOpponent()).size() == 0);
		boolean weHaveMorePoints = state.scoreBoard(player) > 0;
 		
 		if (gameOver && weHaveMorePoints) return 1;
 		if (gameOver && !weHaveMorePoints) return 0;
 
 		//Loop through features and evaluate each one
 		for (Feature f: Features)
 			boardscore += (f.evaluate(state, player) / (double)f.bestScore) * f.getWeight();
 		return boardscore;
 	}
 	
 	//A game has just ended, evaluate and adjust features based on Win/Loss
 	public void update (FeatureSet opponent, Board finalstate) {
 		// TODO Learn how to learn
 	}
 	
 	/**
 	 * Save the FeatureSet to a serialised Feature array
 	 */
 	public void save() {
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(playerID + ".spl"));
 			out.writeObject(Features);
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Load the FeatureSet from a serialised Feature array.
 	 * If the serialised file is not found; attempt to load from text file
 	 */
 	@SuppressWarnings("unchecked")
 	public void load() {
 		try {
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(playerID + ".spl"));
 			Features = (ArrayList<Feature>) in.readObject();
 			in.close();
 		} catch (FileNotFoundException e) {
 			// Simple way to add new player (use .pl if .spl not found)
 			loadFromFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Save the FeatureSet to a text file
 	 */
 	public void saveToFile () {
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(playerID + ".pl"));
 			for (Feature f: Features)
 				bw.write(f.getClass().getSimpleName() + ":" + Double.toString(f.getWeight()) + "\n");
 			bw.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Load the FeatureSet from a text file
 	 */
 	public void loadFromFile () {
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(playerID + ".pl"));
 			String featureLn;
 			String fname;
 			double fweight;
 			while ((featureLn = br.readLine()) != null)   {
 				fname   = featureLn.split(":")[0];
 				fweight = Double.parseDouble(featureLn.split(":")[1]);
 				Constructor<?> fcons = Class.forName("iago.features."+fname).getConstructor(new Class[]{Double.TYPE});
 				Feature f = (Feature) fcons.newInstance(new Object[]{fweight});
 				Features.add(f);
 			}
 		} catch (FileNotFoundException e) {
 			// Player does not exist. Do nothing
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	/** Modifies this feature set so that the weights of this feature set are the sum of the original + the other
 	 * 
 	 * @param other		The other feature set that we're adding to this one
 	 */
 	public void combine(FeatureSet other){
 		for(Feature o : other){
 			for(Feature t : this){
 				if(o.name==t.name){
 					t.setWeight(t.getWeight() + o.getWeight());
 				}
 			}
 		}
 	}
 	@Override
 	public Feature get(int index){
 		return Features.get(index);
 	}
 	
 	@Override
 	public int size(){
 		return Features.size();
 	}
 	@Override
 	public Iterator<Feature> iterator() {
 		return Features.iterator();
 	}
 	@Override
 	public String toString(){
 		String output = "{";
 		for (Feature f : Features){
 			output += "(Name: "+f.name + ", Weight: "+f.getWeight()+"), ";
 		}
 		output += "}";
 		return output;
 	}
 	
 	public String getName(){
 		return playerID;
 	}
 }
