 package edu.cmu.ebiz.task8.model;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Formatter;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import edu.cmu.ebiz.task8.bean.PeopleAgeBean;
 import edu.cmu.ebiz.task8.bean.PeopleGenderBean;
 import edu.cmu.ebiz.task8.bean.PeopleIncomeBean;
 import edu.cmu.ebiz.task8.bean.PeopleSegmentBean;
 
 public class ZillowDAO {
 	private static Document document;
 
 	//this method check whether user input is a valid city
 	public boolean getMsgCode(String state, String city, String area) {
 		try {
 			// prepare statement, based on your own api
 			String preparedURL = "http://www.zillow.com/webservice/GetDemographics.htm?zws-id=X1-ZWz1di5yjoznd7_2ca3z&state="+state
 					+"&city=" + city 
 					+"&neighborhood="+ area;
 
 			String xmlString = GetXMLDocString.getString(preparedURL);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true); // never forget this!
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource(new StringReader(xmlString));
 			Document doc = builder.parse(is);
 
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

 			//before using XPath get information, first check error code!
			XPathExpression msgCode = xpath.compile("//message/code/text()");
			Object code = msgCode.evaluate(doc, XPathConstants.NODE);
			Node codeNode = (Node) code;
			if (codeNode.getNodeValue().equals("0"))
 				return true;
 			else {
 				return false;
 			}
 
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 
 	public ArrayList<PeopleSegmentBean> getSegmentation(String state, String city, String area) {
 		try {
 			// prepare statement, based on your own api
 			String preparedURL = "http://www.zillow.com/webservice/GetDemographics.htm?zws-id=X1-ZWz1di5yjoznd7_2ca3z" 
 					+"&state="+ replaceSpace(state)
 					+"&city=" + replaceSpace(city)
 					+"&neighborhood=" + replaceSpace(area);
 
 			String xmlString = GetXMLDocString.getString(preparedURL);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true); // never forget this!
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource(new StringReader(xmlString));
 			Document doc = builder.parse(is);
 
 			XPathFactory xFactory = XPathFactory.newInstance();
 			XPath xpath = xFactory.newXPath();
 
 			//here is your customize xpath expression
 			/**
 			 * <liveshere>
 			 * 		<title>Ramen Metros</title>
 			 * 		<name>Lower-income urban singles.</name>
 			 * 		<description>
 			 * 			Most rent their apartment or condo. Some have a college education and work in services and the professional sector.
 			 *  	</description>
 			 * </liveshere>
 			 */
 			//get <title>
 			NodeList titles = GetXMLDocString.getExpressionResult(doc, "//page[name='People']/segmentation/liveshere/title/text()");
 			NodeList names = GetXMLDocString.getExpressionResult(doc, "//page[name='People']/segmentation/liveshere/name/text()");
 			NodeList descs = GetXMLDocString.getExpressionResult(doc, "//page[name='People']/segmentation/liveshere/description/text()");
 			
 			ArrayList<PeopleSegmentBean> segments = new ArrayList<PeopleSegmentBean>();
 			for (int i = 0; i<titles.getLength(); i++) {
 				PeopleSegmentBean currentSeg = new PeopleSegmentBean();
 				currentSeg.setTitle(titles.item(i).getNodeValue());
 				if (names.item(i) != null) 
 					currentSeg.setName(names.item(i).getNodeValue());
 				if (descs.item(i) != null)
 					currentSeg.setDescription(descs.item(i).getNodeValue());
 
 				segments.add(currentSeg);
 			} 
 
 			return segments;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}  catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public PeopleIncomeBean getIncome(String state, String city, String area) {
 		try {
 			// prepare statement, based on your own api
 			String preparedURL = "http://www.zillow.com/webservice/GetDemographics.htm?zws-id=X1-ZWz1di5yjoznd7_2ca3z&state="+replaceSpace(state)
 					+"&city=" + replaceSpace(city)
 					+"&neighborhood="+ replaceSpace(area);
 
 			String xmlString = GetXMLDocString.getString(preparedURL);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true); // never forget this!
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource(new StringReader(xmlString));
 			Document doc = builder.parse(is);
 
 			// easy way to get result of XPath Expression, just call GetXMLDocString
 			NodeList nationIncome = GetXMLDocString.getExpressionResult(doc, "//attribute[name='Median Household Income']/values/nation/value/text()");
 			NodeList cityIncome = GetXMLDocString.getExpressionResult(doc, "//attribute[name='Median Household Income']/values/city/value/text()");
 			NodeList neighborIncome = GetXMLDocString.getExpressionResult(doc, "//attribute[name='Median Household Income']/values/neighborhood/value/text()");
 
 			//for each area, there is only one set of data, so we don't use for loop
 			PeopleIncomeBean income = new PeopleIncomeBean();
 			if (nationIncome == null || nationIncome.getLength() == 0) 
 				income.setNationIncome("Data does not exist");
 			else {
 				String nincome = nationIncome.item(0).getNodeValue();
 				int i = nincome.indexOf(".");
 				if (i != -1) 
 					income.setNationIncome(nincome.substring(0, i+3));
 				else 
 					income.setNationIncome(nincome);
 				
 				double nI = Double.parseDouble(nincome);
 				income.setNationP(nI/800);
 			}
 			
 			if (cityIncome == null || cityIncome.getLength() == 0) 
 				income.setCityIncome("Data does not exist");
 			else {
 				String cincome = cityIncome.item(0).getNodeValue();
 				int i = cincome.indexOf(".");
 				if (i != -1)
 					income.setCityIncome(cincome.substring(0, i+3));
 				else 
 					income.setCityIncome(cincome);
 				double cI = Double.parseDouble(cincome);
 				income.setCityP(cI/800);
 			}
 
 			if (neighborIncome == null || neighborIncome.getLength() == 0) 
 				income.setNeighborIncome("Data does not exist");
 			else {
 				String aincome = neighborIncome.item(0).getNodeValue();
 				int i = aincome.indexOf(".");
 				if (i != -1)
 					income.setNeighborIncome(aincome.substring(0, i+3)); 
 				else {
 					income.setNeighborIncome(aincome);
 				}
 				double neiI = Double.parseDouble(aincome);
 				income.setNeighborP(neiI/800);
 			}
 
 			return income;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	public PeopleGenderBean getGender(String state, String city, String area) {
 		try {
 			// prepare statement, based on your own api
 			String preparedURL = "http://www.zillow.com/webservice/GetDemographics.htm?zws-id=X1-ZWz1di5yjoznd7_2ca3z" 
 					+"&state="+ replaceSpace(state)
 					+"&city=" + replaceSpace(city)
 					+"&neighborhood=" + replaceSpace(area);
 
 			String xmlString = GetXMLDocString.getString(preparedURL);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true); // never forget this!
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource(new StringReader(xmlString));
 			Document doc = builder.parse(is);
 
 			XPathFactory xFactory = XPathFactory.newInstance();
 			XPath xpath = xFactory.newXPath();
 			
 			PeopleGenderBean gender = new PeopleGenderBean();
 			
 			NodeList divorcedF = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Divorced-Female']/value/text()");
 			NodeList divorcedM = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Divorced-Male']/value/text()");
 			NodeList marryF = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Married-Female']/value/text()");
 			NodeList marryM = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Married-Male']/value/text()");
 			NodeList singleF = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Single-Female']/value/text()");
 			NodeList singleM = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Single-Male']/value/text()");
 			NodeList wF = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Widowed-Female']/value/text()");
 			NodeList wM = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-RelationshipStatus']/data/attribute[name='Widowed-Male']/value/text()");
 			
 			if (divorcedF == null || divorcedF.getLength() == 0) {
 				gender = new PeopleGenderBean("0");
 			} else {
 				long df = Math.round(Double.parseDouble(divorcedF.item(0).getNodeValue())*100);
 				gender.setDivorcedFemale(Long.toString(df));
 				
 				long dm = Math.round(Double.parseDouble(divorcedM.item(0).getNodeValue())*100);
 				gender.setDivorcedMale(Long.toString(dm));
 				long mf = Math.round(Double.parseDouble(marryF.item(0).getNodeValue())*100);
 				gender.setMarriedFemale(Long.toString(mf));
 				long mm = Math.round(Double.parseDouble(marryM.item(0).getNodeValue())*100);
 				gender.setMarriedMale(Long.toString(mm));
 				long sf = Math.round(Double.parseDouble(singleF.item(0).getNodeValue())*100);
 				gender.setSingleFemale(Long.toString(sf));
 				long sm = Math.round(Double.parseDouble(singleM.item(0).getNodeValue())*100);
 				gender.setSingleMale(Long.toString(sm));
 				long wf = Math.round(Double.parseDouble(wF.item(0).getNodeValue())*100);
 				gender.setWidowedFemale(Long.toString(wf));
 				long wm = Math.round(Double.parseDouble(wM.item(0).getNodeValue())*100);
 				gender.setWidowedMale(Long.toString(wm));
 			}
 			return gender;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}  catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	public PeopleAgeBean getAgeDecade(String state, String city, String area) {
 		try {
 			// prepare statement, based on your own api
 			String preparedURL = "http://www.zillow.com/webservice/GetDemographics.htm?zws-id=X1-ZWz1di5yjoznd7_2ca3z" 
 					+"&state="+ replaceSpace(state)
 					+"&city=" + replaceSpace(city)
 					+"&neighborhood=" + replaceSpace(area);
 
 			String xmlString = GetXMLDocString.getString(preparedURL);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true); // never forget this!
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource(new StringReader(xmlString));
 			Document doc = builder.parse(is);
 
 			XPathFactory xFactory = XPathFactory.newInstance();
 			XPath xpath = xFactory.newXPath();
 			
 			PeopleAgeBean ages = new PeopleAgeBean();
 			
 			NodeList zeros = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-AgeDecade']/data/attribute[name='0s']/value/text()");
 			NodeList tens = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-AgeDecade']/data/attribute[name='10s']/value/text()");
 			NodeList twentys = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-AgeDecade']/data/attribute[name='20s']/value/text()");
 			NodeList thirtys = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-AgeDecade']/data/attribute[name='30s']/value/text()");
 			NodeList fortys = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-AgeDecade']/data/attribute[name='40s']/value/text()");
 			NodeList fiftys = GetXMLDocString.getExpressionResult(doc, 
 					"//page[name='People']/tables/table[name='Census Summary-AgeDecade']/data/attribute[name='50s']/value/text()");
 			
 			if (zeros == null || zeros.getLength() == 0) {
 				ages.setZero(0);
 				ages.setTen(0);
 				ages.setTwenty(0);
 				ages.setThirty(0);
 				ages.setForty(0);
 				ages.setFifty(0);
 				ages.setSixseventy(0);
 			} else {
 				String a0s = zeros.item(0).getNodeValue();
 				Double d0s = (double) Math.round(Double.parseDouble(a0s)*1000)/10;
 				String a10s = tens.item(0).getNodeValue();
 				Double d10s = (double) Math.round(Double.parseDouble(a10s)*1000)/10;
 				String a20s = twentys.item(0).getNodeValue();
 				Double d20s = (double) Math.round(Double.parseDouble(a20s)*1000)/10;
 				String a30s = thirtys.item(0).getNodeValue();
 				Double d30s = (double) Math.round(Double.parseDouble(a30s)*1000)/10;
 				String a40s = fortys.item(0).getNodeValue();
 				Double d40s = (double) Math.round(Double.parseDouble(a40s)*1000)/10;
 				String a50s = fiftys.item(0).getNodeValue();
 				Double d50s = (double) Math.round(Double.parseDouble(a50s)*1000)/10;
 				
 				Double d60s = 100 - d0s - d10s -d20s -d30s - d40s -d50s;
 				ages.setZero(d0s);
 				ages.setTen(d10s);
 				ages.setTwenty(d20s);
 				ages.setThirty(d30s);
 				ages.setForty(d40s);
 				ages.setFifty(d50s);
 				DecimalFormat df = new DecimalFormat("#.00");
 				ages.setSixseventy(Double.parseDouble(df.format(d60s)));
 			}
 
 			return ages;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}  catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	public String[] getLatLong(String state, String city, String area) {
 		String[] latlong = new String[2];
 		try {
 			// prepare statement, based on your own api
 			String preparedURL = "http://www.zillow.com/webservice/GetDemographics.htm?zws-id=X1-ZWz1di5yjoznd7_2ca3z" 
 					+"&state="+ replaceSpace(state)
 					+"&city=" + replaceSpace(city)
 					+"&neighborhood=" + replaceSpace(area);
 
 			String xmlString = GetXMLDocString.getString(preparedURL);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true); // never forget this!
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			InputSource is = new InputSource(new StringReader(xmlString));
 			Document doc = builder.parse(is);
 
 			XPathFactory xFactory = XPathFactory.newInstance();
 			XPath xpath = xFactory.newXPath();
 			
 			NodeList lat = GetXMLDocString.getExpressionResult(doc, "//region/latitude/text()");
 			NodeList longt = GetXMLDocString.getExpressionResult(doc, "//region/longitude/text()");
			
			latlong[0] = lat.item(0).getNodeValue();
			latlong[1] = longt.item(0).getNodeValue();
 
 			return latlong;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}  catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	// covert space to %20, prevent data error
 	private String replaceSpace(String str) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i<str.length(); i++) {
 			if (str.charAt(i) == ' ') {
 				sb.append("%20");
 			} else {
 				sb.append(str.charAt(i));
 			}
 		}
 
 		return sb.toString();
 	}
 }
