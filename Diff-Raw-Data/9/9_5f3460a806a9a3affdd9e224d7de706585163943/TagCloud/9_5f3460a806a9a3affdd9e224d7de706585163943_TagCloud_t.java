 import java.util.ArrayList;
 
 import processing.core.*;
 import wordcram.*;
 import wordcram.text.StopWords;
 
 public class TagCloud extends GuiElement{
 	
 	public ArrayList<wordcram.Word> wordList;
 	public wordcram.Word[] w;
 	
 	public WordCram wc;
 	PGraphics buffer;
 	
 	public int plotWidth;
 	public int infoWidth = 100;
 	
 	public Character character;
 	
 	public TagCloud(Character c, int xValue, int yValue, int WEIGHT, int HEIGHT) {
 	
 		x = xValue;
 		y = yValue;
 		
 		width = WEIGHT;
 		height = HEIGHT;
 				
 		plotWidth = width - infoWidth; // Need space for icons and stat
 		
 		character = c;
 		
 		wordList = new ArrayList<wordcram.Word>();
 		
 		setTagCloud();
 
 	}
 	
 	public void setTagCloud() {
 		int i = Parser.findCharacter(character.getName()); //Find index of the character based on String name.
 
 		ArrayList<Word> myList = Parser.ALL_CHARACTERS.get(i).getWordRange(GLOBAL.episodeStart.getSeason(), GLOBAL.episodeStart.getEpisode(), GLOBAL.episodeEnd.getSeason(), GLOBAL.episodeEnd.getEpisode());
 
 		int max = 60;  
 
 		System.out.println(myList.size());
 		
 		if(myList.size() == 0) {
 			w = new wordcram.Word[1];
 			w[0] = new wordcram.Word("No Words Said in Range", 1);
 			max = 1;
 		}
 			
 		else { 
 			//Set max to a lower number if array size is less than max
 			if(myList.size() < max) { max = myList.size(); }
 			w = new wordcram.Word[max];
 			
 			//Parse your new ArrayList
 			for(int x=0; x<max; ++x) {
 				
 				String aWord = myList.get(x).getWord();  
 				int itsWeight = myList.get(x).getWeight();
 				
 				wordList.add( new wordcram.Word(aWord,itsWeight) );
 				//System.out.println("Added Word " + aWord + " - Weight " + itsWeight);
 				
 				w[x] = new wordcram.Word(aWord,itsWeight);
 				
 			}
 		}
 
 		buffer = GLOBAL.processing.createGraphics(plotWidth, height, GLOBAL.processing.JAVA2D);
 		buffer.beginDraw();
 		buffer.background(GLOBAL.colorTagCloudBackground);
 				
 		wc = new WordCram(GLOBAL.processing)
 					.fromWords(w)
 					.withCustomCanvas(buffer)
 					.withColors(GLOBAL.COLORS.getArray())
 					.sizedByWeight(6,60).withWordPadding(3)
 					.withAngler(Anglers.horiz())
 					.withPlacer(Placers.centerClump())
 					.maxNumberOfWordsToDraw(max)
 					.withStopWords(StopWords.ENGLISH)
 					.maxAttemptsToPlaceWord(10000);
 	}
 
 	public void draw() {
 		//Draw Title
		GLOBAL.processing.fill(GLOBAL.colorText);
 		GLOBAL.processing.textFont(GLOBAL.tFont,14);
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.text(character.getName_firstToUppercase()
 								+ "'s Common Words from S" + GLOBAL.episodeStart.getSeason()
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
 		
 		
 		GLOBAL.processing.noStroke();
 		GLOBAL.processing.rectMode(GLOBAL.processing.CORNERS);
 		GLOBAL.processing.fill(GLOBAL.colorPlotArea);
 		GLOBAL.processing.rect( x,y, x + plotWidth, y + height);
 		GLOBAL.processing.fill(GLOBAL.processing.color(255));
 
 		if (wc.hasMore()) {  
 			wc.drawNext();		
 		}
 		else {
 			buffer.endDraw();
 			GLOBAL.processing.image(buffer, x, y);
 			//System.out.println(wc.getSkippedWords().length);
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
 	
 	
 	public void changePosition(int xValue, int yValue, int w, int l) {
 		
 		x = xValue;
 		y = yValue;
 		
 		width = w;
 		height = l;
 		
 		plotWidth = width - infoWidth;
 		
 		buffer.width = plotWidth;
 		buffer.height = height;
 		
 		setTagCloud();
 		
 	}
 
 }
