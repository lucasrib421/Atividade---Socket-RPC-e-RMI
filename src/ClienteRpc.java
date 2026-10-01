import fila.rpc.ChamarProximoRequest;
import fila.rpc.ConsultaResponse;
import fila.rpc.ConsultarPosicaoRequest;
import fila.rpc.EntrarNaFilaRequest;
import fila.rpc.Estado;
import fila.rpc.FinalizarAtendimentoRequest;
import fila.rpc.FilaAtendimentoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class ClienteRpc implements ClienteFila {
    private final ManagedChannel canal;
    private final FilaAtendimentoServiceGrpc.FilaAtendimentoServiceBlockingStub cliente;

    public ClienteRpc() {
        canal = ManagedChannelBuilder.forAddress("127.0.0.1", 50051)
                .usePlaintext()
                .build();
        cliente = FilaAtendimentoServiceGrpc.newBlockingStub(canal);
    }

    @Override
    public FilaAtendimento.Consulta entrarNaFila(String nome) {
        ConsultaResponse resposta = cliente.entrarNaFila(
                EntrarNaFilaRequest.newBuilder().setNome(nome).build());
        return paraConsulta(resposta);
    }

    @Override
    public FilaAtendimento.Consulta consultarPosicao(int senha) {
        ConsultaResponse resposta = cliente.consultarPosicao(
                ConsultarPosicaoRequest.newBuilder().setSenha(senha).build());
        return paraConsulta(resposta);
    }

    @Override
    public FilaAtendimento.Consulta chamarProximo() {
        ConsultaResponse resposta = cliente.chamarProximo(ChamarProximoRequest.getDefaultInstance());
        return paraConsulta(resposta);
    }

    @Override
    public FilaAtendimento.Consulta finalizarAtendimento(int senha) {
        ConsultaResponse resposta = cliente.finalizarAtendimento(
                FinalizarAtendimentoRequest.newBuilder().setSenha(senha).build());
        return paraConsulta(resposta);
    }

    @Override
    public void close() {
        canal.shutdown();
        try {
            if (!canal.awaitTermination(2, TimeUnit.SECONDS)) {
                canal.shutdownNow();
            }
        } catch (InterruptedException erro) {
            canal.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static FilaAtendimento.Consulta paraConsulta(ConsultaResponse resposta) {
        FilaAtendimento.Estado estado = switch (resposta.getEstado()) {
            case AGUARDANDO -> FilaAtendimento.Estado.AGUARDANDO;
            case EM_ATENDIMENTO -> FilaAtendimento.Estado.EM_ATENDIMENTO;
            case FINALIZADO -> FilaAtendimento.Estado.FINALIZADO;
            case ESTADO_NAO_DEFINIDO, UNRECOGNIZED ->
                    throw new IllegalStateException("O servidor retornou um estado desconhecido.");
        };
        Integer posicao = resposta.hasPosicao() ? resposta.getPosicao() : null;
        return new FilaAtendimento.Consulta(resposta.getSenha(), resposta.getNome(), estado, posicao);
    }

    public static void main(String[] args) {
        boolean chamar = args.length == 1 && args[0].equalsIgnoreCase("CHAMAR");
        boolean entrar = args.length >= 2 && args[0].equalsIgnoreCase("ENTRAR");
        boolean outraOperacao = args.length == 2
                && (args[0].equalsIgnoreCase("CONSULTAR") || args[0].equalsIgnoreCase("FINALIZAR"));
        if (!chamar && !entrar && !outraOperacao) {
            System.err.println("Uso: ClienteRpc ENTRAR \"Nome\", CONSULTAR senha, CHAMAR ou FINALIZAR senha");
            System.exit(1);
        }

        try (ClienteRpc rpc = new ClienteRpc()) {
            FilaAtendimento.Consulta resultado = switch (args[0].toUpperCase()) {
                case "ENTRAR" -> rpc.entrarNaFila(
                        String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                case "CONSULTAR" -> rpc.consultarPosicao(Integer.parseInt(args[1]));
                case "CHAMAR" -> rpc.chamarProximo();
                case "FINALIZAR" -> rpc.finalizarAtendimento(Integer.parseInt(args[1]));
                default -> throw new IllegalArgumentException("Operacao nao reconhecida: " + args[0]);
            };
            String posicao = resultado.posicao() == null
                    ? "sem posicao na espera"
                    : Integer.toString(resultado.posicao());
            System.out.println("Recebida do servidor: senha=" + resultado.senha()
                    + ", paciente=" + resultado.nome()
                    + ", estado=" + resultado.estado()
                    + ", posicao=" + posicao);
        } catch (RuntimeException erro) {
            System.err.println("Falha no pedido: " + erro.getMessage());
            System.exit(1);
        }
    }
}
