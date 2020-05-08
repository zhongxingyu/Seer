 /*******************************************************************************
  *  Copyright (C) 2013 Justin Stoecker
  *  The MIT License. See LICENSE in project root.
  *******************************************************************************/
 package roboviz.draw;
 
 import java.awt.Font;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import jgl.core.Viewport;
 import jgl.math.vector.Transform;
 import jgl.math.vector.Vec3f;
 import jgl.scene.cameras.Camera;
 import roboviz.draw.drawable.Circle;
 import roboviz.draw.drawable.Drawable;
 import roboviz.draw.drawable.DrawableGraph;
 import roboviz.draw.drawable.DrawableNode;
 import roboviz.draw.drawable.Sphere;
 import roboviz.draw.drawable.Text;
 import roboviz.draw.gui.DrawPanel;
 
 import com.jogamp.opengl.util.awt.TextRenderer;
 
 public class DrawManager {
 
   Connection    connection;
   Parser        parser;
   DrawableGraph graph        = new DrawableGraph("Shapes");
   TextRenderer  textRenderer = new TextRenderer(new Font("Verdana", Font.PLAIN, 14), true, true);
   DrawPanel     drawPanel    = new DrawPanel(graph);
 
   public DrawManager() {
   }
   
   public DrawPanel getDrawPanel() {
     return drawPanel;
   }
 
   public void init() {
     try {
       parser = new Parser(this);
       connection = new Connection(this, 32769);
     } catch (SocketException e) {
       e.printStackTrace();
     } catch (UnknownHostException e) {
       e.printStackTrace();
     }
   }
 
   public void dispose(GL2 gl) {
     connection.shutdown();
     Circle.cache.dispose(gl);
     Sphere.cache.dispose(gl);
   }
 
   public void reset() {
     graph.clear();
   }
 
   public void render(GL2 gl, Camera camera, Viewport viewport) {
     gl.glPushAttrib(GL2.GL_ENABLE_BIT);
     gl.glEnable(GL.GL_BLEND);
     gl.glEnable(GL.GL_DEPTH_TEST);
     gl.glDisable(GL2.GL_LIGHTING);
     gl.glDisable(GL.GL_TEXTURE_2D);
     gl.glDisable(GL2.GL_LIGHTING);
 
     ArrayList<Text> texts = new ArrayList<Text>();
 
     synchronized (graph) {
       renderNode(gl, graph.getRoot(), texts);
     }
 
     if (texts.size() > 0) {
       textRenderer.beginRendering(viewport.width, viewport.height);
       for (Text text : texts) {
         Vec3f p = Transform.worldToWindow(viewport, camera, text.position);
         textRenderer.setColor(text.color.x, text.color.y, text.color.z, 1);
         textRenderer.draw3D(text.text, p.x, p.y, p.z, 1);
       }
       textRenderer.endRendering();
     }
 
     gl.glPopAttrib();
   }
 
   private void renderNode(GL2 gl, DrawableNode node, List<Text> textNodes) {
     if (!node.isVisible())
       return;
    
     for (Drawable drawable : node.getFront()) {
       if (drawable instanceof Text)
         textNodes.add((Text) drawable);
       else
         drawable.render(gl);
     }
 
     if (node.getChildren() != null) {
       for (DrawableNode child : node.getChildren()) {
         renderNode(gl, child, textNodes);
       }
     }
   }
 }
