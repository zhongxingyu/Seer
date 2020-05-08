 package org.thestaticvoid.iriverter;
 
 import java.io.*;
import java.util.*;
 
 public class Profile {
 	private File profileFile;
 
 	private Profile(File profileFile) {
 		this.profileFile = profileFile;
 	}
 
 	public static Profile getProfile(String profileName) {
 		return new Profile(new File(Config.getPackageDataDir() + "/profiles/" + profileName + ".profile"));
 	}
 
 	public static Profile[] getAllProfiles() {
 		String[] profilesStrings = new File(Config.getPackageDataDir() + "/profiles/").list();
 		Profile[] profiles = new Profile[profilesStrings.length];
 
 		for (int i = 0; i < profiles.length; i++)
 			profiles[i] = new Profile(new File(Config.getPackageDataDir() + "/profiles/" + profilesStrings[i]));
 
 		return profiles;
 	}
 
 	private String readOption(String option) {
 		String returnSetting = "";
 
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(profileFile));
 			String line;
 
 			while ((line = input.readLine()) != null)
 				if (line.indexOf(option + "=") > -1)
 					returnSetting = line.substring(line.indexOf("=") + 1);
 
 			input.close();
 		} catch (IOException e) {
 			// empty
 		}
 
 		return returnSetting;
 	}
 	
 	public String getProfileName() {
 		String profileFileName = profileFile.getName();
 		return profileFileName.substring(0, profileFileName.indexOf('.'));
 	}
 	
 	public String getDevice() {
 		return readOption("device");
 	}
 	
 	public int getMaxVideoBitrate() {
 		return Integer.parseInt(readOption("maxVideoBitrate"));
 	}
 	
 	public int getMaxAudioBitrate() {
 		return Integer.parseInt(readOption("maxAudioBitrate"));
 	}
 	
 	public Dimensions[] getDimensions() {
		StringTokenizer dimensionsTokens = new StringTokenizer(readOption("dimensions"));
		Dimensions[] dimensions = new Dimensions[dimensionsTokens.countTokens()];
 
 		for (int i = 0; i < dimensions.length; i++)
			dimensions[i] = new Dimensions(dimensionsTokens.nextToken());
 
 		return dimensions;
 	}
 	
 	public int getMaxFrameRate() {
 		return Integer.parseInt(readOption("maxFrameRate"));
 	}
 	
 	public int getMaxLength() {
 		try {
 			return Integer.parseInt(readOption("maxLength"));
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 }
