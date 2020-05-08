 package org.codefaces.ui.internal.commands;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.codefaces.core.models.Repo;
 import org.codefaces.core.models.RepoCredential;
 import org.codefaces.core.models.RepoFile;
 import org.codefaces.core.models.RepoFileInfo;
 import org.codefaces.core.models.RepoFolder;
 import org.codefaces.core.models.RepoFolderInfo;
 import org.codefaces.core.models.RepoFolderRoot;
 import org.codefaces.core.models.RepoInfo;
 import org.codefaces.core.models.RepoResource;
 import org.codefaces.core.models.RepoResourceType;
 import org.eclipse.core.runtime.IPath;
 
 public class RepoModelTestingUtils {
 	private static class MockRepoFolderInfo extends RepoFolderInfo{
 		public MockRepoFolderInfo() {
 			super(null);
 		}
 
 		private Collection<RepoResource> children = new ArrayList<RepoResource>();
 		
 		public void addChild(RepoResource child){
 			children.add(child);
 		}
 		
 		@Override
 		public Collection<RepoResource> getChildren() {
 			return children;
 		}
 
 		@Override
 		public boolean hasChildren() {
 			return !getChildren().isEmpty();
 		}
 	}
 	
 	private static class MockRepoFileInfo extends RepoFileInfo{
 
 		public MockRepoFileInfo() {
 			super(null, null, null, null, 0);
 		}
 
 		private Collection<RepoResource> children = new ArrayList<RepoResource>();
 		
 		@Override
 		public Collection<RepoResource> getChildren() {
 			return children;
 		}
 
 		@Override
 		public boolean hasChildren() {
 			return !getChildren().isEmpty();
 		}
 	}
 	
 	private static class MockRepoInfo extends RepoInfo{
 		public MockRepoInfo() {
 			super(null);
 		}
 
 		private Collection<RepoResource> children = new ArrayList<RepoResource>();
 		
 		public void addChild(RepoResource child){
 			children.add(child);
 		}
 		
 		@Override
 		public Collection<RepoResource> getChildren() {
 			return children;
 		}
 
 		@Override
 		public boolean hasChildren() {
 			return !getChildren().isEmpty();
 		}
 	}
 	
 	private static class MockRepo extends Repo{
 		protected MockRepo(String kind, String url, String name,
 				RepoCredential credential, RepoInfo repoInfo,
 				RepoFolderInfo rootInfo) {
 			super(kind, url, name, credential, repoInfo, rootInfo);
 		}
 	}
 	
 	private static class MockRepoFile extends RepoFile{
 		protected MockRepoFile(RepoFolderRoot root, RepoResource parent,
 				String id, String name, RepoFileInfo info) {
 			super(root, parent, id, name, info);
 		}
 	}
 	
 	private static class MockRepoFolder extends RepoFolder{
 		protected MockRepoFolder(RepoFolderRoot root, RepoResource parent,
 				String id, String name, RepoResourceType type,
 				RepoFolderInfo info) {
 			super(root, parent, id, name, type, info);
 		}
 	}
 	
 	public static RepoResource createMockRepoResourceFromPath(String mockRepoKind,
 			String mockUrl, String mockUser, String mockPassword, IPath mockPath, boolean isFile) {
 		
 		MockRepoInfo mockRepoInfo = new MockRepoInfo();
 		MockRepoFolderInfo mockRootInfo = new MockRepoFolderInfo();
 		MockRepo repo = new MockRepo(mockRepoKind, mockUrl, "repoName",
 				new RepoCredential(mockUser, mockPassword), mockRepoInfo, mockRootInfo);
 		mockRepoInfo.addChild(repo.getRoot());
 		
 		String[] segments = mockPath.segments();
 		RepoResource currResource = repo.getRoot();
 		MockRepoFolderInfo currInfo = mockRootInfo;
 		for(int i=0; i < segments.length; i++){
 			MockRepoFolderInfo folderInfo = new MockRepoFolderInfo();
 		
 			RepoResource resource;
 			if(isFile && (i == segments.length - 1)){
 				MockRepoFileInfo fileInfo = new MockRepoFileInfo();
 				resource = new MockRepoFile(repo.getRoot(), currResource,
 						segments[i], segments[i], fileInfo);
 			}
 			else{
 				resource = new MockRepoFolder(repo.getRoot(), currResource,
 						segments[i], segments[i], RepoResourceType.FOLDER, folderInfo);
 			}
 			currInfo.addChild(resource);
 			currResource = resource;
 			currInfo = folderInfo;
 		}
 		
 		return currResource;
 	}
 }
