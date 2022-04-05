package org.ndimens.tomcat.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.jms.*;
import javax.servlet.ServletException;
import java.io.IOException;

public class ActiveMqLogValve extends ValveBase {
    private static final Log log = LogFactory.getLog(AccessLogValve.class);
    private String url;
    private String queue = "local";

    public ActiveMqLogValve() throws JMSException {
        super();
        log.info("MQ INIT");
    }

    // TODO: фильтр по типу содержимого (только данные)
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queue);
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            TextMessage requestMessage = session.createTextMessage(request.getRequestURI());
            producer.send(requestMessage);
        } catch (JMSException e) {
            log.error("Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }

        getNext().invoke(request, response);

        if (session == null || producer == null) {
            return;
        }

        try {
            TextMessage responseMessage = session.createTextMessage(response.getContentType());
            producer.send(responseMessage);

            session.close();
            connection.close();
        } catch (JMSException e) {
            log.error("Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }
}
