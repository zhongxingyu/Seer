 package skittles.g4FatKid;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedMap;
 
 class EatStrategy{
 	private int[] aintInHand;
 	private int intColorNum;
 /*	//double dblHappiness;
 	//String strClassName;
 	//int intPlayerIndex;*/
 	private double approxDist;
 	private double[] adblTastes;
 	private int intLastEatIndex;
 	private int intLastEatNum;
 	//private List<Integer> whatILikeMost = new ArrayList<Integer>();
 	private int likeMostInitCount = 0;
 	SortedMap<Integer, Double> whatILikeMostScore;
 	PreferredColors prefs;
 	
 	public EatStrategy(int[] aintInHand, int intColorNum, PreferredColors prefs) {	
 		this.aintInHand = aintInHand;
 		this.intColorNum = intColorNum;
 		//this.whatILikeMostScore = whatILikeMostScore;
 		intLastEatIndex = 0;
 		approxDist = 1/aintInHand.length;
 		likeMostInitCount = aintInHand[0];
 		this.prefs = prefs;
 		
 	}
 
 	public void updatePrefs(PreferredColors prefs){
 		this.prefs = prefs;
 	}
 	
 	public String eatNow(){
 		String whatToEatNow = "";
 		int i = 1;
 		int max = 0;
 		//this.intLastEatIndex = intLastEatIndex;
 		/*
 		if(intLastEatIndex == 0)
 			for(int j=0;j<aintInHand.length;j++){
 				if(aintInHand[j] > max)
 					max = j;
 				whatToEatNow = (max)+" "+1+"";
 				intLastEatIndex = max;
 			}
 		if (intLastEatIndex+1 < aintInHand.length){
 			whatToEatNow = (intLastEatIndex+1) + " " + 1 + "";
 			intLastEatIndex += 1;
 		}
 		*/
 		List<Integer> ateAlready = new ArrayList<Integer>();
 		int j;
 		if(i != aintInHand.length){
 			for(j=0;j<aintInHand.length;j++)
 				if(aintInHand[j]>max && !ateAlready.contains(j)){
 					max = j;
 					ateAlready.add(j);
 				}
 			intLastEatIndex = j;
 			whatToEatNow = j+" "+1;
 			i++;
 		}
 		else{
 			intLastEatIndex = prefs.getLowestRankedColor();
			//if(prefs.)
 			whatToEatNow = intLastEatIndex+" "+1;
 		}
 		
 		return whatToEatNow;
 	}
 }
 
 
