 package org.drooms.impl.util;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.SortedMap;
 
 import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
 import org.drooms.api.Player;
 
 import freemarker.ext.beans.BeansWrapper;
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 
 public abstract class TournamentResults {
 
     public static class GameResults {
 
         private final Map<Player, DescriptiveStatistics> stats = new HashMap<>();
         private int games = 0;
 
         private GameResults() {
             // prevent outside instantiation
         }
 
         public void addResults(final Map<Player, Integer> result) {
             this.games++;
             for (final Map.Entry<Player, Integer> entry : result.entrySet()) {
                 final Player p = entry.getKey();
                 final int points = entry.getValue();
                 if (!this.stats.containsKey(p)) {
                     this.stats.put(p, new DescriptiveStatistics());
                 }
                 this.stats.get(p).addValue(points);
             }
         }
 
         public BigDecimal getAverage(final Player p) {
             return BigDecimal.valueOf(this.getStats(p).getMean());
         }
 
         public BigDecimal getFirstQuartile(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(0.25));
         }
 
         // FIXME better name
         public int getGames() {
             return this.games;
         }
 
         public BigDecimal getMax(final Player p) {
             return BigDecimal.valueOf(this.getStats(p).getMax());
         }
 
         public BigDecimal getMedian(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(0.5));
         }
 
         public BigDecimal getMin(final Player p) {
             return BigDecimal.valueOf(this.getStats(p).getMin());
         }
 
         public BigDecimal getStandardDeviation(final Player p) {
             return BigDecimal.valueOf(this.getStats(p).getStandardDeviation());
         }
 
         private DescriptiveStatistics getStats(final Player p) {
             if (this.stats.containsKey(p)) {
                 return this.stats.get(p);
             } else {
                 final DescriptiveStatistics d = new DescriptiveStatistics();
                 d.addValue(0.0);
                 return d;
             }
         }
 
         public BigDecimal getThirdQuartile(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(0.75));
         }
     }
 
     private final Map<String, GameResults> results = new HashMap<>();
 
     private final Collection<Player> players;
     private final String name;
 
     public TournamentResults(final String name, final Collection<Player> players) {
         this.name = name;
         this.players = Collections.unmodifiableCollection(players);
     }
 
     public void addResults(final String game, final Map<Player, Integer> result) {
         if (!this.results.containsKey(game)) {
             this.results.put(game, new GameResults());
         }
         this.results.get(game).addResults(result);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Map assembleGameOverview() {
         final Map result = new HashMap();
         for (final Map.Entry<String, GameResults> game : this.results
                 .entrySet()) {
             result.put(game.getKey(),
                     this.evaluateGame(this.players, game.getValue()));
         }
         return Collections.unmodifiableMap(result);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Map assembleResults() {
         final Map result = new HashMap();
         result.put("name", this.name);
         result.put("players", this.players);
         result.put("games", this.assembleTournamentOverview());
         result.put("gameScore", this.assembleGameOverview());
         result.put("gameResults", this.results);
         result.put("results", this.evaluate());
         return Collections.unmodifiableMap(result);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Map assembleTournamentOverview() {
         final Map result = new HashMap();
         for (final Map.Entry<String, GameResults> game : this.results
                 .entrySet()) {
             result.put(game.getKey(), game.getValue().getGames());
         }
         return Collections.unmodifiableMap(result);
     }
 
     public Map<Long, Collection<Player>> evaluate() {
         final Collection<List<Collection<Player>>> gameResults = new LinkedList<>();
         for (final String game : this.getGameNames()) {
             gameResults.add(this.evaluateGame(this.players,
                     this.results.get(game)));
         }
         return this.evaluateTournament(this.players, gameResults);
     }
 
     protected abstract List<Collection<Player>> evaluateGame(
             final Collection<Player> players, final GameResults game);
 
     protected abstract SortedMap<Long, Collection<Player>> evaluateTournament(
             final Collection<Player> players,
             final Collection<List<Collection<Player>>> gameResults);
 
     public Collection<String> getGameNames() {
         return Collections.unmodifiableSet(this.results.keySet());
     }
 
     public void write(final Writer w) throws IOException {
         final Configuration freemarker = new Configuration();
         freemarker.setClassForTemplateLoading(TournamentResults.class, "");
         freemarker.setObjectWrapper(new BeansWrapper());
         freemarker.setLocale(Locale.US);
         freemarker.setNumberFormat("computer");
         try {
             freemarker.getTemplate("tournament-report.html.ftl").process(
                     this.assembleResults(), w);
         } catch (final TemplateException ex) {
             throw new IllegalStateException("Invalid template.", ex);
         }
     }
 
 }
