 package org.iplantc.de.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.core.uidiskresource.client.util.DiskResourceUtil;
 import org.iplantc.de.client.utils.DataUtils;
 import org.iplantc.de.client.utils.DataUtils.Action;
 
 import com.google.gwt.junit.client.GWTTestCase;
 
 public class GwtTestDataUtils extends GWTTestCase {
 
     @Override
     public String getModuleName() {
         return "org.iplantc.de.discoveryenvironment"; //$NON-NLS-1$
     }
 
     private List<DiskResource> buildMixedPermissionsFilesList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
         File f1 = new File("/iplant/home/ipctest/analyses/1/test1", "test1", new Permissions(true,
                 false, false));
         File f2 = new File("/iplant/home/ipctest/analyses/1/test2", "test2", new Permissions(true, true,
                 true));
 
         File f3 = new File("/iplant/home/ipctest/analyses/1/test3", "test3", new Permissions(true,
                 false, false));
         list.add(f1);
         list.add(f2);
         list.add(f3);
         return list;
     }
 
     private List<DiskResource> buildReadOnlyFilesList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
         File f1 = new File("/iplant/home/ipctest/analyses/1/test1", "test1", new Permissions(true,
                 false, false));
         File f2 = new File("/iplant/home/ipctest/analyses/1/test2", "test2", new Permissions(true,
                 false, false));
 
         File f3 = new File("/iplant/home/ipctest/analyses/1/test3", "test3", new Permissions(true,
                 false, false));
 
         list.add(f1);
         list.add(f2);
         list.add(f3);
         return list;
     }
 
     private List<DiskResource> buildReadWriteFilesList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
         File f1 = new File("/iplant/home/ipctest/analyses/1/test1", "test1", new Permissions(true, true,
                 true));
         File f2 = new File("/iplant/home/ipctest/analyses/1/test2", "test2", new Permissions(true, true,
                 true));
 
         File f3 = new File("/iplant/home/ipctest/analyses/1/test3", "test3", new Permissions(true, true,
                 true));
         list.add(f1);
         list.add(f2);
         list.add(f3);
         return list;
     }
 
     private List<DiskResource> buildFileFolderList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
         File f1 = new File("/iplant/home/ipctest/analyses/1/test1", "test1", new Permissions(true, true,
                 true));
         File f2 = new File("/iplant/home/ipctest/analyses/1/test2", "test2", new Permissions(true, true,
                 true));
 
         File f3 = new File("/iplant/home/ipctest/analyses/1/test3", "test3", new Permissions(true, true,
                 true));
         Folder fo1 = new Folder("/iplant/home/ipctest/analyses/1", "1", false, new Permissions(true,
                 true, true));
 
         list.add(f1);
         list.add(f2);
         list.add(f3);
         list.add(fo1);
 
         return list;
     }
 
     private File buildFile() {
         return new File("/iplant/home/ipctest/analyses/1/test1", "test1", new Permissions(true, true,
                 true));
     }
 
     private Folder buildFolder() {
         return new Folder("/iplant/home/ipctest/analyses/1", "1", false, new Permissions(true, true,
                 true));
     }
 
     private Folder buildReadOnlyFolder() {
         return new Folder("/iplant/home/ipctest/analyses/1", "1", false, new Permissions(true, false,
                 false));
     }
 
     private List<DiskResource> buildSingleFileList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
         list.add(buildFile());
         return list;
     }
 
     private List<DiskResource> buildSingleFolderList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
         list.add(buildFolder());
         return list;
     }
 
     private List<DiskResource> buildReadOnlyFolderList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
 
         Folder fo1 = new Folder("/iplant/home/ipctest/analyses/1", "1", false, new Permissions(true,
                 false, false));
         Folder fo2 = new Folder("/iplant/home/ipctest/analyses/2", "2", false, new Permissions(true,
                 false, false));
         Folder fo3 = new Folder("/iplant/home/ipctest/analyses/3", "3", false, new Permissions(true,
                 false, false));
 
         list.add(fo1);
         list.add(fo2);
         list.add(fo3);
 
         return list;
     }
 
     private List<DiskResource> buildReadWriteFolderList() {
         List<DiskResource> list = new ArrayList<DiskResource>();
 
         Folder fo1 = new Folder("/iplant/home/ipctest/analyses/1", "1", false, new Permissions(true,
                 true, true));
         Folder fo2 = new Folder("/iplant/home/ipctest/analyses/2", "2", false, new Permissions(true,
                 true, true));
         Folder fo3 = new Folder("/iplant/home/ipctest/analyses/3", "3", false, new Permissions(true,
                 true, true));
 
         list.add(fo1);
         list.add(fo2);
         list.add(fo3);
 
         return list;
     }
 
     public void testEmptyHasFolders() {
         List<DiskResource> list = null;
         assertFalse(DataUtils.hasFolders(list));
         list = new ArrayList<DiskResource>();
         assertFalse(DataUtils.hasFolders(list));
     }
 
     public void testHadFolders() {
         assertFalse(DataUtils.hasFolders(buildReadOnlyFilesList()));
         assertTrue(DataUtils.hasFolders(buildFileFolderList()));
     }
 
     public void testEmptySupportedActions() {
        // only paste as disabled should returned
         List<DiskResource> list = null;
         List<Action> actions = DataUtils.getSupportedActions(list);
        assertEquals(1, actions.size());
         list = new ArrayList<DiskResource>();
         actions = DataUtils.getSupportedActions(list);
        // only paste as disabled should returned
        assertEquals(1, actions.size());
     }
 
     public void testSupportedActions() {
         List<Action> actions = DataUtils.getSupportedActions(buildSingleFileList());
         assertTrue(actions.contains(Action.Delete));
         assertTrue(actions.contains(Action.SimpleDownload));
         assertTrue(actions.contains(Action.BulkDownload));
         assertTrue(actions.contains(Action.RenameFile));
         assertTrue(actions.contains(Action.View));
         assertTrue(actions.contains(Action.ViewTree));
         assertTrue(actions.contains(Action.Metadata));
         assertTrue(actions.contains(Action.Share));
        assertTrue(actions.contains(Action.Copy));
        assertTrue(actions.contains(Action.Paste));
        assertEquals(10, actions.size());
 
         actions = DataUtils.getSupportedActions(buildSingleFolderList());
         assertTrue(actions.contains(Action.Delete));
         assertTrue(actions.contains(Action.BulkDownload));
         assertTrue(actions.contains(Action.RenameFolder));
         assertTrue(actions.contains(Action.Metadata));
         assertTrue(actions.contains(Action.Share));
        assertTrue(actions.contains(Action.Copy));
        assertTrue(actions.contains(Action.Paste));
        assertEquals(7, actions.size());
 
         actions = DataUtils.getSupportedActions(buildFileFolderList());
         assertTrue(actions.contains(Action.Delete));
         assertTrue(actions.contains(Action.BulkDownload));
         assertTrue(actions.contains(Action.Share));
        assertTrue(actions.contains(Action.Copy));
        assertTrue(actions.contains(Action.Paste));
        assertEquals(5, actions.size());
 
         actions = DataUtils.getSupportedActions(buildMixedPermissionsFilesList());
         assertTrue(actions.contains(Action.Delete));
         assertTrue(actions.contains(Action.SimpleDownload));
         assertTrue(actions.contains(Action.BulkDownload));
         assertTrue(actions.contains(Action.View));
         assertTrue(actions.contains(Action.Share));
        assertTrue(actions.contains(Action.Copy));
        assertTrue(actions.contains(Action.Paste));
        assertEquals(7, actions.size());
 
         actions = DataUtils.getSupportedActions(buildReadOnlyFolderList());
         assertTrue(actions.contains(Action.Delete));
         assertTrue(actions.contains(Action.BulkDownload));
         assertTrue(actions.contains(Action.Share));
        assertTrue(actions.contains(Action.Copy));
        assertTrue(actions.contains(Action.Paste));
        assertEquals(5, actions.size());
     }
 
     public void testParseParentEmpty() {
         String parent = DiskResourceUtil.parseParent(null);
         assertEquals("", parent);
 
         parent = DiskResourceUtil.parseParent("");
         assertEquals("", parent);
     }
 
     public void testParseParent() {
         String parent = DiskResourceUtil.parseParent("/iplant/home/ipctest/analyses/1");
         assertEquals("/iplant/home/ipctest/analyses", parent);
     }
 
     public void testParseNameEmpty() {
         String name = DiskResourceUtil.parseNameFromPath(null);
         assertEquals("", name);
 
         name = DiskResourceUtil.parseNameFromPath("");
         assertEquals("", name);
     }
 
     public void testParseName() {
         String name = DiskResourceUtil.parseNameFromPath("/iplant/home/ipctest/analyses/1");
         assertEquals("1", name);
 
         name = DiskResourceUtil.parseNameFromPath("/iplant/home/ipctest/analyses/space name");
         assertEquals("space name", name);
     }
 
     public void testSizeForDisplay() {
         String size = DataUtils.getSizeForDisplay(512);
         assertEquals("512 bytes", size);
 
         size = DataUtils.getSizeForDisplay(1024);
         assertEquals("1.0 KB", size);
 
         size = DataUtils.getSizeForDisplay(1048576);
         assertEquals("1.0 MB", size);
 
         size = DataUtils.getSizeForDisplay(1073741824);
         assertEquals("1.0 GB", size);
     }
 
     public void testIsMovable() {
         assertFalse(DataUtils.isMovable(null));
         assertFalse(DataUtils.isMovable(new ArrayList<DiskResource>()));
         assertTrue(DataUtils.isMovable(buildReadWriteFilesList()));
         assertTrue(DataUtils.isMovable(buildReadWriteFolderList()));
         assertFalse(DataUtils.isMovable(buildMixedPermissionsFilesList()));
     }
 
     public void testIsViewable() {
         assertFalse(DataUtils.isViewable(null));
         assertFalse(DataUtils.isViewable(new ArrayList<DiskResource>()));
         assertTrue(DataUtils.isViewable(buildReadWriteFilesList()));
         assertTrue(DataUtils.isViewable(buildReadWriteFolderList()));
         assertTrue(DataUtils.isViewable(buildMixedPermissionsFilesList()));
     }
 
     public void testIsDownloadable() {
         assertFalse(DataUtils.isViewable(null));
         assertFalse(DataUtils.isViewable(new ArrayList<DiskResource>()));
         assertTrue(DataUtils.isViewable(buildReadWriteFilesList()));
         assertTrue(DataUtils.isViewable(buildReadWriteFolderList()));
         assertTrue(DataUtils.isViewable(buildMixedPermissionsFilesList()));
     }
 
     public void testIsDeletable() {
         assertFalse(DataUtils.isDeletable(null));
         assertFalse(DataUtils.isDeletable(new ArrayList<DiskResource>()));
         assertTrue(DataUtils.isDeletable(buildReadWriteFilesList()));
         assertTrue(DataUtils.isDeletable(buildReadWriteFolderList()));
         assertFalse(DataUtils.isDeletable(buildMixedPermissionsFilesList()));
         assertFalse(DataUtils.isDeletable(buildReadOnlyFolderList()));
     }
 
     public void testIsRenamable() {
         assertFalse(DataUtils.isRenamable(null));
         assertTrue(DataUtils.isRenamable(buildFolder()));
         assertTrue(DataUtils.isRenamable(buildFile()));
     }
 
     public void testCanUploadFolder() {
         assertFalse(DataUtils.canUploadToThisFolder(buildReadOnlyFolder()));
         assertTrue(DataUtils.canUploadToThisFolder(buildFolder()));
     }
 
     public void testcanCreateFolderInThisFolder() {
         assertFalse(DataUtils.canCreateFolderInThisFolder(buildReadOnlyFolder()));
         assertTrue(DataUtils.canCreateFolderInThisFolder(buildFolder()));
     }
 }
