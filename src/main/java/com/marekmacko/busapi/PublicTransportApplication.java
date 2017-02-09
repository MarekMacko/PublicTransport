package com.marekmacko.busapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
public class PublicTransportApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublicTransportApplication.class, args);
	}

	@PostConstruct
	public void parseNewData() {
		try {
			new HtmlParser().parseStopDepartures("/pasazer/rozklady-jazdy,tabliczka,1,527");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
