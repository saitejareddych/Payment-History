package com.paymenthistory.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymenthistory.model.PaymentHistory;
import com.paymenthistory.service.PaymentHistoryService;

@RestController
@RequestMapping("/paymenthistory")
public class PaymentHistoryController {
	
	@Autowired
	private PaymentHistoryService paymentHistoryService;
	
	@GetMapping("/getpaymenthistory/{id}/{date}")
	public List<PaymentHistory> getPaymentHistory(@PathVariable("id") int customerId, @PathVariable("date")int  timeperiod) throws IllegalAccessException{
		
		return paymentHistoryService.getPaymentHistory(customerId, timeperiod);
		
	}

}
