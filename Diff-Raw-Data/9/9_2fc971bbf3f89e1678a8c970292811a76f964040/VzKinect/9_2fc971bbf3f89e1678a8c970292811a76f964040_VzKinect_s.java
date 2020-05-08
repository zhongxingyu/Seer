 package rgbdslam;
 
 import java.awt.*;
 
 import april.vis.*;
 import april.jmat.*;
 
 public class VzKinect implements VisObject
 {
     static VisObject vo;
 
     static {
        VisChain vc = new VisChain(new VisChain(LinAlg.scale(0.05, 0.3, 0.03),
                                                 new VzBox(new VzMesh.Style(Color.orange))),
                                    new VisChain(LinAlg.translate(0, 0, -0.05),
                                                 LinAlg.scale(0.05),
                                                 new VzSquarePyramid(new VzMesh.Style(Color.orange))),
                                    new VisChain(LinAlg.rotateY(-Math.PI/2),
                                                 LinAlg.translate(0, 0, -0.2),
                                                 LinAlg.scale(0.005, 0.005, 0.2),
                                                new VzCone(new VzMesh.Style(Color.red))));
 
         vo = vc;
     }
 
     public VzKinect()
     {
     }
 
     public void render(VisCanvas vc, VisLayer vl, VisCanvas.RenderInfo rinfo, GL gl)
     {
         vo.render(vc, vl, rinfo, gl);
     }
 }
