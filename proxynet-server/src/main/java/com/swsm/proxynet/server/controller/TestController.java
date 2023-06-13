package com.swsm.proxynet.server.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liujie
 * @date 2023-06-13
 */
@RestController
public class TestController {
    
    @Data
    @AllArgsConstructor
    public static class Name {
        public String name;
    }
    
    @GetMapping("/hello2")
    public Name hello2(@RequestParam(value = "name", required = false) String name) {
        return new Name("i am " + (name == null ? "swsm" : name));
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false) String name) {
        return "i am " + (name == null ? "swsm" : name);
    }
    
}
