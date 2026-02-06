package com.project.abac.decryption;

import com.project.abac.encryption.Ciphertext;
import java.util.Set;

public class TraditionalDecryptor {

    public boolean decrypt(Ciphertext ct, Set<String> userAttrs) {
        return ct.getPolicy().evaluate(userAttrs);
    }
}
