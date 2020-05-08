 
 package gov.usgs.cida.data.grib;
 
 import com.google.common.io.Closeables;
 import com.google.common.io.Flushables;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import org.apache.commons.io.IOUtils;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
import org.junit.Before;
 import org.junit.Test;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.time.Calendar;
 import ucar.nc2.time.CalendarDate;
 import ucar.nc2.time.CalendarPeriod;
 import ucar.nc2.time.CalendarPeriod.Field;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class RollingNetCDFArchiveTest {
     
     private static String testGribFile = RollingNetCDFArchiveTest.class.getClassLoader().getResource(
             "gov/usgs/cida/data/grib/QPE.20100319.009.160").getFile();
    private File tmpNc = null;
    
    @Before
    public void setUpClass() throws IOException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        tmpNc = new File(tmpdir + File.separator + Long.toString(System.nanoTime()) + ".nc");
        tmpNc.deleteOnExit();
    }
 
     /**
      * Test of addFile method, of class RollingNetCDFArchive.
      * @throws Exception 
      */
 //    @Test
 //    public void testGeotoolsProjection() throws Exception {
 //        RollingNetCDFArchive roll = new RollingNetCDFArchive(new File("/tmp/test.nc"));
 //        roll.setExcludeList(RollingNetCDFArchive.DIM, "time1");
 //        roll.setExcludeList(RollingNetCDFArchive.VAR, "time1", "time_bounds", "time1_bounds", "Total_precipitation_surface_Mixed_intervals_Accumulation");
 //        roll.setExcludeList(RollingNetCDFArchive.XY, "PolarStereographic_Projection", "x", "y");
 //        roll.setGridMapping("Latitude_Longitude");
 //        roll.setUnlimitedDimension("time", "hours since 2000-01-01 00:00:00");
 //        roll.setGridVariables("1-hour_Quantitative_Precip_Estimate_surface_1_Hour_Accumulation");
 //        roll.define(new File("/home/jordan/test/QPE.20100319.009.160"));
 //        roll.addFile(new File("/home/jordan/test/QPE.20100319.009.160"));
 //        CoordinateReferenceSystem crs = roll.getCRS();
 //        GridDataset dataset = roll.getDataset();
 //        double[] xCoords = roll.getXCoords();
 //        double[] yCoords = roll.getYCoords();
 //        assertThat(xCoords.length, is(equalTo(250)));
 //        assertThat(yCoords.length, is(equalTo(260)));
 //        double[] latLons = roll.transformToLatLon(xCoords, yCoords);
 //        BufferedWriter lonWriter = null;
 //        BufferedWriter latWriter = null;
 //        try {
 //            lonWriter = new BufferedWriter(new FileWriter("/tmp/lons"));
 //            latWriter = new BufferedWriter(new FileWriter("/tmp/lats"));
 //            for (int i=0; i<latLons.length; i+=2) {
 //                lonWriter.append(" " + latLons[i]);
 //                latWriter.append(" " + latLons[i+1]);
 //            }
 //            assertThat(latLons.length, is(equalTo(2 * 250 * 260)));
 //        }
 //        finally {
 //            Flushables.flushQuietly(roll);
 //            Closeables.closeQuietly(roll);
 //            IOUtils.closeQuietly(lonWriter);
 //            IOUtils.closeQuietly(latWriter);
 //        }
 //    }
     
    private RollingNetCDFArchive define() throws Exception {
        RollingNetCDFArchive roll = new RollingNetCDFArchive(tmpNc);
         roll.setExcludeList(RollingNetCDFArchive.DIM, "time1");
         roll.setExcludeList(RollingNetCDFArchive.VAR, "time1", "time_bounds", "time1_bounds", "Total_precipitation_surface_Mixed_intervals_Accumulation");
         roll.setExcludeList(RollingNetCDFArchive.XY, "PolarStereographic_Projection", "x", "y");
         roll.setGridMapping("Latitude_Longitude");
         roll.setUnlimitedDimension("time", "hours since 2000-01-01 00:00:00");
         roll.setGridVariables("1-hour_Quantitative_Precip_Estimate_surface_1_Hour_Accumulation");
         roll.define(new File(testGribFile));
        return roll;
    }
    
    @Test
    public void testDefine() throws Exception {
        RollingNetCDFArchive roll = define();
         double[] xCoords = roll.getXCoords();
         double[] yCoords = roll.getYCoords();
         assertThat(xCoords.length, is(equalTo(250)));
         assertThat(yCoords.length, is(equalTo(260)));
        double[][] latLons = roll.transform();
         BufferedWriter lonWriter = null;
         BufferedWriter latWriter = null;
         try {
             lonWriter = new BufferedWriter(new FileWriter("/tmp/lonsnc"));
             latWriter = new BufferedWriter(new FileWriter("/tmp/latsnc"));
             for (int i=0; i<latLons[0].length; i++) {
                 latWriter.append(" " + latLons[0][i]);
                 lonWriter.append(" " + latLons[1][i]);
             }
             assertThat(latLons[0].length, is(equalTo(250 * 260)));
         }
         finally {
             Flushables.flushQuietly(roll);
             Closeables.closeQuietly(roll);
             IOUtils.closeQuietly(lonWriter);
             IOUtils.closeQuietly(latWriter);
         }
     }
     
     @Test
     public void testAddFile() throws IOException, InvalidRangeException, Exception {
        RollingNetCDFArchive roll = define();
         roll.setGridVariables("1-hour_Quantitative_Precip_Estimate_surface_1_Hour_Accumulation");
         roll.setUnlimitedDimension("time", "hours since 2000-01-01T00:00:00Z");
         roll.addFile(new File(testGribFile));
         Flushables.flushQuietly(roll);
         Closeables.closeQuietly(roll);
     }
     
    /* Finish currently doesn't do anything, leaving dimension unlimited */
     @Test
     public void testFinish() throws IOException {
         RollingNetCDFArchive roll = new RollingNetCDFArchive(new File("/tmp/test.nc"));
         roll.finish();
         Closeables.closeQuietly(roll);
     }
 
     @Test
     public void testCalendarDateFromUdunits() {
         String units = "0 hours since 2000-01-01T00:00:00Z";
         CalendarDate originDate = CalendarDate.parseUdunits(null, units);
         assertThat(originDate.compareTo(CalendarDate.of(Calendar.none, 2000, 1, 1, 0, 0, 0)), is(equalTo(0)));
     }
     
     @Test
     public void testCalendarPeriodFromUdunits() {
         String units = "hours";
         Field fromUnitString = CalendarPeriod.fromUnitString(units);
         assertThat(fromUnitString, is(equalTo(CalendarPeriod.Field.Hour)));
     }
 }
