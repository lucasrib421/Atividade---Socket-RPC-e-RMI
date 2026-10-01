import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class TelaFila extends JFrame {
    private final ClienteFila cliente;
    private final List<Integer> senhasCriadas = new ArrayList<>();
    private final JTextField campoNome = new JTextField(16);
    private final JTextField campoSenha = new JTextField(8);
    private final JTextArea quadro = new JTextArea(13, 42);
    private final JLabel aviso = new JLabel("Pronto para atender.");

    public TelaFila(ClienteFila cliente, String mecanismo) {
        super("Fila da Clinica - cliente " + mecanismo);
        this.cliente = cliente;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                TelaFila.this.cliente.close();
            }
        });
        setLayout(new BorderLayout(10, 10));

        JPanel controles = new JPanel(new GridLayout(3, 1, 0, 4));

        JPanel entrada = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton botaoEntrar = new JButton("Entrar na fila");
        entrada.add(new JLabel("Nome:"));
        entrada.add(campoNome);
        entrada.add(botaoEntrar);
        controles.add(entrada);

        JPanel consulta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton botaoConsultar = new JButton("Consultar");
        JButton botaoFinalizar = new JButton("Finalizar atendimento");
        consulta.add(new JLabel("Senha:"));
        consulta.add(campoSenha);
        consulta.add(botaoConsultar);
        consulta.add(botaoFinalizar);
        controles.add(consulta);

        JPanel chamada = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton botaoChamar = new JButton("Chamar proximo");
        chamada.add(botaoChamar);
        controles.add(chamada);

        quadro.setEditable(false);
        add(controles, BorderLayout.NORTH);
        add(new JScrollPane(quadro), BorderLayout.CENTER);
        add(aviso, BorderLayout.SOUTH);

        botaoEntrar.addActionListener(evento -> executar(() -> {
            FilaAtendimento.Consulta entradaCriada = cliente.entrarNaFila(campoNome.getText());
            senhasCriadas.add(entradaCriada.senha());
            campoSenha.setText(Integer.toString(entradaCriada.senha()));
            campoNome.setText("");
            aviso.setText("Senha " + entradaCriada.senha() + " criada.");
        }));
        botaoConsultar.addActionListener(evento -> executar(() -> {
            FilaAtendimento.Consulta resultado = cliente.consultarPosicao(lerSenha());
            String posicao = resultado.posicao() == null
                    ? "sem posicao na espera"
                    : "posicao " + resultado.posicao();
            aviso.setText(resultado.nome() + ": " + resultado.estado() + ", " + posicao + ".");
        }));
        botaoChamar.addActionListener(evento -> executar(() -> {
            FilaAtendimento.Consulta pessoa = cliente.chamarProximo();
            campoSenha.setText(Integer.toString(pessoa.senha()));
            aviso.setText("Chamado: " + pessoa.nome() + " (senha " + pessoa.senha() + ").");
        }));
        botaoFinalizar.addActionListener(evento -> executar(() -> {
            int senha = lerSenha();
            cliente.finalizarAtendimento(senha);
            aviso.setText("Atendimento da senha " + senha + " finalizado.");
        }));

        quadro.setText("FILA DE ESPERA\nNinguem aguardando.\n\nEM ATENDIMENTO\nNenhum.\n\nFINALIZADOS\nNenhum.\n");
        pack();
        setLocationRelativeTo(null);
    }

    private int lerSenha() {
        try {
            return Integer.parseInt(campoSenha.getText().trim());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Digite uma senha numerica.");
        }
    }

    @FunctionalInterface
    private interface OperacaoTela {
        void executar() throws IOException;
    }

    private void executar(OperacaoTela operacao) {
        try {
            operacao.executar();
            atualizarQuadro();
        } catch (IOException erro) {
            aviso.setText("Nao foi possivel comunicar com o servidor: " + erro.getMessage());
        } catch (RuntimeException erro) {
            aviso.setText("Pedido recusado: " + erro.getMessage());
        }
    }

    private void atualizarQuadro() {
        List<FilaAtendimento.Consulta> consultas = new ArrayList<>();
        for (int senha : senhasCriadas) {
            try {
                consultas.add(cliente.consultarPosicao(senha));
            } catch (IOException | RuntimeException erro) {
                aviso.setText("Falha ao atualizar a fila pelo servidor: " + erro.getMessage());
                return;
            }
        }

        StringBuilder texto = new StringBuilder("FILA DE ESPERA\n");
        boolean existeAguardando = false;
        for (FilaAtendimento.Consulta consulta : consultas) {
            if (consulta.estado() == FilaAtendimento.Estado.AGUARDANDO) {
                texto.append(consulta.posicao()).append(". ")
                        .append(consulta.nome()).append(" (senha ").append(consulta.senha()).append(")\n");
                existeAguardando = true;
            }
        }
        if (!existeAguardando) {
            texto.append("Ninguem aguardando.\n");
        }

        texto.append("\nEM ATENDIMENTO\n");
        boolean existeAtendimento = false;
        for (FilaAtendimento.Consulta consulta : consultas) {
            if (consulta.estado() == FilaAtendimento.Estado.EM_ATENDIMENTO) {
                texto.append(consulta.nome()).append(" (senha ").append(consulta.senha()).append(")\n");
                existeAtendimento = true;
            }
        }
        if (!existeAtendimento) {
            texto.append("Nenhum.\n");
        }

        texto.append("\nFINALIZADOS\n");
        boolean existeFinalizado = false;
        for (FilaAtendimento.Consulta consulta : consultas) {
            if (consulta.estado() == FilaAtendimento.Estado.FINALIZADO) {
                texto.append(consulta.nome()).append(" (senha ").append(consulta.senha()).append(")\n");
                existeFinalizado = true;
            }
        }
        if (!existeFinalizado) {
            texto.append("Nenhum.\n");
        }
        quadro.setText(texto.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaFila(new ClienteSocket(), "Socket").setVisible(true));
    }
}
