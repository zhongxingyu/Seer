 package org.commonjava.web.fd.data;
 
 import static org.commonjava.web.fd.fixture.CouchShiroTestFixture.clearSubject;
 import static org.commonjava.web.fd.fixture.CouchShiroTestFixture.setupSecurityManager;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Level;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.commonjava.auth.couch.data.PasswordManager;
 import org.commonjava.auth.couch.data.UserDataException;
 import org.commonjava.auth.couch.data.UserDataManager;
 import org.commonjava.auth.couch.inject.UserData;
 import org.commonjava.auth.shiro.couch.CouchPermissionResolver;
 import org.commonjava.auth.shiro.couch.CouchRealm;
 import org.commonjava.auth.shiro.couch.model.ShiroUserUtils;
 import org.commonjava.couch.db.CouchManager;
 import org.commonjava.couch.rbac.User;
 import org.commonjava.couch.test.fixture.LoggingFixture;
 import org.commonjava.web.fd.WSFileDataTestPlan;
 import org.commonjava.web.fd.WorkspaceDataTestPlan;
 import org.commonjava.web.fd.fixture.InjectableTemporaryFolder;
import org.commonjava.web.fd.fixture.TestFDFactory;
 import org.commonjava.web.fd.fixture.WeldJUnit4Runner;
 import org.commonjava.web.fd.inject.FileDepotData;
 import org.commonjava.web.fd.model.Workspace;
 import org.commonjava.web.fd.model.WorkspaceFile;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestName;
 import org.junit.runner.RunWith;
 
 @RunWith( WeldJUnit4Runner.class )
 public class WorkspaceDataManagerWeldTest
     implements WorkspaceDataTestPlan, WSFileDataTestPlan
 {
 
     private CouchRealm realm;
 
     @Inject
     private WorkspaceDataManager dataManager;
 
     @Inject
     private PasswordManager passwordManager;
 
     @Inject
     @FileDepotData
     private CouchManager fdCouch;
 
     @Inject
     @UserData
     private CouchManager userCouch;
 
     @Inject
     private UserDataManager userMgr;
 
     @Rule
     @Inject
     public InjectableTemporaryFolder temp;
 
     @Rule
     public final TestName name = new TestName();
 
    @Inject
    private TestFDFactory factory;

     @BeforeClass
     public static void setupStatic()
     {
         LoggingFixture.setupLogging( Level.INFO );
     }
 
     @Before
     public void setup()
         throws Exception
     {
         realm = new CouchRealm( userMgr, new CouchPermissionResolver( userMgr ) );
         setupSecurityManager( realm );
 
         fdCouch.dropDatabase();
         userCouch.dropDatabase();
         dataManager.install();
     }
 
     @After
     public void teardown()
         throws Exception
     {
         fdCouch.dropDatabase();
         userCouch.dropDatabase();
        factory.delete();
 
         clearSubject();
     }
 
     /*
      * (non-Javadoc)
      * @see org.commonjava.web.fd.data.WorkspaceDataManagerTestPlan#storeAndRetrieveWorkspace()
      */
     @Override
     @Test
     public void storeAndRetrieveWorkspace()
         throws WorkspaceDataException
     {
         final Workspace ws = new Workspace( name.getMethodName() );
 
         dataManager.storeWorkspace( ws );
         final Workspace result = dataManager.getWorkspace( ws.getName() );
 
         assertThat( result.getName(), equalTo( ws.getName() ) );
     }
 
     /*
      * (non-Javadoc)
      * @see org.commonjava.web.fd.data.WorkspaceDataManagerTestPlan#storeTwoAndRetrieveAll()
      */
     @Override
     @Test
     public void storeTwoAndRetrieveAll()
         throws WorkspaceDataException
     {
         final Workspace ws = new Workspace( name.getMethodName() );
         final Workspace ws2 = new Workspace( name.getMethodName() + "2" );
 
         dataManager.storeWorkspace( ws );
         dataManager.storeWorkspace( ws2 );
 
         final List<Workspace> workspaces = dataManager.getWorkspaces();
 
         assertThat( workspaces, notNullValue() );
         assertThat( workspaces.size(), equalTo( 2 ) );
 
         assertThat( workspaces.contains( ws ), equalTo( true ) );
         assertThat( workspaces.contains( ws2 ), equalTo( true ) );
     }
 
     /*
      * (non-Javadoc)
      * @see org.commonjava.web.fd.data.WorkspaceDataManagerTestPlan#storeThreeAndRetrieveTwoAssociatedWithUser()
      */
     @Override
     @Test
     public void storeThreeAndRetrieveTwoAssociatedWithUser()
         throws WorkspaceDataException, UserDataException
     {
         final Workspace ws = new Workspace( name.getMethodName() );
         final Workspace ws2 = new Workspace( name.getMethodName() + "2" );
         final Workspace ws3 = new Workspace( name.getMethodName() + "3" );
 
         dataManager.storeWorkspace( ws );
         dataManager.storeWorkspace( ws2 );
         dataManager.storeWorkspace( ws3 );
 
         final User user =
             new User( "user", "user@nowherer.com", "User", "One", passwordManager.digestPassword( "password" ) );
         user.addRole( Workspace.userRole( ws.getName() ) );
         user.addRole( Workspace.userRole( ws2.getName() ) );
 
         userMgr.storeUser( user );
 
         final Subject subject = SecurityUtils.getSubject();
         subject.login( ShiroUserUtils.getAuthenticationToken( user ) );
 
         final List<Workspace> workspaces = dataManager.getWorkspacesForUser( subject );
 
         assertThat( workspaces, notNullValue() );
         assertThat( workspaces.size(), equalTo( 2 ) );
 
         assertThat( workspaces.contains( ws ), equalTo( true ) );
         assertThat( workspaces.contains( ws2 ), equalTo( true ) );
         assertThat( workspaces.contains( ws3 ), equalTo( false ) );
     }
 
     /*
      * (non-Javadoc)
      * @see org.commonjava.web.fd.data.WorkspaceDataManagerTestPlan#storeAndDeleteWorkspace()
      */
     @Override
     @Test
     public void storeAndDeleteWorkspace()
         throws WorkspaceDataException
     {
         final Workspace ws = new Workspace( name.getMethodName() );
 
         dataManager.storeWorkspace( ws );
         Workspace result = dataManager.getWorkspace( ws.getName() );
 
         assertThat( result, notNullValue() );
         assertThat( result.getName(), equalTo( ws.getName() ) );
 
         dataManager.deleteWorkspace( ws.getName() );
 
         result = dataManager.getWorkspace( ws.getName() );
 
         assertThat( result, nullValue() );
     }
 
     @Override
     @Test
     public void storeFileWithWorkspaceAndRetrieveInfo()
         throws Exception
     {
         final Workspace ws = new Workspace( name.getMethodName() );
 
         dataManager.storeWorkspace( ws );
 
         final File data = temp.newFile( "test.txt" );
         FileUtils.write( data, "This is a test" );
         final WorkspaceFile wsFile =
             new WorkspaceFile( ws.getName(), "test.txt", "text/plain", data.length(), new Date(), data );
 
         dataManager.storeWorkspaceFile( wsFile );
 
         final WorkspaceFile result = dataManager.getWorkspaceFile( ws.getName(), wsFile.getFileName() );
 
         assertThat( result.getContentLength(), equalTo( wsFile.getContentLength() ) );
         assertThat( result.getContentType(), equalTo( wsFile.getContentType() ) );
     }
 
     @Override
     @Test
     public void storeFileWithWorkspaceAndRetrieveData()
         throws Exception
     {
         final Workspace ws = new Workspace( name.getMethodName() );
 
         dataManager.storeWorkspace( ws );
 
         final File data = temp.newFile( "test.txt" );
         FileUtils.write( data, "This is a test" );
         final WorkspaceFile wsFile =
             new WorkspaceFile( ws.getName(), "test.txt", "text/plain", data.length(), new Date(), data );
 
         dataManager.storeWorkspaceFile( wsFile );
 
         final WorkspaceFile result = dataManager.getWorkspaceFile( ws.getName(), wsFile.getFileName() );
 
         assertThat( result.getContentLength(), equalTo( wsFile.getContentLength() ) );
         assertThat( result.getContentType(), equalTo( wsFile.getContentType() ) );
 
         assertThat( FileUtils.readFileToString( result.getFile() ), equalTo( "This is a test" ) );
     }
 
     @Override
     @Test
     public void storeFileAndDeleteFileWithWorkspace()
         throws Exception
     {
         final Workspace ws = new Workspace( name.getMethodName() );
 
         dataManager.storeWorkspace( ws );
 
         final File data = temp.newFile( "test.txt" );
         FileUtils.write( data, "This is a test" );
         final WorkspaceFile wsFile =
             new WorkspaceFile( ws.getName(), "test.txt", "text/plain", data.length(), new Date(), data );
 
         dataManager.storeWorkspaceFile( wsFile );
         assertThat( dataManager.getWorkspaceFile( ws.getName(), wsFile.getFileName() ), notNullValue() );
 
         dataManager.deleteWorkspaceFile( ws.getName(), wsFile.getFileName() );
         assertThat( dataManager.getWorkspaceFile( ws.getName(), wsFile.getFileName() ), nullValue() );
     }
 
 }
