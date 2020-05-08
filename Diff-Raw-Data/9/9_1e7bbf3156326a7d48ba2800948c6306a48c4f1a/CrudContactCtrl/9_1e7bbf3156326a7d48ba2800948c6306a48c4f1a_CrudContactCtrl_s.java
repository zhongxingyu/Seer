 package fr.esiea.esieaddress.controllers.crud;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import fr.esiea.esieaddress.dao.exception.DaoException;
 import fr.esiea.esieaddress.model.contact.Contact;
 import fr.esiea.esieaddress.model.contact.view.ContactView;
 import fr.esiea.esieaddress.service.crud.ICrudService;
 import fr.esiea.esieaddress.service.exception.ServiceException;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 @Controller
 @RequestMapping("/contacts")
 public class CrudContactCtrl {
 
 	private final static Logger LOGGER = Logger.getLogger(CrudContactCtrl.class);
 	@Autowired
 	@Qualifier("contactValidationDecorator")
 	ICrudService<Contact> crudService;
 
     @Secured("ROLE_USER")
     @RequestMapping(method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public void getAll(HttpServletResponse servletResponse) throws ServiceException, DaoException, IOException {
 
 		LOGGER.info("[Controller] Querying Contact list");
 		/*
 		 * Make the list with only id, firstname, lastname, actif
 		 */
 		ObjectMapper objectMapper = new ObjectMapper();
 		objectMapper.writerWithView(ContactView.LightView.class).writeValue(servletResponse.getOutputStream(), crudService.getAll());
 	}
 
     @Secured("ROLE_USER")
     @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
 	@ResponseBody
 	public Contact getById(@PathVariable("id") String contactId) throws ServiceException, DaoException {
 
 		LOGGER.info("[Controller] Querying Contact with id : \"" + contactId + "\"");
 		return crudService.getOne(contactId);
 	}
 
     @Secured("ROLE_USER")
     @RequestMapping(method = RequestMethod.POST, consumes = "application/json;charset=UTF-8")
 	@ResponseStatus(HttpStatus.CREATED)
 	public void create(@RequestBody Contact contact) throws ServiceException, DaoException {
 
 		LOGGER.info("[Controller] Querying to create new contact : " + contact.toString() + "\"");
 		crudService.insert(contact);
 	}
 
     @Secured("ROLE_USER")
     @RequestMapping(value = "", method = RequestMethod.PUT, consumes = "application/json")
	@ResponseStatus(HttpStatus.OK)
 	public void edit(@RequestBody Contact contact) throws ServiceException, DaoException {
 
 		LOGGER.info("[Controller] Querying to edit Contact : \"" + contact.toString() + "\"");
 		crudService.save(contact);
 	}
 
     @Secured("ROLE_USER")
     @RequestMapping(value = "/{idContact}/visibility/{visibility}", method = RequestMethod.PUT, consumes = "application/json")
 	@ResponseStatus(HttpStatus.OK)
 	public void edit(@PathVariable String idContact,@PathVariable Boolean visibility) throws ServiceException,
 			DaoException {
 		//Permit to update the visibility of a client without have all informations
 		LOGGER.info("[Controller] Querying set visibility " + visibility + " to contact : \"" + idContact + "\"");
 		Contact contact = crudService.getOne(idContact);
 		if (visibility != contact.isActif()) {
 			contact.setActif(visibility);
 			crudService.save(contact);
 		}
 	}
 
     @Secured("ROLE_USER")
     @RequestMapping(value = "/{idContact}", method = RequestMethod.DELETE)
 	@ResponseStatus(HttpStatus.OK)
 	public void delete(@PathVariable String idContact) throws ServiceException, DaoException {
 
 		LOGGER.info("[Controller] Querying to delete Contact with id : \"" + idContact + "\"");
 		crudService.remove(idContact);
 	}
 }
