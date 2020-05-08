 /*
  * utils.json - JsonObject.java - Copyright © 2010 David Roden
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
 
 package net.pterodactylus.util.json;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * A JSON object contains a mapping from {@link String} keys to values that can
  * be {@link String}s, {@link Number}s, {@link Boolean}s, {@link JsonArray}s,
  * or other {@link JsonObject}s.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class JsonObject {
 
 	/** The key-value mappings. */
 	private final Map<String, Object> values = new HashMap<String, Object>();
 
 	/**
 	 * Stores the given {@link String} value under the given key.
 	 *
 	 * @param key
 	 *            The key to store the value under
 	 * @param value
 	 *            The value to store
 	 * @return This object (for method chaining)
 	 */
 	public JsonObject put(String key, String value) {
 		values.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Stores the given {@link Number} value under the given key.
 	 *
 	 * @param key
 	 *            The key to store the value under
 	 * @param value
 	 *            The value to store
 	 * @return This object (for method chaining)
 	 */
 	public JsonObject put(String key, Number value) {
 		values.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Stores the given {@link Boolean} value under the given key.
 	 *
 	 * @param key
 	 *            The key to store the value under
 	 * @param value
 	 *            The value to store
 	 * @return This object (for method chaining)
 	 */
 	public JsonObject put(String key, boolean value) {
 		values.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Stores the given {@link JsonObject JSON object} under the given key.
 	 *
 	 * @param key
 	 *            The key to store the value under
 	 * @param value
 	 *            The value to store
 	 * @return This object (for method chaining)
 	 */
 	public JsonObject put(String key, JsonObject value) {
 		values.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Stores the given {@link JsonArray JSON array} under the given key.
 	 *
 	 * @param key
 	 *            The key to store the value under
 	 * @param value
 	 *            The value to store
 	 * @return This object (for method chaining)
 	 */
 	public JsonObject put(String key, JsonArray value) {
 		values.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Writes this object to the given writer.
 	 *
 	 * @param writer
 	 *            The writer to write the object to
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public void write(Writer writer) throws IOException {
 		writer.write('{');
		boolean first = true;
 		for (Entry<String, Object> entry : values.entrySet()) {
			if (first) {
				first = false;
			} else {
				writer.write(',');
			}
 			writer.write(JsonUtils.format(entry.getKey()));
 			writer.write(':');
 			writer.write(JsonUtils.format(entry.getValue()));
 		}
 		writer.write('}');
 	}
 
 	/**
 	 * Returns a textual representation of this object. This method is
 	 * equivalent to calling {@link #write(Writer)} with a newly created
 	 * {@link StringWriter}.
 	 *
 	 * @return A textual representation of this array
 	 */
 	@Override
 	public String toString() {
 		StringWriter stringWriter = new StringWriter();
 		try {
 			write(stringWriter);
 		} catch (IOException ioe1) {
 			/* ignore, StringWriter never throws. */
 		}
 		return stringWriter.toString();
 	}
 
 }
