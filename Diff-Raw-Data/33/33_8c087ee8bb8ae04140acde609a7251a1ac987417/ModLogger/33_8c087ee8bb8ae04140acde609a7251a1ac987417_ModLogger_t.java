 package mods.alice.villagerblock.utility;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 
 public class ModLogger
 {
 	/** チャットメッセージの先頭文字列です。 */
 	static final String chatHeader = "[§0A§1l§2i§3c§4e§5T§6o§7y§8B§9o§ax§r]";
 	static Logger log;
 
 	static
 	{
		// getGlobal is not defined in JDK 1.6.
		//log = Logger.getGlobal();
		log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	}
 
 	/**
 	 * Loggerオブジェクトを登録します。ModLoggerはこれにログ出力を委任します。
 	 * @param _log 登録するLoggerのインスタンス
 	 */
 	public static void associateLogger(Logger _log)
 	{
 		synchronized(log)
 		{
 			log = _log;
 		}
 	}
 
 	static String addHeader(String format, Object ... args)
 	{
 		StringBuilder message;
 
 		message = new StringBuilder();
 		message.append(chatHeader);
 		message.append(' ');
 		message.append(String.format(format, args));
 
 		return message.toString();
 	}
 
 	public static void chatNotify(World world, String format, Object ... args)
 	{
 		String message;
 
 		if(world.isRemote)
 		{
 			return;
 		}
 
 		message = addHeader(format, args);
 		for(Object player : world.playerEntities)
 		{
 			if(player instanceof EntityPlayer)
 			{
 				((EntityPlayer)player).sendChatToPlayer(message);
 			}
 		}
 	}
 
 	public static void error(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.SEVERE, format, args);
 			}
 		}
 	}
 
 	public static void warning(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.WARNING, format, args);
 			}
 		}
 	}
 
 	public static void information(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.INFO, format, args);
 			}
 		}
 	}
 
 	public static void config(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.CONFIG, format, args);
 			}
 		}
 	}
 
 	public static void fine(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.FINE, format, args);
 			}
 		}
 	}
 
 	public static void finer(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.FINER, format, args);
 			}
 		}
 	}
 
 	public static void finest(String format, Object ... args)
 	{
 		synchronized(log)
 		{
 			if(log != null)
 			{
 				log.log(Level.FINEST, format, args);
 			}
 		}
 	}
 }
