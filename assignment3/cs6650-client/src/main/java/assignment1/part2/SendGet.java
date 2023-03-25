package assignment1.part2;

import assignment1.utils.Counter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;


public class SendGet implements Runnable{
  private final CloseableHttpClient client;
  private final String baseUrl;
  private final String[] apiEndpoints;
  private final AtomicInteger numGetRequests;
  private final AtomicLongArray latencies;
  private final AtomicLong minLatency;
  private final AtomicLong maxLatency;
  private final AtomicBoolean isRunning;
  private final Random random;
  private final Counter counter;
  private static final int GET_REQUESTS_PER_SECOND = 5;

  SendGet(CloseableHttpClient client, String baseUrl, String[] apiEndpoints, AtomicInteger numGetRequests, AtomicLongArray latencies, AtomicLong minLatency, AtomicLong maxLatency, AtomicBoolean isRunning,
      Counter counter) {
    this.client = client;
    this.baseUrl = baseUrl;
    this.apiEndpoints = apiEndpoints;
    this.numGetRequests = numGetRequests;
    this.latencies = latencies;
    this.minLatency = minLatency;
    this.maxLatency = maxLatency;
    this.isRunning = isRunning;
    this.counter = counter;
    this.random = new Random();
  }

  @Override
  public void run() {
    if (!isRunning.get()) {
      return;
    }
    for (int i = 0; i < GET_REQUESTS_PER_SECOND; i++) {
      int apiIndex = random.nextInt(apiEndpoints.length);
      int userId = random.nextInt(5000);
      String apiUrl = baseUrl + apiEndpoints[apiIndex] + "/" + userId;
      long startTime = System.currentTimeMillis();
      int responseCode = sendGetRequest(apiUrl);
      long endTime = System.currentTimeMillis();
      long latency = endTime - startTime;

      if (responseCode >= 200 && responseCode < 300) {
        counter.increment();
        int index = numGetRequests.getAndIncrement();
        latencies.set(index, latency);
        minLatency.updateAndGet(prev -> Math.min(prev, latency));
        maxLatency.updateAndGet(prev -> Math.max(prev, latency));
      }
    }
  }

  private int sendGetRequest(String apiUrl) {
    int responseCode = -1;
    try {
      HttpGet httpGet = new HttpGet(apiUrl);
      HttpResponse response = client.execute(httpGet);
      responseCode = response.getStatusLine().getStatusCode();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return responseCode;
  }
}
