package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.CustoResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.PrecoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.PrecoResponse;
import com.br.pdvpostocombustivel_frontend.service.CustoService;
import com.br.pdvpostocombustivel_frontend.service.PrecoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelaPrecoCrud extends JFrame {

    private final PrecoService precoService;
    private final CustoService custoService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtId = new JTextField();
    private final JTextField txtIdProduto = new JTextField();
    private final JTextField txtValor = new JTextField();

    private List<PrecoResponse> listaDePrecos = new ArrayList<>();

    public TelaPrecoCrud(PrecoService precoService, CustoService custoService) {
        this.precoService = precoService;
        this.custoService = custoService;

        setTitle("Cadastro e Histórico de Preços");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        String[] columnNames = {"ID Preço", "ID Produto", "Valor (R$)", "Data/Hora Alteração"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        txtId.setEditable(false);

        formPanel.add(new JLabel("ID Preço (automático):"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("ID do Produto:"));
        formPanel.add(txtIdProduto);
        formPanel.add(new JLabel("Valor (R$):"));
        formPanel.add(txtValor);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar Formulário");
        JButton btnSugerirPreco = new JButton("Calcular Preço Ideal");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSugerirPreco);
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
        btnSugerirPreco.addActionListener(e -> sugerirPreco());
        table.getSelectionModel().addListSelectionListener(e -> preencherFormularioComLinhaSelecionada());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                atualizarTabela();
            }
        });

        atualizarTabela();
    }

    private void atualizarTabela() {
        new SwingWorker<List<PrecoResponse>, Void>() {
            @Override
            protected List<PrecoResponse> doInBackground() throws Exception {
                return precoService.listarPrecos();
            }

            @Override
            protected void done() {
                try {
                    listaDePrecos = get();
                    tableModel.setRowCount(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                    if (listaDePrecos != null) {
                        for (PrecoResponse p : listaDePrecos) {
                            tableModel.addRow(new Object[]{
                                    p.id(),
                                    p.idProduto(),
                                    String.format("R$ %.2f", p.valor()),
                                    p.dataHoraAlteracao().format(formatter)
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaPrecoCrud.this, "Erro ao buscar histórico de preços: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void salvar() {
        if (txtIdProduto.getText().isBlank() || txtValor.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "ID do Produto e Valor são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal valor;
        Long idProduto;
        try {
            idProduto = Long.parseLong(txtIdProduto.getText());
            valor = new BigDecimal(txtValor.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do Produto e Valor devem ser números válidos.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PrecoRequest request = new PrecoRequest(valor, idProduto);
        Long id = txtId.getText().isEmpty() ? null : Long.valueOf(txtId.getText());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                precoService.salvarPreco(request, id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TelaPrecoCrud.this, "Preço salvo com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                } catch (Exception e) {
                    lidarComErroDeApi(e, "Erro ao salvar preço");
                }
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um registro de preço para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza? Isso excluirá este registro do histórico.", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Long id = listaDePrecos.get(table.getSelectedRow()).id();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    precoService.excluirPreco(id);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(TelaPrecoCrud.this, "Registro de preço excluído com sucesso!");
                        limparFormulario();
                        atualizarTabela();
                    } catch (Exception e) {
                        lidarComErroDeApi(e, "Erro ao excluir preço");
                    }
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < listaDePrecos.size()) {
            PrecoResponse p = listaDePrecos.get(selectedRow);
            txtId.setText(p.id().toString());
            txtIdProduto.setText(p.idProduto().toString());
            txtValor.setText(p.valor().toPlainString());
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtIdProduto.setText("");
        txtValor.setText("");
        table.clearSelection();
    }

    private void lidarComErroDeApi(Exception e, String titulo) {
        String errorMessage = "Erro desconhecido.";
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
            System.err.println(titulo + ": ");
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        JOptionPane.showMessageDialog(this, errorMessage, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private void sugerirPreco() {
        String inputCustoCompra = JOptionPane.showInputDialog(
                this,
                "Qual o Custo de Compra (da distribuidora) deste produto? \nEx: 4.10",
                "Calcular Preço Sugerido",
                JOptionPane.QUESTION_MESSAGE
        );

        if (inputCustoCompra == null || inputCustoCompra.isBlank()) {
            return;
        }

        final BigDecimal custoCompra;
        try {
            custoCompra = new BigDecimal(inputCustoCompra.replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Custo de compra inválido. Use apenas números.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<CustoResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected CustoResponse doInBackground() throws Exception {
                return custoService.getCustoMaisRecente();
            }

            @Override
            protected void done() {
                try {
                    CustoResponse regraDeCusto = get();

                    BigDecimal custoFixo = BigDecimal.valueOf(regraDeCusto.custoFixo());
                    BigDecimal imposto = BigDecimal.valueOf(regraDeCusto.imposto());
                    BigDecimal margemLucro = BigDecimal.valueOf(regraDeCusto.margemLucro());

                    BigDecimal custoTotal = custoCompra.add(custoFixo).add(imposto);

                    BigDecimal precoSugerido = custoTotal.multiply(BigDecimal.ONE.add(margemLucro));

                    precoSugerido = precoSugerido.setScale(2, java.math.RoundingMode.HALF_UP);

                    txtValor.setText(precoSugerido.toPlainString());

                    JOptionPane.showMessageDialog(TelaPrecoCrud.this,
                            "Preço Sugerido (R$ " + precoSugerido.toPlainString() + ") foi preenchido.\n" +
                                    "Ajuste se necessário e clique em Salvar.",
                            "Cálculo Concluído",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    lidarComErroDeApi(e, "Erro ao buscar regras de custo");
                }
            }
        };
        worker.execute();
    }
}