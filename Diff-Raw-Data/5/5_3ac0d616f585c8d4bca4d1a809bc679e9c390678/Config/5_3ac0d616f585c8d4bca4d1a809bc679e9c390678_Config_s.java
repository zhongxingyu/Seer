
 class Config {
 
     // pouze pro vytvoreni seznamu
     private Converter set = new Converter();
 
     // prida vsechny jednotky do seznamu (vse je uvedeno v metrech)
     public Config() {
         set.addUnit(new Unit("m", "Metr", 1));
         set.addUnit(new Unit("km", "Kilometr", 1000));
         set.addUnit(new Unit("cm", "Centimetr", 0.01));
         set.addUnit(new Unit("dm", "Decimetr", 0.1));
         set.addUnit(new Unit("mm", "Milimetr", 0.001));
         set.addUnit(new Unit("lea.", "League (Pěší hodina) = 3 mi.", 4828.032));
         set.addUnit(new Unit("mi.", "Míle", 1609.344));
         set.addUnit(new Unit("fur.", "Furlong (Délka brázdy) = 220 yd. => 660 ft.", 201.168));
         set.addUnit(new Unit("ch", "Chain (Podle kovového řetězu o 100 článcích) = 22 yd.", 20.1168));
         set.addUnit(new Unit("ech", "Engineer chain", 30.48));
         set.addUnit(new Unit("link", "???", 0.201168));
         set.addUnit(new Unit("fall", "??? = 6 ell", 6.858));
         set.addUnit(new Unit("rd.", "Rod (pole, perch)", 5.0292));
         set.addUnit(new Unit("fm.", "Fathom (námořní a hornická míra pro měření hloubky)", 1.8288));
         set.addUnit(new Unit("goad", "Pro měření tkaniny = 54 in. => 1.5 mi.", 1.3716));
         set.addUnit(new Unit("ell", "Pro měření tkaniny = 20 nail", 1.143));
         set.addUnit(new Unit("yd.", "Yard (vzdálenost mezi špičkou nosu a palcem) = 3 ft.", 0.9144));
         set.addUnit(new Unit("ft.", "Feet (stopa) = 12 in", 0.3048));
         set.addUnit(new Unit("nail", "Pro měření tkaniny = 1/16 yd.", 0.05715));
         set.addUnit(new Unit("in", "Inch (palec)", 0.0254));
         set.addUnit(new Unit("line", "Desetina in (palce), někdy jako 1/12", 0.00254));
         set.addUnit(new Unit("mil", "Tisícina in (palce)", 0.0000254));
         set.addUnit(new Unit("n.m.", "Námořní míle", 1852));
         set.addUnit(new Unit("n mile", "Britská námořní míle", 1853.181));
         set.addUnit(new Unit("geo mile)", "Zeměpisná míle", 7420));
         set.addUnit(new Unit("AU", "Astronomická jednotka", 149597870000.0));
         set.addUnit(new Unit("ly", "Světelný rok", 9.4605E+15));
         set.addUnit(new Unit("pc", "Parsek (parallax of one arc second)", 3.0857E+16));
         set.addUnit(new Unit("hubble", "Miliarda světelných let", 9.4605E+24));
         set.addUnit(new Unit("S", "Spat (miliarda kilometrů)", 1E+12));
         set.addUnit(new Unit("fm", "Fermi", 1E-15));
         set.addUnit(new Unit("A", "Angström", 1E-10));
         set.addUnit(new Unit("X", "Vlnová délka RTG a gama záření", 1.002077897E-13));
         set.addUnit(new Unit("U", "Rozměr rozvaděčů a počítačových skříní (rack)", 0.04445));
         set.addUnit(new Unit("px", "Pixel", 3780));
         set.addUnit(new Unit("pt", "Point (používá se u tiskáren)", 2845));
         set.addUnit(new Unit("prst", "Prostě prst", 50.201));
         set.addUnit(new Unit("prut", "Prostě prut", 0.209));
        set.addUnit(new Unit("píď", "Prostě píď", 5.02)); 
     }
 
     // vrati prevodnik s naplnenym seznamem
     public Converter returnConfig() {
         return set;
     }
 }
