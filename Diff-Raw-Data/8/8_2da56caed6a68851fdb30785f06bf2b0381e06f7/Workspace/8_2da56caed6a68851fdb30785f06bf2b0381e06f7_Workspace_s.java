 package backEnd;
 
 import java.awt.Color;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.ResourceBundle;
 import util.Palette;
 
 public class Workspace extends Observable {
     
     private static final String DEFAULT_RESOURCES = "resources.FrontEnd";
     private Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private Color DEFAULT_PEN_COLOR = Color.BLACK;
     private Observer myObserver;
     private Model myModel;
     private Color myBackgroundColor;
    private List<Turtle> myTurtleList;
     private Palette myPalette;
     private ResourceBundle myResources;
     
 
     public Workspace(Observer observer, Model model){
         myObserver = observer;
         addObserver(myObserver);
         myModel = model;
         myResources = ResourceBundle.getBundle(DEFAULT_RESOURCES);
         myBackgroundColor = DEFAULT_BACKGROUND_COLOR;
         myPalette = new Palette();
     }
     
     public void setBackground(int colorIndex){
         try{
             myBackgroundColor = myPalette.getColor(colorIndex);
             setChanged();
         }
         catch(IndexOutOfBoundsException e){
             myModel.showErrorMsg(myResources.getString("colorIndex"));
         }
     }
     
     public void setPalette(int colorIndex, int r, int g, int b){
         myPalette.setColor(colorIndex, r, g, b);
         setChanged();
     }
     
     @Override
     protected void setChanged () {
         super.setChanged();
         notifyObservers();
     }
 }
