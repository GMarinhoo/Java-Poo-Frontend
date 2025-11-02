package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.RegistroCompletoRequest;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoPessoa;
import com.br.pdvpostocombustivel_frontend.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Lazy
public class TelaCadastro extends JDialog {


    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtSenha = new JPasswordField(20);
    private final JPasswordField txtConfirmarSenha = new JPasswordField(20);
    private final JComboBox<TipoAcesso> comboPerfil = new JComboBox<>(TipoAcesso.values());

    private final JTextField txtNomeCompleto = new JTextField(20);
    private final JTextField txtCpfCnpj = new JTextField(20);
    private final JTextField txtCtps = new JTextField(20);
    private final JFormattedTextField txtDataNascimento;
    private final JComboBox<TipoPessoa> comboTipoPessoa = new JComboBox<>(TipoPessoa.values());

    private final JButton btnRegistrar = new JButton("Registrar");
    private final JButton btnCancelar = new JButton("Cancelar");

    @Autowired
    private AuthService authService;

    public TelaCadastro(Frame owner) {
        super(owner, "Cadastro de Novo Usuário", true);

        setSize(450, 700);
        setLocationRelativeTo(owner);
        setResizable(false);

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            txtDataNascimento = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            throw new RuntimeException("Erro ao criar máscara de data", e);
        }

        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Menos 'inset' vertical
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        painelPrincipal.add(new JLabel("Novo Usuário (Login):"), gbc);
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

        gbc.gridy = 8;
        gbc.insets = new Insets(10, 10, 10, 10);
        painelPrincipal.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 10, 5, 10); // Reseta o 'inset'

        gbc.gridy = 9;
        painelPrincipal.add(new JLabel("--- Dados Pessoais ---"), gbc);

        gbc.gridy = 10;
        painelPrincipal.add(new JLabel("Nome Completo:"), gbc);
        gbc.gridy = 11;
        painelPrincipal.add(txtNomeCompleto, gbc);

        gbc.gridy = 12;
        painelPrincipal.add(new JLabel("CPF/CNPJ:"), gbc);
        gbc.gridy = 13;
        painelPrincipal.add(txtCpfCnpj, gbc);

        gbc.gridy = 14;
        painelPrincipal.add(new JLabel("Nº CTPS (Opcional):"), gbc);
        gbc.gridy = 15;
        painelPrincipal.add(txtCtps, gbc);

        gbc.gridy = 16;
        painelPrincipal.add(new JLabel("Data Nascimento (dd/mm/aaaa):"), gbc);
        gbc.gridy = 17;
        painelPrincipal.add(txtDataNascimento, gbc);

        gbc.gridy = 18;
        painelPrincipal.add(new JLabel("Tipo Pessoa:"), gbc);
        gbc.gridy = 19;
        painelPrincipal.add(comboTipoPessoa, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelBotoes.add(btnRegistrar);
        painelBotoes.add(btnCancelar);
        gbc.gridy = 20;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
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

        String nomeCompleto = txtNomeCompleto.getText();
        String cpfCnpj = txtCpfCnpj.getText();
        String ctpsStr = txtCtps.getText();
        String dataNascStr = txtDataNascimento.getText();
        TipoPessoa tipoPessoa = (TipoPessoa) comboTipoPessoa.getSelectedItem();

        if (usuario.isBlank() || senha.isBlank() || nomeCompleto.isBlank() || cpfCnpj.isBlank() || dataNascStr.equals("  /  /    ")) {
            JOptionPane.showMessageDialog(this, "Usuário, Senha, Nome Completo, CPF/CNPJ e Data de Nascimento são obrigatórios.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (perfil == null) {
            JOptionPane.showMessageDialog(this, "Selecione um perfil de acesso.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long numeroCtps = null;
        if (!ctpsStr.isBlank()) {
            try {
                numeroCtps = Long.parseLong(ctpsStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número de CTPS inválido. Use apenas números.", "Erro", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        LocalDate dataNascimento;
        try {
            dataNascimento = LocalDate.parse(dataNascStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de data de nascimento inválido. Use dd/mm/aaaa.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RegistroCompletoRequest request = new RegistroCompletoRequest(
                nomeCompleto,
                cpfCnpj,
                numeroCtps,
                dataNascimento,
                tipoPessoa,
                usuario,
                senha,
                perfil
        );

        btnRegistrar.setEnabled(false);
        btnCancelar.setEnabled(false);

        SwingWorker<AcessoResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AcessoResponse doInBackground() throws Exception {
                return authService.registrarCompleto(request);
            }

            @Override
            protected void done() {
                try {
                    AcessoResponse response = get();
                    JOptionPane.showMessageDialog(TelaCadastro.this,
                            "Usuário '" + response.usuario() + "' (Pessoa: " + response.nomePessoa() + ") registrado com sucesso!",
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
                } finally {
                    btnRegistrar.setEnabled(true);
                    btnCancelar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}