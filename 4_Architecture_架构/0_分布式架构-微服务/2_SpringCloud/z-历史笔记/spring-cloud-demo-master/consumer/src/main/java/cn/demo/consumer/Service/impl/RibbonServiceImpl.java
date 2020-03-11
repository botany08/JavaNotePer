package cn.demo.consumer.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class RibbonServiceImpl {
	
	@Autowired
	RestTemplate restTemplate;
	
	@HystrixCommand(fallbackMethod="hystrixMe")
	public String testRibbon(String name){
		return restTemplate.getForObject("http://SERVICE0/intf?name="+name, String.class);
	}
	
	public String hystrixMe(String name){
		return name+"ERROR!";
	}
}
