 package org.twitterNotifier.ui;
 
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Panel;
 import java.awt.TrayIcon;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.plaf.metal.MetalIconFactory;
 
 import org.twitterNotifier.TwitterNotifier;
 import org.twitterNotifier.twitterapi.TweetListener;
 
 import twitter4j.Status;
 
 /**
  * Receives events and populates the UI
  * 
  * @author pyppo
  * 
  */
 public class TwitterListener implements TweetListener {
 
 	private TweetList uiList;
 
 	private TrayIcon icon;
 
 	public TwitterListener(TweetList uiList, TrayIcon icon) {
 		super();
 		this.uiList = uiList;
 		this.icon = icon;
 	}
 
 	@Override
 	public void newTweets(List<Status> tweets) {
 		if (tweets.size() > 0) {
 			uiList.addTweets(tweets);
 			icon.setImage(getPresentImage());
 		}
 	}
 
 	private Image getPresentImage() throws HeadlessException {
		ImageIcon defaultIcon = new ImageIcon(this.getClass().getResource("../twitterIcon.gif"));
 		return defaultIcon.getImage();
 	
 	}
 }
