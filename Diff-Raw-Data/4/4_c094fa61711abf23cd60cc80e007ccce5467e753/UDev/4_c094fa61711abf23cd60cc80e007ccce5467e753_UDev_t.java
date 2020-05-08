 package com.contentjon.hardware;
 
 import com.sun.jna.Library;
 import com.sun.jna.Pointer;
 
 public interface UDev extends Library {
 
     Pointer udev_new();
     void    udev_ref(Pointer udev);
     void    udev_unref(Pointer udev);
 
     String udev_get_sys_path(Pointer udev);
     String udev_get_dev_path(Pointer udev);
 
     Pointer udev_list_entry_get_next(Pointer current);
     String  udev_list_entry_get_name(Pointer entry);
     String  udev_list_entry_get_value(Pointer entry);
 
     Pointer udev_enumerate_new(Pointer udev);
     void    udev_enumerate_ref(Pointer enumeration);
     void    udev_enumerate_unref(Pointer enumeration);
     void    udev_enumerate_add_match_subsystem(Pointer enumeration, String subsystem);
     void    udev_enumerate_add_match_sysname(Pointer enumeration, String name);
     void    udev_enumerate_add_match_sysattr(Pointer enumeration, String attr, String value);
     void    udev_enumerate_add_match_property(Pointer enumeration, String attr, String value);
     void    udev_enumerate_scan_devices(Pointer enumeration);
     Pointer udev_enumerate_get_list_entry(Pointer enumeration);
 
     Pointer udev_device_new_from_syspath(Pointer udev, String name);
    void    udev_device_ref(Pointer udev);
    void    udev_device_unref(Pointer udev);
 
     String udev_device_get_subsystem(Pointer device);
     String udev_device_get_syspath(Pointer device);
     String udev_device_get_sysname(Pointer device);
     String udev_device_get_sysattr_value(Pointer device, String key);
     String udev_device_get_property_value(Pointer device, String key);
 
     Pointer udev_device_get_properties_list_entry(Pointer device);
 }
