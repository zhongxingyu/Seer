 package org.simple.parser.excel;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.DateUtil;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.simple.parser.core.ErrorBean;
 import org.simple.parser.core.ErrorBean.ColErrors;
 import org.simple.parser.core.annotations.ColumnDef;
 import org.simple.parser.core.annotations.ParserDef;
 import org.simple.parser.core.formatters.CellFormatter;
 import org.simple.parser.core.interfaces.IFileBean;
 import org.simple.parser.core.interfaces.IFileParser;
 import org.simple.parser.core.validators.CellValidator;
 import org.simple.parser.exceptions.SimpleParserException;
 
 
 
 
 //TODO Create custom exception object
 public class ExcelParser<T extends IFileBean> implements IFileParser<T>{
 
 	//mandatory 
 	private int noOfColumns=-1;
 
 	//optional
 	private int noOfRows=-1;
 	private int sheetNo=-1;
 	private int startRow=-1;
 	private int startCol=-1;
 	private int maxNoOfRows=-1;
 	private String dateFormat=null;
 
 	//config fields
 	private final Map<Integer,Field> flds= new HashMap<Integer,Field>();
 	private final Map<Integer,Class<? extends CellValidator>[]> validators = new HashMap<Integer,Class<? extends CellValidator>[]>();
 	private final Map<Integer,CellFormatter> writeFormatters = new HashMap<Integer, CellFormatter>();
 	private final Map<Integer,CellFormatter> readFormatters = new HashMap<Integer, CellFormatter>();
 	private final Map<Integer,Boolean> unique = new HashMap<Integer, Boolean>();
 	private Class<T> ouptutDTOClass;
 
 	private List<T> fileObjList=null;
 	private List<ErrorBean> errorList=null;
 	Map<Integer,Map<Object,Integer>> uniqueMap=null;
 	/**
 	 * Initialise parser configurations from {@link ParserDef} annotation file
 	 */
 	public void initialize(ParserDef props,Class<T> clazz) throws SimpleParserException  {
 		try{
 			this.noOfColumns= props.noOfColumns();
 			this.noOfRows=props.noOfRows();
 			this.sheetNo=props.sheetNo();
 			this.startRow=props.startRow();
 			this.startCol=props.startCol();
 			this.maxNoOfRows=props.maxNoOfRows();
 			this.dateFormat=props.dateformat();
 			this.ouptutDTOClass=clazz;
 			initMaps();
 		}catch (Exception e) {
 			throw new SimpleParserException("Error in configuration msg"+e.getMessage());
 		}
 	}
 
 	/**
 	 * Initialise parser configurations from property file
 	 */
 	public void initialize(Properties props) throws SimpleParserException  {
 		try{
 			this.noOfColumns= Integer.parseInt(props.getProperty("NO_OF_COLUMNS", "-1"));
 			this.noOfRows=Integer.parseInt(props.getProperty("NO_OF_ROWS", "-1"));
 			this.sheetNo=Integer.parseInt(props.getProperty("SHEET_NO", "0"));
 			this.startRow=Integer.parseInt(props.getProperty("START_ROW", "0"));
 			this.startCol=Integer.parseInt(props.getProperty("START_COL", "0"));
 			this.maxNoOfRows=Integer.parseInt(props.getProperty("MAX_ROWS", "-1"));
 		}catch (Exception e) {
 			throw new SimpleParserException("Error in configuration msg"+e.getMessage());
 		}
 	}
 
 	public List<T> getParsedObjects() {
 		return this.fileObjList;
 	}
 
 
 	public List<ErrorBean> getErrorObjects() {
 		return this.errorList;
 	}
 
 	public boolean isSucessfull() {
 		return (this.errorList.size() == 0);
 	}
 
 	public void parse(File fileObj) throws SimpleParserException {
 
 		Workbook w = getWorkbook(fileObj,true);// file has to exist for read case
 
 		fileObjList= new ArrayList<T>();
 		errorList=new ArrayList<ErrorBean>();
 
 
 		Sheet sheet = w.getSheetAt(sheetNo);
 		noOfRows =(noOfRows == -1) ? sheet.getLastRowNum()+1 : noOfRows;
 
 		int colWidth=this.noOfColumns-this.startCol;
 		if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");
 
 
 		int actualRowCount=0;
 		L2: for (int i = startRow; i < this.noOfRows; i++)
 		{
 			ErrorBean err = new ErrorBean(i);
 			T obj;
 			try	{
 				obj = ouptutDTOClass.newInstance();
 			}catch(Exception er)	{
 				throw new SimpleParserException("Error in creating class instace from input class Object using reflection.. Check JVM security settings");
 			}
 
 			Row row = sheet.getRow(i);
 			if(row == null) 	continue L2; // ignore blank rows
 			int j=0;
 			int emptyCount=1;
			uniqueMap = new HashMap<Integer, Map<Object,Integer>>();//added fo checking unique constrain violation
 			try{
 				actualRowCount++;
 				L1:for (j = this.startCol; j < this.noOfColumns; j++) 
 				{
 					Cell cell = row.getCell(j);
 					Field fld = flds.get(j);
 					if(fld == null) continue L1;// ignore columns not mapped to DTO objects
 					Object data = (cell == null) ? null : getCellVal(cell);// added to prevent null pointer exception for unused columns
 					if(data == null)emptyCount++;
 					try{
 //						if(data != null)System.out.print(fld.getName()+" | "+data+ " - " );
 						if(unique.get(j))	checkUnique(data, j); // unique constraint check
 						data=validateAndFormat(data, validators.get(j), readFormatters.get(j));
 					}catch(SimpleParserException p){
 						err.addColError(new ColErrors(j, p.getMessage()));// col error
 						continue L1;
 					}
 					fld.setAccessible(true);
 					fld.set(obj, typeConversion(fld.getType(),data));
 				}
 //				System.out.println("we");
 			}catch (Exception e) { // Added to coninute processing other rows
 				err.addColError(new ColErrors(j,e.getMessage()));
 				j=0;// make sure this obj is not added to fileObjList
 			}
 			//			System.out.println(actualRowCount+"_"+emptyCount+"_"+colWidth);
 			if(!err.hasErrors() )			this.fileObjList.add(obj);// completed full loop without error caseobject
 			else if(emptyCount < colWidth)  this.errorList.add(err); //TODO Remove this check
 			else							actualRowCount--;// empty row case
 		}
 
 		if(maxNoOfRows != -1 && maxNoOfRows < actualRowCount)	throw new SimpleParserException("Exceed maximun number("+maxNoOfRows+") of permitted rows ");
 
 	}
 
 	private void checkUnique(Object data,int colIndx) throws SimpleParserException{

 		Map<Object,Integer> m = uniqueMap.get(colIndx);
 		if(m== null)
 		{	
 			m=new HashMap<Object, Integer>();
 			m.put(data, 1);
 		}
 		else
 		{
 			if(m.containsKey(data))	throw new SimpleParserException("Unique contraint violated");
 			else					m.put(data,1);
 		}
 		uniqueMap.put(colIndx, m);
 	}
 
 	private Object validateAndFormat(Object data, Class<? extends CellValidator>[] validatorclses,CellFormatter formatter) throws SimpleParserException, InstantiationException, IllegalAccessException{
 		try{
 			for(Class<? extends CellValidator> validatorCls : validatorclses){
 				CellValidator validator = validatorCls.newInstance();
 				String errorMsg=validator.valid(data) ;
 				if(errorMsg  != null){// invalid case
 					throw new SimpleParserException(errorMsg);
 				}
 			}
 			return formatter.format(data);
 		}catch (Exception e) {
 			throw new SimpleParserException(e.getLocalizedMessage());
 		}
 	}
 
 	private Object getCellVal(Cell cell) throws SimpleParserException {
 		switch(cell.getCellType()){
 		case Cell.CELL_TYPE_NUMERIC:
 			return (DateUtil.isCellDateFormatted(cell)) ? new SimpleDateFormat(dateFormat).format(cell.getDateCellValue())/* date case */: cell.getNumericCellValue();
 		case Cell.CELL_TYPE_BLANK: 		return null;
 		case Cell.CELL_TYPE_BOOLEAN: 	return cell.getBooleanCellValue();
 		case Cell.CELL_TYPE_ERROR: 		throw new SimpleParserException("Invalid Cell type (Error)");
 		case Cell.CELL_TYPE_FORMULA: 	throw new SimpleParserException("Invalid Cell type (Formula)");
 		default:						return cell.getStringCellValue(); // String case
 		}
 	}
 
 	private Object typeConversion(Class<?> clazz,Object val) throws ParseException
 	{
 		if(val == null) 						return null;
 		String name = clazz.getSimpleName();
 		if(name.equalsIgnoreCase("Short") || name.equalsIgnoreCase("short"))	return (short)((Number)val).doubleValue();
 		if(name.equalsIgnoreCase("Integer") || name.equalsIgnoreCase("int"))	return (int)  ((Number)val).doubleValue();
 		if(name.equalsIgnoreCase("Long"))										return (long) ((Number)val).doubleValue();
 		if(name.equalsIgnoreCase("Float"))										return (float)((Number)val).doubleValue();
 		if(name.equalsIgnoreCase("Double"))										return 		  ((Number)val).doubleValue();
 		if(name.equalsIgnoreCase("Date")){
 			SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat); // MOVE THIS TO A PROPERTY FILE
 			return dateFormat.parse(val.toString());
 		}
 		return val.toString();
 	}
 
 
 	private void initMaps() throws SimpleParserException{
 		int maxIndex=0;
 		try
 		{
 			Field[] allFlds= ouptutDTOClass.getDeclaredFields();
 			for(Field fld : allFlds){
 				fld.setAccessible(true);
 				ColumnDef colDef =fld.getAnnotation(ColumnDef.class);
 				if(colDef == null) continue;
 				int index = colDef.index();
 				flds.put(index, fld);
 				validators.put(index,colDef.validators());
 				writeFormatters.put(index, colDef.writeFormatter().newInstance());
 				readFormatters.put(index, colDef.formatter().newInstance());
 				unique.put(index, colDef.unique());
 				maxIndex = (maxIndex < index) ? index : maxIndex;
 			}
 
 		}catch (Exception e) {
 			throw new SimpleParserException("Error in parsing annotations.. Error msg : "+e.getMessage());
 		}
 
 		if(maxIndex > noOfColumns) throw new SimpleParserException("Error in annoation configuration. Col index exceed noOf Columns declared");
 	}
 
 	private Workbook getWorkbook(File fileObj, boolean fileExist) throws SimpleParserException{
 		InputStream in = null;
 		if(fileExist){ // fileOb exist for read case and does not exist for write case
 			try	{
 				in = new FileInputStream(fileObj);
 			} catch (FileNotFoundException e1)
 			{
 				throw new SimpleParserException("Invalid File path");
 			}
 			if(noOfColumns == -1)	throw new SimpleParserException("No of Columns is manadatory for Excel parsing");
 		}
 
 		String fileName=fileObj.getName();
 		String[] ext= fileName.split("\\.");
 		String type = ext[ext.length-1];
 		try
 		{
 			if(fileExist){
 				if(type.equalsIgnoreCase("xls"))	return new HSSFWorkbook(in);
 				else								return new XSSFWorkbook(in);
 			}else{
 				if(type.equalsIgnoreCase("xls"))	return new HSSFWorkbook();
 				else								return new XSSFWorkbook();
 			}
 
 		}catch(IOException i)
 		{
 			throw new SimpleParserException("Error in parsing file using appache.poi lib.. File not a valid excel");
 		}
 	}
 
 	private String getStringVal(Class<? extends Field> clazz,Object val) {
 		if(val == null) 	return "";
 		String name = clazz.getSimpleName();
 		if(name.equalsIgnoreCase("Date")){
 			SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat); 
 			return(dateFormat.format((Date) val));
 		}
 		return val+"";
 	}
 
 	private void setCellVal(Cell cell, Class<?> clazz, Object val) throws ParseException {
 		if(val == null) {														cell.setCellValue(""); return;   }
 		String name = clazz.getSimpleName();
 		if(name.equalsIgnoreCase("String") )									{cell.setCellValue(val.toString()); return;}
 		if(name.equalsIgnoreCase("Short") || name.equalsIgnoreCase("short"))	{cell.setCellValue(((Number)val).doubleValue()); return;}
 		if(name.equalsIgnoreCase("Integer") || name.equalsIgnoreCase("int"))	{cell.setCellValue(((Number)val).doubleValue()); return;}
 		if(name.equalsIgnoreCase("Long"))										{cell.setCellValue(((Number)val).doubleValue()); return;}
 		if(name.equalsIgnoreCase("Float"))										{cell.setCellValue(((Number)val).doubleValue()); return;}
 		if(name.equalsIgnoreCase("Double"))										{cell.setCellValue(((Number)val).doubleValue()); return;}
 		if(name.equalsIgnoreCase("Date")){
 			SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat); 
 			cell.setCellValue(dateFormat.parse(val.toString()));
 			return;
 		}
 	}
 
 	public boolean writeObjects(List<T> objs,File fileObj) throws SimpleParserException {
 		OutputStream out;
 		boolean fileExist = fileObj.exists();
 		Workbook w = getWorkbook(fileObj,fileExist);
 
 		Sheet sheet = (fileExist)? w.getSheetAt(sheetNo) : w.createSheet();
 		int colWidth=this.noOfColumns-this.startCol;
 		if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");
 
 		int start=startRow;
 		for (T obj : objs)
 		{
 			ErrorBean err = new ErrorBean(start);
 			Row row = (fileExist)? sheet.getRow(start++) : sheet.createRow(start++);
 			if(row == null) 	throw new SimpleParserException("Row returned null from Sheet for row id "+start);
 			int j=0;
 			uniqueMap = new HashMap<Integer, Map<Object,Integer>>();//added fo checking unique constrain violation
 			try{
 				L1:for (j = this.startCol; j < this.noOfColumns; j++) 
 				{
 					Cell cell = (fileExist)?row.getCell(j): row.createCell(j);
 					Field fld = flds.get(j);
 					if(fld == null) continue L1;// ignore columns not mapped to DTO objects
 					Object data = (cell == null) ? null : fld.get(obj);// added to prevent null pointer exception for unused columns
 					try{
 						if(unique.get(j) && data != null)	checkUnique(data,j); // unique constraint check
 						data=validateAndFormat(data, validators.get(j), writeFormatters.get(j));
 						setCellVal(cell,fld.getType(),data);
 					}catch(SimpleParserException p){
 						System.out.println(p.getMessage());
 						err.addColError(new ColErrors(j, p.getMessage()));// col error
 						break L1;
 					}
 				}
 			}catch (Exception e) { // Added to coninute processing other rows
 				err.addColError(new ColErrors(j,e.getMessage()));
 				j=0;// make sure this obj is not added to fileObjList
 			}
 
 			if(err.hasErrors() )	this.errorList.add(err); //TODO Remove this check
 		}
 		if(this.errorList.size() != 0) return false;
 		else{
 			try{
 				out = new FileOutputStream(fileObj);
 				w.write(out);
 				out.flush();
 				out.close();
 				return true;
 			}catch (Exception e) {
 				throw new SimpleParserException(e);
 			}
 		}
 	}
 
 
 	public void writeObjectsToNewFile(List<T> obj, String filePath)
 			throws SimpleParserException {
 		// TODO Auto-generated method stub
 
 	}
 
 
 
 
 
 }
