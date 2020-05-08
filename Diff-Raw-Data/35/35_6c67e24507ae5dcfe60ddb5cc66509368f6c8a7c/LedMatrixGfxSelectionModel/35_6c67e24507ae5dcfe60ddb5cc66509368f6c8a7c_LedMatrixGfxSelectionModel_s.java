 package be.kuleuven.med.brainfuck.core;
 
 import java.util.Collections;
 import java.util.Set;
 
 import org.jdesktop.application.AbstractBean;
 
 import be.kuleuven.med.brainfuck.settings.LedSettings;
 
 import com.google.common.collect.Sets;
 
 public class LedMatrixGfxSelectionModel extends AbstractBean {
 
 	public static final String ROW_PIN = "rowPin";
 	
 	public static final String COLUMN_PIN = "columnPin";
 	
 	public static final String ROW_SELECTED = "rowSelected";
 	
 	public static final String COLUMN_SELECTED = "columnSelected";
 
 	private Set<LedSettings> selectedLedSettings = Sets.newHashSet();
 
 	private boolean rowSelected;
 	
 	private boolean columnSelected;
 
 	private int rowPin;
 
 	private int columnPin;
 
 	public boolean isSelected(LedSettings ledSettings) {
 		return selectedLedSettings.contains(ledSettings);
 	}
 	
 	public int getRowPin() {
 		return rowPin;
 	}
 
 	public void setRowPin(int rowPin) {
 		for(LedSettings ledSettings : selectedLedSettings) {
 			ledSettings.setRowPin(rowPin);
 		}
 		firePropertyChange(ROW_PIN, this.rowPin , this.rowPin = rowPin);
 	}
 
 	public int getColumnPin() {
 		return columnPin;
 	}
 
 	public void setColumnPin(int columnPin) {
 		for(LedSettings ledSettings : selectedLedSettings) {
 			ledSettings.setColumnPin(columnPin);
 		}
 		firePropertyChange(COLUMN_PIN, this.columnPin , this.columnPin = columnPin);
 		
 	}
 
 	public boolean isRowSelected() {
 		return rowSelected;
 	}
 
 	public void setRowSelected(boolean rowSelected) {
 		firePropertyChange(ROW_SELECTED, this.rowSelected, this.rowSelected = rowSelected);
 	}
 
 	public boolean isColumnSelected() {
 		return columnSelected;
 	}
 
 	public void setColumnSelected(boolean columnSelected) {
 		firePropertyChange(COLUMN_SELECTED, this.columnSelected, this.columnSelected = columnSelected);
 	}
 
 	public Set<LedSettings> getSelectedLedSettings() {
 		return Collections.unmodifiableSet(selectedLedSettings);
 	}
 
 	public void setSelectedLedSettings(Set<LedSettings> selectedLedSettings) {
 		this.selectedLedSettings = selectedLedSettings;
 	}
 	
 }
