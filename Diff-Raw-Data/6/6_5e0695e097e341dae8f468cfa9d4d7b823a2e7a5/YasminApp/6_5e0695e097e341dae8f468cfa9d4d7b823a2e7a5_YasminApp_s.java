 /*
  * Copyright (C) 2011 YasminApp Team. All rights reserved.
  */
 
 package com.yasminapp.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class YasminApp implements EntryPoint {
 
   private static final int AES_BLOCK_SIZE = 16;
 
   private TextBox passphrase;
   private ListBox keylist_enc;
   private ListBox keylist_dec;
   private TextArea plaintext;
   private TextArea cipher;
 
   public YasminApp() {
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
 
     // temp variable used for null checking
     Element e = $doc.getElementById("passphrase").cast();
     assert (e != null);
     passphrase = TextBox.wrap(e);
 
     e = $doc.getElementById("encrypt").cast();
     assert (e != null);
     Anchor encryptButton = Anchor.wrap(e);
     encryptButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         encrypt();
       }
     });
 
     e = $doc.getElementById("plaintext").cast();
     assert (e != null);
     plaintext = TextArea.wrap(e);
 
     e = $doc.getElementById("cipher").cast();
     assert (e != null);
     cipher = TextArea.wrap(e);
 
     e = $doc.getElementById("do-decrypt").cast();
     assert (e != null);
     Anchor decryptButton = Anchor.wrap(e);
     decryptButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         decrypt();
       }
     });
 
     e = $doc.getElementById("generate-key").cast();
     assert (e != null);
     Button generateKey = Button.wrap(e);
     generateKey.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         generate_key();
       }
     });
 
     String[] keys = load_keys();
     e = $doc.getElementById("keylist-enc").cast();
     assert (e != null);
     ListBox keylist_enc = new ListBox();
     for (String key : keys) {
       keylist_enc.addItem(key);
     }
     keylist_enc.setVisibleItemCount(1);
 
     e = $doc.getElementById("keylist-dec").cast();
     assert (e != null);
     ListBox keylist_dec = new ListBox();
     for (String key : keys) {
       keylist_dec.addItem(key);
     }
     keylist_dec.setVisibleItemCount(1);
   }
 
   byte[] padToLength(byte[] in, int len) {
     byte[] out = in;
     if (in.length < len) {
       out = new byte[len];
       System.arraycopy(in, 0, out, 0, in.length);
     }
     return out;
   }
 
   public String generate_key() {
    return new String("No key for you! :D~~~~~~~~~~~~~~");
   }
 
   public void store_keys(String[] keylist) {
     alert("Implement me, bitches!");
   }
 
   public String[] load_keys() {
     return new String[] { "This", "space", "intentionally", "left", "blank" };
   }
 
   public void encrypt() {
     String key = keylist_enc.getValue(keylist_enc.getSelectedIndex());
     byte[] keyBytes = Hex.fromHex(key);
     AES aes = new AES();
     aes.init(true, keyBytes);
     int offset = 0;
     byte[] inputBytes;
 
     inputBytes = UTF8.encode(plaintext.getValue());
     // (optional): compress
     int len = inputBytes.length;
 
     if (len % AES_BLOCK_SIZE > 0) {
       len = ((len / AES_BLOCK_SIZE) + 1) * AES_BLOCK_SIZE;
     }
 
     inputBytes = padToLength(inputBytes, len);
 
     byte[] cipherBytes = new byte[len];
     while (offset + AES_BLOCK_SIZE <= len) {
       aes.processBlock(inputBytes, offset, cipherBytes, offset);
       offset += AES_BLOCK_SIZE; // AES block size
     }
     int remainder = len % AES_BLOCK_SIZE;
     if (remainder > 0) {
       byte[] padded = new byte[AES_BLOCK_SIZE];
       System.arraycopy(inputBytes, offset, padded, 0, remainder);
       aes.processBlock(padded, 0, cipherBytes, 0);
     }
     String result = Base64.encode(cipherBytes);
     plaintext.setValue(result);
     cipher.setValue(result);
   }
 
   public void decrypt() {
     String key = keylist_dec.getValue(keylist_enc.getSelectedIndex());
     byte[] keyBytes = Hex.fromHex(key);
     AES aes = new AES();
     aes.init(false, keyBytes);
     int offset = 0;
     byte[] cipherTextBytes;
     cipherTextBytes = Base64.decode(cipher.getValue());
     // (optional): decompress
 
     int len = cipherTextBytes.length;
     int remainder = len % AES_BLOCK_SIZE;
     if (remainder != 0) {
       alert("Invalid ciphertext!");
       return;
     }
     byte[] plainTextBytes = new byte[len];
     while (offset + AES_BLOCK_SIZE <= len) {
       aes.processBlock(cipherTextBytes, offset, plainTextBytes, offset);
       offset += AES_BLOCK_SIZE; // AES block size
     }
     String result = UTF8.decode(plainTextBytes);
     plaintext.setValue(result);
   }
 
   public native void alert(String msg) /*-{
 		$wnd.alert(msg);
   }-*/;
 }
