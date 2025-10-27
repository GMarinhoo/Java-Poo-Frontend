package com.br.pdvpostocombustivel_frontend;

import com.br.pdvpostocombustivel_frontend.view.TelaContatoCrud;
import com.br.pdvpostocombustivel_frontend.view.TelaEstoqueCrud;
import com.br.pdvpostocombustivel_frontend.view.TelaPessoaCrud;
import com.br.pdvpostocombustivel_frontend.view.TelaLogin;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import javax.swing.SwingUtilities;
import com.br.pdvpostocombustivel_frontend.view.TelaProdutoCrud;
import javax.swing.UIManager;

@SpringBootApplication
public class FrontendApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    public static void main(String[] args) {
        var context = new SpringApplicationBuilder(FrontendApplication.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            var tela = context.getBean(TelaLogin.class);
            tela.setVisible(true);
        });
    }
}