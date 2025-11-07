package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.PessoaRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.PessoaResponse;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoPessoa;
import com.br.pdvpostocombustivel_frontend.service.PessoaService;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TelaPessoaCrud extends JFrame {

    private final PessoaService pessoaService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtId = new JTextField();
    private final JTextField txtNome = new JTextField();
    private final JTextField txtCpfCnpj = new JTextField();
    private final JTextField txtCtps = new JTextField();
    private final JFormattedTextField txtDataNascimento;
    private final JComboBox<TipoPessoa> comboTipoPessoa = new JComboBox<>(TipoPessoa.values());

    public TelaPessoaCrud(PessoaService pessoaService) {
        this.pessoaService = pessoaService;

        setTitle("Cadastro de Pessoas");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Nome Completo", "CPF/CNPJ", "CTPS", "Data Nasc.", "Tipo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Torna a tabela não editável
            }
        };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        txtId.setEditable(false);

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            txtDataNascimento = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        formPanel.add(new JLabel("ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nome Completo:"));
        formPanel.add(txtNome);
        formPanel.add(new JLabel("CPF/CNPJ:"));
        formPanel.add(txtCpfCnpj);
        formPanel.add(new JLabel("Nº CTPS:"));
        formPanel.add(txtCtps);
        formPanel.add(new JLabel("Data Nascimento (dd/mm/aaaa):"));
        formPanel.add(txtDataNascimento);
        formPanel.add(new JLabel("Tipo de Pessoa:"));
        formPanel.add(comboTipoPessoa);

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
        new SwingWorker<List<PessoaResponse>, Void>() {
            @Override
            protected List<PessoaResponse> doInBackground() throws Exception {
                return pessoaService.listarPessoas();
            }

            @Override
            protected void done() {
                try {
                    List<PessoaResponse> pessoas = get();
                    tableModel.setRowCount(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    for (PessoaResponse p : pessoas) {
                        tableModel.addRow(new Object[]{
                                p.id(),
                                p.nomeCompleto(),
                                p.cpfCnpj(),
                                p.numeroCtps(),
                                p.dataNascimento() != null ? p.dataNascimento().format(formatter) : "",
                                p.tipoPessoa()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaPessoaCrud.this, "Erro ao buscar pessoas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void salvar() {
        if (txtNome.getText().isBlank() || txtCpfCnpj.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Nome e CPF/CNPJ são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataNascimento = null;
        try {
            dataNascimento = LocalDate.parse(txtDataNascimento.getText(), formatter);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido. Use dd/mm/aaaa.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PessoaRequest request = new PessoaRequest(
                txtNome.getText(),
                txtCpfCnpj.getText(),
                Long.parseLong(txtCtps.getText()),
                dataNascimento,
                (TipoPessoa) comboTipoPessoa.getSelectedItem()
        );

        Long id = txtId.getText().isEmpty() ? null : Long.valueOf(txtId.getText());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                pessoaService.salvarPessoa(request, id);
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(TelaPessoaCrud.this, "Pessoa salva com sucesso!");
                limparFormulario();
                atualizarTabela();
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma pessoa para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Long id = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    pessoaService.excluirPessoa(id);
                    return null;
                }
                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(TelaPessoaCrud.this, "Pessoa excluída com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNome.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtCpfCnpj.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCtps.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtDataNascimento.setText(tableModel.getValueAt(selectedRow, 4).toString());
            comboTipoPessoa.setSelectedItem(tableModel.getValueAt(selectedRow, 5));
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtNome.setText("");
        txtCpfCnpj.setText("");
        txtCtps.setText("");
        txtDataNascimento.setText("");
        comboTipoPessoa.setSelectedIndex(0);
        table.clearSelection();
    }
}