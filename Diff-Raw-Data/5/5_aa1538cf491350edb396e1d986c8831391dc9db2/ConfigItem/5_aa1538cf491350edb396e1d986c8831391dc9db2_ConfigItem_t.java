 package pleocmd.cfg;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JComboBox;
 
 import pleocmd.Log;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.InternalException;
 import pleocmd.itfc.gui.Layouter;
 
 public class ConfigItem<E> extends ConfigValue {
 
 	private final List<String> identifiers = new ArrayList<String>();
 
 	private final boolean freeAssign;
 
 	private String content;
 
 	private JComboBox cb;
 
 	/**
 	 * Creates a new {@link ConfigItem}.
 	 * 
 	 * @param label
 	 *            name of this {@link ConfigItem} - used in GUI mode
 	 *            configuration and for configuration files
 	 * @param freeAssign
 	 *            if true any string may be assigned to this {@link ConfigItem},
 	 *            if false only the ones in the list of identifiers may be used
 	 * @param identifiers
 	 *            list of valid {@link String}s that can be used for this
 	 *            {@link ConfigItem} or - if freeAssign is true - a list of
 	 *            proposals for GUI mode configuration.
 	 */
 	public ConfigItem(final String label, final boolean freeAssign,
 			final List<E> identifiers) {
 		super(label);
 
		if (identifiers.isEmpty() && !freeAssign)
 			throw new IllegalArgumentException("list of identifiers is empty");
 		try {
 			for (final E id : identifiers) {
 				final String idStr = id.toString();
 				checkValidString(idStr, false);
 				this.identifiers.add(idStr);
 			}
 		} catch (final ConfigurationException e) {
 			throw new IllegalArgumentException(
 					"List of identifiers is invalid", e);
 		}
 
 		this.freeAssign = freeAssign;
		if (!identifiers.isEmpty()) setContentIndex(0);
 	}
 
 	public ConfigItem(final String label, final String content,
 			final List<E> identifiers) {
 		this(label, true, identifiers);
 		try {
 			setContent(content);
 		} catch (final ConfigurationException e) {
 			throw new InternalException(e);
 		}
 	}
 
 	public ConfigItem(final String label, final int contentIndex,
 			final List<E> identifiers) {
 		this(label, true, identifiers);
 		setContentIndex(contentIndex);
 	}
 
 	public final String getContent() {
 		return content;
 	}
 
 	public final void setContent(final String content)
 			throws ConfigurationException {
 		if (content == null) throw new NullPointerException("content");
 		if (!freeAssign && !identifiers.contains(content))
 			throw new ConfigurationException("Invalid constant "
 					+ "for '%s': '%s' - must be one of '%s'", getLabel(),
 					content, Arrays.toString(identifiers.toArray()));
 		checkValidString(content, false);
 		this.content = content;
 	}
 
 	public final void setContentIndex(final int content) {
 		if (content < 0 || content >= identifiers.size())
 			throw new IndexOutOfBoundsException(String.format(
 					"New content %d for '%s' must be between 0 "
 							+ "and %d for '%s'", content, getLabel(),
 					identifiers.size() - 1, Arrays.toString(identifiers
 							.toArray())));
 		this.content = identifiers.get(content);
 	}
 
 	public final List<String> getIdentifiers() {
 		return Collections.unmodifiableList(identifiers);
 	}
 
 	@Override
 	public final String asString() {
 		return content;
 	}
 
 	@Override
 	final void setFromString(final String string) throws ConfigurationException {
 		setContent(string);
 	}
 
 	@Override
 	final List<String> asStrings() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	final void setFromStrings(final List<String> strings) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	final String getIdentifier() {
 		return null;
 	}
 
 	@Override
 	final boolean isSingleLined() {
 		return true;
 	}
 
 	@Override
 	public final boolean insertGUIComponents(final Layouter lay) {
 		cb = new JComboBox(identifiers.toArray());
 		cb.setEditable(freeAssign);
 		cb.setSelectedItem(content);
 		lay.add(cb, true);
 		return false;
 	}
 
 	@Override
 	public final void setFromGUIComponents() {
 		try {
 			setContent(cb.getSelectedItem().toString());
 		} catch (final ConfigurationException e) {
 			Log.error(e, "Cannot set value '%s'", getLabel());
 		}
 	}
 
 }
