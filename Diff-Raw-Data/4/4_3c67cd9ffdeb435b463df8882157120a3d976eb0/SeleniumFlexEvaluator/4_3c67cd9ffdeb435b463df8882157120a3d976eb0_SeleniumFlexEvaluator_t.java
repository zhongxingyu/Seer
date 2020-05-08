 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.testatoo.cartridge.flex3.evaluator.selenium;
 
 import com.thoughtworks.selenium.Selenium;
 import com.thoughtworks.selenium.SeleniumException;
 import com.thoughtworks.selenium.Wait;
 import org.testatoo.cartridge.flex3.FlexEvaluator;
 import org.testatoo.core.*;
 import org.testatoo.core.component.*;
 import org.testatoo.core.component.datagrid.*;
 import org.testatoo.core.input.Click;
 import org.testatoo.core.nature.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.testatoo.core.ComponentType.*;
 
 public class SeleniumFlexEvaluator extends AbstractEvaluator<Selenium> implements FlexEvaluator {
 
     private final Selenium selenium;
     private final String name;
     private String flexObjectIdentifier;
     private Component focusedComponent;
 
     private static final String ROW_ALIAS = "testatoo_row";
     private static final String COLUMN_ALIAS = "testatoo_column";
     private static final String CELL_ALIAS = "testatoo_cell";
 
     /**
      * Class constructor specifying the used selenium engine
      *
      * @param selenium the selenium engine
      */
     public SeleniumFlexEvaluator(String name, Selenium selenium) {
         this.name = name;
         this.selenium = selenium;
     }
 
     public SeleniumFlexEvaluator(Selenium selenium) {
         this(DEFAULT_NAME, selenium);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Selenium implementation() {
         return selenium;
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String name() {
         return name;
     }
 
     /**
      * @see org.testatoo.cartridge.flex3.FlexEvaluator
      */
     @Override
     public void open(String url) {
         selenium.open(url);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Boolean existComponent(String id) {
         if (id.equals("html"))
             return selenium.isElementPresent("xpath=/" + id);
 
         if (id.contains(ROW_ALIAS) || id.contains(COLUMN_ALIAS) || id.contains(CELL_ALIAS)) {
             return true;
         }
 
         if (id.equals(org.testatoo.cartridge.flex3.component.AlertBox.ID))
             return Boolean.valueOf(selenium.getEval("window.document['" + flexObjectId() + "'].existAlertBox();"));
 
         return Boolean.valueOf(selenium.getEval("window.document['" + flexObjectId()
                 + "'].existComponent('" + id + "');"));
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public ComponentType componentType(String id) {
         if (id.contains(ROW_ALIAS)) {
             return Row;
         }
 
         if (id.contains(COLUMN_ALIAS)) {
             return Column;
         }
 
         if (id.contains(CELL_ALIAS)) {
             return Cell;
         }
 
         return ComponentType.valueOf(selenium.getEval("window.document['" + flexObjectId()
                 + "'].componentType('" + id + "');"));
     }
 
     /**
      * @see org.testatoo.cartridge.flex3.FlexEvaluator
      */
     @Override
     public void workOn(final String flexObjectIdentifier) {
         new Wait() {
             public boolean until() {
                 try {
                     return Boolean.valueOf(selenium.getEval("window.document.embeds['" + flexObjectIdentifier + "'].ready();"));
                 } catch (Exception e) {
                     return false;
                 }
             }
         }.wait("flash component : " + flexObjectIdentifier + " not found");
         this.flexObjectIdentifier = flexObjectIdentifier;
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Boolean isVisible(Component component) {
         return Boolean.valueOf(selenium.getEval("window.document['" + flexObjectId()
                 + "'].isComponentVisible('" + component.id() + "');"));
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Boolean isEnabled(Component component) {
         return Boolean.valueOf(selenium.getEval("window.document['" + flexObjectId()
                 + "'].isComponentEnabled('" + component.id() + "');"));
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void click(Component component, Click which) {
         selenium.getEval("window.document['" + flexObjectId() + "'].clickOn('" + component.id() + "');");
         waitForCondition();
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String text(TextSupport textSupport) {
         // TODO need refactor on flex agent
         if (textSupport instanceof Button) {
             return selenium.getEval("window.document['" + flexObjectId() + "'].buttonText('" + ((Component) textSupport).id() + "');");
         }
 
         if (textSupport instanceof Link) {
             try {
                 return selenium.getEval("window.document['" + flexObjectId() + "'].label('" + ((Component) textSupport).id() + "');");
             } catch (SeleniumException e) {
                 return "";
             }
         }
 
         return "Not supported feature";
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String icon(IconSupport iconSupport) {
         // TODO need refactor on flex agent
         if (iconSupport instanceof Button) {
             String icon = selenium.getEval("window.document['" + flexObjectId() + "'].buttonIcon('" + ((Component) iconSupport).id() + "');");
             if (icon.equals("null"))
                 return "";
             return icon;
         }
         return "";
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void close(AbstractWindow window) {
         if (window.id().equals(org.testatoo.cartridge.flex3.component.AlertBox.ID))
             selenium.getEval("window.document['" + flexObjectId() + "'].closeAlertBox();");
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String title(TitleSupport titleSupport) {
         Component component = (Component) titleSupport;
         if (component instanceof AlertBox)
             return selenium.getEval("window.document['" + flexObjectId() + "'].alertBoxTitle('" + component.id() + "');");
 
        if (component instanceof Page) {
            return selenium.getTitle();
        }

         if (component instanceof Column) {
             String[] cmpId = component.id().split(":");
             return selenium.getEval("window.document['" + flexObjectId() + "'].dataGridColumnTitle('" + cmpId[1] + "', " + cmpId[2] + ");");
         }
 
         return selenium.getEval("window.document['" + flexObjectId() + "'].panelTitle('" + component.id() + "');");
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String message(AlertBox alertbox) {
         return selenium.getEval("window.document['" + flexObjectId() + "'].alertBoxMessage();");
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void check(Checkable checkable) {
         if (!checkable.isChecked())
             click((Component) checkable, Click.left);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Boolean isChecked(Checkable checkable) {
         return Boolean.valueOf(selenium.getEval("window.document['" + flexObjectId()
                 + "'].isChecked('" + ((Component) checkable).id() + "');"));
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void unCheck(CheckBox checkbox) {
         if (checkbox.isChecked())
             click(checkbox, Click.left);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String label(LabelSupport labelSupport) {
         try {
             return selenium.getEval("window.document['" + flexObjectId()
                     + "'].label('" + ((Component) labelSupport).id() + "');");
         } catch (SeleniumException e) {
             return "";
         }
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Selection<Column> columns(DataGrid datagrid) {
         String cmpId = datagrid.id();
 
         int numberOfColumns = Integer.valueOf(selenium.getEval("window.document['" + flexObjectId() + "'].dataGridColumnNumber('" + cmpId + "');"));
         List<Column> columns = new ArrayList<Column>();
 
         for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
             Column column = new Column(this, COLUMN_ALIAS + ":" + cmpId + ":" + columnIndex);
             columns.add(column);
         }
         return ListSelection.from(columns);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Selection<Row> rows(DataGrid datagrid) {
         String cmpId = datagrid.id();
 
         int numberOfRows = Integer.valueOf(selenium.getEval("window.document['" + flexObjectId() + "'].dataGridRowNumber('" + cmpId + "');"));
 
         List<Row> rows = new ArrayList<Row>();
         for (int rowNum = 0; rowNum < numberOfRows; rowNum++) {
             Row row = new Row(this, ROW_ALIAS + ":" + cmpId + ":" + rowNum);
             rows.add(row);
         }
         return ListSelection.from(rows);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Selection<Cell> cells(CellContainer cellContainer) {
         String[] cmpId = ((Component) cellContainer).id().split(":");
 
         List<Cell> cells = new ArrayList<Cell>();
 
         if (cmpId[0].contains(ROW_ALIAS)) {
             int numberOfColumns = Integer.valueOf(selenium.getEval("window.document['" + flexObjectId() + "'].dataGridColumnNumber('" + cmpId[1] + "');"));
             for (int columnNum = 0; columnNum < numberOfColumns; columnNum++) {
                 Cell cell = new Cell(this, CELL_ALIAS + ":" + cmpId[1] + ":" + cmpId[2] + ":" + columnNum);
                 cells.add(cell);
             }
         }
 
         if (cmpId[0].contains(COLUMN_ALIAS)) {
             int numberOfRows = Integer.valueOf(selenium.getEval("window.document['" + flexObjectId() + "'].dataGridRowNumber('" + cmpId[1] + "');"));
             for (int rowNum = 0; rowNum < numberOfRows; rowNum++) {
                 Cell cell = new Cell(this, CELL_ALIAS + ":" + cmpId[1] + ":" + rowNum + ":" + cmpId[2]);
                 cells.add(cell);
             }
         }
 
         return ListSelection.from(cells);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public String value(ValueSupport valueSupport) {
         Component component = (Component) valueSupport;
 
         if (component instanceof AbstractTextField) {
             return selenium.getEval("window.document['" + flexObjectId() + "'].textInputValue('" + component.id() + "');");
         }
 
         if (component instanceof Cell) {
             String[] cmpId = component.id().split(":");
 
             String field = selenium.getEval("window.document['" + flexObjectId() + "'].dataGridColumnDataField('" + cmpId[1] + "', " + cmpId[3] + ");");
             return selenium.getEval("window.document['" + flexObjectId() + "'].dataGridCellValue('" + cmpId[1] + "', " + cmpId[2] + ", '" + field + "');");
         }
         return "";
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Selection<String> selectedValues(ListModel listModel) {
         List<String> selectedValues = new ArrayList<String>();
 
         if (listModel instanceof DropDown)
             selectedValues.add(selenium.getEval("window.document['" + flexObjectId()
                     + "'].dropDownSelectedValue('" + listModel.id() + "');"));
         return ListSelection.from(selectedValues);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public Selection<String> values(ListModel listModel) {
         String field;
         String[] data = new String[0];
 
         if (listModel instanceof DropDown) {
             field = selenium.getEval("window.document['" + flexObjectId()
                     + "'].dropDownLabelField('" + listModel.id() + "');");
             data = selenium.getEval("window.document['" + flexObjectId() + "']" +
                     ".dropDownValues('" + listModel.id() + "', '" + field + "');").split(",");
         }
 
         List<String> values = new ArrayList<String>();
         values.addAll(Arrays.asList(data));
         return ListSelection.from(values);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void select(String value, ListModel listModel) {
         if (listModel instanceof DropDown)
             selenium.getEval("window.document['" + flexObjectId()
                     + "'].dropDownSelectValue('" + listModel.id() + "', '" + value + "');");
         waitForCondition();
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     public Integer maxLength(AbstractTextField textfield) {
         return Integer.valueOf(selenium.getEval("window.document['" + flexObjectId()
                 + "'].textInputMaxLength('" + textfield.id() + "');"));
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void type(String text) {
         type(text, focusedComponent);
     }
 
     /**
      * @see org.testatoo.core.Evaluator
      */
     @Override
     public void focusOn(Component component) {
         focusedComponent = component;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("SeleniumFlexEvaluator");
         sb.append("{location='").append(selenium.getLocation()).append('\'');
         sb.append('}');
         return sb.toString();
     }
 
     // -------------- Private ----------------------
 
     private String flexObjectId() {
         if (flexObjectIdentifier != null)
             return flexObjectIdentifier;
         throw new ComponentException("Unable to define flex object targeted. Set it with workOn('flexId')");
     }
 
     private void type(String value, Component component) {
         if (component instanceof AbstractTextField) {
             selenium.getEval("window.document['" + flexObjectId()
                     + "'].typeOnTextInput('" + component.id() + "', '" + value + "');");
         }
         waitForCondition();
     }
 
     private void waitForCondition() {
         new Wait() {
             public boolean until() {
                 return getWaitingCondition().isReach();
             }
         }.wait("One of the waiting conditions has fail", 60000);
     }
 
 //    @Override
 //    public String pageSource() {
 //        return selenium.getHtmlSource();
 //    }
 //
 //    @Override
 //    public String title(Page page) {
 //        return selenium.getTitle();
 //    }
 //
 //    @Override
 //    public String icon(Button button) {
 //        String icon = selenium.getEval("window.document['" + flexObjectId() + "'].buttonIcon('" + button.id() + "');");
 //        if (icon.equals("null"))
 //            return "";
 //        return icon;
 //    }
 //
 //    @Override
 //    public Boolean hasFocus(Component component) {
 //        return focusedComponent != null && focusedComponent.equals(component);
 //    }
 //
 //    @Override
 //    public Type flexComponentType(String id) {
 //        return Type.valueOf(selenium.getEval("window.document['" + flexObjectId()
 //                + "'].flexComponentType('" + id + "');"));
 //    }
 //
 //    public Boolean contains(Container container, Component component) {
 //        return Boolean.valueOf(selenium.getEval("window.document['" + flexObjectId() + "'].contain('"
 //                + ((Component) container).id() + "', '" + component.id() + "');"));
 //    }
 //
 //    public void unselect(String value, ListModel listModel) {
 //    }
 //
 //    public String source(Image image) {
 //        return null;
 //    }
 //
 //
 //    public String reference(Link link) {
 //        return null;
 //    }
 //
 //    public List<Button> buttons(DialogBox dialogbox) {
 //        return new ArrayList<Button>(0);
 //    }
 //
 //    public String message(DialogBox alertbox) {
 //        return null;
 //    }
 }
