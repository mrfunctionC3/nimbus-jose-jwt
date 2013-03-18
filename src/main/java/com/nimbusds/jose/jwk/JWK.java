package com.nimbusds.jose.jwk;


import java.text.ParseException;

import net.minidev.json.JSONAware;
import net.minidev.json.JSONObject;

import com.nimbusds.jose.Algorithm;

import com.nimbusds.jose.util.JSONObjectUtils;


/**
 * The base abstract class for public JSON Web Keys (JWKs). It serialises to a 
 * JSON object.
 *
 * <p>The following JSON object members are common to all JWK types:
 *
 * <ul>
 *     <li>{@link #getKeyType kty} (required)
 *     <li>{@link #getKeyUse use} (optional)
 *     <li>{@link #getKeyID kid} (optional)
 * </ul>
 *
 * <p>Example JWK (of the Elliptic Curve type):
 *
 * <pre>
 * {
 *   "kty" : "EC",
 *   "crv" : "P-256",
 *   "x"   : "MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4",
 *   "y"   : "4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM",
 *   "use" : "enc",
 *   "kid" : "1"
 * }
 * </pre>
 *
 * @author Vladimir Dzhuvinov
 * @author Justin Richer
 * @version $version$ (2013-03-18)
 */
public abstract class JWK implements JSONAware {


	/**
	 * The key type, required.
	 */
	private final KeyType kty;


	/**
	 * The key use, optional.
	 */
	private final Use use;


	/**
	 * The intended JOSE algorithm for the key, optional.
	 */
	private final Algorithm alg;


	/**
	 * The key ID, optional.
	 */
	private final String kid;


	/**
	 * Creates a new JSON Web Key (JWK).
	 *
	 * @param kty The key type. Must not be {@code null}.
	 * @param use The key use, {@code null} if not specified or if the key 
	 *            is intended for signing as well as encryption.
	 * @param alg The intended JOSE algorithm for the key, {@code null} if
	 *            not specified.
	 * @param kid The key ID, {@code null} if not specified.
	 */
	protected JWK(final KeyType kty, final Use use, final Algorithm alg, final String kid) {

		if (kty == null) {
			throw new IllegalArgumentException("The key type \"kty\" must not be null");
		}

		this.kty = kty;

		this.use = use;

		this.alg = alg;

		this.kid = kid;
	}


	/**
	 * Gets the type ({@code kty}) of this JWK.
	 *
	 * @return The key type.
	 */
	public KeyType getKeyType() {

		return kty;
	}


	/**
	 * Gets the use ({@code use}) of this JWK.
	 *
	 * @return The key use, {@code null} if not specified or if the key is
	 *         intended for signing as well as encryption.
	 */
	public Use getKeyUse() {

		return use;
	}


	/**
	 * Gets the intended JOSE algorithm ({@code alg}) for this JWK.
	 *
	 * @return The intended JOSE algorithm, {@code null} if not specified.
	 */
	public Algorithm getAlgorithm() {

		return alg;
	}


	/**
	 * Gets the ID ({@code kid}) of this JWK. The key ID can be used to 
	 * match a specific key. This can be used, for instance, to choose a 
	 * key within a {@link JWKSet} during key rollover. The key ID may also 
	 * correspond to a JWS/JWE {@code kid} header parameter value.
	 *
	 * @return The key ID, {@code null} if not specified.
	 */
	public String getKeyID() {

		return kid;
	}


	/**
	 * Returns a JSON object representation of this JWK. Sensitive 
	 * non-public parameters, such as EC and RSA private key parameters or
	 * symmetric key values, will not be included in the output JSON 
	 * object. See the alternative {@link #toJSONObject(boolean)} method if
	 * you wish to include them.
	 *
	 * <p>Example:
	 *
	 * <pre>
	 * {
	 *   "kty" : "RSA",
	 *   "use" : "sig",
	 *   "kid" : "fd28e025-8d24-48bc-a51a-e2ffc8bc274b"
	 * }
	 * </pre>
	 *
	 * @return The JSON object representation.
	 */
	public JSONObject toJSONObject() {

		return toJSONObject(false);
	}


	/**
	 * Returns a JSON object representation of this JWK. This method is 
	 * intended to be called from extending classes.
	 *
	 * <p>Example:
	 *
	 * <pre>
	 * {
	 *   "kty" : "RSA",
	 *   "use" : "sig",
	 *   "kid" : "fd28e025-8d24-48bc-a51a-e2ffc8bc274b"
	 * }
	 * </pre>
	 *
	 * @param includeNonPublicParams Controls the inclusion of sensitive 
	 *                               non-public key parameters into the
	 *                               output JSON object. If {@code true} 
	 *                               private parameters (for EC and RSA 
	 *                               keys) and symmetric secret values will
	 *                               be included in the output JSON object.
	 *                               If {@code false} only the public key
	 *                               parameters will be included.
	 *
	 * @return The JSON object representation.
	 */
	public JSONObject toJSONObject(final boolean includeNonPublicParams) {

		JSONObject o = new JSONObject();

		// No sensitive JWK params in base object

		o.put("kty", kty.getValue());

		if (use != null) {

			if (use == Use.SIGNATURE) {
				o.put("use", "sig");
			}

			if (use == Use.ENCRYPTION) {
				o.put("use", "enc");
			}
		}

		if (alg != null) {
			o.put("alg", alg.getName());
		}

		if (kid != null) {
			o.put("kid", kid);
		}

		return o;
	}


