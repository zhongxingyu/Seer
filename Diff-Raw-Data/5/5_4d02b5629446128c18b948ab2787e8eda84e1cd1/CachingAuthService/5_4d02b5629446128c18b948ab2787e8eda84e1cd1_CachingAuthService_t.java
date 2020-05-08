 package com.marakana.filez.service;
 
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.util.concurrent.UncheckedExecutionException;
 import com.marakana.filez.domain.Realm;
 import com.marakana.filez.domain.UsernameAndPassword;
 
 public class CachingAuthService implements AuthService {
 
 	private static class UsernamePasswordRealmTuple {
 		private final UsernameAndPassword usernamePassword;
 		private final Realm realm;
 
 		public UsernamePasswordRealmTuple(UsernameAndPassword usernamePassword,
 				Realm realm) {
 			this.usernamePassword = usernamePassword;
 			this.realm = realm;
 		}
 
 		public UsernameAndPassword getUsernamePassword() {
 			return usernamePassword;
 		}
 
 		public Realm getRealm() {
 			return realm;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((realm == null) ? 0 : realm.hashCode());
 			result = prime
 					* result
 					+ ((usernamePassword == null) ? 0 : usernamePassword
 							.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			UsernamePasswordRealmTuple other = (UsernamePasswordRealmTuple) obj;
 			if (realm == null) {
 				if (other.realm != null) {
 					return false;
 				}
 			} else if (!realm.equals(other.realm)) {
 				return false;
 			}
 			if (usernamePassword == null) {
 				if (other.usernamePassword != null) {
 					return false;
 				}
 			} else if (!usernamePassword.equals(other.usernamePassword)) {
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return usernamePassword + "-" + realm;
 		}
 	}
 
 	private final LoadingCache<UsernamePasswordRealmTuple, AuthResult> cache;
 	private final boolean cacheForbidden;
 
 	public CachingAuthService(final AuthService authService, int maxSize,
 			int ttlInSeconds, boolean cacheForbidden) {
 		this.cache = CacheBuilder
 				.newBuilder()
 				.maximumSize(maxSize)
 				.expireAfterWrite(ttlInSeconds, TimeUnit.SECONDS)
 				.build(new CacheLoader<UsernamePasswordRealmTuple, AuthResult>() {
 					public AuthResult load(UsernamePasswordRealmTuple tuple) {
 						return authService.auth(tuple.getUsernamePassword(),
 								tuple.getRealm());
 					}
 				});
 		this.cacheForbidden = cacheForbidden;
 	}
 
 	@Override
 	public AuthResult auth(UsernameAndPassword usernameAndPassword, Realm realm)
 			throws AuthServiceException {
 		UsernamePasswordRealmTuple tuple = new UsernamePasswordRealmTuple(
 				usernameAndPassword, realm);
 		try {
 			AuthResult result = this.cache.getUnchecked(tuple);
 			switch (result) {
 			case OK:
 				break;
 			case FORBIDDEN:
				if (this.cacheForbidden) {
 					break;
				} else {
					// fall-through to default
 				}
 			default:
 				this.cache.invalidate(tuple);
 			}
 			return result;
 		} catch (UncheckedExecutionException e) {
 			Throwable cause = e.getCause();
 			if (cause instanceof AuthServiceException) {
 				throw (AuthServiceException) cause;
 			} else if (cause instanceof RuntimeException) {
 				throw (RuntimeException) cause;
 			} else {
 				throw e;
 			}
 		}
 	}
 }
