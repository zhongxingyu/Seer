 package shooter;
 
 import org.lwjgl.input.Keyboard;
 import static org.lwjgl.opengl.GL11.*;
 
 import gl.texture.Texture;
 import gl.texture.TextureLoader;
 import core.FrameUtils;
 import shooter.dialogue.DialogueSequence;
 
 public class DialogueHandler {
 	private boolean isActive = false;
 	private static final Texture actorTexture = TextureLoader.loadTextureFromFile("res/textures/corcom.png");
 	private boolean previousSpacebarState = false;
 	private int sequencePlayhead = 0;
 	private DialogueSequence sequence;
 	
 	public void showDialogueSequence(DialogueSequence sequence) {
 		isActive = true;
 		this.sequencePlayhead = 0;
 		this.sequence = sequence;
 	}
 	
 	public void update() {
 		boolean currentSpacebarState = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
 		if((previousSpacebarState == true) && (currentSpacebarState == false)) {
 			this.advanceDialogueSequence();
 		}
 		previousSpacebarState = currentSpacebarState;
 	}
 	
 	private void advanceDialogueSequence() {
 		sequencePlayhead++;
		if(sequencePlayhead == this.sequence.dialogueTextures.length) {
 			this.isActive = false;
 		}
 	}
 
 	public boolean isActive() {
 		return isActive;
 	}
 
 	public void render() {
 		FrameUtils.set2DMode();
 		
 		drawBackground();
 		
 		glEnable(GL_TEXTURE_2D);
 		glColor4f(1, 1, 1, 1);
 		
 		drawActorTexture();
 		drawDialogueTexture();
 	}
 
 	private void drawBackground() {
 		double aspectRatio = FrameUtils.calculateAspectRatio();
 		glColor4f(0, 0, 0, 1);
 		glBegin(GL_QUADS);
 		glTexCoord2d(0, 0);
 		glVertex2d(0, 0);
 		glTexCoord2d(1, 0);
 		glVertex2d(aspectRatio, 0);
 		glTexCoord2d(1, 1);
 		glVertex2d(aspectRatio, 1);
 		glTexCoord2d(0, 1);
 		glVertex2d(0, 1);
 		glEnd();
 	}
 
 	private void drawActorTexture() {
 		double aspectRatio = FrameUtils.calculateAspectRatio();
 		double centerX = aspectRatio / 2;
 		double xOffset = 0.5 / aspectRatio;
 		
 		actorTexture.bind();
 		glBegin(GL_QUADS);
 		glTexCoord2d(0, 0);
 		glVertex2d(centerX - xOffset, 0.1);
 		glTexCoord2d(1, 0);
 		glVertex2d(centerX + xOffset, 0.1);
 		glTexCoord2d(1, 1);
 		glVertex2d(centerX + xOffset, 0.6);
 		glTexCoord2d(0, 1);
 		glVertex2d(centerX - xOffset, 0.6);
 		glEnd();
 	}
 
 	private void drawDialogueTexture() {
 		double aspectRatio = FrameUtils.calculateAspectRatio();
 		double centerX = aspectRatio / 2;
 		double xOffset = 0.5 / aspectRatio;
 
 		this.sequence.dialogueTextures[this.sequencePlayhead].bind();
 		
 		glBegin(GL_QUADS);
 		glTexCoord2d(0, 0);
 		glVertex2d(centerX - xOffset, 0.65);
 		glTexCoord2d(1, 0);
 		glVertex2d(centerX + xOffset, 0.65);
 		glTexCoord2d(1, 1);
 		glVertex2d(centerX + xOffset, 0.85);
 		glTexCoord2d(0, 1);
 		glVertex2d(centerX - xOffset, 0.85);
 		glEnd();
 	}
 }
