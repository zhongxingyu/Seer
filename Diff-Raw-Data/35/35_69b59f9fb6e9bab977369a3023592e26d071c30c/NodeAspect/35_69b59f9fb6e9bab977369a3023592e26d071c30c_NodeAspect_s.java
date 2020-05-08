 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview.shapes;
 
 import graphview.shapes.BaseShape;
 import geometry.Rect;
 import geometry.Vec2;
 import graphview.GraphNode;
 import graphview.GraphScene;
 import java.awt.Color;
 import java.awt.Font;
 
 /**
  *
  * @author Kirill
  */
 
 
 public abstract class NodeAspect extends BaseShape{
 
     public enum eContainerType
     {
         DEFAULT,
         RESIZE_PARENT_TO_CHILDS,
         RESIZE_PARENT_TO_CHILDS_EQUI,
         RESIZE_CHILDS_TO_PARENT,
     };
 
     public enum eNodeAspectType
     {
         BOX,
         ELLIPSE,
         TEXT,
         IMAGE
     };
 
     ColorProperty color=null;
     StringProperty hint=null;
 
     int labelNode=-1;
     GraphNode graphParent;
     eNodeAspectType aspectType;
     eContainerType containerType;
     public float containerOffset=7;
     
     
     public NodeAspect()
     {
         color=propCreate("Color", Color.YELLOW);
         hint=propCreate("Hint", "none");  
     };
     
     public Color getColor()
     {
         return color.getProp();
     };
     
     public void setColor(Color val)
     {
         color.setProp(val);
     };
     
     public void createLabel(String str)
     {
         removeAllChilds();
         labelNode=addChild(GraphScene.createNodeShape(eNodeAspectType.TEXT));
         setLabel(str);
     };
     
     public String getLabel()
     {
         if(labelNode==-1) return "";
         return ((TextShape)childs.get(labelNode)).getText();
     };
     
     public void setLabel(String txt)
     {
         if(labelNode==-1) return;
         
         ((TextShape)childs.get(labelNode)).setText(txt);
         ((TextShape)childs.get(labelNode)).update();
     };
     
     /////////////////////////////CONTAINER////////////////////////////////
     
     public void setContainmentObject(eNodeAspectType type)
     {
         removeAllChilds();
         addChild(GraphScene.createNodeShape(type));
         update();
     };
     
     public void setContainerMode(eContainerType mode)
     {
         containerType=mode;
         updateContainer();
     };
     
     public eContainerType getContainerMode()
     {
         return containerType;
     };
     
     public eNodeAspectType getAspectType()
     {
         return aspectType;
     }
     
     public void updateContainer()
     {
         if(containerType==eContainerType.RESIZE_CHILDS_TO_PARENT)
         {
             for(int i=0;i<childs.size();i++)
             {
                 childs.get(i).setRectangle(getContainRect());
             };
         }
         else if(containerType==eContainerType.RESIZE_PARENT_TO_CHILDS || 
                 containerType==eContainerType.RESIZE_PARENT_TO_CHILDS_EQUI)
         {
             Rect r=getChildsRect();
             if(containerType==eContainerType.RESIZE_PARENT_TO_CHILDS_EQUI)
             {
                 if(r.getSize().x>r.getSize().y) r.setSize(new Vec2(r.getSize().x,r.getSize().x));
                 else if(r.getSize().x<r.getSize().y) r.setSize(new Vec2(r.getSize().y,r.getSize().y));
             };
             
            setSize(r.getSize().plus(containerOffset));
             
             for(int i=0;i<childs.size();i++)
             {
                 childs.get(i).bMoveable=true;
                 childs.get(i).setCenterPosition(getSize().divide(2));
                 childs.get(i).bMoveable=false;
             };
         }
     };
     
     public void fitToChilds(boolean bEquilateral)
     {
         Rect r=getChildsRect();
         Vec2 size=r.getSize();
         if(bEquilateral)
         {
             if(size.x>size.y) size.y=size.x;
             else size.x=size.y; 
         };
         
         setSize(size.plus(containerOffset*2));
     }
     
     public abstract Rect getContainRect();
     ///////////////////////////////END CONTAINER/////////////////////////////
     
     @Override
     public eShapeAspect getShapeAspect()
     {
         return eShapeAspect.NODE;
     }
     
     public void update()
     {
         updateContainer();
         super.update();
     };   
 }
