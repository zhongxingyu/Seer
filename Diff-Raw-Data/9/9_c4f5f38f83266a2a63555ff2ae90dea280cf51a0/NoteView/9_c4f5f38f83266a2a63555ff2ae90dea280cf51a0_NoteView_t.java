 package net.todd.biblestudy.rcp.views;
 
 import java.util.List;
 
 import javax.swing.event.EventListenerList;
 
 import net.todd.biblestudy.db.NoteStyle;
 import net.todd.biblestudy.rcp.presenters.INoteListener;
 import net.todd.biblestudy.rcp.presenters.ViewEvent;
 
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 public class NoteView extends ViewPart implements INoteView
 {
 	public static final String ID = "net.todd.biblestudy.rcp.NoteView"; 
 	
 	EventListenerList eventListeners = new EventListenerList();
 
 	private Menu rightClickTextMenu;
 
 	private Point lastClickedCoordinates;
 	
 	private Composite parent;
 
 	private ITextViewer textViewer;
 
 	@Override
 	public void createPartControl(Composite parent)
 	{
 		this.parent = parent;
 		
 		GridLayout gridLayout = new GridLayout(1, false);
 		gridLayout.marginTop = 2;
 		gridLayout.marginBottom = 2;
 		gridLayout.marginLeft = 2;
 		gridLayout.marginRight = 2;
 		
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		
 		Composite composite = new Composite(parent, SWT.NONE);
 		
 		composite.setLayout(gridLayout);
 		composite.setLayoutData(gridData);
 		
 		createTextBox(composite);
 		createRightClickMenu(parent);
 	}
 
 	private void createRightClickMenu(Composite parent)
 	{
 		rightClickTextMenu = new Menu(parent);
 		rightClickTextMenu.setVisible(false);
 		MenuItem createLink = new MenuItem(rightClickTextMenu, SWT.POP_UP);
 		createLink.setText("Create link");
 		createLink.setEnabled(true);
 		createLink.addSelectionListener(new SelectionAdapter() 
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				fireEvent(new ViewEvent(ViewEvent.NOTE_CREATE_LINK_EVENT));
 			}
 		});
 	}
 	
 	private void fireEvent(ViewEvent event)
 	{
 		INoteListener[] listeners = eventListeners.getListeners(INoteListener.class);
 		
 		for (INoteListener listener : listeners)
 		{
 			listener.handleEvent(event);
 		}
 	}
 
 	private void createTextBox(Composite parent)
 	{
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		
 		textViewer = new TextViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		textViewer.setDocument(new Document());
 		textViewer.getTextWidget().setLayoutData(gridData);
 		
 		textViewer.getTextWidget().setLayoutData(gridData);
 		textViewer.getTextWidget().addModifyListener(new ModifyListener() 
 		{
 			/*
 			 * (non-Javadoc)
 			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
 			 */
 			public void modifyText(ModifyEvent e)
 			{
 				fireEvent(new ViewEvent(ViewEvent.NOTE_CONTENT_CHANGED));
 			}
 		});
 		textViewer.getTextWidget().addMouseListener(new MouseAdapter() 
 		{
 			/*
 			 * (non-Javadoc)
 			 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
 			 */
 			public void mouseUp(MouseEvent e)
 			{
				if (e.stateMask == SWT.BUTTON3 || e.stateMask == (SWT.BUTTON1 | SWT.CTRL))
				{	// right-click and ctrl+mouse1 for macs
 					lastClickedCoordinates = new Point(e.x, e.y);
 					fireEvent(new ViewEvent(ViewEvent.NOTE_SHOW_RIGHT_CLICK_MENU));
 				}
				if (e.stateMask == SWT.BUTTON1)
 				{
 					Point point = new Point(e.x, e.y);
 					
 					int offset = textViewer.getTextWidget().getOffsetAtLocation(point);
 					
 					ViewEvent viewEvent = new ViewEvent(ViewEvent.NOTE_CLICKED);
 					viewEvent.setData(offset);
 					fireEvent(viewEvent);
 				}
 			}
 		});
 		textViewer.getTextWidget().addMouseMoveListener(new MouseMoveListener()
 		{
 			/*
 			 * (non-Javadoc)
 			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
 			 */
 			public void mouseMove(MouseEvent e)
 			{
 				Point point = new Point(e.x, e.y);
 				
 				int offset = textViewer.getTextWidget().getOffsetAtLocation(point);
 				
 				ViewEvent viewEvent = new ViewEvent(ViewEvent.NOTE_HOVERING);
 				viewEvent.setData(offset);
 				fireEvent(viewEvent);
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#getLastClickedCoordinates()
 	 */
 	public Point getLastClickedCoordinates()
 	{
 		return lastClickedCoordinates;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#showRightClickPopup(int, int)
 	 */
 	public void showRightClickPopup(int x, int y)
 	{
 		String selectionText = textViewer.getTextWidget().getSelectionText();
 		
 		if (selectionText == null || selectionText.length() != 0)
 		{
 			Point point = parent.toDisplay(x, y);
 			rightClickTextMenu.setLocation(point);
 			rightClickTextMenu.setVisible(true);
 		}
 	}
 
 	@Override
 	public void setFocus()
 	{
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#addNoteViewListener(net.todd.biblestudy.rcp.presenters.INoteListener)
 	 */
 	public void addNoteViewListener(INoteListener noteListener)
 	{
 		eventListeners.add(INoteListener.class, noteListener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#setContentText(java.lang.String)
 	 */
 	public void setContentText(String text)
 	{
 		if (text != null)
 		{
 			textViewer.getTextWidget().setText(text);
 		}
 	}
 	
 	public String getContentText()
 	{
 		return textViewer.getTextWidget().getText();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#setViewTitle(java.lang.String)
 	 */
 	public void setViewTitle(String title)
 	{
 		setPartName(title);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
 	 */
 	public void dispose()
 	{
 		fireEvent(new ViewEvent(ViewEvent.NOTE_CLOSE));
 		
 		super.dispose();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#removeNoteViewListener(net.todd.biblestudy.rcp.presenters.INoteListener)
 	 */
 	public void removeNoteViewListener(INoteListener noteListener)
 	{
 		eventListeners.remove(INoteListener.class, noteListener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#getSelectedText()
 	 */
 	public String getSelectedText()
 	{
 		return textViewer.getTextWidget().getSelectionText();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#getSelectionPoint()
 	 */
 	public Point getSelectionPoint()
 	{
 		return textViewer.getTextWidget().getSelection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#saveNote()
 	 */
 	public void saveNote()
 	{
 		fireEvent(new ViewEvent(ViewEvent.NOTE_SAVE));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#deleteNote()
 	 */
 	public void deleteNote()
 	{
 		fireEvent(new ViewEvent(ViewEvent.NOTE_DELETE));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#closeView(java.lang.String)
 	 */
 	public void closeView(String secondaryId)
 	{
 		IViewReference viewReference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(ID, secondaryId);
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewReference);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#replaceNoteStyles(java.util.List)
 	 */
 	public void replaceNoteStyles(List<NoteStyle> styleList)
 	{
 		for (NoteStyle style : styleList)
 		{
 			StyleRange styleRange = convertToStyleRange(style);
 			
 			textViewer.getTextWidget().setStyleRange(styleRange);
 		}
 	}
 	
 	private StyleRange convertToStyleRange(NoteStyle style)
 	{
 		StyleRange styleRange = new StyleRange();
 		styleRange.start = style.getStart();
 		styleRange.length = style.getLength();
 		styleRange.underline = style.isUnderlined();
 		
 		return styleRange;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#changeCursorToPointer()
 	 */
 	public void changeCursorToPointer()
 	{
 		Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
 		textViewer.getTextWidget().setCursor(cursor);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.todd.biblestudy.rcp.views.INoteView#changeCursorToText()
 	 */
 	public void changeCursorToText()
 	{
 		Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM);
 		textViewer.getTextWidget().setCursor(cursor);
 	}
 }
