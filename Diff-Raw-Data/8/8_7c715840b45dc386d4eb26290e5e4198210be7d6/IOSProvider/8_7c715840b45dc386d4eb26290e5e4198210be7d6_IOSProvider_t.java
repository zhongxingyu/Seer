 package com.turpgames.framework.impl.ios;
 
 import org.robovm.cocoatouch.foundation.NSBundle;
 import org.robovm.cocoatouch.foundation.NSDictionary;
 import org.robovm.cocoatouch.foundation.NSObject;
 import org.robovm.cocoatouch.foundation.NSString;
 
 import com.turpgames.framework.v0.IEnvironmentProvider;
 import com.turpgames.framework.v0.util.Version;
 
 public class IOSProvider implements IEnvironmentProvider {
 	private Version version;
 
 	@Override
 	public Version getVersion() {
 		if (version == null) {
 			try {
 				NSBundle mainBundle = NSBundle.getMainBundle();
 				if (mainBundle == null)
					throw new Exception("mainBundle is null");
 				
 				NSDictionary infoDictionary = mainBundle.getInfoDictionary();
 				if (infoDictionary == null)
					throw new Exception("infoDictionary is null");
 				
 				NSObject versionEntry = infoDictionary.get(new NSString("CFBundleShortVersionString"));
 				if (versionEntry == null)
					throw new Exception("versionEntry is null");
 				
 				String versionString = versionEntry.toString();				
 				version = new Version(versionString);
 			} catch (Throwable t) {
 				t.printStackTrace();
 				version = new Version("1.0");
 			}
 		}
 		return version;
 	}
 }
