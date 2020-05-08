 /*
  * Copyright 2012 Sebastien Zurfluh
  * 
  * This file is part of "Parcours".
  * 
  * "Parcours" is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * "Parcours" is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with "Parcours".  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.sebastienzurfluh.swissmuseum.parcours.client;
 
 
 import ch.sebastienzurfluh.swissmuseum.parcours.client.control.AppPresenter;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.ui.client.MGWTStyle;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class SwissMuseumParcours implements EntryPoint {
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
		// Load the styles in the right order. This method is used because MGWT keeps overwriting
		// my styles.
		MGWTStyle.getTheme().getMGWTClientBundle().getMainCss().ensureInjected();
		MGWTStyle.injectStyleSheet("SwissMuseumCore.css");
		MGWTStyle.injectStyleSheet("SwissMuseumParcours.css");
		
 		AppPresenter appPresenter = new AppPresenter(RootPanel.get());
 		appPresenter.start();
 	}
 }
