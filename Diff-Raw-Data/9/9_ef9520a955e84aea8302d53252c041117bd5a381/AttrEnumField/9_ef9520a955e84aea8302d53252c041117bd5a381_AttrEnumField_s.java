 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.nlesc.vlet.gui.panels.fields;
 
import nl.esciencecenter.ptk.data.StringList;
 import nl.nlesc.vlet.data.VAttribute;
 import nl.nlesc.vlet.data.VAttributeType;
 
 public class AttrEnumField extends JStringComboBox implements IAttributeField
 {
     private static final long serialVersionUID = -2524144091178443352L;
    private StringList values;
     boolean enumEditable = false; // whether enum types are editable
 
     public AttrEnumField()
     {
        super();
         init();
     }
 
     public AttrEnumField(String name, String[] vals)
     {
         super(vals);
         setName(name);
     }
 
     private void init()
     {
         StringComboBoxModel model = new StringComboBoxModel();
         setModel(model);
     }
 
     public void setValues(String[] values)
     {
        this.values = new StringList(values);

         if (values == null)
             values = new String[0];
 
         this.setModel(new StringComboBoxModel(values));
     }
     
     public StringComboBoxModel getModel()
     {
         return (StringComboBoxModel)super.getModel(); 
     }
     
     public void addValue(String enumVal)
     {
         this.getModel().addElement(enumVal);
     }
 
     public void removeValue(String enumVal)
     {
         ((StringComboBoxModel) this.getModel()).removeElement(enumVal);
     }
 
     public void setValue(String txt)
     {
         this.getModel().setSelectedItem(txt);
     }
 
     public String getName()
     {
         return super.getName();
     }
 
     public String getValue()
     {
         Object obj = this.getSelectedItem();
         if (obj != null)
             return obj.toString();
         return null;
     }
 
     public void updateFrom(VAttribute attr)
     {
         this.setValue(attr.getStringValue());
     }
 
     // public void setEditable(boolean flag)
     // {
     // this.setEditable(flag);
     // }
 
     public VAttributeType getVAttributeType()
     {
         return VAttributeType.ENUM;
     }
 
     /**
      * Selectable => drop down option is 'selectable'. optionsEditable = drop
      * down selection entries are editable as well !
      */
     public void setEditable(boolean selectable, boolean optionsEditable)
     {
         this.setEnabled(selectable);
         this.setEditable(optionsEditable);
     }
 
 }
