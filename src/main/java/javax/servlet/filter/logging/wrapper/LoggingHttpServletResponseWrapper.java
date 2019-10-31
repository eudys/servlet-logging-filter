package javax.servlet.filter.logging.wrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	private final LoggingServletOutpuStream loggingServletOutpuStream = new LoggingServletOutpuStream();


	public LoggingHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return loggingServletOutpuStream;
	}

	public String getContent() throws IOException
	{
		try {
			baos.flush();
			return baos.toString(UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	private void writeToOriginal(byte[] b) throws IOException
	{
		super.getOutputStream().write(b);
	}

	private void writeToOriginal(int b) throws IOException
	{
		super.getOutputStream().write(b);
	}


	private class LoggingServletOutpuStream extends ServletOutputStream {

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			// not used
		}

		@Override
		public void write(int b) throws IOException
		{
			LoggingHttpServletResponseWrapper.this.baos.write(b);
			LoggingHttpServletResponseWrapper.this.writeToOriginal(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			LoggingHttpServletResponseWrapper.this.baos.write(b);
			LoggingHttpServletResponseWrapper.this.writeToOriginal(b);
		}
	}
}
