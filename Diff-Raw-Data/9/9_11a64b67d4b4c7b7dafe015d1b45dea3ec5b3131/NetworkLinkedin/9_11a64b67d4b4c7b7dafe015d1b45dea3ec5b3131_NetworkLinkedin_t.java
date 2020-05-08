 package carnero.me.data;
 
 import carnero.me.R;
 import carnero.me.model.EntryIntent;
 import carnero.me.model.Network;
 
 @SuppressWarnings("UnusedDeclaration")
 public class NetworkLinkedin extends Network {
 
 	public NetworkLinkedin() {
 		iconOn = R.drawable.ic_linkedin;
 		iconOff = R.drawable.ic_linkedin_off;
 		title = R.string.network_linkedin;
 		description = R.string.network_linkedin_desc;
		tapAction = new EntryIntent().setWeb("http://cz.linkedin.com/in/carnerocc/");
 		packageName = new String[]{"com.linkedin.android"};
 	}
 }
