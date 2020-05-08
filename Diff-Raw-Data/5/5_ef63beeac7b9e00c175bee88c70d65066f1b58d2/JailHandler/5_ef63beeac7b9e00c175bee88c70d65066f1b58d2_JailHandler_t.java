 package no.runsafe.runsafejail.handlers;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.runsafejail.Jail;
 import org.bukkit.configuration.ConfigurationSection;
 
 import java.util.HashMap;
 import java.util.Set;
 
 public class JailHandler implements IConfigurationChanged
 {
	public JailHandler()
	{
		this.jails = new HashMap<String, Jail>();
	}

 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		ConfigurationSection configJails = configuration.getSection("jails");
 		Set<String> jails = configJails.getKeys(false);
 
 		for (String jail : jails)
 		{
 			ConfigurationSection jailInfo = configJails.getConfigurationSection(jail);
 			this.jails.put(jail, new Jail(
 					jailInfo.getDouble("x"),
 					jailInfo.getDouble("y"),
 					jailInfo.getDouble("z"),
 					jailInfo.getString("world")
 			));
 		}
 	}
 
 	public Jail getJailByName(String jailName)
 	{
 		return (this.jails.containsKey(jailName) ? this.jails.get(jailName) : null);
 	}
 
 	private HashMap<String, Jail> jails;
 }
