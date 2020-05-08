 package me.josvth.bukkitformatlibrary.managers;
 
 import me.josvth.bukkitformatlibrary.FormattedMessage;
 import me.josvth.bukkitformatlibrary.formatter.Formatter;
 import me.josvth.bukkitformatlibrary.formatter.FormatterGroup;
 import org.bukkit.configuration.ConfigurationSection;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class YamlFormatManager extends FormatManager {
 
 	public YamlFormatManager() {
 		super();
 	}
 
 	public YamlFormatManager(FormatManager manager, boolean includeMessages) {
 		super(manager, includeMessages);
 	}
 
 	public YamlFormatManager(ConfigurationSection formattersSection, ConfigurationSection messageSection, String groupIdentifier) {
 		loadFormatters(formattersSection);
 		loadMessages(messageSection, groupIdentifier);
 	}
 
 	public void loadFormatters(ConfigurationSection section) {
 
 		Set<String> unloadedFormatters = section.getKeys(false);
 
 		int iteration = 0;
 
 		while (iteration < 10) {
 
 			Iterator<String> iterator = unloadedFormatters.iterator();
 
 			while (iterator.hasNext()) {
 
 				String name = iterator.next();
 
 				String type = getType(section, name, null);
 
 				if ("group".equalsIgnoreCase(type)) {
 
 					try {
 
 						List<String> formatterNames = getFormatterNames(section, name, null);
 
 						if (formatterNames != null) {
 
 							// Check if all groups formatters are loaded
 							if (this.formatters.keySet().containsAll(formatterNames)) {
 
								List<Formatter> groupFormatters = new ArrayList<Formatter>();
 
 								// Make formatter list
 								for (String formatter : formatterNames) {
									groupFormatters.add(getFormatter(formatter));
 								}
 
 								// Add group formatter
								formatters.put(name, new FormatterGroup(name, groupFormatters));
 
 								// Remove from unloaded
 								iterator.remove();
 
 							}
 
 						} else {
 
 							// Remove from unloaded
 							iterator.remove();
 
 						}
 
 
 					} catch (IllegalArgumentException e) {
 						e.printStackTrace();
 					}
 
 				} else if (type != null) {
 
 					Class<? extends Formatter> clazz = getRegisteredFormatter(type);
 
 					if (clazz != null) {
 
 						try {
 
 							Formatter formatter = clazz.getConstructor(String.class, Map.class).newInstance(name, getSettings(section, name, null));
 
 							addFormatter(formatter);
 
 
 
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 
 					}
 
 					// Remove from unloaded
 					iterator.remove();
 
 				}
 
 			}
 
 			iteration++;
 
 		}
 
 	}
 
 	private List<String> getFormatterNames(ConfigurationSection section, String groupName, Set<String> children) throws IllegalArgumentException {
 
 		List<String> formatters = null;
 
 		if (section.isString(groupName + ".parent")) {
 			if (children == null) children = new HashSet<String>();
 			children.add(groupName);
 			formatters = getFormatterNames(section, groupName + ".parent", children);
 		}
 
 		List<String> groupFormatters = section.getStringList(groupName + ".formatters");
 
 		if (groupFormatters != null) {
 			if (formatters == null) {
 				formatters = groupFormatters;
 			} else {
 				formatters.addAll(groupFormatters);
 			}
 		}
 
 		if (children != null) {
 			for (String formatter : formatters) {
 				if (children.contains(formatter))
 					throw new IllegalArgumentException(formatter + " loops with " + groupName + "!");
 			}
 		}
 
 		return formatters;
 
 	}
 
 	private String getType(ConfigurationSection section, String formatterName, Set<String> children) throws IllegalArgumentException {
 
 		String type = section.getString(formatterName + ".type");
 
 		if (type != null)
 			return type;
 
 		String parentName = section.getString(formatterName + ".parent");
 
 		if (parentName != null) {
 
 			if (children == null) {
 				children = new HashSet<String>();
 			} else {
 				for (String child : children)
 					if (parentName.equals(child))
 						throw new IllegalArgumentException(formatterName + " loops with " + parentName);
 			}
 
 			children.add(formatterName);
 
 			type = getType(section, parentName, children);
 
 		}
 
 		return type;
 
 	}
 
 	private Map<String, Object> getSettings(ConfigurationSection section, String formatterName, Set<String> children) throws IllegalArgumentException {
 
 		Map<String, Object> settings = null;
 
 		String parentName = section.getString(formatterName + ".parent");
 
 		if (parentName != null) {
 
 			if (children == null) {
 				children = new HashSet<String>();
 			} else {
 				for (String child : children)
 					if (parentName.equals(child))
 						throw new IllegalArgumentException(formatterName + " loops with " + parentName);
 			}
 
 			children.add(formatterName);
 
 			settings = getSettings(section, parentName, children);
 
 		}
 
 		if (section.isConfigurationSection(formatterName + ".settings")) {
 			if (settings == null) {
 				settings = section.getConfigurationSection(formatterName + ".settings").getValues(false);
 			} else {
 				settings.putAll(section.getConfigurationSection(formatterName + ".settings").getValues(false));
 			}
 		}
 
 		return settings;
 
 	}
 
 	public void loadMessages(ConfigurationSection section, String groupIdentifier) {
 
 		Pattern pattern = Pattern.compile(String.format("%s[^ ]+%s", groupIdentifier, groupIdentifier));
 
 		for (String key : section.getKeys(true)) {
 			if (section.isString(key)) {
 
 				String message = section.getString(key);
 
 				// Check if we can pre format this message
 				Matcher matcher = pattern.matcher(message);
 
 				if (matcher.lookingAt()) {
 
 					String[] formatterNames = matcher.group().split(Pattern.quote("|"));
 
 					// We take the rest of the string as the message
 					message = message.substring(matcher.end());
 
 					String preFormat = null;
 
 					// We pre-format the message with the groups
 					for (String formatterName : formatterNames) {
 
 						Formatter formatter = getFormatter(formatterName);
 
 						if (formatter != null) {
 
 							// Only if we found a valid group we initialize the variable
 							if (preFormat == null) preFormat = message;
 
 							// We pre-format the message following this group
 							preFormat = formatter.format(preFormat);
 
 						}
 
 					}
 
 					// Only if we could pre-format the message we add it
 					if (preFormat != null)
 						addPreFormattedMessage(key, new FormattedMessage(preFormat));
 
 				}
 
 				addMessage(key, message);
 
 			}
 		}
 
 	}
 
 }
