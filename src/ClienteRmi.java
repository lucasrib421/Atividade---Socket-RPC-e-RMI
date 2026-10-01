import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public final class ClienteRmi implements ClienteFila {
    private static final int PORTA_REGISTRO = 1099;
    private static final String NOME_SERVICO = "FilaAtendimento";
    private final FilaAtendimentoRemota servico;

    public ClienteRmi() throws IOException {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        try {
            Registry registro = LocateRegistry.getRegistry("127.0.0.1", PORTA_REGISTRO);
            servico = (FilaAtendimentoRemota) registro.lookup(NOME_SERVICO);
        } catch (NotBoundException erro) {
            throw new IOException("O servico RMI ainda nao foi registrado.", erro);
        }
    }

    @Override
    public FilaAtendimento.Consulta entrarNaFila(String nome) throws IOException {
        return converter(servico.entrarNaFila(nome));
    }

    @Override
    public FilaAtendimento.Consulta consultarPosicao(int senha) throws IOException {
        return converter(servico.consultarPosicao(senha));
    }

    @Override
    public FilaAtendimento.Consulta chamarProximo() throws IOException {
        return converter(servico.chamarProximo());
    }

    @Override
    public FilaAtendimento.Consulta finalizarAtendimento(int senha) throws IOException {
        return converter(servico.finalizarAtendimento(senha));
    }

    @Override
    public void close() {
        // O cliente RMI mantem apenas a referencia remota, sem canal local para encerrar.
    }

    private static FilaAtendimento.Consulta converter(ConsultaRemota consulta) {
        return new FilaAtendimento.Consulta(
                consulta.senha(), consulta.nome(), consulta.estado(), consulta.posicao());
    }

    public static void main(String[] args) {
        boolean chamar = args.length == 1 && args[0].equalsIgnoreCase("CHAMAR");
        boolean entrar = args.length >= 2 && args[0].equalsIgnoreCase("ENTRAR");
        boolean outraOperacao = args.length == 2
                && (args[0].equalsIgnoreCase("CONSULTAR") || args[0].equalsIgnoreCase("FINALIZAR"));
        if (!chamar && !entrar && !outraOperacao) {
            System.err.println("Uso: ClienteRmi ENTRAR \"Nome\", CONSULTAR senha, CHAMAR ou FINALIZAR senha");
            System.exit(1);
        }

        try (ClienteRmi rmi = new ClienteRmi()) {
            FilaAtendimento.Consulta resultado = switch (args[0].toUpperCase()) {
                case "ENTRAR" -> rmi.entrarNaFila(
                        String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                case "CONSULTAR" -> rmi.consultarPosicao(Integer.parseInt(args[1]));
                case "CHAMAR" -> rmi.chamarProximo();
                case "FINALIZAR" -> rmi.finalizarAtendimento(Integer.parseInt(args[1]));
                default -> throw new IllegalArgumentException("Operacao nao reconhecida: " + args[0]);
            };
            String posicao = resultado.posicao() == null
                    ? "sem posicao na espera"
                    : Integer.toString(resultado.posicao());
            System.out.println("Recebida do servidor: senha=" + resultado.senha()
                    + ", paciente=" + resultado.nome()
                    + ", estado=" + resultado.estado()
                    + ", posicao=" + posicao);
        } catch (IOException | RuntimeException erro) {
            System.err.println("Falha no pedido: " + erro.getMessage());
            System.exit(1);
        }
    }
}
