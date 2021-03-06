/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.strategy.encrypt.impl;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

/**
 * AES sharding encryptor.
 *
 * @author panjuan
 */
@Getter
@Setter
public final class AESShardingEncryptor implements ShardingEncryptor {
    
    private static final String AES_KEY = "aes.key.value";
    
    private Properties properties = new Properties();
    
    @Override
    public String getType() {
        return "AES";
    }
    
    @Override
    public void init() {
    }
    
    @Override
    @SneakyThrows
    public String encrypt(final Object plaintext) {
        byte[] result = getCipher(Cipher.ENCRYPT_MODE).doFinal(StringUtils.getBytesUtf8(String.valueOf(plaintext)));
        return Base64.encodeBase64String(result);
    }
    
    @Override
    @SneakyThrows
    public Object decrypt(final String ciphertext) {
        if (null == ciphertext) {
            return null;
        }
        byte[] result = getCipher(Cipher.DECRYPT_MODE).doFinal(Base64.decodeBase64(String.valueOf(ciphertext)));
        return new String(result, StandardCharsets.UTF_8);
    }
    
    private Cipher getCipher(final int decryptMode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Preconditions.checkArgument(properties.containsKey(AES_KEY), "No available secret key for `%s`.", AESShardingEncryptor.class.getName());
        Cipher result = Cipher.getInstance(getType());
        result.init(decryptMode, new SecretKeySpec(createSecretKey(), getType()));
        return result;
    }
    
    private byte[] createSecretKey() {
        Preconditions.checkArgument(null != properties.get(AES_KEY), String.format("%s can not be null.", AES_KEY));
        return Arrays.copyOf(DigestUtils.sha1(properties.get(AES_KEY).toString()), 16);
    }
}
