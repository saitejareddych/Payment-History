package com.paymenthistory.service;

import com.paymenthistory.model.PaymentHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ExternalServices {

    @Autowired
    private WebClient webclient;
    @Value("${servicea.url}")
    private String serviceAurl;

    @Value("${serviceb.url}")
    private String serviceBurl;

    @Async
    public CompletableFuture<List<PaymentHistory>> getPaymentHistoryFromSystemB(int customerid, int timeperiod){

        LocalDate fromDateService1=LocalDate.now().minusDays(LocalDate.now().getDayOfMonth()-1);
        LocalDate toDateToService2 = fromDateService1.minusDays(1);
        int daysTosubract = timeperiod - LocalDate.now().getDayOfMonth();
        LocalDate fromDateService2 = toDateToService2.minusDays(daysTosubract);
        System.out.println(Thread.currentThread().getName());
        Map<String,String> urlVariablesForSystem2=new HashMap<>();

        //set urlVariables for system B
        urlVariablesForSystem2.put("id", String.valueOf(customerid));
        urlVariablesForSystem2.put("fromDate", fromDateService2.toString());
        urlVariablesForSystem2.put("toDate", toDateToService2.toString());

        List<PaymentHistory> paymentHistoryPrevMonths =	webclient.method(HttpMethod.GET).uri(serviceBurl, urlVariablesForSystem2).retrieve().bodyToMono(new ParameterizedTypeReference<List<PaymentHistory>>() {
        }).block();


        return CompletableFuture.completedFuture(paymentHistoryPrevMonths);

    }

    @Async
    public CompletableFuture<List<PaymentHistory>> getPaymentHistoryFromSystemA(int customerid, int timeperiod){

        LocalDate fromDateService1= LocalDate.now().minusDays(timeperiod);
        Map<String,String> urlVariables=new HashMap<>();
        System.out.println(Thread.currentThread().getName());
        //set urlVariables for system A
        urlVariables.put("id", String.valueOf(customerid));
        urlVariables.put("fromDate", fromDateService1.toString());

        LinkedMultiValueMap<String, String> hdr=new LinkedMultiValueMap<>();


        hdr.set(HttpHeaders.ACCEPT, MediaType.toString(Arrays.asList(MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML)));


        List<PaymentHistory> paymentHistory =	webclient.method(HttpMethod.GET).uri(serviceAurl, urlVariables).headers(headers->headers.addAll(headers)).  retrieve().bodyToMono(new ParameterizedTypeReference<List<PaymentHistory>>() {
        }).block();


        return CompletableFuture.completedFuture(paymentHistory);
    }

}
