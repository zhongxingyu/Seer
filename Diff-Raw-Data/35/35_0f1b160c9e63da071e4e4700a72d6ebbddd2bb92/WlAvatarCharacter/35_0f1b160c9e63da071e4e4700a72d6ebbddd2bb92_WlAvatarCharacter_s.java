 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;
 import imi.character.CharacterParams;
 import imi.character.avatar.Avatar;
 import imi.character.avatar.AvatarContext.TriggerNames;
 import imi.character.statemachine.GameContext;
 import java.awt.event.KeyEvent;
 import java.net.URL;
 import org.jdesktop.mtgame.WorldManager;
 
 /**
  *
  * @author paulby
  */
 public class WlAvatarCharacter extends Avatar {
     
     /**
      * Builder pattern impl
      */
     public static class WlAvatarCharacterBuilder extends AvatarBuilder {
         /**
          * Construct a new builder using the specified configuration file and
          * the provided world manager.
          * @param configurationFile
          * @param worldManager
          */
         public WlAvatarCharacterBuilder(URL configurationFile, WorldManager worldManager) {
             super(configurationFile, worldManager);
         }
 
         /**
          * Construct a new builder using the specified character params and the
          * provided worldmanager.
          * @param attributeParams
          * @param worldManager
          */
         public WlAvatarCharacterBuilder(CharacterParams attributeParams, WorldManager worldManager) {
             super(attributeParams, worldManager);
         }
 
         @Override
         public WlAvatarCharacterBuilder baseURL(String baseURL) {
             super.baseURL(baseURL);
             return this;
         }
 
         @Override
         public WlAvatarCharacterBuilder addEntity(boolean addEntity) {
             super.addEntity(addEntity);
             return this;
         }
 
         /**
          * {@inheritDoc AvatarBuilder}
          */
         @Override
         public WlAvatarCharacter build() {
             WlAvatarCharacter result = new WlAvatarCharacter(this);
             return result;
         }
     }
 
     /**
      * Builder pattern constructor
      * @param builder
      */
     protected WlAvatarCharacter(WlAvatarCharacterBuilder builder) {
         super(builder);
     }
 
     @Override
     protected GameContext instantiateContext() {
         return new WlAvatarContext(this);
     }
 
     public void triggerActionStart(TriggerNames trigger) {
         m_context.triggerPressed(trigger.ordinal());
     }
 
     public void triggerActionStop(TriggerNames trigger) {
         m_context.triggerReleased(trigger.ordinal());
     }
 
     public void playAnimation(String name) {
         ((WlAvatarContext)getContext()).playMiscAnimation(name);
     }
 
     public Iterable<String> getAnimationNames() {
         return ((WlAvatarContext)getContext()).getAnimationNames();
     }
 
     // TESTING
     private void bigHeadMode(WlAvatarCharacter avatar)
     {
         avatar.setBigHeadMode(2.0f);
     }
 
     @Override
     protected void initKeyBindings()
     {
         m_keyBindings.put(KeyEvent.VK_SHIFT,        TriggerNames.Movement_Modifier.ordinal());
         m_keyBindings.put(KeyEvent.VK_A,            TriggerNames.Move_Left.ordinal());
         m_keyBindings.put(KeyEvent.VK_LEFT,         TriggerNames.Move_Left.ordinal());
         m_keyBindings.put(KeyEvent.VK_D,            TriggerNames.Move_Right.ordinal());
         m_keyBindings.put(KeyEvent.VK_RIGHT,        TriggerNames.Move_Right.ordinal());
         m_keyBindings.put(KeyEvent.VK_W,            TriggerNames.Move_Forward.ordinal());
         m_keyBindings.put(KeyEvent.VK_UP,           TriggerNames.Move_Forward.ordinal());
         m_keyBindings.put(KeyEvent.VK_S,            TriggerNames.Move_Back.ordinal());
         m_keyBindings.put(KeyEvent.VK_DOWN,         TriggerNames.Move_Back.ordinal());
         //        m_keyBindings.put(KeyEvent.VK_CONTROL,      TriggerNames.MiscAction.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_ENTER,        TriggerNames.ToggleSteering.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_HOME,         TriggerNames.GoSit.ordinal());
         m_keyBindings.put(KeyEvent.VK_ADD,          TriggerNames.Move_Down.ordinal());
         m_keyBindings.put(KeyEvent.VK_PAGE_UP,      TriggerNames.Move_Down.ordinal());
         m_keyBindings.put(KeyEvent.VK_SUBTRACT,     TriggerNames.Move_Up.ordinal());
         m_keyBindings.put(KeyEvent.VK_PAGE_DOWN,    TriggerNames.Move_Up.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_COMMA,        TriggerNames.Reverse.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_PERIOD,       TriggerNames.NextAction.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_1,            TriggerNames.GoTo1.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_2,            TriggerNames.GoTo2.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_3,            TriggerNames.GoTo3.ordinal());
         m_keyBindings.put(KeyEvent.VK_G,            TriggerNames.SitOnGround.ordinal());
         m_keyBindings.put(KeyEvent.VK_0,            TriggerNames.Smile.ordinal());
         m_keyBindings.put(KeyEvent.VK_9,            TriggerNames.Frown.ordinal());
         m_keyBindings.put(KeyEvent.VK_8,            TriggerNames.Scorn.ordinal());
         m_keyBindings.put(KeyEvent.VK_Q,            TriggerNames.Move_Strafe_Left.ordinal());
         m_keyBindings.put(KeyEvent.VK_E,            TriggerNames.Move_Strafe_Right.ordinal());
         m_keyBindings.put(KeyEvent.VK_P,            TriggerNames.Point.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_Q,            TriggerNames.ToggleLeftArm.ordinal());
 //        m_keyBindings.put(KeyEvent.VK_E,            TriggerNames.ToggleRightArm.ordinal());
     }
 }
