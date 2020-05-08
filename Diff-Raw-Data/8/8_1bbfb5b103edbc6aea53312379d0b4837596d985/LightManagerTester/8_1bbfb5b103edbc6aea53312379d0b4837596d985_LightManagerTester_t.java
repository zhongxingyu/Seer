 /**
  * Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  * DeviceManagerPanel.java - 3 juil. 2013
  */
 package fr.imag.adele.apam.test.lights.panel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.ApamResolver;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Link;
 import fr.imag.adele.apam.test.lights.devices.BinaryLight;
 import fr.imag.adele.apam.test.lights.devices.SimpleButton;
 import fr.imag.adele.apam.test.lights.devices.messages.LightStatusChanged;
 
 /**
  * @author thibaud
  * 
  */
 public class LightManagerTester implements ActionListener {
 
     Set<BinaryLight> theLights;
 
     Set<SimpleButton> theButtons;
 
     JFrame myFrame;
     JButton btn1;
     JButton btn2;
     JButton btn3;
     
     private int toShow; 
 
     private static Logger logger = LoggerFactory
 	    .getLogger(LightManagerTester.class);
 
     /**
      * 
      */
     public LightManagerTester() {
 	super();
     }
 
     public void started() {
 	logger.debug("started()");
 
 	myFrame = new JFrame();
 	myFrame.setLayout(new BoxLayout(myFrame.getContentPane(),
 		BoxLayout.Y_AXIS));
 	btn1 = new JButton("test 1: My Kitchen relations (default)");
 	btn2 = new JButton("test 2: press Button Kitchen");
 	btn3 = new JButton("test 3: press Button Living");
 	btn1.addActionListener(this);
 	btn2.addActionListener(this);
 	btn3.addActionListener(this);
 	myFrame.add(btn1);
 	myFrame.add(btn2);
 	myFrame.add(btn3);
 
 	rebuildLightsColumn();
 	rebuildButtonsColumn();
 	myFrame.pack();
 	if(toShow>0)
 	    myFrame.setVisible(true);
 
     }
     
     public void show() {
 	if(toShow>0)
 	    myFrame.setVisible(true);
 	else myFrame.setVisible(false);
 	  
 	
     }
 
     public void stopped() {
 	logger.debug("stopped()");
     }
 
     public void aLightStatusHasChanged(LightStatusChanged event) {
 	logger.debug("aLightStatusHasChanged(LightStatusChanged event, light  on : "
 		+ event.isLightOn() + ")");
 	rebuildLightsColumn();
     }
 
     /**
      * CallBack method on dependency resolution
      * 
      * @param button
      */
 
     public void newButton(Instance inst) {
 	SimpleButton btn = (SimpleButton) inst;
 	logger.debug("newButton(SimpleButton button : " + btn.getName()
 		+ " in " + btn.getLocation() + ")");
 	rebuildButtonsColumn();
     }
 
     /**
      * CallBack method on dependency resolution
      */
     public void removeButton(Instance inst) {
	SimpleButton btn = (SimpleButton) inst.getServiceObject();
 	logger.debug("removeButton(SimpleButton button : " + btn.getName()
 		+ " in " + btn.getLocation() + ")");
 	rebuildButtonsColumn();
     }
 
     /**
      * CallBack method on dependency resolution
      * 
      * @param light
      */
     public void newLight(Instance inst) {
	BinaryLight light = (BinaryLight) inst.getServiceObject();
 	logger.debug("newLight(Instance light : " + light.getName() + " in "
 		+ light.getLocation() + ")");
 	rebuildLightsColumn();
     }
 
     /**
      * CallBack method on dependency resolution
      */
     public void removeLight(Instance inst) {
	BinaryLight light = (BinaryLight) inst.getServiceObject();
 	logger.debug("removeLight(Instance light : " + light.getName() + " in "
 		+ light.getLocation() + ")");
 	rebuildLightsColumn();
     }
 
     private void rebuildButtonsColumn() {
 	logger.debug("rebuildButtonsColumn(), found " + theButtons.size()
 		+ " buttons to add to the list");
 
 	if (theButtons != null && theButtons.size() > 0) {
 	    Iterator<SimpleButton> it = theButtons.iterator();
 	    while (it.hasNext()) {
 		SimpleButton btn = it.next();
 		logger.debug("rebuildButtonsColumn() -> " + btn.getName()
 			+ " is in " + btn.getLocation());
 
 	    }
 	}
     }
 
     private void rebuildLightsColumn() {
 	logger.debug("rebuildLightsColumn(), found " + theLights.size()
 		+ " lights to add to the list");
 
 	if (theLights != null && theLights.size() > 0) {
 	    Iterator<BinaryLight> it = theLights.iterator();
 	    while (it.hasNext()) {
 		BinaryLight light = it.next();
 		logger.debug("rebuildLightsColumn() -> " + light.getName()
 			+ " is in " + light.getLocation());
 
 	    }
 	}
     }
 
     public void testPressButton(String location) {
 	logger.info("testPressButton(), Location button to test " + location);
 	if (theButtons != null && theButtons.size() > 0) {
 	    boolean found = false;
 	    Iterator<SimpleButton> it = theButtons.iterator();
 	    while (it.hasNext()&&!found) {
 		SimpleButton btn = it.next();
 		if (btn.getLocation().equals(location)) {
 		    logger.debug("testPressButton(), found button to test");
 		    found= true;
 		    btn.pressButton();
 		}
 	    }
 	}
     }
 
     public void testButtonKitchen() throws Exception {
 	shutDownLights();
 	try {
 	    Thread.sleep(200);
 	} catch (InterruptedException e) {
 	    logger.error("Test stopped");
 	    e.printStackTrace();
 	}
 	testPressButton("kitchen");
 	try {
 	    Thread.sleep(200);
 	} catch (InterruptedException e) {
 	    logger.error("Test stopped");
 	    e.printStackTrace();
 	}
 	String error="";
 	
 	for(BinaryLight light : theLights) {
 	    if(light.getLocation().equals("kitchen")) {
 		    if(!light.isLightOn()) {
 			error+=light.getName()+" in "+light.getLocation()+" is off (should be on);";
 		    }
 	    } else if(light.isLightOn())
 		error+=light.getName()+" in "+light.getLocation()+" is off (should be on);";
 	}
 	if(!error.equals("")) {
 	    logger.error("Light status incorrect : "+error);
 	    throw new Exception("Light status incorrect : "+error);
 	}
     }
 
     public void testButtonLiving() throws Exception{
 	shutDownLights();
 	try {
 	    Thread.sleep(200);
 	} catch (InterruptedException e) {
 	    logger.error("Test stopped");
 	    e.printStackTrace();
 	}
 	testPressButton("living");
 	try {
 	    Thread.sleep(200);
 	} catch (InterruptedException e) {
 	    logger.error("Test stopped");
 	    e.printStackTrace();
 	}
 	String error="";
 
 	for (BinaryLight light : theLights) {
 	    // no light should be on because there is now Lighting application
 	    // in the living
 	    if (light.isLightOn())
 		error+=light.getName()+" in "+light.getLocation()+" is on (should be off);";
 	}
 	if(!error.equals("")) {
 	    logger.error("Light status incorrect : "+error);
 	    throw new Exception("Light status incorrect : "+error);
 	}
     }
 
     private void shutDownLights() {
 	for (BinaryLight light : theLights) {
 	    light.setLightStatus(false);
 	}
     }
 
     @Deprecated
     public void testMyKitchenBinding() {
 	logger.info("testMyKitchenBinding()");
 	ApamResolver resolver = CST.apamResolver;
 	Instance lightApplication = resolver.findInstByName(null, "myKitchen");
 
 	Set<String> theoricLinks = new HashSet<String>();
 	theoricLinks.add("buttonKitchen");
 	theoricLinks.add("lightKitchen");
 
 	Set<Link> listRelations = lightApplication.getRawLinks();
 	Iterator<Link> it = listRelations.iterator();
 	while (it.hasNext()) {
 	    Link rel = it.next();
 	    logger.debug("testMyKitchenBinding(), link to "
 		    + rel.getDestination().getName());
 	    // Should find one binding with buttonKitchen and one binding with a
 	    // lightKitchen
 	    if (theoricLinks.contains(rel.getDestination().getName())) {
 		theoricLinks.remove(rel.getDestination().getName());
 	    } else
 		logger.error("testMyKitchenBinding() -> this link should not exists");
 	}
 	if (theoricLinks.size() > 0)
 	    logger.error("testMyKitchenBinding() -> not all links completed");
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
 	try {
 	if (e.getSource() == btn1)
 	    testMyKitchenBinding();
 	else if (e.getSource() == btn2)
 	    testButtonKitchen();
 	else if (e.getSource() == btn3)
 	    testButtonLiving();
 	} catch (Exception exc) {
 	    exc.printStackTrace();
 	}
     }
 
 }
