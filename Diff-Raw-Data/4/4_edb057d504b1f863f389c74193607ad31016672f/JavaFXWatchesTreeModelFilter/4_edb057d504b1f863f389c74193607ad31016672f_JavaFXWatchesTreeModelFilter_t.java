 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.debugger.watchesfiltering;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.netbeans.api.debugger.DebuggerManager;
 import org.netbeans.api.debugger.Watch;
 import org.netbeans.api.debugger.jpda.ClassVariable;
 import org.netbeans.api.debugger.jpda.Field;
 import org.netbeans.api.debugger.jpda.JPDADebugger;
 import org.netbeans.spi.debugger.ContextProvider;
 import org.netbeans.spi.viewmodel.ModelListener;
 import org.netbeans.spi.viewmodel.TreeModel;
 import org.netbeans.spi.viewmodel.TreeModelFilter;
 import org.netbeans.spi.viewmodel.UnknownTypeException;
 
 /**
  *
  * @author Michal Skvor
  */
 public class JavaFXWatchesTreeModelFilter implements TreeModelFilter {
 
     private final JPDADebugger debugger;
     private final Map<Watch, JavaFXWatch> watch2JavaFXWatch = new HashMap<Watch, JavaFXWatch>();
     private DebuggerListener listener;
 
     public JavaFXWatchesTreeModelFilter() {
         debugger = null;
     }
 
     public JavaFXWatchesTreeModelFilter( ContextProvider lookupProvider ) {
         debugger = (JPDADebugger)lookupProvider.lookupFirst( null, JPDADebugger.class );
     }
 
     public Object getRoot( TreeModel original ) {
         return original.getRoot();
     }
 
     public Object[] getChildren( TreeModel original, Object parent, int from, int to ) throws UnknownTypeException {
         if( parent == original.getRoot()) {
             Watch [] allWatches = DebuggerManager.getDebuggerManager().getWatches();
             Object [] result = original.getChildren( parent, from, to );
 
             //original model returns array of JPDAWatch-es, thus we must create an Object array
             //to allow merging with JspElWatch-es
             Object[] ch = new Object[result.length];
             System.arraycopy( result, 0, ch, 0, result.length );
 
             synchronized( watch2JavaFXWatch ) {
                 for( int i = from; i < allWatches.length; i++ ) {
                     Watch w = allWatches[i];
                     String expression = w.getExpression();
                     if( isJavaFXexpression( expression )) {
                         JavaFXWatch jw = watch2JavaFXWatch.get( w );
                         if( jw == null ) {
                             jw = new JavaFXWatch( w, debugger );
                             watch2JavaFXWatch.put( w, jw );
                         }
                         ch[i - from] = jw;
                     }
                 }
             }
 
             if( listener == null ) {
                 listener = new DebuggerListener( this, debugger );
             }
 
             return ch;
         } else if( parent instanceof JavaFXWatch ) {
             JavaFXWatch w = (JavaFXWatch)parent;
             // Filter variables
             Object children[] = original.getChildren( w.getVariable(), from, to );
             List vc = new ArrayList();
             for( int i = 0; i < children.length; i++ ) {
                 Object obj = children[i];
                 if( obj instanceof ClassVariable ) {
                     ClassVariable cv = (ClassVariable)obj;
 //                        vc.add( obj );
                 } else if( obj instanceof Field ) {
                     Field f = (Field)obj;
                     if( f.getName().startsWith( "$" )) {
                         vc.add( obj );
                     }
                 }
             }
             return vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         } else {
             return original.getChildren( parent, from, to );
         }
     }
 
     public int getChildrenCount( TreeModel original, Object node ) throws UnknownTypeException {
         if( node == original.getRoot() && listener == null ) {
             listener = new DebuggerListener( this, debugger );
         } else if( node instanceof JavaFXWatch ) {
             JavaFXWatch w = (JavaFXWatch)node;
            if( w.getValue() != null && w.getVariable() != null )
                 return original.getChildrenCount( w.getVariable());
         }
         return original.getChildrenCount( node );
     }
 
     public boolean isLeaf( TreeModel original, Object node ) throws UnknownTypeException {
         boolean il = true;
         if( node == original.getRoot()) {
             il = false;
         } else if( node instanceof JavaFXWatch ) {
             JavaFXWatch w = (JavaFXWatch)node;
             il = false;
 //            if( w.getVariable() != null )
 //                il = original.isLeaf( w.getVariable());
         }
         return il;
     }
 
     private boolean isJavaFXexpression( String expression ) {
         return true;
     }
 
     public void addModelListener( ModelListener l ) {
     }
 
     public void removeModelListener( ModelListener l ) {
     }
 
     void fireTreeChanged() {
         synchronized( watch2JavaFXWatch ) {
             for( JavaFXWatch javafxWatch : watch2JavaFXWatch.values()) {
                 javafxWatch.setUnevaluated();
             }
         }
     }
 
     private static class DebuggerListener implements PropertyChangeListener {
 
         WeakReference<JavaFXWatchesTreeModelFilter> javafxWatchesFilterRef;
         WeakReference<JPDADebugger> debuggerRef;
 
         DebuggerListener( JavaFXWatchesTreeModelFilter javafxWatchesFilter, JPDADebugger debugger ) {
             javafxWatchesFilterRef = new WeakReference<JavaFXWatchesTreeModelFilter>( javafxWatchesFilter );
             debuggerRef = new WeakReference<JPDADebugger>( debugger );
             debugger.addPropertyChangeListener( this );
         }
 
         public void propertyChange( PropertyChangeEvent evt ) {
 
             if( debuggerRef.get().getState() == JPDADebugger.STATE_DISCONNECTED ) {
                 destroy();
                 return;
             }
             if( debuggerRef.get().getState() == JPDADebugger.STATE_RUNNING ) {
                 return;
             }
 
             final JavaFXWatchesTreeModelFilter javafxWatchesFilter = getJavaFXWatchesFilter();
             if( javafxWatchesFilter != null ) {
                 javafxWatchesFilter.fireTreeChanged();
             }
         }
 
         private JavaFXWatchesTreeModelFilter getJavaFXWatchesFilter() {
             JavaFXWatchesTreeModelFilter javafxWatchesFilter = javafxWatchesFilterRef.get();
             if( javafxWatchesFilter == null ) {
                 destroy();
             }
             return javafxWatchesFilter;
         }
 
         private void destroy() {
             JPDADebugger debugger = debuggerRef.get();
             if( debugger != null ) {
                 debugger.removePropertyChangeListener( this );
             }
         }
     }
 }
