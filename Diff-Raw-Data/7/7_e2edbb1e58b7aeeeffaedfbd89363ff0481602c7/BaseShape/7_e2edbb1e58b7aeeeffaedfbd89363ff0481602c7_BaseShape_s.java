 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview.shapes;
 
 import geometry.Intersect;
 import geometry.Rect;
 import geometry.Vec2;
 import graphevents.BaseEvent;
 import graphevents.ShapeMouseEvent;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.util.ArrayList;
 import property.PropertyObject;
 
 /**
  *
  * @author Kirill
  */
 
 
 public abstract class BaseShape extends PropertyObject{
     private Vec2Property localCoordProp=null;
     private Vec2Property sizeProp=null;
     
     private Vec2 localCoord=new Vec2();
     private Vec2 globalCoord=new Vec2();
     private Vec2 shapeSize=new Vec2();
     protected boolean bMoveable=true;
     protected boolean bResizeable=true;
     protected BaseShape parent=null;
     protected boolean bSelected=false;
     protected int childIndex=-1;
     protected boolean bVisible=true;
     protected boolean bMouseIn=false;
     protected boolean bHaveGrip=true;
     
    public boolean bDebugDrawBBox = true;
     
     public enum eShapeAspect
     {
         DEFAULT,
         NODE,
         EDGE
     };
     
     public static final int GRIP_NONE = 0;
     public static final int GRIP_LEFT = 1;
     public static final int GRIP_RIGHT = 2;
     public static final int GRIP_TOP = 3;
     public static final int GRIP_BOTTOM = 4;
     public static final int GRIP_TOP_LEFT = 5;
     public static final int GRIP_TOP_RIGHT = 6;
     public static final int GRIP_BOTTOM_LEFT = 7;
     public static final int GRIP_BOTTOM_RIGHT = 8;
     
     protected ArrayList<BaseShape> childs=new ArrayList<BaseShape>();
     protected ArrayList<Integer> selectedChilds=new ArrayList<Integer>();
     protected ArrayList<Integer> zbuffer=new ArrayList<Integer>();
     
     public BaseShape()
     {
         localCoordProp=propCreate("Position", new Vec2());
         sizeProp=propCreate("Size", new Vec2());
     };
     
     public BaseShape getParent()
     {
         return parent;
     };
     
     public boolean isSelected()
     {
         return bSelected;
     };
     
     public eShapeAspect getShapeAspect()
     {
         return eShapeAspect.DEFAULT;
     };
     
     /////////////////////PROPERTIES/////////////////////////////////////
     
     @Override
     public void updateProperties(boolean bUpdateToProp)
     {
         if(bUpdateToProp)
         {
             localCoordProp.setProp(localCoord);
             sizeProp.setProp(shapeSize);
         }
         else
         {
             setPosition(localCoordProp.getProp());
             setSize(sizeProp.getProp());
         };
     };
     
     ////////////////////END PROPERTIES//////////////////////////////////
     
     /////////////////////MOVEMENT///////////////////////////////////////
     
     public void updateGlobalCoord()
     {
         if(parent==null) globalCoord.set(localCoord);
         else globalCoord.set(parent.globalCoord.plus(localCoord));
         updateChildsCoord();
     };
     
     public void updateChildsCoord()
     {
         for(int i=0;i<childs.size();i++)
         {
             childs.get(i).updateGlobalCoord();
         };
     };
     
     public void setPosition(Vec2 coord)
     {
         if(!bMoveable) return;
         localCoord.set(onMove(localCoord,coord));
         updateGlobalCoord();
         localCoordProp.setProp(localCoord);
     };
     
     public void move(Vec2 v)
     {
         setPosition(localCoord.plus(v));
     };
     
     public void setCenterPosition(Vec2 coord)
     {
         Rect r=getRectangle();
         r.setCenterPosition(coord);
         setRectangle(r);
     };
     
     public Vec2 getPosition()
     {
         return new Vec2(localCoord);
     };
     
     public Vec2 getGlobalPosition()
     {
         return new Vec2(globalCoord);
     };
     
     public void setSize(Vec2 size)
     {
         if(!bResizeable) return;
         if(size.x<5) size.x=5;
         if(size.y<5) size.y=5;
         shapeSize.set(onResize(shapeSize,size));
         sizeProp.setProp(shapeSize);
     };
     
     public Vec2 getSize()
     {
         return new Vec2(shapeSize);
     };
     
     public void setRectangle(Rect r)
     {
         setPosition(r.getTopLeft());
         setSize(r.getSize());
     }
     
     public Rect getRectangle()
     {
         return new Rect(localCoord.x,localCoord.y,
                 localCoord.x+shapeSize.x,localCoord.y+shapeSize.y);
     };
     
     public Rect getGlobalRectangle()
     {
         return new Rect(globalCoord.x,globalCoord.y,
                 globalCoord.x+shapeSize.x,globalCoord.y+shapeSize.y);
     };
     
     protected Vec2 onResize(Vec2 oldSize, Vec2 newSize){
         return newSize;
     }
     
     protected Vec2 onMove(Vec2 oldCoord, Vec2 newCoord){
         return newCoord;
     }
     
     public Vec2 toLocal(Vec2 global)
     {
         return global.minus(globalCoord);
     };
     
     public Rect toLocal(Rect global)
     {
         Rect r=new Rect(global);
         r.setPosition(global.getTopLeft().minus(globalCoord));
         return r;
     };
     
     public Vec2 toGlobal(Vec2 local)
     {
         return local.plus(globalCoord);
     };
     
     public Rect toGlobal(Rect local)
     {
         Rect r=new Rect(local);
         r.setPosition(local.getTopLeft().plus(globalCoord));
         return r;
     };
     
     public Rect getBoundingRect()
     {
         Rect r=getGlobalRectangle();
         Rect childs=this.getChildsRect();
        if(childs.getSquare()!=0) r.add(childs);
         return r;
     };
     
     /////////////////////////END MOVEMENT//////////////////////////////////
     
     ///////////////////////////CHILDS///////////////////////////////////////
     public int testChildIntersect(Vec2 pt)
     {
         for(int i=zbuffer.size()-1;i>=0;i--)
         {
             if(childs.get(zbuffer.get(i)).isIntersects(pt)) return zbuffer.get(i);
         }
         return -1;
     };
     
     public BaseShape getIntersectedChild(Vec2 pt){
         BaseShape shape=null;
         for(int i=zbuffer.size()-1;i>=0;i--)
         {
             if(!childs.get(zbuffer.get(i)).isIntersects(pt) &&
                 childs.get(zbuffer.get(i)).isGripIntersect(pt)==GRIP_NONE) 
                 continue;
             shape=childs.get(zbuffer.get(i)).getIntersectedChild(pt);
             if(shape!=null) return shape;
         }
         if(isIntersects(pt) || isGripIntersect(pt)!=GRIP_NONE)
             return this;
         return null;
     };
     
     public int addChild(BaseShape shape)
     {
         if(shape==null) return-1;
         for(int i=0;i<childs.size();i++)
         {
             if(childs.get(i)==shape) return -1;
         };
         
         if(shape.parent!=null)
         {
             shape.parent.removeChild(shape.childIndex);
         };
         
         childs.add(shape);
         shape.parent=this;
         shape.updateGlobalCoord();
         shape.childIndex=childs.size()-1;
         
         addToZBuffer(shape.childIndex);
         update();
         return shape.childIndex;
     };
     
     public Rect getChildsRect()
     {
         Rect r=new Rect();
         boolean bFirst=true;
         for(int i=0;i<childs.size();i++)
         {
             if(childs.get(i)==null) continue;
             
             if(bFirst) 
             {
                 r.set(childs.get(i).getBoundingRect());
                 bFirst=false;
             }
             else r.add(childs.get(i).getBoundingRect());
         };
         return r;
     };
     
     public int getNumChilds(){
         return childs.size();
     };
     
     public BaseShape getChild(int index){
         return childs.get(index);
     };
     
     public void removeChild(int index){
         for(int i=0;i<zbuffer.size();i++)
         {
             if(zbuffer.get(i)==index)
             {
                 zbuffer.remove(i);
                 break;
             };
         };
         
         for(int i=0;i<selectedChilds.size();i++)
         {
             if(selectedChilds.get(i)==index)
             {
                 selectedChilds.remove(i);
                 break;
             };
         };
         
         childs.set(index,null);
         update();
     }
     
     public int getIndex()
     {
         return childIndex;
     };
     
     public void removeAllChilds()
     {
         zbuffer.clear();
         clearSelection();
         
         for(int i=0;i<childs.size();i++)
         {
             if(childs.get(i)!=null)
             {
                 childs.get(i).removeAllChilds();
             };
         };
         childs.clear();
     };
     
     //////////////////////////END CHILDS///////////////////////////////
     
     //////////////////////SELECTION_VISIBILITY/////////////////////////
     
     public void setVisible(boolean bVal)
     {
         if(bVal==bVisible) return;
         
         if(bVal==true && parent!=null)
         {
             parent.addToZBuffer(childIndex);
         }
         else if(bVal==false && parent!=null)
         {
             parent.removeFromZBuffer(childIndex);
         };
     };
     
     protected void addToZBuffer(int index)
     {
         for(int i=0;i<zbuffer.size();i++)
         {
             if(zbuffer.get(i)==index) return;
         };
         zbuffer.add(index);
     };
     
     protected void removeFromZBuffer(int index)
     {
         for(int i=0;i<zbuffer.size();i++)
         {
             if(zbuffer.get(i)==index)
             {
                 zbuffer.remove(i);
                 return;
             };
         };
     };
     
     protected void addToSelected(int index)
     {
         for(int i=0;i<selectedChilds.size();i++)
         {
             if(selectedChilds.get(i)==index) return;
         };
         selectedChilds.add(index);
         bringToTop(index);
     };
     
     protected void removeFromSelected(int index)
     {
         for(int i=0;i<selectedChilds.size();i++)
         {
             if(selectedChilds.get(i)==index)
             {
                 selectedChilds.remove(i);
                 return;
             };
         };
     };
     
     public void bringToTop(int index)
     {
         for(int i=0;i<zbuffer.size();i++)
         {
             if(zbuffer.get(i)==index)
             {
                 zbuffer.remove(i);
                 zbuffer.add(index);
                 return;
             };
         };
     };
     
     public void setSelected(boolean bVal)
     {
         if(parent!=null)
         {
             if(bVal)
             {
                 parent.addToSelected(childIndex);
             }
             else
             {
                 parent.removeFromSelected(childIndex);
             };
         }
         bSelected=bVal;
     };
     
     public void clearSelection()
     {
         for(int i=0;i<selectedChilds.size();i++)
         {
             childs.get(selectedChilds.get(i)).bSelected=false;
         };
         selectedChilds.clear();
         
         update();
     };
     
     public void clearAllSelection()
     {
         for(int i=0;i<childs.size();i++)
         {
             childs.get(i).bSelected=false;
             childs.get(i).clearAllSelection();
         };
         selectedChilds.clear();
         
         update();
     };
     
     ////////////////////////END SELECTION_VISIBILITY//////////////////////
     
     /////////////////////////////EVENTS/////////////////////////////////////
     protected boolean bReceiveMouseClick=true;
     protected boolean bReceiveMouseDrag=true;
     protected boolean bReceiveMousePress=true;
     protected boolean bReceiveMouseRelease=true;
     protected boolean bReceiveMouseMove=true;
     
     public boolean isReceivedMouseClick() {
         return bReceiveMouseClick;
     }
     
     public boolean isReceivedMouseDrag() {
         return bReceiveMouseDrag;
     }
     
     public boolean isReceivedMousePress() {
         return bReceiveMousePress;
     }
     
     public boolean isReceivedMouseRelease() {
         return bReceiveMouseRelease;
     }
     
     public boolean isReceivedMouseMove() {
         return bReceiveMouseMove;
     }
     
     
     protected boolean testEvent(BaseEvent evt)
     {
         if(evt.getType()==BaseEvent.EVENT_TYPE_MOUSE)
         {
             ShapeMouseEvent mEvt=(ShapeMouseEvent)evt;
             if(mEvt.getSubtype()==ShapeMouseEvent.CLICK && bReceiveMouseClick) 
                 return true;
             if(mEvt.getSubtype()==ShapeMouseEvent.DRAG && bReceiveMouseDrag) 
                 return true;
             if(mEvt.getSubtype()==ShapeMouseEvent.MOVE && bReceiveMouseMove) 
                 return true;
             if(mEvt.getSubtype()==ShapeMouseEvent.PRESS && bReceiveMousePress) 
                 return true;
             if(mEvt.getSubtype()==ShapeMouseEvent.RELEASE && bReceiveMouseRelease) 
                 return true;
         };
         return false;
     }
     
     
     public boolean processEvent(BaseEvent evt)
     {
         switch(evt.getType())
         {
             case BaseEvent.EVENT_TYPE_MOUSE: 
                 return processMouseEvent((ShapeMouseEvent)evt);
         };
         return false;
     };
     
     protected boolean processMouseEventBase(ShapeMouseEvent evt)
     {
         boolean bRet=false;
         switch(evt.getSubtype())
         {
             case ShapeMouseEvent.CLICK:     bRet=onMouseClick(evt); break;
             case ShapeMouseEvent.DRAG:      bRet=onMouseDrag(evt); break;
             case ShapeMouseEvent.MOVE:      bRet=onMouseMove(evt); break;
             case ShapeMouseEvent.PRESS:     bRet=onMousePress(evt); break;
             case ShapeMouseEvent.RELEASE:   bRet=onMouseRelease(evt); break;
             default: return false;
         };
         return bRet;
     };
     
     public int testChildMouseEvent(Vec2 pt, BaseEvent evt)
     {
         for(int i=zbuffer.size()-1;i>=0;i--)
         {
             if(childs.get(zbuffer.get(i)).isIntersects(pt) &&
                     childs.get(zbuffer.get(i)).testEvent(evt)) 
             {
                 return zbuffer.get(i);
             }
         }
         return -1;
     };
     
     public void dragSelected(Vec2 delta)
     {
         nMode=2;
         for(int j=0;j<selectedChilds.size();j++)
         {
             childs.get(selectedChilds.get(j)).move(delta);
         };
     };
     
     protected boolean processMouseEvent(ShapeMouseEvent evt)
     {
         int numChild=testChildMouseEvent(evt.getPosition(),evt);
 
         if(numChild!=-1) 
         {
             if(!childs.get(numChild).processEvent(evt))
                 return processMouseEventBase(evt);
         }
         else
         {
             return processMouseEventBase(evt);
         };
 
         return false;
     };
     
     public boolean onMouseClick(ShapeMouseEvent evt){
         if(evt.getButton()==1)
         {
             boolean bSel=bSelected;
             //clearSelection();
             setSelected(!bSel);
             return true;
         }
         return false;
     }
     
     int nMode=0;
     public boolean onMousePress(ShapeMouseEvent evt){
         if(evt.getButton()==1 && bSelected) 
         {
             nMode=1;
             return true;
         }
         return false;
     }
     
     public boolean onMouseRelease(ShapeMouseEvent evt){
         nMode=0;
         return false;
     }
     
     public boolean onMouseDrag(ShapeMouseEvent evt){
         
         return true;
     }
    
     Vec2 debugMouseMove=new Vec2();
     public boolean onMouseMove(ShapeMouseEvent evt){
         debugMouseMove=toGlobal(evt.getPosition());
         
         if(!bMouseIn)
         {
             onMouseIn(evt);
         };
         return true;
     }
     
     public boolean onMouseIn(ShapeMouseEvent evt){
         bMouseIn=true;
         for(int i=0;i<childs.size();i++)
         {
             if(childs.get(i).bMouseIn)
                 childs.get(i).onMouseOut(evt);
         };
         if(parent!=null && parent.bMouseIn)
             parent.bMouseIn=false;
         return true;
     }
     
     public boolean onMouseOut(ShapeMouseEvent evt){
         bMouseIn=false;
         for(int i=0;i<childs.size();i++)
         {
             if(childs.get(i).bMouseIn)
                 childs.get(i).onMouseOut(evt);
         };
         if(parent!=null && parent.bMouseIn)
             parent.bMouseIn=false;
         return true;
     }
     /////////////////////////END EVENTS/////////////////////////////////////
     
     //////////////////////GRIP//////////////////////////////////////////////
     
     public int isGripIntersect(Vec2 pt)
     {
         if(!bHaveGrip) return GRIP_NONE;
         if(!bSelected) return GRIP_NONE;
         //if(Intersect.rectangle_point(getLocalPlacement().getIncreased(10), 
         //        pt)!=Intersect.INCLUSION) return GRIP_NONE;
 
         Rect r=new Rect(-3,-3,3,3);
         
         r.setCenterPosition(getGlobalRectangle().getTopLeft());
         if(Intersect.rectangle_point(r, pt)==Intersect.INCLUSION)
             return GRIP_TOP_LEFT;
         
         r.setCenterPosition(getGlobalRectangle().getTopRight());
         if(Intersect.rectangle_point(r, pt)==Intersect.INCLUSION)
             return GRIP_TOP_RIGHT;
         
         r.setCenterPosition(getGlobalRectangle().getBottomLeft());
         if(Intersect.rectangle_point(r, pt)==Intersect.INCLUSION)
             return GRIP_BOTTOM_LEFT;
         
         r.setCenterPosition(getGlobalRectangle().getBottomRight());
         if(Intersect.rectangle_point(r, pt)==Intersect.INCLUSION)
             return GRIP_BOTTOM_RIGHT;
             
         if(Intersect.lineseg_point(getGlobalRectangle().getTopLeft(), 
                 getGlobalRectangle().getTopRight(),3, pt)==Intersect.INCLUSION) 
             return GRIP_TOP;
         if(Intersect.lineseg_point(getGlobalRectangle().getTopRight(), 
                 getGlobalRectangle().getBottomRight(),3, pt)==Intersect.INCLUSION) 
             return GRIP_RIGHT;
         if(Intersect.lineseg_point(getGlobalRectangle().getBottomRight(), 
                 getGlobalRectangle().getBottomLeft(),3, pt)==Intersect.INCLUSION) 
             return GRIP_BOTTOM;
         if(Intersect.lineseg_point(getGlobalRectangle().getBottomLeft(), 
                 getGlobalRectangle().getTopLeft(),3, pt)==Intersect.INCLUSION) 
             return GRIP_LEFT;
         return GRIP_NONE;
     };
     
     public void drawGrip(Graphics2D g)
     {
         if(!bHaveGrip) return;
         Rect globalPlace=getGlobalRectangle();
         
         g.setColor(Color.ORANGE);
         
         Stroke oldStroke=g.getStroke();
         BasicStroke stroke=new BasicStroke(3);
         g.setStroke(stroke);
         g.drawRect((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
         g.setStroke(oldStroke);
         
         g.setColor(Color.BLACK);
         g.drawRect((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
         
         Rect r=new Rect(-3,-3,3,3);
         r.setCenterPosition(getGlobalRectangle().getTopLeft());
         g.fillRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
         r.setCenterPosition(getGlobalRectangle().getTopRight());
         g.fillRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
         r.setCenterPosition(getGlobalRectangle().getBottomLeft());
         g.fillRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
         r.setCenterPosition(getGlobalRectangle().getBottomRight());
         g.fillRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
     };
     
     public void onGripDragged(int nGrip, Vec2 delta)
     {
         Rect r=getRectangle();
         switch(nGrip)
         {
             case GRIP_TOP: r.top+=delta.y; break;
             case GRIP_BOTTOM: r.bottom+=delta.y; break;
             case GRIP_LEFT: r.left+=delta.x; break;
             case GRIP_RIGHT: r.right+=delta.x; break;
                 
             case GRIP_TOP_LEFT: r.top+=delta.y; r.left+=delta.x; break;
             case GRIP_TOP_RIGHT: r.top+=delta.y; r.right+=delta.x; break;
             case GRIP_BOTTOM_LEFT: r.bottom+=delta.y; r.left+=delta.x; break;
             case GRIP_BOTTOM_RIGHT: r.bottom+=delta.y; r.right+=delta.x; break;
         };
         
         setRectangle(r);
         update();
     };
     ///////////////////////END GRIP/////////////////////////////////////////
     
     
     public void update()
     {
         for(int i=0;i<childs.size();i++)
         {
             if(childs.get(i)!=null) 
                 childs.get(i).update();
         };
     };
     
     public void draw(Graphics2D g)
     {
         if(bSelected) drawGrip(g);
         if(bMouseIn && false)
         {
             g.setColor(Color.green);
             Rect r=getGlobalRectangle().getReduced(10);
             g.fillRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
         };
         
         
         
         //g.setColor(Color.blue);
         //g.fillRect((int)debugMouseMove.x-5, (int)debugMouseMove.y-5, 10, 10);
         for(int i=0;i<zbuffer.size();i++)
         {
             if(childs.get(zbuffer.get(i))!=null) 
                 childs.get(zbuffer.get(i)).draw(g);
         };
         
         if(bDebugDrawBBox)
         {
              Rect r=getBoundingRect();
              g.setColor(Color.red);
              g.drawRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
              g.setColor(Color.black);
              g.drawRect((int)r.left+1, (int)r.top+1, (int)r.getSize().x+1, (int)r.getSize().y+1);
         };
     };
     
     
     public abstract boolean isIntersects(Vec2 pt);
     public abstract boolean isIntersects(Rect r);
     public abstract Vec2 getPortPoint(Vec2 from);
 }
