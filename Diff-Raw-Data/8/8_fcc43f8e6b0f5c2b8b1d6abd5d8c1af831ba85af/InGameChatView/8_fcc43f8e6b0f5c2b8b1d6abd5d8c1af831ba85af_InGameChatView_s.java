 package com.evervoid.client.views;
 
 import com.evervoid.client.EVFrameManager;
 import com.evervoid.client.KeyboardKey;
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.graphics.GraphicsUtils;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.interfaces.EVFrameObserver;
 import com.evervoid.client.ui.chat.ChatControl;
 import com.evervoid.state.Color;
 import com.evervoid.state.geometry.Dimension;
 
 public class InGameChatView extends EverView implements EVFrameObserver
 {
 	private static final float sChatCloseTimeout = 4;
 	static final Dimension sChatDimension = new Dimension(512, 384);
 	private final ChatControl aChatControl;
 	private final AnimatedAlpha aChatControlAlpha;
 	private boolean aChatControlFocused = false;
 	private boolean aChatControlOpen = false;
 	private float aChatTimeout = 0f;
 
 	public InGameChatView()
 	{
 		aChatControl = new ChatControl();
 		aChatControlAlpha = aChatControl.getNewAlphaAnimation();
 		aChatControlAlpha.setDuration(0.5f).setAlpha(0f).translate(0, 0, 10000); // Bring to front
 		addNode(aChatControl);
 		resolutionChanged();
 	}
 
 	private void closeChat()
 	{
 		aChatControlAlpha.setTargetAlpha(0).start();
 		aChatControl.defocus();
 		aChatControlOpen = false;
 		aChatControlFocused = false;
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		aChatTimeout += f.aTpf;
 		if (aChatTimeout >= sChatCloseTimeout) {
 			closeChat();
 			aChatTimeout = 0;
 			EVFrameManager.deregister(this);
 		}
 	}
 
 	@Override
 	public boolean onKeyPress(final KeyboardKey key, final float tpf)
 	{
 		if (!aChatControlOpen && key.equals(KeyboardKey.T)) {
 			openChat(true);
 			return true;
 		}
 		if (aChatControlOpen && key.equals(KeyboardKey.ESCAPE)) {
 			closeChat();
 			return true;
 		}
 		if (aChatControlFocused) {
 			aChatControl.onKeyPress(key);
 			return true;
 		}
 		return super.onKeyPress(key, tpf);
 	}
 
 	private void openChat(final boolean focused)
 	{
 		aChatControlAlpha.setTargetAlpha(0.7f).start();
 		aChatControlOpen = true;
		aChatControlFocused = true;
		aChatControl.focus();
 	}
 
 	public void receivedChat(final String player, final Color playerColor, final String message)
 	{
 		aChatControl.messageReceived(player, GraphicsUtils.getColorRGBA(playerColor), message);
 		if (!aChatControlFocused) {
 			aChatTimeout = 0;
 			if (!aChatControlOpen) {
 				EVFrameManager.register(this);
 				openChat(false);
 			}
 		}
 	}
 
 	@Override
 	public void setBounds(final Bounds pBounds)
 	{
 		super.setBounds(pBounds);
 		aChatControl.setBounds(pBounds);
 	}
 }
