 package com.lavida.swing.form;
 
 import com.lavida.swing.LocaleHolder;
 import org.springframework.context.MessageSource;
 import org.springframework.context.MessageSourceAware;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * AbstractForm
  * <p/>
  * Created: 20:25 08.08.13
  *
  * @author Pavel
  */
 public abstract class AbstractForm implements MessageSourceAware {
     protected JFrame form;
     protected Container rootContainer;
 
     @Resource
     protected MessageSource messageSource;
 
     @Resource
     protected LocaleHolder localeHolder;
 
     @PostConstruct
     public void init() {
         form = new JFrame();
         initializeForm();
         rootContainer = form.getContentPane();
         initializeComponents();
     }
 
     protected void initializeForm() {
         form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         form.setResizable(false);
     }
 
     protected abstract void initializeComponents();
 
     public void showMessage(String titleKey, String messageKey) {
         JOptionPane.showMessageDialog(form,
                 messageSource.getMessage(messageKey, null, localeHolder.getLocale()),
                 messageSource.getMessage(titleKey, null, localeHolder.getLocale()),
                JOptionPane.DEFAULT_OPTION);
     }
 
     public void showMessageBox(String titleKey, String messageBody) {
         JOptionPane.showMessageDialog(form, messageBody,
                 messageSource.getMessage(titleKey, null, localeHolder.getLocale()),
                JOptionPane.WARNING_MESSAGE);
 
     }
 
     public void show() {
         form.setVisible(true);
     }
 
     public void hide() {
         form.dispose();
     }
 
     public void update() {
         form.repaint();
     }
 
     public JFrame getForm() {
         return form;
     }
 
     @Override
     public void setMessageSource(MessageSource messageSource) {
         this.messageSource = messageSource;
     }
 }
