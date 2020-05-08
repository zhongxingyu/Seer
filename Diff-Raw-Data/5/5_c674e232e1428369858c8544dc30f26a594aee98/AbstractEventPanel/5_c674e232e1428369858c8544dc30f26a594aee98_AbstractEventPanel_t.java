 package jcue.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import jcue.domain.AbstractCue;
 import jcue.domain.CueList;
 import jcue.domain.CueType;
 import jcue.domain.audiocue.AudioCue;
 import jcue.domain.eventcue.AbstractEvent;
 import jcue.domain.eventcue.EventCue;
 import net.miginfocom.swing.MigLayout;
 
 /**
  *
  * @author Jaakko
  */
 public class AbstractEventPanel extends JPanel implements ActionListener {
     
     private AbstractEvent event;
     
     private JLabel targetCueLabel;
     protected JComboBox targetCueSelect;
     
 
     public AbstractEventPanel(EventCue cue) {
         super(new MigLayout("fillx, insets panel"));
         
         this.targetCueLabel = new JLabel("Target cue:");
         this.targetCueSelect = new JComboBox();
         this.targetCueSelect.addActionListener(this);
         
         addComponents();
     }
     
     private void addComponents() {
         this.add(this.targetCueLabel, "split 2");
         this.add(this.targetCueSelect, "wmin 200, span, wrap");
     }
     
     public void setEvent(AbstractEvent event) {
         this.event = event;
         
         update();
     }
     
     protected void update() {
         ArrayList<AbstractCue> cues = CueList.getInstance().getCues(CueType.AUDIO);
         AbstractCue[] tmpArray = new AbstractCue[cues.size()];
         AbstractCue[] cueArray = cues.toArray(tmpArray);
 
         ComboBoxModel cbm = new DefaultComboBoxModel(cueArray);
         this.targetCueSelect.setModel(cbm);
         
         if (this.event != null) {
             this.targetCueSelect.setSelectedItem(this.event.getTargetCue());
         }
     }
     
     @Override
     public void actionPerformed(ActionEvent ae) {
         Object source = ae.getSource();
         
         if (source == this.targetCueSelect) {
             JComboBox cb = (JComboBox) source;
             
             if (this.event != null) {
                AudioCue target = (AudioCue) cb.getSelectedItem();
                if (target != null) {
                    this.event.setTargetCue(target);
                }
             }
         }
     }
 }
