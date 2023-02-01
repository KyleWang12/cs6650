package servlets;

import bean.SwipeDetail;
import com.google.gson.Gson;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "servlets.SwipeServlet", value = "/servlets.SwipeServlet")
public class SwipeServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    Gson gson = new Gson();

    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("message: Invalid inputs");
      return;
    }
    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(gson.toJson("message: Invalid inputs"));
      return;
    }

    try {
      SwipeDetail swipeDetail = (SwipeDetail) gson.fromJson(request.getReader(), SwipeDetail.class);
//      if(!isBodyValid(swipeDetail)) {
//        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//        response.getWriter().write(gson.toJson("message: Invalid inputs"));
//        return;
//      }
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.getWriter().write(gson.toJson("message: Swipe successful"));
    }catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }




  }

  private boolean isUrlValid(final String[] urlParts) {
    // validate the request url path according to the API spec
    // urlPath  = "/{left/right}"
    // urlParts = [, left]

    if (urlParts[1].equals("left") || urlParts[1].equals("right")) {
      return true;
    }
    return urlParts.length == 2;
  }

  private boolean isBodyValid(final Object bean) {
    // validate the request body according to the API spec
    try{
      for(PropertyDescriptor propertyDescriptor :
          Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors()){

        final Method readMethod = propertyDescriptor.getReadMethod();
        if (readMethod != null) {
          String res = (String) readMethod.invoke(bean);
          if (res == null || res.isEmpty()) {
            return false;
          }
        }
      }
    }catch (Exception e) {

    }
    return true;
  }

}
