 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.service;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.collect.ImmutableList;
 
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.idd.generated.Klanten;
 import nl.surfnet.bod.repo.InstituteRepo;
 import nl.surfnet.bod.support.InstituteFactory;
 import nl.surfnet.bod.support.KlantenFactory;
 
 @RunWith(MockitoJUnitRunner.class)
 public class InstituteIddServiceTest {
 
   @InjectMocks
   private InstituteIddService subject;
 
   @Mock
   private IddClient iddClientMock;
 
   @Mock
   private InstituteRepo instituteRepoMock;
 
   @Mock
   private LogEventService logEventService;
   
   @Mock
   private SnmpAgentService snmpAgentService;
 
   @Test
   public void whenAllInstituesEqualDontUpdate() {
     List<Institute> institutes = ImmutableList.of(
         new InstituteFactory().setId(1L).setName("SURFnet").setShortName("SURF").create(), new InstituteFactory()
             .setId(2L).setName("Zilverline").setShortName("Z").create());
     List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
         .setKlantafkorting("SURF").create(), new KlantenFactory().setKlantid(2).setKlantnaam("Zilverline")
         .setKlantafkorting("Z").create());
 
     when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
     when(iddClientMock.getKlanten()).thenReturn(klanten);
 
     subject.refreshInstitutes();
 
     verify(instituteRepoMock, times(2)).save(eq(Collections.<Institute> emptyList()));
   }
 
   @Test
   public void whenAKlantHasChangedUpdateIt() {
     List<Institute> institutes = ImmutableList.of(
         new InstituteFactory().setId(1L).setName("SURFnet").setShortName("SURF").create(), new InstituteFactory()
             .setId(2L).setName("Zilverline").setShortName("Z").create());
     List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
         .setKlantafkorting("SURF").create(), new KlantenFactory().setKlantid(2).setKlantnaam("Zilverline BV")
         .setKlantafkorting("Z").create());
 
     when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
     when(iddClientMock.getKlanten()).thenReturn(klanten);
 
     subject.refreshInstitutes();
 
     verify(instituteRepoMock).save(
         argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z").create())));
     verify(instituteRepoMock).save(
         argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline BV").setShortName("Z").create())));
 
     verify(instituteRepoMock).findByAlignedWithIDD(true);
     verifyNoMoreInteractions(instituteRepoMock);
   }
 
   @Test
   public void whenAKlantIsRemovedFromIddItShouldBeUnAlligned() {
     List<Institute> institutes = ImmutableList.of(
         new InstituteFactory().setId(1L).setName("SURFnet").setShortName("SURF").setAlignedWithIDD(true).create(),
         new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z").setAlignedWithIDD(true).create());
     List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
         .setKlantafkorting("SURF").create());
 
     when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
     when(iddClientMock.getKlanten()).thenReturn(klanten);
 
     subject.refreshInstitutes();
 
     verify(instituteRepoMock).save(
         argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z").create())));
     verify(instituteRepoMock).save(eq(Collections.<Institute> emptyList()));
 
     verify(instituteRepoMock).findByAlignedWithIDD(true);
     verifyNoMoreInteractions(instituteRepoMock);
 
     assertThat(institutes.get(1).isAlignedWithIDD(), is(false));
   }
 
   @Test
   public void whenAnUnalignedInstituteReappearsAgainInIDDItShouldBeAligned() {
     List<Institute> institutes = ImmutableList.of(new InstituteFactory().setId(1L).setName("SURFnet")
         .setShortName("SURF").setAlignedWithIDD(true).create());
 
     List<Klanten> klanten = ImmutableList.of(new KlantenFactory().setKlantid(1).setKlantnaam("SURFnet")
         .setKlantafkorting("SURF").create(), new KlantenFactory().setKlantid(2).setKlantnaam("Zilverline")
         .setKlantafkorting("Z").create());
 
     when(instituteRepoMock.findByAlignedWithIDD(true)).thenReturn(institutes);
     when(iddClientMock.getKlanten()).thenReturn(klanten);
 
     subject.refreshInstitutes();
 
     verify(instituteRepoMock).save(eq(Collections.<Institute> emptyList()));
 
     //TODO setAlignedWithIDD Attribute is not matched
     verify(instituteRepoMock).save(
         argThat(contains(new InstituteFactory().setId(2L).setName("Zilverline").setShortName("Z")
             .setAlignedWithIDD(true).create())));
 
     verify(instituteRepoMock).findByAlignedWithIDD(true);
     verifyNoMoreInteractions(instituteRepoMock);
 
     assertThat(institutes.get(0).isAlignedWithIDD(), is(true));
   }
 
 }
