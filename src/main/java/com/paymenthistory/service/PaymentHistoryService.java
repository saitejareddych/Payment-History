package com.paymenthistory.service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymenthistory.model.PaymentHistory;



@Service
public class PaymentHistoryService {

	@Autowired
	private ExternalServices es;
	public List<PaymentHistory> getPaymentHistory(int customerid, int timeperiod) throws IllegalAccessException {
		
		if(timeperiod<=0){
			throw new IllegalAccessException();
		}
		
		//if the time period is in current month
		//then only system A api is called 
		if(LocalDate.now().getDayOfMonth()-timeperiod>0) {
			CompletableFuture<List<PaymentHistory>> listFromSysA =  es.getPaymentHistoryFromSystemA(customerid, timeperiod);
			return Stream.of(listFromSysA).map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList());
		}
		//if the time period is in current month and previous months
		//then both system A and system B api are called
		else if(LocalDate.now().getDayOfMonth()-timeperiod<0) {
			
			CompletableFuture<List<PaymentHistory>> listFromSysA =  es.getPaymentHistoryFromSystemA(customerid, timeperiod);
			CompletableFuture<List<PaymentHistory>> listFromSysB =  es.getPaymentHistoryFromSystemB(customerid, timeperiod);

			return Stream.of(listFromSysA, listFromSysB).map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList());
		}
		return null;
		
	}


}
