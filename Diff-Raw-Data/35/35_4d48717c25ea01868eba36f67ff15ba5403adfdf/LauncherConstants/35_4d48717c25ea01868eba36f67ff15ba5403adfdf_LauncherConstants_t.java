 package net.minecraft.launcher;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 public class LauncherConstants {
	public static final String VERSION_NAME = "1.2.3-15 (amd 2013)";
 	public static final int VERSION_NUMERIC = 15;
 	public static final URI URL_REGISTER = constantURI("http://tsino.unet.by/register.php");
 	public static final String URL_DOWNLOAD_BASE = "http://tsino.unet.by/minecraft/klient/";
 	// public static final String URL_DOWNLOAD_BASE =
 	// "http://pftbest.lan/launcher/";
 	public static final String URL_MODS_BASE = URL_DOWNLOAD_BASE + "mods/";
 	public static final String URL_RESOURCE_BASE = "https://s3.amazonaws.com/Minecraft.Resources/";
 	public static final String URL_BLOG = "http://tsino.unet.by/minecraft/motd.html";
 	public static final String URL_SUPPORT = "http://tsino.unet.by";
 	public static final int UNVERSIONED_BOOTSTRAP_VERSION = 0;
 	public static final int MINIMUM_BOOTSTRAP_SUPPORTED = 4;
 	public static final String URL_BOOTSTRAP_DOWNLOAD = "http://tsino.unet.by";
 	public static final String[] BOOTSTRAP_OUT_OF_DATE_BUTTONS = { "Go to URL",
 			"Close" };
 
 	public static final String[] CONFIRM_PROFILE_DELETION_OPTIONS = {
 			"Delete profile", "Cancel" };
 
 	public static final URI URL_FORGOT_USERNAME = constantURI("http://tsino.unet.by");
 	public static final URI URL_FORGOT_PASSWORD_MINECRAFT = constantURI("http://tsino.unet.by");
 	public static final URI URL_FORGOT_MIGRATED_EMAIL = constantURI("http://tsino.unet.by");
 	public static final int MAX_NATIVES_LIFE_IN_SECONDS = 3600;
 	public static final String DEFAULT_VERSION_INCOMPATIBILITY_REASON = "This version is incompatible with your computer. Please try another one by going into Edit Profile and selecting one through the dropdown. Sorry!";
 
 	public static URI constantURI(String input) {
 		try {
 			return new URI(input);
 		} catch (URISyntaxException e) {
 			throw new Error(e);
 		}
 	}
 }
 
 /*
  * Location: Z:\home\vadim\.minecraft\launcher.jar Qualified Name:
  * net.minecraft.launcher.LauncherConstants JD-Core Version: 0.6.2
  */
