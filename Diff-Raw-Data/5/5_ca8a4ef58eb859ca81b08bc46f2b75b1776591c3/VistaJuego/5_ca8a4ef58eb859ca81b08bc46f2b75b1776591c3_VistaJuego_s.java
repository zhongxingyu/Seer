 package org.example.asteroides;
 
 import java.util.List;
 import java.util.Vector;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint.Style;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.RectShape;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class VistaJuego extends View implements SensorEventListener {
 
 	// //// ASTEROIDES //////
 
 	private Vector<Grafico> _asteroides; // Vector con los Asteroides
 	private int numAsteroides = 5; // Número inicial de asteroides
 	private int numFragmentos = 3; // Fragmentos en que se divide
 
 	// NAVE //
 	private Grafico nave;// Gráfico de la nave
 	private int giroNave; // Incremento de dirección
 	private float aceleracionNave; // aumento de velocidad
 
 	// Incremento estándar de giro y aceleración
 
 	private static final int PASO_GIRO_NAVE = 5;
 	private static final float PASO_ACELERACION_NAVE = 0.5f;
 
 	// //// THREAD Y TIEMPO //////
 
 	// Thread encargado de procesar el juego
 	private ThreadJuego thread = new ThreadJuego();
 
 	// Cada cuanto queremos procesar cambios (ms)
 	private static int PERIODO_PROCESO = 50;
 
 	// Cuando se realizó el último proceso
 	private long ultimoProceso = 0;
 
 	// //// MISIL //////
 	private Grafico misil;
 	private static int PASO_VELOCIDAD_MISIL = 12;
 	private boolean misilActivo = false;
 	private int tiempoMisil;
 
 	private float lastEventX = 0;
 	private float lastEventY = 0;
 	private boolean disparo = false;
 
 	// preferences //
 
 	private int _graphicsType = 0;
 
 	// sensores //
 
 	SensorManager _sensorManager;
 
 	public VistaJuego(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		_sensorManager = (SensorManager) context.getSystemService(
 			Context.SENSOR_SERVICE);
 
 		List<Sensor> listSensors = _sensorManager.getSensorList(
 			Sensor.TYPE_ACCELEROMETER);
 
 		if (!listSensors.isEmpty()) {
			Sensor orientationSensor = listSensors.get(0);
 			_sensorManager.registerListener(
				this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
 		}
 
 		Drawable drawableNave  = null;
 		Drawable drawableAsteroide = null;
 		Drawable drawableMisil = null;
 
 		drawableNave =
 			context.getResources().getDrawable(R.drawable.nave);
 
 		drawableAsteroide =
 			context.getResources().getDrawable(R.drawable.asteroide1);
 
 		_asteroides = new Vector<Grafico>();
 
 		for (int i = 0; i < numAsteroides; i++) {
 			Grafico asteroide = new Grafico(this, drawableAsteroide);
 
 			asteroide.setIncY(Math.random() * 4 - 2);
 			asteroide.setIncX(Math.random() * 4 - 2);
 			asteroide.setAngulo((int) (Math.random() * 360));
 			asteroide.setRotacion((int) (Math.random() * 8 - 4));
 
 			_asteroides.add(asteroide);
 		}
 
 		nave = new Grafico(this, drawableNave);
 
 		// graphic type is retrieved from preferences
 
 		SharedPreferences prefs =
 			PreferenceManager.getDefaultSharedPreferences(context);
 
 		// and set remembered preferences
 		_graphicsType = Integer.parseInt(
 			prefs.getString("tiposGraficos", "1"));
 
 		if (_graphicsType == 0) {
 			// misil vectorial
 
 			ShapeDrawable dMisil = new ShapeDrawable(new RectShape());
 			dMisil.getPaint().setColor(Color.WHITE);
 			dMisil.getPaint().setStyle(Style.STROKE);
 			dMisil.setIntrinsicWidth(15);
 			dMisil.setIntrinsicHeight(3);
 
 			drawableMisil = dMisil;
 		}
 		else if (_graphicsType == 1){
 			// bitmap
 
 			drawableMisil =
 				context.getResources().getDrawable(R.drawable.misil1);
 		}
 
 		misil = new Grafico(this, drawableMisil);
 	}
 
 	public ThreadJuego getThread() {
 		return thread;
 	}
 
 	public void pausarSensores() {
 		_sensorManager.unregisterListener(this);
 	}
 
 	public void reanudarSensores() {
 		List<Sensor> accelerometerSensors = _sensorManager.getSensorList(
 			Sensor.TYPE_ACCELEROMETER);
 
 		if (!accelerometerSensors.isEmpty()) {
 			for (Sensor sensor : accelerometerSensors) {
 				if (sensor != null) {
 					_sensorManager.registerListener(
 						this, sensor, SensorManager.SENSOR_DELAY_GAME);
 				}
 			}
 		}
 	}
 
 	protected synchronized void actualizaFisica() {
 		long ahora = System.currentTimeMillis();
 
 		// No hagas nada si el período de proceso no se ha cumplido.
 
 		if (ultimoProceso + PERIODO_PROCESO > ahora) {
 			return;
 		}
 
 		// Para una ejecución en tiempo real calculamos retardo
 
 		double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO;
 
 		ultimoProceso = ahora; // Para la próxima vez
 
 		// Actualizamos velocidad y dirección de la nave a partir de 
 		// giroNave y aceleracionNave (según la entrada del jugador)
 
 		nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));
 
 		double nIncX = nave.getIncX() + aceleracionNave *
 			Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
 
 		double nIncY = nave.getIncY() + aceleracionNave *
 			Math.sin(Math.toRadians(nave.getAngulo())) * retardo;
 
 		// Actualizamos si el módulo de la velocidad no excede el máximo
 
 		if (Math.hypot(nIncX,nIncY) <= Grafico.getMaxVelocidad()){
 			nave.setIncX(nIncX);
 			nave.setIncY(nIncY);
 		}
 
 		// Actualizamos posiciones X e Y
 
 		nave.incrementaPos(retardo);
 
 		for (Grafico asteroide : _asteroides) {
 			asteroide.incrementaPos(retardo);
 		}
 
 		if (misilActivo) {
 			misil.incrementaPos(retardo);
 			tiempoMisil -= retardo;
 
 			if (tiempoMisil < 0) {
 				misilActivo = false;
 			} 
 			else {
 				for (int i = 0; i < _asteroides.size(); i++)
 					if (misil.verificaColision(_asteroides.elementAt(i))) {
 						_destruyeAsteroide(i);
 
 						break;
 					}
 			}
 		}
 	}
 
 	@Override
 	protected void onSizeChanged(
 		int ancho, int alto, int ancho_inter, int alto_inter) {
 
 		super.onSizeChanged(ancho, alto, ancho_inter, alto_inter);
 
 		// Una vez que conocemos nuestro ancho y alto.
 
 		for (Grafico asteroide : _asteroides) {
 			do{
 				asteroide.setPosX(Math.random()*(ancho-asteroide.getAncho()));
 				asteroide.setPosY(Math.random()*(alto-asteroide.getAlto()));
 			}
 			while(asteroide.distancia(nave) < (ancho+alto)/5);
 		}
 
 		int x_centro = (super.getWidth() - nave.getAncho()) / 2;
 		int y_centro = (super.getHeight() - nave.getAlto()) / 2;
 
 		nave.setPosX(x_centro);
 		nave.setPosY(y_centro);
 
 		ultimoProceso = System.currentTimeMillis();
 		thread.start();
 	}
 
 	@Override
 	public boolean onTouchEvent (MotionEvent event) {
 		super.onTouchEvent(event);
 
 		float currentEventX = event.getX();
 		float currentEventY = event.getY();
 
 		switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				disparo = true;
 
 				break;
 			case MotionEvent.ACTION_MOVE:
 				float desplazamientoX = Math.abs(currentEventX - lastEventX);
 				float desplazamientoY = Math.abs(currentEventY - lastEventY);
 
 				if (desplazamientoY < 6 && desplazamientoX > 6){
 					// desplazamiento horizontal : x > y
 
 					giroNave = Math.round((currentEventX - lastEventX) / 2);
 					disparo = false;
 				}
 				else if (desplazamientoX < 6 && desplazamientoY > 6){
 					// desplazamiento vertical: y > x
 
 					// aceleramos si la Y actual es menor que la anterior
 
 					if (lastEventY > currentEventY) {
 						aceleracionNave =
 							Math.round((lastEventY - currentEventY) / 100);
 					}
 					else {
 						aceleracionNave = 0;
 					}
 
 					disparo = false;
 				}
 
 				break;
 			case MotionEvent.ACTION_UP:
 				giroNave = 0;
 				aceleracionNave = 0;
 
 				if (disparo){
 					_activaMisil();
 				}
 
 				break;
 		}
 
 		lastEventX = currentEventX;
 		lastEventY = currentEventY;
 
 		return true;
 	}
 
 	@Override
 	protected synchronized void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		for (Grafico asteroide : _asteroides) {
 			asteroide.dibujaGrafico(canvas);
 		}
 
 		nave.dibujaGrafico(canvas);
 
 		if (misilActivo) {
 			misil.dibujaGrafico(canvas);
 		}
 	}
 
 	public class ThreadJuego extends Thread {
 
 		@Override
 		public void run() {
 			_corriendo = true;
 
 			while (_corriendo) {
 				actualizaFisica();
 
 				synchronized (this) {
 					while (_pausa) {
 						try {
 							wait();
 						} catch (Exception e) {
 						}
 					}
 				}
 			}
 		}
 
 		public synchronized void pausar() {
 			_pausa = true;
 		}
 
 		public synchronized void reanudar() {
 			_pausa = false;
 
 			notify();
 		}
 
 		public void detener() {
 			_corriendo = false;
 
 			if (_pausa) {
 				reanudar();
 			}
 		}
 
 		private boolean _pausa;
 		private boolean _corriendo;
 
 	}
 
 	private void _destruyeAsteroide(int i) {
 		_asteroides.remove(i);
 
 		misilActivo = false;
 	}
 
 	private void _activaMisil() {
 		misil.setPosX(
 			nave.getPosX() + nave.getAncho() / 2 - misil.getAncho() / 2);
 		misil.setPosY(
 			nave.getPosY() + nave.getAlto() / 2 - misil.getAlto() / 2);
 		misil.setAngulo(nave.getAngulo());
 		misil.setIncX(
 			Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
 		misil.setIncY(
 			Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
 
 		tiempoMisil =
 			(int) Math.min(
 				this.getWidth() / Math.abs( misil.getIncX()),
 				this.getHeight() / Math.abs(misil.getIncY())) - 2;
 
 		misilActivo = true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 	}
 
 	private boolean hayValorInicial = false;
 	private float valorInicial;
 
 	@Override 
 	public void onSensorChanged(SensorEvent event) {
 		// 0: axis Z
 		// 1: axis X
 		// 2: axis Y
 		float valor = event.values[1];
 
 		if (!hayValorInicial){
 			valorInicial = valor;
 			hayValorInicial = true;
 		}
 
 		giroNave = (int) (valor - valorInicial);
 	}
 
 }
