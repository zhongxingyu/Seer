 package com.example.testvideocam;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import ru.pvolan.event.ParametrizedCustomEvent;
 import ru.pvolan.trace.Trace;
 
 import android.hardware.Camera;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.media.MediaRecorder.*;
 import android.view.SurfaceHolder;
 
 public class CameraManager
 {
 	private Camera camera;
 	private int cameraNo;
 	
 	private MediaRecorder mediaRecorder;
 	private File outputFile;
 	
 	public ParametrizedCustomEvent<Throwable> onVideoCaptureError = new ParametrizedCustomEvent<Throwable>();
 	public ParametrizedCustomEvent<MediaRecorderInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<MediaRecorderInfo>();
 	
 	public CameraManager()
 	{
 		super();
 	}
 	
 	public void createCamera(int cameraNo)
 	{
 		try
 		{
 			this.cameraNo = cameraNo;
 			camera = Camera.open(cameraNo);
 			Trace.Print("Camera created " + cameraNo);
 		}
 		catch (Exception exception)
 		{
 			Trace.Print(exception);
 			camera.release();
 			camera = null;
 
 			onVideoCaptureError.fire(exception);
 		}		
 	}
 	
 	
 
 
 
 	public void destroyCamera()
 	{
 		camera.stopPreview();
 		camera.setPreviewCallback(null);
 		camera.release();
 		camera = null;
 		Trace.Print("Camera destroyed " + cameraNo);
 	}
 
 
 
 	public void updateCameraSizeApproximately(SurfaceHolder holder, int w, int h)
 	{
 		Camera.Parameters parameters = camera.getParameters();
 
 		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
 		Camera.Size max = camera.new Size(0, 0);
 
 		for (Camera.Size size : previewSizes)
 		{
 			
			Trace.Print("Camera " + cameraNo + " Available size " + max.width + "x" + max.height);
 
 			if (size.width <= w && size.height <= h
 					&& (max.width < size.width || max.height < size.height))
 			{
 				max = size;
 			}
 
 			
 			
 		}
 
 		Trace.Print("max " + max.width + "x" + max.height);
 
 		//TODO Excepional devices
 		
 		parameters.setRotation(90);
 		parameters.set("orientation", "portrait");
 		camera.setDisplayOrientation(90);
 		parameters.setPreviewSize(max.width, max.height);
 		camera.setParameters(parameters);
 		
 
 		try
 		{
 			camera.setPreviewDisplay(holder);
 		}
 		catch (IOException exception)
 		{
 			Trace.Print(exception);
 			camera.release();
 			camera = null;
 
 			onVideoCaptureError.fire(exception);
 		}
 	}
 
 
 
 	public File startRecording(SurfaceHolder holder) throws IOException
 	{
 		mediaRecorder = new MediaRecorder();
 		mediaRecorder.setCamera(camera);
 		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
 		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 		mediaRecorder.setProfile(CamcorderProfile
 				.get(cameraNo, CamcorderProfile.QUALITY_HIGH));
 
 		outputFile = MediaFileManager.getOutputVideoFile();
 		if (outputFile == null)
 		{
 			throw new RuntimeException("Cannot create output file");
 		}
 
 		mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
 		mediaRecorder.setPreviewDisplay(holder.getSurface());
 		
 		//TODO Exceptional devices
 		mediaRecorder.setOrientationHint(90);
 		
		mediaRecorder.setVideoSize(640,480); //TODO ????
		
 		mediaRecorder.setOnErrorListener(new OnErrorListener() 
 		{
 			@Override
 			public void onError(MediaRecorder mr, int what, int extra)
 			{
 				onVideoCaptureError.fire(new CaptureException(mr, what, extra));
 			}
 		});
 		
 		mediaRecorder.setOnInfoListener(new OnInfoListener() {
 			
 			@Override
 			public void onInfo(MediaRecorder mr, int what, int extra)
 			{
 				onVideoCaptureInfo.fire(new MediaRecorderInfo(mr, what, extra));
 			}
 		});
 
 		mediaRecorder.prepare();
 
 		Trace.Print("#######################START RECORD#####################################");
 		mediaRecorder.start();
 		
 		return outputFile;
 	}
 
 
 
 	public void stopRecording()
 	{
 		Trace.Print("#######################STOP RECORD#####################################");
 		try
 		{
 			mediaRecorder.stop();
 		}
 		catch (RuntimeException e)
 		{
 			if (outputFile != null)
 			{
 				if (outputFile.exists())
 					outputFile.delete();
 			}
 		}
 
 		mediaRecorder.reset();
 		mediaRecorder.release();
 		mediaRecorder = null;
 	}
 
 
 
 	public void startPreview(boolean lockRequired)
 	{
 		if (lockRequired)
 			camera.lock();
 		camera.startPreview();
 	}
 
 
 
 	public void stopPreview(boolean unlockRequired)
 	{
 		camera.stopPreview();
 		if (unlockRequired)
 			camera.unlock();
 	}
 	
 	public int getMaxCameraNo()
 	{
 		return Camera.getNumberOfCameras()-1;		
 	}
 }
