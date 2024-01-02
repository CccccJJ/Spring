package org.example.n01_basic;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

    public static void main(String[] args) {
        System.setProperty("basicPath","n01_basic/basic.xml");

        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:${basicPath}")){

            final SimpleBean bean = context.getBean(SimpleBean.class);

            bean.send();
        }finally {
            System.out.println("end...");
        }
    }

}
