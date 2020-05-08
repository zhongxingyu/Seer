 package gfx;
 
 import gfx.GBC.Align;
 import gfx.MenuField.FieldType;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import blueMaggot.Game;
 import blueMaggot.GameState;
 
 public class MenuOptionsLan extends Menu {
 	private Dimension btnSize = new Dimension(180, 20);
 	private int width;
 
 	private MenuField fieldLanNick;
 	private MenuField fieldIp;
 
 	private MenuButton btnStartLan;
 	private MenuButton btnReturn;
 
 	private MenuLabel playerOne;
 	private MenuLabel connectIp;
 	private MenuLabel isHost;
 	private MenuLabel yourIp;
 
 	private MenuCheckBox boxIsHost;
 
 	private drawCustomText showIp;
 
 	private class drawCustomText extends Canvas {
 		String text;
 
 		drawCustomText(String text) {
 			super.repaint();
 			this.text = text;
 			System.out.println(btnSize);
 			setPreferredSize(btnSize);
 			setMaximumSize(btnSize);
 			setMinimumSize(btnSize);
 			setVisible(true);
 		}
 
 		@Override
 		public void paint(Graphics g) {
 			// g.setColor(Color.yellow);
 			// g.fillRect(0, 0, getWidth(), getHeight());
 
 			g.setColor(Color.black);
 			g.setFont(Menu.font);
 			g.drawString(text, 0, 17);
 		}
 	}
 
 	public String getLocalIp() {
 		String ip = "127.0.0.1";
 		try {
 			ip = InetAddress.getLocalHost().getHostAddress();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		System.out.println("local ip: " + ip);
 		return ip;
 	}
 
 	public MenuOptionsLan(Game game) {
 		super();
 
 		setVisible(false);
 
 		fieldIp = new MenuField(15, FieldType.IP);
 		fieldLanNick = new MenuField(15, FieldType.TEXT);
 
 		btnStartLan = new MenuButton(Labels.START_LAN_GAME, this, game, btnSize);
 		btnReturn = new MenuButton(Labels.RETURN, this, game, btnSize);
 
 		playerOne = new MenuLabel(Labels.LAN_NICK);
 		connectIp = new MenuLabel(Labels.CONNECT_IP);
 		isHost = new MenuLabel(Labels.IS_HOST);
 
 		boxIsHost = new MenuCheckBox();
 		showIp = new drawCustomText(getLocalIp());
 		yourIp = new MenuLabel(Labels.YOUR_IP);
 
 		// left column
 		add(playerOne, new GBC(0, 0, Align.RIGHT));
 		add(connectIp, new GBC(0, 2, Align.RIGHT));
 		add(isHost, new GBC(0, 3, Align.RIGHT));
 		add(boxIsHost, new GBC(1, 3, Align.LEFT));
 		add(yourIp, new GBC(0, 4, Align.RIGHT));
 		add(showIp, new GBC(1, 4, Align.LEFT));
 
 		// right column
 		add(fieldLanNick, new GBC(1, 0, Align.LEFT));
 		add(fieldIp, new GBC(1, 2, Align.LEFT));
 		add(btnStartLan, new GBC(0, 5, Align.RIGHT));
 		add(btnReturn, new GBC(1, 5, Align.LEFT));
 	}
 
 	public void apply(Game game) {
 		GameState.getInstance().setHost(boxIsHost.getState());
 	
 		if (fieldLanNick.msg != null) {
 			if (GameState.getInstance().isHost())
				GameState.getInstance().setPlayer2Nick(fieldLanNick.msg);
			else
 				GameState.getInstance().setPlayer1Nick(fieldLanNick.msg);
 		}
 		GameState.getInstance().hostIp = fieldIp.msg;
 	}
 }
