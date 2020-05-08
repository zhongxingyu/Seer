 package com.phybots.picode.api;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import com.phybots.picode.camera.Camera;
 
 public abstract class Poser {
 
 	public final static String identifierSeparator = "/";
 
 	protected MotorManager motorManager;
 
 	private String name;
 
 	private PoserTypeInfo poserType;
 
 	public Poser() {
 		setPoserType(PoserLibrary.getTypeInfo(this));
 		initialize();
 		PoserLibrary.getInstance().addPoser(this);
 	}
 	
 	protected abstract void initialize();
 
 	public MotorManager getMotorManager() {
 		return motorManager;
 	}
 
 	public Camera getCamera() {
 		return PoserLibrary.getInstance().getCameraManager().getCamera(this);
 	}
 
 	private void setPoserType(PoserTypeInfo poserType) {
 		this.poserType = poserType;
 	}
 
 	public PoserTypeInfo getPoserType() {
 		return poserType;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void dispose() {
 		getMotorManager().reset();
 		PoserLibrary.getInstance().removePoser(this);
 	}
 
 	public boolean setPose(Pose pose) {
 		return getMotorManager().setPose(pose);
 	}
 
 	public Pose getPose() {
 		return getMotorManager().getPose();
 	}
 
 	public Pose capture() {
 		PoseLibrary poseLibrary = PoseLibrary.getInstance();
 		Pose pose = getPose();
 		if (pose == null) {
 			return pose;
 		}
 		pose.setName(poseLibrary.newName());
 		Camera camera = getCamera();
 		if (camera != null) {
 			BufferedImage image = camera.getImage();
 			boolean isShowingHuman = this instanceof Human;
			if (!isShowingHuman) {
 				BufferedImage invertedImage = new BufferedImage(
 						image.getWidth(),
 						image.getHeight(),
 						image.getType());
 				Graphics g = invertedImage.createGraphics();
 				int x = image.getWidth() - 1;
 				int y = image.getHeight() - 1;
 				g.drawImage(image,
 						x, 0, 0, y,
 						0, 0, x, y, null);
 				g.dispose();
 				image = invertedImage;
 			}
 			pose.setPhoto(image);
 		}
 		try {
 			pose.save();
 		} catch (IOException e) {
 			return null;
 		}
 		poseLibrary.addPose(pose);
 		return pose;
 	}
 
 	public boolean isActing() {
 		return getMotorManager().isActing();
 	}
 
 	public Action action() {
 		Action action = new Action(this);
 		return action;
 	}
 
 	public void showCaptureFrame(boolean isVisible) {
 		PoserLibrary.getInstance().setCurrentPoser(this);
 		PoserLibrary.getInstance().showCameraFrame(isVisible);
 	}
 
 	public String getIdentifier() {
 		return getClass().getSimpleName();
 	}
 
 	public PoserInfo getInfo() {
 		PoserInfo poserInfo = new PoserInfo();
 		poserInfo.name = getName();
 		poserInfo.type = getPoserType();
 		return poserInfo;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s (%s)", name, getClass().getSimpleName());
 	}
 
 }
