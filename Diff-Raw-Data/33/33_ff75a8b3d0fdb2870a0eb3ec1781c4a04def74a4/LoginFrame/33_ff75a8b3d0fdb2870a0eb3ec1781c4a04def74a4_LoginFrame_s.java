 package com.nekocraft.launcher;
 import java.awt.*;  
 import java.awt.event.*;  
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.imageio.stream.ImageInputStream;
 import javax.swing.*;  
 public class LoginFrame extends JFrame{
     public LoginFrame(){
         this.setSize(new Dimension(400, 500));  
         this.setTitle("Nekocraft Launcher");  
         setResizable(false); 
         setLocationRelativeTo(getOwner());
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLayout(null);
         try{
         initPic();
         }
         catch (Exception e){
             NekoLauncher.handleException(e);
         }
         LabelTextField user=new LabelTextField("用户名");
         LabelTextField pass=new LabelTextField("密　码",true);
         user.setBounds(100, 280, 200, 25);
         pass.setBounds(100,370,200,25);
         this.getContentPane().add(user);
         this.getContentPane().add(pass);
     }
     private void initPic() throws IOException{
         JPanel panel=new JPanel(){
                     @Override
             protected void paintComponent(Graphics g) {
                 // TODO Auto-generated method stub
                 super.paintComponent(g);
                 try{
                 g.drawImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("background.jpg")), 0, 0, this);
                 }
                 catch (Exception e){
                     NekoLauncher.handleException(e);
                 }
             }
     };
                 panel.setLayout(null);
         this.setContentPane(panel);
         JLabel logo=new JLabel(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("nekocraft.png"))));
        //JLabel bk=new JLabel(new ImageIcon());
         logo.setBounds(0,0,400,130);
        //bk.setBounds(0,0,400,500);
         this.getContentPane().add(logo);
        //this.getContentPane().add(bk);
     }
 }
