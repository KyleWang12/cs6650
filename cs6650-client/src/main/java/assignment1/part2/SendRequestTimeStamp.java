package assignment1.part2;

import assignment1.utils.Counter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class SendRequestTimeStamp implements Runnable {

  private String baseUrl;
  private final ThreadLocalRandom random = ThreadLocalRandom.current();
  private static final char[] symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  private final CloseableHttpClient client;
  private Counter counter;
  private Queue<Record> queue;

  public SendRequestTimeStamp(CloseableHttpClient client, Counter counter, String baseUrl, Queue<Record> queue) {
    this.client = client;
    this.counter = counter;
    this.baseUrl = baseUrl;
    this.queue = queue;
  }

  @Override
  public void run() {
    long start = System.currentTimeMillis();
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
    int statusCode = 0;
    CloseableHttpResponse response = null;
    while (allowedTries > 0 && !success) {
      try {
        response = client.execute(post);
        statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 201) {
          success = true;
          counter.increment();
        }
        EntityUtils.consume(response.getEntity());
        response.close();
      } catch (IOException e) {

      }
      allowedTries--;
    }
    long end = System.currentTimeMillis();
    queue.add(new Record(start, "POST", end - start, statusCode));
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