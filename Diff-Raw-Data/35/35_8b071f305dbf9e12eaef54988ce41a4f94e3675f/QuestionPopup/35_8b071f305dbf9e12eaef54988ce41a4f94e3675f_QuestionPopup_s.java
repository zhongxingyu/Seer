 package com.caramelcode.mcstudent.gui;
 
 import org.lwjgl.input.Keyboard;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.network.packet.Packet250CustomPayload;
 
 import com.caramelcode.mcstudent.RewardMachine;
 import com.caramelcode.mcstudent.forge.AssetHelper;
 import com.caramelcode.mcstudent.forge.MCStudentModInfo;
 import com.caramelcode.mcstudent.questions.ArithmeticQuestion;
 import com.caramelcode.mcstudent.questions.QuestionFactory;
 import com.caramelcode.mcstudent.questions.Question;
 import com.google.common.base.Strings;
 import com.mcf.davidee.guilib.basic.Label;
 import com.mcf.davidee.guilib.core.Button;
 import com.mcf.davidee.guilib.core.Button.ButtonHandler;
 import com.mcf.davidee.guilib.core.Container;
 import com.mcf.davidee.guilib.core.TextField;
 import com.mcf.davidee.guilib.core.TextField.CharacterFilter;
 import com.mcf.davidee.guilib.vanilla.ButtonVanilla;
 import com.mcf.davidee.guilib.vanilla.TextFieldVanilla;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class QuestionPopup extends MCStudentPopup {
 
 	private Container container;
 	private Label titleLabel, questionLabel;
 	private Button submit;
 	private TextField answerField;
 	private Question question;
 
 	private boolean validInput() {
 		String text = answerField.getText();
 		if (Strings.isNullOrEmpty(text)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public QuestionPopup() {
 		super(null);
 		question = QuestionFactory.buildQuestion();
 	}
 
 	@Override
 	public void updateScreen() {
 		submit.setEnabled(validInput());
 		super.updateScreen();
 	}
 
 	@Override
 	protected void revalidateGui() {
 		int startY = (height - HEIGHT) / 2;
 
 		titleLabel.setPosition(width / 2, startY + 5);
 		questionLabel.setPosition(width / 2, startY + 40);
 		answerField.setPosition(width / 2 - 75, startY + 55);
 		submit.setPosition(width / 2 - 60, startY + 100);
 
 		container.revalidate(0, 0, width, height);
 	}
 
 	@Override
 	protected void createGui() {
 		container = new Container();
 
 		titleLabel = new Label("Question", 0, 0);
 		questionLabel = new Label(question.getQuestionText(), 0, 0);
 		titleLabel.setShadowedText(false);
 		questionLabel.setShadowedText(false);
 		submit = new ButtonVanilla(120, 20, "Answer", new ButtonHandler() {
 			public void buttonClicked(Button button) {
 				processAnswer();
 			}
 		});
 		submit.setEnabled(false);
 
 		answerField = new TextFieldVanilla(150, 20, 0xffa09172, 0xff373737, new CharacterFilter() {
 			public String filter(String s) {
 				StringBuilder sb = new StringBuilder();
 				for (char c : s.toCharArray())
 					if (isAllowedCharacter(c))
 						sb.append(c);
 				return sb.toString();
 			}
 
 			public boolean isAllowedCharacter(char c) {
 				if (question instanceof ArithmeticQuestion) {
 					return Character.isDigit(c);
 				} 
 				
 				return Character.isLetterOrDigit(c);
 			}
 		});
 
 		answerField.setMaxLength(23);
 		answerField.click(1, 1);
 
 		container.addWidgets(titleLabel, questionLabel, answerField, submit);
 		container.setFocused(answerField);
 
 		containers.add(container);
 		selectedContainer = container;
 	}
 
 	private void processAnswer() {
 		if (answerField.getText().equalsIgnoreCase(question.getAnswer())) {
 			// answer was correct play the correct answer sound
 			Minecraft mc = Minecraft.getMinecraft();
 			mc.sndManager.playSoundFX(AssetHelper.SOUND_CORRECT, 3.0F, 1.0F);
 
 			// Give player a reward
 			PacketDispatcher.sendPacketToServer(new Packet250CustomPayload(
 					MCStudentModInfo.CHANNEL, new byte[] { 1 }));
 			
 			// close the current question GUI
 			close();
 			
 			// display correct confirmation popup
 			mc.displayGuiScreen(new CorrectAnswerPopup());
 		} else {
			// answer was wrong, get a new question and update the popup
			question = QuestionFactory.buildQuestion();
 			RewardMachine.resetCorrectCount(); // set the correct streak back to 0
			questionLabel.setText(question.getQuestionText());
 		}
 	}
 
 	@Override
 	public boolean doesGuiPauseGame() {
 		return true;
 	}
 
 	@Override
 	protected void reopenedGui() {
 	}
 
 	@Override
 	protected void unhandledKeyTyped(char c, int code) {
 		if (code == Keyboard.KEY_RETURN) {
 			processAnswer();
 		}
 	}
 }
