 package org.esupportail.opi.web.utils.fj;
 
 import fj.F;
 import fj.data.Stream;
 import org.esupportail.commons.services.i18n.I18nService;
 import org.esupportail.opi.domain.DomainApoService;
 import org.esupportail.opi.domain.beans.parameters.Transfert;
 import org.esupportail.opi.domain.beans.parameters.TypeTraitement;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.IndVoeuPojo;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Modifier;
 import java.util.*;
 
 import static fj.data.Stream.iterableStream;
 import static org.esupportail.opi.web.utils.fj.Conversions.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: p-opidev
  * Date: 03/07/13
  * Time: 18:27
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ConversionsTest {
 
     @Mock
     private DomainApoService mockApoServ;
 
     @Mock
     private I18nService mockI18n;
 
     private Transfert transfert;
     private Transfert nonTransfert;
 
 
     @Test
     public void testConversionesConstructorIsPrivate() throws Exception {
         //Given Predicates private constructor
         //When
         Constructor constructor = Conversions.class.getDeclaredConstructor();
         //Then
         assertTrue(Modifier.isPrivate(constructor.getModifiers()));
     }
 
 
     @Test
     public void testkeepOnlyAvisWithValidationEqualsFalse() throws Exception {
         //Given
         Set<IndVoeuPojo> setWithInvalidVoeu = buildSetWithInvalidVoeu();
         assertEquals(2, setWithInvalidVoeu.size());
         List<IndividuPojo> mockListIndi = new ArrayList<>();
         IndividuPojo ipj1 = new IndividuPojo();
         //that we should filter this one
         ipj1.setIndVoeuxPojo(setWithInvalidVoeu);
         mockListIndi.add(ipj1);
         Stream<IndividuPojo> stream = iterableStream(mockListIndi);
         assertEquals(1, stream.length());
         assertEquals(2, stream.index(0).getIndVoeuxPojo().size());
         assertTrue(retrieveVoeuInStreamWithValidatAvisEquals(stream, true));
         assertTrue(retrieveVoeuInStreamWithValidatAvisEquals(stream, false));
         //When
         Stream<IndividuPojo> result = stream.map(keepOnlyVoeuWithValidatedAvisEquals(false));
         //Then
         assertEquals(stream.length(), result.length());
         assertEquals(1, result.index(0).getIndVoeuxPojo().size());
         assertFalse(retrieveVoeuInStreamWithValidatAvisEquals(stream, true));
         assertTrue(retrieveVoeuInStreamWithValidatAvisEquals(stream, false));
     }
 
     @Test
     public void testkeepOnlyAvisWithValidationEqualsTrue() throws Exception {
         //Given
         Set<IndVoeuPojo> setWithInvalidVoeu = buildSetWithInvalidVoeu();
         assertEquals(2, setWithInvalidVoeu.size());
         List<IndividuPojo> mockListIndi = new ArrayList<>();
         IndividuPojo ipj1 = new IndividuPojo();
         //that we should filter this one
         ipj1.setIndVoeuxPojo(setWithInvalidVoeu);
         mockListIndi.add(ipj1);
         Stream<IndividuPojo> stream = iterableStream(mockListIndi);
         assertEquals(1, stream.length());
         assertEquals(2, stream.index(0).getIndVoeuxPojo().size());
         assertTrue(retrieveVoeuInStreamWithValidatAvisEquals(stream, true));
         assertTrue(retrieveVoeuInStreamWithValidatAvisEquals(stream, false));
         //When
         Stream<IndividuPojo> result = stream.map(keepOnlyVoeuWithValidatedAvisEquals(true));
         //Then
         assertEquals(stream.length(), result.length());
         assertEquals(1, result.index(0).getIndVoeuxPojo().size());
         assertTrue(retrieveVoeuInStreamWithValidatAvisEquals(stream, true));
         assertFalse(retrieveVoeuInStreamWithValidatAvisEquals(stream, false));
     }
 
     @Test
     public void testRemoveVoeuWithTreatmentEquals() throws Exception {
         //Given
         Stream<IndividuPojo> stream = buildIndPojoStreamWithElementToBeFiltered();
         assertEquals(1, stream.length());
         assertEquals(2, stream.index(0).getIndVoeuxPojo().size());
         assertTrue("Should find some transfert traitement", retrieveSomeTransfert(stream, transfert));
         assertTrue("Should find some nontransfert traitement", retrieveSomeTransfert(stream, nonTransfert));
         //When
         Stream<IndividuPojo> result = stream.map(removeVoeuWithTreatmentEquals(transfert));
         //Then
         assertEquals(stream.length(), result.length());
         assertEquals(1, result.index(0).getIndVoeuxPojo().size());
         assertTrue("Should nontransfert left in stream", new ArrayList<IndVoeuPojo>(result.index(0).getIndVoeuxPojo()).get(0)
                 .getTypeTraitement().equals(nonTransfert));
     }
 
     @Test
     public void testSaboteurRemoveVoeuWithTreatmentEquals() throws Exception {
         //Given
         Stream<IndividuPojo> stream = buildIndPojoStreamWithElementToBeFiltered();
         assertEquals(1, stream.length());
         assertTrue("Should find some transfert traitement", retrieveSomeTransfert(stream, transfert));
         assertTrue("Should find some nontransfert traitement", retrieveSomeTransfert(stream, nonTransfert));
         //When
         Stream<IndividuPojo> result = stream.map(removeVoeuWithTreatmentEquals(nonTransfert));
         //Then
         assertEquals(stream.length(), result.length());
         assertEquals(1, result.index(0).getIndVoeuxPojo().size());
         assertTrue("Should transfert left in stream", new ArrayList<IndVoeuPojo>(result.index(0).getIndVoeuxPojo())
                 .get(0).getTypeTraitement().equals(transfert));
     }
 
     @Test
     public void testDecodeRegimeInscription() throws Exception {
         //Given
         Stream<RegimeInscription> stream = buildRegimeInscriptionStream();
         //When
         Stream<Integer> result = stream.map(decodeRegimeInscription());
         //Then
         assertEquals(new Integer(1), result.index(0));
         assertEquals(new Integer(2), result.index(1));
     }
 
     @Test
     public void testInitCursusScol() throws Exception {
         //Given
         List<IndividuPojo> mockListIndi = new ArrayList<>();
         IndividuPojo ipj1 = mock(IndividuPojo.class);
         IndividuPojo ipj2 = mock(IndividuPojo.class);
         IndividuPojo ipj3 = mock(IndividuPojo.class);
         mockListIndi.add(ipj1);
         mockListIndi.add(ipj2);
         mockListIndi.add(ipj3);
         Stream<IndividuPojo> stream = iterableStream(mockListIndi);
         assertEquals("Built 3 items", 3, stream.length());
         verify(ipj1, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj2, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj3, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         //When
         Stream<IndividuPojo> result = stream.map(initCursusScol(true, mockApoServ, mockI18n));
         //Then should init
         assertEquals("Should not add/remove item from list", stream.length(), result.length());
         verify(ipj1, times(1)).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj2, times(1)).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj3, times(1)).initIndCursusScolPojo(mockApoServ, mockI18n);
     }
 
     @Test
     public void testNoInitCursusScol() throws Exception {
         //Given
         List<IndividuPojo> mockListIndi = new ArrayList<>();
         IndividuPojo ipj1 = mock(IndividuPojo.class);
         IndividuPojo ipj2 = mock(IndividuPojo.class);
         IndividuPojo ipj3 = mock(IndividuPojo.class);
         mockListIndi.add(ipj1);
         mockListIndi.add(ipj2);
         mockListIndi.add(ipj3);
         Stream<IndividuPojo> stream = iterableStream(mockListIndi);
         assertEquals("Built 3 items", 3, stream.length());
         verify(ipj1, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj2, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj3, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         //When
         Stream<IndividuPojo> result = stream.map(initCursusScol(false, mockApoServ, mockI18n));
         //Then should not init
         assertEquals("Should not add/remove item from list", stream.length(), result.length());
         verify(ipj1, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj2, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
         verify(ipj3, never()).initIndCursusScolPojo(mockApoServ, mockI18n);
     }
 
     private Set<IndVoeuPojo> buildSetWithVoeuOnTransfert() {
         IndVoeuPojo onTransfertIndVoeu = buildIndVoeuWithTransfert(true);
         IndVoeuPojo notOnTransfertIndVoeu = buildIndVoeuWithTransfert(false);
         assertFalse(onTransfertIndVoeu.equals(notOnTransfertIndVoeu));
         Set<IndVoeuPojo> result = new HashSet<>();
         result.add(notOnTransfertIndVoeu);
         result.add(onTransfertIndVoeu);
         assertEquals(2, result.size());
         return result;
     }
 
     private Set<IndVoeuPojo> buildSetWithInvalidVoeu() {
         IndVoeuPojo dumbValidIndVoeu = buildValidVoeu(true);
         IndVoeuPojo dumbInvalidVoeu = buildValidVoeu(false);
         assertFalse(dumbInvalidVoeu.equals(dumbValidIndVoeu));
         Set<IndVoeuPojo> result = new HashSet<>();
         result.add(dumbValidIndVoeu);
         result.add(dumbInvalidVoeu);
         assertEquals(2, result.size());
         return result;
     }
 
     private IndVoeuPojo buildValidVoeu(boolean valid) {
         IndVoeuPojo result = buildIndVoeu();
         if (valid) {
             result.setAvisEnService(buildValidAvis(valid));
         } else {
             result.setAvisEnService(buildValidAvis(valid));
         }
         return result;
     }
 
     private Avis buildValidAvis(boolean valid) {
         Avis result = new Avis();
         if (valid) {
             result.setValidation(true);
         } else {
             result.setValidation(false);
         }
         return result;
     }
 
     private Stream<RegimeInscription> buildRegimeInscriptionStream() {
         List<RegimeInscription> regs = new ArrayList<>();
         RegimeInscription r1 = mock(RegimeInscription.class);
         when(r1.getCode()).thenReturn(1);
         RegimeInscription r2 = mock(RegimeInscription.class);
         when(r2.getCode()).thenReturn(2);
         regs.add(r1);
         regs.add(r2);
         return iterableStream(regs);
     }
 
     private IndVoeuPojo buildIndVoeuWithTransfert(boolean onTransfert) {
         IndVoeuPojo result = buildIndVoeu();
         result.setTypeTraitement(buildTreatementWithTransfert(onTransfert));
         return result;
     }
 
     /**
      * Should redefine VersionEtape {@see IndVoeuPojo.equals()}
      *
      * @return
      */
     private IndVoeuPojo buildIndVoeu() {
         IndVoeuPojo result = new IndVoeuPojo();
         VersionEtapeDTO vedto = new VersionEtapeDTO();
        Random r = new Random();
         vedto.setCodEtp(String.valueOf(r.nextInt(10)+r.nextInt(5)));
         vedto.setCodVrsVet(r.nextInt(25)+r.nextInt(4));
         result.setVrsEtape(vedto);
         return result;
     }
 
     private Stream<IndividuPojo> buildIndPojoStreamWithElementToBeFiltered() {
         List<IndividuPojo> listIndi = new ArrayList<>();
         IndividuPojo ipj1 = new IndividuPojo();
         //that we should filter this one
         ipj1.setIndVoeuxPojo(buildSetWithVoeuOnTransfert());
         listIndi.add(ipj1);
         return iterableStream(listIndi);
     }
 
     private boolean retrieveSomeTransfert(final Stream<IndividuPojo> stream, final TypeTraitement transfert) {
         return stream.exists(new F<IndividuPojo, Boolean>() {
             public Boolean f(IndividuPojo individuPojo) {
                 return iterableStream(individuPojo.getIndVoeuxPojo()).exists(Predicates.isTraitementNotEquals(transfert));
             }
         });
     }
 
     private boolean retrieveVoeuInStreamWithValidatAvisEquals(Stream<IndividuPojo> stream, final boolean onlyValidate) {
         return stream.exists(new F<IndividuPojo, Boolean>() {
             @Override
             public Boolean f(IndividuPojo individuPojo) {
                 return iterableStream(individuPojo.getIndVoeuxPojo()).exists(Predicates.keepOnlyAvisWithValidationEquals(onlyValidate));
             }
         });
     }
 
     private Transfert buildTreatementWithTransfert(boolean onTransfert) {
         Transfert result = new Transfert();
         if (onTransfert) {
             result.setCode("TR");
             result.setLabel("Transfert");
             transfert = result;
         } else {
             result.setCode("NTR");
             result.setLabel("NonTransfert");
             nonTransfert = result;
         }
         return result;
     }
 }