	/**
	 * Returns the JSON object string representation of this JWK.
	 *
	 * @return The JSON object string representation.
	 */
	@Override
	public String toJSONString() {

		return toJSONObject().toString();
	}


	/**
	 * @see #toJSONString
	 */
	@Override
	public String toString() {

		return toJSONObject().toString();
	}


	/**
	 * Parses a JWK from the specified JSON object string representation.
	 * 
	 * <p>The JWK must represent one of the following:
	 *
	 * <ul>
	 *     <li>{@link ECPublicKey} or {@link ECKeyPair}.
	 *     <li>{@link RSAPublicKey} or {@link RSAKeyPair}.
	 *     <li>{@link SymmetricKey}.
	 * </ul>
	 *
	 * @param s The JSON object string to parse. Must not be {@code null}.
	 *
	 * @return The JWK.
	 *
	 * @throws ParseException If the JSON object string couldn't be parsed 
	 *                        to a supported JWK.
	 */
	public static JWK parse(final String s)
			throws ParseException {

		return parse(JSONObjectUtils.parseJSONObject(s));
	}


	/**
	 * Parses a JWK from the specified JSON object representation.
	 * 
	 * <p>The JWK must represent one of the following:
	 *
	 * <ul>
	 *     <li>{@link ECPublicKey} or {@link ECKeyPair}.
	 *     <li>{@link RSAPublicKey} or {@link RSAKeyPair}.
	 *     <li>{@link SymmetricKey}.
	 * </ul>
	 *
	 * @param jsonObject The JSON object to parse. Must not be 
	 *                   {@code null}.
	 *
	 * @return The JWK.
	 *
	 * @throws ParseException If the JSON object couldn't be parsed to a 
	 *                        supported JWK.
	 */
	public static JWK parse(final JSONObject jsonObject)
			throws ParseException {

		KeyType kty = KeyType.parse(JSONObjectUtils.getString(jsonObject, "kty"));

		if (kty == KeyType.EC) {
			
			// Test for private 'd' EC coordinate
			if (jsonObject.containsKey("d")) {

				return ECKeyPair.parse(jsonObject);

			} else {

				return ECPublicKey.parse(jsonObject);
			}

		} else if (kty == KeyType.RSA) {
			
			// Test for private exponent (from first private key representation)
			// or first prime factor (second private key representation)
			if (jsonObject.containsKey("d") || jsonObject.containsKey("p")) {

				return RSAKeyPair.parse(jsonObject);

			} else {

				return RSAPublicKey.parse(jsonObject);	
			}

		} else if (kty == KeyType.OCT) {
			
			return SymmetricKey.parse(jsonObject);

		} else {

			throw new ParseException("Unsupported key type \"kty\" parameter: " + kty, 0);
		}
	}


	/**
	 * Parses a key use ({@code use}) parameter from the specified JSON 
	 * object representation of a JWK.
	 *
	 * @param jsonObject The JSON object to parse. Must not be 
	 *                   {@code null}.
	 *
	 * @return The key use, {@code null} if not specified.
	 *
	 * @throws ParseException If the key use parameter couldn't be parsed.
	 */
	protected static Use parseKeyUse(final JSONObject jsonObject)
			throws ParseException {

		if (jsonObject.get("use") == null) {
			return null;
		}

		String useStr = JSONObjectUtils.getString(jsonObject, "use");

		if (useStr.equals("sig")) {
			return Use.SIGNATURE;
		} else if (useStr.equals("enc")) {
			return Use.ENCRYPTION;
		} else {
			throw new ParseException("Invalid or unsupported key use \"use\" parameter, must be \"sig\" or \"enc\"", 0);
		}
	}


	/**
	 * Parses an algorithm ({@code alg}) parameter from the specified JSON
	 * object representation of a JWK.
	 *
	 * <p>Note that the algorithm requirement level is not inferred.
	 *
	 * @param jsonObject The JSON object to parse. Must not be 
	 *                   {@code null}.
	 *
	 * @return The algorithm, {@code null} if not specified.
	 *
	 * @throws ParseException If the algorithm parameter couldn't be
	 *                        parsed.
	 */
	protected static Algorithm parseAlgorithm(final JSONObject jsonObject)
			throws ParseException {

		if (jsonObject.get("alg") == null) {
			return null;
		}

		String algStr = JSONObjectUtils.getString(jsonObject, "alg");

		return new Algorithm(algStr);
	}


	/**
	 * Parses a key ID ({@code kid}) parameter from the specified JSON
	 * object representation of a JWK.
	 *
	 * @param jsonObject The JSON object to parse. Must not be 
	 *                   {@code null}.
	 *
	 * @return The key ID, {@code null} if not specified.
	 *
	 * @throws ParseException If the key ID parameter couldn't be parsed.
	 */
	protected static String parseKeyID(final JSONObject jsonObject)
			throws ParseException {

		if (jsonObject.get("kid") == null) {
			return null;
		}

		return JSONObjectUtils.getString(jsonObject, "kid");
	}
}