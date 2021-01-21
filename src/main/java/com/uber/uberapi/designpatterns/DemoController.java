package com.uber.uberapi.designpatterns;

import com.uber.uberapi.repositories.AccountRepository;
import com.uber.uberapi.repositories.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoController implements CommandLineRunner {
    @Autowired
    DriverRepository driverRepository;

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        //accountRepository.findFirstByUsername("matt");

//        Account acc1 = Account.builder()
//                .username("account3")
//                .password("pass")
//                .build();
//
//        Account acc2 = Account.builder()
//                .username("account4")
//                .password("pass")
//                .build();
//        System.out.println(acc1.getId());
//        accountRepository.save(acc1);
//        System.out.println(acc1.getId());

        //S3Manager s3Manager = S3Manager.getInstance();
        //s3Manager.upload("abc");


    }
}
