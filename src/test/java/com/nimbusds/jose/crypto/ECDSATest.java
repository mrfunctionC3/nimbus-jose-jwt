/*
 * nimbus-jose-jwt
 *
 * Copyright 2012-2016, Connect2id Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.nimbusds.jose.crypto;


import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

import junit.framework.TestCase;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.util.ByteUtils;
import com.nimbusds.jose.util.StandardCharset;


/**
 * Tests the static ECDSA utilities.
 *
 * @version 2022-04-21
 */
public class ECDSATest extends TestCase {


	public void testResolveAlgFromCurve()
		throws JOSEException {

		assertEquals(JWSAlgorithm.ES256, ECDSA.resolveAlgorithm(Curve.P_256));
		assertEquals(JWSAlgorithm.ES256K, ECDSA.resolveAlgorithm(Curve.SECP256K1));
		assertEquals(JWSAlgorithm.ES384, ECDSA.resolveAlgorithm(Curve.P_384));
		assertEquals(JWSAlgorithm.ES512, ECDSA.resolveAlgorithm(Curve.P_521));

		try {
			ECDSA.resolveAlgorithm((Curve)null);

		} catch (JOSEException e) {
			assertEquals("The EC key curve is not supported, must be P-256, P-384 or P-521", e.getMessage());
		}
	}


	public void testResolveAlgFromECKey_P256()
		throws Exception {

		KeyPair keyPair = ECDSARoundTripTest.createECKeyPair(ECDSARoundTripTest.EC256SPEC);
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

		assertEquals(JWSAlgorithm.ES256, ECDSA.resolveAlgorithm(publicKey));
		assertEquals(JWSAlgorithm.ES256, ECDSA.resolveAlgorithm(privateKey));
	}


	public void testResolveAlgFromECKey_P256K()
		throws Exception {

		KeyPair keyPair = ECDSARoundTripTest.createECKeyPair(ECDSARoundTripTest.EC256KSPEC);
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

		assertEquals(JWSAlgorithm.ES256K, ECDSA.resolveAlgorithm(publicKey));
		assertEquals(JWSAlgorithm.ES256K, ECDSA.resolveAlgorithm(privateKey));
	}


	public void testResolveAlgFromECKey_P384()
		throws Exception {

		KeyPair keyPair = ECDSARoundTripTest.createECKeyPair(ECDSARoundTripTest.EC384SPEC);
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

		assertEquals(JWSAlgorithm.ES384, ECDSA.resolveAlgorithm(publicKey));
		assertEquals(JWSAlgorithm.ES384, ECDSA.resolveAlgorithm(privateKey));
	}


	public void testResolveAlgFromECKey_P521()
		throws Exception {

		KeyPair keyPair = ECDSARoundTripTest.createECKeyPair(ECDSARoundTripTest.EC512SPEC);
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

		assertEquals(JWSAlgorithm.ES512, ECDSA.resolveAlgorithm(publicKey));
		assertEquals(JWSAlgorithm.ES512, ECDSA.resolveAlgorithm(privateKey));
	}
	
	
	public void test_default_JCE_for_CVE_2022_21449() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, JOSEException {
		
		KeyPair keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair();
		
		byte[] blankSignature = new byte[64];
		
		Signature signature = Signature.getInstance("SHA256WithECDSAInP1363Format");
		
		signature.initVerify(keyPair.getPublic());
		signature.update("Hello, World".getBytes());
		boolean verify = signature.verify(blankSignature);
		assertFalse("Blank signature must not be valid", verify);
	}
	
	
	public void testTranscoding_concat_to_DER() throws Exception {
		
		KeyPair keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair();
		Signature signature = Signature.getInstance("SHA256WithECDSAInP1363Format");
		signature.initSign(keyPair.getPrivate());
		signature.update("Hello, world!".getBytes(StandardCharset.UTF_8));
		byte[] signatureBytesConcat = signature.sign();
		
		byte[] signatureBytesDER = ECDSA.transcodeSignatureToDER(signatureBytesConcat);
		signature = Signature.getInstance("SHA256WithECDSA");
		signature.initVerify(keyPair.getPublic());
		signature.update("Hello, world!".getBytes(StandardCharset.UTF_8));
		assertTrue(signature.verify(signatureBytesDER));
	}
	
	
	public void testTranscoding_DER_to_concat() throws Exception {
		
		KeyPair keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair();
		Signature signature = Signature.getInstance("SHA256WithECDSA");
		signature.initSign(keyPair.getPrivate());
		signature.update("Hello, world!".getBytes(StandardCharset.UTF_8));
		byte[] signatureBytesDER = signature.sign();
		
		byte[] signatureBytesConcat = ECDSA.transcodeSignatureToConcat(signatureBytesDER, 64);
		signature = Signature.getInstance("SHA256WithECDSAInP1363Format");
		signature.initVerify(keyPair.getPublic());
		signature.update("Hello, world!".getBytes(StandardCharset.UTF_8));
		assertTrue(signature.verify(signatureBytesConcat));
	}
	
	
	public void testTranscoding_toDER_blank() {
		
		try {
			ECDSA.transcodeSignatureToDER(new byte[64]);
		} catch (JOSEException e) {
			assertEquals("Index 64 out of bounds for length 64", e.getMessage());
			assertTrue(e.getCause() instanceof ArrayIndexOutOfBoundsException);
		}
	}
	
	
	public void testTranscoding_toDER_rBlank_sOnes() throws JOSEException {
		
		byte[] rBytes = new byte[32];
		byte[] sBytes = new byte[32];
		Arrays.fill(sBytes, Byte.MAX_VALUE);
		
		ECDSA.transcodeSignatureToDER(ByteUtils.concat(rBytes, sBytes));
	}
	
	
	public void testTranscoding_toDER_rOnes_sZeros() {
		
		byte[] rBytes = new byte[32];
		Arrays.fill(rBytes, Byte.MAX_VALUE);
		byte[] sBytes = new byte[32];
		
		try {
			ECDSA.transcodeSignatureToDER(ByteUtils.concat(rBytes, sBytes));
		} catch (JOSEException e) {
			assertEquals("Index 64 out of bounds for length 64", e.getMessage());
			assertTrue(e.getCause() instanceof ArrayIndexOutOfBoundsException);
		}
	}
}
