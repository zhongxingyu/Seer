 package org.mashupmedia.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.mashupmedia.constants.MashUpMediaConstants;
 import org.mashupmedia.encode.FfMpegManager;
 import org.mashupmedia.model.Group;
 import org.mashupmedia.model.Role;
 import org.mashupmedia.model.User;
 import org.mashupmedia.model.media.MediaEncoding;
 import org.mashupmedia.util.AdminHelper;
 import org.mashupmedia.util.MediaItemHelper.MediaContentType;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class InitialisationManagerImpl implements InitialisationManager {
 	private Logger logger = Logger.getLogger(getClass());
 
 	@Autowired
 	private AdminManager adminManager;
 
 	@Autowired
 	private ConfigurationManager configurationManager;
 	
 	@Autowired
 	private VideoManager videoManager; 
 	
 	@Autowired
 	private FfMpegManager ffMpegManager;
 	
 	@Autowired
 	private MediaManager mediaManager;
 	
 	@Override
 	public void initialiseApplication() {
 		User user = adminManager.getUser(MashUpMediaConstants.ADMIN_USER_DEFAULT_USERNAME);
 		if (user != null) {
 			logger.info("Database has already been initialised. Exiting....");
 			return;
 		}
 		
 		initialiseUniqueInstallationName();
 		initialiseGroups();
 		initialiseFirstRoles();
 		adminManager.initialiseAdminUser();
 		adminManager.initialiseSystemUser();
 		initialiseEncoder();
 		videoManager.initialiseVideoResolutions();
 		initialiseMediaEncodings();
 	}
 
 
 	private void initialiseMediaEncodings() {
 		
 		// Audio encoding
 		mediaManager.saveMediaEncoding(createMediaEncoding(MediaContentType.MP3_ENCODED, 1));
 		mediaManager.saveMediaEncoding(createMediaEncoding(MediaContentType.OGA, 2));
		mediaManager.saveMediaEncoding(createMediaEncoding(MediaContentType.MP3_ORIGINAL, 3));		
 		
 		// Video encoding
 		mediaManager.saveMediaEncoding(createMediaEncoding(MediaContentType.MP4, 1));
 		mediaManager.saveMediaEncoding(createMediaEncoding(MediaContentType.WEBM, 2));
 	}
 	
 	private MediaEncoding createMediaEncoding(MediaContentType mediaContentType, int ranking) {
 		MediaEncoding mediaEncoding = new MediaEncoding();
		mediaEncoding.setMediaContentType(mediaContentType);
		mediaEncoding.setRanking(ranking);
 		return mediaEncoding;		
 	}
 
 
 	private void initialiseEncoder() {
 		File ffMpegFile = ffMpegManager.findFFMpegExecutable();
 		if (ffMpegFile == null) {
 			configurationManager.saveConfiguration(MashUpMediaConstants.IS_FFMPEG_INSTALLED,
 					Boolean.FALSE.toString());
 			return;
 		}
 
 		try {
 			boolean isValid = ffMpegManager.isValidFfMpeg(ffMpegFile);
 			if (isValid) {
 				configurationManager.saveConfiguration(MashUpMediaConstants.IS_FFMPEG_INSTALLED,
 						Boolean.TRUE.toString());
 				return;
 			}
 		} catch (IOException e) {
 			logger.info("Error validating ffmpeg", e);
 		}
 
 		configurationManager.saveConfiguration(MashUpMediaConstants.IS_FFMPEG_INSTALLED,
 				Boolean.FALSE.toString());
 	}
 
 	private void initialiseUniqueInstallationName() {
 
 		StringBuilder uniqueNameBuilder = new StringBuilder();
 		uniqueNameBuilder.append(System.getenv("os.arch"));
 		uniqueNameBuilder.append(System.getenv("os.name"));
 		uniqueNameBuilder.append(System.getenv("os.version"));
 		uniqueNameBuilder.append(System.getenv("user.country"));
 		uniqueNameBuilder.append(System.getenv("user.dir"));
 		uniqueNameBuilder.append(System.getenv("user.home"));
 		uniqueNameBuilder.append(System.getenv("user.name"));
 
 		Date date = new Date();
 		String uniqueInstallationName = String.valueOf(date.getTime())
 				+ String.valueOf(uniqueNameBuilder.toString().hashCode());
 		configurationManager.saveConfiguration(MashUpMediaConstants.UNIQUE_INSTALLATION_NAME, uniqueInstallationName);
 	}
 
 	protected void initialiseGroups() {
 		saveGroup("Friends");
 		saveGroup("Family");
 	}
 
 	protected void saveGroup(String name) {
 		Group group = new Group();
 		group.setName(name);
 		adminManager.saveGroup(group);
 
 	}
 
 //	protected void initialiseAdminUserAndRoles() {
 //		Set<Role> roles = initialiseFirstRoles();
 //		
 //		User administrator = adminManager.initialiseAdminUser();
 //		
 //		User user = new User();
 //		user.setName(DEFAULT_NAME);
 //		user.setUsername(DEFAULT_USERNAME);
 //		user.setPassword(DEFAULT_PASSWORD);
 //
 //		user.setEnabled(true);
 //		user.setEditable(false);
 //		user.setRoles(roles);
 //
 //		List<Group> groups = adminManager.getGroups();
 //		user.setGroups(new HashSet<Group>(groups));
 //		adminManager.saveUser(user);
 //		// adminManager.updatePassword(DEFAULT_USERNAME, DEFAULT_PASSWORD);
 //	}
 
 	protected void initialiseFirstRoles() {
 		saveRole(AdminHelper.ROLE_ADMIN_IDNAME, "Administrator", User.ROLE_ADMINISTRATOR);
 		saveRole(AdminHelper.ROLE_USER_IDNAME, "User", "ROLE_USER");
 	}
 
 	protected Role saveRole(String idName, String name, String authority) {
 		Role role = new Role();
 		role.setIdName(idName);
 		role.setName(name);
 		role.setAuthority(authority);
 		adminManager.saveRole(role);
 		return role;
 	}
 
 }
