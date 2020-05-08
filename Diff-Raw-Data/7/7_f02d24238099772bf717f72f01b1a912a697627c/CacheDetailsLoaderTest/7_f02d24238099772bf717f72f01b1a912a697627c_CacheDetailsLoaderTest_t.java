 
 package com.google.code.geobeagle.cachedetails;
 
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.Details;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.DetailsError;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.DetailsImpl;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.DetailsOpener;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.DetailsReader;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.DetailsReaderError;
 import com.google.code.geobeagle.cachedetails.CacheDetailsLoader.DetailsReaderImpl;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.Activity;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 @PrepareForTest( {
         Activity.class, DetailsOpener.class, DetailsReaderImpl.class, File.class,
         CacheDetailsLoader.class
 })
 @RunWith(PowerMockRunner.class)
 public class CacheDetailsLoaderTest {
 
     private FileDataVersionChecker fileDataVersionChecker;
 
     @Before
     public void setUp() {
         fileDataVersionChecker = PowerMock.createMock(FileDataVersionChecker.class);
     }
     @Test
     public void testDetailsError() {
         Activity activity = PowerMock.createMock(Activity.class);
 
         EasyMock.expect(activity.getString(57, "foo.gpx")).andReturn("error msg");
 
         PowerMock.replayAll();
         assertEquals("error msg", new DetailsError(activity, 57, "foo.gpx").getString());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testDetailsImpl() {
         byte[] buffer = {
                 't', 'e', 's', 't'
         };
         assertEquals("test", new DetailsImpl(buffer).getString());
     }
 
     @Test
     public void testDetailsOpener() throws Exception {
         File file = PowerMock.createMock(File.class);
         FileInputStream fileInputStream = PowerMock.createMock(FileInputStream.class);
         DetailsReaderImpl detailsReaderImpl = PowerMock.createMock(DetailsReaderImpl.class);
         Activity activity = PowerMock.createMock(Activity.class);
         File detailsDir = PowerMock.createMock(File.class);
 
         PowerMock.expectNew(File.class, CacheDetailsLoader.SDCARD_DIR).andReturn(detailsDir);
         EasyMock.expect(detailsDir.isDirectory()).andReturn(true);
         EasyMock.expect(file.getAbsolutePath()).andReturn("/sdcard/foo.gpx");
         PowerMock.expectNew(FileInputStream.class, file).andReturn(fileInputStream);
         EasyMock.expect(file.length()).andReturn(27L);
         PowerMock.expectNew(DetailsReaderImpl.class, EasyMock.eq(activity),
                 EasyMock.eq("/sdcard/foo.gpx"), EasyMock.eq(fileInputStream),
                 EasyMock.isA(byte[].class)).andReturn(detailsReaderImpl);
 
         PowerMock.replayAll();
         assertEquals(detailsReaderImpl, new DetailsOpener(activity, fileDataVersionChecker)
                 .open(file));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testDetailsOpenerFileNotFound() throws Exception {
         File file = PowerMock.createMock(File.class);
         DetailsReaderError detailsReaderError = PowerMock
                 .createMock(DetailsReaderError.class);
         Activity activity = PowerMock.createMock(Activity.class);
         File detailsDir = PowerMock.createMock(File.class);
 
         PowerMock.expectNew(File.class, CacheDetailsLoader.SDCARD_DIR).andReturn(detailsDir);
         EasyMock.expect(detailsDir.isDirectory()).andReturn(true);
         EasyMock.expect(fileDataVersionChecker.needsUpdating()).andReturn(false);
         EasyMock.expect(file.getAbsolutePath()).andReturn("/sdcard/foo.html");
         PowerMock.expectNew(FileInputStream.class, file).andThrow(
                 new FileNotFoundException("/sdcard/foo.html"));
         PowerMock.expectNew(DetailsReaderError.class, activity,
                 R.string.error_opening_details_file, "/sdcard/foo.html").andReturn(
                 detailsReaderError);
 
         PowerMock.replayAll();
         assertEquals(detailsReaderError, new DetailsOpener(activity, fileDataVersionChecker)
                 .open(file));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testDetailsReader() throws Exception {
         FileInputStream fileInputStream = PowerMock.createMock(FileInputStream.class);
         Activity activity = PowerMock.createMock(Activity.class);
         DetailsImpl details = PowerMock.createMock(DetailsImpl.class);
 
         byte[] buffer = new byte[10];
         EasyMock.expect(fileInputStream.read(buffer)).andReturn(10);
         fileInputStream.close();
         PowerMock.expectNew(DetailsImpl.class, buffer).andReturn(details);
 
         PowerMock.replayAll();
         assertEquals(details, new DetailsReaderImpl(activity, "/sdcard/foo.gpx", fileInputStream,
                 buffer).read());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testDetailsReaderFileNotFound() throws Exception {
         Activity activity = PowerMock.createMock(Activity.class);
         DetailsError detailsError = PowerMock.createMock(DetailsError.class);
 
         PowerMock.expectNew(DetailsError.class, activity, R.string.error_opening_details_file,
                 "/sdcard/foo.html").andReturn(detailsError);
 
         PowerMock.replayAll();
         assertEquals(detailsError, new DetailsReaderError(activity,
                 R.string.error_opening_details_file, "/sdcard/foo.html").read());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testDetailsReaderIOError() throws Exception {
         FileInputStream fileInputStream = PowerMock.createMock(FileInputStream.class);
         Activity activity = PowerMock.createMock(Activity.class);
         DetailsError details = PowerMock.createMock(DetailsError.class);
 
         byte[] buffer = new byte[10];
         EasyMock.expect(fileInputStream.read(buffer)).andThrow(new IOException("/sdcard/foo.gpx"));
         PowerMock.expectNew(DetailsError.class, activity, R.string.error_reading_details_file,
                 "/sdcard/foo.gpx").andReturn(details);
 
         PowerMock.replayAll();
         assertEquals(details, new DetailsReaderImpl(activity, "/sdcard/foo.gpx", fileInputStream,
                 buffer).read());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testLoad() throws Exception {
         DetailsOpener detailsOpener = PowerMock.createMock(DetailsOpener.class);
         File file = PowerMock.createMock(File.class);
         DetailsReader detailsReader = PowerMock.createMock(DetailsReader.class);
         Details details = PowerMock.createMock(Details.class);
        FilePathStrategy filePathStrategy = PowerMock.createMock(FilePathStrategy.class);
 
        EasyMock.expect(filePathStrategy.getPath("foo.gpx", "GC123")).andReturn(
                CacheDetailsLoader.DETAILS_DIR + "foo.gpx/GC123.html");
         PowerMock.expectNew(File.class, CacheDetailsLoader.DETAILS_DIR + "foo.gpx/GC123.html")
                 .andReturn(file);
         EasyMock.expect(detailsOpener.open(file)).andReturn(detailsReader);
         EasyMock.expect(detailsReader.read()).andReturn(details);
         EasyMock.expect(details.getString()).andReturn("cache details");
 
         PowerMock.replayAll();
        assertEquals("cache details", new CacheDetailsLoader(detailsOpener, filePathStrategy)
                 .load("foo.gpx", "GC123"));
         PowerMock.verifyAll();
     }
 }
