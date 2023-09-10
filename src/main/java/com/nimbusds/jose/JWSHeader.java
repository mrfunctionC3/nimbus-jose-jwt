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

package com.nimbusds.jose;


import java.net.URI;
import java.text.ParseException;
import java.util.*;

import net.jcip.annotations.Immutable;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jose.util.X509CertChainUtils;


/**
 * JSON Web Signature (JWS) header. This class is immutable.
 *
 * <p>Supports the following {@link #getRegisteredParameterNames registered
 * header parameters}:
 *
 * <ul>
 *     <li>alg
 *     <li>jku
 *     <li>jwk
 *     <li>x5u
 *     <li>x5t
 *     <li>x5t#S256
 *     <li>x5c
 *     <li>kid
 *     <li>typ
 *     <li>cty
 *     <li>crit
 *     <li>b64
 * </ul>
 *
 * <p>The header may also include {@link #getCustomParams custom
 * parameters}; these will be serialised and parsed along the registered ones.
 *
 * <p>Example header of a JSON Web Signature (JWS) object using the 
 * {@link JWSAlgorithm#HS256 HMAC SHA-256 algorithm}:
 *
 * <pre>
 * {
 *   "alg" : "HS256"
 * }
 * </pre>
 *
 * @author Vladimir Dzhuvinov
 * @version 2022-03-07
 */
@Immutable
public final class JWSHeader extends CommonSEHeader {


	private static final long serialVersionUID = 1L;


	/**
	 * The registered parameter names.
	 */
	private static final Set<String> REGISTERED_PARAMETER_NAMES;


	static {
		Set<String> p = new HashSet<>();

		p.add(HeaderParameterNames.ALGORITHM);
		p.add(HeaderParameterNames.JWK_SET_URL);
		p.add(HeaderParameterNames.JWK);
		p.add(HeaderParameterNames.X_509_CERT_URL);
		p.add(HeaderParameterNames.X_509_CERT_SHA_1_THUMBPRINT);
		p.add(HeaderParameterNames.X_509_CERT_SHA_256_THUMBPRINT);
		p.add(HeaderParameterNames.X_509_CERT_CHAIN);
		p.add(HeaderParameterNames.KEY_ID);
		p.add(HeaderParameterNames.TYPE);
		p.add(HeaderParameterNames.CONTENT_TYPE);
		p.add(HeaderParameterNames.CRITICAL);
		p.add(HeaderParameterNames.BASE64_URL_ENCODE_PAYLOAD);

		REGISTERED_PARAMETER_NAMES = Collections.unmodifiableSet(p);
	}


	/**
	 * Builder for constructing JSON Web Signature (JWS) headers.
	 *
	 * <p>Example usage:
	 *
	 * <pre>
	 * JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
	 *                    .contentType("text/plain")
	 *                    .customParam("exp", new Date().getTime())
	 *                    .build();
	 * </pre>
	 */
	public static class Builder {


		/**
		 * The JWS algorithm.
		 */
		private final JWSAlgorithm alg;


		/**
		 * The JOSE object type.
		 */
		private JOSEObjectType typ;


		/**
		 * The content type.
		 */
		private String cty;


		/**
		 * The critical headers.
		 */
		private Set<String> crit;


		/**
		 * Public JWK Set URL.
		 */
		private URI jku;


		/**
		 * Public JWK.
		 */
		private JWK jwk;


		/**
		 * X.509 certificate URL.
		 */
		private URI x5u;


		/**
		 * X.509 certificate SHA-1 thumbprint.
		 */
		@Deprecated
		private Base64URL x5t;


		/**
		 * X.509 certificate SHA-256 thumbprint.
		 */
		private Base64URL x5t256;


		/**
		 * The X.509 certificate chain corresponding to the key used to
		 * sign the JWS object.
		 */
		private List<Base64> x5c;


		/**
		 * Key ID.
		 */
		private String kid;
		
		
		/**
		 * Base64URL encoding of the payload, the default is
		 * {@code true} for standard JWS serialisation.
		 */
		private boolean b64 = true;


		/**
		 * Custom header parameters.
		 */
		private Map<String,Object> customParams;


		/**
		 * The parsed Base64URL.
		 */
		private Base64URL parsedBase64URL;


