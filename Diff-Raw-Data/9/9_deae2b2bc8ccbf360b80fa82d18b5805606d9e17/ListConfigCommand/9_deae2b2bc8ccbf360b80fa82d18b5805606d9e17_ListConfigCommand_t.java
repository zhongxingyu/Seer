 package org.pescuma.buildhealth.cli.commands.config;
 
 import static com.google.common.base.Strings.*;
 import static java.lang.Math.*;
 import io.airlift.command.Command;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.pescuma.buildhealth.analyser.BuildHealthAnalyser;
 import org.pescuma.buildhealth.analyser.BuildHealthAnalyserPreference;
 import org.pescuma.buildhealth.cli.BuildHealthCliCommand;
 import org.pescuma.buildhealth.prefs.Preferences;
 
 @Command(name = "list", description = "List report preferences")
 public class ListConfigCommand extends BuildHealthCliCommand {
 	
 	private static final String PREFIX = "    ";
 	
 	// TODO
 	// @Arguments(title = "key", description = "only show keys starting with this value")
 	// public List<String> args;
 	
 	@Override
 	public void execute() {
 		Preferences prefs = buildHealth.getPreferences();
 		
 		Set<String[]> keys = new TreeSet<String[]>(getKeyComparator());
 		keys.addAll(prefs.getKeys());
 		
 		List<BuildHealthAnalyser> analysers = buildHealth.getAnalysers();
 		analysers = sortAnalysers(analysers);
 		
 		StringBuilder out = new StringBuilder();
 		
 		boolean needNewLine = false;
 		for (BuildHealthAnalyser analyser : analysers) {
 			List<BuildHealthAnalyserPreference> ps = analyser.getPreferences();
 			if (ps.isEmpty())
 				continue;
 			
 			boolean hasName = !isNullOrEmpty(analyser.getName());
 			
 			if (needNewLine)
 				out.append("\n");
 			needNewLine = hasName;
 			
 			String prefix = "";
 			if (hasName) {
 				out.append(prefix).append(analyser.getName()).append(":\n");
 				prefix = PREFIX;
 			}
 			
 			for (BuildHealthAnalyserPreference pref : ps) {
 				out.append(prefix);
 				append(out, prefs, pref.getKey(), pref.getDefVal(), pref.getDescription());
 				keys.remove(pref.getKey());
 			}
 		}
 		
 		if (needNewLine && !keys.isEmpty())
 			out.append("\n");
 		
 		for (String[] key : keys)
 			append(out, prefs, key, "<no default value>", "");
 		
 		if (out.length() < 1)
 			out.append("No preferences set");
 		
 		System.out.print(out.toString());
 	}
 	
 	private void append(StringBuilder out, Preferences prefs, String[] key, String defVal, String description) {
 		for (int i = 0; i < key.length; i++) {
 			if (i > 0)
 				out.append(" ");
 			out.append(key[i]);
 		}
 		out.append(" = ");
 		out.append(get(prefs, key, defVal));
 		if (!description.isEmpty())
 			out.append(" [").append(description).append("]");
 		out.append("\n");
 	}
 	
 	private List<BuildHealthAnalyser> sortAnalysers(List<BuildHealthAnalyser> analysers) {
 		analysers = new ArrayList<BuildHealthAnalyser>(analysers);
 		
 		Collections.sort(analysers, new Comparator<BuildHealthAnalyser>() {
 			@Override
 			public int compare(BuildHealthAnalyser o1, BuildHealthAnalyser o2) {
 				String n1 = nullToEmpty(o1.getName());
 				String n2 = nullToEmpty(o2.getName());
 				if (n1.equals(n2))
 					return 0;
 				if (n1.isEmpty())
 					return 1;
 				if (n2.isEmpty())
 					return -1;
 				return n1.compareToIgnoreCase(n2);
 			}
 		});
 		
 		return analysers;
 	}
 	
 	private String get(Preferences prefs, String[] key, String defVal) {
 		for (int i = 0; i < key.length - 1; i++)
 			prefs = prefs.child(key[i]);
 		
 		return prefs.get(key[key.length - 1], defVal);
 	}
 	
 	private Comparator<String[]> getKeyComparator() {
 		return new Comparator<String[]>() {
 			@Override
 			public int compare(String[] o1, String[] o2) {
 				int size = min(o1.length, o2.length);
 				for (int i = 0; i < size; i++) {
					int comp = o1[i].compareTo(o2[i]);
 					if (comp != 0)
 						return comp;
 				}
 				return o1.length - o2.length;
 			}
 		};
 	}
 }
