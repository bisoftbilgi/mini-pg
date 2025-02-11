package com.bisoft.minipg.helper;


import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@RequiredArgsConstructor
public class SymmetricEncryptionUtil {

    @Value("${bfm.approval-key:B1s0ft25}")
    public String approvalSecretKey;

    @Value("${bfm.user-crypted:false}")
    public boolean isEncrypted;

    static final int EXPIRE_DAYS_LATER = 365;

    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH      = 128;
    private static final int MIN_NONCE_SIZE  = 12;
    private static final int MAX_NONCE_SIZE  = 16;

    // dynamically generated iv vector is used as salt
    public String decrypt(final String strToDecrypt) {
        if (!isEncrypted) {
            return strToDecrypt;
        }

        try {
            // Wrap the data into a byte buffer to ease the reading process
            final ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(strToDecrypt));

            final int nonceSize = byteBuffer.getInt();

            // Make sure that the file was encrypted properly
            if (nonceSize < MIN_NONCE_SIZE || nonceSize >= MAX_NONCE_SIZE) {
                throw new IllegalArgumentException(
                        "Nonce size is incorrect. Make sure that the incoming data is an AES encrypted");
            }
            final byte[] iv = new byte[nonceSize];
            byteBuffer.get(iv);

            // Prepare your key/password
            final SecretKey secretKey = generateSecretKey(iv, this.approvalSecretKey);

            // get the rest of encrypted data
            final byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            final Cipher     cipher        = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(KEY_LENGTH, iv);

            // Encryption mode on!
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Encrypt the data
            return new String(cipher.doFinal(cipherBytes), UTF_8);
        } catch (Exception e) {
            log.error("Error while decrypting: ", e);
        }

        return null;
    }

    public String encrypt(final String strToEncrypt) {

        try {
            // Prepare the nonce
            final SecureRandom secureRandom = new SecureRandom();

            // Nonce should be 12 bytes
            final byte[] iv = new byte[MIN_NONCE_SIZE];
            secureRandom.nextBytes(iv);

            // Prepare your key/password
            final SecretKey secretKey = generateSecretKey(iv, approvalSecretKey);
            //System.out.println("Approval Key:" + approvalSecretKey);

            final Cipher           cipher        = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec parameterSpec = new GCMParameterSpec(KEY_LENGTH, iv);

            // Encryption mode on!
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt the data
            final byte[] encryptedData = cipher.doFinal(strToEncrypt.getBytes(UTF_8));

            // Concatenate everything and return the final data
            final ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Error while encrypting: ", e);
        }

        return null;
    }

    private SecretKey generateSecretKey(final byte[] iv, final String approvalSecretKey) throws Exception {

        KeySpec spec = new PBEKeySpec(
                approvalSecretKey.toCharArray(),
                iv,
                ITERATION_COUNT,
                KEY_LENGTH); // AES-128

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[]           key              = secretKeyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
}