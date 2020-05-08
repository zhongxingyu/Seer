 /**
  * Copyright (c) 2008 Devin Weaver
  * Licensed under the Educational Community License version 1.0
  * See the file COPYING with this distrobution for details.
  */
 package com.tritarget.client;
 
 // Imports {{{
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 // }}}
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class CipherEncoder implements EntryPoint, ChangeListener {
     // Constants
     public static final String APP_NAME = "Cipher Encoder";
    public static final String APP_VERSION = "0.2";
 
     // Private fields {{{
     private TextBox key1;
     private TextBox key2;
     private TextArea text;
     private TextArea output;
     private RadioButton[] cipher_choice;
     private Button encode_btn;
     private Button decode_btn;
     private VerticalPanel debug_panel;
     private TabPanel main_tab_panel;
     // }}}
 
     /**
      * This is the entry point method.
      */
     // onModuleLoad() {{{
     public void onModuleLoad() {
        int tab_index = 0;
 
         // Setup debug panel {{{2
         this.debug_panel = new VerticalPanel();
 
         // Setup the main panel. {{{2
         debug("Initializing main panel.");
         VerticalPanel main_panel = new VerticalPanel();
         HorizontalPanel top_panel = new HorizontalPanel();
         VerticalPanel chooser_panel = new VerticalPanel();
         VerticalPanel keys_panel = new VerticalPanel();
         HorizontalPanel buttons_panel = new HorizontalPanel();
         HorizontalPanel text_panel = new HorizontalPanel();
         DisclosurePanel disclose_panel = new DisclosurePanel("Debug Console");
         disclose_panel.add(this.debug_panel);
 
         // Setup the key entries {{{2
         debug("Initializing key entry panel.");
         this.key1 = new TextBox();
         this.key1.addChangeListener(this);
         this.key1.setTabIndex(tab_index++);
         this.key2 = new TextBox();
         this.key2.addChangeListener(this);
         this.key2.setTabIndex(tab_index++);
         keys_panel.add(new Label("First Key:"));
         keys_panel.add(key1);
         keys_panel.add(new Label("Second Key:"));
         keys_panel.add(key2);
         top_panel.add(keys_panel);
 
         // Setup the cipher chooser. {{{2
         debug("Initializing cipher chooser panel.");
         this.cipher_choice = new RadioButton[3];
         this.cipher_choice[0] = new RadioButton("cipher_choice_group", "Double Box Playfair");
         this.cipher_choice[0].setTabIndex(-1);
         this.cipher_choice[1] = new RadioButton("cipher_choice_group", "Double Transposition");
         this.cipher_choice[1].setTabIndex(-1);
         this.cipher_choice[2] = new RadioButton("cipher_choice_group", "Both");
         this.cipher_choice[2].setTabIndex(-1);
         this.cipher_choice[2].setChecked(true);
         chooser_panel.add(new Label("Choose Cipher:"));
         chooser_panel.add(this.cipher_choice[0]);
         chooser_panel.add(this.cipher_choice[1]);
         chooser_panel.add(this.cipher_choice[2]);
         top_panel.add(chooser_panel);
 
         // Setup the Text input and output box {{{2
         debug("Initializing text area panels.");
         this.text = new TextArea();
         this.text.setCharacterWidth(40);
         this.text.setVisibleLines(5);
         this.text.addChangeListener(this);
         this.text.setTabIndex(tab_index++);
         this.output = new TextArea();
         this.output.setCharacterWidth(40);
         this.output.setVisibleLines(5);
         this.output.setTabIndex(-1);
         this.output.setReadOnly(true);
         this.output.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 TextArea ta = (TextArea) sender;
                 ta.setSelectionRange(0, ta.getText().length());
             }
         });
         VerticalPanel left_text_panel = new VerticalPanel();
         left_text_panel.add(new Label("Enter text to encode/decode:"));
         left_text_panel.add(this.text);
         text_panel.add(left_text_panel);
         VerticalPanel right_text_panel = new VerticalPanel();
         right_text_panel.add(new Label("Result:"));
         right_text_panel.add(this.output);
         text_panel.add(right_text_panel);
 
         // Setup the buttons panel {{{2
         debug("Initializing button panel.");
         this.encode_btn = new Button("Encode");
         this.encode_btn.setEnabled(false);
         this.encode_btn.setTabIndex(tab_index++);
         this.encode_btn.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 CipherEncoder.this.encode();
             }
         });
         this.decode_btn = new Button("Decode");
         this.decode_btn.setEnabled(false);
         this.decode_btn.setTabIndex(tab_index++);
         this.decode_btn.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 CipherEncoder.this.decode();
             }
         });
         buttons_panel.add(this.encode_btn);
         buttons_panel.add(new Label("<-->"));
         buttons_panel.add(this.decode_btn);
 
 
         // Setup tabs {{{2
         debug("Initializing main tab panel");
         this.main_tab_panel = new TabPanel();
         this.main_tab_panel.add(main_panel, "Main");
         this.main_tab_panel.add(AboutBox.getHtml(), "About");
         this.main_tab_panel.selectTab(0);
 
         // Setting style classes. {{{2
         debug("Stylizing panels");
         main_panel.addStyleName("main-panel");
         top_panel.addStyleName("top-panel");
         keys_panel.addStyleName("keys-panel");
         chooser_panel.addStyleName("chooser-panel");
         buttons_panel.addStyleName("buttons-panel");
         text_panel.addStyleName("text-panel");
         disclose_panel.addStyleName("disclose-panel");
         this.main_tab_panel.addStyleName("main-tab-panel");
         this.text.addStyleDependentName("input");
         this.output.addStyleDependentName("output");
 
         // Add panels to main container {{{2
         debug("Rendering.");
         main_panel.add(InstructionBox.getHtml());
         main_panel.add(top_panel);
         main_panel.add(buttons_panel);
         main_panel.add(text_panel);
         main_panel.add(disclose_panel);
 
         // Add main panel to the root {{{2
         RootPanel.get().add(this.main_tab_panel);
         RootPanel.get("loading").setVisible(false);
         debug("Application ready.");
     } // }}}1
 
     /**
      * Runs the encoding logic.
      */
     // encode() {{{
     public void encode() {
         debugClear();
         String output = "";
         String input = this.text.getText();
         // Double Box Playfair
         if (this.cipher_choice[0].isChecked() || this.cipher_choice[2].isChecked())
         {
             DoublePlayfairCipher dpc = new DoublePlayfairCipher(this.key1.getText(), this.key2.getText());
             debug("Executing DoublePlayfairCipher");
             debugFixed(dpc.toString());
             output = dpc.encode(input);
             debug("Result: " + output);
         }
         if (this.cipher_choice[2].isChecked())
         {
             input = output;
         }
         // Double Transposition
         if (this.cipher_choice[1].isChecked() || this.cipher_choice[2].isChecked())
         {
             TranspositionCipher tc = new TranspositionCipher(this.key1.getText());
             debug("Executing first TranspositionCipher");
             debugFixed(tc.toString());
             output = tc.encode(input);
             debug("Result: " + output);
             tc = new TranspositionCipher(this.key2.getText());
             debug("Executing second TranspositionCipher");
             debugFixed(tc.toString());
             output = tc.encode(output);
             debug("Result: " + output);
         }
         this.output.setText(output);
     }
     // }}}
 
     /**
      * Runs the decoding logic.
      */
     // decode() {{{
     public void decode() {
         debugClear();
         String output = "";
         String input = this.text.getText();
         // Double Transposition
         if (this.cipher_choice[1].isChecked() || this.cipher_choice[2].isChecked())
         {
             TranspositionCipher tc = new TranspositionCipher(this.key2.getText());
             debug("Executing second TranspositionCipher");
             debugFixed(tc.toString());
             output = tc.decode(input);
             debug("Result: " + output);
             tc = new TranspositionCipher(this.key1.getText());
             debug("Executing first TranspositionCipher");
             debugFixed(tc.toString());
             output = tc.decode(output);
             debug("Result: " + output);
         }
         if (this.cipher_choice[2].isChecked())
         {
             input = output;
         }
         // Double Box Playfair
         if (this.cipher_choice[0].isChecked() || this.cipher_choice[2].isChecked())
         {
             DoublePlayfairCipher dpc = new DoublePlayfairCipher(this.key1.getText(), this.key2.getText());
             debug("Executing DoublePlayfairCipher");
             debugFixed(dpc.toString());
             output = dpc.decode(input);
             debug("Result: " + output);
         }
         this.output.setText(output);
     }
     // }}}
 
     /**
      * Validates the fields and enable/disables the buttons.
      * @param sender the widget that triggered the event.
      */
     // onChange(Widget) {{{
     public void onChange(Widget sender) {
         if (this.key1.getText().length() > 0
             && this.key2.getText().length() > 0
             && this.text.getText().length() > 0)
         {
             this.encode_btn.setEnabled(true);
             this.decode_btn.setEnabled(true);
         }
         else
         {
             this.encode_btn.setEnabled(false);
             this.decode_btn.setEnabled(false);
         }
     }
     // }}}
 
     /**
      * Add text to the debug console.
      * @param text the text to add to the debug console.
      */
     // debug(String) {{{
     public void debug(String text) {
         this.debug_panel.add(new Label(text));
     }
     // }}}
 
     /**
      * Clear the debug console.
      */
     // debugClear() {{{
     public void debugClear() {
         this.debug_panel.clear();
     }
     // }}}
 
     /**
      * Add HTML to the debug console.
      * @param text the text to add to the debug console.
      */
     // debugHtml(String) {{{
     public void debugHtml(String text) {
         this.debug_panel.add(new HTML(text));
     }
     // }}}
 
     /**
      * Add text pre formatted to the debug console.
      * @param text the text to add to the debug console.
      */
     // debugFixed(String) {{{
     public void debugFixed(String text) {
         this.debug_panel.add(new HTML("<pre>" + text + "</pre>"));
     }
     // }}}
 }
 /* vim:set et sw=4 fdm=marker: */
