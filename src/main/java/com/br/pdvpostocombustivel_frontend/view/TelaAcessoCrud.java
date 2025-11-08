package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;
import com.br.pdvpostocombustivel_frontend.service.AcessoService;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class TelaAcessoCrud extends JFrame {

    private final AcessoService acessoService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtId = new JTextField();
    private final JTextField txtUsuario = new JTextField();
    private final JTextField txtNomePessoa = new JTextField();
    private final JComboBox<TipoAcesso> comboPerfil = new JComboBox<>(TipoAcesso.values());

    private List<AcessoResponse> listaDeAcessos = new ArrayList<>();

    public TelaAcessoCrud(AcessoService acessoService) {
        this.acessoService = acessoService;

        setTitle("Gerenciamento de Acessos (Logins)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        int padding = 15;
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(padding, padding, padding, padding));

        String[] columnNames = {"ID Acesso", "Usuário (Login)", "Perfil", "ID Pessoa", "Nome Pessoa"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        txtId.setEditable(false);
        txtUsuario.setEditable(false);
        txtNomePessoa.setEditable(false);

        formPanel.add(new JLabel("ID Acesso:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Usuário (Login):"));
        formPanel.add(txtUsuario);
        formPanel.add(new JLabel("Nome Pessoa:"));
        formPanel.add(txtNomePessoa);
        formPanel.add(new JLabel("Novo Perfil:"));
        formPanel.add(comboPerfil);

        JButton btnAtualizarPerfil = new JButton("Atualizar Perfil");
        JButton btnExcluir = new JButton("Excluir Login");
        JButton btnLimpar = new JButton("Limpar Seleção");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnAtualizarPerfil);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnLimpar);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        btnAtualizarPerfil.addActionListener(e -> atualizarPerfil());
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
        new SwingWorker<List<AcessoResponse>, Void>() {
            @Override
            protected List<AcessoResponse> doInBackground() throws Exception {
                return acessoService.listarAcessos();
            }

            @Override
            protected void done() {
                try {
                    listaDeAcessos = get();
                    tableModel.setRowCount(0); // Limpa a tabela

                    if (listaDeAcessos != null) {
                        for (AcessoResponse a : listaDeAcessos) {
                            tableModel.addRow(new Object[]{
                                    a.idAcesso(),
                                    a.usuario(),
                                    a.perfil(),
                                    a.idPessoa(),
                                    a.nomePessoa()
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaAcessoCrud.this, "Erro ao buscar acessos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void atualizarPerfil() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um acesso na tabela para atualizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = listaDeAcessos.get(table.getSelectedRow()).idAcesso();
        TipoAcesso novoPerfil = (TipoAcesso) comboPerfil.getSelectedItem();

        if (novoPerfil == null) {
            JOptionPane.showMessageDialog(this, "Selecione um perfil válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                acessoService.atualizarPerfil(id, novoPerfil);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TelaAcessoCrud.this, "Perfil do usuário atualizado com sucesso!");
                    limparFormulario();
                    atualizarTabela();
                } catch (Exception e) {
                    lidarComErroDeApi(e, "Erro ao atualizar perfil");
                }
            }
        }.execute();
    }

    private void excluir() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um acesso (login) para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "AVISO IMPORTANTE:\n" +
                        "Isso excluirá o LOGIN do usuário.\n" +
                        "Você ainda precisará excluir a 'Pessoa' associada no menu 'Cadastros -> Pessoas'.\n\n" +
                        "Tem certeza?",
                "Confirmar Exclusão de Acesso",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Long id = listaDeAcessos.get(table.getSelectedRow()).idAcesso();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    acessoService.excluirAcesso(id);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(TelaAcessoCrud.this, "Acesso (login) excluído com sucesso!");
                        limparFormulario();
                        atualizarTabela();
                    } catch (Exception e) {
                        lidarComErroDeApi(e, "Erro ao excluir acesso");
                    }
                }
            }.execute();
        }
    }

    private void preencherFormularioComLinhaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < listaDeAcessos.size()) {
            AcessoResponse a = listaDeAcessos.get(selectedRow);

            txtId.setText(a.idAcesso().toString());
            txtUsuario.setText(a.usuario());
            txtNomePessoa.setText(a.nomePessoa());
            comboPerfil.setSelectedItem(a.perfil());
        }
    }

    private void limparFormulario() {
        txtId.setText("");
        txtUsuario.setText("");
        txtNomePessoa.setText("");
        comboPerfil.setSelectedIndex(0);
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