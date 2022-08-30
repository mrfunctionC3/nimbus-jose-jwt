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


import net.jcip.annotations.ThreadSafe;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.cache.CachedObject;
import com.nimbusds.jose.util.events.EventListener;


/**
 * {@linkplain JWKSetSource} with outage tolerance to handle temporary network
 * issues and endpoint downtime, potentially running into minutes or hours.
 * Transparently caches the JWK set provided by the wrapped
 * {@linkplain JWKSetSource}, returning it in case the underlying source throws
 * a {@linkplain JWKSetUnavailableException}.
 *
 * @author Thomas Rørvik Skjølberg
 * @author Vladimir Dzhuvinov
 * @version 2022-08-28
 */
@ThreadSafe
public class OutageTolerantJWKSetSource<C extends SecurityContext> extends AbstractCachingJWKSetSource<C> {
	
	
	/**
	 * JWK set source outage event.
	 */
	public static class OutageEvent<C extends SecurityContext> extends AbstractJWKSetSourceEvent<OutageTolerantJWKSetSource<C>, C> {
		
		private final long remainingTime;
		
		private OutageEvent(final OutageTolerantJWKSetSource<C> source, final long remainingTime, final C context) {
			super(source, context);
			this.remainingTime = remainingTime;
		}
		
		
		/**
		 * Returns the remaining time until the outage cache expires.
		 *
		 * @return The remaining time, in milliseconds.
		 */
		public long getRemainingTime() {
			return remainingTime;
		}
	}
	
	
	private final EventListener<OutageTolerantJWKSetSource<C>, C> eventListener;
	
	
	/**
	 * Creates a new outage tolerant JWK set source.
	 *
	 * @param source        The JWK set source to decorate. Must not be
	 *                      {@code null}.
	 * @param timeToLive    The time to live of the cached JWK set to cover
	 *                      outages, in milliseconds.
	 * @param eventListener The event listener, {@code null} if not
	 *                      specified.
	 */
	public OutageTolerantJWKSetSource(final JWKSetSource<C> source,
					  final long timeToLive,
					  final EventListener<OutageTolerantJWKSetSource<C>,C> eventListener) {
		super(source, timeToLive);
		this.eventListener = eventListener;
	}

	
	@Override
	public JWKSet getJWKSet(final boolean forceReload, final long currentTime, final C context) throws KeySourceException {
		try {
			// cache if successfully refreshed by the underlying source
			JWKSet jwkSet = getSource().getJWKSet(forceReload, currentTime, context);
			cacheJWKSet(jwkSet, currentTime);
			return jwkSet;
			
		} catch (JWKSetUnavailableException e1) {
			if (!forceReload) {
				// return the previously cached JWT set
				CachedObject<JWKSet> cache = getCachedJWKSet();
				if (cache != null && cache.isValid(currentTime)) {
					long remainingTime = cache.getExpirationTime() - currentTime; // in millis
					if (eventListener != null) {
						eventListener.notify(new OutageEvent<>(this, remainingTime, context));
					}
					return cache.get();
				}
			}

			throw e1;
		}
	}
}