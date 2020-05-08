 package sat.radio.message.stream;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import sat.radio.message.*;
 import sat.utils.crypto.RSAKey;
 import sat.utils.geo.Coordinates;
 import sat.utils.routes.Waypoint;
 
 /**
  * Flux de sortie des messages radio.
  */
 public class MessageOutputStream extends FilterOutputStream {
 	/**
 	 * Un flux interne servant de buffer pour l'écriture des messages.
 	 */
 	private ByteArrayOutputStream baos;
 
 	/**
 	 * Un flux d'écriture de données binaires.
 	 */
 	private DataOutputStream dos;
 
 	/**
 	 * Indique si le flux doit utiliser le mode étendu.
 	 */
 	private boolean extended = false;
 
 	/**
 	 * Crée un nouveau flux de sortie de messages radio.
 	 * 
 	 * @param out
 	 *            Le flux vers lequel les messages sérialisés seront écrit.
 	 */
 	public MessageOutputStream(OutputStream out) {
 		super(out);
 
 		baos = new ByteArrayOutputStream();
 		dos = new DataOutputStream(baos);
 	}
 
 	/**
 	 * Ecrit un message dans le flux sous-jacent.
 	 * 
 	 * @param m
 	 *            Le message a écrire.
 	 * @throws IOException
 	 *             Si l'écriture dans le flux sous-jacent a provoqué une
 	 *             exception.
 	 */
 	public synchronized void writeMessage(Message m) throws IOException {
 		// Clear the previous output buffer
 		baos.reset();
 
 		if(extended) {
 			byte[] id = Serializer.serialize(m.getID());
 			dos.writeInt(id.length);
 			dos.write(id);
 		}
 		else {
 			dos.write(m.getID().toLegacyID());
 		}
 
 		dos.writeInt(m.getLength());
 		dos.writeInt(m.getPriority());
 
 		Coordinates c = m.getCoordinates();
 
 		if(extended) { // Extended mode use floats for coordinates
 			dos.writeFloat(c.getX());
 			dos.writeFloat(c.getY());
 			dos.writeFloat(c.getZ());
 		}
 		else {
 			dos.writeInt((int) c.getX());
 			dos.writeInt((int) c.getY());
 		}
 
 		dos.writeInt(m.getType().ordinal());
 
 		switch(m.getType()) {
 			case HELLO:
 				writeMessageAttributes((MessageHello) m);
 				break;
 
 			case DATA:
 				writeMessageAttributes((MessageData) m);
 				break;
 
 			case MAYDAY:
 				writeMessageAttributes((MessageMayDay) m);
 				break;
 
 			case SENDRSA:
 				writeMessageAttributes((MessageSendRSAKey) m);
 				break;
 
 			case ROUTING:
 				writeMessageAttributes((MessageRouting) m);
 				break;
 
 			case CHOKE:
 			case UNCHOKE:
 			case BYE:
 			case KEEPALIVE:
 			case LANDINGREQUEST:
 			case UPGRADE:
 				// No additionnal data
 				break;
 
 			default:
 				// Invalid message, send nothing
 				throw new IOException("Invalid message");
 		}
 
 		dos.flush(); // useful ?
 		out.write(baos.toByteArray());
 		out.flush();
 	}
 
 	/**
 	 * Ecrit les attributs spécifiques à un message Hello.
 	 */
 	private void writeMessageAttributes(MessageHello m) throws IOException {
 		byte reserved = 0;
 
 		reserved += m.isCiphered() ? 1 << 4 : 0;
 		reserved += m.isExtended() ? 1 << 7 : 0;
 
 		dos.write(reserved);
 	}
 
 	/**
 	 * Ecrit les attributs spécifiques à un message Data.
 	 */
 	private void writeMessageAttributes(MessageData m) throws IOException {
 		dos.write(m.getHash());
 		dos.writeInt(m.getContinuation());
 
 		if(extended) {
 			byte[] serializedString = Serializer.serialize(m.getFormat());
 			dos.writeInt(serializedString.length);
 			dos.write(serializedString);
 		}
 		else {
			byte[] legacyFormatByte = new byte[4];
			byte[] formatByte = m.getFormat().getBytes();
			
			System.arraycopy(formatByte, 0, legacyFormatByte, 0, ((formatByte.length > 4) ? 4 : formatByte.length));
			
			dos.write(legacyFormatByte);
 		}
 
 		dos.writeInt(m.getFileSize());
 		dos.write(m.getPayload());
 	}
 
 	/**
 	 * Ecrit les attributs spécifiques à un message MayDay.
 	 */
 	private void writeMessageAttributes(MessageMayDay m) throws IOException {
 		dos.write(m.getCause().getBytes());
 	}
 
 	/**
 	 * Ecrit les attributs spécifiques à un message SendRSAKey.
 	 */
 	private void writeMessageAttributes(MessageSendRSAKey m) throws IOException {
 		RSAKey key = m.getKey();
 
 		dos.writeInt(key.getLength());
 
 		byte[] modulus = key.getModulus().toByteArray();
 		dos.writeInt(modulus.length);
 		dos.write(modulus);
 
 		byte[] exponent = key.getExponent().toByteArray();
 		dos.writeInt(exponent.length);
 		dos.write(exponent);
 	}
 
 	/**
 	 * Ecrit les attributs spécifiques à un message Routing.
 	 */
 	private void writeMessageAttributes(MessageRouting m) throws IOException {
 		Waypoint waypoint = m.getWaypoint();
 
 		dos.writeInt(m.getRoutingType().ordinal());
 		dos.writeInt(waypoint.getType().ordinal());
 
 		if(m.getLength() > 0) {
 			if(extended) {
 				dos.writeFloat(waypoint.getAngle());
 			}
 			else {
 				dos.writeInt((int) waypoint.getAngle());
 			}
 		}
 	}
 
 	/**
 	 * Indique si le flux est étendu.
 	 */
 	public boolean isExtended() {
 		return extended;
 	}
 
 	/**
 	 * Défini si le flux doit utiliser le mode étendu.
 	 */
 	public synchronized void setExtended(boolean extended) {
 		this.extended = extended;
 	}
 }
