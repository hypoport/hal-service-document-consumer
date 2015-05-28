package org.hypoport.hal.consuming;
import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Callable;

public class HttpClientRetryExecutor {

  public static <T> T executeWithRetryOnNetworkError(Callable<T> request) throws Exception {
    int maxRetries = 3;
    int actualRetries = 1;
    Exception lastException;
    do {
      try {
        return request.call();
      }
      catch (SocketException | SSLException | EOFException se) {
        actualRetries++;
        lastException = se;
      }
    } while (actualRetries <= maxRetries);
    throw lastException;
  }
}
