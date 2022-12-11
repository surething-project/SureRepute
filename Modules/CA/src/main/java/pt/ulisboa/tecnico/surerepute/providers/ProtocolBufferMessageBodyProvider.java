package pt.ulisboa.tecnico.surerepute.providers;

import com.google.protobuf.Message;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static pt.ulisboa.tecnico.surerepute.CAServer.APPLICATION_PROTOBUF;

/**
 * A Jersey provider which enables using Protocol Buffers to parse request entities into objects and
 * generate response entities from objects.
 */
@Provider
@Consumes({APPLICATION_PROTOBUF})
@Produces({APPLICATION_PROTOBUF})
public class ProtocolBufferMessageBodyProvider
    implements MessageBodyReader<Message>, MessageBodyWriter<Message> {

  private final Map<Class<Message>, Method> methodCache = new ConcurrentHashMap<>();

  @Override
  public boolean isReadable(
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType) {
    return Message.class.isAssignableFrom(type);
  }

  @Override
  public Message readFrom(
      final Class<Message> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, String> httpHeaders,
      final InputStream entityStream)
      throws IOException {

    final Method newBuilder =
        methodCache.computeIfAbsent(
            type,
            t -> {
              try {
                return t.getMethod("newBuilder");
              } catch (Exception e) {
                return null;
              }
            });

    final Message.Builder builder;
    try {
      assert newBuilder != null;
      builder = (Message.Builder) newBuilder.invoke(type);
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }

    return builder.mergeFrom(entityStream).build();
  }

  @Override
  public long getSize(
      final Message m,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType) {

    return m.getSerializedSize();
  }

  @Override
  public boolean isWriteable(
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType) {
    return Message.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(
      final Message m,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders,
      final OutputStream entityStream)
      throws IOException {
    m.writeTo(entityStream);
  }
}
