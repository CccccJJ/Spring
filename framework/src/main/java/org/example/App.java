package org.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:services.xml");

        Object aService = context.getBean("aService");
        System.out.println(aService);


        context = new ClassPathXmlApplicationContext("classpath:backup/services.xml");

        aService = context.getBean("aService");
        System.out.println(aService);
    }
}
