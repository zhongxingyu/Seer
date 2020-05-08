 /*
  *(c) Copyright QNX Software Systems Ltd. 2002.
  * All Rights Reserved.
  * 
  */
 
 package org.rubypeople.rdt.internal.debug.ui.actions;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.text.source.IVerticalRulerInfo;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
 import org.eclipse.ui.texteditor.ITextEditor;
 
/**
 * 
 * Enter type comment.
 * 
 * @since Aug 23, 2002
 */
 public class ManageBreakpointRulerActionDelegate extends AbstractRulerActionDelegate
 {
 	static final private String EDITOR_ID = "org.rubypeople.rdt.ui.EditorRubyFile"; //$NON-NLS-1$
 
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
 	public void setActiveEditor( IAction callerAction, IEditorPart targetEditor )
 	{		
 		if ( targetEditor != null )
 		{
 			String id = targetEditor.getSite().getId();
			System.out.println("setActiveEditor, id="+id) ;
 			if ( !id.equals( EDITOR_ID ) )
 				targetEditor = null;
 		}
 		super.setActiveEditor( callerAction, targetEditor );
 	}
 
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
 	public IAction createAction( ITextEditor editor, IVerticalRulerInfo rulerInfo )
 	{
		System.out.println("createAction") ;
 		return new ManageBreakpointRulerAction( rulerInfo, editor );
 	}
 }
