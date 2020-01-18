package server.MyMemoryAPI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonDeserialize(using = ID.Deserializer.class)
@JsonSerialize(using = ID.Serializer.class)
public class ID {
    public Long integerValue;
    public String stringValue;

    static class Deserializer extends JsonDeserializer<ID> {
        @Override
        public ID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ID value = new ID();
            switch (jsonParser.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                    value.integerValue = jsonParser.readValueAs(Long.class);
                    break;
                case VALUE_STRING:
                    value.stringValue = jsonParser.readValueAs(String.class);
                    break;
                default:
                    throw new IOException("Cannot deserialize ID");
            }
            return value;
        }
    }

    static class Serializer extends JsonSerializer<ID> {
        @Override
        public void serialize(ID obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (obj.integerValue != null) {
                jsonGenerator.writeObject(obj.integerValue);
                return;
            }
            if (obj.stringValue != null) {
                jsonGenerator.writeObject(obj.stringValue);
                return;
            }
            throw new IOException("ID must not be null");
        }
    }
}
