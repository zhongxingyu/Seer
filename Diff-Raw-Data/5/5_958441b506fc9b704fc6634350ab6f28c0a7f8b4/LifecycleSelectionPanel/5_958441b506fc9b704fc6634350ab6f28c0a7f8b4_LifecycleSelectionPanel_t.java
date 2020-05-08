 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.util;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.mule.galaxy.web.client.AbstractShowable;
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WPhase;
 
 public class LifecycleSelectionPanel extends AbstractShowable {
 
     private ListBox lifecyclesLB;
     private ListBox phasesLB;
     private Collection lifecycles;
     
     public LifecycleSelectionPanel(ErrorPanel menuPanel, RegistryServiceAsync svc) {
         super();
         
         FlowPanel panel = new FlowPanel();
        Label label = new Label("Lifecycle ");
         label.setStyleName("lifecycle-selection-header");
         panel.add(label);
         lifecyclesLB = new ListBox();
         //lifecyclesLB.setVisibleItemCount(4);
         
         phasesLB = new ListBox();
         //phasesLB.setVisibleItemCount(10);
         
         svc.getLifecycles(new AbstractCallback(menuPanel) {
             public void onSuccess(Object o) {
                 initLifecycles((Collection)o);
             }
         });
         
         lifecyclesLB.addChangeListener(new ChangeListener() {
 
             public void onChange(Widget w) {
                 selectLifecycle();
             }
             
         });
 
        label = new Label("Phases ");
         label.setStyleName("lifecycle-selection-header");
         
         panel.add(lifecyclesLB);
         panel.add(label);
         panel.add(phasesLB);
         
         initWidget(panel);
     }
 
     protected void selectLifecycle() {
         int idx = lifecyclesLB.getSelectedIndex();
         if (idx == -1) {
             Window.alert("No lifecycle selected");
             return;
         }
         
         String name = lifecyclesLB.getItemText(idx);
         
         WLifecycle l = getLifecycle(name);
         
         phasesLB.clear();
         
         phasesLB.addItem("All Phases", "_all");
         phasesLB.addItem("--", "_none");
         
         for (Iterator<WPhase> itr = l.getPhases().iterator(); itr.hasNext();) {
             WPhase phase = itr.next();
             
             phasesLB.addItem(phase.getName());
         }
     }
 
     private WLifecycle getLifecycle(String name) {
         for (Iterator itr = lifecycles.iterator(); itr.hasNext();) {
             WLifecycle l = (WLifecycle)itr.next();
 
             if (l.getName().equals(name)) {
                 return l;
             }
         }
         return null;
     }
 
     protected void initLifecycles(Collection o) {
         this.lifecycles = o;
         for (Iterator itr = o.iterator(); itr.hasNext();) {
             WLifecycle l = (WLifecycle)itr.next();
             
             lifecyclesLB.addItem(l.getName());
         }
         
         lifecyclesLB.setSelectedIndex(0);
         selectLifecycle();
     }
 
     public String getSelectedLifecycle() {
         int idx = lifecyclesLB.getSelectedIndex();
         if (idx == -1) {
             return null;
         }
         
         return lifecyclesLB.getValue(idx);
     }
 
     public String getSelectedPhase() {
         int idx = phasesLB.getSelectedIndex();
         if (idx == -1) {
             return null;
         }
         
         return phasesLB.getValue(idx);
     }
 
     public void addPhaseChangeListener(ChangeListener changeListener) {
         phasesLB.addChangeListener(changeListener);
     }
 
     public void setEnabled(boolean enabled) {
         phasesLB.setEnabled(enabled);
         lifecyclesLB.setEnabled(enabled);
     }
 
 }
