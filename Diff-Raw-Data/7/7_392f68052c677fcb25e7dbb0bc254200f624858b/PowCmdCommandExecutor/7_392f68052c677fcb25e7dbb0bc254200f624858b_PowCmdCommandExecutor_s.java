 package bob_puyon.PowCmd;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PowCmdCommandExecutor implements CommandExecutor {
 	private PowCmd plg;
 
 	PowCmdCommandExecutor(PowCmd instance) {
 		this.plg = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 

		Player p = null;

		if( sender instanceof Player ){
			p = (Player)sender;
		}
 
 		if( !p.hasPermission("powcmd.exec") ){
 			//権限がない場合は応答なしで処理
 			p.sendMessage( ChatColor.RED + "You don't have Permissions!");
 			return true;
 		}
 
 		if( args[0].equalsIgnoreCase("help")){
 			p.sendMessage(  "------------ [ PowCmd Help ]------------");
 			p.sendMessage(  "Command : powcmd <Alias : pc>");
 			p.sendMessage(  ChatColor.GREEN + "pc console <command>");
 			p.sendMessage(  "    :コンソールにコマンドを実行させます");
 			p.sendMessage(  ChatColor.GREEN + "pc force [player] <command/chat>");
 			p.sendMessage(  "    :特定のプレイヤーにコマンドを実行させます");
 			p.sendMessage(  ChatColor.GREEN + "pc forceothers <command/chat>");
 			p.sendMessage(  "    :自分以外全員にコマンドを実行させます");
 			p.sendMessage(  ChatColor.GREEN + "pc forceall <command/chat>");
 			p.sendMessage(  "    :自分含め全員にコマンドを実行させます");
 			p.sendMessage(  "----------------------------------------");
 			return true;
 		}
 
 		String sendcmd = "";
 
 		if( args.length < 2 ){
 			p.sendMessage(  PowCmd.msgPrefix + "コマンドの指定が不足しています");
 			return true;
 		}
 
 		if( args[0].equalsIgnoreCase("console")){
 			// 送信したいコマンドを再結合
 			for( int i=0; i<args.length - 1; i++ ){	sendcmd += args[i+1] + " "; }
 			// コンソールからコマンド発行
 			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), sendcmd);
 
 		}else if( args[0].equalsIgnoreCase("force") ){
 			Player executer = Bukkit.getPlayer(args[1]);
 			if( executer == null ){
 				p.sendMessage(  PowCmd.msgPrefix + "指定したプレイヤーが見つかりません");
 				return true;
 			}
 			// 送信したいコマンドを再結合
 			for( int i=1; i<args.length - 1; i++ ){	sendcmd += args[i+1] + " "; }
 			//executer.performCommand(sendcmd);
 			executer.chat( sendcmd );
 
 		}else if( args[0].equalsIgnoreCase("forceothers") ){
 			// 送信したいコマンドを再結合
 			for( int i=0; i<args.length - 1; i++ ){	sendcmd += args[i+1] + " "; }
 			// 自分以外のプレイヤーに対して指定したコマンドを発行させる
 			for( Player executer : Bukkit.getOnlinePlayers() ){
 				if( p != executer ){
 					executer.chat( sendcmd );
 				}
 			}
 
 		}else if( args[0].equalsIgnoreCase("forceall") ){
 			// 送信したいコマンドを再結合
 			for( int i=0; i<args.length - 1; i++ ){	sendcmd += args[i+1] + " "; }
 			// 自分を含めたすべてのプレイヤーに対してコマンドを発行させる
 			for( Player executer : Bukkit.getOnlinePlayers() ){
 				executer.chat( sendcmd );
 			}
 		}
 		return true;
 	}
 }
 
 
