package com.br.pdvpostocombustivel_frontend;

import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import javax.swing.*;
import java.awt.*;

public class AppMain extends JFrame {

    public AppMain() {
        setTitle("Painel Moderno - PDV Posto Combustível");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainPanel);

        JLabel title = new JLabel("Dashboard do Sistema");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        buttonPanel.add(criarBotao("Cadastro de Produtos", e -> mostrarMensagem("Abrindo cadastro...")));
        buttonPanel.add(criarBotao("Controle de Estoque", e -> mostrarMensagem("Acessando estoque...")));
        buttonPanel.add(criarBotao("Relatórios", e -> mostrarMensagem("Gerando relatórios...")));
        buttonPanel.add(criarBotao("Configurações", e -> mostrarMensagem("Abrindo configurações...")));

        JLabel footer = new JLabel("Desenvolvido por Gabriel Marinho", SwingConstants.CENTER);
        footer.setForeground(new Color(180, 180, 180));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mainPanel.add(footer, BorderLayout.SOUTH);
    }

    private JButton criarBotao(String texto, java.awt.event.ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFocusPainted(false);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setBackground(new Color(75, 110, 175));
        botao.setForeground(Color.WHITE);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        botao.addActionListener(acao);

        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(new Color(95, 130, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(new Color(75, 110, 175));
            }
        });

        return botao;
    }

    private void mostrarMensagem(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public static void main(String[] args) {
        try {
            FlatDraculaIJTheme.setup();

            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 20);
            UIManager.put("TextComponent.arc", 15);

        } catch (Exception ex) {
            System.err.println("Falha ao aplicar tema FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> new AppMain().setVisible(true));
    }
}
