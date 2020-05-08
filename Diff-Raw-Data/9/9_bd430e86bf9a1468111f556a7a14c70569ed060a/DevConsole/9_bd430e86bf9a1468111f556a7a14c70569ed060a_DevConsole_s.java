 package sps.core;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import sps.bridge.DrawDepths;
 import sps.graphics.Assets;
 import sps.graphics.Renderer;
 import sps.text.TextPool;
 
 public class DevConsole {
 
    public static final int margin = 50;
 
     private class ConsoleText {
         private Point2 position = new Point2(0, 0);
         private String content;
 
         public ConsoleText(int x, int y, String content) {
            this.position.reset(0, 0, false);
             this.content = content;
         }
 
         public void draw() {
             TextPool.get().write(content, position);
         }
 
         public String getContent() {
             return content;
         }
     }
 
     private static DevConsole __instance;
 
     public static DevConsole get() {
         if (__instance == null) {
             __instance = new DevConsole();
         }
         return __instance;
     }
 
     private final int messageLimit = 20;
     private final ConsoleText[] _contents = new ConsoleText[messageLimit];
     private int _index = 0;
     private boolean _isVisible = false;
     private final Color _bgColor;
     private final Sprite _consoleBase;
 
     private DevConsole() {
         _bgColor = Color.BLACK;
         _bgColor.a = (byte) 180;
         _consoleBase = Assets.get().baseMenu();
         add("The development console has been started.");
     }
 
     private int getY(int index) {
         return Renderer.get().VirtualHeight - (index * margin / 2);
     }
 
     public void add(String message) {
         if (_index < _contents.length) {
             _contents[_index++] = new ConsoleText(margin, getY(_index), message);
        } else {
             for (int ii = 0; ii < _contents.length - 1; ii++) {
                 _contents[ii] = new ConsoleText(margin, getY(ii), _contents[ii + 1].getContent());
             }
             _contents[_contents.length - 1] = new ConsoleText(margin, getY(_contents.length - 1), message);
         }
     }
 
     public void draw() {
         if (_isVisible) {
             Renderer.get().draw(_consoleBase, Point2.Zero, DrawDepths.get("DevConsole"), _bgColor, Renderer.get().VirtualWidth, Renderer.get().VirtualHeight);
             for (ConsoleText _content : _contents) {
                 if (_content != null) {
                     _content.draw();
                 }
             }
         }
     }
 
     public void toggle() {
         if (SpsConfig.get().devConsoleEnabled) {
             _isVisible = !_isVisible;
         }
     }
 }
