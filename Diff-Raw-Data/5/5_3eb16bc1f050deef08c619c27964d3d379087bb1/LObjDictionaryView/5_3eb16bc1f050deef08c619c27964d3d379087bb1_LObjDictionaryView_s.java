 package org.concord.LabBook;
 
 import waba.ui.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import extra.util.*;
 import extra.ui.*;
 
 public class LObjDictionaryView extends LabObjectView 
     implements ActionListener, LObjViewContainer, DialogListener
 {
     TreeControl treeControl;
     TreeModel treeModel;
     RelativeContainer me = new RelativeContainer();
     LabObjectView lObjView = null;
 
     int newIndex = 0;
 
     LObjDictionary dict;
     GridContainer buttons = null; 
  
     Button doneButton = new Button("Done");
     Button newButton = new Button("New");
     Button openButton = new Button("Open");
     Button editButton = new Button("Edit");
     Button delButton = new Button("Del");
 
     Menu editMenu = new Menu("Edit");
     Menu viewMenu = new Menu("View");
 
     PropContainer creationProps = new PropContainer();
     PropContainer subCreateProps = creationProps.createSubContainer("Sub");
     String [] creationTypes = {"Folder", "Notes", "Questions", "Data Collector", "Drawing"};
     PropObject newObjType = new PropObject("Type", creationTypes);
 
     boolean editStatus = false;
 
     public LObjDictionaryView(LObjViewContainer vc, LObjDictionary d)
     {
 	super(vc);
 	dict = d;
 	lObj =dict;
 	add(me);
 	editMenu.add("Rename...");
 	editMenu.add("Properties...");
 	editMenu.add("Toggle hidden");
 	viewMenu.add("Paging View");
 	editMenu.addActionListener(this);
 	viewMenu.addActionListener(this);
 
 	if(vc != null){
 	    vc.addMenu(this, editMenu);
 	    vc.addMenu(this, viewMenu);
 	}
    	
 	creationProps.addProperty(newObjType, "Sub");
     }
 
     public void layout(boolean sDone)
     {
 	if(didLayout) return;
 	didLayout = true;
 
 	showDone = sDone;
 
 	treeModel = new TreeModel(dict);
 	treeControl = new TreeControl(treeModel);
 	treeControl.showRoot(false);
 	me.add(treeControl);
 
 	if(showDone){
 	    buttons = new GridContainer(4,1);
 	    buttons.add(doneButton, 3, 0);
 	} else {
 	    buttons = new GridContainer(3,1);
 	}
 	buttons.add(newButton, 0, 0);
 	buttons.add(openButton, 1, 0);
 	buttons.add(delButton, 2, 0);
 	me.add(buttons);
     }
 
     public void setRect(int x, int y, int width, int height)
     {
 	super.setRect(x,y,width,height);
 	if(!didLayout) layout(false);
 	
 	me.setRect(0,0, width, height);
 	treeControl.setRect(1,1,width-2, height-22);
 	Debug.println("Setting grid size: " + width + " " + height);
 	buttons.setRect(0,height-20,width,20);
     }
 
     Dialog newDialog = null;
 
     public void onEvent(Event e)
     {
 	if(e.type == ControlEvent.PRESSED){
 	    TreeNode curNode;
 	    TreeNode parent;	   
 
 	    LabObject newObj;
 	    if(e.target == newButton){
 		String [] buttons = {"Cancel", "Create"};
 		newDialog = Dialog.showInputDialog(this, "Create", "Create a new Object",
 						      buttons,Dialog.CHOICE_INP_DIALOG, creationTypes);
 
 	    } else if(e.target == delButton){
 		curNode = treeControl.getSelected();
 		if(curNode == null || curNode.toString().equals("..empty..")) return;
 		parent = treeControl.getSelectedParent();
 		treeModel.removeNodeFromParent(curNode, parent);
 	    } else if(e.target == openButton){
 		curNode = treeControl.getSelected();
 		if(curNode == null || curNode.toString().equals("..empty..")) return;
 		showPage(curNode, false);
 	    } else if(e.target == doneButton){
 		if(container != null){
 		    container.done(this);
 		}
 	    }	    
 	}
     }
 
     Dialog rnDialog = null;
 
     public void dialogClosed(DialogEvent e)
     {
 	String command = e.getActionCommand();
 	if(e.getSource() == newDialog){
 	    if(command.equals("Create")){
 		String objType = (String)e.getInfo();
 		LabObject newObj = null;
 		boolean autoEdit = false;
 		if(objType.equals("Folder")){
 		    newObj = new LObjDictionary();
 		} else if(objType.equals("Notes")){
 		    newObj = new LObjDocument();
 		    autoEdit = true;
 		} else if(objType.equals("Questions")){
 		    newObj = LObjQuestion.makeNewQuestionSet();
 		    autoEdit = true;
 		} else if(objType.equals("Data Collector")){	       
 		    LObjDataControl dc = LObjDataControl.makeNew();
 		    newObj = dc.dict;
 		    dc.dict.hideChildren = true;
 		    if(treeControl.getSelected() == null){
 			dc.setDataDict((LObjDictionary)treeModel.getRoot());
 		    } else {
 			dc.setDataDict((LObjDictionary)treeControl.getSelectedParent());
 		    }
 		    autoEdit = true;
 		} else if(objType.equals("Drawing")){
 		    newObj = new LObjDrawing();
 		    autoEdit = true;
 		}
 		if(newObj != null){
 		    TreeNode curNode = treeControl.getSelected();
 		    TreeNode parent = treeControl.getSelectedParent();
 		    if(newIndex == 0){
			newObj.name = "New_" + objType;		    
 		    } else {
			newObj.name = "New_" + objType + " " + newIndex;		    
 		    }
 		    newIndex++;
 		    TreeNode newNode = dict.getNode(newObj);
 		    if(curNode == null){
 			treeModel.insertNodeInto(newNode, treeModel.getRoot(), treeModel.getRoot().getChildCount());
 		    } else {
 			treeModel.insertNodeInto(newNode, parent, parent.getIndex(curNode)+1);
 		    }		    
 		    if(autoEdit){
 			showPage(newNode, true);
 
 		    }
 		}
 
 	    }
 	} else if(e.getSource() == rnDialog){
 	    if(command.equals("Ok")){
 		// This is a bug
 	       
 		TreeNode selObj = treeControl.getSelected();
 		if(selObj == null){
 		    dict.name = (String)e.getInfo();
 		    return;
 		}
 
 		if(selObj instanceof LabObject){
 		    ((LabObject)selObj).name = (String)e.getInfo();
 		    treeControl.repaint();
 		    dict.lBook.store(((LabObject)selObj));
 		    // repaint??
 		} else if(selObj instanceof LabObjectPtr){
 		    LabObject obj = dict.lBook.load((LabObjectPtr)selObj);
 		    if(obj != null){
 			obj.name = (String)e.getInfo();
 			dict.lBook.store(obj);
 		    }
 		}
 		treeControl.reparse();
 		treeControl.repaint();
 	    }
 	}		   
     }
 
     public void actionPerformed(ActionEvent e)
     {
 	String command;
 	Debug.println("Got action: " + e.getActionCommand());
 
 	if(e.getSource() == lObjView){
 	    if(e.getActionCommand().equals("Done")){
 		lObjView.close();
 		remove(lObjView);
 		add(me);
 	    }
 	} else if(e.getSource() == viewMenu){
 	    if(e.getActionCommand().equals("Paging View")){
 		dict.viewType = dict.PAGING_VIEW;
 		if(container != null){
 		    container.reload(this);
 		}
 	    }
 	} else if(e.getSource() == editMenu){	    
 	    if(e.getActionCommand().equals("Rename...")){
 		TreeNode selObj = treeControl.getSelected();
 		String [] buttons = {"Cancel", "Ok"};
 		if(selObj != null){
 		    if(selObj.toString().equals("..empty..")) return;
 		    rnDialog = Dialog.showInputDialog(this, "Rename Object", "Old Name was " + selObj.toString(),
 						      buttons,Dialog.EDIT_INP_DIALOG);
 		} else {
 		    rnDialog = Dialog.showInputDialog(this, "Rename Parent", "Old Name was " + dict.name,
 						      buttons,Dialog.EDIT_INP_DIALOG);
 		}		    
 	    } else if(e.getActionCommand().equals("Properties...")){
 		TreeNode curNode = treeControl.getSelected();
 		if(curNode == null || curNode.toString().equals("..empty..")) return;
 		showPage(curNode, true);
 	    } else if(e.getActionCommand().equals("Toggle hidden")){
 		LObjDictionary.globalHide = !LObjDictionary.globalHide;
 		if(container != null) container.reload(this);
 
 	    }
 	} 
     }
 
     public void showPage(TreeNode curNode, boolean edit)
     {
 	LabObject obj = null;
 	
 	if(curNode instanceof LabObjectPtr){
 	    obj = dict.lBook.load((LabObjectPtr)curNode);
 	    if(obj == null) Debug.println("showPage: object not in database: dI " +
 					       ((LabObjectPtr)curNode).devId + 
 					       " oI " + ((LabObjectPtr)curNode).objId);
 	} else if(curNode instanceof LObjDictionary){	
 	    obj = (LabObject)curNode;
 	}
 
 	if(obj == null){
 	    Debug.println("Weirdness in showPage");
 	    return;
 	}
 
 	delMenu(this, viewMenu);
 	delMenu(this, editMenu);
 
 	editStatus = edit;
 	lObjView = obj.getView(this, edit);
 
 	if(lObjView == null){
 	    addMenu(this, editMenu);
 	    addMenu(this, viewMenu);
 	    return;
 	}
 	remove(me);
         lObjView.layout(true);
 	lObjView.setRect(x,y,width,height);
 	add(lObjView);
 
     }
 
     public void addMenu(LabObjectView source, org.concord.waba.extra.ui.Menu menu)
     {
 	if(container != null) container.addMenu(this, menu);
     }
     
     public void delMenu(LabObjectView source, org.concord.waba.extra.ui.Menu menu)
     {
 	if(container != null) container.delMenu(this, menu);
     }
 
     public void done(LabObjectView source)
     {
 	if(source == lObjView){
 	    lObjView.close();
 	    dict.lBook.store(lObjView.lObj);
 	    // I might want to do a commit here lets try it....
 	    // of course if we are embedded this might be a problem
 	    dict.lBook.commit();
 
 	    remove(lObjView);
 	    treeControl.reparse();
 	    add(me);
 	    addMenu(this, editMenu);
 	    addMenu(this, viewMenu);
 	    lObjView = null;
 	}
 	//	System.gc();
     }
 
     public void reload(LabObjectView source)
     {
 	if(source == lObjView){
 	    LabObject obj = source.getLabObject();
 	    lObjView.close();
 	    remove(lObjView);
 	    
 	    lObjView = obj.getView(this, editStatus);
 	    lObjView.layout(true);
 	    lObjView.setRect(x,y,width,height);
 	    add(lObjView);
 	}
     }
 
     public LObjDictionary getDict()
     {
 	return dict;
     }
 
     public void close()
     {
 	
 	if(container != null){
 	    container.delMenu(this,editMenu);
 	    container.delMenu(this,viewMenu);
 	}
 
 	// Commit ???
 	// Store ??
     }
 
 }
