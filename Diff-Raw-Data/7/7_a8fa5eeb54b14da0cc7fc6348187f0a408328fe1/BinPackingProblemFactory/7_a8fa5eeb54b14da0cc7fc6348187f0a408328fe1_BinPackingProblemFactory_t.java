 package edu.upenn.cis.cis350.algorithmvisualization;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.res.XmlResourceParser;
 
 /**
  * Generates the Bins and BinObjects for each Bin Packing problem difficulty, 
  * as specified in res/xml/problems.xml. Also calculates optimal solutions.
  */
 public class BinPackingProblemFactory {
 	
 	private Collection<BinObject> easyObjects, mediumObjects, hardObjects;
 	private Collection<Bin> easyBins, mediumBins, hardBins;
 	private double easyOptSol, mediumOptSol, hardOptSol;
 	
 	/**
 	 * @param difficulty The difficulty of the problem as specified in problems.xml.
 	 * @return a Collection of all BinObjects for the specified difficulty, or null if the
 	 * difficulty is unspecified in problems.xml.
 	 */
 	public Collection<BinObject> getBinObjects(String difficulty) {
 		if ("easy".equalsIgnoreCase(difficulty)) return easyObjects;
 		else if ("medium".equalsIgnoreCase(difficulty)) return mediumObjects;
 		else if ("hard".equalsIgnoreCase(difficulty)) return hardObjects;
 		return null;
 	}
 	
 	/**
 	 * @param difficulty The difficulty of the problem as specified in problems.xml.
 	 * @return a Collection of all Bins for the specified difficulty, or null if the
 	 * difficulty is unspecified in problems.xml.
 	 */
 	public Collection<Bin> getBins(String difficulty) {
 		if ("easy".equalsIgnoreCase(difficulty)) return easyBins;
 		else if ("medium".equalsIgnoreCase(difficulty)) return mediumBins;
 		else if ("hard".equalsIgnoreCase(difficulty)) return hardBins;
 		return null;
 	}
 	
 	/**
 	 * @param difficulty The difficulty of the problem as specified in problems.xml.
 	 * @return the value of the optimal solution for the specified difficulty, or -1 if the
 	 * difficulty is unspecified in problems.xml.
 	 */
 	public double getOptimalSolution(String difficulty) {
 		if ("easy".equalsIgnoreCase(difficulty)) return easyOptSol;
 		else if ("medium".equalsIgnoreCase(difficulty)) return mediumOptSol;
 		else if ("hard".equalsIgnoreCase(difficulty)) return hardOptSol;
 		return -1;
 	}
 	
 	/**
 	 * For each difficulty: reads in the app config specified in res/xml/problems.xml,
 	 * generates the corresponding Bin and BinObject collections, and calculates
 	 * the value of the optimal solution.
 	 */
 	public BinPackingProblemFactory(Context context) {
 		//Initialize instance variables
 		easyBins = new ArrayList<Bin>();
 		mediumBins = new ArrayList<Bin>();
 		hardBins = new ArrayList<Bin>();
 		easyObjects = new ArrayList<BinObject>();
 		mediumObjects = new ArrayList<BinObject>();
 		hardObjects = new ArrayList<BinObject>();
 		easyOptSol = -1;
 		mediumOptSol = -1;
 		hardOptSol = -1;
 		
 		//Parse problems.xml
 		XmlResourceParser parser = context.getResources().getXml(R.id.bin_packing);
 		try {
 			int event = parser.next();
			while (!(event == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item"))) {
 				String difficulty = "";
 				if (event == XmlPullParser.START_TAG) {
 					String element = parser.getName();
 					if ("problem".equals(element)) { //Parse problem difficulty
 						difficulty = parser.getAttributeValue("", "difficulty");
 						if (!"easy".equalsIgnoreCase(difficulty) && !"medium".equalsIgnoreCase(difficulty)
 								&& !"hard".equalsIgnoreCase(difficulty)) 
 							throw new XmlPullParserException("Invalidly formatted file");
 					} else if ("bin".equals(element)) { //Parse Bin element
 						double capacity = new Double(parser.getAttributeValue("","capacity"));
 						Bin bin = new Bin(capacity);
 						if ("easy".equalsIgnoreCase(difficulty)) easyBins.add(bin);
 						else if ("medium".equalsIgnoreCase(difficulty)) mediumBins.add(bin);
 						else if ("hard".equalsIgnoreCase(difficulty)) hardBins.add(bin);
 						else throw new XmlPullParserException("Invalidly formatted file");
 					} else if ("object".equals(element)) { //Parse BinObject element
 						double weight = new Double(parser.getAttributeValue("","weight"));
 						double value = new Double(parser.getAttributeValue("","value"));
 						String type = parser.getAttributeValue("","type");
 						BinObject obj = new BinObject(weight, value, type);
 						if ("easy".equalsIgnoreCase(difficulty)) easyObjects.add(obj);
 						else if ("medium".equalsIgnoreCase(difficulty)) mediumObjects.add(obj);
 						else if ("hard".equalsIgnoreCase(difficulty)) hardObjects.add(obj);
 						else throw new XmlPullParserException("Invalidly formatted file");
					}
 				}
 				event = parser.next();
 			}
 		} catch (Exception e) { //Invalidly formatted XML file
 			System.err.println("Problem parsing res/values/problems.xml - please check file format");
 			System.exit(-1);
 		} finally {
 			parser.close();
 		}
 		
 		//TODO Calculate optimal solutions
 	}
 	
 }
