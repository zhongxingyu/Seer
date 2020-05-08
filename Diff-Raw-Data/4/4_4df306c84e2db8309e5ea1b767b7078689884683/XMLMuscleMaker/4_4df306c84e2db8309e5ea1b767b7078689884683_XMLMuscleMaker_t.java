 package xml;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import jboxGlue.Mass;
 import jboxGlue.Muscle;
 import jboxGlue.Spring;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class XMLMuscleMaker extends XMLReader{
 	private static final String A = "a";
	private static final String B = "b";
 	private static final String AMP = "amplitude";
 	private static final String CONST = "constant";
 	private static final String MUSC = "muscle";
 	private static final String REST = "restlength";
 
 
 
 
 	HashMap<String, Mass> myMassMap;
 
 	Document doc;
 	public XMLMuscleMaker(String xmlFile, HashMap<String, Mass> myMassMap, Document doc) {
 		super(xmlFile);
 		this.myMassMap = myMassMap;
 		this.doc = doc;
 	}
 	public ArrayList<Spring> makeMuscleObjects(){
 		Mass one;
 		Mass two;
 		double restLength;
 		double constant;
 		double amplitude;
 		ArrayList<Spring> muscles = new ArrayList<Spring>();
 		
 		//myDocument doc = docIn();
 		NodeList nodes = myDoc.getElementsByTagName(MUSC);
 		for (int i = 0; i < nodes.getLength(); i++) {
 			restLength = -1;
 			constant = 1;
 					
 			Node springItem = nodes.item(i);
 			NamedNodeMap nodeMap = springItem.getAttributes();
 			
 			one = myMassMap.get(nodeMap.getNamedItem(A).getNodeValue());
			two = myMassMap.get(nodeMap.getNamedItem(B).getNodeValue());
 			
 			amplitude = Double.parseDouble(nodeMap.getNamedItem(AMP).getNodeValue());
 			
 			try{restLength = Double.parseDouble(nodeMap.getNamedItem(REST).getNodeValue());}
 			catch(Exception e){}
 
 			try{constant = Double.parseDouble(nodeMap.getNamedItem(CONST).getNodeValue());}
 			catch(Exception e){}
 			
 			if(restLength == -1){
 				muscles.add(new Muscle(one, two, amplitude));
 			}
 			else{
 				muscles.add(new Muscle(one, two, restLength, constant, amplitude));
 			}
 		}
 		return muscles;
 	}
 }
