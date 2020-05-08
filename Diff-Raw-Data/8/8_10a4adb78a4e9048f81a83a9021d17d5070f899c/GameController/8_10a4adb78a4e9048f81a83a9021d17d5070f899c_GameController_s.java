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
 
import org.investovator.ann.nngaming.util.GameTypes;
 import org.investovator.controller.utils.enums.GameStates;
 import org.investovator.controller.utils.events.GameEventListener;
 import org.investovator.controller.utils.exceptions.GameCreationException;
 import org.investovator.controller.utils.exceptions.GameProgressingException;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public interface GameController {
 
    public String createGameInstance(GameTypes type) throws GameCreationException;
 
     public List<String> getGameInstances();
 
     public void registerListener(String instance, GameEventListener listener);
 
     public void removeListener(String instance, GameEventListener listener);
 
    public void startGame(String instance) throws GameProgressingException;
 
     public void stopGame(String instance);
 
     public void setupGame(String instance, Object[] configurations);
 
     public GameStates getCurrentGameState(String instance);
 
 }
