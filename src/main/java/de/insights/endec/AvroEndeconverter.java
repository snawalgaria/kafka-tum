package de.insights.endec;

import io.confluent.connect.avro.AvroConverter;
import io.confluent.connect.avro.AvroConverterConfig;
import io.confluent.connect.avro.AvroData;
import io.confluent.connect.avro.AvroDataConfig;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerializer;
import io.confluent.kafka.serializers.NonRecordContainer;
import java.util.Map;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.IndexedRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.storage.Converter;

public class AvroEndeconverter extends AvroConverter implements Converter {
    private SchemaRegistryClient schemaRegistry;
    private AvroEndeconverter.Serializer serializer;
    private AvroEndeconverter.Deserializer deserializer;
    private boolean isKey;
    private AvroData avroData;
    @Override
    public void configure(Map<String, ?> configs, boolean b) {
        this.isKey = isKey;
        AvroConverterConfig avroConverterConfig = new AvroConverterConfig(configs);
        if (this.schemaRegistry == null) {
            this.schemaRegistry = new CachedSchemaRegistryClient(avroConverterConfig.getSchemaRegistryUrls(), avroConverterConfig.getMaxSchemasPerSubject());
        }

        this.serializer = new AvroEndeconverter.Serializer(this.schemaRegistry, avroConverterConfig.autoRegisterSchema());
        this.deserializer = new AvroEndeconverter.Deserializer(this.schemaRegistry);
        this.avroData = new AvroData(new AvroDataConfig(configs));
    }

    @Override
    public byte[] fromConnectData(String topic, Schema schema, Object value) {
        try {
            return this.serializer.serialize(topic, this.isKey, this.avroData.fromConnectData(schema, value));
        } catch (SerializationException var5) {
            String var10002 = "Failed to serialize Avro data from topic %s :";
            throw new DataException(String.format(topic), var5);
        }
    }

    @Override
    public SchemaAndValue toConnectData(String topic, byte[] value) {
        String var10002;
        try {
            GenericContainer deserialized = this.deserializer.deserialize(topic, this.isKey, value);
            if (deserialized == null) {
                return SchemaAndValue.NULL;
            } else if (deserialized instanceof IndexedRecord) {
                return this.avroData.toConnectData(deserialized.getSchema(), deserialized);
            } else if (deserialized instanceof NonRecordContainer) {
                return this.avroData.toConnectData(deserialized.getSchema(), ((NonRecordContainer)deserialized).getValue());
            } else {
                var10002 = "Unsupported type returned during deserialization of topic %s ";
                throw new DataException(String.format(topic));
            }
        } catch (SerializationException var4) {
            var10002 = "Failed to deserialize data for topic %s to Avro: ";
            throw new DataException(String.format(topic), var4);
        }
    }
    private static class Deserializer extends AbstractKafkaAvroDeserializer {
        public Deserializer(SchemaRegistryClient client) {
            this.schemaRegistry = client;
        }

        public GenericContainer deserialize(String topic, boolean isKey, byte[] payload) {
            byte[] decrypted = decrypt(payload);
            return this.deserializeWithSchemaAndVersion(topic, isKey, decrypted);
        }
        public byte[] decrypt(byte[] data) {

            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (data[i] - 2);
            }
            return data;

        }
    }

    private static class Serializer extends AbstractKafkaAvroSerializer {
        public Serializer(SchemaRegistryClient client, boolean autoRegisterSchema) {
            this.schemaRegistry = client;
            this.autoRegisterSchema = autoRegisterSchema;
        }

        public byte[] serialize(String topic, boolean isKey, Object value) {
            byte bytes[]=this.serializeImpl(getSubjectName(topic, isKey), value);
            byte encrypted[] =encrypt(topic,bytes);
            return encrypted;

        }
        public byte[] encrypt(String topic, byte[] serialized) {

            for (int i = 0; i < serialized.length; i++) {
                serialized[i] = (byte) (serialized[i] + 2);
            }
            return serialized;
        }
    }
}
