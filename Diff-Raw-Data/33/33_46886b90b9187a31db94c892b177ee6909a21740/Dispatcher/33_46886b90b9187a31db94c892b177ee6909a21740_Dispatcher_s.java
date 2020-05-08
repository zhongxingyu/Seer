 package org.blink.net;
 
 import com.jme3.math.Vector3f;
 import org.blink.net.message.Chat;
 import org.blink.net.message.Connect;
 import org.blink.net.message.Damage;
 import org.blink.net.message.Move;
 import org.blink.net.message.Spell;
 import org.blink.net.message.Sync;
 import org.blink.net.message.util.EnumSpell;
 import org.blink.net.model.Player;
 import org.blink.net.model.PlayerData;
import org.blink.util.CustomDecimalFormat;
 import org.blink.util.LocalConfig;
 
 /**
  *
  * @author cmessel
  */
 public class Dispatcher {
 
    private static CustomDecimalFormat cdf = new CustomDecimalFormat();

     public static void connect() {
         Connect c = new Connect();
         c.setVector3f(Vector3f.ZERO);
         c.setModelName(LocalConfig.getModel());
         c.send();
     }
 
     public static void move(Vector3f contactPoint) {
         Move move = new Move();
        cdf.round(contactPoint);
         move.setVector3f(contactPoint);
         move.send();
     }
 
     public static void chat(String text) {
         Chat chat = new Chat();
         chat.setText(text);
         chat.send();
     }
 
     public static void sync() {
         Sync sync = new Sync();
         Player p = PlayerData.get(LocalConfig.getName());
         sync.setHealth(100);
         sync.setVector3f(p.getModel().getLocalTranslation());
         sync.send();
     }
 
     public static void spell(EnumSpell enumSpell) {
         Spell spell = new Spell();
         Player p = PlayerData.get(LocalConfig.getName());
         spell.setSpellName(enumSpell.name());
         spell.setVector3f(p.getModel().getLocalTranslation());
         spell.send();
     }
 
     public static void damage(String target, String spellName) {
         Damage dmg = new Damage();
         dmg.setSpellName(spellName);
         dmg.setTarget(target);
         dmg.send();
     }
 }
