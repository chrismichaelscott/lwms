package com.underiteration.lwms.api.exception;

public class JsonMarshallingException extends Throwable {

	public JsonMarshallingException(String message) {

		super(message);
	}

	public JsonMarshallingException(Throwable throwable) {

		super(throwable);
	}
}
