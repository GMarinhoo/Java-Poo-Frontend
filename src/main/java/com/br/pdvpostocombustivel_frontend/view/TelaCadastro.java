package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;
import com.br.pdvpostocombustivel_frontend.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import java.awt.*;

@Component
@Lazy
public class TelaCadastro extends JDialog {

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtSenha = new JPasswordField(20);
    private final JPasswordField txtConfirmarSenha = new JPasswordField(20);
    private final JComboBox<TipoAcesso> comboPerfil = new JComboBox<>(TipoAcesso.values());
    private final JButton btnRegistrar = new JButton("Registrar");
    private final JButton btnCancelar = new JButton("Cancelar");


    @Autowired
    private AuthService authService;

    public TelaCadastro(Frame owner) {
        super(owner, "Cadastro de Novo Usuário", true);
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        painelPrincipal.add(new JLabel("Novo Usuário:"), gbc);
        gbc.gridy = 1;
        painelPrincipal.add(txtUsuario, gbc);

        gbc.gridy = 2;
        painelPrincipal.add(new JLabel("Senha:"), gbc);
        gbc.gridy = 3;
        painelPrincipal.add(txtSenha, gbc);

        gbc.gridy = 4;
        painelPrincipal.add(new JLabel("Confirmar Senha:"), gbc);
        gbc.gridy = 5;
        painelPrincipal.add(txtConfirmarSenha, gbc);

        gbc.gridy = 6;
        painelPrincipal.add(new JLabel("Perfil de Acesso:"), gbc);
        gbc.gridy = 7;
        painelPrincipal.add(comboPerfil, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelBotoes.add(btnRegistrar);
        painelBotoes.add(btnCancelar);
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        painelPrincipal.add(painelBotoes, gbc);

        add(painelPrincipal);

        btnRegistrar.addActionListener(e -> registrar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void registrar() {
        String usuario = txtUsuario.getText();
        String senha = new String(txtSenha.getPassword());
        String confirmarSenha = new String(txtConfirmarSenha.getPassword());
        TipoAcesso perfil = (TipoAcesso) comboPerfil.getSelectedItem();

        if (usuario.isBlank() || senha.isBlank()) {
            JOptionPane.showMessageDialog(this, "Usuário e senha são obrigatórios.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (perfil == null) {
            JOptionPane.showMessageDialog(this, "Selecione um perfil.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Tentando registrar: " + usuario + " com perfil " + perfil);
        AcessoRequest request = new AcessoRequest(usuario, senha, perfil);

        SwingWorker<AcessoResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AcessoResponse doInBackground() throws Exception {
                return acessoService.registrar(request);
            }

            @Override
            protected void done() {
                try {
                    AcessoResponse response = get(); // Pega o resultado
                    JOptionPane.showMessageDialog(TelaCadastro.this,
                            "Usuário '" + response.usuario() + "' registrado com sucesso!",
                            "Cadastro Realizado", JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                } catch (Exception e) {
                    String errorMessage = "Erro desconhecido durante o cadastro.";
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
                        System.err.println("Erro inesperado no cadastro: ");
                        e.printStackTrace();
                        errorMessage = e.getMessage();
                    }
                    JOptionPane.showMessageDialog(TelaCadastro.this, errorMessage, "Falha no Cadastro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}