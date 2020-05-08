 // vim:filetype=java:ts=4
 /*
 	Copyright (c) 2005, 2006
 	Conor McDermottroe.  All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without
 	modification, are permitted provided that the following conditions
 	are met:
 	1. Redistributions of source code must retain the above copyright
 	   notice, this list of conditions and the following disclaimer.
 	2. Redistributions in binary form must reproduce the above copyright
 	   notice, this list of conditions and the following disclaimer in the
 	   documentation and/or other materials provided with the distribution.
 	3. Neither the name of the author nor the names of any contributors to
 	   the software may be used to endorse or promote products derived from
 	   this software without specific prior written permission.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 	HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 	TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 	OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 	OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package com.mcdermottroe.exemplar.output;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import com.mcdermottroe.exemplar.Constants;
 import com.mcdermottroe.exemplar.DBC;
 import com.mcdermottroe.exemplar.Utils;
 import com.mcdermottroe.exemplar.model.XMLDocumentType;
 import com.mcdermottroe.exemplar.ui.Message;
 
 /** Output handling utility methods.
 
 	@author	Conor McDermottroe
 	@since	0.1
 */
 public final class OutputUtils {
 	/** Private constructor to prevent instantiation of this class. */
 	private OutputUtils() {
 		DBC.UNREACHABLE_CODE();
 	}
 
 	/** Load the code fragment resource bundle for a given source generator.
 
 		@param generatorName	The canonical name of the {@link
 								XMLParserSourceGenerator} class.
 		@return					The correct {@link ResourceBundle} for the
 								given {@link XMLParserSourceGenerator}.
 	*/
 	public static ResourceBundle getCodeFragments(String generatorName) {
 		DBC.REQUIRE(generatorName != null);
 		if (generatorName == null) {
 			return null;
 		}
 
 		String genName = generatorName.substring(
 			0,
 			generatorName.length() - Constants.Output.CLASS.length() - 1
 		);
 		return ResourceBundle.getBundle(
			genName + Constants.Output.CODE_FRAGMENTS_FILE_EXTENSION
 		);
 	}
 
 	/** Generate a parser given an {@link XMLDocumentType} and the type of
 		parser that is required.
 
 		@param	doctype			The {@link XMLDocumentType} describing the
 								vocabulary.
 		@param	filename		The name of the file to output to (directory
 								name for multi file output).
 		@param	language		The language of the parser to be output.
 		@param	api				The API (if any) that the parser to be output
 								must conform to.
 		@throws OutputException	if any of the required parameters are null, if
 								the generator cannot be instantiated or if the
 								generator throws an exception while trying to
 								generate code.
 	*/
 	public static void generateParser	(
 											XMLDocumentType doctype,
 											String filename,
 											String language,
 											String api
 										)
 	throws OutputException
 	{
 		// Preconditions
 		if (doctype == null) {
 			throw new OutputException(Message.SOURCE_GENERATOR_DOCTYPE_NULL);
 		}
 		if (language == null) {
 			throw new OutputException(Message.SOURCE_GENERATOR_LANGUAGE_NULL);
 		}
 
 		// Make the generator
 		XMLParserSourceGenerator gen;
 		gen = XMLParserSourceGenerator.create(language, api);
 
 		// The output file
 		File outputFile = null;
 		if (filename != null) {
 			outputFile = new File(filename);
 		}
 
 		// Make the generator work
 		try {
 			gen.generateParser(doctype, outputFile);
 		} catch (XMLParserGeneratorException e) {
 			throw new OutputException(
 				Message.SOURCE_GENERATOR_THREW_EXCEPTION,
 				e
 			);
 		}
 	}
 
 	/** Write a {@link String} to a {@link File}.
 
 		@param	s				The {@link String} to write to the {@link
 								File}.
 		@param	file			The {@link File} to write the {@link String}
 								to.
 		@throws	OutputException if anything goes wrong.
 	*/
 	public static void writeStringToFile(String s, File file)
 	throws OutputException
 	{
 		DBC.REQUIRE(s != null);
 		DBC.REQUIRE(file != null);
 		if (s == null || file == null) {
 			return;
 		}
 
 		BufferedWriter output = null;
 		try {
 			// Write the string to the output file.
 			output = new BufferedWriter(new FileWriter(file));
 			output.write(s);
 			output.close();
 
 			// Prevent output from being closed a second time.
 			output = null;
 		} catch (IOException e) {
 			throw new OutputException(
 				Message.FILE_WRITE_IO_EXCEPTION(
 					file.getAbsolutePath()
 				),
 				e
 			);
 		} finally {
 			if (output != null) {
 				try {
 					output.close();
 				} catch (IOException e) {
 					// Ignore, there's nothing more that can be done.
 				}
 			}
 		}
 	}
 
 	/** Discover the available output languages.
 
 		@return	A {@link SortedMap} where the keys are the available output
 				languages and the values are the descriptions of those output
 				languages.
 	*/
 	public static SortedMap availableOutputLanguages() {
 		SortedMap availableOutputLanguages = new TreeMap();
 
 		SortedSet languageAPIPairs = availableOutputLanguageAPIPairs();
 		for (Iterator it = languageAPIPairs.iterator(); it.hasNext(); ) {
 			LanguageAPIPair pair = (LanguageAPIPair)it.next();
 			String language = pair.getLanguage();
 			String api = pair.getAPI();
 			XMLParserSourceGenerator gen;
 			gen = XMLParserSourceGenerator.create(language, api);
 			StringBuffer description = new StringBuffer(gen.describeLanguage());
 			LanguageAPIPair containedPair = new LanguageAPIPair(language, null);
 			if (!languageAPIPairs.contains(containedPair)) {
 				description.append(Message.OPTION_LANGUAGE_REQUIRES_API);
 			}
 			availableOutputLanguages.put(language, description.toString());
 		}
 
 		return availableOutputLanguages;
 	}
 
 	/** Discover the available output APIs.
 
 		@return A {@link SortedMap} where the keys are the available output
 				APIs and the values are the descriptions of those output APIs.
 	*/
 	public static SortedMap availableOutputAPIs() {
 		SortedMap availableOutputAPIs = new TreeMap();
 
 		SortedSet languageAPIPairs = availableOutputLanguageAPIPairs();
 		for (Iterator it = languageAPIPairs.iterator(); it.hasNext(); ) {
 			LanguageAPIPair pair = (LanguageAPIPair)it.next();
 			String language = pair.getLanguage();
 			String api = pair.getAPI();
 			if (api != null) {
 				XMLParserSourceGenerator gen;
 				gen = XMLParserSourceGenerator.create(language, api);
 				StringBuffer description = new StringBuffer(gen.describeAPI());
 				description.append(Message.OPTION_LANGUAGE_OF_API(language));
 				availableOutputAPIs.put(api, description.toString());
 			}
 		}
 
 		return availableOutputAPIs;
 	}
 
 	/** Discover the set of output language-API pairs that are legal.
 
 		@return A {@link SortedSet} containing a {@link LanguageAPIPair} for
 				every legal combination of language and API.
 	*/
 	private static SortedSet availableOutputLanguageAPIPairs() {
 		SortedSet ret = new TreeSet();
 
 		List packages = Utils.findSubPackages(Constants.Output.PACKAGE);
 		for (Iterator it = packages.iterator(); it.hasNext(); ) {
 			String packageName = (String)it.next();
 			String outputLanguageAPI = packageName.substring(
 				Constants.Output.PACKAGE.length() + 1
 			);
 
 			LanguageAPIPair pair = null;
 			int fSI = outputLanguageAPI.indexOf(
 				(int)Constants.Character.FULL_STOP
 			);
 			if (fSI != -1) {
 				String lang = outputLanguageAPI.substring(0, fSI);
 				String api = outputLanguageAPI.substring(fSI + 1);
 				if (api.indexOf((int)Constants.Character.FULL_STOP) == -1) {
 					pair = new LanguageAPIPair(lang, api);
 				}
 			} else {
 				pair = new LanguageAPIPair(outputLanguageAPI, null);
 			}
 
 			DBC.ASSERT(pair != null);
 			if (pair == null) {
 				return null;
 			}
 			XMLParserSourceGenerator gen;
 			gen = XMLParserSourceGenerator.create(
 				pair.getLanguage(),
 				pair.getAPI()
 			);
 			if (gen != null) {
 				ret.add(pair);
 			}
 		}
 
 		return ret;
 	}
 
 	/** A class representing a language-API pair.
 
 		@author	Conor McDermottroe
 		@since	0.1
 	*/
 	private static final class LanguageAPIPair implements Comparable {
 		/** The language. May not be null. */
 		private String language;
 
 		/** The API. May be null. */
 		private String api;
 
 		/** Basic constructor to set the members.
 
 			@param theLanguage	The value for the language member.
 			@param theApi		The value for the api member.
 		*/
 		private LanguageAPIPair(String theLanguage, String theApi) {
 			DBC.REQUIRE(theLanguage != null);
 			language = theLanguage;
 			api = theApi;
 			DBC.ENSURE(language != null);
 		}
 
 		/** Accessor for the language member.
 
 			@return The language member. Guaranteed not to be null.
 		*/
 		public String getLanguage() {
 			DBC.ENSURE(language != null);
 			return language;
 		}
 
 		/** Accessor for the api member.
 
 			@return The api member. May be null.
 		*/
 		public String getAPI() {
 			return api;
 		}
 
 		/** See {@link Object#equals(Object)}.
 
 			@param	o	The object to compare against.
 			@return		True if <code>this</code> is equal to <code>o</code>.
 		*/
 		public boolean equals(Object o) {
 			if (this == o) {
 				return true;
 			}
 			if (o == null || !(o instanceof LanguageAPIPair)) {
 				return false;
 			}
 
 			LanguageAPIPair other = (LanguageAPIPair)o;
 			if (!Utils.areDeeplyEqual(language, other.getLanguage())) {
 				return false;
 			}
 			if (!Utils.areDeeplyEqual(api, other.getAPI())) {
 				return false;
 			}
 
 			return true;
 		}
 
 		/** See {@link Object#hashCode()}.
 
 			@return	A hash code.
 		*/
 		public int hashCode() {
 			Object[] hashCodeVars = {
 				language,
 				api,
 			};
 			return Utils.genericHashCode(hashCodeVars);
 		}
 
 		/** See {@link Object#toString()}.
 
 			@return	A descriptive {@link String}.
 		*/
 		public String toString() {
 			return	Constants.Character.LEFT_CURLY +
 					language +
 					Constants.Character.COMMA +
 					Constants.Character.SPACE +
 					api +
 					Constants.Character.RIGHT_CURLY;
 		}
 
 		/** Implement {@link Comparable} so that {@link LanguageAPIPair}
 			objects can be contained in a sorted {@link java.util.Collection}.
 
 			@param	o	The object to compare this one to.
 			@return		-1, 0 or 1 if this object is less-than, equal to, or
 						greater than o, respectively.
 		*/
 		public int compareTo(Object o) {
 			// This is required by the general contract of
 			// Comparable.compareTo(Object)
 			DBC.REQUIRE(o != null);
 			if (o == null) {
 				throw new NullPointerException();
 			}
 
 			int result;
 			if (o instanceof LanguageAPIPair) {
 				LanguageAPIPair other = (LanguageAPIPair)o;
 
 				int langCompare = language.compareTo(other.getLanguage());
 				if (langCompare == 0) {
 					if (api == null) {
 						if (other.getAPI() == null) {
 							result = 0;
 						} else {
 							result = -1;
 						}
 					} else {
 						String otherAPI = other.getAPI();
 						if (otherAPI == null) {
 							result = 1;
 						} else {
 							result = api.compareTo(otherAPI);
 						}
 					}
 				} else {
 					result = langCompare;
 				}
 			} else {
 				// Only LanguageAPIPair objects should be compared against.
 				DBC.IGNORED_ERROR();
 				result = -1;
 			}
 
 			return result;
 		}
 	}
 }
