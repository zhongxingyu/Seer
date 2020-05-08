 package edu.thu.cslab.footwith.form;
 
 import edu.thu.cslab.footwith.server.Mediator;
 import edu.thu.cslab.footwith.server.TextFormatException;
 
 import edu.thu.cslab.footwith.server.User;
 import edu.thu.cslab.footwith.server.UserManager;
 import org.json.JSONException;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Vector;
 import java.util.logging.SimpleFormatter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wangjiayu
  * Date: 13-3-16
  * Time: 下午4:03
  * To change this template use File | Settings | File Templates.
  */
 public class InsertPlanForm extends JFrame {
 
     private  JTextField siteName = new JTextField();
     private  JTextField startTime = new JTextField();
     private  JTextField endTime = new JTextField();
     private  JTextField organizer = new JTextField();
     private  JTextField numbers = new JTextField();
     Mediator dataSource = new Mediator();
     Vector<String> city;
     Vector<String>  siteNames =null;
 
     public  InsertPlanForm() {
         this.setBounds(200,200,400,200);
         init();
         this.setTitle("订制行程");
         this.setVisible(true);
     }
 
     private void init() {
 
         final  BorderLayout borderLayout = new BorderLayout();
         borderLayout.setVgap(5);
 
         getContentPane().setLayout(borderLayout);
 
 
         final JPanel mainPanel = new JPanel();
         mainPanel.setBorder(new EmptyBorder(5,10,5,10)); // need to understand
         final  GridLayout gridLayout = new GridLayout(4,4);
         gridLayout.setVgap(5);
         gridLayout.setHgap(5);
         mainPanel.setLayout(gridLayout);
         getContentPane().add(mainPanel,"North");
 
         JLabel planTitleLb1 = new JLabel("计划名称");
         mainPanel.add(planTitleLb1);
         final JTextField planTitle= new JTextField();
         mainPanel.add(planTitle);
 
         JLabel organizerLabel = new JLabel("组织者");
         mainPanel.add(organizerLabel);
         final JTextField organizer = new JTextField();
         mainPanel.add(organizer);
 
         JLabel groupNumMaxLabel = new JLabel("人数");
         mainPanel.add(groupNumMaxLabel);
         final JTextField groupNumMax = new JTextField();
         mainPanel.add(groupNumMax);
 
         JLabel siteCityLb1 = new JLabel(("景点城市"));
         mainPanel.add(siteCityLb1);
 
         city = dataSource.getAllLocations();
         city.insertElementAt("",0);
         final JComboBox cityCombBox = new JComboBox(city);
         mainPanel.add(cityCombBox);
 
         JLabel siteIdLb1 = new JLabel("景点1");
         final  JComboBox siteNameCom1 = new JComboBox();
         mainPanel.add(siteIdLb1);
         mainPanel.add(siteNameCom1);
 
         JLabel siteIdLb2 = new JLabel("景点2");
         final  JComboBox siteNameCom2 = new JComboBox();
         mainPanel.add(siteIdLb2);
         mainPanel.add(siteNameCom2);
 
 //        JLabel siteIdLb3 = new JLabel("景点3");
 //        final  JComboBox siteNameCom3 = new JComboBox();
 //        mainPanel.add(siteIdLb3);
 //        mainPanel.add(siteNameCom3);
 
         cityCombBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String item = cityCombBox.getSelectedItem().toString();
                 if(item.length() ==0 || item == null )
                     JOptionPane.showMessageDialog(null,"请选择城市");
                 else {
                     siteNames = new Vector<String>();
                     try {
 
                         siteNames = dataSource.selectSiteNameWithLocation(item.substring(0,item.length()));
                     } catch (TextFormatException e1) {
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     } catch (SQLException e1) {
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     //siteNames.addElement("");
                     siteNames.insertElementAt("",0);
                     DefaultComboBoxModel model = new DefaultComboBoxModel(siteNames);
                     // model.addElement("hello");
                     siteNameCom1.setModel(model);
             }
             }
         });
         siteNameCom1.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 DefaultComboBoxModel model = new DefaultComboBoxModel(siteNames);
                 siteNameCom2.setModel(model);
             }
         });
 //        siteNameCom2.addActionListener(new ActionListener(){
 //            @Override
 //            public void actionPerformed(ActionEvent e) {
 //
 //                DefaultComboBoxModel model = new DefaultComboBoxModel(siteNames);
 //                siteNameCom3.setModel(model);
 //            }
 //        });
 
 //        JLabel siteIdOther = new JLabel("其它景点");
 //        final  JTextField siteNameText = new JTextField();
 //        siteNameText.setText("请以，间隔");
 //        siteNameText.setEnabled(false);
 //
 //        mainPanel.add(siteIdOther);
 //        mainPanel.add(siteNameText);
 
         JLabel startTimeLb = new JLabel("开始时间");
         SimpleDateFormat myfmt = new SimpleDateFormat("yyyy-MM-dd");
         final JFormattedTextField startTime  = new JFormattedTextField(myfmt);
         startTime.setValue(new java.util.Date());
         mainPanel.add(startTimeLb);
         mainPanel.add(startTime);
 
         JLabel endTimeLb = new JLabel("结束时间");
         final JFormattedTextField endTime = new JFormattedTextField(myfmt);
         endTime.setValue(new Date());
         mainPanel.add(endTimeLb);
         mainPanel.add(endTime);
 
 
         final  JPanel bottomPanel = new JPanel();
         bottomPanel.setBorder(new LineBorder(SystemColor.activeCaptionBorder,1,false));
         getContentPane().add(bottomPanel,"South") ;
         final  FlowLayout flowLayout = new FlowLayout();
         flowLayout.setVgap(2);
         flowLayout.setHgap(30);
         flowLayout.setAlignment(FlowLayout.RIGHT);
         bottomPanel.setLayout(flowLayout);
         final JButton btnAdd = new JButton("增加");
         final JButton btnReset = new JButton("重置");
         bottomPanel.add(btnAdd);
         bottomPanel.add(btnReset);
 
         //       MyActionActionLister myBtnAddActionLister = new MyActionActionLister();
 
         btnAdd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
               SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
                 if(cityCombBox.getSelectedItem() == null || cityCombBox.getSelectedItem().toString().length() == 0){
                     JOptionPane.showMessageDialog(null,"请选择城市");
                 } else if(siteNameCom1.getSelectedItem() == null || siteNameCom1.getSelectedItem().toString().length() == 0){
                     JOptionPane.showMessageDialog(null,"请选择景点");
                 }else if(organizer.getText().length() ==0 || groupNumMax.getText().length() == 0)
                      {
                          JOptionPane.showMessageDialog(null,"信息不全");
                      } else{
 
                          int response =  JOptionPane.showConfirmDialog(null,"确定提交","are you sure",JOptionPane.YES_NO_OPTION);
                          if(response == JOptionPane.YES_OPTION){
                              Mediator pm=new Mediator();
                              try{
<<<<<<< HEAD
                                  String city = cityCombBox.getSelectedItem().toString();
                                  String site = siteNameCom1.getSelectedItem().toString()+","+siteNameCom2.getSelectedItem().toString();
                                  pm.addPlanFromForm(planTitle.getText().toString(), new UserManager().selectUser(Global.username).getUserID(),new Integer(groupNumMax.getText().toString()).intValue(),siteNameCom1.getSelectedItem().toString(),siteNameCom2.getSelectedItem().toString(),startTime.getText().toString(), endTime.getText().toString());
                                  JOptionPane.showMessageDialog(null,"添加成功！");
                               //   pm.addPlanFromForm(site, 0,0,startTime.getText(),endTime.getText(),organizer.getText(),null);
                                  // function need to be improved
=======
                                 pm.addPlanFromForm(siteName.getText(),0,0, startTime.getText(),endTime.getText(),organizer.getText(),null);
>>>>>>> roselone
                              }catch (TextFormatException e1) {
                                  JOptionPane.showMessageDialog(null,"格式错误，参与人数为数字");
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              } catch (SQLException e1) {
 
                                  JOptionPane.showMessageDialog(null,"不存在用户");
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              } catch (JSONException e1) {
                                  JOptionPane.showMessageDialog(null,"格式错误，参与人数为数字");
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              }
 
                          }
                       }
 //                }
               }
             });
         btnReset.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                    // startTime.setText("");
                    // endTime.setText("");
                     organizer.setText("");
                     groupNumMax.setText("");
                     planTitle.setText("");
 
             }
         });
 
 
         
     }
 
 
     class MyActionActionLister implements  ActionListener{
         @Override
         public void actionPerformed(ActionEvent e) {
 
         }
 
     }
 }
