 package jumpignon;
 
 import org.newdawn.slick.*;
 
 public class Player extends RenderItem{
     
     private int health;
     private int player_id;
     private int kill_score;
     private int death_score;
     private float y_velocity;
     private float x_velocity;
     private boolean isInAir;
     private Sound jumpSound;
     private Sound loseHealthSound;
     private Sound dieSound;
     
     // Infos zum Rendern
     // Ein Spieler hat immer 3 Bilder, die er brauch.
     // Eines für den Zustand in dem er noch "gesund" ist,
     // eines in dem er "angeschlagen" ist und noch eines 
     // wenn er nochmals getroffen wurde.
     private Image image;
     private Image image2;
     private Image image3;
     
     
     public Player(int h_id, int h_x, int h_y) throws SlickException
     {
         health = 3;
         player_id = h_id;
         kill_score = 0;
         death_score = 0;
         width = 48;
         height = 54;
         isInAir = false;
         y_velocity = 0.0f;
         jumpSound = new Sound("resources/sound/player_jump.wav");
         loseHealthSound = new Sound("resources/sound/player_lose_health.wav");
         dieSound = new Sound("resources/sound/player_die.wav");
         
         this.respawn(h_x, h_y);
     }
   
     public void setImage(Image a, Image b, Image c){image = a;image2 = b;image3 = c;}
     
     public int showKills(){return kill_score;}
     
     public int showDeaths(){return death_score;}
     
     public boolean isFalling()
     {
         if(isInAir == true)
         {
             if(y_velocity > 0)  // Der Spieler springt offensichtlich
             {
                 return false;
             }
             else    // Nur wenn eine Abwärtsbewegung stattfindet fällt der Spieler tatsächlich
             {
                 return true;
             }
         }
         else{ return false; }
     }
     
     public void jump()
     {
         y_velocity = 12.0f;
     }
     
     public void die()
     {
         death_score += 1;
         dieSound.play();
         respawn(getSpawnPoint(player_id), 420-height);
         
     }
     
     public void setFalling()
     {
         isInAir = true;
     }
     
     @Override
     public void renderMe(Graphics g) throws SlickException
     {
         if(health == 3){g.drawImage(image, pos_x, pos_y);}
         else if(health == 2){g.drawImage(image2, pos_x, pos_y);}
         else if(health ==1){g.drawImage(image3, pos_x, pos_y);}
         
         if(this.follower != null)
         {
             this.follower.renderMe(g);
         }
     }
     
     public void checkBottomCollisionWithPlayer(Player p1)
     {
         float linkerRandPlayer1 = this.pos_x;
         float rechterRandPlayer1 = this.pos_x + this.width;
         
         float linkerRandPlayer2 = p1.pos_x;
         float rechterRandPlayer2 = p1.pos_x + p1.width;
         
         if(     this.pos_y <= ( p1.get_height() + p1.get_pos_y() )        &&
                 this.pos_y >= ( p1.get_height() + p1.get_pos_y() - 25)    &&
                 linkerRandPlayer1 <= rechterRandPlayer2                   &&
                 rechterRandPlayer1 >= linkerRandPlayer2                   &&
                 
                 p1.isFalling() == true  ) 
             
         {
             if(this.health == 1){p1.gainKill();}
             p1.jump();
             this.loseHp();
         }
     }
     
     public void gainKill()
     {
         kill_score += 1;
     }
     
     public void bottomCollisionWithObject(RenderItem i1)
     {
         isInAir = false;            // der Spieler muss zuvor gesprungen sein, also wird dieser Zustand gelöscht
         y_velocity = 0.0f;          // die Gravitation greift nicht wenn der Spieler auf einem Objekt steht
         this.pos_y = i1.pos_y - (this.height);      // der Spieler soll auf dem Objekt stehen
     }
     
     public void update(GameContainer container, int delta)
     {
         if(isInAir == true) {x_velocity = 0.35f;}
         else                {x_velocity = 0.5f;}
         
         switch(player_id)
         {
             case(1):
                    
             // [<-] Links bewegung
             if(container.getInput().isKeyDown(Input.KEY_LEFT)){
                pos_x -= x_velocity * delta;
             }
             // [<-] Rechts bewegung
             if(container.getInput().isKeyDown(Input.KEY_RIGHT)){
                 pos_x += x_velocity * delta;
             }
             // [↕] Oben bewegung
             if(container.getInput().isKeyDown(Input.KEY_UP) && isInAir == false){
                 y_velocity = 1.0f * delta;
                 isInAir = true;
                 jumpSound.play();
             }
             
             break;
             case(2):
                    
             // [<-] Links bewegung
             if(container.getInput().isKeyDown(Input.KEY_A)){
               pos_x -= x_velocity * delta;
             }
             // [<-] Rechts bewegung
             if(container.getInput().isKeyDown(Input.KEY_D)){
                pos_x += x_velocity * delta;
             }
             // [↕] Oben bewegung
             if(container.getInput().isKeyDown(Input.KEY_W) && isInAir == false){
                 y_velocity = 1.0f * delta;
                isInAir = true;
                 jumpSound.play();
             }
             
             break;
         }
         
         if(isInAir == true && y_velocity >= -12.0f)
         {
             y_velocity -= 0.05f * delta;
         }
         
         pos_y -= y_velocity;
     }
     
     public int getSpawnPoint(int pid)
     {
        switch(pid)
             {
                 case(1):
                     return 100;
                 case(2):
                     return 770;
                 default:
                     return 450;       
             }
     }
     
     public void loseHp()
     {
         health -= 1;
         loseHealthSound.play();
         
         if(health == 0)
         {
             death_score++;
             dieSound.play();
             int respawn_x = getSpawnPoint(player_id);
             this.respawn(respawn_x, 420-54);      // PLAYER 1 KONSTANTE!!
         }
     }
     
     public void gainHp()
     {
         if(health != 2)
         {
             health++;
         }
     }
     
     public void respawn(int h_x, int h_y)
     {
         pos_x = h_x;
         pos_y = h_y;
         health = 3;
     }
     
 }
