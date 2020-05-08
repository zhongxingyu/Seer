 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import org.jdom2.*;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 
 
 public class RequestID {
 	public static Element racine ;
 	public static Document document;
 
 	
 	public String getRacine() {
 		return racine.getName();
 	}
 	
 	public String getType() {
 		return racine.getChild("RequestType").getText();
 	}
 	
 	public String getTimeStart() {
 		return racine.getChild("TimeStart").getText(); 
 	}
 	
 	public String getTimeStop() {
 		return racine.getChild("TimeStop").getText(); 
 	}
 	
 	public String getCellID() {
 		return racine.getChild("CellID").getText(); 
 	}
 	
 	public void createParse(String message) throws Exception
 	{
 		
 		SAXBuilder sxb = new SAXBuilder();
 		document = sxb.build(new StringReader(message));
 		racine = document.getRootElement();
 		System.out.println(racine.getText());
 	}	
 	
	public static Document createError(String requestId, String type, String perrorData, String perrorDescription) {
 	
	racine = new Element(requestId);
 	Document document = new Document(racine);
	
 	Element error = new Element("error");
 	racine.addContent(error);
 	
 	Element errorType = new Element(type);
 	error.addContent(errorType);
 	
 	Element errorData = new Element("ErrorData");
 	errorData.setText(perrorData);
 	errorType.addContent(errorData);
 	
 	Element description = new Element("ErrorDescription");
 	description.setText(perrorDescription);
 	errorType.addContent(description);
 	
 	return document;
 	}
 	
 	
 	public static Document createReplyListCell(String requestId, List<String> cellIdList ) {
 	
 	racine = new Element(requestId);
 	Document document = new Document(racine);
 	
 	Element RequestType = new Element("RequestType");
 	RequestType.setText("ListCell");
 	racine.addContent(RequestType);
 	
 	for(String cellId : cellIdList) {
 		
 		Element cellElement = new Element("Cell");
 		racine.addContent(cellElement);
 		
 		Element cellIdElement = new Element("CellID");
 		cellIdElement.setText(cellId);
 		cellElement.addContent(cellIdElement);
 	
 	}
 
 	return document;
 	
 	}
 	
 	
 	public static Document createReplyCellStatSpeed(String requestId, String cellID , String timeStart , String timeStop,
 			List<CellDirection> directionAList, List<CellDirection> directionBList) {
 	
 	
 	racine = new Element(requestId);
 	Document document = new Document(racine);
 	Element RequestType = new Element("RequestType");
 	RequestType.setText("CellStatSpeed");
 	racine.addContent(RequestType);
 	
 	Element Cell = new Element("Cell");
 	racine.addContent(Cell);
 	
 	Element CellID = new Element("CellID");
 	CellID.setText(cellID);
 	Cell.addContent(CellID);
 	
 	Element TimeStart = new Element("TimeStart");
 	TimeStart.setText(timeStart);
 	Cell.addContent(TimeStart);
 	
 	Element TimeStop = new Element("TimeStop");
 	TimeStop.setText(timeStop);
 	Cell.addContent(TimeStop);
 	
 	Element DirectionCellID = new Element("Direction" + cellID +"A");
 	Cell.addContent(DirectionCellID);
 	
 	for(CellDirection cellDirectionElement : directionAList) {
 		
		if(cellDirectionElement.getError())
		{
			racine = null;
			return createError(requestId, "XMLError", "", "Error preprocessing including false data in XML File");
 		
 			
		}
 		Element CarType = new Element(String.valueOf( ((char)(Integer.parseInt(cellDirectionElement.getCarType())+64) )));
 		DirectionCellID.addContent(CarType);
 		
 		Element MinSpeed = new Element("MinSpeed");
 		MinSpeed.setText(cellDirectionElement.getMinSpeed());
 		CarType.addContent(MinSpeed);
 		
 		Element MaxSpeed = new Element("MaxSpeed");
 		MaxSpeed.setText(cellDirectionElement.getMaxSpeed());
 		CarType.addContent(MaxSpeed);
 		
 		Element AverageSpeed = new Element("AverageSpeed");
 		AverageSpeed.setText(cellDirectionElement.getAverageSpeed());
 		CarType.addContent(AverageSpeed);
 			
 	}
 	
 	Element DirectionCellID1 = new Element("Direction" + cellID +"B");
 	Cell.addContent(DirectionCellID1);
 	
 	for(CellDirection cellDirectionElement : directionBList) {
 			
 		Element CarType = new Element(cellDirectionElement.getCarType());
 		DirectionCellID.addContent(CarType);
 		
 		Element MinSpeed = new Element("MinSpeed");
 		MinSpeed.setText(cellDirectionElement.getMinSpeed());
 		CarType.addContent(MinSpeed);
 		
 		Element MaxSpeed = new Element("MaxSpeed");
 		MaxSpeed.setText(cellDirectionElement.getMaxSpeed());
 		CarType.addContent(MaxSpeed);
 		
 		Element AverageSpeed = new Element("AverageSpeed");
 		AverageSpeed.setText(cellDirectionElement.getAverageSpeed());
 		CarType.addContent(AverageSpeed);
 			
 	}
 	
 	return document;
 	}
 	
 	
 	public static Document createReplyCellStatNet(String requestId, String cellID , String timeStart , String timeStop, String fcarType, 
 	String ftimeStamp, String lcarType, String ltimeStamp, String totalCar, String totalData) {
 	
 	racine = new Element(requestId);
 	Document document = new Document(racine);
 	Element RequestType = new Element("RequestType");
 	RequestType.setText("CellStatNet");
 	racine.addContent(RequestType);
 	
 	Element Cell = new Element("Cell");
 	racine.addContent(Cell);
 	
 	Element CellID = new Element("CellID");
 	CellID.setText(cellID);
 	Cell.addContent(CellID);
 	
 	Element TimeStart = new Element("TimeStart");
 	TimeStart.setText(timeStart);
 	Cell.addContent(TimeStart);
 	
 	Element TimeStop = new Element("TimeStop");
 	TimeStop.setText(timeStop);
 	Cell.addContent(TimeStop);
 	
 	Element FirstCar = new Element("FirstCar");
 	Cell.addContent(FirstCar);
 	
 	Element CarType = new Element("CarType");
 	CarType.setText(fcarType);
 	FirstCar.addContent(CarType);
 	
 	Element timeStamp = new Element("timeStamp");
 	timeStamp.setText(ftimeStamp);
 	FirstCar.addContent(timeStamp);
 	
 	Element LastCar = new Element("LastCar");
 	Cell.addContent(LastCar);
 	
 	Element CarType1 = new Element("CarType");
 	CarType1.setText(lcarType);
 	LastCar.addContent(CarType1);
 	
 	Element timeStamp1 = new Element("timeStamp");
 	timeStamp1.setText(ltimeStamp);
 	LastCar.addContent(timeStamp1);
 	
 	Element TotalCar = new Element("TotalCar");
 	TotalCar.setText(totalCar);
 	Cell.addContent(TotalCar);
 	
 	Element TotalData = new Element("TotalData");
 	TotalData.setText(totalData);
 	Cell.addContent(TotalData);
 	
 	return document;
 	}
 	
 }
 
 
 
 
