 package org.apache.lucene.misc;
 
 /* ====================================================================
  * The Apache Software License, Version 1.1
  *
  * Copyright (c) 2004 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Apache" and "Apache Software Foundation" and
  *    "Apache Lucene" must not be used to endorse or promote products
  *    derived from this software without prior written permission. For
  *    written permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    "Apache Lucene", nor may "Apache" appear in their name, without
  *    prior written permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org/>.
  */
  
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * TrigramLanguageGuesser implements language guessing based on trigrams
  * 
  * @author Jean-Francois Halleux
  * @version $version$
  * 
  */
 public class TrigramLanguageGuesser implements LanguageGuesser {
 
 	private Map trigramsmap = new HashMap();
 	private Trigrams test = new Trigrams();
 	private TrigramGenerator tg;
 
 	/*
 	 * Construct a LanguageGuesser
 	 * fileLocation is a directory containing
 	 * xx.tri files where xx is the ISO-639 Language Code 
 	 * see http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
 	 */
 	public TrigramLanguageGuesser(String fileLocation) throws IOException {
 		tg = new TrigramGenerator();
 		tg.addTrigramListener(test);
 
 		File f = new File(fileLocation);
 		if (f.isDirectory()) {
 			String[] files = f.list();
 			for (int i = 0; i < files.length; i++) {
 				if (files[i].endsWith("tri")) {
					addFile(f.getAbsolutePath() + File.separator + files[i]);
 				}
 			}
 		}
 		
 		if (trigramsmap.size()==0) throw new RuntimeException("Location [ "+f.getAbsolutePath()+" ] doesn't contain any .tri file");
 	}
 	
 	public TrigramLanguageGuesser(URL[] urls) throws IOException {
 		
 		tg = new TrigramGenerator();
 		tg.addTrigramListener(test);
 		
 		for(int i=0; i<urls.length; i++){
 			URL url = urls[i];
 			String path = url.getPath();
 			
 			int fileNameStart = path.lastIndexOf('/') + 1;
 			int fileNameEnd = path.lastIndexOf('.');
 			
 			String code = path.substring(fileNameStart, fileNameEnd);
 			
 			addFromStream(code, url.openStream());
 		}
 	}
 
 	/**
 	 * Adds a single xx.tri file
 	 */
 	private void addFile(String file) throws IOException {
 		File f = new File(file);
 		String isoCode = f.getName().substring(0, f.getName().length() - 4);
 		Trigrams t = Trigrams.loadFromFile(f.getCanonicalPath());
 		trigramsmap.put(t, isoCode);
 	}
 	
 	private void addFromStream(String code, InputStream is) throws IOException {
 		trigramsmap.put(Trigrams.loadFromStream(is), code);
 	}
 	
 	/**
 	 * Returns the supported languages
 	 */
 	public String[] supportedLanguages() {
 		Object[] la=trigramsmap.values().toArray();
 		String[] las=new String[la.length];
 		for (int i=0;i<la.length;i++) {
 			las[i]=(String)la[i];
 		}
 		return las;
 	}
 
 	/*
 	 * Guess the language of r and returns its ISO-639 Code
 	 */
 	public String guessLanguage(Reader r) throws IOException {
 		return guessLanguage(r, Integer.MAX_VALUE);
 	}
 
 	/*
 	 * Guess the language of r and returns its ISO-639 Code
 	 * Maximum maxGrams of r are processed
 	 */
 	public String guessLanguage(Reader r, int maxGrams) throws IOException {
 		if (r==null) throw new IllegalArgumentException("Reader r must not be null");
 		if (maxGrams < 1) throw new IllegalArgumentException("maxGrams must be greater or equal to 1");
 		
 		Trigrams bestreference = null;
 
 		tg.setReader(r);
 		test.clear();
 		tg.start(maxGrams);
 
 		Set refset = trigramsmap.keySet();
 		Iterator it = refset.iterator();
 		long min = Long.MAX_VALUE;
 		while (it.hasNext()) {
 			Trigrams reference = (Trigrams) it.next();
 			long distance = test.distance(reference);
 			if (distance < min) {
 				bestreference = reference;
 				min = distance;
 			}
 		}
 		return (String) trigramsmap.get(bestreference);
 	}
 
 	/*
 	 * Guess the language of r and returns and array of Language Probability
 	 * sorted in decreasing order of probability
 	 */
 	public LanguageProbability[] guessLanguages(Reader r) throws IOException {
 		return guessLanguages(r, Integer.MAX_VALUE);
 	}
 
 	/*
 	* Guess the language of r and returns and array of Language Probability
 	* sorted in decreasing order of probability
 	* 
 	* If r is empty, returns all languages with probability of 0.0
 	* 
 	* Maximum maxGrams of r are processed
 	*/
 	public LanguageProbability[] guessLanguages(Reader r, int maxGrams)
 		throws IOException {
 			
 		if (r==null) throw new IllegalArgumentException("Reader r must not be null");
 		if (maxGrams < 1) throw new IllegalArgumentException("maxGrams must be greater or equal to 1");
 		
 		SortedSet ss = new TreeSet();
 
 		tg.setReader(r);
 		test.clear();
 		tg.start(maxGrams);
 
 		Set refset = trigramsmap.keySet();
 		Iterator it = refset.iterator();
 		while (it.hasNext()) {
 			Trigrams reference = (Trigrams) it.next();
 			long distance = test.distance(reference);
 			ss.add(
 				new LanguageProbability(
 					(String) trigramsmap.get(reference),
 					distance));
 		}
 
 		//Transfer to array
 		LanguageProbability[] lp = new LanguageProbability[ss.size()];
 		it = ss.iterator();
 		int i = 0;
 		while (it.hasNext()) {
 			lp[i++] = (LanguageProbability) it.next();
 		}
 
 		float minprob = lp[0].probability;
 		
 		//In case of an empty reader, all languages are returned
 		//with a probability of 0.0
 		if (minprob>0.0f) {
 			for (i = 0; i < lp.length; i++) {
 				lp[i].probability = minprob / lp[i].probability;
 			}
 		}
 		
 		return lp;
 	}
 }
