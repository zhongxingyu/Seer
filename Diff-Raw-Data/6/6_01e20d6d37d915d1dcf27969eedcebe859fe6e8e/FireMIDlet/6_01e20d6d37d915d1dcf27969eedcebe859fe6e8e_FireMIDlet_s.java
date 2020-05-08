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
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Alert;
 import javax.microedition.lcdui.AlertType;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.Font;
 
 import mnemogogo.mobile.hexcsv.Card;
 import mnemogogo.mobile.hexcsv.FindCardDirJ2ME;
 import mnemogogo.mobile.hexcsv.Progress;
 
 import gr.fire.browser.Browser;
 import gr.fire.browser.util.Page;
 import gr.fire.ui.FireTheme;
 import gr.fire.core.FireScreen;
 import gr.fire.core.KeyListener;
 import gr.fire.core.Component;
 import gr.fire.core.Theme;
 import gr.fire.util.Log;
 import gr.fire.util.FireConnector;
 import gr.fire.core.FireListener;
 
 // for buttons:
 import gr.fire.core.BoxLayout;
 import gr.fire.core.Container;
 import gr.fire.core.GridLayout;
 import gr.fire.ui.InputComponent;
 
 public class FireMIDlet
     extends Core
     implements CommandListener,
                gr.fire.core.CommandListener,
                KeyListener,
                FireListener
 {
     boolean initialized = false;
 
     StringBuffer path;
     int pathLen;
 
     Display display;
     HttpClient httpClient;
     Browser browser;
     FireScreen screen;
 
     Panel currentPanel;
     Card currentCard;
     String currentTitle;
 
     Panel showButtons;
     Panel gradeButtons;
 
     ProgressGauge progressGauge;
 
     Command cmdOk;
     Command cmdExit;
     Command cmdShow;
     Command cmdShowQ;
     Command cmdShowA;
     Command cmdReshow;
     Command cmdButton;
 
     private final boolean debug = false;
 
     private int current;
     private final int CARD_DIRS = 0;
     private final int ABOUT = 1;
     private final int QUESTION = 2;
     private final int ANSWER = 3;
     private final int WAIT = 4;
     private final int KEYMAP = 5;
 
     protected final int BUTTONS_NONE = 0;
     protected final int BUTTONS_SHOW = 1;
     protected final int BUTTONS_GRADE = 2;
 
     Runtime rt = Runtime.getRuntime();
 
     public FireMIDlet()
     {
         Log.showDebug = debug;
 
         display = Display.getDisplay(this);
 
         // initialize a browser instance
         screen = FireScreen.getScreen(display);
         config.setScreen(screen);
 
         screen.setFullScreenMode(true);
         try {
             if (config.isBigScreen) {
                 screen.setTheme(new FireTheme("file://hires.properties"));
             } else {
                 screen.setTheme(new FireTheme("file://normal.properties"));
             }
         } catch (Exception e) {}
 
         progressGauge = new ProgressGauge();
         progressHandler = (Progress)progressGauge;
 
         httpClient = new HttpClient(new FireConnector());
         browser = new Browser(httpClient);
 
         cmdOk = new Command(okText, Command.OK, 1); 
         cmdExit = new Command(exitText, Command.EXIT, 5);
         cmdShow = new Command(showText, Command.ITEM, 1);
         cmdShowQ = new Command(closeText, Command.ITEM, 1);
         cmdShowA = new Command(closeText, Command.ITEM, 1);
         cmdReshow = new Command(closeText, Command.ITEM, 1);
         cmdButton = new Command("invisible", Command.OK, 1);
     }
 
     public void startApp()
         throws MIDletStateChangeException
     {
         if (!initialized) {
             if (config.leftSoftKey != 0) {
                 screen.leftSoftKey = config.leftSoftKey;
             }
             if (config.rightSoftKey != 0) {
                 screen.rightSoftKey = config.rightSoftKey;
             }
             showAbout();
             initialized = true;
         }
     }
 
     public void destroyApp(boolean unconditional)
     {
         saveCards();
         super.destroyApp(unconditional);
         screen.destroy();
         notifyDestroyed();
     }
 
     public void setCardDir(String cardpath)
         throws IOException
     {
         httpClient.setUrlPrefix(cardpath);
         path = new StringBuffer(cardpath);
         pathLen = path.length();
     }
 
     private Page makePage(String contents)
     {
         try {
             ByteArrayInputStream in =
                 new ByteArrayInputStream(contents.getBytes("UTF-8"));
             Page page = browser.loadPage(in, "UTF-8");
             in.close();
 
             return page;
         } catch (Exception e) {
             showFatal(e.toString(), false);
         }
 
         return null;
     }
 
     private Page loadPage(String pagePath)
     {
         try {
             Page page = browser.loadPage(pagePath, HttpConnection.GET, null, null);
             return page;
         } catch (Exception e) {
             showFatal(e.toString(), false);
         }
 
         return null;
     }
 
     StringBuffer makeCardHtml(boolean includeAnswer)
     {
         StringBuffer html = new StringBuffer(
             "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<body>");
 
         String question = curCard.getQuestion();
         String answer = curCard.getAnswer();
 
         if (config.centerText) {
             html.append("<center>");
         }
 
         if (question == null || answer == null) {
             html.append(nocardloadedText);
 
         } else if (includeAnswer) {
             if (!curCard.getOverlay()) {
                 html.append(question);
                 html.append("<hr/>");
             }
 
             html.append(answer);
 
         } else {
             html.append(question);
         }
 
         if (config.centerText) {
             html.append("</center>");
         }
 
         html.append("</body>");
 
         return html;
     }
 
     // from Fire demo: SimpleCalc.java
     private Panel makeButtonRow(String symbols[],
                                 Command cmdLeft, Command cmdRight)
     {
         InputComponent button;
 
         Font buttonFont;
         int buttonHeight;
         int rows = 1;
         int cols = symbols.length;
 
         if (config.isBigScreen) {
             buttonFont = Font.getFont(Font.FACE_SYSTEM,
                                       Font.STYLE_BOLD,
                                       Font.SIZE_LARGE);
             buttonHeight = buttonFont.getHeight() * 4;
             if (symbols.length >= 6) {
                 rows = 2;
                 cols = symbols.length / 2;
                 buttonHeight *= 2;
             }
         } else {
             buttonFont = Font.getFont(Font.FACE_SYSTEM,
                                       Font.STYLE_BOLD,
                                       Font.SIZE_MEDIUM);
             buttonHeight = buttonFont.getHeight() * 2;
         }
 
         Container pad = new Container(new GridLayout(rows, cols));
         
         for(int i = 0; i<symbols.length; ++i) {
             button = new InputComponent(InputComponent.BUTTON);
             button.setValue(symbols[i]); 
             button.setCommandListener(this);
             button.setKeyListener(this);
             button.setCommand(cmdButton);
             button.setLeftSoftKeyCommand(cmdLeft);
             button.setRightSoftKeyCommand(cmdRight);
             button.setForegroundColor(
                 FireScreen.getTheme().getIntProperty("button.fg.color"));
             button.setBackgroundColor(
                 FireScreen.getTheme().getIntProperty("button.bg.color"));
             button.setFont(buttonFont);
             button.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
             pad.add(button);
         }
         
         Panel padPane = new Panel(pad, Panel.NO_SCROLLBAR, false);              
         padPane.setShowBackground(true);
         padPane.setBackgroundColor(
             FireScreen.getTheme().getIntProperty("titlebar.bg.color"));
         padPane.setPrefSize(FireScreen.getScreen().getWidth(), buttonHeight);
 
         return padPane;
     }
 
     private Panel makeGradeButtons(Command cmdLeft, Command cmdRight)
     {
         String buttons[] = {"0", "1", "2", "3", "4", "5"};
         return makeButtonRow(buttons, cmdLeft, cmdRight);
     }
 
     private Panel makeShowButtons(Command cmdLeft, Command cmdRight)
     {
         String buttons[] = {skipCardText, showAnswerText, showStatsText};
         return makeButtonRow(buttons, cmdLeft, cmdRight);
     }
 
     private Panel makeDisplay(Page htmlPage, int buttonMode,
                               Command cmdLeft, Command cmdRight)
     {
         if (!config.showButtons) {
             buttonMode = BUTTONS_NONE;
         }
         boolean htmlDecorations = (buttonMode == BUTTONS_NONE);
 
         // create a panel to display cards / information
         Panel htmlPanel = new Panel(htmlPage.getPageContainer(),
                                     Panel.VERTICAL_SCROLLBAR,
                                     htmlDecorations,
                                     config);
         htmlPanel.setCommandListener(this);
         htmlPanel.setDragScroll(true);
         htmlPanel.setKeyListener(this);
         htmlPanel.setLeftSoftKeyCommand(cmdLeft);
         htmlPanel.setRightSoftKeyCommand(cmdRight);
 
         if (buttonMode == BUTTONS_NONE) {
             return htmlPanel;
         }
 
         Container controls = new Container(new BoxLayout(BoxLayout.Y_AXIS));
         controls.add(htmlPanel);
 
         switch (buttonMode) {
         case BUTTONS_SHOW:
             if (showButtons == null) {
                 showButtons = makeShowButtons(cmdLeft, cmdRight);
             }
             controls.add(showButtons);
             break;
 
         case BUTTONS_GRADE:
             if (gradeButtons == null) {
                 gradeButtons = makeGradeButtons(cmdLeft, cmdRight);
             }
             controls.add(gradeButtons);
             break;
         }
 
         Panel outer = new Panel(controls, Panel.NO_SCROLLBAR, true);
         outer.setCommandListener(this);
         outer.setKeyListener(this);
         outer.setLeftSoftKeyCommand(cmdLeft);
         outer.setRightSoftKeyCommand(cmdRight);
 
         outer.scrollPanel = htmlPanel;
         return outer;
     }
 
     public void setCard(Card card, int numLeft)
         throws Exception, IOException
     {
         if (card != null) {
             currentTitle = card.categoryName() + "\t"
                                 + Integer.toString(numLeft);
         }
         currentCard = card;
     }
 
     void checkExportTime()
     {
         int days_left = carddb.daysLeft();
         byte icon;
         String msg;
 
         if (days_left < 0) {
             msg = updateOverdueText;
             icon = gr.fire.ui.Alert.TYPE_WARNING;
         } else if (days_left == 0) {
             msg = updateTodayText;
             icon = gr.fire.ui.Alert.TYPE_INFO;
         } else {
             return;
         }
 
         screen.showAlert(msg, icon,
                          gr.fire.ui.Alert.USER_SELECTED_OK, cmdShowQ, this);
     }
 
     void showFatal(String msg, boolean exit)
     {
         Command cmdAfter = null;
         if (exit) {
             cmdAfter = cmdExit;
         }
 
         screen.showAlert(msg, gr.fire.ui.Alert.TYPE_ERROR,
                          gr.fire.ui.Alert.USER_SELECTED_OK, cmdAfter, this);
     }
 
     void showDone()
     {
         String msg = doneText;
         screen.showAlert(msg,
                          gr.fire.ui.Alert.TYPE_INFO,
                          gr.fire.ui.Alert.USER_SELECTED_OK,
                          cmdExit, this);
     }
 
     private void addStatRow(StringBuffer msg, String name, String stat)
     {
         msg.append("<tr><td>");
         msg.append(name);
         msg.append("</td><td>");
         msg.append(stat);
         msg.append("</td></tr>");
     }
 
     private void addStatRow(StringBuffer msg, String name, int stat)
     {
         addStatRow(msg, name, Integer.toString(stat));
     }
 
     private void addStatRow(StringBuffer msg, String name, float stat)
     {
         addStatRow(msg, name, Float.toString(stat));
     }
 
     private void addStatRow(StringBuffer msg, String name, long stat)
     {
         addStatRow(msg, name, Long.toString(stat));
     }
 
     private void futureScheduleText(StringBuffer r)
     {
         int[] indays = carddb.getFutureSchedule();
         if (indays == null) {
             return;
         }
 
         r.append("<table>");
         r.append("<tr><td>" + forDaysText + ":</td><td/></tr>");
         for (int i=0; i < indays.length; ++i) {
             r.append("<tr><td>");
             r.append(inText);
             r.append(" ");
             r.append(i + 1);
             r.append(" ");
             r.append(daysText);
             r.append(": </td><td>");
             r.append(indays[i]);
             r.append("</td></tr>");
         }
         r.append("</table>");
     }
 
     void showStats(int returnTo)
     {
         int daysLeft = carddb.daysLeft();
         StringBuffer msg = new StringBuffer("<body><p>");
 
         if (daysLeft < 0) {
             msg.append(updateOverdueText);
         } else if (daysLeft == 0) {
             msg.append(updateTodayText);
         } else {
             futureScheduleText(msg);
         }
 
         if (curCard != null) {
             msg.append("<br/><br/><table>");
             addStatRow(msg, gradeText + ": ", curCard.grade);
             addStatRow(msg, easinessText + ": ", curCard.feasiness());
             addStatRow(msg, repetitionsText + ": ", curCard.repetitions());
             addStatRow(msg, lapsesText + ": ", curCard.lapses);
             addStatRow(msg, daysSinceLastText + ": ",
                 curCard.daysSinceLastRep(carddb.days_since_start));
             addStatRow(msg, daysUntilNextText + ": ",
                 curCard.daysUntilNextRep(carddb.days_since_start));
             msg.append("</table>");
         }
 
         msg.append("<br/><br/><table>");
         addStatRow(msg, cardsdirText, config.cardpath);
         addStatRow(msg, freeMemoryText, rt.freeMemory());
         addStatRow(msg, totalMemoryText, rt.totalMemory());
         msg.append("</table></p></body>");
 
         Panel statPanel = makeDisplay(makePage(msg.toString()), BUTTONS_NONE,
                                        null, cmdReshow);
 
         if (statPanel != null) {
             statPanel.setRightSoftKeyCommand(cmdReshow);
             statPanel.setLabel(statisticsText);
             screen.setCurrent(statPanel);
         }
     }
 
     void showAbout()
     {
         AboutPanel aboutPanel = new AboutPanel(screen, versionInfo,
                                         this, cmdOk, cmdExit, config);
 
         // copy across the current configured values
         if ((config.cardpath != null)
             && (FindCardDirJ2ME.isCardDir(config.cardpath, null))) {
             aboutPanel.cardpath = config.cardpath;
         } else {
             config.cardpath = null;
             aboutPanel.cardpath = null;
         }
         aboutPanel.fontSize = config.fontSize;
         aboutPanel.cardsToLoad = config.cardsToLoad;
         aboutPanel.touchScreen = config.showButtons;
         aboutPanel.centerText = config.centerText;
         int i = 0;
         while (i < 6) {
             aboutPanel.keys[i] = config.gradeKey[i];
             ++i;
         }
         aboutPanel.keys[i++] = config.statKey;
         aboutPanel.keys[i++] = config.skipKey;
 
         current = ABOUT;
         screen.setCurrent(aboutPanel);
         aboutPanel.repaintControls();
     }
 
     void showQuestionScreen()
     {
         if (currentCard != null) {
             currentPanel = makeDisplay(makePage(makeCardHtml(false).toString()),
                                         BUTTONS_SHOW, cmdShow, cmdExit);
             currentPanel.setLeftSoftKeyCommand(cmdShow);
             currentPanel.setRightSoftKeyCommand(cmdExit);
             currentPanel.setKeyListener(this);
             currentPanel.setLabel(currentTitle);
             screen.setCurrent(currentPanel);
             current = QUESTION;
         } else {
             showDone();
         }
     }
 
     void showAnswerScreen()
     {
         currentPanel = makeDisplay(makePage(makeCardHtml(true).toString()),
                                   BUTTONS_GRADE, null, cmdExit);
         currentPanel.setRightSoftKeyCommand(cmdExit);
         currentPanel.setKeyListener(this);
         currentPanel.setLabel(currentTitle);
 
         screen.setCurrent(currentPanel);
         current = ANSWER;
     }
 
     private void showNextQuestion()
     {
         if (nextQuestion()) {
             //System.out.println(carddb.toString());
             showQuestionScreen();
             startThinking();
         } else {
             showDone();
         }
     }
 
     public void updateFont(int fontsize)
     {
         Theme theme = screen.getTheme();
         Font cur_font = theme.getFontProperty("xhtml.font");
         Font new_font = Font.getFont(cur_font.getFace(),
                                      cur_font.getStyle(),
                                      fontsize);
         theme.setFontProperty("xhtml.font", new_font);
         screen.setTheme(theme);
     }
 
     /* FireListener methods */
 
     public void sizeChanged(int newWidth, int newHeight)
     {
         try {
             Panel disPanel = (Panel)screen.getCurrent();
             if (disPanel != null) {
                 // side effect: recalculates position of label
                 disPanel.setLabel(disPanel.getLabel());
 
                 if ((currentPanel != null) && (currentPanel != disPanel)) {
                     // side effect: recalculates position of label
                     currentPanel.setLabel(currentPanel.getLabel());
                 }
             }
         } catch (Exception e) {}
 
         if (gradeButtons != null) {
             int[] size = gradeButtons.getPrefSize();
             if ((size != null) && (size[1] >= 0)) {
                 gradeButtons.setPrefSize(FireScreen.getScreen().getWidth(),
                                          size[1]);
             }
         }
 
         if (showButtons != null) {
             int[] size = showButtons.getPrefSize();
             if ((size != null) && (size[1] >= 0)) {
                 showButtons.setPrefSize(FireScreen.getScreen().getWidth(),
                                         size[1]);
             }
         }
     }
 
     public void hideNotify() { }
     public void showNotify() { }
 
     /* Respond to input methods */
 
     public void commandAction(javax.microedition.lcdui.Command cmd, Component c)
     {
         String label = cmd.getLabel();
 
         if (cmd.equals(cmdShowQ)) {
             showQuestionScreen();
             unpauseThinking();
             return;
 
         } else if (cmd.equals(cmdShowA)) {
             showAnswerScreen();
             return;
         
         } else if (cmd.equals(cmdReshow)) {
             screen.setCurrent(currentPanel);
 
         } else if (cmd.equals(cmdButton)) {
             String val = ((InputComponent)c).getValue();
             System.out.println("button: " + val);
 
             if (showAnswerText.equals(val)) {
                 showAnswerScreen();
 
             } else if (skipCardText.equals(val)) {
                 curCard.setSkip();
                 showNextQuestion();
 
             } else if (showStatsText.equals(val)) {
                 showStats(current);
 
             } else {
                 try {
                     doGrade(Integer.parseInt(val));
                     showNextQuestion();
                 } catch (NumberFormatException e) { }
             }
 
         }
 
         if (current == ABOUT) {
             // Save the settings on both Ok and Exit
             AboutPanel aboutPanel = (AboutPanel)c;
 
             if (aboutPanel.dirty) {
                 config.cardpath = aboutPanel.cardpath;
                 config.fontSize = aboutPanel.fontSize;
                 if (aboutPanel.cardsToLoad > 0) {
                     config.cardsToLoad = aboutPanel.cardsToLoad;
                 }
                 config.showButtons = aboutPanel.touchScreen;
                 config.centerText = aboutPanel.centerText;
                 int i = 0;
                 while (i < 6) {
                     config.gradeKey[i] = aboutPanel.keys[i];
                     ++i;
                 }
                 config.statKey = aboutPanel.keys[i++];
                 config.skipKey = aboutPanel.keys[i++];
 
                 config.leftSoftKey = screen.leftSoftKey;
                 config.rightSoftKey = screen.rightSoftKey;
 
                 config.save((Progress)progressGauge);
             }
         }
 
         if ((current == ABOUT) && (label.equals(okText))) {
 
             updateFont(config.fontSize);
 
             if (config.cardpath == null) {
                 showFatal(selectCarddir, false);
 
             } else {
                 loadCards();
                 //carddb.dumpCards();
                 showNextQuestion();
                 checkExportTime();
             }
 
         } else if (label.equals(showText)) {
             showAnswerScreen();
 
         } else if (label.equals(exitText)) {
             saveCards();
             notifyDestroyed();
         }
     }
 
     public void commandAction(javax.microedition.lcdui.Command cmd, Displayable dis)
     {
         String title = dis.getTitle();
         String label = cmd.getLabel();
 
         if (cmd.equals(cmdShowQ)) {
             showQuestionScreen();
             unpauseThinking();
             return;
 
         } else if (cmd.equals(cmdShowA)) {
             showAnswerScreen();
             return;
 
         } else if (cmd.equals(cmdReshow)) {
             screen.setCurrent(currentPanel);
             return;
         }
     }
 
     public void keyReleased(int code, Component src)
     {
         switch (current) {
         case QUESTION:
             if (code == config.skipKey) {
                 curCard.setSkip();
                 showNextQuestion();
 
             } else if (code == config.statKey) {
                 showStats(QUESTION);
             }
             break;
 
         case ANSWER:
             int grade = -1;
 
             for (int i=0; i < config.gradeKey.length; ++i) {
                 if (code == config.gradeKey[i]) {
                     grade = i;
                     break;
                 }
             }
 
             if (grade != -1) {
                 doGrade(grade);
                 showNextQuestion();
 
             } else if (code == config.statKey) {
                 showStats(ANSWER);
             }
         
         default:
             break;
         }
     }
 
     public void keyRepeated(int code, Component src) {  }
     public void keyPressed(int code, Component src) { }
 }
 
