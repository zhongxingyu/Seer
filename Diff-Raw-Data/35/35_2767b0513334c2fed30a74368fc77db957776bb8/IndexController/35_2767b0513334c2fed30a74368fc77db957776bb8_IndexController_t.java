 /***
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package br.com.caelum.vraptor.blank;
 
 import java.util.List;
 
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.dao.CategoriaDao;
 import br.com.caelum.vraptor.dao.PlaylistDao;
 import br.com.caelum.vraptor.dao.UsuarioWeb;
 import br.com.caelum.vraptor.models.Categoria;
 import br.com.caelum.vraptor.models.Playlist;
 import br.com.caelum.vraptor.models.Usuario;
 
 @Resource
 public class IndexController {
 
 	private final Result result;
 	
 	private final UsuarioWeb usuarioWeb;
 
 	public IndexController(Result result, UsuarioWeb usuarioWeb) {
 		this.result = result;
 		this.usuarioWeb = usuarioWeb;
 	}
 
 	@Path("/")
	public void index() {

 		if (! this.usuarioWeb.isLogado()) {
 			result.redirectTo(IndexController.class).login();
		} else {
			PlaylistDao dao = new PlaylistDao();
			CategoriaDao cd = new CategoriaDao();
 
			System.out.println(dao.listaTudo());

			// Form de novo usuario
			result.include("categorias", cd.listaPorUsuario(this.usuarioWeb.getId()));
			result.include("playlistList", dao.listaPorUsuario(this.usuarioWeb.getId()));
			//return dao.listaPorUsuario(this.usuarioWeb.getId());
		}
 	}
 	
 	@Path("/login")
 	public void login() {
 		
 	}
 	
 
 	
 }
    
 
 
