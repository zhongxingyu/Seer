 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.weather.provider.previmeteo;
 
 
 import org.gots.weather.WeatherCurrentCondition;
 import org.gots.weather.WeatherForecastCondition;
 import org.gots.weather.WeatherSet;
 import org.gots.weather.WeatherUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * SAXHandler capable of extracting information out of the xml-data returned by
  * the Google Weather API.
  */
 public class PrevimeteoWeatherHandler extends DefaultHandler {
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private WeatherSet myWeatherSet = null;
 	private boolean in_forecast_information = false;
 	private boolean in_current_conditions = false;
 	private boolean in_forecast_conditions = false;
 
 	private boolean usingSITemperature = true; // false means Fahrenheit
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	public WeatherSet getWeatherSet() {
 		return this.myWeatherSet;
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	@Override
 	public void startDocument() throws SAXException {
 		this.myWeatherSet = new WeatherSet();
 	}
 
 	@Override
 	public void endDocument() throws SAXException {
 		// Nothing
 	}
 
 	@Override
 	public void startElement(String namespaceURI, String localName,
 			String qName, Attributes atts) throws SAXException {
 		// 'Outer' Tags
 		if (localName.equals("forecast_information")) {
 			this.in_forecast_information = true;
 		} else if (localName.equals("current_conditions")) {
 			this.myWeatherSet
 					.setWeatherCurrentCondition(new WeatherCurrentCondition());
 			this.in_current_conditions = true;
 		} else if (localName.equals("forecast_conditions")) {
 			this.myWeatherSet.getWeatherForecastConditions().add(
 					new WeatherForecastCondition());
 			this.in_forecast_conditions = true;
 		} else {
 			String dataAttribute = atts.getValue("data");
 			// 'Inner' Tags of "<forecast_information>"
 			if (localName.equals("city")) {
 			} else if (localName.equals("postal_code")) {
 			} else if (localName.equals("latitude_e6")) {
 				/* One could use this to convert city-name to Lat/Long. */
 			} else if (localName.equals("longitude_e6")) {
 				/* One could use this to convert city-name to Lat/Long. */
 			} else if (localName.equals("forecast_date")) {
 			} else if (localName.equals("current_date_time")) {
 			} else if (localName.equals("unit_system")) {
 				if (dataAttribute.equals("SI"))
 					this.usingSITemperature = true;
 			}
 			// SHARED(!) 'Inner' Tags within "<current_conditions>" AND
 			// "<forecast_conditions>"
 			else if (localName.equals("day_of_week")) {
 				if (this.in_current_conditions) {
 //					this.myWeatherSet.getWeatherCurrentCondition()
 //							.setDayofYear(dataAttribute);
 				} else if (this.in_forecast_conditions) {
 //					this.myWeatherSet.getLastWeatherForecastCondition()
 //							.setDayofYear(dataAttribute);
 				}
 			} else if (localName.equals("icon")) {
 				if (this.in_current_conditions) {
 					this.myWeatherSet.getWeatherCurrentCondition().setIconURL(
 							dataAttribute);
 				} else if (this.in_forecast_conditions) {
 					this.myWeatherSet.getLastWeatherForecastCondition()
 							.setIconURL(dataAttribute);
 				}
 			} else if (localName.equals("condition")) {
 				if (this.in_current_conditions) {
 					this.myWeatherSet.getWeatherCurrentCondition()
 							.setCondition(dataAttribute);
 				} else if (this.in_forecast_conditions) {
 					this.myWeatherSet.getLastWeatherForecastCondition()
 							.setCondition(dataAttribute);
 				}
 			}
 			// 'Inner' Tags within "<current_conditions>"
 			else if (localName.equals("temp_f")) {
 				this.myWeatherSet.getWeatherCurrentCondition()
 						.setTempFahrenheit(Integer.parseInt(dataAttribute));
 			} else if (localName.equals("temp_c")) {
 				this.myWeatherSet.getWeatherCurrentCondition().setTempCelciusMin(
 						Integer.parseInt(dataAttribute));
 			} else if (localName.equals("humidity")) {
 				this.myWeatherSet.getWeatherCurrentCondition().setHumidity(
						new Integer(dataAttribute.substring(11,dataAttribute.indexOf('%')).trim()));
 			} else if (localName.equals("wind_condition")) {
 				this.myWeatherSet.getWeatherCurrentCondition()
 						.setWindCondition(dataAttribute);
 			}
 			// 'Inner' Tags within "<forecast_conditions>"
 			else if (localName.equals("low")) {
 				int temp = Integer.parseInt(dataAttribute);
 				if (this.usingSITemperature) {
 					this.myWeatherSet.getLastWeatherForecastCondition()
 							.setTempCelciusMin(temp);
 				} else {
 					this.myWeatherSet.getLastWeatherForecastCondition()
 							.setTempCelciusMin(
 									WeatherUtils.fahrenheitToCelsius(temp));
 				}
 			} else if (localName.equals("high")) {
 				int temp = Integer.parseInt(dataAttribute);
 				if (this.usingSITemperature) {
 					this.myWeatherSet.getLastWeatherForecastCondition()
 							.setTempCelciusMax(temp);
 				} else {
 					this.myWeatherSet.getLastWeatherForecastCondition()
 							.setTempCelciusMax(
 									WeatherUtils.fahrenheitToCelsius(temp));
 				}
 			}
 		}
 	}
 
 	@Override
 	public void endElement(String namespaceURI, String localName, String qName)
 			throws SAXException {
 		if (localName.equals("forecast_information")) {
 			this.in_forecast_information = false;
 		} else if (localName.equals("current_conditions")) {
 			this.in_current_conditions = false;
 		} else if (localName.equals("forecast_conditions")) {
 			this.in_forecast_conditions = false;
 		}
 	}
 
 	@Override
 	public void characters(char ch[], int start, int length) {
 		/*
 		 * Would be called on the following structure:
 		 * <element>characters</element>
 		 */
 	}
 }
