 /*
 * Copyright (C) 2011 YasminApp Team. All rights reserved.
  */
 
 package com.yasminapp.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class YasminApp implements EntryPoint {
 
   private static final int AES_BLOCK_SIZE = 16;
   private final TextArea leftWindow = new TextArea();
   private final TextArea rightWindow = new TextArea();
 
   private final SimplePanel leftSizePanel = new SimplePanel();
   private final SimplePanel rightSizePanel = new SimplePanel();
   private boolean encrypting;
   private TextBox passphrase;
   private TextBox key;
   private TextArea plaintext;
   private TextArea cipher;
 
   public YasminApp() {
     this.leftWindow.setCharacterWidth(70);
     this.leftWindow.setVisibleLines(25);
     this.rightWindow.setCharacterWidth(70);
     this.rightWindow.setVisibleLines(25);
   }
 
   public void onModuleLoad() {
     // Set up uncaught exception handler
     if (GWT.isScript()) {
       GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
         public void onUncaughtException(Throwable e) {
           alert("Uncaught exception: " + e);
         }
       });
     }
 
     Document $doc = Document.get();
 
     passphrase = TextBox.wrap($doc.getElementById("passphrase"));
     key = TextBox.wrap($doc.getElementById("key"));
 
     Button encryptButton = Button.wrap($doc.getElementById("encrypt"));
     encryptButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         encrypt();
       }
     });
 
     plaintext = TextArea.wrap($doc.getElementById("plaintext"));
     cipher = TextArea.wrap($doc.getElementById("cipher"));
 
     Button encryptClearButton = Button.wrap($doc.getElementById("clear_plaintext"));
     encryptClearButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         plaintext.setText("");
       }
     });
 
     Button decryptButton = Button.wrap($doc.getElementById("decrypt"));
     decryptButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         decrypt();
       }
     });
 
     Button decryptClearButton = Button.wrap($doc.getElementById("cipher_clear"));
     decryptClearButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         cipher.setText("");
       }
     });
   }
 
   public void encrypt() {
     byte[] keyBytes = Hex.fromHex(key.getText());
     cipher.setValue(transform(keyBytes, plaintext.getValue(), true));
   }
 
   public void decrypt() {
     byte[] keyBytes = Hex.fromHex(key.getText());
     plaintext.setValue(transform(keyBytes, cipher.getValue(), false));
   }
 
   public static String transform(byte[] key, String plaintext, boolean encrypt) {
     AES aes = new AES();
     aes.init(encrypt, key);
     int offset = 0;
     byte[] inputBytes;
     if (encrypt) {
       inputBytes = UTF8.encode(plaintext);
       // (optional): compress
     } else {
       inputBytes = Base64.decode(plaintext);
       // (optional): decompress
     }
     int len = inputBytes.length;
     byte[] cipherBytes = new byte[len];
     while (offset + AES_BLOCK_SIZE <= len) {
       aes.processBlock(inputBytes, offset, cipherBytes, offset);
       offset += AES_BLOCK_SIZE; // AES block size
     }
     int remainder = len % AES_BLOCK_SIZE;
     if (remainder > 0) {
       byte[] padded = new byte[AES_BLOCK_SIZE];
       System.arraycopy(inputBytes, offset, padded, 0, AES_BLOCK_SIZE - remainder);
       aes.processBlock(padded, 0, cipherBytes, offset);
     }
     String result;
     if (encrypt) {
       // (optional): compress
       result = Base64.encode(cipherBytes);
     } else {
       // (optional): decompress
       result = UTF8.decode(cipherBytes);
     }
     return result;
   }
 
   public native void alert(String msg) /*-{
 		$wnd.alert(msg);
   }-*/;
 }
