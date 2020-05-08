 package mapeper.minecraft.modloader.config.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 import mapeper.minecraft.modloader.config.DefaultConfiguration;
 
 public class MemoryConfigGUI extends JPanel implements ActionListener {
 	JComboBox<String> dropDown = new JComboBox<String>(new String[]{"Minecraft-Default","Java-Default","Custom"});
 	JLabel minLabel = new JLabel("Min:");
 	JLabel maxLabel = new JLabel("Max:");
 	JSpinner minSpinner = new JSpinner(new SpinnerNumberModel());
 	JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel());
 	DirtyState dirty;
 	public MemoryConfigGUI(DirtyState dirty)
 	{
 		this.dirty=dirty;
 		dropDown.addActionListener(this);
 		minSpinner.addMouseWheelListener(new SpinnerMouseWheelListener());
 		maxSpinner.addMouseWheelListener(new SpinnerMouseWheelListener());
 		minSpinner.addChangeListener(dirty);
 		maxSpinner.addChangeListener(dirty);
 		this.setBorder(BorderFactory.createTitledBorder("Memory"));
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx=1;
 //		c.gridx=0;
 //		c.gridy=0;
 		c.gridwidth=2;
 		c.gridheight=1;
 		this.add(dropDown,c);
 		c.gridwidth=1;
 		c.gridx=1;
 		c.weightx=0.7;
 		c.gridy=1;
 		this.add(minSpinner,c);
 		c.gridy=2;
 		this.add(maxSpinner,c);
 		c.weightx=0;
 		c.fill=GridBagConstraints.NONE;
 		c.gridx=0;
 		c.gridy=1;
 		this.add(minLabel,c);
 		c.gridy=2;
 		this.add(maxLabel,c);
 
 		setFieldsEnabled(false);
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		switch(dropDown.getSelectedIndex())
 		{
 		case 1: 
 			minSpinner.setValue(0);maxSpinner.setValue(0);
 			setFieldsEnabled(false);break;
 		case 0: 
 			minSpinner.setValue(DefaultConfiguration.getInstance().getMinMemory());maxSpinner.setValue(DefaultConfiguration.getInstance().getMaxMemory());
 			setFieldsEnabled(false);break;
 		case 2:
 			setFieldsEnabled(true);
 		}
 		dirty.setDirty();
 	}
 	private void setFieldsEnabled(boolean b)
 	{
 		minSpinner.setEnabled(b);maxSpinner.setEnabled(b);
 		minLabel.setEnabled(b);maxLabel.setEnabled(b);
 	}
 	private class SpinnerMouseWheelListener implements MouseWheelListener
 	{
 
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent e) {
 			JSpinner spinner = (JSpinner)e.getSource();
 			if(spinner.isEnabled())
 			{
 				spinner.setValue((int)spinner.getValue()-e.getWheelRotation()*32);
 				dirty.setDirty();
 			}
 		}
 		
 	}
 	public void setMemory(int min, int max)
 	{
 		//Minecraft-Default
 		if(min==DefaultConfiguration.getInstance().getMinMemory()&&max==DefaultConfiguration.getInstance().getMaxMemory())
 		{
 			dropDown.setSelectedIndex(0);
			minSpinner.setValue(1024);
			maxSpinner.setValue(0);
 			setFieldsEnabled(false);
 		}
 		//Java-Default
 		else if(min<=0&&max<=0)
 		{
 			dropDown.setSelectedIndex(1);
 			minSpinner.setValue(0);
 			maxSpinner.setValue(0);
 			setFieldsEnabled(false);
 		}
 		//Custom
 		else
 		{
 			dropDown.setSelectedIndex(2);
 			minSpinner.setValue(min);
 			maxSpinner.setValue(max);
 			setFieldsEnabled(true);
 		}
 		dirty.setDirty();
 	}
 	public int getMinMemory()
 	{
 		return (int) minSpinner.getValue();
 	}
 	public int getMaxMemory()
 	{
 		return (int) maxSpinner.getValue();
 	}
 }
