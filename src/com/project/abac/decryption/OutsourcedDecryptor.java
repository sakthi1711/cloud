package com.project.abac.decryption;

import com.project.abac.cloudsim.CloudSimManager;
import com.project.abac.encryption.Ciphertext;

import java.util.Set;

public class OutsourcedDecryptor {

    public long decrypt(Ciphertext ct, Set<String> userAttrs) {

        System.out.println("\n[OUTSOURCED DECRYPTION]");

        long cloudTime =
                CloudSimManager.executePartialDecryption(ct.getGroupCount());

        long start = System.currentTimeMillis();
        boolean allowed = ct.getPolicy().evaluate(userAttrs);
        long end = System.currentTimeMillis();

        long userTime = end - start;

        System.out.println("[DECRYPTION] User-side time: " + userTime + " ms");
        System.out.println("[DECRYPTION] Policy satisfied: " + allowed);

        return cloudTime + userTime;
    }
}
