package com.underiteration.lwms.utils;

import com.underiteration.lwms.api.exception.MessageWritingException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.logging.Logger;

import static com.underiteration.lwms.service.LocalServiceRegister.buildServiceDescription;

public class MessageHelper {

	public static final byte DELIMIT = '|';

	private static Logger logger = Logger.getLogger(MessageHelper.class.getCanonicalName());

	public static MessageReader reader(InputStream message) {
		return new MessageReader(message);
	}

	public static MessageWriter writer(OutputStream response) {
		return new MessageWriter(response);
	}

	public static class MessageReader implements Closeable {

		private final InputStream message;

		private MessageReader(InputStream message) {
			this.message = message;
		}

		public Optional<String> nextPart() {

			StringBuffer buffer = new StringBuffer();
			try {
				for (int lastByte = message.read(); lastByte >= 0 && ((char) lastByte) != DELIMIT; lastByte = message.read()) {
					buffer.append(((char) lastByte));
					if (message.available() == 0) break; //TODO: is this needed?
				}

				return Optional.of(buffer.toString());
			} catch (IOException e) {
				return Optional.empty();
			}
		}

		public Optional<String> remaining() {

			try {
				return Optional.of(new String(message.readAllBytes()));
			} catch (IOException e) {
				return Optional.empty();
			}
		}

		public void close() {

			try {
				message.close();
			} catch (IOException e) {
				logger.warning(e.getLocalizedMessage());
			}
		}
	}

	public static class MessageWriter implements Closeable {

		private final OutputStream response;

		private MessageWriter(OutputStream response) {
			this.response = response;
		}

		public void write(byte[] content) throws MessageWritingException {

			try {
				response.write(content);
				response.flush();
			} catch (IOException e) {
				throw new MessageWritingException(e);
			}
		}

		public void close() {

			try {
				response.close();
			} catch (IOException e) {
				logger.warning(e.getLocalizedMessage());
			}
		}

	}

}
