package assignment1.part1;

import assignment1.utils.Counter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class App {
    private static final int NUM_HTTP_CONNECTIONS = 150;
    private static final int MAX_NUM_THREADS = 150;
    private static final int NUM_REQUESTS = 500000;
    private static final String BASE_URL = "http://35.162.29.14:8080/twinder-sp/swipe/";

    public static void main(String[] args) throws Exception {

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connManager).build();
        connManager.setMaxTotal(NUM_HTTP_CONNECTIONS);
        connManager.setDefaultMaxPerRoute(NUM_HTTP_CONNECTIONS);

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUM_THREADS);
        Counter counter = new Counter();

        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_REQUESTS; i++) {
            executorService.submit(new SendRequest(client, counter, BASE_URL));
        }

        executorService.shutdown();
        executorService.awaitTermination(1000, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        System.out.println("Total Run Time:\t\t\t\t" + (end - start) + "ms");
        System.out.println("Successful Requests:\t" + counter.getCount() + "/" + NUM_REQUESTS);
        System.out.println("Failed Requests:\t\t\t" + (NUM_REQUESTS - counter.getCount()) + "/" + NUM_REQUESTS);
        System.out.println("Request Per Second:\t\t" + NUM_REQUESTS * 1000 / (end - start) + "/sec");
    }
}
