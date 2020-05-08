 package generators;
 
 import java.util.Random;
 import map.Map;
 
 public abstract class Perlin {
 
 	//Applique l'agorithme de Perlin sur la Map passe en paramtre
 	public static void appliquePerlin(Map monde){
 		appliquePerlin(monde,30);
 	}
 	
 	public static void appliquePerlin(Map monde,int range){
 		int[][] hauteur = perlin(monde.getSize());
		for(int x=0;x<500;x++){
			for(int z=0;z<500;z++){
 				int h = (int)( 64 + (double)( range*(128-Math.abs(hauteur[x][z]))/128 ) );
 				for(int y=0;y<h;y++){
 					monde.setBlock(x, z, y, (short)(3));
 				}
 				monde.setBlock(x, z, h, (short)(2));
 				for(int y=h+1;y<64;y++){
 					monde.setBlock(x, z, y, (short)(8));
 				}
 			}
 		}
 	}
 	
 	private static int[][] perlin(int[] taille){
 		return perlin(taille,System.currentTimeMillis());
 	}
 	
 	
 	private static int[][] perlin(int[] taille, long seed){
 		
 		int i,j,n,a,f_courante;
 		double sum_persistances=0;
 		int frequence = 2;
 		int octaves = 4;
 		double persistance = 0.7;
 		int[][] c = new int[taille[0]][taille[1]];
 		Calque init = new Calque(taille,1);
 		Random rand = new Random(seed);
 		Calque[] mes_calques = new Calque[octaves];
 		for(n=0;n<octaves;n++){
 			mes_calques[n] = new Calque(taille,Math.pow(persistance,n+1));
 		}
 		
 		for(i=0;i<taille[0];i++){
 			for(j=0;j<taille[1];j++){
 				init.setV(i, j, rand.nextInt(256));
 			}
 		}
 		
 		f_courante=frequence;
 		for(n=0;n<octaves;n++){
 			for(i=0;i<taille[0];i++){
 				for(j=0;j<taille[1];j++){
 					a=valeur_interpolee(i,j,f_courante,init);
 					mes_calques[n].setV(i, j, a);
 				}
 			}
 			f_courante*=frequence;
 		}
 		
 		for(n=0;n<octaves;n++){
 			sum_persistances +=mes_calques[n].getPersistance();
 		}
 		
 		for (i=0; i<taille[0]; i++){
 			for (j=0; j<taille[1]; j++){
 			    for (n=0; n<octaves; n++){
 			      c[i][j]+=mes_calques[n].getV(i, j)*mes_calques[n].getPersistance();
 			    }
 			     c[i][j] /= sum_persistances;
 			}
 		}
 		
 		return c;
 	}
 
 	private static int valeur_interpolee(int i, int j, int frequence, Calque r) {
 		
 		int borne1x, borne1y, borne2x, borne2y, q;
 		int[] taille = r.getTaille();
 		double pas;
 		pas = (double)(taille[0]/frequence);
 		 
 		q = (int)((double)(i)/pas);
 		borne1x = (int)(q*pas);
 		borne2x = (int)((q+1)*pas);
 		 
 		if(borne2x >= taille[0])
 		  borne2x = (taille[0])-1;
 
 		pas = (double)(taille[1]/frequence);
 		
 		q = (int)((double)(j)/pas);
 		borne1y = (int)(q*pas);
 		borne2y = (int)((q+1)*pas);
 		 
 		if(borne2y >= taille[1])
 		  borne2y = (taille[1])-1;
 		 
 		int b00,b01,b10,b11;
 		b00 = r.getV(borne1x,borne1y);
 		b01 = r.getV(borne1x,borne2y);
 		b10 = r.getV(borne2x,borne1y);
 		b11 = r.getV(borne2x,borne2y);
 		 
 		int v1  = interpolate(b00, b01, borne2y-borne1y, j-borne1y);
 		int v2  = interpolate(b10, b11, borne2y-borne1y, j-borne1y);
 		int fin = interpolate(v1, v2, borne2x-borne1x , i-borne1x);
 		 
 		return fin;
 
 	}
 
 	private static int interpolate(int y1, int y2, int n, int delta) {
 		if (n==0)
         return y1;
         if (n==1)
         return y2;
  
         double a = (double)delta/n;
  
         double v1 = 3*Math.pow(1-a,2) - 2*Math.pow(1-a,3);
         double v2 = 3*Math.pow(a,2) - 2*Math.pow(a,3);
  
         return (int)(y1*v1 + y2*v2);
 
 	}
 	
 }
