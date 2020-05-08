 package pro.kornev.kcar.cop.services.video;
 
 import android.content.pm.PackageManager;
 import android.graphics.ImageFormat;
 import android.graphics.Rect;
 import android.graphics.SurfaceTexture;
 import android.graphics.YuvImage;
 import android.hardware.Camera;
 
 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.util.List;
 import java.util.Queue;
 
 import pro.kornev.kcar.cop.providers.LogsDB;
 import pro.kornev.kcar.cop.services.CopService;
 import pro.kornev.kcar.cop.services.network.NetworkListener;
 import pro.kornev.kcar.cop.services.network.NetworkService;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 /**
  *
  */
 public class VideoService implements NetworkListener, Camera.PreviewCallback, Camera.ErrorCallback {
     private final CopService copService;
     private final LogsDB log;
 
     private volatile Camera mCamera;
     private volatile boolean startPreview = false;
     private volatile List<Camera.Size> sizes;
     private volatile Camera.Size size;
     private volatile int previewFormat = ImageFormat.NV21;
 
     private int quality = 50;
     private int fps = 1;
     private long lastFrameTime = 0;
     private CameraPreview cameraPreview = null;
     private boolean isFlashAvailable = false;
     private boolean isFlashOn = false;
 
     public VideoService(CopService cs) {
         copService = cs;
         log = new LogsDB(copService);
     }
 
     public void start() {
         try {
             if (copService != null) {
                 cameraPreview = new CameraPreview(copService);
                 if (copService.getPackageManager() != null) {
                     isFlashAvailable = copService.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                 }
             }
             sizes = getCamera().getParameters().getSupportedPreviewSizes();
             log.putLog("VS Is running");
         } catch (Throwable e) {
             log.putLog("VS Start was failed: " + e.getMessage());
         }
     }
 
     public synchronized void stop() {
        try {
            stopPreview();
            mCamera.release();
        } catch (Exception ignored) {}
     }
 
     // Network listener
     @Override
     public void onDataReceived(Data data) {
         if (data.cmd < Protocol.Cmd.copFirst() || data.cmd > Protocol.Cmd.copLast()) {
             return;
         }
         if (data.cmd == Protocol.Cmd.camState()) {
             if (data.bData == 0) {
                 log.putLog("VS Stop preview");
                 stopPreview();
             } else {
                 log.putLog("VS Start preview");
                 startPreview();
             }
         } else if (data.cmd == Protocol.Cmd.camFps()) {
             log.putLog("VS Set FPS to " + data.bData);
             setFps(data.bData);
         } else if (data.cmd == Protocol.Cmd.camQuality()) {
             log.putLog("VS Set quality to " + data.bData);
             setQuality(data.bData);
         } else if (data.cmd == Protocol.Cmd.camFlash()) {
             log.putLog("VS Set flash to " + data.bData);
             isFlashOn = data.bData != 0;
             resetCamera();
         } else if (data.cmd == Protocol.Cmd.camSizeList()) {
             log.putLog("VS Send camera preview sizes");
             if (sizes == null || sizes.isEmpty()) {
                 data = new Data();
                 data.cmd = Protocol.Cmd.error();
                 data.bData = Protocol.Cmd.camSizeList();
                 write(data);
                 return;
             }
             ByteBuffer bb = ByteBuffer.allocate(sizes.size() * 8);
             for (Camera.Size size : sizes) {
                 bb.putInt(size.width);
                 bb.putInt(size.height);
             }
             data = new Data();
             data.cmd = Protocol.Cmd.camSizeList();
             data.type = Protocol.arrayType();
             data.aData = bb.array();
             data.aSize = data.aData.length;
             write(data);
         } else if (data.cmd == Protocol.Cmd.camSizeSet()) {
             log.putLog("VS Set cam size");
             ByteBuffer bb = ByteBuffer.wrap(data.aData);
             size = getSize(bb.getInt(), bb.getInt(), sizes);
             resetCamera();
         }
     }
 
     // Camera listener
     @Override
     public void onPreviewFrame(byte[] buf, Camera camera) {
         try {
             if (!copService.isRunning()) {
                 stopPreview();
                 return;
             }
             if (System.currentTimeMillis() - lastFrameTime < 1000 / getFps()) {
                //camera.addCallbackBuffer(buf);
                 return;
             }
             YuvImage image = new YuvImage(buf, previewFormat, size.width, size.height, null);
            //camera.addCallbackBuffer(buf);
 
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), getQuality(), baos);
 
             byte[] aData = baos.toByteArray();
 
             Data data = new Data();
             data.cmd = Protocol.Cmd.camImg();
             data.type = Protocol.arrayType();
             data.aSize = aData.length;
             data.aData = aData;
             write(data);
             lastFrameTime = System.currentTimeMillis();
         } catch (Throwable e) {
             log.putLog("VS Frame error: " + e.getMessage());
         }
     }
 
     @Override
     public void onError(int error, Camera camera) {
         log.putLog("VS Camera error: " + error);
         Data data = new Data();
         data.cmd = Protocol.Cmd.error();
         data.bData = Protocol.Cmd.camState();
         write(data);
         resetCamera();
     }
 
     private void setupParameters(Camera camera) {
         Camera.Parameters parameters = camera.getParameters();
         /*List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
         if (supportedFps != null) {
             int minFps = supportedFps.get(0)[0];
             parameters.setPreviewFpsRange(minFps, minFps);
         }*/
         previewFormat = parameters.getPreviewFormat();
         if (size == null) {
             size = getSize(0, 0, sizes);
         }
         parameters.setPreviewSize(size.width, size.height);
         if (isFlashAvailable && isFlashOn) {
             parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
         } else {
             parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
         }
         camera.setParameters(parameters);
     }
 
     private void initCamera(Camera camera) {
         try {
             setupParameters(camera);
            cameraPreview.setCamera(camera);
             SurfaceTexture surfaceTexture = new SurfaceTexture(0);
             camera.setPreviewTexture(surfaceTexture);
            camera.setPreviewCallback(this);
            /*camera.setPreviewCallbackWithBuffer(this);
             int previewBufSize = size.height * size.width * ImageFormat.getBitsPerPixel(previewFormat) / 8;
            for (int i=0; i < 3; i++) {
                 camera.addCallbackBuffer(new byte[previewBufSize]);
            }*/
             camera.setErrorCallback(this);
         } catch (Throwable e) {
             log.putLog("VS Failed init camera: " + e.getMessage());
             e.printStackTrace();
         }
     }
 
     private synchronized int getFps() {
         return fps;
     }
 
     private synchronized void setFps(int fps) {
         this.fps = fps > 0 ? fps : 1;
     }
 
     private synchronized int getQuality() {
         return quality;
     }
 
     private synchronized void setQuality(int quality) {
         this.quality = quality;
     }
 
     private synchronized boolean isStartPreview() {
         return startPreview;
     }
 
     private synchronized void setStartPreview(boolean startPreview) {
         this.startPreview = startPreview;
     }
 
     private Camera.Size getSize(int w, int h, List<Camera.Size> sizes) {
         Camera.Size minSize = null;
         if (sizes != null && !sizes.isEmpty()) {
             minSize = sizes.get(0);
             for (Camera.Size size : sizes) {
                 if (size.width == w && size.height == h) {
                     return size;
                 }
                 if (minSize.width > size.width) {
                     minSize = size;
                 }
             }
         }
         return minSize;
     }
 
     private synchronized Camera getCamera() {
         try {
             if (mCamera == null) {
                 mCamera = Camera.open();
             }
             mCamera.lock();
         } catch (Throwable ignore) {
         }
         return mCamera;
     }
 
     private synchronized void releaseCamera() {
         try {
             if (mCamera != null) {
                 mCamera.unlock();
             }
         } catch (Throwable ignored) {
         }
     }
 
     private synchronized void resetCamera() {
         if (isStartPreview()) {
             stopPreview();
             startPreview();
         }
     }
 
     private synchronized void startPreview() {
         try {
             if (isStartPreview()) return;
             Camera camera = getCamera();
             camera.reconnect();
             initCamera(camera);
             camera.startPreview();
             setStartPreview(true);
             log.putLog("VS Preview was started");
         } catch (Throwable e) {
             log.putLog("VS Failed start camera preview: " + e.getMessage());
         }
     }
 
     private synchronized void stopPreview() {
         try {
             if (!isStartPreview()) return;
             getCamera().stopPreview();
             releaseCamera();
             setStartPreview(false);
             log.putLog("VS Preview was stopped");
         } catch (Throwable e) {
             log.putLog("Failed stop camera preview: " + e.getMessage());
         }
     }
 
     private void write(Data data) {
         if (copService.getNetworkService() == null) {
             return;
         }
         copService.getNetworkService().write(data);
     }
 }
