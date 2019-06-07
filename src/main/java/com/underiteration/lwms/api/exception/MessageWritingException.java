package com.underiteration.lwms.api.exception;

public class MessageWritingException extends Exception {

	public MessageWritingException(Throwable throwble) {

		super(throwble);
	}

	public MessageWritingException(String message) {

		super(message);
	}
}
