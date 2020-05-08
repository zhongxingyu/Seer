 /**
  * Copyright (c) 2012, Institute of Telematics, Institute of Information Systems (Dennis Pfisterer, Sven Groppe, Andreas Haller, Thomas Kiencke, Sebastian Walther, Mario David), University of Luebeck
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * 	  disclaimer.
  * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
  * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
  * 	  products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package p2p.distribution;
 
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.SettableFuture;
 import lupos.datastructures.items.Item;
 import lupos.datastructures.items.Triple;
 import lupos.datastructures.items.literal.Literal;
 import lupos.engine.operators.tripleoperator.TriplePattern;
 import net.tomp2p.futures.BaseFutureAdapter;
 import net.tomp2p.futures.FutureDHT;
 import net.tomp2p.p2p.Peer;
 import net.tomp2p.p2p.RequestP2PConfiguration;
 import net.tomp2p.peers.Number160;
 import net.tomp2p.rpc.SenderCacheStrategy;
 import net.tomp2p.storage.Data;
 import p2p.P2PAdapter;
 import p2p.distribution.strategies.SevenKeyDistribution;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 /**
  * The Class AbstractDistributionStrategy.
  */
 public abstract class AbstractDistributionStrategy implements
 		DistributionStrategy {
 
 	private Collection<Triple> result;
 	private String key;
 	private Boolean isReady;
 	/** The peer. */
 	protected Peer peer;
 
 	public static int distributionCounter = 0;
 	private static int storedCounter = 0;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * P2P.TOM.TRIPLE.DISTRIBUTION.DistributionStrategy#setPeer(net.tomp2p.p2p
 	 * .Peer)
 	 */
 	public void setPeer(Peer peer) {
 		this.peer = peer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * P2P.TOM.TRIPLE.DISTRIBUTION.DistributionStrategy#distribute(P2P.TOM.RDFTriple
 	 * )
 	 */
 	public abstract void distribute(Triple triple) throws IOException;
 
 	public abstract void remove(Triple triple) throws IOException;
 
 	public abstract boolean contains(Triple triple) throws IOException;
 
 	/**
 	 * Adds a given key value pair to network.
 	 * 
 	 * @param key
 	 *            the key
 	 * @param value
 	 *            the value
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	static SenderCacheStrategy senderCacheStrategy = new SenderCacheStrategy(
 			250, 750);
 	static RequestP2PConfiguration r = new RequestP2PConfiguration(1, 0, 0,
 			false, false, senderCacheStrategy);
 
 	protected void addToNetwork(String key, Triple value) throws IOException {
 		Number160 hash = Number160.createHash(key);
 		Number160 contentKey = Number160.createHash(value.toN3String());
 		distributionCounter++;
 		peer.put(hash).setData(contentKey, new Data(value))
 				.setRequestP2PConfiguration(r).start()
 				.addListener(new BaseFutureAdapter<FutureDHT>() {
 					public void operationComplete(FutureDHT future)
 							throws Exception {
 						storedCounter++;
 						// future.shutdown();
 					}
 				});
 	}
 
 	public boolean isDistributionReady() {
 		if (distributionCounter == storedCounter) {
 			distributionCounter = 0;
 			storedCounter = 0;
 		}
 		return distributionCounter <= (storedCounter * 1.001);
 	}
 
 	/**
 	 * Adds a given key value pair to network.
 	 * 
 	 * @param key
 	 *            the key
 	 * @param value
 	 *            the value
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	protected void removeFromNetwork(String key, Triple triple)
 			throws IOException {
 		Number160 hash = Number160.createHash(key);
 		Number160 contentKey = Number160.createHash(triple.toN3String());
 
 		peer.remove(hash).setContentKey(contentKey).start()
 				.awaitUninterruptibly();
 	}
 
 	/**
 	 * Check if a key is in the Network.
 	 * 
 	 * @param key
 	 *            the key
 	 * @param value
 	 *            the value
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	protected boolean isInNetwork(String key, Triple triple) throws IOException {
 
 		Number160 contentKey = Number160.createHash(triple.toN3String());
 
 		FutureDHT future =
 
 		peer.get(Number160.createHash(key)).setContentKey(contentKey).start();
 
 		future.awaitUninterruptibly();
 
 		return future.isSuccess();
 
 	}
 
 	public Collection<Triple> get(List<Literal> literale) {
 		result = new LinkedList<Triple>();
 		isReady = false;
 		String key = "";
 
 		// Key erzeugen:
 		/*
 		 * Wenn die Anzahl der Literale größer ist als die Anzahl der Literale
 		 * nach denen man gehasht hat muss man den Key verändern.
 		 */
 		if (literale.size() > P2PAdapter.DISTRIBUTION_STRATEGY) {
 			List<Literal> keyList = new ArrayList<Literal>();
 			for (int i = 0; i < P2PAdapter.DISTRIBUTION_STRATEGY; i++) {
 				keyList.add(literale.get(i));
 			}
 			key = generateKey(keyList);
 		}
 		/*
 		 * Wenn die Anzahl der Literale und die verwendete Hashstrategie gleich
 		 * ist kann man jedes Literal hashen. Bei der 7 Key Strategie geht jede
 		 * Variante.
 		 */
 		else if (P2PAdapter.DISTRIBUTION_STRATEGY == SevenKeyDistribution.STRATEGY_ID
 				|| literale.size() == P2PAdapter.DISTRIBUTION_STRATEGY) {
 			key = generateKey(literale);
 		}
 
 		// perform p2p operation
 		FutureDHT future = peer.get(Number160.createHash(key)).setAll().start();
 		future.addListener(new BaseFutureAdapter<FutureDHT>() {
 			public void operationComplete(FutureDHT future) throws Exception {
 				if (future.isSuccess()) {
 					// add all p2p results to the result collection
 					for (Data r : future.getDataMap().values()) {
 						try {
 							result.add((Triple) r.getObject());
 
 						} catch (ClassNotFoundException e) {
 							e.printStackTrace();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 
 					isReady = true;
 
 				} else {
 					isReady = true;
 				}
 			}
 		});
 
 		while (!isReady) {
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Generate key.
 	 * 
 	 * @param items
 	 *            the items
 	 * @return the string
 	 */
 	private String generateKey(List<Literal> items) {
 		StringBuilder key = new StringBuilder();
 		for (Literal literal : items)
 			key.append(literal.toString());
 		return key.toString();
 	}
 
 	protected ListenableFuture<Iterable<Triple>> getTriplesFromP2P(final String key) {
 
 		final SettableFuture<Iterable<Triple>> future = SettableFuture.create();
 
 		peer.get(Number160.createHash(key)).setAll().start().addListener(new BaseFutureAdapter<FutureDHT>() {
 			public void operationComplete(FutureDHT futureDHT) throws Exception {
 
 				if (futureDHT.isSuccess()) {
 
 					final List<Triple> result = newArrayList();
 
 					// add all p2p results to the result collection
 					for (Data r : futureDHT.getDataMap().values()) {
 						try {
 							result.add((Triple) r.getObject());
 						} catch (Exception e) {
 							future.setException(e);
 						}
 					}
 
 				} else {
					future.setException(new RuntimeException(futureDHT.getFailedReason()));
 				}
 			}
 		}
 		);
 
 		return future;
 	}
 
 	protected boolean containsAtLeastOneLiteral(final TriplePattern pattern) {
 		for (Item item : pattern.getItems()) {
 			if (!item.isVariable()) {
 				 return true;
 			}
 		}
 		return false;
 	}
 
 	protected boolean containsAtLeastTwoLiterals(final TriplePattern pattern) {
 		return pattern.getVariables().size() < 2;
 	}
 
 	protected boolean containsOnlyLiterals(final TriplePattern pattern) {
 		for (Item item : pattern) {
 			if (item.isVariable()) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
