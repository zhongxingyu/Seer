 package com.android.tecla.keyboard;
 
 import com.android.inputmethod.keyboard.Key;
 import com.android.inputmethod.keyboard.Keyboard;
 import com.android.inputmethod.keyboard.KeyboardView;
 
 import android.os.Handler;
 import android.os.Message;
 
 public class IMEAdapter {
 
 	private static final String tag = "IMEAdapter";
 	
 	private static Keyboard sKeyboard = null;
 	private static KeyboardView sKeyboardView = null;
 	private static Key[] sKeys = null;
 		
 	private static final int REDRAW_KEYBOARD = 0x22;
 	
 	private static Handler sHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case(REDRAW_KEYBOARD):	sKeyboardView.invalidateAllKeys();
 									break;
 			default:				break;
 			}	
 			super.handleMessage(msg);
 		}
 		
 	};
 	
 	public static boolean setKeyboardView(KeyboardView kbv) {
 		sKeyboardView = kbv;
 		if(kbv == null) {
 			sKeyboard = null;
 			sKeys = null;	
 			return false;
 		}
 		sKeyboard = kbv.getKeyboard();
 		if(sKeyboard == null || sKeyboard.mKeys == null) return false;
 		sKeys = sortKeys(sKeyboard.mKeys);
 		IMEStates.reset();
 		return true;
 	}
 	
 	private static Key[] sortKeys(Key[] keys) {
 		Key[] sorted_keys = keys.clone();
 		
 		// sort rows
 		for(int i=1; i<sorted_keys.length; ++i) {
 			Key key = sorted_keys[i];
 			boolean inserted = false;
 			for(int j=i; j>0 && !inserted; --j) {
 				if(key.mY >= sorted_keys[j-1].mY) {
 					sorted_keys[j] = key; 
 					inserted = true;
 				} else if(j==1) {
 					sorted_keys[j] = sorted_keys[j-1]; 
 					sorted_keys[j-1] = key;
 				} else {
 					sorted_keys[j] = sorted_keys[j-1]; 
 				}
 			}
 		}
 		
 		// sort columns
 		int start_index = 0;
 		int end_index = 0;
 		while(start_index < sorted_keys.length) {
 			while(end_index<sorted_keys.length ) {
 				if(sorted_keys[start_index].mY != sorted_keys[end_index].mY) break;
 				++end_index;
 			}
 			for(int i=start_index + 1; i<end_index; ++i) {
 				Key key = sorted_keys[i];
 				boolean inserted = false;
 				for(int j=i; j>start_index && !inserted; --j) {
 					if(key.mX >= sorted_keys[j-1].mX) {
 						sorted_keys[j] = key; 
 						inserted = true;
 					} else if(j==start_index+1) {
 						sorted_keys[j] = sorted_keys[j-1]; 
 						sorted_keys[j-1] = key;
 					} else {
 						sorted_keys[j] = sorted_keys[j-1]; 
 					}
 				}
 			}
 			start_index = end_index;
 		}
 		
 		return sorted_keys;
 		
 	}
 	
 	public static void selectHighlighted() {
 		int index = IMEStates.getCurrentKeyIndex();
 		if(index < 0 || index >= sKeys.length) return;
 		Key key = sKeys[index];
 		TeclaIME.getInstance().getCurrentInputConnection()
 			.commitText(String.valueOf((char)key.mCode), 1);		
 	}
 
 	public static void selectScanHighlighted() {
 		IMEStates.click();
 	}
 
 	public static void scanNext() {
 		switch(IMEStates.sState) {
 		case(IMEStates.SCAN_STOPPED):	break;
 		case(IMEStates.SCAN_ROW):		IMEAdapter.highlightNextRow();
 										break;
 		case(IMEStates.SCAN_COLUMN):	IMEAdapter.highlightNextKey();
 										break;
 		case(IMEStates.SCAN_CLICK):		IMEStates.sState = IMEStates.SCAN_ROW;
 										highlightKey(IMEStates.getCurrentKeyIndex(), false);
 										IMEStates.reset();
 										IMEAdapter.highlightNextRow();		
 										break;
 		default:						break;
 		}		
 	}
 	
 	public static void scanPrevious() {
 		switch(IMEStates.sState) {
 		case(IMEStates.SCAN_STOPPED):	break;
 		case(IMEStates.SCAN_ROW):		IMEAdapter.highlightPreviousRow();
 										break;
 		case(IMEStates.SCAN_COLUMN):	IMEAdapter.highlightPreviousKey();
 		case(IMEStates.SCAN_CLICK):		IMEStates.sState = IMEStates.SCAN_ROW;
 										IMEStates.reset();
 										IMEAdapter.highlightPreviousRow();	
 										break;
 		default:						break;
 		}		
 		
 	}
 
 	public static void scanUp() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys[index];
 		Key test_key;
 		int closest_key_index = -1;
 		float distance_sq, shortest_distance_sq;
 		shortest_distance_sq = Float.MAX_VALUE;
 		for(int i=0; i<sKeys.length; ++i) {
 			if(i == index) continue;
 			test_key = sKeys[i];
 			if(test_key.mY < key.mY) {
 				distance_sq = (key.mY - test_key.mY)*(key.mY - test_key.mY) + 
 						(key.mX - test_key.mX)*(key.mX - test_key.mX); 
 				if(distance_sq < shortest_distance_sq) {
 					shortest_distance_sq = distance_sq;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void scanDown() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys[index];
 		Key test_key;
 		int closest_key_index = -1;
 		float distance_sq, shortest_distance_sq;
 		shortest_distance_sq = Float.MAX_VALUE;
 		for(int i=0; i<sKeys.length; ++i) {
 			if(i == index) continue;
 			test_key = sKeys[i];
 			if(test_key.mY > key.mY) {
 				distance_sq = (key.mY - test_key.mY)*(key.mY - test_key.mY) + 
 						(key.mX - test_key.mX)*(key.mX - test_key.mX); 
 				if(distance_sq < shortest_distance_sq) {
 					shortest_distance_sq = distance_sq;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void scanLeft() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys[index];
 		Key test_key;
 		int closest_key_index = -1;
 		int distance;
 		int shortest_distance = Integer.MAX_VALUE;
 		for(int i=0; i<sKeys.length; ++i) {
 			if(i == index) continue;
 			test_key = sKeys[i];
 			if(test_key.mY == key.mY && test_key.mX < key.mX) {
 				distance = key.mX - test_key.mX;
 				if(distance < shortest_distance) {
 					shortest_distance = distance;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void scanRight() {
 		int index = IMEStates.getCurrentKeyIndex();
 		Key key = sKeys[index];
 		Key test_key;
 		int closest_key_index = -1;
 		int distance;
 		int shortest_distance = Integer.MAX_VALUE;
 		for(int i=0; i<sKeys.length; ++i) {
 			if(i == index) continue;
 			test_key = sKeys[i];
 			if(test_key.mY == key.mY && test_key.mX > key.mX) {
 				distance = test_key.mX - key.mX;
 				if(distance < shortest_distance) {
 					shortest_distance = distance;
 					closest_key_index = i;
 				}
 			}
 		}
 		if(closest_key_index != -1) {
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.setKey(closest_key_index);
 			IMEAdapter.highlightKey(closest_key_index, true);
 		}
 		IMEAdapter.invalidateKeys();
 	}
 
 	public static void stepOut() {
 		if(IMEStates.sState != IMEStates.SCAN_ROW) {
 			int index = IMEStates.getCurrentKeyIndex();
 			IMEAdapter.highlightKey(index, false);
 			IMEStates.sState = IMEStates.SCAN_ROW;
 			IMEStates.reset();
 			IMEAdapter.highlightKeys(IMEStates.getRowStart(0), 
 					IMEStates.getRowEnd(0), true);
 		}
 	}
 	
 	
 	private static void highlightKey(int key_index, boolean highlighted) {
 		if(sKeys == null || key_index < 0 || key_index >= sKeys.length) return; 
         Key key = sKeys[key_index];
         if(highlighted) key.onPressed();
         else key.onReleased();
 	}	
 	
 	private static void highlightKeys(int start_index, int end_index, boolean highlighted) {
 		for(int i=start_index; i<=end_index; ++i) {
 			highlightKey(i, highlighted);
 		}			
 	}
 
 	private static void invalidateKeys() {
 		Message msg = new Message();
 		msg.what = IMEAdapter.REDRAW_KEYBOARD;
 		sHandler.sendMessageDelayed(msg, 0);		
 	}
 	
 	private static void highlightNextKey() {
 		if(sKeyboard ==null) return;
 		highlightKey(IMEStates.getCurrentKeyIndex(), false);
 		highlightKey(IMEStates.scanNextKey(), true);
 		invalidateKeys();	
 	}
 	
 	private static void highlightPreviousKey() {
 		if(sKeyboard ==null) return;
 		highlightKey(IMEStates.getCurrentKeyIndex(), false);
 		highlightKey(IMEStates.scanPreviousKey(), true);
 		invalidateKeys();		
 	}
 	
 	private static void highlightNextRow() {
 		if(sKeyboard ==null) return;
 		if(IMEStates.sCurrentRow == IMEStates.sRowCount)
 			WordPredictionAdapter.highlightNext();
 		else highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
 		IMEStates.scanNextRow();
 		if(IMEStates.sCurrentRow == IMEStates.sRowCount)
 			WordPredictionAdapter.highlightNext();
 		else highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
 		invalidateKeys();		
 	}
 	
 	private static void highlightPreviousRow() {
 		if(sKeyboard ==null) return;
 		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
 		IMEStates.scanPreviousRow();
 		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
 		invalidateKeys();		
 	}
 	
 	private static class IMEStates {
 
 		private static final int SCAN_STOPPED = 0xa0;
 		private static final int SCAN_ROW = 0xa1;
 		private static final int SCAN_COLUMN = 0xa2;
 		private static final int SCAN_CLICK = 0xa3;
 		private static final int SCAN_WORDPREDICTION = 0xa4;
 		private static int sState = SCAN_STOPPED;
 		
 		private static int sRowCount = 0;
 		private static int sCurrentRow = -1;
 		private static int sCurrentColumn = -1; 
 		private static int sKeyStartIndex = -1;
 		private static int sKeyEndIndex = -1;
 		
 		private static void reset() {
 			if(sKeyboard == null) return;
 			sRowCount = getRowCount();
 			sCurrentRow = -1;
 			sCurrentColumn = -1;
 			sKeyStartIndex = getRowStart(0);
 			sKeyEndIndex = getRowEnd(0);
 		}
 
 		private static void click() {
 			switch(sState) {
 			case(SCAN_STOPPED):		sState = SCAN_ROW;
 									AutomaticScan.startAutoScan();
 									break;
 			case(SCAN_ROW):			if(sCurrentRow == sRowCount) sState = SCAN_WORDPREDICTION;
 									else sState = SCAN_COLUMN;
 									highlightKeys(sKeyStartIndex, sKeyEndIndex, false);
 									AutomaticScan.resetTimer();
 									break;
 			case(SCAN_COLUMN):		sState = SCAN_CLICK;
 									IMEAdapter.selectHighlighted();
 									AutomaticScan.setExtendedTimer();
 									break;
 			case(SCAN_CLICK):		IMEAdapter.selectHighlighted();
 									AutomaticScan.setExtendedTimer();
 									break;
 			case(SCAN_WORDPREDICTION):	break;
 			default:			break;
 			}
 		}
 		
 		private static int getCurrentKeyIndex() {
 			return sCurrentColumn;
 		}
 
 		private static int getCurrentRowIndex() {
 			return sCurrentRow;
 		}
 		
 		private static boolean setKey(int index) {
 			if(index < 0 || index >= sKeys.length) return false;
 			sCurrentColumn = index;
 			return true;
 		}
 
 		private static boolean setKeyRow(int index) {
 			if(index < 0 || index >= IMEStates.sRowCount) return false;
 			sCurrentRow = index;
 			return true;
 		}
 		
 		private static int scanNextKey() {
 			if(sCurrentColumn == -1) sCurrentColumn = sKeyStartIndex;
 			else {
 				++sCurrentColumn;
 				if(sCurrentColumn > sKeyEndIndex) sCurrentColumn = -1;
 			}
 			return sCurrentColumn;
 		}
 		
 		private static int scanPreviousKey() {
 			if(sCurrentColumn == -1) sCurrentColumn = sKeyEndIndex;
 			else {
 				--sCurrentColumn;
 				if(sCurrentColumn < sKeyStartIndex) sCurrentColumn = -1;
 			}
 			return sCurrentColumn;
 		}
 		
 		private static void scanNextRow() {
 			++sCurrentRow;
 			sCurrentRow %= sRowCount + 1;
 			updateRowKeyIndices();
 		}
 		
 		private static void scanPreviousRow() {
 			if(sCurrentRow == -1) sCurrentRow = sRowCount;
 			else --sCurrentRow;
 			updateRowKeyIndices();
 		}
 		
 		private static void updateRowKeyIndices() {
 			sKeyStartIndex = getRowStart(sCurrentRow);
 			sKeyEndIndex = getRowEnd(sCurrentRow);			
 		}
 		
 		private static int getRowStart(int rowNumber) {
			if(sKeyboard == null || rowNumber == -1 || rowNumber == sRowCount) return -1;
 			int keyCounter = 0;
 			if (rowNumber != 0) {
 				Key[] keyList = sKeys;
 				Key key;
 				int rowCounter = 0;
 				int prevCoord = keyList[0].mY;
 				int thisCoord;
 				while (rowCounter != rowNumber) {
 					keyCounter++;
 					key = keyList[keyCounter];
 					thisCoord = key.mY;
 					if (thisCoord != prevCoord) {
 						// Changed rows
 						rowCounter++;
 						prevCoord = thisCoord;
 					}
 				}
 			}
 			return keyCounter;
 		}
 
 		private static int getRowEnd(int rowNumber) {
			if(sKeyboard == null || rowNumber == -1 || rowNumber == sRowCount) return -1;
 			Key[] keyList = sKeys;
 			int totalKeys = keyList.length;
 			int keyCounter = 0;
 			if (rowNumber == (getRowCount() - 1)) {
 				keyCounter = totalKeys - 1;
 			} else {
 				Key key;
 				int rowCounter = 0;
 				int prevCoord = keyList[0].mY;
 				int thisCoord;
 				while (rowCounter <= rowNumber) {
 					keyCounter++;
 					key = keyList[keyCounter];
 					thisCoord = key.mY;
 					if (thisCoord != prevCoord) {
 						// Changed rows
 						rowCounter++;
 						prevCoord = thisCoord;
 					}
 				}
 				keyCounter--;
 			}
 			return keyCounter;
 		}
 
 		private static int getRowCount() {
 			if(sKeyboard == null) return 0;
 			Key[] keyList = sKeys;
 			Key key;
 			int rowCounter = 0;
 			int coord = 0;
 			for(int i=0; i<keyList.length; ++i) {
 				key = keyList[i];
 				if (rowCounter == 0) {
 					rowCounter++;
 					coord = key.mY;
 				}
 				if (coord != key.mY) {
 					rowCounter++;
 					coord = key.mY;
 				}
 			}
 			return rowCounter;
 		}
 			
 	}
 	
 }
