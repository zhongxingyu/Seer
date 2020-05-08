 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.spantus.extr.wordspot.service.impl.test;
 
 import com.google.common.collect.Ordering;
 import com.google.common.primitives.Longs;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.spantus.core.beans.RecognitionResult;
 import org.spantus.core.beans.SignalSegment;
 import org.spantus.core.extractor.dao.MarkerDao;
 import org.spantus.core.junit.SlowTests;
 import org.spantus.core.marker.Marker;
 import org.spantus.core.marker.MarkerSetHolder;
 import org.spantus.core.marker.service.IMarkerService;
 import org.spantus.core.marker.service.MarkerServiceFactory;
 import org.spantus.core.wav.AudioManagerFactory;
 import org.spantus.extr.wordspot.service.impl.WordSpottingListenerLogImpl;
 import org.spantus.extr.wordspot.service.impl.test.util.ExtNameFilter;
 import org.spantus.extr.wordspot.util.dao.WordSpotResult;
 import org.spantus.extr.wordspot.util.dao.WspotJdbcDao;
 import org.spantus.utils.FileUtils;
 import org.spantus.work.services.WorkServiceFactory;
 
 /**
  *
  * @author as
  */
 public class WordSpottingServiceImplExp extends WordSpottingServiceImplTest {
 
     private static final Logger log = Logger.getLogger(WordSpottingServiceImplExp.class);
 
     private WspotJdbcDao wspotDao;
     
 
     
     private static final Ordering<Entry<RecognitionResult, SignalSegment>> order =
             new Ordering<Entry<RecognitionResult, SignalSegment>>() {
                 @Override
                 public int compare(Entry<RecognitionResult, SignalSegment> left,
                         Entry<RecognitionResult, SignalSegment> right) {
                     return Longs.compare(left.getValue().getMarker().getStart(),
                             right.getValue().getMarker().getStart());
                 }
             };
 
     @Before
     @Override
     public void setUp() throws Exception {
         super.setUp();
        
          wspotDao = new WspotJdbcDao();
     }
 
     @Override
    protected void setUpPath() throws Exception {
        super.setUpPath();
         String path =
 //                "/tmp/test" //                
 //                "/home/as/tmp/garsynas.lietuvos-syn-wpitch/TEST/"
                 "/home/as/tmp/garsynas.lietuvos-syn-wopitch//TEST"
                 ;
         String fileName =
                                 "RZj0815_13_23-30_1.wav"
                 //                "RAj031004_13_16a-30_1.wav"
                 //                "RAj031013_18_24a-30_1.wav"
 //                "RCz041110_18_29-30_1.wav"
 //                "RBz041003_18_6-30_1.wav"
 //                "RBs041003_13_36a-30_1.wav"
 //                "RBp031123_13_29-30_1.wav"
 //                "lietuvos_mbr_test-30_1.wav"
                 ;
         setWavFile(new File(path, fileName));
         setRepositoryPathRoot(new File("/home/as/tmp/garsynas.lietuvos-syn-wopitch/"));
         setAcceptableSyllables(new String[]{"liet", "tuvos"});
         setSearchWord("lietuvos");
     }
     
      @Ignore
     @Test
     @Category(SlowTests.class)
     @Override
     public void test_wordSpotting() throws MalformedURLException {
         //given
         
         WordSpotResult result = doWordspot(getWavFile());
         String resultsStr = extractResultStr(result.getSegments());
 
         log.error("Marker =>" + result.getOriginalMarker());
         log.error(getWavFile() + "=>" + order.sortedCopy(result.getSegments().entrySet()));
 
         //then
         //Assert.assertTrue("read time " + length + ">"+(ended-started), length > ended-started);
         Assert.assertEquals("Recognition", "lietuvos", resultsStr);
         SignalSegment firstSegment = result.getSegments().values().iterator().next();
         Assert.assertEquals("Recognition start", result.getOriginalMarker().getStart(), firstSegment.getMarker().getStart(), 320D);
         Assert.assertEquals("Recognition length", result.getOriginalMarker().getLength(), firstSegment.getMarker().getLength(), 150);
 
     }
 
 
 
     @Test
     @Category(SlowTests.class)
     public void bulkTest() throws MalformedURLException {
         wspotDao.setRecreate(true);
         wspotDao.init();
         
         File[] files = getWavFile().getParentFile().listFiles(new ExtNameFilter("wav"));
         List<AssertionError> list = new ArrayList<>();
         int foundSize = 0;
         for (File file : files) {
 //            if(!file.getName().contains(
 //                    "RZd0706_18_06-30_1.wav"
 //                    )){
 //                continue;
 //            }
              log.debug("start: " + file);
                 WordSpotResult result = doWordspot(file);
                 wspotDao.save(result);
                 foundSize += result.getSegments().size();
 //                String resultsStr = extractResultStr(result.getSegments());
                 log.debug("done: " + file);
                 log.error("Marker =>" + result.getOriginalMarker());
                 log.error(getWavFile() + "=>" + order.sortedCopy(result.getSegments().entrySet()));                
         }
 //        log.error("files =>" + files.length);
         log.error("foundSize =>" + foundSize);
 //        Assert.assertEquals(0, list.size());
         wspotDao.destroy();
         Assert.assertTrue("One element at least", foundSize>0);
 
     }
 
        public WordSpotResult doWordspot(File aWavFile) throws MalformedURLException {
         URL aWavUrl = aWavFile.toURI().toURL();
         
         WordSpotResult result = new WordSpotResult();
         WordSpottingListenerLogImpl listener = new WordSpottingListenerLogImpl(getSearchWord(),
                 getAcceptableSyllables(),
                 getRepositoryPathWord().getAbsolutePath());
         listener.setServiceConfig(serviceConfig);
         Long length = AudioManagerFactory.createAudioManager().findLengthInMils(
                 aWavUrl);
         //various experiments uses various lietuvos trasnsciption
         Marker keywordMarker = findKeyword(aWavFile);
         result.setAudioLength(length);
         result.setOriginalMarker(keywordMarker);
         result.setFileName(aWavFile.getName());
         
         //when 
         result.setExperimentStarted(System.currentTimeMillis());
         wordSpottingServiceImpl.wordSpotting(aWavUrl, listener);
         result.setExperimentEnded(System.currentTimeMillis());
         Map<RecognitionResult, SignalSegment> segments = listener.getWordSegments();
         result.setSegments(segments);
         return result;
 
     }
     
 
 
 
     
     private Marker findKeyword(File aWavFile) {
         MarkerSetHolder markers = findMarkerSetHolderByWav(aWavFile);
         Marker marker = getMarkerService().findByLabel("-l'-ie-t-|-u-v-oo-s", markers);
         if(marker == null){
             Marker lietMarker = getMarkerService().findByLabel("-l-ie-t", markers);
             Marker uvosMarker = getMarkerService().findByLabel("-u-v-o:-s", markers);
             if(uvosMarker == null){
                 uvosMarker = getMarkerService().findByLabel("-u-v-oo-s", markers);
             }
             marker = new Marker();
             marker.setStart(lietMarker.getStart());
             marker.setEnd(uvosMarker.getEnd());
             marker.setLabel(lietMarker.getLabel()+uvosMarker.getLabel());
         }
         return marker;
     }
 }
