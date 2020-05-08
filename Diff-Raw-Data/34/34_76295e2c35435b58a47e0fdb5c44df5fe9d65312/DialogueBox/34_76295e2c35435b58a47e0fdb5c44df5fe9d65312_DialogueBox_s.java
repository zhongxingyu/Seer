 package com.vgdc.merge.ui.dialogue;
 
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenAccessor;
 import aurelienribon.tweenengine.TweenEquations;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.utils.Json;
 import com.vgdc.merge.events.Event;
 import com.vgdc.merge.events.EventSystem;
 import com.vgdc.merge.world.World;
 
 public class DialogueBox extends Window {
 
 	private World world;
 	private DialogueLabel dialogueLabel;
 	private ContinueLabel continueLabel;
	private Image leftSpeaker;
 	private DialogueScript script;
 	private Event onCloseEvent;
 	private Skin skin;
 	private EventSystem eventSystem;
 	private boolean closed;
 	private boolean canSkip = true;
 	private ScrollPane scrollPane;
 	private DialogueManager manager;
 
 	private DialogueScript.Page currentPage;
 	private int pagePointer = 0;
 	private int linePointer = 0;
 	private DialogueScript.Emotion currentEmotion;
 
 	public DialogueBox(DialogueManager manager, World world, String scriptPath, Skin skin) {
 		super("", skin);
 		this.manager = manager;
 		this.world = world;
 		this.skin = skin;
 		setScript(scriptPath);
 		create();
 	}
 
 	public void setScript(String scriptPath) {
 		Json json = new Json();
 		script = json.fromJson(DialogueScript.class, Gdx.files.internal(scriptPath));
 	}
 
 	private void create() {
 		this.setSize(450, 125);
		leftSpeaker = new Image();
 
 		this.row();
		add(leftSpeaker).top().size(64, 64);
 		dialogueLabel = new DialogueLabel(skin);
 		{
 			// this.add(textLabel).top().left().expand().size(500, 100);
 			dialogueLabel.setWrap(true);
 			dialogueLabel.setAlignment(Align.top | Align.left);
 			dialogueLabel.setDialogueListener(new DialogueLabelListener() {
 				@Override
 				public void lineFinished(DialogueLabel label) {
 					continueLabel.setText("Click RMB to continue...");
 					continueLabel.startFadeTween();
 
 					scrollPane.setScrollY(dialogueLabel.getHeight());
 				}
 			});
 		}
 		scrollPane = new ScrollPane(dialogueLabel, skin);
 		add(scrollPane).top().fill().expand();
 		
 		this.row();
 
 		continueLabel = new ContinueLabel("", skin);
 		{
 			continueLabel.setAlignment(Align.right);
 			this.add(continueLabel).right().colspan(2).size(300, 25);
 		}
 		// final TextButton closeButton = new TextButton("CLOSE", skin);
 		// {
 		// closeButton.addListener(new ClickListener() {
 		// @Override
 		// public void clicked(InputEvent event, float x, float y) {
 		// close();
 		// }
 		// });
 		// this.add(closeButton).left().size(50, 25);
 		// }
 		//
 		// final TextButton continueButton = new TextButton("NEXT", skin);
 		// {
 		// continueButton.addListener(new ContinueButtonListener());
 		// this.add(continueButton).right().size(50, 25);
 		// }
 
 		this.setMovable(true);
 		// this.debug();
 
 		currentPage = script.pages[0];
 		setTitle(currentPage.speaker);
 		currentEmotion = currentPage.emotions[0];
 		dialogueLabel.setScriptLine(currentEmotion.lines[0]);
		setLeftSpeaker(currentEmotion.image);
 		
 		world.stop();
 	}
 
 	private boolean buttonPressed;
 	private int emotionPointer;
 
 	@Override
 	public void act(float delta) {
 		if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
 			if (buttonPressed == false) {
 				if (dialogueLabel.isEndOfLineReached()) {
 					nextLine();
 				}
 				else {
 					if (canSkip) {
 						dialogueLabel.finishLine();
 					}
 				}
 			}
 			buttonPressed = true;
 		}
 		else {
 			buttonPressed = false;
 		}
 
 		// scroll to end
 		if (!dialogueLabel.isEndOfLineReached()) {
 			scrollPane.setScrollY(dialogueLabel.getHeight());
 		}
 
 		super.act(delta);
 	}
 
 	private void nextLine() {
 		linePointer++;
 		if (linePointer < currentEmotion.lines.length) {
 			dialogueLabel.setScriptLine(currentEmotion.lines[linePointer]);
 			continueLabel.setText("");
 			continueLabel.stopFadeTween();
 		}
 		else {
 			nextEmotion();
 		}
 	}
 
 	private void nextEmotion() {
 		emotionPointer++;
 		linePointer = -1;
 		if (emotionPointer < currentPage.emotions.length) {
 			currentEmotion = currentPage.emotions[emotionPointer];
			setLeftSpeaker(currentEmotion.image);
 			nextLine();
 		}
 		else{
 			nextPage();
 		}
 	}
 
 	private void nextPage() {
 		pagePointer++;
 		emotionPointer = -1;
 		if (pagePointer < script.pages.length) {
 			currentPage = script.pages[pagePointer];
 			setTitle(currentPage.speaker);
 			nextEmotion();
 		}
 		else {
 			close();
 		}
 	}
 
 	public void setSkip(boolean canSkip) {
 		this.canSkip = canSkip;
 	}
 
 	public void setScrollSpeed(float charsPerSecond) {
 		this.dialogueLabel.setScrollSpeed(charsPerSecond);
 	}
 
 	// Can optionally specify a event to be called when this dialogue box is
 	// closed. Allows for an event chain.
 	public void setOnCloseEvent(Event onCloseEvent, EventSystem eventSystem) {
 		this.onCloseEvent = onCloseEvent;
 		this.eventSystem = eventSystem;
 	}
 
 	public void close() {
 		this.remove();
 		this.closed = true;
 		if (onCloseEvent != null) {
 			eventSystem.addEvent(onCloseEvent);
 		}
 		world.run();
 	}
 
 	public boolean isClosed() {
 		return closed;
 	}
 
	public void setLeftSpeaker(String imageName) {
 		TextureRegionDrawable image = manager.getPortrait(imageName);
		leftSpeaker.setDrawable(image);
 	}
 
 	private class ContinueLabel extends Label {
 		private Tween tween;
 		private TweenManager tweenManager;
 
 		public ContinueLabel(CharSequence text, Skin skin) {
 			super(text, skin);
 			create();
 		}
 
 		public void startFadeTween() {
 			this.setColor(1, 1, 1, 0);
 			tweenManager.add(tween);
 			tweenManager.resume();
 		}
 
 		public void stopFadeTween() {
 			tweenManager.pause();
 		}
 
 		@Override
 		public void act(float delta) {
 			super.act(delta);
 			tweenManager.update(delta);
 		}
 
 		private void create() {
 			tweenManager = new TweenManager();
 			Tween.registerAccessor(Label.class, new LabelAlphaTween());
 
 			tween = Tween.to(this, LabelAlphaTween.ALPHA, 1f).target(1).ease(TweenEquations.easeInQuad)
 					.repeatYoyo(Tween.INFINITY, 0.5f);
 		}
 
 		private class LabelAlphaTween implements TweenAccessor<Label> {
 			public static final int ALPHA = 1;
 
 			@Override
 			public int getValues(Label target, int tweenType, float[] returnValues) {
 				switch (tweenType) {
 				case ALPHA:
 					returnValues[0] = target.getColor().a;
 					return ALPHA;
 				default:
 					return 0;
 				}
 			}
 
 			@Override
 			public void setValues(Label target, int tweenType, float[] newValues) {
 				switch (tweenType) {
 				case ALPHA:
 					target.setColor(1, 1, 1, newValues[0]);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	}
 }
