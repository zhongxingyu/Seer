 package jig.ironLegends.screens;
 
 import jig.engine.Mouse;
 import jig.engine.RenderingContext;
 import jig.engine.Sprite;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.IronLegends;
 import jig.ironLegends.PlayerInfo;
 import jig.ironLegends.core.Fonts;
 import jig.ironLegends.core.GameScreen;
 import jig.ironLegends.core.KeyCommands;
 import jig.ironLegends.core.TextWriter;
 import jig.ironLegends.core.ui.RolloverButton;
 import jig.ironLegends.core.ui.TextEditBox;
 import jig.ironLegends.messages.SPStartGame;
 import jig.ironLegends.oxide.packets.ILPacket;
 import jig.ironLegends.oxide.packets.ILPacketFactory;
 import jig.ironLegends.oxide.packets.ILStartGamePacket;
 import jig.ironLegends.router.ClientContext;
 import jig.ironLegends.router.ServerContext;
 import jig.ironLegends.router.SinglePlayerMsgTransport;
 
 /**
  * A splash screen class with the corresponding button "links" and rollovers.
  * @author Travis Hall
  */
 public class SplashScreen extends GameScreen {
 	protected PlayerInfo m_playerInfo;
 	
 	protected Sprite bg;
 	protected Sprite header;
 	protected Sprite csbox;
 	protected Sprite banner;
 	protected RolloverButton hbutton;
 	protected RolloverButton mbutton;
 	protected RolloverButton sbutton;
 	
 	protected TextEditBox csEntryBox;
 	protected IronLegends m_game;
 	protected Fonts m_fonts;
 	
 	public SplashScreen(int name, Fonts fonts, PlayerInfo playerInfo, IronLegends game) {
 		super(name);
 		
 		m_playerInfo = playerInfo;
 		m_game = game;
 		m_fonts = fonts;
 		
 		bg = new Sprite(IronLegends.SCREEN_SPRITE_SHEET + "#background");
 		bg.setPosition(new Vector2D(0, 0));
 		
 		header = new Sprite(IronLegends.SCREEN_SPRITE_SHEET + "#header");
 		header.setPosition(new Vector2D(0, 0));
 		
 		csbox = new Sprite(IronLegends.SCREEN_SPRITE_SHEET + "#csbox");
 		csbox.setPosition(new Vector2D(266, 300));
 		
 		banner = new Sprite(IronLegends.SCREEN_SPRITE_SHEET + "#banner");
 		banner.setPosition(new Vector2D(34, 0));
 
 		sbutton = new RolloverButton(-3, 0, 447,IronLegends.SCREEN_SPRITE_SHEET + "#sp-button");
 		mbutton = new RolloverButton(-2, 0, 491,IronLegends.SCREEN_SPRITE_SHEET + "#mp-button");
 		hbutton = new RolloverButton(-1, 0, 535,IronLegends.SCREEN_SPRITE_SHEET + "#help-button");
 		
 		csEntryBox = new TextEditBox(fonts.textFont, -4, 292, 360, 
 				IronLegends.SCREEN_SPRITE_SHEET + "#csshader");
 		csEntryBox.setText(playerInfo.getName().toUpperCase());
 	}
 	
 	@Override
 	public void render(RenderingContext rc) {
 		bg.render(rc);
 		banner.render(rc);
 		header.render(rc);
 		csbox.render(rc);
 		
 		sbutton.render(rc);
 		mbutton.render(rc);
 		hbutton.render(rc);
 		
 		csEntryBox.render(rc);
 		
 		TextWriter text = new TextWriter(rc);
 		text.setFont(m_fonts.textFont);
 		text.setY(IronLegends.SCREEN_HEIGHT - 100);
 		text.setLineStart(-1);
 		text.println("by");
 		
 		text.setFont(m_fonts.lgTextFont);
 		text.println("Travis Hall");
 		text.println("Bhadresh Patel");
 		text.println("Michael Persons");
 		
 		text.setFont(m_fonts.textFont);
 		text.setY(IronLegends.SCREEN_HEIGHT - 25);
 		text.println("Washington State University Vancouver");
 	}
 	
 	@Override
 	public int processCommands(KeyCommands keyCmds, Mouse mouse, final long deltaMs)
 	{
 		hbutton.update(mouse, deltaMs);
 		if (hbutton.wasLeftClicked())
 			return IronLegends.HELP_SCREEN;
 		mbutton.update(mouse, deltaMs);
 		if (mbutton.wasLeftClicked()) {
 			this.m_game.client.setActive(true);
 			this.m_game.client.setLookingForServers(true);
 			return IronLegends.SERVER_SCREEN;
 		}
 		sbutton.update(mouse, deltaMs);
 		if (sbutton.wasLeftClicked())
 		{
 			if (m_game.m_server == null)
 			{
 				// single player, so launch server on this client
 				m_game.m_server = new ServerContext();
 				// send message to server to "start game"
 				//(probably should have "connection logic" first
 				m_game.m_client = new ClientContext();
 				// transport of messages can just be queue movement for single player
 				m_game.m_clientMsgTransport = new SinglePlayerMsgTransport(m_game.m_server.getRxQueue(), m_game.m_client.getRxQueue());
 				m_game.m_serverMsgTransport = new SinglePlayerMsgTransport(m_game.m_client.getRxQueue(), m_game.m_server.getRxQueue());
 
 				String sSelectedMap = "mapitems.txt";
 				int packetId = this.m_game.client.packetID();
 				
 				ILStartGamePacket startGamePacket = ILPacketFactory.newStartGamePacket(packetId
						//, m_game.client.hostAddress.getHostAddress()+"\0"
						, "garbage" + "\0"
						, m_game.client.myAddress.getHostAddress() + "\0");
 				
 				startGamePacket.map = sSelectedMap;
 				startGamePacket.m_bSinglePlayer = true;
 				
 				startGamePacket.addPlayer("FixMyName", 0);
 				/*
 				for (int i = 0; i < 4; ++i)
 				{
 					startGamePacket.addPlayer("AI-" + (i+1), i+1);
 				}
 				*/
 				
 				// TODO: single player, name ai tanks as ai-1 .. 
 				
 				//SPStartGame msg = new SPStartGame(sSelectedMap);
 				//m_game.m_client.send(msg);
 				m_game.m_client.send(startGamePacket);
 			}
 			
 			// TODO Push this to the correct "Lobby" screen
 			//return IronLegends.GAMEPLAY_SCREEN;
 			return name();
 		}
 		
 		csEntryBox.update(mouse, deltaMs);
 		if (csEntryBox.isActive())
 		{
 			csEntryBox.processInput(keyCmds);
 			m_playerInfo.setName(csEntryBox.getText().toUpperCase());
 		}
 		
 		return name();		
 	}
 	
 	@Override
 	public void activate(int prevScreen)
 	{
 		super.activate(prevScreen);
 	}
 }
