 package ch.opentrainingcenter.core.db;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Status;
 import org.junit.After;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 @SuppressWarnings("nls")
 public class DatabaseAccessFactoryTest {
 
     @After
     public void after() {
         DatabaseAccessFactory.reset();
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInitialisierung() throws CoreException {
         DatabaseAccessFactory.getDatabaseAccess();
     }
 
     @Test
     public void testGetDAo() throws CoreException {
         DatabaseAccessFactory.init("H2 Database", "org.h2.Driver", "jdbc:h2:file:~/.otc_dev/otc", "", "sa", "");
         DatabaseAccessFactory.getDatabaseAccess();
        final DatabaseAccessFactory instance = DatabaseAccessFactory.getInstance();
         final String extensionAttr = "attr";
 
         final IConfigurationElement[] confItems = new IConfigurationElement[1];
         final IConfigurationElement mockA = Mockito.mock(IConfigurationElement.class);
         Mockito.when(mockA.getName()).thenReturn("Junit");
         Mockito.when(mockA.getNamespaceIdentifier()).thenReturn("namespace");
 
         final IDatabaseAccess mockAccess = Mockito.mock(IDatabaseAccess.class);
         Mockito.when(mockAccess.getName()).thenReturn("H2 Database");
         Mockito.when(mockA.createExecutableExtension(extensionAttr)).thenReturn(mockAccess);
 
         confItems[0] = mockA;
        final Map<String, IDatabaseAccess> dao = instance.getDao(confItems, extensionAttr);
 
         assertNotNull("Es muss ein dao auch zurückgegeben werden", dao);
         assertEquals("Es muss ein dao zurückgegeben werden", 1, dao.size());
     }
 
     @Test
     public void testGetDAoMitCoreException() throws CoreException {
         DatabaseAccessFactory.init("H2 Database", "org.h2.Driver", "jdbc:h2:file:~/.otc_dev/otc", "", "sa", "");
         DatabaseAccessFactory.getDatabaseAccess();
        final DatabaseAccessFactory instance = DatabaseAccessFactory.getInstance();
         final String extensionAttr = "attr";
 
         final IConfigurationElement[] confItems = new IConfigurationElement[1];
         final IConfigurationElement mockA = Mockito.mock(IConfigurationElement.class);
         Mockito.when(mockA.getName()).thenReturn("Junit");
         Mockito.when(mockA.getNamespaceIdentifier()).thenReturn("namespace");
         final CoreException ce = new CoreException(Status.CANCEL_STATUS);
 
         Mockito.when(mockA.createExecutableExtension(extensionAttr)).thenThrow(ce);
 
         confItems[0] = mockA;
        final Map<String, IDatabaseAccess> daos = instance.getDao(confItems, extensionAttr);
         assertTrue("Extension wurde nicht gefunden", daos.isEmpty());
     }
 }
