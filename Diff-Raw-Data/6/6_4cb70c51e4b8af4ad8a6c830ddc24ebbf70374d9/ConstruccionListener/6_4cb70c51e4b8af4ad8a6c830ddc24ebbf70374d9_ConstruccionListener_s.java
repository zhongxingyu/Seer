 package skills.tala;
 
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.Plugin;
 
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 import core.skills.SkillPlayer;
 import core.skills.SkillPlayerManager;
 
 public class ConstruccionListener implements Listener {
 
 	SkillPlayerManager spm;
 	Logger log;
 	Plugin pl;
 
 	public ConstruccionListener(SkillPlayerManager spm, Logger log, Plugin pl) {
 
 		this.spm = spm;
 		this.log = log;
 		this.pl = pl;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void ConstruccionBlockPlaceEvent(BlockPlaceEvent event) {
 
		Block block = event.getBlockPlaced();
 		Player player = event.getPlayer();
 		PermissionUser user = PermissionsEx.getUser(player);
 		SkillPlayer sp;
 
 		if (user.inGroup("construccion")) {
 					
 			sp = spm.getSkillPlayer(player.getName());
 
			if (block.getTypeId() == 4) {
 
 				sp.addConstruccionExp(200);
 				player.sendMessage("Experiencia en construccion subio en 200 puntos");
 				player.sendMessage("Experiencia en construccion: "
 					+ sp.getConstruccionExp());
 				player.sendMessage("Nivel de construccion: " + sp.getConstruccionLvl());
 				player.sendMessage("Nivel total: " + sp.getTotalLevel());
 			}
 		}
 	}
 }
