 package org.i4qwee.chgk.trainer.view.dialogs;
 
 import org.i4qwee.chgk.trainer.controller.brain.ScoreManagerSingleton;
 import org.i4qwee.chgk.trainer.controller.brain.listener.GameStateListener;
 import org.i4qwee.chgk.trainer.controller.brain.manager.AnswerSideManager;
 import org.i4qwee.chgk.trainer.controller.brain.manager.GameStateManager;
 import org.i4qwee.chgk.trainer.model.enums.GameState;
 import org.i4qwee.chgk.trainer.view.DefaultUIProvider;
 
 import javax.swing.*;
 import java.awt.event.*;
 
 public class BrainConfirmationDialog extends AbstractDialog implements GameStateListener
 {
     private JPanel contentPane;
     private JButton correctButton;
     private JButton incorrectButton;
     private JLabel messageLabel;
 
     private final AnswerSideManager answerSideManager = AnswerSideManager.getInstance();
 
     public BrainConfirmationDialog()
     {
         GameStateManager.getInstance().addListener(this);
 
         setResizable(false);
         setModal(true);
 
         contentPane.setBorder(DefaultUIProvider.getDefaultEmptyEtchedEmptyBorder());
 
         setContentPane(contentPane);
         getRootPane().setDefaultButton(correctButton);
 
         correctButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 onCorrect();
             }
         });
 
         incorrectButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 onIncorrect();
             }
         });
 
 // call onIncorrect() when cross is clicked
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 onIncorrect();
             }
         });
 
 // call onIncorrect() on LEFT
         contentPane.registerKeyboardAction(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 onCorrect();
             }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 // call onIncorrect() on RIGHT
         contentPane.registerKeyboardAction(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 onIncorrect();
             }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
     }
 
     private void onCorrect()
     {
         ScoreManagerSingleton.getInstance().answer(true);
         dispose();
     }
 
     private void onIncorrect()
     {
         ScoreManagerSingleton.getInstance().answer(false);
         dispose();
     }
 
     public void onGameStageChanged(GameState gameState)
     {
         switch (gameState)
         {
             case PAUSED:
 
                 String name = answerSideManager.getAnswersName();
 
                 if (name != null && !name.equals(""))
                     messageLabel.setText(name + ", правильно?");
                 else
                     messageLabel.setText("Правильно?");
 
                 showDialog();
                 break;
         }
     }
 }
