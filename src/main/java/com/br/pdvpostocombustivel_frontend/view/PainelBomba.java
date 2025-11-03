package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.ProdutoResponse;
import com.br.pdvpostocombustivel_frontend.service.ProdutoService;
import com.br.pdvpostocombustivel_frontend.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

@Component
@Scope("prototype")
public class PainelBomba extends JPanel {

    private final ProdutoService produtoService;
    private final VendaService vendaService;

    private JComboBox<ProdutoResponse> comboCombustivel;
    private JLabel lblPrecoLitro;
    private JTextField txtValor;
    private JTextField txtLitros;
    private JButton btnVender;
    private JLabel lblStatus;

    private Long idFrentistaLogado;

    @Autowired
    public PainelBomba(ProdutoService produtoService, VendaService vendaService) {
        this.produtoService = produtoService;
        this.vendaService = vendaService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Bomba", TitledBorder.LEFT, TitledBorder.TOP));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel painelSelecao = new JPanel(new GridLayout(2, 2, 5, 5));

        comboCombustivel = new JComboBox<>();
        lblPrecoLitro = new JLabel("R$ 0,00 / litro");
        lblPrecoLitro.setFont(new Font("Arial", Font.BOLD, 16));

        painelSelecao.add(new JLabel("Combustível:"));
        painelSelecao.add(comboCombustivel);
        painelSelecao.add(new JLabel("Preço:"));
        painelSelecao.add(lblPrecoLitro);

        JPanel painelVenda = new JPanel(new GridLayout(2, 2, 5, 5));

        txtValor = new JTextField();
        txtLitros = new JTextField();

        painelVenda.add(new JLabel("Valor (R$):"));
        painelVenda.add(txtValor);
        painelVenda.add(new JLabel("Litros:"));
        painelVenda.add(txtLitros);

        JPanel painelAcao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnVender = new JButton("Registrar Venda");
        btnVender.setFont(new Font("Arial", Font.BOLD, 18));
        btnVender.setBackground(new Color(0, 153, 51)); // Verde
        btnVender.setForeground(Color.WHITE);

        lblStatus = new JLabel("Status: Livre");

        painelAcao.add(btnVender);
        painelAcao.add(lblStatus);

        add(painelSelecao, BorderLayout.NORTH);
        add(painelVenda, BorderLayout.CENTER);
        add(painelAcao, BorderLayout.SOUTH);
    }

    public void carregarCombustiveis(List<ProdutoResponse> produtos) {
        comboCombustivel.removeAllItems();
        comboCombustivel.addItem(new ProdutoResponse(-1L, "Selecione...", null, null, null, null, null));

        for (ProdutoResponse produto : produtos) {
            comboCombustivel.addItem(produto);
        }

        comboCombustivel.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ProdutoResponse prod) {
                    setText(prod.nome());
                }
                return this;
            }
        });
    }

    public void setFrentista(Long idFrentista) {
        this.idFrentistaLogado = idFrentista;
    }
}