 /*
  *   casmi examples
  *   http://casmi.github.com/
 *   Copyright (C) 2011, Xcoo, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package casmi.sound;
 
 import java.util.ArrayList;
 import java.util.List;
 
import casmi.sound.AudioPlayer;
import casmi.sound.Sound;

 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.element.Line;
 
 /**
  * Sound example.
  * 
  * @author Y. Ban
  */
 public class SoundExample extends Applet {
 
     static final String RESOURCE_PATH = Sound.class.getResource("sample.mp3").getPath();
     
     Sound sound;
     AudioPlayer player;
     List<Line> leftLines;
     List<Line> rightLines;
 
     @Override
     public void setup() {
         setSize(512, 200);
         sound = new Sound();
         player = sound.loadFile(RESOURCE_PATH, 2048);
         leftLines = new ArrayList<Line>();
         rightLines = new ArrayList<Line>();
         setLine();
     }
 
     public void setLine() {
         for (int i = 0; i < player.bufferSize() - 1; i++) {
             double x1 = map(i, 0, player.bufferSize(), 0, getWidth());
             double x2 = map(i + 1, 0, player.bufferSize(), 0, getWidth());
             leftLines.add(new Line(x1, 50 + player.left().get(i) * 50, x2, 50 + player.left().get(i + 1) * 50));
             rightLines.add(new Line(x1, 150 + player.right().get(i) * 50, x2, 150 + player.right().get(i + 1) * 50));
             leftLines.get(i).setStrokeColor(ColorSet.AQUAMARINE);
             rightLines.get(i).setStrokeColor(ColorSet.AQUAMARINE);
             addObject(leftLines.get(i));
             addObject(rightLines.get(i));
         }
     }
 
     static public final double map(double value,
                                    float istart, double istop,
                                    float ostart, double ostop) {
         return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
     }
 
     @Override
     public void exit() {
         player.close();
         sound.stop();
     }
 
     @Override
     public void update() {
         for (int i = 0; i < player.bufferSize() - 1; i++) {
             double x1 = map(i, 0, player.bufferSize(), 0, getWidth());
             double x2 = map(i + 1, 0, player.bufferSize(), 0, getWidth());
             leftLines.get(i).set(x1, 50 + player.left().get(i) * 50, x2, 50 + player.left().get(i + 1) * 50);
             rightLines.get(i).set(x1, 150 + player.right().get(i) * 50, x2, 150 + player.right().get(i + 1) * 50);
         }
     }
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {}
 
     @Override
     public void keyEvent(KeyEvent e) {
         if (e == KeyEvent.PRESSED) {
             switch(getKey()) {
             case 's':
                 player.play();
                 break;
             case 'q':
                 player.close();
                 break;
             case 'p':
                 player.pause();
                 break;
             }
         }
     }
 
     public static void main(String[] args) {
         AppletRunner.run("casmi.sound.SoundExample", "Sound Example");
     }
 
 }
