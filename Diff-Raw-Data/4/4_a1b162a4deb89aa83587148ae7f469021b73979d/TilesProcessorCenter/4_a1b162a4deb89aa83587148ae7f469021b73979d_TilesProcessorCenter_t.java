 package com.pandacoder.tests.mapview;
 
 import java.util.Stack;
 
 import android.graphics.Bitmap;
 import android.util.Log;
 
 /**
  * Центр обработки тайлов. Принимает запросы на выдачу изображений тайлов. Качает тайлы из сети или берет из
  * кеша в постоянной памяти. Скачанные тайлы ложит в кеш
  * 
  */
 public class TilesProcessorCenter extends Thread {
 	
 	private final static String LOG_TAG = TilesProcessorCenter.class.getSimpleName();
 	
 	private final SimpleMapView mapView;
 	private final Stack<TileRequest> tileRequestsStackQueue;
 	private final YandexTileMiner tileMiner;
 	
 	private final TilesRamCache tilesRamCache;
 	private final TilesPersistentMemoryCache tilesPersistentCache;
 	
 	private final Bitmap requestedTileBitmap;
 	
 	private boolean paused = false;
 	
 	/**
 	 * Создает центр обработки тайлов.
 	 * 
 	 * @param mapView вид карта
 	 * @param tilesRamCache кеш в оперативной памяти, если null - не используется
 	 * @param tilesPersistentCache кеш в постоянной памяти, если null - не используется
 	 * 
 	 * @throws NullPointerException если mapView == null
 	 */
 	TilesProcessorCenter(SimpleMapView mapView, TilesRamCache tilesRamCache, TilesPersistentMemoryCache tilesPersistentCache) {
 		
 		if (mapView == null) throw new NullPointerException("mapView can't be null");
 		
 		this.mapView = mapView;
 		this.tilesRamCache = tilesRamCache;
 		this.tilesPersistentCache = tilesPersistentCache;
 		
 		this.tileRequestsStackQueue = new Stack<TileRequest>();
 		this.tileMiner = new YandexTileMiner();
 		
 		this.requestedTileBitmap = Bitmap.createBitmap(TileSpecs.TILE_SIZE_WH_PX, TileSpecs.TILE_SIZE_WH_PX, TileSpecs.TILE_BITMAP_CONFIG);
 	}
 	
 
 	//TODO надо бы переделать это место
 	/**
 	 * Основная логика потока обрабоки запросов на тайлы. 
 	 * Пытается восстановить tilesPersistentCache, потом:
 	 * ждет новых запросов, если очередь запросов пуста;
 	 * скачивает новые тайлы из сети;
 	 * берет тайлы из кеша в постоянной памяти.
 	 */
 	@Override
 	public void run() {
 		
 		if (tilesPersistentCache != null) tilesPersistentCache.restore();
 		
 		while (!isInterrupted()) {
 		
 			// ждем пока нам не дадут новых заданий или не прервут нас
 			synchronized(this) {
 				if (tileRequestsStackQueue.isEmpty() || paused == true) {
 					try {
 						wait();
 					} catch (InterruptedException ex) {
 						interrupt();
 					}
 				}
 			}
 			
 			if (isInterrupted()) break;
 				
 			TileRequest currentTileRequest = null;
 			synchronized(this) {
 				if (tileRequestsStackQueue.isEmpty()) {
 					continue;
 				} else {
 					currentTileRequest = tileRequestsStackQueue.pop();
 				}
 			}
 			
 			if (isInterrupted()) break;
 			
 			boolean tileWasInCache = false;
 			if (tilesPersistentCache != null) {
 				tileWasInCache = tilesPersistentCache.get(currentTileRequest, requestedTileBitmap);
 				if (tileWasInCache == true) {
 					mapView.addTileOnMapBitmap(currentTileRequest, requestedTileBitmap);
 					Log.i(LOG_TAG, "FROM FLASH: " + currentTileRequest);
 				}
 			}
 			
 			if (isInterrupted()) break;
 			
 			if (tileWasInCache == false) { // нужно скачать тайл
 				Bitmap tileBitmap = tileMiner.getTileBitmap(currentTileRequest);
 				if (tileBitmap != null) {
 					
 					if (tilesRamCache != null) {	// если есть кеш в раме
 						tilesRamCache.put(currentTileRequest, tileBitmap);
 					}
 
 					if (tilesPersistentCache != null) {	// если ест кеш во флеше
 						tilesPersistentCache.put(currentTileRequest, tileBitmap);
 					}
 					
 					mapView.addTileOnMapBitmap(currentTileRequest, tileBitmap);
 					Log.i(LOG_TAG, "FROM NET: " + currentTileRequest);
					tileBitmap.recycle();
 				}
 			}			
 		}
 	}
 
 	/**
 	 * Запрашивает опеределенный тайл.
 	 * @param tileRequest запрос
 	 */
 	public synchronized void request(TileRequest tileRequest) {
 		if (!tileRequestsStackQueue.contains(tileRequest)) {
 			tileRequestsStackQueue.push(tileRequest);
 		}
 	}
 	
 	/**
 	 * Очищает очередь ожидающих обработки запросов
 	 */
 	public synchronized void clearRequstQueue() {
 		tileRequestsStackQueue.clear();
 	}
 	
 	/**
 	 * Инициирует обработку запросов, из очереди. Вызывать после добавления новых 
 	 * запросов в очередь методом request.
 	 */
 	public synchronized void doRequests() {
 		notify();
 	}
 	
 	/**
 	 * Ставит процессор тайлов на паузу. Новые задание не начинают обработку.
 	 */
 	public synchronized void pauseProcessing() {
 		paused = true;
 		notify();
 	}
 	
 	/**
 	 * Возобновляет процесс обработки тайлов, если он до этого был остановлен
 	 * функцией {@link #pauseProcessing} 
 	 */
 	public synchronized void resumeProcessing() {
 		paused = false;
 		notify();
 	}
 	
 	
 	/**
 	 * Очищает ресурсы
 	 */
 	public synchronized void destroy() {
 		
 		if (requestedTileBitmap != null) {
 			requestedTileBitmap.recycle();
 		}
 	} 
 }
