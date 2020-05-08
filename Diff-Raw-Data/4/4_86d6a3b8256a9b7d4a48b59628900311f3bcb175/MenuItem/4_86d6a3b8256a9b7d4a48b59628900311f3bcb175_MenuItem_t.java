 package net.cscott.sdr.anim;
 
 import net.cscott.sdr.anim.TextureText.JustifyX;
 import net.cscott.sdr.anim.TextureText.JustifyY;
 
 import com.jme.input.AbsoluteMouse;
 import com.jme.input.action.InputActionEvent;
 import com.jme.input.action.MouseInputAction;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Node;
 
 /**
  * A {@link MenuItem} is one row of the {@link MenuState}.  It is in charge
  * of its own highlight state and input processing.
  * @author C. Scott Ananian
 * @version $Id: MenuItem.java,v 1.5 2006-11-28 06:46:31 cananian Exp $
  */
 public class MenuItem extends Node {
     private final TextureText label, value;
     private final MenuArrow leftArrow, rightArrow;
     private final String[] valueText;
     private int which = 0;
     private boolean isEnabled = false;
     public MenuItem(String nodeName, String labelText, BaseState st, String... valueText) {
         super(nodeName+"/Node");
         this.valueText = valueText;
         // menu label
         this.label = st._mkText(nodeName+"/label:", labelText, 128,
                 JustifyX.LEFT, JustifyY.MIDDLE, st.x(83-320),st.y(0),st.x(280),st.y(44));
         label.setColor(new ColorRGBA(.95f,.95f,.95f,1));
         this.attachChild(label);
         // menu arrows: height 44, width 24
 
         this.leftArrow = new MenuArrow
         (nodeName+"/arrow/left", st, true);
         leftArrow.getLocalTranslation().set(st.x(468-75-1-320),st.y(0),0);
         this.leftArrow.setCullMode(CULL_ALWAYS);
         this.attachChild(leftArrow);
 
         this.rightArrow = new MenuArrow
         (nodeName+"/arrow/right", st, false);
         rightArrow.getLocalTranslation().set(st.x(468+75+1-320),st.y(0),0);
         if (valueText.length<=1)
             this.rightArrow.setCullMode(CULL_ALWAYS);
         this.attachChild(rightArrow);
 
         // menu values
         this.value = st._mkText(nodeName+"/value:", getValue(which), 128,
                 JustifyX.CENTER, JustifyY.MIDDLE, st.x(468-320), 0, st.x(150),st.y(24));
         value.setColor(new ColorRGBA(1,1,0,1));
         this.attachChild(value);
     }
     public void setEnabled(boolean isEnabled) {
         this.isEnabled = isEnabled;
         float l = isEnabled ? 1f : .95f;
         this.label.setColor(new ColorRGBA(l, l, l, 1));
         this.value.setColor(new ColorRGBA(1,1,isEnabled?.5f:0,1));
     }
     protected String getValue(int which) { return valueText[which]; }
     private void update() {
         this.value.setText(getValue(which));
         this.leftArrow.setCullMode((which==0)?CULL_ALWAYS:CULL_NEVER);
         this.rightArrow.setCullMode((which==valueText.length-1)?CULL_ALWAYS:CULL_NEVER);
         onChange(which);
     }
     public void inc() {
         // XXX: flash right arrow?
         if (which<valueText.length-1) { which++; update(); }
     }
     public void dec() {
         // XXX: flash left arrow?
         if (which>0) { which--; update(); }
     }
     /** Subclasses can override this method to get notification of state
      * changes. */
     protected void onChange(int which) { }
     
     /////////// InputActions
     public MouseInputAction getMouseInputAction() {
         return new MouseInputAction() {
             @Override
             public void performAction(InputActionEvent event) {
                 if (!isEnabled) return; // only look for input if enabled
                 AbsoluteMouse am = (AbsoluteMouse) mouse;
                 if (mouse==null) return;
                 relativeLoc.set(am.getHotSpotPosition());
                 relativeLoc.subtractLocal(MenuItem.this.getLocalTranslation());
                 // we don't handle rotation or scaling at the moment.
                 assert MenuItem.this.getLocalRotation().isIdentity();
                 assert MenuItem.this.getLocalScale().equals(Vector3f.UNIT_XYZ);
 
                 Vector3f leftTrans = leftArrow.getLocalTranslation();
                 boolean inLeft = leftArrow.isInside
                     (relativeLoc.x-leftTrans.x, relativeLoc.y-leftTrans.y);
 
                 Vector3f rightTrans = rightArrow.getLocalTranslation();
                 boolean inRight =rightArrow.isInside
                     (relativeLoc.x-rightTrans.x, relativeLoc.y-rightTrans.y);
 
                 leftArrow.setSelected(inLeft);
                 rightArrow.setSelected(inRight);
                 if (event.getTriggerPressed()) {
                     if (inLeft) dec();
                     if (inRight) inc();
                 }
             }
             private final Vector3f relativeLoc = new Vector3f();
         };
     }
     
 }
