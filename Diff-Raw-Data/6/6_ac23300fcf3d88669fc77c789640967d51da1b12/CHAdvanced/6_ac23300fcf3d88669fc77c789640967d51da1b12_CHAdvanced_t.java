 package com.laytonsmith.chadvanced;
 
 import com.laytonsmith.PureUtilities.SimpleVersion;
 import com.laytonsmith.PureUtilities.Version;
 import com.laytonsmith.core.extensions.AbstractExtension;
 import com.laytonsmith.core.extensions.MSExtension;
 
@MSExtension("CHAdvanced")
 public class CHAdvanced extends AbstractExtension {
 
     public Version getVersion() {
 	return new SimpleVersion(1, 0, 0);
     }
 
     @Override
     public void onStartup() {
 	System.out.println("CHAdvanced loaded: "  + getVersion() + " - LadyCailin");
     }
 
     @Override
     public void onShutdown() {
 	System.out.println("CHAdvanced unloaded: "  + getVersion() + " - LadyCailin");
     }
 }
