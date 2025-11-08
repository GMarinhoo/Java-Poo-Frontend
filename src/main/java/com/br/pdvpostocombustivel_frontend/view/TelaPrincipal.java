package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.service.CustoService;
import com.br.pdvpostocombustivel_frontend.service.PrecoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.br.pdvpostocombustivel_frontend.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.br.pdvpostocombustivel_frontend.model.dto.ProdutoResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
public class TelaPrincipal extends JFrame {

    private Long usuarioId;
    private String nomeUsuario;

    private ApplicationContext context;
    private ProdutoService produtoService;

    private PainelBomba painelBomba1;
    private PainelBomba painelBomba2;
    private PainelBomba painelBomba3;

    public TelaPrincipal (ApplicationContext context, ProdutoService produtoService) {

        this.context = context;
        this.produtoService = produtoService;

        setTitle("PDV Posto de Combustível - Principal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        JMenuBar menuBar = new JMenuBar();
        JMenu menuCadastros = new JMenu("Sistema");
        JMenuItem itemPessoas = new JMenuItem("Pessoas");
        JMenuItem itemProdutos = new JMenuItem("Produtos");
        JMenuItem itemContatos = new JMenuItem("Contatos");
        JMenuItem itemEstoque = new JMenuItem("Estoque");
        JMenuItem itemUsuario = new JMenuItem("Cadastrar Novo Usuário");
        JMenuItem itemAcessos = new JMenuItem("Gerenciar Acessos");
        JMenuItem itemPrecos = new JMenuItem("Definir Preços");
        JMenuItem itemCustos = new JMenuItem("Custos");

        menuCadastros.add(itemUsuario);
        menuCadastros.add(itemAcessos);
        menuCadastros.addSeparator();
        menuCadastros.add(itemPessoas);
        menuCadastros.add(itemProdutos);
        menuCadastros.add(itemPrecos);
        menuCadastros.add(itemCustos);
        menuCadastros.add(itemContatos);
        menuCadastros.add(itemEstoque);
        menuBar.add(menuCadastros);

        JMenu menuSistema = new JMenu("Sair");
        JMenuItem itemLogout = new JMenuItem("Logout");
        menuSistema.add(itemLogout);
        menuBar.add(menuSistema);

        setJMenuBar(menuBar);

        JPanel painelDeBombas = new JPanel(new GridLayout(1, 3, 10, 10));

        this.painelBomba1 = context.getBean(PainelBomba.class);
        this.painelBomba2 = context.getBean(PainelBomba.class);
        this.painelBomba3 = context.getBean(PainelBomba.class);

        painelDeBombas.add(painelBomba1);
        painelDeBombas.add(painelBomba2);
        painelDeBombas.add(painelBomba3);

        add(painelDeBombas, BorderLayout.CENTER);

        itemPessoas.addActionListener(e -> abrirTelaCrud(TelaPessoaCrud.class));
        itemProdutos.addActionListener(e -> abrirTelaCrud(TelaProdutoCrud.class));
        itemContatos.addActionListener(e -> abrirTelaCrud(TelaContatoCrud.class));
        itemEstoque.addActionListener(e -> abrirTelaCrud(TelaEstoqueCrud.class));
        itemPrecos.addActionListener(e -> abrirTelaCrud(TelaPrecoCrud.class));
        itemCustos.addActionListener(e -> abrirTelaCrud(TelaCustoCrud.class));
        itemUsuario.addActionListener(e -> abrirTelaCadastroUsuario());
        itemAcessos.addActionListener(e -> abrirTelaCrud(TelaAcessoCrud.class));
        itemLogout.addActionListener(e -> fazerLogout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                System.out.println("Janela Principal ativada, recarregando produtos...");
                carregarDadosIniciais();
            }
        });

        carregarDadosIniciais();
    }

    public void setUsuarioLogado(Long idAcesso, String nomePessoa) {
        this.usuarioId = idAcesso;
        this.nomeUsuario = nomePessoa;
        setTitle("PDV Posto de Combustível - Usuário: " + nomePessoa);

        if (painelBomba1 != null) {
            painelBomba1.setFrentista(idAcesso);
            painelBomba2.setFrentista(idAcesso);
            painelBomba3.setFrentista(idAcesso);
        }
    }

    private <T extends JFrame> void abrirTelaCrud(Class<T> telaClass) {
        try {
            T tela;

            if (telaClass.equals(TelaPrecoCrud.class)) {
                PrecoService precoService = context.getBean(PrecoService.class);
                CustoService custoService = context.getBean(CustoService.class);

                tela = (T) new TelaPrecoCrud(precoService, custoService);

            } else {
                tela = context.getBean(telaClass);
            }

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

    private void abrirTelaCadastroUsuario() {
        TelaCadastro telaCadastro = context.getBean(TelaCadastro.class, this);
        telaCadastro.pack();
        telaCadastro.setLocationRelativeTo(this);
        telaCadastro.setVisible(true);
    }

    private void carregarDadosIniciais() {
        new SwingWorker<List<ProdutoResponse>, Void>() {

            @Override
            protected List<ProdutoResponse> doInBackground() throws Exception {
                return produtoService.listarProdutos();
            }

            @Override
            protected void done() {
                try {
                    List<ProdutoResponse> produtos = get();

                    painelBomba1.carregarCombustiveis(produtos);
                    painelBomba2.carregarCombustiveis(produtos);
                    painelBomba3.carregarCombustiveis(produtos);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaPrincipal.this,
                            "Erro grave ao carregar produtos do backend:\n" + e.getMessage(),
                            "Erro de Carga", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}