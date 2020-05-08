 package pro.kornev.kcontrol.view.panels.settings;
 
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 import pro.kornev.kcontrol.service.SettingService;
 import pro.kornev.kcontrol.service.SettingsListener;
 import pro.kornev.kcontrol.service.joystick.KJoystick;
 import pro.kornev.kcontrol.service.network.ProxyService;
 import pro.kornev.kcontrol.service.network.ProxyServiceListener;
 import pro.kornev.kcontrol.view.panels.CustomPanel;
 import pro.kornev.kcontrol.view.panels.state.PreviewPanel;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.nio.ByteBuffer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vkornev
  * Date: 18.11.13
  * Time: 10:38
  */
 public class PreviewSettings extends CustomPanel implements SettingsListener, ActionListener, ProxyServiceListener {
     private ProxyService proxyService = null;
     private JCheckBox state;
     private JTextField fps = null;
     private JTextField quality = null;
     private JCheckBox flash;
     private JComboBox<PreviewSize> sizes;
 
     public PreviewSettings() {
         super("Preview settings");
         JButton apply = new JButton("Apply");
         apply.addActionListener(this);
         state = new JCheckBox("Camera switch on");
         JLabel fpsLabel = new JLabel("FPS:");
         fps = new JTextField("1");
         JLabel qualityLabel = new JLabel("Quality:");
         quality = new JTextField("50");
         flash = new JCheckBox("Switch on LED flash");
         JLabel sizesLabel = new JLabel("Preview sizes:");
         sizes = new JComboBox<>();
         JButton getSizes = new JButton("get");
 
         int y = 0;
         add(state, getGbl().setGrid(0, y++));
 
         add(fpsLabel, getGbl().setGrid(0, y).weightH(0.2));
         add(fps, getGbl().setGrid(1, y++).weightH(0.8));
 
         add(qualityLabel, getGbl().setGrid(0, y));
         add(quality, getGbl().setGrid(1, y++));
 
         add(flash, getGbl().setGrid(0, y++).colSpan());
 
         JPanel sizesPanel = new JPanel(new GridBagLayout());
         sizesPanel.add(sizes, getGbl().setGrid(0, 0).weightH(0.9));
         sizesPanel.add(getSizes, getGbl().setGrid(1, 0).weightH(0.1));
         getSizes.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 getSizes();
             }
         });
         add(sizesLabel, getGbl().setGrid(0, y));
         add(sizesPanel, getGbl().setGrid(1, y++));
 
         add(apply, getGbl().setGrid(1, y));
 
         SettingService.i.addListener(this);
     }
 
     @Override
     public void changeJoystick(KJoystick joystick) {
     }
 
     @Override
     public void changeProxy(ProxyService proxyService) {
         this.proxyService = proxyService;
         proxyService.addListener(this);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         apply();
     }
 
     private void getSizes() {
         if (proxyService == null) return;
         Data data = new Data();
         data.cmd = Protocol.Cmd.camSizeList();
         proxyService.send(data);
     }
 
     private void apply() {
         Data data;
         if (PreviewPanel.isStartPreview()) {
             data = new Data();
             data.cmd = Protocol.Cmd.camState();
            data.bData = Protocol.Req.off();
             proxyService.send(data);
         }
 
         data = new Data();
         data.cmd = Protocol.Cmd.camState();
         data.bData = state.isSelected() ? Protocol.Req.on() : Protocol.Req.off();
         proxyService.send(data);
 
         if (!state.isSelected()) return;
 
         data = new Data();
         data.cmd = Protocol.Cmd.camFps();
         data.bData = Byte.valueOf(fps.getText());
         proxyService.send(data);
 
         data = new Data();
         data.cmd = Protocol.Cmd.camQuality();
         data.bData = Byte.valueOf(quality.getText());
         proxyService.send(data);
 
         data = new Data();
         data.cmd = Protocol.Cmd.camFlash();
         data.bData = (byte)(flash.isSelected() ? 1 : 0);
         proxyService.send(data);
 
         PreviewSize size = (PreviewSize)sizes.getSelectedItem();
         if (size == null) return;
         data = new Data();
         data.cmd = Protocol.Cmd.camSizeSet();
         data.type = Protocol.arrayType();
         ByteBuffer bb = ByteBuffer.allocate(8);
         bb.putInt(size.width);
         bb.putInt(size.height);
         data.aData = bb.array();
         data.aSize = data.aData.length;
         proxyService.send(data);
 
         if (PreviewPanel.isStartPreview()) {
             data = new Data();
             data.cmd = Protocol.Cmd.camState();
            data.bData = Protocol.Req.on();
             proxyService.send(data);
         }
     }
 
     @Override
     public void onPackageReceive(Data data) {
         if (data.cmd != Protocol.Cmd.camSizeList()) {
             return;
         }
         sizes.removeAllItems();
         ByteBuffer bb = ByteBuffer.wrap(data.aData);
         for (int i=data.aSize/8; i>0; i--) {
             PreviewSize size = new PreviewSize();
             size.width = bb.getInt(); //width
             size.height = bb.getInt(); //height
             sizes.addItem(size);
         }
     }
 }
