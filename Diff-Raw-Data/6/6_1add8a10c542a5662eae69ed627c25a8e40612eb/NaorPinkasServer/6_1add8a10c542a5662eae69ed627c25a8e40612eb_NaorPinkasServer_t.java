 package cryptocast.crypto.naorpinkas;
 
 import cryptocast.crypto.*;
 import cryptocast.crypto.Protos.BInteger;
 import cryptocast.crypto.naorpinkas.Protos.*;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.protobuf.ByteString;
 
 import static com.google.common.base.Preconditions.*;
 
 /**
  * A server in the Naor-Pinkas broadcast encryption scheme. It knows
  * the entire polynomial and therefore all the private keys of its users. 
  */
 public abstract class NaorPinkasServer<T>
       implements NaorPinkasServerInterface, Serializable {
     private static final long serialVersionUID = -6864326409385317975L;
 
     private NaorPinkasServerContext<T> context;
     private T gp0;  // $g^P(0)$
     private Map<NaorPinkasIdentity, NaorPinkasPersonalKey<T>> keyByIdentity =
                                Maps.newHashMap();
     private Set<NaorPinkasIdentity> revokedUsers = Sets.newHashSet();
     
     private CyclicGroupOfPrimeOrder<T> group;
     private int t;
     
     private static SecureRandom rnd = new SecureRandom();
 
     protected NaorPinkasServer(NaorPinkasServerContext<T> context) {
         this.context = context;
         this.group = context.getGroup();
         this.t = context.getT();
         this.gp0 = group.getPowerOfG(
                           context.getPoly().evaluate(BigInteger.ZERO));
     }
 
     protected abstract Optional<byte[]> encryptSecretWithItem(byte[] secret, T key);
 
     @Override
     public int getT() {
         return t;
     }
     
     public NaorPinkasServerContext<T> getContext() { return context; }
     
     protected static class UnresolvedEncryptionMessage<T> {
         public NaorPinkasMessageCommon common;
         public ImmutableList<NaorPinkasShare<T>> shares;
         private UnresolvedEncryptionMessage(NaorPinkasMessageCommon common,
                                 ImmutableList<NaorPinkasShare<T>> shares) {
             this.common = common;
             this.shares = shares;
         }
     }
     
     protected UnresolvedEncryptionMessage<T> encryptPartial(byte[] secret) {
         BigInteger r = group.getFieldModOrder().randomElement(rnd);
         T grp0 = group.pow(gp0, r), // g^{r P(0)
           gr = group.getPowerOfG(r);
         Optional<byte[]> mEncryptedSecret = encryptSecretWithItem(secret, grp0);
         checkArgument(mEncryptedSecret.isPresent(), "Secret is too large to encrypt");
         byte[] encryptedSecret = mEncryptedSecret.get();
         
         ImmutableList.Builder<NaorPinkasShare<T>> shares = ImmutableList.builder();
         int i = 0;
         int dummies = t - revokedUsers.size();
         while (i < dummies) {
             NaorPinkasShare<T> share = getDummyKey(i).getShare(r, gr);
             shares.add(share);
             ++i;
         }
         for (NaorPinkasIdentity id : revokedUsers) {
             NaorPinkasShare<T> share = getPersonalKey(id).get().getShare(r, gr);
             shares.add(share);
         }
         ImmutableList.Builder<BInteger> lagrangeCoeffs = ImmutableList.builder();
         for (NaorPinkasShare<T> share : shares.build()) {
             BigInteger c = context.getLagrange().getCoefficients().get(share.getI());
             assert c != null;
             lagrangeCoeffs.add(packBigInt(c));
         }
         NaorPinkasMessageCommon common = 
                 NaorPinkasMessageCommon.newBuilder()
                     .setT(t)
                     .setR(packBigInt(r))
                     .setEncryptedSecret(ByteString.copyFrom(encryptedSecret))
                     .addAllCoefficients(lagrangeCoeffs.build())
                     .build();
         return new UnresolvedEncryptionMessage<T>(common, shares.build());
     }
     
     protected BInteger packBigInt(BigInteger x) {
         return BInteger.newBuilder()
                 .setTwoComplement(ByteString.copyFrom(x.toByteArray()))
                 .build();
     }
     
     /**
      * @param i An index.
      * @return The identity with the given index.
      */
     @Override
     public NaorPinkasIdentity getIdentity(int i) {
         return getUserKey(i).getIdentity();
     }
 
     private NaorPinkasPersonalKey<T> getUserKey(int i) {
         // first t users are dummies
         NaorPinkasPersonalKey<T> key = context.getKeyGen().get(t + i);
         keyByIdentity.put(key.getIdentity(), key);
         return key;
     }
 
     private NaorPinkasPersonalKey<T> getDummyKey(int i) {
         return context.getKeyGen().get(i);
     }
 
     /**
      * Revokes multiple users.
      * 
      * @param id The identities of the users
      * @return true, if the set of revoked users changed or false otherwise
      */
     @Override
     public boolean revoke(Set<NaorPinkasIdentity> ids) throws NoMoreRevocationsPossibleError {
         Set<NaorPinkasIdentity> notYetRevoked = Sets.difference(ids, revokedUsers);
         if (notYetRevoked.size() == 0) { return false; }
         if (getRemainingRevocations() < notYetRevoked.size()) {
             throw new NoMoreRevocationsPossibleError();
         }
         for (NaorPinkasIdentity id : notYetRevoked) {
             revokeUnconditional(id);
         }
         return true;
     }
     
     /**
      * Revokes a user.
      * 
      * @param id The identity of the user
      * @return true, if the set of revoked users changed or false otherwise
      */
     @Override
     public boolean revoke(NaorPinkasIdentity id) throws NoMoreRevocationsPossibleError {
         return revoke(ImmutableSet.of(id));
     }
     
     // must only be called if we know revocation will be successful
     // and that the user is not already revoked!
     private void revokeUnconditional(NaorPinkasIdentity id) {
         assert getRemainingRevocations() > 0 && !revokedUsers.contains(id);
         // TODO abstract this away
         // remove highest dummy key and add new identity
         context.getLagrange().removeX(
                 getDummyKey(getRemainingRevocations() - 1).getIdentity().getI());
         context.getLagrange().addX(id.getI());
         revokedUsers.add(id);
     }
     
     private int getRemainingRevocations() {
         return t - revokedUsers.size();
     }
     
     /**
      * Authorizes a user.
      * 
      * @param id The identity of the user
      * @return true, if the set of revoked users changed or false otherwise
      */
     @Override
     public boolean unrevoke(NaorPinkasIdentity id) {
         if (!revokedUsers.contains(id)) { return false; }
         // TODO abstract this away
         // remove old identity and add new dummy
         context.getLagrange().removeX(id.getI());
         context.getLagrange().addX(
                     getDummyKey(t - revokedUsers.size()).getIdentity().getI());
         revokedUsers.remove(id);
         return true;
     }
     
     /**
      * @param id The identity of the user
      * @return Whether the user is revoked.
      */
     @Override
     public boolean isRevoked(NaorPinkasIdentity id) {
         return revokedUsers.contains(id);
     }
     
     /**
      * @param id The identity to look up
      * @return The private key of the user or absent if no
      * such user exists
      */
     public Optional<NaorPinkasPersonalKey<T>> getPersonalKey(NaorPinkasIdentity id) {
         return Optional.fromNullable(keyByIdentity.get(id));
     }
 }
