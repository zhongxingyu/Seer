 package org.bh.gui.swing.comp;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.JCheckBox;
 
 import org.apache.log4j.Logger;
 import org.bh.data.types.IValue;
 import org.bh.data.types.IntegerValue;
 import org.bh.data.types.StringValue;
 import org.bh.gui.CompValueChangeManager;
 import org.bh.gui.IBHModelComponent;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.validation.ValidationRule;
 
 /**
  * 
  * <p>
  * This class extends the Swing <code>JCheckBox</code> .
  * 
  * @author Kharitonov.Anton
  * @version 0.1, 2010/01/07
  * 
  * @author Norman
  * @version 0.2, 16.01.2010
  * 
  */
 
 @SuppressWarnings("serial")
 public class BHCheckBox extends JCheckBox implements IBHModelComponent,
 		IPlatformListener {
 
 	static final Logger log = Logger.getLogger(BHCheckBox.class);
 	static final ITranslator translator = Services.getTranslator();
 
 	/**
 	 * unique key to identify Label.
 	 */
 	String key;
 	String hint;
 
 	boolean changeListenerEnabled = true;
 	final CompValueChangeManager valueChangeManager = new CompValueChangeManager();
 
 	/**
 	 * Constructor to create new <code>BHCheckBox</code>. with key based text
 	 * 
 	 * @param key
 	 *            unique key
 	 */
 	public BHCheckBox(final Object key) {
 		super();
 		this.key = key.toString();
		reloadText(); //unnötiger doppelter Aufruf entfernt, da es Probleme in
		// mit der Anzeige in den Einstellungen gab, was den Text der
		// chbanimation gab
 		Services.addPlatformListener(this);
 		addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (changeListenerEnabled) {
 					valueChangeManager
 							.fireCompValueChangeEvent(BHCheckBox.this);
 				}
 			}
 		});
 	}
 
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	@Override
 	public String getHint() {
 		return hint;
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			reloadText();
 		}
 	}
 
 	@Override
 	public ValidationRule[] getValidationRules() {
 		return new ValidationRule[0];
 	}
 
 	@Override
 	public IValue getValue() {
 		return new IntegerValue(isSelected() ? 1 : 0);
 	}
 
 	@Override
 	public CompValueChangeManager getValueChangeManager() {
 		return valueChangeManager;
 	}
 
 	@Override
 	public void setValidationRules(ValidationRule[] validationRules) {
 		// noop
 	}
 
 	@Override
 	public void setValue(IValue value) {
 		if (value == null) {
 			setSelected(false);
 			return;
 		} else if (value instanceof IntegerValue) {
 			int valInt = ((IntegerValue) value).getValue();
 			changeListenerEnabled = false;
 			setSelected(valInt >= 1);
 			changeListenerEnabled = true;
 		} else if (value instanceof StringValue) {
 			String valStr = ((StringValue) value).getString();
 			changeListenerEnabled = false;
 			setSelected(valStr.toLowerCase().equals("true"));
 			changeListenerEnabled = false;
 		}
 	}
 
 	protected void reloadText() {
 		hint = Services.getTranslator().translate(key, ITranslator.LONG);
 		setToolTipText(hint);
		setText(translator.translate(key));
 	}
 
 	@Override
 	public String toString() {
 		return translator.translate(key);
 	}
 }
