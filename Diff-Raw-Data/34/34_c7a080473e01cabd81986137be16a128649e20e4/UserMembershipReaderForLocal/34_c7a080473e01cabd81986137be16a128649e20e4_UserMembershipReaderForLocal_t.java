 package org.softwareFm.eclipse.user;
 
 import org.softwareFm.common.IFileDescription;
 import org.softwareFm.common.IGitLocal;
 import org.softwareFm.common.IUserReader;
 import org.softwareFm.common.constants.GroupConstants;
 import org.softwareFm.common.url.IUrlGenerator;
 
 public class UserMembershipReaderForLocal extends AbstractUserMembershipReader {
 
 	private final IGitLocal gitLocal;
 	private final String userCryptoKey;
 
 	public UserMembershipReaderForLocal(IUrlGenerator userUrlGenerator, IGitLocal gitLocal, IUserReader user, String userCryptoKey) {
 		super(userUrlGenerator, user);
 		this.gitLocal = gitLocal;
 		this.userCryptoKey = userCryptoKey;
 	}
 
 	@Override
 	protected String getGroupFileAsText(IFileDescription fileDescription) {
 		return gitLocal.getFileAsString(fileDescription);
 	}
 
 	@Override
 	protected String getMembershipCrypto(String softwareFmId) {
 		String result = user.getUserProperty(softwareFmId, userCryptoKey, GroupConstants.membershipCryptoKey);
 		return result;
 	}
 
 }
