 package aigilas.hud;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.creatures.StatType;
 import aigilas.management.Commands;
 import com.badlogic.gdx.graphics.Color;
 import sps.bridge.DrawDepth;
 import sps.core.Point2;
 import sps.core.Settings;
 import sps.core.Spx;
 
 public class SkillHud extends BaseHud {
     private static final String __separator = "|";
 
     private Point2 _manaPosition = new Point2();
 
     public SkillHud(BaseCreature owner) {
         super(owner, Settings.get().spriteWidth, Spx.VirtualHeight / 4);
         _manaPosition = new Point2(getMeterAnchor().X, getMeterAnchor().Y - Spx.VirtualHeight / 4);
     }
 
     private int calculateHeight(StatType statType) {
        return (int) ((_parent.get(statType) / _parent.getMax(statType)) * _dimensions.Y);
     }
 
     private int costOfCurrentSkill() {
        return (int) (_parent.getCurrentSkillCost() / _parent.getMax(StatType.Mana) * _dimensions.Y);
     }
 
     private String getSkillStrings() {
         return "A:" + _parent.getActiveSkillName() + __separator + "X:" + _parent.getHotSkillName(Commands.HotSkill1) + __separator + "Y:" + _parent.getHotSkillName(Commands.HotSkill2) + __separator + "B:" + _parent.getHotSkillName(Commands.HotSkill3) + __separator;
     }
 
     public void update() {
         if (_isVisible) {
             _textHandler.update();
             _textHandler.clear();
             _textHandler.writeDefault(getSkillStrings(), 0, 0, getSkillAnchor());
         }
     }
 
     @Override
     public void draw() {
         if (!_isVisible) {
             return;
         }
 
         Spx.Renderer.draw(_menuBase, getMeterAnchor(), DrawDepth.HudBG, Color.GREEN, Settings.get().spriteWidth, calculateHeight(StatType.Health));
         Spx.Renderer.draw(_menuBase, _manaPosition, DrawDepth.HudBG, Color.BLUE, Settings.get().spriteWidth, calculateHeight(StatType.Mana));
         Spx.Renderer.draw(_menuBase, _manaPosition, DrawDepth.HudBG, Color.YELLOW, Settings.get().spriteWidth / 2, costOfCurrentSkill());
 
         _textHandler.draw();
     }
 }
