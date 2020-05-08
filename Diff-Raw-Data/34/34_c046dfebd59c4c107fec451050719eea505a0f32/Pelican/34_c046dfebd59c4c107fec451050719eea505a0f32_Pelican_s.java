 package edu.umb.jsPedigrees.client.Pelican;
 /********************************************************************
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Library General Public
  *  License as published by the Free Software Foundation; either
  *  version 2 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Library General Public License for more details.
  *
  *  You should have received a copy of the GNU Library General Public
  *  License along with this library; if not, write to the
  *  Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  *  Boston, MA  02111-1307, USA.
  *
  *  @author Copyright (C) Frank Dudbridge
  *  modified 2012 Brian White
  *
  ********************************************************************/
 
 //package uk.ac.mrc.rfcgr;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Vector;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.umb.jsPedigrees.client.PE.PedigreeSolution;
 import edu.umb.jsPedigrees.client.PE.PedigreeSolver;
 
 public class Pelican extends AbsolutePanel implements ClickHandler {
 
 	public static int PEDIGREE_SIZE = 600;
 	public static String PEDIGREE_SIZE_STR = String.valueOf(PEDIGREE_SIZE + "px");
 
 	public static MenuBar menuBar;
 	private MenuItem undoItem;
 	private MenuItem redoItem;
 	private PopupPanel popup;
 	private PelicanPerson currentPerson;
 	private int currentId;
 	private boolean pedHasChanged;
 	private MenuItem Parents;
 	private Vector<Vector<PelicanPerson>> history;
 	private int historyPosition;
 
 	private HashSet<String> matingList;
 
 	/*
 	 * array for determining where the user has activated the context menu
 	 * 	(right-clicked)
 	 * 
 	 * it is pixel-for-pixel the drawing canvas
 	 * 
 	 * background = 0
 	 * if a PelicanPerson is there, then the cells under that person have his/her id#
 	 */
 	private int[][] screen; 
 
 	/* {{{ constructor (popup menu) */
 
 	public Pelican(RootPanel rootPanel) {
 		super();
 		setSize(PEDIGREE_SIZE_STR, PEDIGREE_SIZE_STR);
 		setStyleName("jsPX-canvas");
 
 		makeMenus(rootPanel);
 
 		history = new Vector<Vector<PelicanPerson>>();
 		historyPosition = 0;
 		matingList = new HashSet<String>();
 
 		addDomHandler(this, ClickEvent.getType());
 
 		exportNewPedigree(this);
 		exportSolvePedigree(this);
 
 		Scheduler.get().scheduleDeferred(new Command() { 
 			public void execute() {
 				notifyHostpage();
 			}
 		});
 	}
 
 	public static native void notifyHostpage() /*-{
 	if (typeof $wnd.pedexIsReady === 'function') 
 		$wnd.pedexIsReady();
 	}-*/;
 
 	private static native void exportNewPedigree(Pelican p) /*-{
 		$wnd.pedexNewPedigree = $entry(function() {return p.@edu.umb.jsPedigrees.client.Pelican.Pelican::newPedigree()();});
     }-*/;
 
 	private static native void exportSolvePedigree(Pelican p) /*-{
 		$wnd.pedexSolvePedigree = $entry(function() {return p.@edu.umb.jsPedigrees.client.Pelican.Pelican::solvePedigree()();});
 	}-*/;
 
 
 	private void makeMenus(RootPanel rootPanel) {
 		// set up the popup menu that appears when you click on a person
 		popup = new PopupPanel();
 
 		MenuBar personMenu = new MenuBar(true);
 
 		MenuBar addMenu = new MenuBar(true);
 		addMenu.addItem("1 son", new Command() {
 			public void execute() {
 				addChildren("1 son");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("1 daughter", new Command() {
 			public void execute() {
 				addChildren("1 daughter");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("2 sons", new Command() {
 			public void execute() {
 				addChildren("2 sons");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("2 daughters", new Command() {
 			public void execute() {
 				addChildren("2 daughters");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("3 sons", new Command() {
 			public void execute() {
 				addChildren("3 sons");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("3 daughters", new Command() {
 			public void execute() {
 				addChildren("3 daughters");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("Spouse+son", new Command() {
 			public void execute() {
 				addSpouse("Spouse+son");
 				popup.hide();
 			}
 		});
 		addMenu.addItem("Spouse+daughter", new Command() {
 			public void execute() {
 				addSpouse("Spouse+daughter");
 				popup.hide();
 			}
 		});
 		Parents = new MenuItem("Parents", new Command() {
 			public void execute() {
 				addParents();
 				popup.hide();
 			}
 		});
 		addMenu.addItem(Parents);
 		personMenu.addItem("Add", addMenu);
 
 		MenuBar changeMenu = new MenuBar(true);
 
 		MenuBar changeAff = new MenuBar(true);
 		changeAff.addItem("Affected", new Command() {
 			public void execute() {
 				currentPerson.affection = PelicanPerson.affected;
 				updateDisplay();
 				popup.hide();
 			}
 		});
 		changeAff.addItem("Unaffected", new Command() {
 			public void execute() {
 				currentPerson.affection = PelicanPerson.unaffected;
 				updateDisplay();
 				popup.hide();
 			}
 		});
 		changeMenu.addItem("Affection", changeAff);
 
 		MenuBar changeSex = new MenuBar(true);
 		changeSex.addItem("Male", new Command() {
 			public void execute() {
 				currentPerson.sex = PelicanPerson.male;
 				updateDisplay();
 				popup.hide();
 			}
 		});
 		changeSex.addItem("Female", new Command() {
 			public void execute() {
 				currentPerson.sex = PelicanPerson.female;
 				updateDisplay();
 				popup.hide();
 			}
 		});
 		changeMenu.addItem("Sex", changeSex);
 
 		personMenu.addItem("Change", changeMenu);
 
 		personMenu.addItem("Delete", new Command() {
 			public void execute() {
 				deletePerson(currentPerson);
 				popup.hide();
 			}
 		});
 
 		personMenu.addItem("Cancel", new Command() {
 			public void execute() {
 				popup.hide();
 			}
 		});
 
 		MenuBar popupMenu = new MenuBar();
 		popupMenu.addItem("Edit", personMenu);
 		popupMenu.setAutoOpen(true);
 		popup.add(popupMenu);
 
 		// main menu
 		MenuBar mainMenu = new MenuBar();
 		mainMenu.setWidth("50px");
 		mainMenu.setAnimationEnabled(true);
 
 		MenuBar editMenu = new MenuBar(true);
 		editMenu.setAnimationEnabled(true);
 
 		editMenu.addItem("New Pedigree", new Command() {
 			public void execute() {
 				newPedigree();
 				updateDisplay();
 			}
 		});
 		undoItem = new MenuItem("Undo", new Command() {
 			public void execute() {
 				if (historyPosition > 1) {
 					historyPosition--;
 					Vector<PelicanPerson> savedPed = (Vector<PelicanPerson>)history.elementAt(historyPosition - 1);
 					loadPedigree(savedPed);
 					pedHasChanged = true;
 					updateDisplay();
 				}
 			}
 		});
 		editMenu.addItem(undoItem);
 		redoItem = new MenuItem("Redo", new Command() {
 			public void execute() {
 				if (historyPosition < history.size()) {
 					historyPosition++;
 					Vector<PelicanPerson> savedPed = (Vector<PelicanPerson>)history.elementAt(historyPosition - 1);
 					loadPedigree(savedPed);
 					pedHasChanged = true;
 					updateDisplay();
 				}
 			}
 		});
 		editMenu.addItem(redoItem);
 		editMenu.addItem("Renumber", new Command() {
 			public void execute() {
 				renumberAll();
 				updateDisplay();
 			}
 		});
 		mainMenu.addItem(new MenuItem("Edit", editMenu));
 
 		rootPanel.add(mainMenu);
 
 	}
 
 	private void newPedigree() {
 		// start out with a single female
 		clear();
 		currentId = 1;
 		add(new PelicanPerson(this, currentId++, PelicanPerson.female, 0));
 		savePedigree();
 		pedHasChanged = true;
 		updateDisplay();
 	}
 
 	// save current pedigree in the history, and clear the future
 	private void savePedigree() {
 		Vector<PelicanPerson> savedPed = new Vector<PelicanPerson>();
 		HashMap<Integer, PelicanPerson> idMap = new HashMap<Integer, PelicanPerson>();
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson p = (PelicanPerson)getWidget(i);
 				savedPed.add(new PelicanPerson(p));
 				idMap.put(new Integer(p.id), (PelicanPerson)savedPed.lastElement());
 			}
 		for(int i=0;i<savedPed.size();i++) {
 			PelicanPerson p=(PelicanPerson)savedPed.elementAt(i);
 			if (p.father!=null)
 				p.father=(PelicanPerson)idMap.get(p.father.id);
 			if (p.mother!=null)
 				p.mother=(PelicanPerson)idMap.get(p.mother.id);
 		}
 		while(history.size()>historyPosition)
 			history.remove(history.lastElement());
 		history.add(savedPed);
 		historyPosition++;
 	}
 
 
 	// load pedigree from the history
 	private void loadPedigree(Vector<PelicanPerson> savedPed) {
 		clear();
 		HashMap<Integer, PelicanPerson> idMap = new HashMap<Integer, PelicanPerson>();
 		for(int i = 0; i < savedPed.size() ; i++) {
 			PelicanPerson p=(PelicanPerson)savedPed.elementAt(i);
 			PelicanPerson person=new PelicanPerson(p);
 			add(person);
 			idMap.put(new Integer(p.id),person);
 		}
 		for(int i = 0; i < getWidgetCount(); i++) {
 			PelicanPerson p = (PelicanPerson)getWidget(i);
 			if (p.father!=null)
 				//gww		p.father=(PelicanPerson)idMap.get(new Integer(p.father.id));
 				p.father=(PelicanPerson)idMap.get(p.father.id);
 			if (p.mother!=null)
 				//gww		p.mother=(PelicanPerson)idMap.get(new Integer(p.mother.id));
 				p.mother=(PelicanPerson)idMap.get(p.mother.id);
 		}
 		updateDisplay();
 	}
 
 	/* }}} */
 
 	/* {{{ updateDisplay */
 
 	public void updateDisplay() {
 		savePedigree();
 		pedHasChanged = true;
 		drawPedigree();
 	}
 
 	/* }}} */
 
 	/* {{{ areSpouses */
 
 	private boolean areSpouses(PelicanPerson person1,PelicanPerson person2) {
 		if (person1==null || person2==null) return(false);
 		// construct unique set entry from the id strings concatenated with a space
 		if (matingList.contains(person1.id+" "+person2.id) ||
 				matingList.contains(person2.id+" "+person1.id))
 			return(true);
 		return(false);
 	}
 
 	/* }}} */
 
 	/* {{{ isChild */
 
 	private boolean isChild(PelicanPerson child,PelicanPerson person1,PelicanPerson person2) {
 		if (child.father==person1 && child.mother==person2 ||
 				child.mother==person1 && child.father==person2)
 			return(true);
 		return(false);
 	}
 
 	/* }}} */
 
 	/* {{{ areSibs */
 
 	private boolean areSibs(PelicanPerson person1,PelicanPerson person2) {
 		if (person1.father==person2.father && person1.mother==person2.mother)
 			return(true);
 		return(false);
 	}
 
 	/* }}} */
 
 	/* {{{ isAncestor */
 
 	private boolean isAncestor(PelicanPerson parent,PelicanPerson child) {
 		if (child.father==parent || child.mother==parent)
 			return(true);
 		if (child.father!=null) {
 			if (isAncestor(parent,child.father)) return(true);
 		}
 		if (child.mother!=null) {
 			if (isAncestor(parent,child.mother)) return(true);
 		}
 		return(false);
 	}
 
 	/* }}} */
 
 	/* {{{ addParents */
 
 	private void addParents() {
 		// new father
 		currentPerson.father=new PelicanPerson(this, currentId++, PelicanPerson.male, currentPerson.generation-1);
 		add(currentPerson.father);
 		// new mother
 		currentPerson.mother=new PelicanPerson(this, currentId++, PelicanPerson.female, currentPerson.generation-1);
 		add(currentPerson.mother);
 
 		updateDisplay();
 	}
 
 
 	/* {{{ addChildren */
 
 	private void addChildren(String request) {
 
 		// find spouse for this subject
 		PelicanPerson spouse=null;
 		for(int i = 0;i< getWidgetCount() && spouse == null; i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson person = (PelicanPerson)getWidget(i);
 				if (areSpouses(person,currentPerson))
 					spouse=person;
 			}
 		// create a spouse
 		if (spouse==null) {
 			if (currentPerson.sex==PelicanPerson.female)
 				spouse=new PelicanPerson(this, currentId++, PelicanPerson.male, currentPerson.generation);
 			else
 				spouse=new PelicanPerson(this, currentId++, PelicanPerson.female, currentPerson.generation);
 			add(spouse);
 		}
 
 		// add in the children
 		int generation=Math.max(currentPerson.generation,spouse.generation);
 		for(int i=0;i<Integer.parseInt(request.substring(0,1));i++) {
 			if (currentPerson.sex==PelicanPerson.female) {
 				if (request.charAt(2)=='s')
 					add(new PelicanPerson(this, currentId++, spouse, currentPerson, PelicanPerson.male, generation+1));
 				else
 					add(new PelicanPerson(this, currentId++, spouse, currentPerson, PelicanPerson.female, generation+1));
 			}
 			else {
 				if (request.charAt(2)=='s')
 					add(new PelicanPerson(this, currentId++, currentPerson, spouse, PelicanPerson.male, generation+1));
 				else
 					add(new PelicanPerson(this, currentId++, currentPerson, spouse, PelicanPerson.female, generation+1));
 			}
 		}
 
 		updateDisplay();
 	}
 
 	/* }}} */
 
 	/* {{{ addSpouse */
 
 	private void addSpouse(String request) {
 
 		// create a spouse
 		int spouseId=currentId++;
 		PelicanPerson spouse;
 		if (currentPerson.sex==PelicanPerson.female) {
 			spouse=new PelicanPerson(this, spouseId, PelicanPerson.male, currentPerson.generation);
 			add(spouse);
 		}
 		else {
 			spouse=new PelicanPerson(this, spouseId, PelicanPerson.female, currentPerson.generation);
 			add(spouse);
 		}
 		// add in the child
 		if (currentPerson.sex==PelicanPerson.female) {
 			if (request.charAt(7)=='s')
 				add(new PelicanPerson(this, currentId++, spouse, currentPerson, PelicanPerson.male, currentPerson.generation+1));
 			else
 				add(new PelicanPerson(this, currentId++, spouse, currentPerson, PelicanPerson.female, currentPerson.generation+1));
 		}
 		else {
 			if (request.charAt(7)=='s')
 				add(new PelicanPerson(this, currentId++, currentPerson, spouse, PelicanPerson.male, currentPerson.generation+1));
 			else
 				add(new PelicanPerson(this, currentId++, currentPerson, spouse, PelicanPerson.female, currentPerson.generation+1));
 		}
 
 		updateDisplay();
 	}
 
 	/* }}} */
 
 	/* {{{ changeId */
 
 	// swaps the Id's for two subjects
 	private void changeId(int oldId, int newId) {
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson person = (PelicanPerson)getWidget(i);
 				if (person.id == oldId) person.id = newId;
 				else if (person.id == newId) person.id = oldId;
 			}
 	}
 
 
 	// renumber the subjects, top-down, left-right
 	public void renumberAll() {
 		boolean someChange=false;
 
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson person = (PelicanPerson)getWidget(i);
 				int thisId = 0;
 				for(int j = 0; j < getWidgetCount(); j++)
 					if (getWidget(j) instanceof PelicanPerson) {
 						PelicanPerson person2 = (PelicanPerson)getWidget(j);
 						if (person2.generation<person.generation ||
 								person2.generation==person.generation &&
 								getWidgetLeft(person2) <= getWidgetLeft(person))
 							thisId++;
 					}
 				if (person.id!=thisId) {
 					changeId(person.id,thisId);
 					someChange=true;
 				}
 			}
 		if (someChange)
 			updateDisplay();
 	}
 
 
 
 	/* {{{ deletePerson */
 
 	// recursively mark a subject and its descendents for deletion
 	private void markForDeletion(PelicanPerson person) {
 		// remove all children
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson p = (PelicanPerson)getWidget(i);
 				if (p.father == person || p.mother == person)
 					markForDeletion(p);
 			}
 		// remove orphan spouses, who have no other spouses of their own
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson p = (PelicanPerson)getWidget(i);
 				if (areSpouses(p,person) && p.isOrphan()) {
 					boolean soleMate=true;
 					for(int j = 0; j < getWidgetCount();j++)
 						if (getWidget(j) instanceof PelicanPerson) {
 							PelicanPerson pp = (PelicanPerson)getWidget(j);
 							if (areSpouses(p,pp) && pp != person)
 								soleMate = false;
 						}
 					if (soleMate) p.laidOut=true;
 				}
 			}
 		// remove this subject
 		person.laidOut=true;
 	}
 
 	// main routine to delete a person
 	private void deletePerson(PelicanPerson person) {
 		// check if the person has children
 		boolean hasChild = false;
 		boolean hasPaternalSibs = false;
 		boolean hasMaternalSibs = false;
 		for(int i = 0; i < getWidgetCount();i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				PelicanPerson p = (PelicanPerson)getWidget(i);
 				if (p.father==person || p.mother==person)
 					hasChild=true;
 				if (p!=person && p.father==person.father)
 					hasPaternalSibs=true;
 				if (p!=person && p.mother==person.mother)
 					hasMaternalSibs=true;
 			}
 		if (!hasChild && hasPaternalSibs && hasMaternalSibs) {
 			remove(person);
 			updateDisplay();
 			return;
 		}
 
 
 		// here, laidOut indicates whether a subject is marked for deletion 
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson)
 				((PelicanPerson)getWidget(i)).laidOut=false;
 
 		markForDeletion(person);
 		if (person.hasMother())
 			if (person.mother.isOrphan() && !hasMaternalSibs)
 				person.mother.laidOut=true;
 		if (person.hasFather())
 			if (person.father.isOrphan() && !hasPaternalSibs)
 				person.father.laidOut=true;
 
 		boolean empty=true;
 		for(int i = 0; i < getWidgetCount();i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				if (((PelicanPerson)getWidget(i)).laidOut) {
 					remove(i);
 					i--;
 				}
 				else empty=false;
 			}
 		if (empty) newPedigree();
 		updateDisplay();
 	}
 
 	private PelicanPerson getPersonById(int id) {
 		PelicanPerson[] people = getAllPeople();
 		for (int i = 0; i < people.length; i++) {
 			if(people[i].id == id) return people[i];
 		}
 		return null;
 	}
 
 	/* {{{ layoutPerson */
 
 	// recursively lay out a person
 	// returns: number of horizontal units taken up by its nuclear family
 	private double layoutPerson(PelicanPerson person,int across) {
 		int offspringSpace=0;
 		int spouseSpace=0;
 		PelicanPerson spouse=null;
 		int verticalOffset = 0;
 
 		// lay out with the first spouse
 		boolean haveSpouse=false;
 		boolean hasSibs=false;
 		for(int i=0;i<getWidgetCount() && !haveSpouse;i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				spouse=(PelicanPerson)getWidget(i);
 				PelicanPerson lastChild=null;
 				if (areSpouses(spouse,person) /* && spouse.isOrphan() */ /*&&
 					     !spouse.laidOut*/) {
 					for(int j=0;j<getWidgetCount();j++)
 						if (getWidget(j) instanceof PelicanPerson) {
 							PelicanPerson child=(PelicanPerson)getWidget(j);
 							if (isChild(child,person,spouse)) {
 								offspringSpace+=layoutPerson(child,across+offspringSpace);
 								lastChild=child;
 							}
 						}
 					haveSpouse=true;
 				}
 				if (areSibs(spouse,person) && spouse!=person) hasSibs=true;
 			}
 		// place to the left of the spouse
 		if (!person.laidOut) {
 			if (offspringSpace > 1) {
 				setWidgetPosition(person,
 						PelicanPerson.xSpace * (2 * across + offspringSpace - 2)/2,
 						(PelicanPerson.ySpace+verticalOffset) * person.generation);
 			} else {
 				setWidgetPosition(person,
 						PelicanPerson.xSpace * across,
 						(PelicanPerson.ySpace + verticalOffset) * person.generation);
 			}
 			// move a singleton child across by half a space
 			if (!hasSibs && !haveSpouse) {
 				if ((person.father==null || !person.father.laidOut) &&
 						(person.mother==null || !person.mother.laidOut)) {
 					setWidgetPosition(person,
 							getWidgetLeft(person) + PelicanPerson.xSpace/2,
 							getWidgetTop(person));
 				}
 			}
 			person.laidOut=true;
 			spouseSpace++;
 		}
 		// place the spouse
 		if (haveSpouse && !spouse.laidOut && spouse.generation==person.generation) {
 			if (offspringSpace>1) {
 				setWidgetPosition(spouse,
 						PelicanPerson.xSpace * (2 * across + offspringSpace - 2 + spouseSpace * 2)/2,
 						(PelicanPerson.ySpace + verticalOffset) * person.generation);
 			} else {
 				setWidgetPosition(spouse, 
 						PelicanPerson.xSpace * (across + spouseSpace), 
 						(PelicanPerson.ySpace + verticalOffset) * person.generation);
 			}
 			spouse.laidOut=true;
 			spouseSpace++;
 		}
 
 		int maxSpace=(offspringSpace>spouseSpace)?offspringSpace:spouseSpace;
 
 		// lay out with other spouses
 		for(int i=0;i<getWidgetCount();i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				spouse=(PelicanPerson)getWidget(i);
 				spouseSpace=0;
 				offspringSpace=0;
 				if (areSpouses(spouse,person) &&
 						(spouse.isOrphan() || !spouse.laidOut)) {
 					for(int j=0;j<getWidgetCount();j++)
 						if (getWidget(j) instanceof PelicanPerson) {
 							PelicanPerson child=(PelicanPerson)getWidget(j);
 							if (isChild(child,person,spouse))
 								offspringSpace+=layoutPerson(child,across+maxSpace+offspringSpace);
 						}
 					// place the spouse
 					if (!spouse.laidOut && spouse.generation==person.generation) {
 						setWidgetPosition(spouse, 
 								PelicanPerson.xSpace * (2 * (across + maxSpace) + offspringSpace - 1)/2, 
 								(PelicanPerson.ySpace + verticalOffset) * person.generation);
 						spouse.laidOut=true;
 						spouseSpace++;
 					}
 					maxSpace+=(offspringSpace>spouseSpace)?offspringSpace:spouseSpace;
 				}
 			}
 
 		// System.out.println("ID "+person.id+" max "+String.valueOf(maxSpace));
 
 		return(maxSpace);
 	}
 
 
 	public PelicanPerson[] getAllPeople() {
 		ArrayList<PelicanPerson> people = new ArrayList<PelicanPerson>();
 		for (int i = 0; i < getWidgetCount(); i++) {
 			if (getWidget(i) instanceof PelicanPerson) {
 				people.add((PelicanPerson)getWidget(i));
 			}
 		}
 		return people.toArray(new PelicanPerson[people.size()]);
 	}
 
 	public HashSet<String> getMatingList() {
 		return matingList;
 	}
 
 	/* {{{ paintComponent: draw the pedigree */
 
 	// draw the pedigree
 	public void drawPedigree() {
 
 		if (!pedHasChanged) return;
 
 		undoItem.setEnabled(historyPosition>1);
 		redoItem.setEnabled(historyPosition!=history.size());
 
 		PelicanPerson[] people = getAllPeople();
 		int numPeople = people.length;
 
 		// Initialise: nobody laid out, orphans are root subjects
 		for(int i=0;i<numPeople;i++) {
 			people[i].laidOut = false;
 			people[i].root = people[i].isOrphan();
 		}
 
 		// Make list of matings and ensure roots have orphan spouses
 		matingList.clear();
 		for(int i=0;i<numPeople;i++) {
 			PelicanPerson person = people[i];
 			if (person.father!=null && person.mother!=null)
 				//gww                  matingList.add(new Dimension(person.father.id,person.mother.id));
 				matingList.add(person.father.id+" "+person.mother.id);
 			if (person.father!=null && !person.father.isOrphan())
 				if (person.mother!=null && person.mother.generation>=person.father.generation)
 					person.mother.root=false;
 			if (person.mother!=null && !person.mother.isOrphan())
 				if (person.father!=null && person.father.generation>=person.mother.generation)
 					person.father.root=false;
 		}
 
 		// lay out the root subjects
 		// person is a root if it has spouses which are all orphans
 		int rootSpace=0;
 		for(int i=0;i<numPeople;i++) {
 			PelicanPerson person = people[i];
 			if (!person.laidOut && person.isOrphan() &&
 					person.isRoot()) {
 				rootSpace+=layoutPerson(person,rootSpace);
 			}
 		}
 
 		// normalise x-y locations
 		for(int i=0;i<getWidgetCount();i++) {
 			if (!(getWidget(i) instanceof PelicanPerson)) remove(i);
 		}
 		int minx=0;
 		int miny=0;
 		int maxx=0;
 		int maxy=0;
 		for(int i=0;i<getWidgetCount();i++) {
 			Widget c = getWidget(i);
 			if (i==0 || getWidgetLeft(c) < minx) minx = getWidgetLeft(c);
 			if (i==0 || getWidgetTop(c)<miny) miny=getWidgetTop(c);
 			if (i==0 || getWidgetLeft(c)>maxx) maxx=getWidgetLeft(c);
 			if (i==0 || getWidgetTop(c)>maxy) maxy=getWidgetTop(c);
 		}
 
 		// set up array to tell where clicks land
 		screen = new int[PEDIGREE_SIZE][PEDIGREE_SIZE];		// initializes all to 0
 
 		for(int i=0;i<getWidgetCount();i++) {
 			Widget c = getWidget(i);
 			// if pedigree fits in the frame, then centre it
 			// otherwise start it at (0,0)
 			int px, py;
 			if (maxx-minx+PelicanPerson.xSpace < getOffsetWidth()) {
 				px = getWidgetLeft(c)-(maxx + minx - getOffsetWidth() + PelicanPerson.symbolSize)/2;
 				py = getWidgetTop(c)-miny+PelicanPerson.symbolSize/2;
 			} else {
 				px = getWidgetLeft(c)-minx+PelicanPerson.symbolSize/2;
 				py = getWidgetTop(c)-miny+PelicanPerson.symbolSize/2;
 			}
 			setWidgetPosition(c, px, py);
 
 			if (c instanceof PelicanPerson) {
 				PelicanPerson p = (PelicanPerson)c;
 				p.drawSymbol();
 				for (int x = px; x < (px + p.symbolSize); x++) {
 					for (int y = py; y < (py + p.symbolSize); y++) {
 						screen[x][y] = p.id;
 					}
 				}
 			}
 		}
 
 		// draw the lines on the graph
 		insert(PelicanLines.drawLines(this),0,0,1);
 
 		setSize(String.valueOf(maxx - minx + PelicanPerson.xSpace + PelicanPerson.symbolSize/2),
 				String.valueOf(maxy - miny + PelicanPerson.ySpace + PelicanPerson.symbolSize/2));
 
 		setVisible(true);
 
 		currentId = 0;
 		// need to check if this is a valid integer ID 
 		for(int i = 0; i < getWidgetCount(); i++)
 			if (getWidget(i) instanceof PelicanPerson) {
 				// if 'id' is a valid integer
 				int id = ((PelicanPerson)getWidget(i)).id;
 				if (id > 0)
 					currentId=Math.max(currentId,((PelicanPerson)getWidget(i)).id);
 			}
 		currentId++;
 		currentPerson=null;
 		pedHasChanged=false;    
 	}
 
 	/* }}} */
 
 
 
 	/* {{{ checkIntegrity */
 
 	// check that fathers are male, etc
 	private String checkIntegrity(Vector pedigree) {
 		StringBuffer b = new StringBuffer();
 
 		for(int i=0;i<pedigree.size();i++) {
 			PelicanPerson person=(PelicanPerson)pedigree.elementAt(i);
 			boolean fatherError=false;
 			boolean motherError=false;
 			for(int j=0;j<pedigree.size();j++) {
 				PelicanPerson parent=(PelicanPerson)pedigree.elementAt(j);
 				if (parent==person.father && parent.sex==PelicanPerson.female)
 					fatherError=true;
 				if (parent==person.mother && parent.sex==PelicanPerson.male)
 					motherError=true;
 			}
 			// perhaps the pid/mid fields are swapped?
 			if (fatherError && motherError) {
 				b.append("Subject "+person.id+" has a male mother and female father.\n");
 			} else {
 				if (fatherError) {
 					b.append("Subject "+person.id+" has a female father.\n");
 				}
 				if (motherError) {
 					b.append("Subject "+person.id+" has a male mother.\n");
 				}
 			}
 		}
 		return b.toString();
 	}
 
 	/**
 	 *
 	 * Popup menu listener
 	 *
 	 */
 	public void onClick(ClickEvent event) {
 		int x = event.getX();
 		int y = event.getY();
 		if (screen[x][y] != 0) {
 			currentPerson = getPersonById(screen[x][y]);
 			popup.showRelativeTo(currentPerson);
 			popup.show();
 		} else {
 			popup.hide();
 		}
 
 	}
 
 
 	public String solvePedigree() {
 		renumberAll();
 		updateDisplay();
 
 		String integrity = checkIntegrity(history.lastElement());
 		if (integrity.equals("")) {
 			StringBuffer b = new StringBuffer();
 			PedigreeSolver ps = new PedigreeSolver(getAllPeople(), getMatingList());
 			PedigreeSolution sol = ps.solve();
			b.append(sol);
 			PedigreeSolution consolidatedSol = ps.consolidate(sol);
 			b.append(consolidatedSol + "\n");
 			return b.toString();
 		} else {
 			return integrity;
 		}
 
 	}
 }
