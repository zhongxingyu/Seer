 package nl.digitalica.skydivekompasroos.test;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.List;
 
 import nl.digitalica.skydivekompasroos.Canopy;
 import nl.digitalica.skydivekompasroos.Manufacturer;
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 public class CanopyDbExportToCsv extends AndroidTestCase {
 
 	private static final String SEPARATOR = ",";
 	private static final String CSVFILENAME = "KompasroosAppParachutes.csv";
 
 	private static final String TAG = "CSV";
 
 	/**
 	 * We manually mark this as a test so it can be executed
 	 * 
 	 * @throws IOException
 	 */
 	public void testExportCanopies() throws IOException {
 		List<Canopy> canopies = Canopy.getAllCanopiesInList(getContext());
 		HashMap<String, Manufacturer> manufacturers = Manufacturer
 				.getManufacturerHash(getContext());
 
 		Log.v(TAG,
				",name,manufacturer,country,cells,minsize,maxsize,firstYear,lastYear,isCommon,remarks,url");
 
 		for (Canopy c : canopies) {
 			Manufacturer m = manufacturers.get(c.manufacturer);
 			StringBuilder line = new StringBuilder();
			line.append(SEPARATOR); // convenient, to remove other logcat cols.
 			line.append(c.name + SEPARATOR);
 			line.append(c.manufacturer + SEPARATOR);
 			if (m != null)
 				line.append(m.countryFullName() + SEPARATOR);
 			else
 				line.append(SEPARATOR);
 			line.append((c.cells == null ? "" : c.cells) + SEPARATOR);
 			line.append((c.minSize == null ? "" : c.minSize) + SEPARATOR);
 			line.append((c.maxSize == null ? "" : c.maxSize) + SEPARATOR);
 			line.append((c.firstYearOfProduction == null ? ""
 					: c.firstYearOfProduction) + SEPARATOR);
 			line.append((c.lastYearOfProduction == null ? ""
 					: c.lastYearOfProduction) + SEPARATOR);
 			line.append((c.commontype ? "yes" : "no") + SEPARATOR);
 			line.append((c.remarks == null ? "" : c.remarks) + SEPARATOR);
 			line.append((c.url == null ? "" : c.url) + SEPARATOR);
 			Log.v(TAG, line.toString());
 		}
 
 	}
 }
