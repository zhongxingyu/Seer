 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import ar.edu.itba.paw.grupo1.controller.exception.InvalidParameterException;
 import ar.edu.itba.paw.grupo1.model.Picture;
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.model.User;
 import ar.edu.itba.paw.grupo1.service.PictureService;
 import ar.edu.itba.paw.grupo1.service.PropertyService;
 
 @Controller
 @RequestMapping(value="picture")
 public class PictureController extends AbstractPictureController {
 
 	protected PictureService pictureService;
 	
 	@Autowired
 	public PictureController(PropertyService propertyService, PictureService pictureService) {
 		super(propertyService);
 		this.pictureService = pictureService;
 	}
 	
 	@RequestMapping(value="add", method = RequestMethod.GET)
 	protected ModelAndView addGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		ModelAndView mav = new ModelAndView();
 		User user = getLoggedInUser(req);		
 		int propId = -1;
 		
 		try {
 			propId = Integer.parseInt(req.getParameter("propId"));
 		} catch (NumberFormatException e) {
 			throw new InvalidParameterException();
 		}
 		
 		Property property = propertyService.getById(Integer.parseInt(req.getParameter("propId")));
 		if (property.getUser().getId() == user.getId()) {
 			Picture picture = new Picture();
 			
 			picture.setProperty(property);
 			req.setAttribute("picture", picture);
 		} else {
 			req.setAttribute("noPermissions", 1);
 		}
 		
 		return render(req, resp, "editPicture.jsp", "Add Picture", mav);	
 	}
 
 	@RequestMapping(value="add", method = RequestMethod.POST)
 	protected ModelAndView addPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		ModelAndView mav = new ModelAndView();
 
 		// Create a factory for disk-based file items
 		FileItemFactory factory = new DiskFileItemFactory();
 
 		// Create a new file upload handler
 		ServletFileUpload upload = new ServletFileUpload(factory);
 
 		List<FileItem> items = null;
 		// Parse the request
 		try {
 			items = (List<FileItem>) upload.parseRequest(req);
 		} catch (FileUploadException e) {
 			req.setAttribute("fatal", 1);
 			return render(req, resp, "editPicture.jsp", "Add Picture", mav);
 		}
 		
 		Picture picture = new Picture();		
 		
 		FileItem file = null;
 		
 		Iterator<FileItem> iter = items.iterator();
 		while (iter.hasNext()) {
 		    FileItem item = (FileItem) iter.next();
 		    if ("name".equals(item.getFieldName())) {
 		    	picture.setName(item.getString());
 		    } else if ("propId".equals(item.getFieldName())) {
 		    	Property property = propertyService.getById(Integer.parseInt(item.getString()));
 		    	picture.setProperty(property);
 		    } else if ("file".equals(item.getFieldName())) {
 		    	file = item;
 		    }
 		}		
 		
 		boolean error = false;
 		
 		if (picture.getName().equals("") || picture.getName().length() > 50) {
 			error = true;
 			req.setAttribute("nameError", 1);
 		}
 		
 		String extension = null;
 		
 		if (file.getName().equals("")) {
 			error = true;
 			req.setAttribute("fileError", 1);
 		} else if (!file.getName().contains(".")) {
 			req.setAttribute("extensionError", 1);
 			error = true;
 		} else {
 			extension = file.getName().substring(file.getName().lastIndexOf('.'));
 			if (!extension.equals(".jpg") && !extension.equals(".png") && !extension.equals(".jpeg") && !extension.equals(".gif")) {
 				req.setAttribute("extensionError", 1);
 				error = true;
 			}
 		}
 		
 		if (error) {
 			req.setAttribute("picture", picture);
 			return render(req, resp, "editPicture.jsp", "Add Picture", mav);
 		}
 		
 		if (picture.getProperty().getUser().getId() != getLoggedInUser(req).getId()) {
 			req.setAttribute("noPermissions", 1);
 			return render(req, resp, "editPicture.jsp", "Edit Picture", mav);
 		}
 		
 		picture.setExtension(extension);
 		
 		pictureService.save(picture);
 		
 		try {
 			file.write(new File(getServletContext().getRealPath("/images") + "/" + picture.getId() + picture.getExtension()));
 		} catch (Exception e) {
 			req.setAttribute("picture", picture);
 			req.setAttribute("writeError", 1);
 			render(req, resp, "editPicture.jsp", "Add Picture", mav);
 		}
		RedirectView view = new RedirectView("/property/edit?id=" + picture.getProperty(),true);
 		return new ModelAndView(view);
 
 	}
 	
 	@RequestMapping(value="edit", method = RequestMethod.GET)
 	protected ModelAndView editGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		ModelAndView mav = new ModelAndView();
 		Picture picture = null;
 		if (req.getParameter("id") != null) {
 			User user = getLoggedInUser(req);
 			try {
 				picture = pictureService.getById(Integer.parseInt(req.getParameter("id")));
 			} catch (NumberFormatException e) {
 				throw new InvalidParameterException();
 			}
 			
 			
 			if (picture != null && picture.getProperty().getUser().getId() == user.getId()) {
 				req.setAttribute("edit", 1);
 				req.setAttribute("picture", picture);
 			} else {
 				req.setAttribute("noPermissions", 1);
 			}
 		} else {
 			throw new InvalidParameterException();
 		}
 		return render(req, resp, "editPicture.jsp", "Edit Picture", mav);
 	}
 
 	@RequestMapping(value="edit", method = RequestMethod.POST)
 	protected ModelAndView editPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		ModelAndView mav = new ModelAndView();
 		Picture picture = null;
 		
 		try {
 			picture = pictureService.getById(Integer.parseInt(req.getParameter("id")));
 		} catch (NumberFormatException e) {
 			throw new InvalidParameterException();
 		}
 				
 		if (picture == null || picture.getProperty().getUser().getId() != getLoggedInUser(req).getId()) {
 			req.setAttribute("noPermissions", 1);
 			return render(req, resp, "editPicture.jsp", "Edit Picture", mav);
 		}
 		
 		if (req.getParameter("submit") != null) {
 			picture.setName(req.getParameter("name"));
 			if (picture.getName().equals("") || picture.getName().length() > 50) {
 				req.setAttribute("edit", 1);
 				req.setAttribute("picture", picture);
 				req.setAttribute("nameError", 1);
 				return render(req, resp, "editPicture.jsp", "Edit Picture", mav);
 			} 
 			pictureService.save(picture);
 		}
 		
 		
 		if (req.getParameter("delete") != null) {
 			pictureService.delete(picture);
 			File file = new File(getServletContext().getRealPath("/images/") + "/" + picture.getId() + picture.getExtension());
 			if(!file.delete()) {
 				req.setAttribute("edit", 1);
 				req.setAttribute("picture", picture);
 				req.setAttribute("deleteError", 1);
 				return render(req, resp, "editPicture.jsp", "Edit Picture", mav);
 			}
 		}
		RedirectView view = new RedirectView("/property/edit?id=" + picture.getProperty(), true);
 		return new ModelAndView(view);
 
 	}
 	
 }
