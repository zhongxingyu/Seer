 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
     
     public static void optionscalc() {
         render(params);
     }
   
     public static void sayHello(String myName) {
     	//System.out.println("Rendering");
     	Map<String, Double>  renderMap = null;
    	 
 			List<Map<String,Double>> m = new ArrayList<Map<String,Double>>();
 	    	
     	try {
     		
     		 String buyOrSell =  null;
 			 String contracts =  null;
 			 String expirationDate =  null;
 			 String strike = null;
 			 String callOrPut =  null;
 			 String premium= null;
 			 String ticker = params.get("ticker");
 			 String stockPrice = params.get("stockPrice");
 			 Iterator<String> keyItr = params.data.keySet().iterator();
 			
 			// int size = params.data.size() - 2;
 			 
 			// System.out.println("size is "+size);
 			 //ok its multiple of 6 then
 			 int noOfIndexes =   Integer.parseInt(params.get("POITableCount"));
 				
 			 //System.out.println(("POITableCount is "+noOfIndexes));
 			 
 			 List<Map<String,String>> positionsList = new ArrayList<Map<String,String>>();
 				
 			 
 			 //System.out.println("index is "+noOfIndexes);
 			// System.out.println(params.toString());
 			 //now loop over
 			 for(int i =1;i<=noOfIndexes;i++)
 			 {
 				 Map<String,String> map = new HashMap<String, String>();
 				 buyOrSell =  params.get("longshort_"+i);
 				 if(buyOrSell == null || buyOrSell.trim().equals("--")) continue;
 				 //System.out.println("buyOrSell is "+buyOrSell);
 				 map.put("buyOrSell", buyOrSell);
 				 contracts = params.get("contracts_"+i);
 				 //System.out.println("Contracts is "+contracts);
 				 map.put("contracts", contracts);
 				 expirationDate = params.get("expiration_1");
 				 map.put("expiration", expirationDate);
 				 strike = params.get("strike_"+i);
 				 map.put("strike", strike);
 				 callOrPut = params.get("type_"+i);
 				 map.put("callOrPut", callOrPut);
 				 premium=params.get("premium_"+i);
 				 map.put("premium", premium);
 				 //System.out.println("map is "+map);
 				 positionsList.add(map);
 				 
 			 }
 			 
 			 OptionsCalculator calculator = new OptionsCalculator();
 			 renderMap = calculator.process(positionsList,ticker,stockPrice);
 			 renderMap.put("inv", calculator.getInvestment());
 			 
 			 m.add(renderMap);
 			    
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	
    	String compare = params.get("compare");
    	//System.out.println("compare is "+compare);
    	if(compare !=null && compare.equals("compare"))
     	{
     	try {
     		
    		 String buyOrSell =  null;
 			 String contracts =  null;
 			 String expirationDate =  null;
 			 String strike = null;
 			 String callOrPut =  null;
 			 String premium= null;
 			 String ticker = params.get("ticker");
 			 String stockPrice = params.get("stockPrice");
 			 Iterator<String> keyItr = params.data.keySet().iterator();
 			
 			// int size = params.data.size() - 2;
 			 
 			 List<Map<String,String>> positionsList = new ArrayList<Map<String,String>>();
 				
 			// System.out.println("size is "+size);
 			 //ok its multiple of 6 then
			 int noOfIndexes =   Integer.parseInt(params.get("POITable1Count"));
 			 
 			// System.out.println("POITable1Count is "+noOfIndexes);
 			   
 			 
 			// System.out.println(params.toString());
 			 //now loop over
 			 for(int i =1;i<=noOfIndexes;i++)
 			 {
 				 Map<String,String> map = new HashMap<String, String>();
 				 buyOrSell =  params.get("longshort_"+i+"c");
 				// System.out.println("b "+buyOrSell);
 				 if(buyOrSell == null || buyOrSell.trim().equals("--")) continue;
 				 //System.out.println("buyOrSell is "+buyOrSell);
 				 map.put("buyOrSell", buyOrSell);
 				 contracts = params.get("contracts_"+i+"c");
 				 //System.out.println("Contracts is "+contracts);
 				 map.put("contracts", contracts);
 				 expirationDate = params.get("expiration_1");
 				 map.put("expiration", expirationDate);
 				 strike = params.get("strike_"+i+"c");
 				 map.put("strike", strike);
 				 callOrPut = params.get("type_"+i+"c");
 				 map.put("callOrPut", callOrPut);
 				 premium=params.get("premium_"+i+"c");
 				 map.put("premium", premium);
 				 //System.out.println("map is "+map);
 				 positionsList.add(map);
 				 
 			 }
 			 if(!positionsList.isEmpty())
 			 {
 			 OptionsCalculator calculator = new OptionsCalculator();
 			 renderMap = calculator.process(positionsList,ticker,stockPrice);
 			 renderMap.put("inv_c",calculator.getInvestment());
 			 //System.out.println("rendermap "+renderMap);
 			 m.add(renderMap);
 			 }
 			    
 			 
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	}
        //System.out.println("M is "+m);
         render(m);
     }
     
     
     public static void share() {
     	//System.out.println(params);
        render(params);
     }
 
 }
