 package spx.devtools;
 
 import spx.bridge.DrawDepth;
 import spx.core.SpxManager;
 import spx.text.TextType;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 public class DevConsole {
	private class ConsoleText extends spx.text.Text {
 		public ConsoleText(int x, int y, String content) {
 			super(content, x, y, TextType.Action);
 		}
 
 		@Override
 		public void Draw() {
 			SpxManager.Renderer.DrawString(_contents, _position, Color.WHITE, 1.0f, DrawDepth.DevConsoleText);
 		}
 
 		public String GetContent() {
 			return _contents;
 		}
 	}
 
 	private static DevConsole __instance;
 
 	public static DevConsole Get() {
 		if (__instance == null) {
 			__instance = new DevConsole();
 		}
 		return __instance;
 	}
 
 	private final ConsoleText[] _contents = new ConsoleText[10];
 	private int _index = 0;
 	private boolean _isVisible = false;
 	private final Color _bgColor;
 	private final Sprite _consoleBase;
 
 	private DevConsole() {
 		_bgColor = Color.BLACK;
 		_bgColor.a = (byte) 180;
 		_consoleBase = SpxManager.GetMenuBaseAsset();
		Add("The console has been started.");
 	}
 
 	public void Add(String message) {
 		if (_index < _contents.length) {
			_contents[_index++] = new ConsoleText(50, _index * 50 + 50, message);
 		}
 		else {
 			for (int ii = 0; ii < _contents.length - 1; ii++) {
				_contents[ii] = new ConsoleText(50, ii * 50 + 50, _contents[ii + 1].GetContent());
 			}
			_contents[_contents.length - 1] = new ConsoleText(50, (_contents.length - 1) * 50 + 50, message);
 		}
 	}
 
 	public void Draw() {
 		if (_isVisible) {
			SpxManager.Renderer.Draw(_consoleBase, SpxManager.GetCenter(), DrawDepth.DevConsole, _bgColor, SpxManager.WindowWidth, SpxManager.WindowHeight);
 			for (int ii = 0; ii < _contents.length; ii++) {
 				if (_contents[ii] != null) {
 					_contents[ii].Draw();
 				}
 			}
 		}
 	}
 
 	public void Toggle() {
 		_isVisible = !_isVisible;
 	}
 }
