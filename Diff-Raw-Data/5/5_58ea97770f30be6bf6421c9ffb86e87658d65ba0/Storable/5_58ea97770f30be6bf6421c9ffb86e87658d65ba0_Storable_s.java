 /*
  * utils - Storable.java - Copyright © 2011 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.storage;
 
 /**
  * Interface for objects that can write themselves to a byte array.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public interface Storable {
 
 	/**
 	 * The ID of the object.
 	 *
 	 * @return The ID of the object
 	 */
 	public long getId();
 
 	/**
 	 * Writes the contents of this {@code Storable} to a byte array.
 	 *
 	 * @return The byte array with the content
 	 * @throws StorageException
 	 *             if a store error occurs
 	 */
 	public byte[] getBuffer() throws StorageException;
 
 	/**
 	 * Helper methods for populating and parsing byte arrays.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	static class Utils {
 
 		/**
 		 * Returns a char from the byte array.
 		 *
 		 * @param buffer
 		 *            The byte array
 		 * @param position
 		 *            The position of the data
 		 * @return The char from the byte array
 		 */
 		public static char getChar(byte[] buffer, int position) {
			return (char) (buffer[position + 0] | (buffer[position + 1] << 8));
 		}
 
 		/**
 		 * Returns an integer from the byte array.
 		 *
 		 * @param buffer
 		 *            The byte array
 		 * @param position
 		 *            The position of the data
 		 * @return The integer from the byte array
 		 */
 		public static int getInt(byte[] buffer, int position) {
 			return (buffer[position + 0] & 0xff) | ((buffer[position + 1] << 8) & 0xff00) | ((buffer[position + 2] << 16) & 0xff0000) | ((buffer[position + 3] << 24) & 0xff000000);
 		}
 
 		/**
 		 * Returns a long from the byte array.
 		 *
 		 * @param buffer
 		 *            The byte array
 		 * @param position
 		 *            The position of the data
 		 * @return The long from the byte array
 		 */
 		public static long getLong(byte[] buffer, int position) {
			return (buffer[position + 0] & 0xff) | ((buffer[position + 1] << 8) & 0xff00) | ((buffer[position + 2] << 16) & 0xff0000) | ((buffer[position + 3] << 24) & 0xff000000) | ((buffer[position + 4] << 32) & 0xff00000000L) | ((buffer[position + 5] << 40) & 0xff0000000000L) | ((buffer[position + 6] << 48) & 0xff000000000000L) | ((buffer[position + 7] << 56) & 0xff00000000000000L);
 		}
 
 		/**
 		 * Stores a char in the byte array.
 		 *
 		 * @param value
 		 *            The char to store
 		 * @param buffer
 		 *            The byte array
 		 * @param position
 		 *            The position of the data
 		 */
 		public static void putChar(char value, byte[] buffer, int position) {
 			buffer[position + 0] = (byte) (value & 0xff);
 			buffer[position + 1] = (byte) ((value >>> 8) & 0xff);
 		}
 
 		/**
 		 * Stores an integer in the byte array.
 		 *
 		 * @param value
 		 *            The integer to store
 		 * @param buffer
 		 *            The byte array
 		 * @param position
 		 *            The position of the data
 		 */
 		public static void putInt(int value, byte[] buffer, int position) {
 			buffer[position + 0] = (byte) (value & 0xff);
 			buffer[position + 1] = (byte) ((value >>> 8) & 0xff);
 			buffer[position + 2] = (byte) ((value >>> 16) & 0xff);
 			buffer[position + 3] = (byte) ((value >>> 24) & 0xff);
 		}
 
 		/**
 		 * Stores a long in the byte array.
 		 *
 		 * @param value
 		 *            The long to store
 		 * @param buffer
 		 *            The byte array
 		 * @param position
 		 *            The position of the data
 		 */
 		public static void putLong(long value, byte[] buffer, int position) {
 			buffer[position + 0] = (byte) (value & 0xff);
 			buffer[position + 1] = (byte) ((value >>> 8) & 0xff);
 			buffer[position + 2] = (byte) ((value >>> 16) & 0xff);
 			buffer[position + 3] = (byte) ((value >>> 24) & 0xff);
 			buffer[position + 4] = (byte) ((value >>> 32) & 0xff);
 			buffer[position + 5] = (byte) ((value >>> 40) & 0xff);
 			buffer[position + 6] = (byte) ((value >>> 48) & 0xff);
 			buffer[position + 7] = (byte) ((value >>> 56) & 0xff);
 		}
 
 	}
 
 }
