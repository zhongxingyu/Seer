 package order.layer;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 import order.Message;
 import order.Message.Type;
 
 import panchat.data.User;
 
 public class TotalOrderLayer extends OrderLayer {
 
 	/*
 	 * Cuando hacemos una simulación queremos que el escenarios sea
 	 * determinista, como existe la posibilidad que se dé una situación de
 	 * empate con 2 mensajes de igual prioridad (2 procesos inician a la vez y
 	 * realizan los mismos pasos, resultado 2 mensajes totales con misma
 	 * prioridad), en un escenario real desenpataríamos de mediante el
 	 * identificador aleatorio del mensaje.
 	 * 
 	 * Para hacerlo determinista en nuestra simulación, númeraremos de manera
 	 * total mediante un contador estático.
 	 */
 	private final boolean simulating;
 
 	private static int msgCount = 0;
 
 	private String simulationDelivery = "";
 
 	/*
 	 * Tramas para cada una de las fases de la ordenación total.
 	 */
 	public class TotalSendMsg implements Message.Fifo, Message.Total {
 		int clock;
 		TotalUndeliverable content;
 	}
 
 	public class TotalProposalMsg implements Message.Fifo, Message.Total {
 		int msgReference;
 		int clock;
 	}
 
 	public class TotalFinalMsg implements Message.Fifo, Message.Total {
 		int msgReference;
 		int clock;
 	}
 
 	/*
 	 * Atributos
 	 */
 
 	// Reloj de lamport
 	private int clock = 0;
 
 	// Cola de prioridad que almacena los mensajes "undeliverable"s
 	private List<TotalUndeliverable> priorityQueue = new LinkedList<TotalUndeliverable>();
 
 	// Tabla hash para acceder de manera constante a los mensajes por su
 	// identificador
 	private HashMap<Integer, TotalUndeliverable> hash = new HashMap<Integer, TotalUndeliverable>();
 
 	public TotalOrderLayer(User user) {
 		super(user);
 		this.simulating = false;
 	}
 
 	public TotalOrderLayer(User user, boolean simulating) {
 		super(user);
 		msgCount = 0;
 		this.simulating = simulating;
 	}
 
 	/*
 	 * 1º Etapa, envio del mensaje Total
 	 */
 	private void sendMsg(Message msg) {
 		// Incrementamos el reloj, y añadimos el reloj al mensaje
 		this.clock++;
 
 		// Parametros que necesitamos guardar en el mensaje "undeliverable"
 		// - El identificador
 		// - La prioridad inicial
 		// - La lista de usuarios (para saber cuando hemos recibido todas las
 		// respuestas
 		// - El mensaje
 
 		// La referencia será aleatoria o determinista dependiendo de si estamos
 		// realizando una simulación o no.
 		int msgRef = simulating ? msgCount++ : UUID.randomUUID().hashCode();
 		int priority = clock;
 		List<User> list = new LinkedList<User>(this.userList);
 
 		TotalUndeliverable undeliverable = new TotalUndeliverable(msgRef,
 				priority, msg, list);
 
 		// Añadimos el mensaje a la cola
 		this.priorityQueue.add(undeliverable);
 		this.hash.put(msgRef, undeliverable);
 
 		// Enviamos una trama al resto de clientes
 		TotalSendMsg send = new TotalSendMsg();
 		send.content = undeliverable.clone();
 		send.clock = clock;
 
 		super.sendMsg(this.userList, new Message(send, this.user));
 	}
 
 	@Override
 	public synchronized void sendMsg(List<User> users, Message msg) {
 		sendMsg(msg);
 	}
 
 	@Override
 	public synchronized void sendMsg(User user, Message msg) {
 		sendMsg(msg);
 	}
 
 	/*
 	 * 2º y 3º Etapa
 	 */
 	@Override
 	protected receiveStatus okayToRecv(Message msg) {
 
 		// Cada vez que recibimos un mensaje, aumentamos el reloj de lamport.
 		this.clock++;
 
 		/*
 		 * - On receiving a message, a process marks it as undeliverable and
 		 * sends the value of the logical clock as the proposed timestamp to the
 		 * initiator.
 		 */
 		if (msg.getContent() instanceof TotalSendMsg) {
 
 			TotalSendMsg totalMsg = (TotalSendMsg) msg.getContent();
 			TotalUndeliverable undeliverable = totalMsg.content.clone();
 
 			this.clock = Math.max(this.clock, undeliverable.priority);
 			undeliverable.priority = this.clock;
 
 			this.priorityQueue.add(undeliverable);
 			this.hash.put(undeliverable.msgReference, undeliverable);
 
 			TotalProposalMsg proposal = new TotalProposalMsg();
 			proposal.clock = this.clock;
 			proposal.msgReference = undeliverable.msgReference;
 
 			super.sendMsg(msg.getUsuario(), new Message(proposal, this.user));
 		}
 		/*
 		 * - When the initiator has received all the proposed timestamps, it
 		 * takes the maximum of all proposals and assigns that timestamp as the
 		 * final timestamp to that message. This value is sent to all the
 		 * destinations.
 		 */
 		else if (msg.getContent() instanceof TotalProposalMsg) {
 
 			TotalProposalMsg totalMsg = (TotalProposalMsg) msg.getContent();
 			TotalUndeliverable undeliverable = this.hash
 					.get(totalMsg.msgReference);

			this.clock = Math.max(this.clock, undeliverable.priority);
			undeliverable.priority = this.clock;

 			if (undeliverable != null) {
 
 				// Actualizamos la prioridad del mensaje
 				undeliverable.addPriority(msg.getUsuario(), totalMsg.clock);
 
 				if (undeliverable.deliverable) {
 
 					TotalFinalMsg proposal = new TotalFinalMsg();
 					proposal.clock = this.clock; // undeliverable.priority;
 					proposal.msgReference = undeliverable.msgReference;
 
 					super.sendMsg(undeliverable.userList, new Message(proposal,
 							this.user));
 				}
 
 			} else {
 				System.out.println("Propuesta de mensaje total incorrecta");
 			}
 		}
 		/*
 		 * - On receiving the final timestamp of a message, it is marked as
 		 * deliverable.
 		 */
 		else if (msg.getContent() instanceof TotalFinalMsg) {
 
 			TotalFinalMsg totalMsg = (TotalFinalMsg) msg.getContent();
 			TotalUndeliverable undeliverable = this.hash
 					.get(totalMsg.msgReference);
 
 			if (undeliverable != null) {
 
 				// Actualizamos la prioridad del mensaje
 				undeliverable.setDeliverable(totalMsg.clock);
 
 			} else {
 				System.out.println("Mensaje total incorrecto");
 			}
 
 		} else {
 			System.out.println("error");
 		}
 
 		Collections.sort(priorityQueue);
 
 		/*
 		 * - A deliverable message is delivered to the site if it has the
 		 * smallest timestamp in the message queue.
 		 */
 		while (priorityQueue.size() > 0 && priorityQueue.get(0).deliverable) {
 			TotalUndeliverable under = priorityQueue.remove(0);
 
 			if (simulating)
 				simulationDelivery += " " + under.msgReference;
 
 			this.deliveryQueue.add(under.content);
 		}
 		if (this.deliveryQueue.size() > 0)
 			return receiveStatus.Delivery;
 
 		return receiveStatus.Delete;
 	}
 
 	@Override
 	public Type orderCapability() {
 		return Message.Type.TOTAL;
 	}
 
 	public String getSimulationDelivery() {
 		return this.simulationDelivery;
 	}
 }
