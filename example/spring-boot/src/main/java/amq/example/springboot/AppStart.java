package amq.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author leeon
 */
@ComponentScan(basePackages = "com.artlongs")
@SpringBootApplication
public class AppStart {

    /**
     * 启动项目前.请先运行MQ 服务器: {@link com.artlongs.amq.core.AioMqServer}
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        SpringApplication.run(AppStart.class, args);
    }

}



