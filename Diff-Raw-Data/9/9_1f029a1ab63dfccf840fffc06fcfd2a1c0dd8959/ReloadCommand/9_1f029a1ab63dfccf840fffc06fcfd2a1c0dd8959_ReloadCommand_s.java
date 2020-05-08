 /**
  * Likes - Package: syam.likes.command
  * Created: 2012/10/01 0:21:19
  */
 package syam.likes.command;
 
 import syam.likes.permission.Perms;
 import syam.likes.util.Actions;
 
 /**
  * ReloadCommand (ReloadCommand.java)
  * @author syam(syamn)
  */
 public class ReloadCommand extends BaseCommand {
 	public ReloadCommand(){
 		bePlayer = false;
 		name = "reload";
 		argLength = 0;
 		usage = "<- reload config.yml";
 	}
 
 	@Override
 	public void execute() {
 		try{
 			plugin.getConfigs().loadConfig(false);
 		}catch (Exception ex){
 			log.warning(logPrefix+"an error occured while trying to load the config file.");
 			ex.printStackTrace();
 			return;
 		}
 
 		// 権限管理プラグイン再設定
 		Perms.setupPermissionHandler();
 
 		Actions.message(sender, "&aConfiguration reloaded!");
 	}
 
 	@Override
 	public boolean permission() {
 		return Perms.RELOAD.has(sender);
 	}
 
 }
