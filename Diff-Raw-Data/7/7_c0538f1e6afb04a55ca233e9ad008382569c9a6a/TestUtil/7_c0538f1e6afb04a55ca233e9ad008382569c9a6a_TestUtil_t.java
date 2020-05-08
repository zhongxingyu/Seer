 package org.jboss.pressgang.ccms.contentspec.client.commands.base;
 
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Mockito.mock;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.provider.UserProvider;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 /**
  * Methods shared across tests.
  *
  * @author kamiller@redhat.com (Katie Miller)
  */
 public class TestUtil {
     public static File createRealFile(String newFilename, String contents) throws IOException {
         File file = new File(newFilename);
         FileUtils.writeStringToFile(file, contents);
         return file;
     }
 
     public static void setValidFileProperties(File fileMock) {
         given(fileMock.isDirectory()).willReturn(false);
         given(fileMock.isFile()).willReturn(true);
         given(fileMock.exists()).willReturn(true);
     }
 
     public static void setValidLevelMocking(Level levelMock, String title) {
         given(levelMock.getLevelType()).willReturn(LevelType.BASE);
         given(levelMock.getNumberOfSpecTopics()).willReturn(1);
         given(levelMock.getTitle()).willReturn(title);
     }
 
     public static void setValidContentSpecWrapperMocking(ContentSpecWrapper contentSpecWrapperMock, String randomAlphanumString,
             Integer id) {
         given(contentSpecWrapperMock.getId()).willReturn(id);
         given(contentSpecWrapperMock.getRevision()).willReturn(id);
         final UpdateableCollectionWrapper<CSNodeWrapper> metaData = createValidContentSpecMetaDatasMocking(randomAlphanumString);
         given(contentSpecWrapperMock.getChildren()).willReturn(metaData);
     }
 
     private static UpdateableCollectionWrapper<CSNodeWrapper> createValidContentSpecMetaDatasMocking(String randomAlphanumString) {
         final UpdateableCollectionWrapper<CSNodeWrapper> metaDatas = mock(UpdateableCollectionWrapper.class);
 
         final CSNodeWrapper versionMetaData = createValidMetaDataMocking(3, "Version", randomAlphanumString, null);
         final CSNodeWrapper productMetaData = createValidMetaDataMocking(2, "Product", randomAlphanumString, versionMetaData);
         final CSNodeWrapper titleMetaData = createValidMetaDataMocking(1, "Title", randomAlphanumString, productMetaData);
 
         final List<CSNodeWrapper> metaData = Arrays.asList(titleMetaData, productMetaData, versionMetaData);
         given(metaDatas.size()).willReturn(metaData.size());
         given(metaDatas.getItems()).willReturn(metaData);
 
         return metaDatas;
     }
 
     private static CSNodeWrapper createValidMetaDataMocking(Integer id, String key, String value, CSNodeWrapper next) {
         final CSNodeWrapper metaDataMock = mock(CSNodeWrapper.class);
         given(metaDataMock.getId()).willReturn(id);
         given(metaDataMock.getNodeType()).willReturn(7);
         given(metaDataMock.getTitle()).willReturn(key);
         given(metaDataMock.getAdditionalText()).willReturn(value);
         given(metaDataMock.getNextNode()).willReturn(next);
         return metaDataMock;
     }
 
     public static void setValidContentSpecMocking(ContentSpec contentSpecMock, Level levelMock, String randomAlphanumString, Integer id) {
         given(contentSpecMock.getBaseLevel()).willReturn(levelMock);
         given(contentSpecMock.getTitle()).willReturn(randomAlphanumString);
         given(contentSpecMock.getProduct()).willReturn(randomAlphanumString);
         given(contentSpecMock.getVersion()).willReturn("1");
         given(contentSpecMock.getDtd()).willReturn("Docbook 4.5");
         given(contentSpecMock.getCopyrightHolder()).willReturn(randomAlphanumString);
        given(contentSpecMock.getDefaultPublicanCfg()).willCallRealMethod();
         given(contentSpecMock.getId()).willReturn(id);
         given(contentSpecMock.getRevision()).willReturn(null);
         given(contentSpecMock.getChecksum()).willReturn(HashUtilities.generateMD5("ID = " + id + "\nTitle = " + randomAlphanumString +
                 "\nProduct = " + randomAlphanumString + "\nVersion = " + randomAlphanumString + "\n"));
     }
 
     public static String createValidContentSpecString(String randomAlphanumString, Integer id) {
         final String spec = "ID = " + id + "\nTitle = " + randomAlphanumString +
                 "\nProduct = " + randomAlphanumString + "\nVersion = " + randomAlphanumString + "\n";
         return "CHECKSUM=" + HashUtilities.generateMD5(spec) + "\n" + spec;
     }
 
     public static void setUpAuthorisedUser(BaseCommand command, UserProvider userProviderMock, CollectionWrapper<UserWrapper> usersMock,
             UserWrapper userMock, String username) {
         command.setUsername(username);
         given(userProviderMock.getUsersByName(username)).willReturn(usersMock);
         given(usersMock.size()).willReturn(1);
         given(usersMock.getItems()).willReturn(Arrays.asList(userMock));
         given(userMock.getUsername()).willReturn(username);
     }
 
     public static void setUpZanataDetails(ZanataDetails zanataDetailsMock, String serverUrl, String project, String version,
             String username, String token) {
         given(zanataDetailsMock.getServer()).willReturn(serverUrl);
         given(zanataDetailsMock.getProject()).willReturn(project);
         given(zanataDetailsMock.getVersion()).willReturn(version);
         given(zanataDetailsMock.getUsername()).willReturn(username);
         given(zanataDetailsMock.getToken()).willReturn(token);
     }
 }
