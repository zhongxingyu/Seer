 package nz.gen.wellington.guardian.android.model;
 
 import android.graphics.Color;
 
 public class WhiteOnBlackColourScheme extends ColourScheme {
 	
 	@Override
 	public Integer getBackground() {
 		return null;
 	}
 
 	@Override
 	public Integer getBodytext() {
 		return Color.LTGRAY;
 	}
 
 	@Override
 	public Integer getHeadline() {
 		return Color.WHITE;
 	}
 	
 	@Override
 	public Integer getAvailableTag() {
		return Color.DKGRAY;
 	}
 
 	@Override
 	public Integer getUnavailableTag() {
		return Color.WHITE;
 	}
 	
 	@Override
 	public Integer getStatus() {
 		return Color.LTGRAY;
 	}
 	
 }
