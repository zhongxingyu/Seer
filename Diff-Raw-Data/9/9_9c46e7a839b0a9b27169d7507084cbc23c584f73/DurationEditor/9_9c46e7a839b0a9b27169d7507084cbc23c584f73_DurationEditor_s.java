 package net.sf.redmine_mylyn.internal.ui.editor;
 
 import java.util.regex.Pattern;
 
 import net.sf.redmine_mylyn.internal.ui.Images;
 
 import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
 import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
 import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
 import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
 import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
 import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
 import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
 import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 
 public class DurationEditor extends AbstractAttributeEditor {
 
 	private final static Pattern VALIDATION_REGEX1 = Pattern.compile("^\\d*(?::(?:[0-5][0-9]?)?)?$"); 
 	
 	private final static Pattern VALIDATION_REGEX2 = Pattern.compile("^\\d*(?:\\.(?:\\d*)?)?$"); 
 	
 	Text text;
 	
 	private final TaskDataModelListener modelListener;
 	
 	public DurationEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
 		super(manager, taskAttribute);
 		setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
 		
 		modelListener = new TaskDataModelListener() {
 			@Override
 			public void attributeChanged(TaskDataModelEvent event) {
 				if(event.getTaskAttribute().getId().equals(getTaskAttribute().getId())) {
 					if(event.getTaskAttribute().getValue().isEmpty()) {
 						setValue(0,0);
 					} else {
 						setValue(Long.parseLong(event.getTaskAttribute().getValue()));
 					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public void createControl(Composite parent, FormToolkit toolkit) {
 		Control control = null;
 
 		Composite composite = toolkit.createComposite(parent);
 		GridLayout layout = new GridLayout(isReadOnly()?2:3, false);
 		layout.marginHeight = 3;
 		composite.setLayout(layout);
 
 		if (isReadOnly()) {
 			//Move into RedmineUtil
 			String val = "0:00";
 			if (!getTaskAttribute().getValue().isEmpty()) {
 				float hours = Float.parseFloat(getTaskAttribute().getValue());
 				int minutes = 60* ( ((int)(hours*100)) %100 ) /100;
 				val = String.format("%02d:%02d", (int)hours, minutes);
 			}
 			
 			control = toolkit.createText(composite, val, SWT.FLAT | SWT.READ_ONLY);
 			control.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
 		} else {
 			text = new Text(composite, SWT.FLAT);
 			if (!getTaskAttribute().getValue().isEmpty()) {
 				setValue(Long.parseLong(getTaskAttribute().getValue()));
 			}
 			
 			text.addVerifyListener(new VerifyListener() {
 				@Override
 				public void verifyText(VerifyEvent e) {
 					if (e.text.isEmpty()) {
 						return;
 					}
 					
 					String oldText = text.getText();
 					e.text = e.text.replace(',', '.');
 					
 					String newText;
 					
 					if(oldText.isEmpty() || e.start==0 && e.end==oldText.length()) {
 						newText = e.text;
 					} else if(e.start>=oldText.length()) {
 						newText = oldText + e.text;
 					} else {
 						newText = oldText.substring(0, e.start);
 						newText += e.text;
 						if (oldText.length()>=e.start) {
 							newText += oldText.substring(e.start);
 						}
 					}
 					
 					if (!VALIDATION_REGEX1.matcher(newText).matches() && !VALIDATION_REGEX2.matcher(newText).matches()) {
 						e.doit=false;
 					}
 				}
 			});
 			
 			text.addFocusListener(new FocusListener() {
 				@Override
 				public void focusLost(FocusEvent e) {
 					setValue(text.getText());
 				}
 				@Override
 				public void focusGained(FocusEvent e) {
 				};
 			});
 			
 			ImageHyperlink clearEstimated = toolkit.createImageHyperlink(composite, SWT.NONE);
 			clearEstimated.setImage(Images.getImage(Images.CLEAR));
 			clearEstimated.setToolTipText("Clear");
 			clearEstimated.addHyperlinkListener(new HyperlinkAdapter() {
 				@Override
 				public void linkActivated(HyperlinkEvent e) {
 					text.setText("");
 				}
 			});
 			
 			text.addDisposeListener(new DisposeListener() {
 				@Override
 				public void widgetDisposed(DisposeEvent e) {
 					getModel().removeModelListener(modelListener);
 				}
 			});
 			getModel().addModelListener(modelListener);
 
 			control = text;
 		}
 		
 		toolkit.paintBordersFor(composite);
 		toolkit.adapt(control, false, false);
 		setControl(control);
 
 		
 	}
 
 	public void setValue(String mixed) {
 		if (mixed!=null && !(mixed=mixed.trim()).isEmpty()) {
 			try {
 				String[] parts;
 				if((parts=mixed.split(":")).length==2) {
 					//1:30
 					setValue((parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0])), (parts[1].isEmpty() ? 0 : Integer.parseInt(parts[1])));
 					return;
 				} else if (mixed.matches("^(?:\\d*\\.)?\\d+$")) {
 					//.30 or 0.30 or 0
 					setValue(Float.parseFloat(mixed));
 					return;
 				}
 			} catch(NumberFormatException e) {
 				
 			}
 		}
 		setValue(0, 0);
 	}
 	
 	public void setValue(long milisec) {
 		int minutes = (int) Math.ceil(milisec/1000/60);
 		setValue(minutes/60, minutes%60);
 	}
 	
 	public void setValue(float hours) {
 		int minutes = 60* ( ((int)(hours*100)) %100 ) /100;
 		setValue((int)hours, minutes);
 	}
 	
 	private void setValue(int hours, int minutes) {
 		String newVal = String.format("%01d:%02d", hours, minutes);
 		
 		if(!newVal.equals(text.getText())) {
 			text.setText(newVal);
 		}
 
 		long milisec = (hours*60+minutes)*60000;
 		String newAttributeValue = milisec==0l ? "" : Long.toString(milisec);
 		if (!getTaskAttribute().getValue().equals(newAttributeValue)) {
 			getTaskAttribute().setValue(newAttributeValue);
 			attributeChanged();
 		}
 	}
 	
 }
