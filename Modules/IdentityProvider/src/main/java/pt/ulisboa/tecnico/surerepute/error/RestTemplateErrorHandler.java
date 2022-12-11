package pt.ulisboa.tecnico.surerepute.error;

import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RestTemplateErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
    return clientHttpResponse.getStatusCode() != HttpStatus.OK;
  }

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
    throw new APIException(
        Response.Status.fromStatusCode(clientHttpResponse.getRawStatusCode()),
        bodyToString(clientHttpResponse.getBody()));
  }

  private String bodyToString(InputStream body) throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
    String line = bufferedReader.readLine();
    while (line != null) {
      builder.append(line).append(System.lineSeparator());
      line = bufferedReader.readLine();
    }
    bufferedReader.close();
    return builder.toString();
  }
}
