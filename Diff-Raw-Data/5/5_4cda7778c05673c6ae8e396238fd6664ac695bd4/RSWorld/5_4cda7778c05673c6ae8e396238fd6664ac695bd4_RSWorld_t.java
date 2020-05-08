 package org.pcbot.net.wrappers;
 
 import org.pcbot.net.methods.Worlds;
 import org.rsbot.script.methods.Lobby;
 import org.rsbot.script.methods.MethodContext;
 import org.rsbot.script.methods.MethodProvider;
 import org.rsbot.script.wrappers.RSComponent;
 
 public class RSWorld extends MethodProvider {
 	private final RSComponent component;
 
 	public RSWorld(final MethodContext ctx, final RSComponent component) {
 		super(ctx);
 		this.component = component;
 	}
 
 	public int getServer() {
		return methods.worlds.openTab() ? Integer.parseInt(methods.interfaces.getComponent(Lobby.WORLD_SELECT_INTERFACE, Lobby.WORLD_SELECT_INTERFACE_WORLD_NAME).getComponents()[component.getComponentIndex()].getText()) : -1;
 	}
 
 	public long getPing() {
 		return methods.worlds.averagePing(getServer());
 	}
 
 	public boolean isMembers() {
 		if (methods.worlds.openTab()) {
 			try {
 				final RSComponent star = methods.interfaces.getComponent(Lobby.WORLD_SELECT_INTERFACE, Worlds.INTERFACE_WORLD_SELECT_STARS).getComponents()[component.getComponentIndex()];
 				if (star.getBackgroundColor() == Worlds.INTERFACE_MEMBERS_TEXTURE) {
 					return true;
 				}
			} catch (ArrayIndexOutOfBoundsException ignored) {
 			}
 		}
 		return false;
 	}
 }
