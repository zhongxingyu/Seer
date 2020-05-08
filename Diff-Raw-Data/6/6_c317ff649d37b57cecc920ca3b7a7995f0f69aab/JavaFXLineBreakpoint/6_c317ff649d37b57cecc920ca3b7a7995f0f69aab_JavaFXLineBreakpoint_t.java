 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 
 package org.netbeans.modules.javafx.debugger.breakpoints;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import org.netbeans.api.debugger.Breakpoint;
 import org.netbeans.api.debugger.DebuggerManager;
 import org.netbeans.api.debugger.jpda.JPDABreakpoint;
 import org.netbeans.api.debugger.jpda.LineBreakpoint;
 import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
 import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
 import org.netbeans.modules.javafx.debugger.utils.Utils;
 
 /**
  *
  * @author Michal Skvor
  */
 public class JavaFXLineBreakpoint extends Breakpoint implements PropertyChangeListener {
     private LineBreakpoint javalb;
 
     private boolean enabled = true;
     private boolean hidden = false;
     private String url = ""; // NOI18N
     private int lineNumber;
     private String condition = ""; // NOI18N
     private String printText;
 
 
     public static final int SUSPEND_ALL = JPDABreakpoint.SUSPEND_ALL;
     public static final int SUSPEND_EVENT_THREAD = JPDABreakpoint.SUSPEND_EVENT_THREAD;
     public static final int SUSPEND_NONE = JPDABreakpoint.SUSPEND_NONE;
     private int suspend = SUSPEND_ALL;
 
     public static final String PROP_SUSPEND = JPDABreakpoint.PROP_SUSPEND;
     public static final String PROP_HIDDEN = JPDABreakpoint.PROP_HIDDEN;
     public static final String PROP_URL = LineBreakpoint.PROP_URL;
     public static final String PROP_LINE_NUMBER = LineBreakpoint.PROP_LINE_NUMBER;
     public static final String PROP_CONDITION = LineBreakpoint.PROP_CONDITION;
     public static final String PROP_PRINT_TEXT = JPDABreakpoint.PROP_PRINT_TEXT;
 
     public JavaFXLineBreakpoint() {}
 
     public JavaFXLineBreakpoint( String url, int lineNumber ) {
         super();
 
         this.url = url;
         this.lineNumber = lineNumber;
 
         String pt = "Breakpoint reached at line {lineNumber} in {fxName} by thread {threadName}.";
         this.printText = org.openide.util.Utilities.replaceString( pt, "{fxName}", Utils.getFXName( url ));
         
 
         javalb = LineBreakpoint.create( url, lineNumber );
         javalb.setSourceName( Utils.getFXName( url ));
         javalb.setSourcePath( Utils.getFXPath( url ));
         javalb.setPreferredClassName( Utils.getFXClassName( url, lineNumber ) + "*" ); // HACK: Add all class files
         javalb.setHidden( true );
         javalb.setPrintText( printText );
 //        javalb.setCondition( "true" );
         javalb.addPropertyChangeListener( this );
 
         String context = Utils.getContextPath( url );
 
         // FIXME: determine 'real' context path for web app based on used application server
         // See issues 146793, 161026, 162286 and 162715 (new API request)
 
 //        String condition = "request.getContextPath().equals(\"" + context + "\")"; // NOI18N
 //        javalb.setCondition(condition);
 
         DebuggerManager d = DebuggerManager.getDebuggerManager();
         d.addBreakpoint( javalb );
         this.url = url;
         this.lineNumber = lineNumber;
     }
 
     public LineBreakpoint getLineBreakpoint() {
         return javalb;
     }
     /**
      * Creates a new breakpoint for given parameters.
      *
      * @param url a url
      * @param lineNumber a line number
      * @return a new breakpoint for given parameters
      */
     public static JavaFXLineBreakpoint create( String url, int lineNumber ) {
         return new JavaFXLineBreakpoint( url, lineNumber );
     }
 
     @Override
     public boolean isEnabled() {
         return enabled;
     }
 
     @Override
     public void disable() {
         if( !enabled ) return;
         enabled = false;
 
         if( javalb != null ) {
             javalb.disable();
         }
 
         firePropertyChange( PROP_ENABLED, Boolean.TRUE, Boolean.FALSE );
     }
 
     /**
      * Gets value of suspend property.
      *
      * @return value of suspend property
      */
     public int getSuspend() {
         return suspend;
     }
 
     /**
      * Sets value of suspend property.
      *
      * @param s a new value of suspend property
      */
     public void setSuspend( int s ) {
         if( s == suspend ) return;
         int old = suspend;
         suspend = s;
         if( javalb != null ) {
             javalb.setSuspend( s );
         }
         firePropertyChange( PROP_SUSPEND, new Integer( old ), new Integer( s ));
     }
 
     /**
      * Gets value of hidden property.
      *
      * @return value of hidden property
      */
     public boolean isHidden () {
         return hidden;
     }
 
     /**
      * Sets value of hidden property.
      *
      * @param h a new value of hidden property
      */
     public void setHidden( boolean h ) {
         if( h == hidden ) return;
         boolean old = hidden;
         hidden = h;
         firePropertyChange( PROP_HIDDEN, Boolean.valueOf( old ), Boolean.valueOf( h ));
     }
 
     @Override
     public void enable() {
         if( enabled ) return;
         enabled = true;
 
         if( javalb != null ) {
             javalb.enable();
         }
 
         firePropertyChange( PROP_ENABLED, Boolean.FALSE, Boolean.TRUE );
     }
 
     public void removeLineBreakpoint() {
         DebuggerManager d = DebuggerManager.getDebuggerManager();
         d.removeBreakpoint( javalb );
     }
 
     /**
      * Sets name of class to stop on.
      *
      * @param cn a new name of class to stop on
      */
     public void setURL( String url ) {
         if(( url == this.url ) ||
              (( url != null ) && ( this.url != null ) && url.equals( this.url ))
         ) return;
         String old = this.url;
         this.url = url;
         if( javalb != null ) {
             javalb.setSourceName( Utils.getFXName( url ));
             javalb.setSourcePath( Utils.getFXPath( url ));
             javalb.setPreferredClassName( Utils.getFXClassName( url, lineNumber ) + "*" ); // HACK: Add all class files
         }
         firePropertyChange( PROP_URL, old, url );
     }
 
     /**
      * Gets name of class to stop on.
      *
      * @return name of class to stop on
      */
     public String getURL() {
         return url;
     }
 
     /**
      * Gets number of line to stop on.
      *
      * @return line number to stop on
      */
     public int getLineNumber() {
         return lineNumber;
     }
 
     /**
      * Sets number of line to stop on.
      *
      * @param ln a line number to stop on
      */
     public void setLineNumber( int ln ) {
         if( ln == lineNumber ) return;
         int old = lineNumber;
         lineNumber = ln;
         if( javalb != null ) {
             javalb.setLineNumber( ln );
         }
         firePropertyChange( PROP_LINE_NUMBER, new Integer( old ), new Integer( getLineNumber()));
     }
 
     /**
      * Sets condition.
      *
      * @param c a new condition
      */
     public void setCondition( String c ) {
         if( c != null ) c = c.trim ();
         if(( c == condition ) ||
              (( c != null ) && ( condition != null ) && condition.equals( c ))
         ) return;
         String old = condition;
         condition = c;
         if (javalb != null) {
             javalb.setCondition(c);
         }
         firePropertyChange(PROP_CONDITION, old, condition);
     }
 
     /**
      * Returns condition.
      *
      * @return cond a condition
      */
     public String getCondition () {
         return condition;
     }
 
     /**
      * Gets value of print text property.
      *
      * @return value of print text property
      */
     public String getPrintText () {
         return printText;
     }
 
     /**
      * Sets value of print text property.
      *
      * @param printText a new value of print text property
      */
     public void setPrintText( String printText ) {
         if( this.printText == printText ) return;
         String old = this.printText;
         this.printText = printText;
         if( javalb != null ) {
             javalb.setPrintText( printText );
         }
         firePropertyChange( PROP_PRINT_TEXT, old, printText );
     }
 
     public void propertyChange(PropertyChangeEvent evt) {
         if( LineBreakpoint.PROP_URL.equals( evt.getPropertyName())) {
                 this.setURL((String) evt.getNewValue());
        } else if( LineBreakpoint.PROP_ENABLED.equals( evt.getPropertyName())) {
            if((Boolean) evt.getNewValue()) {
                enable();
            } else {
                disable();
            }
         }
     }
 }
