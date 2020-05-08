 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.LineNumberReader;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class Spider {
 	// Regex constants
 	private static final Pattern HREF_REGEX = Pattern.compile(
 			"href=\"([^'\" <>]*)\"", Pattern.CASE_INSENSITIVE);
 	private static final Pattern HOST_REGEX = Pattern.compile(
 			"^http://(?:(.+?)/|(.+)$)", Pattern.CASE_INSENSITIVE);
 	private static final Pattern PATH_REGEX = Pattern.compile(
 			"^http://.+?(/.*)$", Pattern.CASE_INSENSITIVE);
 	private static final Pattern HEADER_REGEX = Pattern.compile(
 			"^HTTP/(?<version>[.0-9]+) (?<code>[0-9]+)|" +
 			"^content-type:\\s*(?<ctype>[a-z\\-/]+)",
 			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 
 	// Other constants
 	private static final List<Link> NO_LINKS = Collections.emptyList();
 	private static final List<InvalidLink> NO_INVALID_LINKS = Collections.emptyList();
 
 	protected final String baseAddress;
 	private final String baseHost;
 	protected final List<InvalidLink> invalids = Collections.synchronizedList(new ArrayList<InvalidLink>());
 	protected final Set<String> viewed = Collections.synchronizedSet(new HashSet<String>());
 
 	protected final SpiderWorkQueue workQueue;
 
 	// Construtor
 	public Spider(String baseAddress) {
 		if (baseAddress == null)
 			throw new NullPointerException();
 
 		baseAddress = baseAddress.trim();
 		if (!isValidArg(baseAddress))
 			throw new IllegalArgumentException("O argumento dever ser um " +
 					"endereço http válido, finalizado por /");
 
 		this.baseAddress = baseAddress;
 		this.baseHost = getHost(baseAddress);
 
 		// Obtém a quantidade de núcleos e define duas tarefas para cada núcleo
 		int availableCpus = Runtime.getRuntime().availableProcessors();
 		this.workQueue = new SpiderWorkQueue(availableCpus*2);
 	}
 
 	private boolean isValidArg(final String address) {
 		if (Pattern.matches("^http://[^'\" ]+/$", address))
 			return true;
 
 		return false;
 	}
 
 	private String getHost(String address) {
 		Matcher matcher = HOST_REGEX.matcher(address);
 
 		if (matcher.find()) {
 			String host = matcher.group(1);
 			if (host != null)
 				return host;
 			else
 				return matcher.group(2);
 		}
 
 		return null;
 	}
 
 	private String getAddressPath(String address) {
 		Matcher matcher = PATH_REGEX.matcher(address);
 
 		if (matcher.find())
 			return matcher.group(1);
 
 		return "/";
 	}
 
 	/**
 	 * Normaliza o link, removendo todos os "./" (diretório corrente) e
 	 * substituindo todos os "../" (diretório acima).
 	 * @param link String contendo o link a ser normalizado
 	 * @return String com o link normalizado
 	 */
 	private String normalizedLink(String link) throws NormalizationException {
 		String[] split = link.split("/+");
 		if (split.length == 0)
 			return link;
 
 		List<String> pieces = new LinkedList<>(Arrays.asList(split));
 		for (int i = 0; i < pieces.size(); ) {
 			if (pieces.get(i).equals("."))
 				pieces.remove(i);
 			else if (pieces.get(i).equals("..")) {
 				pieces.remove(i);
 				try {
 					pieces.remove(i-1);
 				} catch (IndexOutOfBoundsException e) {
 					throw new NormalizationException("Link mal formado");
 				}
 				i--;
 			} else
 				i++;
 		}
 
 		// Detectando links mal formados (sem host ou protocolo)
 		if (link.startsWith("http:")) {
 			if (!pieces.get(0).equals("http:") || !pieces.get(1).equals(getHost(link)))
 				throw new NormalizationException("Link mal formado");
 		}
 		// Detectando links mal formados (deixou de ser absoluto)
 		else if (link.startsWith("/") && !pieces.get(0).equals(""))
 			throw new NormalizationException("Link mal formado");
 
 		// Juntando os pedaços do link
 		StringBuffer path = new StringBuffer();
 		Iterator<String> it = pieces.iterator();
 
 		String piece = it.next();
 		path.append(piece);
 		if (piece.equals("http:"))
 			path.append('/');
 
 		while (it.hasNext()) {
 			piece = it.next();
 			path.append('/').append(piece);
 		}
 
 		if (link.endsWith("/"))
 			path.append('/');
 
 		return path.toString();
 	}
 
 	private String absoluteLink(String link) {
 		try {
 			// Link já é absoluto (somente http)
 			if (link.startsWith("http:"))
 				return normalizedLink(link);
 
 			// Outros protocolos não são suportados
 			if (link.contains(":"))
 				return null;
 
 			// Link relativo à raiz deve se tornar absoluto
 			if (link.startsWith("/"))
 				return "http://" + this.baseHost + normalizedLink(link);
 
 			// Links relativos
 			return normalizedLink(this.baseAddress + link);
 		} catch (NormalizationException e) {}
 
 		// Links mal formados
 		return null;
 	}
 
 	/**
 	 * Obtém todos os links em uma página HTML, passada como argumento através
 	 * de um {@link InputStream}.
 	 *
 	 * @param sock Página HTML
 	 * @return Lista de links encontrados
 	 * @throws IOException se ocorrer um erro de E/S
 	 */
 	private List<Link> findLinks(final SpiderSocket sock, String address) {
 		final List<Link> foundLinks = new ArrayList<>();
 
 		String line;
 		try {
 			LineNumberReader input = sock.getInput();
 			while ((line=input.readLine()) != null) {
 				final Matcher matcher = HREF_REGEX.matcher(line);
 				while (matcher.find()) {
 					String linkTo = absoluteLink(matcher.group(1));
 					if (linkTo != null)
 						foundLinks.add(new Link(address, linkTo, input.getLineNumber()));
 				}
 			}
 			sock.getRealSock().close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return foundLinks;
 	}
 
 	private SpiderSocket getSpiderSocket(String host) throws IOException {
 		return new SpiderSocket(new Socket(host, 80));
 	}
 
 	protected Header httpHead(String address) throws IOException {
 		// Conexão
 		String host = getHost(address);
 		address = getAddressPath(address);
 		SpiderSocket sock = getSpiderSocket(host);
 
 		// Requisição
 		String requisition = String.format("HEAD %s HTTP/1.1\r\n" +
 				"Host:%s\r\n" +
 				"Connection: close\r\n" +
 				"\r\n", address, host);
 		sock.getOutput().write(requisition.getBytes());
 
 		Header header = readHeaderHttp(sock);
 		sock.getRealSock().close();
 
 		return header;
 	}
 
 	protected Page httpGet(String address) throws IOException {
 		// Conexão
		String addressPath = getAddressPath(address);
 		SpiderSocket sock = getSpiderSocket(this.baseHost);
 
 		// Requisição
 		String requisition = String.format("GET %s HTTP/1.1\r\n" +
 				"Host:%s\r\n" +
 				"Connection: close\r\n" +
				"\r\n", addressPath, this.baseHost);
 		sock.getOutput().write(requisition.getBytes());
 
 		Header header = readHeaderHttp(sock);
 
 		// Se o código de status não é 200 ou o content-type não é HTML, despreza a conexão
 		if (header.getStatusCode() != 200 || !header.getContentType().equals("text/html")) {
 			sock.getRealSock().close();
 			return new Page(header, NO_LINKS);
 		}
 
 		// Obtém a lista de links desse endereço
 		List<Link> links = findLinks(sock, address);
 
 		return new Page(header, links);
 	}
 
 	private Header readHeaderHttp(SpiderSocket sock) throws IOException {
 		BufferedReader sockInput = sock.getInput();
 		final StringBuilder sbHeader = new StringBuilder(500);
 
 		String line;
 		while ((line=sockInput.readLine()) != null && line.length() > 0)
 			sbHeader.append(line).append(System.lineSeparator());
 
 		Map<String, Object> headerFields = new Hashtable<>();
 
 		// Obtém cabeçalho
 		Matcher matcher = HEADER_REGEX.matcher(sbHeader);
 		String matched;
 		while (matcher.find()) {
 			if ((matched=matcher.group(Header.HTTP_VERSION)) != null)
 				headerFields.put(Header.HTTP_VERSION, matched);
 			if ((matched=matcher.group(Header.STATUS_CODE)) != null)
 				headerFields.put(Header.STATUS_CODE, Integer.parseInt(matched));
 			if ((matched=matcher.group(Header.CONTENT_TYPE)) != null)
 				headerFields.put(Header.CONTENT_TYPE, matched);
 		}
 
 		return new Header(headerFields);
 	}
 
 	protected List<InvalidLink> invalidLinks(Link link) {
 		Thread threadGet = new SpiderThreadGet(this, link);
 		try {
 			this.workQueue.submit(threadGet);
 			this.workQueue.awaitEnd();
 		} catch (InterruptedException e) {
 			return NO_INVALID_LINKS;
 		}
 
 		return this.invalids;
 	}
 
 	public List<InvalidLink> invalidLinks() {
 		this.viewed.add(this.baseAddress);
 		return invalidLinks(new Link("sitebase", this.baseAddress, 0));
 	}
 
 	public static void main(String[] args) throws IOException {
 		long startTime = System.currentTimeMillis();
 
 		String url;
 		if (args.length < 1) {
 			System.out.print("Digite um endereço: ");
 			Scanner scanner = new Scanner(System.in);
 			url = scanner.nextLine();
 		} else {
 			url = args[0];
 		}
 
 		Spider spider = null;
 		try {
 			spider = new Spider(url);
 		} catch (IllegalArgumentException e) {
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 
 		for (InvalidLink invalid : spider.invalidLinks()) {
 			Link link = invalid.getLink();
 			System.out.println(String.format("%s %03d %s %d",
 					link.getLinkTo(), invalid.getStatusCode(), link.getPageUrl(), link.getLine()));
 		}
 
 		System.out.println("TIME " + (System.currentTimeMillis() - startTime));
 	}
 
 }
 
 class SpiderThreadGet extends Thread {
 	private Spider spider;
 	private Link link;
 	private Page page;
 
 	public SpiderThreadGet(Spider spider, Link link) {
 		this.spider = spider;
 		this.link = link;
 	}
 
 	@Override
 	public void run() {
 		doGet();
 	}
 
 	private void doGet() {
 		try {
 			page = spider.httpGet(link.getLinkTo());
 
 			// Se retorna algo diferente de 200, nem mesmo verifica o content-type
 			if (page.getStatusCode() != 200) {
 				synchronized (spider.invalids) {
 					spider.invalids.add(new InvalidLink(link, page.getStatusCode()));
 				}
 			}
 		} catch (IOException e) {
 			// Erro de DNS
 			synchronized (spider.invalids) {
 				spider.invalids.add(new InvalidLink(link, 0));
 			}
 		}
 	}
 
 	public void dispatchLinks() {
 		for (final Link found : page.getLinks()) {
 			final String linkTo = found.getLinkTo();
 			synchronized (spider.viewed) {
 				if (spider.viewed.contains(linkTo))
 					continue;
 				else
 					spider.viewed.add(linkTo);
 			}
 
 			if (linkTo.startsWith(spider.baseAddress)) {
 				Thread threadGet = new SpiderThreadGet(spider, found);
 				spider.workQueue.submit(threadGet);
 			} else {
 				Thread threadHead = new SpiderThreadHead(spider, found);
 				spider.workQueue.submit(threadHead);
 			}
 		}
 	}
 }
 
 class SpiderThreadHead extends Thread {
 	private Spider spider;
 	private Link found;
 
 	public SpiderThreadHead(Spider spider, Link found) {
 		this.spider = spider;
 		this.found = found;
 	}
 
 	@Override
 	public void run() {
 		doHead();
 	}
 
 	private void doHead() {
 		try {
 			int code = spider.httpHead(found.getLinkTo()).getStatusCode();
 			if (code != 200) {
 				synchronized (spider.invalids) {
 					spider.invalids.add(new InvalidLink(found, code));
 				}
 			}
 		} catch (IOException e) {
 			// Erro de rede (DNS, etc)
 			synchronized (spider.invalids) {
 				spider.invalids.add(new InvalidLink(found, 0));
 			}
 		}
 
 	}
 }
 
 class SpiderWorkQueue {
 	private ReentrantLock access;
 	private BlockingQueue<Thread> queue;
 
 	public SpiderWorkQueue(int capacity) {
 		queue = new ArrayBlockingQueue<>(capacity, true);
 		access = new ReentrantLock(true);
 	}
 
 	public void awaitEnd() throws InterruptedException {
 		while (!queue.isEmpty()) {
 			Iterator<Thread> it = queue.iterator();
 			while (it.hasNext()) {
 				Thread thread = it.next();
 				if (!thread.isAlive()) {
 					try {
 						it.remove();
 						if (thread instanceof SpiderThreadGet)
 							((SpiderThreadGet) thread).dispatchLinks();
 					} catch (IllegalStateException e) {
 						// já foi removido pela ação de adicionar
 					}
 				}
 			}
 			Thread.sleep(50);
 		}
 	}
 
 	public boolean submit(Thread elem) {
 		access.lock();
 
 		// Se não pôde adicionar, então a fila está cheia. Assim, removemos
 		// antes pelo menos uma thread finalizada.
 		if (!queue.offer(elem)) {
 			Iterator<Thread> it = queue.iterator();
 			int removed = 0;
 			while (true) {
 				if (!it.hasNext()) {
 					if (removed > 0)
 						break;
 					it = queue.iterator();
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {
 					}
 				}
 
 				Thread thread = it.next();
 				if (!thread.isAlive()) {
 					try {
 						removed++;
 						it.remove();
 						if (thread instanceof SpiderThreadGet)
 							((SpiderThreadGet) thread).dispatchLinks();
 					} catch (IllegalStateException e) {
 						// já foi removido pela ação de esperar
 					}
 				}
 			}
 		}
 
 		// Adicionamos à fila e executamos
 		queue.offer(elem);
 		elem.start();
 
 		access.unlock();
 
 		return true;
 	}
 }
