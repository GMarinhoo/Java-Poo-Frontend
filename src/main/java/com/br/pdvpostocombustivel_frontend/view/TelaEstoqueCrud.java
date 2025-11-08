package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.EstoqueRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.EstoqueResponse;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoEstoque;
import com.br.pdvpostocombustivel_frontend.service.EstoqueService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelaEstoqueCrud extends JFrame {

    private final EstoqueService estoqueService;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField txtId = new JTextField();
    private final JTextField txtQuantidade = new JTextField();
    private final JTextField txtLocalTanque = new JTextField();
    private final JTextField txtLoteEndereco = new JTextField();
    private final JTextField txtLoteFabricacao = new JTextField();
    private final JFormattedTextField txtDataValidade;
    private final JComboBox<TipoEstoque> comboTipo = new JComboBox<>(TipoEstoque.values());

    private final JTextField txtIdProduto = new JTextField();

    private List<EstoqueResponse> listaDeEstoque = new ArrayList<>();

    public TelaEstoqueCrud(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;

        setTitle("Cadastro de Estoque");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        String[] columnNames = {"ID Estoque", "ID Produto", "Qtd", "Local/Tanque", "Lote End.", "Lote Fab.", "Validade", "Tipo"};
        tableModel = new DefaultTableModel(columnNames, 0) { /* ... isCellEditable ... */ };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        txtId.setEditable(false);

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            txtDataValidade = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        formPanel.add(new JLabel("ID Estoque:"));
        formPanel.add(txtId);

        formPanel.add(new JLabel("ID Produto:"));
        formPanel.add(txtIdProduto);

        formPanel.add(new JLabel("Quantidade:"));
        formPanel.add(txtQuantidade);
        formPanel.add(new JLabel("Local/Tanque:"));
        formPanel.add(txtLocalTanque);
        formPanel.add(new JLabel("Lote Endereço:"));
        formPanel.add(txtLoteEndereco);
        formPanel.add(new JLabel("Lote Fabricação:"));
        formPanel.add(txtLoteFabricacao);
        formPanel.add(new JLabel("Data Validade (dd/mm/aaaa):"));
        formPanel.add(txtDataValidade);
        formPanel.add(new JLabel("Tipo Estoque:"));
        formPanel.add(comboTipo);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar Formulário");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSalvar); buttonPanel.add(btnExcluir); buttonPanel.add(btnLimpar);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER); topPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparFormulario());
        table.getSelectionModel().addListSelectionListener(e -> preencherFormularioComLinhaSelecionada());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                System.out.println("Janela de Estoque ativada, atualizando tabela...");
                atualizarTabela();
            }
        });

        atualizarTabela();
    }

    private void atualizarTabela() {
        new SwingWorker<List<EstoqueResponse>, Void>() {
            @Override
            protected List<EstoqueResponse> doInBackground() throws Exception {
                return estoqueService.listarEstoques();
            }
            @Override
            protected void done() {
                try {
                    listaDeEstoque = get();
                    tableModel.setRowCount(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    if (listaDeEstoque != null) {
                        for (EstoqueResponse e : listaDeEstoque) {
                            tableModel.addRow(new Object[]{
                                    e.id(),
                                    e.idProduto(),
                                    e.quantidade(),
                                    e.localTanque(),
                                    e.loteEndereco(),
                                    e.loteFabricacao(),
                                    e.dataValidade() != null ? e.dataValidade().format(formatter) : "",
                                    e.tipo()
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaEstoqueCrud.this, "Erro ao buscar estoque: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void salvar() {
        if (txtQuantidade.getText().isBlank() || txtIdProduto.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "ID do Produto e Quantidade são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal quantidade;
        LocalDate dataValidade;
        Long idProduto;
        try {
            idProduto = Long.parseLong(txtIdProduto.getText());
            quantidade = new BigDecimal(txtQuantidade.getText().replace(",", "."));
            dataValidade = LocalDate.parse(txtDataValidade.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "ID do Produto e Quantidade devem ser números.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception dataEx) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido. Use dd/mm/aaaa.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EstoqueRequest request = new EstoqueRequest(
                idProduto, // <-- O novo argumento
                quantidade,
                txtLocalTanque.getText(),
                txtLoteEndereco.getText(),
                txtLoteFabricacao.getText(),
                dataValidade,
                (TipoEstoque) comboTipo.getSelectedItem()
        );

        Long id = txtId.getText().isEmpty() ? null : Long.valueOf(txtId.getText());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                estoqueService.salvarEstoque(request, id);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TelaEstoqueCrud.this, "Estoque salvo com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    if (e.getCause() instanceof HttpClientErrorException) { /* ... tratamento HttpClientErrorException ... */ } else { e.printStackTrace(); }
                    JOptionPane.showMessageDialog(TelaEstoqueCrud.this, "Erro ao salvar estoque: " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um item do estoque para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Long id = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    estoqueService.excluirEstoque(id);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(TelaEstoqueCrud.this, "Item do estoque excluído com sucesso!");
                        limparFormulario();
                        atualizarTabela();
                    } catch (Exception e) {
                        String errorMessage = e.getMessage();
                        if (e.getCause() instanceof HttpClientErrorException) { /* ... tratamento HttpClientErrorException ... */ } else { e.printStackTrace(); }
                        JOptionPane.showMessageDialog(TelaEstoqueCrud.this, "Erro ao excluir item do estoque: " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < listaDeEstoque.size()) {
            EstoqueResponse est = listaDeEstoque.get(selectedRow);
            txtId.setText(est.id().toString());
            txtIdProduto.setText(est.idProduto().toString());
            txtQuantidade.setText(est.quantidade().toString());
            txtLocalTanque.setText(est.localTanque());
            txtLoteEndereco.setText(est.loteEndereco());
            txtLoteFabricacao.setText(est.loteFabricacao());
            txtDataValidade.setText(est.dataValidade() != null ? est.dataValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            comboTipo.setSelectedItem(est.tipo());
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtIdProduto.setText("");
        txtQuantidade.setText("");
        txtLocalTanque.setText("");
        txtLoteEndereco.setText("");
        txtLoteFabricacao.setText("");
        txtDataValidade.setText("");
        comboTipo.setSelectedIndex(0);
        table.clearSelection();
    }
}