		/**
		 * Creates a new JWS header builder.
		 *
		 * @param alg The JWS algorithm ({@code alg}) parameter. Must
		 *            not be "none" or {@code null}.
		 */
		public Builder(final JWSAlgorithm alg) {

			if (alg.getName().equals(Algorithm.NONE.getName())) {
				throw new IllegalArgumentException("The JWS algorithm \"alg\" cannot be \"none\"");
			}

			this.alg = alg;
		}


		/**
		 * Creates a new JWS header builder with the parameters from
		 * the specified header.
		 *
		 * @param jwsHeader The JWS header to use. Must not be
		 *                  {@code null}.
		 */
		public Builder(final JWSHeader jwsHeader) {

			this(jwsHeader.getAlgorithm());

			typ = jwsHeader.getType();
			cty = jwsHeader.getContentType();
			crit = jwsHeader.getCriticalParams();

			jku = jwsHeader.getJWKURL();
			jwk = jwsHeader.getJWK();
			x5u = jwsHeader.getX509CertURL();
			x5t = jwsHeader.getX509CertThumbprint();
			x5t256 = jwsHeader.getX509CertSHA256Thumbprint();
			x5c = jwsHeader.getX509CertChain();
			kid = jwsHeader.getKeyID();
			b64 = jwsHeader.isBase64URLEncodePayload();
			customParams = jwsHeader.getCustomParams();
		}


		/**
		 * Sets the type ({@code typ}) parameter.
		 *
		 * @param typ The type parameter, {@code null} if not
		 *            specified.
		 *
		 * @return This builder.
		 */
		public Builder type(final JOSEObjectType typ) {

			this.typ = typ;
			return this;
		}


		/**
		 * Sets the content type ({@code cty}) parameter.
		 *
		 * @param cty The content type parameter, {@code null} if not
		 *            specified.
		 *
		 * @return This builder.
		 */
		public Builder contentType(final String cty) {

			this.cty = cty;
			return this;
		}


		/**
		 * Sets the critical header parameters ({@code crit})
		 * parameter.
		 *
		 * @param crit The names of the critical header parameters,
		 *             empty set or {@code null} if none.
		 *
		 * @return This builder.
		 */
		public Builder criticalParams(final Set<String> crit) {

			this.crit = crit;
			return this;
		}


		/**
		 * Sets the public JSON Web Key (JWK) Set URL ({@code jku})
		 * parameter.
		 *
		 * @param jku The public JSON Web Key (JWK) Set URL parameter,
		 *            {@code null} if not specified.
		 *
		 * @return This builder.
		 */
		public Builder jwkURL(final URI jku) {

			this.jku = jku;
			return this;
		}


		/**
		 * Sets the public JSON Web Key (JWK) ({@code jwk}) parameter.
		 *
		 * @param jwk The public JSON Web Key (JWK) ({@code jwk})
		 *            parameter, {@code null} if not specified.
		 *
		 * @return This builder.
		 */
		public Builder jwk(final JWK jwk) {

			if (jwk != null && jwk.isPrivate()) {
				throw new IllegalArgumentException("The JWK must be public");
			}
			
			this.jwk = jwk;
			return this;
		}


		/**
		 * Sets the X.509 certificate URL ({@code x5u}) parameter.
		 *
		 * @param x5u The X.509 certificate URL parameter, {@code null}
		 *            if not specified.
		 *
		 * @return This builder.
		 */
		public Builder x509CertURL(final URI x5u) {

			this.x5u = x5u;
			return this;
		}


		/**
		 * Sets the X.509 certificate SHA-1 thumbprint ({@code x5t})
		 * parameter.
		 *
		 * @param x5t The X.509 certificate SHA-1 thumbprint parameter,
		 *            {@code null} if not specified.
		 *
		 * @return This builder.
		 */
		@Deprecated
		public Builder x509CertThumbprint(final Base64URL x5t) {

			this.x5t = x5t;
			return this;
		}


		/**
		 * Sets the X.509 certificate SHA-256 thumbprint
		 * ({@code x5t#S256}) parameter.
		 *
		 * @param x5t256 The X.509 certificate SHA-256 thumbprint
		 *               parameter, {@code null} if not specified.
		 *
		 * @return This builder.
		 */
		public Builder x509CertSHA256Thumbprint(final Base64URL x5t256) {

			this.x5t256 = x5t256;
			return this;
		}


