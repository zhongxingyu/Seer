 package com.lenis0012.bukkit.btm.nms;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.lenis0012.bukkit.btm.BeTheMob;
 import com.lenis0012.bukkit.btm.api.Disguise;
 import com.lenis0012.bukkit.btm.events.PlayerInteractDisguisedEvent;
 import com.lenis0012.bukkit.btm.nms.wrappers.EntityPlayer;
 import com.lenis0012.bukkit.btm.nms.wrappers.MinecraftServer;
 import com.lenis0012.bukkit.btm.nms.wrappers.Packet;
 import com.lenis0012.bukkit.btm.nms.wrappers.PlayerConnection;
 import com.lenis0012.bukkit.btm.util.DynamicUtil;
 import com.lenis0012.bukkit.btm.util.NetworkUtil;
 
 import net.sf.cglib.proxy.Callback;
 import net.sf.cglib.proxy.CallbackFilter;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 import net.sf.cglib.proxy.NoOp;
 
 public class PlayerConnectionCallback implements MethodInterceptor, CallbackFilter {
 	private static Logger log = Logger.getLogger("Minecraft");
 	private static final Class<?> PlayerConnectionClass = DynamicUtil.getNMSClass("PlayerConnection");
 	private static final List<Object> connections;
 	
 	static {
 		MinecraftServer server = new MinecraftServer();
 		connections = server.getPlayerConnections();
 	}
 	
 	private static void transfer(Class<?> fromClass, Object from, Object to) {
 		if(fromClass == null)
 			return;
 		
 		try {
 			for(Field field : fromClass.getDeclaredFields()) {
 				field.setAccessible(true);
 				try {
 					field.set(to, field.get(from));
 				} catch(Exception e) {
 					continue;
 				}
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		transfer(fromClass.getSuperclass(), from, to);
 	}
 	
 	private static boolean canReplace(Object playerConnection) {
 		return playerConnection.getClass().equals(PlayerConnectionClass);
 	}
 	
 	public static void hook(Player player) {
 		BeTheMob plugin = BeTheMob.instance;
 		EntityPlayer ep = new EntityPlayer(player);
 		PlayerConnection oldCon = new PlayerConnection(ep.getPlayerConnection());
 		PlayerConnectionCallback callback = new PlayerConnectionCallback();
 		
		if(!canReplace(oldCon)) {
 			log.severe("[BeTheMob] Could no replace playerConnection for player: " + player.getName());
 			log.severe("Please install ProtcolLib if this happends for more players!");
 		}
 		
 		Enhancer e = new Enhancer();
 		e.setSuperclass(PlayerConnectionClass);
 		e.setCallbacks(new Callback[] {callback, NoOp.INSTANCE});
 		e.setCallbackFilter(callback);
 		e.setClassLoader(plugin.getLoader());
 		
 		Class<?>[] params = new Class<?>[] {
 				DynamicUtil.getNMSClass("MinecraftServer"),
 				DynamicUtil.getNMSClass("INetworkManager"),
 				DynamicUtil.getNMSClass("EntityPlayer")
 		};
 		Object[] values = new Object[] {
 				oldCon.getServer(),
 				oldCon.getNetworkManager(),
 				ep.getHandle()
 		};
 		
 		Object newCon = e.create(params, values);
 		transfer(oldCon.getClass(), oldCon, newCon);
 		ep.setPlayerConnection(newCon);
 		
 		synchronized(connections) {
 			ListIterator<Object> it = connections.listIterator();
 			while(it.hasNext()) {
 				PlayerConnection con = new PlayerConnection(it.next());
 				if(con.getPlayer().equals(ep)) {
 					it.set(newCon);
 					break;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public int accept(Method method) {
 		Class<?>[] params = method.getParameterTypes();
 		String name = method.getName();
 		if(name.equals("a") && params.length == 1) {
 			String className = params[0].getSimpleName();
 			if(className.equals("Packet7UseEntity") || className.equals("Packet14BlockDig"))
 				return 0;
 		}
 		
 		return 1;
 	}
 
 	@Override
 	public Object intercept(Object instance, Method method, Object[] args,
 			MethodProxy proxy) throws Throwable {
 		BeTheMob plugin = BeTheMob.instance;
 		String className = method.getParameterTypes()[0].getSimpleName();
 		EntityPlayer ep = new PlayerConnection(instance).getPlayer();
 		Player player = ep.getBukkitEntity();
 		Packet packet = new Packet(args[0]);
 		
 		if(className.equals("Packet7UseEntity")) {
 			EntityPlayer target = null;
 			Disguise disguise = null;
 			int targetId = packet.readInt("target");
 			int action = packet.readInt("action");
 			
 			for(String user : plugin.disguises.keySet()) {
 				Disguise dis = plugin.disguises.get(user);
 				Player check = dis.getPlayer();
 				
 				if(check != null && check.isOnline() && dis.getEntityId() == targetId) {
 					target = new EntityPlayer(check);
 					disguise = dis;
 				}
 			}
 			
 			if(target != null) {
 				boolean flag = ep.longDistance(target.getHandle());
 				double distance = 36D;
 				
 				if(!flag)
 					distance = 9D;
 				
 				if(ep.distance(target.getHandle()) < distance) {
 					if(action == 0) {
 						PlayerInteractDisguisedEvent ev = new PlayerInteractDisguisedEvent(player, disguise);
 		            	Bukkit.getServer().getPluginManager().callEvent(ev);
 		            	if(!ev.isCancelled())
 		            		ep.interact(target.getHandle());
 		            	
 		            	return Void.TYPE;
 					} else if(action == 1) {
 						ep.attack(target.getHandle());
 						Player t_player = target.getBukkitEntity();
 						t_player.getWorld().playSound(t_player.getLocation(), Sound.HURT_FLESH, 63F, 1F);
 						
 						return Void.TYPE;
 					}
 				}
 			}
 		} else {
 			if(BeTheMob.getApi().isDisguised(player)) {
 				World world = player.getWorld();
 				int action = packet.readInt("e");
 				if(action == 1 || action == 2) {
 					int x = packet.readInt("a");
 					int y = packet.readInt("b");
 					int z = packet.readInt("c");
 					int type = world.getBlockTypeIdAt(x, y, z);
 					int data = world.getBlockAt(x, y, z).getData();
 					
 					if(action == 1)
 						ep.stopDigging(x, y, z);
 					else
 						ep.finishDigging(x, y, z);
 					
 					Packet newPacket = new Packet("Packet53BlockChange");
 					newPacket.write("a", x);
 					newPacket.write("b", y);
 					newPacket.write("c", z);
 					newPacket.write("material", type);
 					newPacket.write("data", data);
 					
 					Location loc = player.getLocation();
 					double d0 = loc.getX() - (x + 0.5);
 					double d1 = loc.getY() - (y + 0.5) + 1.5;
 					double d2 = loc.getZ() - (z + 0.5);
 					double d3 = d0 * d0 + d1 * d1 + d2 * d2;
 					
 					if(d3 > 36D)
 						return proxy.invokeSuper(instance, args);
 					
 					if(y >= 256)
 						return proxy.invokeSuper(instance, args);
 					
 					NetworkUtil.sendGlobalPacket(newPacket, world);
 					
 					return Void.TYPE;
 				}
 			}
 		}
 		
 		return proxy.invokeSuper(instance, args);
 	}
 }
