 /*
  * Copyright 2012 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of Michael Hoffer <info@michaelhoffer.de>.
  */
 package eu.mihosoft.vtk;
 
 import java.awt.event.*;
 
 import javax.swing.Timer;
 import vtk.*;
 
 /**
  * Almost exact copy of original {@link vtk.vtkCanvas}.
  *
  * It is used internally only. We can now study and experiment without the need
  * to change the original code. We will probably change mouse gestures and some
  * keys.
  *
  * @author Michael Hoffer <info@michaelhoffer.de>
  *
  * <p><b>Original Description:</b></p>
  *
  * Java AWT component that encapsulate vtkRenderWindow, vtkRenderer, vtkCamera,
  * vtkLight.
  *
  *
  * @author Kitware
  */
 public class VTKCanvas extends VTKPanel implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
 
     private static final long serialVersionUID = 1L;
     protected vtkGenericRenderWindowInteractor iren = new vtkGenericRenderWindowInteractor();
     protected Timer timer = new Timer(10, new VTKCanvas.DelayAction());
     protected int ctrlPressed = 0;
     protected int shiftPressed = 0;
     protected vtkPlaneWidget pw = new vtkPlaneWidget();
     protected vtkBoxWidget bw = new vtkBoxWidget();
     
     private double defaultCamPosX = 0;
     private double defaultCamPosY = 0;
    private double defaultCamPosZ = 8;
     
     public void setDefaultCamPos(double x, double y, double z) {
         setDefaultCamPosX(x);
         setDefaultCamPosY(y);
         setDefaultCamPosZ(z);
     }
 
     static {
         // load up hybrid for 3d widgets
         vtkNativeLibrary.HYBRID.LoadLibrary();
         vtkNativeLibrary.WIDGETS.LoadLibrary();
     }
 
     @Override
     public void Delete() {
         iren = null;
         pw = null;
         bw = null;
         super.Delete();
     }
 
     public VTKCanvas() {
         super();
         Initialize();
     }
 
     public VTKCanvas(vtkRenderWindow renwin) {
         super(renwin);
         Initialize();
     }
 
     protected void Initialize() {
         iren.SetRenderWindow(rw);
         iren.TimerEventResetsTimerOff();
         iren.AddObserver("CreateTimerEvent", this, "StartTimer");
         iren.AddObserver("DestroyTimerEvent", this, "DestroyTimer");
         iren.SetSize(200, 200);
         iren.ConfigureEvent();
         pw.AddObserver("EnableEvent", this, "BeginPlaneInteraction");
         bw.AddObserver("EnableEvent", this, "BeginBoxInteraction");
         pw.SetKeyPressActivationValue('l');
         bw.SetKeyPressActivationValue('b');
 
         pw.SetInteractor(iren);
         bw.SetInteractor(iren);
 
         addComponentListener(new ComponentAdapter() {
 
             public void componentResized(ComponentEvent event) {
                 // The Canvas is being resized, get the new size
                 int width = getWidth();
                 int height = getHeight();
                 setSize(width, height);
             }
         });
 
         ren.SetBackground(0.0, 0.0, 0.0);
 
         // Setup same interactor style than vtkPanel
         vtkInteractorStyleTrackballCamera style = new vtkInteractorStyleTrackballCamera();
         iren.SetInteractorStyle(style);
 
         addMouseWheelListener(this);
     }
 
     public void StartTimer() {
         if (timer.isRunning()) {
             timer.stop();
         }
 
         timer.setRepeats(true);
         timer.start();
     }
 
     public void DestroyTimer() {
         if (timer.isRunning()) {
             timer.stop();
         }
     }
 
     /**
      * Replace by getRenderWindowInteractor()
      */
     @Deprecated
     public vtkGenericRenderWindowInteractor getIren() {
         return this.iren;
     }
 
     public vtkGenericRenderWindowInteractor getRenderWindowInteractor() {
         return this.iren;
     }
 
     public void setInteractorStyle(vtkInteractorStyle style) {
         iren.SetInteractorStyle(style);
     }
 
     public void addToPlaneWidget(vtkProp3D prop) {
         pw.SetProp3D(prop);
         pw.PlaceWidget();
     }
 
     public void addToBoxWidget(vtkProp3D prop) {
         bw.SetProp3D(prop);
         bw.PlaceWidget();
     }
 
     public void BeginPlaneInteraction() {
         System.out.println("Plane widget begin interaction");
     }
 
     public void BeginBoxInteraction() {
         System.out.println("Box widget begin interaction");
     }
 
     public void setSize(int x, int y) {
         super.setSize(x, y);
         if (windowset == 1) {
             Lock();
             rw.SetSize(x, y);
             iren.SetSize(x, y);
             iren.ConfigureEvent();
             UnLock();
         }
     }
 
     public void resetCamera() {
         Lock();
         ren.ResetCamera();
         cam.SetPosition(getDefaultCamPosX(), getDefaultCamPosY(), getDefaultCamPosZ());
         cam.SetRoll(0);
         UpdateLight();
         UnLock();
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mousePressed(MouseEvent e) {
         if (ren.VisibleActorCount() == 0) {
             return;
         }
         Lock();
         rw.SetDesiredUpdateRate(5.0);
         lastX = e.getX();
         lastY = e.getY();
 
         ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1 : 0;
         shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1 : 0;
 
         iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed, shiftPressed, '0', 0, "0");
 
         if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
             iren.LeftButtonPressEvent();
         } else if ((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
             iren.MiddleButtonPressEvent();
         } else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
             iren.RightButtonPressEvent();
         }
         UnLock();
     }
 
     public void mouseReleased(MouseEvent e) {
         rw.SetDesiredUpdateRate(0.01);
 
         ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1 : 0;
         shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1 : 0;
 
         iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed, shiftPressed, '0', 0, "0");
 
         if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
             Lock();
             iren.LeftButtonReleaseEvent();
             UnLock();
         }
 
         if ((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
             Lock();
             iren.MiddleButtonReleaseEvent();
             UnLock();
         }
 
         if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
             Lock();
             iren.RightButtonReleaseEvent();
             UnLock();
         }
     }
 
     public void mouseEntered(MouseEvent e) {
         this.requestFocus();
         iren.SetEventInformationFlipY(e.getX(), e.getY(), 0, 0, '0', 0, "0");
         iren.EnterEvent();
     }
 
     public void mouseExited(MouseEvent e) {
         iren.SetEventInformationFlipY(e.getX(), e.getY(), 0, 0, '0', 0, "0");
         iren.LeaveEvent();
     }
 
     public void mouseMoved(MouseEvent e) {
         lastX = e.getX();
         lastY = e.getY();
 
         ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1 : 0;
         shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1 : 0;
 
         iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed, shiftPressed, '0', 0, "0");
 
         Lock();
         iren.MouseMoveEvent();
         UnLock();
     }
 
     public void mouseDragged(MouseEvent e) {
         if (ren.VisibleActorCount() == 0) {
             return;
         }
 
         ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1 : 0;
         shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1 : 0;
 
         iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed, shiftPressed, '0', 0, "0");
 
         Lock();
         iren.MouseMoveEvent();
         UnLock();
 
         UpdateLight();
     }
 
     @Override
     public void mouseWheelMoved(MouseWheelEvent e) {
         ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1 : 0;
         shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1 : 0;
 
         iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed, shiftPressed, '0', 0, "0");
 
         Lock();
         if (e.getWheelRotation() > 0) {
             System.out.println("Forward");
             iren.MouseWheelForwardEvent();
         } else {
             System.out.println("Backward");
             iren.MouseWheelBackwardEvent();
         }
         UnLock();
     }
 
     public void keyTyped(KeyEvent e) {
     }
 
     public void keyPressed(KeyEvent e) {
         if (ren.VisibleActorCount() == 0) {
             return;
         }
         char keyChar = e.getKeyChar();
 
         ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1 : 0;
         shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1 : 0;
 
         iren.SetEventInformationFlipY(lastX, lastY, ctrlPressed, shiftPressed, keyChar, 0, String.valueOf(keyChar));
 
         Lock();
         iren.KeyPressEvent();
         iren.CharEvent();
         UnLock();
     }
 
     public void keyReleased(KeyEvent e) {
     }
 
     /**
      * @return the defaultCamPosX
      */
     public double getDefaultCamPosX() {
         return defaultCamPosX;
     }
 
     /**
      * @param defaultCamPosX the defaultCamPosX to set
      */
     public void setDefaultCamPosX(double defaultCamPosX) {
         this.defaultCamPosX = defaultCamPosX;
     }
 
     /**
      * @return the defaultCamPosY
      */
     public double getDefaultCamPosY() {
         return defaultCamPosY;
     }
 
     /**
      * @param defaultCamPosY the defaultCamPosY to set
      */
     public void setDefaultCamPosY(double defaultCamPosY) {
         this.defaultCamPosY = defaultCamPosY;
     }
 
     /**
      * @return the defaultCamPosZ
      */
     public double getDefaultCamPosZ() {
         return defaultCamPosZ;
     }
 
     /**
      * @param defaultCamPosZ the defaultCamPosZ to set
      */
     public void setDefaultCamPosZ(double defaultCamPosZ) {
         this.defaultCamPosZ = defaultCamPosZ;
     }
 
     private class DelayAction implements ActionListener {
 
         public void actionPerformed(ActionEvent evt) {
             Lock();
             iren.TimerEvent();
             UpdateLight();
             UnLock();
         }
     }
 }
