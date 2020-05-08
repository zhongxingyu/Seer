 package com.dhbw.dvst.model;
 
 import java.util.ArrayList;
 
 public class Spiel{
 	/**
 	 * Spielmodi
 	 */
 	public static final int modus_einspieler = 0;
 	public static final int modus_mehrspieler_server = 1;
 	public static final int modus_mehrspieler_client = 2;
 	
 	private ArrayList<Spielfigur> alleSpielfiguren;
 	private ArrayList<Spieler> alleSpieler;
 	private int spielmodus;
 	private Spielbrett spielbrett;
 	private Spielplatte platte_aktiv;
 	private ArrayList<Spielkarte> kartenstapel;
 	
 	/**
 	 * Konstruktor
 	 */
 	public Spiel() {
 		alleSpielfiguren = new ArrayList<Spielfigur>();
 		initialisiereSpielfiguren();
 		alleSpieler = new ArrayList<Spieler>();
 	}
 	
 	/**
 	 * Spielfiguren initialisieren
 	 */
 	public void initialisiereSpielfiguren() {
 		this.alleSpielfiguren.add(new Spielfigur("motorrad", "rot", ""));
 		this.alleSpielfiguren.add(new Spielfigur("auto", "rot", ""));
 		this.alleSpielfiguren.add(new Spielfigur("bus", "rot", ""));
 		this.alleSpielfiguren.add(new Spielfigur("motorrad", "gelb", ""));
 		this.alleSpielfiguren.add(new Spielfigur("auto", "gelb", ""));
 		this.alleSpielfiguren.add(new Spielfigur("bus", "gelb", ""));
		this.alleSpielfiguren.add(new Spielfigur("motorrad", "gruen", ""));
		this.alleSpielfiguren.add(new Spielfigur("auto", "gruen", ""));
		this.alleSpielfiguren.add(new Spielfigur("bus", "gruen", ""));
 		this.alleSpielfiguren.add(new Spielfigur("motorrad", "blau", ""));
 		this.alleSpielfiguren.add(new Spielfigur("auto", "blau", ""));
 		this.alleSpielfiguren.add(new Spielfigur("bus", "blau", ""));
 	}
 	
 	/**
 	 * Spieler zum Spiel hinzufuegen
 	 * @param name
 	 * @param spielfigur
 	 */
 	public void spielerHinzufuegen(String name, Spielfigur spielfigur) {
 		Spieler neuerSpieler = new Spieler(name, spielfigur);
 		this.alleSpieler.add(neuerSpieler);
 		spielfigur.setVergeben(true);
 	}
 
 	/**
 	 * Spielfigur als bereits an einen Spieler vergeben markieren
 	 * @param neuerSpieler
 	 */
 //	private void setFigurVergeben(Spieler neuerSpieler) {
 //		//foreach
 //		for (Spielfigur s : this.alleSpielfiguren) {
 //			if(s.getFarbe().equals(neuerSpieler.getSpielfigur().getFarbe())) {
 //				if(s.getForm().equals(neuerSpieler.getSpielfigur().getForm())) {
 //					s.setVergeben(true);
 //				}
 //			}
 //		}
 //	}
 	
 	/**
 	 * Spieler loeschen und Spielfigur wieder freigeben
 	 * @param spieler
 	 */
 	public void spielerLoeschen(Spieler spieler) {
 		spieler.getSpielfigur().setVergeben(false);
 		this.alleSpieler.remove(spieler);
 	}
 	
 	/*
 	 * Getter und Setter Methoden
 	 */
 
 	/**
 	 * @return the spielmodus
 	 */
 	public int getSpielmodus() {
 		return spielmodus;
 	}
 
 	/**
 	 * @param spielmodus the spielmodus to set
 	 */
 	public void setSpielmodus(int spielmodus) {
 		this.spielmodus = spielmodus;
 	}
 
 	public ArrayList<Spielfigur> getAlleSpielfiguren() {
 		return alleSpielfiguren;
 	}
 
 	public void setAlleSpielfiguren(ArrayList<Spielfigur> alleSpielfiguren) {
 		this.alleSpielfiguren = alleSpielfiguren;
 	}
 
 	public ArrayList<Spieler> getAlleSpieler() {
 		return alleSpieler;
 	}
 
 	public void setAlleSpieler(ArrayList<Spieler> alleSpieler) {
 		this.alleSpieler = alleSpieler;
 	}
 
 	public Spielbrett getSpielbrett() {
 		return spielbrett;
 	}
 
 	public void setSpielbrett(Spielbrett spielbrett) {
 		this.spielbrett = spielbrett;
 	}
 
 	public Spielplatte getPlatte_aktiv() {
 		return platte_aktiv;
 	}
 
 	public void setPlatte_aktiv(Spielplatte platte_aktiv) {
 		this.platte_aktiv = platte_aktiv;
 	}
 
 	public ArrayList<Spielkarte> getKartenstapel() {
 		return kartenstapel;
 	}
 
 	public void setKartenstapel(ArrayList<Spielkarte> kartenstapel) {
 		this.kartenstapel = kartenstapel;
 	}
 
 }
