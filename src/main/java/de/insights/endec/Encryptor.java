package de.insights.endec;
import org.apache.kafka.common.serialization.Serializer;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Encryptor<T> extends Serializer<T> {

        byte[] encrypt(String topic, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException;

        byte[] encrypt2(String topic, byte[] data);

    }


