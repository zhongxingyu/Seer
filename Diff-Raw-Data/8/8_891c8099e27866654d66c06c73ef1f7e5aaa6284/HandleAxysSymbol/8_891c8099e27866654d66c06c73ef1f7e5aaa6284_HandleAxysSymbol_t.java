 /**
  * 
  */
 package com.grimesco.gcocentral.axys;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.MultiMap;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import com.grimesco.gcocentral.axys.dao.AXYSSymbolDao;
 import com.grimesco.gcocentral.exception.SymbolNotFoundException;
 import com.grimesco.gcocore.act.Account;
 import com.grimesco.gcocore.axys.model.AxysSymbol;
 
 /**
  * @author jaeboston
  *
  */
 public class HandleAxysSymbol {
 
 	private static Logger logger = LoggerFactory.getLogger(HandleAxysSymbol.class);
 	
 	AXYSSymbolDao symbolDao;
 
 	public File sourceFile;
 	public Date exportedDate;
 
 	public ArrayList<AxysSymbol> symbolList;
 	
 	//private HashMap<String, AxysSymbol> symbolmap = new HashMap<String, AxysSymbol>();
 	//private HashMap<String, AxysSymbol> cusipmap = new HashMap<String, AxysSymbol>();
 	//private HashMap<String, AxysSymbol> namemap = new HashMap<String, AxysSymbol>();
 	public ArrayListMultimap<String, AxysSymbol> symbolmap = ArrayListMultimap.create();
 	public ArrayListMultimap<String, AxysSymbol> cusipmap = ArrayListMultimap.create();
 	public ArrayListMultimap<String, AxysSymbol> namemap = ArrayListMultimap.create();
 	
 	
 	
 	/** ---------------------------------------------- **/
 	/** Constructor                                    **/
 	/** ---------------------------------------------- **/
 	public HandleAxysSymbol() {
 		
 		super();
 		//-- open/read the application context file
 	    //ApplicationContext ctx = new FileSystemXmlApplicationContext("resources/Spring-Module.xml");
 		
 	    //-- instantiate our spring dao object from the application context
 	    //symbolDao = (AXYSSymbolDao)ctx.getBean("AXYSSymbolDao");
 		//symbolList = (ArrayList<AxysSymbol>) symbolDao.get();
 	}
 
 	/** ---------------------------------------------- **/
 	/** Constructor to connec to database               **/
 	/** ---------------------------------------------- **/
 	public HandleAxysSymbol(AXYSSymbolDao _axyssymboldao) {
 		
 		super();
 		
 		symbolDao = _axyssymboldao;
 		
 		getAxysSymbols();
 
 	}
 	
 	/** ---------------------------------------------- **/
 	/** Constructor                                    **/
 	/** ---------------------------------------------- **/
 	public HandleAxysSymbol(File afile) {
 
 		this.sourceFile = afile;
 		this.exportedDate = this.extractDatefromSourceFile();
 		try {
 				symbolList = extractSymbol();
 		} catch (IOException e) {
 			 e.printStackTrace();
 		}
 	}
 
 	//-- get all symbol from the Database
 	public void getAxysSymbols() {
 
 		//-- clear the buffer before upload
 		symbolmap.clear();
 		cusipmap.clear();
 		namemap.clear();
 		
 		symbolList = (ArrayList<AxysSymbol>) symbolDao.get();
 		
 		
 		//-- loop through symbolList and populate maps
 		for(AxysSymbol item: symbolList) {
 			
 			symbolmap.put(String.valueOf(item.getSYMBOL()).trim().toUpperCase(), item);
 			
 			//-- axys symbol maynot have CUSIP
 			if (item.getCUSIP() != null) {
 				cusipmap.put(String.valueOf(item.getCUSIP()).trim().toUpperCase(), item);
 			} else {
 				cusipmap.put("NOCUSIP", item);
 			}
 
 			//-- axys symbol maynot have NAME
 			if (item.getNAME() != null) {
 				namemap.put(String.valueOf(item.getNAME()).trim().toUpperCase(), item);
 			} else {
 				namemap.put("NONAME", item);
 			}
 		}	
 	}
 	
 	
 	//-- get the symbol from database
 	public AxysSymbol getAxysSymbol(String symbol) throws SymbolNotFoundException {
 		
 		try {
 				
 			List<AxysSymbol> symbollist = symbolDao.findAvailableSymbolBySymbol(symbol);
 		
 			if (symbollist.size() >= 1) {
 				return (AxysSymbol) symbollist.get(0);
 			} else {
 				//-- check the CUSIP
 				symbollist = symbolDao.findAvailableSymbolByCUSIP(symbol);
 				if (symbollist.size() >= 1) {
 					return (AxysSymbol) symbollist.get(0);
 				}else {
 					//-- check the NAME for option
 					symbollist = symbolDao.findAvailableSymbolByName(symbol);
 					if (symbollist.size() >= 1) {
 						return (AxysSymbol) symbollist.get(0);
 					}else {
 						//-- throw error
 						throw new SymbolNotFoundException(symbol);
 					}
 				}
 			}
 		} catch (DataAccessException dae) {
 			throw new SymbolNotFoundException(symbol);
 		}
 	}
 
 	//-- get the symbol from database with less than 9 character of CUSIP
 	//-- NOTE***: for multiple faster access, use getAxysSymbolcache(String _symbol) method 
 	public AxysSymbol getAxysSymbolUsingPartialCusip(String symbol) throws SymbolNotFoundException {
 		
 		try {
 				
 			List<AxysSymbol> symbollist = symbolDao.findAvailableSymbolBySymbol(symbol);
 		
 			if (symbollist.size() >= 1) {
 				return (AxysSymbol) symbollist.get(0);
 			} else {
 				//-- check the CUSIP
 				symbol = symbol.concat("%");
 				symbollist = symbolDao.findAvailableSymbolByCUSIP(symbol);
 				if (symbollist.size() >= 1) {
 					return (AxysSymbol) symbollist.get(0);
 				}else {
 					//-- check the NAME for option
 					symbollist = symbolDao.findAvailableSymbolByName(symbol);
 					if (symbollist.size() >= 1) {
 						return (AxysSymbol) symbollist.get(0);
 					}else {
 						//-- throw error
 						throw new SymbolNotFoundException(symbol);
 					}
 				}
 			}
 		} catch (DataAccessException dae) {
 			throw new SymbolNotFoundException(symbol);
 		}
 	}
 
 
 	//-- get the symbol from cache one to one
 	//-- if multiple asymbol is returned, get the first one
 	public AxysSymbol getAxysSymbolcache(String _symbol) throws SymbolNotFoundException {
 		
 		//-- initialize local variables
 		List<AxysSymbol> ones	= getAxysSymbolListcache(_symbol);
 		return ones.get(0);
 		
 	}
 
 	//-- get the symbol from cache one to many
 	//-- If symbol has 9 ch, it will use 8 ch to find a matching symbol
 	public List<AxysSymbol> getAxysSymbolListcache(String _symbol) throws SymbolNotFoundException {
 		
 		//-- initialize local variables
 		List<AxysSymbol> ones	= null;
 		
 		ones = symbolmap.get(_symbol);
 		
 		if ((ones == null) || (ones.size() == 0)) { //-- not found in symbol column look for in cusip column
 			ones = cusipmap.get(_symbol);
 		}
 		
 		if ((ones == null) || (ones.size() == 0)) {//-- not found in cusip column look for in name column
 			ones = namemap.get(_symbol);
 		}
		if ((ones == null) || (ones.size() == 0)) {
			//-- if still not found use one less char( for example 9 characters -> 8 characters
			//-- Do it for just 9 characters only
			if (_symbol.length() == 9) {
				_symbol = _symbol.substring(0, _symbol.length()-1);
			}
 			ones = symbolmap.get(_symbol);
 		}
 		
 		if ((ones == null) || (ones.size() == 0)) {
 			throw new SymbolNotFoundException(_symbol);
 		}else {
 			return ones;
 		}
 	}
 
 	
 	public AxysSymbol getAxysSymbolcachefromcusip(String _symbol) throws SymbolNotFoundException {
 		
 		//-- initialize local variables
 		List<AxysSymbol> ones	= null;
 		ones =  cusipmap.get(_symbol);
 		
 		if ((ones == null) || (ones.size() == 0)) {
 			throw new SymbolNotFoundException(_symbol);
 		}else {
 			return ones.get(0);  //-- return the first one
 		}
 	}
 	
 	public AxysSymbol getAxysSymbolcachefromname(String _symbol) throws SymbolNotFoundException {
 		
 		//-- initialize local variables
 		List<AxysSymbol> ones	= null;
 		ones = namemap.get(_symbol);
 		
 		if ((ones == null) || (ones.size() == 0)) {
 			throw new SymbolNotFoundException(_symbol);
 		}else {
 			return ones.get(0);
 		}
 	}
 	
 	
 	
 	//-- get the symbol from the Database for options
 	public AxysSymbol getAxysSymbolforOption(String symbol) throws SymbolNotFoundException {
 		try {
 				List<AxysSymbol> symbollist = symbolDao.findAvailableSymbolByName(symbol);
 		
 				if (symbollist.size() >= 1)
 					return (AxysSymbol) symbollist.get(0);
 				else
 					throw new SymbolNotFoundException(symbol);
 			
 		} catch (DataAccessException dae) {
 			throw new SymbolNotFoundException(symbol);
 		}
 	}
 	
 	//-- get the symbol from cache : try to use new method using map
 	@Deprecated
 	public AxysSymbol getAxysSymbolforOptioncache(String symbol) throws SymbolNotFoundException {
 		
 		AxysSymbol one = null;
 		boolean found = false;
 		
 		for (AxysSymbol item : this.symbolList) {
 			//check
 			if (item.getNAME() != null) {
 				
 				if(String.valueOf(item.getNAME()).matches(symbol)) {
 					one = item;
 					found = true;
 					break;
 				}
 			}
 		}
 		
 		if (found) {
 			return one;
 		} else {
 			throw new SymbolNotFoundException(symbol);
 		}	
 		
 			
 	}
 
 	
 	
 	
 	
 	/**
 	 * @return
 	 */
 	private Date extractDatefromSourceFile() {
 		
 		return new java.util.Date(this.sourceFile.lastModified());
 	}
 
 
 
 	/**
 	 * @return
 	 * @throws IOException 
 	 */
 	private ArrayList<AxysSymbol> extractSymbol() throws IOException {
 	
 		BufferedReader is = new BufferedReader(new FileReader(this.sourceFile));
 		int recordnum = 0;
 	    ArrayList<AxysSymbol> al_axyssymbol = new ArrayList<AxysSymbol>();
 	    AxysSymbol axyssymbol;
 		
 		String line = "";
 		
 	    try { 		    
 	    	while (line != null) {
 		    	
 		    	line = is.readLine();
 		    	axyssymbol = parseGetSymbolRecord(line);
 		    	al_axyssymbol.add(axyssymbol);
 		        recordnum++;		
 //		        System.out.println("recordnum :" + String.valueOf(recordnum));     
 //		        System.out.println("NAME :" + String.valueOf(axyssymbol.getNAME()));     
 		    }
 		    
 	    } catch(NullPointerException ex) {
 	    
 	    	// end of file close the file
 	    	is.close();
 	    }
 	    
 	    return al_axyssymbol;
 
 	}
 
 	/**
 	 * @param line
 	 * @return
 	 */
 	private AxysSymbol parseGetSymbolRecord(String line) {
 		
 		AxysSymbol symbolrecord = new AxysSymbol();
 
 		String[] values = line.split(",");
 		
 		symbolrecord.setTYPE(values[0].toCharArray());
 		symbolrecord.setSYMBOL(values[1].toCharArray());
 		symbolrecord.setCUSIP(values[2].toCharArray());
 		if (values.length > 3)
 			symbolrecord.setNAME(values[3].toCharArray());
 		else
 			symbolrecord.setNAME(null);
 		
 		if (values.length > 4)
 			symbolrecord.setDESCRIPTION(values[4].toCharArray());
 		else
 			symbolrecord.setDESCRIPTION(null);
 		
 		return symbolrecord;
 		
 	}
 
 	
 	//-- truncate ACCOUNT table in GCOcentral db
 	public void removeAllAxysSymbols() {
 		
 		symbolDao.truncateTable();
 		
 	}
 	
 	
 	
 	
 	
 
 }
