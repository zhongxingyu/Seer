 package org.frantictools.franticmod;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.frantictools.franticapi.FranticMeAPI;
 import org.frantictools.franticmod.gui.GuiFModMenu;
 import org.frantictools.franticmod.gui.GuiLogin;
 import org.lwjgl.input.Keyboard;
 import org.yaml.snakeyaml.Yaml;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.GuiControls;
 import net.minecraft.src.GuiScreen;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.KeyBinding;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.Packet;
 import net.minecraft.src.ScaledResolution;
 import net.minecraft.src.Session;
 import net.minecraft.src.WorldRenderer;
 import net.minecraft.src.mod_FranticMod;
 
 @SuppressWarnings("unused")
 public class FranticMod {
 
 	public Minecraft mc = ModLoader.getMinecraftInstance();
 	private mod_FranticMod fMod;
 	
 	
 	public static FranticMeAPI fm;
 	
 	private long ticks;
 	
 	public static String username, password;
 	public static boolean loggedIn;
 	
 	private KeyBinding keyBindOpenMenu = new KeyBinding("key.fmodmenu", Keyboard.KEY_BACK);
 //	private KeyBinding keyTestChat     = new KeyBinding("key.chattest", Keyboard.KEY_I);
 
 	public static ArrayList<String> userList = new ArrayList<String>();
 	public FranticMod(mod_FranticMod mod) {
 		this.fMod = mod;
 	}
 
 	public void Initialize() {
 		ModLoader.registerKey(fMod, keyBindOpenMenu, false);
 //		ModLoader.registerKey(fMod, keyTestChat, false);
 		getUsers();
 
 		ModLoader.addLocalization("key.fmodmenu", "Open FMod Menu");
 		
 	}
 
 	public void processTick(float tickNumber, Minecraft mc) {
 		ticks++;
 		if (mc.currentScreen == null && !mc.gameSettings.showDebugInfo /*&& mc.theWorld.isRemote*/)
 		{
 			mc.fontRenderer.drawStringWithShadow("FranticMod v0.1b", 2, 2, 0xFFFFFF);
 			mc.fontRenderer.drawStringWithShadow("You are not vanished", 2, 12, 0x00FF00);
 		}
 		
 		if (ticks % 80 == 0)
 		{
 			//checkVanished();
 		}
 	}
 	
 	public void processGuiTick(float f, Minecraft minecraft, GuiScreen guiscreen) {
 
 	}
 	
 	public void processKeybind(KeyBinding event) {
 		if (mc.currentScreen == null) 
 		{
 			if (event == this.keyBindOpenMenu) {
 				if (loggedIn)
 					ModLoader.openGUI(mc.thePlayer, new GuiFModMenu());
 				else
 					ModLoader.openGUI(mc.thePlayer, new GuiLogin());
 			}
 		
			if (event == this.keyTestChat) {
				mc.thePlayer.addChatMessage("[g] RPGMASTER200: bleh http://google.com bleh");
			}
 		}
 	}
 	
 	protected void checkVanished() 
 	{
 		if (mc.theWorld.isRemote)
 			mc.thePlayer.sendChatMessage("/vanish check");
 	}
 
 	public void getUsers()
 	{	
 		userList.add("RPGMASTER200");
 	}
 
 	private static String getResult(HttpResponse response) throws IllegalStateException, IOException
 	{
 		InputStream input = response.getEntity().getContent();
 		InputStreamReader isr = new InputStreamReader(input);
 		BufferedReader br = new BufferedReader(isr);
 
 		String line = "";
 
 		String rawData = "";
 		while ((line = br.readLine()) != null)
 		{
 			rawData = rawData + line + "\r\n";
 		}
 		return rawData;
 	}
 }
