 package base.game.network.packets.TCP;
 
 public class ClientState {
 
 	public enum Translation {
 		STILL, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
 	}
 
 	public enum Rotation {
 		STILL, CLOCKWISE, COUNTERCLOCKWISE
 	}
 
 	public enum Gun {
 		NO_FIRE, PRIMARY_FIRE, SECONDARY_FIRE
 	}
 
 	private final Translation translation;
 	private final Rotation rotation;
 	private final Gun gun;
 
 	public ClientState(Translation translation, Rotation rotation, Gun gun) {
 		this.translation = translation;
 		this.rotation = rotation;
 		this.gun = gun;
 	}
 
	public ClientState(byte rawState) {		
		int state = rawState & 0xff; // convert to an int (avoidng the unsigned problem in java) 
 		this.gun = Gun.values()[state / (Translation.values().length * Rotation.values().length)];
 
 		state %= (Translation.values().length * Rotation.values().length);
 		this.rotation = Rotation.values()[state / Translation.values().length];
 
 		state %= Translation.values().length;
 		this.translation = Translation.values()[state];
 	}
 
 	public byte getState() {
 		byte out = (byte) translation.ordinal(); // start by putting the
 													// translation state (0-8)
 		out += Translation.values().length * rotation.ordinal(); // add the
 																	// rotation
 																	// state
 																	// (0-2)
 																	// multiplied
 																	// by the 9
 																	// translation
 																	// states
 		out += Translation.values().length * Rotation.values().length * gun.ordinal(); // finally
 																						// add
 																						// the
 																						// gun
 																						// state
 																						// (0-2)
 																						// multiplied
 																						// by
 																						// the
 																						// 27
 																						// roto-translation
 																						// states.
 		return out;
 	}
 
 	@Override
 	public String toString() {
 		String out = "Translation: " + translation.name();
 		out = out.concat(" Rotation: " + rotation.name());
 		out = out.concat(" Gun: " + gun.name());
 		return out;
 	}
 
 }
