 package com.smarthome;
 
 import rajawali.Camera;
 
 public class CameraController {
	SmartHomeActivity activity;
 	private float t;
 	private long timestamp;
 	private float duration = 1500;
 	private float[] p0 = new float[6], p1 = new float[6],
 			p2 = new float[6], p3 = new float[6];
 	private Camera src = new Camera();
 	private Camera tar = new Camera();
 	public boolean ready() {
 		return src != null && tar != null;
 	}
 	public float b(float t, float p0, float p1, float p2, float p3) {
 		float tn = 1 - t;
 		float tn2 = tn * tn;
 		float t2 = t * t;
 		return tn * tn2 * p0 + 3 * tn2 * t * p1 + 3 * tn * t2 * p2 + t2 * t * p3;
 	}
 	public void logSource() {
 		t = 0;
 		timestamp = System.currentTimeMillis();
 		src.setPosition(activity.mRenderer.getCamera().getPosition());
 		src.setRotation(activity.mRenderer.getCamera().getRotation());
 	}
 	public Camera giveSource() {
 		Camera cam = new Camera();
 		cam.setPosition(src.getPosition());
 		cam.setRotation(src.getRotation());
 		return cam;
 	}
 	public void setTarget(Camera target) {
 		tar = target;
 	}
 	public void setCamera() {
 		setCamera(Math.min(1, (System.currentTimeMillis() - timestamp) / duration));
 	}
 	public void setCamera(float t) {
 		Camera cam = activity.mRenderer.getCamera();
 		cam.setX(b(t, src.getX(), src.getX(), tar.getX(), tar.getX()));
 		cam.setY(b(t, src.getY(), src.getY(), tar.getY(), tar.getY()));
 		cam.setZ(b(t, src.getZ(), src.getZ(), tar.getZ(), tar.getZ()));
 		cam.setRotX(b(t, src.getRotX(), src.getRotX(), tar.getRotX(), tar.getRotX()));
 		cam.setRotY(b(t, src.getRotY(), src.getRotY(), tar.getRotY(), tar.getRotY()));
 		cam.setRotZ(b(t, src.getRotZ(), src.getRotZ(), tar.getRotZ(), tar.getRotZ()));
 	}
 	public Camera getCamera(float t) {
 		Camera cam = new Camera();
 		cam.setX(b(t, src.getX(), src.getX(), tar.getX(), tar.getX()));
 		cam.setY(b(t, src.getY(), src.getY(), tar.getY(), tar.getY()));
 		cam.setZ(b(t, src.getZ(), src.getZ(), tar.getZ(), tar.getZ()));
 		cam.setRotX(b(t, src.getRotX(), src.getRotX(), tar.getRotX(), tar.getRotX()));
 		cam.setRotY(b(t, src.getRotY(), src.getRotY(), tar.getRotY(), tar.getRotY()));
 		cam.setRotZ(b(t, src.getRotZ(), src.getRotZ(), tar.getRotZ(), tar.getRotZ()));
 		return cam;
 	}
 	public void createRelativeDisplacement(float x, float y, float z, float rotX, float rotY, float rotZ) {
 		Camera cam = giveSource();
 		cam.setX(cam.getX() + x);
 		cam.setY(cam.getY() + y);
 		cam.setZ(cam.getZ() + z);
 		cam.setRotX(cam.getRotX() + x);
 		cam.setRotY(cam.getRotY() + y);
 		cam.setRotZ(cam.getRotZ() + z);
 		setTarget(cam);
 	}
 }
