package com.underiteration.lwms.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoteServiceDescription {

	@JsonProperty("s") private String serviceName;
	@JsonProperty("i") private String inputTypeHint;
	@JsonProperty("o") private String outputTypeHint;

	public String getServiceName() {

		return serviceName;
	}

	public void setServiceName(String serviceName) {

		this.serviceName = serviceName;
	}

	public String getInputTypeHint() {

		return inputTypeHint;
	}

	public void setInputTypeHint(String inputTypeHint) {

		this.inputTypeHint = inputTypeHint;
	}

	public String getOutputTypeHint() {

		return outputTypeHint;
	}

	public void setOutputTypeHint(String outputTypeHint) {

		this.outputTypeHint = outputTypeHint;
	}
}
