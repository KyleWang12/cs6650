package servlets;

import static constants.RmqConstants.*;
import bean.Swipe;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import rmqpool.RMQChannelFactory;
import org.apache.commons.pool2.impl.*;
import service.UserService;

@WebServlet(name = "servlets.SwipeServlet", value = "/servlets.SwipeServlet")
public class SwipeServlet extends HttpServlet {

  private static final Gson gson = new Gson();
  private Connection connection;
  private GenericObjectPool<Channel> pool;

  @Override
  public void init() throws ServletException {
    super.init();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(userName);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    factory.setHost(hostName);
    factory.setPort(portNumber);
    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMaxTotal(ON_DEMAND);
    config.setBlockWhenExhausted(true);
    config.setMaxWaitMillis(WAIT_TIME_SECS * 1000);
    try {
      connection = factory.newConnection();
      RMQChannelFactory chanFactory = new RMQChannelFactory (connection);
      pool = new GenericObjectPool<>(chanFactory, config);
    } catch (IOException | TimeoutException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    try {
      pool.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("message: Invalid inputs");
      return;
    }
    String leftorright = isUrlValid(urlPath);
    if (leftorright == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(gson.toJson("message: Invalid inputs url"));
      return;
    }

    try {
      Swipe swipe = (Swipe) gson.fromJson(request.getReader(), Swipe.class);
      if(!isBodyValid(swipe)) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson("message: Invalid inputs body"));
        return;
      }
      sendToRabbitMQ(swipe, leftorright);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.getWriter().write(gson.toJson("message: Swipe successful"));
    }catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private void sendToRabbitMQ(Swipe swipe, String leftorright) throws Exception {
    Channel channel = pool.borrowObject();
    JsonElement jsonElement = gson.toJsonTree(swipe);
    jsonElement.getAsJsonObject().addProperty("leftorright", leftorright);
    channel.basicPublish("", QUEUE_NAME, null, gson.toJson(jsonElement).getBytes(StandardCharsets.UTF_8));
    pool.returnObject(channel);
  }

  private String isUrlValid(final String urlPath) {
    String[] urlParts = urlPath.split("/");
    if(urlParts.length > 2 || urlParts.length < 1) {
      return null;
    }
    if (urlParts[1].equals("left") || urlParts[1].equals("right")) {
      return urlParts[1];
    }
    return null;
  }

  private boolean isBodyValid(final Swipe swipe) {
    if (swipe.getSwiper() == null || swipe.getSwipee() == null || swipe.getComment() == null) {
      return false;
    }
    if (swipe.getSwiper().isEmpty() || swipe.getSwipee().isEmpty() || swipe.getComment().isEmpty()) {
      return false;
    }
    return true;
  }

}
