/*
 * MIT License
 *
 * Copyright (c) 2026 Fox2Code
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fox2code.frl.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Helper to add a cipher to an InputStream/OutputStream
 */
public final class FRLStreamCipherHelper {
	private static final String STREAM_ALGORITHM = "AES/CTR/NoPadding";
	private static final int IV_LENGTH_BYTES = 16;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private FRLStreamCipherHelper() {}

	public static OutputStream wrapOutputStream(OutputStream rawOut, SecretKey sharedKey) throws IOException {
		byte[] iv = new byte[IV_LENGTH_BYTES];
		SECURE_RANDOM.nextBytes(iv);
		rawOut.write(iv);
		try {
			Cipher cipher = Cipher.getInstance(STREAM_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, sharedKey, new IvParameterSpec(iv));
			return new CipherOutputStream(rawOut, cipher);
		} catch (GeneralSecurityException e) {
			throw new IOException("Failed to setup cipher", e);
		}
	}

	public static InputStream wrapInputStream(InputStream rawIn, SecretKey sharedKey) throws IOException {
		// Use readNBytes to handle socket fragmentation where one read() might not pull 16 bytes
		byte[] iv = rawIn.readNBytes(IV_LENGTH_BYTES);
		if (iv.length != IV_LENGTH_BYTES) {
			throw new java.io.EOFException("Premature EOF: Failed to ingest 16-byte IV.");
		}
		try {
			Cipher cipher = Cipher.getInstance(STREAM_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, sharedKey, new IvParameterSpec(iv));
			return new CipherInputStream(rawIn, cipher);
		} catch (GeneralSecurityException e) {
			throw new IOException("Failed to setup cipher", e);
		}
	}
}
