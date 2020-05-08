 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Tsoha.controller;
 
 import Tsoha.domain.Peli;
 import Tsoha.domain.Genre;
 import Tsoha.domain.Kommentti;
 import Tsoha.domain.Arvostelu;
 import Tsoha.service.GenreService;
 import Tsoha.service.KommenttiService;
 import Tsoha.service.PeliService;
 import Tsoha.service.ArvosteluService;
 import java.util.List;
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 @Controller
 public class PeliController {
 
     @Autowired
     private PeliService peliService;
     @Autowired
     private GenreService genreService;
     @Autowired
     private KommenttiService kommenttiService;
     @Autowired
     private ArvosteluService arvosteluService;
 
     @RequestMapping(value = "/")
     public String kuuntele() {
         return "redirect:/home";
     }
 
     @RequestMapping(value = "lisaaPeli", method = RequestMethod.POST)
     public String lisaaPeli(@Valid @ModelAttribute("peli") Peli peli, BindingResult result, Model model) {
         if (result.hasErrors()) {
             model.addAttribute("genre", new Genre());
             return "lisaa";
         }
         peli = peliService.add(peli);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "lisaaGenre", method = RequestMethod.POST)
     public String lisaaGenre(@Valid @ModelAttribute("genre") Genre genre, BindingResult result, Model model) {
         if (result.hasErrors()) {
             model.addAttribute("peli", new Peli());
             return "lisaa";
         }
         genre = genreService.add(genre);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "listaa")
    public String listaaPelitJaGenret(Model model) {
         model.addAttribute("pelit", peliService.listAll());
         model.addAttribute("genret", genreService.listAll());
         return "listaa";
     }
 
     @RequestMapping(value = "kommentoi/{peliId}")
     public String kommentoiPelia(@PathVariable Integer peliId, Model model) {
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "kommentoi";
     }
 
     @RequestMapping(value = "poista/{peliId}")
     public String poistaPeli(@PathVariable Integer peliId) {
         Peli peli = peliService.findPeli(peliId);
         peliService.remove(peli);
         return "redirect:/listaa";
     }
     
     @RequestMapping(value = "poistaArvostelu/{arvosteluId}")
     public String poistaArvostelu(@PathVariable Integer arvosteluId) {
         Arvostelu arvostelu = arvosteluService.findArvostelu(arvosteluId);
         arvosteluService.remove(arvostelu);
         return "redirect:/listaa";
     }
     
     @RequestMapping(value = "poistaKommentti/{kommenttiId}")
     public String poistaKommentti(@PathVariable Integer kommenttiId) {
         Kommentti kommentti = kommenttiService.findKommentti(kommenttiId);
         kommenttiService.remove(kommentti);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "/404.html")
     public String error404() {
         return "error";
     }
     
     @RequestMapping(value = "/403.html")
     public String error403() {
         return "authorityError";
     }
 
     @RequestMapping(value = "poistaLaina/{peliId}")
     public String poistaLainasta(@PathVariable Integer peliId) {
         Peli peli = peliService.findPeli(peliId);
         peli.setLainassa(null);
         peliService.add(peli);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "lisaaGenreen/{peliId}")
     public String liitaGenreen(@PathVariable Integer peliId, Model model) {
         model.addAttribute("peli", peliService.findPeli(peliId));
         model.addAttribute("genres", genreService.listAll());
         return "liitaGenreen";
     }
 
     @RequestMapping(value = "arvostele/{peliId}")
     public String arvostele(@PathVariable Integer peliId, Model model) {
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "arvostele";
     }
 
     @RequestMapping(value = "lainaa/{peliId}")
     public String lainaa(@PathVariable Integer peliId, Model model) {
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "lainaa";
     }
 
     @RequestMapping(value = "lainassaHenkilolla")
     public String lainassa(Model model, @RequestParam String lainassa) {
         model.addAttribute("pelit", peliService.findByLainassa(lainassa));
         return "lainassa";
     }
 
     @RequestMapping(value = "lisaaPeliGenreen/{peliId}/{genreId}")
     public String liitaPeliGenreen(@Valid @PathVariable Integer peliId, @PathVariable Integer genreId) {
         Peli peli = peliService.findPeli(peliId);
         Genre genre = genreService.findGenre(genreId);
         peli.setGenre(genre);
         genre.addPeli(peli);
         peliService.add(peli);
         genreService.add(genre);
         return "redirect:/listaa";
     }
     @RequestMapping(value = "muokkaaPelia/{peliId}")
     public String muokkaaPelinNimea(@RequestParam String nimi, @PathVariable Integer peliId){
         Peli peli = peliService.findPeli(peliId);
         peli.setNimi(nimi);
         peliService.add(peli);  
         return "redirect:/listaa";
     }
     
     @RequestMapping(value = "muokkaa/{peliId}")
     public String muokkaaPelia(@PathVariable Integer peliId, Model model){
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "muokkaa";
     }
     
     @RequestMapping(value = "muokkaaGenrea/{genreId}")
     public String muokkaaGenrea(@PathVariable Integer genreId, Model model){
         model.addAttribute("genre", genreService.findGenre(genreId));
         return "muokkaaGenrea";
     }
     
     @RequestMapping(value = "muokkaaGenrenTietoja/{genreId}")
     public String muokkaaGenrenTietoja(@RequestParam String nimi, @PathVariable Integer genreId){
         Genre genre = genreService.findGenre(genreId);
         genre.setNimi(nimi);
         genreService.add(genre);
         return "redirect:/listaa";
     }
             
 
     @RequestMapping(value = "lisaaKommentti/{peliId}")
     public String lisaaKommentti(@Valid @ModelAttribute Kommentti kommentti, BindingResult result, @PathVariable Integer peliId, Model model) {
         if (result.hasErrors()) {
         model.addAttribute("peli", peliService.findPeli(peliId));
             return "kommentoi";
         }
         kommentti.setPeli(peliService.findPeli(peliId));
         kommenttiService.add(kommentti);
         Peli peli = peliService.findPeli(peliId);
         peli.getKommentit().add(kommentti);
         peliService.add(peli);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "lisaaArvostelu/{peliId}")
     public String lisaaArvostelu(@Valid @ModelAttribute Arvostelu arvostelu, BindingResult result, @PathVariable Integer peliId, Model model) {
         if (result.hasErrors()) {
         model.addAttribute("peli", peliService.findPeli(peliId));
             return "arvostele";
         }
         arvostelu.setPeli(peliService.findPeli(peliId));
         arvosteluService.add(arvostelu);
         Peli peli = peliService.findPeli(peliId);
         peli.getArvostelut().add(arvostelu);
         peliService.add(peli);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "lainaaPeli/{peliId}")
     public String lainaaPeli(@PathVariable Integer peliId, @RequestParam String lainassa) {
         Peli peli = peliService.findPeli(peliId);
         peli.setLainassa(lainassa);
         peliService.add(peli);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "/lisaa")
     public String lisaaKuuntelija(Model model) {
         model.addAttribute("peli", new Peli());
         model.addAttribute("genre", new Genre());
         return "lisaa";
     }
 
     @RequestMapping(value = "/home")
     public String home() {
         return "home";
 
     }
 
     @RequestMapping(value = "/error")
     public String error() {
         return "error";
     }
     
     @RequestMapping(value = "/authorityError")
     public String authError() {
         return "authorityError";
     }
 }
