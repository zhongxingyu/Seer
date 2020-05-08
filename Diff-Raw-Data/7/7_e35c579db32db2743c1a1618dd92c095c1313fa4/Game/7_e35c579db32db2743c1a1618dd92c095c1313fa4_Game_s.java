 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.io.*;
 
 
 
 
 public class Game {
 	
 	Board b;
 	boolean whiteFirst;
 	boolean whiteTurn;
 	Set<String> moveSet;
 	
 	public Game() {
 		
 		byte wr,br;
 		moveSet = new HashSet<String>();
 		b = new Board();
 		b.init();
 		do { 
 		wr = (byte)((Math.random()*6)+1);
 		br = (byte)((Math.random()*6)+1);
 		} while (wr==br);
 		first(wr,br);
 		if (whiteFirst) {
 			whiteTurn=true;
 		} else {
 			whiteTurn=false;
 		}
 		
 	}
 	
 	/*
 	 * rolls two dice
 	 * TODO: figure out if it needs a return statement
 	 */
 	public void roll() {
 		byte dice1 = (byte)((Math.random()*6)+1);
 		byte dice2 = (byte)((Math.random()*6)+1);
 	    movegen(dice1, dice2);
	    move((byte)(dice1+dice2),dice1,dice2);
 	}
 	
 	
 	/*
 	 * a method that prins every possible legal move a player (computer or human) can perform, according to the dice they rolled.
 	 * 
 	 */
 	
 	public void movegen(byte r1, byte r2) {
 		
 		boolean exoume_diples = false ;
 		boolean lastRun = false;
 		boolean m1Happened = false;
 		boolean m2Happened = false;
 		boolean wEaten = false;
 		boolean bEaten = false;
 		boolean iCol, ir1Col, ir2Col, ir1r2Col, idubsCol;
 		byte iNum, ir1Num, ir2Num, ir1r2Num, idubsNum;
 		byte lastCounter = 0;
 		
 
 		
 		/* check if we have dubs */
 		if(r1==r2){
 			exoume_diples = true ;
 		}
 		
 		/* checks to see if white has eaten pills */
 		if(b.pst[24].getNum()>0) {
 			wEaten = true;
 		}
 		
 		/* checks to see if black has eaten pills */
 		if(b.pst[25].getNum()>0) {
 			bEaten = true;
 		}
 		
 		/* self-explanatory */
 		if(whiteTurn) {
 			
 			moveSet.clear();
 			
 			//to be removed
 			
 			System.out.println("\t\t\t\t\t generating moves for "+r1+" and "+r2);
 			
 			
 			/* check to see if we are in last run mode */
 			for(int i=0; i<18; i++) {
 				if(b.pst[i].getCol()==false && b.pst[i].getNum() != 0) {
 					lastCounter++;
 				}
 			}
 			if(lastCounter==0) {
 				lastRun = true;
 			} 
 			
 			if(wEaten) {
 				if(!exoume_diples) {
 					ir1Col = b.pst[r1-1].getCol();
 					ir1Num = b.pst[r1-1].getNum();
 					if((ir1Col==false || ir1Num==0) || (ir1Col==true) && (ir1Num==1)) {
 						moveSet.add("out."+r1);
 						m1Happened = true;
 					}
 					
 					ir2Col = b.pst[r2-1].getCol();
 					ir2Num = b.pst[r2-1].getNum();
 					if((ir2Col==false || ir2Num==0) || (ir2Col==true) && (ir2Num==1)) {
 						moveSet.add("out."+r2);
 						m2Happened = true;
 					}
 					
 					if(m1Happened || m2Happened) {
 						
 						ir1r2Col = b.pst[r1+r2-1].getCol();
 						ir1r2Num = b.pst[r1+r2-1].getNum();
 						if((ir1r2Col==false || ir1r2Num==0) || (ir1r2Col==true) && (ir1r2Num==1)) {
 							moveSet.add("out."+(r1+r2));
 						}
 					}
 				} else if(exoume_diples) {
 					
 					byte counter_diplwn = 1;
 					
 					while(counter_diplwn < 5 && ((counter_diplwn*r1)-1) < 24 ) {
 						idubsCol = b.pst[(counter_diplwn*r1)-1].getCol();
 						idubsNum = b.pst[(counter_diplwn*r1)-1].getNum();
 						if((idubsCol==false || idubsNum==0) || (idubsCol==true) && (idubsNum==1)) {
 							
 							moveSet.add("out."+(counter_diplwn*(r1)));
 							
 						} else {
 							break;
 						}
 						counter_diplwn ++ ;
 					}
 				}
 			} else {
 				
 				/* loop that goes through every position on the board */
 				for(int i=0; i<24; i++) {
 					
 					/* legal moves for non-double dice rolls */
 					if (!exoume_diples) {	
 						
 						/* gets the color and number of pills that exist in the current position(i) */
 						iCol = b.pst[i].getCol();
 						iNum = b.pst[i].getNum();
 						
 						/* if the color is white, and there are some pills in the position, continue */
 						if(iCol==false && iNum!=0) {
 							
 							/* if the position the first dice brings us to is within the game borders, continue */
 							if(i+r1<24) {
 								
 								/* gets the color and number of pills of the position we are trying to move to */
 								ir1Col = b.pst[i+r1].getCol();
 								ir1Num = b.pst[i+r1].getNum();
 								
 								/* checks to see if we can move there legally */
 								if((ir1Col==false || ir1Num==0) || (ir1Col==true) && (ir1Num==1)) {
 									
 									/* we print the legal moves, and also add them to moveSet */
 									moveSet.add((i+1)+"."+(i+r1+1));
 									/* tells us the first dice produced a legal move */
 									m1Happened = true;
 									
 									/* not sure if this is useful in this method */
 									if(ir1Col) {
 										System.out.println("It will eat a black pill");
 									}
 								}
 								
 							/* if the position we are trying to move to exceeds the game borders, and we are in last run mode, continue */	
 							} else if (((i+r1) >= 24) && lastRun) {
 								
 								/* again, prints legal moves and adds them to the set */
 								moveSet.add((i+1)+".out");
 								m1Happened = true;
 							}
 							
 							/* same stuff for the second dice */
 							if(i+r2<24) {
 								ir2Col = b.pst[i+r2].getCol();
 								ir2Num = b.pst[i+r2].getNum();
 								if((ir2Col==false || ir2Num==0) || (ir2Col==true) && (ir2Num==1)) {
 	
 									moveSet.add((i+1)+"."+(i+r2+1));
 									m2Happened = true;
 									if(ir2Col) {
 										System.out.println("It will eat a black pill");
 									}
 								}
 							
 							} else if (((i+r2) >= 24) && lastRun) {
 	
 								moveSet.add((i+1)+".out");
 								m2Happened = true;
 							}
 							
 							/* legal moves for the addition of both dice */
 							if(i+r1+r2<24) {	
 								
 								/* checks if at least one of the two dice gives us a legal move in order to continue */
 								if(m1Happened || m2Happened) {
 									
 									/* same stuff as before */
 									ir1r2Col = b.pst[i+r1+r2].getCol();
 									ir1r2Num = b.pst[i+r1+r2].getNum();
 									if((ir1r2Col==false || ir1r2Num==0) || (ir1r2Col==true) && (ir1r2Num==1)) {
 										
 										moveSet.add((i+1)+"."+(i+r1+r2+1));
 										//TODO: possibly remove this check entirely
 										if(ir1r2Col) {
 											System.out.println("It will eat a black pill");
 										}
 									}
 								}
 								
 							/* last run check for both dice */
 							} else if(((i+r1+r2) >=24) && lastRun) {
 								if(m1Happened || m2Happened) {
 	
 									moveSet.add((i+1)+".out");
 								}
 							}
 						}
 					}
 					
 					/* legal moves for double dice rolls */
 					else if(exoume_diples) {
 						
 						byte counter_diplwn = 1;
 						iCol = b.pst[i].getCol() ;
 						iNum = b.pst[i].getNum(); 
 						if(iCol == false && iNum != 0){
 							if(!lastRun){
 								while(counter_diplwn < 5 && i+counter_diplwn*r1 < 24 ) {
 									idubsCol = b.pst[i+counter_diplwn*r1].getCol();
 									idubsNum = b.pst[i+counter_diplwn*r1].getNum();
 									if((idubsCol==false || idubsNum==0) || (idubsCol==true) && (idubsNum==1)) {
 										
 										moveSet.add((i+1)+"."+(i+counter_diplwn*r1+1));
 										//TODO: possibly remove this check entirely
 										if(idubsCol) {
 											System.out.println("It will eat a black pill");
 										}
 									} else {
 										break;
 									}
 									counter_diplwn ++ ;
 								}
 							/* dubs last run mode legals */
 							} else if(lastRun) {
 								while(counter_diplwn < 5) {
 									if(i+counter_diplwn*r1 < 24) {
 										idubsCol = b.pst[i+counter_diplwn*r1].getCol();
 										idubsNum = b.pst[i+counter_diplwn*r1].getNum();
 										if((idubsCol==false || idubsNum==0) || (idubsCol==true) && (idubsNum==1)) {
 											
 											moveSet.add((i+1)+"."+(i+counter_diplwn*r1+1));
 											//TODO: possibly remove this check entirely
 											if(idubsCol) {
 												System.out.println("It will eat a black pill");
 											}	
 											
 											
 										} else { break; }
 										
 										counter_diplwn ++ ;
 										
 									} else {
 										if(iNum>0) {
 											moveSet.add((i+1)+".out");	
 											counter_diplwn++;
 										}	
 									}
 								}
 							}
 						}
 					}
 					
 				m1Happened = false;
 				m2Happened = false;
 				}
 			}
 		Iterator<String> it = moveSet.iterator();
         while (it.hasNext()) {
                 System.out.println(it.next());
         } 
         
 		
 		} else {
 			
 			 moveSet.clear();
 			
 			//TODO: remove this 
 			System.out.println("\t\t\t\t\t black rolled "+r1+" and "+r2);
 			
 			/* check to see if we are in last run mode */
 			for(int i=23; i>6; i--) {
 				if(b.pst[i].getCol()==true && b.pst[i].getNum() != 0) {
 					lastCounter++;
 				}
 			}
 			if(lastCounter==0) {
 				lastRun = true;
 			}
 			
 			if(bEaten) {
 				if(!exoume_diples) {
 					ir1Col = b.pst[24-r1].getCol();
 					ir1Num = b.pst[24-r1].getNum();
 					if((ir1Col==true || ir1Num==0) || (ir1Col==false) && (ir1Num==1)) {
 						moveSet.add("out."+(24-r1+1));
 						m1Happened = true;
 					}
 					
 					ir2Col = b.pst[24-r2].getCol();
 					ir2Num = b.pst[24-r2].getNum();
 					if((ir2Col==true || ir2Num==0) || (ir2Col==false) && (ir2Num==1)) {
 						moveSet.add("out."+(24-r2+1));
 						m2Happened = true;
 					}
 					
 					if(m1Happened || m2Happened) {
 						
 						ir1r2Col = b.pst[24-(r1+r2)].getCol();
 						ir1r2Num = b.pst[24-(r1+r2)].getNum();
 						if((ir1r2Col==true || ir1r2Num==0) || (ir1r2Col==false) && (ir1r2Num==1)) {
 							moveSet.add("out."+(24-(r1+r2+1)));
 						}
 					}
 				} else if(exoume_diples) {
 					
 					byte counter_diplwn = 1;
 					
 					while(counter_diplwn < 5 && (24-counter_diplwn*(r1)) >= 0 ) {
 						idubsCol = b.pst[24-counter_diplwn*(r1)].getCol();
 						idubsNum = b.pst[24-counter_diplwn*(r1)].getNum();
 						if((idubsCol==true || idubsNum==0) || (idubsCol==false) && (idubsNum==1)) {
 							
 							moveSet.add("out."+(24-counter_diplwn*(r1)));
 							
 						} else {
 							break;
 						}
 						counter_diplwn ++ ;
 					}
 				}
 			} else {
 				
 				for(int i=23; i>0; i--) {
 					if(!exoume_diples) {
 						iCol = b.pst[i].getCol();
 						iNum = b.pst[i].getNum();
 						if(iCol==true && iNum!=0) {
 							if(i-r1>=0)	{
 								ir1Col = b.pst[i-r1].getCol();
 								ir1Num = b.pst[i-r1].getNum();
 								if((ir1Col==true || ir1Num==0) || (ir1Col==false) && (ir1Num==1)) {
 									moveSet.add((i+1)+"."+(i-r1+1));
 									m1Happened = true;
 									if((ir1Col==false) && (ir1Num==1)) {
 										System.out.println("It will eat a white pill");
 									}
 								}
 							} else if (((i-r1) < 0) && lastRun) {
 								moveSet.add((i+1)+".out");
 								m1Happened = true;
 							}
 							
 							if(i-r2>=0) {
 								ir2Col = b.pst[i-r2].getCol();
 								ir2Num = b.pst[i-r2].getNum();
 								if((ir2Col==true || ir2Num==0) || (ir2Col==false) && (ir2Num==1)) {
 									moveSet.add((i+1)+"."+(i-r2+1));
 									m2Happened = true;
 									if((ir2Col==false) && (ir2Num==1)) {
 										System.out.println("It will eat a white pill");
 									}
 								}
 							} else if (((i+r2) < 0) && lastRun) {
 								moveSet.add((i+1)+".out");
 								m2Happened = true;
 							}
 							
 							if(i-r1-r2>=0) {
 								if(m1Happened || m2Happened) {
 									ir1r2Col = b.pst[i-r1-r2].getCol();
 									ir1r2Num = b.pst[i-r1-r2].getNum();
 									if((ir1r2Col==true || ir1r2Num==0) || (ir1r2Col==false) && (ir1r2Num==1)) {
 										moveSet.add((i+1)+"."+(i-r1-r2+1));
 										if((ir1r2Col==false) && (ir1r2Num==1)) {
 											System.out.println("It will eat a white pill");
 										}
 									}
 								}
 							} else if(((i-r1-r2) < 0) && lastRun) {
 								if(m1Happened || m2Happened) {
 									moveSet.add((i+1)+".out");
 								}
 							}	
 						}	
 					} 
 					
 					else if(exoume_diples) {
 						
 						byte counter_diplwn = 1;
 						iCol = b.pst[i].getCol() ;
 						iNum = b.pst[i].getNum();
 						if(iCol == true && iNum != 0){
 							if(!lastRun) {
 								while(counter_diplwn < 5 && i-counter_diplwn*r1 >= 0 ){
 									
 									idubsCol = b.pst[i-counter_diplwn*r1].getCol();
 									idubsNum = b.pst[i-counter_diplwn*r1].getNum();
 									if((idubsCol==true || idubsNum==0) || ((idubsCol==false) && (idubsNum==1))) {
 										moveSet.add((i+1)+"."+((i-counter_diplwn*r1)+1));
 										if(idubsCol==false && idubsNum>0) {
 											System.out.println("It will eat a white pill");
 										}
 									} else {
 										break;
 									}
 									counter_diplwn ++ ;
 								}
 							} else if(lastRun) {
 								while(counter_diplwn < 5) {
 									if(i+counter_diplwn*r1 >= 0) {
 										idubsCol = b.pst[i-counter_diplwn*r1].getCol();
 										idubsNum = b.pst[i-counter_diplwn*r1].getNum();
 										if((idubsCol==true || idubsNum==0) || ((idubsCol==false) && (idubsNum==1))) {
 											moveSet.add((i+1)+"."+((i-counter_diplwn*r1)+1));
 											if(idubsCol==false && idubsNum>0) {
 												System.out.println("It will eat a white pill");
 											}
 										} else {
 											break;
 										}
 										counter_diplwn ++ ;
 									} else {
 										if(iNum>0) {
 											moveSet.add((i+1)+".out");	
 											counter_diplwn++;
 										}	
 									}
 									
 								}
 							}
 						}
 					}
 				m1Happened = false;
 				m2Happened = false;
 				}
 			}
 		Iterator<String> it = moveSet.iterator();
         while (it.hasNext()) {
                 System.out.println(it.next());
         } 
 		}
 	}
 	
 	public void movegen(byte dice) {
 		
 		byte lastCounter = 0;
 		boolean lastRun = false;
 		boolean wEaten = false;
 		boolean bEaten = false;
 		boolean diceCol,iCol;
 		byte diceNum,iNum;
 		
 		/* checks to see if white has eaten pills */
 		if(b.pst[24].getNum()>0) {
 			wEaten = true;
 		}
 		
 		/* checks to see if black has eaten pills */
 		if(b.pst[25].getNum()>0) {
 			bEaten = true;
 		}
 		
 		if(whiteTurn) {
 			moveSet.clear();
 			System.out.println("\t\t\t\t\t generating moves for "+dice);
 			
 			
 			/* check to see if we are in last run mode */
 			for(int i=0; i<18; i++) {
 				if(b.pst[i].getCol()==false && b.pst[i].getNum() != 0) {
 					lastCounter++;
 				}
 			}
 			if(lastCounter==0) {
 				lastRun = true;
 			} 
 			
 			if(wEaten) {
 		
 				diceCol = b.pst[dice-1].getCol();
 				diceNum = b.pst[dice-1].getNum();
 				if((!diceCol || diceNum==0) || diceCol && (diceNum==1)) {
 					moveSet.add("out."+dice);
 				}
 				
 				 
 			} else {
 				
 				/* loop that goes through every position on the board */
 				for(int i=0; i<24; i++) {
 					
 					
 					
 					/* gets the color and number of pills that exist in the current position(i) */
 					iCol = b.pst[i].getCol();
 					iNum = b.pst[i].getNum();
 					
 					/* if the color is white, and there are some pills in the position, continue */
 					if(!iCol && iNum!=0) {
 						
 						/* if the position the first dice brings us to is within the game borders, continue */
 						if(i+dice<24) {
 							
 							/* gets the color and number of pills of the position we are trying to move to */
 							diceCol = b.pst[i+dice].getCol();
 							diceNum = b.pst[i+dice].getNum();
 							
 							/* checks to see if we can move there legally */
 							if((!diceCol || diceNum==0) || (diceCol && (diceNum==1))) {
 								
 								/* we print the legal moves, and also add them to moveSet */
 								moveSet.add((i+1)+"."+(i+dice+1));
 								
 								/* not sure if this is useful in this method */
 								if(diceCol) {
 									System.out.println("It will eat a black pill");
 								}
 							}
 							
 						/* if the position we are trying to move to exceeds the game borders, and we are in last run mode, continue */	
 						} else if (((i+dice) >= 24) && lastRun) {
 							
 							/* again, prints legal moves and adds them to the set */
 							moveSet.add((i+1)+".out");
 						}
 					}
 				}
 			}
 		Iterator<String> it = moveSet.iterator();
         while (it.hasNext()) {
                 System.out.println(it.next());
         } 
 		} else {
 			moveSet.clear();
 			System.out.println("\t\t\t\t\t generating moves for "+dice);
 			
 			
 			/* check to see if we are in last run mode */
 			for(int i=23; i>6; i--) {
 				if(b.pst[i].getCol()==true && b.pst[i].getNum() != 0) {
 					lastCounter++;
 				}
 			}
 			if(lastCounter==0) {
 				lastRun = true;
 			}
 			
 			if(bEaten) {
 		
 				diceCol = b.pst[24-dice].getCol();
 				diceNum = b.pst[24-dice].getNum();
 				if((diceCol || diceNum==0) || (!diceCol && (diceNum==1))) {
 					moveSet.add("out."+(24-dice+1));
 				}
 				 
 			} else {
 				
 				/* loop that goes through every position on the board */
 				for(int i=0; i<24; i++) {
 					
 					
 					iCol = b.pst[i].getCol();
 					iNum = b.pst[i].getNum();
 					if(iCol==true && iNum!=0) {
 						if(i-dice>=0)	{
 							diceCol = b.pst[i-dice].getCol();
 							diceNum = b.pst[i-dice].getNum();
 							if((diceCol || diceNum==0) || (!diceCol && (diceNum==1))) {
 								moveSet.add((i+1)+"."+(i-dice+1));
 								if((!diceCol) && (diceNum==1)) {
 									System.out.println("It will eat a white pill");
 								}
 							}
 						} else if (((i-dice) < 0) && lastRun) {
 							moveSet.add((i+1)+".out");
 						}
 					}
 				}
 				Iterator<String> it = moveSet.iterator();
 		        while (it.hasNext()) {
 		                System.out.println(it.next());
 		        } 
 			}
 		}
 		
 	}
 	
 	/* method for the actual moving of pills, checks every move against the moveSet */
 	public void move(byte max, byte r1, byte r2) {
 		String mov;
 		boolean gotit = false;
 		byte counter = 0;
 		if(whiteTurn) {
 			System.out.println("it's white's turn");
 		} else {
 			System.out.println("it's black's turn");
 		}
 		System.out.println("type your move. (style: 1.10)");
 		InputStreamReader read = new InputStreamReader(System.in);
 		BufferedReader in = new BufferedReader(read);
 		
 		try {
 			
 			while(true) {
 				
 				//TODO: change this to work for the gui
 				mov = in.readLine();
 				counter++;
 				System.out.println("counter: "+counter);
 				
 				if (moveSet.contains(mov)) {
 						
 					
 					System.out.println(mov);
 					String delimiter = "\\.";
 					String[] tokens = mov.split(delimiter);
 					if(whiteTurn) {
 						if(tokens[0].equals("out")) {
 							int temp = Integer.parseInt(tokens[1]);
 							if(b.pst[temp-1].getCol()) {
 								b.pst[25].incr();
 								b.pst[temp-1].setCol(false);
 								b.pst[24].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 										break;
 								gotit = finCheck(max, r1, r2, temp);
 								if(gotit)
 									break;
 							} else {
 								b.pst[temp-1].incr();
 								b.pst[temp-1].setCol(false);
 								b.pst[24].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
 								gotit = finCheck(max, r1, r2, temp);
 								if(gotit)
 									break;
 							}
 						} else if(tokens[1].equals("out")) {
 							int temp = Integer.parseInt(tokens[0]);
 							b.pst[26].incr();
 							b.pst[temp-1].decr();
 							if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 								break;
 							gotit = finCheck(max, r1, r2, temp);
 							if(gotit)
 								break;
 						} else {
 							int temp1 = Integer.parseInt(tokens[0]);
 							int temp2 = Integer.parseInt(tokens[1]);
 							if(b.pst[temp2-1].getCol()) {
 								b.pst[25].incr();
 								b.pst[temp2-1].setCol(false);
 								b.pst[temp1-1].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
 								gotit = finCheck(max, r1, r2, Math.abs(temp2-temp1));
 								if(gotit)
 									break;
 							} else {
 								b.pst[temp2-1].incr();
 								b.pst[temp2-1].setCol(false);
 								b.pst[temp1-1].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
 								gotit = finCheck(max, r1, r2, Math.abs(temp2-temp1));
 								if(gotit)
 									break;
 							}
 						}
 						
 					} else {
 						if(tokens[0].equals("out")) {
 							int temp = Integer.parseInt(tokens[1]);
 							if(!(b.pst[temp-1].getCol()) && b.pst[temp-1].getNum()==1) {
 								b.pst[24].incr();
 								b.pst[temp-1].setCol(true);
 								b.pst[25].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
 								gotit = finCheck(max, r1, r2, temp);
 								if(gotit)
 									break;
 							} else {
 								b.pst[temp-1].incr();
 								b.pst[temp-1].setCol(true);
 								b.pst[25].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
								
 								gotit = finCheck(max, r1, r2, temp);
 								if(gotit)
 									break;
 							}
 						} else if(tokens[1].equals("out")) {
 							int temp = Integer.parseInt(tokens[0]);
 							b.pst[27].incr();
 							b.pst[temp-1].decr();
 							if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 								break;
 							gotit = finCheck(max, r1, r2, temp);
 							if(gotit)
 								break;
 						} else {
 							int temp1 = Integer.parseInt(tokens[0]);
 							int temp2 = Integer.parseInt(tokens[1]);
 							if(!(b.pst[temp2-1].getCol()) && b.pst[temp2-1].getNum()==1) {
 								b.pst[24].incr();
 								b.pst[temp2-1].setCol(true);
 								b.pst[temp1-1].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
 								gotit = finCheck(max, r1, r2, Math.abs(temp2-temp1));
 								if(gotit)
 									break;
 							} else {
 								b.pst[temp2-1].incr();
 								b.pst[temp2-1].setCol(true);
 								b.pst[temp1-1].decr();
 								if(((counter==2 && r1!=r2) || (counter==4 && r1==r2)) || moveSet.isEmpty())
 									break;
 								gotit = finCheck(max, r1, r2, Math.abs(temp2-temp1));
 								if(gotit)
 									break;
 							}
 						}
 					}
 					
 				} else {
 					System.out.println("you entered an invalid move, please try again");
 				}
 			}
 		
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	whiteTurn = !whiteTurn;	
 	}
 	
 	public boolean finCheck(byte max, byte r1, byte r2, int move) {
 		boolean finished = false;
 		if(move==max) {
 			finished = true;
 		}
 		else if(move==r1) {
 			movegen(r2);
 			finished = false;
 		}
 		else if(move==r2) {
 			movegen(r1);
 			finished = false;
 		}
 		return finished;
 	}
 	
 	
 	
 	public void first(byte wr, byte br) {
 		
 		if(wr>br) {
 			System.out.println("White rolled "+wr+", Black rolled "+br+". White plays first.");
 			whiteFirst = true;
 			whiteTurn = true;
 		} else if(wr<br){
 			System.out.println("Black rolled "+br+", White rolled "+wr+". Black plays first");
 			whiteFirst = false;
 			whiteTurn =false;
 		}
 	}
 }
 	
