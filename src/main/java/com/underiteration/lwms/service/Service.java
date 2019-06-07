package com.underiteration.lwms.service;

import java.util.function.Function;

public class Service<T, U> {

	private final String name;
	private final Class<T> inputType;
	private final Class<U> outputType;
	private final Function<T, U> function;

	public Service(String name, Class<T> inputType, Class<U> outputType, Function<T, U> function) {
		this.name = name;
		this.inputType = inputType;
		this.outputType = outputType;
		this.function = function;
	}

	public String getName() {

		return name;
	}

	public Class<T> getInputType() {

		return inputType;
	}

	public Class<U> getOutputType() {

		return outputType;
	}

	public Function<T, U> getFunction() {

		return function;
	}
}
