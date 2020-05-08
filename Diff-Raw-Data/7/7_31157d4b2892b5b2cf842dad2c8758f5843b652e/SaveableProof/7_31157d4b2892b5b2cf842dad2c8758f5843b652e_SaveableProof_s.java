 package edu.rpi.phil.legup.saveable;
 
 //import java.beans.XMLDecoder;
 //import java.beans.XMLEncoder;
 import java.awt.Point;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 import java.util.Vector;
 //import java.util.zip.GZIPInputStream;
 //import java.util.zip.GZIPOutputStream;
 
 import javax.swing.JOptionPane;
 
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.newgui.TreePanel;
 
 public class SaveableProof
 {
 	public SaveableProofState[] states;
 	public Vector<SaveableProofTransition> transitions;
 	public PrintWriter out;
 	public File file;
 	private SaveableProof(BoardState state)
 	{
 		states = new SaveableProofState[state.calcID()];
 		transitions = new Vector<SaveableProofTransition>();
 		//state.makeSaveableProof(states, transitions);
 	}
 	
 	/*private BoardState toBoardState()
 	{
 		BoardState[] bstates = new BoardState[states.length];
 		for(int i = 0; i < states.length; ++i)
 		{
 			bstates[i] = BoardState.fromSaveableProofState(states[i]);
 		}
 		
 		for(SaveableProofTransition t : transitions)
 		{
 			bstates[t.id1].addTransitionFrom(bstates[t.id2], t.justification, t.isCaseRule);
 		}
 		
 		bstates[0].recalculateLocation();
 		
 		return bstates[0];
 	}*/
 	public SaveableProof(){}
 /*LOAD XML	
 	//XML functions
 	
 	public static BoardState loadProof(String filename)
     {
 		SaveableProof rv = null;
     	
     	try
     	{
 	    	XMLDecoder d = new XMLDecoder(
 	                new BufferedInputStream(
 	                		//new GZIPInputStream( //For compression
 	                    new FileInputStream(filename)));
 			Object result = d.readObject();
 			d.close();
 			
 			rv = (SaveableProof)result;
     	}
     	catch (Exception e)
     	{
     		JOptionPane.showMessageDialog(null,"Error loading: " + e.toString());
     		rv = null;
     	}
     	
     	BoardState state = rv.toBoardState(); 
     	
     	if(state == null)
     		JOptionPane.showMessageDialog(null,"Board Load Failed");
     		
     	return state;
     }
 */
 //NORMAL LOAD
 	public static BoardState loadProof(String filename) throws IOException
 	{
 		System.out.println("Loading...\n");
 		BoardState state;
 		Scanner scan = new Scanner(new File(filename));
 		String str;
 		int in;
 		Vector <Integer> labels = new Vector<Integer>();
 		//iterate through file
 		//name
 		scan.next(); scan.nextLine();
 		//puzzle
 		str = scan.next();
 		//System.out.println(str);
 		str = scan.next();
 		//str += scan.nextLine();
 		System.out.println(str);
 		//state.setPuzzleName("Battleship");
 		//System.out.println("stupid loadme is working\n");
 		
 		//height
 		System.out.println("Height\n");
 		scan.next();
 		in = scan.nextInt();
 		
 		//width
 		System.out.println("Width\n");
 		scan.next();
 		//initialize new board while grabbing width
 		state = new BoardState(in, scan.nextInt());
 		
 		//now that board exists, set name and offset
 		state.setPuzzleName(str);
 		state.setOffset(new Point(0,0));
 		state.setLocation(new Point(0,0));
 		
 		//top labels
 		System.out.println("Top Labels\n");
 		str = scan.next();
 		//System.out.println(str);
 		str = scan.next();
 		while (!str.equals("bottomLabels:"))
 		{
 			//System.out.println(str);
 			//System.out.flush();
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setTopLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getTopLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//bottom labels
 		System.out.println("Bottom Labels\n");
 		str = scan.next();
 		while (!str.equals("leftLabels:"))
 		{
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setBottomLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getBottomLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//left labels
 		str = scan.next();
 		while (!str.equals("rightLabels:"))
 		{
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setLeftLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getLeftLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//right labels
 		str = scan.next();
 		while (!str.equals("hintCells:"))
 		{
 			labels.add(Integer.parseInt(str));
 			str = scan.next();
 		}
 		state.setRightLabels(new int[labels.size()]);
 		for (int x = 0; x < labels.size(); x++)
 			state.getRightLabels()[x] = labels.get(x);
 		labels.clear();
 		
 		//hint cells;
 		str = scan.next();
 		
 		//extra data
 		//str = scan.next();
 		
 		//board
 		//scan.next();
 		for (int i = 0; i < state.getHeight(); i++)
 		{
 			for (int j = 0; j < state.getWidth(); j++)
 			{
 				state.getBoardCells()[i][j] = scan.nextInt();
 				//System.out.println(loadme.get(0).boardCells[i][j]);
 			}
 		}
 		System.out.println("board cells loaded...");
 		System.out.flush();
 		
 		//transitions
 		str = scan.next();
 		BoardState currentstate = state;
 		while (scan.hasNext() != scan.hasNextInt())
 		{
 			str = scan.next();
 			if (str.equals("newState:"))
 			{
 				//add new child to parent
 				System.out.println("add child\n");
 				currentstate.getTransitionsFrom().add(new BoardState(state));
 				
 				//add parent to child
 				System.out.println("add parent\n");
 				currentstate.getTransitionsFrom().lastElement().getTransitionsTo().add(currentstate);
 
 				//step in to child
 				currentstate = currentstate.getTransitionsFrom().lastElement();
 				
 				//add justification to child
 				scan.nextLine();
				currentstate.setJustification(scan.nextLine());
 				System.out.println("justification loaded...");
 				
 				//add case rule to child
				currentstate.setCaseRuleJustification(scan.nextLine());
 				System.out.println("case rule loaded...");								
 				
 				//is this a transition?
 				currentstate.setModifiableState(scan.nextBoolean());
 				
 				//add offset to child
 				if(currentstate.isModifiable())
 					currentstate.setOffset(new Point(0, (int)(4.5*TreePanel.NODE_RADIUS)));
 				else
 					currentstate.setOffset(new Point(0, 0));
 				
 				//add point changes to child (if they exist)
 				if(scan.hasNextInt())
 				{
 					currentstate.getChangedCells().add(new Point(scan.nextInt(), scan.nextInt()));
 					
 					//change board cell at point
 					currentstate.getBoardCells()[currentstate.getChangedCells().lastElement().y][currentstate.getChangedCells().lastElement().x] = scan.nextInt();
 				}
 			}
 			
 			else if (str.equals("endLeaf:"))
 			{
 				//step out to parent (if we have a parent)
 				if (!currentstate.getTransitionsTo().isEmpty())
 					currentstate = currentstate.getTransitionsTo().lastElement();
 			}
 			
 			//assume str is now the x of the modified cell
 			else
 			{
 				//add point to child
 				currentstate.getChangedCells().add(new Point(Integer.parseInt(str), scan.nextInt()));
 				//change board cell at point
 				currentstate.getBoardCells()[currentstate.getChangedCells().lastElement().y][currentstate.getChangedCells().lastElement().x] = scan.nextInt();
 			}
 		}
 		int the_hash = scan.nextInt();
 		
 		scan.close();
 		System.out.println("File Closed...");
 		
 		//set first state as unmodifiable
 		for(int i = 0; i < state.getHeight(); i++)
 			for(int j = 0; j < state.getWidth(); j++)
 				state.setModifiableCell(j, i, false);
 		System.out.println("...Done\n");
 		return state;
 	}
 /*SAVE XML	
     public static boolean saveProof(BoardState state, String filename)
     {	
     	SaveableProof s = new SaveableProof(state);
     	
     	try
     	{
 	    	 XMLEncoder e = new XMLEncoder(
 	                 new BufferedOutputStream(
 	                		 //new GZIPOutputStream( //For compression
 	                     new FileOutputStream(filename)));
 			e.writeObject(s);
 			e.close();
     	}
     	catch (Exception e)
     	{
     		JOptionPane.showMessageDialog(null,"Error saving: " + e.getMessage());
     		return false;
     	}
     	
     	return true;
     }
 */
 //NORMAL SAVE
 	public static boolean saveProof(BoardState state, String filename) throws IOException
 	{
 		SaveableProof saveme = new SaveableProof(state);
 		saveme.file = new File(filename);
 		saveme.out = new PrintWriter(new FileWriter(filename));
 		//write "static" state data
 		String encrypt_me = "";
 		encrypt_me += "Name: ";
 		encrypt_me += "Bullwinkle Moose";
 		encrypt_me += "\nPuzzle: ";
 		encrypt_me += state.getPuzzleName();
 		encrypt_me += "\nHeight: ";
 		encrypt_me += state.getHeight();
 		encrypt_me += "\nWidth: ";
 		encrypt_me += state.getWidth();
 		encrypt_me += "\ntopLabels:";
 		for (int savelabel : state.getTopLabels())
 		{
 			encrypt_me += ' ';
 			encrypt_me += savelabel;
 		}
 		encrypt_me += "\nbottomLabels:";
 		for (int savelabel : state.getBottomLabels())
 		{
 			encrypt_me += ' ';
 			encrypt_me += savelabel;
 		}
 		encrypt_me += "\nleftLabels:";
 		for (int savelabel : state.getLeftLabels())
 		{
 			encrypt_me += ' ';
 			encrypt_me += savelabel;
 		}
 		encrypt_me += "\nrightLabels:";
 		for (int savelabel : state.getRightLabels())
 		{
 			encrypt_me += ' ';
 			encrypt_me += savelabel;
 		}
 		encrypt_me += "\nhintCells:\n";
 		for (Point savehints : state.getHintCells())
 		{
 			//SAVE HINTCELLS
 		}
 		//saveme.out.print("\nextraData:\n");
 		for (Object extra : state.getExtraData())
 		{
 			//SAVE EXTRADATA, ie
 			//extradata = new extra.tosavedata();
 		}
 		encrypt_me += "\nBoard:\n";
 		for (int i = 0; i < state.getHeight(); i++)
 		{
 			for (int j = 0; j < state.getWidth(); j++)
 			{
 				encrypt_me += state.getBoardCells()[i][j];
 				encrypt_me += ' ';
 			}
 			encrypt_me += '\n';
 		}
 		//save transitions
 		encrypt_me += "Transitions:\n";
 		for (BoardState transist_state : state.getTransitionsFrom())
 			encrypt_me += saveme.printTransitions(transist_state);
 		saveme.out.print(encrypt_me);
 		saveme.out.print(encrypt_me.hashCode());
 		saveme.out.close();
 		return true;
 	}
 	public String printTransitions(BoardState state)
 	{
 		String encrypt_me = "";
 		encrypt_me += "newState:\n";
 		encrypt_me += state.getJustification();
 		encrypt_me += '\n';
 		encrypt_me += state.getCaseRuleJustification();
 		encrypt_me += '\n';
 		/*
 		//print offset
 		encrypt_me += state.getOffset().x;
 		encrypt_me += ' ';
 		encrypt_me += state.getOffset().y;
 		encrypt_me += '\n';
 		*/
 		encrypt_me += state.isModifiable();
 		encrypt_me += '\n';
 		
 		for(Point cell : state.getChangedCells())
 		{
 			encrypt_me += cell.x;
 			encrypt_me += ' ';
 			encrypt_me += cell.y;
 			encrypt_me += ' ';
 			encrypt_me += state.getBoardCells()[cell.y][cell.x];
 			encrypt_me += '\n';
 		}
 		//recurse through children
 		for (BoardState transition_state : state.getTransitionsFrom())
 			encrypt_me += printTransitions(transition_state);
 				
 		encrypt_me += "endLeaf:\n";
 		
 		return encrypt_me;
 	}
 	public SaveableProofState[] getStates()
 	{
 		return states;
 	}
 
 	public void setStates(SaveableProofState[] states)
 	{
 		this.states = states;
 	}
 
 	public Vector<SaveableProofTransition> getTransitions()
 	{
 		return transitions;
 	}
 
 	public void setTransitions(Vector<SaveableProofTransition> transitions)
 	{
 		this.transitions = transitions;
 	}
 }
