/*
 * nimbus-jose-jwt
 *
 * Copyright 2012-2022, Connect2id Ltd.
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

package com.nimbusds.jose.jwk.source;

public class JWKSetUnavailableException extends JWKSetServiceException {

	private static final long serialVersionUID = 1L;

	public JWKSetUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public JWKSetUnavailableException(String message) {
		super(message);
	}

	public JWKSetUnavailableException(Throwable cause) {
		super(cause);
	}

}