		/**
		 * Sets the X.509 certificate chain parameter ({@code x5c})
		 * corresponding to the key used to sign the JWS object.
		 *
		 * @param x5c The X.509 certificate chain parameter,
		 *            {@code null} if not specified.
		 *
		 * @return This builder.
		 */
		public Builder x509CertChain(final List<Base64> x5c) {

			this.x5c = x5c;
			return this;
		}


		/**
		 * Sets the key ID ({@code kid}) parameter.
		 *
		 * @param kid The key ID parameter, {@code null} if not
		 *            specified.
		 *
		 * @return This builder.
		 */
		public Builder keyID(final String kid) {

			this.kid = kid;
			return this;
		}
		
		
		/**
		 * Sets the Base64URL encode payload ({@code b64}) parameter.
		 *
		 * @param b64 {@code true} to Base64URL encode the payload
		 *            for standard JWS serialisation, {@code false} for
		 *            unencoded payload (RFC 7797).
		 *
		 * @return This builder.
		 */
		public Builder base64URLEncodePayload(final boolean b64) {
			
			this.b64 = b64;
			return this;
		}


		/**
		 * Sets a custom (non-registered) parameter.
		 *
		 * @param name  The name of the custom parameter. Must not
		 *              match a registered parameter name and must not
		 *              be {@code null}.
		 * @param value The value of the custom parameter, should map
		 *              to a valid JSON entity, {@code null} if not
		 *              specified.
		 *
		 * @return This builder.
		 *
		 * @throws IllegalArgumentException If the specified parameter
		 *                                  name matches a registered
		 *                                  parameter name.
		 */
		public Builder customParam(final String name, final Object value) {

			if (getRegisteredParameterNames().contains(name)) {
				throw new IllegalArgumentException("The parameter name \"" + name + "\" matches a registered name");
			}

			if (customParams == null) {
				customParams = new HashMap<>();
			}

			customParams.put(name, value);

			return this;
		}


		/**
		 * Sets the custom (non-registered) parameters. The values must
		 * be serialisable to a JSON entity, otherwise will be ignored.
		 *
		 * @param customParameters The custom parameters, empty map or
		 *                         {@code null} if none.
		 *
		 * @return This builder.
		 */
		public Builder customParams(final Map<String, Object> customParameters) {

			this.customParams = customParameters;
			return this;
		}


		/**
		 * Sets the parsed Base64URL.
		 *
		 * @param base64URL The parsed Base64URL, {@code null} if the
		 *                  header is created from scratch.
		 *
		 * @return This builder.
		 */
		public Builder parsedBase64URL(final Base64URL base64URL) {

			this.parsedBase64URL = base64URL;
			return this;
		}


		/**
		 * Builds a new JWS header.
		 *
		 * @return The JWS header.
		 */
		public JWSHeader build() {

			return new JWSHeader(
				alg, typ, cty, crit,
				jku, jwk, x5u, x5t, x5t256, x5c, kid, b64,
				customParams, parsedBase64URL);
		}
	}
	
	
	/**
	 * Base64URL encoding of the payload, {@code true} for standard JWS
	 * serialisation, {@code false} for unencoded payload (RFC 7797).
	 */
	private final boolean b64;


	/**
	 * Creates a new minimal JSON Web Signature (JWS) header.
	 *
	 * <p>Note: Use {@link PlainHeader} to create a header with algorithm
	 * {@link Algorithm#NONE none}.
	 *
	 * @param alg The JWS algorithm ({@code alg}) parameter. Must not be
	 *            "none" or {@code null}.
	 */
	public JWSHeader(final JWSAlgorithm alg) {

		this(alg, null, null, null, null, null, null, null, null, null, null, true,null, null);
	}


