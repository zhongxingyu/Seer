 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.metadata.attributetype.constraint;
 
 import java.util.List;
 
 import org.cotrix.web.common.client.widgets.table.AbstractRow;
 import org.cotrix.web.manage.client.codelist.metadata.attributetype.constraint.ConstraintRow.Button;
 import org.cotrix.web.manage.client.codelist.metadata.attributetype.constraint.ConstraintRow.ConstraintRowListener;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources.PropertyGridStyle;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ConstraintArgumentsRow extends AbstractRow implements ConstraintRowListener {
 	
 	private static final PropertyGridStyle propertyGridStyles = CotrixManagerResources.INSTANCE.propertyGrid();
 
 	private static final int LABEL_COL = 0;
 	private static final int VALUE_COL = 1;
 	
 	private ConstraintArgumentsListener listener;
 	
 	private Label label;
 	
 	private ConstraintsArgumentsPanels argumentsPanels;
 	
 	private String errorStyle;
 	
 	public ConstraintArgumentsRow(String errorStyle) {
 		
 		CotrixManagerResources.INSTANCE.attributeRow().ensureInjected();
 		
 		this.errorStyle = errorStyle;
 		
 		label = new Label("");
 	
 		argumentsPanels = new ConstraintsArgumentsPanels();
 		argumentsPanels.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
 			
 			@Override
 			public void onValueChange(ValueChangeEvent<List<String>> event) {
 				fireValueChanged();
 			}
 		});
 	}
 
 	/**
 	 * @param listener the listener to set
 	 */
 	public void setListener(ConstraintArgumentsListener listener) {
 		this.listener = listener;
 	}
 
 	@Override
 	public void setup() {
 			
 		addCell(LABEL_COL, label);
 		setCellStyle(LABEL_COL, propertyGridStyles.header());
 		table.getCellFormatter().getElement(rowIndex, LABEL_COL).getStyle().setBackgroundColor("#efefef");
 
 		addCell(VALUE_COL, argumentsPanels);
 		setCellStyle(VALUE_COL, propertyGridStyles.value());
		table.getFlexCellFormatter().setColSpan(rowIndex, VALUE_COL, 3);
 	}
 	
 	public void setReadOnly(boolean readOnly) {
 		argumentsPanels.setReadOnly(readOnly);
 	}
 	
 	public int getRowIndex() {
 		Position position = getCellPosition(label);
 		return position.getRow();
 	}
 	
 	public List<String> getArgumentsValues() {
 		return argumentsPanels.getArgumentsValues();
 	}
 	
 	public void setArgumentsValues(List<String> arguments) {
 		argumentsPanels.setArgumentsValues(arguments);
 	}
 	
 	public void setConstraintName(String constraintName) {
 		argumentsPanels.showConstraintPanel(constraintName);
 	}
 	
 	private void fireValueChanged() {
 		if (listener!=null) listener.onValueChanged(this);
 	}
 	
 	public void setValid(boolean valid) {
 		argumentsPanels.setStyle(errorStyle, !valid);
 	}
 	
 	public interface ConstraintArgumentsListener {
 		public void onValueChanged(ConstraintArgumentsRow row);
 	}
 
 	@Override
 	public void onButtonClicked(ConstraintRow row,	Button button) {
 		
 	}
 
 	@Override
 	public void onValueChanged(ConstraintRow row) {
 		String constraintName = row.getConstraintName();
 		int argsCount = argumentsPanels.showConstraintPanel(constraintName);
 		setVisible(argsCount>0);
 	}
 	
 }
