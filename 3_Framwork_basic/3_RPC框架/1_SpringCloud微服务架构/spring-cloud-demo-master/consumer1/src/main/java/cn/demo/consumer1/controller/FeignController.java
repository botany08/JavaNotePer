package cn.demo.consumer1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.demo.consumer1.service.intf.FeignServiceIntf;

@RestController
public class FeignController {
	@Autowired
	FeignServiceIntf feignServiceIntf;
	
	@RequestMapping(value="/feignintf",method=RequestMethod.GET)
	public String feignCon(@RequestParam String name){
		return feignServiceIntf.feignSer(name);
	}
}
