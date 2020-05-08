 package com.fornacif.osgi.manager.internal;
 
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.beans.property.SimpleLongProperty;
 import javafx.beans.property.SimpleStringProperty;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.startlevel.BundleStartLevel;
 
 public class BundleRow {
 
 	private final SimpleLongProperty id;
 	private final SimpleStringProperty state;
 	private final SimpleStringProperty name;
 	private final SimpleStringProperty version;
 	private final SimpleIntegerProperty startLevel;
 
 	public BundleRow(Bundle bundle) {
 		this.id = new SimpleLongProperty(bundle.getBundleId());
 		this.state = getState(bundle);
		this.name = new SimpleStringProperty(bundle.getHeaders().get(Constants.BUNDLE_NAME).toString());
 		this.version = new SimpleStringProperty(bundle.getVersion().toString());
 		this.startLevel = getStartLevel(bundle);
 	}
 
 	private SimpleStringProperty getState(Bundle bundle) {
 		switch (bundle.getState()) {
 		case Bundle.INSTALLED:
 			return new SimpleStringProperty("INSTALLED");
 		case Bundle.RESOLVED:
 			return new SimpleStringProperty("RESOLVED");
 		case Bundle.ACTIVE:
 			return new SimpleStringProperty("ACTIVE");
 		case Bundle.STARTING:
 			return new SimpleStringProperty("STARTING");
 		case Bundle.STOPPING:
 			return new SimpleStringProperty("STOPPING");
 		case Bundle.UNINSTALLED:
 			return new SimpleStringProperty("UNINSTALLED");
 		default:
 			return new SimpleStringProperty("UNKNOWN");
 		}
 	}
 	
 	private SimpleIntegerProperty getStartLevel(Bundle bundle) {
		BundleStartLevel bundleStartLevel = (BundleStartLevel) bundle.adapt(BundleStartLevel.class);
 		return new SimpleIntegerProperty(bundleStartLevel.getStartLevel());
 	}
 
 	public Long getId() {
 		return id.get();
 	}
 
 	public void setId(Long id) {
 		this.id.set(id);
 	}
 
 	public String getState() {
 		return state.get();
 	}
 
 	public void setState(String state) {
 		this.state.set(state);
 	}
 
 	public String getName() {
 		return name.get();
 	}
 
 	public void setName(String name) {
 		this.name.set(name);
 	}
 
 	public String getVersion() {
 		return version.get();
 	}
 
 	public void setVersion(String version) {
 		this.version.set(version);
 	}
 	
 	public Integer getStartLevel() {
 		return startLevel.get();
 	}
 
 	public void setStartLevel(Integer startLevel) {
 		this.startLevel.set(startLevel);
 	}
 
 }
