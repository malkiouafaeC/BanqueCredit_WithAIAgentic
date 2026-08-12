package formulAI.project.bank.banqueCredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Point d'entree de l'application "banque-credit".
 * Packaging WAR pour deploiement sur Tomcat externe (DEC-003).
 */
@SpringBootApplication
public class BanqueCreditApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BanqueCreditApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BanqueCreditApplication.class, args);
    }
}

