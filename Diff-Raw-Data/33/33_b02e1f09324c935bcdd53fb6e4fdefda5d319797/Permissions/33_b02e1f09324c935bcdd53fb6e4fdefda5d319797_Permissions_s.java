 package de.autoit4you.bankaccount;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 
 public class Permissions {
 	
	
	private Permission permission;

	public boolean setupPermissions(){
		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
		if(permissionProvider != null)
			permission = permissionProvider.getProvider();
			return (permission != null);
	}
	
 	public boolean user(CommandSender sender, Permission perm){
 		return (sender.hasPermission(perm.node) || sender.isOp());
 	}
 
     public enum Permission {
         // /account user permissions
         ACCOUNT_HELP("bankaccount.user.help"), ACCOUNT_OPEN("bankaccount.user.open"), ACCOUNT_CLOSE("bankaccount.user.close"),
         ACCOUNT_LIST("bankaccount.user.list"), ACCOUNT_WITHDRAW("bankaccount.user.withdraw"), ACCOUNT_DEPOSIT("bankaccount.user.deposit"),
         ACCOUNT_BALANCE("bankaccount.user.balance"), ACCOUNT_TRANSFER("bankaccount.user.transfer"), ACCOUNT_TOP("bankaccount.user.top"),
         // /account admin permissions
         ACCOUNT_MANAGE_USER("bankaccount.user.manageuser"), ACCOUNT_MANAGE_ADMIN("bankaccount.user.manageadmin"), ACCOUNT_MANAGE_TRANSFER("bankaccount.user.transferownership"),
         ACCOUNT_MANAGE_PASSWORD("bankaccount.user.managepassword");
 
         private String node;
         private Permission(String permNode) {
             this.node = permNode;
         }
     }
 }
