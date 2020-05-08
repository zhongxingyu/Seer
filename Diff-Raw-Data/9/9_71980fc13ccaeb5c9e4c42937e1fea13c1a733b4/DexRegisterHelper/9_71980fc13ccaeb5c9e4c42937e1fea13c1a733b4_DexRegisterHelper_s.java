 package com.rx201.dx.translator.util;
 
 import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 
 public class DexRegisterHelper {
 	public static DexRegister next(DexRegister reg) {
 		return new DexRegister(reg.getOriginalIndex() + 1);
 	}
 	
 	public static boolean deepEqual(DexRegister r0, DexRegister r1) {
 		return r0.getOriginalIndex() == r1.getOriginalIndex();
 	}
 	
 	private static int PARAM_OFFSET = 100000; 
 	public static int normalize(int paramId) {
 		return PARAM_OFFSET + paramId;
 	}
 
 	public static int normalize(DexRegister reg) {
 		if (reg instanceof DexParameterRegister) 
 			return normalize(((DexParameterRegister)reg).getParameterIndex());
 		else
 			return reg.getOriginalIndex();
 	}
 
 	public static boolean isPair(DexRegister r0, DexRegister r1) {
		return r0.getOriginalIndex() + 1 == r1.getOriginalIndex();
 	}
 	
 	public static DexRegister getTempRegister(int index) {
 		return new DexRegister(PARAM_OFFSET - 1 - index);
 	}
 	
 }
