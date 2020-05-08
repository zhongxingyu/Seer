 package src;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class kmeans {
 	static String filename;
 	static int k;
 
 	// 1 = random
 	// 2 = SelectCentroids
	static int initCentMethod = 2;
 
 	static int centRecalcMethod;
 
 	// 1 = # of reassigned pointeres
 	// 2 = total distance change of all centroids
 	// 3 = decrease of SSE
 	static int stopCriteria = 2;
 
 	static int reassignMinimum = 4;
 	static double clustoidDistMinimum = 1.0;
 	static double SSEDecreaseMinimum = 5.0;
 
 	public static void main(String[] args) {
 		// System.exit(0);
 		// java kmeans <Filename> <k>
 		if (args.length == 2) {
 			filename = args[0];
 			k = Integer.parseInt(args[1]);
 		} else {
 			System.err.println("Use: java kmeans <Filename> <k>");
 			System.exit(1);
 		}
 		// read in the file and extract the needed information
 		Csv csvInfo = new Csv(filename);
 		ArrayList<ArrayList<Double>> dataPoints = new ArrayList<ArrayList<Double>>(
 				csvInfo.datas);
 		ArrayList<String> stringLists = csvInfo.strings;
 		ArrayList<Integer> restrictions = csvInfo.restrictions;
 
 		// remove the restricted columns
 		for (int i = restrictions.size() - 1; i >= 0; i--) {
 			// for each dataPoints
 			for (int j = 0; j < dataPoints.size(); j++) {
 				if (restrictions.get(i) != 1) {
 					dataPoints.get(j).remove(i);
 					csvInfo.datas.get(j).remove(i);
 				}
 			}
 			if (restrictions.get(i) != 1) {
 				restrictions.remove(i);
 				csvInfo.restrictions.remove(i);
 			}
 		}
 
 		System.out.println("Restrictions:");
 		System.out.println(restrictions);
 		System.out.println("strings:");
 		for (int i = 0; i < stringLists.size(); i++) {
 			System.out.println(stringLists.get(i));
 		}
 		System.out.println("Datas:(" + dataPoints.size() + ")");
 		for (int i = 0; i < dataPoints.size(); i++) {
 			System.out.println(dataPoints.get(i));
 		}
 
 		ArrayList<Centroid> cent;
 		// select initial centroid
 		if (initCentMethod == 1) {
 			cent = SIC_Random(dataPoints, k);
 		} else {
 			cent = SIC_SelectCentroids(dataPoints, k);
 		}
 
 		// this becomes false when the main loop should be over
 		boolean keepOnGoing = true;
 		// keeps track of SSE to see if main loops should be over
 		double SSEVal = -1;
 
 		// while we should keep on going
 		while (keepOnGoing) {
 			// if it's not the first time around
 			if (SSEVal != -1) {
 				// calculate the SSE
 				SSEVal = calcSSE(cent);
 				System.out.println("SSEVal=" + SSEVal);
 			}
 
 			// reset the centroids: clear all datapoints, and changed to false
 			for (int i = 0; i < cent.size(); i++) {
 				cent.get(i).reset();
 			}
 
 			// for each data points
 			for (int idx = 0; idx < dataPoints.size(); idx++) {
 				// find the nearest centroid
 				int indexOfCent = nearestCentroid(dataPoints.get(idx), cent);
 				// put the data point into the centroid
 				cent.get(indexOfCent).add(dataPoints.get(idx));
 			}
 
 			// recalcuate the centroid positions for each centroids
 			for (int i = 0; i < cent.size(); i++) {
 				cent.get(i).calcCenter_Mean();
 				System.out.println(i + ":" + cent.get(i));
 			}
 			// check if we should stop:-------------------------
 
 			// reassignement of data pointers
 			int reassign = 0;
 			for (int i = 0; i < cent.size(); i++) {
 				reassign += Math.abs(cent.get(i).getLR() - cent.get(i).getRE());
 			}
 			System.out.println("reassign=" + reassign);
 			if ((stopCriteria == 1) && reassign >= -1
 					&& reassign <= reassignMinimum) {
 				keepOnGoing = false;
 			}
 			// change in clustoids position
 			double distanceChanged = 0.0;
 			for (int i = 0; i < cent.size(); i++) {
 				distanceChanged += cent.get(i).distChange();
 			}
 			System.out.println("distanceChanged=" + distanceChanged);
 			if ((stopCriteria == 2) && distanceChanged <= clustoidDistMinimum) {
 				keepOnGoing = false;
 			}
 			// insignificant decrease in SSE
 			double decreseSSE = SSEVal - calcSSE(cent);
 			SSEVal = calcSSE(cent);
 			System.out.println("decreseSSE=" + decreseSSE);
 			if ((stopCriteria == 3) && decreseSSE <= SSEDecreaseMinimum) {
 				keepOnGoing = false;
 			}
 		}
 
 		System.out.println("===============================");
 		// print out and evaluate the centroids
 		for (int i = 0; i < cent.size(); i++) {
 			System.out.println("Cluster " + i + ": ");
 			System.out.println("Center: " + cent.get(i).getPos());
 			// calc min and max distance from the centroid
 			double maxDistValue = pointDistance(cent.get(i).getDP().get(0),
 					cent.get(i).getPos());
 			double minDistValue = pointDistance(cent.get(i).getDP().get(0),
 					cent.get(i).getPos());
 			double averageDistValue = 0;
 			int maxDistIndex = 0;
 			int minDistIndex = 0;
 			for (int tempDistIndex = 0; 
 					tempDistIndex < cent.get(i).getDP().size(); 
 					tempDistIndex++) {
 				double tempDistValue = pointDistance(
 						cent.get(i).getDP().get(tempDistIndex), cent.get(i)
 								.getPos());
 				if (maxDistValue < tempDistValue) {
 					maxDistValue = tempDistValue;
 					maxDistIndex = tempDistIndex;
 				}
 				if (minDistValue > tempDistValue) {
 					minDistValue = tempDistValue;
 					minDistIndex = tempDistIndex;
 				}
 				averageDistValue += tempDistValue;
 			}
 			averageDistValue /= cent.get(i).getDP().size();
 			System.out.println("Max Dist. to Center: " + maxDistValue);
 			System.out.println("Min Dist. to Center: " + minDistValue);
 			System.out.println("Avg Dist. to Center: " + averageDistValue);
 
 			System.out.println(cent.get(i).getDP().size() + " Points:");
 			for (int DPIndex = 0; DPIndex < cent.get(i).getDP().size(); DPIndex++) {
 				System.out.println(cent.get(i).getDP().get(DPIndex));
 			}
 			System.out.println("");
 		}
 
 		// if it's 2d data, let's plot it.
 		if (restrictions.size() == 2) {
 			System.out.println("2D data detected, plotting...");
 			ArrayList<ArrayList<ArrayList<Double>>> ALLL = new ArrayList<ArrayList<ArrayList<Double>>>();
 			for (int i = 0; i < cent.size(); i++) {
 				ALLL.add(cent.get(i).getDP());
 			}
 			new ScatterPlot(ALLL);
 		}
 	}
 
 	// select initial centroid randomly
 	private static ArrayList<Centroid> SIC_Random(
 			ArrayList<ArrayList<Double>> dataPoints, int k) {
 		ArrayList<Centroid> answer = new ArrayList<Centroid>();
 		Random rand = new Random();
 		for (int i = 0; i < k; i++) {
 			int randNum = Math.abs(rand.nextInt());
 			int index = randNum % dataPoints.size();
 			Centroid tempCent = new Centroid();
 			tempCent.add(new ArrayList<Double>(dataPoints.get(index)));
			tempCent.setDP(new ArrayList<ArrayList<Double>>());
 			answer.add(new Centroid(tempCent));
 			answer.get(i).calcCenter_Mean();
 		}
 		System.out.println("dataPoints.size()=" + dataPoints.size());
 		return answer;
 	}
 
 	// select initial centroid using the select centroids algorithm
 	private static ArrayList<Centroid> SIC_SelectCentroids(
 			ArrayList<ArrayList<Double>> dataPoints, int k) {
 		ArrayList<Centroid> answer = new ArrayList<Centroid>();
 		
 		//calculate the center of the whole dataPoints
 		ArrayList<Double> center = centerOfPoints(dataPoints);
 		//System.out.println("center="+center);
 		
 		//find the furtest one, that's the first centroid
 		int furthestIndex = furthestPoint(center,dataPoints);
 		
 		//keep track of used ones
 		int [] usedCentroids = new int[k];
 		usedCentroids[0] = furthestIndex;
 		
 		//adding the first centroid
 		Centroid tempCent = new Centroid();
 		tempCent.add(dataPoints.get(furthestIndex));
 		tempCent.calcCenter_Mean();
 		tempCent.setDP(new ArrayList<ArrayList<Double>>());
 		//System.out.println("0: "+tempCent.getPos());		
 		answer.add(new Centroid(tempCent));
 		
 		//keep track of all the points that was used for centroid
 		
 		//for number of k-1
 		for(int centCount = 1; centCount < k; centCount++){
 			double maxDistValue = pointDistance(dataPoints.get(0), answer.get(0).getPos());
 			int maxDistIndex = 0;
 			//System.out.println("answer.size()= "+answer.size());
 			//for each datapoints
 			for(int currDP = 0; currDP < dataPoints.size(); currDP++){
 				//check if this value was already used by looking up the array of used dataPoint indexes
 				boolean alreadyUsed = false;
 				for(int i = 0; i < usedCentroids.length; i++){
 					if(currDP == usedCentroids[i]){
 						alreadyUsed = true;
 					}
 				}
 				if(alreadyUsed){
 					//System.out.println(dataPoints.get(currDP)+" is already used");
 					continue;
 				}
 				
 				double tempDistValue = 0;
 				//for each centroid already there
 				for(int currCent = 0; currCent < answer.size(); currCent++){
 					//add the distance from curr datapoint to curr centroid
 					double pointDist = pointDistance(dataPoints.get(currDP), answer.get(currCent).getPos());
 					//System.out.println("    pointDist (vs "+currCent+")= "+pointDist+"| DP"+dataPoints.get(currDP)+"| cent"+answer.get(currCent).getPos());
 					tempDistValue += pointDist;
 				}
 				//System.out.println("  tempDistValue= "+tempDistValue);
 				
 				if(tempDistValue > maxDistValue){
 					maxDistValue = tempDistValue;
 					maxDistIndex = currDP;
 				}
 			}
 			//System.out.println(centCount+":MaxVal "+maxDistValue);
 			
 			//set this index of datapoint as used to create centroid
 			usedCentroids[centCount] = maxDistIndex;
 				
 			//create the centroid and add it to the list
 			tempCent = new Centroid();
 			tempCent.add(dataPoints.get(maxDistIndex));
 			tempCent.calcCenter_Mean();
 			tempCent.setDP(new ArrayList<ArrayList<Double>>());
 			System.out.println(centCount+":Pos "+tempCent.getPos());
 			answer.add(new Centroid(tempCent));
 		}
 		  
 
 		return answer;
 	}
 
 	// given a data point and list of centroids, return the index of the nearest
 	// centroid
 	private static int nearestCentroid(ArrayList<Double> dataPoint,
 			ArrayList<Centroid> cent) {
 		if (cent.size() == 0) {
 			System.err.println("No list of centroids found!");
 		}
 		int index = 0;
 		double minDist = pointDistance(dataPoint, cent.get(0).getPos());
 		for (int centIdx = 1; centIdx < cent.size(); centIdx++) {
 			double currDist = pointDistance(dataPoint, cent.get(centIdx)
 					.getPos());
 			if (currDist < minDist) {
 				index = centIdx;
 				minDist = currDist;
 			}
 		}
 		return index;
 	}
 
 	public static int nearestPoint(ArrayList<Double> point,
 			ArrayList<ArrayList<Double>> dataPoints) {
 		if (dataPoints.size() == 0) {
 			System.err.println("No list of points found!");
 		}
 		int index = 0;
 		double minDist = pointDistance(point, dataPoints.get(0));
 		for (int dpIndex = 1; dpIndex < dataPoints.size(); dpIndex++) {
 			double currDist = pointDistance(point, dataPoints.get(dpIndex));
 			if (currDist < minDist) {
 				index = dpIndex;
 				minDist = currDist;
 			}
 		}
 		return index;
 	}
 
 	public static int furthestPoint(ArrayList<Double> point,
 			ArrayList<ArrayList<Double>> dataPoints) {
 		if (dataPoints.size() == 0) {
 			System.err.println("No list of points found!");
 		}
 		int index = 0;
 		double maxDist = pointDistance(point, dataPoints.get(0));
 		for (int dpIndex = 1; dpIndex < dataPoints.size(); dpIndex++) {
 			double currDist = pointDistance(point, dataPoints.get(dpIndex));
 			if (currDist > maxDist) {
 				index = dpIndex;
 				maxDist = currDist;
 			}
 		}
 		return index;
 	}
 
 	public static ArrayList<Double> centerOfPoints(
 			ArrayList<ArrayList<Double>> dataPoints) {
 		if (dataPoints.size() == 0) {
 			System.err
 					.println("Trying to calculate the center for empty list of points!");
 		}
 		ArrayList<Double> answer = new ArrayList<Double>();
 
 		// for each entry in a point
 		for (int pointIndex = 0; pointIndex < dataPoints.get(0).size(); pointIndex++) {
 			double tempSum = 0;
 			// for each dataPoints
 			for (int DPIndex = 0; DPIndex < dataPoints.size(); DPIndex++) {
 				tempSum += dataPoints.get(DPIndex).get(pointIndex);
 			}
 			tempSum /= (double) dataPoints.size();
 			answer.add(tempSum);
 		}
 		return answer;
 	}
 
 	public static double pointDistance(ArrayList<Double> pt1,
 			ArrayList<Double> pt2) {
 		if (pt1.size() != pt2.size()) {
 			System.err.println("Vector size does not match!");
 			System.err.println("pt1 size=" + pt1.size() + "|pt2 size="
 					+ pt2.size());
 			//return 0;
 		}
 
 		double result = 0.0;
 		for (int i = 0; i < pt1.size(); i++) {
 			result += Math.pow(pt1.get(i) - pt2.get(i), 2);
 		}
 		result = Math.sqrt(result);
 		return result;
 	}
 
 	private static double calcSSE(ArrayList<Centroid> cent) {
 		// TODO
 		return -1;
 	}
 }
