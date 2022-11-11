package com.example.demo;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
@EnableAutoConfiguration
public class RedisTemplateTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "node1:cpu";
        redisTemplate.opsForValue().set(redisKey, 99);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }
}
