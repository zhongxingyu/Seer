 /*
  * CreateForceTool.java
  *
  * Created on July 16, 2007, 3:45 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.tools;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.Point;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class CreateMomentTool2D extends CreateLoadTool { //implements ClickListener {
 
     protected Moment moment;
     //protected Diagram world;
     /** Creates a new instance of CreateForceTool */
     public CreateMomentTool2D(FreeBodyDiagram world) {
         super(world);
     }
 
     @Override
     protected LabelSelector createLabelSelector() {
         LabelSelector labelTool = new LabelSelector(getDiagram(), moment, moment.getAnchor().getTranslation());
         labelTool.setAdvice("Please give a name or a value for your moment");
         return labelTool;
     }
 
     @Override
     protected void onActivate() {
         super.onActivate();
         StaticsApplication.getApp().setAdviceKey("fbd_tools_createMoment");
     }
 
     @Override
     protected List<Load> createLoads(Point anchor) {
 
         moment = new Moment(anchor, new Vector3bd("0", "0", "1"), "M");
         moment.createDefaultSchematicRepresentation();
        new LabelManipulator(moment);
         return Collections.singletonList((Load) moment);
     }
 }
