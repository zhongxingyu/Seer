 package model;
 
 import java.io.FileInputStream;
 import java.io.DataInputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 
 import java.util.ArrayList;
 
 public class FileParser {
     
     private IOverlord overlord;
     
     public FileParser(IOverlord overlord) {
         this.overlord = overlord;
     }
     
     public void loadFile(String fileName) {
         ArrayList<String> input = new ArrayList<String>();
         String strLine = "";
         
         try {
             FileInputStream fstream = new FileInputStream(fileName);
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             
             while((strLine = br.readLine()) != null) {
                 input.add(strLine);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         generate(input);
     }
     
     private void generate(ArrayList<String> input) {
         for(String s : input) {
             String[] tok = s.split("\\s");
             
             switch(tok[0]) {
                 case "":  //blank line in file
                     break;
                 case "Absorber":
                     overlord.addAbsorber(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]), Integer.parseInt(tok[4]), Integer.parseInt(tok[5]));
                     break;
                 case "Ball":
                    overlord.addBall(tok[1], Float.parseFloat(tok[2]), Float.parseFloat(tok[3]), Float.parseFloat(tok[4]), Float.parseFloat(tok[5]));
                     break;
                 case "Circle":
                     overlord.addCircle(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]));
                     break;
                 case "LeftFlipper":
                     overlord.addFlipper(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]), false);
                     break;
                 case "RightFlipper":
                     overlord.addFlipper(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]), true);
                     break;
                 case "Square":
                     overlord.addSquare(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]));
                     break;
                 case "Triangle":
                     overlord.addTriangle(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]));
                     break;
                 case "Gravity":
                     overlord.setGravity(Float.parseFloat(tok[1]));
                     break;
                 case "Friction":
                     overlord.setFriction(Float.parseFloat(tok[1]), Float.parseFloat(tok[2]));
                     break;
                 case "Move":
                     overlord.moveGizmo(tok[1], Integer.parseInt(tok[2]), Integer.parseInt(tok[3]));
                     break;
                 case "Rotate":
                     overlord.rotateGizmo(tok[1]);
                     break;
                 case "Connect":
                     overlord.connect(tok[1], tok[2]);
                     break;
                 case "KeyConnect":
                     boolean temp = false;
                     if(tok[3].equals("up")) {
                         temp = true;
                     } else if(tok[3].equals("down")) {
                         temp = false;
                     } else {
                         //error
                     }
                     overlord.keyConnect(Integer.parseInt(tok[2]), temp, tok[4]);
                     break;
                 case "Delete":
                    overlord.removeFromBoard(tok[1]);
                     break;
                 default: //Error throw up error message dialog box
                     break;
             }
         }
     }
     
     public void saveFile() {
         
     }
     
 }
