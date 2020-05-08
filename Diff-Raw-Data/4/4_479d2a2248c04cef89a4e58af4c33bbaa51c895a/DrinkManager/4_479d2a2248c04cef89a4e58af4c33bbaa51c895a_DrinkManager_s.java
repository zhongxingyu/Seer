 /*
 New BSD License
 Copyright (c) 2012, MyBar Team All rights reserved.
 mybar@turbotorsk.se
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 �	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 �	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 �	Neither the name of the MyBar nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 
 
 package se.turbotorsk.mybar.controller;
 import java.util.LinkedList;
 import se.turbotorsk.mybar.model.Drink;
 import se.turbotorsk.mybar.model.Ingredient;
 
 
 public class DrinkManager {
 	private LinkedList<Drink> myBar = null; 
 
 	public DrinkManager(){;}
 
 	public LinkedList<Drink> getMyBar(LinkedList<Ingredient> ingredientList, LinkedList<Drink> drinkList)
 	{
 		String[] drinks;
 		int ingredientID = 0, count = 0;
 		boolean found = false; 
 		for(Drink drink : drinkList ){
 			drinks = drink.getIngredient().split(";");
 			for(int countID = 0;  countID <= drinks.length; countID+=2){
 				ingredientID = Integer.parseInt(drinks[count]);
 				found = false; 
 				for(Ingredient ingredient : ingredientList){
					if(ingredient.getId() == ingredientID) {
 						found = true; 
 						break;
 					}
 				}
 				if (found == false) {
 					break;
 				}
 			myBar.add(drink);
 			}	
 		}
 			return myBar;
 	}
 
 
 }
