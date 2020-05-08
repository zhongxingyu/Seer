 package org.haldean.chopper.server;
 
 import javax.media.j3d.*;
 import javax.vecmath.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import com.sun.j3d.utils.universe.*; 
 import com.sun.j3d.utils.geometry.*;
 import java.util.*;
 
 public class OrientationComponent extends JPanel {
     TransformGroup chopperRotator;
     SetAngleBehavior angleBehavior;
 
     public OrientationComponent() {
 	super(new GridLayout(1,1));
 	GraphicsConfiguration config = 
 	    SimpleUniverse.getPreferredConfiguration();
 	Canvas3D c3d = new Canvas3D(config);
 	add(c3d);
 
 	SimpleUniverse u = new SimpleUniverse(c3d);
 	u.getViewingPlatform().setNominalViewingTransform();
 
 	u.addBranchGraph(createSceneGraph());
     }
 
     public String getName() {
 	return "Orientation";
     }
 
     private BranchGroup createSceneGraph() {
 	BranchGroup objectRoot = new BranchGroup();
 
 	TransformGroup chopperModel = getChopperModel();
 
 	/* The region in which the behavior is allowed to take place */
 	BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 100);
 
 	angleBehavior = new SetAngleBehavior(chopperModel);
 	angleBehavior.setSchedulingBounds(bounds);
 
 	TransformGroup rotateGroup = new TransformGroup();
 	rotateGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
 	Alpha rotationAlpha = new Alpha(-1, 8000);
 	RotationInterpolator rotator = 
 	    new RotationInterpolator(rotationAlpha, rotateGroup);
 
 	rotator.setSchedulingBounds(bounds);
 
 	objectRoot.addChild(rotateGroup);
 	rotateGroup.addChild(chopperModel);
 	rotateGroup.addChild(angleBehavior);
 	//rotateGroup.addChild(rotator);
 
 	/* Optimize! Enhance! */
 	objectRoot.compile();
 	return objectRoot;
     }
 
     private TransformGroup getChopperModel() {
 	BranchGroup node = new BranchGroup();
 
 	Appearance metal = new Appearance();
 	metal.setColoringAttributes(new ColoringAttributes(0.5f, 0.5f, 0.5f,
 						       ColoringAttributes.SHADE_GOURAUD));
 	Appearance red = new Appearance();
 	red.setColoringAttributes(new ColoringAttributes(1.0f, 0f, 0f,
 						       ColoringAttributes.SHADE_GOURAUD));
 	//red.setLineAttributes(new LineAttributes(1, LineAttributes.PATTERN_SOLID, true
 
 	Appearance green = new Appearance();
 	green.setColoringAttributes(new ColoringAttributes(0f, 1.0f, 0f,
 						       ColoringAttributes.SHADE_GOURAUD));
 
 	Appearance blue = new Appearance();
 	blue.setColoringAttributes(new ColoringAttributes(1.0f, 1.0f, 0f,
 						       ColoringAttributes.SHADE_GOURAUD));
 
 	/* The X bars */
 	Cylinder xbar1 = new Cylinder(0.01f, 1f, metal);
 	Cylinder xbar2 = new Cylinder(0.01f, 1f, metal);
 
 	/* Move bar 1 to be perpendicular */
 	Transform3D rotateZ = new Transform3D();
 	rotateZ.rotZ(Math.PI / 2d);
 	TransformGroup grpZ = new TransformGroup(rotateZ);
 	grpZ.addChild(xbar1);
 
 	/* Rotate them both to be in the XY plane */
 	Transform3D rotateToXY = new Transform3D();
 	rotateToXY.rotX(Math.PI / 2d);
 	TransformGroup grpXY = new TransformGroup(rotateToXY);
 	grpXY.addChild(xbar2);
 	grpXY.addChild(grpZ);
 
 	/* The downwards-pointing vector */
 	Cylinder cyl = new Cylinder(0.005f, 0.25f, red);
 	Transform3D translate = new Transform3D();
 	translate.set(new Vector3f(0f, -0.125f, 0f));
 	TransformGroup cylTransform = new TransformGroup(translate);
 	cylTransform.addChild(cyl);
 	    
 	/* The "propellers" */
 	Cylinder prop1 = new Cylinder(0.03f, 0.1f, blue);
 	Cylinder prop2 = new Cylinder(0.03f, 0.1f, red);
 	Cylinder prop3 = new Cylinder(0.03f, 0.1f, blue);
 	Cylinder prop4 = new Cylinder(0.03f, 0.1f, red);
 
 	Transform3D prop1Trans = new Transform3D();
 	prop1Trans.set(new Vector3f(0.5f, 0, 0));
 	TransformGroup prop1g = new TransformGroup(prop1Trans);
 	prop1g.addChild(prop1);
 	node.addChild(prop1g);
 
 	Transform3D prop2Trans = new Transform3D();
 	prop2Trans.set(new Vector3f(0, 0, 0.5f));
 	TransformGroup prop2g = new TransformGroup(prop2Trans);
 	prop2g.addChild(prop2);
 	node.addChild(prop2g);
 
 	Transform3D prop3Trans = new Transform3D();
 	prop3Trans.set(new Vector3f(-0.5f, 0, 0));
 	TransformGroup prop3g = new TransformGroup(prop3Trans);
 	prop3g.addChild(prop3);
 	node.addChild(prop3g);
 
 	Transform3D prop4Trans = new Transform3D();
 	prop4Trans.set(new Vector3f(0, 0, -0.5f));
 	TransformGroup prop4g = new TransformGroup(prop4Trans);
 	prop4g.addChild(prop4);
 	node.addChild(prop4g);
 
 	node.addChild(grpXY);
 	node.addChild(cylTransform);
 
 	chopperRotator = new TransformGroup();
 	chopperRotator.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
 	chopperRotator.addChild(node);
 	return chopperRotator;
     }
 
     public void setOrientation(Orientation o) {
 	angleBehavior.setAngle(o);
 	angleBehavior.processStimulus(null);
     }
 
     public class SetAngleBehavior extends Behavior {
         private TransformGroup targetTG;
         private Transform3D rotationX = new Transform3D();
 	private Transform3D rotationZ = new Transform3D();
 	private Orientation angle;
 
 	public SetAngleBehavior(TransformGroup _targetTG) {
 	    targetTG = _targetTG;
 	    angle = new Orientation(0, 0, 0);
 	}
 
	public void initialize(){
 	    ;
 	}
 
	public void processStimulus(Enumeration criteria){
 	    rotationX.rotX(- angle.getTilt(Orientation.RADIANS));
 	    rotationZ.rotZ(angle.getPitch(Orientation.RADIANS));
	    rotationX.mul(rotationZ);
 	    
	    Transform3D rotateY = new Transform3D();
	    rotateY.rotY(- angle.getRoll(Orientation.RADIANS));
	    rotationX.mul(rotateY);

	    targetTG.setTransform(rotationX);
 	}
 
 	public void setAngle(Orientation _angle) {
 	    angle = _angle;
 	}
     }
 
     public static void main(String args[]) {
 	Debug.enable = true;
 	JFrame frame = new JFrame();
 	frame.setPreferredSize(new Dimension(300, 300));
 	OrientationComponent o = new OrientationComponent();
 	frame.add(o);
 	frame.pack();
 	frame.setVisible(true);
 
 	o.setOrientation(new Orientation(0, 45, 45));
     }
 }
