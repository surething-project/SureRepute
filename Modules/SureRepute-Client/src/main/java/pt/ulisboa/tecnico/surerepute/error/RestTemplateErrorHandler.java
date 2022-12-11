package pt.ulisboa.tecnico.surerepute.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import pt.ulisboa.tecnico.surerepute.SureReputeClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(SureReputeClient.class.getName());

  @Override
  public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
    return (clientHttpResponse.getStatusCode().series() == CLIENT_ERROR
        || clientHttpResponse.getStatusCode().series() == SERVER_ERROR);
  }

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
    throw new SureReputeClientException(
        "Error received with status: "
            + clientHttpResponse.getStatusText()
            + "and responseBody "
            + bodyToString(clientHttpResponse.getBody()));
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
