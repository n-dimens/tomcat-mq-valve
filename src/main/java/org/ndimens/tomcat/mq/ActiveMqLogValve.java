package org.ndimens.tomcat.mq;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

public class ActiveMqLogValve extends ValveBase {
    // TOOD: url в server.xml
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("[MQ] Input: " + request.getRequestURI());

        getNext().invoke(request, response);

        System.out.println("[MQ] Output: " + response.getContentType());
    }
}
