 package pleocmd.cfg;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.swing.JLabel;
 
 import pleocmd.itfc.gui.Layouter;
 import pleocmd.itfc.gui.Layouter.Button;
 
 public abstract class ConfigMap<K, V> extends ConfigValue {
 
 	private final Map<K, List<V>> content;
 
 	private JLabel shortDescription;
 
 	public ConfigMap(final String label) {
 		super(label);
 		content = new HashMap<K, List<V>>();
 	}
 
 	public final Set<K> getAllKeys() {
 		return Collections.unmodifiableSet(content.keySet());
 	}
 
 	public final List<K> getAllKeysSorted(final Comparator<? super K> comp) {
 		final List<K> list = new ArrayList<K>(content.size());
 		for (final K k : content.keySet())
 			list.add(k);
 		Collections.sort(list, comp);
 		return list;
 	}
 
 	public final List<V> getContent(final K key) {
 		final List<V> list = content.get(key);
 		return Collections.unmodifiableList(list != null ? list
 				: new ArrayList<V>(0));
 	}
 
 	public final boolean hasContent(final K key) {
 		return content.containsKey(key);
 	}
 
 	public final void setContent(final K key, final List<V> list) {
 		content.put(key, list);
 	}
 
 	public final void renameContent(final K key, final K newKey) {
 		final List<V> list = content.remove(key);
 		if (list == null)
 			throw new IllegalArgumentException(String.format(
 					"Key '%s' was not in the map", key));
 		content.put(newKey, list);
 	}
 
 	public final void addContent(final K key, final V value) {
 		List<V> list = content.get(key);
 		if (list == null) {
 			list = new ArrayList<V>();
 			content.put(key, list);
 		}
 		list.add(value);
 	}
 
 	public final void createContent(final K key) {
 		if (!content.containsKey(key)) content.put(key, new ArrayList<V>());
 	}
 
 	public final void removeContent(final K key) {
 		content.remove(key);
 	}
 
 	public final void clearContent(final K key) {
 		final List<V> list = content.get(key);
 		if (list != null) list.clear();
 	}
 
 	public final void clearContent() {
 		content.clear();
 	}
 
 	public final <F extends K, W extends V> void assignFrom(
 			final ConfigMap<F, W> map) {
 		clearContent();
		for (final F key : map.getAllKeys())
 			for (final W v : map.getContent(key))
 				addContent(key, v);
 	}
 
 	@Override
 	final String asString() {
 		final StringBuilder sb = new StringBuilder("[");
 		for (final Entry<K, List<V>> entry : content.entrySet()) {
 			sb.append(entry.getKey());
 			sb.append(": ");
 			sb.append(entry.getValue().size());
 			sb.append("x, ");
 		}
 		if (!content.isEmpty()) {
 			sb.deleteCharAt(sb.length() - 1);
 			sb.deleteCharAt(sb.length() - 1);
 		}
 		sb.append("]");
 		return sb.toString();
 	}
 
 	@Override
 	final void setFromString(final String string) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	final List<String> asStrings() {
 		final List<String> res = new ArrayList<String>();
 		for (final Entry<K, List<V>> entry : content.entrySet()) {
 			res.add(entry.getKey() + " =>");
 			for (final V s : entry.getValue())
 				res.add("\t" + s);
 		}
 		return res;
 	}
 
 	@Override
 	final void setFromStrings(final List<String> strings)
 			throws ConfigurationException {
 		content.clear();
 		List<V> list = null;
 		for (final String str : strings)
 			if (str.endsWith("=>")) {
 				list = new ArrayList<V>();
 				content.put(
 						createKey(str.substring(0, str.length() - 2).trim()),
 						list);
 			} else {
 				if (list == null)
 					throw new ConfigurationException("Found list entries "
 							+ "before the first key");
 				list.add(createValue(str));
 			}
 	}
 
 	protected abstract K createKey(String keyAsString)
 			throws ConfigurationException;
 
 	protected abstract V createValue(String valueAsString)
 			throws ConfigurationException;
 
 	protected abstract void modifiyMapViaGUI();
 
 	@Override
 	final String getIdentifier() {
 		return "map";
 	}
 
 	@Override
 	final boolean isSingleLined() {
 		return false;
 	}
 
 	@Override
 	public final void insertGUIComponents(final Layouter lay) {
 		lay.add(shortDescription = new JLabel(asString()), false);
 		lay.addButton(Button.Modify, "Modifies the map", new Runnable() {
 			@Override
 			public void run() {
 				modifiyMapViaGUI();
 				getShortDescription().setText(asString());
 			}
 		});
 	}
 
 	@Override
 	public final void setFromGUIComponents() {
 		// handled internally
 	}
 
 	public final JLabel getShortDescription() {
 		return shortDescription;
 	}
 
 }
