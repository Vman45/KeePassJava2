/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.security;

// use spongycastle repackaging of bouncycastle in deference to Android needs
import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.io.CipherInputStream;
import org.spongycastle.crypto.io.CipherOutputStream;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encryption and decryption utilities..
 *
 * @author jo
 */
public class Encryption {

    /**
     * Gets a digest for a UTF-8 encoded string
     *
     * @param string the string
     * @return a digest as a byte array
     */
    @SuppressWarnings("unused")
    public static byte[] getDigest(String string) {
        return getDigest(string, "UTF-8");
    }

    /**
     * Gets a digest for a string
     *
     * @param string the string
     * @param encoding the encoding of the String
     * @return a digest as a byte array
     */
    public static byte[] getDigest(String string, String encoding) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException("String cannot be null or empty");

        if (encoding == null || encoding.length() == 0)
            throw new IllegalArgumentException("Encoding cannot be null or empty");

        MessageDigest md = getSha256MessageDigestInstance();

        try {
            byte[] bytes = string.getBytes(encoding);
            md.update(bytes, 0, bytes.length);
            return md.digest();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(encoding + " is not supported");
        }
    }

    /**
     * Gets a SHA-256 message digest instance
     *
     * @return A MessageDigest
     */
    public static MessageDigest getSha256MessageDigestInstance() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported");
        }
    }

    /**
     * Gets a SHA-512 message digest instance
     *
     * @return A MessageDigest
     */
    public static MessageDigest getSha512MessageDigestInstance() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 is not supported");
        }
    }

    /**
     * Create a decrypted input stream from an encrypted one
     */
    public static InputStream getDecryptedInputStream(InputStream encryptedInputStream, PaddedBufferedBlockCipher pbbc, byte[] keyData, byte[] ivData) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(keyData), ivData);
        pbbc.init(false, keyAndIV);
        return new CipherInputStream(encryptedInputStream, pbbc);
    }

    /**
     * Create a decrypted input stream from an encrypted one
     */
    public static InputStream getDecryptedInputStream(InputStream encryptedInputStream, StreamCipher cipher, byte[] keyData, byte[] ivData) throws IOException {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(keyData), ivData);
        cipher.init(false, keyAndIV);
        return new CipherInputStream(encryptedInputStream, cipher);
    }

    /**
     * Create an encrypted output stream from an unencrypted output stream
     */
    public static OutputStream getEncryptedOutputStream (OutputStream decryptedOutputStream, byte[] keyData, byte[] ivData) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(keyData), ivData);
        PaddedBufferedBlockCipher pbbc = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
        pbbc.init(true, keyAndIV);
        return new CipherOutputStream(decryptedOutputStream, pbbc);
    }
}
