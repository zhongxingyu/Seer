 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.metadata.attributedefinition.constraint;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.cotrix.web.common.client.util.FadeAnimation;
 import org.cotrix.web.common.client.util.FadeAnimation.Speed;
 import org.cotrix.web.common.client.widgets.table.AbstractRow;
 import org.cotrix.web.common.client.widgets.table.Table;
 import org.cotrix.web.common.shared.codelist.attributedefinition.UIConstraint;
 import org.cotrix.web.manage.client.codelist.metadata.attributedefinition.constraint.ConstraintArgumentsRow.ConstraintArgumentsListener;
 import org.cotrix.web.manage.client.codelist.metadata.attributedefinition.constraint.ConstraintRow.Button;
 import org.cotrix.web.manage.client.codelist.metadata.attributedefinition.constraint.ConstraintRow.ConstraintRowListener;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ConstraintsPanel implements HasValueChangeHandlers<Void> {
 	
 	private MetaConstraintProvider metaConstraintProvider = GWT.create(MetaConstraintProvider.class);
 	
 	private Table table;
 	
 	private FadeAnimation addRowAnimation;
 	private AddRow addRow;
 
 	private Map<ConstraintRow, UIConstraint> constraintsRows;
 	private Map<ConstraintArgumentsRow, UIConstraint> constraintsArgumentRows;
 	
 	private ConstraintRowListener rowListener;
 	private ConstraintArgumentsListener argumentsRowListener;
 	
 	private boolean readOnly = true;
 	
 	private String errorStyle;
 	
 	private boolean valid;
 	
 	private HandlerManager handlerManager;
 	
 	public ConstraintsPanel(Table table, String errorStyle) {
 		this.table = table;
 		this.errorStyle = errorStyle;
 		
 		addAddRow();
 		setAddRowReadOnly(readOnly);
 
 		rowListener = new ConstraintRowListener() {
 			
 			@Override
 			public void onValueChanged(ConstraintRow row) {
 				UIConstraint constraint = constraintsRows.get(row);
 				constraint.setName(row.getConstraintName());
 				fireValueChanged();
 			}
 			
 			@Override
 			public void onButtonClicked(ConstraintRow row, Button button) {
 				switch (button) {
 					case DELETE: removeRow(row); break;
 					case FULL_EDIT: break;
 				}
 			}
 		};
 		
 		argumentsRowListener = new ConstraintArgumentsListener() {
 			
 			@Override
 			public void onValueChanged(ConstraintArgumentsRow row) {
 				UIConstraint constraint = constraintsArgumentRows.get(row);
 				Log.trace("row: "+row);
 				Log.trace("constraint: "+constraint);
 				constraint.setParameters(row.getArgumentsValues());
 				fireValueChanged();
 			}
 		};
 		
 		constraintsRows = new HashMap<ConstraintRow, UIConstraint>();
 		constraintsArgumentRows = new HashMap<ConstraintArgumentsRow,UIConstraint>();
 		
 		handlerManager = new HandlerManager(this);
 	}
 	
 	private void addAddRow() {
 		addRow = new AddRow();
 		addRow.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				addEmptyConstraintRow();				
 			}
 		});
 		table.addRow(addRow);	
 		
 		addRowAnimation = new FadeAnimation(addRow.getElement());
 	}
 	
 	private void addEmptyConstraintRow() {
 		UIConstraint constraint = new UIConstraint();
 		if (metaConstraintProvider.getMetaConstraints().isEmpty()) throw new IllegalStateException("No constraints available");
 		constraint.setName(metaConstraintProvider.getMetaConstraints().get(0).getName());
 		addConstraint(constraint);		
 		fireValueChanged();
 	}
 	
 	private void removeRow(ConstraintRow row) {
 		ConstraintArgumentsRow argumentsRow = getArgumentsRow(constraintsRows.get(row));
 		
 		deattachRow(row);
 		deattachRow(argumentsRow);
 		
 		constraintsRows.remove(row);
 		constraintsArgumentRows.remove(argumentsRow);
 		
 		fireValueChanged();
 	}
 	
 	private ConstraintArgumentsRow getArgumentsRow(UIConstraint constraint) {
 		for (Entry<ConstraintArgumentsRow, UIConstraint> entry:constraintsArgumentRows.entrySet()) {
 			if (entry.getValue() == constraint) return entry.getKey();
 		}
 		throw new IllegalArgumentException("No arguments panel found for constraint "+constraint);
 	}
 	
 	private void deattachRow(ConstraintRow row) {
 		table.getFlexTable().removeRow(row.getRowIndex());
 		row.removeListener(null);
 	}
 	
 	private void deattachRow(ConstraintArgumentsRow row) {
 		table.getFlexTable().removeRow(row.getRowIndex());
 		row.setListener(null);
 	}
 
 	
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 		setAddRowReadOnly(readOnly);
 		
 		for (ConstraintRow row:constraintsRows.keySet()) row.setReadOnly(readOnly);
 		for (ConstraintArgumentsRow row:constraintsArgumentRows.keySet()) row.setReadOnly(readOnly);
 		
 		if (!readOnly) validate();
 	}
 	
 	private void setAddRowReadOnly(boolean readOnly) {
 		table.getFlexTable().getRowFormatter().setVisible(addRow.getRow(), !readOnly);
 		if (readOnly) addRowAnimation.setVisibility(false, Speed.IMMEDIATE);
 		else addRowAnimation.setVisibility(true, Speed.VERY_FAST);
 	}
 	
 	public void setConstraints(List<UIConstraint> constraints) {
 		removeAllRows();
 		for (UIConstraint constraint:constraints) addConstraint(constraint);
 		validate();
 	}
 	
 	private void removeAllRows() {
 		for (ConstraintRow row:constraintsRows.keySet()) deattachRow(row);
 		for (ConstraintArgumentsRow row:constraintsArgumentRows.keySet()) deattachRow(row);
 
 		constraintsRows.clear();
 		constraintsArgumentRows.clear();
		//fireValueChanged();
 	}
 	
 	private void addConstraint(UIConstraint constraint) {
 		ConstraintRow constraintRow = new ConstraintRow(errorStyle);
 		constraintRow.setConstraintName(constraint.getName());
 		
 		ConstraintArgumentsRow constraintArgumentsRow = new ConstraintArgumentsRow(errorStyle);
 		constraintArgumentsRow.setConstraintName(constraint.getName());
 		constraintArgumentsRow.setArgumentsValues(constraint.getParameters());
 		
 
 		addConstraintRow(constraintRow, constraintArgumentsRow);
 		
 		constraintsRows.put(constraintRow, constraint);
 		constraintsArgumentRows.put(constraintArgumentsRow, constraint);
 	}
 	
 	private void addConstraintRow(ConstraintRow constraintRow, ConstraintArgumentsRow argumentsRow) {
 		constraintRow.addListener(rowListener);
 		constraintRow.addListener(argumentsRow);
 		
 		argumentsRow.setConstraintName(constraintRow.getConstraintName());
 		argumentsRow.setListener(argumentsRowListener);
 		
 		table.insertRow(addRow.getRow(), constraintRow);
 		table.insertRow(addRow.getRow(), argumentsRow);
 		constraintRow.setReadOnly(readOnly);
 		argumentsRow.setReadOnly(readOnly);
 	}
 	
 	public List<UIConstraint> getConstraints() {
 		return new ArrayList<UIConstraint>(constraintsRows.values());
 	}
 	
 	private void fireValueChanged() {
 		validate();
 		ValueChangeEvent.fire(this, null);
 	}
 	
 	public void validate() {
 		valid = true;
 		for (Entry<ConstraintRow, UIConstraint> entry:constraintsRows.entrySet()) valid &= validate(entry.getKey(), entry.getValue());
 		for (Entry<ConstraintArgumentsRow, UIConstraint> entry:constraintsArgumentRows.entrySet()) valid &= validate(entry.getKey(), entry.getValue());
 	}
 	
 	private boolean validate(ConstraintRow row, UIConstraint constraint) {
 		//TODO
 		boolean valid = row.getConstraintName() != null && !row.getConstraintName().isEmpty();
 		row.setValid(valid);
 		return valid;
 	}
 	
 	private boolean validate(ConstraintArgumentsRow row, UIConstraint constraint) {
 		//TODO
 		boolean valid = row.getArgumentsValues() != null;
 		if (row.getArgumentsValues() != null) for (String arg:row.getArgumentsValues()) valid &= arg!=null && !arg.isEmpty();
 		row.setValid(valid);
 		return valid;
 	}
 
 	public boolean areValid() {
 		return valid;
 	}
 
 	@Override
 	public void fireEvent(GwtEvent<?> event) {
 		handlerManager.fireEvent(event);
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Void> handler) {
 		return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
 	}
 
 
 	private class AddRow extends AbstractRow implements HasClickHandlers {
 		
 		private FocusPanel clickPanel;
 		
 		public AddRow() {
 			
 			Label label = new Label("Add Constraint");
 			label.setStyleName(CotrixManagerResources.INSTANCE.css().addLabel());
 			
 			clickPanel = new FocusPanel(label);
 		}
 
 		@Override
 		public void setup() {
 			addCell(0, clickPanel);
 			table.getFlexCellFormatter().setColSpan(rowIndex, 0, 4);
 			table.getFlexCellFormatter().setStyleName(getRow(), 0, CotrixManagerResources.INSTANCE.css().addLabelCell());
 		}
 		
 		public int getRow() {
 			return getCellPosition(clickPanel).getRow();
 		}
 
 		@Override
 		public void fireEvent(GwtEvent<?> event) {			
 		}
 
 		@Override
 		public HandlerRegistration addClickHandler(ClickHandler handler) {
 			return clickPanel.addClickHandler(handler);
 		}
 		
 		public Element getElement() {
 			return clickPanel.getElement();
 		}
 	}
 }