	/**
	 * Creates a new JSON Web Signature (JWS) header.
	 *
	 * <p>Note: Use {@link PlainHeader} to create a header with algorithm
	 * {@link Algorithm#NONE none}.
	 *
	 * @param alg             The JWS algorithm ({@code alg}) parameter.
	 *                        Must not be "none" or {@code null}.
	 * @param typ             The type ({@code typ}) parameter,
	 *                        {@code null} if not specified.
	 * @param cty             The content type ({@code cty}) parameter,
	 *                        {@code null} if not specified.
	 * @param crit            The names of the critical header
	 *                        ({@code crit}) parameters, empty set or
	 *                        {@code null} if none.
	 * @param jku             The JSON Web Key (JWK) Set URL ({@code jku})
	 *                        parameter, {@code null} if not specified.
	 * @param jwk             The X.509 certificate URL ({@code jwk})
	 *                        parameter, {@code null} if not specified.
	 * @param x5u             The X.509 certificate URL parameter
	 *                        ({@code x5u}), {@code null} if not specified.
	 * @param x5t             The X.509 certificate SHA-1 thumbprint
	 *                        ({@code x5t}) parameter, {@code null} if not
	 *                        specified.
	 * @param x5t256          The X.509 certificate SHA-256 thumbprint
	 *                        ({@code x5t#S256}) parameter, {@code null} if
	 *                        not specified.
	 * @param x5c             The X.509 certificate chain ({@code x5c})
	 *                        parameter, {@code null} if not specified.
	 * @param kid             The key ID ({@code kid}) parameter,
	 *                        {@code null} if not specified.
	 * @param customParams    The custom parameters, empty map or
	 *                        {@code null} if none.
	 * @param parsedBase64URL The parsed Base64URL, {@code null} if the
	 *                        header is created from scratch.
	 */
	@Deprecated
	public JWSHeader(final JWSAlgorithm alg,
			 final JOSEObjectType typ,
			 final String cty,
			 final Set<String> crit,
			 final URI jku,
			 final JWK jwk,
			 final URI x5u,
			 final Base64URL x5t,
			 final Base64URL x5t256,
			 final List<Base64> x5c,
			 final String kid,
			 final Map<String,Object> customParams,
			 final Base64URL parsedBase64URL) {

		this(alg, typ, cty, crit, jku, jwk, x5u, x5t, x5t256, x5c, kid, true, customParams, parsedBase64URL);
	}


	/**
	 * Creates a new JSON Web Signature (JWS) header.
	 *
	 * <p>Note: Use {@link PlainHeader} to create a header with algorithm
	 * {@link Algorithm#NONE none}.
	 *
	 * @param alg             The JWS algorithm ({@code alg}) parameter.
	 *                        Must not be "none" or {@code null}.
	 * @param typ             The type ({@code typ}) parameter,
	 *                        {@code null} if not specified.
	 * @param cty             The content type ({@code cty}) parameter,
	 *                        {@code null} if not specified.
	 * @param crit            The names of the critical header
	 *                        ({@code crit}) parameters, empty set or
	 *                        {@code null} if none.
	 * @param jku             The JSON Web Key (JWK) Set URL ({@code jku})
	 *                        parameter, {@code null} if not specified.
	 * @param jwk             The X.509 certificate URL ({@code jwk})
	 *                        parameter, {@code null} if not specified.
	 * @param x5u             The X.509 certificate URL parameter
	 *                        ({@code x5u}), {@code null} if not specified.
	 * @param x5t             The X.509 certificate SHA-1 thumbprint
	 *                        ({@code x5t}) parameter, {@code null} if not
	 *                        specified.
	 * @param x5t256          The X.509 certificate SHA-256 thumbprint
	 *                        ({@code x5t#S256}) parameter, {@code null} if
	 *                        not specified.
	 * @param x5c             The X.509 certificate chain ({@code x5c})
	 *                        parameter, {@code null} if not specified.
	 * @param kid             The key ID ({@code kid}) parameter,
	 *                        {@code null} if not specified.
	 * @param b64             {@code true} to Base64URL encode the payload
	 *                        for standard JWS serialisation, {@code false}
	 *                        for unencoded payload (RFC 7797).
	 * @param customParams    The custom parameters, empty map or
	 *                        {@code null} if none.
	 * @param parsedBase64URL The parsed Base64URL, {@code null} if the
	 *                        header is created from scratch.
	 */
	public JWSHeader(final JWSAlgorithm alg,
			 final JOSEObjectType typ,
			 final String cty,
			 final Set<String> crit,
			 final URI jku,
			 final JWK jwk,
			 final URI x5u,
			 final Base64URL x5t,
			 final Base64URL x5t256,
			 final List<Base64> x5c,
			 final String kid,
			 final boolean b64,
			 final Map<String,Object> customParams,
			 final Base64URL parsedBase64URL) {

		super(alg, typ, cty, crit, jku, jwk, x5u, x5t, x5t256, x5c, kid, customParams, parsedBase64URL);

		if (alg == null) {
			throw new IllegalArgumentException("The algorithm \"alg\" header parameter must not be null");
		}

		if (alg.getName().equals(Algorithm.NONE.getName())) {
			throw new IllegalArgumentException("The JWS algorithm \"alg\" cannot be \"none\"");
		}
		
		this.b64 = b64;
	}


