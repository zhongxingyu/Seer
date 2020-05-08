 package it.unito.geosummly.discovery;
 
 import fi.foyt.foursquare.api.FoursquareApiException;
 import it.unito.geosummly.BoundingBox;
 import it.unito.geosummly.FoursquareDataObject;
 import it.unito.geosummly.FoursquareSearchVenues;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.apache.commons.csv.CSVFormat;
 import org.apache.commons.csv.CSVPrinter;
 
 public class AreaDiscovery {
 	
 	public static Logger logger = Logger.getLogger(AreaDiscovery.class.toString());
 	
 	public static void main(String[] args) throws UnknownHostException, FoursquareApiException {
 		ArrayList<String> features=new ArrayList<String>();
 		features.add("Arts & Entertainment");
 		features.add("College & University");
 		features.add("Event");
 		features.add("Food");
 		features.add("Nightlife Spot");
 		features.add("Outdoors & Recreation");
 		features.add("Professional & Other Places");
 		features.add("Residence");
 		features.add("Shop & Service");
 		features.add("Travel & Transport");
 		
 		/***************************************************************************************/
 		/*********************************VENUES OF AREA 1**************************************/
 		/***************************************************************************************/
 		
		double north= 44.484014614538964; //coordinates of Val Maira, Piedmont
		double south= 44.442604689982026;
		double west= 7.318267822265625;
		double east= 7.391395568847656;
 		
 		int cellsNumber=20; //it corresponds to cellsNumber of class grid
 		int sampleNumber=100; //number of samples of the sample area
 		ArrayList<BoundingBox> gridStructure=new ArrayList<BoundingBox>();
 		ArrayList<ArrayList<Double>> supportMatrix=new ArrayList<ArrayList<Double>>();
 		ArrayList<ArrayList<Double>> FreqStructure=new ArrayList<ArrayList<Double>>();
 		ArrayList<ArrayList<Double>> devStructure=new ArrayList<ArrayList<Double>>();
 		HashMap<String, Integer> map=new HashMap<String, Integer>(); //HashMap of all the distinct categories
 		SampleArea sA=new SampleArea(north, south, west, east);
 		sA.setGridStructure(gridStructure);
 		sA.setFreqStructure(FreqStructure);
 		sA.setDevStructure(devStructure);
 		sA.setCellsNumber(cellsNumber);
 		sA.setSampleNumber(sampleNumber);
 		sA.setMap(map);
 		sA.updateMap(features);
 		sA.setFeatures(features);
 		sA.createSampleGrid(); //fill the gridStructure with 'sampleNumber' samples
 		ArrayList<Double> row_of_matrix; //one row per box;
 		
 		ArrayList<String> distinct_list; //list of all the distinct categories for a single cell
 		ArrayList<Integer> occurrences_list; //list of the occurrences of the distinct categories for a single cell
 		
 		FoursquareSearchVenues fsv=new FoursquareSearchVenues();
 		ArrayList<FoursquareDataObject> venueInfo;
 		for(BoundingBox b: sA.getGridStructure()){
 			//Venues of a single cell
 			venueInfo=fsv.searchVenues(b.getNorth(), b.getSouth(), b.getWest(), b.getEast());
 			distinct_list=fsv.createCategoryList(venueInfo); 
 			occurrences_list=fsv.getCategoryOccurences(venueInfo, distinct_list);
 			row_of_matrix=sA.fillRecord(occurrences_list, distinct_list); //create a consistent row (related to the categories)
 			supportMatrix.add(row_of_matrix);
 		}
 		
 		sA.getIntrafeatureFrequency(supportMatrix); //get a structure with intra-feature frequencies
 		
 		/***************************************************************************************/
 		/*********************************VENUES OF AREA 2**************************************/
 		/***************************************************************************************/
 
		double north_1= 45.153353406479106; //coordinates of Val di Susa, Piedmont
		double south_1= 45.13416254594802;
		double west_1= 7.142057418823242;
		double east_1= 7.170724868774414;
 		
 		int cellsNumber_1=20; //it corresponds to cellsNumber of class grid
 		int sampleNumber_1=100; //number of samples of the sample area
 		ArrayList<BoundingBox> gridStructure_1=new ArrayList<BoundingBox>();
 		ArrayList<ArrayList<Double>> supportMatrix_1=new ArrayList<ArrayList<Double>>();
 		ArrayList<ArrayList<Double>> FreqStructure_1=new ArrayList<ArrayList<Double>>();
 		ArrayList<ArrayList<Double>> devStructure_1=new ArrayList<ArrayList<Double>>();
 		HashMap<String, Integer> map_1=new HashMap<String, Integer>(); //HashMap of all the distinct categories
 		SampleArea sA_1=new SampleArea(north_1, south_1, west_1, east_1);
 		sA_1.setGridStructure(gridStructure_1);
 		sA_1.setFreqStructure(FreqStructure_1);
 		sA_1.setDevStructure(devStructure_1);
 		sA_1.setCellsNumber(cellsNumber_1);
 		sA_1.setSampleNumber(sampleNumber_1);
 		sA_1.setMap(map_1);
 		sA_1.updateMap(features);
 		sA_1.setFeatures(features);
 		sA_1.createSampleGrid(); //fill the gridStructure with 'sampleNumber' samples
 		ArrayList<Double> row_of_matrix_1; //one row per box;
 		
 		ArrayList<String> distinct_list_1; //list of all the distinct categories for a single cell
 		ArrayList<Integer> occurrences_list_1; //list of the occurrences of the distinct categories for a single cell
 		
 		FoursquareSearchVenues fsv_1=new FoursquareSearchVenues();
 		ArrayList<FoursquareDataObject> venueInfo_1;
 		for(BoundingBox b: sA_1.getGridStructure()){
 			//Venues of a single cell
 			venueInfo_1=fsv_1.searchVenues(b.getNorth(), b.getSouth(), b.getWest(), b.getEast());
 			distinct_list_1=fsv_1.createCategoryList(venueInfo_1); 
 			occurrences_list_1=fsv_1.getCategoryOccurences(venueInfo_1, distinct_list_1);
 			row_of_matrix_1=sA_1.fillRecord(occurrences_list_1, distinct_list_1); //create a consistent row (related to the categories)
 			supportMatrix_1.add(row_of_matrix_1);
 		}
 		
 		sA_1.getIntrafeatureFrequency(supportMatrix_1); //get a structure with intra-feature frequencies
 		
 		/***************************************************************************************/
 		/*********************************VENUES OF AREA 3**************************************/
 		/***************************************************************************************/
 		
 		/*double north_2= 41.91875659707589; //coordinates of Rome
 		double south_2= 41.87409864599624;
 		double west_2= 12.451157569885254;
 		double east_2= 12.518448829650879;
 		
 		int cellsNumber_2=20; //it corresponds to cellsNumber of class grid
 		int sampleNumber_2=100; //number of samples of the sample area
 		ArrayList<BoundingBox> gridStructure_2=new ArrayList<BoundingBox>();
 		ArrayList<ArrayList<Double>> supportMatrix_2=new ArrayList<ArrayList<Double>>();
 		ArrayList<ArrayList<Double>> FreqStructure_2=new ArrayList<ArrayList<Double>>();
 		ArrayList<ArrayList<Double>> devStructure_2=new ArrayList<ArrayList<Double>>();
 		HashMap<String, Integer> map_2=new HashMap<String, Integer>(); //HashMap of all the distinct categories
 		SampleArea sA_2=new SampleArea(north_2, south_2, west_2, east_2);
 		sA_2.setGridStructure(gridStructure_2);
 		sA_2.setFreqStructure(FreqStructure_2);
 		sA_2.setDevStructure(devStructure_2);
 		sA_2.setCellsNumber(cellsNumber_2);
 		sA_2.setSampleNumber(sampleNumber_2);
 		sA_2.setMap(map_2);
 		sA_2.updateMap(features);
 		sA_2.setFeatures(features);
 		sA_2.createSampleGrid(); //fill the gridStructure with 'sampleNumber' samples
 		ArrayList<Double> row_of_matrix_2; //one row per box;
 		
 		ArrayList<String> distinct_list_2; //list of all the distinct categories for a single cell
 		ArrayList<Integer> occurrences_list_2; //list of the occurrences of the distinct categories for a single cell
 		
 		FoursquareSearchVenues fsv_2=new FoursquareSearchVenues();
 		ArrayList<FoursquareDataObject> venueInfo_2;
 		for(BoundingBox b: sA_2.getGridStructure()){
 			//Venues of a single cell
 			venueInfo_2=fsv_2.searchVenues(b.getNorth(), b.getSouth(), b.getWest(), b.getEast());
 			distinct_list_2=fsv_2.createCategoryList(venueInfo_2); 
 			occurrences_list_2=fsv_2.getCategoryOccurences(venueInfo_2, distinct_list_2);
 			row_of_matrix_2=sA_2.fillRecord(occurrences_list_2, distinct_list_2); //create a consistent row (related to the categories)
 			supportMatrix_2.add(row_of_matrix_2);
 		}
 		
 		sA_2.getIntrafeatureFrequency(supportMatrix_2); //get a structure with intra-feature frequencies*/
 		
 		/***************************************************************************************/
 		/******************************CREATE THE BIG MATRIX**********************************/
 		/***************************************************************************************/
 		
 		ArrayList<ArrayList<Double>> bigArea=new ArrayList<ArrayList<Double>>();
		ClassOfArea metropolitan=new ClassOfArea("Rural", bigArea, sA.getFreqStructure(), sA_1.getFreqStructure()/*, sA_2.getFreqStructure()*/);
 		ArrayList<Double> meanFrequencies=metropolitan.getMeanFrequencies(metropolitan.getBigArea());
 		ArrayList<ArrayList<Double>> stdMatrix=metropolitan.getStdMatrix(metropolitan.getBigArea());
 		ArrayList<Double> stdSingles=new ArrayList<Double>(stdMatrix.get(0));
 		ArrayList<Double> singleDensities=metropolitan.getSingleDensities(meanFrequencies, stdSingles);
 		ArrayList<Double> pairDensities=metropolitan.getPairDensities(metropolitan.getBigArea(), meanFrequencies);
 		
 		/***************************************************************************************/
 		/******************************WRITE OUTPUT TO CSV**********************************/
 		/***************************************************************************************/
 		
 		// write down the matrices of densities and standard deviation values to a file		
 		ByteArrayOutputStream bout_freq1 = new ByteArrayOutputStream();
 		OutputStreamWriter osw_freq1 = new OutputStreamWriter(bout_freq1);
 		ByteArrayOutputStream bout_freq2 = new ByteArrayOutputStream();
 		OutputStreamWriter osw_freq2 = new OutputStreamWriter(bout_freq2);
 		/*ByteArrayOutputStream bout_freq3 = new ByteArrayOutputStream();
 		OutputStreamWriter osw_freq3 = new OutputStreamWriter(bout_freq3);*/
 		ByteArrayOutputStream bout_std = new ByteArrayOutputStream();
 		OutputStreamWriter osw_std = new OutputStreamWriter(bout_std);
 		ByteArrayOutputStream bout_singles = new ByteArrayOutputStream();
 		OutputStreamWriter osw_singles = new OutputStreamWriter(bout_singles);
 		ByteArrayOutputStream bout_pairs = new ByteArrayOutputStream();
 		OutputStreamWriter osw_pairs = new OutputStreamWriter(bout_pairs);
         try {
             CSVPrinter csv_freq1 = new CSVPrinter(osw_freq1, CSVFormat.DEFAULT);
             CSVPrinter csv_freq2 = new CSVPrinter(osw_freq2, CSVFormat.DEFAULT);
             //CSVPrinter csv_freq3 = new CSVPrinter(osw_freq3, CSVFormat.DEFAULT);
             CSVPrinter csv_std = new CSVPrinter(osw_std, CSVFormat.DEFAULT);
             CSVPrinter csv_singles = new CSVPrinter(osw_singles, CSVFormat.DEFAULT);
             CSVPrinter csv_pairs = new CSVPrinter(osw_pairs, CSVFormat.DEFAULT);
 		
             // write the header of the matrix for frequency and standard deviation
             ArrayList<String> hdr=sA.getFeatures();
             for(String s: hdr) {
             	csv_freq1.print(s);
             	csv_freq2.print(s);
             	//csv_freq3.print(s);
             	csv_std.print(s);
             }
             csv_freq1.println();
             csv_freq2.println();
             //csv_freq3.println();
             csv_std.println();
             
             // iterate per each row of the matrix for frequency and standard deviation
             ArrayList<ArrayList<Double>> freq1=sA.getFreqStructure();
             ArrayList<ArrayList<Double>> freq2=sA_1.getFreqStructure();
             //ArrayList<ArrayList<Double>> freq3=sA_2.getFreqStructure();
             
             for(ArrayList<Double> a: freq1) {
             	for(Double d: a) {
             		csv_freq1.print(d);
             	}
             	csv_freq1.println();
             }
             csv_freq1.flush();
             
             for(ArrayList<Double> a: freq2) {
             	for(Double d: a) {
             		csv_freq2.print(d);
             	}
             	csv_freq2.println();
             }
             csv_freq2.flush();
             
             /*for(ArrayList<Double> a: freq3) {
             	for(Double d: a) {
             		csv_freq3.print(d);
             	}
             	csv_freq3.println();
             }
             csv_freq3.flush();*/
             
             for(ArrayList<Double> a: stdMatrix) {
             	for(Double d: a) {
             		csv_std.print(d);
             	}
             	csv_std.println();
             }
             csv_std.flush();
             
             //write down for density of singles
             for(int i=0;i<singleDensities.size();i++) {
             	csv_singles.print(features.get(i));
         		csv_singles.print(singleDensities.get(i));
             	csv_singles.println();
             }
             csv_singles.flush();
             
             //write down for density of pairs
             ArrayList<String> pairFeatures=metropolitan.getFeaturesForPairs(features);
 	        for(int i=0;i<pairDensities.size();i++) {
 	        	csv_pairs.print(pairFeatures.get(i));
 	    		csv_pairs.print(pairDensities.get(i));
 	        	csv_pairs.println();
 	        }
 	        csv_pairs.flush();
             
         } catch (IOException e1) {
             e1.printStackTrace();
         }
 		
 		OutputStream outputStream_freq1;
 		OutputStream outputStream_freq2;
 		//OutputStream outputStream_freq3;
 		OutputStream outputStream_std;
 		OutputStream outputStream_singles;
 		OutputStream outputStream_pairs;
         try {
            outputStream_freq1 = new FileOutputStream ("output/discovery/rural/freqRuralValMaira.csv");
            outputStream_freq2 = new FileOutputStream ("output/discovery/rural/freqRuralValDiSusa.csv");
             //outputStream_freq3 = new FileOutputStream ("output/discovery/metropolitan/freqMetroRome.csv");
            outputStream_std = new FileOutputStream ("output/discovery/rural/stdRuralArea.csv");
            outputStream_singles = new FileOutputStream ("output/discovery/rural/densRuralSingles.csv");
            outputStream_pairs = new FileOutputStream ("output/discovery/rural/densRuralCouples.csv");
             bout_freq1.writeTo(outputStream_freq1);
             bout_freq2.writeTo(outputStream_freq2);
             //bout_freq3.writeTo(outputStream_freq3);
             bout_std.writeTo(outputStream_std);
             bout_singles.writeTo(outputStream_singles);
             bout_pairs.writeTo(outputStream_pairs);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 	}
 }
