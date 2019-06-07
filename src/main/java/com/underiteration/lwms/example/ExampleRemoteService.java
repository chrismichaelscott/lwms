package com.underiteration.lwms.example;

import com.underiteration.lwms.service.Runner;
import com.underiteration.lwms.service.Service;

import static com.underiteration.lwms.service.LocalServiceRegister.registerService;

public class ExampleRemoteService extends Runner {

	public static String capitalize(String in) {
		return in.toUpperCase();
	}

	public static void main(String[] args) {

		registerService(new Service<>("capitalize", String.class, String.class, ExampleRemoteService::capitalize));
		start();
	}

}
