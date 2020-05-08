 package se.fpt.ft;
 
 import java.util.Calendar;
 import java.util.Iterator;
 
 import android.text.format.DateFormat;
 
 public class Uppsala extends Ticket {
 
 	public Uppsala() {
 		boolean isUngdom = mCurrentSettings.contains("Uppsala" + ":" + String.valueOf(R.id.ungdom))? true : false;
 		int price = 0;
 		String code = new String();
 		String zone = new String();
 		
 		// Default to stadsbussarna
 		int id = R.id.Ux;
 				
 		if (!mCurrentSettings.isEmpty()) {
 			final Iterator<String> iter = mCurrentSettings.iterator();
 			while (iter.hasNext()) {
 				final String setting = iter.next();
 				if (setting.startsWith("Uppsala" + ":")) {
 					final String[] settings = setting.split(":");
 					id = Integer.valueOf(settings[1]);
 				}
 			}
 		}
 
 		switch (id) {
 		case R.id.Ux:
 			timePiece.add(Calendar.MINUTE, 90);
 			price = isUngdom ? 15 : 25;
 			code = isUngdom ? "UU" : "UV";
 			zone = "Stadsbuss";
 			break;
 		case R.id.ULxT:
 			price = isUngdom ? 12 : 20;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULUT" : "ULVT";
 			zone = "Tätort";
 			break;
 		case R.id.ULx1:
 			price = isUngdom ? 27 : 45;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULU1" : "ULV1";
 			zone = "Zon 1";
 			break;
 		case R.id.ULx2:
 			price = isUngdom ? 27 : 45;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULU2" : "ULV2";
 			zone = "Zon 2";
 			break;
 		case R.id.ULxP:
 			price = isUngdom ? 27 : 45;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULUP" : "ULVP";
 			zone = "PLUS";
 			break;
 		case R.id.UL2SLCx:
 			price = isUngdom ? 35 : 55;
 			timePiece.add(Calendar.MINUTE, 120);
 			code = isUngdom ? "UL2SLCU" : "UL2SLCV";
 			zone = "UL zon 2 + SL zon C Nord";
 			break;
 		case R.id.UL2SLx:
 			price = isUngdom ? 45 : 80;
 			timePiece.add(Calendar.MINUTE, 120);
 			code = isUngdom ? "UL2SLU" : "UL2SLV";
 			zone = "UL zon 2 + SL Län";
 			break;
 		case R.id.ULx2P:
 			price = isUngdom ? 54 : 90;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULU2P" : "ULV2P";
 			zone = "Zon 2 PLUS";
 			break;
 		case R.id.ULx12:
 			price = isUngdom ? 54 : 90;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULU12" : "ULV12";
 			zone = "Län";
 			break;
 		case R.id.ULSLCx:
 			price = isUngdom ? 50 : 90;
 			timePiece.add(Calendar.MINUTE, 120);
 			code = isUngdom ? "ULSLCU" : "ULSLCV";
 			zone = "UL Län + SL zon C Nord";
 			break;
 		case R.id.ULSLx:
 			price = isUngdom ? 65 : 110;
 			timePiece.add(Calendar.MINUTE, 120);
 			code = isUngdom ? "ULSLU" : "ULSLV";
			zone = "UL + SL Län";
 			break;
 		case R.id.ULx12P:
 			price = isUngdom ? 81 : 135;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = isUngdom ? "ULU12P" : "ULV12P";
 			zone = "Län PLUS";
 			break;
 		case R.id.UL12PSLx:
 			price = isUngdom ? 92 : 155;
 			timePiece.add(Calendar.MINUTE, 150);
 			code = isUngdom ? "UL12PSLU" : "UL12PSLV";
			zone = "UL Län PLUS + SL Län";
 			break;
 		case R.id.ULCY:
 			price = 45;
 			timePiece.add(Calendar.MINUTE, 90);
 			code = "ULCY";
 			zone = "Cykel";
 			break;
 		}
 
 		String creature = isUngdom ? "UNGDOM" : "VUXEN";
 		if (id == R.id.ULCY)
 			creature = "Cykel";
 
 		contentInboxValues.put("address", "UL" + String.valueOf(numberTail));
 		contentInboxValues.put("body", "UV " + creature + " Giltig till "
 				+ DateFormat.format("kk:mm yyyy-MM-dd", timePiece) + "\n"
 				+ zone + "\n\n" + String.valueOf(price) + " SEK (6% MOMS)\n"
 				+ seed + "\n\n" + generateRandomAEOXStringBlock(seed));
 
 		contentOutboxValues.put("address", String.valueOf("0704202222"));
 		contentOutboxValues.put("body", code);
 		contentOutboxValues.put("date",
 				new java.util.Date().getTime() - 60 * 1000);
 	}
 }
