package com.interswitch.apigateway;

import org.junit.jupiter.api.Test;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class RSAPublicKeyTest {

    @Test
    public void loadKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjdnDG5wxEnL8euH/zqbsJjj1+uzF2S1PMKGgwNsZ2G/oYx4Py4jhJKRL5YplH7CUQsBf5Z+Qojtabc4ssLLImXSgr+Bz/R2x9JkwLLAsw6+Ib0KpSFXmRlvAwxbZYFvY4xVYaNyjL4/jSvkhKT7DMAJBeH+/mY5WCVSyr2IoTT0UY5FofyxSKUoJOVyddvUQO5991I1CoSYXHUdtoIEd7nPHRhrJz6c4vuJaEpfGqR/sLvW29m9FsC5CoWqTzVXFCGKZ46k7ieIy8DmTsn/glGUqmDBQ1dZZ9VnNcSi4+GYxoM9HtgEetXhqeyiuh0reXP2k5D0AihlinYX8zJPdmwIDAQAB";
        byte[] keybytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keybytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        assertThat(pubKey.getAlgorithm()).isEqualTo("RSA");
    }
}
