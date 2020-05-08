 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.system;
 
 import java.util.List;
 import java.util.Map;
 
 import org.atlasapi.media.entity.Description;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.mongo.AliasWriter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 @Controller
 public class AliasController {
 
 	private final AliasWriter writer;
 	private final ContentResolver finder;
 
 	public AliasController(AliasWriter writer, ContentResolver finder) {
 		this.writer = writer;
 		this.finder = finder;
 	}
 	
 	@RequestMapping("/system/aliases")
 	public String showAliasForm() {
		return "system/aliases";
 	}
 	
 	@RequestMapping(value="/system/aliases", method=RequestMethod.POST)
 	public String addAlias(@RequestParam String csvAliases, Map<String, Object> model) {
 		
 		List<String> info = Lists.newArrayList();
 		List<String> errors = Lists.newArrayList();
 		
 		ImmutableList<AliasAndTarget> aliases = aliasesFrom(csvAliases);
 		for (AliasAndTarget aliasAndTarget : aliases) {
 			
 			Description content = finder.findByUri(aliasAndTarget.alias);
 			if (content != null) {
 				info.add("Not adding alias " + aliasAndTarget.alias + "  because it already exists");
 				continue;
 			}
 			
 			try { 
 				Description canonicalContent = finder.findByUri(aliasAndTarget.canonicalUri);
 				if (canonicalContent == null) {
 					errors.add("Not adding alias " + aliasAndTarget.alias + "  because the canonicalUri (" + aliasAndTarget.canonicalUri + ") can't be found");
 					continue;
 				}
 				writer.addAliases(canonicalContent.getCanonicalUri(), Sets.newHashSet(aliasAndTarget.alias));
 			} catch (Exception e) {
 				errors.add("Not adding alias " + aliasAndTarget.alias + "  because the canonicalUri (" + aliasAndTarget.canonicalUri + ") threw a Fetch Exception");
 				continue;
 			}
 			
 		}
 		
 		model.put("info", info);
 		model.put("errors", errors);
		return "/system/aliases";
 	}
 	
 	private static ImmutableList<AliasAndTarget> aliasesFrom(String csv) {
 		List<AliasAndTarget> aliases = Lists.newArrayList();
 		for (String line : csv.split("\n")) {
 			if (Strings.isNullOrEmpty(line)) {
 				continue;
 			}
 			List<String> parts = Lists.newArrayList(Splitter.on(',').trimResults().split(line));
 			if (parts.size() != 2) {
 				throw new IllegalStateException("Malformed alias file");
 			}
 			aliases.add(new AliasAndTarget(parts.get(1).trim(), parts.get(0).trim()));
 		}
 		return ImmutableList.copyOf(aliases);
 	}
 	
 	private static class AliasAndTarget {
 		
 		private final String canonicalUri;
 		private final String alias;
 		
 		public AliasAndTarget(String canonicalUri, String alias) {
 			if (!isUri(canonicalUri) || !isUri(alias)) {
 				throw new IllegalArgumentException("Malformed uri");
 			}
 			this.canonicalUri = canonicalUri;
 			this.alias = alias;
 		}
 
 		private boolean isUri(String uri) {
 			return uri.startsWith("http://");
 		}
 	}
 }
