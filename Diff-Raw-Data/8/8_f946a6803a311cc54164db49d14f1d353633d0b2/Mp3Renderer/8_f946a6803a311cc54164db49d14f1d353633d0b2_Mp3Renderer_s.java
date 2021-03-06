 /**
  * 
  */
 package de.beimax.spacealert.render;
 
 import de.beimax.spacealert.mission.Mission;
 import de.beimax.spacealert.util.Options;
 
 /**
  * @author mkalus
  *
  */
 public class Mp3Renderer implements Renderer {
 
 	/* (non-Javadoc)
 	 * @see de.beimax.spacealert.render.Renderer#print(de.beimax.spacealert.mission.Mission)
 	 */
 	public boolean print(Mission mission) {
		// get options
 		Options options = Options.getOptions();
 		
 		// check file name/size
 		if (options.outPutfilePrefix == null || options.outPutfilePrefix.isEmpty()) {
 			System.out.println("Error writing MP3 file: file prefix is empty.");
 		}
 		
 		// TODO not yet supported
		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.beimax.spacealert.render.Renderer#output(de.beimax.spacealert.mission.Mission)
 	 */
 	public boolean output(Mission mission) {
 		// convert output MP3 to play
 		Options options = Options.getOptions();
 		if (!options.silent) {
 			System.out.println("Converting --print MP3 to --play.");
 		}
 		options.play = true;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "MP3";
 	}
 }
