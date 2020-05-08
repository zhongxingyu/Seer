 /*
  * Copyright (C) 2013 InventIt Inc.
  * 
  * See https://github.com/inventit/moatandroid-examples
  */
 package com.yourinventit.moat.android.example;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Looper;
 import android.widget.Toast;
 
 import com.yourinventit.dmc.api.moat.ContextFactory;
 import com.yourinventit.dmc.api.moat.Moat;
 import com.yourinventit.dmc.api.moat.android.MoatAndroidFactory;
 import com.yourinventit.dmc.api.moat.android.MoatAndroidFactory.Callback;
 
 /**
  * 
  * @author dbaba@yourinventit.com
  * 
  */
 public class MoatIoTService extends Service {
 
 	/**
 	 * {@link Logger}
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(MoatIoTService.class);
 
 	/**
 	 * This is a shared {@link MotionSensorListener} instance between
 	 * {@link SampleApplication} and this class.
 	 */
 	private static MotionSensorListener motionSensorListener;
 
 	/**
 	 * @return the motionSensorListener
 	 */
 	static MotionSensorListener getMotionSensorListener() {
 		return MoatIoTService.motionSensorListener;
 	}
 
 	/**
 	 * @param motionSensorListener
 	 *            the motionSensorListener to set
 	 */
 	static void setMotionSensorListener(
 			MotionSensorListener motionSensorListener) {
 		MoatIoTService.motionSensorListener = motionSensorListener;
 	}
 
 	/**
 	 * Is the MoatIoTService.motionSensorListener assigned an object?
 	 * 
 	 * @return
 	 */
 	static boolean isMoationSensorListenerAssigned() {
 		return MoatIoTService.motionSensorListener != null;
 	}
 
 	/**
 	 * {@link DatabaseHelper}
 	 */
 	private DatabaseHelper databaseHelper;
 
 	/**
 	 * {@link Moat}
 	 */
 	private Moat moat;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		// This service doesn't have any IBinder instance.
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see android.app.Service#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		LOGGER.info("onCreate(): Preparing the token file and the enrollment info.");
 		super.onCreate();
 
 		// Just for code redability
 		final Context context = this;
 
 		// Creating a new DatabaseHelper
 		databaseHelper = new DatabaseHelper(context);
 		LOGGER.info("onCreate(): DatabaseHelper has been initialized.");
 
 		byte[] token = null;
 		try {
 			// Loading a security token signed twice,
 			// by Service-Sync Sandbox Server and your own.
 			token = toByteArray(getAssets().open("moat/signed.bin"));
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		}
 
 		// Initializing MOAT object asynchronously
 		MoatAndroidFactory.getInstance().initMoat(token, context,
 				new Callback() {
 					/**
 					 * This method will be invoked when the initialization is
 					 * successfully terminated.
 					 * 
 					 * @see com.yourinventit.dmc.api.moat.android.MoatAndroidFactory.Callback#onInitialized(com.yourinventit.dmc.api.moat.Moat,
 					 *      java.lang.String)
 					 */
 					public void onInitialized(Moat moat, String urnPrefix) {
 						LOGGER.info("onCreate(): OK! MOAT initialization is successful.");
 						if (databaseHelper == null) {
 							throw new IllegalStateException(
 									"Inconsistent State. Re-start the app.");
 						}
 						// Holding the passed moat instance
 						MoatIoTService.this.moat = moat;
 
 						// AndroidContextFactory
 						final ContextFactory contextFactory = new AndroidContextFactory(
 								context);
 
 						// Registering ShakeEvent model
 						final ShakeEventModelMapper shakeEventModelMapper = new ShakeEventModelMapper(
 								databaseHelper.getShakeEventDao(),
 								databaseHelper.getConnectionSource());
 						moat.registerModel(MotionSensorListener.getMoatUrn(
 								urnPrefix, "ShakeEvent", "1.0"),
 								ShakeEvent.class, shakeEventModelMapper,
 								contextFactory);
 						LOGGER.info("onCreate(): ShakeEvent has been registered.");
 
 						// Registering VibrationDevice model
 						final VibrationDevice vibrationDevice = new VibrationDevice();
 						moat.registerModel(
 								MotionSensorListener.getMoatUrn(urnPrefix,
 										"VibrateDevice", "1.0"),
 								VibrationDevice.class,
 								new VibrationDeviceModelMapper(vibrationDevice),
 								contextFactory);
 						LOGGER.info("onCreate(): VibrationDevice has been registered.");
 
 						// Instantiating a MotionSensorListener class.
 						// Notice! Assigning to the STATIC variable.
 						setMotionSensorListener(new MotionSensorListener(moat,
 								urnPrefix, shakeEventModelMapper, context));
 						LOGGER.info("onCreate(): MotionSensorListener has been initialized.");
 						LOGGER.info("onCreate(): OK. I'm ready.");
 					}
 
 					/**
 					 * This method will be invoked when unexpected exception
 					 * occurs during initialization process.
 					 * 
 					 * @see com.yourinventit.dmc.api.moat.android.MoatAndroidFactory.Callback#onThrowable(java.lang.Throwable)
 					 */
 					public void onThrowable(final Throwable throwable) {
 						LOGGER.error("onCreate(): ERROR!!!!!!!!!!.", throwable);
 						Looper.prepare();
 						new Handler(getMainLooper()).post(new Runnable() {
 							public void run() {
 								Toast.makeText(
 										getApplicationContext(),
 										"Exception Occured. Failed to initMoat!:"
 												+ throwable.getMessage(),
 										Toast.LENGTH_LONG).show();
 							}
 						});
 					}
 
 				});
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see android.app.Service#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		LOGGER.info("onDestroy(): Terminating this instance.");
 		super.onDestroy();
 
 		// Shutdown the databaseHelper
 		databaseHelper.close();
 		databaseHelper = null;
 
 		if (MoatIoTService.motionSensorListener != null) {
 			// Remove BroadcastReceiver
 			MoatIoTService.motionSensorListener.invalidate();
 			MoatIoTService.motionSensorListener = null;
 		}
 
		if (moat != null) {
 			// Remove unused model descriptors
 			moat.removeModel(ShakeEvent.class);
 			moat.removeModel(VibrationDevice.class);
			moat = null;
 		}
 		LOGGER.info("onDestroy(): Done!");
 	}
 
 	/**
 	 * Reads an {@link InputStream} and returns as bytes.
 	 * 
 	 * @param inputStream
 	 * @return
 	 */
 	static byte[] toByteArray(InputStream inputStream) {
 		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		final byte[] buffer = new byte[1024];
 		int len = 0;
 		try {
 			while ((len = inputStream.read(buffer)) != -1) {
 				outputStream.write(buffer, 0, len);
 			}
 			return outputStream.toByteArray();
 		} catch (IOException exception) {
 			throw new IllegalStateException(exception);
 		}
 	}
 }
