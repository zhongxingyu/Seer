 package org.dawnsci.rcp.histogram;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawnsci.rcp.functions.ColourSchemeContribution;
 import org.dawnsci.rcp.functions.TransferFunctionContribution;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 
 public class ExtentionPointManager {
 	
 	private static final String TRANSFER_FUNCTION_ID = "org.dawnsci.rcp.histogram.channelColourScheme";
 	private static final String COLOUR_SCHEME_ID = "org.dawnsci.rcp.histogram.colourScheme";
 	private List<TransferFunctionContribution> transferFunctions;
 	private List<ColourSchemeContribution> colourSchemes;
 	
 	private static ExtentionPointManager staticManager;
 	public static ExtentionPointManager getManager() {
 		if (staticManager==null) staticManager = new ExtentionPointManager();
 		return staticManager;
 	}
 	private ExtentionPointManager() {
 		
 	}
 	
 	/**
	 * Get all the extensions for a particular ID
 	 * @param extensionPointId The ID which is referenced
 	 * @return
 	 */
 	private IExtension[] getExtensions(String extensionPointId) {
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
 		IExtension[] extensions = point.getExtensions();
 		return extensions;
 	}
 	
 	
 	/**
 	 * Get all the relevant transfer Function Contributions
 	 * @return
 	 */
 	public List<TransferFunctionContribution> getTransferFunctionContributions() {
 
 		if(transferFunctions != null) {
 			return transferFunctions;
 		}
 		
 		transferFunctions = new ArrayList<TransferFunctionContribution>();
 		
 		IExtension[] extensions = getExtensions(TRANSFER_FUNCTION_ID);
 
 		for(int i=0; i<extensions.length; i++) {
 
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configElements = extension.getConfigurationElements();	
 
 			for(int j=0; j<configElements.length; j++) {
 				IConfigurationElement config = configElements[j];
 				
 				transferFunctions.add(TransferFunctionContribution.getTransferFunctionContribution(config));
 			
 			}
 		}
 		
 		return transferFunctions;
 	}	
 	
 	/**
 	 * Get all the Colour Scheme Contributions
 	 * @return
 	 */
 	public List<ColourSchemeContribution> getColourSchemeContributions() {
 
 		if(colourSchemes != null) {
 			return colourSchemes;
 		}
 		
 		colourSchemes = new ArrayList<ColourSchemeContribution>();
 		
 		IExtension[] extensions = getExtensions(COLOUR_SCHEME_ID);
 
 		for(int i=0; i<extensions.length; i++) {
 
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configElements = extension.getConfigurationElements();	
 
 			for(int j=0; j<configElements.length; j++) {
 				IConfigurationElement config = configElements[j];
 				
 				colourSchemes.add(ColourSchemeContribution.getColourSchemeContribution(config));
 			
 			}
 		}
 		
 		return colourSchemes;
 	}
 
 	/**
 	 * Get a transfer function contribution by name
 	 * @param name the name of the Function
 	 * @return
 	 */
 	public TransferFunctionContribution getTransferFunction(String name) {
 		if (transferFunctions==null) getTransferFunctionContributions();
 		for (TransferFunctionContribution function : transferFunctions) {
 			if (function.getName().compareTo(name) == 0) {
 				return function;
 			}
 		}
 		throw new IllegalArgumentException("Could not find an appropriate Transfer Function");
 	}
 	/**
 	 * Get a transfer function contribution by name
 	 * @param name the name of the Function
 	 * @return
 	 */
 	public TransferFunctionContribution getTransferFunctionByID(String id) {
 		if (transferFunctions==null) getTransferFunctionContributions();
 		for (TransferFunctionContribution function : transferFunctions) {
 			if (function.getId().compareTo(id) == 0) {
 				return function;
 			}
 		}
 		throw new IllegalArgumentException("Could not find an appropriate Transfer Function");
 	}
 
 
 	/**
 	 * Get a colour scheme contribution by name
 	 * @param name the name of the colour scheme
 	 * @return
 	 */
 	public ColourSchemeContribution getColourSchemeContribution(String name) {
 		for (ColourSchemeContribution colourScheme : getColourSchemeContributions()) {
 			if(colourScheme.getName().compareTo(name) == 0) {
 				return colourScheme;
 			}
 		}
 		throw new IllegalArgumentException("Could not find an appropriate Colour Scheme");
 	}
 
 
 	/**
	 * Get a transfer function contribution by ID
 	 * @param ID the ID of the function
 	 * @return
 	 */
 	public TransferFunctionContribution getTransferFunctionFromID(String ID) {
 		for (TransferFunctionContribution function : transferFunctions) {
 			if (function.getId().compareTo(ID) == 0) {
 				return function;
 			}
 		}
 		throw new IllegalArgumentException("Could Not find an appropriate ID");
 	}	
 	
 		
 }
