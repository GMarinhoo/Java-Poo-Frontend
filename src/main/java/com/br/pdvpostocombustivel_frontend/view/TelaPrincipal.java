package com.br.pdvpostocombustivel_frontend.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Component
public class TelaPrincipal extends JFrame {

    private Long usuarioId;
    private String nomeUsuario;

    @Autowired
    private ApplicationContext context;


    private JLabel labelBemVindo;

    public TelaPrincipal() {
        setTitle("PDV Posto de Combustível - Principal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        setLayout(new BorderLayout(10, 10));

        JMenuBar menuBar = new JMenuBar();
        JMenu menuCadastros = new JMenu("Cadastros");
        JMenuItem itemPessoas = new JMenuItem("Pessoas");
        JMenuItem itemProdutos = new JMenuItem("Produtos");
        JMenuItem itemContatos = new JMenuItem("Contatos");
        JMenuItem itemEstoque = new JMenuItem("Estoque");

        menuCadastros.add(itemPessoas);
        menuCadastros.add(itemProdutos);
        menuCadastros.add(itemContatos);
        menuCadastros.add(itemEstoque);
        menuBar.add(menuCadastros);

        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem itemLogout = new JMenuItem("Logout");
        menuSistema.add(itemLogout);
        menuBar.add(menuSistema);

        setJMenuBar(menuBar);

        JPanel painelCentral = new JPanel();
        painelCentral.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        painelCentral.setBorder(BorderFactory.createTitledBorder("Operação"));

        labelBemVindo = new JLabel("Login bem-sucedido! Tela Principal.");
        labelBemVindo.setFont(new Font("Arial", Font.BOLD, 24));
        painelCentral.add(labelBemVindo);

        add(painelCentral, BorderLayout.CENTER);

        itemPessoas.addActionListener(e -> abrirTelaCrud(TelaPessoaCrud.class));
        itemProdutos.addActionListener(e -> abrirTelaCrud(TelaProdutoCrud.class));
        itemContatos.addActionListener(e -> abrirTelaCrud(TelaContatoCrud.class));
        itemEstoque.addActionListener(e -> abrirTelaCrud(TelaEstoqueCrud.class));

        itemLogout.addActionListener(e -> fazerLogout());

    }

    public void setUsuarioLogado(Long id, String nome) {
        this.usuarioId = id;
        this.nomeUsuario = nome;
        setTitle("PDV Posto de Combustível - Usuário: " + nomeUsuario);
        labelBemVindo.setText("Bem-vindo, " + nomeUsuario + "!");
    }

    private <T extends JFrame> void abrirTelaCrud(Class<T> telaClass) {
        try {
            T tela = context.getBean(telaClass);
            tela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            tela.toFront();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir tela de " + telaClass.getSimpleName() + ": " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void fazerLogout() {
        this.setVisible(false);
        this.usuarioId = null;
        this.nomeUsuario = null;

        TelaLogin telaLogin = context.getBean(TelaLogin.class);
        telaLogin.setVisible(true);
    }
}