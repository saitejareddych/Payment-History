package com.paymenthistory.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
	private WebClient webclient;
	
	@Value("${servicea.url}")
	private String serviceAurl;
	
	@Value("${serviceb.url}")
	private String serviceBurl;
	
	

	public List<PaymentHistory> getPaymentHistory(int customerid, int timeperiod) throws IllegalAccessException {
		
		if(timeperiod<=0){
			throw new IllegalAccessException();
		}
		
		//if the time period is in current month
		//then only system A api is called 
		if(LocalDate.now().getDayOfMonth()-timeperiod>0) {
			
			return getPaymentHistoryFromSystemA(customerid, timeperiod);
			
		}
		//if the time period is in current month and previous months
		//then both system A and system B api are called
		else if(LocalDate.now().getDayOfMonth()-timeperiod<0) {
			
			return getPaymentHistoryFromAllSystems(customerid, timeperiod);
		}
		return null;
		
	}
	
	private List<PaymentHistory> getPaymentHistoryFromAllSystems(int customerid, int timeperiod){
		
		LocalDate fromDateService1=LocalDate.now().minusDays(LocalDate.now().getDayOfMonth()-1);
		LocalDate toDateToService2 = fromDateService1.minusDays(1);
		int daysTosubract = timeperiod - LocalDate.now().getDayOfMonth();
		LocalDate fromDateService2 = toDateToService2.minusDays(daysTosubract);
		
		Map<String,String> urlVariablesForSystem1=new HashMap<>();
		
		//set urlVariables for system A 
		urlVariablesForSystem1.put("id", String.valueOf(customerid));
		urlVariablesForSystem1.put("fromDate", fromDateService1.toString());
		
		List<PaymentHistory> paymentHistoryCurrentMonth = webclient.method(HttpMethod.GET).uri(serviceAurl, urlVariablesForSystem1).retrieve().bodyToMono(new ParameterizedTypeReference<List<PaymentHistory>>() {
		}).block();
		
		Map<String,String> urlVariablesForSystem2=new HashMap<>();
		
		//set urlVariables for system B
		urlVariablesForSystem2.put("id", String.valueOf(customerid));
		urlVariablesForSystem2.put("fromDate", fromDateService2.toString());
		urlVariablesForSystem2.put("toDate", toDateToService2.toString());
		
		List<PaymentHistory> paymentHistoryPrevMonths =	webclient.method(HttpMethod.GET).uri(serviceBurl, urlVariablesForSystem2).retrieve().bodyToMono(new ParameterizedTypeReference<List<PaymentHistory>>() {
		}).block();
		
		paymentHistoryCurrentMonth.addAll(paymentHistoryPrevMonths);
		
		return paymentHistoryCurrentMonth;
		
	}
	
	private List<PaymentHistory> getPaymentHistoryFromSystemA(int customerid, int timeperiod){
		
		LocalDate fromDateService1= LocalDate.now().minusDays(timeperiod);
		Map<String,String> urlVariables=new HashMap<>();
		
		//set urlVariables for system A 
		urlVariables.put("id", String.valueOf(customerid));
		urlVariables.put("fromDate", fromDateService1.toString());
		
		LinkedMultiValueMap<String, String> hdr=new LinkedMultiValueMap<>();
		
		
		hdr.set(HttpHeaders.ACCEPT, MediaType.toString(Arrays.asList(MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML)));
		
		
		List<PaymentHistory> paymentHistory =	webclient.method(HttpMethod.GET).uri(serviceAurl, urlVariables).headers(headers->headers.addAll(headers)).  retrieve().bodyToMono(new ParameterizedTypeReference<List<PaymentHistory>>() {
		}).block();
		
		
		return paymentHistory;
	}

}
