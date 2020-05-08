 package edu.nyu.cs.cs2580;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Vector;
 
 class Evaluator {
 
 	static String query;
 
 	public static void main(String[] args) throws IOException {
 		HashMap<String, HashMap<Integer, Double>> scored_judgments = new HashMap<String, HashMap<Integer, Double>>();
 
 		HashMap<String, HashMap<Integer, Double>> relevant_judgments = new HashMap<String, HashMap<Integer, Double>>();
 
 		// HashMap < String , HashMap < Integer , Double > > relevance_judgments
 		// =
 		// new HashMap < String , HashMap < Integer , Double > >();
 
 		if (args.length < 1) {
 			System.out.println("need to provide relevance_judgments");
 			return;
 		}
 		String p = args[0];
 		// first read the relevance judgments into the HashMap
 		readRelevanceJudgments(p, relevant_judgments, scored_judgments);
 		// now evaluate the results from stdin
 		Vector<Vector<String>> data = new Vector<Vector<String>>();
 		data = dataCollection();
 
 		DCG dcg = new DCG(scored_judgments, data, relevant_judgments);
 
 		String evaluatorOutput = "";
 
 		evaluatorOutput += query + "\t";
 
 		evaluatorOutput += evaluatePrecision(relevant_judgments, data, 1)
 				+ "\t" + evaluatePrecision(relevant_judgments, data, 5) + "\t"
 				+ evaluatePrecision(relevant_judgments, data, 10);
 
 		evaluatorOutput += "\t" + evaluateRecall(relevant_judgments, data, 1)
 				+ "\t" + evaluateRecall(relevant_judgments, data, 5) + "\t"
 				+ evaluateRecall(relevant_judgments, data, 10);
 
 		evaluatorOutput += "\t" + f1Score(relevant_judgments, data, 1) + "\t"
 				+ f1Score(relevant_judgments, data, 5) + "\t"
 				+ f1Score(relevant_judgments, data, 10);
 
 		Evaluator.computeRecallPrecision(relevant_judgments, data);
 
 		Vector<Double> recalVec = Evaluator.getRecallValues();
 		Vector<Double> precisionVec = Evaluator.getPrecisionValues();
 
 		double[] recallPoints = new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5,
 				0.6, 0.7, 0.8, 0.9, 1.0 };
 		for (int i = 0; i < recallPoints.length; i++) {
 			evaluatorOutput += "\t"
 					+ Utilities.getPrecisionAtRecall(recalVec, precisionVec,
 							recallPoints[i]);
 		}
 
 		evaluatorOutput += "\t" + averagePrecision(relevant_judgments, data);
 
 		evaluatorOutput += "\t" + dcg.computeNDCG(query, 1) + "\t"
 				+ dcg.computeNDCG(query, 5) + "\t" + dcg.computeNDCG(query, 10);
 		evaluatorOutput += "\t" + dcg.computeReciprocalRank(query);
 
 		System.out.println(evaluatorOutput);
 	}
 
 	public static void readRelevanceJudgments(String p,
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
 			HashMap<String, HashMap<Integer, Double>> scored_judgments) {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(p));
 			try {
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					// parse the query,did,relevance line
 					Scanner s = new Scanner(line).useDelimiter("\t");
 					String query = s.next();
 					int did = Integer.parseInt(s.next());
 					String grade = s.next();
 					double relScore = 1.0; // un-judged document
 					double rel = 0.0; // un-judged document
 					// convert to binary relevance
 					if (grade.equals("Perfect")) {
 						relScore = 5.0;
 						rel = 1.0;
 					} else if (grade.equals("Excellent")) {
 						relScore = 4.0;
 						rel = 1.0;
 					} else if (grade.equals("Good")) {
 						relScore = 3.0;
 						rel = 1.0;
 					} else if (grade.equals("Fair")) {
 						relScore = 2.0;
 						rel = 0.0;
 					} else if (grade.equals("Bad")) {
 						relScore = 1.0;
 						rel = 0.0;
 					}
 					if (relevance_judgments.containsKey(query) == false) {
 						HashMap<Integer, Double> qr = new HashMap<Integer, Double>();
 						relevance_judgments.put(query, qr);
 
 						HashMap<Integer, Double> qr_scored = new HashMap<Integer, Double>();
 						scored_judgments.put(query, qr_scored);
 					}
 
 					HashMap<Integer, Double> qr = relevance_judgments
 							.get(query);
 					qr.put(did, rel);
 
 					HashMap<Integer, Double> qr_scored = scored_judgments
 							.get(query);
 					qr_scored.put(did, relScore);
 				}
 			} finally {
 				reader.close();
 			}
 		} catch (IOException ioe) {
 			System.err.println("Oops " + ioe.getMessage());
 		}
 	}
 
 	public static void evaluateStdin(
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments) {
 		// only consider one query per call
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					System.in));
 
 			String line = null;
 			double RR = 0.0;
 			double N = 0.0;
 			while ((line = reader.readLine()) != null) {
 				Scanner s = new Scanner(line).useDelimiter("\t");
 				query = s.next();
 				int did = Integer.parseInt(s.next());
 				String title = s.next();
 				double rel = Double.parseDouble(s.next());
 				if (relevance_judgments.containsKey(query) == false) {
 					throw new IOException("query not found");
 				}
 				HashMap<Integer, Double> qr = relevance_judgments.get(query);
 				if (qr.containsKey(did) != false) {
 					RR += qr.get(did);
 				}
 				++N;
 			}
 			// System.out.println(Double.toString(RR/N));
 		} catch (Exception e) {
 			System.err.println("Error:" + e.getMessage());
 		}
 	}
 
 	/* Added By Ravi */
 
 	/**
 	 * Reads input data from Ranker's output and creates a collection of data.
 	 * 
 	 * @return
 	 */
 	public static Vector<Vector<String>> dataCollection() {
 		Vector<Vector<String>> data = new Vector<Vector<String>>();
 		Vector<String> row;
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					System.in));
 
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				row = new Vector<String>();
 				Scanner s = new Scanner(line).useDelimiter("\t");
 				query = s.next();
 				String did = s.next();
 				row.add(query);
 				row.add(did);
 				data.add(row);
 			}
 		} catch (Exception e) {
 			System.err.println("Error:" + e.getMessage());
 		}
 		return data;
 	}
 
 	/**
 	 * This function calculates Precision value at a given point depending on
 	 * the relevance.
 	 * 
 	 * @param relevance_judgments
 	 * @return
 	 */
 	public static double evaluatePrecision(
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
 			Vector<Vector<String>> data, int precisionPoint) {
 		double RR = 0.0;
 		double N = 0.0;
 		try {
 			int lineCount = 0;
 			for (int i = 0; i < data.size(); i++) {
 				lineCount++;
 				if (lineCount > precisionPoint)
 					break;
 				Vector<String> row = data.get(i);
 				String query = row.get(0);
 				int did = Integer.parseInt(row.get(1));
 				if (relevance_judgments.containsKey(query) == false) {
 					throw new IOException("query not found");
 				}
 				HashMap<Integer, Double> qr = relevance_judgments.get(query);
 				if (qr.containsKey(did) != false) {
 					RR += qr.get(did);
 				}
 				++N;
 			}
 		} catch (Exception e) {
 			System.err.println("Error:" + e.getMessage());
 		}
 
 		if (RR != 0.0 && N != 0.0)
 			return (RR / N);
 		else
 			return 0.0;
 	}
 
 	/**
 	 * This function calculates Recall value at a given point depending on the
 	 * relevance.
 	 * 
 	 * @param relevance_judgments
 	 * @param recallPoint
 	 * @return
 	 */
 	public static double evaluateRecall(
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
 			Vector<Vector<String>> data, int recallPoint) {
 		double RR = 0.0;
 		double N = 0.0;
 		String query = "";
 		try {
 			int lineCount = 0;
 			for (int i = 0; i < data.size(); i++) {
 				lineCount++;
 				if (lineCount > recallPoint)
 					break;
 				Vector<String> row = data.get(i);
 				query = row.get(0);
 				int did = Integer.parseInt(row.get(1));
 				if (relevance_judgments.containsKey(query) == false) {
 					throw new IOException("query not found");
 				}
 				HashMap<Integer, Double> qr = relevance_judgments.get(query);
 				if (qr.containsKey(did) != false) {
 					RR += qr.get(did);
 				}
 			}
 			if (relevance_judgments.containsKey(query) == false) {
 				throw new IOException("query not found");
 			}
 			HashMap<Integer, Double> qr = relevance_judgments.get(query);
 			Iterator<Entry<Integer, Double>> it = qr.entrySet().iterator();
 			while (it.hasNext()) {
 				Map.Entry<Integer, Double> pairs = (Map.Entry<Integer, Double>) it
 						.next();
 				if (pairs.getValue() != 0d) {
 					N += pairs.getValue();
 				}
 			}
 			// System.out.println("Recall at : "+recallPoint);
 			// System.out.println(Double.toString(RR/N));
 		} catch (Exception e) {
 			System.err.println("Error:" + e.getMessage());
 		}
 		if (RR != 0.0 && N != 0.0)
 			return (RR / N);
 		else
 			return 0.0;
 	}
 
 	private static Vector<Double> recallValues;
 	private static Vector<Double> precisionValues;
 
 	public static Vector<Double> getRecallValues() {
 		return recallValues;
 	}
 
 	public static Vector<Double> getPrecisionValues() {
 		return precisionValues;
 	}
 
 	public static void computeRecallPrecision(
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
 			Vector<Vector<String>> data) {
 
 		recallValues = new Vector<Double>();
 		precisionValues = new Vector<Double>();
 		int averagePoint = data.size();
 		for (int i = 1; i <= averagePoint; i++) {
 			recallValues.add(evaluateRecall(relevance_judgments, data, i));
 			precisionValues
 					.add(evaluatePrecision(relevance_judgments, data, i));
 		}
 	}
 
 	public static double averagePrecision(
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
 			Vector<Vector<String>> data) {
 
 		computeRecallPrecision(relevance_judgments, data);
 
 		double precisionSum = precisionValues.get(0);
 		double previous = recallValues.get(0);
 		double count = 0;
 		if (previous != 0d) {
 			count = 1;
 		}
 		for (int i = 1; i < recallValues.size(); i++) {
 			if (recallValues.get(i) != previous) {
 				previous = recallValues.get(i);
 				precisionSum += precisionValues.get(i);
 				count++;
 			}
 		}
		
		if(count != 0.0d) {
			return (precisionSum / count);
		} else {
			return 0;
		}
		
 	}
 
 	public static double f1Score(
 			HashMap<String, HashMap<Integer, Double>> relevance_judgments,
 			Vector<Vector<String>> data, int f1ScorePoint) {
 		double precision = evaluatePrecision(relevance_judgments, data,
 				f1ScorePoint);
 		double recall = evaluateRecall(relevance_judgments, data, f1ScorePoint);
 		double num = 2 * precision * recall;
 		double denom = precision + recall;
 
 		if (denom != 0.0)
 			return (num / denom);
 		else
 			return 0.0;
 	}
 
 }
