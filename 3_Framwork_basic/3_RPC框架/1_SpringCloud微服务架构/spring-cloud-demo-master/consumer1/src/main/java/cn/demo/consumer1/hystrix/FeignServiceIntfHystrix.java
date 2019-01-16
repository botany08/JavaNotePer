package cn.demo.consumer1.hystrix;

import org.springframework.stereotype.Component;

import cn.demo.consumer1.service.intf.FeignServiceIntf;

@Component
public class FeignServiceIntfHystrix implements FeignServiceIntf {
	@Override
	public String feignSer(String name) {
		return "ERROR!!";
	}
}
