 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.metrics;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.plugins.erlang.language.ErlangFunction;
 import org.sonar.plugins.erlang.utils.StringUtils;
 
 public final class PublicApiCounter {
 	
 	private PublicApiCounter(){}
 	
 	private static final Logger LOG = LoggerFactory.getLogger(PublicApiCounter.class);
 	private static final Pattern exportPattern = Pattern.compile("(-export\\(\\[)(.*?)(\\]\\))\\.", Pattern.DOTALL
 			+ Pattern.MULTILINE);
 	private static final Pattern exportAllPattern = Pattern.compile("-compile\\(.*?export_all.*?\\)\\.", Pattern.DOTALL
 			+ Pattern.MULTILINE);
 	private static final Pattern specPattern = Pattern.compile("^-spec.*");
 	
 	/**
 	 * Return with a list of doubles first value is the number of public APIs the
 	 * second is the number of undocumented APIs
 	 * 
 	 * @param source
 	 * @param linesnAlyzer
 	 * @return
 	 * @throws IOException
 	 */
 	public static List<Double> countPublicApi(String source, ErlangSourceByLineAnalyzer linesnAlyzer) throws IOException {
 		int numOfPublicMethods = 0;
 		int numOfUndocPublicMethods = 0;
 		List<Double> ret = new ArrayList<Double>();
 		String sourceWithoutComment = source.replaceAll("%[^\n]*", "");
 		if (exportAllPattern.matcher(source).find()) {
 			for (ErlangFunction function : linesnAlyzer.getFunctions()) {
 				int start = source.indexOf(function.getFirstLine());
 				numOfUndocPublicMethods = increaseIfNecessary(source, numOfUndocPublicMethods, start);
 				numOfPublicMethods++;
 			}
 		} else {
 			Matcher m = exportPattern.matcher(sourceWithoutComment);
 			while (m.find()) {
 				String exportedMethods = m.group(2);
 				String[] publicMethods = exportedMethods.replaceAll("[\n\r\t]", "").split(",");
 				numOfPublicMethods += publicMethods.length;
 				for (String method : publicMethods) {
 					LOG.debug("Processing: " + method);
 					method = method.trim();
					String methodName = method.split("\\/")[0].trim();
					Integer numOfVariables = Integer.valueOf(method.split("\\/")[1].trim());
 					StringBuilder regEx = new StringBuilder(methodName);
 					regEx.append("\\(.*?");
 					for (int i = 1; i < numOfVariables; i++) {
 						regEx.append(",.*?");
 					}
 					regEx.append("\\).*");
 					Matcher findMethodMatcher = Pattern.compile(regEx.toString(), Pattern.DOTALL + Pattern.MULTILINE)
 							.matcher(source);
 					findMethodMatcher.find();
 					int start = findMethodMatcher.start();
 					numOfUndocPublicMethods = increaseIfNecessary(source, numOfUndocPublicMethods, start);
 				}
 
 			}
 		}
 		ret.add((double) numOfPublicMethods);
 		ret.add((double) numOfUndocPublicMethods);
 		return ret;
 	}
 
 	private static int increaseIfNecessary(String source, int numOfUndocPublicMethods, int start) throws IOException {
 		List<String> linesBefore = getSourceBefore(source, start);
 		boolean isComment = isDocumented(linesBefore);
 		if (!isComment) {
 			return (numOfUndocPublicMethods+1);
 		}
 		return numOfUndocPublicMethods;
 	}
 
 	private static List<String> getSourceBefore(String source, int start) throws IOException {
 		String beforeMethod = source.substring(0, start);
 		return StringUtils.convertStringToListOfLines(beforeMethod);
 	}
 
 	private static boolean isDocumented(List<String> linesBefore) {
 		for (ListIterator<String> iterator = linesBefore.listIterator(linesBefore.size()); iterator.hasPrevious();) {
 			String line = iterator.previous();
 			if (StringUtils.isBlank(line) || ErlangSourceByLineAnalyzer.isDecoratorPatter.matcher(line).matches() || specPattern.matcher(line).matches()) {
 				continue;
 			}
 			if (ErlangSourceByLineAnalyzer.isCommentPatter.matcher(line).matches()) {
 				return true;
 			}
 			return false;
 		}
 		return false;
 	}
 
 }
