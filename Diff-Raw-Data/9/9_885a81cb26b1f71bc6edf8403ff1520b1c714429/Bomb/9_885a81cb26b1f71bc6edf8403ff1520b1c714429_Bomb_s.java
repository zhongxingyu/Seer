 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: henke
  * Date: 9/26/13
  * Time: 2:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Bomb {
     private ID id;
     private Timer bombTimer, kickTimer;
     private int explosionRadius;
     private BlockType originalBlock;
     private Playfield background;
     private Position position;
     private List<Player> playerList;
 
     public Bomb(ID id, Position position, int explosionRadius, Playfield background, List<Player> playerList){
         this.id = id;
         this.position=position;
         this.explosionRadius = explosionRadius;
         this.background=background;
         this.playerList=playerList;
     }
 
     public ID getId(){
         return id;
     }
 
     public Position getPosition(){
         return position;
     }
 
     public void activate(){
         final Action explode = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 explode();
             }
         };
         originalBlock = background.getData(position);
         background.setData(position, BlockType.BOMB);
         bombTimer = new Timer(3000, explode);
         bombTimer.setCoalesce(true);
         bombTimer.start();
     }
 
     public void kick(final int xDirection, final int yDirection){
         final Position newPosition = new Position(position);
         final Action fly = new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 newPosition.nextPosition(xDirection, yDirection);
                 //logiskt test med player är inte testat
                for (int i = 0; i <= playerList.size()-1; i++) {
                    if(background.getData(newPosition).isWalkable()&&(!newPosition.equals(playerList.get(i).getPosition()))){
                         background.setData(position, originalBlock);
                         position.nextPosition(xDirection, yDirection);
                         originalBlock=background.getData(position);
                         background.setData(position, BlockType.BOMB);
                     }else{
                         kickTimer.stop();
                     }
                }
             }
         };
         kickTimer = new Timer(500, fly);
         kickTimer.setCoalesce(true);
         kickTimer.start();
     }
 
     public void explode(){
         bombTimer.stop();
 
         playerList.get(id.read()).deactivateBomb(position);
         //kills player on top of the bomb
         for (int i = 0; i <= playerList.size()-1; i++) {
             if(position.equals(playerList.get(i).getPosition())){
                 playerList.get(i).kill();
             }
         }
         explosionRadius(new Position(position), true, 1);
         explosionRadius(new Position(position), true, -1);
         explosionRadius(new Position(position), false, 1);
         explosionRadius(new Position(position), false, -1);
         background.setData(position, originalBlock);
     }
 
     //Axis == true => X-axis
     //Axis == false => Y-axis
     public void explosionRadius(Position position, boolean axis, int n){
 
         for(int i = 0; i <explosionRadius; i++){
             if (axis)
                 position.setX(position.getX() + n);
             else
                 position.setY(position.getY() + n);
             //for (int j = 0; j <= playerList.size()-1; j++) {
             if(position.equals(playerList.get(0).getPosition())){
                 playerList.get(0).kill();
                 break;
             }else if(position.equals(playerList.get(1).getPosition())){
                 playerList.get(1).kill();
                 break;
             } else if(background.getData(position).isDestructible()&&!background.getData(position).isWalkable()){
                 background.placeReplacement(position);
                 break;
             }else if (!background.getData(position).isDestructible()) {
                 break;
             }
             //fuling för att kunnna se explosionen
             //finns del funktion i playfield, clearFromExplosion, anropas från moveRight i player
             //else{
             // background.setData(position, BlockType.EXPLOSION);
             // }
         }
     }
 }
