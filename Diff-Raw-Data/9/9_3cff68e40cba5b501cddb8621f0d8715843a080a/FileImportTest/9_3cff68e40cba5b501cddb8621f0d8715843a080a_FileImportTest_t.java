 package ch.opentrainingcenter.importer.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import ch.opentrainingcenter.core.db.IDatabaseAccess;
 import ch.opentrainingcenter.core.importer.ConvertContainer;
 import ch.opentrainingcenter.core.importer.IConvert2Tcx;
 import ch.opentrainingcenter.core.importer.IFileCopy;
 import ch.opentrainingcenter.model.importer.IGpsFileModel;
 import ch.opentrainingcenter.model.importer.IGpsFileModelWrapper;
import ch.opentrainingcenter.model.strecke.StreckeModel;
 import ch.opentrainingcenter.tcx.ActivityLapT;
 import ch.opentrainingcenter.tcx.ActivityT;
 import ch.opentrainingcenter.tcx.HeartRateInBeatsPerMinuteT;
 import ch.opentrainingcenter.tcx.IntensityT;
 import ch.opentrainingcenter.transfer.IAthlete;
 import ch.opentrainingcenter.transfer.ITraining;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 @SuppressWarnings("nls")
 public class FileImportTest {
     private FileImport fileImport;
     private ConvertContainer cc;
     private IAthlete athlete;
     private IDatabaseAccess dbAccess;
     private File garminFile;
     private String dir;
 
     private static DatatypeFactory df;
 
     private final Double distanz = Double.valueOf(1042);
     private final Double dauer = Double.valueOf(42);
     private final int avgHeart = 145;
     private final int maxHeart = 189;
     private final double maxSpeed = 3.45d;
     private IFileCopy fileCopy;
 
     @Before
     public void before() throws IOException, DatatypeConfigurationException {
 
         cc = mock(ConvertContainer.class);
         athlete = mock(IAthlete.class);
         dbAccess = mock(IDatabaseAccess.class);
         fileCopy = mock(IFileCopy.class);
         garminFile = File.createTempFile("garmin", ".gmn");
         dir = getDirectory(garminFile.getPath());
 
         df = DatatypeFactory.newInstance();
     }
 
     private String getDirectory(final String path) {
         return path.substring(0, path.lastIndexOf(File.separator));
     }
 
     @After
     public void after() {
         garminFile.deleteOnExit();
     }
 
     @Test
     public void testConstructor() {
         fileImport = new FileImport(cc, athlete, dbAccess, dir, fileCopy);
         assertNotNull(fileImport);
     }
 
     @Test
     public void testImport() throws Exception {
         fileImport = new FileImport(cc, athlete, dbAccess, dir, fileCopy);
         final IGpsFileModelWrapper modelWrapper = mock(IGpsFileModelWrapper.class);
         final List<IGpsFileModel> values = new ArrayList<IGpsFileModel>();
         final IGpsFileModel fileModel = mock(IGpsFileModel.class);
         when(fileModel.getFileName()).thenReturn(garminFile.getName());
        final StreckeModel route = Mockito.mock(StreckeModel.class);
        when(route.getId()).thenReturn(42);
        when(fileModel.getRoute()).thenReturn(route);
         values.add(fileModel);
         when(modelWrapper.getGpsFileModels()).thenReturn(values);
 
         final IConvert2Tcx converter = mock(IConvert2Tcx.class);
         final List<ActivityT> vals = new ArrayList<ActivityT>();
         final ActivityT activity = mock(ActivityT.class);
         vals.add(activity);
 
         final Date date = new Date();
         when(activity.getId()).thenReturn(convert(date));
         final List<ActivityLapT> laps = new ArrayList<ActivityLapT>();
         final ActivityLapT lap = createActivityLap(maxSpeed, true, (short) maxHeart);
         laps.add(lap);
         when(activity.getLap()).thenReturn(laps);
 
         when(converter.convertActivity(garminFile)).thenReturn(vals);
         when(cc.getMatchingConverter(garminFile)).thenReturn(converter);
 
         final IProgressMonitor monitor = mock(IProgressMonitor.class);
 
         when(dbAccess.importRecord(anyInt(), anyString(), (Date) any(), (ITraining) any(), Mockito.anyInt(), anyInt())).thenReturn(42);
 
         final List<ActivityT> importFile = fileImport.importFile(dir, modelWrapper, monitor);
 
         assertEquals("Ein Record wurde importiert", 1, importFile.size());
     }
 
     @Test
     public void testNichtImportiert() throws Exception {
         fileImport = new FileImport(cc, athlete, dbAccess, dir, fileCopy);
         final IGpsFileModelWrapper modelWrapper = mock(IGpsFileModelWrapper.class);
         final List<IGpsFileModel> values = new ArrayList<IGpsFileModel>();
         final IGpsFileModel fileModel = mock(IGpsFileModel.class);
         when(fileModel.getFileName()).thenReturn(garminFile.getName());
        final StreckeModel route = Mockito.mock(StreckeModel.class);
        when(route.getId()).thenReturn(42);
        when(fileModel.getRoute()).thenReturn(route);
         values.add(fileModel);
         when(modelWrapper.getGpsFileModels()).thenReturn(values);
 
         final IConvert2Tcx converter = mock(IConvert2Tcx.class);
         final List<ActivityT> vals = new ArrayList<ActivityT>();
         final ActivityT activity = mock(ActivityT.class);
         vals.add(activity);
 
         final Date date = new Date();
         when(activity.getId()).thenReturn(convert(date));
         final List<ActivityLapT> laps = new ArrayList<ActivityLapT>();
         final ActivityLapT lap = createActivityLap(maxSpeed, true, (short) maxHeart);
         laps.add(lap);
         when(activity.getLap()).thenReturn(laps);
 
         when(converter.convertActivity(garminFile)).thenReturn(vals);
         when(cc.getMatchingConverter(garminFile)).thenReturn(converter);
 
         final IProgressMonitor monitor = mock(IProgressMonitor.class);
 
         when(dbAccess.importRecord(anyInt(), anyString(), (Date) any(), (ITraining) any(), Mockito.anyInt(), anyInt())).thenReturn(-1);
 
         final List<ActivityT> importFile = fileImport.importFile(dir, modelWrapper, monitor);
 
         assertEquals("Kein Record wurde importiert", 0, importFile.size());
     }
 
     private ActivityLapT createActivityLap(final Double speed, final boolean withCardio, final short maxHeartRate) {
         final ActivityLapT mock = mock(ActivityLapT.class);
         when(mock.getIntensity()).thenReturn(IntensityT.ACTIVE);
         when(mock.getDistanceMeters()).thenReturn(distanz.doubleValue());
         when(mock.getMaximumSpeed()).thenReturn(speed);
         when(mock.getTotalTimeSeconds()).thenReturn(dauer);
         final HeartRateInBeatsPerMinuteT heartAvg = mock(HeartRateInBeatsPerMinuteT.class);
         when(heartAvg.getValue()).thenReturn((short) avgHeart);
         when(mock.getAverageHeartRateBpm()).thenReturn(heartAvg);
 
         final HeartRateInBeatsPerMinuteT heartMax = mock(HeartRateInBeatsPerMinuteT.class);
         when(heartMax.getValue()).thenReturn(maxHeartRate);
         if (withCardio) {
             when(mock.getMaximumHeartRateBpm()).thenReturn(heartMax);
         } else {
             when(mock.getMaximumHeartRateBpm()).thenReturn(null);
         }
 
         return mock;
     }
 
     private static XMLGregorianCalendar convert(final Date date) {
         if (date == null) {
             return null;
         } else {
             final GregorianCalendar gc = new GregorianCalendar();
             gc.setTimeInMillis(date.getTime());
             return df.newXMLGregorianCalendar(gc);
         }
     }
 }
