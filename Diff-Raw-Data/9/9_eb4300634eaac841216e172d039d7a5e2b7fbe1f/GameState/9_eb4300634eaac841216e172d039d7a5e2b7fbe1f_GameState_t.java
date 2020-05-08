 package com.pacwar;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Point;
 import android.widget.Toast;
 
 public class GameState implements Runnable {
 	public static MainActivity view;
 	static int ghosts = 2, pacmans = 2;
 	static int map_w = Global.MAP_WIDTH, map_h = Global.MAP_HEIGHT;
 	float screen_w, screen_h;
 	static int framesPerSec = 20;
 	// men 1,2,3,4,5,6...
 	// player1 +ve, player2 -ve
 	static final int block = 0;
 	static final int pac = 1 << 20;
 	static final int select = 0;
 	static final int action = 1;
 	static boolean[][] map = new boolean[map_h][map_w];
 	static boolean[][] vis = new boolean[map_h][map_w];
 	static float cell_w, cell_h;
 	static int curPlayer;
 	static Man[] selectedMan;
 	String name;
 	Player[] players;
 
 	public GameState(MainActivity mainActivity) throws IOException {
 		view = mainActivity;
 		init(Global.SCREEN_WIDTH, Global.SCREEN_HEIGHT);
 		new Thread(this).start();
 	}
 
 	public void init(float screen_w, float screen_h) throws IOException {
 		this.screen_w = screen_w;
 		this.screen_h = screen_h;
 		cell_w = (screen_w / map_w);
 		cell_h = (screen_h / map_h);
 		Global.CELL_WIDTH = cell_w;
 		Global.CELL_HEIGHT = cell_h;
 		Global.TOUCH_ERROR_THRESHOLD = Math.max(Global.CELL_HEIGHT,
 				Global.CELL_WIDTH) * 5;
 
 		mapinit();
 
 		players = new Player[2];
 		players[0] = new Player();
 		players[1] = new Player();
 
 		players[0].state = select;
 		players[1].state = select;
 
 		selectedMan = new Man[2];
 
 		initializePlayersMen();
 	}
 
 	private void initializePlayersMen() {
 
 		// Pac 1
 		Man pm1 = new Man();
 		pm1.x = cell_w;
 		pm1.y = cell_h;
 		pm1.destX = pm1.x;
 		pm1.destY = pm1.y;
 		pm1.cenX = (pm1.x + 1.5f * cell_w);
 		pm1.cenY = (pm1.y + 1.5f * cell_h);
 		pm1.type = Global.PACMAN_TYPE;
 		pm1.color = 0;
 		players[0].addMan(pm1);
 		view.show_pacman(0, 0, (int) pm1.x, (int) pm1.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 		// ghost 1
 
 		Man pm11 = new Man();
 		pm11.x = pm1.x + (3 * cell_w);
 		pm11.y = cell_h;
 		pm11.destX = pm11.x;
 		pm11.destY = pm11.y;
 		pm11.cenX = (pm11.x + 1.5f * cell_w);
 		pm11.cenY = (pm11.y + 1.5f * cell_h);
 		pm11.type = Global.GHOST_TYPE;
 		pm11.color = 0;
 		players[0].addMan(pm11);
 		view.show_ghost(0, 0, (int) pm11.x, (int) pm11.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 		// Pac 12
 		Man pm12 = new Man();
 		pm12.x = Global.SCREEN_WIDTH - cell_w - (3 * cell_w);
 		pm12.y = cell_h;
 		pm12.destX = pm12.x;
 		pm12.destY = pm12.y;
 		pm12.cenX = (pm12.x + 1.5f * cell_w);
 		pm12.cenY = (pm12.y + 1.5f * cell_h);
 		pm12.type = Global.PACMAN_TYPE;
 		pm12.color = 1;
 		players[0].addMan(pm12);
 		view.show_pacman(1, 0, (int) pm12.x, (int) pm12.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 		// ghost 1
 
 		Man pm122 = new Man();
 		pm122.x = pm12.x - (3 * cell_w);
 		pm122.y = cell_h;
 		pm122.destX = pm122.x;
 		pm122.destY = pm122.y;
 		pm122.cenX = (pm122.x + 1.5f * cell_w);
 		pm122.cenY = (pm122.y + 1.5f * cell_h);
 		pm122.type = Global.GHOST_TYPE;
 		pm122.color = 1;
 		players[0].addMan(pm122);
 		view.show_ghost(1, 0, (int) pm122.x, (int) pm122.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 		// _______________________Player 2___________________________________
 
 		// Pac 2
 		Man pm2 = new Man();
 		pm2.x = Global.SCREEN_WIDTH - cell_w - (3 * cell_w);
 		pm2.y = Global.SCREEN_HEIGHT - cell_h - (3 * cell_h);
 		pm2.destX = pm2.x;
 		pm2.destY = pm2.y;
 		pm2.cenX = (pm2.x + 1.5f * cell_w);
 		pm2.cenY = (pm2.y + 1.5f * cell_h);
 		pm2.type = Global.PACMAN_TYPE;
 		pm2.color = 0;
 		players[1].addMan(pm2);
 		view.show_pacman(0, 1, (int) pm2.x, (int) pm2.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 		// ghost 2
 
 		Man pm22 = new Man();
 		pm22.x = pm2.x - (3 * cell_w);
 		pm22.y = pm2.y;
 		pm22.destX = pm22.x;
 		pm22.destY = pm22.y;
 		pm22.cenX = (pm22.x + 1.5f * cell_w);
 		pm22.cenY = (pm22.y + 1.5f * cell_h);
 		pm22.type = Global.GHOST_TYPE;
 		pm22.color = 0;
 		players[1].addMan(pm22);
 		view.show_ghost(0, 1, (int) pm22.x, (int) pm22.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 		// Pac 2
 		Man pm21 = new Man();
 		pm21.x = cell_w;
 		pm21.y = Global.SCREEN_HEIGHT - cell_h - (3 * cell_h);
 		pm21.destX = pm21.x;
 		pm21.destY = pm21.y;
 		pm21.cenX = (pm21.x + 1.5f * cell_w);
 		pm21.cenY = (pm21.y + 1.5f * cell_h);
 		pm21.type = Global.PACMAN_TYPE;
 		pm21.color = 1;
 		players[1].addMan(pm21);
 		view.show_pacman(1, 1, (int) pm21.x, (int) pm21.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 		// ghost 2
 
 		Man pm222 = new Man();
 		pm222.x = pm21.x + (3 * cell_w);
 		pm222.y = pm21.y;
 		pm222.destX = pm222.x;
 		pm222.destY = pm222.y;
 		pm222.cenX = (pm222.x + 1.5f * cell_w);
 		pm222.cenY = (pm222.y + 1.5f * cell_h);
 		pm222.type = Global.GHOST_TYPE;
 		pm222.color = 1;
 		players[1].addMan(pm222);
 		view.show_ghost(1, 1, (int) pm222.x, (int) pm222.y, (int) cell_w * 3,
 				(int) cell_h * 3);
 
 	}
 
 	public void mapinit() throws IOException {
 		AssetManager am = view.getAssets();
 		InputStream is = am.open("initial_map.txt");
 		BufferedReader buff = new BufferedReader(new InputStreamReader(is));
 		for (int i = 0; i < Global.MAP_HEIGHT; i++) {
 			String row = buff.readLine();
 			for (int j = 0; j < row.length(); j++) {
 				if (row.charAt(j) == '.')
 					map[i][j] = true;
 			}
 		}
 		buff.close();
 	}
 
 	class state {
 		int x, y, d;
 		state p;
 
 		public state(int x, int y) {
 			this.x = x;
 			this.y = y;
 			d = 0;
 			p = null;
 		}
 
 		public state(int x, int y, state pp, int dd) {
 			this.x = x;
 			this.y = y;
 			p = pp;
 			d = dd;
 		}
 	}
 
 	int[] dx = { 0, 0, 1, -1, 1, 1, -1, -1 };
 	int[] dy = { 1, -1, 0, 0, -1, 1, 1, -1 };
 
 	// x1,y1,x2,y2 are the indices on map NOT pixels
 	// x1,x2 HORIZONTAL map[0].length
 	// y1,y2 VERTICAL map.length
 	public Point[] findPath(int x1, int y1, int x2, int y2) {
 		Queue<state> q = new LinkedList<state>();
 		state cur = null;
 		q.add(new state(x1, y1));
 		for (int i = 0; i < vis.length; i++)
 			for (int j = 0; j < vis[0].length; j++)
 				vis[i][j] = false;
 		int nx, ny;
 		while (!q.isEmpty()) {
 			cur = q.poll();
 			if (cur.x == x2 && cur.y == y2)
 				break;
 			for (int k = 0; k < 4; k++) {
 				nx = cur.x + dx[k];
 				ny = cur.y + dy[k];
 				if (nx < 0 || nx >= Global.MAP_HEIGHT || ny < 0
 						|| ny >= Global.MAP_WIDTH || vis[nx][ny]
 						|| !map[nx][ny])
 					continue;
 				vis[cur.x][cur.y] = true;
 				q.add(new state(nx, ny, cur, cur.d + 1));
 			}
 		}
 		Point[] ret = new Point[cur.d];
 		while (cur.p != null) {
 			ret[cur.d - 1] = new Point(cur.x, cur.y);
 			cur = cur.p;
 		}
 		return ret;
 	}
 
 	public void SceneTouch(float x, float y, int player) {
 		if (player == curPlayer)
 			try {
 				// TODO set the name variable
 				Lobby.message = EncodeMsg(x, y);
 				new Lobby().new DownloadFilesTask().execute(Lobby.sendMessage);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		if (players[player].state == select) {
 			Man manClicked = null;
 			float min = 1 << 27, tmp;
 			Man cur;
 
 			for (int i = 0; i < players[player].men.size(); i++) {
 				cur = players[player].men.get(i);
 				tmp = (cur.cenX - x) * (cur.cenX - x) + (cur.cenY - y)
 						* (cur.cenY - y);
 				if (tmp < min
 						&& tmp < Global.TOUCH_ERROR_THRESHOLD
 								* Global.TOUCH_ERROR_THRESHOLD) {
 					min = tmp;
 					manClicked = cur;
 				}
 			}
 
 			if (/* manClicked > 0 && */manClicked != null
 					&& manClicked.equals(selectedMan[player])
 					&& players[player].state == action) {
 				return;
 			}
 
 			if (/* manClicked > 0 && */manClicked != null) {
 				selectedMan[player] = manClicked;
 				players[player].state = action;
 			}
 		} else if (players[player].state == action) {
 			players[player].state = select;
 			Man man = selectedMan[player];
 			System.out.println("Selected Man = " + selectedMan[player]);
 			System.out.println("Man updated : " + man.type + " -- " + man.x);
 			System.out.println(players[player].men.size());
 
 			Point p = getPoint(x, y);
 			if (p != null) {
 				man.next = findPath((int) (man.cenY / cell_h),
 						(int) (man.cenX / cell_w), p.x, p.y);
 				if (man.next.length > 0) {
 					man.current_point_to_go = 1;
 					man.destX = (man.next[0].y - 1) * Global.CELL_WIDTH;
 					man.destY = (man.next[0].x - 1) * Global.CELL_HEIGHT;
 				}
 			}
 		}
 	}
 
 	private String EncodeMsg(float x, float y) {
 		return x + " " + y;
 		// byte[] m = new byte[8];
 		// int xx = Float.floatToRawIntBits(x);
 		// int yy = Float.floatToRawIntBits(y);
 		// for (int i = 0; i < 4; i++)
 		// m[i] = (byte) ((xx >> (i * 8)) & 0xff);
 		// for (int i = 0; i < 4; i++)
 		// m[i + 4] = (byte) ((yy >> (i * 8)) & 0xff);
 		// return new String(m);
 	}
 
 	public Point getPoint(float x, float y) {
 		int yMap = (int) (x / Global.CELL_WIDTH);
 		int xMap = (int) (y / Global.CELL_HEIGHT);
 		if (map[xMap][yMap])
 			return new Point(xMap, yMap);
 		int nx, ny;
 		for (int margin = 1; margin <= 2; margin++) {
 			for (int i = 0; i < 8; i++) {
 				nx = xMap + margin * dx[i];
 				ny = yMap + margin * dy[i];
 				if (nx < 0 || nx >= Global.MAP_HEIGHT || ny < 0
 						|| ny >= Global.MAP_WIDTH)
 					continue;
 				if (map[nx][ny])
 					return new Point(nx, ny);
 			}
 		}
 		return null;
 	}
 
 	public void changeScreenSize(int sizeX, int sizeY) {
 		// view.windowSizeChanged(sizeX, sizeY);
 	}
 
 	@Override
 	public void run() {
 		long currentTime = System.nanoTime();
 		long minFrameTime = 1000000000 / framesPerSec;
 		while (true) {
 			long newTime = System.nanoTime();
 			long frameTime = newTime - currentTime;
 			currentTime = newTime;
 
 			for (int k = 0; k < 2; k++) {
 				for (int i = 0; i < players[k].men.size(); i++) {
 					float xx = players[k].men.get(i).x, yy = players[k].men
 							.get(i).y;
 					players[k].men.get(i).updateMe();
 					float xx2 = players[k].men.get(i).x, yy2 = players[k].men
 							.get(i).y;
 					if (xx != xx2 || yy != yy2) {
 						if (players[k].men.get(i).type == Global.PACMAN_TYPE) {
 							view.move_pacman(players[k].men.get(i).color, k,
 									(int) (players[k].men.get(i).x),
 									(int) (players[k].men.get(i).y));
 						} else {
 							view.move_ghost(players[k].men.get(i).color, k,
 									(int) (players[k].men.get(i).x),
 									(int) (players[k].men.get(i).y));
 						}
 						boolean gameEnded = testScore(
 								(k + 1) % 2,
 								getPoint(players[k].men.get(i).cenX,
 										players[k].men.get(i).cenY),
								players[k].men.get(i), k, i);
 						if (gameEnded) {
 							if (k == curPlayer) {
 								view.runOnUiThread(new Runnable() {
 									public void run() {
 										Toast.makeText(view, "You Win !!!",
 												Global.TOAST_DURATION).show();
 									}
 								});
 							} else {
 								view.runOnUiThread(new Runnable() {
 									public void run() {
 										Toast.makeText(view, "You Lose !!!",
 												Global.TOAST_DURATION).show();
 									}
 								});
 							}
 							view.finish();
 						}
 					}
 				}
 			}
 
 			// XXX NOT WORKING MESH 3AREF LEH !!! if(frameTime<minFrameTime){
 			try {
 				if (minFrameTime - frameTime > 0)
 					Thread.sleep((minFrameTime - frameTime) / 1000000); //
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			// try {
 			// Thread.sleep(30);
 			// } catch (InterruptedException e) {
 			// e.printStackTrace();
 			// }
 		}
 	}
 
 	private boolean testScore(int otherIndex, Point point, Man caller,
			int callerIndex, int callerManIndex) {
 		for (int i = 0; i < players[otherIndex].men.size(); i++) {
 			Man current = players[otherIndex].men.get(i);
 			if (getPoint(current.x, current.y).equals(point)) {
 				if (current.type != caller.type) {
 					boolean status = false;
 					if (current.type == Global.GHOST_TYPE) {
 						view.hide_pacman(caller.color, callerIndex);
 						players[otherIndex].score++;
						status = players[callerIndex]
								.losePacMan(callerManIndex);
 					} else {
 						view.hide_pacman(current.color, otherIndex);
 						players[callerIndex].score++;
 						status = players[otherIndex].losePacMan(i);
 					}
 					if (status) {
 						return true;
 					}
 					i--;
 				}
 			}
 		}
 		return false;
 	}
 }
