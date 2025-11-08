package com.br.pdvpostocombustivel_frontend.view;

import com.br.pdvpostocombustivel_frontend.model.dto.*;
import com.br.pdvpostocombustivel_frontend.service.PrecoService;
import com.br.pdvpostocombustivel_frontend.service.ProdutoService;
import com.br.pdvpostocombustivel_frontend.service.VendaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Dimension;

@Component
@Scope("prototype")
public class PainelBomba extends JPanel {

    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final PrecoService precoService;

    private JComboBox<ProdutoResponse> comboCombustivel;
    private JLabel lblPrecoLitro;
    private JTextField txtValor;
    private JTextField txtLitros;
    private JButton btnVender;
    private JLabel lblStatus;
    private JComboBox<String> comboFormaPagamento;

    private Long idFrentistaLogado;
    private BigDecimal precoAtual = BigDecimal.ZERO;

    @Autowired
    public PainelBomba(ProdutoService produtoService, VendaService vendaService, PrecoService precoService) {
        this.produtoService = produtoService;
        this.vendaService = vendaService;
        this.precoService = precoService;

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

        JPanel painelVenda = new JPanel(new GridLayout(3, 2, 5, 5));

        txtValor = new JTextField();
        txtLitros = new JTextField();

        comboFormaPagamento = new JComboBox<>();
        comboFormaPagamento.addItem("Dinheiro");
        comboFormaPagamento.addItem("PIX");
        comboFormaPagamento.addItem("Cartão de Débito");
        comboFormaPagamento.addItem("Cartão de Crédito");

        painelVenda.add(new JLabel("Valor (R$):"));
        painelVenda.add(txtValor);
        painelVenda.add(new JLabel("Litros:"));
        painelVenda.add(txtLitros);
        painelVenda.add(new JLabel("Pagamento:"));
        painelVenda.add(comboFormaPagamento);

        JPanel painelAcao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnVender = new JButton("Registrar Venda");
        btnVender.setFont(new Font("Arial", Font.BOLD, 18));
        btnVender.setBackground(new Color(0, 153, 51));
        btnVender.setForeground(Color.WHITE);

        lblStatus = new JLabel("Status: Livre");

        painelAcao.add(btnVender);
        painelAcao.add(lblStatus);

        add(painelSelecao, BorderLayout.NORTH);
        add(painelVenda, BorderLayout.CENTER);
        add(painelAcao, BorderLayout.SOUTH);

        comboCombustivel.addActionListener(e -> atualizarPreco());
        txtValor.addActionListener(e -> calcularLitrosPorValor());
        txtLitros.addActionListener(e -> calcularValorPorLitros());
        btnVender.addActionListener(e -> realizarVenda());
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

    private void atualizarPreco() {
        Object itemSelecionado = comboCombustivel.getSelectedItem();

        if (itemSelecionado instanceof ProdutoResponse produto) {
            if (produto.id() == -1L) {
                lblPrecoLitro.setText("R$ 0,00 / litro");
                precoAtual = BigDecimal.ZERO;
                return;
            }

            new SwingWorker<PrecoResponse, Void>() {
                @Override
                protected PrecoResponse doInBackground() throws Exception {
                    return precoService.getPrecoAtual(produto.id());
                }

                @Override
                protected void done() {
                    try {
                        PrecoResponse preco = get();
                        PainelBomba.this.precoAtual = preco.valor();

                        String precoFormatado = String.format("R$ %.2f / litro", preco.valor());
                        lblPrecoLitro.setText(precoFormatado);

                    } catch (Exception e) {
                        lblPrecoLitro.setText("Erro!");
                        PainelBomba.this.precoAtual = BigDecimal.ZERO;
                        JOptionPane.showMessageDialog(PainelBomba.this,
                                "Erro ao buscar preço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void calcularLitrosPorValor() {
        if (precoAtual.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um combustível com preço válido primeiro.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal valorDesejado = new BigDecimal(txtValor.getText().replace(",", "."));
            BigDecimal litrosCalculados = valorDesejado.divide(precoAtual, 3, RoundingMode.HALF_UP);
            txtLitros.setText(String.format("%.3f", litrosCalculados));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Use apenas números.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (ArithmeticException e) {
            JOptionPane.showMessageDialog(this, "Erro no cálculo (divisão por zero).", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcularValorPorLitros() {
        if (precoAtual.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um combustível com preço válido primeiro.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal litrosDesejados = new BigDecimal(txtLitros.getText().replace(",", "."));
            BigDecimal valorCalculado = litrosDesejados.multiply(precoAtual);
            txtValor.setText(String.format("%.2f", valorCalculado));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor de litros inválido. Use apenas números.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void realizarVenda() {
        if (idFrentistaLogado == null) {
            JOptionPane.showMessageDialog(this, "Erro fatal: Frentista não identificado. Tente relogar no sistema.", "Erro de Sessão", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object itemSelecionado = comboCombustivel.getSelectedItem();
        if (!(itemSelecionado instanceof ProdutoResponse produto) || produto.id() == -1L) {
            JOptionPane.showMessageDialog(this, "Selecione um combustível válido.", "Erro de Venda", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtLitros.getText().isBlank() && !txtValor.getText().isBlank()) {
            calcularLitrosPorValor();
        }

        BigDecimal quantidade;
        try {
            quantidade = new BigDecimal(txtLitros.getText().replace(",", "."));
            if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "A quantidade de litros deve ser maior que zero.", "Erro de Venda", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "A quantidade de litros é inválida. Calcule o valor primeiro.", "Erro de Venda", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String formaPagamento = (String) comboFormaPagamento.getSelectedItem();

        VendaItemRequest itemRequest = new VendaItemRequest(produto.id(), quantidade);
        VendaRequest vendaRequest = new VendaRequest(
                idFrentistaLogado,
                formaPagamento,
                List.of(itemRequest)
        );

        btnVender.setEnabled(false);
        lblStatus.setText("Registrando...");

        SwingWorker<VendaResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected VendaResponse doInBackground() throws Exception {
                return vendaService.registrarVenda(vendaRequest);
            }

            @Override
            protected void done() {
                try {
                    VendaResponse respostaDaVenda = get();
                    String msgSucesso = "Venda ID: " + respostaDaVenda.idVenda() + " registrada com sucesso!\n"
                            + "Valor Total: R$ " + String.format("%.2f", respostaDaVenda.valorTotal()) + "\n\n"
                            + "Deseja imprimir o cupom fiscal?";
                    int resposta = JOptionPane.showConfirmDialog(
                            PainelBomba.this,
                            msgSucesso,
                            "Venda Concluída",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (resposta == JOptionPane.YES_OPTION) {
                        mostrarCupomFiscal(respostaDaVenda);
                    }
                    limparPainel();
                } catch (Exception e) {
                    String errorMessage = "Erro desconhecido ao registrar a venda.";
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
                        System.err.println("Erro inesperado na venda: ");
                        e.printStackTrace();
                        errorMessage = e.getMessage();
                    }
                    JOptionPane.showMessageDialog(PainelBomba.this, errorMessage, "Falha na Venda", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnVender.setEnabled(true);
                    lblStatus.setText("Status: Livre");
                }
            }
        };
        worker.execute();
    }

    private void limparPainel() {
        txtValor.setText("");
        txtLitros.setText("");
        comboCombustivel.setSelectedIndex(0);
        comboFormaPagamento.setSelectedIndex(0);
        lblPrecoLitro.setText("R$ 0,00 / litro");
        precoAtual = BigDecimal.ZERO;
    }

    private String centerString(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;

        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    private void mostrarCupomFiscal(VendaResponse venda) {

        final int LARGURA = 45;
        String separadorLinha = "-".repeat(LARGURA) + "\n";
        String separadorDuplo = "=".repeat(LARGURA) + "\n";

        StringBuilder sb = new StringBuilder(1024);
        sb.append(separadorDuplo);
        sb.append(centerString("PDV Posto Combustível Ltda.", LARGURA)).append("\n");
        sb.append(centerString("CNPJ: 33.000.167/0001-01 | IE: 123.456.789-0", LARGURA)).append("\n");
        sb.append(centerString("Av. Acadêmica, 123 - Bairro Universitário", LARGURA)).append("\n");
        sb.append(separadorDuplo);
        sb.append(centerString("COMPROVANTE DE VENDA", LARGURA)).append("\n");
        sb.append(separadorDuplo);

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        sb.append(String.format("Data/Hora: %" + (LARGURA - 11) + "s\n", venda.dataHora().format(formatter)));
        sb.append(String.format("Venda ID: %" + (LARGURA - 10) + "d\n", venda.idVenda()));
        sb.append(String.format("Frentista: %" + (LARGURA - 11) + "s\n", venda.nomeFrentista()));
        sb.append(String.format("CPF/CNPJ: %" + (LARGURA - 10) + "s\n", "(Opcional)"));
        sb.append(String.format("Forma Pgto: %" + (LARGURA - 12) + "s\n", venda.formaPagamento()));
        sb.append(separadorLinha);

        sb.append("PRODUTO\n");
        sb.append(separadorLinha);

        for (VendaItemResponse item : venda.itens()) {
            String nomeProduto = item.nomeProduto();
            sb.append(nomeProduto).append("\n");

            String linhaPreco = String.format("       %,.3f L x R$ %,.2f",
                    item.quantidade(),
                    item.precoUnitario()
            );

            String linhaTotalItem = String.format("R$ %,.2f", item.precoTotal());

            sb.append(linhaPreco);
            sb.append(" ".repeat(Math.max(0, LARGURA - linhaPreco.length() - linhaTotalItem.length())));
            sb.append(linhaTotalItem).append("\n");
        }
        sb.append(separadorLinha);

        String textoTotal = "VALOR TOTAL:";
        String valorTotal = String.format("R$ %,.2f", venda.valorTotal());
        sb.append(textoTotal);
        sb.append(" ".repeat(Math.max(0, LARGURA - textoTotal.length() - valorTotal.length())));
        sb.append(valorTotal).append("\n");
        sb.append(separadorLinha);

        BigDecimal percentualTributo = new BigDecimal("0.33");
        BigDecimal valorTributo = venda.valorTotal().multiply(percentualTributo).setScale(2, RoundingMode.HALF_UP);

        String linhaTributo = String.format("Valor aprox. de tributos: R$ %,.2f (%.2f%%)",
                valorTributo,
                percentualTributo.multiply(BigDecimal.valueOf(100))
        );
        sb.append(linhaTributo).append("\n\n");

        sb.append(centerString("Obrigado pela preferência!", LARGURA)).append("\n");
        sb.append(centerString("*Comprovante de Projeto - Sem Valor Fiscal*", LARGURA)).append("\n");
        sb.append(separadorDuplo);

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(380, 450));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Cupom Fiscal - Venda ID: " + venda.idVenda(),
                JOptionPane.PLAIN_MESSAGE);
    }
}