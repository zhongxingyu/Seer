 package kkckkc.syntaxpane.parse.grammar.textmate;
 
 import kkckkc.syntaxpane.parse.grammar.*;
 import kkckkc.syntaxpane.parse.grammar.util.DefaultPatternSupplier;
 import kkckkc.syntaxpane.regex.JoniPatternFactory;
 import kkckkc.syntaxpane.regex.PatternFactory;
 import kkckkc.utils.plist.GeneralPListReader;
 import kkckkc.utils.plist.PListReader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class TextmateLanguageParser {
 	private static final Comparator<SubPatternContext> SUBPATTERN_COMPARATOR = new Comparator<SubPatternContext>() {
         public int compare(SubPatternContext object1, SubPatternContext object2) {
 	        return new Integer(object1.getSubPatternIdx()).compareTo(object2.getSubPatternIdx());
         }
 	};
 	private File file;
 	private PatternFactory factory;
 	private String rootScope;
 
 	public TextmateLanguageParser(File file) {
 		this.file = file;
 
         final PatternFactory delegate = new JoniPatternFactory();
 		this.factory = new PatternFactory() {
             @Override
             public kkckkc.syntaxpane.regex.Pattern create(String s) {
                 // TODO: Investigate if this is really needed
                 s = s.replaceAll("\\\\n", "\n");
                 return delegate.create(s);
             }
         };
 	}
 
 	public Language parse() throws FileNotFoundException, IOException {
 		GeneralPListReader r = new GeneralPListReader();
 
 		Map m = (Map) r.read(file);
 		
 		Language l = new Language((String) m.get("scopeName"));
 		l.setName((String) m.get("name"));
 		
 		if (m.containsKey("firstLineMatch")) 
 			l.setFirstLinePattern(new DefaultPatternSupplier((String) m.get("firstLineMatch"), factory));
 		
 		l.setFoldStart(new DefaultPatternSupplier((String) m.get("foldingStartMarker"), factory));
 		l.setFoldEnd(new DefaultPatternSupplier((String) m.get("foldingStopMarker"), factory));
 		
 		List<String> fileTypes = (List<String>) m.get("fileTypes");
 		if (fileTypes != null && ! fileTypes.isEmpty()) {
 			StringBuilder fileTypePattern = new StringBuilder();
 			for (String s : fileTypes) {
 				fileTypePattern.append("(.*\\." + s + ")|");
 			}
 			fileTypePattern.setLength(fileTypePattern.length() - 1);
 
 			l.setFileNamePattern(new DefaultPatternSupplier(fileTypePattern.toString(), factory));
 		}
 		
 		rootScope = (String) m.get("scopeName");
 		RootContext rc = new RootContext(rootScope);
 		
 		List<Context> contexts = new ArrayList<Context>();
 		for (Map entry : ((List<Map>) m.get("patterns"))) {
 			contexts.add(parseContext(entry));
 		}
 		rc.setChildReferences(contexts.toArray(new Context[] {}));
 
 		l.setRootContext(rc);
 		
 		if (m.containsKey("repository")) {
 			contexts = new ArrayList<Context>();
 			for (Map.Entry entry : ((Map<?, ?>) m.get("repository")).entrySet()) {
 				Context c = buildContainerList((Map) entry.getValue(), (String) entry.getKey());
 				c.setId((String) entry.getKey());
 				c.setName((String) entry.getKey());
 				
 				if (c instanceof ContextList) {
 					((ContextList) c).setBegin(null);
 				} else {
 					throw new RuntimeException("Unsupported context type " + c);
 				}
 				contexts.add(c);
 			}
 			l.setSupportingContexts(contexts.toArray(new Context[] {}));
 		}
 		
 		return l;
 	}
 	
 	private Context parseContext(Map entry) {
 		if (entry.containsKey("match")) {
 			return parseSimpleContext(entry);
 		} else if (entry.containsKey("include")) {
 			return parseIncludeContext(entry);
 		} else if (entry.containsKey("begin")) {
 			return parseContainerContext(entry);
 		} else {
 			throw new RuntimeException("Unsupported context type " + entry);
 		}
     }
 
 	private Context buildContainerList(Map entry, String id) {
 	    ContextList cc = new ContextList(factory);
 
 	    if (entry.containsKey("name") || entry.containsKey("begin") || entry.containsKey("match")) {
 	    	Context c = parseContext(entry);
 	    	if (c.getId() == null) {
 		    	c.setId(id);
 		    	c.setName(id);
 	    	}
 			cc.setChildReferences(new Context[] { c });
 	    } else {
 	    	cc.setName(id);
 	    	cc.setId(id);
 	    	
 			List<Map> patterns = (List<Map>) entry.get("patterns");
 			List<Context> contexts = new ArrayList<Context>();
 			if (patterns != null) {
 				for (Map e : patterns) {
 					contexts.add(parseContext(e));
 				}
 			}
 			
 			cc.setChildReferences(contexts.toArray(new Context[] {}));
 	    }
 	    
 	    return cc;
     }
 
 	private Context parseIncludeContext(Map entry) {
 		String ref = (String) entry.get("include");
 		
 		if (ref.startsWith("#")) {
 			return new ReferenceContext(ref.substring(1) + ":*");
 			
 		// TODO: Verify this
 		} else if (ref.equals("$self") || ref.equals("$base")) {
 			return new ReferenceContext(rootScope);
 			
 		} else {
 			
 			return new ReferenceContext(ref + ":" + ref);
 			
 		}
 	}
 
 	private Context parseContainerContext(Map entry) {
 	    ContainerContext cc = new ContainerContext(factory);
 		cc.setId((String) entry.get("name")); 
 		cc.setName((String) entry.get("name"));
 
 		if (entry.containsKey("disabled")) {
             cc.setDisabled(true);
         } else {
             cc.setDisabled(false);
         }
 
 		if (entry.containsKey("begin")) 
 			cc.setBegin(factory.create((String) entry.get("begin")));
 		if (entry.containsKey("end")) {
             cc.setEnd(factory.create(fixBackrefs((String) entry.get("end"))));
         }
 
 		List<Map> patterns = (List<Map>) entry.get("patterns");
 		List<Context> contexts = new ArrayList<Context>();
 		if (patterns != null) {
 			for (Map e : patterns) {
 				contexts.add(parseContext(e));
 			}
 		}
 
         if (entry.containsKey("contentName")) {
             ContainerContext s = new ContainerContext(factory);
             s.setId((String) entry.get("contentName"));
             s.setName((String) entry.get("contentName"));
             s.setContentNameContext(true);
 
             s.setBegin(factory.create("(?=.)"));
             if (entry.containsKey("end"))
                 s.setEnd(factory.create("(?=" + fixBackrefs((String) entry.get("end")) + ")"));
             else
                 s.setEnd(factory.create("(?=.)"));
 
             s.setChildReferences(contexts.toArray(new Context[] {}));
 
             contexts.clear();
             contexts.add(s);
         }
 
 		if (entry.containsKey("beginCaptures")) {
 			List<SubPatternContext> dest = new ArrayList<SubPatternContext>();
 			for (Map.Entry e : ((Map<?, ?>) entry.get("beginCaptures")).entrySet()) {
 				SubPatternContext spc = new SubPatternContext();
 				spc.setSubPattern((String) e.getKey());
 				spc.setWhere(SubPatternContext.Where.START);
 				spc.setId((String) ((Map) e.getValue()).get("name")); 
 				dest.add(spc);
 			}
 			Collections.sort(dest, SUBPATTERN_COMPARATOR);
 			contexts.addAll(dest);
 		}
 		
 		if (entry.containsKey("endCaptures")) {
 			List<SubPatternContext> dest = new ArrayList<SubPatternContext>();
 			for (Map.Entry e : ((Map<?, ?>) entry.get("endCaptures")).entrySet()) {
 				SubPatternContext spc = new SubPatternContext();
 				spc.setSubPattern((String) e.getKey());
 				spc.setWhere(SubPatternContext.Where.END);
 				spc.setId((String) ((Map) e.getValue()).get("name")); 
 				dest.add(spc);
 			}
 			Collections.sort(dest, SUBPATTERN_COMPARATOR);
 			contexts.addAll(dest);
 		}
 
         if (entry.containsKey("captures")) {
             List<SubPatternContext> dest = new ArrayList<SubPatternContext>();
             for (Map.Entry e : ((Map<?, ?>) entry.get("captures")).entrySet()) {
                 SubPatternContext spc = new SubPatternContext();
                 spc.setSubPattern((String) e.getKey());
                 spc.setWhere(SubPatternContext.Where.START);
                 spc.setId((String) ((Map) e.getValue()).get("name"));
                 dest.add(spc);
 
                 spc = new SubPatternContext();
                 spc.setSubPattern((String) e.getKey());
                 spc.setWhere(SubPatternContext.Where.END);
                 spc.setId((String) ((Map) e.getValue()).get("name"));
                 dest.add(spc);
             }
             Collections.sort(dest, SUBPATTERN_COMPARATOR);
             contexts.addAll(dest);
         }
 
 		cc.setChildReferences(contexts.toArray(new Context[] {}));
 		
 	    return cc;
     }
 
     Pattern CHANGE_BACKREF = Pattern.compile("\\\\([0-9]+)");
     private String fixBackrefs(String s) {
        return CHANGE_BACKREF.matcher(s).replaceAll("\\\\%{$1@start}");
     }
 
     private Context parseSimpleContext(Map entry) {
 		SimpleContext sc = new SimpleContext();
 		sc.setId((String) entry.get("name")); 
 		sc.setName((String) entry.get("name"));
 		sc.setPattern(factory.create((String) entry.get("match")));
 
 		if (entry.containsKey("captures")) {
 			List<SubPatternContext> contexts = new ArrayList<SubPatternContext>();
 			for (Map.Entry e : ((Map<?, ?>) entry.get("captures")).entrySet()) {
 				SubPatternContext spc = new SubPatternContext();
 				spc.setSubPattern((String) e.getKey());
 				spc.setId((String) ((Map) e.getValue()).get("name")); 
 				contexts.add(spc);
 			}
 			Collections.sort(contexts, SUBPATTERN_COMPARATOR);
 			sc.setChildReferences(contexts.toArray(new Context[] {}));
 		}
 		
 		return sc;
     }
 	
 	private static void recurse(File file, PListReader r) throws FileNotFoundException, IOException {
 		for (File f : file.listFiles()) {
 			if (f.getName().endsWith(".plist") || f.getName().endsWith(".tmLanguage")) {
 				if (file.getName().equals("Syntaxes")) {
 					System.out.println(f);
 					TextmateLanguageParser lp = new TextmateLanguageParser(f);
 					System.out.println(lp.parse().getLanguageId());
 				}
 			} else if (f.isDirectory()) {
 				recurse(f, r);
 			}
 		}
 	}
 }
