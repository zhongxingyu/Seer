 package org.openrosa.client.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.openrosa.client.Context;
 import org.openrosa.client.controller.FormDesignerController;
 import org.openrosa.client.controller.IFormDesignerListener;
 import org.openrosa.client.controller.IFormUIListener;
 import org.openrosa.client.dnd.JrTreePanelDropTarget;
 import org.openrosa.client.model.DataDef;
 import org.openrosa.client.model.DataDefBase;
 import org.openrosa.client.model.DataInstanceDef;
 import org.openrosa.client.model.FormDef;
 import org.openrosa.client.model.GroupDef;
 import org.openrosa.client.model.IFormElement;
 import org.openrosa.client.model.ItemSetDef;
 import org.openrosa.client.model.OptionDef;
 import org.openrosa.client.model.PredicateDef;
 import org.openrosa.client.model.QuestionDef;
 import org.openrosa.client.model.RepeatQtnsDef;
 import org.openrosa.client.model.TreeModelItem;
 import org.openrosa.client.util.FormHandler;
 import org.purc.purcforms.client.controller.IFormActionListener;
 import org.purc.purcforms.client.controller.IFormSelectionListener;
 import org.purc.purcforms.client.locale.LocaleText;
 import org.purc.purcforms.client.util.FormDesignerUtil;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelIconProvider;
 import com.extjs.gxt.ui.client.dnd.DND.Feedback;
 import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.DNDListener;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 
 
 /**
  * Displays questions in a tree view.
  * 
  * @author daniel
  *
  */
 public class FormsTreeView extends com.extjs.gxt.ui.client.widget.Composite implements SelectionHandler<TreeItem>,IFormActionListener, ModelIconProvider<TreeModelItem>, IFormUIListener {
 
 	/**
 	 * Specifies the images that will be bundled for this Composite and specify
 	 * that tree's images should also be included in the same bundle.
 	 */
 	public interface Images extends Toolbar.Images, Tree.Resources {
 		@Source("org/openrosa/client/resources/drafts.gif")
 		ImageResource drafts();
 		@Source("org/openrosa/client/resources/markRead.gif")
 		ImageResource markRead();
 		@Source("org/openrosa/client/resources/templates.gif")
 		ImageResource templates();
 		@Source("org/openrosa/client/resources/note.gif")
 		ImageResource note();
 		@Source("org/openrosa/client/resources/lookup.gif")
 		ImageResource lookup();
 		@Source("org/openrosa/client/resources/hiddenquestion.gif")		
 		ImageResource hiddenQuestion();
 		@Source("org/openrosa/client/resources/dataquestion.gif")
 		ImageResource dataQuestion();
 	}
 
 	
 	
 	
 	/** The main or root widget for displaying the list of forms and their contents
 	 * in a tree view.
 	 */
 	//private Tree tree;
 
 	/** The tree images. */
 	private final Images images;
 
 	/** Pop up for displaying tree item context menu. */
 	private PopupPanel popup;
 
 	/** The item that has been copied to the clipboard. */
 	private Object clipboardItem;
 	private boolean inCutMode = false;
 
 	/** The currently selected tree item. */
 	private TreeItem item;
 
 	/** Flag determining whether to set the form node as the root tree node. */
 	private boolean showFormAsRoot;
 
 	/** The currently selected form. */
 	private FormDef formDef;
 
 	/** List of form item selection listeners. */
 	private List<IFormSelectionListener> formSelectionListeners = new ArrayList<IFormSelectionListener>();
 
 	/** The next available form id. We always have one form for OpenRosa form designer. */
 	private int nextFormId = 1;
 
 	/** The next available question id. */
 	private int nextQuestionId = 0;
 
 	/** The next available question option id. */
 	private int nextOptionId = 0;
 
 	/** The listener to form designer global events. */
 	private IFormDesignerListener formDesignerListener;
 
 	private TreePanel treePanel;
 	
 
 	/**
 	 * Creates a new instance of the forms tree view widget.
 	 * 
 	 * @param images the tree images.
 	 * @param formSelectionListener the form item selection events listener.
 	 */
 	public FormsTreeView(Images images,IFormSelectionListener formSelectionListener) {
 
 		FormDesignerController.clearIsDirty();
 		this.images = images;
 		this.formSelectionListeners.add(formSelectionListener);
 
 
 		treePanel = new TreePanel(new TreeStore<TreeModelItem>());
 		treePanel.getStyle().setLeafIcon(AbstractImagePrototype.create(images.newform())); 
 		//treePanel.getStyle().setJointExpandedIcon(jointExpandedIcon)
 		treePanel.setDisplayProperty("text");  
 		treePanel.setAutoLoad(true);
 		//treePanel.setAutoHeight(true);
 		treePanel.setIconProvider(this);
 		initComponent(treePanel);
 
 		treePanel.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<TreeModelItem>>(){
 			public void handleEvent(SelectionChangedEvent<TreeModelItem> te) {
 				if(te.getSelectedItem() == null)
 					return;
 
 				Object selObject = te.getSelectedItem().getUserObject();
 				Context.setFormDef(FormDef.getFormDef((IFormElement)selObject));
 				formDef = Context.getFormDef();
 				fireFormItemSelected(selObject);
 			}
 		});
 
 
 		//add drag and drop
 		TreePanelDragSource source = new TreePanelDragSource(treePanel);  
 		source.addDNDListener(new DNDListener() {  
 			@Override  
 			public void dragStart(DNDEvent e) {  
 				ModelData sel = treePanel.getSelectionModel().getSelectedItem();  
 				if (sel != null && sel == treePanel.getStore().getRootItems().get(0)) {  
 					e.setCancelled(true);  
 					e.getStatus().setStatus(false);  
 					return;  
 				}  
 				super.dragStart(e);  
 			}
 			
		});  
 
 		JrTreePanelDropTarget target = new JrTreePanelDropTarget(treePanel,this);  
 		target.setAllowSelfAsSource(true);  
 		target.setFeedback(Feedback.BOTH); 
 
 		initContextMenu();
 	}
 
 
 
 	
 	private void scrollToLeft(){
 		DeferredCommand.addCommand(new Command(){
 			public void execute(){
 				Element element = (Element)getParent().getParent().getParent().getElement().getChildNodes().getItem(0).getChildNodes().getItem(0);
 				DOM.setElementPropertyInt(element, "scrollLeft", 0);
 			}
 		});
 	}
 
 
 	/**
 	 * Sets the listener for form designer global events.
 	 * 
 	 * @param formDesignerListener the listener.
 	 */
 	public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
 		this.formDesignerListener = formDesignerListener;
 	}
 
 	/**
 	 * Adds a listener to form item selection events.
 	 * 
 	 * @param formSelectionListener the listener to add.
 	 */
 	public void addFormSelectionListener(IFormSelectionListener formSelectionListener){
 		this.formSelectionListeners.add(formSelectionListener);
 	}
 
 	public void showFormAsRoot(boolean showFormAsRoot){
 		this.showFormAsRoot = showFormAsRoot;
 	}
 
 	/**
 	 * Prepares the tree item context menu.
 	 */
 	private void initContextMenu(){
 		popup = new PopupPanel(true,true);
 
 		MenuBar menuBar = new MenuBar(true);
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("addNew")),true, new Command(){
 			public void execute() {popup.hide(); /*addNewItem();*/}});
 
 		menuBar.addSeparator();		  
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("addNewChild")),true, new Command()
 			{
 				public void execute() 
 				{
 					popup.hide();
 					List<TreeModelItem> selectedItems = treePanel.getSelectionModel().getSelectedItems();
 					for(TreeModelItem item : selectedItems)
 					{
 						IFormElement element = (IFormElement)(item.getUserObject());
 						if(item.getUserObject() instanceof QuestionDef)
 						{
 							QuestionDef question = (QuestionDef)(item.getUserObject());
 							if(question.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || question.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
 							{
 								FormHandler.addNewOptionDef(question);
 								return;
 							}
 						}
 					}
 				}
 			});
 
 		menuBar.addSeparator();		  
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteItem")),true,new Command(){
 			public void execute() {popup.hide(); formDesignerListener.deleteSelectedItem();}});
 
 		menuBar.addSeparator();		  
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.moveup(),LocaleText.get("moveUp")),true, new Command(){
 			public void execute() {popup.hide(); /*moveItemUp();*/}});
 
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.movedown(),LocaleText.get("moveDown")),true, new Command(){
 			public void execute() {popup.hide(); /*moveItemDown();*/}});
 
 		menuBar.addSeparator();		  
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true,new Command(){
 			public void execute() {popup.hide(); formDesignerListener.copyItem();}});
 
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true,new Command(){
 			public void execute() {popup.hide(); formDesignerListener.cutItem();}});
 
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),LocaleText.get("paste")),true,new Command(){
 			public void execute() {popup.hide(); formDesignerListener.pasteItem();}});
 
 		menuBar.addSeparator();		  
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("save")),true,new Command(){
 			public void execute() {popup.hide(); /*saveItem();*/}});
 
 		menuBar.addSeparator();		  
 		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.refresh(),LocaleText.get("refresh")),true,new Command(){
 			public void execute() {popup.hide(); /*refreshItem();*/}});
 
 		popup.setWidget(menuBar);
 	}
 
 
 
 
 	/**
 	 * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(SelectionEvent)
 	 */
 	public void onSelection(SelectionEvent<TreeItem> event){
 
 		scrollToLeft();
 
 		TreeItem item = event.getSelectedItem();
 		System.out.println("Got here and shouldn't get much further");
 		//Should not call this more than once for the same selected item.
 		if(item != this.item){
 
 			Context.setFormDef(FormDef.getFormDef((IFormElement)item.getUserObject()));
 			formDef = Context.getFormDef();
 
 
 			fireFormItemSelected(item.getUserObject());
 			this.item = item;
 
 			//Expand if has kids such that users do not have to click the plus
 			//sign to expand. Besides, some are not even aware of that.
 			//if(item.getChildCount() > 0)
 			//	item.setState(true);
 		}
 	}
 
 	/**
 	 * Notifies all form item selection listeners about the currently
 	 * selected form item.
 	 * 
 	 * @param formItem the selected form item.
 	 */
 	private void fireFormItemSelected(Object formItem){
 		for(int i=0; i<formSelectionListeners.size(); i++)
 			formSelectionListeners.get(i).onFormItemSelected(formItem);
 	}
 
 
 
 	
 	/**
 	 * Check if a form with a given id is loaded.
 	 * 
 	 * @param formId the form id.
 	 * @return true if it exists, else false.
 	 */
 	public boolean formExists(int formId){
 		int count = treePanel.getStore().getChildCount(); //.getItemCount();
 		for(int index = 0; index < count; index++){
 			TreeModelItem item = (TreeModelItem)treePanel.getStore().getChild(index);
 			if( (item.getUserObject() instanceof FormDef) && ((FormDef)item.getUserObject()).getId() == formId){
 				//tree.setSelectedItem(item);
 				treePanel.getSelectionModel().select(item, false);
 				return true;
 			}
 		}
 
 		return false;
 	}
 	
 
 	/**
 	 * Used to refresh the form
 	 * @param formDef
 	 */
 	/*
 	public void refreshForm(FormDef formDef){
 		//tree.clear();
 		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
 		if(item != null){
 			TreeModelItem root = getSelectedItemRoot(item);
 			formDef.setId(((FormDef)root.getUserObject()).getId());
 
 			//tree.removeItem(root);
 			treePanel.getStore().remove(root);
 		}
 
 		handleLoadNewForm(formDef);
 	}
 	*/
 	
 
 	/**
 	 * Gets the list of forms that have been loaded.
 	 * 
 	 * @return the form list.
 	 */
 	public List<FormDef> getForms(){
 		List<FormDef> forms = new ArrayList<FormDef>();
 
 		int count = treePanel.getStore().getChildCount(); //.getItemCount();
 		for(int index = 0; index < count; index++)
 		{
 			if(((TreeModelItem)treePanel.getStore().getChild(index)).getUserObject() instanceof FormDef)
 			{
 				forms.add((FormDef)((TreeModelItem)treePanel.getStore().getChild(index)).getUserObject());
 			}
 		}
 
 		return forms;
 	}
 
 	
 	
 
 	/**
 	 * Adds a new child to root at the end of roots kids.
 	 * @param root
 	 * @param text
 	 * @param userObject
 	 * @return TreeModelItem corresponding to newly added child
 	 */
 	private TreeModelItem addImageItem(TreeModelItem root, String text, Object userObject){		
 		FormDesignerController.makeDirty();
 		return addImageItem(root, text, userObject, root.getChildCount());
 	}
 	
 	/**
 	 * Used to add a new object at a specified index
 	 * @param root
 	 * @param text
 	 * @param userObject
 	 * @param index
 	 * @return the treemodelItem of the object to insert
 	 */
 	private TreeModelItem addImageItem(TreeModelItem root, String text, Object userObject, int index){
 		FormDesignerController.makeDirty();
 		TreeModelItem item;
 		
 		item = new TreeModelItem(text,userObject,root, index);
 		treePanel.getStore().insert(root, item, index, true);
 		
 		
 		return item;
 	}
 	
 	
 	
 	
 	
 
 	
 	/**
 	 * Gets the currently selected items
 	 * @return
 	 */
 	public List<IFormElement> getSelectedItems()
 	{
 		//init our array
 		List<IFormElement> sortedItems = new ArrayList<IFormElement>();
 		
 		List<TreeModelItem> items = treePanel.getSelectionModel().getSelectedItems();
 		if(items.size() <= 1)
 		{
 			if(items.size() == 1)
 			{
 				sortedItems.add((IFormElement)(items.get(0).getUserObject()));
 			}
 			return sortedItems;
 		}
 		
 		//if there's more than one item we'll need to ensure the correct order		
 		TreeModelItem parent = (TreeModelItem)((TreeModelItem)items.get(0)).getParent();
 		for(ModelData md : parent.getChildren())
 		{
 			for(Object o : items)
 			{
 				TreeModelItem tmi = (TreeModelItem)o;
 				if(tmi == md)
 				{
 					sortedItems.add((IFormElement)(tmi.getUserObject()));
 				}
 			}
 		}
 		
 		return sortedItems; 
 	}
 	
 
 		
 	
 
 /**
 	 * Gets the index of the tree item which is at the root level.
 	 * 
 	 * @param item the tree root item whose index we are to get.
 	 * @return the index of the tree item.
 	 */
 	private int getRootItemIndex(TreeModelItem item){
 		int count = treePanel.getStore().getChildCount(); //getItemCount()
 		for(int index = 0; index < count; index++){
 			if(item == treePanel.getStore().getChild(index))
 				return index;
 		}
 
 		return 0;
 	}
 
 
 
 
 	
 	/**
 	 * This function is used to recursively update
 	 * all the text for all the elements below and including the 
 	 * given item
 	 * @param item where to start updating text
 	 */
 	private void updateTreeText(TreeModelItem item)
 	{
 		//update the given items text
 		IFormElement element = (IFormElement)(item.getUserObject());
 		item.setText(element.getText());
 		treePanel.getStore().update(item);
 		
 		//now loop over the kids of this item and recurse
 		List<ModelData> kids = item.getChildren();
 		for(ModelData _kid : kids)
 		{
 			//make sure we check the isntance 
 			if(!(_kid instanceof TreeModelItem))
 			{
 				continue;
 			}
 			TreeModelItem kid = (TreeModelItem)_kid;
 			//recurse
 			updateTreeText(kid);
 		}
 	}
 
 	 
 
 		
 	
 	/**
 	 * This method moves the thingToMove to the newParentOfThingToMove and places it there at index newPostion
 	 * @param thingToMove - The thing that's getting moved around
 	 * @param newParentOfThingToMove - Where we're putting the thing that's getting move around
 	 * @param newPosition - The index of the thing we're moving around
 	 */
 	public void moveDropHandler(List<IFormElement> thingsToMove, IFormElement newParentOfThingToMove, int newPosition)
 	{
 		
 		//call the FormDesignerController and let them know a move is under way
 		for(IFormElement thingToMove : thingsToMove)
 		{
 			formDesignerListener.moveFormObjects(thingToMove, newParentOfThingToMove, newPosition, this);
 		}
 		
 	}
 	
 
 	/**
 	 * @see org.purc.purcforms.client.controller.IFormChangeListener#onFormItemChanged(java.lang.Object)
 	 */
 	public Object handleFormItemChanged(Object formItem) {
 		FormDesignerController.makeDirty();
 		TreeModelItem item = this.getTreeModelItemFromFormComponent((IFormElement)formItem);
 		if(item == null)
 			return formItem; //How can this happen?
 
 		if(item.getUserObject() != formItem)
 			return formItem;
 
 		if(formItem instanceof QuestionDef){
 			QuestionDef element = (QuestionDef)formItem;
 			
 			if(element.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || element.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
 			{
 				//check if it was a non-itemset select and now it is an itemset
 				if(element.usesItemSet() && (item.getChildCount() == 0 || !(((TreeModelItem)(item.getChild(0))).getUserObject() instanceof ItemSetDef)))
 				{
 					int count = item.getChildCount();
 					for(int i = 0; i < count; i++)
 					{
 						ModelData md = item.getChild(0);
 						item.remove(md);
 						treePanel.getStore().remove(md);
 					}
 					ItemSetDef itemSet = ((ItemSetDef)element.getChildren().get(0));
 					addImageItem(item, itemSet.toString(), itemSet); 
 				}
 				//check for the opposite
 				if(!element.usesItemSet() && (item.getChildCount() == 0 || (((TreeModelItem)(item.getChild(0))).getUserObject() instanceof ItemSetDef)))
 				{
 					int count = item.getChildCount();
 					for(int i = 0; i < count; i++)
 					{
 						ModelData md = item.getChild(0);
 						item.remove(md);
 						treePanel.getStore().remove(md);
 					}
 					//addNewOptionDef(element, item);
 				}
 			}
 			
 			item.setText(element.getText());
 			treePanel.getStore().update(item);
 		}
 		else if (formItem instanceof ItemSetDef)
 		{
 			ItemSetDef itemSet = (ItemSetDef)formItem;
 			item.setText(itemSet.getText());
 			treePanel.getStore().update(item);
 		}
 		else if(formItem instanceof OptionDef){
 			OptionDef optionDef = (OptionDef)formItem;
 			
 			item.setText(optionDef.getText());
 			treePanel.getStore().update(item);
 						
 			this.updateTreeText((TreeModelItem)(item.getParent()));
 		}
 		else if(formItem instanceof GroupDef){
 			IFormElement element = (IFormElement)formItem;
 			item.setText(element.getText());
 			treePanel.getStore().update(item);
 		}
 		else if(formItem instanceof FormDef){
 			FormDef formDef = (FormDef)formItem;
 			
 			item.setText(formDef.getName());
 			treePanel.getStore().update(item);
 		}
 		else if(formItem instanceof DataInstanceDef)
 		{
 			DataInstanceDef dataInstance = (DataInstanceDef)formItem;
 			item.setText(dataInstance.getInstanceId());
 			treePanel.getStore().update(item);
 		}
 		else if(formItem instanceof DataDef)
 		{
 			DataDef dataDef = (DataDef)formItem;
 			item.setText(dataDef.toString());
 			treePanel.getStore().update(item);
 		}		
 		else if(formItem instanceof PredicateDef)
 		{
 			PredicateDef predicate = (PredicateDef)formItem;
 			ItemSetDef itemSet = (ItemSetDef)predicate.getParent();
 			item.setText(itemSet.getText());
 			treePanel.getStore().update(item);
 		}
 		return formItem;
 	}
 
 	
 	
 
 
 
 
 
 
 	/**
 	 * Gets the selected form.
 	 * 
 	 * @return the selected form.
 	 */
 	public FormDef getSelectedForm(){
 		return formDef; //we always have one form in openrosa form designer.
 	}
 
 
 	private TreeModelItem getSelectedItemRoot(TreeModelItem item){
 		if(item == null)
 			return null;
 
 		if(item.getParent() == null)
 			return item;
 		return getSelectedItemRoot((TreeModelItem)item.getParent());
 	}
 	
 	
 	
 
 	/**
 	 * Removes all forms.
 	 */
 	public void clear(){
 		FormDesignerController.clearIsDirty();
 		treePanel.getStore().removeAll();
 	}
 
 	/**
 	 * Checks if the selected form is valid for saving.
 	 * 
 	 * @return true if valid, else false.
 	 */
 	public boolean isValidForm(){
 		TreeModelItem  parent = getSelectedItemRoot((TreeModelItem)treePanel.getSelectionModel().getSelectedItem());
 		if(parent == null)
 			return true;
 
 		Map<String,String> pageNos = new HashMap<String,String>();
 		Map<String,QuestionDef> bindings = new HashMap<String,QuestionDef>();
 		int count = parent.getChildCount();
 		for(int index = 0; index < count; index++){		
 		}
 
 		return true;
 	}
 
 
 	/**
 	 * Checks if the selected form is in read only mode. In read only mode
 	 * we can only change the text and help text of items.
 	 * 
 	 * @return true if in read only mode, else false.
 	 */
 	private boolean inReadOnlyMode(){
 		return Context.isStructureReadOnly();
 	}
 
 
 	
 	public AbstractImagePrototype getIcon(TreeModelItem model){
 		ImageResource imageResource = images.newform();
 		int type = ((IFormElement)model.getUserObject()).getDataType();
 		if(!((IFormElement)model.getUserObject()).isVisible())
 		{
 			imageResource = images.hiddenQuestion();
 		}
 		if (model.getUserObject() instanceof DataDefBase || model.getUserObject() instanceof ItemSetDef)
 		{
 			imageResource = images.dataQuestion();
 		}
 
 		
 		if(type == QuestionDef.QTN_TYPE_GROUP)
 			imageResource = images.note();
 		else if(type == QuestionDef.QTN_TYPE_REPEAT)
 			imageResource = images.drafts();
 		
 		return AbstractImagePrototype.create(imageResource);
 	}
 	
 	
 
 		
 	
 
 	/**
 	 * returns the treeModelItem that represents the given form element
 	 * @param target the form element we're looking for
 	 * @return the treeModelItem that represents the given element, or null
 	 */
 	private TreeModelItem getTreeModelItemFromFormComponent(IFormElement target)
 	{
 		List<TreeModelItem> rootItems = treePanel.getStore().getRootItems();
 		for(TreeModelItem item : rootItems)
 		{
 			TreeModelItem result = getTreeModelItemFromFormComponentHelper(target, item);
 			if(result != null)
 			{
 				return result;
 			}
 		}
 		
 		return null;
 	}//end getTreeModelItemFromFormComponent()
 	
 	
 	private TreeModelItem getTreeModelItemFromFormComponentHelper(IFormElement target, TreeModelItem item)
 	{
 		//check the current item
 		if(item.getUserObject().equals(target))
 		{
 			return item;
 		}
 		//check the kids of the current item
 		for(ModelData _kid : item.getChildren())
 		{
 			TreeModelItem kid = (TreeModelItem)_kid;
 			TreeModelItem result = getTreeModelItemFromFormComponentHelper(target, kid);
 			//if we found something, return it
 			if(result != null)
 			{
 				return result;
 			}
 		}
 		
 		return null;
 	}//end getTreeModelItemFromFormComponentHelper()
 	
 	//********************************************* Handles updates to the form***********************************/
 	//********************************************* Handles updates to the form***********************************/
 	//********************************************* Handles updates to the form***********************************/
 	
 	
 	
 	/**
 	 * Used to add a new element to the form
 	 * @param form the form that has been updated
 	 * @param insertPoint the item that was highlighted when the question was inserted
 	 * @param newElement the new question
 	 */
 	public void handleAddNewElement(FormDef form, IFormElement insertPoint, IFormElement newElement)
 	{
 		handleAddNewElement(form, insertPoint, newElement, false);
 	}
 	
 	/**
 	 * This is called when a new question has been added to a form
 	 * @param form the form that has been updated
 	 * @param insertPoint the item that was highlighted when the question was inserted
 	 * @param newElement the new question
 	 * @param insertAsChild True if we want to add the item as a child, and not sibling, of insertPoint
 	 */
 	public TreeModelItem handleAddNewElement(FormDef form, IFormElement insertPoint, IFormElement newElement, boolean insertAsChild) 
 	{
 		//first make sure the affected form is the one this UI element is representing
 		if(!formDef.equals(form))
 		{
 			return null; //this is a form that doesn't conern us
 		}
 		//in case insert point is null
 		if(insertPoint == null)
 		{
 			insertPoint = form;
 		}
 		
 		//if we're adding a new question, make sure we're not inserting it on a data element
 		if(!(newElement instanceof DataDefBase) && (insertPoint instanceof DataDefBase))
 		{
 			insertPoint = form;
 		}
 		
 		//get the TreeModelItem that corresponds to insertPoint
 		TreeModelItem selectedItem = getTreeModelItemFromFormComponent(insertPoint);
 		
 		//if we couldn't find it, bounce
 		if(selectedItem == null)
 		{
 			return null;			
 		}
 				
 
 		//init the new form item
 		TreeModelItem newItem;
 		
 		//if we're not adding this to the form itself
 		if((insertPoint instanceof FormDef) || 
 				(insertPoint instanceof GroupDef) ||
 				((insertPoint instanceof QuestionDef) && (((QuestionDef)insertPoint).getDataType() == QuestionDef.QTN_TYPE_REPEAT )) ||
 				insertAsChild)
 		{
 			newItem = addImageItem(selectedItem, newElement.getText(), newElement);
 		}
 		else
 		{			
 			newItem = addImageItem((TreeModelItem)selectedItem.getParent(), newElement.getText(), newElement);
 		}
 		
 		treePanel.getSelectionModel().select(newItem, false);
 		treePanel.setExpanded(newItem, true);
 		
 		//now add any children this new element might have had
 		List<IFormElement> children = newElement.getChildren();
 		if(children != null)
 		{
 			for(IFormElement element : children)
 			{
 				handleAddNewElement(form, newElement, element, true);
 			}
 		}
 		
 		return newItem;
 	}//end handleAddNewQuestions()
 	
 	/**
 	 * Used to add a new tree UI item for an option that is added to a 
 	 * question
 	 * @param question
 	 * @param option
 	 */
 	public void handleAddNewOption(QuestionDef question, OptionDef option)
 	{
 		handleAddNewElement(question.getFormDef(), question, option, true);
 		
 	}//end handleAddNewOption()
 	
 	/**
 	 * Used to add a new tree UI item for an option that is added to a 
 	 * question
 	 * @param question
 	 * @param option
 	 */
 	private TreeModelItem handleAddNewOptionR(QuestionDef question, OptionDef option)
 	{
 		//figure out the UI element for the given question
 				TreeModelItem questionItem = getTreeModelItemFromFormComponent(question);
 				//create the option and add it to the tree
 				TreeModelItem modelItem = new TreeModelItem(option.getText(),option,questionItem);
 				treePanel.getStore().add(questionItem,modelItem, true);
 				treePanel.getSelectionModel().select(modelItem, false);
 				
 				//update the rest of the options in case they've changed
 				updateTreeText(questionItem);
 				
 				return modelItem;
 				
 	}//end handleAddNewOption()
 	
 	
 	/**
 	 * This removes the UI items that correspond to the
 	 * form elements that were just blown away.
 	 * @param selectedElements
 	 */
 	public void handleDelete(List<IFormElement> selectedElements) 
 	{
 		//loop over the elements and remove them
 		for(IFormElement element : selectedElements)
 		{
 			//get the TreeModelItem that corresponds to the given element
 			TreeModelItem item = getTreeModelItemFromFormComponent(element);
 			
 			TreeModelItem parent = (TreeModelItem)item.getParent();
 						
 			int index;
 			if(parent != null){			
 				index = parent.indexOf(item);
 
 				//If last item is the one selected, the select the previous, else the next.
 				if(index == parent.getChildCount()-1)
 					index -= 1;
 
 				//Remove the selected item.
 				parent.remove(item);
 				treePanel.getStore().remove(item);
 
 				//If no more kids, then select the parent.
 				if(parent.getChildCount() == 0)
 					treePanel.getSelectionModel().select(parent, false);
 				//tree.setSelectedItem(parent);
 				else
 					treePanel.getSelectionModel().select(parent.getChild(index), false);
 				//tree.setSelectedItem(parent.getChild(index));
 			}
 			else //Must be the form root, or a data instance
 			{ 
 				if(item.getUserObject() instanceof FormDef)
 				{
 					treePanel.getStore().removeAll();
 				}
 
 				
 				index = getRootItemIndex(item);
 				treePanel.getStore().remove(item);
 
 				int count = treePanel.getStore().getChildCount(); //tree.getItemCount();
 
 				//If we have any items left, select the one which was after
 				//the one we have just removed.
 				if(count > 0){
 
 					//If we have deleted the last item, select the item which was before it.
 					if(index == count)
 						index--;
 
 					//tree.setSelectedItem(tree.getItem(index));
 					treePanel.getSelectionModel().select(treePanel.getStore().getChild(index), false);
 				}
 			}
 
 
 			if(treePanel.getSelectionModel().getSelectedItem() == null){
 				//if they deleted everything then clear the dirty flag.
 				//If there's nothing there then it's clean
 				Context.setFormDef(null);
 				formDef = null;
 				fireFormItemSelected(null);
 
 				nextFormId = 1;
 				nextQuestionId = 0;
 				nextOptionId = 0;
 			}
 			
 			if(element instanceof OptionDef)
 			{
 				updateTreeText(parent);
 			}
 			
 		}//end big loop
 	}//end handleDelete()
 	
 	/**
 	 * This handles what happens when something is copied
 	 * @param selectedElements the things that have been copied
 	 */
 	public void handleCopy(List<IFormElement> selectedElements) 
 	{
 		inCutMode = false;
 	}//end handleCopy()
 	
 	/**
 	 * This hanldes what happens when something is cut.
 	 * @param selectedElements The things that are going to be cut
 	 */
 	public void handleCut(List<IFormElement> selectedElements) 
 	{
 		inCutMode = true;
 	}//end handleCut();
 	
 	/**
 	 * This handles new elements beign pasted into a form.
 	 * The pasted elements can come from anywhere by the way.
 	 * @param selectedElements Elements we'll be pasting into this current form
 	 */
 	public void handlePaste(IFormElement insertPoint, List<IFormElement> selectedElements, List<IFormElement> clipboard) 
 	{
 		//get the place in the tree where the pasted stuff should go 
 		TreeModelItem insertItem = getTreeModelItemFromFormComponent(insertPoint);
 		//loop over the pasted things
 		for(IFormElement element : selectedElements)
 		{
 			handlePasteHelper(insertItem, element);
 		}//end loop over the elements
 		
 		
 		
 	}//end handlePaste()
 	
 	
 	/**
 	 * This helps paste things into the tree view
 	 * @param insertPoint Where to insert the pasted stuff
 	 * @param element The pasted stuff
 	 */
 	private void handlePasteHelper(TreeModelItem insertPoint, IFormElement element)
 	{
 		//init our new UI component
 		TreeModelItem newItem = null;
 		//get whta the insert point, points to
 		IFormElement insertPointElement = (IFormElement)(insertPoint.getUserObject());
 		//do various things based on what we're inserting
 		//what if we're adding a form
 		if(element instanceof FormDef)
 		{
 			//just recurse over the kids of the form
 			List<IFormElement> kids = element.getChildren();
 			for(IFormElement kid : kids)
 			{
 				handlePasteHelper(insertPoint, kid);
 			}
 		}
 		//what if we're adding an item set
 		else if(element instanceof ItemSetDef)
 		{
 			if(insertPointElement instanceof QuestionDef && 
 					(((QuestionDef)insertPointElement).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
 					((QuestionDef)insertPointElement).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE))
 			{
 				//get the parent of the insertpoint
 				TreeModelItem parentItem = (TreeModelItem)(insertPoint);
 				//add the new image				
 				newItem = addImageItem(parentItem, element.getText(), element);
 				treePanel.getSelectionModel().select(newItem, false);
 			}
 			
 		}
 		//what if we're adding a option		
 		else if(element instanceof OptionDef)
 		{
 			//are we adding this to a question?
 			if(insertPointElement instanceof QuestionDef)
 			{
 				newItem = handleAddNewOptionR((QuestionDef)insertPointElement, (OptionDef)element);
 			}
 			else if(insertPointElement instanceof OptionDef) //or what if we're adding it with it's siblings
 			{
 				//get the parent of the insertpoint
 				TreeModelItem parentItem = (TreeModelItem)(insertPoint.getParent());
 				//get the index of the insert point
 				int index = parentItem.indexOf(insertPoint);
 				
 				
 				
 				newItem = addImageItem(parentItem, element.getText(), element, index);
 				treePanel.getStore().add(parentItem,newItem, true);
 				treePanel.getSelectionModel().select(newItem, false);
 				
 				
 				//update the rest of the options in case they've changed
 				updateTreeText(parentItem);
 			}
 		}
 		else if(element instanceof QuestionDef) // we're adding a question def
 		{
 			//are we adding this to a question?
 			if((insertPointElement instanceof RepeatQtnsDef || (insertPointElement instanceof QuestionDef && ((QuestionDef)insertPointElement).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 					|| insertPointElement instanceof FormDef
 					|| insertPointElement instanceof GroupDef
 					)
 			{
 				//insert the new item				
 				newItem = addImageItem(insertPoint, element.getText(), element);
 			}
 
 			else if(insertPointElement instanceof QuestionDef)
 			{
 				//get the parent of the insertpoint
 				TreeModelItem parentItem = (TreeModelItem)(insertPoint.getParent());
 				//get the index of the insert point
 				int index = parentItem.indexOf(insertPoint);
 				
 				//insert the new item				
 				newItem = addImageItem(parentItem, element.getText(), element, index);
 			}			
 			else if(insertPointElement instanceof OptionDef)
 			{
 				TreeModelItem parentItem = (TreeModelItem)(insertPoint.getParent().getParent());
 				//get the index of the insert point
 				int index = parentItem.indexOf(insertPoint.getParent());
 				newItem = addImageItem(parentItem, element.getText(), element, index);
 			}
 		}
 		else if (element instanceof GroupDef)
 		{
 			//are we adding this to a question?
 			if((insertPointElement instanceof RepeatQtnsDef || (insertPointElement instanceof QuestionDef && ((QuestionDef)insertPointElement).getDataType() == QuestionDef.QTN_TYPE_REPEAT))
 					|| insertPointElement instanceof FormDef
 					|| insertPointElement instanceof GroupDef
 					)
 			{
 				//insert the new item				
 				newItem = addImageItem(insertPoint, element.getText(), element);
 			}
 
 			else if(insertPointElement instanceof QuestionDef)
 			{
 				//get the parent of the insertpoint
 				TreeModelItem parentItem = (TreeModelItem)(insertPoint.getParent());
 				//get the index of the insert point
 				int index = parentItem.indexOf(insertPoint);
 				
 				//insert the new item				
 				newItem = addImageItem(parentItem, element.getText(), element, index);
 			}
 			
 			else if(insertPointElement instanceof OptionDef)
 			{
 				TreeModelItem parentItem = (TreeModelItem)(insertPoint.getParent().getParent());
 				//get the index of the insert point
 				int index = parentItem.indexOf(insertPoint.getParent());
 				newItem = addImageItem(parentItem, element.getText(), element, index);
 			}
 		}//end group def
 		else if(element instanceof DataInstanceDef || element instanceof DataDef)
 		{
 			handleAddData(insertPointElement, (DataDefBase)element);
 			return;
 		}
 		
 		//recurse to add kids
 		List<IFormElement> kids = element.getChildren();
 		if(kids != null)
 		{
 			for(IFormElement kid : kids)
 			{
 				handlePasteHelper(newItem, kid);
 			}
 		}
 	}//end handlePasteHelper
 
 
 	/**
 	 * This method lets the tree know it should add a new data item to the mix
 	 * @param selectedItem whatever the user has selected
 	 * @param element the data in question
 	 */
 	public void handleAddData(IFormElement selectedItem, DataDefBase element)
 	{
 		TreeModelItem parentItem = null;
 		if(selectedItem != null) 
 		{
 			parentItem = this.getTreeModelItemFromFormComponent(selectedItem);
 		}
 		
 		TreeModelItem instanceItem = null;
 		if(element instanceof DataInstanceDef)
 		{
 			DataInstanceDef dataInstanceDef = (DataInstanceDef)element;
 			
 			instanceItem = new TreeModelItem(LocaleText.get("dataInstance")+" - "+dataInstanceDef.toString(), dataInstanceDef, parentItem);
 			if(parentItem == null)
 			{
 				treePanel.getStore().add(instanceItem, true);
 				treePanel.getSelectionModel().select(instanceItem, false);
 				
 			}
 			else
 			{
 				treePanel.getStore().add(parentItem, instanceItem, true);
 				treePanel.getSelectionModel().select(instanceItem, false);
 			}
 		}
 		else if (element instanceof DataDef)
 		{
 			DataDef dataDef = (DataDef)element;
 			instanceItem = new TreeModelItem(dataDef.toString(), dataDef, parentItem);
 			if(parentItem == null)
 			{
 				treePanel.getStore().add(instanceItem, true);
 				treePanel.getSelectionModel().select(instanceItem, false);
 			}
 			else
 			{
 				treePanel.getStore().add(parentItem, instanceItem, true);
 				treePanel.getSelectionModel().select(instanceItem, false);
 			}
 		}
 		//loop over the kids and add them
 		for(int i = 0; i < element.getChildCount(); i++)
 		{
 			DataDefBase ddb = (DataDefBase)element;
 			handleAddData((IFormElement)(instanceItem.getUserObject()), (DataDefBase)(ddb.getChildAt(i)));
 		}
 		
 	}//end handleAddDataInstance
 
 
 	public void handleLoadNewForm(FormDef formDef){
 		
 		//Etherton: these were passed in as part of the method, but for conformity to interfaces
 		//they've been moved down here
 		boolean select = true;
 		boolean langRefresh = false;
 		treePanel.getStore().removeAll();
 		
 
 		formDef.setId(nextFormId);
 
 		if(!langRefresh){
 			int count = formDef.getQuestionCount();
 			if(nextQuestionId <= count)
 				nextQuestionId = count;
 
 			this.formDef = formDef;
 
 			if(formExists(formDef.getId()))
 				return;
 
 			//A temporary hack to ensure top level object is accessed.
 			//fireFormItemSelected(formDef);
 						
 		}
 
 		TreeModelItem formRoot = null;
 		if(showFormAsRoot){
 			formRoot = new TreeModelItem(formDef.getName(),formDef,null);
 			treePanel.getStore().add(formRoot, true);
 			
 			
 		}
 
 		if(formDef.getChildren() != null){
 			for(int index = 0; index < formDef.getChildren().size(); index++){
 				IFormElement element = formDef.getChildAt(index);
 				TreeModelItem pageRoot = null;
 
 
 				this.handleAddNewElement(formDef, formDef, element, true);
 				
 
 				//We expand only the first page.
 				if(index == 0)
 					treePanel.setExpanded(pageRoot, true);
 				//pageRoot.setState(true);    
 			}
 		}
 
 		if(select && formRoot != null){
 			//tree.setSelectedItem(formRoot);
 			//formRoot.setState(true);
 			treePanel.setExpanded(formRoot, true);
 			treePanel.getSelectionModel().select(formRoot, false);
 		}
 		
 		//Add the data instances if any			
 		if(formDef.getDataInstances().size() > 0)
 		{
 			HashMap<String, DataInstanceDef> dataInstances = formDef.getDataInstances();
 			
 			//load in the kids
 			Set<String> keys = dataInstances.keySet();
 			for(String key : keys)
 			{
 				DataDefBase dataDef = dataInstances.get(key);
 				//use null to make the data def add as root elements
 				handleAddData(null, dataDef);
 			}
 		}
 		
 		FormDesignerController.clearIsDirty();
 
 	}//end handleLoadNewForm();
 
 
 
 
 	/**
 	 * This is used to notify the UI that an element has been moved, most
 	 * likely as a reslut of a drag drop operation
 	 * @param thingToMove The thing that has been moved
 	 * @param newParentOfThingToMove the new parent of the thing that has been moved
 	 * @param newPosition the new position as a child of the parent for the thing that has been moved
 	 */
 	public void handleMoveFormObjects(IFormElement thingToMove, IFormElement newParentOfThingToMove, int newPosition) 
 	{
 		//get the items that correspond to these elements
 		TreeModelItem thingToMoveItem = getTreeModelItemFromFormComponent(thingToMove);
 		TreeModelItem newParentOfThingToMoveItem = getTreeModelItemFromFormComponent(newParentOfThingToMove);
 		
 
 		//remove thing to move
 		treePanel.getStore().remove(thingToMoveItem);
 		//remove from parent
 		thingToMoveItem.getParent().remove(thingToMoveItem);
 		//set new root of thing to move
 		thingToMoveItem.setParent(newParentOfThingToMoveItem);
 		//add the thing to move to it's new home.
 		treePanel.getStore().insert(newParentOfThingToMoveItem, thingToMoveItem, newPosition, true);
 		
 	}//end handleMoveFormObjects()
 
 
 
 
 	/**
 	 * Force the UI to update it's text
 	 * This'll generally happen when there's a language switch
 	 * @param element
 	 */
 	public void handleRefreshText(IFormElement element) 
 	{
 		TreeModelItem item = this.getTreeModelItemFromFormComponent(element);
 		updateTreeText(item);
 	}//end handleRefreshText()
 
 
 
 
 	/**
 	 * Used to set the selected item in the UI
 	 */
 	public void setItemAsSelected(IFormElement toSelect) 
 	{
 		TreeModelItem tmi = getTreeModelItemFromFormComponent(toSelect);
 		
 		treePanel.getSelectionModel().select(tmi, false);
 
 	}//end setItemAsSelected()
 	
 
 	
 }
