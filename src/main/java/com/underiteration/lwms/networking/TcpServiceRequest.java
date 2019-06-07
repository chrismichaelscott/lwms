package com.underiteration.lwms.networking;

import com.underiteration.lwms.service.Verbs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import static com.underiteration.lwms.utils.MessageHelper.DELIMIT;

public class TcpServiceRequest {

	private static Logger logger = Logger.getLogger(TcpServiceRequest.class.getCanonicalName());

	public static void sendAsyncRemoteServiceRequest(InetAddress address, Integer servicePort, String serviceName, String jobReference, Integer callbackPort, String payload) throws IOException {

		logger.info(String.format("Sending request to %s at %s:%s, job reference is: %s", serviceName, address, servicePort, jobReference));

		Socket socket = new Socket(address, servicePort);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();

		out.write(Verbs.CALL.name().getBytes());
		out.write(DELIMIT);
		out.write(serviceName.getBytes());
		out.write(DELIMIT);
		out.write(jobReference.getBytes());
		out.write(DELIMIT);
		out.write(callbackPort.toString().getBytes());
		out.write(DELIMIT);
		out.write(payload.getBytes());
		out.flush();

		out.close();
		in.close();

	}

	public static void sendAsyncRemoteServiceResponse(InetAddress address, Integer servicePort, String jobReference, Boolean success, String payload) throws IOException {

		logger.info(String.format("Sending response for job %s to %s:%s", jobReference, address, servicePort));

		Socket socket = new Socket(address, servicePort);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();

		out.write(Verbs.REPLY.name().getBytes());
		out.write(DELIMIT);
		out.write(jobReference.getBytes());
		out.write(DELIMIT);
		out.write((success) ? 0 : 1);
		// TODO: Add back-pressure
		out.write(DELIMIT);
		out.write(payload.getBytes());
		out.flush();

		out.close();
		in.close();

	}

	public static byte[] sendOptionsRequest(InetAddress address, Integer servicePort) throws IOException {

		Socket socket = new Socket(address, servicePort);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();

		out.write(Verbs.OPTIONS.name().getBytes());
		out.flush();

		byte[] response = in.readAllBytes();

		out.close();
		in.close();

		return response;
	}



}
