 /**
  * Copyright (c) 2011, Mikael Svahn, Softhouse Consulting AB
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package se.softhouse.garden.orchid.commons.text.storage.provider;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import se.softhouse.garden.orchid.commons.text.storage.provider.OrchidMessageStorageCache.MessageFactory;
 
 /**
  * An abstract class for resources that can load messages into a cache.
  * 
  * @author Mikael Svahn
  * 
  */
 public abstract class OrchidMessageResource {
 
 	protected static final List<String> EMPTY_LIST = new ArrayList<String>();
 	protected static final List<String> TYPE_PKGS_ROOT = new ArrayList<String>(Arrays.asList("+", "type"));
 	protected final OrchidMessageResourceInfo resourceInfo;
 	protected final String charsetName;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param resourceInfo
 	 *            The information about the resource
 	 * @param charsetName
 	 *            The charset to use when loading messages from this resource
 	 */
 	public OrchidMessageResource(OrchidMessageResourceInfo resourceInfo, String charsetName) {
 		this.resourceInfo = resourceInfo;
 		this.charsetName = charsetName;
 	}
 
 	/**
 	 * Load all messages in the specified resource.
 	 * 
 	 * @param cache
 	 *            The cache to load the messages into
 	 * @param pkgs
 	 *            The prefix to add to the code
 	 * @param messageFactory
 	 *            The message factory to use when creating a message
 	 * @throws IOException
 	 */
 	public abstract <T> void loadMessages(OrchidMessageStorageCache<T> cache, List<String> pkgs, MessageFactory<T> messageFactory) throws IOException;
 
 	/**
 	 * Create a new package list by copying the pkgs list and adding the code.
 	 * 
 	 * @param pkgs
 	 *            The current list to copy
 	 * @param code
 	 *            The code to add to the end of the list
 	 * @return The newly created list
 	 */
 	protected List<String> createPackageList(List<String> pkgs, String code) {
 		List<String> pkg = new ArrayList<String>(pkgs);
 		pkg.add(code);
 		return pkg;
 	}
 
 	/**
 	 * Creates a list of package names from another list and adding codes by
 	 * parsing the code argument.
 	 * 
 	 * @param pkgs
 	 *            The packages list to extend (a copy will be made)
 	 * @param code
 	 *            The codes to add to list (each code is separated by the delim)
 	 * @param delim
 	 *            The string that separates the codes
 	 * @return
 	 */
 	protected List<String> createPackageList(List<String> pkgs, String code, String delim) {
 		List<String> pkg = new ArrayList<String>(pkgs);
 		if (code != null) {
 			String[] codes = code.split(delim);
 			for (String c : codes) {
				pkg.add(c);
 			}
 		}
 		return pkg;
 	}
 
 	/**
 	 * Add the message to the cache by first creating it from the value by using
 	 * the messageFactory.
 	 * 
 	 * @param cache
 	 *            The cache to add the message to
 	 * @param pkgs
 	 *            The package list to add to the code in order to create the
 	 *            message key
 	 * @param messageFactory
 	 *            The factory to use to create a message
 	 * @param code
 	 *            The last part of the key of the message
 	 * @param value
 	 *            The value of the message, used to create the message
 	 */
 	protected <T> void addToCache(OrchidMessageStorageCache<T> cache, List<String> prePkgs, List<String> pkgs, MessageFactory<T> messageFactory, String code,
 	        String value) {
 		cache.addToCache(prePkgs, createPackageList(pkgs, code), this.resourceInfo.getLocaleCode(),
 		        messageFactory.createMessage(value, this.resourceInfo.getLocale()));
 	}
 
 }
