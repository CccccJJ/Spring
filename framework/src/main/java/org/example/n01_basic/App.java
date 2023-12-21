package org.example.n01_basic;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Objects;

public class App {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext("n01_basic/basic.xml");
            final SimpleBean bean = context.getBean(SimpleBean.class);

            bean.send();
        } finally {
            if (Objects.nonNull(context)) {
                context.close();
            }
        }
    }

}
