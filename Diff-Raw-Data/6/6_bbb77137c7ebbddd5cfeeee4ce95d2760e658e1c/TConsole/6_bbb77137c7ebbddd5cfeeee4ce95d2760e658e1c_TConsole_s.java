 /*
  * Copyright 2011 Christian Thiemann <christian@spato.net>
  * Developed at Northwestern University <http://rocs.northwestern.edu>
  *
  * This file is part of TransparentGUI, a GUI library for Processing.
  *
  * TransparentGUI is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TransparentGUI is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TransparentGUI.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tGUI;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PGraphics;
 import processing.core.PImage;
 import java.util.Vector;
 import java.util.Stack;
 
 public class TConsole extends TComponent {
 
   protected String tag = null;  // if non-null, this is prefixed to every message on standard output
   protected boolean showDebug = true;
   protected boolean persistent = false;  // if true, messages don't fade away
   protected boolean fancy = true;  // true: rolling msgs from bottom filling as much space as given by bounds; false: msgs from top with proper minimum size reporting
   protected float fnsize = 10;
   protected int progbarwidth = 125, progbarheight;
   protected PFont fnNorm;
   protected PFont fnBold;
   protected PImage imgProgBar;
 
   public static final int ALIGN_LEFT = PApplet.LEFT;
   public static final int ALIGN_CENTER = PApplet.CENTER;
   public static final int ALIGN_RIGHT = PApplet.RIGHT;
   protected int align = ALIGN_RIGHT;
 
   public static final int BUFFER_SIZE = 5000;  // number of messages to keep
 
   public static final int MSG_ERROR = 1;
   public static final int MSG_WARNING = 2;
   public static final int MSG_INFO = 3;
   public static final int MSG_NOTE = 4;
   public static final int MSG_DEBUG = 5;
   public static final int MSG_PROGRESS = 256;
   public static final int MSG_STICKY = 512;
   public static final int MSG_REVOKED = 1024;
 
   public class Message {
     public int type;
     public String text;
     public int t0;  // posting time of message
     public float progress;  // current progress, between 0 and 1
     public int t1 = -1;  // time of finishing progress; -1 = unfinished; -2 = aborted
     public int tE = Integer.MAX_VALUE;  // time of exit, i.e. when this message will start fading out
     public float y = Float.NaN, a = Float.NaN;  // last drawn y position on screen and alpha value
 
     public Message(int type, String text) { this(type, text, false); }
     public Message(int type, String text, boolean prog) {
       this.type = type; this.text = ("" + text).replace('\n', ' ');  // if text is null, this will yield "null"
       this.t0 = gui.app.millis();
       if (prog) { this.type = this.type | MSG_PROGRESS; progress = 0; this.text += " \u2026"; }  // add ellipsis
     }
     public Message sticky() { TConsole.this.pushSticky(this); setSticky(true); return this; }  // for console.logXXX(...).sticky()
     public void revoke() { setRevoked(true); }
     public Message indeterminate() { updateProgress(0.5f); return this; }
 
     public boolean hasProgress() { return (type & MSG_PROGRESS) > 0; }
     public boolean hasActiveProgress() { return hasProgress() && (t1 == -1); }
     public void updateProgress(float p) { updateProgress(p, 1); }
     public void updateProgress(float p, float pmax) {
       if (!hasActiveProgress()) return;
       float lastProgress = progress; progress = p/pmax;
       TConsole.this.handleUpdateProgress(this, lastProgress);
       if (progress >= 1) finishProgress(); }
     public void finishProgress() { finishProgress(null); }
     public void finishProgress(String addText) {
       if (!hasActiveProgress()) return;
       progress = 1; t1 = gui.app.millis(); text += " " + ((addText != null) ? addText + ", " : "") + minsec(t1 - t0); setExitTime(t1);
       TConsole.this.handleFinishProgress(this); }
     public void abortProgress() {
       if (!hasActiveProgress()) return;
       progress = 1; t1 = -2; text += " aborted"; setExitTime(gui.app.millis());
       TConsole.this.handleAbortProgress(this); }
 
     public boolean isSticky() { return (type & MSG_STICKY) > 0; }
     public void setSticky(boolean sticky) {
       if (isSticky() && !sticky) {
         setExitTime(t1 = gui.app.millis());  // try to exit normally...
         // ... but wait for any messages that appeared during the sticky period
         for (int i = msgs.size()-1; i > msgs.indexOf(this); i--)
           if (msgs.get(i).tE != Integer.MAX_VALUE)
             tE = PApplet.max(tE, msgs.get(i).tE);
         type = type & ~MSG_STICKY;
       } else if (!isSticky() && sticky) {
         tE = Integer.MAX_VALUE;
         type = type | MSG_STICKY;
       }
     }
 
     public boolean isRevoked() { return (type & MSG_REVOKED) > 0; }
     public void setRevoked(boolean revoked) {
       if (revoked) type = type | MSG_REVOKED;
       else type = type & ~MSG_REVOKED; }
 
     protected void setExitTime(int tRef) {
       tE = 1500;
       if (hasProgress())
         tE = (int)PApplet.min(tE, 1000*PApplet.sqrt((t1 - t0)/1000.f));  // make short progress msgs disappear very fast
       if ((type & 0xff) <= MSG_NOTE) tE *= 2;
       if ((type & 0xff) <= MSG_INFO) tE *= 1.5;
       if ((type & 0xff) <= MSG_WARNING) tE *= 1.25;
       if ((type & 0xff) <= MSG_ERROR) tE *= 1.1;
       tE = tRef + tE;
     }
 
     public String toString() {
       String res = (TConsole.this.tag != null) ? "[" + TConsole.this.tag + "] " : "";
       if (hasProgress()) {
         res += ">>> " + text.replace("\u2026", "...");  // remove Unicode ellipsis
         if (hasActiveProgress())
           res += String.format(" %.0f%%", 100*progress);
       } else {
         switch (type & 255) {
           case MSG_ERROR: res += "XXX"; break;
           case MSG_WARNING: res += "!!!"; break;
           case MSG_INFO: res += "###"; break;
           case MSG_NOTE: res += "==="; break;
           case MSG_DEBUG: res += "---"; break;
         }
         res += " " + text;
       }
       return res;
     }
   }
 
   Vector<Message> msgs = new Vector<Message>();
   Stack<Message> stickyMsgs = new Stack<Message>();
   Message msgprog = null;  // most recent message with progress bar
 
   public TConsole(TransparentGUI gui) { this(gui, null, false); }
   public TConsole(TransparentGUI gui, String tag) { this(gui, tag, false); }
   public TConsole(TransparentGUI gui, boolean showDebug) { this(gui, null, showDebug); }
   public TConsole(TransparentGUI gui, String tag, boolean showDebug) {
     super(gui);
     this.tag = tag;
     this.showDebug = showDebug;
     capturesMouse = false;
     setFocusable(false);
     setFontSize(fnsize);
     setPadding(10);
   }
 
   public String getTag() { return tag; }
   public void setTag(String tag) { this.tag = tag; }
 
   public boolean showsDebug() { return showDebug; }
   public void setShowDebug(boolean b) { showDebug = b; }
 
   public boolean isPersistent() { return persistent; }
   public void setPersistent(boolean b) { persistent = b; }
   public boolean isFancy() { return fancy; }
   public void setFancy(boolean b) { fancy = b; persistent = !b; }
 
   public int getAlignment() { return align; }
   public void setAlignment(int align) { this.align = align; }
 
   public float getFontSize() { return fnsize; }
   public void setFontSize(float fnsize) {
     this.fnsize = fnsize;
     fnNorm = gui.createFont("GillSans", fnsize);
     fnBold = gui.createFont("GillSans-Bold", fnsize);
     progbarheight = Math.round(.6f*fnsize);
     imgProgBar = gui.app.createImage(progbarwidth, progbarheight, PApplet.ARGB);
   }
 
   public void pushSticky(Message msg) { stickyMsgs.push(msg); msg.setSticky(true); }
   public Message popSticky() { Message msg = stickyMsgs.pop(); msg.setSticky(false); return msg; }
 
   public void clear() { msgs.clear(); if (!fancy) invalidate(); }
 
   public TComponent.Dimension getMinimumSize() {
     return new TComponent.Dimension(100, fancy ? 100 : msgs.isEmpty() ? 0 : fnsize + 1.25f*fnsize*(msgs.size() - 1)); }
 
   protected String minsec(int millis) {
     if (millis < 100)
       return "less than 0.1 seconds";
     if (millis < 1000)
       return PApplet.nf(millis/1000.f, 0, 1) + " seconds";
     int secs = Math.round(millis/1000);
     int mins = secs/60;
     secs = secs - mins*60;
     int hours = mins/60;
     mins = mins - hours*60;
     return ((hours > 0) ? hours + " hour" + ((hours == 1) ? "" : "s") + " " : "")
          + ((mins > 0) ? mins + " minute" + ((mins == 1) ? "" : "s") + " " : "")
          + ((secs > 0) ? secs + " second" + ((secs == 1) ? "" : "s") + "" : "");
   }
 
   protected Message log(Message msg) {
     if (msgprog != null) { gui.app.println(" [...]"); msgprog = null; }  // clean-up unfinished progress logging
     String msgstr = msg.toString() + "\n";
     if (msg.hasProgress()) msgstr = msgstr.substring(0, msgstr.length() - 4);  // strip " 0%\n"
     gui.app.print(msgstr);
     msgs.add(msg); if (!fancy) invalidate();
     if (!msg.hasProgress()) msg.setExitTime(msg.t0);
     if (msgs.size() > BUFFER_SIZE) msgs.remove(0);
     return msg;
   }
 
   public Message logError(String txt) { return log(new Message(MSG_ERROR, txt)); }
   public Message logError(String txt, Throwable e) { return logError(txt + prettyThrow(e)); }
   public Message logError(Throwable e) { return logError(prettyThrow(e, true)); }
   public Message logWarning(String txt) { return log(new Message(MSG_WARNING, txt)); }
   public Message logInfo(String txt) { return log(new Message(MSG_INFO, txt)); }
   public Message logNote(String txt) { return log(new Message(MSG_NOTE, txt)); }
   public Message logDebug(String txt) { return log(new Message(MSG_DEBUG, txt)); }
 
   public Message logProgress(String txt) { return msgprog = log(new Message(MSG_NOTE, txt, true)); }
   // short-cuts to updating the last progress message
   public void updateProgress(float val) { if (msgprog != null) msgprog.updateProgress(val); }
   public void updateProgress(float val, float maxval) { if (msgprog != null) msgprog.updateProgress(val, maxval); }
   public void finishProgress() { if (msgprog != null) msgprog.finishProgress(); }
   public void finishProgress(String addText) { if (msgprog != null) msgprog.finishProgress(addText); }
   public void abortProgress() { if (msgprog != null) msgprog.abortProgress(); }
   public Message abortProgress(Throwable e) { return abortProgress(prettyThrow(e, true)); }
   public Message abortProgress(String error, Throwable e) { return abortProgress(error + prettyThrow(e)); }
   public Message abortProgress(String error) {
     TConsole.Message prog = msgprog;
     if (msgprog != null) msgprog.abortProgress();
     TConsole.Message err = logError(error);
     err.tE = prog.tE = PApplet.max(err.tE, prog.tE);
     return err;
   }
   // the following functions are called by Message when progress updates happen, so that TConsole can update standard output
   protected void handleUpdateProgress(Message msg, float lastProgress) {
     if (msg != msgprog) return;  // not our business anymore
     if ((int)(10*msgprog.progress) != (int)(10*lastProgress))
       gui.app.print(" " + (int)(100*msgprog.progress) + "%");
   }
   protected void handleFinishProgress(Message msg) {
     if (msg != msgprog) return;  // not our business anymore
     gui.app.println(" -- " + minsec(msgprog.t1 - msgprog.t0));
     msgprog = null;
   }
   protected void handleAbortProgress(Message msg) {
     if (msg != msgprog) return;  // not our business anymore
     gui.app.println(" -- aborted");
     msgprog = null;
   }
 
   public Message getLastMessage() { return (msgs.size() > 0) ? msgs.lastElement() : null; }
 
   public static String prettyThrow(Throwable t) { return prettyThrow(t, false); }
   public static String prettyThrow(Throwable t, boolean firstCapital) {
     if (t == null) return "null";
     while (t.getCause() != null) t = t.getCause();  // FIXME: does this ignore too much?
     String name = t.getClass().getName();
     name = name.substring(name.lastIndexOf('.') + 1);
     if (name.equals("Exception"))
       return t.getMessage();  // this would yield an empty name
     if (name.endsWith("Exception"))
       name = name.substring(0, name.length() - 9);
     name = name.replaceAll("(.)([A-Z][a-z])", "$1 $2");  // this one does OutOfBounds -> Out OfBounds (because the tOf is matched, but f cannot be reused in fBo)
     name = name.replaceAll("([^ ])([A-Z][a-z])", "$1 $2");  // this one will fix all two letter camel-case words too
     name = name.toLowerCase();
     if (firstCapital) name = name.substring(0, 1).toUpperCase() + ((name.length() > 1) ? name.substring(1) : "");
     return name + ": " + t.getMessage();
   }
 
   protected float drawMessage(PGraphics g, Message msg, float x, float y) {
     if (Float.isNaN(msg.y*msg.a)) { msg.y = y; msg.a = 33/255.f; }  // initial message position is at target position
     float ta = (((gui.app.millis() > msg.tE) || msg.isRevoked()) && !persistent) ? 0 : 1;
     msg.a += 3*(ta - msg.a)*PApplet.min(gui.dt, 1.f/3);
     msg.y += 3*(y - msg.y)*PApplet.min(gui.dt, 1.f/3);
     float y0 = bounds.y + padding.top + (showDebug ? 2*fnsize : 0);
     float y1 = y0 + 5*fnsize;
     float a = 255*msg.a*(fancy ? PApplet.max(0, PApplet.min(1, (msg.y - y0)/(y1 - y0))) : 1);
     if (!fancy) {
       msg.a = ta; a = 255*msg.a;
       msg.y = y; if (y - g.textAscent() < y0) return 255*msg.a; }
     if (a < 1) return a;
     // draw message text
     g.noStroke();
     switch (msg.type & 255) {
       case MSG_ERROR: g.fill(200, 0, 0, a); g.textFont(fnBold); break;
       case MSG_WARNING: g.fill(200, 0, 0, a); g.textFont(fnNorm); break;
       case MSG_INFO: g.fill(0, a); g.textFont(fnBold); break;
       case MSG_NOTE: g.fill(0, a); g.textFont(fnNorm); break;
       case MSG_DEBUG: g.fill(0, .75f*a); g.textFont(fnNorm); break;
     }
     if (msg.isRevoked()) g.fill(127, a);
     String text = msg.text;
     float progbarWidth = msg.hasActiveProgress() ? progbarwidth : 0;  // stupid var names...
     float progbarSpace = msg.hasActiveProgress() ? g.textWidth(" ") : 0;
    while (g.textWidth(text) > bounds.width - padding.left - padding.right) {
       text = text.substring(0, text.length()-2) + "\u2026";  // shorten and add ellipsis
      if (msg.hasActiveProgress()) { progbarWidth = 50; progbarSpace = 0; } }  // cut down on the progbar
     float textX = (align == ALIGN_LEFT)   ? x :
                   (align == ALIGN_CENTER) ? x - (progbarWidth + progbarSpace)/2 :
                                             x - (progbarWidth + progbarSpace);
     g.text(text, textX, msg.y);
     if (msg.isRevoked()) {  // strike-through
       float tw = g.textWidth(text);
       float textX1 = textX - 3; if (align != ALIGN_LEFT) textX1 -= tw/2; if (align == ALIGN_RIGHT) textX1 -= tw/2;
       float textX2 = textX + 3; if (align != ALIGN_RIGHT) textX2 += tw/2; if (align == ALIGN_LEFT) textX2 += tw/2;
       float textYm = y - g.textDescent() - g.textAscent()/3;
       g.stroke(127, a);// g.strokeWeight(.5f);
       g.line(textX1, textYm, textX2, textYm);// g.strokeWeight(1);
     }
     // draw message's progress bar, if applicable
     if (msg.hasActiveProgress()) {
       imgProgBar.loadPixels();
       for (int px = 0; px < progbarwidth; px++)
         for (int py = 0; py < progbarheight; py++)
           imgProgBar.pixels[py*progbarwidth + px] =
             (gui.app.random(1) < msg.progress) ? gui.app.color(0, a) : gui.app.color(0, 0);
       imgProgBar.updatePixels();
       float progbarX = (align == ALIGN_LEFT)   ? x + g.textWidth(text) + progbarSpace :
                        (align == ALIGN_CENTER) ? x - (progbarWidth + progbarSpace)/2 + g.textWidth(text)/2 :
                                                  x - progbarWidth;
       g.image(imgProgBar, progbarX, msg.y - progbarheight + 1, progbarWidth, progbarheight);
     }
     return 255*msg.a;
   }
 
   private boolean showMillis = false;
   private int tLast = 0;
 
   public void draw(PGraphics g) {
     super.draw(g);
     // draw messages
     if (!msgs.isEmpty()) {
       float lambda = .001f;
       g.textAlign(align, g.BASELINE);
       float x = (align == ALIGN_LEFT)   ? bounds.x + padding.left :
                 (align == ALIGN_CENTER) ? bounds.x + bounds.width/2 :
                                           bounds.x + bounds.width - padding.right;
       float y = bounds.y + bounds.height - padding.bottom;
       g.imageMode(g.CORNER);
       float lastMessageAlpha = 0;
       for (int i = msgs.size() - 1; i >= 0; i--, y -= (lastMessageAlpha > 32) ? 1.25f*fnsize : 0)
         if ((msgs.get(i).type != MSG_DEBUG) || showDebug)
           lastMessageAlpha = drawMessage(g, msgs.get(i), x, y);
     }
     // draw memory usage and fps
     if (showDebug) {
       g.noStroke();
       float max = Runtime.getRuntime().maxMemory();
       float alloc = 50*Runtime.getRuntime().totalMemory()/max;
       float free = 50*Runtime.getRuntime().freeMemory()/max;
       max = 50;
       float x = bounds.x + bounds.width - padding.right;
       float y = bounds.y + padding.top;
       g.rectMode(g.CORNER);
       g.fill(0, 150, 0); g.rect(x - max, y, max, .5f*fnsize);
       g.fill(0, 200, 0); g.rect(x - max, y, alloc, .5f*fnsize);
       g.fill(200, 0, 0); g.rect(x - max, y, (alloc - free), .5f*fnsize);
       g.fill(0); g.textAlign(g.RIGHT, g.TOP); g.textFont(fnNorm);
       String str = Math.round(gui.app.frameRate) + " fps";
       int t = gui.app.millis();
       if (!showMillis && (t - tLast > 250)) showMillis = true;
       if (showMillis && (t - tLast < 200)) showMillis = false;
       if (showMillis) str = gui.app.nfc(t - tLast) + " ms";
       g.text(str, x, y + .6f*fnsize);
       tLast = t;
     }
   }
 
   public String toString() {
     String res = "";
     for (Message msg : msgs)
       res += msg.toString() + "\n";
     return res;
   }
 }
