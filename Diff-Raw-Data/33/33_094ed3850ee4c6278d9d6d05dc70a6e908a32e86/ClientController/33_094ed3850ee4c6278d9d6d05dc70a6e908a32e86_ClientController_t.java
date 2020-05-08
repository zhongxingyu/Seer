 /***
  * 
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. Neither the name of the
  * copyright holders nor the names of its contributors may be used to endorse or
  * promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package br.com.caelum.integracao.server.logic;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.http.DefaultHttp;
 import br.com.caelum.integracao.server.Client;
 import br.com.caelum.integracao.server.agent.Clients;
 import br.com.caelum.integracao.server.agent.DefaultAgent;
 import br.com.caelum.integracao.server.label.Labels;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Put;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.view.Results;
 
 @Resource
 public class ClientController {
 	
 	
 	private final Logger logger = LoggerFactory.getLogger(ClientController.class);
 	
 	private final Clients clients;
 	private final Result result;
 
 	private final Labels labels;
 
 	public ClientController(Clients db, Result result, Labels labels) {
 		this.clients = db;
 		this.result = result;
 		this.labels = labels;
 	}
 	
 	public void add(Client client) {
 		this.clients.register(client);
 		showList();
 	}
 
 	private void showList() {
 		result.use(Results.logic()).redirectTo(ClientController.class).list();
 	}
 	
 	public void list() {
 		result.include("free", clients.freeClients());
 		result.include("locked", clients.lockedClients());
 		result.include("inactive", clients.inactiveClients());
 	}
 
 	@Post
 	@Path("/client/{client.id}/deactivate")
 	public void deactivate(Client client) {
 		clients.get(client).deactivate();
 		showList();
 	}
 	
 	@Post
 	@Path("/client/{client.id}/activate")
 	public void activate(Client client) {
 		clients.get(client).activate();
 		showList();
 	}
 	
 	@Get
 	public void form() {
 		logger.debug("Client form");
 	}
 	
 	@Get
 	@Path("/client/show/{client.id}")
 	public void show(Client client) {
 		result.include("client", clients.get(client));
 		result.include("tags", labels.getTags());
 	}
 	
 	@Put
 	@Path("/client/{client.id}")
 	public void update(Client client, String labels) {
 		logger.debug("Updating " + client.getId() + " with " + labels);
 		Client toUpdate = clients.get(client);
 		toUpdate.tag(this.labels.lookup(labels));
 		showList();
 	}
 	
 	@Post
 	@Path("/client/{client.id}/stop")
 	public void stop(Client client) {
 		logger.debug("Stopping " + client.getId());
 		client = clients.get(client);
		if(client.getCurrentJob()!=null) {
			client.stop(new DefaultAgent(client.getBaseUri(), new DefaultHttp()));
		}
 		showList();
 	}
 
 }
