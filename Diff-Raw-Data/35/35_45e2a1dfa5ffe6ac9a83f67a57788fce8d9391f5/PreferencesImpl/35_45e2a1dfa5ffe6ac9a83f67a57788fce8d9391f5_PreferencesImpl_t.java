 package org.svaor.preferences;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.LinkedList;
 import java.util.prefs.AbstractPreferences;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 import static com.google.common.collect.Lists.newLinkedList;
 import static org.fluentjava.FluentUtils.cast;
 
 /** @author Smelik Evgeniy */
 public class PreferencesImpl extends AbstractPreferences {
 	private static final Log log = LogFactory.getLog(PreferencesImpl.class);
 
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

 	private Long nodeId;
 	@Nullable
 	private String username;
 
 	//spring
 	private static PreferencesProvider preferencesProvider;
 
 	private PreferencesImpl(AbstractPreferences parent, @NotNull String nodeName, @Nullable String username, @NotNull Long nodeId) {
 		super(parent, nodeName);
 
 		this.username = username;
 		this.nodeId = nodeId;
 	}
 
 	private PreferencesImpl(AbstractPreferences parent, @NotNull String nodeName, @Nullable String username) {
 		super(parent, nodeName);
 
 		this.username = username;
 	}
 
 	public static Preferences getUserRoot(String username) {
 		if (log.isDebugEnabled()) {
 			log.debug("Getting root preferences for user [" + username + ']');
 		}
 
 		final Long rootNodeId = preferencesProvider.loadOrCreateRootNode(username);
 
 		return new PreferencesImpl(null, "", username, rootNodeId);
 	}
 
 	public static Preferences getSystemRoot() {
 		log.debug("Getting preferences for system root");
 
 		final Long rootNodeId = preferencesProvider.loadOrCreateRootNode(null);
 		return new PreferencesImpl(null, "", null, rootNodeId);
 	}
 
 	@Override
 	protected void putSpi(@NotNull String key, @NotNull String value) {
 		final LinkedList<String> nodes = newLinkedList();
 		Preferences preferences = this;
 		while (true) {
 			nodes.addFirst(preferences.name());
 
 			final Preferences parent = preferences.parent();
 			if (parent.parent() == null) {
 				break;
 			} else {
 				preferences = parent;
 			}
 		}
 
 		final PreferencesImpl rootPreferences = cast(preferences.parent());
 		final Long rootNodeId = rootPreferences.getNodeId();
 		assert rootNodeId != null;
 		final Long[] ids = preferencesProvider.setProperty(rootNodeId, nodes.toArray(new String[nodes.size()]), key, value);
 
 		PreferencesImpl preferencesImpl = this;
 		for (int i = ids.length - 1; i >= 0; i--) {
 			preferencesImpl.setNodeId(ids[i]);
 			preferencesImpl = cast(preferencesImpl.parent());
 		}
 	}
 
 	@Nullable
 	@Override
 	protected String getSpi(@NotNull String key) {
		if (nodeId == null) {
			return null;
		}
 		return preferencesProvider.getProperty(nodeId, key);
 	}
 
 	@Override
 	protected void removeSpi(@NotNull String key) {
 		preferencesProvider.removeProperty(nodeId, key);
 	}
 
 	@Override
 	protected void removeNodeSpi() throws BackingStoreException {
 		preferencesProvider.removeNode(nodeId);
 	}
 
 	@Override
 	protected String[] keysSpi() throws BackingStoreException {
		if (nodeId == null) {
			return EMPTY_STRING_ARRAY;
		}
 		return preferencesProvider.getPropertyKeys(nodeId);
 	}
 
 	@Override
 	protected String[] childrenNamesSpi() throws BackingStoreException {
		if (nodeId == null) {
			return EMPTY_STRING_ARRAY;
		}
 		return preferencesProvider.getNodeNames(nodeId);
 	}
 
 	@Override
 	protected AbstractPreferences childSpi(@NotNull String name) {
 		if (nodeId == null) {
 			// Probably nodes have not been saved yet. We will do this later by saving appropriate property
 			return new PreferencesImpl(this, name, username);
 		} else {
 			final Long childNodeId = preferencesProvider.loadNode(nodeId, name);
 			if (childNodeId == null) {
 				return new PreferencesImpl(this, name, username);
 			} else {
 				return new PreferencesImpl(this, name, username, childNodeId);
 			}
 		}
 	}
 
 	@Override
 	protected void syncSpi() throws BackingStoreException {
 	}
 
 	@Override
 	protected void flushSpi() throws BackingStoreException {
 	}
 
 	@Nullable
 	public Long getNodeId() {
 		return nodeId;
 	}
 
 	public void setNodeId(Long nodeId) {
 		this.nodeId = nodeId;
 	}
 
 	public static void setPreferencesProvider(PreferencesProvider preferencesProvider) {
 		PreferencesImpl.preferencesProvider = preferencesProvider;
 	}
 }
