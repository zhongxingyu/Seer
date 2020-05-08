 package workers;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import play.Logger;
 
 import models.TripMetaData;
 
 public class StatisticsGenerator {
 
     private static int nrOfBlocks = 4;
     private static double[] ratio = new double[nrOfBlocks];
     private static double[] overhead = new double[nrOfBlocks];
     private static long[] distribution = new long[nrOfBlocks];
 
     public static void updateStatistics() {
 	List<TripMetaData> elements = TripMetaData.find.where().ne("calculated_duration", 0).findList();
 	int totalElements = elements.size();
 	int blockSize = (int) Math.ceil(((double) totalElements) / nrOfBlocks);
 	Collections.sort(elements, new TripMetaDataComparator());
 
 	double totalCrowFlyDistance;
 	double totalDirectionsDistance;
 	double totalTravelTime;
 
 	for (int i = 0; i < nrOfBlocks; i++) {
 	    totalCrowFlyDistance = 0;
 	    totalDirectionsDistance = 0;
 	    totalTravelTime = 0;
 	    for (int j = i * blockSize; j < Math.min(totalElements, (i + 1) * blockSize); j++) {
 		totalCrowFlyDistance += elements.get(j).getCrowFlyDistance();
 		totalDirectionsDistance += elements.get(j).getDirectionsDistance();
 		totalTravelTime += elements.get(j).getCalculatedDuration();
 	    }
 	    distribution[i] = elements.get(Math.min(totalElements - 1, (i + 1) * blockSize - 1)).getCrowFlyDistance();
 	    overhead[i] = totalDirectionsDistance / totalCrowFlyDistance;
 	    ratio[i] = totalDirectionsDistance / totalTravelTime;
 	    Logger.info(String.format("Block end: %d\tRatio: %03f\tOverhead: %03f", distribution[i], ratio[i], overhead[i]));
 	}
     }
 
     public static double getCrowFlyDistanceOverhead(double crowFlyDistance) {
 	return overhead[findBlockNumberByDistance(crowFlyDistance)];
     }
 
     public static double getDistanceToTravelTimeRatio(double crowFlyDistance) {
 	return ratio[findBlockNumberByDistance(crowFlyDistance)];
     }
 
     private static int findBlockNumberByDistance(double crowFlyDistance) {
 	int i = 0;
	while (i < distribution.length && distribution[i] < crowFlyDistance) {
 	    i++;
 	}
 	return i;
     }
 
     private static class TripMetaDataComparator implements Comparator<TripMetaData> {
 	@Override
 	public int compare(TripMetaData tmd1, TripMetaData tmd2) {
 	    return Double.compare(tmd1.getCrowFlyDistance(), tmd2.getCrowFlyDistance());
 	}
     }
 }
