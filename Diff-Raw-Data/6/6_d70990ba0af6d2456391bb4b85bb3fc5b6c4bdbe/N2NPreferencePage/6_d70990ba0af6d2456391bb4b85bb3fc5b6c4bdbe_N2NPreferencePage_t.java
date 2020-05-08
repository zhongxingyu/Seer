 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.views.neighbours.views;
 
 import org.amanzi.awe.views.neighbours.NeighboursPlugin;
 import org.amanzi.awe.views.neighbours.PreferenceInitializer;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * <p>
  *Preference page for Node to Node relation view.
  * </p>
  * @author TsAr
  * @since 1.0.0
  */
 public class N2NPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
 
     @Override
     protected void createFieldEditors() {
         Composite attributePanel = getFieldEditorParent();
         final IntegerFieldEditor maxRow = new IntegerFieldEditor(PreferenceInitializer.N2N_MAX_SORTED_ROW, "Max rows in table for sorting", attributePanel);
         maxRow.setValidRange(0, Integer.MAX_VALUE);
         addField(maxRow);
 
         
     }
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(NeighboursPlugin.getDefault().getPreferenceStore());
     }
 
 
 
 }
