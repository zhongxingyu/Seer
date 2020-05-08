 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fletcher
  * Date: 8/19/12
  * Time: 4:42 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class Parameters implements java.io.Serializable
 {
     transient int width;
     transient int height;
     float[] center;
 
     double rotateAngle;
     double scaleFactor;
 
     transient int matrixSize;
     float blurRadius;
     float unsharpRadius;
     float unsharpWeight;
 
     float contrast[];
     float brightness[];
     float gamma[];
 
     transient float borderColor[];
 
     transient boolean noiseOn;
     float noiseStd;
 
     transient float[] debugMatrix = null;
 
     Parameters() {
         width = 300;
         height = 300;
         center = new float[]{(float)(Math.floor(width/2.0)+0.25), (float)(Math.floor(height/2.0)+0.25)};
         rotateAngle = Math.PI * (1.0/5.0);
         scaleFactor = 1.15;
         matrixSize = 5;
         blurRadius = 0.5f;
         unsharpRadius = 1.0f;
        unsharpWeight = 0.5f;
         contrast = new float[]{1.0f, 1.0f, 1.0f};
         brightness = new float[]{0.0f, 0.0f, 0.0f};
         borderColor = new float[]{0.05f, 0.03f, 0.03f};
        gamma = new float[]{2.0f, 1.8f, 2.2f};
         noiseOn = true;
         noiseStd = 0.001f;
     }
 
     Parameters(Parameters in){
         clone(in);
     }
 
     public int pixelNum() {
         return width * height;
     }
 
     public void clone(Parameters in){
         width = in.width;
         height = in.height;
         center = in.center.clone();
         rotateAngle = in.rotateAngle;
         scaleFactor = in.scaleFactor;
         matrixSize = in.matrixSize;
         blurRadius = in.blurRadius;
         unsharpRadius = in.unsharpRadius;
         unsharpWeight = in.unsharpWeight;
         contrast = in.contrast.clone();
         brightness = in.brightness.clone();
         if(in.borderColor != null)
             borderColor = in.borderColor.clone();
         gamma = in.gamma.clone();
         noiseOn = in.noiseOn;
         noiseStd = in.noiseStd;
     }
 
     public void partialClone(Parameters in){
         center = in.center.clone();
         rotateAngle = in.rotateAngle;
         scaleFactor = in.scaleFactor;
         blurRadius = in.blurRadius;
         unsharpRadius = in.unsharpRadius;
         unsharpWeight = in.unsharpWeight;
         contrast = in.contrast.clone();
         brightness = in.brightness.clone();
         gamma = in.gamma.clone();
         noiseStd = in.noiseStd;
     }
 
     public String serialize() {
         try{
             ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(outStream);
             objectStream.writeObject(this);
             String paramString =
                 com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(outStream.toByteArray());
             return paramString;
         }catch(java.io.IOException i)
         {
             i.printStackTrace();
             return null;
         }
     }
 
     public void deserialize(String in) {
         try{
             byte[] byteArray =
                 com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(in);
             ByteArrayInputStream inStream = new ByteArrayInputStream(byteArray);
             ObjectInputStream objectStream = new ObjectInputStream(inStream);
             Parameters p = (Parameters) objectStream.readObject();
             partialClone(p);
         }catch(Exception i)
         {
             i.printStackTrace();
             return;
         }
     }
 
     public float[] getBorderColorGamma() {
         float[] r = new float[borderColor.length];
         for(int i = 0; i < borderColor.length; i++) {
             r[i] = (float)Math.pow(borderColor[i], gamma[i]);
         }
         return r;
     }
 
     public synchronized float[] getDebugMatrix() {
         while(debugMatrix == null) {
             try {
                 wait();
             } catch (InterruptedException e) {
 
             }
         }
         return debugMatrix;
     }
 
     public synchronized void setDebugMatrix(float[] in) {
         debugMatrix = in;
         notifyAll();
     }
 }
