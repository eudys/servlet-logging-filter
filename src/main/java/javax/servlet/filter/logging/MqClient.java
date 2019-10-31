package javax.servlet.filter.logging;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqClient
{
	private static final int NUM_THREADS = 5;

	private static MqClient instance;
	private ExecutorService threadPool;
	private Connection connection;

	private MqClient() throws JMSException
	{
		threadPool = Executors.newFixedThreadPool(NUM_THREADS);
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		// Create a Connection
		connection = connectionFactory.createConnection();
		connection.start();
	}

	public static MqClient getInstance() throws JMSException
	{
		if(instance == null){
			instance = new MqClient();
		}
		return instance;
	}

	public void send(String text)
	{
		threadPool.execute(new MessageSender(connection, text));
			//connection.close();
	}

	public void cleanup()
	{
		try
		{
			connection.close();
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	}
}
