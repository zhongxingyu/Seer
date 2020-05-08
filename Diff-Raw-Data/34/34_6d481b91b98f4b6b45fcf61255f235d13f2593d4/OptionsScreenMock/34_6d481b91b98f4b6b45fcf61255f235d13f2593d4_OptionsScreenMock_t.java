 package ui.swing.options;
 
 import org.reactive.Signal;
 import org.reactive.Source;
 
 public class OptionsScreenMock implements OptionsScreenModel {

 	Source<String> password = new Source<String>("bar");
 
 	@Override
 	public Signal<String> password() {
 		return password;
 	}
 
 	@Override
	public void setConfig(final boolean selected, final String username,
			final String password) {

 	}
 
 	@Override
 	public Signal<Boolean> twitterEnabled() {
 		return new Source<Boolean>(true);
 	}
 
 	@Override
 	public Signal<String> userName() {
 		return new Source<String>("bar");
 	}
 
 }
