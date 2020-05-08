 package pl.edu.agh.megamud.module;
 
 import java.sql.SQLException;
 
 import pl.edu.agh.megamud.GameServer;
 import pl.edu.agh.megamud.base.Controller;
 import pl.edu.agh.megamud.base.Creature;
 import pl.edu.agh.megamud.base.CyclicBehaviour;
 import pl.edu.agh.megamud.base.DatabaseModule;
 import pl.edu.agh.megamud.base.Location;
 import pl.edu.agh.megamud.base.SimpleItem;
 import pl.edu.agh.megamud.base.itemtype.Weapon;
 import pl.edu.agh.megamud.dao.Attribute;
 import pl.edu.agh.megamud.dao.LocationItem;
 import pl.edu.agh.megamud.dao.base.LocationBase;
 import pl.edu.agh.megamud.mechanix.CommandHit;
 import pl.edu.agh.megamud.mechanix.FightBehaviour;
 import pl.edu.agh.megamud.world.AggressiveSentry;
 import pl.edu.agh.megamud.world.CaveInitializer;
 import pl.edu.agh.megamud.world.Chochlik;
 import pl.edu.agh.megamud.world.CreatureFactory;
 import pl.edu.agh.megamud.world.Sentry;
 
 /**
  * Abstraction of a in-server module. A module loads locations, NPCs, new items
  * etc.
  * 
  * @author Tomasz
  */
 public class DefaultModule extends DatabaseModule {
 	public String getId() {
 		return "default";
 	}
 
 	public String getDescription() {
 		return "Default game module.";
 	}
 
 	private void prepareAttributes() throws SQLException {
 		// AttributeBase.createDao().deleteBuilder().delete();
 		Attribute.insertIfNotExists(Attribute.STRENGTH);
 		Attribute.insertIfNotExists(Attribute.DEXTERITY);
 		Attribute.insertIfNotExists(Attribute.DAMAGE);
 	}
 
 	private void clearLocations() throws SQLException {
 		LocationBase.createDao().deleteBuilder().delete();
 		LocationItem.createDao().deleteBuilder().delete();
 	}
 
 	protected void init() {
 		try {
 			clearLocations();
 			prepareAttributes();
 
 			CaveInitializer.init(this.getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		super.init();
 
 		// Commands
 
 		installCommands();
 
 		Weapon sword = new Weapon("sword", "little rusty sword");
 		sword.giveTo(GameServer.getInstance().getLocation(CaveInitializer.B2.getName()));
 		sword.initAtribute(Attribute.findByName(Attribute.DAMAGE));
 		sword.setAttribute(Attribute.DAMAGE, 3L);
 
 		new CyclicBehaviour(GameServer.getInstance().getLocation(
 				CaveInitializer.C7), 1000L) {
 			protected void action() {
 				Location location = (Location) owner;
 
 				if (location.getItems().containsKey("apple"))
 					return;
 
 				SimpleItem it = new SimpleItem("apple",
 						"Precious, golden apple.");
 				it.giveTo(location);
 			}
 		}.init();
 		
 		installNPC(
 				new Chochlik(),
 				new Creature("Chochlik")
 					.setLevel(100)
 					.setHp(666),
 				GameServer.getInstance().getLocation(CaveInitializer.B3));
 		
 		installNPC(
 				new Sentry(), 
 				CreatureFactory.getRat(),
 				GameServer.getInstance().getLocation(CaveInitializer.B2));
<<<<<<< Updated upstream
 		
 		installNPC(
 				new AggressiveSentry(), 
 				CreatureFactory.getRat(),
 				GameServer.getInstance().getLocation(CaveInitializer.D3));
 
 		installNPC(
 				new AggressiveSentry(), 
 				CreatureFactory.getRat(),
 				GameServer.getInstance().getLocation(CaveInitializer.E1));
 	
 		installNPC(
 				new AggressiveSentry(), 
 				CreatureFactory.getRat(),
 				GameServer.getInstance().getLocation(CaveInitializer.D5));
 		
 		installNPC(
 				new Sentry(), 
 				CreatureFactory.getRat(),
 				GameServer.getInstance().getLocation(CaveInitializer.C6));
 		
 		installNPC(
 				new Sentry(), 
 				CreatureFactory.getRat(),
 				GameServer.getInstance().getLocation(CaveInitializer.D1));		
=======
	
>>>>>>> Stashed changes
 	}
 
 	private void installCommands() {
 		installCommand(new CommandEquip());
 		installCommand(new CommandHit());
 		installCommand(new CommandUnequip());
 		installCommand(new CommandExit());
 		installCommand(new CommandGoto());
 		installCommand(new CommandHelp());
 		installCommand(new CommandInfo());
 		installCommand(new CommandLogin());
 		installCommand(new CommandLook());
 
 		installCommand(new CommandTake());
 		installCommand(new CommandGive());
 		installCommand(new CommandDrop());
 
 		installCommand(new CommandSay());
 
 		installCommand(new CommandKill());
 	}
 
 	public void onNewController(Controller c) {
 		findCommands("login").get(0).installTo(c);
 		findCommands("exit").get(0).installTo(c);
 		findCommands("help").get(0).installTo(c);
 
 		findCommands("kill").get(0).installTo(c);
 	}
 
 	public void onKillController(Controller c) {
 		findCommands("login").get(0).uninstallFrom(c);
 		findCommands("exit").get(0).uninstallFrom(c);
 		findCommands("help").get(0).uninstallFrom(c);
 
 		findCommands("kill").get(0).uninstallFrom(c);
 	}
 
 	public void onNewCreature(Creature c) {
 		Controller d = c.getController();
 
 		findCommands("login").get(0).uninstallFrom(d);
 		findCommands("info").get(0).installTo(d);
 		findCommands(CommandEquip.commandString).get(0).installTo(d);
 		findCommands(CommandUnequip.commandString).get(0).installTo(d);
 		findCommands(CommandHit.commandString).get(0).installTo(d);
 
 		findCommands("take").get(0).installTo(d);
 		findCommands("drop").get(0).installTo(d);
 		findCommands("give").get(0).installTo(d);
 
 		findCommands("look").get(0).installTo(d);
 		findCommands("goto").get(0).installTo(d);
 		findCommands("say").get(0).installTo(d);
 	}
 
 	public void onKillCreature(Creature c) {
 		Controller d = c.getController();
 		if (d == null)
 			return;
 		findCommands("info").get(0).uninstallFrom(d);
 		findCommands("login").get(0).installTo(d);
 
 		findCommands(CommandEquip.commandString).get(0).uninstallFrom(d);
 		findCommands(CommandUnequip.commandString).get(0).uninstallFrom(d);
 		findCommands(CommandHit.commandString).get(0).uninstallFrom(d);
 		findCommands("take").get(0).uninstallFrom(d);
 		findCommands("drop").get(0).uninstallFrom(d);
 		findCommands("give").get(0).uninstallFrom(d);
 
 		findCommands("look").get(0).uninstallFrom(d);
 		findCommands("goto").get(0).uninstallFrom(d);
 		findCommands("say").get(0).uninstallFrom(d);
 	}
 }
