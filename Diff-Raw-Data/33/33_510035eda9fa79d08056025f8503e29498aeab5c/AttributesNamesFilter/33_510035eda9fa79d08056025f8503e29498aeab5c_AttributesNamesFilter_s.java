 /*
  * Copyright (c) 2013, Dienst Landelijk Gebied - Ministerie van Economische Zaken
  * 
  * Gepubliceerd onder de BSD 2-clause licentie, 
  * zie https://github.com/MinELenI/CBSviewer/blob/master/LICENSE.md voor de volledige licentie.
  */
 package nl.mineleni.cbsviewer.servlet.wms;
 
 import nl.mineleni.cbsviewer.util.AvailableLayersBean;
 
 /**
  * Verzorgt de filterconfiguratie van attribuut namen.
  * 
  * @author prinsmc
  * 
  */
 public class AttributesNamesFilter {
 	/**
 	 * lijst met beschikbare filters.
 	 */
	private transient final AvailableLayersBean layers = new AvailableLayersBean();
 
 	/**
 	 * Filtert de input.
 	 * 
 	 * @param attribute
 	 *            een te filteren waarde
 	 * @param layerID
 	 *            de identifier van de layer
 	 * 
 	 * @return de gefilterde input zoals in de filtermapping beschreven.
 	 */
 	public String filterValue(final String attribute, final String layerID) {
 		if (this.hasFilters(layerID)) {
 			final String[] aliases = this.layers.getLayerByID(layerID)
 					.getAliases().split(",\\s*");
 			final String[] attributes = this.layers.getLayerByID(layerID)
 					.getAttributes().split(",\\s*");
 
 			for (int i = 0; i < attributes.length; i++) {
 				if (attributes[i].equals(attribute)) {
 					return aliases[i];
 				}
 			}
 		}
 		return attribute;
 	}
 
 	/**
 	 * Geeft aan of het filter inhoud heeft.
 	 * 
 	 * @param layerID
 	 *            de identifier van de layer
 	 * 
 	 * @return {@code true} als het filter inhoud heeft
 	 */
 	private boolean hasFilters(final String layerID) {
 		return (null != this.layers.getLayerByID(layerID).getAliases());
 	}
 }
