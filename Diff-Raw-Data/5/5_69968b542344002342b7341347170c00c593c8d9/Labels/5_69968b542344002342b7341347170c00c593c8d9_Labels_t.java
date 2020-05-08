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
 package br.com.caelum.integracao.server.label;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import br.com.caelum.integracao.server.dao.Database;
 import br.com.caelum.vraptor.ioc.RequestScoped;
 
 @SuppressWarnings("unchecked")
 @RequestScoped
 public class Labels {
 	
 	private final Session session;
 
 	public Labels(Database db) {
 		this.session = db.getSession();
 	}
 
 	public List<Label> getTags() {
		return session.createQuery("from Label").list();
 	}
 
 	public Label getLabel(String name) {
		Query query = session.createQuery("from Label as t where t.name = :name");
 		query.setParameter("name", name);
 		List<Label> results = query.list();
 		if(results.isEmpty()) {
 			Label tag = new Label(name);
 			session.save(tag);
 			return tag;
 		}
 		return results.get(0);
 	}
 
 	public List<Label> lookup(String tags) {
 		List<Label> list = new ArrayList<Label>();
 		String[] tagsFound = tags.split("\\s*,\\s*");
 		for(String tag : tagsFound) {
 			if(!tag.equals("")) {
 				list.add(getLabel(tag));
 			}
 		}
 		return list;
 	}
 
 
 }
