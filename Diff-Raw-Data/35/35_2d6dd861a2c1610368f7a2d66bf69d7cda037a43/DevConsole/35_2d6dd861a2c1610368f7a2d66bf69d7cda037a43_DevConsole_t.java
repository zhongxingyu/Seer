 package sps.core;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import sps.bridge.DrawDepths;
 import sps.graphics.Assets;
 import sps.graphics.Renderer;
 import sps.text.Text;
 import sps.text.TextPool;
 
 public class DevConsole {
 
     public static final int margin = 70;
 
     private class ConsoleText {
         private Point2 position = new Point2(0, 0);
         private Text content;
 
 
         public ConsoleText(int x, int y, String content) {
             this.position.reset(x, y, false);
             this.content = TextPool.get().write(content, position);
            this.content.setVisible(_isVisible);
         }
 
         public void draw() {
            if (_isVisible) {
                 content.draw();
             }
         }
 
        public Text getContent() {
            return content;
         }
 
         public void setContent(String content) {
             this.content.setMessage(content);
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
    private boolean _isVisible;
     private final Color _bgColor;
     private final Sprite _consoleBase;
 
     private DevConsole() {
         _bgColor = Color.BLACK;
         _bgColor.a = (byte) 180;
         _consoleBase = Assets.get().baseMenu();
        _isVisible = false;
         add("The development console has been started.");
     }
 
     private int getY(int index) {
         return Renderer.get().VirtualHeight - (index * margin / 2);
     }
 
     public void add(String message) {
         if (_index < _contents.length) {
             _contents[_index++] = new ConsoleText(margin, getY(_index), message);
         }
         else {
             for (int ii = 0; ii < _contents.length - 1; ii++) {
                _contents[ii].setContent(_contents[ii + 1].getContent().getMessage());
             }
             _contents[_contents.length - 1].setContent(message);
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
            for (int ii = 0; ii < _contents.length; ii++) {
                if (_contents[ii] != null) {
                    _contents[ii].getContent().setVisible(_isVisible);
                }
            }
         }
     }
 }
