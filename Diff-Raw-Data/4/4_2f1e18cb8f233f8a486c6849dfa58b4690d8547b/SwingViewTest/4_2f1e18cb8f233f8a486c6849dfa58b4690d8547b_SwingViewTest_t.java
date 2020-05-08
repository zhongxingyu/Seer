 package com.cmendenhall;
 
 import junit.framework.*;
 import org.junit.*;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.*;
 
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 @RunWith(JUnit4.class)
 public class SwingViewTest extends TicTacToeTest{
 
     SwingView swingView;
 
     @Before
     public void setUp() {
         swingView = new SwingView();
     }
 
     @Test
     public void swingViewHasCorrectSize() {
         assertEquals(350, swingView.getWidth());
         assertEquals(700, swingView.getHeight());
     }
 
     private Component getComponent(Container container, String name) {
         for (Component component : container.getComponents()) {
             if (name.equals(component.getName())) {
                 return component;
             }
         }
         return null;
     }
 
     @Test
     public void swingViewHasMessagePanel() {
         SwingView.MessagePanel messagePanel =
                 (SwingView.MessagePanel)getComponent(swingView.getContentPane(), "messagePanel");
         JLabel label =
                 (JLabel)getComponent(messagePanel, "messagePanelLabel");
         assertTrue(label.isVisible());
         assertEquals("Welcome to Tic-Tac-Toe", label.getText());
     }
 
     @Test
     public void swingViewHasBoardPanel() {
         SwingView.BoardPanel boardPanel =
                 (SwingView.BoardPanel)getComponent(swingView.getContentPane(), "boardPanel");
         assertTrue(boardPanel.isVisible());
     }
 
     @Test
     public void boardPanelContainsJTable() {
         SwingView.BoardPanel boardPanel =
                 (SwingView.BoardPanel)getComponent(swingView.getContentPane(), "boardPanel");
         JTable boardTable =
                 (JTable)getComponent(boardPanel, "boardTable");
         assertTrue(boardTable.isVisible());
     }
 
     @Test
     public void boardPanelCorrectlyDisplaysBoard() {
         SwingView.BoardPanel boardPanel =
                 (SwingView.BoardPanel)getComponent(swingView.getContentPane(), "boardPanel");
         boardPanel.loadBoard(noWins);
     }
 
     @Test
     public void swingViewHasConfigPanel() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         assertTrue(configPanel.isVisible());
     }
 
     @Test
     public void configPanelHasGameActionPanel() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.GameActionPanel gameActionPanel =
                 (SwingView.ConfigPanel.GameActionPanel)getComponent(configPanel, "gameActionPanel");
     }
 
     @Test
     public void gameActionPanelHasNewGameButton() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.GameActionPanel gameActionPanel =
                 (SwingView.ConfigPanel.GameActionPanel)getComponent(configPanel, "gameActionPanel");
 
         JButton newGameButton =
                 (JButton)getComponent(gameActionPanel, "newGameButton");
 
         assertTrue(newGameButton.isVisible());
     }
 
 
     @Test
     public void newGameButtonHasCorrectLabel() {
 
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.GameActionPanel gameActionPanel =
                 (SwingView.ConfigPanel.GameActionPanel)getComponent(configPanel, "gameActionPanel");
 
         JButton newGameButton =
                 (JButton)getComponent(gameActionPanel, "newGameButton");
 
         assertEquals("New game", newGameButton.getText());
     }
 
     @Test
     public void configPanelHasPlayerOneConfigPanel() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         SwingView.ConfigPanel.PlayerConfigPanel playerOneConfigPanel =
                 (SwingView.ConfigPanel.PlayerConfigPanel)getComponent(configPanel, "playerOneConfigPanel");
         assertTrue(playerOneConfigPanel.isVisible());
     }
 
 
     @Test
     public void configPanelHasPlayerTwoConfigPanel() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         SwingView.ConfigPanel.PlayerConfigPanel playerTwoConfigPanel =
                 (SwingView.ConfigPanel.PlayerConfigPanel)getComponent(configPanel, "playerTwoConfigPanel");
         assertTrue(playerTwoConfigPanel.isVisible());
     }
 
     @Test
     public void playerConfigPanelHasTwoRadioButtons() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         SwingView.ConfigPanel.PlayerConfigPanel playerTwoConfigPanel =
                 (SwingView.ConfigPanel.PlayerConfigPanel)getComponent(configPanel, "playerTwoConfigPanel");
         JRadioButton humanButton =
                 (JRadioButton)getComponent(playerTwoConfigPanel, "humanButton");
         JRadioButton computerButton =
                 (JRadioButton)getComponent(playerTwoConfigPanel, "computerButton");
         assertTrue(humanButton.isVisible());
         assertTrue(computerButton.isVisible());
     }
 
     @Test
     public void playerConfigPanelHasCorrectLabel() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         SwingView.ConfigPanel.PlayerConfigPanel playerTwoConfigPanel =
                 (SwingView.ConfigPanel.PlayerConfigPanel)getComponent(configPanel, "playerTwoConfigPanel");
         JLabel playerTwo =
                 (JLabel)getComponent(playerTwoConfigPanel, "playerLabel");
         assertEquals("Player Two", playerTwo.getText());
     }
 
     @Test
     public void playerConfigPanelStoresPlayerConfigState() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         SwingView.ConfigPanel.PlayerConfigPanel playerTwoConfigPanel =
                 (SwingView.ConfigPanel.PlayerConfigPanel)getComponent(configPanel, "playerTwoConfigPanel");
         assertTrue(playerTwoConfigPanel.humanSelected());
     }
 
     @Test
     public void radioButtonsHaveCorrectLabels() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
         SwingView.ConfigPanel.PlayerConfigPanel playerTwoConfigPanel =
                 (SwingView.ConfigPanel.PlayerConfigPanel)getComponent(configPanel, "playerTwoConfigPanel");
         JRadioButton humanButton =
                 (JRadioButton)getComponent(playerTwoConfigPanel, "humanButton");
         JRadioButton computerButton =
                 (JRadioButton)getComponent(playerTwoConfigPanel, "computerButton");
         assertEquals("Human", humanButton.getText());
         assertEquals("Computer", computerButton.getText());
     }
 
     @Test
     public void configPanelHasBoardConfigPanel() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.BoardConfigPanel boardConfigPanel =
                 (SwingView.ConfigPanel.BoardConfigPanel)getComponent(configPanel, "boardConfigPanel");
         assertTrue(boardConfigPanel.isVisible());
     }
 
     @Test
     public void boardConfigPanelHasBoardSizeSpinner() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.BoardConfigPanel boardConfigPanel =
                 (SwingView.ConfigPanel.BoardConfigPanel)getComponent(configPanel, "boardConfigPanel");
 
         JSpinner boardSizeSpinner =
                 (JSpinner)getComponent(boardConfigPanel, "boardSizeSpinner");
 
         assertTrue(boardSizeSpinner.isVisible());
     }
 
 
     /*@Test
     public void boardSizeSpinnerHasRoomForTwoDigits() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.BoardConfigPanel boardConfigPanel =
                 (SwingView.ConfigPanel.BoardConfigPanel)getComponent(configPanel, "boardConfigPanel");
 
         JSpinner boardSizeSpinner =
                 (JSpinner)getComponent(boardConfigPanel, "boardSizeSpinner");
 
         assertEquals(50, boardSizeSpinner.getWidth());
     }*/
 
     @Test
     public void boardConfigPanelSpinnerStoresSpinnerState() {
         SwingView.ConfigPanel configPanel =
                 (SwingView.ConfigPanel)getComponent(swingView.getContentPane(), "configPanel");
 
         SwingView.ConfigPanel.BoardConfigPanel boardConfigPanel =
                 (SwingView.ConfigPanel.BoardConfigPanel)getComponent(configPanel, "boardConfigPanel");
 
         assertEquals(3, boardConfigPanel.boardSize());
     }
 
 }
