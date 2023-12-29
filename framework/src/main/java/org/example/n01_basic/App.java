package org.example.n01_basic;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Objects;

public class App {

    public static void main(String[] args) {
        System.setProperty("basicPath","n01_basic/basic.xml");
        ClassPathXmlApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext("classpath:${basicPath}");
            final SimpleBean bean = context.getBean(SimpleBean.class);

            bean.send();
        } finally {
            if (Objects.nonNull(context)) {
                context.close();
            }
        }
    }

}
