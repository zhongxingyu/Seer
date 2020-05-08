 package il.ac.huji.shoppit;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.ImageFormat;
 import android.graphics.Matrix;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.google.zxing.*;
 import com.google.zxing.common.HybridBinarizer;
 import com.google.zxing.oned.EAN13Reader;
 
 /**
  * @author Elie2
  * This fragment is used to take a picture. It is used by both NewItemActivity and NewShopActivity.
  */
 public class CameraFragment extends Fragment {
 
 	public static final String TAG = "CameraFragment";
 
 	private Camera camera;
 	private SurfaceView surfaceView;
 	private ImageButton photoButton;
 	private Button barcodeButton;
 	private boolean barcodeMode = false,
 			currentlyScanning = false;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
 			Bundle savedInstanceState) {
 
 		barcodeMode = false;
 		currentlyScanning = false;
 		View v = inflater.inflate(R.layout.fragment_camera, parent, false);
 
 		photoButton = (ImageButton) v.findViewById(R.id.camera_photo_button);
 		barcodeButton = (Button) v.findViewById(R.id.barcode_button);
 
 		// hide barcode button when adding shop
 		if (getActivity().getClass() == NewShopActivity.class) {
 			barcodeButton.setVisibility(View.INVISIBLE);
 		}
 
 		if (camera == null) {
 			try {
 				camera = Camera.open();
 				photoButton.setEnabled(true);
 			} catch (Exception e) {
 				Log.e(TAG, "No camera with exception: " + e.getMessage());
 				photoButton.setEnabled(false);
 				Toast.makeText(getActivity(), "No camera detected",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 
 		//Barcode scanning is allowed only if the camera is of type NV21.
 		//Don't really know what that is, but that's the only option I've seen when scanning
 		//for a barcode. Seems like it's the most probable option though.
 		Parameters parameters = camera.getParameters();
 		int imageFormat = parameters.getPreviewFormat();
 		if (imageFormat != ImageFormat.NV21)
 			barcodeButton.setVisibility(View.INVISIBLE);
 
 
 
 		//This part will search for a barcode in the image.
 		camera.setPreviewCallback(new Camera.PreviewCallback() {
 
 			@Override
 			public void onPreviewFrame(byte[] data, Camera camera) {
 				if (barcodeMode && !currentlyScanning) {
 
 					currentlyScanning = true;
 
 					//When we see that barcode works this code can be erased
 					/*Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);
 					Log.d("HERE", bMap+"");
 					int w = bMap.getWidth();
 					int h = bMap.getHeight();
 					int imageSize = w * h;
 					int [] argb = new int[imageSize];
 					bMap.getPixels(argb, 0, w, 0, 0, w, h);
 					byte [] yuv = new byte[imageSize / 2 * 3];
 					encodeYUV420SP(yuv, argb, w, h);
 					bMap.recycle();*/
 					
 					
 					//This part gets the image from the camera in the appropriate format
 					Camera.Parameters parameters = camera.getParameters();
 			        Size size = parameters.getPreviewSize();
 			        int w = size.width;
 					int h = size.height;
 					LuminanceSource source = new PlanarYUVLuminanceSource(data, w, h, 0, 0, w, h, false);
 					BinaryBitmap binBmp = new BinaryBitmap(new HybridBinarizer(source));
 
 					//This is the actual scanning
 					try {
 						EAN13Reader reader = new EAN13Reader();
 						reader.reset();
 						String code = reader.decode(binBmp).getText();
 						
 						Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
 						Log.d("BARCODE", code);
 						camera.setPreviewCallback(null);
 
 					} catch (Exception e) {
 						// barcode not recognized in picture
 						Log.d("ERROR", e.toString());
 					}
 
 					currentlyScanning = false;
 				}
 			}
 		});
 
 
 		photoButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (camera == null)
 					return;
 
 				camera.takePicture(new Camera.ShutterCallback() {
 
 					@Override
 					public void onShutter() {
 						// nothing to do
 					}
 
 				}, null, new Camera.PictureCallback() {
 
 					@Override
 					public void onPictureTaken(byte[] data, Camera camera) {
 						saveScaledPhoto(data);
 						// addPhotoToShopAndReturn(data);
 					}
 
 				});
 
 			}
 		});
 
 		barcodeButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				barcodeMode = !barcodeMode;
 				if (barcodeMode) {
 					photoButton.setVisibility(View.INVISIBLE);
 					barcodeButton.setText("Take item picture");
 				}
 				else {
 					photoButton.setVisibility(View.VISIBLE);
 					barcodeButton.setText("Scan barcode");
 				}
 
 			}
 
 		});
 
 		surfaceView = (SurfaceView) v.findViewById(R.id.camera_surface_view);
 		SurfaceHolder holder = surfaceView.getHolder();
 		holder.addCallback(new Callback() {
 
 			public void surfaceCreated(SurfaceHolder holder) {
 				try {
 					if (camera != null) {
 						camera.setDisplayOrientation(90);
 						camera.setPreviewDisplay(holder);
 						camera.startPreview();
 					}
 				} catch (IOException e) {
 					Log.e(TAG, "Error setting up preview", e);
 				}
 			}
 
 			public void surfaceChanged(SurfaceHolder holder, int format,
 					int width, int height) {
 				// nothing here
 			}
 
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				// nothing here
 			}
 
 		});
 
 		return v;
 	}
 
 
 
 	//Helper functions for getting the image from the camera in the right format
	byte [] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
 
 		int [] argb = new int[inputWidth * inputHeight];
 
 		scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
 
 		byte [] yuv = new byte[inputWidth*inputHeight*3/2];
 		encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
 
 		scaled.recycle();
 
 		return yuv;
 	}
 
 	void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
 		final int frameSize = width * height;
 
 		int yIndex = 0;
 		int uvIndex = frameSize;
 
 		int a, R, G, B, Y, U, V;
 		int index = 0;
 		for (int j = 0; j < height; j++) {
 			for (int i = 0; i < width; i++) {
 
 				a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
 				R = (argb[index] & 0xff0000) >> 16;
 				G = (argb[index] & 0xff00) >> 8;
 				B = (argb[index] & 0xff) >> 0;
 
 				// well known RGB to YUV algorithm
 				Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
 				U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
 				V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;
 
 				// NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
 				//    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
 				//    pixel AND every other scanline.
 				yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
 				if (j % 2 == 0 && index % 2 == 0) { 
 					yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
 					yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
 				}
 
 				index ++;
 			}
 		}
	}
 
 
 
 	/* we don't actually want this scaling/resizing, since it makes the picture too small,
 	 * but we might want to do some other scaling, so I've left the code in for now.
 	 * 
 	 * ParseQueryAdapter loads ParseFiles into a ParseImageView at whatever size
 	 * they are saved. Since we never need a full-size image in our app, we'll
 	 * save a scaled one right away.
 	 */
 	//	@SuppressWarnings("unused")
 	//	private void saveScaledPhoto(byte[] data) {
 	//		
 	//		// Resize photo from camera byte array
 	//		Bitmap shopImage = BitmapFactory.decodeByteArray(data, 0, data.length);
 	//		Bitmap shopImageScaled = Bitmap.createScaledBitmap(shopImage, 200, 200
 	//				* shopImage.getHeight() / shopImage.getWidth(), false);
 	//
 	//		Matrix matrix = new Matrix();
 	//		matrix.postRotate(90);
 	//		Bitmap rotatedScaledShopImage = Bitmap.createBitmap(shopImageScaled, 0,
 	//				0, shopImageScaled.getWidth(), shopImageScaled.getHeight(),
 	//				matrix, true);
 	//
 	//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	//		rotatedScaledShopImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 	//
 	//		byte[] scaledData = bos.toByteArray();
 	//		
 	//		addPhotoToShopAndReturn(scaledData);
 	//	}
 
 	private void saveScaledPhoto(byte[] data) {
 
 		// Resize photo from camera byte array
 		Bitmap shopImage = BitmapFactory.decodeByteArray(data, 0, data.length);
 
 		Matrix matrix = new Matrix();
 		matrix.postRotate(90);
 		Bitmap rotatedShopImage = Bitmap.createBitmap(shopImage, 0,
 				0, shopImage.getWidth(), shopImage.getHeight(),
 				matrix, true);
 
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		rotatedShopImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 
 		byte[] scaledData = bos.toByteArray();
 
 		addPhotoToShopAndReturn(scaledData);
 	}
 
 	private void addPhotoToShopAndReturn(byte[] data) {
 
 		if (getActivity().getClass() == NewShopActivity.class) {
 			Log.i(TAG, "NewShopActivity");
 			((NewShopActivity) getActivity()).setCurrentPhotoData(data);
 
 			FragmentManager fm = getActivity().getFragmentManager();
 			fm.popBackStack("NewShopFragment",
 					FragmentManager.POP_BACK_STACK_INCLUSIVE);
 		} else if (getActivity().getClass() == NewItemActivity.class) {
 			Log.i(TAG, "NewItemActivity");
 			((NewItemActivity) getActivity()).setCurrentPhotoData(data);
 
 			Fragment cameraFragment = new NewItemFragment();
 			FragmentTransaction transaction = getActivity().getFragmentManager()
 					.beginTransaction();
 			transaction.replace(R.id.fragmentContainer, cameraFragment);
 			transaction.addToBackStack("NewItemFragment");
 			transaction.commit();
 
 		} else {
 			Log.e(TAG, "error in addPhotoToShopAndReturn");
 		}
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		if (camera == null) {
 			try {
 				camera = Camera.open();
 				photoButton.setEnabled(true);
 			} catch (Exception e) {
 				Log.i(TAG, "No camera: " + e.getMessage());
 				photoButton.setEnabled(false);
 				Toast.makeText(getActivity(), "No camera detected",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		if (camera != null) {
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 		}
 		super.onPause();
 	}
 
 }
