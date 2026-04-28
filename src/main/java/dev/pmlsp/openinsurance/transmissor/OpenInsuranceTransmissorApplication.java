package dev.pmlsp.openinsurance.transmissor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpenInsuranceTransmissorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenInsuranceTransmissorApplication.class, args);
    }
}
