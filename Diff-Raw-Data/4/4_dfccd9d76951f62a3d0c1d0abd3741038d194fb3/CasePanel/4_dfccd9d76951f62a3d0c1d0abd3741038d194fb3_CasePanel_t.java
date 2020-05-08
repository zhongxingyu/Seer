 package edu.rpi.phil.legup.newgui;
 
 import java.awt.FlowLayout;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JToggleButton;
 import javax.swing.JOptionPane;
 
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.Justification;
 import edu.rpi.phil.legup.CaseRule;
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.Selection;
 import edu.rpi.phil.legup.Permutations;
 import edu.rpi.phil.legup.newgui.CaseRuleSelectionHelper;
 import edu.rpi.phil.legup.PuzzleModule;
 import edu.rpi.phil.legup.puzzles.treetent.TreeTent;
 import edu.rpi.phil.legup.puzzles.treetent.CaseLinkTree;
 import edu.rpi.phil.legup.puzzles.treetent.CaseLinkTent;
 import edu.rpi.phil.legup.puzzles.treetent.ExtraTreeTentLink;
 import edu.rpi.phil.legup.puzzles.lightup.LightUp;
 import edu.rpi.phil.legup.puzzles.lightup.CaseSatisfyNumber;
 
 import javax.swing.*;
 import java.awt.*;
 import javax.swing.TransferHandler;
 import java.awt.datatransfer.*;
 import java.awt.event.*;
 import javax.swing.ImageIcon;
 import javax.swing.JToggleButton;
 /**
  * Provides a user interface for users to provide case rule justifications
  *
  */
 public class CasePanel extends JustificationPanel
 {
 	private static final long serialVersionUID = -2304281047341398965L;
 
 	protected final ImageIcon icon = new ImageIcon("images/Case Rules.gif");
 	protected final String name = "Case Rules";
 	protected final String toolTip = "Case Rules";
 	//MouseListener listener = new DragMouseAdapter();
 	private Vector<CaseRule> caseRules = null;
 
 	private CaseRule defaultApplication; //NEEDED! Not yet reimplmented!
 
 	/**
 	 * Create a new CasePanel
 	 */
 	CasePanel(JustificationFrame jf)
 	{
 		this.parentFrame = jf;
 		setLayout(new WrapLayout());
 	}
 
 	/**
 	 * set the case rules displayed by this case rule panel
 	 * @param caseRules the vector of CaseRules
 	 */
 	public void setCaseRules(Vector<CaseRule> caseRules)
 	{
 		this.caseRules = caseRules;
 		clearButtons();
 
 		buttons = new JToggleButton[caseRules.size()];
 
 		for (int x = 0; x < caseRules.size(); ++x)
 		{
 			CaseRule c = caseRules.get(x);
 			buttons[x] = new JToggleButton(c.getImageIcon());
 			this.parentFrame.getButtonGroup().add(buttons[x]);
 
 			buttons[x].setToolTipText(c.getName() + ": " + c.getDescription());
 			buttons[x].addActionListener(this);
 			//removed due to drag-drop being de-prioritized
 			//buttons[x].addMouseListener(listener);
 			//buttons[x].setTransferHandler(new TransferHandler("icon"));
 			add(buttons[x]);
 		}
 
 		revalidate();
 	}
 
 	/**
 	 * Check if the given case Rule can be applied to current board state
 	 * @param c the case rule to be applied
 	 */
 	private void checkCaseRule(CaseRule c)
 	{
 		Selection sel = Legup.getInstance().getSelections().getFirstSelection();
 
 		if (sel.isTransition())
 		{
 			BoardState state = sel.getState();
 
 			// Update: Check only if immediate feedback enabled
 			state.setCaseSplitJustification(c);
 			String error = c.checkCaseRule(state);
 			JustificationFrame.justificationApplied(state, c);
 			parentFrame.resetJustificationButtons();
 
 			if (error == null && LEGUP_Gui.profFlag(LEGUP_Gui.IMD_FEEDBACK))
 				parentFrame.setStatus(true,"The case rule is applied correctly!");
 			else if (LEGUP_Gui.profFlag(LEGUP_Gui.IMD_FEEDBACK)) parentFrame.setStatus(false, error);
 		}
 		else
 		{
 			parentFrame.resetJustificationButtons();
 			parentFrame.setStatus(false, "Case Rules can only be applied to transitions, not states.");
 			sel.getState().setJustification(null);
 		}
 
 		//parent.rep
 	}
 
 
 	/**
 	 * Depresses the current rule button for user display
 	 * @param c Rule to be pressed
 	 * @return Whether or not the rule exists
 	 */
 	public boolean setCaseRule(CaseRule c)
 	{
 		for (int x = 0; x < caseRules.size(); ++x)
 		{
 			if (caseRules.get(x).equals(c))
 			{
 				buttons[x].setSelected(true);
 				checkCaseRule(c);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected Justification addJustification(int button)
 	{
 		Selection selection = Legup.getInstance().getSelections().getFirstSelection();
 		BoardState cur = selection.getState();
 		
 		if (cur.getTransitionsFrom().size() > 0)
 			return null;
 		if (cur.isModifiable() && Legup.getInstance().getGui().autoGenCaseRules)
 			return null;
 		if (!cur.isModifiable() && !Legup.getInstance().getGui().autoGenCaseRules)
 			return null;
 		if (cur.getCaseRuleJustification() != null)
 			return null;
 
 		CaseRule r = caseRules.get(button);
 		
 		/*int quantityofcases = Integer.valueOf(JOptionPane.showInputDialog(null,"How many branches?")).intValue();
 		if(quantityofcases > 10)quantityofcases = 10; //some sanity checks on the input, to prevent
 		if(quantityofcases < 2)quantityofcases = 2; //the user from creating 100 nodes or something
 		*/
 		if(Legup.getInstance().getGui().autoGenCaseRules)
 		{
 			Object[] msg = new Object[2];
 			CaseRuleSelectionHelper crsh = new CaseRuleSelectionHelper(null/*Legup.getInstance().getGui()*/);
 			crsh.mode = caseRules.get(button).crshMode();
 			crsh.tileTypes = caseRules.get(button).crshTileType();
 			msg[0] = "Select where you would like to apply the CaseRule, and then select ok.";
 			msg[1] = crsh;
 			JOptionPane.showMessageDialog(null,msg);
 			if((crsh.pointSelected.x == -5) && (crsh.pointSelected.y == -5))
 			{
 				//System.out.println("Nothing selected.");
 				return null;
 			}
 			else
 			{
 				//System.out.println("Point ("+crsh.pointSelected.x+","+crsh.pointSelected.y+") selected.");
 				PuzzleModule pm = Legup.getInstance().getPuzzleModule();
 				if(crsh.mode == CaseRuleSelectionHelper.MODE_TILE)
 				{
 					int quantityofcases = Legup.getInstance().getPuzzleModule().numAcceptableStates(); 
 					for (int i = 1; i < quantityofcases; i++)
 					{
 						BoardState tmp = cur.addTransitionFrom();
 						tmp.setCaseSplitJustification(caseRules.get(button));
 						tmp.setCellContents(crsh.pointSelected.x,crsh.pointSelected.y,pm.getStateNumber(pm.getStateName(i)));
 						tmp.endTransition();
 					}
 				}
 				else if(crsh.mode == CaseRuleSelectionHelper.MODE_COL_ROW)
 				{
 					boolean row = (crsh.pointSelected.x == -1)? true : false;
 					int where = (row)? crsh.pointSelected.y : crsh.pointSelected.x;
 					int num_blanks = cur.numEmptySpaces(where,row);
 					int[] whatgoesintheblanks = new int[num_blanks];
 					for(int c1=0;c1<whatgoesintheblanks.length;c1++)
 					{
 						whatgoesintheblanks[c1] = 0;
 					}
 					int num_defaults = 0;
 					if(pm instanceof TreeTent)
 					{
 						//start with what the label says
 						int correct_tents = row?(TreeTent.translateNumTents(cur.getLabel(BoardState.LABEL_RIGHT,where))):(TreeTent.translateNumTents(cur.getLabel(BoardState.LABEL_BOTTOM,where)));
 						//subtract the amount of tents already in the row
 						for(int n=0;n<((row)?(cur.getWidth()):(cur.getHeight()));n++)
 						{
 							correct_tents -= (TreeTent.CELL_TENT == (cur.getCellContents(row?n:where,row?where:n)))?1:0;
 						}
 						//set the amount of defaults (grass) to the number of tiles that need to be filled minus the correct number of tents
 						num_defaults = num_blanks - correct_tents;
 					}
 					//System.out.println(num_defaults);
 					if(num_defaults < 0)return null; //state is a contradiction in a way that interferes with the construction of a caserule
 					while(Permutations.nextPermutation(whatgoesintheblanks,num_defaults))
 					{
 						BoardState tmp = cur.addTransitionFrom();
 						tmp.setCaseSplitJustification(caseRules.get(button));
 						tmp.fillBlanks(where,row,whatgoesintheblanks);
 						tmp.endTransition();
 					}
 				}
 				else if(crsh.mode == CaseRuleSelectionHelper.MODE_TILETYPE)
 				{
 					if(pm instanceof TreeTent)
 					{
 						if(caseRules.get(button) instanceof CaseLinkTree)
 						{
 							for(int c1=0;c1<4;c1++) //4: one for each orthagonal direction
 							{
 								int x = crsh.pointSelected.x + ((c1<2) ? ((c1%2 == 0)?-1:1) : 0);
 								int y = crsh.pointSelected.y + ((c1<2) ? 0 : ((c1%2 == 0)?-1:1));
 								if(x < 0 || x >= cur.getWidth() || y < 0 || y >= cur.getHeight())continue;
 								BoardState tmp = null;
 								if(cur.getCellContents(x,y) == TreeTent.CELL_UNKNOWN)
 								{
 									tmp = cur.addTransitionFrom();
 									tmp.setCaseSplitJustification(caseRules.get(button));
 									tmp.setCellContents(x,y,TreeTent.CELL_TENT);
 									/*for(int c2=0;c2<4;c2++)
 									{
 										if(c1 == c2)continue;
 										int x2 = crsh.pointSelected.x + ((c2<2) ? ((c2%2 == 0)?-1:1) : 0);
 										int y2 = crsh.pointSelected.y + ((c2<2) ? 0 : ((c2%2 == 0)?-1:1));
 										if(x2 < 0 || x2 >= cur.getWidth() || y2 < 0 || y2 >= cur.getHeight())continue;
 										if(cur.getCellContents(x2,y2) != TreeTent.CELL_UNKNOWN)continue;
 										tmp.setCellContents(x2,y2,TreeTent.CELL_GRASS);
 									}*/
 								}
 								else if(cur.getCellContents(x,y) == TreeTent.CELL_TENT)
 								{
 									tmp = cur.addTransitionFrom();
 									tmp.setCaseSplitJustification(caseRules.get(button));
 									/*for(int c2=0;c2<4;c2++)
 									{
 										if(c1 == c2)continue;
 										int x2 = crsh.pointSelected.x + ((c2<2) ? ((c2%2 == 0)?-1:1) : 0);
 										int y2 = crsh.pointSelected.y + ((c2<2) ? 0 : ((c2%2 == 0)?-1:1));
 										if(x2 < 0 || x2 >= cur.getWidth() || y2 < 0 || y2 >= cur.getHeight())continue;
 										if(cur.getCellContents(x2,y2) != TreeTent.CELL_UNKNOWN)continue;
 										tmp.setCellContents(x2,y2,TreeTent.CELL_GRASS);
 									}*/
 								}
 								else if(cur.getCellContents(x,y) != TreeTent.CELL_TENT)continue;
 								if(tmp != null)
 								{
 									ExtraTreeTentLink link = new ExtraTreeTentLink(new Point(x,y),crsh.pointSelected);
 									tmp.addExtraData(link);
 									tmp.extraDataDelta.add(link);
 									tmp.endTransition();
 								}
 							}
 						}
 						if(caseRules.get(button) instanceof CaseLinkTent)
 						{
 							for(int c1=0;c1<4;c1++) //4: one for each orthagonal direction
 							{
 								int x = crsh.pointSelected.x + ((c1<2) ? ((c1%2 == 0)?-1:1) : 0);
 								int y = crsh.pointSelected.y + ((c1<2) ? 0 : ((c1%2 == 0)?-1:1));
 								if(x < 0 || x >= cur.getWidth() || y < 0 || y >= cur.getHeight())continue;
 								if(cur.getCellContents(x,y) != TreeTent.CELL_TREE)continue;
								if(TreeTent.isLinked(cur.getExtraData(),new Point(x,y)))continue;
 								BoardState tmp = cur.addTransitionFrom();
 								tmp.setCaseSplitJustification(caseRules.get(button));
 								ExtraTreeTentLink link = new ExtraTreeTentLink(new Point(x,y),crsh.pointSelected);
 								tmp.addExtraData(link);
 								tmp.extraDataDelta.add(link);
 								tmp.endTransition();
 							}
 						}
 					}
 				}
 				if(pm instanceof LightUp)
 				{
 					if(caseRules.get(button) instanceof CaseSatisfyNumber)
 					{
 						int num_blanks = CaseLinkTree.calcAdjacentTiles(cur,crsh.pointSelected,LightUp.CELL_UNKNOWN);
 						int num_lights = CaseLinkTree.calcAdjacentTiles(cur,crsh.pointSelected,LightUp.CELL_LIGHT);
 						int num_lights_needed = CaseSatisfyNumber.getBlockValue(cur.getCellContents(crsh.pointSelected.x,crsh.pointSelected.y))-num_lights;
 						int num_empties = num_blanks - num_lights_needed;
 						int[] whatgoesintheblanks = new int[num_blanks];
 						for(int c1=0;c1<num_blanks;c1++)
 						{
 							whatgoesintheblanks[c1] = 0;
 						}
 						System.out.println(num_empties);
 						while(Permutations.nextPermutation(whatgoesintheblanks,num_empties))
 						{
 							BoardState tmp = cur.addTransitionFrom();
 							tmp.setCaseSplitJustification(caseRules.get(button));
 							int counter = 0;
 							for(int c3=0;c3<4;c3++)
 							{
 								int x = crsh.pointSelected.x + ((c3<2) ? ((c3%2 == 0)?-1:1) : 0);
 								int y = crsh.pointSelected.y + ((c3<2) ? 0 : ((c3%2 == 0)?-1:1));
 								if(x < 0 || x >= cur.getWidth() || y < 0 || y >= cur.getHeight())continue;
 								if(cur.getCellContents(x,y) != LightUp.CELL_UNKNOWN)continue;
 								tmp.setCellContents(x,y,pm.getStateNumber(pm.getStateName(whatgoesintheblanks[counter])));
 								++counter;
 							}
 							tmp.endTransition();
 						}
 					}
 				}
 				if((cur.getTransitionsFrom().size() > 0) && (cur.getTransitionsFrom().get(0) != null))
 				{
 					Legup.getInstance().getSelections().setSelection(new Selection(cur.getTransitionsFrom().get(0),false));
 				}
 			}
 		}
 		else
 		{
 			if(cur.getSingleParentState().getTransitionsFrom().size() <= 1)cur.setCaseSplitJustification(caseRules.get(button));
 			else
 			{
 				cur.setCaseSplitJustification(cur.getSingleParentState().getTransitionsFrom().get(0).getCaseRuleJustification());
 				if(cur.getSingleParentState().getTransitionsFrom().get(0).getCaseRuleJustification() != caseRules.get(button))
 				{
 					String msg = "Different case rules cannot be selected for the same branch set, the rule used for the first branch was used instead of the one selected.";
					//Legup.getInstance().getGui().showStatus(msg,true); //this won't work due to occuring before a transition change (which clears the status bar), if a clean workaround is thought of, possibly replace the modal box (JOptionPane) with this (same message)
 					JOptionPane.showMessageDialog(null,msg);
 					System.out.println(msg);
 				}
 			}
 		}
 		//Legup.getInstance().getSelections().setSelection(new Selection(cur.getTransitionsFrom().get(0), false));
 		return r;
 	}
 
 	@Override
 	protected void checkJustification(int button)
 	{
 		checkCaseRule(caseRules.get(button));
 	}
 
 	@Override
 	protected Justification doDefaultApplication(int index, BoardState state)
 	{
 		//We set the current default application so we know which to apply for later
 		CaseRule r = caseRules.get(index);
 		boolean legal = r.startDefaultApplication(state);
 
 		if (!legal)
 		{
 			parentFrame.setStatus(false, "There is not legal default application that can be applied.");
 			return null;
 		}
 		else
 		{
 			parentFrame.setStatus(true, r.getApplicationText());
 			Legup.getInstance().getPuzzleModule().defaultApplication = r;
 			this.defaultApplication = r;
 			return r;
 		}
 	}
 }
