package cn.demo.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.demo.consumer.Service.impl.RibbonServiceImpl;

@RestController
public class RibbonController {
	@Autowired
	RibbonServiceImpl ribbonServiceImpl;
	
	@RequestMapping(value="/rintf",method=RequestMethod.GET)
	public String ribo(@RequestParam String name){
		return ribbonServiceImpl.testRibbon(name);
	}
}
