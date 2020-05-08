 /*
  * ChibiPaintMod
  *     Copyright (c) 2012-2014 Sergey Semushin
  *     Copyright (c) 2006-2008 Marc Schefer
  *
  *     This file is part of ChibiPaintMod (previously ChibiPaint).
  *
  *     ChibiPaintMod is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     ChibiPaintMod is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package chibipaint.effects;
 
 import chibipaint.engine.CPLayer;
 import chibipaint.engine.CPSelection;
 
 public class CPMakeGrayscaleEffect extends CPEffect
 {
 @Override
 public void doEffectOn (CPLayer layer, CPSelection selection)
 {
   modifyByOffset (layer, selection);
 }
 
 private int transformColor (int color)
 {
   int red = color & 0xFF0000 >>> 16;
   int green = color & 0x00FF00 >> 8;
   int blue = color & 0xFF;
   int v = (int) (0.2125 * red + 0.7154 * green + 0.0721 * blue);
   return (color & 0xFF000000) | v << 16 | v << 8 | v;
 }
 
 public int modify (int[] data, byte[] selData, int offset)
 {
  int selValue = selData[offset] & 0xFF;
  int alphaValue = (data[offset] >>> 24) & 0xFF;
  if (selValue >= alphaValue)
    return transformColor (data[offset]);
  else
    {
      return colorInOpaque (((alphaValue - selValue) << 24) | 0xFFFFFF & data[offset], selData[offset], transformColor (data[offset]));
    }
 }
 
 public int modify (int[] data, int offset)
 {
   return transformColor (data[offset]);
 }
 }
