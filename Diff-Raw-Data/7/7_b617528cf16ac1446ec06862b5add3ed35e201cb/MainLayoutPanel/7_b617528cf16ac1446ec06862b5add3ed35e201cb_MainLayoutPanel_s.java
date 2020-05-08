 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.client.layout;
 
 import java.util.HashMap;
 
 import org.sopeco.webui.client.layout.center.EmptyCenterPanel;
 import org.sopeco.webui.client.layout.center.ICenterController;
 import org.sopeco.webui.client.layout.center.NoScenario;
 import org.sopeco.webui.client.layout.center.execute.ExecuteController;
 import org.sopeco.webui.client.layout.center.experiment.ExperimentController;
 import org.sopeco.webui.client.layout.center.result.ResultController;
 import org.sopeco.webui.client.layout.center.specification.SpecificationController;
 import org.sopeco.webui.client.layout.center.visualization.VisualizationController;
 import org.sopeco.webui.client.layout.navigation.NaviController;
 import org.sopeco.webui.client.layout.navigation.NaviItem;
 import org.sopeco.webui.client.manager.Manager;
 import org.sopeco.webui.client.manager.ScenarioManager;
 import org.sopeco.webui.shared.helper.Metering;
 
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * The main-layout of the web-application. From here you can reach all layout
  * objects.
  * 
  * @author Marius Oehler
  * 
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public final class MainLayoutPanel extends DockLayoutPanel {
 
 	/** Width of the navigation bar on the left. */
 	public static final int NAVI_WIDTH_EM = 15;
 
 	/** Singleton object of this class. */
 	private static MainLayoutPanel layoutPanel;
 
 	/**
 	 * Removes the {@link MainLayoutPanel} instance which is reachable through
 	 * {@link #get()}.
 	 */
 	public static void destroy() {
 		if (layoutPanel != null) {
 			layoutPanel = null;
 		}
 	}
 
 	/**
 	 * Returns an instance of the {@link MainLayoutPanel}. This instance is a
 	 * singleton object for the current session.
 	 * 
 	 * @return {@link MainLayoutPanel} instance
 	 */
 	public static MainLayoutPanel get() {
 		if (layoutPanel == null) {
 			layoutPanel = new MainLayoutPanel();
 		}
 		return layoutPanel;
 	}
 
 	private HashMap<Class<ICenterController>, ICenterController> controllerMap = new HashMap<Class<ICenterController>, ICenterController>();
 
 	private Class currentCenterClass;
 
 	private NaviController naviController;
 
 	private NorthPanel northPanel;
 
 	/**
 	 * Cosntructor that calls the {@link #initialize()} method.
 	 */
 	private MainLayoutPanel() {
 		super(Unit.EM);
 		initialize();
 	}
 
 	/**
 	 * Adds a new {@link NaviItem} to the left navigation bar, which changes the
 	 * view to the {@link ICenterController#getView()} of the given controller
 	 * by clicking on it.
 	 * 
 	 * @param controllerClass
 	 *            {@link ICenterController} instance which provides the view
 	 * @param text
 	 *            of the {@link NaviItem}
 	 * @return the created {@link NaviItem}
 	 */
 	public <T extends ICenterController> NaviItem addCenterController(Class<T> controllerClass, String text) {
 		return addCenterController(controllerClass, text, null);
 	}
 
 	/**
 	 * Adds a new {@link NaviItem} to the left navigation bar, which changes the
 	 * view to the {@link ICenterController#getView()} of the given controller
 	 * by clicking on it.
 	 * 
 	 * @param controllerClass
 	 *            {@link ICenterController} instance which provides the view
 	 * @param text
 	 *            of the {@link NaviItem}
 	 * @param subText
 	 *            subtitle of the {@link NaviItem}
 	 * @return the created {@link NaviItem}
 	 */
 	public <T extends ICenterController> NaviItem addCenterController(Class<T> controllerClass, String text,
 			String subText) {
 		return naviController.addItem(controllerClass, text, subText);
 	}
 
 	/**
 	 * Returns the existing {@link ICenterController} instance of the specified
 	 * class.
 	 * 
 	 * @param clazz
 	 *            of the desired {@link ICenterController}
 	 * @return {@link ICenterController} instance or <code>null</code> if no
 	 *         instance exists.
 	 */
 	public <T extends ICenterController> T getController(Class<T> clazz) {
 		for (ICenterController controller : controllerMap.values()) {
 			if (clazz.equals(controller.getClass())) {
 				return (T) controller;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the controller that is responsible for the left navigation bar.
 	 * 
 	 * @return instance of {@link NaviController}
 	 */
 	public NaviController getNaviController() {
 		return naviController;
 	}
 
 	/**
 	 * Returns the instance of the top navigation bar.
 	 * 
 	 * @return instance of the {@link NorthPanel}
 	 */
 	public NorthPanel getNorthPanel() {
 		if (northPanel == null) {
 			northPanel = new NorthPanel(this);
 		}
 		return northPanel;
 	}
 
 	/**
 	 * Removes the left navigation bar from the layout.
 	 */
 	public void hideNavigation() {
 		remove(naviController.getView());
 	}
 
 	/**
 	 * The left navigation bar will be refreshed. This is required if data of
 	 * specification or experiment (like name changed, experiments added..) were
 	 * edited.
 	 */
 	public void refreshNavigation() {
 		naviController.clear();
 
 		// Specification Item
 		String selectedSpecification = Manager.get().getCurrentScenarioDetails().getSelectedSpecification();
 		addCenterController(SpecificationController.class, "Specification", selectedSpecification);
 
 		// Experiments
 		naviController.addExperiments();
 
 		// Add Specification Item
 		naviController.addAddExpSeriesItem();
 
 		// Execution, Results and Visualization Item
 		addCenterController(ExecuteController.class, "Execute");
 		addCenterController(ResultController.class, "Result");
 		addCenterController(VisualizationController.class, "Visualization");
 
 		// Refresh "change-specification" popup
 		naviController.refreshSpecificationPopup();
 	}
 
 	/**
 	 * Calls on all registered {@link ICenterController} instances the
 	 * {@link ICenterController#reload()} method. After that, the navigation and
 	 * the main view will be refreshed.
 	 */
 	public void reloadPanels() {
 		double metering = Metering.start();
 
 		for (ICenterController controller : controllerMap.values()) {
 			controller.reload();
 		}
 		refreshNavigation();
 		refreshView();
 
 		Metering.stop(metering);
 	}
 
 	/**
 	 * Changes the view to the specification with the specified name.
 	 * 
 	 * @param specificationName
 	 *            of the specification
 	 */
 	public void setSpecification(String specificationName) {
 		refreshNavigation();
 		switchView(SpecificationController.class);
 		naviController.getItem(SpecificationController.class).get(0).setSubText(specificationName);
 		getController(SpecificationController.class).getView().setSpecificationName(specificationName);
 		naviController.getSpecificationPopup().setSelectedItem(specificationName);
 	}
 
 	/**
 	 * Adds the left navigation bar to the layout.
 	 */
 	public void showNavigation() {
 		Widget center = getCenter();
 		if (center != null) {
 			remove(center);
 
 		}
 		addWest(naviController.getView(), NAVI_WIDTH_EM);
 		getWidgetContainerElement(naviController.getView()).getStyle().setOverflow(Overflow.VISIBLE);
 
 		if (center != null) {
 			add(center);
 		}
 	}
 
 	/**
 	 * Causes the application to switch to the experiment that has the given
 	 * name. The view is changed, too.
 	 * 
 	 * @param experimentName
 	 *            of the next experiment
 	 */
 	public void switchToExperiment(String experimentName) {
 		ScenarioManager.get().experiment().setCurrentExperiment(experimentName);
 		switchView(ExperimentController.class);
 	}
 
 	/**
 	 * Switches the view to the widget of the existing instance of the given
 	 * {@link ICenterController} class.
 	 * 
 	 * @param targetClass
 	 *            of the instance which provides the next widget
 	 */
 	public <T extends ICenterController> void switchView(Class<T> targetClass) {
 		currentCenterClass = targetClass;
		controllerMap.get(targetClass).onSwitchTo();
 		refreshView();
 	}
 
 	/**
 	 * Initializes all necessary objects. This is where CenterCotnroller are
 	 * registered. This is necessary that the view can be changed on them by
 	 * calling {@link #switchView(Class)}.
 	 */
 	private void initialize() {
 		ScenarioManager.get().switchScenario(Manager.get().getAccountDetails().getSelectedScenario());
 		addNorth(getNorthPanel(), Float.parseFloat(NorthPanel.PANEL_HEIGHT));
 
 		naviController = new NaviController();
 		addWest(naviController.getView(), NAVI_WIDTH_EM);
 
 		registerCenterController(new SpecificationController());
 		registerCenterController(new ExperimentController());
 		registerCenterController(new ExecuteController());
 		registerCenterController(new ResultController());
 		registerCenterController(new VisualizationController());
 
 		refreshView();
 
		

 	}
 
 	/**
 	 * Refreshes the layout. The widget that is related to the
 	 * <code>currentCenterClass</code> attribute will be set as the center view.
 	 * Depending on the view, the navigation on the left hand is hidden or
 	 * visible.
 	 */
 	private void refreshView() {
 		if (getCenter() != null) {
 			remove(getCenter());
 		}
 
 		naviController.setSelectedItem(currentCenterClass);
 
 		if (Manager.get().getAccountDetails().getScenarioNames().length == 0) {
 
 			hideNavigation();
 			add(new NoScenario());
 
 		} else if (currentCenterClass == null) {
 
 			hideNavigation();
 			add(new EmptyCenterPanel());
 
 		} else {
 
 			showNavigation();
 			add(controllerMap.get(currentCenterClass).getView());
 
 		}
 	}
 
 	/**
 	 * The given {@link ICenterController} will be registered. After that the
 	 * view can be changed on his view which is accessible by
 	 * {@link ICenterController#getView()}.
 	 * 
 	 * @param controller
 	 *            {@link ICenterController} instance
 	 */
 	private void registerCenterController(ICenterController controller) {
 		Class clazz = controller.getClass();
 		if (controllerMap.containsKey(clazz)) {
 			throw new RuntimeException("ICenterController of class " + clazz.getName() + " already exists.");
 		}
 
 		controllerMap.put(clazz, controller);
 	}
 }
