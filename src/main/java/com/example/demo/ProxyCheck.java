package com.example.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
public class ProxyCheck {

    @RequestMapping(path = "/**", method = RequestMethod.GET)

    public ResponseEntity<String> apiProxyGet(HttpServletRequest request, HttpServletResponse res) throws UnsupportedEncodingException, InterruptedException {
        // 获取请求url
        String url = request.getRequestURL().toString();
        // 获取url中携带的参数，即/后面的内容
        String query = request.getQueryString();

//        String target = "http://202.204.53.241:8080";
        Server s = new Server();
        String target = s.getBestServer();
//        String target = s.getBestServerRR();
        System.out.println(target);
        System.out.println();
        target = "http://" +  target + ":8080" + url.substring(21);

        // 新的url拼接上旧url后的请求参数
        if (query != null && !query.equals("") && !query.equals("null")) {
            target = target + "?" + query;
        }

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // 获取到请求头
        Enumeration<String> headerNames = request.getHeaderNames();

        HttpHeaders headers = new HttpHeaders();
        Map<String, String> headerMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String header = request.getHeader(headerName);
            headerMap.put(headerName, header);
            headers.add(headerName, header);
        }

        // 构造HttpEntity，新请求会携带本次请求的请求头
        HttpEntity entity = new HttpEntity<String>(headers);

        ResponseEntity<String> response = restTemplate.getForEntity(target, String.class, entity);

        return response;
    }


}
