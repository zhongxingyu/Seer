 package com.cvgstudios.pokemonchrome.javascript;
 
 import com.cvgstudios.pokemonchrome.gamelogic.PokemonCreature;
 
 public class JSPokemonHandler {
 
 	PokemonCreature handledPokemon;
 
 	public void setHandle(PokemonCreature handlingPokemon) {
 		handledPokemon = handlingPokemon;
 	}
 
 	public JSPokemonHandler() {
 
 	}
 
 	public void set_type(String type) {
 		handledPokemon.setType(type);
 	}
 
 	public void set_iv_hp(int hp) {
 		handledPokemon.setIvHP(hp);
 	}
 
 	public void set_iv_attack(int attack) {
 		handledPokemon.setIvAttack(attack);
 	}
 
 	public void set_iv_defense(int defense) {
 		handledPokemon.setIvDefense(defense);
 	}
 
 	public void set_iv_sp_attack(int spAttack) {
 		handledPokemon.setIvSpAttack(spAttack);
 	}
 
 	public void set_iv_sp_defense(int spDefense) {
 		handledPokemon.setIvSpDefense(spDefense);
 	}
 
 	public void set_iv_speed(int speed) {
 		handledPokemon.setIvSpeed(speed);
 	}
 
 	public void set_action_allowed(String actioncodename) {
 		handledPokemon.setActionAvailability(actioncodename, true);
 	}
 
 	public void set_action_disallowed(String actioncodename) {
 		handledPokemon.setActionAvailability(actioncodename, false);
 	}
	
	public int get_hp(){
		return handledPokemon.
 	}
 
 }
