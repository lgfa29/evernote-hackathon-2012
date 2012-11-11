// Copyright (c) 2011, Chute Corporation. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
// * Neither the name of the Chute Corporation nor the names
// of its contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
// IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
// OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.
//
package com.darko.imagedownloader;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import android.content.ContentResolver;

/**
 * @author Darko.Grozdanovski
 * 
 * 
 *         {@link URLStreamHandlerFactory} for {@code content://},
 *         {@code file://}, and {@code android.resource://} URIs.
 **/
public class ContentURLStreamHandlerFactory implements URLStreamHandlerFactory {

	private final ContentResolver resolver;

	public ContentURLStreamHandlerFactory(ContentResolver resolver) {
		if (resolver == null) {
			throw new NullPointerException();
		}
		this.resolver = resolver;
	}

	/**
	 * {@inheritDoc}
	 */
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (ContentResolver.SCHEME_CONTENT.equals(protocol)
				|| ContentResolver.SCHEME_FILE.equals(protocol)
				|| ContentResolver.SCHEME_ANDROID_RESOURCE.equals(protocol)) {
			return new ContentURLStreamHandler(this.resolver);
		} else {
			return null;
		}
	}

}
