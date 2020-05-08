 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Joëll Portier
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.dare2date.domein.lastfm;
 
 import com.dare2date.externeservice.lastfm.LastfmAPI;
 import com.dare2date.utility.HttpClient;
 import junit.framework.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.xml.transform.sax.SAXSource;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 @RunWith(BlockJUnit4ClassRunner.class)
 public class LastfmDataTest {
 
 
     @Test
     public void testLastfmDataCompare() {
         LastfmData data1 = new LastfmData();
         data1.addEvent(new LastfmEvent(1234,"GelijkFesti"));
         data1.addEvent(new LastfmEvent(561631,"SVDVDABC"));
         data1.addEvent(new LastfmEvent(48651465,"SKN LSfS"));
         data1.addEvent(new LastfmEvent(51206105,"Matchy Match"));
         data1.addEvent(new LastfmEvent(126521,"OLKMOPKM"));
 
         LastfmData data2 = new LastfmData();
         data2.addEvent(new LastfmEvent(156198510,"SSFKN SKFLMS"));
         data2.addEvent(new LastfmEvent(51206105,"Matchy Match"));
         data2.addEvent(new LastfmEvent(51651051,"SKMNKSMC"));
         data2.addEvent(new LastfmEvent(84584879,"SVDVDABC"));
         data2.addEvent(new LastfmEvent(415154,"SVDVSMC MSCDABC"));
         data2.addEvent(new LastfmEvent(150515,"SCOSKPO"));
         data2.addEvent(new LastfmEvent(1234,"GelijkFesti"));
 
        Assert.assertEquals(2,data1.getMatchingEvents(data2).size());
        Assert.assertEquals(1234,data1.getMatchingEvents(data2).get(0).getId());
        Assert.assertEquals(51206105,data1.getMatchingEvents(data2).get(1).getId());
     }
 }
