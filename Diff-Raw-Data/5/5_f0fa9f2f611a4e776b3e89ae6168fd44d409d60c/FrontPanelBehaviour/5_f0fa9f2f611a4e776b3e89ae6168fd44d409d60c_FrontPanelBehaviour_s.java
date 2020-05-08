 package com.sirenian.hellbound.gui;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 import org.jbehave.core.Ensure;
 import org.jbehave.core.minimock.UsingMiniMock;
 import org.jbehave.core.mock.Mock;
 import org.jbehave.threaded.swing.DefaultWindowWrapper;
 import org.jbehave.threaded.swing.WindowWrapper;
 
 import com.sirenian.hellbound.domain.game.GameRequestListener;
 
 public class FrontPanelBehaviour extends UsingMiniMock {
 	
 	public void shouldContainTheButtonToStartTheGame() throws Exception {

 		WindowWrapper wrapper = new DefaultWindowWrapper("TestFrame");
 		
 		Mock gameStarter = mock(GameRequestListener.class);
 		gameStarter.expects("requestStartGame");
 		
 		FrontPanel panel = new FrontPanel((GameRequestListener)gameStarter);
 
 		JFrame frame = new JFrame();
 		frame.setName("TestFrame");
 		frame.getContentPane().add(panel);
 		
 		frame.setVisible(true);
 		wrapper.clickButton("startGame.button");		
 		verifyMocks();
 		
 		JButton button = (JButton)wrapper.findComponent("startGame.button");
 		Ensure.that("Start Game", eq(button.getText()));
 		
 		frame.dispose();
 	}
 }
