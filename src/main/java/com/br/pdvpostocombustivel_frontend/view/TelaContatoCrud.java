package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.ContatoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.ContatoResponse;
import com.br.pdvpostocombustivel_frontend.service.ContatoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelaContatoCrud extends JFrame {

    private final ContatoService contatoService;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField txtId = new JTextField();
    private final JTextField txtTelefone = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtEndereco = new JTextField();

    private List<ContatoResponse> listaDeContatos = new ArrayList<>();

    public TelaContatoCrud(ContatoService contatoService) {
        this.contatoService = contatoService;

        setTitle("Cadastro de Contatos");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        String[] columnNames = {"ID", "Telefone", "Email", "Endereço"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        txtId.setEditable(false);

        formPanel.add(new JLabel("ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Telefone:"));
        formPanel.add(txtTelefone);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Endereço:"));
        formPanel.add(txtEndereco);

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
        new SwingWorker<List<ContatoResponse>, Void>() {
            @Override
            protected List<ContatoResponse> doInBackground() throws Exception {
                return contatoService.listarContatos();
            }

            @Override
            protected void done() {
                try {
                    listaDeContatos = get();
                    tableModel.setRowCount(0);
                    if (listaDeContatos != null) {
                        for (ContatoResponse c : listaDeContatos) {
                            tableModel.addRow(new Object[]{
                                    c.id(),
                                    c.telefone(),
                                    c.email(),
                                    c.endereco()
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaContatoCrud.this, "Erro ao buscar contatos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void salvar() {
        if (txtTelefone.getText().isBlank() || txtEmail.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Telefone e Email são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ContatoRequest request = new ContatoRequest(
                txtTelefone.getText(),
                txtEmail.getText(),
                txtEndereco.getText()
        );

        Long id = txtId.getText().isEmpty() ? null : Long.valueOf(txtId.getText());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                contatoService.salvarContato(request, id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TelaContatoCrud.this, "Contato salvo com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    if (e.getCause() instanceof HttpClientErrorException) {
                        errorMessage = ((HttpClientErrorException) e.getCause()).getResponseBodyAsString();
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            JsonNode root = mapper.readTree(errorMessage);
                            if (root.has("message")) {
                                errorMessage = root.get("message").asText();
                            }
                        } catch (Exception jsonEx) {
                            System.err.println("Não foi possível parsear JSON de erro: " + errorMessage);
                        }
                    } else {
                        e.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(TelaContatoCrud.this, "Erro ao salvar contato: " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um contato para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Long id = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    contatoService.excluirContato(id);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(TelaContatoCrud.this, "Contato excluído com sucesso!");
                        limparFormulario();
                        atualizarTabela();
                    } catch (Exception e) {
                        String errorMessage = e.getMessage();
                        if (e.getCause() instanceof HttpClientErrorException) {
                            errorMessage = ((HttpClientErrorException) e.getCause()).getResponseBodyAsString();
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.registerModule(new JavaTimeModule());
                                JsonNode root = mapper.readTree(errorMessage);
                                if (root.has("message")) {
                                    errorMessage = root.get("message").asText();
                                }
                            } catch (Exception jsonEx) {
                                System.err.println("Não foi possível parsear JSON de erro: " + errorMessage);
                            }
                        } else {
                            e.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(TelaContatoCrud.this, "Erro ao excluir contato: " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < listaDeContatos.size()) {
            ContatoResponse c = listaDeContatos.get(selectedRow);
            txtId.setText(c.id().toString());
            txtTelefone.setText(c.telefone());
            txtEmail.setText(c.email());
            txtEndereco.setText(c.endereco());
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtTelefone.setText("");
        txtEmail.setText("");
        txtEndereco.setText("");
        table.clearSelection();
    }
}