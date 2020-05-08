 package bitcity;
 
 import java.awt.Point;
 import java.awt.Graphics2D;
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 public class Car extends MovingObject {
 	private static double changeDirectionProb = 0.2;
 	private World world;
 	private int state = World.CAR_RUNING;
 	
 	public Car(World world, Point startPos, char direction) {
 		super(startPos, direction);
 		this.world = world;
 	}
 	
 	public static Car createCar(World world, Point startPos) throws Exception {
 		char up, down, left, right;
 		char direction;
 		
 		/* XXX Sempre considere x e y como linha e coluna, respectivamente. */
 		up = world.getElementAt(startPos.x - 1, startPos.y);
 		down = world.getElementAt(startPos.x + 1, startPos.y);
 		left = world.getElementAt(startPos.x, startPos.y - 1);
 		right = world.getElementAt(startPos.x, startPos.y + 1);
 		if (up != ' ' && up != Parser.SENTINEL && up != Parser.SIDEWALK) {
 			direction = up;
 		} else if (down != ' ' && down != Parser.SENTINEL && down != Parser.SIDEWALK) {
 			direction = down;
 		} else if (left != ' ' && left != Parser.SENTINEL && left != Parser.SIDEWALK) {
 			direction = left;
 		} else if (right != ' ' && right != Parser.SENTINEL && right != Parser.SIDEWALK) {
 			direction = right;
 		} else {
 			throw new Exception("No initial direction found.");
 		}
 		
 		return new Car(world, startPos, direction);
 	}
 	
 	public synchronized void move() throws Exception {
 		Point nextPos;
 		double prob;
 		char ahead = this.world.getElementAhead(this.direction, this.pos);
 		
		
 		if (ahead == Parser.SENTINEL || ahead == Parser.GARAGE) {
 			/* '&' simboliza estacionamento agora. */
 			throw new Exception("Destroy car");
 		}
 		
 		Point pAhead = this.getNextPos(this.direction, this.pos);
 		
 		if (this.world.getSemaphores().containsKey(ahead)) {
 			
 			if (this.world.getSemaphores().get(ahead).isopen(pAhead)){
 				if (this.world.getRoadElement(pAhead.x, pAhead.y) != World.CAR_STOPED){
					this.world.setRoadElement(this.pos.x, this.pos.y, World.ROAD);
 					this.pos = this.getNextPos(this.direction, this.pos); /* XXX */
 				} else {
 					this.state = World.CAR_STOPED;
 					this.world.setRoadElement(this.pos.x, this.pos.y, World.CAR_STOPED);
					//this.bell();
 				}
 			} else {
 				this.state = World.CAR_STOPED;
 				this.world.setRoadElement(this.pos.x, this.pos.y, World.CAR_STOPED);
 				System.out.println(this.world.getRoadElement(this.pos.x, this.pos.y));
 			}
 			
 		} else {
 			System.out.println("ahead " + ahead );
 			if (this.world.getRoadElement(pAhead.x, pAhead.y) != World.CAR_STOPED){
				this.world.setRoadElement(this.pos.x, this.pos.y, World.ROAD);
 				this.pos = this.getNextPos(this.direction, this.pos);
 				if (ahead != ' ' && ahead != Parser.SIDEWALK && ahead != this.direction) {
 					/* Chance de mudar de dire��o. */
 					nextPos = this.getNextPos(ahead, this.pos);
 					if (this.world.getElementAt(nextPos) == ahead) {
 						/* Com certeza precisa mudar de dire��o. */
 						this.direction = ahead;
 					} else {
 						/* Tem chance de trocar de dire��o. */
 						prob = Math.random();
 						if (prob < changeDirectionProb) {
 							this.direction = ahead;
 						}
 					}
 				}
 			} else {
 				System.out.println("proximo esta parado");
 				this.state = World.CAR_STOPED;
 				//this.world.setRoadElement(this.pos.x, this.pos.y, World.CAR_STOPED);
 			}
 			
 			
 		}
 		this.world.setRoadElement(this.pos.x, this.pos.y, this.state);
 	}
 	
 	/* XXX Not used */
 	public void draw(Graphics2D g) {
 		g.setColor(Color.BLUE);
 		g.drawRect(this.pos.x, this.pos.y, 10, 10);
 	}
 	
 	public void run() {
 		while (true) {
 			try {
 				this.move();
 				Thread.sleep((int)(1000/20.));
 			} catch (Exception e) {
 				System.out.println(">>>> " + e.getMessage());
 				break;
 			}
 			//System.out.println(this.pos);
 		}
 		
 	}
 	
 	public void bell(){
 			 
 		new Thread(new Runnable () {
 			
 			public void run(){
 		        File soundFile = new File("data/beepbeep.wav");
 		 
 		        AudioInputStream audioInputStream = null;
 		        try { 
 		            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
 		        } catch (UnsupportedAudioFileException e1) { 
 		            e1.printStackTrace();
 		            return;
 		        } catch (IOException e1) { 
 		            e1.printStackTrace();
 		            return;
 		        } 
 		 
 		        AudioFormat format = audioInputStream.getFormat();
 		        SourceDataLine auline = null;
 		        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 		 
 		        try { 
 		            auline = (SourceDataLine) AudioSystem.getLine(info);
 		            auline.open(format);
 		        } catch (LineUnavailableException e) { 
 		            e.printStackTrace();
 		            return;
 		        } catch (Exception e) { 
 		            e.printStackTrace();
 		            return;
 		        } 
 		 
 		        auline.start();
 		        int nBytesRead = 0;
 		        byte[] abData = new byte[524288]; // 128Kb 
 		 
 		        try { 
 		            while (nBytesRead != -1) { 
 		                nBytesRead = audioInputStream.read(abData, 0, abData.length);
 		                if (nBytesRead >= 0) 
 		                    auline.write(abData, 0, nBytesRead);
 		            } 
 		        } catch (IOException e) { 
 		            e.printStackTrace();
 		            return;
 		        } finally { 
 		            auline.drain();
 		            auline.close();
 		        }
 			}
 		}).start();
 	}
 }
 
