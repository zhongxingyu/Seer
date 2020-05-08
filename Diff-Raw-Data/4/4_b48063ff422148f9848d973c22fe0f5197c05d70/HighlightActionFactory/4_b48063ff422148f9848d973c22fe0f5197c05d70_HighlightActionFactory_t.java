 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
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
 package org.netbeans.modules.javafx.fxd.composer.model.actions;
 
 import java.awt.AWTEvent;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.util.List;
 import javax.swing.Action;
 import org.netbeans.modules.javafx.fxd.composer.model.FXDElement;
 import org.netbeans.modules.javafx.fxd.composer.model.FXDElementOutline;
 import org.netbeans.modules.javafx.fxd.composer.model.FXDFileModel;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class HighlightActionFactory extends AbstractComposerActionFactory {
     
     private class HighlightAction extends AbstractComposerAction {
         private final FXDElement m_highlighted;
 
         public HighlightAction(FXDElement highlighted) {
             assert highlighted != null : "Null object for highlight"; //NOI18N
             assert highlighted.isVisible();
             m_highlighted = highlighted;
             getController().getScenePanel().setToolTipText(getTooltipText());
             m_highlighted.repaint(FXDElementOutline.SELECTOR_OVERLAP);
         }
 
         @Override
         public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
             assert getController().getActionController().containsAction(HighlightAction.class);
 
             if ( !isOutsideEvent) {
                 if ( evt.getID() == MouseEvent.MOUSE_MOVED) {
                     MouseEvent me = (MouseEvent) evt;
                     FXDElement elem = getController().getElementAt(me.getX(), me.getY());
                     if ( elem == null || elem.equals(m_highlighted))  {
                         actionCompleted();
                     }
                 } else if ( evt.getID() == MouseEvent.MOUSE_EXITED) {
                     actionCompleted();
                 }
 
             }
             return false;
         }
 
         public void paint(Graphics g) {
             if (!m_highlighted.isDeleted()) {
                 if ( !m_isCompleted && getDataModel().getIsHighlightOn()) {
                     m_highlighted.getOutline().highlight(g, 0, 0);
                 }
             } else {
                 actionCompleted();
             }
         }
 
         @Override
         public void actionCompleted() {
             super.actionCompleted();
             getController().getScenePanel().setToolTipText(""); //NOI18N
             m_highlighted.repaint(FXDElementOutline.SELECTOR_OVERLAP);
         }
 
         private String getTooltipText() {
             String text = "";  //NOI18N
             if (getDataModel().getIsTooltipOn()) {
                 final StringBuilder sb = new StringBuilder();
                     sb.append("<html>&nbsp;");  //NOI18N
                     sb.append( m_highlighted.getName());
                     sb.append( "&nbsp;{<br>"); //NOI18N
                 
                 m_highlighted.visitAttributes( new FXDFileModel.ElementAttrVisitor() {
                     public boolean visitAttribute(String attrName, String attrValue) {
                         sb.append("&nbsp;&nbsp;&nbsp;&nbsp;"); //NOI18N
                         sb.append( attrName);
                         sb.append( ": "); //NOI18N
                         sb.append( attrValue);
                         sb.append( "<br>"); //NOI18N
                         return true;
                     }
                 });
                 sb.append("}"); //NOI18N
                 sb.append("</html>"); //NOI18N
                 text = sb.toString();
             }
             return text;    
         }
     }
     
     public abstract class ExtendedAction extends AbstractFXDToggleAction {
         protected ExtendedAction( String id) {
             super(id);  
             refresh();
         }
         
         @Override
         public void actionPerformed(ActionEvent e) {
             setIsSelectedImpl( !isSelectedImpl());
             refresh();
         }
         
         public void refresh() {
             setIsSelected( isSelectedImpl());
         }
         
         protected abstract boolean isSelectedImpl();
         
         protected abstract boolean setIsSelectedImpl(boolean newState);
     };
     
     public final class ToggleHighlightAction  extends ExtendedAction {
         public ToggleHighlightAction() {
             super("toggle_highlight");  //NOI18N
         }
         protected boolean isSelectedImpl() {
             return getDataModel().getIsHighlightOn();
         }
 
         protected boolean setIsSelectedImpl(boolean newState) {
             boolean changed = getController().setIsHighlightOn(newState);
             return changed ? !newState : newState;
         }
     };    
         
     public final class ToggleTooltipAction extends ExtendedAction {
         public ToggleTooltipAction() {
             super("toggle_tooltip"); //NOI18N
         }
         protected boolean isSelectedImpl() {
             return getDataModel().getIsTooltipOn();
         }
 
         protected boolean setIsSelectedImpl(boolean newState) {
             boolean changed = getController().setIsTooltipOn(newState);
             return changed ? !newState : newState;
         }
     };    
     
     private ToggleHighlightAction  m_highlightAction = null;
     private ToggleTooltipAction    m_tooltipAction   = null;
         
     public HighlightActionFactory(FXZDataObject dObj) {
         super(dObj);
     }
         
     @Override
     public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
         if ( !isOutsideEvent && e.getID() == MouseEvent.MOUSE_MOVED) {
             if ( !getController().getActionController().containsAction(HighlightAction.class)) {
                 MouseEvent me = (MouseEvent)e;
                 FXDElement elem = getController().getElementAt(me.getX(), me.getY());
                 if ( elem != null)  {
                     return new HighlightAction( elem);
                 }
             }             
         }
         return null;
     }
 
     @Override
     public synchronized Action [] getMenuActions() {
         if ( m_highlightAction == null) {
             m_highlightAction = new ToggleHighlightAction();
             m_tooltipAction   = new ToggleTooltipAction();
         }
         return new Action [] { m_highlightAction, m_tooltipAction};
     }    
 }
 
 
