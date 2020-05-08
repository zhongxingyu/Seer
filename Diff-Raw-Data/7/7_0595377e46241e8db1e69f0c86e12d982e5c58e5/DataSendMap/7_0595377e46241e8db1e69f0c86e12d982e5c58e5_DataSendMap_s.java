 package connectors.commands.data;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXB;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import essentials.enums.LetterEnum;
 import essentials.objects.Brick;
 import essentials.objects.ScrabbleMap;
 
 /**
  * Class for representing a scrabble map (for JAXB)
  * @author hannes
  *
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "dataSendMap")
 public class DataSendMap {
 	
 	/**
 	 * Map of rows
 	 */
 	@XmlElement
 	protected List<DataSendMapRow> maprow;
 	
 	/**
 	 * Constructor
 	 */
 	public DataSendMap() {}
 	
 	/**
 	 * Constructor
 	 * Converts ScrabbleMap to DataSendMap object for JAXB marshalling
 	 * @param aMap Scrabblemap
 	 */
 	public DataSendMap(ScrabbleMap aMap){
 		Brick[][] mMap = aMap.getMap();
 		
 		maprow = new ArrayList<DataSendMapRow>();
 		
 		for( int row = 0; row < mMap.length; row++ ){
 			maprow.add( new DataSendMapRow(mMap[row]) );
 		}
 		
 	}
 	
 	
 	/**
 	 * Converts object back to ScrabbleMap
 	 * @return
 	 */
 	public ScrabbleMap getScrabbleMap(){
 		ScrabbleMap rMap = new ScrabbleMap();
 		Brick[][] mMap = new Brick[countRows()][countColumns()];
 		int x = 0,  y = 0;
 		
 		for( DataSendMapRow row : maprow ){
 			for( Brick b : row.getBrick() ){
 				
 				if( (b.getLetter() != LetterEnum.NULL)
 						&& (y < countRows())
 						&& (x < countColumns()) ){
 					mMap[y][x] = b;
 				}				
 				x++;
 				
 			}
 			x = 0;
 			y++;
 		}
 		rMap.setMap(mMap);
 		
 		return rMap;
 	}
 	
 	protected int countRows(){
 		return maprow.size();
 	}
 	
 	protected int countColumns(){
 		return maprow.get(0).countColumns();
 	}
 	
 	
 	/**
 	 * @return Xml String of map
 	 */
 	public String toString(){
 		StringWriter dataWriter = new StringWriter();
 		JAXB.marshal(this, dataWriter);
 		return dataWriter.toString();
 	}
 	
 }
