package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.ProdutoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.ProdutoResponse;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoProduto;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoProduto;
import com.br.pdvpostocombustivel_frontend.service.ProdutoService;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelaProdutoCrud extends JFrame {

    private final ProdutoService produtoService;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField txtId = new JTextField();
    private final JTextField txtNome = new JTextField();
    private final JTextField txtReferencia = new JTextField();
    private final JTextField txtFornecedor = new JTextField();
    private final JTextField txtCategoria = new JTextField();
    private final JTextField txtMarca = new JTextField();
    private final JComboBox<TipoProduto> comboTipo = new JComboBox<>(TipoProduto.values());

    private List<ProdutoResponse> listaDeProdutos = new ArrayList<>();

    public TelaProdutoCrud(ProdutoService produtoService) {
        this.produtoService = produtoService;

        setTitle("Cadastro de Produtos");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        String[] columnNames = {"ID", "Nome", "Referência", "Fornecedor", "Categoria", "Marca", "Tipo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        txtId.setEditable(false);

        formPanel.add(new JLabel("ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nome:"));
        formPanel.add(txtNome);
        formPanel.add(new JLabel("Referência:"));
        formPanel.add(txtReferencia);
        formPanel.add(new JLabel("Fornecedor:"));
        formPanel.add(txtFornecedor);
        formPanel.add(new JLabel("Categoria:"));
        formPanel.add(txtCategoria);
        formPanel.add(new JLabel("Marca:"));
        formPanel.add(txtMarca);
        formPanel.add(new JLabel("Tipo Combustível:"));
        formPanel.add(comboTipo);


        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar Formulário");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnLimpar);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparFormulario());
        table.getSelectionModel().addListSelectionListener(e -> preencherFormularioComLinhaSelecionada());

        atualizarTabela();
    }

    private void atualizarTabela() {
        new SwingWorker<List<ProdutoResponse>, Void>() {
            @Override
            protected List<ProdutoResponse> doInBackground() throws Exception {
                return produtoService.listarProdutos();
            }

            @Override
            protected void done() {
                try {
                    listaDeProdutos = get();
                    tableModel.setRowCount(0);
                    for (ProdutoResponse p : listaDeProdutos) {
                        tableModel.addRow(new Object[]{
                                p.id(),
                                p.nome(),
                                p.referencia(),
                                p.fornecedor(),
                                p.categoria(),
                                p.marca(),
                                p.tipoProduto()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaProdutoCrud.this, "Erro ao buscar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void salvar() {
        if (txtNome.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "O nome do produto é obrigatório.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProdutoRequest request = new ProdutoRequest(
                txtNome.getText(),
                txtReferencia.getText(),
                txtFornecedor.getText(),
                txtCategoria.getText(),
                txtMarca.getText(),
                (TipoProduto) comboTipo.getSelectedItem()
        );

        Long id = txtId.getText().isEmpty() ? null : Long.valueOf(txtId.getText());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                produtoService.salvarProduto(request, id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TelaProdutoCrud.this, "Produto salvo com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    if (e.getCause() instanceof org.springframework.web.client.HttpClientErrorException) {
                        errorMessage = ((org.springframework.web.client.HttpClientErrorException) e.getCause()).getResponseBodyAsString();
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(errorMessage);
                            if (root.has("message")) {
                                errorMessage = root.get("message").asText();
                            }
                        } catch (Exception jsonEx) { /* Ignora se não conseguir parsear */ }
                    }
                    JOptionPane.showMessageDialog(TelaProdutoCrud.this, "Erro ao salvar produto: " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Long id = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    produtoService.excluirProduto(id);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get(); // Verifica erro
                        JOptionPane.showMessageDialog(TelaProdutoCrud.this, "Produto excluído com sucesso!");
                        limparFormulario();
                        atualizarTabela();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(TelaProdutoCrud.this, "Erro ao excluir produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < listaDeProdutos.size()) {
            ProdutoResponse p = listaDeProdutos.get(selectedRow);
            txtId.setText(p.id().toString());
            txtNome.setText(p.nome());
            txtReferencia.setText(p.referencia());
            txtFornecedor.setText(p.fornecedor());
            txtCategoria.setText(p.categoria());
            txtMarca.setText(p.marca());
            comboTipo.setSelectedItem(p.tipoProduto());
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtNome.setText("");
        txtReferencia.setText("");
        txtFornecedor.setText("");
        txtCategoria.setText("");
        txtMarca.setText("");
        comboTipo.setSelectedIndex(0);
        table.clearSelection();
    }
}