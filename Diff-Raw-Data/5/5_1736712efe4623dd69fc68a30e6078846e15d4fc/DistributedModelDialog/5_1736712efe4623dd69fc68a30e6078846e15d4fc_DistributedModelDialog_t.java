 package jDistsim.ui.dialog;
 
 import jDistsim.application.designer.common.Application;
 import jDistsim.core.simulation.distributed.DistributedModelDefinition;
 import jDistsim.ui.TypeInputValidator;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 2.3.13
  * Time: 13:53
  */
 public class DistributedModelDialog extends BaseDialog {
 
     private JTextField modelNameTextField;
     private JTextField rmiModelNameTextField;
     private JTextField addressTextField;
     private JTextField portTextField;
     private JCheckBox lookaheadCheckBox;
     private DistributedModelDefinition distributedModelDefinition;
 
     public DistributedModelDialog(JFrame parent) {
         this(parent, DistributedModelDefinition.createDefault());
     }
 
     public DistributedModelDialog(JFrame parent, DistributedModelDefinition distributedModelDefinition) {
         super(parent, "Adding a new distributed model");
         this.distributedModelDefinition = distributedModelDefinition;
     }
 
 
     @Override
     protected void initializeUI() {
         setTitle("Configure distributed model dialog");
         modelNameTextField.setText(distributedModelDefinition.getModelName());
         rmiModelNameTextField.setText(distributedModelDefinition.getRmiModelName());
         addressTextField.setText(distributedModelDefinition.getAddress());
         portTextField.setText(String.valueOf(distributedModelDefinition.getPort()));
         lookaheadCheckBox.setSelected(distributedModelDefinition.isLookahead());
     }
 
     @Override
     protected void buildWindowBody() {
         modelNameTextField = getComponentFactory().makeTextField(13);
         rmiModelNameTextField = getComponentFactory().makeTextField(13);
         addressTextField = getComponentFactory().makeTextField();
         portTextField = getComponentFactory().makeTextField(5);
         lookaheadCheckBox = getComponentFactory().makeCheckBox("lookahead  (sending  null messages)");
         setSize(new Dimension(getWidth(), 210));
 
         constraints.gridwidth = 1;
         build(getComponentFactory().makeLabel("RMI model name"));
         constraints.gridwidth = GridBagConstraints.REMAINDER;
         build(getComponentFactory().makeLabel("Model name (only local)"));
 
         constraints.gridwidth = 1;
         constraints.insets = new Insets(0, 0, 8, 5);
         build(rmiModelNameTextField);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        build(modelNameTextField);
 
         constraints.gridwidth = 1;
         constraints.insets = new Insets(0, 0, 0, 0);
         build(getComponentFactory().makeLabel("Remote address"));
         constraints.gridwidth = GridBagConstraints.REMAINDER;
         build(getComponentFactory().makeLabel("Remote port"));
 
         constraints.insets = new Insets(0, 0, 8, 5);
         constraints.gridwidth = 1;
         build(addressTextField);
         build(portTextField);
         constraints.gridwidth = GridBagConstraints.REMAINDER;
         build(getComponentFactory().makeLabel(new String()));
 
         constraints.insets = new Insets(0, 0, 0, 0);
         constraints.gridwidth = GridBagConstraints.REMAINDER;
         build(lookaheadCheckBox);
     }
 
     @Override
     protected boolean okButtonLogic() {
         try {
             Iterable<String> models = Application.global().getDistributedModels().keys();
             TypeInputValidator validator = new TypeInputValidator();
             String modelName = validator.validateString(modelNameTextField.getText(), "Model name");
             String rmiModelName = validator.validateString(rmiModelNameTextField.getText(), "RMI model name");
             rmiModelName = validator.validateSpecialCharacters(rmiModelName, "RMI model name (contains special characters)");
             rmiModelName = validator.validateDuplicity(models, rmiModelName, "RMI model name (duplicity)");
             String address = validator.validateString(addressTextField.getText(), "Remote address");
             int port = validator.validateInteger(portTextField.getText(), "Remote port");
             boolean lookahead = lookaheadCheckBox.isSelected();
 
             distributedModelDefinition.setModelName(modelName);
             distributedModelDefinition.setRmiModelName(rmiModelName);
             distributedModelDefinition.setAddress(address);
             distributedModelDefinition.setPort(port);
             distributedModelDefinition.setLookahead(lookahead);
             return true;
         } catch (Exception exception) {
             return false;
         }
     }
 
     public DistributedModelDefinition getDistributedModelDefinition() {
         return distributedModelDefinition;
     }
 }
