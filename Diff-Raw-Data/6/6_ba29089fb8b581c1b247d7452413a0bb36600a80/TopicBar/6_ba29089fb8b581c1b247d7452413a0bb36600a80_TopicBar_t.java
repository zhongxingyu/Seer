 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components;
 
 import com.dmdirc.Channel;
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.Topic;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.ReplacePasteAction;
 import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
 import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
 import com.dmdirc.addons.ui_swing.components.inputfields.TextPaneInputField;
 import com.dmdirc.addons.ui_swing.components.text.WrapEditorKit;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.TopicChangeListener;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.plugins.PluginManager;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.messages.ColourManager;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.util.annotations.factory.Factory;
 import com.dmdirc.util.annotations.factory.Unbound;
 
 import com.google.common.base.Optional;
 
 import java.awt.Color;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Component to show and edit topics for a channel.
  */
 @Factory(inject = true, singleton = true)
 public class TopicBar extends JComponent implements ActionListener, ConfigChangeListener,
         MouseListener, DocumentListener, TopicChangeListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1;
     /** Topic text. */
     private final TextPaneInputField topicText;
     /** Edit button. */
     private final JButton topicEdit;
     /** Cancel button. */
     private final JButton topicCancel;
     /** Manager to use to resolve colours. */
     private final ColourManager colourManager;
     /** The window this topic bar is for. */
     private final ChannelFrame window;
     /** Associated channel. */
     private final Channel channel;
     /** the maximum length allowed for a topic. */
     private final int topicLengthMax;
     /** The config domain to read settings from. */
     private final String domain;
     /** Clipboard to copy and paste from. */
     private final Clipboard clipboard;
     /** Empty Attribute set. */
     private SimpleAttributeSet as;
     /** Foreground Colour. */
     private Color foregroundColour;
     /** Background Colour. */
     private Color backgroundColour;
     /** Error icon. */
     private final JLabel errorIcon;
     /** Show the topic bar? */
     private boolean showBar;
     /** Show the full topic, or truncate? */
     private boolean showFull;
     /** Hide topic bar when topic is empty? */
     private boolean hideEmpty;
 
     /**
      * Creates a new instance of {@link TopicBar}.
      *
      * @param parentWindow      The window that ultimately contains this topic bar.
      * @param globalConfig      The config provider to read settings from.
      * @param domain            The domain that settings are stored in.
      * @param colourManager     The colour manager to use for colour input.
      * @param pluginManager     The plugin manager to use for plugin information.
      * @param clipboard         The clipboard to copy and paste from
      * @param commandController The controller to use for command information.
      * @param channel           The channel that this topic bar is for.
      * @param window            The window this topic bar is for.
      * @param iconManager       The icon manager to use for this bar's icons.
      */
     public TopicBar(
             @SuppressWarnings("qualifiers") @MainWindow final Window parentWindow,
             @SuppressWarnings("qualifiers") @GlobalConfig final AggregateConfigProvider globalConfig,
             @SuppressWarnings("qualifiers") @PluginDomain(SwingController.class) final String domain,
             final ColourManager colourManager,
             final PluginManager pluginManager,
             final Clipboard clipboard,
             final CommandController commandController,
             @Unbound final Channel channel,
             @Unbound final ChannelFrame window,
             @Unbound final IconManager iconManager) {
         this.channel = channel;
         this.domain = domain;
         this.colourManager = colourManager;
         this.window = window;
         this.clipboard = clipboard;
         topicText = new TextPaneInputField(parentWindow, globalConfig, colourManager, iconManager);
         topicLengthMax = channel.getMaxTopicLength();
         updateOptions();
         errorIcon = new JLabel(iconManager.getIcon("input-error"));
         topicText.setEditorKit(new WrapEditorKit(showFull, channel.getEventBus(), window));
         ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
                 new NewlinesDocumentFilter());
 
         topicText.getActionMap().put("paste-from-clipboard",
                 new ReplacePasteAction(clipboard, "(\r\n|\n|\r)", " "));
         topicEdit = new ImageButton<>("edit",
                 iconManager.getIcon("edit-inactive"),
                 iconManager.getIcon("edit"));
         topicCancel = new ImageButton<>("cancel",
                 iconManager.getIcon("close"),
                 iconManager.getIcon("close-active"));
 
         final SwingInputHandler handler = new SwingInputHandler(
                 pluginManager, topicText, commandController, channel.getCommandParser(),
                 channel, channel.getEventBus());
         handler.setTypes(true, false, true, false);
         handler.setTabCompleter(channel.getTabCompleter());
 
         final JScrollPane sp = new JScrollPane(topicText);
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
 
         setLayout(new MigLayout("fillx, ins 0, hidemode 3"));
         add(sp, "growx, pushx");
         add(errorIcon, "");
         add(topicCancel, "");
         add(topicEdit, "");
         //Fix broken layout manager
         invalidate();
         validate();
         invalidate();
 
         channel.addTopicChangeListener(this);
         topicText.addActionListener(this);
         topicEdit.addActionListener(this);
         topicCancel.addActionListener(this);
         topicText.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enterButton");
         topicText.getActionMap().put("enterButton", new AbstractAction("enterButton") {
             /** A version number for this class. */
             private static final long serialVersionUID = 1;
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 commitTopicEdit();
             }
         });
         topicText.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escapeButton");
         topicText.getActionMap().put("escapeButton", new AbstractAction("escapeButton") {
             /** A version number for this class. */
             private static final long serialVersionUID = 1;
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 cancelTopicEdit();
             }
         });
         topicText.addMouseListener(this);
         topicText.getDocument().addDocumentListener(this);
 
         globalConfig.addChangeListener("ui", "backgroundcolour", this);
         globalConfig.addChangeListener("ui", "foregroundcolour", this);
         globalConfig.addChangeListener("ui", "inputbackgroundcolour", this);
         globalConfig.addChangeListener("ui", "inputforegroundcolour", this);
         globalConfig.addChangeListener(domain, "showfulltopic", this);
         globalConfig.addChangeListener(domain, "hideEmptyTopicBar", this);
         globalConfig.addChangeListener(domain, "showtopicbar", this);
 
         setVisible(true);
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
         setColours();
         validateTopic();
 
         final Optional<Topic> topic = channel.getCurrentTopic();
         if (topic.isPresent()) {
             topicChanged(channel, topic.get());
         }
     }
 
     @Override
     public final void topicChanged(final Channel channel, final Topic topic) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (topicText.isEditable()) {
                     return;
                 }
                 topicText.setText("");
                if (topic != null) {
                 channel.getStyliser().addStyledString(
                         (StyledDocument) topicText.getDocument(),
                         new String[]{Styliser.CODE_HEXCOLOUR
                             + UIUtilities.getHex(foregroundColour)
                                + topic.getTopic(),}, as);
                }
                 topicText.setCaretPosition(0);
                 validateTopic();
                 setVisible(false);
                 setVisible(true);
             }
         });
     }
 
     @Override
     public void setVisible(final boolean visibility) {
         if (!showBar || !visibility) {
             super.setVisible(false);
             return;
         }
         if (hideEmpty) {
             super.setVisible(topicText.getDocument().getLength() != 0);
             return;
         }
         super.setVisible(true);
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (!channel.isOnChannel()) {
             return;
         }
         if (e.getSource() == topicEdit) {
             if (topicText.isEditable()) {
                 commitTopicEdit();
             } else {
                 setupTopicEdit();
             }
         } else if (e.getSource() == topicCancel) {
             cancelTopicEdit();
         }
     }
 
     /**
      * Commits a topic edit to the parent channel.
      */
     private void commitTopicEdit() {
         final Optional<Topic> oldTopic = channel.getCurrentTopic();
         if ((!oldTopic.isPresent() && !topicText.getText().isEmpty())
                 || (oldTopic.isPresent() && !oldTopic.get().getTopic().equals(topicText.getText()))) {
             channel.setTopic(topicText.getText());
         }
 
         final Optional<Topic> newTopic = channel.getCurrentTopic();
         window.getInputField().requestFocusInWindow();
         if (newTopic.isPresent()) {
             topicText.setText(newTopic.get().getTopic());
         } else {
             topicText.setText("");
         }
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
     }
 
     /**
      * Sets the topic ready to be edited, changing attributes and focus.
      */
     private void setupTopicEdit() {
         topicText.setVisible(false);
         topicText.setText("");
         final Optional<Topic> topic = channel.getCurrentTopic();
         if (topic.isPresent()) {
             topicText.setText(topic.get().getTopic());
         }
         applyAttributes();
         topicText.setCaretPosition(0);
         topicText.setFocusable(true);
         topicText.setEditable(true);
         topicText.setVisible(true);
         topicText.requestFocusInWindow();
         topicCancel.setVisible(true);
     }
 
     /**
      * Cancels a topic edit, resetting focus and button states.
      */
     private void cancelTopicEdit() {
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
         window.getInputField().requestFocusInWindow();
         topicChanged(channel, null);
     }
 
     /**
      * Load and set colours.
      */
     private void setColours() {
         backgroundColour = UIUtilities.convertColour(
                 colourManager.getColourFromString(
                         channel.getConfigManager().getOptionString(
                                 "ui", "inputbackgroundcolour",
                                 "ui", "backgroundcolour"), null));
         foregroundColour = UIUtilities.convertColour(
                 colourManager.getColourFromString(
                         channel.getConfigManager().getOptionString(
                                 "ui", "inputforegroundcolour",
                                 "ui", "foregroundcolour"), null));
         setBackground(backgroundColour);
         setForeground(foregroundColour);
         setDisabledTextColour(foregroundColour);
         setCaretColor(foregroundColour);
         setAttributes();
     }
 
     /**
      * Sets sensible attributes.
      */
     private void setAttributes() {
         as = new SimpleAttributeSet();
         StyleConstants.setFontFamily(as, topicText.getFont().getFamily());
         StyleConstants.setFontSize(as, topicText.getFont().getSize());
         StyleConstants.setBackground(as, backgroundColour);
         StyleConstants.setForeground(as, foregroundColour);
         StyleConstants.setUnderline(as, false);
         StyleConstants.setBold(as, false);
         StyleConstants.setItalic(as, false);
     }
 
     /**
      * Applies predefined attributes to the topic bar.
      */
     private void applyAttributes() {
         setAttributes();
         ((DefaultStyledDocument) topicText.getDocument())
                 .setCharacterAttributes(0, Integer.MAX_VALUE, as, true);
     }
 
     /**
      * Sets the caret position in this topic bar.
      *
      * @param position New position
      */
     public void setCaretPosition(final int position) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 topicText.setCaretPosition(position);
             }
         });
     }
 
     /**
      * Sets the caret colour to the specified colour.
      *
      * @param optionColour Colour for the caret
      */
     public void setCaretColor(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 topicText.setCaretColor(optionColour);
             }
         });
     }
 
     @Override
     public void setForeground(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 topicText.setForeground(optionColour);
             }
         });
     }
 
     /**
      * Sets the disabled text colour to the specified colour.
      *
      * @param optionColour Colour for the disabled text
      */
     public void setDisabledTextColour(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 topicText.setDisabledTextColor(optionColour);
             }
         });
     }
 
     @Override
     public void setBackground(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 topicText.setBackground(optionColour);
             }
         });
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         updateOptions();
         setVisible(showBar);
         cancelTopicEdit();
         if ("showfulltopic".equals(key)) {
             topicText.setEditorKit(new WrapEditorKit(showFull, channel.getEventBus(), window));
             ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
                     new NewlinesDocumentFilter());
             topicChanged(channel, null);
         }
         setColours();
     }
 
     private void updateOptions() {
         showFull = channel.getConfigManager().getOptionBool(domain, "showfulltopic");
         hideEmpty = channel.getConfigManager().getOptionBool(domain, "hideEmptyTopicBar");
         showBar = channel.getConfigManager().getOptionBool(domain, "showtopicbar");
     }
 
     /**
      * Closes this topic bar.
      */
     public void close() {
         channel.removeTopicChangeListener(this);
     }
 
     /**
      * Validates the topic text and shows errors as appropriate.
      */
     public void validateTopic() {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (topicText.isEditable()) {
                     final int charsLeft = topicLengthMax - topicText.getText().
                             length();
                     if (charsLeft < 0) {
                         errorIcon.setVisible(true);
                         errorIcon.setToolTipText("Topic too long: " + topicText.
                                 getText().length() + " of " + topicLengthMax);
                     } else {
                         errorIcon.setVisible(false);
                         errorIcon.setToolTipText(null);
                     }
                 } else {
                     errorIcon.setVisible(false);
                 }
             }
         });
     }
 
     @Override
     public void mouseClicked(final MouseEvent e) {
         if (e.getClickCount() == 2 && !topicText.isEditable()) {
             topicEdit.doClick();
         }
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void insertUpdate(final DocumentEvent e) {
         validateTopic();
         if (topicText.isEditable()) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     applyAttributes();
                 }
             });
         }
     }
 
     @Override
     public void removeUpdate(final DocumentEvent e) {
         validateTopic();
     }
 
     @Override
     public void changedUpdate(final DocumentEvent e) {
         validateTopic();
     }
 
 }
