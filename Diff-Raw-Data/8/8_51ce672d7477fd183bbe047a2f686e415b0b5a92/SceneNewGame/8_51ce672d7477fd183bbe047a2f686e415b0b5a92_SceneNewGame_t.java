 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.game.world.Map;
 import com.vloxlands.gen.MapGenerator;
 import com.vloxlands.net.Server;
 import com.vloxlands.net.packet.Packet;
 import com.vloxlands.net.packet.Packet1Disconnect;
 import com.vloxlands.net.packet.Packet3ChatMessage;
 import com.vloxlands.net.packet.Packet4ServerInfo;
 import com.vloxlands.net.packet.Packet6Ready;
 import com.vloxlands.net.packet.Packet7Settings;
 import com.vloxlands.net.packet.Packet8Loading;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.Action;
 import com.vloxlands.ui.ChatContainer;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.Dialog;
 import com.vloxlands.ui.GuiRotation;
 import com.vloxlands.ui.IGuiElement;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.InputField;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.LobbySlot;
 import com.vloxlands.ui.ProgressBar;
 import com.vloxlands.ui.Spinner;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.RenderAssistant;
 
 public class SceneNewGame extends Scene
 {
 	ProgressBar progress;
 	TextButton start;
 	Spinner xSize, zSize, radius;
 	static Container lobby;
 	static ChatContainer chat;
 	
 	@Override
 	public void init()
 	{
 		if (Game.server == null && !Game.client.isConnected())
 		{
 			Game.server = new Server(Game.IP); // host
 		}
 		
 		Game.currentMap = new Map();
 		
 		if (!Game.client.isConnected()) Game.client.connectToServer(Game.IP);
 		
 		setBackground();
 		
 		setTitle(Tr._("newGame"));
 		
 		if (lobby == null)
 		{
 			lobby = new Container(0, 115, Display.getWidth() - TextButton.WIDTH - 90, Display.getHeight() - 220);
 		}
 		lobby.setX(0);
 		lobby.setY(115);
 		lobby.setWidth(Display.getWidth() - TextButton.WIDTH - 90);
 		lobby.setHeight(Display.getHeight() - 220);
 		content.add(lobby);
 		
 		if (chat == null)
 		{
 			chat = new ChatContainer(0, 115 + Display.getHeight() - 220 - TextButton.HEIGHT - 150, Display.getWidth() - TextButton.WIDTH - 90, TextButton.HEIGHT + 150);
 		}
 		chat.setX(0);
 		chat.setY(115 + Display.getHeight() - 220 - TextButton.HEIGHT - 150);
 		chat.setWidth(Display.getWidth() - TextButton.WIDTH - 90);
 		chat.setHeight(TextButton.HEIGHT + 150);
 		content.add(chat);
 		
 		final InputField chatInput = new InputField(5, Display.getHeight() - 115 - InputField.HEIGHT - 15, Display.getWidth() - TextButton.WIDTH - 120, "", "");
 		chatInput.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				String msg = chatInput.getText();
 				if (msg.length() > 0)
 				{
 					try
 					{
 						Game.client.sendPacket(new Packet3ChatMessage(Game.client.getUsername(), msg));
 						chatInput.setText("");
 					}
 					catch (IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		content.add(chatInput);
 		
 		content.add(new Container(Display.getWidth() - TextButton.WIDTH - 90, 115, TextButton.WIDTH + 90, Display.getHeight() - 220));
 		
 		progress = new ProgressBar(Display.getWidth() / 2, Display.getHeight() / 2 - ProgressBar.HEIGHT / 2, 400, 0, false);
 		progress.setVisible(false);
 		content.add(progress);
 		
 		TextButton disco = new TextButton(Display.getWidth() / 2 - TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("disconnect"));
 		disco.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.client.disconnect();
 				Game.currentGame.setScene(new SceneMainMenu());
 			}
 		});
 		content.add(disco);
 		
 		TextButton back = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("back"));
 		back.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.currentGame.setScene(new SceneMainMenu());
 			}
 		});
 		content.add(back);
 		
 		start = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH, Display.getHeight() - TextButton.HEIGHT, Tr._("start"));
 		start.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.server.setMapGenerator(new MapGenerator(xSize.getValue(), zSize.getValue(), 20, 48));
 			}
 		});
 		if (Game.client.isConnectedToLocalhost())
 		{
 			start.setEnabled(true);
 			back.setX(Display.getWidth() / 2 - TextButton.WIDTH / 2);
 			disco.setX((int) (Display.getWidth() / 2 - TextButton.WIDTH * 1.5f));
 			content.add(start);
 		}
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 130, (TextButton.WIDTH + 70) / 2, 25, "X-" + Tr._("islands") + ":", false));
		xSize = new Spinner(Display.getWidth() - TextButton.WIDTH - 80 + (TextButton.WIDTH + 70) / 2, 125, (TextButton.WIDTH + 70) / 2, 1, 10, 1, 1, GuiRotation.HORIZONTAL);
 		xSize.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				try
 				{
 					Game.client.sendPacket(new Packet7Settings("xSize", xSize.getValue() + ""));
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 		xSize.setEnabled(Game.client.isConnectedToLocalhost());
 		content.add(xSize);
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 175, (TextButton.WIDTH + 70) / 2, 25, "Z-" + Tr._("islands") + ":", false));
		zSize = new Spinner(Display.getWidth() - TextButton.WIDTH - 80 + (TextButton.WIDTH + 70) / 2, 170, (TextButton.WIDTH + 70) / 2, 1, 10, 1, 1, GuiRotation.HORIZONTAL);
 		zSize.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				try
 				{
 					Game.client.sendPacket(new Packet7Settings("zSize", zSize.getValue() + ""));
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 		zSize.setEnabled(Game.client.isConnectedToLocalhost());
 		content.add(zSize);
 		
 		try
 		{
 			Game.client.sendPacket(new Packet4ServerInfo());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void updateLobby(String[] pl)
 	{
 		ArrayList<String> players = new ArrayList<>();
 		for (String p : pl)
 			players.add(p);
 		
 		@SuppressWarnings("unchecked")
 		ArrayList<IGuiElement> lobbyCopy = (ArrayList<IGuiElement>) lobby.components.clone();
 		lobby.components.clear();
 		
 		int index = 0;
 		for (IGuiElement iG : lobbyCopy)
 		{
 			if (players.indexOf(((LobbySlot) iG).getUsername()) > -1)
 			{
 				iG.setY(15 + index * LobbySlot.HEIGHT2);
 				iG.setWidth(lobby.getWidth() - 20);
 				iG.setX(10);
 				lobby.add(iG);
 				players.remove(((LobbySlot) iG).getUsername());
 				index++;
 			}
 		}
 		for (int i = 0; i < players.size(); i++)
 		{
 			LobbySlot slot = new LobbySlot(players.get(i));
 			slot.parent = lobby;
 			slot.setX(10);
 			slot.setY(15 + index * LobbySlot.HEIGHT2 + i * LobbySlot.HEIGHT2);
 			slot.setWidth(lobby.getWidth() - 20);
 			lobby.add(slot);
 		}
 		
 		for (IGuiElement iG : lobby.components)
 		{
 			LobbySlot slot = (LobbySlot) iG;
 			
 			slot.initButtons();
 			slot.components.get(0).setEnabled(slot.getUsername().equals(Game.client.getUsername())); // rename
 			slot.components.get(1).setEnabled(Game.client.isConnectedToLocalhost() && !slot.getUsername().equals(Game.client.getUsername())); // kick
 			slot.components.get(2).setEnabled(slot.getUsername().equals(Game.client.getUsername())); // ready
 		}
 	}
 	
 	@Override
 	public void render()
 	{
 		super.render();
 		
 		if (!chat.isEnabled())
 		{
 			glEnable(GL_BLEND);
 			glColor4f(IGuiElement.gray.x, IGuiElement.gray.y, IGuiElement.gray.z, IGuiElement.gray.w);
 			glBindTexture(GL_TEXTURE_2D, 0);
 			RenderAssistant.renderRect(0, 0, Display.getWidth(), Display.getHeight());
 			glColor4f(1, 1, 1, 1);
 			progress.render();
 			glDisable(GL_BLEND);
 		}
 		
		if (Game.currentMap.islands.size() == xSize.getValue() * zSize.getValue())
 		{
 			progress.title = Tr._("renderchunks");
 			Game.currentMap.initMap();
 		}
 		
 		if (Game.currentMap.initialized) Game.currentGame.setScene(new SceneGame());
 	}
 	
 	@Override
 	public void onClientMessage(String message)
 	{
 		String type = message.substring(0, message.indexOf("$"));
 		Vector3f color = new Vector3f(1, 1, 1);
 		switch (type)
 		{
 			case "INFO":
 			{
 				color = new Vector3f(0.9f, 0.9f, 0);
 				break;
 			}
 		}
 		chat.addMessage(message.substring(message.indexOf("$") + 1), color);
 	}
 	
 	@Override
 	public void onClientReveivedPacket(Packet packet)
 	{
 		switch (packet.getType())
 		{
 			case DISCONNECT:
 			{
 				Packet1Disconnect p = (Packet1Disconnect) packet;
 				if (p.getUsername().equals(Game.client.getUsername()))
 				{
 					if (!p.getReason().equals("mp.disconnect"))
 					{
 						Game.currentGame.addScene(new Dialog(Tr._("info"), Tr._(p.getReason()), new Action(Tr._("close"), new IGuiEvent()
 						{
 							@Override
 							public void trigger()
 							{
 								Game.currentGame.setScene(new SceneMainMenu());
 							}
 						})));
 					}
 				}
 				try
 				{
 					Game.client.sendPacket(new Packet4ServerInfo());
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 				break;
 			}
 			case RENAME:
 			{
 				try
 				{
 					Game.client.sendPacket(new Packet4ServerInfo());
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 				break;
 			}
 			case SERVERINFO:
 			{
 				updateLobby(((Packet4ServerInfo) packet).getPlayers());
 				break;
 			}
 			case READY:
 			{
 				Packet6Ready p = (Packet6Ready) packet;
 				for (IGuiElement iG : lobby.components)
 				{
 					LobbySlot slot = (LobbySlot) iG;
 					if (slot.getUsername().equals(p.getUsername()))
 					{
 						TextButton tb = (TextButton) slot.components.get(2);
 						tb.setActive(p.getReady());
 						if (p.getReady()) tb.textColor = new Vector3f(124 / 256f, 222 / 256f, 106 / 256f);
 						else tb.textColor = new Vector3f(1, 1, 1);
 						
 						if (Game.client.isConnectedToLocalhost()) start.setEnabled(Game.server.areAllClientsReady());
 						break;
 					}
 				}
 				break;
 			}
 			case SETTINGS:
 			{
 				Packet7Settings p = (Packet7Settings) packet;
 				switch (p.getKey())
 				{
 					case "xSize":
 					{
 						xSize.setValue(Integer.parseInt(p.getValue()));
 						break;
 					}
 					case "zSize":
 					{
 						zSize.setValue(Integer.parseInt(p.getValue()));
 						break;
 					}
 				}
 				break;
 			}
 			case LOADING:
 			{
 				Packet8Loading p = (Packet8Loading) packet;
 				lockScene();
 				progress.setValue(p.getPercentage());
 				progress.title = Tr._(p.getAction());
 				break;
 			}
 			default:
 				break;
 		}
 	}
 }
