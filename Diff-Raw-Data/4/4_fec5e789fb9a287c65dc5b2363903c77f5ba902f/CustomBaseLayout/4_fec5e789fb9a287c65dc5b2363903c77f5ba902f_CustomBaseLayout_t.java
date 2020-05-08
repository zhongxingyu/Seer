 package com.homenet.demo.ui;
 
 
 import com.homenet.bootstrap.loader.RootLayoutFactory;
 import com.homenet.bootstrap.loader.UIRootLoader;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.Label;
 
 import java.io.File;
 import java.io.InputStream;
 
 
 public class CustomBaseLayout implements UIRootLoader {
 
     @Override
     public void getRootFactory(RootLayoutFactory factory) {
         factory.setCompositionPage(new Button("test"));
         InputStream is = getClass().getResourceAsStream("/img/logo.png");
         factory.customizeLogo(is);
         CssLayout comp = new CssLayout(new Label("MenuItem"));
         comp.setStyleName("us-menu-layout");
         InputStream cssSource = getClass().getResourceAsStream("/styles/styles.css");
         factory.addCSSSource(cssSource);
         factory.customizeTopMenuCol2(comp);
        factory.customizeTopMenuCol3(new Label("MenuItem3"));
        factory.customizeTopMenuCol4(new Label("MenuItem4"));
 
     }
 
 }
