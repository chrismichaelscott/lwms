package com.underiteration.lwms.service;

import java.net.InetAddress;

public class Node {

	private String id;
	private InetAddress address;
	private Integer servicePort;

	public Node() {}

	public Node(String id, InetAddress address, Integer servicePort) {
		setId(id);
		setAddress(address);
		setServicePort(servicePort);
	}

	public String getId() {

		return id;
	}

	private void setId(String id) {

		this.id = id;
	}

	public InetAddress getAddress() {

		return address;
	}

	private void setAddress(InetAddress address) {

		this.address = address;
	}

	public Integer getServicePort() {

		return servicePort;
	}

	private void setServicePort(Integer servicePort) {

		this.servicePort = servicePort;
	}
}
