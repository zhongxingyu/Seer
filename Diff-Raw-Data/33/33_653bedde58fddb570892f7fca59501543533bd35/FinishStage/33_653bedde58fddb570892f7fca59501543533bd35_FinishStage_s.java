 package eu32k.ludumdare.ld26.stages;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 import eu32k.ludumdare.ld26.effects.EffectsManager;
 import eu32k.ludumdare.ld26.rendering.Background;
 import eu32k.ludumdare.ld26.state.PlayerState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class FinishStage extends AbstractStage {
 
    private List<Label> list = new ArrayList<Label>();
 
    public FinishStage(EffectsManager effects) {
       this.effects = effects;
   }

   @Override
   public void draw() {
      clear();

       Map<String, Integer> genericStatistics = StateMachine.instance().getState(PlayerState.class).genericStatistics;
 
       Table table = new Table();
       table.setFillParent(true);
       table.center();
 
       for (String key : genericStatistics.keySet()) {
          Label nameField = new Label(key + ": ", skin);
          list.add(nameField);
          Label valueField = new Label("" + genericStatistics.get(key), skin);
          list.add(valueField);
          table.add(nameField).fill().pad(4);
          table.add(valueField).fill().pad(4);
          table.row();
       }

       addActor(table);
 
       Color color = effects.getCurrentColor();
       Background.draw(color, false);
       for (Label label : list) {
          label.setColor(color);
       }
       super.draw();
    }
 }
