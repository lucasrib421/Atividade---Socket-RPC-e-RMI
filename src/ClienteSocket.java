import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class ClienteSocket implements ClienteFila {
    private static final String SERVIDOR_PADRAO = "127.0.0.1";
    private static final int PORTA_PADRAO = 5000;
    private final String servidor;
    private final int porta;

    public ClienteSocket() {
        this(SERVIDOR_PADRAO, PORTA_PADRAO);
    }

    public ClienteSocket(int porta) {
        this(SERVIDOR_PADRAO, porta);
    }

    public ClienteSocket(String servidor, int porta) {
        if (servidor == null || servidor.isBlank()) {
            throw new IllegalArgumentException("O endereco do servidor nao pode estar vazio.");
        }
        if (porta < 1 || porta > 65535) {
            throw new IllegalArgumentException("A porta deve estar entre 1 e 65535.");
        }
        this.servidor = servidor;
        this.porta = porta;
    }

    @Override
    public FilaAtendimento.Consulta entrarNaFila(String nome) throws IOException {
        return interpretarConsulta(trocarMensagem("ENTRAR|" + nome));
    }

    @Override
    public FilaAtendimento.Consulta consultarPosicao(int senha) throws IOException {
        return interpretarConsulta(trocarMensagem("CONSULTAR|" + senha));
    }

    @Override
    public FilaAtendimento.Consulta chamarProximo() throws IOException {
        return interpretarConsulta(trocarMensagem("CHAMAR|"));
    }

    @Override
    public FilaAtendimento.Consulta finalizarAtendimento(int senha) throws IOException {
        return interpretarConsulta(trocarMensagem("FINALIZAR|" + senha));
    }

    private String trocarMensagem(String requisicao) throws IOException {
        try (Socket conexao = new Socket(servidor, porta);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter saida = new PrintWriter(
                     new OutputStreamWriter(conexao.getOutputStream(), StandardCharsets.UTF_8), true)) {
            saida.println(requisicao);

            String resposta = entrada.readLine();
            if (resposta == null) {
                throw new IOException("O servidor encerrou a conexao sem responder.");
            }
            if (resposta.startsWith("ERRO|")) {
                throw new IllegalArgumentException(resposta.substring("ERRO|".length()));
            }
            return resposta;
        }
    }

    private FilaAtendimento.Consulta interpretarConsulta(String resposta) throws IOException {
        // O quinto campo contem o restante da linha, incluindo possiveis barras verticais no nome.
        String[] partes = resposta.split("\\|", 5);
        if (partes.length != 5 || !partes[0].equals("OK")) {
            throw new IOException("Resposta do servidor fora do formato esperado: " + resposta);
        }

        try {
            int senha = Integer.parseInt(partes[1]);
            FilaAtendimento.Estado estado = FilaAtendimento.Estado.valueOf(partes[2]);
            Integer posicao = partes[3].equals("-") ? null : Integer.parseInt(partes[3]);
            return new FilaAtendimento.Consulta(senha, partes[4], estado, posicao);
        } catch (IllegalArgumentException erro) {
            throw new IOException("O servidor enviou dados invalidos: " + resposta, erro);
        }
    }

    @Override
    public void close() {
        // O cliente Socket abre e fecha a conexao em cada operacao.
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            imprimirUso();
            System.exit(1);
        }

        String operacao = args[0].toUpperCase();
        boolean chamar = operacao.equals("CHAMAR");
        boolean entrar = operacao.equals("ENTRAR");
        boolean consultarOuFinalizar = operacao.equals("CONSULTAR") || operacao.equals("FINALIZAR");
        boolean argumentosValidos = chamar && (args.length == 1 || args.length == 2)
                || entrar && (args.length == 2 || args.length == 3)
                || consultarOuFinalizar && (args.length == 2 || args.length == 3);
        if (!argumentosValidos) {
            imprimirUso();
            System.exit(1);
        }

        try {
            boolean portaInformada = chamar && args.length == 2
                    || (entrar || consultarOuFinalizar) && args.length == 3;
            int porta = portaInformada ? Integer.parseInt(args[args.length - 1]) : PORTA_PADRAO;
            ClienteSocket cliente = new ClienteSocket(porta);
            FilaAtendimento.Consulta resultado;
            if (entrar) {
                resultado = cliente.entrarNaFila(args[1]);
            } else if (operacao.equals("CONSULTAR")) {
                resultado = cliente.consultarPosicao(Integer.parseInt(args[1]));
            } else if (chamar) {
                resultado = cliente.chamarProximo();
            } else if (operacao.equals("FINALIZAR")) {
                resultado = cliente.finalizarAtendimento(Integer.parseInt(args[1]));
            } else {
                throw new IllegalArgumentException("Operacao nao reconhecida: " + args[0]);
            }

            String posicao = resultado.posicao() == null
                    ? "sem posicao na espera"
                    : resultado.posicao().toString();
            System.out.println("Recebida do servidor: senha=" + resultado.senha()
                    + ", paciente=" + resultado.nome()
                    + ", estado=" + resultado.estado()
                    + ", posicao=" + posicao);
        } catch (IOException | IllegalArgumentException erro) {
            System.err.println("Falha no pedido: " + erro.getMessage());
            System.exit(1);
        }
    }

    private static void imprimirUso() {
        System.err.println("Uso: ClienteSocket ENTRAR \"Nome\" [porta], CONSULTAR senha [porta], CHAMAR [porta] ou FINALIZAR senha [porta]");
    }
}
