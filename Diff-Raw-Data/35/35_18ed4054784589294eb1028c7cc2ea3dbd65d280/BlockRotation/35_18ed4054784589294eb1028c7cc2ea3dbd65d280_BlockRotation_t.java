 package com.tehbeard.map.schematic.blocks;
 
 public enum BlockRotation {
 
 	WALL_MOUNTED(0x8,1,3,2,4),
 	STAIRS(0x4,3,0,2,1),
 
 	BED(0xC,0,1,2,3),
 	COCOA(0xC,0,1,2,3),
 	HOOK(0xC,0,1,2,3),
 	REPEATER(0xC,0,1,2,3),
 	FENCEGATE(0xC,0,1,2,3),
 	END_PORTAL(0xC,0,1,2,3),
 
 	CONTAINER_PISTON(0xC,2,5,3,4),
 
 	PUMPKIN(0,0,1,2,3),
 
 
 	TRAPDOOR(0,0,3,1,2),	
 
 	HEAD(0,2,4,3,5),
 
 	QUARTZ(0,3,4),
 
 	ANVIL(0xE,0,1),
 
 
 	RAIL(new RotateFunction(){
 
 		public int rotate(int data, int rotations) {
 
 			int r = data & ~0x8;
 			int d = data & 0x8;
 			int rot = rotations;
 
 			if(rot < 0){
 				rot = 4 - (rot%4);
 			}
 			for(int i =0; i< rot;i++){
 				switch(r){
 				//flat straight
 				case 0: r = 1; break;
 				case 1: r = 0; break;
 				//SLOPE
 				case 2: r = 5; break;
 				case 5: r = 3; break;
 				case 3: r = 4; break;
 				case 4: r = 2; break;
 
 				//corner
 				case 6: r = 7; break;
 				case 7: r = 8; break;
 				case 8: r = 9; break;
 				case 9: r = 6; break;
 
 				}
 			}
 			return r | d;
 			//			0x0: flat track going north-south
 			//			0x1: flat track going west-east
 			//			0x2: track ascending to the east
 			//			0x3: track ascending to the west
 			//			0x4: track ascending to the north
 			//			0x5: track ascending to the south
 			//			Regular minecart tracks can make a circle from four rails:
 			//			0x6: northwest corner (connecting east and south)
 			//			0x7: northeast corner (connecting west and south)
 			//			0x8: southeast corner (connecting west and north)
 			//			0x9: southwest corner (connecting east and north)
 		}
 
 	}),
 	VINES(new RotateFunction(){
 
 		public int rotate(int data, int rotations) {
 			int rot = rotations;
 
 			if(rot < 0){
 				rot = 4 - (rot%4);
 			}
 			
 			for(int i =0; i< rot;i++){
 				if((data & 0x1) != 0){data+=16;}
 				data = ((data << 1) | (data >> 3)) & 0xf;
 			}
 			return data;
 		}}),
 		BIG_MUSHROOM(null),
 		DOOR(new RotateFunction(){
 
 			public int rotate(int data, int rotations) {
 
 
 				int isTop = data & 0x8;
 				if(isTop != 0 ){
 					return data;
 
 				}else{
 					if(rotations < 0){
 						rotations = 4 - (rotations%4);
 					}
 					int extra = data & ~0x3;
 					int rotateData = data & 0x3;
 					for(int i =0; i<rotations;i++){
 						
 						switch(rotateData){
 						case 0: rotateData = 1;break;
 						case 1: rotateData = 2;break;
 						case 2: rotateData = 3;break;
 						case 3: rotateData = 0;break;
 						}
 					}
 					return rotateData | extra;
 				}
 				
 			}
 		}),
 		SIGN_POST(new RotateFunction(){
 
 			public int rotate(int data, int rotations) {
 				return (data + 4) % 16;
 			}
 
 
 		}),
 
 
 		;
 
 	BlockRotation(int mask, int... rotations){
 		this.mask = mask;
 		this.rotationsArray = rotations;
 	}
 
 	private int[] rotationsArray;
 	private int mask;
 
 	private RotateFunction rotateFunc;
 
 	private BlockRotation(RotateFunction rotateFunc) {
 		this.rotateFunc = rotateFunc;
 	}
 
 	/**
 	 * Rotates block data
 	 * @param data
 	 * @param rotations
 	 * @return
 	 */
 	public int rotate(int data, int rotations){
 		if(rotateFunc != null){
 			return rotateFunc.rotate(data, rotations);
 		}
 		else
 		{
 			int extraData = data & mask;
 			int rotate = data & ~mask;
 			int idx = getIdx(rotationsArray, rotate);
 			if(idx == -1){return data;}
 			idx = (idx + rotations) % rotationsArray.length;
 			if(idx < 0){
 				idx += rotationsArray.length;
 			}
 			return rotationsArray[idx] | extraData;
 
 		}
 	}
 
 
 	private int getIdx(int[] array,int value){
 		for(int i =0;i<array.length;i++){
 			if(array[i] == value){
 				return i;
 			}
 		}
 		return -1;
 
 	}
 
 	public static void main(String[] args){
 		for(int i = 0;i> -30;i--){
 			System.out.println(i % 8);
 		}
 	}
 }
