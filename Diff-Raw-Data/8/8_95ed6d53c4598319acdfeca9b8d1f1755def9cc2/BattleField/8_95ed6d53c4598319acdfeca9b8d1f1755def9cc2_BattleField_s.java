 /**
  * BattleField is a class describing the battlefield (that is to say, the game screen). It
  * can be broadly described as a matrix: every element of this matrix will contain an alien,
  * a gun, a shot, a casemate or will be empty. An instance of BattleField represents a
  * snapshot of the current battlefield configuration.
  */
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 import java.util.StringTokenizer;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 
 public class BattleField {
 
 	//FIELD
 	
 	protected int gunCounter=0;
 	protected BattleFieldElement[][] battlefield;  //do not have to containt any null value
 	protected static int rows;
 	protected static int columns;						
 	private String filename;						//name of the file where the configurations used (saved,restored...)
 	protected static int score;
 	protected static int life;
 	protected static boolean dead =	false;
 	// CONSTRUCTOR
 	/**
 	 * Constructor for the BattleField element
 	 * 
 	 * @throws IllegalElementException, IllegalPositionException
 	 */
 	public BattleField(String filename) throws IllegalElementException, IllegalPositionException {
 		score = 0;
 		life = 10;
 		setFilename(filename);
 		reload();
 		
 	}
 	
 	 public void playSound(String s) {
 		    try {        
 		        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("./src/sounds/"+s).getAbsoluteFile());
 		        Clip clip = AudioSystem.getClip();
 		        clip.open(audioInputStream);
 		        clip.start();
 		    } catch(Exception e){
 		        System.out.println("Error sound in BattleField.java");
 		        e.printStackTrace();
 		    }
 		}
 	
 	public int getScore(){
 		return this.score;
 	}
 	
 	public int getLife(){
 		return this.life;
 	}
 	//METHODS
 	/**
 	 * getFilename method.  A method to get the filename.
 	 * 
 	 * @return filename - the filename
 	 */
 	public String getFilename() {				
 		return this.filename;					
 	}
 					
 	/**
 	 * setFilename method.  A method to set the filename
 	 * 
 	 * @param s - a String for the filename
 	 */
 	public void setFilename(String s) {			
 		this.filename= s;						
 	}
 	 
 	/*
 	 *getRows method.  A method to return the rows
 	 *
 	 * @return rows - returns the number of rows
 	 */
 	public static int getRows() {
 		return rows;
 	}
 
 	/**
 	 * setRows method.  A method to set the Rows.
 	 * 
 	 * @param rows - int for the number of rows
 	 */
 	public static void setRows(int rows) {
 		BattleField.rows = rows;
 	}
 
 	/**
 	 * getColumns method. Method to get the Columns.
 	 * 
 	 * @return columns - returns the number of columns.
 	 */
 	public static int getColumns() {
 		return columns;
 	}
 
 	/**
 	 * setColumns method - Method to set the columns.
 	 * 
 	 * @param columns - int for the number of columns.
 	 */
 	public static void setColumns(int columns) {
 		BattleField.columns = columns;
 	}
 
 	/**
 	 * getBattleFieldElement method.  Gets the vertical and horizontal position.
 	 * 
 	 * @param v - int for vertical
 	 * @param h - int for horizontal
 	 * @return battlefield - returns the vertical and horizontal position of the BattleFieldElement
 	 */
 	public BattleFieldElement getBattleFieldElement(int v, int h){
 		return battlefield[v][h];						//what if the position has been forced to null before the call? Manage it here or somewhere else?
 	}										
 
 	public BattleFieldElement[][] getBattlefieldMatrix() {
 		return battlefield;
 	}
 
 	/**
 	 * toString method.  Returns getbattleField
 	 * 
 	 * @return getBattleField - returns the Battlefield
 	 */
 	public String toString(){	
 		return getBattleField();									
 	}									
 
 	/**
 	 * backup method.  Attempts to backup the file.
 	 * 
 	 * @param file - String of the filename.
 	 */
 	public void backup(String file) {
 		File f = new File (file); 
 			try {
 			    FileWriter fw = new FileWriter (f);
 				String backup = this.getBattleField();
 			    fw.write(backup);
 			    fw.close();
 			} catch (IOException exception) {
 			    System.out.println ("Impossible to create the backup file !");
 			}
 	}										
 
 	/**
 	 * getBattleField method.  Returns Encode+itemCounter + item + "$"
 	 * @return Encode+itemCounter + item + "$"
 	 */
 	public String getBattleField() {
 		int itemCounter=0; 
 		String item = this.battlefield[0][0].toString();												
 		String Encode = this.battlefield.length + "|" + this.battlefield[0].length + "|";				//Encode start with  #rows | #column |
 		for (int v = 0 ; v < rows; v++) {
 			if(v>0) {
 				Encode=Encode+itemCounter+item+"$";											//add $ when newLine
 				item = this.battlefield[v][0].toString();
 				itemCounter=0;
 			}
 			for (int h = 0; h < columns; h++) {
 				if(item.equals(this.battlefield[v][h].toString())) {								//if last item and this one are of the same class
 					if(itemCounter==9){															// if is the 10th then write on the Encode string
 						Encode=Encode + itemCounter + item;
 						itemCounter=1;
 					}	
 					else																		//else increment the counter
 						itemCounter++;
 				} else {																	//else write on the string	
 					Encode = Encode + itemCounter + item;
 					itemCounter = 1;
 					item = this.battlefield[v][h].toString();
 				}//end if-else	
 			}//end for(columns)	
 		}//end for(rows)		
 		
 		return Encode+itemCounter + item + "$";
 	}
 
 	/**
 	 * write method.  Appends the current configuration to the current content of the file.
 	 */
 	public void write() {					// append the current configuration to the current content of the file;
 		
 		BufferedWriter bw = null;
 		
 		try {
 			bw = new BufferedWriter(new FileWriter(this.filename, true));
 			bw.newLine();
 			bw.write(this.getBattleField()+"\n");
 			bw.close();
 		} catch (IOException e) {
 			System.out.println("IOException in the write method !");
 		} 
 
 	}
 
 	/**
 	 * reload method.  Loads the last battlefield configuration.
 	 * 
 	 * @throws IllegalElementException
 	 * @throws IllegalPositionException
 	 */
 	public void reload() throws IllegalElementException, IllegalPositionException {		// read again from filename the last-configuration of the battlefield 																									
 		FileReader fr = null;
 		BufferedReader bf = null;
 		try {
 			fr = new FileReader(this.filename);
 			bf = new BufferedReader(fr);
 			String str = "", line = "";
 
 			while (str != null) {
 				line = str;
 				str = bf.readLine();
 			}
 			setBattleField(line); //Send to setBattleField method, the lastest configuration of the file
 		
 			bf.close();
 			fr.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found in the reload method!");
 		} catch (IOException e) {
 			System.out.println("IOException in the reload method!");
 		} 
 	}
 		
 	/**
 	 * clone method. Clones an object
 	 * 
 	 * @throw CloneNotSupportedException
 	 */
 	/*public Object clone() throws CloneNotSupportedException {			//it should be modified, it returns an Object, not just a BattleFieldElement (e.g. clone a whole battlefield)
 		 BattleFieldElement bf = null;
 		    try {
 	        	bf = (BattleFieldElement) super.clone();
 	        } catch(CloneNotSupportedException e) {
 	            System.out.println("Clone method not supported!");
 	        }
 	        bf = (BattleFieldElement)this.clone();
 	        return bf;
 	}*/											
 
 	/**
 	 * setBattleFieldElement.  Checks that only the red spacecraft is in the top row
 	 * and the gun in the bottom row, or that they are empty
 	 * @param v - int vertical
 	 * @param h - int horizontal
 	 * @param b - BattleFieldElement
 	 * @throws IllegalElementException
 	 * @throws IllegalPositionException
 	 */
 	void setBattleFieldElement(int v, int h, BattleFieldElement b) throws IllegalElementException, IllegalPositionException {
 		
 		//check if something different from a Gun is placed in the bottom row
 	    if ((v == rows-2) && (!b.toString().equals("G")) && (!b.toString().equals(" ")) && (!b.toString().equals("S")) ) 
 	  		throw new IllegalElementException("Only Gun, AlienShot or Empty cells can be placed in the bottom row");
 		//check if something different from a RedSpacecraft is placed in the top row
 		if ((v == 0) && (!b.toString().equals("R"))  && ((!b.toString().equals(" "))) && (!b.toString().equals("s")) ) 
 	  		throw new IllegalElementException("Only a RedSpacecraft, Gunshot or Empty Cells can be placed in the top row");
 		
 	//	if ((v == rows-1) && (!b.toString().equals("G") ) )
 	  //		throw new IllegalElementException("Only casemate there");
 			
 			switch(b.toString().charAt(0)){
 				case 'R':	if(v != 0) {
 								throw new IllegalPositionException("RedSpacecraft cannot be placed in line "+v);
 							}
 							battlefield[v][h] = b;
 							break;
 						  
 				case 'G':	 if(gunCounter>0) {
 								throw new IllegalElementException("Only one Gun per BattleField");
 							}
 							if(v != rows-2) {
 								throw new IllegalPositionException("The Gun must be placed in the bottom line of the BattleField");
 							}
 							gunCounter++;
 							battlefield[v][h]= b;
 							break;
 						
 				case 'C': 	if(v!=rows-1) {
 								throw new IllegalPositionException("Casemates can just be placed in bottom line");
 							}
 							battlefield[v][h]= b;
 							break;
 							
 				case 's':	if(battlefield[v][h].toString().equals("s")) {
 								break;
 							}
 							
 							if(battlefield[v][h].toString().equals("S")) {
 								score += 10;
 								break;
 							}
 				case 'S':	if(battlefield[v][h]==null || battlefield[v][h].toString().equals(" ")){
 								battlefield[v][h]= b;
 								break;
 							}
 							else{
 								battlefield[v][h]=new Empty(v,h);
 								break;
 							}
 							
 				default: 	battlefield[v][h]= b;
 			}	
 			
 	}
 		/**
 		 * setBattleField method.  Initialises the battlefield configuration as specified by the parameters.
 		 * 
 		 * @param s - String 
 		 * @throws IllegalElementException
 		 * @throws IllegalPositionException
 		 */
 	public void setBattleField(String s) throws IllegalElementException, IllegalPositionException {   // a method initializing the battle-field configuration as specified in the parameter: 
 		try{
 			String config_line = s;
 			StringTokenizer st = new StringTokenizer(config_line,"|");
 			
 			//Get the rows from the String
 			String s_rows = st.nextToken();
 			rows = Integer.parseInt(s_rows);
 			
 			//Get the columns from the String
 			String s_columns = st.nextToken();
 			columns = Integer.parseInt(s_columns);
 			
 			battlefield = new BattleFieldElement[rows][columns];
 			
 			//The rest of the String (Positions of the BattlefieldElements
 			String result = st.nextToken();
 			
 			int i=0;
 			int h=0;
 			int v=0;
 			int numberOfItem=0;
 			String toBeConverted;			//had a problem with the parseint 'couse it require a String (not a character)
 			BattleFieldElement b=null;
 
 			while(i<result.length()){
 				if(result.charAt(i)=='$'){				//is the char a $?
 					v++;								//YES:  newLine
 					h=0;										//start from the first column
 					result = result.substring(i+1);				//cut the part done to the result
 					i=0;										//restart from the first char of result
 					continue;									//restart the loop
 				}
 				
 				if(i%2==0){								// is it a number?
 					toBeConverted=""+result.charAt(i);				//then convert to an int	
 					numberOfItem=Integer.parseInt(toBeConverted);
 				}
 				else {									//it isn't!
 					for(int k=0;k<numberOfItem;k++){		//set "numberOfItem" element in the battlefield 
 						switch (result.charAt(i)) { 
 							case ' ': 	b=new Empty(v,h);
 										setBattleFieldElement(v, h, b);
 										break; 
 										
 							case 'R':	b=new RedSpacecraft(v,h);
 										setBattleFieldElement(v, h, b);
 										break;
 										
 							case 'A': 	b=new OneStepAlien(v,h);
 										setBattleFieldElement(v, h, b);
 										break;  
 							
 							case 'C': 	b=new Casemate(v,h);
 										setBattleFieldElement(v, h, b);
 										break;  
 										
 							case 'G': 	b=new Gun(v,h);
 										setBattleFieldElement(v, h, b);
 										break;
 										
 							case 's': 	b=new GunShot(v,h);
 										setBattleFieldElement(v, h, b);
 										break;  
 										
 							case 'S': 	b=new AlienShot(v,h);
 										setBattleFieldElement(v, h, b);
 										break; 
 																
 							default: 	break; // must throw an illegal string filename exception
 						}//end switch
 						h++; //move to next pos;
 				
 					}//end for
 				}//end else
 				
 				i++;	//next char
 			
 			}//end while
 		} catch(NullPointerException e){
 			System.out.println("String given doesn't fit enought the matrix");
 		} 
 		
 	}	
 	
 	/**
 	 * move method. 
 	 * @throws IllegalElementException
 	 * @throws IllegalPositionException
 	 */
 	public void move() throws IllegalElementException, IllegalPositionException{ 
 	  
 		AlienShot as=null;
 		int consecutiveRed=0;
 		int cA=0;
 		boolean theyMove= doTheyMove();
 		
 		//System.out.println(doTheyMove());
 		for(int v=0; v<rows; v++)	{							// each row starting from 0 
 			
 			for(int h=0; h<columns; h++) {
 				switch(battlefield[v][h].toString().charAt(0)) {		// switch to the case returned from the toString() of the battlefield[x][y]					
 								//CaseMate & Empty
 					case ' ':
 					case 'C': 	break;							
 					case 'A':	if(theyMove){
 									if(OneStepAlien.armyDirection==-1){					//they go left no problem
 										alienCollide(v,h);
 										break;
 									}
 									else												//they Move but to the right
 										if(battlefield[v][h+1].toString().equals("A")){ //managing case next is Alien
 											cA++;
 											battlefield[v][h].move(v, h+OneStepAlien.armyDirection);
 											break;
 										}
 										else
 											if(cA==0){						//if there weren´t consecutive		
 
 												alienCollide(v,h);			//we move it
 												h++;
 												break;
 											}
 											else {
 												alienCollide(v,h); 
 												for(int k=1;k<=cA;k++){
 													battlefield[v][h]=battlefield[v][h-k];
 												}
 												setBattleFieldElement(v,h-cA,new Empty(v,h-cA));
 												cA=0;
 												h++;
 												break;
 											}
 								}
 								else break;
 					
 						
 						
 						
 				
 					
 								//RedSpacecraft
 					case 'R':   if((battlefield[v][h].getXOffset()==0)|| (battlefield[v][h-1].toString().equals("s"))) { //if near left border or near a shot just break
 									setBattleFieldElement(v,h,new Empty(v,h));				//replace with an empty cell
 									break;
 								}
 								else {														//if far from border instead and not near a shot
 									battlefield[v][h].move(v,h-1);	//and put the RedSpacecraft to the left
 									battlefield[v][h-1]=battlefield[v][h];
 									setBattleFieldElement(v,h,new Empty(v,h));				//replace with an empty cell
 									break;//end case R						
 								}//end else
 					
 					case 's': 	if(battlefield[v][h].getYOffset()==0){
 									setBattleFieldElement(v,h,new Empty(v,h)); 
 								}
 								else{
 									String elementType = battlefield[v-1][h].toString();
 									if(elementType.equals("A") || elementType.equals("R") || elementType.equals("C")||elementType.equals("S")){
 										setBattleFieldElement(v-1,h,new Empty(v-1,h));
 										setBattleFieldElement(v,h, new Empty(v,h));
 										if (elementType.equals("A")) {
 											playSound("invaderkilled.wav");
 											score += 50;
 										} 
 										if (elementType.equals("R")) {
 											score += 350;
 											playSound("invaderkilled.wav");
 										}
 										if (elementType.equals("S")) {
 											score += 10;
 										}
 										
 									}
 									else{
 										battlefield[v][h].move(v-1,h);	
 										battlefield[v-1][h]=battlefield[v][h];
 										setBattleFieldElement(v,h,new Empty(v,h));
 									}	
 									
 						
 								}
 								break;
 					
 					case 'S':	as=(AlienShot)battlefield[v][h];
 								if(as.getMoved()==0){
 									if(battlefield[v][h].getYOffset()==0){
 										setBattleFieldElement(v,h,new Empty(v,h)); 
 										life--;
 										break;
 									}
 									else{
 										String elementType = battlefield[v+1][h].toString();
 										if(elementType.equals("A") || elementType.equals("G")  || elementType.equals("C")|| elementType.equals("s")){
 											setBattleFieldElement(v+1,h,new Empty(v+1,h));							
 											setBattleFieldElement(v,h, new Empty(v,h));
 											if( elementType.equals("G") ) {
 												Gui.shootAllowed = false;
 												gunCounter--;
 												playSound("explosion.wav");
 												//life--;
 												
 												setBattleFieldElement(rows-2, columns/2, new Gun(rows-2,columns/2));
 												dead = true;
 											}
 											
 											
 											
 											
 											if (elementType.equals("s")) {
 												score += 10;
 											}
 										}
 										else{
 											
 											battlefield[v][h].move(v+1,h);	
 											battlefield[v+1][h]=battlefield[v][h];
 											setBattleFieldElement(v,h,new Empty(v,h));
 											as=(AlienShot)battlefield[v+1][h];
 											as.setMoved(1);
 										}	
 										
 							
 									}
 								}
 								break;
 					
 				/*	case 'G':	Gun g= (Gun)battlefield[v][h];
 								
 								
 								if (g.getXOffset() != 0) {
 									this.gunCollide(v, h, g);	
 								} else {
 									Gun.changeDirection();
 									this.gunCollide(v, h, g);
 								}
 
 						
 								break;
 										
 					*/
 					
 					//
 								default:  break;
 					
 
 				}//end switch
 				
 			}//For2
 		} //for1
 		
 		
 		for(int v=0; v<rows; v++){ 			// each row starting from 0 
 			for(int h=0; h<columns; h++) { 			// each column starting from 0
 				if(battlefield[v][h] instanceof AlienShot){	//if it is an AlienShot
 					as=(AlienShot)battlefield[v][h];
 					as.setMoved(0);									//set it as "not moved"
 				}
 			}
 		}
 		
 		doTheyShot();
 		
 	}//end of method
 
 
 	
 	/**
 	 * doTheyMove method.  Method to determine if they move
 	 * 
 	 * @return boolean - True or false
 	 */
 	public boolean doTheyMove(){
 		OneStepAlien osa=null;
 		boolean dirChanged=false;
 		for(int v=0; v<rows; v++){				// each row starting from 0 
 			if(!dirChanged){
 				for(int h=0; h<columns; h++){ 			// each column starting from 0
 					if(battlefield[v][h] instanceof OneStepAlien){	//if it is an Alien
 						osa=(OneStepAlien)battlefield[v][h];		//put it in the "osa" variable
 						if(osa.getXOffset()==0 || (osa.getXOffset()!=0 && battlefield[v][h+OneStepAlien.armyDirection] instanceof Casemate)){	//check if near border or near casemate 
 							OneStepAlien.changeDirection();
 							dirChanged=true;
 							break;
 						}
 					}
 				}//end h for
 			}
 		}//end v for
 	
 		
 		
 		
 		if(!dirChanged){			//if the direction hasn't been changed they will move 
 			return true;
 		} else{			//if the direction has been changed
 			dirChanged=false;
 			for(int v=0; v<rows; v++){				// each row starting from 0 
 				if(!dirChanged) {	
 					for(int h=0; h<columns; h++){ 			// each column starting from 0
 						if(battlefield[v][h] instanceof OneStepAlien){	//if it is an Alien
 							osa=(OneStepAlien)battlefield[v][h];		//put it in the "osa" variable
 							if(osa.getXOffset()==0 || (osa.getXOffset()!=0 && battlefield[v][h+OneStepAlien.armyDirection] instanceof Casemate)){	//check if near border or near casemate 
 								OneStepAlien.changeDirection();
 								dirChanged=true;
 								break;
 							}
 						}
 					}//end h for
 				}
 			}//end v for
 			
 			if(!dirChanged){
 				return true;
 			}
 			return false;		//don't move if reach this instruction
 		}				
 	}//end method
 	
 /*
 	public void gunCollide(int v, int h, Gun g) throws IllegalElementException, IllegalPositionException {
 		
 		if (battlefield[v][h+g.getDirection()].equals("S")) {
 			setBattleFieldElement(v,h,new Empty(v,h));
 			setBattleFieldElement(v,h+g.getDirection(),new Empty(v,h+g.getDirection()));
 			gunCounter--;
 			setBattleFieldElement(rows-1,0,new Gun(rows-1,0));
 			System.out.println("DEAD");
 		} else {
 //			battlefield[v][h].move(v,h+g.getDirection());	
 //			battlefield[v][h+g.getDirection()]=battlefield[v][h];
 //			setBattleFieldElement(v,h,new Empty(v,h));
 		}
 	}
 	*/
 	
 
 /**
  * doTheyShot method.  A method to determine it they shoot.
  */
 	public void doTheyShot(){
 		OneStepAlien osa = null;
 		boolean bALien = false;
 		for(int h=0; h<columns; h++){				// each row starting from 0 
 
 				for(int v=0; v<rows-1; v++){ 			// each column starting from 0
 					if(battlefield[v][h] instanceof OneStepAlien){	//if it is an Alien
 						osa=(OneStepAlien)battlefield[v][h];		//put it in the "osa" variable
 						bALien = true;
 					}
 				}//end h for
 				Random ran = new Random();
 				int numRand = ran.nextInt(100)+1;
 				if (numRand < 3 && bALien == true ) {
 				
 						try {
 							setBattleFieldElement(osa.y+1,osa.x,new AlienShot(osa.y+1,osa.x));
 							
 						} catch (IllegalElementException
 								| IllegalPositionException e) {
 						
 						}
 						bALien = false;
 				}
 		}//end v for
 
 				
 	}
 	
 	
 	/**
 	 * alienCollide method.  Determines what happens when an alien collides with another object.
 	 * 
 	 * @param v - int vertical position
 	 * @param h - int horizontal position
 	 * @throws IllegalElementException
 	 * @throws IllegalPositionException
 	 */
 	public void alienCollide(int v, int h) throws IllegalElementException, IllegalPositionException {
 	
 		if (battlefield[v][h+OneStepAlien.armyDirection].toString().equals(" ") ) {
 			battlefield[v][h].move(v,h+OneStepAlien.armyDirection);
 			battlefield[v][h+OneStepAlien.armyDirection]=battlefield[v][h];
 			setBattleFieldElement(v,h,new Empty(v,h));
 			
 		} 
 		else {
 				setBattleFieldElement(v,h,new Empty(v,h));
 				setBattleFieldElement(v,h+OneStepAlien.armyDirection,new Empty(v,h));				
 		}
 	}
 	
 }//end of class
 
