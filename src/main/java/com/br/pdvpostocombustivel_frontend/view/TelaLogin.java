package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.LoginRequest;
import com.br.pdvpostocombustivel_frontend.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import java.awt.*;

@Component
public class TelaLogin extends JFrame {

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtSenha = new JPasswordField(20);
    private final JButton btnEntrar = new JButton("Entrar");

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AuthService authService;

    public TelaLogin() {
        setTitle("Login - PDV Posto de Combustível");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        painelPrincipal.add(new JLabel("Usuário:"), gbc);
        gbc.gridy = 1;
        painelPrincipal.add(txtUsuario, gbc);
        gbc.gridy = 2;
        painelPrincipal.add(new JLabel("Senha:"), gbc);
        gbc.gridy = 3;
        painelPrincipal.add(txtSenha, gbc);
        gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        painelPrincipal.add(btnEntrar, gbc);
        gbc.gridx = 1;

        add(painelPrincipal);

        btnEntrar.addActionListener(e -> tentarLogin());
    }

    private void tentarLogin() {
        String usuario = txtUsuario.getText();
        String senha = new String(txtSenha.getPassword());

        if (usuario.isBlank() || senha.isBlank()) {
            JOptionPane.showMessageDialog(this, "Usuário e senha são obrigatórios.", "Erro de Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoginRequest loginRequest = new LoginRequest(usuario, senha);

        SwingWorker<AcessoResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AcessoResponse doInBackground() throws Exception {
                return authService.login(loginRequest);
            }

            @Override
            protected void done() {
                try {
                    AcessoResponse usuarioLogado = get();

                    System.out.println("Login bem-sucedido para: " + usuarioLogado.usuario() + " com perfil " + usuarioLogado.perfil());

                    TelaLogin.this.setVisible(false);
                    txtUsuario.setText("");
                    txtSenha.setText("");


                    TelaPrincipal telaPrincipal = context.getBean(TelaPrincipal.class);
                    telaPrincipal.setUsuarioLogado(usuarioLogado.idAcesso(), usuarioLogado.nomePessoa());
                    telaPrincipal.setVisible(true);

                } catch (Exception e) {
                    String errorMessage = "Erro desconhecido durante o login.";
                    if (e.getCause() instanceof HttpClientErrorException exHttp) {
                        try {
                            String responseBody = exHttp.getResponseBodyAsString();
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            JsonNode root = mapper.readTree(responseBody);
                            if (root.has("message")) {
                                errorMessage = root.get("message").asText();
                            } else {
                                errorMessage = "Erro " + exHttp.getStatusCode() + ": " + exHttp.getStatusText();
                            }
                        } catch (Exception jsonEx) {
                            errorMessage = "Erro " + exHttp.getStatusCode() + ": Não foi possível ler a resposta do servidor.";
                        }
                    } else {
                        System.err.println("Erro inesperado no login: ");
                        e.printStackTrace();
                        errorMessage = e.getMessage();
                    }
                    JOptionPane.showMessageDialog(TelaLogin.this, errorMessage, "Falha no Login", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}