 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.common;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.text.JTextComponent;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import cz.cuni.mff.peckam.java.origamist.common.LangString;
 import cz.cuni.mff.peckam.java.origamist.common.jaxb.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.configuration.Configuration;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 import cz.cuni.mff.peckam.java.origamist.utils.UniversalDocumentListener;
 
 /**
  * A text field allowing the user to input translations of a string in various locales.
  * 
  * @param T The type of the text field to be used.
  * 
  * @author Martin Pecka
  */
 public class JLangStringListTextField<T extends JTextComponent> extends JPanel
 {
 
     /** */
     private static final long  serialVersionUID  = -3484876190164726267L;
 
     /** The list of translatable strings. */
     protected List<LangString> strings;
 
     /** The list of available locales. */
     protected JLocaleComboBox  locales;
 
     /** The textual value of the currently chosen locale. */
     protected T                value;
 
     /** If not <code>null</code>, the textField will be added to the scrollPane. */
     protected JScrollPane      scrollPane;
 
     /** It <code>true</code>, the locale change event is currently being processed. */
     protected boolean          isLocaleChanging  = false;
 
     /** It <code>true</code>, <code>locales</code> combobox is currently updating its item order. */
     protected boolean          isUpdatingLocales = false;
 
     /**
      * @param strings The list of translatable strings.
      * @param textField The component to be used for typing in the translations.
      */
     public JLangStringListTextField(List<LangString> strings, T textField)
     {
         this(strings, textField, null);
     }
 
     /**
      * @param strings The list of translatable strings.
      * @param textField The component to be used for typing in the translations.
      * @param scrollPane If not <code>null</code>, the textField will be added to the scrollPane.
      */
     public JLangStringListTextField(List<LangString> strings, T textField, JScrollPane scrollPane)
     {
         if (strings == null)
             throw new NullPointerException("Cannot create JLangStringListTextField with a null list.");
 
         this.strings = strings;
 
         this.scrollPane = scrollPane;
 
         setLayout(new FormLayout("0px:grow,$lcgap,pref", "pref"));
 
         CellConstraints cc = new CellConstraints();
         value = textField;
         if (scrollPane == null) {
             add(value, cc.xy(1, 1));
         } else {
             scrollPane.setViewportView(value);
             add(scrollPane, cc.xy(1, 1));
         }
 
         locales = new JLocaleComboBox();
         updateLocales();
         add(locales, cc.xy(3, 1));
 
         locales.addPopupMenuListener(new PopupMenuListener() {
             @Override
             public void popupMenuWillBecomeVisible(PopupMenuEvent e)
             {
             }
 
             @Override
             public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
             {
                 value.requestFocusInWindow();
             }
 
             @Override
             public void popupMenuCanceled(PopupMenuEvent e)
             {
                 value.requestFocusInWindow();
             }
         });
 
         locales.setAction(new AbstractAction() {
             /** */
             private static final long serialVersionUID = -5477415299077842204L;
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (isUpdatingLocales)
                     return;
 
                 isLocaleChanging = true;
                 Locale l = (Locale) locales.getSelectedItem();
                 LangString string = getLangString(l);
                 if (string == null) {
                     value.setText("");
                 } else {
                     value.setText(string.getValue());
                 }
                 isLocaleChanging = false;
             }
         });
         locales.setSelectedIndex(0);
 
         final DocumentListener textChangeListener = new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 if (isLocaleChanging)
                     return;
 
                 LangString string = getLangString((Locale) locales.getSelectedItem());
                 if (string == null) {
                     string = (LangString) new ObjectFactory().createLangString();
                     string.setLang((Locale) locales.getSelectedItem());
                     string.setValue("");
                     JLangStringListTextField.this.strings.add(string);
                     updateLocales();
                 }
 
                 if (value.getText() != null && value.getText().length() > 0) {
                     string.setValue(value.getText());
                 } else {
                     JLangStringListTextField.this.strings.remove(string);
                     updateLocales();
                 }
             }
         };
 
         value.getDocument().addDocumentListener(textChangeListener);
         value.addPropertyChangeListener("document", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 value.getDocument().addDocumentListener(textChangeListener);
             }
         });
 
         setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
     }
 
     /**
      * @return The locales that should be topped. The default implementation returns the application and diagram locales
      *         from user settings along with all locales for which an entry exists in this input.
      */
     protected LinkedHashSet<Locale> getSuggestedLocales()
     {
         LinkedHashSet<Locale> result = new LinkedHashSet<Locale>(2);
 
         Configuration conf = ServiceLocator.get(ConfigurationManager.class).get();
 
         boolean isSetForLocale = false, isSetForDiagramLocale = false;
 
         Locale locale = conf.getLocale();
         for (LangString s : strings) {
             if (s.getLang().equals(locale)) {
                 isSetForLocale = true;
                 break;
             }
         }
 
         Locale diagramLocale = conf.getDiagramLocale();
         if (diagramLocale.equals(locale)) {
             isSetForDiagramLocale = isSetForLocale;
         } else {
             for (LangString s : strings) {
                 if (s.getLang().equals(diagramLocale)) {
                     isSetForDiagramLocale = true;
                     break;
                 }
             }
         }
 
         if (isSetForLocale)
             result.add(conf.getLocale());
         if (isSetForDiagramLocale)
             result.add(conf.getDiagramLocale());
 
         for (LangString s : strings)
             result.add(s.getLang());
 
         if (!isSetForLocale)
             result.add(conf.getLocale());
         if (!isSetForDiagramLocale)
             result.add(conf.getDiagramLocale());
 
         return result;
     }
 
     /**
      * Updates the combobox with locales to have the suggested locales moved to the top.
      */
     protected void updateLocales()
     {
         isUpdatingLocales = true;
         locales.updateSuggested(getSuggestedLocales());
         isUpdatingLocales = false;
     }
 
     /**
      * Return the {@link LangString} from <code>strings</code> for the given {@link Locale}.
      * 
      * @param l The locale which to find the {@link LangString} for.
      * @return The {@link LangString} from <code>strings</code> for the given {@link Locale} or <code>null</code> if the
      *         list doesn't contain any {@link LangString} for the given {@link Locale}.
      */
     protected LangString getLangString(Locale l)
     {
         for (LangString s : strings) {
             if (s.getLang().equals(l))
                 return s;
         }
         return null;
     }
 
     /**
      * @return the strings
      */
     public List<LangString> getStrings()
     {
         return strings;
     }
 
     /**
      * @param strings the strings to set
      */
     public void setStrings(List<LangString> strings)
     {
         this.strings = strings;
         updateLocales();
         locales.setSelectedIndex(0);
     }
 
     /**
      * @return the locales
      */
     public JLocaleComboBox getComboBox()
     {
         return locales;
     }
 
     /**
      * @return the value
      */
     public T getTextField()
     {
         return value;
     }
 
     /**
      * @return the scrollPane
      */
     public JScrollPane getScrollPane()
     {
         return scrollPane;
     }
 
     @Override
     public void setEnabled(boolean enabled)
     {
         super.setEnabled(enabled);
         locales.setEnabled(enabled);
         value.setEnabled(enabled);
         if (scrollPane != null)
             scrollPane.setEnabled(enabled);
     }
 }
