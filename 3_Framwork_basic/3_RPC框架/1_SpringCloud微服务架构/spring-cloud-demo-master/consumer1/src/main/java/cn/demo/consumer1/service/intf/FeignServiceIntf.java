package cn.demo.consumer1.service.intf;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.demo.consumer1.hystrix.FeignServiceIntfHystrix;

@FeignClient(value="SERVICE0",fallback=FeignServiceIntfHystrix.class)
public interface FeignServiceIntf {
	@RequestMapping(value="/intf",method=RequestMethod.GET)
	public String feignSer(@RequestParam(value="name") String name);
}
