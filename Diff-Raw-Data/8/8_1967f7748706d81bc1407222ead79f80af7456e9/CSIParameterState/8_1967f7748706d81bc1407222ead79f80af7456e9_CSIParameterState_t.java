 package terminator.terminal.states;
 
 import e.util.*;
 import java.awt.*;
 
 import terminator.*;
 import terminator.terminal.*;
 import terminator.terminal.actions.*;
 
 public class CSIParameterState extends State {
         private static CSIParameterState instance = new CSIParameterState();
 
         private static boolean isPrivate = false;
         private static CSIParameters parameters = new CSIParameters();
 
         public static State enter() {
                 isPrivate = false;
                 parameters.clear();
                 return instance;
         }
 
         public static State enter(ActionQueue actions, char c) {
                 enter();
                 if (c == '?') {
                         isPrivate = true;
                         return instance;
                 }
                 return instance.process(actions, c);
         }
 
         public State process(ActionQueue actions, char c) {
                 if (c == ';') {
                         parameters.next();
                         return this;
                 }
                 if ('0' <= c && c <= '9') {
                         parameters.process(c);
                         return this;
                 }
                 if (0x20 <= c && c <= 0x3f)
                         return CSIIgnoreState.enter();
                 if (0x40 <= c && c <= 0x7e) {
                         enqueueCSI(actions, c);
                         return GroundState.enter();
                 }
                 return super.process(actions, c);
         }
 
         private void enqueueCSI(ActionQueue actions, char c) {
                 switch (c) {
                 case 'A': actions.add(new MoveCursorUp(parameters.getCount())); break;
                 case 'B': actions.add(new MoveCursorDown(parameters.getCount())); break;
                 case 'C': actions.add(new MoveCursorRight(parameters.getCount())); break;
                 case 'D': actions.add(new MoveCursorLeft(parameters.getCount())); break;
                 case 'H': actions.add(new PositionCursor(parameters.getInt(0), parameters.getInt(1))); break;
                 case 'K': csiClearLine(actions); break;
                 case 'J': csiClearScreen(actions); break;
                 case 'L': actions.add(new InsertLines(parameters.getCount())); break;
                 case 'M': actions.add(new DeleteLines(parameters.getCount())); break;
                 case 'P': actions.add(new DeleteCharacters(parameters.getCount())); break;
                 case 'h': csiSetModes(actions, true); break;
                 case 'l': csiSetModes(actions, false); break;
                 case 'm': csiModifyStyles(actions); break;
                 case 'r': actions.add(new SetScrollingRegion(parameters.getInt(0), parameters.getInt(1))); break;
                 default: Log.warn("Unknown CSI sequence " + c); break;
                 }
         }
 
         private void csiClearLine(ActionQueue actions) {
                 switch (parameters.getType()) {
                 case 0: actions.add(new ClearToEndOfLine()); break;
                 case 1: actions.add(new ClearToBeginningOfLine()); break;
                 default: Log.warn("Unknown line clearing request " + parameters.getType()); break;
                 }
         }
 
         private void csiClearScreen(ActionQueue actions) {
                 switch (parameters.getType()) {
                 case 0: actions.add(new ClearToEndOfScreen()); break;
                 default: Log.warn("Unknown screen clearing request " + parameters.getType()); break;
                 }
         }
 
         private void csiSetModes(ActionQueue actions, boolean on) {
                 for (int i = 0; i < parameters.count(); i++)
                         if (isPrivate)
                                 csiSetPrivateMode(actions, parameters.get(i), on);
                         else
                                 csiSetMode(actions, parameters.get(i), on);
         }
 
         private void csiSetPrivateMode(ActionQueue actions, int mode, boolean on) {
                 switch (mode) {
                 case 25: actions.add(new SetCursorVisible(on)); break;
                 default: Log.warn("Unknown private mode " + mode); break;
                 }
         }
 
         private void csiSetMode(ActionQueue actions, int mode, boolean on) {
                 switch (mode) {
                 case 4: actions.add(new SetInsertMode(on)); break;
                 default: Log.warn("Unknown mode " + mode); break;
                 }
         }
 
         private void csiModifyStyles(ActionQueue actions) {
                 ModifyStyle style = new ModifyStyle();
                 int i = 0;
                 while (i < parameters.count())
                         i = csiModifyStyle(style, i);
                 actions.add(style);
         }
 
         private int csiModifyStyle(ModifyStyle style, int i) {
                 int parameter = parameters.get(i++);
                 switch (parameter) {
                 case 0:
                         style.clearForeground();
                         style.clearBackground();
                         style.reverseVideo(false);
                         style.underline(false);
                         break;
                 case 4:
                         style.underline(true);
                         break;
                 case 7:
                         style.reverseVideo(true);
                         break;
                 case 24:
                         style.underline(false);
                         break;
                 case 27:
                         style.reverseVideo(false);
                         break;
                 case 30: case 31: case 32: case 33: case 34: case 35: case 36: case 37:
                         style.foreground(Palettes.getColor(parameter - 30));
                         break;
                 case 38:
                         i = csiModifyExtendedColor(style, i, true);
                         break;
                 case 39:
                         style.clearForeground();
                         break;
                 case 40: case 41: case 42: case 43: case 44: case 45: case 46: case 47:
                         style.background(Palettes.getColor(parameter - 40));
                         break;
                 case 48:
                         i = csiModifyExtendedColor(style, i, false);
                         break;
                 case 49:
                         style.clearBackground();
                         break;
                 default:
                         Log.warn("Unknown style attribute " + parameter);
                         break;
                 }
                 return i;
         }
 
         private int csiModifyExtendedColor(ModifyStyle style, int i, boolean foreground) {
                 if (parameters.get(i++) != 5)
                         return i + 7;
                 if (i >= parameters.count())
                         return i;
                 Color color = Palettes.getColor(parameters.get(i++));
                 if (foreground)
                         style.foreground(color);
                 else
                         style.background(color);
                 return i;
         }
 }
