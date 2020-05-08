 package workers;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import play.Logger;
 
 import models.TripMetaData;
 
 public class StatisticsGenerator {
 
     private static final int minimumBlockSize = 100;
 
     private static int nrOfBlocks;
     private static int blockSize;
     private static double[] ratio;
     private static double[] overhead;
     private static long[] distribution;
 
     public static void updateStatistics() {
 
 	List<TripMetaData> elements = TripMetaData.find.where().ne("calculated_duration", 0).findList();
 	int totalElements = elements.size();
 	calculateBlocksize(totalElements);
 
 	Collections.sort(elements, new TripMetaDataCrowFlyDistanceComparator());
 
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
 
     private static void calculateBlocksize(int totalElements) {
 	nrOfBlocks = (int) Math.floor(totalElements / minimumBlockSize);
 	blockSize = (int) Math.ceil(((double) totalElements) / nrOfBlocks);
 	ratio = new double[nrOfBlocks];
 	overhead = new double[nrOfBlocks];
 	distribution = new long[nrOfBlocks];
     }
 
     public static double getCrowFlyDistanceOverhead(double crowFlyDistance) {
	if (overhead.length > 0) {
 	    return overhead[findBlockNumberByDistance(crowFlyDistance)];
 	} else {
 	    return 1.3;
 	}
     }
 
     public static double getDistanceToTravelTimeRatio(double crowFlyDistance) {
	if (ratio.length > 0) {
 	    return ratio[findBlockNumberByDistance(crowFlyDistance)];
 	} else {
 	    return 20;
 	}
     }
 
     private static int findBlockNumberByDistance(double crowFlyDistance) {
 	int i = 0;
 	while (i < distribution.length - 1 && distribution[i] < crowFlyDistance) {
 	    i++;
 	}
 	return i;
     }
 
     private static class TripMetaDataCrowFlyDistanceComparator implements Comparator<TripMetaData> {
 	@Override
 	public int compare(TripMetaData tmd1, TripMetaData tmd2) {
 	    return Double.compare(tmd1.getCrowFlyDistance(), tmd2.getCrowFlyDistance());
 	}
     }
 }
