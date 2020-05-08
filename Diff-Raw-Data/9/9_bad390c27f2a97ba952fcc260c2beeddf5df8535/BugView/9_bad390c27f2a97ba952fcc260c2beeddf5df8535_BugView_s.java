 /*******************************************************************************
  * Copyright (c) 2011 Ralf Sternberg.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ralf Sternberg - initial implementation and API
  ******************************************************************************/
 package ralfstx.mylyn.bugview.internal;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.mylyn.tasks.core.ITask;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.ViewPart;
 
 
 public class BugView extends ViewPart {
 
   static final int COL_ID = 0;
   static final int COL_TITLE = 1;
 
   private TableViewer viewer;
   private Text searchField;
   private Collection<TaskFilter> searchFilters = Collections.emptyList();
   private final SearchQueryParser queryParser = new SearchQueryParser();
 
   @Override
   public void createPartControl( Composite parent ) {
     parent.setLayout( createMainLayout() );
     createSearchTextField( parent );
     createTableViewer( parent );
     makeActions();
     refreshViewer();
   }
 
   @Override
   public void setFocus() {
     viewer.getControl().setFocus();
   }
 
   private void createSearchTextField( Composite parent ) {
     searchField = new Text( parent, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH | SWT.ICON_CANCEL );
     searchField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
     searchField.addSelectionListener( new SelectionAdapter() {
 
       @Override
       public void widgetDefaultSelected( SelectionEvent e ) {
         String query = searchField.getText().trim();
         searchFilters = queryParser.parse( query );
         viewer.refresh( false );
       }
     } );
   }
 
   private void createTableViewer( Composite parent ) {
     Table table = new Table( parent, SWT.VIRTUAL );
     table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
     table.setLinesVisible( true );
     new TableColumn( table, SWT.LEFT ).setWidth( 80 );
     new TableColumn( table, SWT.LEFT ).setWidth( 400 );
     viewer = new TableViewer( table );
     viewer.setLabelProvider( new TaskLabelProvider() );
     viewer.setContentProvider( new ArrayContentProvider() );
     viewer.setComparator( new TaskLastModifiedComparator() );
     viewer.addFilter( new TaskViewerFilter() );
     addStrikeThrough();
     addDoubleClickBehavior();
   }
 
   private void addStrikeThrough() {
     StrikeThroughProvider strikeThroughProvider = new StrikeThroughProvider() {
       public boolean getStrikeThrough( Object element ) {
         if( element instanceof ITask ) {
           ITask task = (ITask)element;
           return task.isCompleted();
         }
         return false;
       }
     };
     TableViewerStrikeThroughUtil.attach( strikeThroughProvider, viewer );
   }
 
   private void addDoubleClickBehavior() {
     viewer.addDoubleClickListener( new IDoubleClickListener() {
       public void doubleClick( DoubleClickEvent event ) {
         ITask selectedTask = getSelectedTask();
         if( selectedTask != null ) {
           MylynBridge.openTaskInEditor( selectedTask );
         }
       }
     } );
   }
 
   private void makeActions() {
     ImageDescriptor refreshImage = Activator.getImageDescriptor( "/icons/refresh.gif" );
     IAction refreshAction = new Action( "Refresh", refreshImage ) {
       @Override
       public void run() {
         refreshViewer();
       }
     };
     getViewSite().getActionBars().getToolBarManager().add( refreshAction );
   }
 
   private void refreshViewer() {
     Collection<ITask> tasks = MylynBridge.getAllTasks();
     viewer.setInput( tasks );
     updateStatusBar();
   }
 
   private ITask getSelectedTask() {
     ISelection selection = viewer.getSelection();
     if( !selection.isEmpty() && selection instanceof StructuredSelection ) {
       StructuredSelection structuredSelection = (StructuredSelection)selection;
       Object element = structuredSelection.getFirstElement();
       if( element instanceof ITask ) {
         return (ITask)element;
       }
     }
     return null;
   }
 
   private void updateStatusBar() {
     String message = null;
    Object input = viewer.getInput();
    if( input instanceof Collection ) {
      Collection<?> collection = (Collection<?>)input;
      message = collection.size() + " bugs";
    }
     IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
     statusLineManager.setMessage( message );
   }
 
   private static GridLayout createMainLayout() {
     GridLayout mainLayout = new GridLayout();
     mainLayout.marginWidth = 0;
     mainLayout.marginHeight = 0;
     mainLayout.verticalSpacing = 2;
     return mainLayout;
   }
 
   private static final class TaskLastModifiedComparator extends ViewerComparator {
     @Override
     public int compare( Viewer viewer, Object element1, Object element2 ) {
       int result = 0;
       if( element1 instanceof ITask && element2 instanceof ITask ) {
         result = compareModificationDate( (ITask)element1, (ITask)element2 ) * -1;
       }
       return result;
     }
 
     private static int compareModificationDate( ITask task1, ITask task2 ) {
       int result;
       Date modDate1 = task1.getModificationDate();
       Date modDate2 = task2.getModificationDate();
       if( modDate1 == null ) {
         result = modDate2 == null ? 0 : -1;
       } else {
         result = modDate2 == null ? 1 : modDate1.compareTo( modDate2 );
       }
       return result;
     }
   }
 
   private final class TaskViewerFilter extends ViewerFilter {
     @Override
     public boolean select( Viewer viewer, Object parentElement, Object element ) {
       if( element instanceof ITask ) {
         ITask task = (ITask)element;
         for( TaskFilter filter : searchFilters ) {
           if( !filter.matches( task ) ) {
             return false;
           }
         }
         return true;
       }
       return false;
     }
   }
 
 }
