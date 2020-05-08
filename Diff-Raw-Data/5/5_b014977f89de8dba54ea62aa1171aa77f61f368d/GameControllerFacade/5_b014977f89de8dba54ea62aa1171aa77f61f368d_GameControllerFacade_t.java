 /*
  * investovator, Stock Market Gaming framework
  * Copyright (C) 2013  investovator
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.controller;
 
 
 import org.investovator.controller.agentgaming.AgentGameFacade;
 import org.investovator.controller.utils.enums.GameModes;
 import org.investovator.controller.utils.enums.GameStates;
 import org.investovator.controller.utils.events.GameCreationProgressChanged;
 import org.investovator.controller.utils.events.GameEvent;
 import org.investovator.controller.utils.events.GameEventListener;
 import org.investovator.controller.utils.exceptions.GameProgressingException;
 import org.investovator.jasa.api.JASAFacade;
 import org.investovator.jasa.api.MarketFacade;
 
 import java.util.ArrayList;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class GameControllerFacade {
 
     private AgentGameFacade agentGameFacade;
     private static GameModes currentGameMode = null;
     private static GameStates currentGameState = GameStates.NEW;
     private ArrayList<GameEventListener> listeners;
 
 
     private static GameControllerFacade instance;
 
     public static GameControllerFacade getInstance() {
         if(instance == null){
             synchronized(GameControllerFacade.class){
                 if(instance == null)
                     instance = new GameControllerFacade();
             }
         }
         return instance;
     }
 
     private GameControllerFacade(){
         listeners = new ArrayList<GameEventListener>();
         //agentGameFacade = new AgentGameFacade();
         //agentGameFacade.setupAgentGame();
     }
 
     public void startGame(GameModes gameMode, Object[] configutrations) throws GameProgressingException{
 
         if(currentGameState==GameStates.RUNNING) throw new GameProgressingException();
 
         //todo - Handle "Configured" state
         switch (gameMode) {
             case AGENT_GAME:
                 startAgentGame();
                 currentGameMode=GameModes.AGENT_GAME;
                 currentGameState=GameStates.RUNNING;
                 break;
             case NN_GAME:
                 break;
             case PAYBACK_ENG:
                currentGameMode=GameModes.PAYBACK_ENG;
                currentGameState=GameStates.RUNNING;
                 break;
         }
 
     }
 
 
     private void startAgentGame(){
 
         MarketFacade simulationFacade = JASAFacade.getMarketFacade();
         simulationFacade.startSimulation();
 
         new Thread(new Runnable() {
             @Override
             public void run() {
                 for (int i = 0; i < 5; i++) {
 
                     try {
                         Thread.sleep(1000);
                         notifyListeners(new GameCreationProgressChanged(GameModes.AGENT_GAME, (((float)i)/4) ));
 
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }).start();
     }
 
 
 
     private void notifyListeners(GameEvent event){
          for(GameEventListener listener : listeners){
              listener.eventOccurred(event);
          }
     }
 
     public void registerListener(GameEventListener listener){
         //agentGameFacade.registerListener(listener);
         listeners.add(listener);
     }
 
     public GameModes getCurrentGameMode(){
         return currentGameMode;
     }
 
     public GameStates getCurrentGameState(){
         return  currentGameState;
     }
 
 }
