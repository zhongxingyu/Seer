 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Jul 15, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.concord.otrunk.view.document;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.PlainDocument;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTChangeNotifying;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.AbstractOTJComponentView;
 import org.concord.otrunk.view.AbstractOTJComponentContainerView;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class OTTextObjectView extends AbstractOTJComponentContainerView
 	implements DocumentListener, OTChangeListener
 {
 	protected OTDocument pfObject;
 	
 	protected PlainDocument textAreaModel = null;
 	protected JTextArea textArea;
 
     JLabel labelView = null;
     private boolean changingText = false;

	private boolean changeListenerAdded;
 	
 	protected void setup(OTObject pfTextObject)
 	{
 		this.pfObject = (OTDocument)pfTextObject;
 		if(pfTextObject instanceof OTChangeNotifying){
 		    ((OTChangeNotifying)pfTextObject).addOTChangeListener(this);
		    changeListenerAdded = true;
 		}
 	}
 
 	public void initTextAreaModel()
 	{
 		if(textAreaModel == null) {
 			textAreaModel = new PlainDocument();
 			try
 			{
 				textAreaModel.insertString(0, pfObject.getDocumentText(), null);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 
 			textAreaModel.addDocumentListener(this);
 		}		
 	}
 	
 	public JComponent getComponent(OTObject otObject)
 	{
 		setup(otObject);
 		
 		// To make something editable it is better to make:
 		// either a second view class should be created and a 
 		// separate view entry used to point to that.  Or a custom viewEntry should be 
 		// created with this view using a property of that view entry to determine 
 		// if it should be editable or not.
 		if(pfObject.getInput()) {
 		
 			initTextAreaModel();
 		
 			textArea = new JTextArea(textAreaModel);
 			textArea.setLineWrap(true);
 			textArea.setWrapStyleWord(true);
 			JScrollPane scrollPane = new JScrollPane(textArea);
 
 			textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 		
 			return scrollPane;
 		} else {
 		    labelView = new JLabel(pfObject.getDocumentText());
 			return labelView;
 		}
 	}
 
 
 	public void changedUpdate(DocumentEvent event)
 	{
 		try {
 		    changingText = true;
 			pfObject.setDocumentText(textAreaModel.getText(0, textAreaModel.getLength()));
 			changingText = false;
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTChangeListener#stateChanged(org.concord.framework.otrunk.OTChangeEvent)
      */
     public void stateChanged(OTChangeEvent e)
     {
         if(changingText){
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
             exc.printStackTrace();
         }
     }
     
 	public void insertUpdate(DocumentEvent event)
 	{
 		try {
 		    changingText = true;
 			pfObject.setDocumentText(textAreaModel.getText(0, textAreaModel.getLength()));
 			changingText = false;
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void removeUpdate(DocumentEvent event)
 	{
 		try {
 		    changingText = true;
 			pfObject.setDocumentText(textAreaModel.getText(0, textAreaModel.getLength()));
 			changingText = false;
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean isChangingText(){
 		return changingText;
 	}
 	
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.view.OTJComponentView#viewClosed()
      */
     public void viewClosed()
     {
         if(textAreaModel != null) {
             textAreaModel.removeDocumentListener(this);
         }
 
		if(pfObject instanceof OTChangeNotifying && changeListenerAdded){
 		    ((OTChangeNotifying)pfObject).removeOTChangeListener(this);
 		}
 
     }
 
 }
