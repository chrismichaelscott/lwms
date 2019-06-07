package com.underiteration.lwms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.underiteration.lwms.api.exception.MessageFormatException;
import com.underiteration.lwms.api.exception.MessageWritingException;
import com.underiteration.lwms.jobpool.PendingRemoteJobs;
import com.underiteration.lwms.jobpool.PoolManager;
import com.underiteration.lwms.utils.MessageHelper;
import com.underiteration.lwms.utils.MessageHelper.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static com.underiteration.lwms.service.LocalServiceRegister.buildServiceDescription;

public class RequestHandler implements Runnable {

	private static Logger logger = Logger.getLogger(ServiceDiscoveryListener.class.getCanonicalName());

	private final MessageReader message;
	private final MessageWriter response;
	private final InetAddress callerAddress;

	private ExecutorService pool = PoolManager.getDefaultPool();

	public RequestHandler(Socket socket) throws IOException {
		this.callerAddress = socket.getInetAddress();
		this.message = MessageHelper.reader(socket.getInputStream());
		this.response = MessageHelper.writer(socket.getOutputStream());
	}

	@Override
	public void run() {

		String verb = message.nextPart().get();

		switch (Verbs.valueOf(verb)) {
			case OPTIONS:

				handleOptionsRequest();

				break;

			case CALL:

				try {
					handleCallRequest();
				} catch (MessageFormatException e) {
					logger.warning(e.getLocalizedMessage());
				}

				break;

			case REPLY:

				try {
					handleReplyRequest();
				} catch (MessageFormatException e) {
					logger.warning(e.getLocalizedMessage());
				}

				break;
		}

		message.close();
		response.close();

	}

	private void handleOptionsRequest() {

		logger.info("Handling OPTIONS request");

		ObjectMapper mapper = new ObjectMapper();
		try {
			response.write(mapper.writeValueAsBytes(buildServiceDescription()));
		} catch (Exception e) {
			logger.warning(e.getLocalizedMessage());
		}
	}

	private void handleCallRequest() throws MessageFormatException {

		String target = message.nextPart().orElseThrow(() -> new MessageFormatException("Message did not include a target"));

		String jobReference = message.nextPart().orElseThrow(() -> new MessageFormatException("Message did not include a job reference"));

		String callbackPort = message.nextPart().orElseThrow(() -> new MessageFormatException("Message did not include a callback port"));

		String payload = message.remaining().orElse("");

		logger.info(String.format("Handling CALL request to %s with reference %s", target, jobReference));

		pool.execute(new ServiceRunner(callerAddress, Integer.valueOf(callbackPort.toString()), target.toString(), jobReference.toString(), payload));
	}

	private void handleReplyRequest() throws MessageFormatException {

		String jobReference = message.nextPart().orElseThrow(() -> new MessageFormatException("Message did not include a job reference"));

		String success = message.nextPart().orElseThrow(() -> new MessageFormatException("Message did not include a success value"));

		String payload = message.remaining().orElse("");

		logger.info(String.format("Handling REPLY request with reference %s", jobReference));

		PendingRemoteJobs.instance().resolveFuture(jobReference, payload);
	}
}
