 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.geom.RoundRectangle2D;
 import java.awt.image.ImageObserver;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.NumberFormat;
 import java.util.Locale;
 
 import javax.imageio.ImageIO;
 
 import sun.misc.BASE64Decoder;
 
 import com.quirlion.script.Script;
 import com.quirlion.script.types.Area;
 import com.quirlion.script.types.Location;
 import com.quirlion.script.types.Thing;
 
 public class AYanilleMiner extends Script {
	private int bankBoothID = 2213, minedIron = 0, minedGems = 0;
 	private int[] ironIDs = { 37307, 37308, 37309 };
 		
 	private Location[] yanilleMinePath = {
 		new Location(2611, 3092),
 		new Location(2607, 3099),
 		new Location(2615, 3104),
 		new Location(2620, 3116),
 		new Location(2623, 3133),
		new Location(2626, 3146)
 	};
 	
 	private Area yanilleBank = new Area(new Location(2613, 3097), new Location(2609, 3088));
 	private Area yanilleMine = new Area(new Location(2629, 3153), new Location(2623, 3145));
 	
 	private long startTime;
 	
 	private String basketPNG = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhc" +
 			"mUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIvSURBVDjLpZNPiBJRHMffG6aZHcd1RNaYSspxSbFkWTpIh+iwVEpsFC1EYO2hQ9BlDx" +
 			"067L1b0KVDRQUa3jzWEughiDDDZRXbDtauU5QV205R6jo6at+3lNShKdgHH77zm9/f994MHQwGZCuLI1tctgVKpZJQLBYluxj6ty3" +
 			"M3V+alfvNG1Efzy03XGT9e3vu+rkD9/5rAiTPiGI/2RvZNrrSkmWL52ReGNw9f+3hzD8LIHmC9M2M4pHI2upbEqD18tdPnwmzOWJl" +
 			"pi/fmrAtcMrfnld5k+gvdeKTrcXT07FJxVovMHuMtsiUv3/xjzOoVCo3Lcs6DEi32xVAIBKJ0MzCY3My6BN1XSeqqpKnperGiamDU" +
 			"i6Xa3U6nTemaRLoGodEy+v1hlEsjBdXBEGg+Xz+2fgORazVamclSSLVavXMnjGHlM1m78iy7Gi321dDoVAYRQK0UCiMU0pfN5vNXS" +
 			"hggH2gDud21gloeNahO6EfoSr4Iopi3TCMBS4aja40Go1vmOA9Ao7DsYgORx0ORxkadzqdS9AYdBn+uKIoTI9omsa28GTzO3iEBeM" +
 			"CHGyCIPQDdDd0lU0AaswG7L0X52QAHbs/uXkL6HD7twnKrIPL5Sqyjm63m00U93g8JdjHoC9QJIYCdfB8+CWmUqkuHKMI8rPThQah" +
 			"r8BeUEWwBt4BFZ33g0vJZPIQ/+s+kcCDDQSTn1c0BElD2HXj0Emv13tg+y/YrUQiITBNp9OdH302kDq15BFkAAAAAElFTkSuQmCC";
 	
 	private String rubyPNG = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmU" +
 			"AQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAHiSURBVDjLzZPdS5NRHMf3Fwy67yYqepnzrSgJetEyl8s0GqvYEHLDFiGr4VwQZTcOpzfe" +
 			"VgQiCIJ4IWJUN+WNUhQqqAVjpg0c4vY8p7l89uLzfDpbV0FE4Y0HPpff7znf3+97TIBpJ5h2h4HxoNMlSUlEET0YEHrAL7Y77orCn" +
 			"dsi7/WIXGuryN64KbRrDrF1uTmZuXjJXTKQAqckw+tXMD0N8/MQjcLSEvq7tySHh1GGhkgPDqI+fUZ8YID44242z5zPpGtONxcNPv" +
 			"LiOUxMwOgoLC7C3ByMjaEHg8R9Pta8XhSPhw23m09NTXzp7SXqv4+oOhkvGlQYXYEPRiQM8jZWVymdqSl0h4M1u531xka+S5br6vj" +
 			"c08Oytx1RcSKhHqlylmYg8+6ReV/qT7phfBwSCZidxXC5SEqhKk1Um41YOMzXW15E+fGYcriy8rctFHzt5nxb2+R2KAQzM7CwgCGf" +
 			"LurryTQ0sNHXx4oUq2XHYsqhcusf15h1uc2a8/pkviv0y0DOIFNbS7a/n28dflRL9bpy0Gr9aw+2rrSYf9jsb3IPH2GMjFCIRFDuB" +
 			"ZB5NeVAWcs/FWnz3IW96VNno7lAJ5oUy7xaar/F+V9NFNU1Fpn3vcy7ktp39Oou/ws74Sc149q/X6rjygAAAABJRU5ErkJggg==";
 	
 	private String clockPNG = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcm" +
 			"UAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAMESURBVDjLXZNrSFNxGMYPgQQRfYv6EgR9kCgKohtFgRAVQUHQh24GQReqhViWlVYbZJl" +
 			"ZmZmombfVpJXTdHa3reM8uszmWpqnmQuX5drmLsdjenR7ev9DR3Xgd3h43+d5/pw/HA4AN9zITSPUhJ14R0xn87+h2ZzJvZVInJpz" +
 			"AQOXQOQMt+/5rvhMCLXv9Vjrt1rSXitmwj+Jua1+Ox+2HfGNdGf6yW8l5sUKPNVcRsiaPDA22Ahv6/7Ae/0aKdviQ0G7B/c6f8Zg+" +
 			"gbfh079Mjno0MhS58lflOsgEjh3BXc+bM/0DzbvDwj314znt/bjof0HdPw3FBq6kP+oCxVNfdDZvqPsrQmf6zdFRtyPJgbrFoqUTe" +
 			"S+FnPrekpmiC2lS+QcUx+qrf0wmFzodYfgC0nwhoYh9oegfdmLsmYXHj7JhV23erS7ZNYHyibGLiLtXsO19BoHSiwu6Ok09gwFg/g" +
 			"y8BO/STOkKFBk7EWh2YkLeh5Hy4Ws2B2w157iDvOpxw4UPRPRTSfL41FIsow7ZeXwUFF4dBQ1L96A/xLEFf1HMC/LxAt25PH+VN0H" +
 			"XH1gh2dEwdBoBGO0OKvW4L7hCdIvavBSsMIRVHCi0ArmZZl4wbYrz/yHSq1Ql9vQLylUEoE7GMal3OuxMG/7CO848N6n4HheK5iXZ" +
 			"eIFmy88Nu+8aYJG24G3ziB+0Ee7wwqemlvQ5w9hcAJwyUDtpwBOFLeBeVkmXpB0qlK9RV2HlLsCsvUivHRhQwoQjhCkA1TgJX1OK0" +
 			"JVzIN5WSZesPZ44XKia+P5BqSS4aq+BzZXABLdhyQrsJPOqv4MVcEbMA/zsky8gLHyYO7hI9laecOZWuzLfYXU2zzSblmQerMZqjw" +
 			"TknOeY9dlIw5kVcrMG/8XpoQgCEkOhwNNJn5i7bFSrFDpsCrFEIPpLacr0WxpibYIQpS86/8pMBqNswnJ6XSivqHBv3R3pmbxzgwz" +
 			"4Z+EaTXtwqIogrzjxIJ4QVVV1UyihxgjFv3/K09Bu/lEkBgg5rLZH+fT5dvfn7iFAAAAAElFTkSuQmCC";
 	
 	public void onStart() {
 		startTime = System.currentTimeMillis();
 	}
 	
 	public int loop() {
 		if(players.getCurrent().isInArea(yanilleMine) && !inventory.isFull()) {
 			log("Player is at the Yanille Mine");
 			Thing currentRock = things.getNearest(ironIDs);
 			
 			Location currentRockLocation = currentRock.getLocation();
 			
 			input.moveMouse(currentRock.getAbsLoc().X, currentRock.getAbsLoc().Y);
 			
 			if(currentRock.click()) {
 				boolean failsafe = false;
 				long failsafeTime = System.currentTimeMillis() + 4500;
 				while(!thingIdChanged(ironIDs, things.getAt(currentRockLocation).getID()) && !failsafe) {
 					if(failsafeTime <= System.currentTimeMillis()) failsafe = true;
 				}
 			}
 		}
 		
 		if(inventory.isFull() && !players.getCurrent().isInArea(yanilleBank)) {
 			walker.walkPathMM(Location.reversePath(yanilleMinePath));
 			
 			log("Walking to bank...");
 			return random(3000, 4000);
 		}
 		
 		if(!inventory.isFull() && !players.getCurrent().isInArea(yanilleMine)) {
 			walker.walkPathMM(yanilleMinePath);
 			
 			log("Walking to mine...");
 			return random(3000, 4000);
 		}
 		
 		if(inventory.isFull() && players.getCurrent().isInArea(yanilleBank) && !bank.isOpen()) {
 			Thing bankBooth = things.getNearest(bankBoothID);
 			
 			input.moveMouse(bankBooth.getAbsLoc().X, bankBooth.getAbsLoc().Y);
 			bankBooth.click("Quickly");
 			
 			boolean timeout = false;
 			
 			long later = System.currentTimeMillis() + 2500;
 			
 			while(!bank.isOpen() && !timeout) {
 				if(later <= System.currentTimeMillis()) timeout = true;
 			}
 			
 			return random(500, 1000);
 		}
 		
 		if(inventory.isFull() && bank.isOpen()) {
 			bank.depositAll();
 			
 			return random(1000, 1500);
 		}
 		
 		return 1;
 	}
 	
 	private boolean thingIdChanged(int[] ids, int id) {
 		for (int i : ids) if(i == id) return false;
 		
 		return true;
 	}
 	
 	public void serverMessageReceived(String message) {
 		if(message.contains("iron")) minedIron++;
 		if(message.contains("sapphire")) minedGems++;
 		if(message.contains("emerald")) minedGems++;
 		if(message.contains("ruby")) minedGems++;
 		if(message.contains("diamond")) minedGems++;
 		
 		return ;
 	}
 	
 	public void onStop() {
 		return ;
 	}
 	
 	public void paint(Graphics g2) {
 		if(!players.getCurrent().isLoggedIn() || players.getCurrent().isInLobby()) return ;
 		
 		Graphics2D g = (Graphics2D)g2;
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		//Rectangles
 		RoundRectangle2D clockBackground = new RoundRectangle2D.Float(
 				interfaces.getMinimap().getRealX() - 144,
 				20,
 				89,
 				26,
 				5,
 				5);
 		
 		RoundRectangle2D scoreboardBackground = new RoundRectangle2D.Float(
 				20,
 				20,
 				89,
 				47,
 				5,
 				5);
 		
 		g.setColor(new Color(0, 0, 0, 127));
 		g.fill(clockBackground);
 		g.fill(scoreboardBackground);
 		
 		//Text
 		g.setColor(Color.white);
 		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 		
 		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
 		
 		g.drawString(nf.format(minedIron), 48, 39);
 		g.drawString(nf.format(minedGems), 48, 58);
 		
 		g.drawString(millisToClock(System.currentTimeMillis() - startTime), interfaces.getMinimap().getRealX() - 139, 37);
 		
 		//Images
 		try {
 			byte[] basketBytes = new BASE64Decoder().decodeBuffer(basketPNG);
 			byte[] rubyBytes = new BASE64Decoder().decodeBuffer(rubyPNG);
 			byte[] clockBytes = new BASE64Decoder().decodeBuffer(clockPNG);
 			
 			InputStream stream = new ByteArrayInputStream(basketBytes);
 			Image basketImage = ImageIO.read(stream);
 			
 			stream = new ByteArrayInputStream(rubyBytes);
 			Image rubyImage = ImageIO.read(stream);
 			
 			stream = new ByteArrayInputStream(clockBytes);
 			Image clockImage = ImageIO.read(stream);
 			
 			ImageObserver observer = null;
 			g.drawImage(basketImage, 25, 25, observer);
 			g.drawImage(rubyImage, 25, 25 + 16 + 4, observer);
 			g.drawImage(clockImage, interfaces.getMinimap().getRealX() - 75, 25, observer);
 		} catch(IOException e) {
 			log(e.getMessage());
 		}
 		
 		return ;
 	}
 	
 	private String millisToClock(long milliseconds) {
 		long seconds = (milliseconds / 1000), minutes = 0, hours = 0;
 		
 		if (seconds >= 60) {
 			minutes = (seconds / 60);
 			seconds -= (minutes * 60);
 		}
 		
 		if (minutes >= 60) {
 			hours = (minutes / 60);
 			minutes -= (hours * 60);
 		}
 		
 		return (hours < 10 ? "0" + hours + ":" : hours + ":")
 				+ (minutes < 10 ? "0" + minutes + ":" : minutes + ":")
 				+ (seconds < 10 ? "0" + seconds : seconds);
 	}
 }
