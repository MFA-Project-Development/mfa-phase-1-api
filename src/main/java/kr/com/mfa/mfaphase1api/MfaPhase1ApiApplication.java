package kr.com.mfa.mfaphase1api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class MfaPhase1ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfaPhase1ApiApplication.class, args);
    }

}
