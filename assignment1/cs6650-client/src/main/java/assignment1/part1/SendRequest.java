package assignment1.part1;

import assignment1.utils.Counter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class SendRequest implements Runnable {

  private String baseUrl;
  private final ThreadLocalRandom random = ThreadLocalRandom.current();
  private static final char[] symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  private final CloseableHttpClient client;
  private Counter counter;

  public SendRequest(CloseableHttpClient client, Counter counter, String baseUrl) {
    this.client = client;
    this.counter = counter;
    this.baseUrl = baseUrl;
  }

  @Override
  public void run() {
    HttpPost post = new HttpPost();
    StringBuilder sb = new StringBuilder();
    int leftOrRight = random.nextInt() & 1;
    if (leftOrRight == 0)
      sb.append("left");
    else
      sb.append("right");
    post.setURI(URI.create(baseUrl + sb.toString()));
    String json = String.format("{\"swiper\":\"%d\",\"swipee\":\"%d\",\"comment\":\"%s\"}", random.nextInt(1, 5000),
        random.nextInt(1, 1000000), getRandomString(10));
    try {
      post.setEntity(new StringEntity(json));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    post.setHeader("Content-type", "application/json");
    int allowedTries = 5;
    boolean success = false;
    CloseableHttpResponse response = null;
    while (allowedTries > 0 && !success) {
      try {
        response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == 201) {
          success = true;
          counter.increment();
        } else {
          System.out.println(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        }
        EntityUtils.consume(response.getEntity());
        response.close();
      } catch (IOException e) {

      }
      allowedTries--;
    }
  }

  private String getRandomString(int length) {
    char buf[] = new char[length];
    for (int i = 0; i < buf.length; i++) {
      int index = random.nextInt(symbols.length);
      buf[i] = symbols[index];
    }
    return new String(buf);
  }

}