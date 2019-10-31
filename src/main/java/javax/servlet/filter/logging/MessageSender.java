package javax.servlet.filter.logging;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class MessageSender implements Runnable
{
	Connection connection;
	private String text;

	public MessageSender(Connection connection, String text)
	{
		this.connection = connection;
		this.text = text;
	}

	@Override public void run()
	{
		try
		{
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// Create the destination (Topic or Queue)
			Destination destination = session.createQueue("SOLID.LOGS");

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a messages
			TextMessage message = session.createTextMessage(text);

			// Tell the producer to send the message
			//System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
			producer.send(message);


			// Clean up
			session.close();
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	}
}
