 package es.deusto.ingenieria.maze;
 
 import es.deusto.ingenieria.aike.xml.InformationXMLReader;
 import es.deusto.ingenieria.maze.Cell.Foot;
 import es.deusto.ingenieria.maze.Cell.Wall;
 import java.awt.Point;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class EnvironmentXMLReader extends InformationXMLReader {
     
     private Cell[][] cells;
     private Point startLocation;
     private Point endLocation;
 
     public EnvironmentXMLReader(String fileXML) {
         super(fileXML);
     }
 
     public Object getInformation() {
         return new Environment(cells, startLocation, endLocation);
     }
     
     @Override
     public void startElement(String uri, String localName, String qName,
                              Attributes attributes) throws SAXException {
         try {		
             if (qName.equals("aike:maze")) {
                 int columns = Integer.parseInt(attributes.getValue("columns"));
                 int rows = Integer.parseInt(attributes.getValue("rows"));
                 cells = new Cell[columns][rows];
                for(int i=0; i < columns; i++) {
                    for (int j=0; i < rows; j++) {
                        cells[i][j] = new Cell();
                    }
                }
             } else if (qName.equals("aike:start")) {
                 int column = Integer.parseInt(attributes.getValue("column"));
                 int row = Integer.parseInt(attributes.getValue("row"));
                 startLocation = new Point(column, row);
             } else if (qName.equals("aike:end")) {
                 int column = Integer.parseInt(attributes.getValue("column"));
                 int row = Integer.parseInt(attributes.getValue("row"));
                 endLocation = new Point(column, row);
             } else if (qName.equals("aike:left")) {
                 int column = Integer.parseInt(attributes.getValue("column"));
                 int row = Integer.parseInt(attributes.getValue("row"));
                 cells[column][row].setFoot(Foot.LEFT);
             } else if (qName.equals("aike:right-wall")) {
                 int column = Integer.parseInt(attributes.getValue("column"));
                 int row = Integer.parseInt(attributes.getValue("row"));
                 cells[column][row].addWall(Wall.RIGHT);
             } else if (qName.equals("aike:bottom-wall")) {
                 int column = Integer.parseInt(attributes.getValue("column"));
                 int row = Integer.parseInt(attributes.getValue("row"));
                 cells[column][row].addWall(Wall.BOTTOM);
             }
         } catch (Exception ex) {
             System.out.println(this.getClass().getName() + ".startElement(): " + ex);
            ex.printStackTrace();
            throw new RuntimeException(ex);
         }
     }
 
 }
