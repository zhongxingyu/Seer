 package Widgets;
 
 import java.util.Calendar;
 
 import org.mt4j.AbstractMTApplication;
 import org.mt4j.components.TransformSpace;
 import org.mt4j.components.visibleComponents.shapes.MTRectangle;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.input.gestureAction.DefaultDragAction;
 import org.mt4j.input.gestureAction.InertiaDragAction;
 import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.font.FontManager;
 import org.mt4j.util.font.IFont;
 import org.mt4j.util.math.Vector3D;
 
 public class WidgetHeure extends MTRectangle{
 	
 	public AbstractMTApplication app;
 	public int x, y;
 	public int heure;
 	public int minute;
 	public int seconde;
 	IFont fontArial = FontManager.getInstance().createFont(app, "LEDFont.ttf", 40, new MTColor(0,255,0));
 	public MTTextArea infos = new MTTextArea(app, fontArial);
 	
 	
 	public WidgetHeure(int _x, int _y, int _width, int _height, AbstractMTApplication _app) {
 		super(_x, _y, _width, _height, _app);
 		app = _app;
 		x = _x;
 		y = _y;
 		
 		affichageDigital();
 		definirHeure();
 		afficherHeure();
 	}
 	
 	public void definirHeure(){
 		Calendar date  = Calendar.getInstance();
 
 	    heure = date.get(Calendar.HOUR_OF_DAY);
 	    minute = date.get(Calendar.MINUTE);
 	    seconde = date.get(Calendar.SECOND);
 	    
 	    System.out.println(heure + " : " + minute + " : " + seconde);
 	}
 	
 	public void afficherHeure(){
 		 String heureA = "" + heure;
 		 String minuteA = "" + minute;
 		 String secondeA = "" + seconde;
 		 
 		 if(heure < 10)
 		 {
 			 heureA = "0" + heureA;
 		 }
 		 
 		 if(minute < 10)
 		 {
 			 minuteA = "0" + minuteA;
 		 }
 		 
 		 if(seconde < 10)
 		 {
 			 secondeA = "0" + secondeA;
 		 }
 		
 		 infos.setText(heureA + " : " + minuteA + " : " + secondeA);
		 infos.setPositionRelativeToParent(new Vector3D(x + this.getWidthXY(TransformSpace.GLOBAL)/2, y + this.getHeightXY(TransformSpace.GLOBAL)/2));
 	}
 	
 	public void affichageDigital(){
 		System.out.println(heure + " : " + minute + " : " + seconde);
 		
 		setTexture(app.loadImage("clock.png"));
 		setNoStroke(true);
 		
 		infos.setFillColor(new MTColor(0, 0, 0, 0));
 		infos.setPickable(false);
 		infos.setNoStroke(true);
 		
 		addChild(infos);
 		
 		unregisterAllInputProcessors();
 		removeAllGestureEventListeners();
 		registerInputProcessor(new DragProcessor(app));
 		addGestureListener(DragProcessor.class, new DefaultDragAction());
 		addGestureListener(DragProcessor.class, new InertiaDragAction());
 	}
 
 }
