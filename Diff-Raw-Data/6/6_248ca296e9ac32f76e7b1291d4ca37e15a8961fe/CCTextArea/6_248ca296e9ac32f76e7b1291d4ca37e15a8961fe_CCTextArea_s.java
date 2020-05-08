 package org.concord.LabBook;
 
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import waba.ui.*;
 import waba.fx.*;
 import waba.sys.*;
 import waba.util.Vector;
 import extra.io.*;
 
 
 class CCTextAreaChooser extends LabBookChooser{
 EmbedObjectPropertyControl		objProperty;
 
 
 	public CCTextAreaChooser(ExtraMainWindow owner,LObjDictionary dict,ViewContainer viewContainer,DialogListener l){
 		super(owner,dict,viewContainer,l);
 	}
 	public void setContent(){
 		Rect r = getContentPane().getRect();
 		super.setContent();
 		if(view != null) view.setRect(0,0,r.width,r.height - 52);
 		if(objProperty == null){
 			objProperty = new EmbedObjectPropertyControl(null);
 			objProperty.layout(false);
 			getContentPane().add(objProperty);
 			objProperty.setRect(0,r.height - 55, r.width, 37);
 		}
 		
 		
 	}
     public void onEvent(Event e){
     	LabObject  obj = null;
     	boolean	   doNotify = false;
     	if(e.type == EmbedObjectPropertyControl.NEED_DEFAULT_SIZE){
 			if(view == null) return;
 			TreeNode curNode = view.treeControl.getSelected();
 			obj = dict.getObj(curNode);	
 			if(obj == null) return;
 			LabObjectView objView = obj.getView(null,false);
 			if(objView == null) return;
 			extra.ui.Dimension d = objView.getPreferredSize();
 			if(d == null) return;
 			if(d.width > 0){
 				objProperty.widthEdit.setText(""+d.width);
 				objProperty.lastW = d.width;
 			}
 			if(d.height > 0){
 				objProperty.heightEdit.setText(""+d.height);
 				objProperty.lastH = d.height;
 			}
     		return;
     	}
     	
 		if(e.type == TreeControl.DOUBLE_CLICK){
 			if(e.target instanceof TreeControl){
 				TreeControl tc = (TreeControl)e.target;
 			    TreeNode curNode = tc.getSelected();
 				obj = dict.getObj(curNode);	
 				doNotify = true;
 			}
 
 		}else if(e.type == ControlEvent.PRESSED && e.target == cancelButton){
 			hide();
 		}else if(e.type == ControlEvent.PRESSED && e.target == choiceButton){
 			if(view != null){
 				TreeNode curNode = view.treeControl.getSelected();
 				if(curNode != null){
 					obj = dict.getObj(curNode);	
 				}		
 			}
 			doNotify = true;
 		}
 		if(doNotify){
 			if(obj != null && listener != null && objProperty != null){
 				boolean wrap = objProperty.wrapCheck.getChecked();
 				objProperty.lastWrap = wrap;
 				int alighn = LBCompDesc.ALIGNMENT_LEFT;
 				if(objProperty.alignmentChoice != null){
 					objProperty.lastAlighnLeft = true;
 					if(objProperty.alignmentChoice.getSelected().equals("Right")){
 						objProperty.lastAlighnLeft = false;
 						alighn = LBCompDesc.ALIGNMENT_RIGHT;
 					}
 				}
 				int wc = 10;
 				if(objProperty.widthEdit != null){
 					wc = Convert.toInt(objProperty.widthEdit.getText());
 				}
 				objProperty.lastW = wc;
 				int hc = 10;
 				if(objProperty.heightEdit != null){
 					hc = Convert.toInt(objProperty.heightEdit.getText());
 				}
 				objProperty.lastH = hc;
 	 	  		LBCompDesc cdesc = new LBCompDesc(0,wc,hc,alighn,wrap,objProperty.linkCheck.getChecked());
 	 	  		objProperty.lastLink = objProperty.linkCheck.getChecked();
 	 	  		cdesc.setObject(obj);
 				listener.dialogClosed(new DialogEvent(this,null,null,cdesc,DialogEvent.OBJECT));
 			}
 			hide();
 		}
 	}
 }
 
 
 
 public class CCTextArea  extends Container implements ViewContainer, MainView, DialogListener{
 //CCStringWrapper		[] lines;
 Vector				lines = null;
 FontMetrics 		fm = null;
 int 				insetLeft = 5;
 int 				insetRight = 10;
 protected Timer 	caretTimer = null;
 protected boolean 	hasCursor = false,cursorOn = false;
 protected Font font = new Font("Helvetica",Font.PLAIN,12);
 
 public	int firstLine = 0;
 
 protected CCTextAreaState curState = new CCTextAreaState();
 			
 LBCompDesc			[]components = null;
 Vector				rows = null;		
 MainView 			mainView = null;
 LObjDictionary 		dict = null;
 LBCompDesc			currObjectViewDesc = null;
 
 
 CCTextAreaChooser		labBookDialog = null;
 LObjSubDict				subDictionary;
 LObjCCTextAreaView		owner;
 String				text;
 
 boolean		needNotifyAboutMenu = true;
 
 
 EmbedObjectPropertyControl currObjPropControl;
 
 ObjPropertyViewDialog 	currObjPropDialog = null;
 
 String []dialogButtonTitles = {"Yes","No"};
 Dialog					confirmDialogClear = null;
 Dialog					confirmDialogDeleteAll = null;
 Dialog					confirmDialogDeleteCurrent = null;
 
 
 Vector	listeners;
 
 public final static int	yTextBegin = 2;
 
 	public CCTextArea(LObjCCTextAreaView owner,MainView mainView,LObjDictionary dict,LObjSubDict subDictionary){
 		super();
 		this.mainView = mainView;
 		this.dict = dict;
 		this.subDictionary = subDictionary;
 		this.owner = owner;
 	}
 	
 	public void returnFromFolder(){
 		owner.container.reload(owner);
 	}
 	
 	public void addTextAreaListener(TextAreaListener l){
 		if(listeners == null){
 			listeners = new Vector();
 			listeners.add(l);
 		}else{
 			int index = listeners.find(l);
 			if(index < 0) listeners.add(l);
 		}
 	}
 	public void removeTextAreaListener(TextAreaListener l){
 		if(listeners == null) return;
 		int index = listeners.find(l);
 		if(index >= 0) listeners.del(index);
 	}
 	
 	public void notifyListeners(int  type){
 		if(listeners == null || listeners.getCount() < 1) return;
 		for(int i = 0; i < listeners.getCount(); i++){
 			TextAreaListener l = (TextAreaListener)listeners.get(i);
 			if(l != null) l.textAreaWasChanged(null);
 		}
 	}
 	
 	public FontMetrics getFontMetrics() {
 		if(fm == null){
 			fm = getFontMetrics(font);
 		}
 		return fm;
 	}
 
 	public MainView getMainView(){
 		return this;
 	}
     public void addMenu(LabObjectView source, Menu menu){
     	if(mainView != null) mainView.addMenu(source,menu);
     }
 
     public void delMenu(LabObjectView source, Menu menu){
     	if(mainView != null) mainView.delMenu(source,menu);
     }
 
 	public void addFileMenuItems(String [] items, org.concord.waba.extra.event.ActionListener source){
     	if(mainView != null) mainView.addFileMenuItems(items,source);
 	}
 
 	public void removeFileMenuItems(String [] items, org.concord.waba.extra.event.ActionListener source){
     	if(mainView != null) mainView.removeFileMenuItems(items,source);
 	}
 
 	public String [] getCreateNames(){
 		return null;
 	}
 
 	public void createObj(String name, LObjDictionaryView dView){
 	}
 	
 	public void showFullWindowView(LabObjectView view){
     	if(mainView != null){
 			if(currObjectViewDesc != null){
 				Object o = currObjectViewDesc.getObject();
 				if(o != null && o instanceof LabObjectView){
 					LabObjectView ov = (LabObjectView)o;
 					ov.setShowMenus(false);
 				}
 			}
 			owner.setShowMenus(false);
    		mainView.showFullWindowView(view);
     	}
 	}
 
 	static boolean propertyMode = false;
 
 
 	public void setPropertyMode(boolean propertyMode){this.propertyMode = propertyMode;}
 	public boolean getPropertyMode(){return propertyMode;}
 
     public void dialogClosed(DialogEvent e){
     	if(e.getSource() == currObjPropDialog){
     		if(currObjectViewDesc != null){
 
  
  				boolean wrap = currObjPropControl.wrapCheck.getChecked();
 				EmbedObjectPropertyControl.lastWrap = wrap;
 				int alighn = LBCompDesc.ALIGNMENT_LEFT;
 				if(currObjPropControl.alignmentChoice != null){
 					EmbedObjectPropertyControl.lastAlighnLeft = true;
 					if(currObjPropControl.alignmentChoice.getSelected().equals("Right")){
 						EmbedObjectPropertyControl.lastAlighnLeft = false;
 					}
 				}
 				EmbedObjectPropertyControl.lastW = 10;
 				if(currObjPropControl.widthEdit != null){
 					EmbedObjectPropertyControl.lastW = Convert.toInt(currObjPropControl.widthEdit.getText());
 				}
 				EmbedObjectPropertyControl.lastH = 10;
 				if(currObjPropControl.heightEdit != null){
 					EmbedObjectPropertyControl.lastH = Convert.toInt(currObjPropControl.heightEdit.getText());
 				}
 	 	  		EmbedObjectPropertyControl.lastLink = currObjPropControl.linkCheck.getChecked();
     			currObjectViewDesc.alignment 	= (EmbedObjectPropertyControl.lastAlighnLeft)?
     											   LBCompDesc.ALIGNMENT_LEFT:LBCompDesc.ALIGNMENT_RIGHT;
     			currObjectViewDesc.wrapping 	= EmbedObjectPropertyControl.lastWrap;
     			currObjectViewDesc.link 		= EmbedObjectPropertyControl.lastLink;
     			currObjectViewDesc.w 			= EmbedObjectPropertyControl.lastW;
     			currObjectViewDesc.h 			= EmbedObjectPropertyControl.lastH;
     			
 				if(currObjectViewDesc.getObject() instanceof LabObjectView){
 					LabObjectView objView = (LabObjectView)currObjectViewDesc.getObject();
 					objView.close();
 				}
  				layoutComponents();
 				setText(getText());
  				layoutComponents();
 				notifyListeners(0);
    			}
     		currObjPropDialog = null;
     		return;
     	}else if(e.getSource() == labBookDialog){
 			if(e.getInfoType() != DialogEvent.OBJECT || e.getInfo() == null) return;
 			if(!(e.getInfo() instanceof LBCompDesc)) return;
 			LBCompDesc obj = (LBCompDesc)e.getInfo();
 			if(obj == null) return;
 			Object o = obj.getObject();
 			if(o == null || !(o instanceof LabObject)) return;
 			LabObject labObject = (LabObject)o;
 			int nComponents = (components == null)?0:components.length;
 			LBCompDesc []newComponents = new LBCompDesc[nComponents+1];
 			if(components != null){
 				waba.sys.Vm.copyArray(components,0,newComponents,0,nComponents);
 			}
 			components = newComponents;
 			LabObjectView view = labObject.getView(this,false);
 			view.setEmbeddedState(true);
 			components[nComponents] = obj;
 			components[nComponents].setObject(view);
 			components[nComponents].lineBefore = getLineIndex(curState.cursorRow + firstLine);
 			
 			if(subDictionary != null) subDictionary.setObj(labObject,nComponents);
 			view.layout(false);
 			add(view);
 			layoutComponents();
 			setText(getText());
 			notifyListeners(0);
 		}else if(e.getSource() == confirmDialogClear){
 			if(e.getActionCommand().equals("Yes")){
 				clearAll();
 				notifyListeners(0);
 			}
 			confirmDialogClear = null;
 		}else if(e.getSource() == confirmDialogDeleteCurrent){
 			if(e.getActionCommand().equals("Yes")){
 				deleteCurrentObject();
 				notifyListeners(0);
 			}
 			confirmDialogDeleteCurrent = null;
 		}else if(e.getSource() == confirmDialogDeleteAll){
 			if(e.getActionCommand().equals("Yes")){
 				deleteAllObjects();
 				notifyListeners(0);
 			}
 			confirmDialogDeleteAll = null;
 		}
     }
     public void writeExternal(DataStream out){
     	out.writeString(getText());
     	out.writeBoolean(components != null);
     	if(components == null) return;
     	out.writeInt(components.length);
     	for(int i = 0; i < components.length; i++){
     		LBCompDesc d = components[i];
     		out.writeBoolean(d != null);
     		if(d == null) continue;
     		d.writeExternal(out);
     	}
     }
 
     public void readExternal(DataStream in){
 		setText(in.readString(),false);
 		boolean wasComponents = in.readBoolean();
 		if(!wasComponents) return;
 		int nComp = in.readInt();
 		if(nComp < 1) return;
 		components = new LBCompDesc[nComp];
 		for(int i = 0; i < nComp; i++){
 			boolean wasPart = in.readBoolean();
 			if(!wasPart) 	components[i] = null;
 			else			components[i] = new LBCompDesc(in);
 		}
     }
 
 
 
     public void done(LabObjectView source){
 		if(labBookDialog != null){
 			labBookDialog.hide();
 			labBookDialog = null;
 		}
 		
 		
     }
 
     public void reload(LabObjectView source){
 /*
 		LabObject obj = source.getLabObject();
 		source.close();
 		remove(source);
 		LabObjectView replacement = obj.getView(this, true);
 //		replacement.setRect(x,y,width,myHeight);
 
 		add(replacement);
 //		lObjView = replacement;
 */
     }
 
 	public void close(){
 		if(labBookDialog != null){
 			labBookDialog.hide();
 			labBookDialog = null;
 		}
 		if(currObjectViewDesc != null){
 			if(currObjectViewDesc.getObject() instanceof LabObjectView){
 				LabObjectView objView = (LabObjectView)currObjectViewDesc.getObject();
 				objView.close();
 			}
 			currObjectViewDesc = null;
 		}
 	}
 
 
 
 	protected int getItemHeight() {
 		FontMetrics fm = getFontMetrics(); 
 		return (fm == null)?0:fm.getHeight()+2;
 	}
 
 	public int getVisRows(){
 		int h = getItemHeight();
 		if(h == 0) return 0;
 		return height / h;
 	}
 
 	public void requireClearingAll(){
 		confirmDialogClear = Dialog.showConfirmDialog(this,"Clearing All","Are you sure? ",dialogButtonTitles,Dialog.QUEST_DIALOG);
 	}
 	public void clearAll(){
 		setText("");
 		deleteAllObjects(false);
 		layoutComponents();
 	}
 	
 	public void requireDeleteCurrentObject(){
 		confirmDialogDeleteCurrent = Dialog.showConfirmDialog(this,"Delete Object","Are you sure? ",dialogButtonTitles,Dialog.QUEST_DIALOG);
 	}
 	
 	public void deleteCurrentObject(boolean doLayout){
 		if(currObjectViewDesc != null){
 			if(currObjectViewDesc.getObject() instanceof LabObjectView){
 				LabObjectView objView = (LabObjectView)currObjectViewDesc.getObject();
 				objView.close();
 			}
 			if(components != null){
 				for(int i = 0; i < components.length; i++){
 					if(currObjectViewDesc == components[i]){
 						LabObjectView view = (LabObjectView)components[i].getObject();
 						if(view != null){
 							remove(view);
 						}
 						if(subDictionary != null) subDictionary.setObj(null,i);
 						for(int j = i; j < components.length - 1; j++){
 							components[j] = components[j+1];
 							LabObjectView 	oView = (LabObjectView)components[j].getObject();
 							LabObject 		tempObj = (oView == null)?null:oView.getLabObject();
 							if(subDictionary != null) subDictionary.setObj(tempObj,j);
 						}
 						int nComp = components.length - 1;
 						if(nComp < 1){
 							components = null;
 						}else{
 							LBCompDesc []newComp = new LBCompDesc[nComp];
 							waba.sys.Vm.copyArray(components,0,newComp,0,nComp);
 							components = newComp;
 						}
 						break;
 					}
 				}
 			}
 			currObjectViewDesc = null;
 			if(doLayout) layoutComponents();
 			setText(getText());
 		}
 	}
 	public void deleteCurrentObject(){
 		deleteCurrentObject(true);
 	}
 
 	public void requireDeleteAllObjects(){
 		confirmDialogDeleteAll = Dialog.showConfirmDialog(this,"Delete All Objects","Are you sure? ",dialogButtonTitles,Dialog.QUEST_DIALOG);
 	}
 	
 
 	public void deleteAllObjects(){
 		deleteAllObjects(true);
 	}
 	public void deleteAllObjects(boolean doLayout){
 		deleteCurrentObject(false);
 		if(components != null){
 			for(int i = 0; i < components.length; i++){
 				LabObjectView view = (LabObjectView)components[i].getObject();
 				if(view != null) remove(view);
 				if(subDictionary != null) subDictionary.setObj(null,i);
 			}
 			components = null;
 			if(doLayout) layoutComponents();
 			setText(getText());
 		}
 	}
 
 	public void insertText(String iStr){
 	
 		removeCursor();
 		if(iStr == null) return;
 		int nStr = iStr.length();
 		if(nStr < 1){
 			iStr += "\n";
 		}else{
 			char c = iStr.charAt(nStr - 1);
 			boolean wasEOL = false;
 			if(c == '\n'){
 				wasEOL = true;
 			}else if(c == '\r'){
 				wasEOL = true;
 			}
 			if(!wasEOL){
 				iStr += "\n";
 			}
 		}
 		int lineIndex = getLineIndex(curState.cursorRow + firstLine);
 		int oldLines = (lines == null)?0:lines.getCount();
 		String str = "";
 		if(lines == null){
 			str = iStr;
 		}else{
 			boolean wasAdded = false;
 			for(int i = 0; i < lines.getCount(); i++){
 				if(i == lineIndex){
 					wasAdded = true;
 					str += iStr;
 				}
 				str += (((CCStringWrapper)lines.get(i)).getStr() + "\n");
 			}
 			if(!wasAdded) str += iStr;
 		}
 		
 		int oldRows = getRowsNumber();
 		setText(str);//temporary
 		int newRows = getRowsNumber();
 		if(newRows > oldRows){
 			curState.cursorRow += (newRows - oldRows);
 		}
 /*
 		int newLines = (lines == null)?0:lines.getCount();
 		int addLines = newLines - oldLines;
 		if(addLines > 0 && components != null){
 			for(int i = 0; i < components.length; i++){
 				LBCompDesc c = components[i];
 				int lineBefore = c.lineBefore;
 				if(lineIndex < lineBefore){
 					c.lineBefore += addLines;
 				}
 			}
 		}
 */
 		layoutComponents();
 		notifyListeners(0);
 		restoreCursor(true);
 	}
 	public void insertEmptyLine(){
 		if(lines == null){
 			setText("");
 		}else{
 			int lineIndex = getLineIndex(curState.cursorRow + firstLine);
 			String str = "";
 			for(int i = 0; i < lines.getCount(); i++){
 				if(i == lineIndex) str += " \n";
 				str += (((CCStringWrapper)lines.get(i)).getStr() + "\n");
 			}
 			setText(str);
 		}
 		layoutComponents();
 		notifyListeners(0);
 	}
 	
 	public String getText(){
 /*		String retValue = "";
 		if(lines == null) return retValue;
 		for(int i = 0; i < lines.length; i++){
 			retValue += (lines[i].getStr() + "\n");
 		}
 		return retValue;*/
 		return text;
 	}
 	public void insertObject(){
 		if(labBookDialog != null){
 			labBookDialog.hide();
 			labBookDialog = null;
 		}
 		if(dict != null){
 			MainWindow mw = MainWindow.getMainWindow();
 			if(!(mw instanceof ExtraMainWindow)) return;
 			labBookDialog = new CCTextAreaChooser((ExtraMainWindow)mw,(LObjDictionary)LabObject.lBook.load(LabObject.lBook.getRoot()),this,this);
 			labBookDialog.setRect(0,0,150,150);
 			labBookDialog.show();
 		}
 
 
 
 	}
 	
 	public void layoutComponents(){
 		if(components != null){
 			for(int i = 0; i < components.length; i++){
 				LBCompDesc c = components[i];
 				if(c == null) continue;
 				Control cntrl = (Control)c.getObject();
 				if(cntrl != null){//?
 					remove(cntrl);
 				}else{
 					LabObject lobj = subDictionary.getObj(i);
 					if(lobj != null){
 						cntrl = lobj.getView(this,false);
 						((LabObjectView)cntrl).setEmbeddedState(true);
 
 						c.setObject(cntrl);
 					}
 				}
 				if(cntrl != null) add(cntrl);//?
 //				int yTop = y;
 				int yTop = CCTextArea.yTextBegin;
 				int line = c.lineBefore;
 				if(line > 0){
 					if(lines != null && line < lines.getCount()){
 //						yTop += (lines[line - 1].endRow - firstLine)*getItemHeight();
 						yTop += (((CCStringWrapper)lines.get(line - 1)).endRow - firstLine)*getItemHeight();
 					}else if(lines != null && line >= lines.getCount()){
 						yTop += (((CCStringWrapper)lines.get(lines.getCount() - 1)).endRow - firstLine)*getItemHeight();
 					}
 				}else if(line == 0){
 					yTop -= firstLine*getItemHeight();
 				}	
 				if(cntrl != null){
 					if(c.alignment == LBCompDesc.ALIGNMENT_RIGHT){
 						cntrl.setRect(x+width-insetRight-c.w,yTop,c.w,c.h);
 					}else{
 						cntrl.setRect(x+insetLeft,yTop,c.w,c.h);
 					}
 				}
 			}
 			if(needNotifyAboutMenu){
 				needNotifyAboutMenu = false;
 			}
 		}
 	}
 	
 	
 	public void setRect(int x, int y, int width, int height){
 		super.setRect(x,y,width,height);
 		layoutComponents();
 	}
 	
 	
 	public void test(){
 		setText(getText());
 		repaint();
 	}
 	public void setText(String str){
 		setText(str,true);
 	}
 	
 	public void setText(String str, boolean forceAction){
 		int determinedSystem = -1; //unix - 0; //mac - 1 dos - 2
 		if(str == null) return;
 		text = str;
 		if(!forceAction) return;
 		lines = new Vector();
 		rows = new Vector();
 		removeCursor();
 		Rect r = getRect();
 		StringBuffer sb = new StringBuffer();
 		int i = 0;
 		int lastRow = 0;
 		int	strLength = str.length();
 		while(i < strLength){
 			boolean wasEndOfLine = false;
 			char c = str.charAt(i);
 			if(c == '\n'){
 				if(determinedSystem == -1) determinedSystem = 0;
 				wasEndOfLine = true;
 			}else if(c == '\r'){
 				if(determinedSystem <= 0){
 					determinedSystem = 1;
 					if(i < strLength - 1){
 						if(str.charAt(i+1) == '\n'){
 							determinedSystem = 2;
 							i++;
 						}
 					}
 				}
 				wasEndOfLine = true;
 			}else{
 				sb.append(c);
 			}
 			if(wasEndOfLine){
 				int nLines = lines.getCount();
 				LBCompDesc compDesc = null;
 				if(components != null){
 					for(int k = 0; k < components.length; k++){
 						LBCompDesc cDesc = components[k];
 						if(cDesc.lineBefore == nLines){
 							compDesc = components[k];
 						}
 					}
 				}
 				int leftMargin = r.x + insetLeft;
 				int rightMargin = r.x + r.width - insetRight;
 				int skipRows = 0;
 				if(compDesc == null){
 					lines.add(new CCStringWrapper(this,sb.toString(),lastRow));
 				}else{
 					int addH = compDesc.h;
 					int addRows = 1 + (addH / getItemHeight());
 					if(compDesc.alignment == LBCompDesc.ALIGNMENT_LEFT){
 						leftMargin = r.x + insetLeft + compDesc.w;
 					}else{
 						rightMargin = r.x + r.width - insetRight - compDesc.w;
 					}
 					if(!compDesc.wrapping){
 						lastRow += addRows;
 					}
 					
 					int nRows = rows.getCount();
 					if(addRows > 0){
 						for(int k = nRows; k < nRows + addRows; k++){
 							CCTARow newRow = new CCTARow();
 							newRow.setMargins(leftMargin,rightMargin);
 							rows.add(newRow);
 						}
 					}
 					lines.add(new CCStringWrapper(this,sb.toString(),lastRow));
 				}
 				int nRows = rows.getCount();
 				int lastLineRow = ((CCStringWrapper)lines.get(nLines)).endRow;
 				int addRows = lastLineRow - nRows;
 				if(addRows > 0){
 					for(int k = nRows; k < nRows + addRows; k++){
 						CCTARow newRow = new CCTARow();
 						newRow.setMargins(r.x + insetLeft,r.x + r.width - insetRight);
 						rows.add(newRow);
 					}
 				}				
 				lastRow += (((CCStringWrapper)lines.get(nLines)).getRows());
 				sb.setLength(0);
 			}
 			i++;
 		}
 		if(lines.getCount() < 1){
 			lines.add(new CCStringWrapper(this,sb.toString(),0));
 		}else{
 			String s = sb.toString();
 			if(s.length() > 1){
 				lines.add(new CCStringWrapper(this,s,lastRow));
 			}
 		}
 		if(components != null){
 			int nLines = (lines == null)?0:lines.getCount();
 			for(int k = 0; k < components.length; k++){
 				LBCompDesc cDesc = components[k];
 				if(cDesc.lineBefore == nLines){
 					int addH = cDesc.h;
 					int addRows = 1 + (addH / getItemHeight());
 					int lastLineRow = (nLines < 1)?0:((CCStringWrapper)lines.get(nLines - 1)).endRow;
 					if(rows == null)  rows = new waba.util.Vector();
 					int nRows = rows.getCount();
 					if(addRows > 0){
 						for(int rw = nRows; rw < nRows + addRows; rw++){
 							CCTARow newRow = new CCTARow();
 							newRow.setMargins(r.x + insetLeft,r.x + r.width - insetRight);
 							rows.add(newRow);
 						}
 					}		
 					break;
 				}
 			}
 		}
 		repaint();
 	}
 	public void onControlEvent(ControlEvent ev){
 		if(ev.type == EmbedObjectPropertyControl.NEED_DEFAULT_SIZE && ev.target == currObjPropControl){
 			LabObjectView oView = null;
 			if(currObjPropDialog != null) oView = currObjPropDialog.getCheckView();
 			if(oView == null) return;
 			extra.ui.Dimension d = oView.getPreferredSize();
 			if(d == null) return;
 			if(d.width > 0){
 				currObjPropControl.widthEdit.setText(""+d.width);
 				currObjPropControl.lastW = d.width;
 			}
 			if(d.height > 0){
 				currObjPropControl.heightEdit.setText(""+d.height);
 				currObjPropControl.lastH = d.height;
 			}
 			return;
 		}
 
 		if (ev.type == ev.TIMER && hasCursor){
 			paintCursor(null);
 		}
 		else if (ev.type == ev.FOCUS_IN){
 			if(ev.target  == this){
 				gotFocus();
 				if(currObjectViewDesc != null){
 					Control cntrl = (Control)currObjectViewDesc.getObject();
 					if((cntrl != null) && (cntrl instanceof LabObjectView)){
 						LabObjectView object = (LabObjectView)cntrl;
 						object.close();
 					}
 	//				currObjectViewDesc = null;
 				}
 			}else if(components != null){
 				LBCompDesc cntrlDesc = findComponentDesc(ev.target);
 				if(cntrlDesc != null){
 					Control cntrl = (Control)cntrlDesc.getObject();
 					if(cntrl instanceof LabObjectView){
 						LabObjectView object = (LabObjectView)cntrl;
 						object.setShowMenus(true);
 						currObjectViewDesc = cntrlDesc;
 						removeCursor();
 						if(!getPropertyMode()){
 							if(cntrlDesc.link){
 								object.setShowMenus(true);
 								if(owner != null){
 									owner.addChoosenLabObjView(object);
 								}
 							}
 						}
 					}
 				}
 			}
 		}else if (ev.type == ev.FOCUS_OUT){
 			if(ev.target  == this){
 				lostFocus();
 			}/*else if(currObjectViewDesc != null){
 				if(ev.target instanceof Control){
 					Control c = (Control)ev.target;
 					while(c != null){
 						if(c == currObjectViewDesc.getObject()) break;
 						c = c.getParent();
 					}
 					if(c == currObjectViewDesc.getObject()){
 						System.out.println("ev.FOCUS_OUT 3 ");
 						Control cntrl = (Control)currObjectViewDesc.getObject();
 						if((cntrl != null) && (cntrl instanceof LabObjectView)){
 							LabObjectView object = (LabObjectView)cntrl;
 							object.close();
 						}
 		//				currObjectViewDesc = null;
 					}
 				}
 			}*/
 		}
 	}
 	public void gotFocus(){
 		restoreCursor();
 	}
 	public void lostFocus(){
 		removeCursor();
 	}
 	
 	LBCompDesc findComponentDesc(Object o){
 		if(components == null || components.length < 1) return null;
 		for(int i = 0; i < components.length; i++){
 			LabObjectView view = getParentLabObjectView(o);
 			if(view == null) continue;
 			if(components[i].getObject() == view) return components[i];
 		}
 		return null;
 	}
 
 	LBCompDesc findComponent(PenEvent e){
 		if(components == null || components.length < 1) return null;
 		for(int i = 0; i < components.length; i++){
 			if(components[i] == null) continue;
 			Object o = components[i].getObject();
 			if(o == null) continue;
 			if(o instanceof LabObjectView){
 				LabObjectView view = (LabObjectView)o;
 				Rect r = view.getRect();
 				if(r == null) continue;
 				if(e.x <  r.x) continue;
 				if(e.y <  r.y) continue;
 				if(e.x >  r.x+r.width) continue;
 				if(e.y >  r.y+r.height) continue;
 				return components[i];
 			}
 		}
 		return null;
 	}
 	
 	
 	void openCompProp(LBCompDesc compDesc){
 		if(compDesc == null) return;
 		LabObjectView objView = (LabObjectView)compDesc.getObject();
 		if(objView == null) return;
 		String name = objView.getLabObject().name;
 		if(currObjPropControl == null){
 			currObjPropControl = new EmbedObjectPropertyControl(null,name);
 			currObjPropControl.layout(true);
 		}
 		if(currObjPropControl != null){
 			currObjPropControl.setName(name);
 			EmbedObjectPropertyControl.lastAlighnLeft 	= (compDesc.alignment == LBCompDesc.ALIGNMENT_LEFT);
 			EmbedObjectPropertyControl.lastWrap 		= compDesc.wrapping;
 			EmbedObjectPropertyControl.lastLink 		= compDesc.link;
 			EmbedObjectPropertyControl.lastW			= compDesc.w;
 			EmbedObjectPropertyControl.lastH			= compDesc.h;
 			MainWindow mw = MainWindow.getMainWindow();
 			if(!(mw instanceof ExtraMainWindow)) return;
 			currObjPropDialog = new ObjPropertyViewDialog((ExtraMainWindow)mw, this, "Properties", currObjPropControl,objView);
 			currObjPropDialog.setRect(0,0,150,150);
 			currObjPropDialog.show();		
 		}
 	}
 	
 	public static LabObjectView getParentLabObjectView(Object o){
 		if(!(o instanceof Control)) return null;
 		LabObjectView view = null;
 		Control c = (Control)o;
 		while(view == null && c != null){
 			if(c instanceof LabObjectView){
 				view = (LabObjectView)c;
 				break;
 			}
 			c = c.getParent();
 		}
 		return view;
 	}
 	public static LObjDictionaryView getParentLObjDictionaryView(Object o){
 		if(!(o instanceof Control)) return null;
 		LObjDictionaryView view = null;
 		Control c = (Control)o;
 		while(view == null && c != null){
 			if(c instanceof LObjDictionaryView){
 				view = (LObjDictionaryView)c;
 				break;
 			}
 			c = c.getParent();
 		}
 		return view;
 	}
 	
 	public void restoreCursor(){
 		restoreCursor(false);
 	}
 	public void restoreCursor(boolean forseAction){
 		if(!forseAction && currObjectViewDesc != null) return;
 		hasCursor =  true;
 		if(caretTimer == null) caretTimer = addTimer(500);
 	}
 	
 	
 	public void removeCursor(){
 		clearCursor();
 		hasCursor = false;
 		cursorOn = false;
 		removeTimer(caretTimer);
 		caretTimer = null;
 	}
 	
 	protected void clearCursor() {
 		if (cursorOn) paintCursor(null);
 	}
 	protected void paintCursor(Graphics gr){
 		Graphics g = gr;
 		if (g == null) g = createGraphics();
 		if (g == null) return;
 		if (hasCursor){
 			Rect r = new Rect(0,0,0,0);
 
 			if (getCharRect(r)){
 				g.drawCursor(r.x,r.y,r.width,r.height);
 				cursorOn = !cursorOn;
 			}
 		}
 		if (gr == null) g.free();
 	}
 	public boolean getCharRect(Rect r){
 		if(r == null) return false;
 		r.width 	= 1;
 		r.height 	= getItemHeight();
 		r.x = curState.cursorPos;
 		r.y = CCTextArea.yTextBegin + curState.cursorRow*getItemHeight();
 		if(r.y > height){
 			removeCursor();
 			return false;
 		}
 		return true;
 	}
 
 	public void onEvent(Event ev){
 		if (ev instanceof PenEvent)
 			onPenEvent((PenEvent)ev);
 		else if (ev instanceof KeyEvent){
 			onKeyEvent((KeyEvent)ev);
 		}else if (ev instanceof ControlEvent){
 			onControlEvent((ControlEvent)ev);
 		}
 		super.onEvent(ev);
 	}
 
 	public void moveUp(){
 		if(firstLine > 0){
 			removeCursor();
 			firstLine--;
 			layoutComponents();
 			repaint();
 		}
 	}
 	public void moveDown(){
 		int nRows = getRowsNumber();
 		if(lines != null && firstLine < nRows - 2){
 			removeCursor();
 			firstLine++;
 			layoutComponents();
 			repaint();
 		}
 	}
 
 
 	public void setFirstLine(int fl){
 		setFirstLine(fl,true);
 	}
 	public void setFirstLine(int fl,boolean dorepaint){
 		int nRows = getRowsNumber();
 		if(lines != null && fl < nRows - 1 && fl >= 0){
 			removeCursor();
 			firstLine = fl;
 			layoutComponents();
 			if(dorepaint) repaint();
 		}
 	}
 
 	public void onKeyEvent(KeyEvent ev){
 		CCTextAreaState tas = curState;
 		if(ev.target != this) return;
 		if (ev.key == IKeys.BACKSPACE) {
 		}else if (ev.key == IKeys.DELETE){
 		}else if (ev.key == IKeys.ENTER){// && editable(this)){
 			insertEmptyLine();
 		}else if (ev.key == IKeys.END){
 		}else if (ev.key == IKeys.HOME){
 			if(firstLine != 0){
 				removeCursor();
 				firstLine = 0;
 				layoutComponents();
 				repaint();
 			}
 		}else if (ev.key == IKeys.LEFT){
 		}else if (ev.key == IKeys.RIGHT){
 		}else if (ev.key == IKeys.UP || ev.key == IKeys.PAGE_UP){
 			moveUp();
 		}else if (ev.key == IKeys.DOWN || ev.key == IKeys.PAGE_DOWN){
 			moveDown();
 		}else if (ev.key >= 32 && ev.key <= 255){
 			if((ev.modifiers & IKeys.CONTROL) > 0){
 				if(ev.key == 'v' || ev.key == 'V'){
 					if(!CCClipboard.isClipboardEmpty()){
 						String str = CCClipboard.getStringContent();
 						if(str != null){
 							insertText(str);
 						}
 					}
 				}
 			}else{
 			}
 		}
 	}
 	
 /*
 	public int getRowsNumber(){
 		int retValue = 0;
 		if(lines == null || lines.getCount() < 1) return 0;
 		for(int i = 0; i < lines.getCount(); i++){
 			retValue += ((CCStringWrapper)lines.get(i)).getRows();
 		}
 		return retValue;
 	}
 */
 	public int getRowsNumber(){
 		int retValue = 0;
 		int nLines = (lines == null)?0:lines.getCount();
 		int addRow = 0;
 		if(components != null){
 			for(int i = 0; i < components.length; i++){
 	    		LBCompDesc d = components[i];
 	    		if(d == null) continue;
 	    		if(d.lineBefore == nLines){
 	    			addRow = 1 + (d.h / getItemHeight());
 	    			break;	
 	    		}
 			}
 		}
 		retValue = addRow;
 		if(nLines < 1) return retValue;
 		retValue = addRow + ((CCStringWrapper)lines.get(lines.getCount() - 1)).endRow + 1;
 		return retValue;
 	}
 	
 	public void onPenEvent(PenEvent ev){
 		if(ev.type == PenEvent.PEN_DOWN){
 			LBCompDesc compDesc = findComponentDesc(ev.target);
 			if(compDesc == null && currObjectViewDesc != null){
 				currObjectViewDesc = null;
 			}
 			if(getPropertyMode()){
 				if(compDesc != null){
 					openCompProp(compDesc);
 				}
 				return;
 			}
 			if(compDesc != null) return;
 			int x = 0;
 			int h = getItemHeight();
 			if(ev.y < CCTextArea.yTextBegin) ev.y = CCTextArea.yTextBegin;
 			int row = 1 + firstLine + ((ev.y - CCTextArea.yTextBegin) / h);
 			if(row > getRowsNumber()) row = getRowsNumber();
 			int lineIndex = getLineIndex(row - 1);
 			if(lines == null){
 				row = 1;
 			}else if(lineIndex >= 0 && lineIndex < lines.getCount()){
 				CCStringWrapper sw = (CCStringWrapper)lines.get(lineIndex);
 				int rIndex = row - 1 - sw.beginRow;
 				if(rIndex >= 0 && rIndex < sw.delimiters.length / 2){
 					String str = sw.getSubString(rIndex);
 					if(str != null){
 						int lastPos = insetLeft + fm.getTextWidth(str);
 						if(ev.x < insetLeft) x = insetLeft;
 						else if(ev.x > lastPos) x = lastPos;
 						else{
 							int xp = insetLeft;
 							x = ev.x;
 							for(int c = 0; c < str.length(); c++){
 								int cw = fm.getCharWidth(str.charAt(c));
 								if(x < xp + cw){
 									x = xp + cw;
 									row = sw.endRow + 1;
 									x = insetLeft;
 									break;
 								}
 								xp += cw;
 							}
 						}
 					}
 				}
 			}
 			if(hasCursor) clearCursor();
 			else		  restoreCursor();
 			curState.cursorPos = x;
 			curState.cursorRow = row - 1 - firstLine;
 			paintCursor(null);
 		}
 	}
 
 	public int getLineIndex(int row){
 		int retValue = 0;
 		if(lines == null || lines.getCount() < 1){
 			return 0;
 		}
 		retValue = lines.getCount();
 		if(row >= getRowsNumber()){
 			return retValue;
 		}
 		int ind = 0;
 		for(int i = 0; i < lines.getCount(); i++){
 			ind = ((CCStringWrapper)lines.get(i)).beginRow;
 			int swRows = ((CCStringWrapper)lines.get(i)).getRows();
 			if(row < ind + swRows){
 				retValue = i;
 				break;
 			}
 //			ind += swRows;
 		}
 		return retValue;
 	}
 	
 	public void onPaint(Graphics g){
 		Rect r = getRect();
 		g.setColor(255,255,255);
 		g.fillRect(0,0,r.width,r.height);
 		g.setColor(0,0,0);
 		doPaintData(g);
 //		g.drawRect(0,0,r.width,r.height);
 	}
 	public void doPaintData(Graphics g){
 		if(lines == null) return;
 		for (int i = 0; i<lines.getCount(); i++){
 			((CCStringWrapper)lines.get(i)).draw(g,firstLine);
 		}
 	}
 
 }
 
 class CCTextAreaState{
 public int cursorRow = 0, cursorPos = 0;
 public int	cursorChar = 0;
 	CCTextAreaState(){
 	}
 }
 
 
 class CCTARow{
 int  	beginPos,endPos;
 	CCTARow(){
 	}
 	public void setMargins(int beginPos,int endPos){
 		this.beginPos 	= beginPos;
 		this.endPos 	= endPos;
 	}
 }
 
 class CCStringWrapper{
 String str;
 CCTextArea owner = null;
 int  	beginPos 	= 5;
 int		endPos 		= 50;
 
 int		beginRow	= -1;
 int		endRow		= -1;
 char 	[]chars = null;
 int		delimiters[] = null;
 static int	[]charWidthMappers = null;
 
 
 	CCStringWrapper(CCTextArea owner,String str,int beginRow){
 		this.owner = owner;
 		Rect r = owner.getRect();
 		beginPos 	= r.x + owner.insetLeft;
 		endPos 		= r.x + r.width - owner.insetRight;
 		setStr(str,beginRow);		
 	}
 	
 	private static void createCharWidthMappers(CCTextArea owner){
 		if(charWidthMappers != null || owner == null) return;
 		FontMetrics fm = owner.getFontMetrics();
 		if(fm == null) return;
 		charWidthMappers = new int[96];
 		for(int i = 0; i < 96; i++){
 			charWidthMappers[i] = fm.getCharWidth((char)(i+32));
 		}
 	}
 	
 	String getStr(){return str;}
 	void setStr(String str,int beginRow){
 		this.beginRow 	= beginRow;
 		if(owner == null){
 			this.str = null;
 			return;
 		}
 		
 		int    numbTotalRows = (owner.rows == null)?0:owner.rows.getCount();
 		
 		
 		this.str = str;
 		FontMetrics fm = owner.getFontMetrics();
 		if(fm == null || str == null) return;
 		int currRow = beginRow;
 		int x 			= (currRow >= numbTotalRows || owner.rows == null)?beginPos:((CCTARow)owner.rows.get(currRow)).beginPos;
 		int lastWord 	= 0;
 		int	blankWidth = fm.getCharWidth(' ');
 		int	delimiterIndex = 0;
 		int i = 0;
 		
 		int nLines = 0;
 		delimiters = null;
 		chars = str.toCharArray();
 		while(i < str.length()){
 			char c = str.charAt(i);
 			if(isWordDelimiter(c)){
 				if(c == '\n') break;
 				lastWord = i+1;
 			}
 			if(charWidthMappers == null){
 				createCharWidthMappers(owner);
 			}
 			int w = 0;
 			if(c >= ' ' && c < (char)128){
 				w = (charWidthMappers != null)?charWidthMappers[(int)c - 32]:fm.getCharWidth(c);
 			}else{
 				w = fm.getCharWidth(c);
 			}
 			
 			
 			int limitX = (currRow >= numbTotalRows || owner.rows == null)?endPos:((CCTARow)owner.rows.get(currRow)).endPos;
 						
 			if(x + w > limitX){
 				if(lastWord == delimiterIndex){
 					lastWord = i;
 				}else{
 					i = lastWord;
 				}
 				int nInt = 0;
 				if(delimiters != null){
 					nInt = delimiters.length;
 				}
 				int	[]newDelimiters = new int[nInt + 2];
 				if(delimiters != null){
 					waba.sys.Vm.copyArray(delimiters,0,newDelimiters,0,nInt);
 				}
 				delimiters = newDelimiters;
 				delimiters[nInt] = delimiterIndex;
 				delimiters[nInt+1] = lastWord;
 				
 				currRow++;
 				x = (currRow >= numbTotalRows || owner.rows == null)?beginPos:((CCTARow)owner.rows.get(currRow)).beginPos;
 				
 				
 				delimiterIndex = i;
 			}else{
 				x += w;
 			}
 			i++;
 		}
 		if(delimiters == null){
 			delimiters = new int[2];
 			delimiters[0] = 0;
 			delimiters[1] = chars.length;
 		}else{
 			if(delimiterIndex < str.length()){
 				int nInt = delimiters.length;
 				int	[]newDelimiters = new int[nInt + 2];
 				waba.sys.Vm.copyArray(delimiters,0,newDelimiters,0,nInt);
 				delimiters = newDelimiters;
 				delimiters[nInt] = delimiterIndex;
 				delimiters[nInt+1] = chars.length;
 			}
 		}
 		this.endRow 	= this.beginRow + (delimiters.length / 2);
 	}
 	
 	public int getRows(){
 		return (endRow - beginRow);
 	}
 	
 	String getFullStr(){
 		return str;
 	}
 	
 	public void setMargins(int beginPos,int endPos){
 		this.beginPos 	= beginPos;
 		this.endPos 	= endPos;
 	}
 	public void setRows(int beginRow,int endRow){
 		this.beginRow 	= beginRow;
 		this.endRow 	= endRow;
 	}
 	
 	public String getSubString(int index){
 		if(chars == null || delimiters == null) return null;
 		if(index < 0 || index >= delimiters.length / 2) return null;
 		return new String(chars,delimiters[2*index],delimiters[2*index+1]-delimiters[2*index]);
 	}
 	
 	public void draw(Graphics gr,int firstRow){
 		if(gr == null || delimiters == null || owner == null) return;
 		gr.setColor(0,0,0);
 		int    numbTotalRows = (owner.rows == null)?0:owner.rows.getCount();
 		int h = owner.getItemHeight();
 		int limitRow = delimiters.length / 2;
 		for(int i = beginRow; i < endRow; i++){
 			if(i < firstRow) continue;
 			int y = CCTextArea.yTextBegin + (i - firstRow)*h;
 			if(i - beginRow < limitRow ){
 				int x = (i >= numbTotalRows || owner.rows == null)?beginPos:((CCTARow)owner.rows.get(i)).beginPos;
 				int index = (i - beginRow)*2;
 				gr.drawText(chars,delimiters[index],delimiters[index+1] - delimiters[index],x,y);
 				
 			}
 		}
 
 	}
 	public static boolean isWordDelimiter(char c){
 		boolean retValue = false;
 		for(int i = 0; i < wordDelimChars.length; i++){
 			if(c == wordDelimChars[i]){
 				retValue = true;
 				break;
 			}
 		}
 /*
 		switch(c){
 			case ' ':
 			case '\t':
 			case ';':
 			case '.':
 			case ',':
 				retValue = true;
 				break;
 		}
 */
 		return retValue;
 	}
 	
 
 private static char []wordDelimChars = {' ','\t',';','.',',','/','\\'};	
 
 }
 
 
 class ObjPropertyViewDialog extends ViewDialog{
 CCTextArea		textArea;
 LabObjectView	checkView;
 	public 	ObjPropertyViewDialog(ExtraMainWindow owner,CCTextArea textArea,String title, 
 						  LabObjectView view,LabObjectView checkView){
 		super(owner,textArea,title,view);
 		this.textArea = textArea;
 		this.checkView = checkView;
 	}
 	public void onEvent(Event e){
 		if(textArea == null) return;
 		if(e instanceof ControlEvent && e.type == EmbedObjectPropertyControl.NEED_DEFAULT_SIZE){
 			textArea.onEvent(e);
 		}
 	}
 	public LabObjectView getCheckView(){return checkView;}
 }
