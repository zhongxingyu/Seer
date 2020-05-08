 package de.uniluebeck.itm.icontrol.gui.view;
 
 /* ----------------------------------------------------------------------
 * This file is part of the WISEBED project.
 * Copyright (C) 2009 by the Institute of Telematics, University of Luebeck
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the BSD License. Refer to bsd-licence.txt
 * file in the root of the source tree for further details.
 ------------------------------------------------------------------------*/
 
 import java.util.LinkedList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 import de.uniluebeck.itm.icontrol.gui.controller.VController;
 import de.uniluebeck.itm.icontrol.gui.view.VBatteryDisplay;
 import de.uniluebeck.itm.icontrol.gui.view.VSensorDisplay;
 
 /**
  * This class realizes the graphical user interface.
  * @see VController
  * 
  * @author Johannes Kotzerke
  */
 public class Gui implements Listener, SelectionListener {
 	private VController controller;
 	private Composite container, features, linkStatus, parameters, sensors;
 	private Combo combo;
 	private CLabel name, text;
 	private Group controlGroup, featuresGroup, linkStatusGroup, parametersGroup, sensorGroup;
 	private LinkedList<Button> buttonList;
 	private LinkedList<Text> textList;
 	private LinkedList<VSensorDisplay> sensorDisplayList;
 	private Button button, searchButton, dialogButton;
 	private int selectedFeature = -1;
 	private VBatteryDisplay battery;
 	private VLinkStatusDisplay linkStatusDisplay;
 	private int time = 500;
 	private Runnable timer = null;
 	
 	public Gui(VController controller, Composite container){
 		this.controller = controller;
 		this.container = container;
 		
 		init();
 	}
 	
 	
 	/**
 	 * This method creates first a single button for searching. After clicking
 	 * a single centered message appears on the screen ,that tells you to
 	 * wait.
 	 */
 	private void init(){
 		container.setLayout(new GridLayout(3, false));
 	    searchButton = new Button(container, SWT.PUSH);
 	    searchButton.setText("Click for robot searching");
 	    searchButton.addListener(SWT.Selection, this);
 	    searchButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
 	}
 	
 	/**
 	 * It disposes the waiting message and creates more or less all needed SWT
 	 * thingies for displaying e.g. the robot's name.
 	 * NOTE: Should be called after <code>init()</code>, but before working
 	 * --> if the first robot is added to the dropdown menu.
 	 */
 	public void run(){
 		text.dispose();
 		container.setLayout(new GridLayout(1, false));
 		Composite top = new Composite(container, SWT.NONE);
 		top.setLayout(new RowLayout());
 		Composite center = new Composite(container, SWT.NONE);
 		GridLayout grid = new GridLayout(3, false);
 		grid.horizontalSpacing = 10;
 		center.setLayout(new RowLayout(SWT.HORIZONTAL));
 		Composite bottom = new Composite(container, SWT.NONE);
 		bottom.setLayout(new RowLayout());
 		bottom.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1));
 	    name = new CLabel(top, SWT.NONE);
 	    name.setFont(new Font(top.getDisplay(), new FontData("Sans Serif", 18, SWT.BOLD)));
 	    name.setForeground(new Color(null, 0x66, 0x66, 0x66));
 	    combo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    battery = new VBatteryDisplay(top);
 	    
 	    linkStatusGroup = new Group(center, SWT.SHADOW_ETCHED_IN);
 	    linkStatusGroup.setText("Link Status and Health");
 	    linkStatusGroup.setLayout(new RowLayout(SWT.VERTICAL));
 	    linkStatus = new Composite(linkStatusGroup, SWT.NONE);
 	    linkStatusDisplay = new VLinkStatusDisplay(linkStatus);
 	    
 	    sensorGroup = new Group(center, SWT.SHADOW_ETCHED_IN);    
 	    sensorGroup.setText("Sensors");
 	    sensorGroup.setLayout(new GridLayout(1, false));
 	    sensors = new Composite(sensorGroup, SWT.NONE);
 	    
 	    controlGroup = new Group(center, SWT.SHADOW_ETCHED_IN);
 	    controlGroup.setText("Control");
 	    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
 	    rowLayout.fill = true;
 	    controlGroup.setLayout(rowLayout);
 	    featuresGroup = new Group(controlGroup, SWT.SHADOW_ETCHED_IN);
 		featuresGroup.setText("Choose action:");
 		featuresGroup.setLayout(new GridLayout(1, false));
 		features = new Composite(featuresGroup, SWT.NONE);
 	    parametersGroup = new Group(controlGroup, SWT.SHADOW_ETCHED_IN);
 	    parametersGroup.setText("Enter parameters:");
 	    parametersGroup.setLayout(new GridLayout(1, false));
 	    parametersGroup.setToolTipText("All parameters are numeric values!");
 	    parameters = new Composite(parametersGroup, SWT.NONE);
 	    
 	    dialogButton = new Button(bottom, SWT.PUSH);
 	    dialogButton.setText("Change displayed sensors");
 	    dialogButton.addListener(SWT.Selection, this);
 	    
 	    button = new Button(bottom, SWT.PUSH);
 	    button.setText("Send Task!");
 	    button.addListener(SWT.Selection, this);
 	}
 	
 	/**
 	 * Starts the timer, that refreshes the sensor's devolution.
 	 */
 	protected void startSensorTimer() {
 		if (timer == null) {
 			timer = new Runnable() {
 		    	public void run() {
 		    		if (controller.getContainsBattery())
 		    			battery.update((int)(101 * Math.random()));
 //		    			battery.update(controller.getCurrentSensorValue("battery"));
 		    		if (!sensorDisplayList.isEmpty()) {
 			    		for (VSensorDisplay sensor : sensorDisplayList)
 			    			sensor.update();
 		    		}
 		    		container.getDisplay().timerExec(time, this);
 		    	}
 		    };
 		}
 	    container.getDisplay().timerExec(time, timer);
 	}
 	
 	/**
 	 * Sets the time period for sensor refreshing.
 	 * @param time refresh time in ms
 	 */
 	protected void setTime(int time) {
 		this.time = time;
 	}
 	
 	/**
 	 * Returns the time period of sensor refreshing
 	 * @return refresh time in ms
 	 */
 	protected int getTime() {
 		return time;
 	}
 	
 	/**
 	 * Stops the timer for sensor refreshing.
 	 */
 	private void stopSensorTimer() {
 		container.getDisplay().timerExec(-1, timer);
 	}
 	
 	/**
 	 * Updates the dropdown menu. If there wasn't any robot before registered,
 	 * the display switches to the new one, else that won't happen and the new
 	 * robot is only added to the dropdown menu.
 	 * NOTE: Should always be called if the controller found a new robot.  
 	 * @param robotIds
 	 * 					the ids of all currently known robots
 	 */
 	public void gotNewRobot(int[] robotIds){
 		for (int i = combo.getItemCount(); i < robotIds.length; i++){
 			combo.add("" + robotIds[i]);
 			combo.addListener(SWT.Selection, this);
 			linkStatusDisplay.addRobot(robotIds[i]);
 			//linkStatusDisplay.refreshLinkStatus(controller.getLinkStatus());
 		}
 		if (combo.getSelectionIndex() == -1){
 			combo.select(0);
 			updateRobot(true);
 		}
 		container.layout(true, true);
 	}
 	
 	/**
 	 * Changes all displayed thingies to the ones of the chosen robot.
 	 * @param firstTime
 	 * 			<code>true</code> if this method is called for the first time
 	 */
 	private void updateRobot(boolean firstTime) {
 		if (!firstTime)
 			stopSensorTimer();
 		int robotId = Integer.valueOf(combo.getItem(combo.getSelectionIndex()));
 		controller.setDisplayedRobot(robotId);
 		name.setText("iRobot: " + combo.getItem(combo.getSelectionIndex()));
 		updateButtonList(controller.getAllFeaturesNames());
 		updateDisplayedSensors();
 		startSensorTimer();
 	}
 	
 	/**
 	 * Updates the current displayed connection quality of the displayed robot to
 	 * all other robots.
 	 * 
 	 * @param linkStatus
 	 * 		contains the link quality of all robots to the displayed one in the
 	 * 		order, they are stored in the <code>robotList</code>.
 	 */
 	public void updateLinkStatus(int[] linkStatus) {
 		this.linkStatusDisplay.refreshLinkStatus(linkStatus);
 	}
 	
 	/**
 	 * Updates the current health of each robot. In this context health means
 	 * how long the last message to another robot is ago.
 	 * 
 	 * @param health
 	 * 			contains the health of each robot in the order, they are stored
 	 * 			in the <code>robotList</code>. The numeric values are the
 	 * 			following ones: 
 	 * 			2: very healthy (last message was received not long ago)
 	 * 			1: ok (last message was received a little bit ago)
 	 * 			0: ill (last message was received a long time ago)
 	 */		
 	public void updateHealthStatus(int[] health) {
 		this.linkStatusDisplay.refreshAllHealthIcons(health);
 	}
 	
 	/**
 	 * It updates the <code>buttonList</code> with the given feature names.
 	 * NOTE: Should be called if another robot was selected for displaying. 
 	 * 
 	 * @param features
 	 * 					the feature names of the displayed robot
 	 */
 	private void updateButtonList(String[] features){
 		if(!this.features.isDisposed())
 			this.features.dispose();
 		this.features = new Composite(featuresGroup, SWT.NONE);
 		this.features.setLayout(new RowLayout(SWT.VERTICAL));
 		buttonList = new LinkedList<Button>();
 		for (int i = 0; i < features.length; i++){
 			Button b = new Button(this.features, SWT.RADIO);
 			buttonList.add(b);
 			b.setText(features[i]);
 			b.addSelectionListener(this);
 			if (i == 0)
 				b.setSelection(true);
 		}
 		selectedFeature = -1;
 		updateParameters(controller.getFeatureParameters(0), 0);
 		this.container.layout(true, true);
 	}
 	
 	/**
 	 * Updates the displayed parameters to the ones of the current selected button.
 	 * NOTE: Should always be called if another button were selected.
 	 * 
 	 * @param parameters
 	 * 			the names of all parameters in a <code>String[]</code>
 	 * @param featureToSelect
 	 * 			the number of the feature in the robot's <code>featureList</code>
 	 * 			for checking if the new	selection is already selected
 	 */
 	private void updateParameters(String[] parameters, int featureToSelect){
 		if (selectedFeature == featureToSelect)
 			return;
 		if(!this.parameters.isDisposed())
 			this.parameters.dispose();
 		selectedFeature = featureToSelect;
 		this.parameters = new Composite(parametersGroup, SWT.NONE);
 		this.parameters.setLayout(new GridLayout(2, false));
		if (parameters.length == 0) {
			CLabel label = new CLabel(this.parameters, SWT.NONE);
			label.setText("There aren't any parameters!");
			parametersGroup.redraw();
			this.container.layout(true, true);
			textList = new LinkedList<Text>();
			return;
		}
 		textList = new LinkedList<Text>();
 		for (int i = 0; i < parameters.length; i++){
			Label l = new Label(this.parameters, SWT.LEFT);
			l.setText(parameters[i] + ":");
			Text t = new Text(this.parameters, SWT.BORDER);
			GridData grid = new GridData();
			grid.widthHint = 80;
			t.setLayoutData(grid);
//			t.setTextLimit(9);
			t.setSize(20, 12);
//			t.addListener(SWT.Verify, this);
			textList.add(t);
 		}
 		controlGroup.redraw();
 		this.container.layout(true, true);
 	}
 	
 	/**
 	 * Manages which sensors are currently displayed and which aren't.
 	 */
 	public void updateDisplayedSensors() {
 		String[] sensorNames2 = controller.getAllSensorNames();
 		for (int i = 0; i < sensorNames2.length; i++)
 			System.out.println("List " + i + " " + sensorNames2[i]);
 		if(!sensors.isDisposed())
 			sensors.dispose();
 		sensors = new Composite(sensorGroup, SWT.NONE);
 		sensors.setLayout(new GridLayout(3, false));
 		sensorDisplayList = new LinkedList<VSensorDisplay>();
 		boolean[] displayed = controller.getDisplayedSensors();
 		boolean anySensor = false;
 		for (int i = 0; i < displayed.length; i++) {
 			if (displayed[i])
 				anySensor = true;
 		}
 		if (anySensor)
 			sensorGroup.setVisible(true);
 		else
 			sensorGroup.setVisible(false);
 		String[] sensorNames = controller.getAllSensorNames();
 		for (int i = 0; i < displayed.length; i++){
 			if (displayed[i]){
 				int[] range = controller.getSensorsMinMax(sensorNames[i]);
 				sensorDisplayList.add(new VSensorDisplay(controller, sensors, sensorNames[i], range[0], range[1]));
 			}
 				
 		}
 		sensors.redraw();
 		container.layout(true, true);
 	}
 	
 	/**
 	 * Takes the by the user entered parameters and puts them into an <code>int[]</code>.
 	 * 
 	 * @return that <code>int[]</code>
 	 */
 	private int[] parseParameters(){
 		if (textList.isEmpty())
 			return new int[]{};
 		int[] parsingResult = new int[textList.size()];
 		System.out.println("textList.size: " + textList.size());
 		boolean anythingWrong = false;
 		for(int i = 0; i < textList.size(); i++){
 			int parse = 0;
 			try{
 				if (textList.get(i).getText().equals(""))
 					textList.get(i).setText("0");
 				parse = Integer.valueOf(textList.get(i).getText());
 				textList.get(i).setBackground(new Color(container.getDisplay(), 255, 255, 255));
 			}catch(NumberFormatException e){				
 				anythingWrong = true;
 				textList.get(i).setBackground(new Color(container.getDisplay(), 255, 84, 84));
 			}
 
 			parsingResult[i] = parse;
 			
 			System.out.println(parsingResult[i]);
 		}
 		if (!anythingWrong)
 			return parsingResult;
 		else
 			return null;
 	}
 
 	@Override
 	public void handleEvent(Event e) {
 //		System.out.println("handleEvent");
 //		if(e.type == SWT.Verify){
 //			e.doit = true;
 		if(e.type == SWT.Selection){
 			Object source = e.widget;
 			if (source.equals(button)){
 				if (parseParameters() != null){
 					controller.doTask(buttonList.get(selectedFeature).getText(), parseParameters());
 				}
 			} else if(source.equals(combo)){
 				String[] split = name.getText().split("[ ]");
 				if (Integer.valueOf(split[1]) == Integer.valueOf(combo.getItem(combo.getSelectionIndex())))
 					return;
 				updateRobot(false);
 			} else if(source.equals(dialogButton)){
 				VSensorDialog dialog = new VSensorDialog(container, this, controller);
 				stopSensorTimer();
 				dialog.open();
 				startSensorTimer();
 			} else if(source.equals(searchButton)){
 				searchButton.dispose();
 				text = new CLabel(container, SWT.CENTER);
 			    text.setText("Please wait, \ncollecting robot data!");
 			    text.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
 			    container.layout(true, true);
 			    controller.showMeWhatYouGot();
 			    
 				// Testeingaben, damit man was zu klicken hat
 			    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 //				controller.onAction(123, 3, new String[] { "drive", "gather", "spread" }, new int[] { 2, 3, 0 }, new String[][] {
 //						{ "direction", "distance" }, {"llllllllllllllllllllllllll", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaa"}, {} }, 3, new String[]{"battery", "left bumper", "right bumper"}, new int[]{0, 100, 0, 10, 0, 100});
 //				controller.onAction(124, 3, new String[] { "drive", "gather", "spread" }, new int[] { 2, 3, 0 }, new String[][] {
 //						{ "direction", "distance" }, {"llllllllllllllllllllllllll", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaa"}, {} }, 3, new String[]{"battery", "left bumper", "right bumper"}, new int[]{0, 100, 0, 1, 0, 1});
 				//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 			}
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		System.out.println("widgetDefaultSelected");
 		Object source = e.widget;
 		for (Button b: buttonList){
 			if (source.equals(b)){
 				System.out.println(buttonList.indexOf(b));
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		System.out.println("widgetSelected");
 		Object source = e.widget;
 		for (Button b: buttonList){
 			if (source.equals(b)){
 				System.out.println(buttonList.indexOf(b));
 				updateParameters(controller.getFeatureParameters(buttonList.indexOf(b)), buttonList.indexOf(b));
 				break;
 			}
 		}
 	}
 }
