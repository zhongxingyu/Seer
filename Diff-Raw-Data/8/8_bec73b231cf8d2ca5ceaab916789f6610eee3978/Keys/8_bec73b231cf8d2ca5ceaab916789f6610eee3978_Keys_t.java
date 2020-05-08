 package sps.io;
 
 import com.badlogic.gdx.Input;
 
 public enum Keys {
     Up(Input.Keys.UP),
     Down(Input.Keys.DOWN),
     Left(Input.Keys.LEFT),
     Right(Input.Keys.RIGHT),
     Enter(Input.Keys.ENTER),
     Backspace(Input.Keys.BACKSPACE),
     R(Input.Keys.R),
     D(Input.Keys.D),
     A(Input.Keys.A),
     S(Input.Keys.S),
    K(Input.Keys.K),
    Z(Input.Keys.Z),
    X(Input.Keys.X),
    C(Input.Keys.C),
     Tilde(Input.Keys.TAB),
     E(Input.Keys.E),
     Space(Input.Keys.SPACE);
 
     private final int _keyCode;
 
     private Keys(int keyCode) {
         _keyCode = keyCode;
     }
 
     public int getKeyCode() {
         return _keyCode;
     }
 
     public static Keys get(String s) {
         for (Keys key : values()) {
             if (key.name().equalsIgnoreCase(s)) {
                 return key;
             }
         }
         return null;
     }
 }
