 package com.buglabs.bug.sysfs;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * A for properties associated with a BMI module attached to
  * BUG.  This class is designed to be subclassed for specific modules.
  * 
  * @author kgilmer
  * 
  */
 public class BMIDevice extends SysfsNode {
 	private String description;
 	private int gpio_usage;
 	private int power_use;
 	private int revision;
 	private int vendor;
 	private int bus_usage;
 	private int memory_size;
 	private int power_charging;
 	private String product_id;
 	private String serial_num;
 	private final int slot;
 	private int gpioUsage;
 	private int powerUse;
 	private int busUsage;
 	private int memorySize;
 	private int powerCharging;
 	private String serialNum;
 	
	public BMIDevice(File root, int slot) {
 		super(root);
 		this.slot = slot;
 		this.description = getFirstLineofFile(new File(root, "description"));
 		this.gpioUsage = parseInt(getFirstLineofFile(new File(root, "gpio_usage")));
 		this.powerUse = parseInt(getFirstLineofFile(new File(root, "power_use")));
 		this.revision = parseInt(getFirstLineofFile(new File(root, "revision")));
 		this.vendor = parseInt(getFirstLineofFile(new File(root, "vendor")));
 		this.busUsage = parseInt(getFirstLineofFile(new File(root, "bus_usage")));
 		this.memorySize = parseInt(getFirstLineofFile(new File(root, "memory_size")));
 		this.powerCharging = parseInt(getFirstLineofFile(new File(root, "power_charging")));
 		this.product_id = parseHexInt(getFirstLineofFile(new File(root, "product")));
 		this.serialNum = parseMultiInt(getFirstLineofFile(new File(root, "serial_num")));
 	}
 
 	/**
 	 * Create an instance of BMIModuleProperties class using the base BMI /sys
 	 * filesystem directory, for example /sys/devices/conn-m1.
 	 * 
 	 * @param directory
 	 * @param slot 
 	 * @return
 	 * @throws IOException if the root directory is not valid and readable.
 	 */
 	protected static BMIDevice createFromSYSDirectory(File directory, int slot) {
 		if (directory == null || !directory.exists() || !directory.isDirectory()) {
 			return null;
 		}
 		
 		String productId = parseHexInt(getFirstLineofFile(new File(directory, "product")));
 
 		if (productId.equals("bmi_camera")) {
 			return new CameraDevice(directory, slot);
 		} 
 		
 		if (productId.equals("bmi_video")) {
 			return new VideoOutDevice(directory, slot);
 		} 
 		
 		return new BMIDevice(directory, slot);
 	}
 	
 	public int getSlot() {
 		return slot;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public int getGpioUsage() {
 		return gpio_usage;
 	}
 
 	public int getPowerUse() {
 		return power_use;
 	}
 
 	public int getRevision() {
 		return revision;
 	}
 
 	public int getVendor() {
 		return vendor;
 	}
 
 	public int getBusUsage() {
 		return bus_usage;
 	}
 
 	public int getMemorySize() {
 		return memory_size;
 	}
 
 	public int getPowerCharging() {
 		return power_charging;
 	}
 
 	public String getProductId() {
 		return product_id;
 	}
 
 	public String getSerialNum() {
 		return serial_num;
 	}
 }
