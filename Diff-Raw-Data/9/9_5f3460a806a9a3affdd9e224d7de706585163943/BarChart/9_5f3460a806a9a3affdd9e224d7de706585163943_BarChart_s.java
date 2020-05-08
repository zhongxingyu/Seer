 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 public class BarChart extends GuiElement{
 	
 	// Define
 	public int CHARACTER_GRAPH = 0;
 	public int EPISODE_GRAPH = 1;
 	public int SEASON_GRAPH = 2;
 	
 	public int GRAPH_TYPE;
 	
 	public Character character;
 	public Episode episode;
 	public Integer season;
 	
 	public int plotWidth;
 	public int infoWidth;
 	
 	public float rectWidth;
 		
 	public BarChart(Character c, int xValue, int yValue, int w, int l, int TYPE) {
 		
 		x = xValue;
 		y = yValue;
 		
 		width = w;
 		height = l;
 		
 		infoWidth = 100;
 		
 		plotWidth = width - infoWidth; // Need space for icons and stat
 		
 		character = c;
 		
 		GRAPH_TYPE = TYPE;
 		
 	}
 	
 	public BarChart(Episode e, int xValue, int yValue, int w, int l, int TYPE) {
 		
 		x = xValue;
 		y = yValue;
 		
 		width = w;
 		height = l;
 		
 		plotWidth = width;
 		
 		episode = e;
 		
 		GRAPH_TYPE = TYPE;
 	}
 	
 	public BarChart(int s, int xValue, int yValue, int w, int l, int TYPE) {
 		
 		x = xValue;
 		y = yValue;
 		
 		width = w;
 		height = l;
 		
 		plotWidth = width;
 		
 		season = new Integer(s);
 		
 		GRAPH_TYPE = TYPE;
 		
 	}
 	
 	public void changePosition(int xValue, int yValue, int w, int l) {
 		
 		x = xValue;
 		y = yValue;
 		
 		width = w;
 		height = l;
 		
 	}
 	
 	public void draw() {	
 
 		GLOBAL.processing.noStroke();
 		GLOBAL.processing.rectMode(GLOBAL.processing.CORNERS);
 		GLOBAL.processing.fill(GLOBAL.colorPlotArea);
 		GLOBAL.processing.rect( x,y, x + plotWidth, y + height);
 		GLOBAL.processing.fill(GLOBAL.processing.color(255));
 		
 		if (GRAPH_TYPE == CHARACTER_GRAPH) {
 				
 			if (GLOBAL.CATCHPHRASES_ANALYSIS == false)
 				createDialogueBarChart();
 			else
 				createCatchphraseBarChart();
 			
 		}
 		
 		else if (GRAPH_TYPE == EPISODE_GRAPH) {
 			
 			drawYLabel(0,90,5,10, "");
 			
 			ArrayList<Character> charactersInEpisode = new ArrayList<Character>();
 			
 			// For each character, add to the charactersInEpisode only the one that appears
 			for(int i =0; i < Parser.ALL_CHARACTERS.size(); i++) {
 				if (episode.getNumberOfLinesPerCharacter(Parser.ALL_CHARACTERS.get(i)) > 0)
 					charactersInEpisode.add(Parser.ALL_CHARACTERS.get(i));
 			}		
 			
 			// Width of a single bar
 			rectWidth = (float)(width)/(2*charactersInEpisode.size());
 
 			float barY = y + height;
 			float value;
 			
 			GuiElement rolloverRect = new GuiElement();
 			
 			// For each character that appears, plot the bar
 			for(int i =0; i < charactersInEpisode.size(); i++) {
 				
 				value = episode.getNumberOfLinesPerCharacter(charactersInEpisode.get(i));
 				barY = GLOBAL.processing.map(value, 0, 90, y + height, y);					 // TODO 100 is a default value, must be set as the max	
 				float barX = GLOBAL.processing.map(i, 0, charactersInEpisode.size() - 1, x + rectWidth, x + width - rectWidth);
 				
 				GLOBAL.processing.rectMode(GLOBAL.processing.CORNERS);
 				GLOBAL.processing.fill(charactersInEpisode.get(i).getColor());//GLOBAL.processing.color(255));
 				GLOBAL.processing.rect( barX - rectWidth/2, barY, barX+ rectWidth/2, y + height);
 				
 				// Draw the character name
 				String label = charactersInEpisode.get(i).getName_firstToUppercase();
 				GLOBAL.processing.fill(GLOBAL.colorText);
 				GLOBAL.processing.textFont(GLOBAL.tFont,8);
 				GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 				GLOBAL.processing.text(label.replace(" ", "\n").replace("-", "-\n"), barX , y + height + 15);
 				
 			}
 			
 			// Draw info	
 			GLOBAL.processing.fill(GLOBAL.colorText);
 			GLOBAL.processing.textFont(GLOBAL.tFont,12);
 			GLOBAL.processing.textAlign(GLOBAL.processing.LEFT);
 			GLOBAL.processing.text("\"" + episode.getName() + "\" Character Appearences (# of Lines Spoken)", x + 10, y - 5);
 			GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 			GLOBAL.processing.text("Season n. " + episode.getSeason(), x + width + 55, y + 20);
 			GLOBAL.processing.text("Episode n. " + episode.getEpisode(), x + width + 55, y + 40);
 			GLOBAL.processing.text("Airdate:", x + width + 55, y + 70);
 			GLOBAL.processing.text(episode.getAirDate(), x + width + 55, y + 90);
 			GLOBAL.processing.text("Network:", x + width + 55, y + 120);
 			GLOBAL.processing.text(episode.getNetwork(), x + width + 55, y + 140);
 			
 			// Search for rollover functions
 			for(int i =0; i < charactersInEpisode.size(); i++) {
 				float barX = GLOBAL.processing.map(i, 0, charactersInEpisode.size() - 1, x + rectWidth, x + width - rectWidth);
 
 				// Check for any mouse rollover functionality to be displayed
 				if (GLOBAL.processing.mouseX > (barX - rectWidth/2) && GLOBAL.processing.mouseX < (barX + rectWidth) 
 						&& GLOBAL.processing.mouseY > y && GLOBAL.processing.mouseY < (y  + height)) {					
 					rolloverRect.x = (int)(barX - rectWidth/2);
 					rolloverRect.y = (int)(y);
 					rolloverRect.width = (int)(rectWidth);
 					rolloverRect.height = (int)(height);
 					rolloverRect.draw();
 					mouseRolloverFunction(charactersInEpisode.get(i));
 					
 				}
 				
 			}		
 			
 			
 		} // end if
 		
 		else if (GRAPH_TYPE == SEASON_GRAPH) {
 			
 			drawYLabel(0,100,5,10,"%");
 
 			// Width of a single bar
 			rectWidth = (float)(width)/(2*Parser.ALL_CHARACTERS.size());
 
 			float barY = y + height;
 			float value;
 			
 			String c = " ";
 			
 			// For each character
 			for(int i =0; i < Parser.ALL_CHARACTERS.size(); i++) {
 
 				switch ( season.intValue() ) {
 				case 0 : 
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodes() / Parser.LIST_ALL.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				case 1 :
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodesBySeason(1) / Parser.LIST_S1.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				case 2 :
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodesBySeason(2) / Parser.LIST_S2.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				case 3 :
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodesBySeason(3) / Parser.LIST_S3.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				case 4 :
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodesBySeason(4) / Parser.LIST_S4.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				case 5 :
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodesBySeason(5) / Parser.LIST_S5.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				case 6 :
 					value = 100* Parser.ALL_CHARACTERS.get(i).getTotalEpisodesBySeason(6) / Parser.LIST_S6.size();
 					barY = GLOBAL.processing.map(value, 0, 100, y + height, y);
 					break;
 				}
 								
 				float barX = GLOBAL.processing.map(i, 0, Parser.ALL_CHARACTERS.size() - 1, x + rectWidth, x + width - rectWidth);
 				GLOBAL.processing.rectMode(GLOBAL.processing.CORNERS);
 				GLOBAL.processing.fill(Parser.ALL_CHARACTERS.get(i).getColor());
 				GLOBAL.processing.rect( barX - rectWidth/2, barY, barX+ rectWidth/2, y + height);
 				
 				if (Parser.ALL_CHARACTERS.get(i).getName_firstToUppercase().toCharArray()[0] != c.toCharArray()[0]) {
 					c = Parser.ALL_CHARACTERS.get(i).getName_firstToUppercase().substring(0, 1);
 					GLOBAL.processing.fill(GLOBAL.colorText);
 					GLOBAL.processing.textFont(GLOBAL.tFont,10);
 					if (!c.equals("E") && !c.equals("Q"))
 						GLOBAL.processing.textAlign(GLOBAL.processing.LEFT);
 					else
 						GLOBAL.processing.textAlign(GLOBAL.processing.RIGHT);
 					GLOBAL.processing.text(c.toUpperCase(), barX , y + height + 15);
 				}
 				
 			}
 			
 			// Draw info	
 			GLOBAL.processing.fill(GLOBAL.colorText);
 			GLOBAL.processing.textFont(GLOBAL.tFont,12);
 			GLOBAL.processing.textAlign(GLOBAL.processing.LEFT);
 			if(season.intValue() != 0)
 				GLOBAL.processing.text("Season " + season.intValue() + " Character Appearences (%)", x + 10, y - 5);
 			else
 				GLOBAL.processing.text("Character Appearences Over All Seasons", x + 10, y - 5);
 			
 			// Rollover
 			for(int i =0; i < Parser.ALL_CHARACTERS.size(); i++) {
 				float barX = GLOBAL.processing.map(i, 0, Parser.ALL_CHARACTERS.size() - 1, x + rectWidth, x + width - rectWidth);
 				// Check for any mouse rollover functionality to be displayed
 				if (GLOBAL.processing.mouseX > (barX - rectWidth/2) && GLOBAL.processing.mouseX < (barX + rectWidth) 
 						&& GLOBAL.processing.mouseY > y && GLOBAL.processing.mouseY < (y  + height)) {	
 					intheyear2525.graphArea.mouseSeasonRolloverFunction(rectWidth, barX, Parser.ALL_CHARACTERS.get(i));
 					mouseRolloverFunction(Parser.ALL_CHARACTERS.get(i));
 				}
 			}
 		} // end if
 		
 	}
 
 	// Dialogue analysis for character
 	public void createDialogueBarChart() {
 		//Draw Title
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.textFont(GLOBAL.tFont,14);
 		GLOBAL.processing.text("Number of Lines Spoken by " 
 								+ character.getName_firstToUppercase()
 								+ " from S" + GLOBAL.episodeStart.getSeason()
 								+ "E" + GLOBAL.episodeStart.getEpisode()
 								+ " to S" + GLOBAL.episodeEnd.getSeason()
 								+ "E" + GLOBAL.episodeEnd.getEpisode(), 
 								x + 320, y - 4);
 		
 		//Draw instructions for slider-bar
 		GLOBAL.processing.textAlign(GLOBAL.processing.LEFT);
 		GLOBAL.processing.textFont(GLOBAL.tFont,12);
 		GLOBAL.processing.fill(GLOBAL.processing.color(136,204,238));
 		GLOBAL.processing.text("Drag either end of bar to add/remove episodes. Drag middle of bar to move selection.", 
 								335, 760);
 		
 		float barY = y + height;
 		float value;
 
 		int indexStart = 0;
 		int indexEnd = Parser.LIST_ALL.size() -1;
 		
 		drawYLabel(0, 90, 5, 10,"");
 
 		for (int i = 0; i< Parser.LIST_ALL.size() ; i++) {
 			if (Parser.LIST_ALL.get(i).getSeason() == GLOBAL.episodeStart.getSeason() && Parser.LIST_ALL.get(i).getEpisode() == GLOBAL.episodeStart.getEpisode())
 				indexStart = i;
 			if (Parser.LIST_ALL.get(i).getSeason() == GLOBAL.episodeEnd.getSeason() && Parser.LIST_ALL.get(i).getEpisode() == GLOBAL.episodeEnd.getEpisode())
 				indexEnd = i;
 		}
 
 		rectWidth = (float)(width)/(2*(indexEnd - indexStart));
 		GLOBAL.COLORS.reset();
 		for ( int i = indexStart; i <= indexEnd; i++ ) {
 			//GLOBAL.processing.stroke(GLOBAL.COLORS.getNextColor());
 			GLOBAL.processing.fill(GLOBAL.COLORS.getNextColor());
 			
 			value = Parser.LIST_ALL.get(i).getNumberOfLinesPerCharacter(character);
 			barY = GLOBAL.processing.map(value, 0, 90, y + height, y);					 // TODO 100 is a default value, must be set as the max	
 			float barX = GLOBAL.processing.map(i, indexStart, indexEnd, x + rectWidth, x + plotWidth - rectWidth);
 			GLOBAL.processing.rect( barX - rectWidth/2, barY, barX + rectWidth/2, y + height);
 
 			// Check for any mouse rollover functionality to be displayed
 			if (GLOBAL.processing.mouseX > (barX - rectWidth/2) && GLOBAL.processing.mouseX < (barX + rectWidth) 
 					&& GLOBAL.processing.mouseY > y && GLOBAL.processing.mouseY < (y  + height)) {
 				String label = "S"+ Parser.LIST_ALL.get(i).getSeason() + " E" + Parser.LIST_ALL.get(i).getEpisode()+ " " + Parser.LIST_ALL.get(i).getName();
 				intheyear2525.graphArea.mouseCharacterRolloverFunction(rectWidth, barX, label);
 				
 			}
 		}
 		
 		// Draw icon and info		
 		GLOBAL.processing.fill(GLOBAL.colorText);
 		GLOBAL.processing.textFont(GLOBAL.tFont,16);
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.text(character.getName_firstToUppercase(), x + width - 40, y + 20);
 		
 		//Display most appeared with
 		GLOBAL.processing.textFont(GLOBAL.tFont,14);
 		GLOBAL.processing.text("Appears Most With:", x + width - 38, y + 140);
 		GLOBAL.processing.text(character.getMostAppearence(), x + width - 38, y + 155);
 
 		if (character.getIcon() != null)
 			GLOBAL.processing.image( character.getIcon(), x + width - 80, y + 30, 80, 80);
 	}
 	
 
 	public void createCatchphraseBarChart() {
 		//Draw Title
 		GLOBAL.processing.textFont(GLOBAL.tFont,14);
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.text("Catchphrases said by " 
 								+ character.getName_firstToUppercase()
 								+ " from S" + GLOBAL.episodeStart.getSeason()
 								+ "E" + GLOBAL.episodeStart.getEpisode()
 								+ " to S" + GLOBAL.episodeEnd.getSeason()
 								+ "E" + GLOBAL.episodeEnd.getEpisode(), 
 								x + 320, y - 4);
 		
 		//Draw instructions for slider-bar
 		GLOBAL.processing.textAlign(GLOBAL.processing.LEFT);
 		GLOBAL.processing.textFont(GLOBAL.tFont,12);
 		GLOBAL.processing.fill(GLOBAL.processing.color(136,204,238));
 		GLOBAL.processing.text("Drag either end of bar to add/remove episodes. Drag middle of bar to move selection.", 
 								335, 760);
 		
 		float rectHeight = y + height;
 		float value;
 
 		int indexStart = 0;
 		int indexEnd = Parser.LIST_ALL.size() -1;
 		
 		drawYLabel(0, 5, 1, 1,"");
 
 		for (int i = 0; i< Parser.LIST_ALL.size() ; i++) {
 			if (Parser.LIST_ALL.get(i).getSeason() == GLOBAL.episodeStart.getSeason() && Parser.LIST_ALL.get(i).getEpisode() == GLOBAL.episodeStart.getEpisode())
 				indexStart = i;
 			if (Parser.LIST_ALL.get(i).getSeason() == GLOBAL.episodeEnd.getSeason() && Parser.LIST_ALL.get(i).getEpisode() == GLOBAL.episodeEnd.getEpisode())
 				indexEnd = i;
 		}
 
 		rectWidth = (float)(width)/(2*(indexEnd - indexStart));
 		GLOBAL.COLORS.reset();
 		
 		for ( int i = indexStart; i <= indexEnd; i++ ) {
 			
 			value = 0;
 			ArrayList<Catchphrase> phrases = character.getAllPhrases();
 			HashMap<Catchphrase, Integer> stringPhrase  = new HashMap();
 			
 			// If character doesn't have any catchphrase, print and go to the next character
 			if (phrases == null) {
 				GLOBAL.processing.fill(character.getColor());
 				GLOBAL.processing.textFont(GLOBAL.tFont,14);
 				GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 				GLOBAL.processing.text(character.getName_firstToUppercase() + " doesn't have any catchphrases", x + plotWidth/2, y + height/2);
 				break; 
 			}
 			
 			// for each phrase
 			for (int j=0; j < phrases.size(); j++) {
 				int numberOfTime = phrases.get(j).getTotalEpisode(Parser.LIST_ALL.get(i).getSeason(), Parser.LIST_ALL.get(i).getEpisode());
 				if ( numberOfTime > 0){
 					value += numberOfTime;
 				    stringPhrase.put(phrases.get(j), new Integer((int)numberOfTime));
 				}
 			}
 			
 			float barX = GLOBAL.processing.map(i, indexStart, indexEnd, x + rectWidth, x + plotWidth - rectWidth);
 			
 			int blockNumber = 0;
 			
 			// Plot a elementary rect for each match of each catchphrase (ex: "beer" 2 -> plot 2 rect )
 			for(Entry<Catchphrase, Integer> entry : stringPhrase.entrySet()) {
 				
 				for ( int k = 0 ; k < entry.getValue(); k++) {
 					rectHeight = GLOBAL.processing.map(1, 0, 5, 0, height);					 
 					GLOBAL.processing.fill(entry.getKey().getColor());
 					GLOBAL.processing.rect( barX - rectWidth/2, y + height - blockNumber*rectHeight, barX + rectWidth/2, y + height - rectHeight - (blockNumber*rectHeight));
 					blockNumber++;
 				}
 			}
 
 		}
 		
 		// Rollover
 		for ( int i = indexStart; i <= indexEnd; i++ ) {
 			
 			
 			value = 0;
 			ArrayList<Catchphrase> phrases = character.getAllPhrases();
 			HashMap<Catchphrase, Integer> stringPhrase  = new HashMap();
 			
 			float barX = GLOBAL.processing.map(i, indexStart, indexEnd, x + rectWidth, x + plotWidth - rectWidth);
 			
 			// Check for any mouse rollover functionality to be displayed
 			if (GLOBAL.processing.mouseX > (barX - rectWidth/2) && GLOBAL.processing.mouseX < (barX + rectWidth) 
 					&& GLOBAL.processing.mouseY > y && GLOBAL.processing.mouseY < (y  + height)) {
 				String label = "S"+ Parser.LIST_ALL.get(i).getSeason() + " E" + Parser.LIST_ALL.get(i).getEpisode()+ " " + Parser.LIST_ALL.get(i).getName();
 				intheyear2525.graphArea.mouseCharacterRolloverFunction(rectWidth, barX, label);
 				
 				// If character doesn't have any catchphrase, print and go to the next character
 				if (phrases == null) 
 					break;
 				
 				// for each phrase
 				for (int j=0; j < phrases.size(); j++) {
 					int numberOfTime = phrases.get(j).getTotalEpisode(Parser.LIST_ALL.get(i).getSeason(), Parser.LIST_ALL.get(i).getEpisode());
 					if ( numberOfTime > 0){
 						value += numberOfTime;
 					    stringPhrase.put(phrases.get(j), new Integer((int)numberOfTime));
 					}
 				}
 								
 				int numElement = 0;
 				
 				for(Entry<Catchphrase, Integer> entry : stringPhrase.entrySet()) {
 					// Draw the rect
 					GLOBAL.processing.noStroke();
 					GLOBAL.processing.rectMode(GLOBAL.processing.CORNERS);
 					GLOBAL.processing.fill(GLOBAL.colorPlotArea);
 					if (GLOBAL.processing.mouseX > x + plotWidth/2)
 						GLOBAL.processing.rect(GLOBAL.processing.mouseX - 100 - GLOBAL.processing.textWidth(entry.getKey().getPhrase()) , GLOBAL.processing.mouseY - 8 - numElement*20 ,
 								GLOBAL.processing.mouseX - 2, GLOBAL.processing.mouseY - 8 - (numElement + 1)*20);
 					else
 						GLOBAL.processing.rect(GLOBAL.processing.mouseX + 100 + GLOBAL.processing.textWidth(entry.getKey().getPhrase()) , GLOBAL.processing.mouseY - 8 - numElement*20 ,
 								GLOBAL.processing.mouseX + 2, GLOBAL.processing.mouseY - 8 - (numElement + 1)*20);
 					GLOBAL.processing.fill(GLOBAL.processing.color(255));
 					
 					// Draw the catchphrases
 					GLOBAL.processing.fill(GLOBAL.colorText);
 					GLOBAL.processing.textFont(GLOBAL.tFont,14);
 					if (GLOBAL.processing.mouseX > x + plotWidth/2) {
 						GLOBAL.processing.textAlign(GLOBAL.processing.RIGHT);
 						GLOBAL.processing.text(entry.getKey().getPhrase() + " = " + entry.getValue() , GLOBAL.processing.mouseX - 8, GLOBAL.processing.mouseY - 11 -numElement*20);
 					}
 					else {
 						GLOBAL.processing.textAlign(GLOBAL.processing.LEFT);
 						GLOBAL.processing.text(entry.getKey().getPhrase() + " = " + entry.getValue() , GLOBAL.processing.mouseX + 8, GLOBAL.processing.mouseY - 11 -numElement*20);
 					}
 						
 					numElement++;
 				}
 			}
 		}
 
 		// Draw icon and info		
 		GLOBAL.processing.fill(GLOBAL.colorText);
 		GLOBAL.processing.textFont(GLOBAL.tFont,16);
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.text(character.getName_firstToUppercase(), x + width - 40, y + 20);
 		
 		//Display most appeared with
 		GLOBAL.processing.textFont(GLOBAL.tFont,14);
 		GLOBAL.processing.text("Appears Most With:", x + width - 38, y + 140);
 		GLOBAL.processing.text(character.getMostAppearence(), x + width - 38, y + 155);
 
 		if (character.getIcon() != null)
 			GLOBAL.processing.image( character.getIcon(), x + width - 80, y + 30, 80, 80);
 		
 	}
 	
 	public void drawYLabel(float minValue, float maxValue, int minInterval, int maxInterval, String s){
 		GLOBAL.processing.fill(GLOBAL.colorText);
 		GLOBAL.processing.textFont(GLOBAL.tFont,10);
 		GLOBAL.processing.stroke(GLOBAL.colorLinesLabelY);
 		GLOBAL.processing.strokeWeight(1);
 		
 		float min = minValue;
 		float max = maxValue;
 		
 		int yIntervalMinor = minInterval;
 		int yIntervalMayor = maxInterval;
 		
 		int plotX1 = x;
 		int plotX2 = x + plotWidth;
 		
 		for (float v = min; v <= max; v += yIntervalMinor) {
 
 			if (v % yIntervalMinor == 0) {     // If a tick mark
 				float y = GLOBAL.processing.map(v, min, max, this.y + height, this.y);  
 				if (v % yIntervalMayor == 0) {        // If a major tick mark
 					float textOffset = GLOBAL.processing.textAscent()/3;  // Center vertically
 					if (v == min) {
 						textOffset = 0;                   // Align by the bottom
 					} 
 					else if (v == max) {
 						textOffset = GLOBAL.processing.textAscent() - 4;        // Align by the top
 					}
 					GLOBAL.processing.textAlign(GLOBAL.processing.RIGHT);
 					GLOBAL.processing.text(GLOBAL.processing.floor(v) + s, plotX1 - 8, y + textOffset);
 					if (v % maxInterval == 0) {
 						GLOBAL.processing.line(plotX1 - 4, y, plotX2, y);
 					}
 					else {
 						GLOBAL.processing.line(plotX1 - 4, y, plotX1, y);     // Draw major tick
 					}
 				} 
 				else {
 					GLOBAL.processing.line(plotX1 - 2, y, plotX1, y);   // Draw minor tick
 				}
 			}
 		}
 		
 		GLOBAL.processing.noStroke();
 
 	    	
 	}
 	
 	public void mouseRolloverFunction(Character character) {
 		
 		// Draw a rectangle, the label and an image inside, now the image is set to 100x100
 		
 		int x1 = GLOBAL.processing.mouseX + 10;
 		
 		if (x1 > x + plotWidth/2)
 			x1 = x1 - 150;
 		
 		// Rectangle
 		GLOBAL.processing.noStroke();
 		GLOBAL.processing.rectMode(GLOBAL.processing.CORNER);
 		GLOBAL.processing.fill(GLOBAL.colorIconBackground);
 		GLOBAL.processing.rect( x1, GLOBAL.processing.mouseY - 40 - 120 , 100, 130); //x,y,width,height
 		
 		// Image
 		if (character.getIcon()!= null)
 			GLOBAL.processing.image(character.getIcon(), x1, GLOBAL.processing.mouseY - 10 - 100, 100,100);
 		
 		// Text
 		GLOBAL.processing.fill(GLOBAL.colorBackgroundLayerTwo);
 		GLOBAL.processing.textFont(GLOBAL.tFont,14);
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.text(character.getName_firstToUppercase().replace(" ", "\n").replace("-", "-\n"), x1 + 50, GLOBAL.processing.mouseY - 18 - 120); // center in the upper side, middle point, of the icon 100x100
 		
 	}
 
 }
