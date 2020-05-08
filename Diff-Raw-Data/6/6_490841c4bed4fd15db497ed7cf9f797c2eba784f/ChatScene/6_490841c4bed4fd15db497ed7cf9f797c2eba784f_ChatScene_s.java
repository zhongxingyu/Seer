 /**
  * Copyright (c) 2009, Coral Reef Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of the Coral Reef Project nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package pl.graniec.coralreef.network.demo.chat;
 
 
 import pulpcore.CoreSystem;
 import pulpcore.Input;
 import pulpcore.animation.Color;
 import pulpcore.animation.Easing;
 import pulpcore.animation.Timeline;
 import pulpcore.animation.event.TimelineEvent;
 import pulpcore.image.BlendMode;
 import pulpcore.image.CoreFont;
 import pulpcore.scene.Scene2D;
 import pulpcore.sprite.FilledSprite;
 import pulpcore.sprite.ImageSprite;
 import pulpcore.sprite.Label;
 import pulpcore.sprite.ScrollPane;
 import pulpcore.sprite.Sprite;
 import pulpcore.sprite.TextField;
 
 /**
  * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
  *
  */
 public class ChatScene extends Scene2D {
 	
 	private static final String BG_RESOURCE = "chat/bg.png";
 	private static final String FRAMES_RESOURCE = "chat/frames.png";
 	private static final String QUESTION_RESOURCE = "chat/nick_question.png";
 	
 	private ImageSprite background;
 	private ImageSprite frames;
 	
 	final private Timeline timeline = new Timeline();
 	
 	int chatLines = 0;
 	private ScrollPane chatScroll;
 	private TextField input;
 	private TextField nickInput;
 	
 	private String nick;
 	private Label nickLabel;
 	private ImageSprite nickQuestionFrame;
 	
 	@Override
 	public void load() {
 		background = new ImageSprite(BG_RESOURCE, 0, 0);
 		add(background);
 		
 		frames = new ImageSprite(FRAMES_RESOURCE, 0, 0);
 		frames.alpha.set(0);
 		add(frames);
 		
 		chatScroll = new ScrollPane(20, 20, 450, 400);
 		chatScroll.alpha.set(0);
 		add(chatScroll);
 		
 		systemMessage("CoralReef Chat Demo");
 		systemMessage("cr-network + cr-chat + cr-pulpcore-desktop");
 		
 		input = new TextField(
 				CoreFont.getSystemFont().tint(0xFFFFFF),
 				CoreFont.getSystemFont().tint(0xFF0000),
 				"",
 				20, 460, 450, CoreFont.getSystemFont().getHeight()
 		);
 		
 		input.caretColor.set(0xFFFFFFFF);
 		input.setAnchor(Sprite.SOUTH_WEST);
 		
 		add(input);
 		
 		
 		nickQuestionFrame = new ImageSprite(QUESTION_RESOURCE, 0, 0);
 		
 		nickQuestionFrame.x.set((640 - nickQuestionFrame.width.get()) / 2);
 		nickQuestionFrame.y.set((480 - nickQuestionFrame.height.get()) / 2);
 		
 		nickQuestionFrame.alpha.set(0);
 		add(nickQuestionFrame);
 		
 		nickInput = new TextField(
 				CoreFont.getSystemFont().tint(0xFFFFFF),
 				CoreFont.getSystemFont().tint(0xFF0000),
 				"",
 				nickQuestionFrame.x.get() + 30, nickQuestionFrame.x.get() + 87,
 				236, CoreFont.getSystemFont().getHeight()
 		);
 		
 		nickInput.caretColor.set(0xFFFFFFFF);
 		nickInput.setAnchor(Sprite.SOUTH_WEST);
 		
 		add(nickInput);
 		
 		nickLabel = new Label(
 				CoreFont.getSystemFont().tint(0xFFFFFFFF),
 				"Tell me what's your name?",
 				nickQuestionFrame.x.get() + nickQuestionFrame.width.get() / 2,
 				nickQuestionFrame.y.get() + 20
 		);
 		
 		nickLabel.setAnchor(Label.NORTH);
 		nickLabel.alpha.set(0);
 		
 		add(nickLabel);
 		
 		nickInput.setFocus(true);
 		
 		timeline.animate(background.alpha, 0, 255, 1000);
 		timeline.animate(frames.alpha, 0, 255, 500, Easing.NONE, 0);
 		timeline.animate(chatScroll.alpha, 0, 255, 500, Easing.NONE, 500);
 		timeline.animate(nickQuestionFrame.alpha, 0, 255, 500, Easing.NONE, 1000);
 		timeline.animate(nickLabel.alpha, 0, 255, 500, Easing.NONE, 1000);
 		
 		
 		timeline.play();
 		
 	}
 	
 	@Override
 	public void update(int elapsedTime) {
 		timeline.update(elapsedTime);
 		
 		if(Input.isPressed(Input.KEY_ENTER)) {
 			
 			if (nick == null && !nickInput.getText().isEmpty()) {
 				
 				nick = nickInput.getText();
 				nickLabel.setText("Hello " + nick + "!");
 				
				timeline.animate(nickQuestionFrame.alpha, 255, 0, 500, Easing.STRONG_OUT, timeline.getTime()+700);
				timeline.animate(nickLabel.alpha, 255, 0, 500, Easing.STRONG_OUT, timeline.getTime()+700);
				timeline.animate(nickInput.alpha, 255, 0, 500, Easing.STRONG_OUT, timeline.getTime()+700);
 				
 				nickInput.enabled.set(false);
 				input.setFocus(true);
 				
 				systemMessage("Connecting to chat server...");
 				
 			} else if (!input.getText().isEmpty()) {
 				
 				addChatLine(input.getText(), 0xFFFFFFFF);
 				input.setText("");
 				
 			}
 		}
 	}
 	
 	public void systemMessage(String text) {
 		addChatLine(">> " + text, 0xFFFF9E9E);
 	}
 	
 	public void addChatLine(String text, int color) {
 		Label label = new Label(CoreFont.getSystemFont().tint(color), text, 0, 0);
 		label.y.set((label.height.get() + 3) * chatLines);
 		
 		label.alpha.set(0);
 		timeline.animate(label.alpha, 0, 255, 300, Easing.NONE, timeline.getTime());
 		
 		chatScroll.add(label);
 		
 		++chatLines;
 	}
 }
