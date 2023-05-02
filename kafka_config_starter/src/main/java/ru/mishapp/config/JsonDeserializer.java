package ru.mishapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class JsonDeserializer<T> implements Deserializer<T> {
    
    private final String encoding = StandardCharsets.UTF_8.name();
    private final ObjectMapper mapper;
    private final Class<T> clazz;
    
    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            } else {
                var valueAsString = new String(data, encoding);
                return mapper.readValue(valueAsString, clazz);
            }
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to StringValue", e);
        }
    }
}
