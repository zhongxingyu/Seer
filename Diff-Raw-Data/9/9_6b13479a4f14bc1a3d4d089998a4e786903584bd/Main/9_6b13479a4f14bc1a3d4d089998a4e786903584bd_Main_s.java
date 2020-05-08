 package fr.inria.lognet.convertexcel;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import net.sf.json.JSONObject;
 
 import org.apache.poi.hssf.extractor.ExcelExtractor;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.xssf.usermodel.XSSFCell;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 /**
  * Class to convert Excel file into json
  * @author rfritz
  *
  */
 public class Main{
 
 	public String fileName;
 	public XSSFWorkbook wb;
 	public ExcelExtractor extractor;
 	public static final String regex = "µ";
 
 	public Main(String fileName) throws IOException{
 		this.fileName = fileName;
 		InputStream inp = new FileInputStream(fileName);
 		this.wb = new XSSFWorkbook(inp);
 	}
 	
 	/**
 	 * Read Excel xlsx file and put it in csv format
 	 * @return csv arrayList
 	 */
 	public ArrayList<String> readExcelFile(){
 		int firstVisibleTab=wb.getFirstVisibleTab();
 		XSSFSheet sheet = wb.getSheetAt(firstVisibleTab);
 		int rowNumber = sheet.getLastRowNum();
 		int cellNumber = 0;
 		XSSFRow row;
 		XSSFCell cell;
 		ArrayList<String> csvSheet = new ArrayList<String>();
 		String buff;
 		for(int i=0;i<=rowNumber;i++){
 			row = sheet.getRow(i);
 			cellNumber = row.getLastCellNum();
 			buff="";
 			for(int j=0;j<cellNumber;j++){
 				cell= row.getCell(j);
 				if(cell.getCellType()==Cell.CELL_TYPE_STRING){
 					//System.out.println(cell.getStringCellValue());
 					buff+=cell.getStringCellValue().trim()+regex;
 				}
 				else if(cell.getCellType()==Cell.CELL_TYPE_NUMERIC){
 					//System.out.println(cell.getNumericCellValue());
 					buff+=(int)cell.getNumericCellValue()+regex;
 				}
 				else if(cell.getCellType()==Cell.CELL_TYPE_BLANK){
 					//System.out.println("blank");
 					buff+=" "+regex;
 				}
 				else{
 					buff+=" "+regex;
 				}
 			}
 			csvSheet.add(buff);
 		}
 		return csvSheet;
 	}
 	
 	/**
 	 * Print csv arrayList
 	 * @param csvSheet
 	 */
 	public void printCsv(ArrayList<String> csvSheet){
 		for(int i=0;i<csvSheet.size();i++){
 			System.out.println(csvSheet.get(i));
 			System.out.println("");
 		}
 	}
 	
 	/**
 	 * Print cell type
 	 * @param type
 	 */
 	public void printCellType(int type){
 		switch(type){
 		case Cell.CELL_TYPE_NUMERIC:
 			System.out.println("TYPE NUMERIC");
 			break;
 		case Cell.CELL_TYPE_STRING:
 			System.out.println("TYPE STRING");
 			break;
 		case Cell.CELL_TYPE_BLANK:
 			System.out.println("TYPE BLANK");
 			break;
 		case Cell.CELL_TYPE_BOOLEAN:
 			System.out.println("TYPE BOOLEAN");
 			break;
 		case Cell.CELL_TYPE_FORMULA:
 			System.out.println("TYPE FORMULA");
 			break;
 		case Cell.CELL_TYPE_ERROR:
 			System.out.println("TYPE ERROR");
 			break;
 			default:
 				System.out.println("PROBLEM NO TYPE");
 		}
 	}
 	
 	
 	public JSONObject[] csvToJson(ArrayList<String> csvSheet){
 		JSONObject[] tabJson = new JSONObject[csvSheet.size()-1];
 		String[] header = csvSheet.get(0).split(regex);
 		int headerLength = header.length;
 		String[] data;
 		JSONObject json = new JSONObject();
 		for(int i=1;i<csvSheet.size();i++){
			System.out.println("line="+i);
			json.clear();
 			data = csvSheet.get(i).split(regex);
			System.out.println("header length: "+headerLength);
			System.out.println("data length: "+ data.length);
 			for(int j=0;j<headerLength;j++){
 				//System.out.println(data[j]);
 				json.put(header[j],data[j]);
 			}
 			System.out.println(json.toString());
 			tabJson[i-1] = json;
 		}
 		return tabJson;
 	}
 	
 	public void printJsonToFile(JSONObject[] tabJson) throws FileNotFoundException{
 		PrintWriter pw = new PrintWriter(fileName+".json");
 		for(int i=0;i<tabJson.length;i++){
 			pw.println(tabJson[i].toString());
 		}
 		pw.close();
 	}
 
 
 	public static void main(String[] args) throws IOException{
 		System.out.println("converting...");
 		Main main = new Main(args[0]);
 		//main.printCsv(main.readExcelFile());
 		main.printJsonToFile(main.csvToJson(main.readExcelFile()));
 		System.out.println("converted");
 	}
 }
