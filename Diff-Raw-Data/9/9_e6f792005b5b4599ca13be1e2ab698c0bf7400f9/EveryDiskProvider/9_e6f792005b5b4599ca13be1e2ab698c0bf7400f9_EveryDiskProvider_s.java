 package com.fellow.every.vfs.v2;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.apache.commons.vfs2.Capability;
 import org.apache.commons.vfs2.FileName;
 import org.apache.commons.vfs2.FileSystem;
 import org.apache.commons.vfs2.FileSystemConfigBuilder;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileSystemOptions;
 import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
 
 import com.fellow.every.auth.AccessToken;
 import com.fellow.every.auth.OAuthTokenAuthenticator;
 import com.fellow.every.disk.DiskAPI;
 
 public class EveryDiskProvider extends AbstractOriginatingFileProvider {
 
 	private AccessToken accessToken;
 	
     /** The capabilities of the WebDAV provider */
     protected static final Collection<Capability> capabilities =
             Collections.unmodifiableCollection(Arrays.asList(new Capability[]
     {
         Capability.CREATE,
         Capability.DELETE,
         Capability.RENAME,
         Capability.GET_TYPE,
         Capability.URI,
         Capability.LIST_CHILDREN,
         Capability.READ_CONTENT,
         Capability.WRITE_CONTENT,
         Capability.GET_LAST_MODIFIED,
         Capability.ATTRIBUTES,
     }));
    
 	@Override
 	public Collection<Capability> getCapabilities() {
         return capabilities;
 	}
 
 	@Override
 	public FileSystemConfigBuilder getConfigBuilder(){
 		return EveryDiskFileSystemConfigBuilder.getInstance();
 	}
 
 	@Override
 	protected FileSystem doCreateFileSystem(FileName rootName,
 			FileSystemOptions fileSystemOptions) throws FileSystemException {
         FileSystemOptions opts = (fileSystemOptions == null) ? new FileSystemOptions() : fileSystemOptions;
 
 		try {
 	        if(accessToken == null){
 		        OAuthTokenAuthenticator authenticator = EveryDiskFileSystemConfigBuilder.getInstance().getAuthenticator(opts);
 		        accessToken = authenticator.requestAuthentication();
 	        }
 			DiskAPI diskAPI = EveryDiskFileSystemConfigBuilder.getInstance().getDiskAPI(opts);
 			return new EveryDiskFileSystem(rootName, opts, diskAPI, accessToken);
 		} catch (Exception e) {
 			throw new FileSystemException(e);
 		}
 	}
 
 }
