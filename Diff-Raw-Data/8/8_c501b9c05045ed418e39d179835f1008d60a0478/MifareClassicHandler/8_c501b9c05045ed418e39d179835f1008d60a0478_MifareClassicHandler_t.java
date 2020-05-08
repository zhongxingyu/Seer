 package pl.org.netrix.nfc_reader.nfc_handlers;
 
 import java.io.IOException;
 
 import pl.org.netrix.nfc_reader.Common;
 import pl.org.netrix.nfc_reader.Logger;
 import pl.org.netrix.nfc_reader.NfcStatus;
 import android.nfc.Tag;
 import android.nfc.tech.MifareClassic;
 
 public class MifareClassicHandler extends Handler {
 
 	public MifareClassicHandler(Logger logger, NfcStatus status) {
 		super(logger, status);
 	}
 
 	public void handleTag(Tag tag) {
 		
 		MifareClassic mfc = MifareClassic.get(tag);
 		
 		try {
 			mfc.connect();
 			
 			mStatus.setStatus("Connected");
 			
 			mLogger.pushStatus("");
 			mLogger.pushStatus("== Info == ");
 			mLogger.pushStatus("Size: " + mfc.getSize());
 			mLogger.pushStatus("Timeout: " + mfc.getTimeout());
 			mLogger.pushStatus("Type: " + mfc.getType());
 			mLogger.pushStatus("BlockCount: " + mfc.getBlockCount());
 			mLogger.pushStatus("MaxTransceiveLength: " + mfc.getMaxTransceiveLength());
 			mLogger.pushStatus("SectorCount: " + mfc.getSectorCount());
 			
 			for(int i = 0; i < mfc.getSectorCount(); ++i) {
 				mLogger.pushStatus("BlockCount in sector " + i + ": " + mfc.getBlockCountInSector(i));
 			}
 			
 			mStatus.setStatus("Reading sectors...");
 			
 			
 			for(int i = 0; i < mfc.getSectorCount(); ++i) {
 				
				if(mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
 					mLogger.pushStatus("Authorization granted to sector " + i + " with MAD key");
				} else if(mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
 					mLogger.pushStatus("Authorization granted to sector " + i + " with DEFAULT key");
				} else if(mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_NFC_FORUM)) {
 					mLogger.pushStatus("Authorization granted to sector " + i + " with NFC_FORUM key");
 				} else {
 					mLogger.pushStatus("Authorization denied to sector " + i);
 					continue;
 				}
 				
 				for(int k = 0; k < mfc.getBlockCountInSector(i); ++k)
 				{
 					String blockData = Common.getHexString(mfc.readBlock(k));
 					mLogger.pushStatus("Block " + k + " data: " + blockData);
 				}
 			}
 			
 			mLogger.pushStatus("");
 			
 		} catch (IOException e) {
 			mStatus.setStatus("Dupa");
 		}
 
 	}
 
 }
