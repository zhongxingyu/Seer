 /*
  * Last modification information:
 * $Revision: 1.25 $
 * $Date: 2007-10-16 18:36:42 $
  * $Author: sfentress $
  *
  * Licence Information
  * Copyright 2007 The Concord Consortium 
 */
 package org.concord.otrunk.view.document;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeNotifying;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewEntryAware;
 import org.concord.framework.otrunk.view.OTViewFactory;
 import org.concord.otrunk.view.OTObjectEditViewConfig;
 import org.concord.otrunk.view.OTObjectListViewer;
import org.concord.otrunk.view.OTViewContainerPanel;
import org.concord.otrunk.view.OTViewContainerPanel.MyViewContainer;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 /**
  * OTCompoundDocEditView
  * This is an edit view for the compound document
  * It has a split pane with a preview in the bottoms
  * and a text area at the top for editing the code of the document 
  *
  * Date created: Feb 5, 2007
  *
  * @author Ingrid Moncada<p>
  *
  */
 public class OTCompoundDocEditView extends AbstractOTDocumentView
 	implements OTViewEntryAware, ActionListener
 {
 	protected JPanel textPanel;
 	protected OTDocumentView previewView;
 	protected OTObjectEditViewConfig viewEntry;
 	protected OTObject document;
 	private JComponent editTextPane;
 	
 	/**
 	 * 
 	 */
 	public OTCompoundDocEditView()
 	{
 		super();
 	}
 
 	public JComponent getComponent(OTObject otObject)
 	{
 		document = otObject;
 		((OTChangeNotifying)document).addOTChangeListener(this);
 		
 		//Get the OTDocumentView view as the 'hardcoded' preview  
 		// this uses the NO_VIEW_MODE so the returned OTDocumentView doesn't get replaced by 
 		// the edit view if the default mode is set to something else.
 		previewView = (OTDocumentView)getViewFactory().getView(document, OTDocumentView.class, OTViewFactory.NO_VIEW_MODE);
 
 		// no view container exists...
 	//	MyViewContainer previewViewContainer = (MyViewContainer)previewView.getViewContainer();
 	//	previewViewContainer.setParentContainer(getViewContainer());
 		
 		//set mode of OTDocumentView to mode set for OTObjectEditViewConfig, so objects
 		//in the Doc will be in the correct mode
 		previewView.setViewMode(viewEntry.getMode());
 				
 		//Create a split pane with the preview pane and the text area 
 		editTextPane = super.getComponent(document);
 		final JPanel editPane = createTextPanel();
 		
 		//If preview is not available, don't use it then
 		if (previewView == null){
 			return editPane;
 		} else if (viewEntry.getUsePopupEditWindows()){
 			JComponent previewPane = previewView.getComponent(document);
 			
 			previewPane.addMouseListener(new MouseListener(){
 
 				public void mouseClicked(MouseEvent evt)
                 {
 					if (!MouseEvent.getMouseModifiersText(evt.getModifiers()).equalsIgnoreCase("Button1")){
 						JFrame frame = new JFrame();
 		                frame.getContentPane().add(editPane);
 		                frame.pack();
 		                frame.setVisible(true);
 					}
                 }
 
 				public void mouseEntered(MouseEvent arg0)
                 {
 	                // TODO Auto-generated method stub
 	                
                 }
 
 				public void mouseExited(MouseEvent arg0)
                 {
 	                // TODO Auto-generated method stub
 	                
                 }
 
 				public void mousePressed(MouseEvent arg0)
                 {
 	                // TODO Auto-generated method stub
 	                
                 }
 
 				public void mouseReleased(MouseEvent arg0)
                 {
 	                // TODO Auto-generated method stub
 	                
                 }});
 			return previewPane;
 		} else{
 			JComponent previewPane = previewView.getComponent(document);
 			
 			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
 			                           editPane, previewPane);
 			splitPane.setOneTouchExpandable(true);
 			splitPane.setDividerLocation(150);
 	
 			return splitPane;
 		}
 	}
 	
 	/**
 	 * Sets up the top panel that allows to edit the text and 
 	 * also has the buttons to insert objects
 	 */
 	public JPanel createTextPanel()
 	{
 		textPanel = new JPanel();
 		textPanel.setLayout(new BorderLayout());
 		
 		//Add the text edit pane at the top
 		textPanel.add(editTextPane);
 		
 		//Create buttons and add to the bottom
 		JPanel buttonsPanel = new JPanel();
 
 		JButton addObjectButton = new JButton("Insert Object");
 		buttonsPanel.add(addObjectButton);
 		addObjectButton.setActionCommand("insertObject");
 		addObjectButton.addActionListener(this);
 		
 		JButton updatePreviewButton = new JButton("Update Preview");
 		buttonsPanel.add(updatePreviewButton);
 		updatePreviewButton.setActionCommand("updatePreview");
 		updatePreviewButton.addActionListener(this);
 		
 		textPanel.add(buttonsPanel, BorderLayout.NORTH);
 		
 		return textPanel;
 	}
 
 	/**
 	 * Listens to insert button
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getActionCommand().equals("insertObject")){
 			
 			if (textArea == null){
 				System.err.println("Error, CompoundDocEditPane has no text area to use. Should I use the compound doc directly?");
 				return;
 			}
 			
 			OTObject objToInsert = getObjectToInsertFromUser();
 			
 			if (objToInsert == null){
 				//No object to insert. Either we couldn't find one, or the user changed his mind
 				return;
 			}
 			
 			OTObjectService objectService = document.getOTObjectService();
 			String strObjID = objectService.getExternalID(objToInsert);
 			
 			String paragraphBreak = "";
 			if (viewEntry.getAddParagraphAfterObject()){
 				paragraphBreak = "<p/>";
 			}
 			String strObjText = "<object refid=\"" + strObjID + "\" />" + paragraphBreak;
 			
 			int pos = textArea.getSelectionStart();
 			textArea.insert(strObjText, pos);
 			
 			updatePreviewView();
 			
 		}
 		else if (e.getActionCommand().equals("updatePreview")){
 			boolean textHasErrors = false;
 			SAXBuilder builder = new SAXBuilder();
 			try {
 				String documentText = "<root>" + ((OTCompoundDoc)document).getBodyText() + "</root>";				
 	        	StringReader reader = new StringReader(documentText);
 	            builder.build(reader);
             } catch (JDOMException e1) {
             	String warning = "Warning: There is an HTML error in your text.\n " + e1.getLocalizedMessage();
             	JOptionPane.showMessageDialog(textPanel, warning,
             		    "HTML error",
             		    JOptionPane.ERROR_MESSAGE);
             	System.err.println("User-generated XML error: "+e1.getCause());
             	textHasErrors = true;
             } catch (IOException e1) {
 	            e1.printStackTrace();
             }
             
             if (!textHasErrors){
             	updatePreviewView();
             }
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void updatePreviewView()
 	{
 		if (previewView == null) return;
 		previewView.updateFormatedView();
 	}
 
 	/**
 	 * Shows a dialog with the list of possible objects to insert and lets the user choose
 	 * 
 	 * @return OT Object selected by the user
 	 */
 	private OTObject getObjectToInsertFromUser()
 	{
 		OTObject otObj = null;
 		
 		otObj = OTObjectListViewer.showDialog(textPanel, "Choose object to add", getFrameManager(), getViewFactory(), 
 				viewEntry.getObjectsToInsert(), ((OTCompoundDoc)pfObject).getOTObjectService(), true, viewEntry.getCopyNewObjectsByDefault());
 		
 		return otObj;
 	}
 
 	/**
 	 * @see org.concord.framework.otrunk.view.OTViewEntryAware#setViewEntry(OTViewEntry)
 	 */
 	public void setViewEntry(OTViewEntry viewEntry)
 	{
 		this.viewEntry = (OTObjectEditViewConfig)viewEntry;
 	}
 	
 	public OTObjectEditViewConfig getViewEntry()
 	{
 		return viewEntry;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.view.document.AbstractOTDocumentView#viewClosed()
 	 */
 	public void viewClosed() 
 	{
 		super.viewClosed();
 		
 		if(previewView != null){
 			previewView.viewClosed();
 		}
 	}
 	
 	public void stateChanged(OTChangeEvent e){
 
         if(isChangingText()){
             // we have caused this event ourselves
             return;
         }
         
         if(labelView != null) {
             labelView.setText(pfObject.getDocumentText());
             return;
         }
         
         try {
             textAreaModel.replace(0, textAreaModel.getLength(), pfObject.getDocumentText(), null);
         } catch (Exception exc) {
          //   exc.printStackTrace();
         }
         updatePreviewView();
         
 	}
 }
