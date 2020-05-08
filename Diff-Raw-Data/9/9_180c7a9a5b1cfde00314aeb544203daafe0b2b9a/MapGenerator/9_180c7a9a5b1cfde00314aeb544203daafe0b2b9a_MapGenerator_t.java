 package models;
 
 import util.RichUtil;
 
 public class MapGenerator {
 
 	/**
 	 * 
 	 */
 	public static GameMap generateMap() {
 		GameMap gameMap = new GameMap(18, 23);
 		
 		Cell cell70 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 7, 0), "百子湾路", "后现代城");
 		Cell cell71 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 1), "百子湾路", "红", 2000);
 		Cell cell72 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 2), "百子湾路", "冰城串吧", 2000);
 		Cell cell73 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 3), "百子湾路", "麻辣E世界", 2000);
 		Cell cell74 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 4), "百子湾路", "爬爬车集散中心", 2000);
 		Cell cell75 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 5), "百子湾路", "云南苍蝇菜馆", 2000);
 		Cell cell76 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 6), "百子湾路", "711购物中心", 2000);
 		Cell cell77 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 7, 7), "百子湾路", "595公交车站");
 		Cell cell78 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 8), "百子湾路", "WON外国零食小店", 2000);
 		Cell cell79 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 9), "百子湾路", "千年老酒店", 2000);
 		Cell cell710 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 10), "百子湾路", "审美美发中心", 2000);
 		Cell cell711 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 11), "百子湾路", "麻渔府", 2000);
 		Cell cell712 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 12), "百子湾路", "大锅粥", 2000);
 		Cell cell713 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 13), "百子湾路", "四惠桥", 2000);
 		Cell cell714 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 7, 14), "百子湾路", "31路公交车站");
 		Cell cell715 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 15), "百子湾路", "通惠河", 2000);
 		Cell cell716 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 16), "百子湾路", "火车东站", 2000);
 		Cell cell717 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 17), "百子湾路", "东郊市场", 2000);
 		Cell cell718 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 18), "百子湾路", "京客隆", 2000);
 		Cell cell719 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 19), "百子湾路", "软妹子水果吧", 2000);
 		Cell cell720 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 20), "百子湾路", "水晶占卜坊", 2000);
 		Cell cell721 = new EstateCell(RichUtil.retrieveCellId(gameMap, 7, 21), "百子湾路", "657公交车站", 2000);
 		
 		Cell cell80 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 8, 0), "湖南路", "长沙火车站");
 		Cell cell90 = new EstateCell(RichUtil.retrieveCellId(gameMap, 9, 0), "湖南路", "孙家", 2000);
 		Cell cell100 = new EstateCell(RichUtil.retrieveCellId(gameMap, 10, 0), "湖南路", "郭孙家", 2000);
 		Cell cell110 = new EstateCell(RichUtil.retrieveCellId(gameMap, 11, 0), "湖南路", "妹记粉店", 2000);
 		Cell cell120 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 12, 0), "湖南路", "长沙港");
 		Cell cell130 = new EstateCell(RichUtil.retrieveCellId(gameMap, 13, 0), "湖南路", "杨玉兴粉", 2000);
 		Cell cell140 = new EstateCell(RichUtil.retrieveCellId(gameMap, 14, 0), "湖南路", "郭家", 2000);
 		Cell cell150 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 0), "湖南路", "长沙乡村基", 2000);
 		Cell cell160 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 0), "湖南路", "湖南大学");
 		
 		Cell cell821 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 8, 21), "湖南路", "海南火车站");
 		Cell cell921 = new EstateCell(RichUtil.retrieveCellId(gameMap, 9, 21), "湖南路", "蜗家", 2000);
 		Cell cell1021 = new EstateCell(RichUtil.retrieveCellId(gameMap, 10, 21), "湖南路", "莎家", 2000);
 		Cell cell1121 = new EstateCell(RichUtil.retrieveCellId(gameMap, 11, 21), "湖南路", "兔女郎夜总会", 2000);
 		Cell cell1221 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 12, 21), "湖南路", "长沙港");
 		Cell cell1321 = new EstateCell(RichUtil.retrieveCellId(gameMap, 13, 21), "湖南路", "蜗莎家", 2000);
 		Cell cell1421 = new EstateCell(RichUtil.retrieveCellId(gameMap, 14, 21), "湖南路", "莎老谢螃蟹", 2000);
 		Cell cell1521 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 21), "湖南路", "海南鲨鱼港", 2000);
 		Cell cell1621 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 21), "湖南路", "海南大学");
 		
 		Cell cell161 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 1), "四川路", "肖家");
 		Cell cell162 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 2), "四川路", "川妹子之家");
 		Cell cell163 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 3), "四川路", "肖胖娃蹄花");
 		Cell cell164 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 4), "四川路", "清油小火锅");
 		Cell cell165 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 5), "四川路", "天天美食");
 		Cell cell166 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 6), "四川路", "绵阳飞机场");
 		Cell cell156 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 15, 6), "四川路", "绵阳海关");
 		Cell cell157 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 7), "四川路", "肖小妹之家", 2000);
 		Cell cell158 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 8), "四川路", "川妹子之家", 2000);
 		Cell cell159 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 15, 9), "四川路", "星期八烧烤");
 		Cell cell1510 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 10), "四川路", "人民公园", 2000);
 		Cell cell1511 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 11), "四川路", "富乐山公园", 2000);
 		Cell cell1512 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 15, 12), "四川路", "老房子");
 		Cell cell1513 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 13), "四川路", "东方红大桥", 2000);
 		Cell cell1514 = new EstateCell(RichUtil.retrieveCellId(gameMap, 15, 14), "四川路", "绵阳二中", 2000);
 		Cell cell1515 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 15, 15), "四川路", "南山中学");
		Cell cell1516 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 15), "四川路", "科委立交桥");
 		Cell cell1616 = new EstateCell(RichUtil.retrieveCellId(gameMap, 16, 16), "四川路", "计划生育委员会", 2000);
 		Cell cell1617 = new EstateCell(RichUtil.retrieveCellId(gameMap, 16, 17), "四川路", "翠花街", 2000);
 		Cell cell1618 = new EstateCell(RichUtil.retrieveCellId(gameMap, 16, 18), "四川路", "四川小吃城", 2000);
 		Cell cell1619 = new EstateCell(RichUtil.retrieveCellId(gameMap, 16, 19), "四川路", "新华书店", 2000);
 		Cell cell1620 = new EmptyCell(RichUtil.retrieveCellId(gameMap, 16, 20), "四川路", "绵阳电脑城");
 		
 		
 		gameMap.mapCells[7][0] = cell70;
 		gameMap.mapCells[7][1] = cell71;
 		gameMap.mapCells[7][2] = cell72;
 		gameMap.mapCells[7][3] = cell73;
 		gameMap.mapCells[7][4] = cell74;
 		gameMap.mapCells[7][5] = cell75;
 		gameMap.mapCells[7][6] = cell76;
 		gameMap.mapCells[7][7] = cell77;
 		gameMap.mapCells[7][8] = cell78;
 		gameMap.mapCells[7][9] = cell79;
 		gameMap.mapCells[7][10] = cell710;
 		gameMap.mapCells[7][11] = cell711;
 		gameMap.mapCells[7][12] = cell712;
 		gameMap.mapCells[7][13] = cell713;
 		gameMap.mapCells[7][14] = cell714;
 		gameMap.mapCells[7][15] = cell715;
 		gameMap.mapCells[7][16] = cell716;
 		gameMap.mapCells[7][17] = cell717;
 		gameMap.mapCells[7][18] = cell718;
 		gameMap.mapCells[7][19] = cell719;
 		gameMap.mapCells[7][20] = cell720;
 		gameMap.mapCells[7][21] = cell721;
 
 		gameMap.mapCells[8][0] = cell80;
 		gameMap.mapCells[9][0] = cell90;
 		gameMap.mapCells[10][0] = cell100;
 		gameMap.mapCells[11][0] = cell110;
 		gameMap.mapCells[12][0] = cell120;
 		gameMap.mapCells[13][0] = cell130;
 		gameMap.mapCells[14][0] = cell140;
 		gameMap.mapCells[15][0] = cell150;
 		gameMap.mapCells[16][0] = cell160;
 		
 		gameMap.mapCells[8][21] = cell821;
 		gameMap.mapCells[9][21] = cell921;
 		gameMap.mapCells[10][21] = cell1021;
 		gameMap.mapCells[11][21] = cell1121;
 		gameMap.mapCells[12][21] = cell1221;
 		gameMap.mapCells[13][21] = cell1321;
 		gameMap.mapCells[14][21] = cell1421;
 		gameMap.mapCells[15][21] = cell1521;
 		gameMap.mapCells[16][21] = cell1621;
 		
 		gameMap.mapCells[16][1] = cell161;
 		gameMap.mapCells[16][2] = cell162;
 		gameMap.mapCells[16][3] = cell163;
 		gameMap.mapCells[16][4] = cell164;
 		gameMap.mapCells[16][5] = cell165;
 		gameMap.mapCells[16][6] = cell166;
 		gameMap.mapCells[15][6] = cell156;
 		gameMap.mapCells[15][7] = cell157;
 		gameMap.mapCells[15][8] = cell158;
 		gameMap.mapCells[15][9] = cell159;
 		gameMap.mapCells[15][10] = cell1510;
 		gameMap.mapCells[15][11] = cell1511;
 		gameMap.mapCells[15][12] = cell1512;
 		gameMap.mapCells[15][13] = cell1513;
 		gameMap.mapCells[15][14] = cell1514;
 		gameMap.mapCells[15][15] = cell1515;
 		gameMap.mapCells[15][16] = cell1516;
 		gameMap.mapCells[16][16] = cell1616;
 		gameMap.mapCells[16][17] = cell1617;
 		gameMap.mapCells[16][18] = cell1618;
 		gameMap.mapCells[16][19] = cell1619;
 		gameMap.mapCells[16][20] = cell1620;
 		
 		cell70.pendNext(cell71);
 		cell71.pendNext(cell72);
 		cell72.pendNext(cell73);
 		cell73.pendNext(cell74);
 		cell74.pendNext(cell75);
 		cell75.pendNext(cell76);
 		cell76.pendNext(cell77);
 		cell77.pendNext(cell78);
 		cell78.pendNext(cell79);
 		cell79.pendNext(cell710);
 		cell710.pendNext(cell711);
 		cell711.pendNext(cell712);
 		cell712.pendNext(cell713);
 		cell713.pendNext(cell714);
 		cell714.pendNext(cell715);
 		cell715.pendNext(cell716);
 		cell716.pendNext(cell717);
 		cell717.pendNext(cell718);
 		cell718.pendNext(cell719);
 		cell719.pendNext(cell720);
 		cell720.pendNext(cell721);
 		cell721.pendNext(cell821);
 		
 		cell821.pendNext(cell921);
 		cell921.pendNext(cell1021);
 		cell1021.pendNext(cell1121);
 		cell1121.pendNext(cell1221);
 		cell1221.pendNext(cell1321);
 		cell1321.pendNext(cell1421);
 		cell1421.pendNext(cell1521);
 		cell1521.pendNext(cell1621);
 		
 		cell1621.pendNext(cell1620);
 		cell1620.pendNext(cell1619);
 		cell1619.pendNext(cell1618);
 		cell1618.pendNext(cell1617);
 		cell1617.pendNext(cell1616);
 		cell1616.pendNext(cell1516);
 		cell1516.pendNext(cell1515);
 		cell1515.pendNext(cell1514);
 		cell1514.pendNext(cell1513);
 		cell1513.pendNext(cell1512);
 		cell1512.pendNext(cell1511);
 		cell1511.pendNext(cell1510);
 		cell1510.pendNext(cell159);
 		cell159.pendNext(cell158);
 		cell158.pendNext(cell157);
 		cell157.pendNext(cell156);
 		cell156.pendNext(cell166);
 		cell166.pendNext(cell165);
 		cell165.pendNext(cell164);
 		cell164.pendNext(cell163);
 		cell163.pendNext(cell162);
 		cell162.pendNext(cell161);
 		cell161.pendNext(cell160);
 		
 		cell160.pendNext(cell150);
 		cell150.pendNext(cell140);
 		cell140.pendNext(cell130);
 		cell130.pendNext(cell120);
 		cell120.pendNext(cell110);
 		cell110.pendNext(cell100);
 		cell100.pendNext(cell90);
 		cell90.pendNext(cell80);
 		cell80.pendNext(cell70);
 		
 		
 		return gameMap;
 	}
 }
