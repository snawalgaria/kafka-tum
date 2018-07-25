package de.insights.endec;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class test1 {
public  static void main(String args[]) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    kgen.init(128);
    SecretKey aesKey = kgen.generateKey();

    // Encrypt cipher
    Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
    cipherOutputStream.write("hello".getBytes());
    cipherOutputStream.flush();
    cipherOutputStream.close();
    byte[] encryptedBytes = outputStream.toByteArray();
    System.out.print(encryptedBytes);

    outputStream = new ByteArrayOutputStream();
    ByteArrayInputStream inStream = new ByteArrayInputStream(encryptedBytes);
    Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    IvParameterSpec ivParameterSpec = new IvParameterSpec(aesKey.getEncoded());
    decryptCipher.init(Cipher.DECRYPT_MODE, aesKey, ivParameterSpec);
    CipherInputStream cipherInputStream = new CipherInputStream(inStream, decryptCipher);
    byte[] buf = new byte[1024];
    int bytesRead;
    while ((bytesRead = cipherInputStream.read(buf)) >= 0) {
        outputStream.write(buf, 0, bytesRead);
    }

    System.out.println("Result: " + new String(outputStream.toByteArray()));

}

}
