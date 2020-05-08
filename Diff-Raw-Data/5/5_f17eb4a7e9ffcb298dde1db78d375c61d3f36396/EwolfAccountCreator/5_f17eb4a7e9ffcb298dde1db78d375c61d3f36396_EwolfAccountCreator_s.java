 package il.technion.ewolf.ewolf;
 
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SFSFile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.SocialFSCreator;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 
 public class EwolfAccountCreator {
 
 	
 	private static final String DEFAULT_WOLFPACK = "wall-readers";
 	private final SocialFSCreator socialFSCreator;
 	private final SocialFS socialFS;
 	private final WolfPackLeader socialGroupsManager;
 	
 	@Inject @Named("ewolf.fs.social_groups.name") String socialGroupsFolderName;
 	
 	// state
 	private SFSFile rootFolder;
 	
 	@Inject
 	EwolfAccountCreator(
 			SocialFSCreator socialFSCreator,
 			SocialFS socialFS,
 			WolfPackLeader socialGroupsManager) {
 		
 		this.socialFSCreator = socialFSCreator;
 		this.socialFS = socialFS;
 		this.socialGroupsManager = socialGroupsManager;
 	}
 	
 	private void createSharedFolder() throws Exception {
 		SFSFile sharedFolder = socialFS.getSFSFileFactory()
 				.createNewFolder()
 				.setName("sharedFolder");
 		//TODO make separate group?
 		WolfPack sharedSocialGroup = socialGroupsManager
				.createSocialGroup(DEFAULT_WOLFPACK);
 		rootFolder.append(sharedFolder, sharedSocialGroup.getGroup());
 	}
 	
 	private void createWall() throws Exception {
 		WolfPack wallReadersSocialGroup = socialGroupsManager
			.createSocialGroup(DEFAULT_WOLFPACK);
 		
 		SFSFile wallFolder = socialFS.getSFSFileFactory()
 			.createNewFolder()
 			.setName("wall");
 			
 		rootFolder.append(wallFolder, wallReadersSocialGroup.getGroup());
 		
 		SFSFile wallPostsFolder = socialFS.getSFSFileFactory()
 				.createNewFolder()
 				.setName("posts");
 
 		wallFolder.append(wallPostsFolder, wallReadersSocialGroup.getGroup());
 	}
 	
 	public void create() throws Exception {
 		socialFSCreator.create();
 		
 		socialFS.login(socialFSCreator.getPassword());
 		
 		Profile myProfile = socialFS.getCredentials().getProfile();
 		rootFolder = myProfile.getRootFile();
 		
 		SFSFile socialGroupsFolder = socialFS.getSFSFileFactory()
 			.createNewFolder()
 			.setName(socialGroupsFolderName);
 		
 		rootFolder.append(socialGroupsFolder, socialFS.getStash().createGroup());
 		
 		createWall();
 		createSharedFolder();
 		
 		System.out.println(socialFS);
 	}
 	
 	
 }
