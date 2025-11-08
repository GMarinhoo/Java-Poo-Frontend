package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.CustoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.CustoResponse;
import com.br.pdvpostocombustivel_frontend.service.CustoService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TelaCustoCrud extends JFrame {

    private final CustoService custoService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtId = new JTextField();
    private final JTextField txtImposto = new JTextField();
    private final JTextField txtCustoVariavel = new JTextField();
    private final JTextField txtCustoFixo = new JTextField();
    private final JTextField txtMargemLucro = new JTextField();
    private final JFormattedTextField txtDataProcessamento;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private List<CustoResponse> listaDeCustos = new ArrayList<>();

    public TelaCustoCrud(CustoService custoService) {
        this.custoService = custoService;

        setTitle("Cadastro de Custos (Histórico)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        String[] columnNames = {"ID", "Imposto (%)", "Custo Var.", "Custo Fixo", "Margem Lucro (%)", "Data Process."};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        txtId.setEditable(false);

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            txtDataProcessamento = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            throw new RuntimeException("Erro ao criar máscara de data", e);
        }

        formPanel.add(new JLabel("ID (automático):"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Imposto (Ex: 0.1 = 10%):"));
        formPanel.add(txtImposto);
        formPanel.add(new JLabel("Custo Variável:"));
        formPanel.add(txtCustoVariavel);
        formPanel.add(new JLabel("Custo Fixo:"));
        formPanel.add(txtCustoFixo);
        formPanel.add(new JLabel("Margem Lucro (Ex: 0.2 = 20%):"));
        formPanel.add(txtMargemLucro);
        formPanel.add(new JLabel("Data Processamento (dd/mm/aaaa):"));
        formPanel.add(txtDataProcessamento);

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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                atualizarTabela();
            }
        });

        atualizarTabela();
    }

    private void atualizarTabela() {
        new SwingWorker<List<CustoResponse>, Void>() {
            @Override
            protected List<CustoResponse> doInBackground() throws Exception {
                return custoService.listarCustos();
            }

            @Override
            protected void done() {
                try {
                    listaDeCustos = get();
                    tableModel.setRowCount(0);

                    if (listaDeCustos != null) {
                        for (CustoResponse c : listaDeCustos) {
                            tableModel.addRow(new Object[]{
                                    c.id(),
                                    c.imposto(),
                                    c.custoVariavel(),
                                    c.custoFixo(),
                                    c.margemLucro(),
                                    dateFormat.format(c.dataProcessamento())
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaCustoCrud.this, "Erro ao buscar custos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void salvar() {
        Double imposto, custoVar, custoFixo, margem;
        Date dataProc;
        try {
            imposto = Double.parseDouble(txtImposto.getText().replace(",", "."));
            custoVar = Double.parseDouble(txtCustoVariavel.getText().replace(",", "."));
            custoFixo = Double.parseDouble(txtCustoFixo.getText().replace(",", "."));
            margem = Double.parseDouble(txtMargemLucro.getText().replace(",", "."));

            dataProc = dateFormat.parse(txtDataProcessamento.getText());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Campos de valor (Imposto, Custos, Margem) devem ser números válidos.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Data inválida. Use o formato dd/mm/aaaa.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CustoRequest request = new CustoRequest(
                imposto,
                custoVar,
                custoFixo,
                margem,
                dataProc
        );

        Long id = txtId.getText().isEmpty() ? null : Long.valueOf(txtId.getText());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                custoService.salvarCusto(request, id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TelaCustoCrud.this, "Custo salvo com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                } catch (Exception e) {
                    lidarComErroDeApi(e, "Erro ao salvar custo");
                }
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um registro de custo para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Long id = listaDeCustos.get(table.getSelectedRow()).id();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    custoService.excluirCusto(id);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(TelaCustoCrud.this, "Registro de custo excluído com sucesso!");
                        limparFormulario();
                        atualizarTabela();
                    } catch (Exception e) {
                        lidarComErroDeApi(e, "Erro ao excluir custo");
                    }
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < listaDeCustos.size()) {
            CustoResponse c = listaDeCustos.get(selectedRow);

            txtId.setText(c.id().toString());
            txtImposto.setText(c.imposto().toString());
            txtCustoVariavel.setText(c.custoVariavel().toString());
            txtCustoFixo.setText(c.custoFixo().toString());
            txtMargemLucro.setText(c.margemLucro().toString());
            txtDataProcessamento.setText(dateFormat.format(c.dataProcessamento()));
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtImposto.setText("");
        txtCustoVariavel.setText("");
        txtCustoFixo.setText("");
        txtMargemLucro.setText("");
        txtDataProcessamento.setText("");
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
}