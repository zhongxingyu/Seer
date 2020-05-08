 package org.eclipse.iee.sample.calendar.pad;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.iee.editor.core.pad.Pad;
 
 public class CalendarPad extends Pad {
 	
 	public CalendarPad() {
 		super();
 	}
 	
 	@Override
 	public void createPartControl(Composite parent)
 	{
 		parent.setLayout(new RowLayout());
 		@SuppressWarnings("unused")
 		DateTime calendar = new DateTime(parent, SWT.CALENDAR);
 	}
 
 	
 	protected CalendarPad(String containerID) {
 		super(containerID);
 	}
 	
 	
 	@Override
 	public Pad copy() {
 		// TODO Auto-generated method stub
 		return new CalendarPad();
 	}
 
 	@Override
 	public String getType() {
 		return "Calendar";
 	}
 
 	@Override
 	public void save() {
 		// TODO Auto-generated method stub
 	}
 
 }
 
