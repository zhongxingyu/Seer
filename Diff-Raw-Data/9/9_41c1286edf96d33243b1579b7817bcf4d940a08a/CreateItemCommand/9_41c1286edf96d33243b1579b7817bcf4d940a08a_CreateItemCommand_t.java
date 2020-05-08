 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.commands;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jst.pagedesigner.dom.IDOMPosition;
 import org.eclipse.jst.pagedesigner.editors.palette.TagToolPaletteEntry;
 import org.eclipse.jst.pagedesigner.utils.CommandUtil;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.w3c.dom.Element;
 
 /**
  * @author mengbo
  */
 public class CreateItemCommand extends DesignerCommand {
 	private final IDOMPosition _position;
 
 	private final TagToolPaletteEntry _tagItem;
 	private Element _ele;
 	private IAdaptable _customizationData;
 
 	/**
 	 * @param label
 	 * @param model 
 	 * @param position 
 	 * @param tagItem 
 	 */
 	public CreateItemCommand(String label, IDOMModel model,
 			IDOMPosition position, TagToolPaletteEntry tagItem) {
 		super(label, model.getDocument());
 		this._position = position;
 		this._tagItem = tagItem;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.commands.DesignerCommand#doExecute()
 	 */
 	protected void doExecute() {
 		Element element = CommandUtil.excuteInsertion(this._tagItem,
 				getModel(), this._position, this._customizationData);
 		if (element != null) {
 			formatNode(element);
 		}
 		this._ele = element;
 	}
 
 	@Override
     protected void postPostExecute() 
 	{
         // during JUnit testing, we may not have viewer.
         // this will cause us not to have undo support,
         // but should not effect testing for this command
 	    if (getViewer() != null)
         {
             super.postPostExecute();
         }
     }
 
     @Override
     protected boolean prePreExecute() 
     {
         // during JUnit testing, we may not have viewer.
         // this will cause us not to have undo support,
         // but should not effect testing for this command
         if (getViewer() != null)
         {
             return super.prePreExecute();
         }
         
         return true;
     }
 
     /*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.commands.DesignerCommand#getAfterCommandDesignerSelection()
 	 */
 	protected ISelection getAfterCommandDesignerSelection() {
 		return toDesignSelection(_ele);
 	}
 
     /**
      * @param customizationData
      */
     public void setCustomizationData(IAdaptable customizationData) 
     {
         _customizationData = customizationData;
     }
     
     /**
      * This method is for test purposes and should generally not be 
      * used by clients.
      * 
      * @return the customization data
      */
     protected final IAdaptable getCustomizationData()
     {
         return _customizationData;
     }
    
    /**
     * @return the result of the command execution
     * TODO: add Object getResult() method to DesignerCommand
     */
    protected Element getResult()
    {
        return this._ele;
    }
 }
