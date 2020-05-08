 /*
  * GameVisualisation.java
  *
  * Created on Apr 10, 2009, 2:13:23 PM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient;
 
 import java.awt.AlphaComposite;
 import shoddybattleclient.utils.*;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JToolTip;
 import javax.swing.ToolTipManager;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.shoddybattle.Pokemon.Gender;
 import shoddybattleclient.shoddybattle.PokemonSpecies;
 
 /**
  *
  * @author ben
  */
 
 interface PokemonDelegate {
         //Get a pokemon in a particular party and slot
         public GameVisualisation.VisualPokemon getPokemonForSlot(int party, int slot);
 }
 
 public class GameVisualisation extends JLayeredPane implements PokemonDelegate {
 
     private static class Pokeball extends JPanel {
         private VisualPokemon m_pokemon;
         public Pokeball(VisualPokemon p) {
             setOpaque(false);
             m_pokemon = p;
             setToolTipText("asdf");
         }
         public Dimension getPreferredSize() {
             return new Dimension(18, 18);
         }
         public void paintComponent(Graphics g) {
             Graphics2D g2 = (Graphics2D)g.create();
             //g2.scale(0.9, 0.9);
             g2.setRenderingHint(
                     RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             int w = getPreferredSize().width - 2;
             int h = getPreferredSize().height - 2;
             if (m_pokemon.getState() == State.FAINTED) {
                 g2.setColor(Color.DARK_GRAY);
                 g2.fillArc(0, 0, w, h, 0, 360);
                 g2.dispose();
                 return;
             }
             g2.setColor(Color.BLACK);
             g2.fillArc(0, 0, w, h, 0, 360);
             g2.setColor(Color.WHITE);
             g2.fillArc(1, 1, w - 2, h - 2, 0, 360);
             g2.setColor(Color.RED);
             g2.fillArc(1, 1, w - 2, h - 2, 0, 180);
             g2.fillArc(1, h / 2 - 2, w - 2, h / 5, 0, -180);
             g2.setColor(Color.BLACK);
             g2.drawArc(1, h / 2 - 2, w - 2, h / 5, 0, -180);
             g2.fillArc(w / 2 - 3, h/2 - 2, 6, 6, 0, 360);
             g2.setColor(Color.WHITE);
             g2.fillArc(w / 2 - 2, h/2 - 1, 4, 4, 0, 360);
             if (m_pokemon.getState() != State.NORMAL) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                 g2.setColor(Color.GRAY);
                 g2.fillArc(0, 0, w, h, 0, 360);
                 g2.setComposite(AlphaComposite.SrcOver);
                 g2.drawString(String.valueOf(m_pokemon.getState().ordinal()), w / 2, h / 2);
             }
             g2.dispose();
         }
         @Override
         public JToolTip createToolTip() {
             return new JCustomTooltip(this,
                     new VisualToolTip(m_pokemon, true));
         }
     }
 
     private static class Sprite extends JPanel {
         private boolean m_front;
         private int m_frame = 0;
         private Image m_image = null;
         private Dimension m_size = new Dimension(84, 84);
         private int m_party;
         private int m_slot;
         private PokemonDelegate m_delegate;
         public Sprite(int party, int slot, boolean front,
                 PokemonDelegate delegate) {
             setOpaque(false);
             setToolTipText("");
             m_front = front;
             m_party = party;
             m_slot = slot;
             m_delegate = delegate;
         }
         public void setSprite(VisualPokemon p) {
             if (p == null) {
                 m_image = null;
                 repaint();
                 return;
             }
             boolean male = Gender.GENDER_MALE.ordinal() == p.m_gender;
             m_image = p.getImage(m_front, male);
             if (m_image == null) {
                 repaint();
                 return;
             }
             MediaTracker tracker = new MediaTracker(this);
             tracker.addImage(m_image, 0);
             try {
                 tracker.waitForAll();
             } catch (Exception e) {
                 
             }
             if (m_image != null) {
                 m_size = new Dimension(m_image.getWidth(this),
                         m_image.getHeight(this));
                 setSize(m_size);
             }
             repaint();
         }
         public Dimension getPreferredSize() {
             return m_size;
         }
         public void paintComponent(Graphics g) {
             g.drawImage(m_image, 0, 0, this);
         }
         @Override
         public JToolTip createToolTip() {
             VisualPokemon p = m_delegate.getPokemonForSlot(m_party, m_slot);
             if (p == null) {
                 setToolTipText(null);
                 return new JToolTip();
             }
             return new JCustomTooltip(this, new VisualToolTip(p, true));
         }
     }
 
     public static class StatusObject {
         private String m_id;
         private String m_name;
         private int m_turns = -1;
         private int m_count = 0;
         public StatusObject(String id, String name) {
             m_id = id;
             m_name = name;
         }
         public StatusObject(String id, String name, int turns) {
             m_id = id;
             m_name = name;
             m_turns = turns;
         }
         public String getId() {
             return m_id;
         }
         public String getName() {
             return m_name;
         }
         public String toString() {
             return (m_turns > 0) ? m_name + " {" + m_turns + "}" : m_name;
         }
         public void addCount() {
             ++m_count;
         }
         public int decreaseCount() {
             return --m_count;
         }
     }
 
     public static class VisualPokemon {
         private int m_id;
         private String m_species;
         private String m_name;
         private int m_level;
         private int m_gender;
         private boolean m_shiny;
         private List<StatusObject> m_statuses = new ArrayList<StatusObject>();
         private int m_healthN = 48;
         private int m_healthD = 48;
         private int[] m_statLevels = new int[8];
         private boolean m_visible = true;
         private int m_slot = -1;
         private boolean m_fainted = false;
         private Pokemon m_pokemon = null;
         private int m_frame = 1;
         private State m_state = State.NORMAL;
         private static Map<String, State> m_stateMap = new HashMap<String, State>();
         static {
             m_stateMap.put(Text.getText(6, 0), State.BURNED);
             m_stateMap.put(Text.getText(8, 0), State.FROZEN);
             m_stateMap.put(Text.getText(9, 0), State.PARALYSED);
             m_stateMap.put(Text.getText(10, 0), State.SLEEPING);
             m_stateMap.put(Text.getText(11, 0), State.POISONED);
         }
 
         public VisualPokemon(int id, int gender, int level, boolean shiny) {
             m_id = id;
             m_gender = gender;
             m_level = level;
             m_shiny = shiny;
         }
         public VisualPokemon() {
             m_species = "???";
             m_gender = 0;
             m_level = 100;
             m_shiny = false;
         }
         public String getSpecies() {
             if (m_pokemon != null) {
                 return m_pokemon.species;
             }
             return m_species;
         }
         public void setName(String name) {
             m_name = name;
         }
         public String getName() {
             if (m_pokemon != null) {
                 return ("".equals(m_pokemon.nickname))
                         ? m_pokemon.species : m_pokemon.nickname;
             }
             return m_name;
         }
         public Pokemon getPokemon() {
             return m_pokemon;
         }
         public void setSpeciesId(int id) {
             m_id = id;
         }
         public void setSpecies(String name) {
             m_species = name;
         }
         public void setGender(int gender) {
             m_gender = gender;
         }
         public int getGender() {
             return m_gender;
         }
         public void setShiny(boolean shiny) {
             m_shiny = shiny;
         }
         public boolean isShiny() {
             return m_shiny;
         }
         public void setLevel(int level) {
             m_level = level;
         }
         public int getLevel() {
             return m_level;
         }
         public StatusObject updateStatus(String id, String status,
                 boolean applied) {
             if (applied) {
                 return addStatus(id, status);
             } else {
                 removeStatus(status);
                 return null;
             }
         }
         public StatusObject addStatus(String id, String status) {
             StatusObject obj = new StatusObject(id, status);
             m_statuses.add(obj);
             if (m_stateMap.containsKey(status)) {
                 m_state = m_stateMap.get(status);
             }
             return obj;
         }
         public void removeStatus(String status) {
             Iterator<StatusObject> i = m_statuses.iterator();
             while (i.hasNext()) {
                 StatusObject obj = i.next();
                 if (obj.getName().equals(status))
                     i.remove();
             }
             if (m_stateMap.containsKey(status)) {
                 m_state = State.NORMAL;
             }
         }
         public List<StatusObject> getStatuses() {
             return m_statuses;
         }
         public void setHealth(int num, int denom) {
             m_healthN = num;
             m_healthD = denom;
         }
         public int getNumerator() {
             return m_healthN;
         }
         public int getDenominator() {
             return m_healthD;
         }
         public void updateStatLevel(int i, int delta, boolean applied) {
             int sign = (applied) ? 1 : -1;
             m_statLevels[i] += sign * delta;
         }
         public int getStatLevel(int idx) {
             return m_statLevels[idx];
         }
         public void setSlot(int slot) {
             m_slot = slot;
         }
         public int getSlot() {
             return m_slot;
         }
         public void faint() {
             m_fainted = true;
         }
         public boolean isFainted() {
             return m_fainted;
         }
         public State getState() {
             if (m_fainted) {
                 return State.FAINTED;
             } else {
                 return m_state;
             }
         }
         public void setPokemon(Pokemon p) {
             m_pokemon = p;
         }
         public void toggleFrame() {
             m_frame = (m_frame == 1) ? 2 : 1;
         }
         public int getFrame() {
             return m_frame;
         }
         public Image getImage(boolean front, boolean male) {
             if (isFainted()) return null;
             for (StatusObject status : m_statuses) {
                 if (status.getId().equals("SubstituteEffect")) {
                     return GameVisualisation.getSubstitute(front);
                 }
             }
             return GameVisualisation.getSprite(m_id, front, male, m_shiny);
         }
     }
 
     public static enum State {
         NORMAL,
         PARALYSED,
         BURNED,
         FROZEN,
         SLEEPING,
         POISONED,
         FAINTED;
     }
 
     private static final int BACKGROUND_COUNT = 23;
 
     private static final int RADIUS_SINGLE = 0;
     private static final int RADIUS_USER_PARTY = 1;
     private static final int RADIUS_ENEMY_PARTY = 2;
     private static final int RADIUS_GLOBAL = 3;
 
     private Image m_background;
     private VisualPokemon[][] m_active;
     private VisualPokemon[][] m_parties;
     private Pokeball[][] m_pokeballs;
     private Sprite[][] m_sprites;
     private int m_view;
     private int m_selected = -1;
     private int m_target = Integer.MAX_VALUE;
     private Graphics2D m_mouseInput;
     private static final IndexColorModel m_colours;
     private final List<PokemonSpecies> m_speciesList;
     private interface StringList extends List<String> {}
     private List<String>[] m_partyStatuses = new StringList[2];
     private Set<String> m_globalStatuses = new HashSet<String>();
     private int m_n;
     //max team length
     private int m_length;
 
     private int m_tooltipParty = Integer.MAX_VALUE;
     private int m_tooltipPoke = Integer.MAX_VALUE;
 
     static {
         ToolTipManager.sharedInstance().setInitialDelay(200);
         ToolTipManager.sharedInstance().setReshowDelay(200);
     }
 
     public static Image getImageFromResource(String file) {
         return Toolkit.getDefaultToolkit()
                 .createImage(GameVisualisation.class.getResource("resources/" + file));
     }
 
     static {
         byte[] r = new byte[4];
         byte[] g = new byte[4];
         byte[] b = new byte[4];
         r[0] = g[0] = b[0] = 0;
         r[1] = g[1] = b[1] = (byte)255;
         g[2] = g[3] = 1;
         b[2] = b[3] = 1;
         r[2] = 0;
         r[3] = 1;
         
         m_colours = new IndexColorModel(4, r.length, r, g, b);
 
         /*ToolTipManager manager = ToolTipManager.sharedInstance();
         manager.setInitialDelay(0);
         manager.setReshowDelay(0);*/
     }
     
     public GameVisualisation(int view, int n, int length, List<PokemonSpecies> speciesList) {
         this.setLayout(null);
         m_view = view;
         m_active = new VisualPokemon[2][n];
         m_parties = new VisualPokemon[2][length];
         m_pokeballs = new Pokeball[2][length];
         m_speciesList = speciesList;
 
         int background = new Random().nextInt(BACKGROUND_COUNT) + 1;
         m_background = getImageFromResource("backgrounds/background" + background + ".png");
         MediaTracker tracker = new MediaTracker(this);
         tracker.addImage(m_background, 0);
         try {
             tracker.waitForAll();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
        int rows = (int)Math.ceil(length / 3d);
         for (int i = 0; i < m_parties.length; i++) {
             for (int j = 0; j < m_parties[i].length; j++) {
                 VisualPokemon p = new VisualPokemon();
                 m_parties[i][j] = p;
                 Pokeball ball = new Pokeball(p);
                 m_pokeballs[i][j] = ball;
                 Dimension d = ball.getPreferredSize();
                 ball.setSize(d);
                 int x, y;
                 int w = m_background.getWidth(this);
                 int h = m_background.getHeight(this);
                 int row = j / 3;
                 int buff = 1;
                 x = (j - 3*row) * (d.width + buff) + 2;
                 y = (d.width + buff) * row + 2;
                 if (view == i) {
                    int pokeballWidth = Math.min(m_parties[i].length, 3)
                            * (d.width + buff);
                    x += w - pokeballWidth - 2;
                    y += h - rows * (d.height + buff);
                 }
                 ball.setLocation(x, y);
                 
                 this.add(ball);
             }
         }
         m_n = n;
         m_length = length;
         m_sprites = new Sprite[2][n];
         for (int i = 0; i < m_sprites.length; i++) {
             for (int j = 0; j < m_sprites[i].length; j++) {
                 boolean us = (i == m_view);
                 Sprite s = new Sprite(i, j, !us, this);
                 s.setSize(s.getPreferredSize());
                 s.setLocation(getSpriteLocation(us, j, m_n, 0, 0));
                 m_sprites[i][j] = s;
                 this.add(s, new Integer(m_view * m_sprites[i].length + j));
             }
         }
         setBorder(BorderFactory.createLineBorder(Color.GRAY));
 
         Dimension d = getPreferredSize();
         final BufferedImage image = new BufferedImage((int)d.getWidth(),
                 (int)d.getHeight(),
                 BufferedImage.TYPE_BYTE_BINARY,
                 m_colours);
         m_mouseInput = image.createGraphics();
 
         addMouseMotionListener(new MouseMotionAdapter() {
             @Override
             public void mouseMoved(MouseEvent e) {
                 int x = e.getX();
                 int y = e.getY();
                 if ((x > image.getWidth()) || (y > image.getHeight())) return;
                 Color c = new Color(image.getRGB(x, y));
                 if (c.equals(Color.WHITE)) {
                     setToolTipText(null);
                     return;
                 } else if (c.getGreen() == 1) {
                     //party
                     displayInformation(c.getRed(), -1);
                 } else if (c.equals(Color.BLACK)) {
                     //field
                     displayInformation(-1, -1);
                 }
             }
         });
     }
 
     public void setSpriteVisible(int party, int slot, boolean visible) {
         m_active[party][slot].m_visible = visible;
     }
 
     public void updateHealth(int party, int slot, int total, int denominator) {
         getPokemonForSlot(party, slot).setHealth(total, denominator);
     }
 
     public VisualPokemon getPokemon(int party, int index) {
         return m_parties[party][index];
     }
 
     public void updateStatus(int party, int position, int radius,
             String statusId, String msg, boolean applied) {
         VisualPokemon p = getPokemon(party, position);
         if (radius == RADIUS_USER_PARTY) {
             if (applied) {
                 m_partyStatuses[party].add(msg);
             } else {
                 m_partyStatuses[party].remove(msg);
             }
             return;
         }
         if (radius == RADIUS_ENEMY_PARTY) {
             if (applied) {
                 m_partyStatuses[1 - party].add(msg);
             } else {
                 m_partyStatuses[1 - party].remove(msg);
             }
             return;
         }
         if (radius == RADIUS_GLOBAL) {
             if (applied) {
                 m_globalStatuses.add(msg);
             } else {
                 m_globalStatuses.remove(msg);
             }
             return;
         }
         String[] parts = msg.split(";");
         if (parts.length == 1) {
             p.updateStatus(statusId, msg, applied);
         } else {
             if ("StatChangeEffect".equals(parts[0])) {
                 int stat = Integer.parseInt(parts[1]);
                 int delta = Integer.parseInt(parts[2]);
                 p.updateStatLevel(stat, delta, applied);
             }
             //todo: other cases with additional information
         }
         m_pokeballs[party][position].repaint();
     }
 
     void faint(int party, int slot) {
         getPokemonForSlot(party, slot).faint();
         repaint();
     }
 
     private void displayInformation(int party, int idx) {
         m_tooltipParty = party;
         m_tooltipPoke = idx;
         List<String> effects;
         if (party == -1) {
             effects = new ArrayList<String>();
             for (String eff : m_globalStatuses) {
                 effects.add(eff);
             }
         } else {
             effects = m_partyStatuses[party];
         }
         if ((effects == null) || (effects.size() == 0)) {
             setToolTipText(null);
             return;
         }
         String text = FindPanel.join(effects, "<br>");
         text = "<html>" + text + "</html>";
         setToolTipText(text);
 
     }
 
     public void updateSprite(int party, int slot) {
         Sprite s = m_sprites[party][slot];
         s.setSprite(getPokemonForSlot(party, slot));
         Dimension d = s.getPreferredSize();
         s.setLocation(getSpriteLocation(party == m_view, slot, m_n,
                 d.width, d.height));
     }
 
     public VisualPokemon getPokemonForSlot(int party, int slot) {
         for (int i = 0; i < m_parties[party].length; i++) {
             if (m_parties[party][i].getSlot() == slot)
                 return m_parties[party][i];
         }
         return null;
     }
 
     public void setSpecies(int party, int slot, String species) {
         VisualPokemon p = getPokemonForSlot(party, slot);
         if (p != null) {
             p.setSpecies(species);
         }
     }
 
     public void sendOut(final int party, final int slot, int index, 
             int speciesId, String species, String name, int gender, int level) {
         VisualPokemon p = getPokemonForSlot(party, slot);
         if (p != null) {
             p.setSlot(-1);
         }
         VisualPokemon newPoke = m_parties[party][index];
         newPoke.setSlot(slot);
         newPoke.setSpeciesId(speciesId);
         newPoke.setSpecies(species);
         newPoke.setName(name);
         newPoke.setLevel(level);
         newPoke.setGender(gender);
     }
 
     public void setPokemon(int party, int index, Pokemon p) {
         m_parties[party][index].setPokemon(p);
     }
 
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(m_background.getWidth(this), m_background.getHeight(this));
     }
 
 
     public void setSelected(int i) {
         m_selected = i;
         repaint();
     }
 
     public void setTarget(int i) {
         m_target = i;
         repaint();
     }
 
     // returns the number of active pokemon in a party
     public int getActiveCount(int party) {
         VisualPokemon[] pokes = m_parties[party];
         int active = 0;
         for (VisualPokemon p : pokes) {
             if (!p.m_fainted) active++;
         }
         return active;
     }
 
     @Override
     public JToolTip createToolTip() {
         if (m_tooltipParty == -1) {
             return new JToolTip();
         } else if (m_tooltipPoke == -1) {
             return new JToolTip();
         }
         VisualPokemon p = m_parties[m_tooltipParty][m_tooltipPoke];
         if (p == null) return new JToolTip();
         
         // TODO: adjust final parameter for spectator support
         VisualToolTip vt = new VisualToolTip(p, m_tooltipParty == m_view);
         return new JCustomTooltip(this, vt);
     }
 
     private Point getSpriteLocation(boolean us, int slot, int n, int w, int h) {
         int x = 0, y;
         int hw = w /2 ;
         Dimension d = this.getPreferredSize();
         if (us) {
             y = d.height - h;
             if (n == 1) {
                 x = 50 - hw;
             } else if (n == 2) {
                 x = (slot == 0) ? 30 - hw: 90 - hw;
             } else if (n == 3) {
                 x = (slot == 0) ? 20 - hw : (slot == 1) ? 55 - hw : 90 - hw;
             } else {
                 x = slot * 23;
             }
         } else {
             y = 90 - h;
             if (n == 1) {
                 x = 190 - hw;
             } else if (n == 2) {
                 x = (slot == 0) ? 170 - hw : 220 - hw;
             } else if (n == 3) {
                 x = (slot == 0) ? 160 - hw : (slot == 1) ? 190 - hw : 220 - hw;
             } else {
                 x = 220 - n * 23 + 23 * slot;
                 y -= 5;
             }
         }
         return new Point(x, y);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         m_mouseInput.setColor(Color.BLACK);
         m_mouseInput.fillRect(0, 0, getWidth(), getHeight());
         m_mouseInput.setColor(Color.WHITE);
         m_mouseInput.fillRect(0, 0, 60, 36);
         m_mouseInput.fillRect(getWidth() - 60, getHeight() - 36, 60, 36);
         m_mouseInput.setColor(new Color(m_view, 1, 0));
         m_mouseInput.fillRect(0, getHeight() - 60, 220, 60);
         m_mouseInput.setColor(new Color(1 - m_view, 1, 0));
         m_mouseInput.fillRect(70, 10, 220, 60);
         g2.drawImage(m_background, 0, 0, this);
         g2.dispose();
     }
 
     private static String createPath(String filename, boolean front,
             boolean shiny, String repo, int frame) {
         StringBuilder builder = new StringBuilder();
         builder.append(Preference.getSpriteLocation());
         builder.append(repo);
         builder.append("/");
         builder.append(front ? "front" : "back");
         builder.append("/");
         builder.append(shiny ? "shiny" : "normal");
         builder.append(frame == 1 ? "" : String.valueOf(frame));
         builder.append("/");
         builder.append(filename);
         return builder.toString();
     }
     
     private static String createPath(int number, boolean front, boolean male,
             boolean shiny, String repo, int frame) {
         StringBuilder builder = new StringBuilder();
         builder.append(number);
         builder.append(male ? "" : "f");
         builder.append(".png");
         return createPath(builder.toString(), front, shiny, repo, frame);
     }
 
     private static String getSpritePath(int number, boolean front, boolean male,
             boolean shiny, String repo, int frame) {
         //look for the correct sprite, then the opposite gender, then the first frames
         File f = new File(createPath(number, front, male, shiny, repo, frame));
         if (f.exists()) return f.toString();
         f = new File(createPath(number, front, !male, shiny, repo, frame));
         if (f.exists()) return f.toString();
         f = new File(createPath(number, front, male, shiny, repo, 1));
         if (f.exists()) return f.toString();
         f = new File(createPath(number, front, !male, shiny, repo, 1));
         if (f.exists()) return f.toString();
         return null;
     }
 
     private static Image getSprite(String path, boolean front) {
         if (path == null) {
             String image = (front) ? "missingno_front.png" :
                     "missingno_back.png";
             return GameVisualisation.getImageFromResource(image);
         }
         return Toolkit.getDefaultToolkit().createImage(path);
     }
     
     public static Image getSprite(int number, boolean front, boolean male,
             boolean shiny, int frame) {
         String qualified = null;
         for (String repo : Preference.getSpriteDirectories()) {
             qualified = getSpritePath(number, front, male, shiny, repo, frame);
             if (qualified != null) break;
         }
 
         return getSprite(qualified, front);
     }
 
     public static Image getSprite(int number, boolean front, boolean male,
             boolean shiny) {
         return getSprite(number, front, male, shiny, 1);
     }
 
     public Image getSprite(String name, boolean front, boolean male,
             boolean shiny, int frame) {
         int number = PokemonSpecies.getIdFromName(m_speciesList, name);
         return getSprite(number, front, male, shiny, frame);
     }
 
     public Image getSprite(String name, boolean front, boolean male, boolean shiny) {
         return getSprite(name, front, male, shiny, 1);
     }
 
     public static Image getSubstitute(boolean front) {
         String qualified = null;
         for (String repo : Preference.getSpriteDirectories()) {
             String path = createPath("substitute.png", front, false, repo, 1);
             if (new File(path).exists()) {
                 qualified = path;
                 break;
             }
         }
 
         return getSprite(qualified, front);
     }
 
     public static void main(String[] args) {
         JFrame frame = new JFrame("Visualisation test");
 
         SpeciesListParser slp = new SpeciesListParser();
         List<PokemonSpecies> species = slp.parseDocument(
                 GameVisualisation.class.getResource("resources/species.xml").toString());
         GameVisualisation panel = new GameVisualisation(1, 2, 6, species);
         frame.setSize(panel.getPreferredSize().width, panel.getPreferredSize().height + 20);
         Random r = new java.util.Random();
         VisualPokemon p1 = new VisualPokemon(1, 1, 100, false);
         VisualPokemon p2 = new VisualPokemon(2, 1, 100, false);
         VisualPokemon p3 = new VisualPokemon(3, 1, 100, false);
         VisualPokemon p4 = new VisualPokemon(4, 1, 100, false);
         /*VisualPokemon p5 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p6 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p7 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p8 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p9 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p10 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p11 = new VisualPokemon(r.nextInt(505), 1, 100, false);
         VisualPokemon p12 = new VisualPokemon(r.nextInt(505), 1, 100, false);*/
         panel.sendOut(0, 0, 0, p1.m_id, p1.getSpecies(), p1.getSpecies(), 
                 p1.getGender(), p1.getLevel());
         panel.sendOut(0, 1, 1, p2.m_id, p1.getSpecies(), p2.getSpecies(),
                 p2.getGender(), p2.getLevel());
         panel.sendOut(1, 0, 0, p3.m_id, p1.getSpecies(), p3.getSpecies(),
                 p3.getGender(), p3.getLevel());
         panel.sendOut(1, 1, 1, p4.m_id, p1.getSpecies(), p4.getSpecies(),
                 p4.getGender(), p4.getLevel());
         panel.updateStatus(0, 0, 0, "SubstituteEffect", "Substitute", true);
         panel.updateStatus(1, 1, 0, "SubstituteEffect", "Substitute", true);
         panel.updateSprite(0, 0);
         panel.updateSprite(0, 1);
         panel.updateSprite(1, 0);
         panel.updateSprite(1, 1);
         frame.setSize(panel.getPreferredSize().width, panel.getPreferredSize().height + 20);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.add(panel);
         frame.setVisible(true);
     }
 }
