 /*
  * Copyright (c) 2009 Timothy Bourke
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the "BSD License" which is distributed with the
  * software in the file LICENSE.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
  * License for more details.
  */
 
 package mnemojojo;
 
 import java.lang.*;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.TextField;
 
 import gr.fire.core.FireScreen;
 import gr.fire.core.Component;
 import gr.fire.core.Container;
 
 import gr.fire.core.BoxLayout;
 import gr.fire.core.Container;
 import gr.fire.core.GridLayout;
 import gr.fire.ui.InputComponent;
 import gr.fire.ui.TextComponent;
 
 class SubPanel
     extends Panel
     implements CommandListener,
                gr.fire.core.CommandListener
 {
     protected FireScreen screen;
     protected gr.fire.core.CommandListener listener;
     protected Command cmdDone;
 
     protected Command cmdButton;
 
     protected static Font sectionFont;
     protected static Font titleFont;
     protected static Font labelFont;
     protected static Font textFont;
 
     protected static int sectionFontHeight;
     protected static int titleFontHeight;
     protected static int labelFontHeight;
     protected static int textFontHeight;
     protected static int buttonHeight;
 
     protected static int buttonBgColor;
     protected static int buttonFgColor;
 
     protected static int radioWidth;
     protected static int radioHeight;
 
     public static String maxFieldRowTitle = "Mnemosyne: ";
     public static String maxFieldRowText = "Pashalis Padeleris";
 
     protected int screenWidth;
     protected final int controlGap = 10;
     protected final int edgeGap = 10;
 
     protected Command cmdLeft;
     protected Command cmdRight;
 
     protected Configuration config;
 
     public SubPanel(String title, FireScreen s, gr.fire.core.CommandListener li,
                     Command cmd, Configuration config)
     {
         super(null, Panel.VERTICAL_SCROLLBAR, true);
         
         screen = s;
         listener = li;
         cmdDone = cmd;
         this.config = config;
         setLabel(title);
 
         screenWidth = columnWidth(screen.getWidth());
 
         if (sectionFont == null) {
             int standardFontSize;
 
             if (config.isBigScreen) {
                 standardFontSize = Font.SIZE_LARGE;
                 radioWidth = InputComponent.RADIO_WIDTH * 3;
                 radioHeight = InputComponent.RADIO_HEIGHT * 3;
             } else {
                 standardFontSize = Font.SIZE_SMALL;
                 radioWidth = InputComponent.RADIO_WIDTH;
                 radioHeight = InputComponent.RADIO_HEIGHT;
             }
 
             // setup fonts
             sectionFont = Font.getFont(Font.FACE_SYSTEM,
                                        Font.STYLE_BOLD,
                                        Font.SIZE_LARGE);
             sectionFontHeight = sectionFont.getHeight();
 
             titleFont = Font.getFont(Font.FACE_SYSTEM,
                                      Font.STYLE_BOLD,
                                      standardFontSize);
             titleFontHeight = titleFont.getHeight();
 
             textFont = Font.getFont(Font.FACE_SYSTEM,
                                     Font.STYLE_PLAIN,
                                     standardFontSize);
             textFontHeight = textFont.getHeight();
 
             labelFont = titleFont;
             labelFontHeight = titleFontHeight;
 
             buttonHeight = labelFontHeight * 2;
             if (config.isBigScreen) {
                 buttonHeight = labelFontHeight * 3;
             }
             buttonBgColor = FireScreen.getTheme().getIntProperty("button.bg.color");
             buttonFgColor = FireScreen.getTheme().getIntProperty("button.fg.color");
         }
 
         cmdButton = new Command("invisible", Command.OK, 1);
     }
 
     public int columnWidth(int screenWidth)
     {
         return ((screenWidth - edgeGap) * 9 / 10); // 90%
     }
 
     protected void exitPanel(Command cmd)
     {
         listener.commandAction(cmd, (Component)this);
     }
 
     protected void exitPanel()
     {
         exitPanel(cmdDone);
     }
 
     protected Container titleRow(String title, int extraGap)
     {
         Container row = new Container(new BoxLayout(BoxLayout.Y_AXIS));
 
         TextComponent titleCmp = new TextComponent(title, screenWidth);
         titleCmp.setFont(sectionFont);
 
         int valign = FireScreen.TOP;
         if (extraGap > 0) {
             valign = FireScreen.BOTTOM;
         }
 
         titleCmp.setLayout(valign | FireScreen.CENTER);
         titleCmp.validate();
 
         row.add(titleCmp);
         row.setPrefSize(screenWidth, sectionFontHeight + extraGap);
 
         return row;
     }
 
     protected Container fieldRow(String title, String text)
     {
         int titleWidth = (int)(titleFont.stringWidth(maxFieldRowTitle));
         int valueWidth = (int)(textFont.stringWidth(maxFieldRowText));
        boolean doubleLine = (titleWidth + valueWidth < screenWidth);
 
         Container row = new Container(
            new BoxLayout(doubleLine?BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
         
         TextComponent titleCmp = new TextComponent(title + ":", titleWidth);
         titleCmp.setFont(titleFont);
         titleCmp.setLayout(FireScreen.TOP | FireScreen.LEFT);
         titleCmp.validate();
 
         TextComponent textCmp = new TextComponent(text);
         textCmp.setFont(textFont);
         textCmp.setLayout(FireScreen.TOP | FireScreen.LEFT);
         textCmp.validate();
 
         row.add(titleCmp);
         row.add(textCmp);
         row.setPrefSize(screenWidth, titleFontHeight * (doubleLine?2:1));
 
         return row;
     }
 
     protected InputComponent checkboxRow(String text, Container cnt)
     {
         Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));
 
         int inputHeight = Math.max(labelFontHeight, radioHeight + 2);
 
         InputComponent checkbox = new InputComponent(InputComponent.CHECKBOX);
         checkbox.setValue(text);
         checkbox.setCommandListener(this);
         checkbox.setCommand(cmdButton);
         checkbox.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
         checkbox.setBackgroundColor(buttonBgColor);
         checkbox.setForegroundColor(buttonFgColor);
         checkbox.setPrefSize(radioWidth, radioHeight);
         checkbox.setLeftSoftKeyCommand(cmdLeft);
         checkbox.setRightSoftKeyCommand(cmdRight);
         checkbox.validate();
 
         TextComponent blank = new TextComponent(" ", 8);
 
         TextComponent title = new TextComponent(text,
                                 screenWidth - radioWidth - 8);
         title.setFont(labelFont);
         title.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
         title.validate();
 
         row.add(checkbox);
         row.add(blank);
         row.add(title);
         row.setPrefSize(screenWidth, inputHeight + controlGap);
         row.validate();
 
         cnt.add(row);
 
         return checkbox;
     }
 
     protected InputComponent numberRow(String text, Container cnt)
     {
         Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));
 
         int titleWidth = screenWidth / 2;
         int numboxWidth = textFont.charWidth('0') * 8;
 
         TextComponent title = new TextComponent(text + "  ", titleWidth);
         title.setFont(labelFont);
         title.setLayout(FireScreen.LEFT | FireScreen.TOP);
         title.validate();
 
         InputComponent numbox = new InputComponent(InputComponent.TEXT);
         numbox.setCommandListener(this);
         numbox.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
         numbox.setRows(1);
         numbox.setValue("");
         numbox.setCommandListener(this);
         numbox.setCommand(cmdButton);
         numbox.setBackgroundColor(buttonBgColor);
         numbox.setForegroundColor(buttonFgColor);
         numbox.setPrefSize(numboxWidth, textFontHeight * 2);
         numbox.setFont(textFont);
         numbox.addTextConstraints(TextField.NUMERIC);
         numbox.setLeftSoftKeyCommand(cmdLeft);
         numbox.setRightSoftKeyCommand(cmdRight);
         numbox.validate();
 
         row.add(title);
         row.add(numbox);
         row.setPrefSize(screenWidth,
                         Math.max(labelFontHeight, textFontHeight * 2)
                         + controlGap);
         row.validate();
 
         cnt.add(row);
 
         return numbox;
     }
 
     protected Container buttonRow(String text)
     {
         Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));
 
         InputComponent button = new InputComponent(InputComponent.BUTTON);
         button.setValue(text);
         button.setCommandListener(this);
         button.setCommand(cmdButton);
         button.setFont(labelFont);
         button.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
         button.setBackgroundColor(buttonBgColor);
         button.setForegroundColor(buttonFgColor);
         button.setPrefSize(screenWidth, buttonHeight);
         button.setLeftSoftKeyCommand(cmdLeft);
         button.setRightSoftKeyCommand(cmdRight);
         button.validate();
 
         row.add(button);
         row.setPrefSize(screenWidth, buttonHeight + controlGap);
         row.validate();
 
         return row;
     }
 
     protected InputComponent radioRow(String text, int width, Container cnt)
     {
         Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));
 
         int inputHeight = Math.max(labelFontHeight, radioHeight + 2);
 
         InputComponent radio = new InputComponent(InputComponent.RADIO);
         radio.setValue(text);
         radio.setCommandListener(this);
         radio.setCommand(cmdButton);
         radio.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
         radio.setBackgroundColor(buttonBgColor);
         radio.setForegroundColor(buttonFgColor);
         radio.setPrefSize(radioWidth, radioHeight);
         radio.setLeftSoftKeyCommand(cmdLeft);
         radio.setRightSoftKeyCommand(cmdRight);
         radio.validate();
 
         TextComponent title = new TextComponent("  " + text,
                                 width - radioWidth);
         title.setFont(textFont);
         title.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
         title.validate();
 
         row.add(radio);
         row.add(title);
         row.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
         row.setPrefSize(width, inputHeight);
 
         cnt.add(row);
 
         return radio;
     }
 
     public void commandAction(javax.microedition.lcdui.Command cmd,
                               Displayable d)
     {
     }
 
     public void commandAction(javax.microedition.lcdui.Command cmd,
                               Component c)
     {
     }
 
     public void screenSizeChanged(int newWidth, int newHeight)
     {
         super.screenSizeChanged(newWidth, newHeight);
         screenWidth = columnWidth(newWidth);
     }
 }
 
