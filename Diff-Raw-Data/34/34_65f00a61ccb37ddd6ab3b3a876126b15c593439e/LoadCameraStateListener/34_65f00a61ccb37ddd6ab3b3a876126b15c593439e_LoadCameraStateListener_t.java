 package com.navid.trafalgar.mod.windtunnel.statelisteners;
 
 import com.jme3.input.FlyByCamera;
 import com.jme3.input.InputManager;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.renderer.Camera;
 import com.navid.trafalgar.camera.ChaseCamera;
 import com.navid.trafalgar.manager.EventListener;
 import com.navid.trafalgar.manager.EventManager;
 import com.navid.trafalgar.manager.LoadCamState;
 import com.navid.trafalgar.manager.StartedState;
 import com.navid.trafalgar.mod.windtunnel.WindTunnelGameModel;
 import com.navid.trafalgar.mod.windtunnel.WindTunnelMainScreen;
 import com.navid.trafalgar.model.AShipModel;
 import com.navid.trafalgar.model.GameStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author anf
  */
 public class LoadCameraStateListener implements LoadCamState, StartedState, EventListener {
 
     /**
      * @param gameModel the gameModel to set
      */
     public void setGameModel(WindTunnelGameModel gameModel) {
         this.gameModel = gameModel;
     }
 
     /**
      * @param gameStatus the gameStatus to set
      */
     public void setGameStatus(GameStatus gameStatus) {
         this.gameStatus = gameStatus;
     }
 
     /**
      * @param inputManager the inputManager to set
      */
     public void setInputManager(InputManager inputManager) {
         this.inputManager = inputManager;
     }
 
     /**
      * @param eventManager the eventManager to set
      */
     public void setEventManager(EventManager eventManager) {
         this.eventManager = eventManager;
     }
 
     private static enum Cameras {
 
         NONE, FLYCAM, TARGETCAM, CHASECAM
     };
     
     @Autowired
     public void setCounterClockMainScreen(WindTunnelMainScreen screen){
         screen.setCameraManager(this);
     }
     
     @Autowired
     private GameStatus gameStatus;
     @Autowired
     private InputManager inputManager;
     @Autowired
     private EventManager eventManager;
     
     
     private FlyByCamera flyCamControl;
     private ChaseCamera chaseCamControl;
     private Camera camera;
     private AShipModel player;
     private String[] mappings = {"Cam1", "Cam2", "Cam3"};
     private LoadCameraStateListener.Cameras selectedCam = LoadCameraStateListener.Cameras.NONE;
     
     @Autowired
     private WindTunnelGameModel gameModel;
     
     private ActionListener actionListener = new ActionListener() {
 
         public void onAction(String name, boolean isPressed, float tpf) {
             if (isPressed) {
 
                 if (name.equals("Cam1")) {
                     setCamera1();
                 }
                 if (name.equals("Cam2")) {
                     setCamera2();
                 }
             }
         }
     };
 
     public void setCamera1() {
         eventManager.fireEvent("DEACTIVATE_CAM");
         eventManager.fireEvent("ACTIVATE_CAM1");
     }
 
     public void setCamera2() {
         eventManager.fireEvent("DEACTIVATE_CAM");
         eventManager.fireEvent("ACTIVATE_CAM2");
     }
 
     public void onLoadCam(float tpf) {
 
         this.camera = gameStatus.getCamera();
         this.player = gameModel.getShip();
 
         // Create a flying cam
         flyCamControl = new FlyByCamera(camera);
         flyCamControl.registerWithInput(inputManager);
         flyCamControl.setMoveSpeed(200);
         flyCamControl.setDragToRotate(true);
 
         
         // Enable a chasing cam
         chaseCamControl = new ChaseCamera(camera, player, inputManager);
         chaseCamControl.setSmoothMotion(true);
        chaseCamControl.setMinDistance(100);
        chaseCamControl.setMinDistance(150);
 
         inputManager.addListener(actionListener, mappings); // load my custom keybinding
 
         eventManager.registerListener(this, new String[]{"DEACTIVATE_CAM", "ACTIVATE_CAM1", "ACTIVATE_CAM2", "PAUSE", "RESUME"});
         eventManager.fireEvent("DEACTIVATE_CAM");
        eventManager.fireEvent("ACTIVATE_CAM2");
     }
 
     public void onStarted(float tpf) {
     }
 
     public void onUnload() {
         //flyCamControl.unregisterInput();
 
         gameStatus.getGameNode().removeControl(chaseCamControl);
 
         inputManager.removeListener(actionListener);
         eventManager.fireEvent("DEACTIVATE_CAM");
     }
 
     public void onEvent(String event) {
         if ("DEACTIVATE_CAM".equals(event)) {
             flyCamControl.setEnabled(false);
             chaseCamControl.setEnabled(false);
             flyCamControl.setDragToRotate(true);
 
         } else if ("ACTIVATE_CAM1".equals(event)) {
             selectedCam = LoadCameraStateListener.Cameras.FLYCAM;
             flyCamControl.setEnabled(true);
 
         } else if ("ACTIVATE_CAM2".equals(event)) {
             selectedCam = LoadCameraStateListener.Cameras.CHASECAM;
             chaseCamControl.setEnabled(true);
 
         } else if ("PAUSE".equals(event)) {
             if (selectedCam == LoadCameraStateListener.Cameras.FLYCAM) {
                 flyCamControl.setEnabled(false);
             }
         } else if ("RESUME".equals(event)) {
             if (selectedCam == LoadCameraStateListener.Cameras.FLYCAM) {
                 flyCamControl.setEnabled(true);
             }
         }
     }
 }
