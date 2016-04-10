package com.nimbusds.jose.jwk.sourcing;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import com.nimbusds.jose.jwk.*;
import junit.framework.TestCase;


/**
 * Tests the immutable JWK set source.
 */
public class ImmutableJWKSetTest extends TestCase {
	

	public void testRun()
		throws Exception {

		KeyPairGenerator pairGen = KeyPairGenerator.getInstance("RSA");
		pairGen.initialize(2048);
		KeyPair keyPair = pairGen.generateKeyPair();

		RSAKey rsaJWK = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic())
			.privateKey((RSAPrivateKey)keyPair.getPrivate())
			.keyID("1")
			.build();

		JWKSet jwkSet = new JWKSet(rsaJWK);

		ImmutableJWKSet immutableJWKSet = new ImmutableJWKSet("123", jwkSet);

		assertEquals("123", immutableJWKSet.getOwner());
		assertEquals(jwkSet, immutableJWKSet.getJWKSet());

		List<JWK> matches = immutableJWKSet.get("123", new JWKSelector(new JWKMatcher.Builder().keyID("1").build()));
		RSAKey m1 = (RSAKey)matches.get(0);
		assertEquals(rsaJWK.getModulus(), m1.getModulus());
		assertEquals(rsaJWK.getPublicExponent(), m1.getPublicExponent());
		assertEquals(rsaJWK.getPrivateExponent(), m1.getPrivateExponent());
		assertEquals(1, matches.size());
	}


	public void testOwnerMismatch()
		throws Exception {

		KeyPairGenerator pairGen = KeyPairGenerator.getInstance("RSA");
		pairGen.initialize(2048);
		KeyPair keyPair = pairGen.generateKeyPair();

		RSAKey rsaJWK = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic())
			.privateKey((RSAPrivateKey)keyPair.getPrivate())
			.keyID("1")
			.build();

		JWKSet jwkSet = new JWKSet(rsaJWK);

		ImmutableJWKSet immutableJWKSet = new ImmutableJWKSet("123", jwkSet);

		assertEquals("123", immutableJWKSet.getOwner());
		assertEquals(jwkSet, immutableJWKSet.getJWKSet());

		assertTrue(immutableJWKSet.get("xxx", new JWKSelector(new JWKMatcher.Builder().keyID("1").build())).isEmpty());
	}
}
