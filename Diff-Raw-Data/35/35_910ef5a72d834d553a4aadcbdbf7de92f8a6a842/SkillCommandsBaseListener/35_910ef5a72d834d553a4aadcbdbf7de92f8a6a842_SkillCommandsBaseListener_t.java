 package skills.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 import core.skills.Skill;
 import core.skills.SkillPlayer;
 import core.skills.SkillPlayerManager;
 import core.skills.SkillType;
 
 public class SkillCommandsBaseListener implements CommandExecutor {
 
 	SkillPlayerManager	spm;
 
 	public SkillCommandsBaseListener(SkillPlayerManager spm) {
 		this.spm = spm;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
 		SkillPlayer sp;
 		PermissionUser user;
 		PermissionManager pex;
 
 		if (cmd.getName().equalsIgnoreCase("skills")) {
 
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("This command can only be run by a player.");
 			} else {
 
 				if (args.length > 0 && args.length < 3) {
 
 					sp = spm.getSkillPlayer(sender.getName());
 					user = PermissionsEx.getUser(sender.getName());
 					pex = PermissionsEx.getPermissionManager();
 
 					if (args[0].equalsIgnoreCase("ver") && args.length == 1) {
 
 						sender.sendMessage(ChatColor.RED + "-----------------------------");
 						sender.sendMessage(ChatColor.GOLD + "::::: SACRA RP || HABILIDADES :::::");
 						sender.sendMessage(ChatColor.RED + "-----------------------------");
 
 						for (Skill skill : spm.getSkillPlayer(sender.getName()).getSkills()) {
 							sender.sendMessage(ChatColor.BLUE + skill.getType().toString()+ " " + skill.getLevel() + "/" + skill.getMaxLevel() + "| Exp: " + skill.getExperience());
 						}
 						sender.sendMessage(ChatColor.RED + "-----------------------------");
 						sender.sendMessage(ChatColor.AQUA + "Tu nivel total: " + sp.getTotalLevel() + "/125");
 						sender.sendMessage(ChatColor.DARK_BLUE + "Experiencia necesaria para siguiente nivel: " + sp.getExpNextLevel());
 					} else {
 						if (args[0].equalsIgnoreCase("ver"))
 							sender.sendMessage(ChatColor.RED + "Escribe /skills ver");
 					}
 
 					if (args[0].equalsIgnoreCase("aprender") && args.length == 2) {
 
 						String skill = args[1];
 						skill.toLowerCase();
 
 						if (skill.contains("_")) {
 
 							skill = skill.split("_", 2)[0];
 
 						}
 
 						for (int i = 0; i < pex.getGroups().length; i++) {
 
 							if (pex.getGroups()[i].getName().equalsIgnoreCase(skill) == true) {
 
 								if (!user.inGroup(skill)) {
 
 									SkillType st = SkillType.getFromString(skill);
 
 									if (st != null) {
 
 										if (sp.getSkills().contains(st)) {
 											sender.sendMessage(ChatColor.RED + "Ya conoces esa habilidad");
 										} else {
 
 											user.addGroup(skill);
											Skill skillDb = spm.getDatabase().createEntityBean(Skill.class);
											skillDb.setType(st);
											skillDb.setPlayer(sp);
											skillDb.setMaxLevel(st.getLevel());
											skillDb.setLevel(st.getLevel() / 10);
											spm.getDatabase().save(skillDb);
											
											sp.addSkill(skillDb);
 
 											sender.sendMessage(ChatColor.LIGHT_PURPLE + "Decides aprender:  " + skill + " y empieza a nivel " + st.getLevel() / 10);
 											return true;
 
 										}
 									} else {
 										sender.sendMessage(ChatColor.RED + "Esa habilidad no existe.");
 									}
 
 								}
 							}
 						}
 						sender.sendMessage(ChatColor.RED + "Esa habilidad no existe o ya la ests aprendiendo.");
 						return true;
 
 					} else {
 						if (args[0].equalsIgnoreCase("aprender"))
 							sender.sendMessage(ChatColor.RED + "Escribe /skills aprender <nombre de habilidad>");
 
 						return true;
 					}
 
 				} else {
 					sender.sendMessage(ChatColor.RED + "No has puesto bien el comando: /skills <aprender>/<ver> <habilidad>");
 					return true;
 				}
 			}
 			return true;
 		}
 		return true;
 	}
 }
