 package com.zylman.protein;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class FeatureVector {
 	String sequence;
 	List<Double> hydrophobicityArr = new ArrayList<Double>();
 	List<Double> hydrophocilityArr = new ArrayList<Double>();
 	List<Double> volumeArr = new ArrayList<Double>();
 	List<Double> polarityArr = new ArrayList<Double>();
 	List<Double> polarizabilityArr = new ArrayList<Double>();
 	List<Double> sasaArr = new ArrayList<Double>();
 	List<Double> nciArr = new ArrayList<Double>();
 
 	FeatureVector(String sequence) {
 		// Store the sequence.
 		this.sequence = sequence;
 		
 		calculateCrossCovariance(sequence, hydrophobicity, hydrophobicityArr);
 
 		calculateCrossCovariance(sequence, hydrophocility, hydrophocilityArr);
 		
 		calculateCrossCovariance(sequence, volume, volumeArr);
 		
 		calculateCrossCovariance(sequence, polarity, polarityArr);
 		
 		calculateCrossCovariance(sequence, polarizability, polarizabilityArr);
 		
 		calculateCrossCovariance(sequence, SASA, sasaArr);
 		
 		calculateCrossCovariance(sequence, NCI, nciArr);
 	}
 	
 	private void calculateCrossCovariance(String sequence, Map<Character, Double> map, List<Double> list) {
		int lg = 30; // Can change this -- optimize!
 		int sequenceLength = sequence.length();
		
		
		// Calculate the feature vector values
 		
 		// Go through each of the lag values
 		for(int lag = 1; lag <= lg; lag++) {
 			double outsideVal = 1/(sequenceLength - lag);
 			double sum = 0;
 			
 			// Average value
 			double average = 0;
 			for(int i = 0; i < sequenceLength; i++) {
 				average += map.get(sequence.charAt(i));
 			}
 			average /= sequenceLength;
 			
 			// Go through each of amino acids
 			for(int i = 0; i < sequenceLength - lag; i++) {
 				
 				// Left term inside the sum
 				double leftTerm = map.get(sequence.charAt(i)) - average;
 				
 				// Right term inside the sum
 				double rightTerm = map.get(sequence.charAt(i + lag)) - average;
 				
 				// Add to sum
 				sum += leftTerm * rightTerm;
 				
 			}
 			
 			// Add the calculated value to the feature vector.
 			list.add(outsideVal * sum);
 		}
 	}
 	
 	String getSequence() {
 		return sequence;
 	}
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> hydrophobicity = new HashMap<Character, Double>(){{
 		put('A',0.62);
 		put('C',0.29);
 		put('D',-0.9);
 		put('E',-0.74);
 		put('F',1.19);
 		put('G',0.48);
 		put('H',-0.4);
 		put('I',1.38);
 		put('K',-1.5);
 		put('L',1.06);
 		put('M',0.64);
 		put('N',-0.78);
 		put('P',0.12);
 		put('Q',-0.85);
 		put('R',-2.53);
 		put('S',-0.18);
 		put('T',-0.05);
 		put('V',1.08);
 		put('W',0.81);
 		put('Y',0.26);		
 	}};
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> hydrophocility = new HashMap<Character, Double>(){{
 		put('A',-0.5);
 		put('C',-1.0);
 		put('D',3.0);
 		put('E',3.0);
 		put('F',-2.5);
 		put('G',0.0);
 		put('H',-0.5);
 		put('I',-1.8);
 		put('K',3.0);
 		put('L',-1.8);
 		put('M',-1.3);
 		put('N',2.0);
 		put('P',0.0);
 		put('Q',0.2);
 		put('R',3.0);
 		put('S',0.3);
 		put('T',-0.4);
 		put('V',-1.5);
 		put('W',-3.4);
 		put('Y',-2.3);
 	}};
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> volume = new HashMap<Character, Double>(){{
 		put('A',27.5);
 		put('C',44.6);
 		put('D',40.0);
 		put('E',62.0);
 		put('F',115.5);
 		put('G',0.0);
 		put('H',79.0);
 		put('I',93.5);
 		put('K',100.0);
 		put('L',93.5);
 		put('M',94.1);
 		put('N',58.7);
 		put('P',41.9);
 		put('Q',80.7);
 		put('R',105.0);
 		put('S',29.3);
 		put('T',51.3);
 		put('V',71.5);
 		put('W',145.5);
 		put('Y',117.3);
 	}};
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> polarity = new HashMap<Character, Double>(){{
 		put('A',8.1);
 		put('C',5.5);
 		put('D',13.0);
 		put('E',12.3);
 		put('F',5.2);
 		put('G',9.0);
 		put('H',10.4);
 		put('I',5.2);
 		put('K',11.3);
 		put('L',4.9);
 		put('M',5.7);
 		put('N',11.6);
 		put('P',8.0);
 		put('Q',10.5);
 		put('R',10.5);
 		put('S',9.2);
 		put('T',8.6);
 		put('V',5.9);
 		put('W',5.4);
 		put('Y',6.2);
 	}};
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> polarizability = new HashMap<Character, Double>(){{
 		put('A',0.046);
 		put('C',0.128);
 		put('D',0.105);
 		put('E',0.151);
 		put('F',0.29);
 		put('G',0.0);
 		put('H',0.23);
 		put('I',0.186);
 		put('K',0.219);
 		put('L',0.186);
 		put('M',0.221);
 		put('N',0.134);
 		put('P',0.131);
 		put('Q',0.18);
 		put('R',0.291);
 		put('S',0.062);
 		put('T',0.108);
 		put('V',0.14);
 		put('W',0.409);
 		put('Y',0.298);
 	}};
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> SASA = new HashMap<Character, Double>(){{
 		put('A',1.181);
 		put('C',1.461);
 		put('D',1.587);
 		put('E',1.862);
 		put('F',2.228);
 		put('G',0.881);
 		put('H',2.025);
 		put('I',1.81);
 		put('K',2.258);
 		put('L',1.931);
 		put('M',2.034);
 		put('N',1.655);
 		put('P',1.468);
 		put('Q',1.932);
 		put('R',2.56);
 		put('S',1.298);
 		put('T',1.525);
 		put('V',1.645);
 		put('W',2.663);
 		put('Y',2.368);
 	}};
 	
 	@SuppressWarnings("serial")
 	Map<Character, Double> NCI = new HashMap<Character, Double>(){{
 		put('A',0.007187);
 		put('C',-0.03661);
 		put('D',-0.02382);
 		put('E',0.006802);
 		put('F',0.037552);
 		put('G',0.179052);
 		put('H',-0.01069);
 		put('I',0.021631);
 		put('K',0.017708);
 		put('L',0.051672);
 		put('M',0.002683);
 		put('N',0.005392);
 		put('P',0.239531);
 		put('Q',0.049211);
 		put('R',0.043587);
 		put('S',0.004627);
 		put('T',0.003352);
 		put('V',0.057004);
 		put('W',0.037977);
 		put('Y',0.023599);
 	}};
 }
