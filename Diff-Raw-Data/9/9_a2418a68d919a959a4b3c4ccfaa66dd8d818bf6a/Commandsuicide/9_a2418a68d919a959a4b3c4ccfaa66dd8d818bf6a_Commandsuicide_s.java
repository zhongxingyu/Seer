 package com.earth2me.essentials.commands;
 
 import static com.earth2me.essentials.I18n._;
 import com.earth2me.essentials.User;
 import org.bukkit.Server;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 
 public class Commandsuicide extends EssentialsCommand
 {
 	public Commandsuicide()
 	{
 		super("suicide");
 	}
 
 	@Override
 	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
 	{
 		EntityDamageEvent ede = new EntityDamageEvent(user, EntityDamageEvent.DamageCause.SUICIDE, 1000);
 		server.getPluginManager().callEvent(ede);
 		user.damage(1000);
 		user.sendMessage(_("suicideMessage"));
 		ess.broadcastMessage(user,_("suicideSuccess", user.getDisplayName()));		
 	}
 }
