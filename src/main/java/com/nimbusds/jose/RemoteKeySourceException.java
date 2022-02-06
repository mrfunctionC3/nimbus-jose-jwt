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


import com.nimbusds.jose.jwk.source.JWKSetUnavailableException;

/**
 * Remote key source exception.
 *
 * @author Vladimir Dzhuvinov
 * @version 2016-06-21
 */
public class RemoteKeySourceException extends JWKSetUnavailableException {

	/**
	 * Creates a new remote key source exception.
	 *
	 * @param message The message.
	 */
	public RemoteKeySourceException(final String message) {
		super(message);
	}

	/**
	 * Creates a new remote key source exception.
	 *
	 * @param message The message.
	 * @param cause   The cause.
	 */
	public RemoteKeySourceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