	/**
	 * Deep copy constructor.
	 *
	 * @param jwsHeader The JWS header to copy. Must not be {@code null}.
	 */
	public JWSHeader(final JWSHeader jwsHeader) {

		this(
			jwsHeader.getAlgorithm(),
			jwsHeader.getType(),
			jwsHeader.getContentType(),
			jwsHeader.getCriticalParams(),
			jwsHeader.getJWKURL(),
			jwsHeader.getJWK(),
			jwsHeader.getX509CertURL(),
			jwsHeader.getX509CertThumbprint(),
			jwsHeader.getX509CertSHA256Thumbprint(),
			jwsHeader.getX509CertChain(),
			jwsHeader.getKeyID(),
			jwsHeader.isBase64URLEncodePayload(),
			jwsHeader.getCustomParams(),
			jwsHeader.getParsedBase64URL()
		);
	}


	/**
	 * Gets the registered parameter names for JWS headers.
	 *
	 * @return The registered parameter names, as an unmodifiable set.
	 */
	public static Set<String> getRegisteredParameterNames() {

		return REGISTERED_PARAMETER_NAMES;
	}


	/**
	 * Gets the algorithm ({@code alg}) parameter.
	 *
	 * @return The algorithm parameter.
	 */
	@Override
	public JWSAlgorithm getAlgorithm() {

		return (JWSAlgorithm)super.getAlgorithm();
	}
	
	
	/**
	 * Returns the Base64URL-encode payload ({@code b64}) parameter.
	 *
	 * @return {@code true} to Base64URL encode the payload for standard
	 *         JWS serialisation, {@code false} for unencoded payload (RFC
	 *         7797).
	 */
	public boolean isBase64URLEncodePayload() {
		
		return b64;
	}
	
	
	@Override
	public Set<String> getIncludedParams() {
		Set<String> includedParams = super.getIncludedParams();
		if (! isBase64URLEncodePayload()) {
			includedParams.add(HeaderParameterNames.BASE64_URL_ENCODE_PAYLOAD);
		}
		return includedParams;
	}
	
	
	@Override
	public Map<String, Object> toJSONObject() {
		Map<String, Object> o = super.toJSONObject();
		if (! isBase64URLEncodePayload()) {
			o.put(HeaderParameterNames.BASE64_URL_ENCODE_PAYLOAD, false);
		}
		return o;
	}
	
	
	/**
	 * Parses a JWS header from the specified JSON object.
	 *
	 * @param jsonObject The JSON object to parse. Must not be
	 *                   {@code null}.
	 *
	 * @return The JWS header.
	 *
	 * @throws ParseException If the specified JSON object doesn't
	 *                        represent a valid JWS header.
	 */
	public static JWSHeader parse(final Map<String, Object> jsonObject)
		throws ParseException {

		return parse(jsonObject, null);
	}


