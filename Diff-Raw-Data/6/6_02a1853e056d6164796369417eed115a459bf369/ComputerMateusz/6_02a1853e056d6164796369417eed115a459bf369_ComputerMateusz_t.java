 package gameEngine;
 
 import main.Main;
 
 public class ComputerMateusz implements Player {
 
 	int val;
 	ComputerMateusz(int val) {
 		this.val = val;
 	}
 	
 	@Override
 	public int makeMove(int x, int y, Board board) {
 		System.out.println("ComputerMateusz moves");
 		
 		int boardWidth=Main.controller.getBoardWidth();
 		int boardHeight=Main.controller.getBoardHeight();
 		int posX=Main.controller.returnPosX();
 		int posY=Main.controller.returnPosY();
 		
 		int newX=posX, newY=posY;
		int targetY;
		if(this.val==1) targetY=0;
		else targetY=boardHeight;
 		int targetX=boardWidth/2;
 		
 		int size=16;
 		
 		int possiblePoints[][] = new int[size][size];
 		for(int i=0; i<16; i++) for(int j=0; j<size; j++) possiblePoints[i][j]=-1;
 		
 		int visitedPoints[][] = new int[size][size];
 		for(int i=0; i<size; i++) for(int j=0; j<size; j++) visitedPoints[i][j]=-1;
 	
 		int imfromX[][] = new int[size][size];
 		int imfromY[][] = new int[size][size];
 		
 		int minH=1;
 		int maxH=minH+boardWidth;
 		int minV=2;
 		int maxV=minV+boardHeight;
 		
 		possiblePoints[posX][posY]=2;
 		
 		int curX,curY;
 		for(int i=0; i<=8; i++) {
 			for(int j=0; j<=i*2; j++) {
 				for(int k=0; k<=i*2; k++) {
 					if((j==0||j==i*2)&&(k==0||k==i*2)) {
 						curX=posX+j-i;
 						curY=posY+k-i;
 						if(curX<=maxH&&curX>=minH&&curY<=maxV&&curY>=minV) {
 							if(possiblePoints[curX][curY]>1) {
 								if(Main.controller.boardLinks(curY-1,curX-1,2)==0) {
 									int neighbours=Main.controller.countNeighbours(curX-1, curY-1);
 									if(neighbours==1||neighbours==0) possiblePoints[curX-1][curY-1]=1;
 									else if(neighbours==8) possiblePoints[curX-1][curY-1]=0;
 									else possiblePoints[curX-1][curY-1]=2;
 									imfromX[curX-1][curY-1]=curX;
 									imfromY[curX-1][curY-1]=curY;
 								}
 								if(Main.controller.boardLinks(curY-1,curX,1)==0) {
 									int pX=curX,pY=curY-1;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 								if(Main.controller.boardLinks(curY-1,curX,3)==0) {
 									int pX=curX+1,pY=curY-1;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 								if(Main.controller.boardLinks(curY,curX,0)==0) {
 									int pX=curX+1,pY=curY;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 								if(Main.controller.boardLinks(curY,curX,2)==0) {
 									int pX=curX+1,pY=curY+1;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 								if(Main.controller.boardLinks(curY,curX,1)==0) {
 									int pX=curX,pY=curY+1;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 								if(Main.controller.boardLinks(curY,curX-1,3)==0) {
 									int pX=curX-1,pY=curY+1;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 								if(Main.controller.boardLinks(curY,curX-1,0)==0) {
 									int pX=curX-1,pY=curY;
 									int neighbours=Main.controller.countNeighbours(pX, pY);
 									if(neighbours==1||neighbours==0) possiblePoints[pX][pY]=1;
 									else if(neighbours==8) possiblePoints[pX][pY]=0;
 									else possiblePoints[pX][pY]=2;
 									imfromX[pX][pY]=curX;
 									imfromY[pX][pY]=curY;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		int closest=1000000,choices=0;
 		for(int i=0; i<size; i++) {
 			for(int j=0; j<size; j++) {
 				if(possiblePoints[i][j]==1) {
 					choices++;
 					if((targetX-i)*(targetX-i)+(targetY-j)*(targetY-j)<closest) {
 						newX=i;
 						newY=j;
 						closest=(targetX-i)*(targetX-i)+(targetY-j)*(targetY-j);
 					}
 				}
 			}
 		}
 		if (closest==1000000) return 5;
 	
 		if(newX==posX&&newY==posY) return 5;
 
 		Main.controller.updatePos(newX,newY);
 		
 		int p1X=newX;
 		int p1Y=newY;
 		int p2X=imfromX[newX][newY];
 		int p2Y=imfromY[newX][newY];
 		int inStart=0;
 		int maxmoves=8;
 		while(inStart!=1) {
 			if(maxmoves==0) return 5;
 			if(p1X==posX&&p1Y==posY) inStart=1;
 			else {
 				if(p2Y>p1Y && p2X>p1X) {
 					//upper left
 					if(Main.controller.boardLinks(p2Y-1,p2X-1,2)==0) Main.controller.updateLinks(p2Y-1, p2X-1, 2, val);
 				}
 				else if(p2Y>p1Y && p2X==p1X) {
 					//upper top
 					if(Main.controller.boardLinks(p2Y-1,p2X,1)==0) Main.controller.updateLinks(p2Y-1,p2X,1,val);
 				}
 				else if(p2Y>p1Y && p2X<p1X) {
 					//upper right
 					if(Main.controller.boardLinks(p2Y-1,p2X,3)==0) Main.controller.updateLinks(p2Y-1,p2X,3,val);
 				}
 				else if(p2Y==p1Y && p2X>p1X) {
 					//middle left
 					if(Main.controller.boardLinks(p2Y,p2X-1,0)==0) Main.controller.updateLinks(p2Y,p2X-1,0,val);
 				}
 				else if(p2Y==p1Y && p2X<p1X) {
 					//middle right
 					if(Main.controller.boardLinks(p2Y,p2X,0)==0) Main.controller.updateLinks(p2Y,p2X,0,val);
 				}
 				else if(p2Y<p1Y && p2X<p1X) {
 					//bottom right
 					if(Main.controller.boardLinks(p2Y,p2X,2)==0) Main.controller.updateLinks(p2Y,p2X,2,val);
 				}
 				else if(p2Y<p1Y && p2X==p1X) {
 					//bottom bottom
 					if(Main.controller.boardLinks(p2Y,p2X,1)==0) Main.controller.updateLinks(p2Y,p2X,1,val);
 				}
 				else if(p2Y<p1Y && p2X>p1X) {
 					//bottom left
 					if(Main.controller.boardLinks(p2Y,p2X-1,3)==0) Main.controller.updateLinks(p2Y,p2X-1,3,val);
 					
 				}
 				int tmpX=imfromX[p2X][p2Y];
 				int tmpY=imfromY[p2X][p2Y];
 				p1Y=p2Y;
 				p1X=p2X;
 				p2Y=tmpY;
 				p2X=tmpX;
 			}
 			maxmoves--;
 		}
 		return 0;
 	}
 
 	@Override
 	public void removeMove() {
 		
 	}
 
 }
