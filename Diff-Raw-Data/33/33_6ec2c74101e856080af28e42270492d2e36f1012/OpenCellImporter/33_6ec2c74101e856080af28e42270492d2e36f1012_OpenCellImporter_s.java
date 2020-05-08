 package edu.umich.eecs.data;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Iterator;
 
 import edu.umich.eecs.dto.Cell;
 import edu.umich.eecs.dto.CellKey;
 import edu.umich.eecs.dto.OpenCell;
 import edu.umich.eecs.dto.OpenCell;
 import edu.umich.eecs.logger.LogClass;
 import edu.umich.eecs.service.CellService;
 import edu.umich.eecs.service.OpenCellService;
 /**
  * This Class imports OpenCellID and OpenCell Map into
  * our local database.
  *  Some points:
  *  1- In openCellID, there are many "N" when a specific in a row 
  *  is unknown. We simply ignore all of them.
  
  *  2- We only import those records with a known mcc in nokia challange 
  *  data set
  *  
  *  3- For open Cell Map, this dataset only contains those countries we need
 	
 	File Format For Open Cell ID:
 	id,lat,lon,mcc,mnc,lac,cellid
 	id differs from cell id and is only an artifcial id for records in openCellid dataset
 	
 	File Format For Open Cell Map:
 	 mcc INT, mnc INT, lac INT, cid INT, lat FLOAT, lon FLOAT
 	
 	
  * 	
  * @author Mehrdad
  *
  */
 public class OpenCellImporter {
 	
 	private FileReader fileReader;
 	private BufferedReader bufferReader;
 	private ArrayList<OpenCell> openCellList;
 	int  persistCounter;
 	
 	OpenCellImporter( FileReader fileReader) {
 		super();
 		this.fileReader = fileReader;
 		bufferReader = new BufferedReader(fileReader);
 		openCellList=new ArrayList<OpenCell>();
 		persistCounter=0;
 	}
 	
 	/*
 	 *
 	 */
 	//OpenCell( double lat, double lon, int mcc, int mnc, int lac,int cellid) 
 	
 	
 	/**
 	 * In this method, we read all rows of opencellid from a dat file.
 	 * and import those rows that we are working with their country codes.
 	 * @param srv
 	 * @param cellSrv 
 	 * @throws IOException
 	 */
 	public void insertOpenCellID(OpenCellService srv, Set<Cell> keysInDb) throws IOException  {
 		String sCurrentLine;
 		int numberOfRows=0;
 		while ((sCurrentLine = bufferReader.readLine()) != null) {
 			
 			String[] splits = sCurrentLine.toString().split(",");
 			
 			try {
 				double lat= getDouble(splits[1]);
 				double lon=getDouble(splits[2]);
 				int mcc=getInteger(splits[3]);
 				int mnc=getInteger(splits[4]);
 				int lac=getInteger(splits[5]);
 				int cellid=getInteger(splits[6]);
 				
 				if(Constants.totalCountryNokia.contains(mcc)==true){
 					numberOfRows++;
 					persistIntoDatabase(new OpenCell(lat,lon,mcc,mnc,lac,cellid) , srv, keysInDb);
 				}
 			} catch (Exception e) {
 			}
 			
 			
 		
 			
 		}
 		System.out.println("Persisting "+ ((persistCounter++)*1000+openCellList.size()));
 		srv.saveListToOpenCell(openCellList);
 		openCellList.clear();
 		
 		System.out.println("Total Rows Inserted from Open CellID .."+numberOfRows);
 	}
 	/**
 	 * In this method, we read all rows of opencell map from a text file.
 	 * and import those rows that we are working with their country codes.
 	 * @param srv
 	 * @param cellSrv 
 	 * @throws IOException
 	 */
 	// mcc INT, mnc INT, lac INT, cid INT, lat FLOAT, lon FLOAT
 	public void insertOpenCellMap(OpenCellService srv, Set<Cell> keysInDb) throws IOException  {
 		String sCurrentLine;
 		int numberOfRows=0;
 		while ((sCurrentLine = bufferReader.readLine()) != null) {
 			
 			String[] splits = sCurrentLine.toString().split(",");
 			
 			try {
 				int mcc=getInteger(splits[0]);
 				int mnc=getInteger(splits[1]);
 				int lac=getInteger(splits[2]);
 				int cellid=getInteger(splits[3]);
 				double lat = getDouble(splits[4]);
 				double lon=getDouble(splits[5]);
 				if(Constants.totalCountryNokia.contains(mcc)==true){
 					numberOfRows++;
 					persistIntoDatabase(new OpenCell(lat,lon,mcc,mnc,lac,cellid) , srv, keysInDb );
 				}
 			} catch (Exception e) {
 			}
 			
 			
 		}
 		System.out.println("Persisting "+ ((persistCounter++)*1000+openCellList.size()));
 		srv.saveListToOpenCell(openCellList);
 		openCellList.clear();
 		
 		System.out.println("Total Rows Inserted from Open CellMap .."+numberOfRows);
 	}
 	
 	
 	/**
 	 * Here, we have used a buffer mechanism for persisting open cell dataset
 	 * @param oCell
 	 * @param l
 	 */
 	public void persistIntoDatabase(OpenCell oCell, OpenCellService l, Set<Cell> keysInDb){
 		
 		openCellList.add(oCell);
 
 		if(openCellList.size()==1000){
 			//
 			// Remove cells that are not in the database to begin with.
 			//
 			Iterator<OpenCell> iterator = openCellList.iterator();
 			while(iterator.hasNext()) {
 				OpenCell openCell = iterator.next();
 				if(!keysInDb.contains(new Cell(new CellKey(
 						openCell.getMcc(),
 						openCell.getCellid(),
 						openCell.getLac(),
 						openCell.getMnc())))) {
 					iterator.remove();
 				}
 			}
 			
 			System.out.println("Persisting " + openCellList.size());
 			l.saveListToOpenCell(openCellList);
 			openCellList.clear();
 			
 		}
 		
 	}
 	/**
 	 * we convert string to double and when we see "N" in our records
 	 * we simply ignore this record by throwing a throwble object
 	 * @param number
 	 * @return
 	 * @throws Exception
 	 */
 	public double getDouble(String number) throws Exception{
 		if(number.contains("N")) throw new Exception();
 		return Double.parseDouble(new String(number).replace("\n", ""));
 	}
 	
 	/**
 	 * the same as getDouble
 	 * @param number
 	 * @return
 	 * @throws Exception
 	 */
 	public int getInteger(String number) throws Exception{
 		if(number.contains("N")) throw new Exception();
 		return Integer.parseInt(new String(number).replace("\n", ""));
 	}
 	
 	public static void main(String[] args)  {
 		
 		OpenCellImporter li;
 		Set<Cell> keysInDb = new HashSet<>(new CellService().getAllCells());
 		try {
 			
			//li = new OpenCellImporter( new FileReader("/Users/Mehrdad/Documents/workspace/589-Project/CellID"));
			li = new OpenCellImporter( new FileReader("C:\\Users\\Pedro\\Desktop\\589 project data\\opencell\\CellID"));
 			li.insertOpenCellID(new OpenCellService(), keysInDb);
 			
			//li = new OpenCellImporter( new FileReader("/Users/Mehrdad/Documents/workspace/589-Project/CellMap.txt"));
			li = new OpenCellImporter( new FileReader("C:\\Users\\Pedro\\Desktop\\589 project data\\opencell\\CellMap.txt"));
 			li.insertOpenCellMap(new OpenCellService(), keysInDb);
 			
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 	}
 	
 	
 
 }
