 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 
 
 public class Leveleditor {
 	private static LWJGL_Font lucida;
 	private static int type = 1;
 	
 	//Main-Methode
 	public static void main(String[] args){
 		GameTime.init();
 		
     	Renderer.initDisplay(800,600,60);
     	Renderer.initGL();
     	Renderer.setClearColor(1.0f, 1.0f, 1.0f, 1.0f); //white
     	
 		try {
 			lucida = new LWJGL_Font("lucida_console2.png");
 			lucida.setScale(0.45f);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			System.exit(0);
 		}
 		
 		String name = getInput("Level Name: ", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
 		name = name + ".xml";
 		int width = Integer.parseInt(getInput("Level Width: ", "0123456789"));
 		int height = Integer.parseInt(getInput("Level Height: ", "0123456789"));
 				
 		int px = 0, py = 0;
 		int p2x = 1, p2y = 1;
 		Feld[][] spielfeld = new Feld[width][height];
 		for(int x = 0; x < width; x++) {
 			for(int y = 0; y < height; y++) {
 				spielfeld[x][y] = new Leerfeld();
 			}
 		}
 		boolean finish=false;
 		
        while (!Display.isCloseRequested()) {
         	// conti bezeichnet den Unterschied zwischen Spiel- und Hauptmenu
         	Renderer.clearGL();
        	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) finish=true;//Beim druecken von esc wird ueberprueft ob LVL gueltig
         	pollInput();
         	
 		    for(int x = 0; x < spielfeld.length; x++) {
 		    	for(int y = 0; y < spielfeld[0].length; y++) {
 		    		spielfeld[x][y].draw(x, y);
 		    	}
 		    }
 		    
 		    int x = MouseFeldX(Mouse.getX());
 		    int y = MouseFeldY(Mouse.getY());
 		    
 		    if(Mouse.isButtonDown(0) && (x < width) && (y < height)) {
 		    	switch(type) {
 		    		case 1: 
 		    			spielfeld[x][y] = new Steinfeld();
 		    			 break;
 		    		case 2: 
 		    			spielfeld[x][y] = new Mauerfeld();
 		    			 break;
 		    		case 3: 
 		    			px = x; py = y;
 		    			 break;
 		    		case 4: 
 		    			p2x = x; p2y = y;
 		    			 break;
 		    	}
 		    }
 		    
 		    if(Mouse.isButtonDown(1) && (x < width) && (y < height)) {
 		    	spielfeld[x][y] = new Leerfeld();
 		    }
 		    
 	    	switch(type) {
 	    		case 1: 
 	    			Renderer.Tile_Wall.draw(Mouse.getX(), Display.getHeight() - Mouse.getY()); 
 	    			lucida.print(0, 0, "   LMB: Steinfeld setzen (1 = Mauer, 2 = P1, 3 = P2)");
 	    			break;
 	    		case 2: 
 	    			Renderer.Tile_Break.draw(Mouse.getX(), Display.getHeight() - Mouse.getY());
 	    			lucida.print(0, 0, "   LMB: Mauer (zerstoerbar) setzen (0 = Steinfeld, 2 = P1, 3 = P2)");
 	    			 break;
 	    		case 3: 
 	    			lucida.print(Mouse.getX(), Display.getHeight() - Mouse.getY(), "P1");
 	    			lucida.print(0, 0, "   LMB: Player 1 setzen (0 = Steinfeld, 1 = Mauer, 3 = P2)");
 	    			 break;
 	    		case 4: 
 	    			lucida.print(Mouse.getX(), Display.getHeight() - Mouse.getY(), "P2");
 	    			lucida.print(0, 0, "   LMB: Player 2 setzen (0 = Steinfeld, 1 = Mauer, 2 = P1)");
 	    			 break;
 	    	}
 	    	lucida.print(0, (int)(lucida.getScale() * 24), "   RMB: Feld loeschen"); 
 	    			
 	    	lucida.print(Feld.getSize()*px, Feld.getSize()*py, "P1");
 	    	lucida.print(Feld.getSize()*p2x, Feld.getSize()*p2y, "P2");
 	    	
 	    	if (finish){
         	if (!startpruefung(px,py,p2x,p2y,width,height)){finish = false;}
         	if (!aufbaupruefung(spielfeld, width, height)) {
         		{lucida.print(0, 560, "Level ungueltig, da nicht gerecht aufgebaut.");
         		finish=false;}	        		
         		}
         	}//while
 		    GameTime.update();
 		    Renderer.sync();
         }
         //abspeichern
         try {
 			speichern(spielfeld,width,height,px,py,p2x,p2y,name);
 		} catch (FileNotFoundException | XMLStreamException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         Renderer.destroy();
 	}
 	
 	
 
 	public static void pollInput(){
 		Keyboard.poll();
 		while (Keyboard.getNumKeyboardEvents() > 0) {
 			if(Keyboard.getEventKeyState() == true) {
 				switch(Keyboard.getEventKey()) {
 					case Keyboard.KEY_ESCAPE:;
 						break;
 					case Keyboard.KEY_1: type = 1;
 						break;
 					case Keyboard.KEY_2: type = 2;
 						break;
 					case Keyboard.KEY_3: type = 3;
 						break;
 					case Keyboard.KEY_4: type = 4;
 					break;
 					
 				}
 			}
 			Keyboard.next();
 		}
 		type = (type + Mouse.getDWheel()/120 - 1)%4 + 1;
 	}
 	
 	private static String getInput(String label, String allowed) {
 		boolean done = false;
 		String input = "";
 		
 		while (!Display.isCloseRequested() && !done) {
 			Renderer.clearGL();
 			lucida.print(0, 0, label + input);
 			Keyboard.poll();
 			while (Keyboard.getNumKeyboardEvents() > 0) {
 				
 				if(Keyboard.getEventKeyState() == true) {
 				
 					String c = ""+Keyboard.getEventCharacter();
 					
 					if(allowed.contains(c)) input += c;
 					
 					switch(Keyboard.getEventKey()) {
 						case Keyboard.KEY_BACK:
 							input = input.length() > 0 ? "" : input.substring(0, input.length()-1);
 							break;
 						case Keyboard.KEY_RETURN: 
 							done = true;
 							break;
 						case Keyboard.KEY_ESCAPE: 
 							return "";
 					}
 				}
 				Keyboard.next();
 			}
 		    GameTime.update();
 		    Renderer.sync();
 		}
 		return input;
 	}
 	
 	private static int MouseFeldX(int x) {
 		return x/Feld.getSize();
 	}
 	
 	private static int MouseFeldY(int y) {
 		return (Display.getHeight() - y)/Feld.getSize();
 	}
 
 	
 	private static void speichern(Feld[][] spielfeld, int width, int height, int px, int py, int p2x, int p2y, String name) throws FileNotFoundException, XMLStreamException {
 		//erstellen der xml-Datei
 		XMLOutputFactory factory = XMLOutputFactory.newInstance();
 		XMLStreamWriter schreiber = factory.createXMLStreamWriter( new FileOutputStream(name));
 		
 		
 		//schreiben der xml-Datei
 		//zunaechst die Groesse des Spielfeldes
 		schreiber.writeStartDocument();
 		schreiber.writeStartElement("Level");
 		schreiber.writeStartElement("Groesse");
 		schreiber.writeAttribute("Breite", String.valueOf(width));
 		schreiber.writeAttribute("Hoehe", String.valueOf(height));
 		schreiber.writeEndElement();
 		
 		//hier die Beschaffenheit der einzelnen Felder
 		schreiber.writeStartElement("Spielfeld");
 		for (int i=0;i<height;i++){
 			schreiber.writeStartElement("Zeile"+String.valueOf(i));
 			for (int j=0;j<width;j++){		
 				if (spielfeld[j][i] instanceof Steinfeld) {schreiber.writeAttribute("X"+String.valueOf(j),"S");}
 				else if (spielfeld[j][i] instanceof Leerfeld) {schreiber.writeAttribute("X"+String.valueOf(j),"L");}
 				else if (spielfeld[j][i] instanceof Mauerfeld) {schreiber.writeAttribute("X"+String.valueOf(j),"M");}
 			}
 			schreiber.writeEndElement();
 		}//end for(i)
 		schreiber.writeEndElement();
 		
 		//festlegen der Startpositionen
 		schreiber.writeStartElement("Startpositionen");
 		
 			schreiber.writeAttribute("p1x", String.valueOf(px));
 			schreiber.writeAttribute("p1y", String.valueOf(py));
 			schreiber.writeAttribute("p2x", String.valueOf(p2x));
 			schreiber.writeAttribute("p2y", String.valueOf(p2y));
 		
 		schreiber.writeEndElement();
 		schreiber.writeEndElement();
 		schreiber.writeEndDocument();
 		schreiber.close();
 	}
 		
 		
 		
 		//Konsistenzpruefungen
 	private static boolean ausgangpruefung(Feld[][] spielfeld) {
 		
 		return true;
 	}
 
 
 	private static boolean startpruefung(int x1, int y1, int x2, int y2, int breit, int hoch) {
 		boolean gefunden=false; //Startposition von Spieler 2 gefunden
 		
 		//position[0]= x-Koordinate Spieler 1, [1] y-S1, [2] x-S2, [3] y-S2
 		if (x2==breit-1-x1)	//vergleicht den Abstand von Spieler 1 zur linken oberen Ecke mit dem Abstand von Spieler 2 mit den anderen Ecken
 			if (y2==hoch-1-y1) gefunden=true;
 		if (x2==breit-1-x1)
 			if (y2==y1) gefunden=true;
 		if (x2==x1)
 			if (y2==hoch-1-y1) gefunden=true;
 		
 		if (gefunden) return true;
 		else {
 			lucida.print(0, 580, "Ein Spieler hat einen Vorteil durch seine Startposition. Bitte Startpositionen korrigieren");
 			return false;
 		}
 		
 	}
 
 
 	private static boolean aufbaupruefung(Feld[][] spielfeld, int breit, int hoch) {
 		//Symmetrie der Spalten pruefen
 		for (int i=1;i<breit/2;i++) {
 			for (int j=0;j<hoch;j++){
 				if ((spielfeld[(breit/2)-i][j] instanceof Steinfeld) && !(spielfeld[(breit/2)+i][j] instanceof Steinfeld)) {return false;}
 			}
 		}
 		//Symmetrie der Zeilen pruefen
 		for (int j=1;j<hoch/2;j++) {
 			for (int i=0;i<breit;i++){
 				if ((spielfeld[i][(hoch/2)-j] instanceof Steinfeld) && !(spielfeld[i][(hoch/2)+j] instanceof Steinfeld )) {return false;}							
 			}
 		}
 		
 		
 		
 		return true;
 	}
 	
 	
 }
 
