 package it.uninsubria.invoice.web;
 
 
 import it.uninsubria.generic.web.HttpMethod;
 import it.uninsubria.generic.web.Page;
 import it.uninsubria.generic.web.WebRequest;
 import it.uninsubria.generic.web.WebResponse;
 import it.uninsubria.invoice.domain.InvoiceRepository;
 import it.uninsubria.invoice.domain.LineItem;
 import it.uninsubria.invoice.domain.Money;
 
 public class InvoicePage extends Page {
 
 	private final InvoiceRepository repository;
 
 	public InvoicePage(InvoiceRepository repository) {
 		this.repository = repository;
 	}
 
 	@Override
 	public void handle(WebRequest request, WebResponse response) {
 		if (HttpMethod.POST == request.getMethod()) {
 			addLineItem(request, response);
 			returnAllLineItems(response);
 		} else {
 			returnAllLineItems(response);
 		}
 	}
 
 	private void returnAllLineItems(WebResponse response) {
		String body = "";
 		for (LineItem item : repository.all()) {
			if (!body.isEmpty()) {
				body += ",";
			}
 			body += "  " + item.toJson() + "\n";
 		}
		body = "[\n" + body + "\n]";
 		response.setContentType("application/json");
 		response.setStatus(200);
		response.setBody(body);
 	}
 
 	private void addLineItem(WebRequest request, WebResponse response) {
 		String description = request.getParameter("description");
 		Money amount = new Money(Integer.parseInt(request.getParameter("amountInCents")));
 		LineItem item = new LineItem(description, amount );
 		repository.addLineItem(item);		
 		response.setStatus(201);
 	}
 }
