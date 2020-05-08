 package org.bh.plugin.xbrlimport;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
import org.bh.data.IDTO;
 import org.bh.data.IPeriodicalValuesDTO;
import org.bh.data.types.Calculable;/*
 import org.bh.plugin.gcc.data.DTOGCCBalanceSheet;
 import org.bh.plugin.gcc.data.DTOGCCProfitLossStatementCostOfSales;
 import org.bh.plugin.gcc.data.DTOGCCProfitLossStatementTotalCost;
import org.bh.plugin.gcc.data.DTOGCCBalanceSheet.Key;*/
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 
 public class XBRLImport {
 	
 	private static XBRLImport instance = null;	
 	
 	private Map<String, String> keyNodeMap = null;
 	
 	private final static String keyNodeFile = "xbrl_key_nodes.txt";
 	
 	private final static String[] balanceSheetKeys = 
 	{"IVG", "SA", "FA", "VOR", "FSVG", "WP", "KBGGKS", "EK", "RS", "VB"};
 	
 	private final static String[] profitLossCostOfSalesKeys =
 	{"UE2", "SBE2", "HK", "VVSBA"};
 	
 	private final static String[] profitLossTotalCostKeys =
 	{"UE", "SBE", "MA", "PA", "ABSCH", "SBA"};
 	
 	private final static Namespace ns = Namespace.getNamespace("de-gaap-ci", "http://www.xbrl.de/de/fr/gaap/ci/2006-12-01");
 	
 	
 	private XBRLImport()
 	{
 		// Load keys and node
 		URL keyNodeFileURL = getClass().getResource(keyNodeFile);
 		
 		if (keyNodeFileURL == null)
 			Logger.getLogger(getClass()).debug("No key<->node file found!");
 		
 		
 		try {
 			BufferedReader fileContent = new BufferedReader(new InputStreamReader(keyNodeFileURL.openStream()));			
 			String line = null;
 			keyNodeMap = new HashMap<String, String>();
 			
 			while ((line = fileContent.readLine()) != null)
 			{
 				if (line.startsWith("#") || line.isEmpty())
 					continue;
 				
 				String key = line.substring(0, line.indexOf('=')).replace(" ", "");
 				String node = line.substring(line.indexOf('=') + 1).replace(" ", "");
 				Logger.getLogger(getClass()).debug("Found value for key: " + key);
 				keyNodeMap.put(key, node);
 			}
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	public static XBRLImport getInstance()	
 	{
 		if (instance == null)
 			instance = new XBRLImport();
 		return instance;
 	}
 	
 	
 	
 	public IPeriodicalValuesDTO getBalanceSheetDTO(String importPath) throws IOException, XBRLNoValueFoundException
 	{
 		// Load file
 		File importedFile = checkFile(importPath);
 		// If there were difficulties in loading the file throw new exception
 		if (importedFile == null)
 			throw new IOException();		
 		
 		// Create new DTO
		//DTOGCCBalanceSheet result = new DTOGCCBalanceSheet();	
		IPeriodicalValuesDTO result = null;
 		if (result == null)
 		{
 			Logger.getLogger(getClass()).debug("Could not create a DTOGCCBalanceSheet object!");
 			return null;
 		}
 		
 		// Create a SAX builder object to parse the xml document
 		SAXBuilder saxBuilder = new SAXBuilder();
 		try {
 			Document doc = saxBuilder.build(importedFile);		
 			// Get all necessary values for the balance sheet
 			Map<String, String> values = getValues(doc.getRootElement(), balanceSheetKeys);
 			
 			// If no values were found then throw a expcetion
 			if (values.size() == 0)
 				throw new XBRLNoValueFoundException();
 			
 			// Put all values into the DTO
 			for (String key : balanceSheetKeys)
 			{
				//Key dtoKey = Enum.valueOf(DTOGCCBalanceSheet.Key.ABET.getDeclaringClass(), key);
				result.put("", Calculable.parseCalculable(values.get(key)));				
 			}
 				
 		} catch (JDOMException e) {				
 			Logger.getLogger(getClass()).error("Parsing exception while importing a XML documetn", e);
 			return null;
 		} 				
 		return result;
 	}
 	
 	public IPeriodicalValuesDTO getProfitLossStatementCostOfSalesDTO(String importPath) throws IOException, XBRLNoValueFoundException
 	{
 		// Load file
 		File importedFile = checkFile(importPath);
 		// If there were difficulties in loading the file throw new exception
 		if (importedFile == null)
 			throw new IOException();		
 		
 		// Create new DTO
		//DTOGCCProfitLossStatementCostOfSales result = new DTOGCCProfitLossStatementCostOfSales();
		IPeriodicalValuesDTO result = null;
 		if (result == null)
 		{
 			Logger.getLogger(getClass()).debug("Could not create a DTOGCCBalanceSheet object!");
 			return null;
 		}
 		
 		// Create a SAX builder object to parse the xml document
 		SAXBuilder saxBuilder = new SAXBuilder();
 		try {
 			Document doc = saxBuilder.build(importedFile);		
 			// Get all necessary values for the balance sheet
 			Map<String, String> values = getValues(doc.getRootElement(), profitLossCostOfSalesKeys);
 			
 			// If no values were found then throw a expcetion
 			if (values.size() == 0)
 				throw new XBRLNoValueFoundException();
 			
 			// Put all values into the DTO
 			for (String key : profitLossCostOfSalesKeys)
 			{			
				//DTOGCCProfitLossStatementCostOfSales.Key dtoKey = Enum.valueOf(DTOGCCProfitLossStatementCostOfSales.Key.AFW.getDeclaringClass(), key.replace("2", ""));
				result.put("", Calculable.parseCalculable(values.get(key)));				
 			}
 				
 		} catch (JDOMException e) {				
 			Logger.getLogger(getClass()).error("Parsing exception while importing the XML document.", e);
 			return null;
 		} 				
 		return result;
 	}
 	
 	
 	public IPeriodicalValuesDTO getProfitLossStatementTotalCostDTO(String importPath) throws IOException, XBRLNoValueFoundException
 	{
 		// Load file
 		File importedFile = checkFile(importPath);
 		// If there were difficulties in loading the file throw new exception
 		if (importedFile == null)
 			throw new IOException();		
 		
 		// Create new DTO
		//DTOGCCProfitLossStatementTotalCost result = new DTOGCCProfitLossStatementTotalCost();	
		IPeriodicalValuesDTO result = null;
 		if (result == null)
 		{
 			Logger.getLogger(getClass()).debug("Could not create a DTOGCCBalanceSheet object!");
 			return null;
 		}
 		
 		// Create a SAX builder object to parse the xml document
 		SAXBuilder saxBuilder = new SAXBuilder();
 		try {
 			Document doc = saxBuilder.build(importedFile);		
 			// Get all necessary values for the balance sheet
 			Map<String, String> values = getValues(doc.getRootElement(), profitLossTotalCostKeys);
 			
 			// If no values were found then throw a expcetion
 			if (values.size() == 0)
 				throw new XBRLNoValueFoundException();
 			
 			// Put all values into the DTO
 			for (String key : profitLossTotalCostKeys)
 			{
				//DTOGCCProfitLossStatementTotalCost.Key dtoKey = Enum.valueOf(DTOGCCProfitLossStatementTotalCost.Key.AFW.getDeclaringClass(), key);
				result.put("", Calculable.parseCalculable(values.get(key)));				
 			}
 				
 		} catch (JDOMException e) {				
 			Logger.getLogger(getClass()).error("Parsing exception while importing the XML document.", e);
 			return null;
 		} 				
 		return result;
 	}
 	
 	
 	
 	private Map<String, String> getValues(Element el, String[] keys)
 	{
 		Map<String, String> result = new HashMap<String, String>();
 		
 		for (String key : keys)
 		{
 			// Get node name
 			String nodeName = keyNodeMap.get(key);
 			
 			String value = null;
 			
 			// The value must be calculated
 			if (nodeName.contains("+") || nodeName.contains("-"))
 			{
 				value = calculateValue(el, nodeName);
 				result.put(key, value);	
 				continue;
 			}
 			
 			List<Element> children = el.getChildren(nodeName, ns);
 			Element searchedElement = null;
 			if (children.size() == 0)
 			{
 				Logger.getLogger(getClass()).debug("No value found for key: " + key);
 				continue;
 			}	
 			else if (children.size() > 1)
 			{
 				for (Element childEl : children)
 				{
 					if (childEl.getAttributeValue("id").contains("AktuellesJahr"))
 					{
 						value = childEl.getValue();
 						break;
 					}					
 				}
 			}
 			else if (children.size() == 1)			
 				value = children.get(0).getValue();
 			
 			if (value != null)
 				result.put(key, value);			
 		}
 		
 		return result;
 	}
 	
 	private String getValue(Element el, String nodeName)
 	{		
 			
 			List<Element> children = el.getChildren(nodeName, ns);
 			String value = null;
 			if (children.size() == 0)
 			{
 				Logger.getLogger(getClass()).debug("Could not find node: " + nodeName);			
 			}	
 			else if (children.size() > 1)
 			{
 				for (Element childEl : children)
 				{
 					if (childEl.getAttributeValue("id").contains("AktuellesJahr"))
 					{
 						value = childEl.getValue();
 						break;
 					}					
 				}
 			}
 			else
 				value = children.get(0).getValue();
 		
 		
 		return value;
 	}
 	
 	
 	private String calculateValue(Element el, String equation)
 	{
 		equation = equation.replace(" ", "");
 		
 		int idxMinus = equation.indexOf("-");
 		int idxPlus = equation.indexOf("+");
 		int idx=0;
 		
 		if (idxMinus == -1)
 			idxMinus = 99;
 		if (idxPlus == -1)
 			idxPlus = 99;
 		
 		if (idxMinus < idxPlus)
 			idx = idxMinus;
 		else if (idxMinus > idxPlus)
 			idx = idxPlus;
 		else
 			idx = 0;
 		
 		String key;
 		if (idx != 0)
 			key = equation.substring(0, idx);
 		else
 			key = equation;
 		String nodeName = keyNodeMap.get(key);		
 		
 		if (nodeName == null)
 			return null;
 		
 		
 		String value = getValue(el, nodeName);
 		if (value == null)
 			return null;
 		Double result = Double.parseDouble(value);
 		if (!key.equals(equation))
 		{
 			value = calculateValue(el, equation.substring(idx + 1));
 			if (value == null)
 				return null;
 			if (idxMinus < idxPlus)				
 				result -= Double.parseDouble(value);			
 			else			
 				result += Double.parseDouble(value);
 			
 		}	
 		
 		return result.toString();
 	}
 	
 	/**
 	 * Creates a File instance by using the given path and
 	 * checks if the file can be processed.
 	 * @return	True 	- File is okay<br>
 	 * 			False	- File can not be used
 	 */
 	private File checkFile(String path)
 	{
 		File importFile = new File(path);
 		
 		if (importFile == null || !importFile.isFile() || !importFile.canRead() ||
 				!importFile.canWrite() || !importFile.exists())
 			return null;
 		
 		return importFile;
 	}	
 	
 
 }