	/**
	 * Parses a JWS header from the specified JSON object.
	 *
	 * @param jsonObject      The JSON object to parse. Must not be
	 *                        {@code null}.
	 * @param parsedBase64URL The original parsed Base64URL, {@code null}
	 *                        if not applicable.
	 *
	 * @return The JWS header.
	 *
	 * @throws ParseException If the specified JSON object doesn't 
	 *                        represent a valid JWS header.
	 */
	public static JWSHeader parse(final Map<String, Object> jsonObject,
				      final Base64URL parsedBase64URL)
		throws ParseException {

		// Get the "alg" parameter
		Algorithm alg = Header.parseAlgorithm(jsonObject);

		if (! (alg instanceof JWSAlgorithm)) {
			throw new ParseException("Not a JWS header", 0);
		}

		JWSHeader.Builder header = new Builder((JWSAlgorithm)alg).parsedBase64URL(parsedBase64URL);

		// Parse optional + custom parameters
		for (final String name: jsonObject.keySet()) {
			
			if(HeaderParameterNames.ALGORITHM.equals(name)) {
				// skip
			} else if(HeaderParameterNames.TYPE.equals(name)) {
				String typValue = JSONObjectUtils.getString(jsonObject, name);
				if (typValue != null) {
					header = header.type(new JOSEObjectType(typValue));
				}
			} else if(HeaderParameterNames.CONTENT_TYPE.equals(name)) {
				header = header.contentType(JSONObjectUtils.getString(jsonObject, name));
			} else if(HeaderParameterNames.CRITICAL.equals(name)) {
				List<String> critValues = JSONObjectUtils.getStringList(jsonObject, name);
				if (critValues != null) {
					header = header.criticalParams(new HashSet<>(critValues));
				}
			} else if(HeaderParameterNames.JWK_SET_URL.equals(name)) {
				header = header.jwkURL(JSONObjectUtils.getURI(jsonObject, name));
			} else if(HeaderParameterNames.JWK.equals(name)) {
				header = header.jwk(CommonSEHeader.parsePublicJWK(JSONObjectUtils.getJSONObject(jsonObject, name)));
			} else if(HeaderParameterNames.X_509_CERT_URL.equals(name)) {
				header = header.x509CertURL(JSONObjectUtils.getURI(jsonObject, name));
			} else if(HeaderParameterNames.X_509_CERT_SHA_1_THUMBPRINT.equals(name)) {
				header = header.x509CertThumbprint(Base64URL.from(JSONObjectUtils.getString(jsonObject, name)));
			} else if(HeaderParameterNames.X_509_CERT_SHA_256_THUMBPRINT.equals(name)) {
				header = header.x509CertSHA256Thumbprint(Base64URL.from(JSONObjectUtils.getString(jsonObject, name)));
			} else if(HeaderParameterNames.X_509_CERT_CHAIN.equals(name)) {
				header = header.x509CertChain(X509CertChainUtils.toBase64List(JSONObjectUtils.getJSONArray(jsonObject, name)));
			} else if(HeaderParameterNames.KEY_ID.equals(name)) {
				header = header.keyID(JSONObjectUtils.getString(jsonObject, name));
			} else if(HeaderParameterNames.BASE64_URL_ENCODE_PAYLOAD.equals(name)) {
				header = header.base64URLEncodePayload(JSONObjectUtils.getBoolean(jsonObject, name));
			} else {
				header = header.customParam(name, jsonObject.get(name));
			}
		}

		return header.build();
	}


	/**
	 * Parses a JWS header from the specified JSON object string.
	 *
	 * @param jsonString The JSON string to parse. Must not be
	 *                   {@code null}.
	 *
	 * @return The JWS header.
	 *
	 * @throws ParseException If the specified JSON object string doesn't
	 *                        represent a valid JWS header.
	 */
	public static JWSHeader parse(final String jsonString)
		throws ParseException {

		return parse(jsonString, null);
	}


	/**
	 * Parses a JWS header from the specified JSON object string.
	 *
	 * @param jsonString      The JSON string to parse. Must not be
	 *                        {@code null}.
	 * @param parsedBase64URL The original parsed Base64URL, {@code null}
	 *                        if not applicable.
	 *
	 * @return The JWS header.
	 *
	 * @throws ParseException If the specified JSON object string doesn't 
	 *                        represent a valid JWS header.
	 */
	public static JWSHeader parse(final String jsonString,
				      final Base64URL parsedBase64URL)
		throws ParseException {

		return parse(JSONObjectUtils.parse(jsonString, MAX_HEADER_STRING_LENGTH), parsedBase64URL);
	}


	/**
	 * Parses a JWS header from the specified Base64URL.
	 *
	 * @param base64URL The Base64URL to parse. Must not be {@code null}.
	 *
	 * @return The JWS header.
	 *
	 * @throws ParseException If the specified Base64URL doesn't represent
	 *                        a valid JWS header.
	 */
	public static JWSHeader parse(final Base64URL base64URL)
		throws ParseException {

		return parse(base64URL.decodeToString(), base64URL);
	}
}
