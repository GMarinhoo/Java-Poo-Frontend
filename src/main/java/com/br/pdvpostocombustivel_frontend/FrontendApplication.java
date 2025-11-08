package com.br.pdvpostocombustivel_frontend;

import com.br.pdvpostocombustivel_frontend.view.TelaLogin;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

@SpringBootApplication
public class FrontendApplication {

    public static void main(String[] args) {

        try {
            FlatDraculaIJTheme.setup();
            UIManager.put("Button.arc", 14);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusWidth", 1);

        } catch (Exception ex) {
            System.err.println("Falha ao aplicar FlatLaf: " + ex.getMessage());
        }

        var context = new SpringApplicationBuilder(FrontendApplication.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            var tela = context.getBean(TelaLogin.class);
            tela.setLocationRelativeTo(null);
            tela.setVisible(true);

            javax.swing.SwingUtilities.updateComponentTreeUI(tela);
        });
    }
}
