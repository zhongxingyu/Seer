 package it.unito.geosummly;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.csv.CSVFormat;
 import org.apache.commons.csv.CSVPrinter;
 
 import com.google.gson.Gson;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.MongoClient;
 import com.mongodb.util.JSON;
 
 import fi.foyt.foursquare.api.FoursquareApiException;
 
 
 public class Main {
     public static Logger logger = Logger.getLogger(Main.class.toString());
     
 	public static void main(String[] args) throws FoursquareApiException, UnknownHostException{
 		
 		/***************************************************************************************/
 		/******************************CREATE THE BOUNDING BOX**********************************/
 		/***************************************************************************************/
 		double north=45.057; //north coordinate of the bounding box
 		double south=45.0390186;
 		double west=7.6600;
 		double east=7.6854548;
 		int cells_number=20; //Number N of cells
 		
 		BoundingBox bbox=new BoundingBox(north, south, west, east); //Initialize the bounding box
 		ArrayList<BoundingBox> data=new ArrayList<BoundingBox>(); //Data structure
 		
 		//Create a N*N grid based on the bounding box
 		Grid grid=new Grid();
 		grid.setCellsNumber(cells_number);
 		grid.setBbox(bbox);
 		grid.setStructure(data);
 		grid.createCells();
 		
 		/***************************************************************************************/
 		/****************************COLLECT ALL THE GEOPOINTS AND******************************/
 		/****************************CREATE THE TRANSFORMATION MATRIX***************************/
 		/***************************************************************************************/
 		//Initialize a MongoDB instance
     	MongoClient mongoClient=new MongoClient("localhost");
     	DB db=mongoClient.getDB("VenueDB");
     	DBCollection coll=db.getCollection("ResultVenues");
     	
     	//Initialize a Gson instance and declare the document which will contain the JSON results for MongoDB 
     	Gson gson=new Gson();
 		BasicDBObject doc;
 		
 		//Initialize the transformation matrix and its parameters
 		ArrayList<ArrayList<Double>> matrix=new ArrayList<ArrayList<Double>>();
 		HashMap<String, Integer> map=new HashMap<String, Integer>(); //HashMap of all the distinct categories
 		ArrayList<String> header=new ArrayList<String>(); //Sorted list of the distinct category (related to the hash map)
		TransformationMatrix tm=new TransformationMatrix();
		tm.setMatrix(matrix);
		tm.setMap(map);
		tm.setHeader(header);
 		ArrayList<Double> row_of_matrix; //row of the transformation matrix (one for each cell);
 		
 		//Support variables for transformation matrix task
 		int cat_num=0; //total number of categories of a single cell
 		int tot_num=0; //overall number of categories
 		double norm_lat=0; //normalized value in [0,1] of latitude 
 		double norm_lng=0;
 		ArrayList<String> distinct_list; //list of all the distinct categories for a single cell
 		ArrayList<Integer> occurrences_list; //list of the occurrences of the distinct categories for a single cell
 		
 		//Download venues informations
 		FoursquareSearchVenues fsv=new FoursquareSearchVenues();
 		ArrayList<FoursquareDataObject> venueInfo;
 		for(BoundingBox b: data){
 		    logger.log(Level.INFO, "Fetching 4square metadata of the cell: " + b.toString());
 
 			//Venues of a single cell
 			venueInfo=fsv.searchVenues(b.getRow(), b.getColumn(), b.getNorth(), b.getSouth(), b.getWest(), b.getEast());
 			
 			for(FoursquareDataObject fdo: venueInfo){
 				//Serialize with Gson
 				String obj=gson.toJson(fdo);
 				//Initialize the document which will contain the JSON result parsed for MongoDB and insert this document into MongoDB collection
 				doc= (BasicDBObject) JSON.parse(obj);
 				coll.insert(doc);
 			}
 			
 			//Transformation matrix task
 			cat_num=fsv.getCategoriesNumber(venueInfo);//set the total number of categories of the cell
 			distinct_list=fsv.createCategoryList(venueInfo); 
 			occurrences_list=fsv.getCategoryOccurences(venueInfo, distinct_list);
			tm.updateMap(distinct_list);//update the hash map
			norm_lat=tm.normalizeCoordinate(-90, 90, b.getCenterLat()); //get the normalized value of latitude coordinate
			norm_lng=tm.normalizeCoordinate(-180, 180, b.getCenterLng());
			row_of_matrix=tm.fillRow(occurrences_list, distinct_list, cat_num, norm_lat, norm_lng, b.getArea()); //create a consistent row (related to the categories)
			if(tot_num < row_of_matrix.size())
				tot_num=row_of_matrix.size(); //update the overall number of categories
			tm.addRow(row_of_matrix);
 		}
		tm.fixRowsLength(tot_num); //update rows length for consistency
 		
 		
 		// write down the transformation matrix to a file		
 		ByteArrayOutputStream bout = new ByteArrayOutputStream();
 		OutputStreamWriter osw = new OutputStreamWriter(bout);
         try {
             CSVPrinter csv = new CSVPrinter(osw, CSVFormat.DEFAULT);
 		
             // write the header of the matrix
            ArrayList<String> hdr=tm.getHeader();
             for(String s: hdr) {
             	csv.print(s);
             }
             csv.println();
             // iterate per each row of the matrix
            ArrayList<ArrayList<Double>> m=tm.getMatrix();
            for(ArrayList<Double> a: m) {
             	for(Double d: a) {
             		csv.print(d);
             	}
             	csv.println();
             }
             csv.flush();
         } catch (IOException e1) {
             e1.printStackTrace();
         }
 		
 		OutputStream outputStream;
         try {
             outputStream = new FileOutputStream ("output/matrix.csv");
             bout.writeTo(outputStream);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 		
 		//Print JSON files (just for debug)
     	/*DBCursor cursorDocJSON = coll.find();
     	while (cursorDocJSON.hasNext()) {
     		System.out.println(cursorDocJSON.next());
     	}*/
 	}
 }
