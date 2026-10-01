import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;

public final class ServidorSocket {
    private static final int PORTA_PADRAO = 5000;
    private final int porta;
    private final boolean registrarRequisicoes;

    // A fila oficial vivera no servidor e sera atualizada ao receber pedidos.
    private final FilaAtendimento fila = new FilaAtendimento();

    private ServidorSocket(int porta, boolean registrarRequisicoes) {
        if (porta < 1 || porta > 65535) {
            throw new IllegalArgumentException("A porta deve estar entre 1 e 65535.");
        }
        this.porta = porta;
        this.registrarRequisicoes = registrarRequisicoes;
    }

    private void iniciar() throws IOException {
        InetAddress enderecoLocal = InetAddress.getByName("127.0.0.1");
        try (ServerSocket servidor = new ServerSocket(porta, 50, enderecoLocal)) {
            System.out.println("Servidor da fila ouvindo em 127.0.0.1:" + porta);
            System.out.println("Pressione Ctrl+C para encerrar.");

            while (true) {
                Socket conexao = servidor.accept();
                atenderUmaRequisicao(conexao);
            }
        }
    }

    private void atenderUmaRequisicao(Socket conexao) throws IOException {
        try (Socket cliente = conexao;
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter saida = new PrintWriter(
                     new OutputStreamWriter(cliente.getOutputStream(), StandardCharsets.UTF_8), true)) {
            if (registrarRequisicoes) {
                System.out.println("Conexao recebida de " + cliente.getRemoteSocketAddress());
            }
            String requisicao = entrada.readLine();
            String resposta = processar(requisicao);
            saida.println(resposta);
            if (registrarRequisicoes) {
                System.out.println("Resposta enviada: " + resposta);
            }
        }
    }

    private String processar(String requisicao) {
        if (requisicao == null) {
            return "ERRO|Requisicao vazia.";
        }

        int separador = requisicao.indexOf('|');
        if (separador < 0) {
            return "ERRO|Formato esperado: ENTRAR|nome.";
        }

        String operacao = requisicao.substring(0, separador);
        String argumento = requisicao.substring(separador + 1);
        if (operacao.equals("ENTRAR")) {
            try {
                return serializar(fila.entrarNaFila(argumento));
            } catch (IllegalArgumentException erro) {
                return "ERRO|" + erro.getMessage();
            }
        }

        if (operacao.equals("CONSULTAR")) {
            try {
                int senha = Integer.parseInt(argumento);
                return serializar(fila.consultarPosicao(senha));
            } catch (NumberFormatException erro) {
                return "ERRO|A senha deve ser numerica.";
            } catch (NoSuchElementException erro) {
                return "ERRO|" + erro.getMessage();
            }
        }

        if (operacao.equals("CHAMAR")) {
            if (!argumento.isEmpty()) {
                return "ERRO|A operacao CHAMAR nao recebe senha.";
            }
            try {
                FilaAtendimento.Chamada chamada = fila.chamarProximo();
                return serializar(fila.consultarPosicao(chamada.senha()));
            } catch (IllegalStateException | NoSuchElementException erro) {
                return "ERRO|" + erro.getMessage();
            }
        }

        if (operacao.equals("FINALIZAR")) {
            try {
                int senha = Integer.parseInt(argumento);
                fila.finalizarAtendimento(senha);
                return serializar(fila.consultarPosicao(senha));
            } catch (NumberFormatException erro) {
                return "ERRO|A senha deve ser numerica.";
            } catch (IllegalStateException | NoSuchElementException erro) {
                return "ERRO|" + erro.getMessage();
            }
        }

        return "ERRO|Operacao ainda nao suportada: " + operacao;
    }

    private String serializar(FilaAtendimento.Consulta consulta) {
        String posicao = consulta.posicao() == null ? "-" : consulta.posicao().toString();
        return "OK|" + consulta.senha() + "|" + consulta.estado()
                + "|" + posicao + "|" + consulta.nome();
    }

    public static void main(String[] args) throws IOException {
        int porta = PORTA_PADRAO;
        boolean portaInformada = false;
        boolean registrarRequisicoes = true;
        try {
            for (String argumento : args) {
                if (argumento.equalsIgnoreCase("--quiet")) {
                    registrarRequisicoes = false;
                } else if (!portaInformada) {
                    porta = Integer.parseInt(argumento);
                    portaInformada = true;
                } else {
                    throw new IllegalArgumentException("Argumento extra: " + argumento);
                }
            }
            new ServidorSocket(porta, registrarRequisicoes).iniciar();
        } catch (IllegalArgumentException erro) {
            System.err.println("Falha ao iniciar servidor: " + erro.getMessage());
            System.err.println("Uso: ServidorSocket [porta] [--quiet]");
            System.exit(1);
        }
    }
}
