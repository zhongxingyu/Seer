 package risk.game;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 
 public class Map {
     /**
      * A list of continents.
      */
     private LinkedList<Continent> continents;
     
     public Map() {
         continents = new LinkedList<Continent>();
         
         initEurope();
         initNorthAmerica();
         initSouthAmerica();
         initAsia();
         initAfrica();
         initAustralia();
         
         setNeighbours();
     }
 
     private void initEurope() {
         LinkedList<Country> countries = new LinkedList<Country>();
         String[] names = {
                 Country.ICELAND,
                 Country.SCANDINAVIA,
                 Country.GREATBRITAIN,
                 Country.NORTHERNEUROPE,
                 Country.WESTERNEUROPE,
                 Country.SOUTHERNEUROPE,
         };
         
         for (String n : names) {
             countries.add(new Country(n));
         }
         continents.add(new Continent("Europe", countries));
     }
     
     private void initNorthAmerica() {
         LinkedList<Country> countries = new LinkedList<Country>();
         String[] names = {
                 Country.ALASKA,
                 Country.NORTHWESTTERRITORY,
                 Country.GREENLAND,
                 Country.EASTERNCANADA,
                 Country.ALBERTA,
                 Country.ONTARIO,
                 Country.EASTERNUNITEDSTATES,
                 Country.WESTERNUNITEDSTATES,
                 Country.CENTRALAMERICA,
         };
         
         for (String n : names) {
             countries.add(new Country(n));
         }
         continents.add(new Continent("North America", countries));
     }
     
     private void initSouthAmerica() {
         LinkedList<Country> countries = new LinkedList<Country>();
         String[] names = {
                 Country.VENEZUELA,
                 Country.BRAZIL,
                 Country.PERU,
                 Country.ARGENTINA,
         };
 
         for (String n : names) {
             countries.add(new Country(n));
         }
         continents.add(new Continent("South America", countries));
     }
     
     private void initAfrica() {
         LinkedList<Country> countries = new LinkedList<Country>();
         String[] names = {
                 Country.EGYPT,
                 Country.NORTHAFRICA,
                 Country.EASTAFRICA,
                 Country.CENTRALAFRICA,
                 Country.SOUTHAFRICA,
                 Country.MADAGASCAR,
         };
 
         for (String n : names) {
             countries.add(new Country(n));
         }
        continents.add(new Continent("Africa", countries));
     }
     
     private void initAsia() {
         LinkedList<Country> countries = new LinkedList<Country>();
         String[] names = {
                 Country.RUSSIA,
                 Country.URAL,
                 Country.SIBERIA,
                 Country.YAKUTSK,
                 Country.KAMCHATKA,
                 Country.AFGHANISTAN,
                 Country.MIDDLEEAST,
                 Country.INDIA,
                 Country.CHINA,
                 Country.SIAM,
                 Country.IRKUTSK,
                 Country.JAPAN,
                 Country.MONGOLIA,
         };
         
         for (String n : names) {
             countries.add(new Country(n));
         }
         continents.add(new Continent("Asia", countries));
     }
     
     private void initAustralia() {
         LinkedList<Country> countries = new LinkedList<Country>();
         String[] names = {
                 Country.INDONESIA,
                 Country.EASTERNAUSTRALIA,
                 Country.WESTERNAUSTRALIA,
                 Country.NEWGUINEA,
         };
         
         for (String n : names) {
             countries.add(new Country(n));
         }
         continents.add(new Continent("Australia", countries));
     }
 
     private void setNeighbours(String a, String b) {
         Country ca = getCountry(a);
         Country cb = getCountry(b);
         
         ca.addNeighbour(cb);
         cb.addNeighbour(ca);
     }
     
     private void setNeighbours() {
         setNeighbours(Country.ALASKA, Country.NORTHWESTTERRITORY);
         setNeighbours(Country.ALASKA, Country.KAMCHATKA);
         setNeighbours(Country.ALASKA, Country.ALBERTA);
         setNeighbours(Country.NORTHWESTTERRITORY, Country.GREENLAND);
         setNeighbours(Country.NORTHWESTTERRITORY, Country.ALBERTA);
         setNeighbours(Country.NORTHWESTTERRITORY, Country.ONTARIO);
         setNeighbours(Country.GREENLAND, Country.ONTARIO);
         setNeighbours(Country.GREENLAND, Country.EASTERNCANADA);
         setNeighbours(Country.GREENLAND, Country.ICELAND);
         setNeighbours(Country.ALBERTA, Country.ONTARIO);
         setNeighbours(Country.ALBERTA, Country.WESTERNUNITEDSTATES);
         setNeighbours(Country.ONTARIO, Country.EASTERNCANADA);
         setNeighbours(Country.ONTARIO, Country.WESTERNUNITEDSTATES);
         setNeighbours(Country.ONTARIO, Country.EASTERNUNITEDSTATES);
         setNeighbours(Country.EASTERNCANADA, Country.EASTERNUNITEDSTATES);
         setNeighbours(Country.WESTERNUNITEDSTATES, Country.EASTERNUNITEDSTATES);
         setNeighbours(Country.WESTERNUNITEDSTATES, Country.CENTRALAMERICA);
         setNeighbours(Country.EASTERNUNITEDSTATES, Country.CENTRALAMERICA);
         setNeighbours(Country.CENTRALAMERICA, Country.VENEZUELA);
         setNeighbours(Country.VENEZUELA, Country.PERU);
         setNeighbours(Country.VENEZUELA, Country.BRAZIL);
         setNeighbours(Country.PERU, Country.BRAZIL);
         setNeighbours(Country.PERU, Country.ARGENTINA);
         setNeighbours(Country.BRAZIL, Country.ARGENTINA);
         setNeighbours(Country.BRAZIL, Country.NORTHAFRICA);
         setNeighbours(Country.ICELAND, Country.SCANDINAVIA);
         setNeighbours(Country.ICELAND, Country.GREATBRITAIN);
         setNeighbours(Country.SCANDINAVIA, Country.RUSSIA);
         setNeighbours(Country.SCANDINAVIA, Country.GREATBRITAIN);
         setNeighbours(Country.SCANDINAVIA, Country.NORTHERNEUROPE);
         setNeighbours(Country.GREATBRITAIN, Country.NORTHERNEUROPE);
         setNeighbours(Country.GREATBRITAIN, Country.WESTERNEUROPE);
         setNeighbours(Country.NORTHERNEUROPE, Country.SOUTHERNEUROPE);
         setNeighbours(Country.NORTHERNEUROPE, Country.RUSSIA);
         setNeighbours(Country.NORTHERNEUROPE, Country.WESTERNEUROPE);
         setNeighbours(Country.WESTERNEUROPE, Country.SOUTHERNEUROPE);
         setNeighbours(Country.WESTERNEUROPE, Country.NORTHAFRICA);
         setNeighbours(Country.SOUTHERNEUROPE, Country.MIDDLEEAST);
         // setNeighbours(Country.SOUTHERNEUROPE, Country.NORTHAFRICA); ????
         setNeighbours(Country.NORTHAFRICA, Country.EGYPT);
         setNeighbours(Country.NORTHAFRICA, Country.EASTAFRICA);
         setNeighbours(Country.NORTHAFRICA, Country.CENTRALAFRICA);
         setNeighbours(Country.EGYPT, Country.MIDDLEEAST);
         setNeighbours(Country.EGYPT, Country.EASTAFRICA);
         setNeighbours(Country.EASTAFRICA, Country.CENTRALAFRICA);
         // setNeighbours(Country.EASTAFRICA, Country.MADAGASCAR); ????
         setNeighbours(Country.CENTRALAFRICA, Country.SOUTHAFRICA);
         setNeighbours(Country.SOUTHAFRICA, Country.MADAGASCAR);
         setNeighbours(Country.RUSSIA, Country.URAL);
         setNeighbours(Country.RUSSIA, Country.AFGHANISTAN);
         setNeighbours(Country.RUSSIA, Country.MIDDLEEAST);
         setNeighbours(Country.URAL, Country.SIBERIA);
         setNeighbours(Country.URAL, Country.AFGHANISTAN);
         setNeighbours(Country.URAL, Country.CHINA);
         setNeighbours(Country.SIBERIA, Country.YAKUTSK);
         setNeighbours(Country.SIBERIA, Country.IRKUTSK);
         setNeighbours(Country.SIBERIA, Country.MONGOLIA);
         setNeighbours(Country.SIBERIA, Country.CHINA);
         setNeighbours(Country.YAKUTSK, Country.KAMCHATKA);
         setNeighbours(Country.YAKUTSK, Country.IRKUTSK);
         setNeighbours(Country.KAMCHATKA, Country.JAPAN);
         setNeighbours(Country.KAMCHATKA, Country.MONGOLIA);
         setNeighbours(Country.IRKUTSK, Country.KAMCHATKA);
         setNeighbours(Country.IRKUTSK, Country.MONGOLIA);
         setNeighbours(Country.MONGOLIA, Country.JAPAN);
         setNeighbours(Country.MONGOLIA, Country.CHINA);
         setNeighbours(Country.AFGHANISTAN, Country.CHINA);
         setNeighbours(Country.AFGHANISTAN, Country.INDIA);
         setNeighbours(Country.AFGHANISTAN, Country.MIDDLEEAST);
         setNeighbours(Country.CHINA, Country.INDIA);
         setNeighbours(Country.CHINA, Country.SIAM);
         setNeighbours(Country.MIDDLEEAST, Country.INDIA);
         setNeighbours(Country.INDIA, Country.SIAM);
         setNeighbours(Country.SIAM, Country.INDONESIA);
         setNeighbours(Country.INDONESIA, Country.NEWGUINEA);
         setNeighbours(Country.INDONESIA, Country.WESTERNAUSTRALIA);
         setNeighbours(Country.INDONESIA, Country.EASTERNAUSTRALIA);
         setNeighbours(Country.NEWGUINEA, Country.EASTERNAUSTRALIA);
         setNeighbours(Country.WESTERNAUSTRALIA, Country.EASTERNAUSTRALIA);
     }
     
     public Country getCountry(String countryName) {
         for(Continent c : continents) {
             for (Country country : c.getCountries()) {
                 if (country.getName().compareTo(countryName) == 0) {
                     return country;
                 }
             }
         }
         return null;
     }
 
     public Collection<Continent> getContinents() {
         return continents;
     }
 
     public void selectCountry(Country c) {
         c.setSelected(true);
     }
 
     public void cancelCountrySelection(Country c) {
         c.setSelected(false);
     }
 
     public Country getSelectedCountryNeighbour(Country c) {
         for (Country neighbour : c.getNeighbours()) {
             if (neighbour.isSelected()) {
                 return neighbour;
             }
         }
         return null;
     }
     
     public boolean isCountrySelected(Country c) {
         return c.isSelected();
     }
 
     public void cancelCountryNeighbourSelection(Country c) {
         for (Country neighbour : c.getNeighbours()) {
             if (neighbour.isSelected()) {
                 neighbour.setSelected(false);
                 break;
             }
         }
     }
 }
