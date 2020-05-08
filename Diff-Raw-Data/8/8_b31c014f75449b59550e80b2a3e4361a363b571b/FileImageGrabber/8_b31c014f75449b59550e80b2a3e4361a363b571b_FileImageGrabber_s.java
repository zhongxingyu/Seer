 package ch.zhaw.headtracker.grabber;
 
 import ch.zhaw.headtracker.algorithm.ControlPanel;
 import ch.zhaw.headtracker.image.Image;
 import java.io.*;
 
 public final class FileImageGrabber implements ImageGrabber {
 	private final String path;
 	private final int width;
 	private final int height;
 	private final float delay;
 	private final ControlPanel.CheckBoxSetting stillFrame = new ControlPanel.CheckBoxSetting("Still frame", false);
 	private final ControlPanel.ButtonSetting restartPlayback = new ControlPanel.ButtonSetting("Restart playback");
 	private Image lastImage = null;
 	private InputStream input = null;
 
 	public FileImageGrabber(String path, int width, int height, float delay) {
 		this.path = path;
 		this.width = width;
 		this.height = height;
 		this.delay = delay;
 	}
 	
 	@SuppressWarnings({ "IOResourceOpenedButNotSafelyClosed" })
 	@Override
 	public Image getImage() throws IOException {
 		if (input == null || restartPlayback.getSignal()) {
 			input = new FileInputStream(path);
 			lastImage = null;
 		}
 		
 		try {
 			Thread.sleep((long) (delay * 1000));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 
 		}
 		
		if (lastImage == null || !stillFrame.value)
			lastImage = Image.readFromStream(input, width, height);
 
 		return lastImage;
 	}
 
 	@Override
 	public ControlPanel.Setting[] getSettings() {
 		return new ControlPanel.Setting[] { stillFrame, restartPlayback };
 	}
 }
