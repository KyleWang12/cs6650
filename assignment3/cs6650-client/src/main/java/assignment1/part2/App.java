package assignment1.part2;

import assignment1.utils.Counter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicInteger;

public class App {
  private static final int NUM_HTTP_CONNECTIONS = 250;
  private static final int MAX_NUM_THREADS = 250;
  private static final int NUM_REQUESTS = 500000;
  private static final String BASE_URL = "http://WebServerALB-999194550.us-west-2.elb.amazonaws.com:8080/twinder_war/swipe/";

  private static final int NUM_GET_THREADS = 1;
  private static final String[] API_ENDPOINTS = {
      "matches",
      "stats"
  };

  public static void main(String[] args) throws Exception {

    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
    CloseableHttpClient client = HttpClients.custom()
        .setConnectionManager(connManager).build();
    connManager.setMaxTotal(NUM_HTTP_CONNECTIONS);
    connManager.setDefaultMaxPerRoute(NUM_HTTP_CONNECTIONS);

    ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUM_THREADS);
    Counter counter = new Counter();
    Counter getCounter = new Counter();
    ArrayBlockingQueue<Record> queue = new ArrayBlockingQueue<>(NUM_REQUESTS);

    AtomicInteger numGetRequests = new AtomicInteger(0);
    AtomicLongArray latencies = new AtomicLongArray(NUM_REQUESTS);
    AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
    AtomicLong maxLatency = new AtomicLong(Long.MIN_VALUE);
    AtomicBoolean isRunning = new AtomicBoolean(true);

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(NUM_GET_THREADS);

    long start = System.currentTimeMillis();
    for (int i = 0; i < NUM_REQUESTS; i++) {
      executorService.submit(new SendRequestTimeStamp(client, counter, BASE_URL, queue));
    }

    for (int i = 0; i < NUM_GET_THREADS; i++) {
      scheduledExecutorService.scheduleAtFixedRate(new SendGet(client, BASE_URL, API_ENDPOINTS, numGetRequests, latencies, minLatency, maxLatency, isRunning,
          getCounter), 0, 1, TimeUnit.SECONDS);
    }

    executorService.shutdown();
    executorService.awaitTermination(1000, TimeUnit.SECONDS);
    long end = System.currentTimeMillis();

    isRunning.set(false);
    scheduledExecutorService.shutdown();
    scheduledExecutorService.awaitTermination(1000, TimeUnit.SECONDS);

    long sumLatencies = 0;
    int numGets = numGetRequests.get();
    for (int i = 0; i < numGets; i++) {
      sumLatencies += latencies.get(i);
    }
    double meanLatency = (double) sumLatencies / numGets;

    List<Record> data = new ArrayList<>();
    queue.drainTo(data);
    Collections.sort(data, (o1, o2) -> (int) (o1.getLatency() - o2.getLatency()));
    long sum = 0;
    double meanResponseTime = 0;
    long medianResponseTime = 0;
    long minResponseTime = data.get(0).getLatency();
    long maxResponseTime = data.get(data.size() - 1).getLatency();
    long percentile99 = 0;
    int cnt = 0;
    for (Record record : data) {
      meanResponseTime += record.getLatency();
      sum += record.getLatency();
      if (cnt == NUM_REQUESTS / 2) {
        medianResponseTime = record.getLatency();
      }
      // calculate 99th percentile
      if (cnt == NUM_REQUESTS * 99 / 100) {
        percentile99 = record.getLatency();
      }
      cnt++;
    }
    meanResponseTime /= NUM_REQUESTS;

    writeToCsv(data);

    System.out.println("Mean Response Time:\t\t" + String.format("%.2f", meanResponseTime) + "ms");
    System.out.println("Median Response Time:\t" + medianResponseTime + "ms");
    System.out.println("P99 Response Time:\t\t" + percentile99 + "ms");
    System.out.println("Min Response Time:\t\t" + minResponseTime + "ms");
    System.out.println("Max Response Time:\t\t" + maxResponseTime + "ms");
    System.out.println("Request Per Second:\t\t" + NUM_REQUESTS * 1000 / (end - start) + "/sec");

    System.out.println("Total Run Time:\t\t\t\t" + (end - start) + "ms");
    System.out.println("Successful Requests:\t" + counter.getCount() + "/" + NUM_REQUESTS);
    System.out.println("Failed Requests:\t\t\t" + (NUM_REQUESTS - counter.getCount()) + "/" + NUM_REQUESTS);
    System.out.println("\nGetThread Statistics:");
    System.out.println("Min Latency: " + minLatency.get() + "ms");
    System.out.println("Mean Latency: " + String.format("%.2f", meanLatency) + "ms");
    System.out.println("Max Latency: " + maxLatency.get() + "ms");
    System.out.println("Successful Requests:\t" + getCounter.getCount() + "/" + numGetRequests.get());
  }

  private static void writeToCsv(List<Record> data) {
    Collections.sort(data, (o1, o2) -> (int) (o1.getStartTime() - o2.getStartTime()));
    long offset = data.get(0).getStartTime();
    try (FileWriter writer = new FileWriter("record_data.csv")) {
      writer.append("StartTime(s)");
      writer.append(",");
      writer.append("RequestType");
      writer.append(",");
      writer.append("Latency(ms)");
      writer.append(",");
      writer.append("ResponseCode");
      writer.append("\n");
      for (Record record : data) {
        long curTime = record.getStartTime() - offset;
        writer.append(String.valueOf(curTime / 1000));
        writer.append(",");
        writer.append(record.getRequestType());
        writer.append(",");
        writer.append(String.valueOf(record.getLatency()));
        writer.append(",");
        writer.append(String.valueOf(record.getResponseCode()));
        writer.append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    Map<Long, Long> plotData = new HashMap<>();
    for (Record record : data) {
      long curTime = record.getStartTime() - offset + data.get(0).getLatency();
      long curSec = curTime / 1000;
      if (plotData.containsKey(curSec)) {
        plotData.put(curSec, plotData.get(curSec) + 1);
      } else {
        plotData.put(curSec, 1L);
      }
    }

    try (FileWriter writer = new FileWriter("plot_performance.csv")) {
      writer.append("Time(s)");
      writer.append(",");
      writer.append("NumRequests");
      writer.append("\n");
      for (Map.Entry<Long, Long> entry : plotData.entrySet()) {
        writer.append(String.valueOf(entry.getKey()));
        writer.append(",");
        writer.append(String.valueOf(entry.getValue()));
        writer.append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

}
