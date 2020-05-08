 package syam.ThemeCreative.Command;
 
 import syam.ThemeCreative.Theme.Theme;
 import syam.ThemeCreative.Theme.ThemeManager;
 import syam.ThemeCreative.Util.Actions;
 
 public class CreateCommand extends BaseCommand{
 	public CreateCommand(){
 		bePlayer = false;
 		name = "create";
 		argLength = 2;
 		usage = "<name> <title> <- create new theme";
 	}
 
 	@Override
 	public boolean execute() {
 		Theme theme = plugin.getTheme(args.get(0));
 		if (theme != null){
 			Actions.message(sender, null, "&cそのテーマ名は既に存在します！");
 			return true;
 		}
 
		// 新規ゲーム登録
 		theme = new Theme(plugin, args.get(0), args.get(1));
 		ThemeManager.setSelectedTheme(player, theme);
 
		Actions.message(sender, null, "&a新規ゲーム'"+theme.getName()+"'を登録して選択しました！");
 		return true;
 	}
 
 	@Override
 	public boolean permission() {
		return sender.hasPermission("flag.admin.theme");
 	}
 }
