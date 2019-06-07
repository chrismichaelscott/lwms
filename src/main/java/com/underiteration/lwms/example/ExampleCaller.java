package com.underiteration.lwms.example;

import com.underiteration.lwms.service.RemoteServiceRegister;
import com.underiteration.lwms.service.Runner;

import static com.underiteration.lwms.service.ServiceCaller.call;

public class ExampleCaller extends Runner {

	public static void main(String[] args) {

		start();

		RemoteServiceRegister.subscribeToServices(name -> {
			if ("capitalize".equals(name)) {

				call("capitalize", "Mixed Case")
						.thenAccept(System.out::println)
						.thenRun(() -> System.exit(0));
			}
		});
	}

}
