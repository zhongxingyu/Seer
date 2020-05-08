 package ui.composites;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.ColorDialog;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 
 import shared.Customs;
 import shared.Message;
 import shared.RoomManager;
 import shared.SWTResourceManager;
 import ui.room.Room;
 import connection.Connection;
 import connection.Settings;
 
 public class ColorComposite extends Composite {
 
 	public ColorComposite(Composite parent, int style) {
 		super(parent, style);
 		setLayout(null);
 		
 		final Customs customs = new Customs();
 		List<Combo> messageColorCombos = new ArrayList<Combo>();
 		
 		Group grpOutputColor = new Group(this, SWT.NONE);
 		grpOutputColor.setText("Output Colors");
 		grpOutputColor.setBounds(10, 10, 186, 206);
 		
 		Label lblConsole = new Label(grpOutputColor, SWT.NONE);
 		lblConsole.setText("Console");
 		lblConsole.setBounds(10, 23, 55, 15);
 		
 		Combo comboConsole = new Combo(grpOutputColor, SWT.READ_ONLY);
 		comboConsole.setBounds(80, 23, 91, 23);
 		comboConsole.setData(Message.CONSOLE);
 		messageColorCombos.add(comboConsole);
 		
 		
 		Label lblMessage = new Label(grpOutputColor, SWT.NONE);
 		lblMessage.setText("Message");
 		lblMessage.setBounds(10, 52, 55, 15);
 		
 		Combo comboMessage = new Combo(grpOutputColor, SWT.READ_ONLY);
 		comboMessage.setBounds(80, 52, 91, 23);
 		comboMessage.setData(Message.MSG);
 		messageColorCombos.add(comboMessage);
 		
 		Label lblNotice = new Label(grpOutputColor, SWT.NONE);
 		lblNotice.setText("Notice");
 		lblNotice.setBounds(10, 81, 55, 15);
 		
 		Combo comboNotice = new Combo(grpOutputColor, SWT.READ_ONLY);
 		comboNotice.setBounds(80, 81, 91, 23);
 		comboNotice.setData(Message.NOTICE);
 		messageColorCombos.add(comboNotice);
 		
 		Label lblAction = new Label(grpOutputColor, SWT.NONE);
 		lblAction.setText("Action");
 		lblAction.setBounds(10, 110, 55, 15);
 		
 		Combo comboAction = new Combo(grpOutputColor, SWT.READ_ONLY);
 		comboAction.setBounds(80, 110, 91, 23);
 		comboAction.setData(Message.ACTION);
 		messageColorCombos.add(comboAction);
 		
 		Label lblPm = new Label(grpOutputColor, SWT.NONE);
 		lblPm.setText("PM");
 		lblPm.setBounds(10, 139, 55, 15);
 		
 		Combo comboPM = new Combo(grpOutputColor, SWT.READ_ONLY);
 		comboPM.setBounds(80, 139, 91, 23);
 		comboPM.setData(Message.PM);
 		messageColorCombos.add(comboPM);
 		
 		Label lblBackground = new Label(grpOutputColor, SWT.NONE);
 		lblBackground.setText("Background");
 		lblBackground.setBounds(10, 168, 64, 15);
 		
 		Combo comboBG = new Combo(grpOutputColor, SWT.READ_ONLY);
 		comboBG.setBounds(80, 168, 91, 23);
 		comboBG.setData(Settings.BACKGROUND);
 		messageColorCombos.add(comboBG);
 		
 		SelectionListener IRCColorListener = new SelectionListener(){
 
 			public void widgetSelected(SelectionEvent e) {
 				
 				Combo combo = (Combo)e.widget;
 				HashMap<Short,String> outputColors = Settings.getSettings().getOutputColors();
 				outputColors.put((Short)combo.getData(), combo.getText());
 				Settings.getSettings().setOutputColors(outputColors);
 				Settings.writeToFile();
 				
 				//check if all the rooms have to change foreground/background
 				if(combo.getData().equals(Message.MSG)||combo.getData().equals(Settings.BACKGROUND))
 				{
 					for(CTabItem i:RoomManager.getMain().getContainer().getItems())
 					{
 						if(i.getControl() instanceof Connection)
 						{
 							for(Room r:((Connection)i.getControl()).getRooms())
 							{
 								StyledText output = r.getOutput();
 								StyledText input = r.getInput();
 								
 								output.setForeground(customs.colors.get(Settings.getSettings().getOutputColors().get(Message.MSG)));
 								output.setBackground(customs.colors.get(Settings.getSettings().getOutputColors().get(Settings.BACKGROUND)));
 								input.setForeground(customs.colors.get(Settings.getSettings().getOutputColors().get(Message.MSG)));
 								input.setBackground(customs.colors.get(Settings.getSettings().getOutputColors().get(Settings.BACKGROUND)));
 							}
 						}
 					}
 				}
 			}
 			
 			public void widgetDefaultSelected(SelectionEvent e) {}
 		};
 		
 		PaintListener pl = new PaintListener(){
 			public void paintControl(PaintEvent e) {
 				int border = 4;
 				String colorStr = Settings.getSettings().getOutputColors().get(e.widget.getData());
 				e.gc.setBackground(customs.colors.get(colorStr));
 				e.gc.fillRectangle(border, border, e.width-2*border, e.height-2*border);
 			}
 		};
 		
 		for(Combo c:messageColorCombos)
 		{
 			c.add("white");
 			c.add("black");
 			c.add("dark blue");
 			c.add("dark green");
 			c.add("red");
 			c.add("brown");
 			c.add("purple");
 			c.add("olive");
 			c.add("yellow");
 			c.add("green");
 			c.add("teal");
 			c.add("cyan");
 			c.add("blue");
 			c.add("magenta");
 			c.add("dark gray");
 			c.add("light gray");
 			
 			String colorStr = Settings.getSettings().getOutputColors().get((Short)c.getData());
 			c.setText(colorStr);
 			c.addSelectionListener(IRCColorListener);
 			c.addPaintListener(pl);
 		}
 		
 		
 		List<Button> statusColorButtons = new ArrayList<Button>();
 		
 		Group grpRoomStatusColors = new Group(this, SWT.NONE);
 		grpRoomStatusColors.setText("Room Status Colors");
 		grpRoomStatusColors.setBounds(218, 10, 202, 177);
 		
 		Label lblNormal = new Label(grpRoomStatusColors, SWT.NONE);
 		lblNormal.setText("Normal");
 		lblNormal.setBounds(10, 23, 85, 15);
 		
 		Button btnNormal = new Button(grpRoomStatusColors, SWT.NONE);
 		btnNormal.setBounds(117, 18, 75, 25);
 		btnNormal.setData(Room.NORMAL);
 		statusColorButtons.add(btnNormal);
 		
 		Label lblNewIrcEvent = new Label(grpRoomStatusColors, SWT.NONE);
 		lblNewIrcEvent.setText("New IRC Event");
 		lblNewIrcEvent.setBounds(10, 56, 85, 15);
 		
 		Button btnIRCEvent = new Button(grpRoomStatusColors, SWT.NONE);
 		btnIRCEvent.setBounds(117, 51, 75, 25);
 		btnIRCEvent.setData(Room.NEW_IRC_EVENT);
 		statusColorButtons.add(btnIRCEvent);
 		
 		Label lblNewMessage = new Label(grpRoomStatusColors, SWT.NONE);
 		lblNewMessage.setText("New Message");
 		lblNewMessage.setBounds(10, 87, 85, 15);
 
 		Button btnNewMSG = new Button(grpRoomStatusColors, SWT.NONE);
 		btnNewMSG.setBounds(117, 82, 75, 25);
 		btnNewMSG.setData(Room.NEW_MESSAGE);
 		statusColorButtons.add(btnNewMSG);
 		
 		Label lblNameCalled = new Label(grpRoomStatusColors, SWT.NONE);
 		lblNameCalled.setText("Name Called");
 		lblNameCalled.setBounds(10, 118, 85, 15);
 
 		Button btnNameCall = new Button(grpRoomStatusColors, SWT.NONE);
 		btnNameCall.setBounds(117, 113, 75, 25);
 		btnNameCall.setData(Room.NAME_CALLED);
 		statusColorButtons.add(btnNameCall);
 		
 		PaintListener colorPainter = new PaintListener(){
 			public void paintControl(PaintEvent e) {
 				int border = 6;
 				RGB rgb = Settings.getSettings().getRoomStatusColors().get(e.widget.getData());
 				e.gc.setBackground(SWTResourceManager.getColor(rgb));
 				e.gc.fillRectangle(border, border, e.width-2*border, e.height-2*border);
 			}
 		};
 		
 		SelectionListener sl = new SelectionListener(){
 
 			public void widgetSelected(SelectionEvent e) {
 				/*Combo combo = (Combo)e.widget;
 				HashMap<Short,String> outputColors = Settings.getSettings().getOutputColors();
 				outputColors.put((Short)combo.getData(), customs.ircColorsStr.get(combo.getText()));
 				Settings.getSettings().setOutputColors(outputColors);
 				Settings.writeToFile();*/
 				
 				ColorDialog cd = new ColorDialog(RoomManager.getMain().getShell());
 				RGB rgb = cd.open();
 				Button btn = (Button)e.widget;
 				HashMap<Integer,RGB> statusColors = Settings.getSettings().getRoomStatusColors();
 				statusColors.put((Integer)btn.getData(), rgb);
 				Settings.getSettings().setRoomStatusColors(statusColors);
 				Settings.writeToFile();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 			
 		};
 		
 		for(Button btn:statusColorButtons)
 		{
 			btn.addPaintListener(colorPainter);
 			btn.addSelectionListener(sl);
 		}
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 	
 	//Put in a map and a value, and it will return the key if
 	//map.get(key).equals(value), else it will return null
 	private Object reverseLookup(Map<?,?> map, Object value)
 	{
 		if(!map.containsValue(value))
 			return null;
 		for(Object key:map.keySet())
 		{
 			if(map.get(key).equals(value))
 				return key;
 		}
 		return null;
 			
 	}
 }
