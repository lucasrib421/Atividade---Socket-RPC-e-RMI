import fila.rpc.ChamarProximoRequest;
import fila.rpc.ConsultaResponse;
import fila.rpc.ConsultarPosicaoRequest;
import fila.rpc.EntrarNaFilaRequest;
import fila.rpc.Estado;
import fila.rpc.FinalizarAtendimentoRequest;
import fila.rpc.FilaAtendimentoServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.NoSuchElementException;

public final class ServidorRpc {
    private static final int PORTA = 50051;

    private ServidorRpc() {}

    public static void main(String[] args) throws IOException, InterruptedException {
        FilaAtendimento fila = new FilaAtendimento();
        Server servidor = ServerBuilder.forPort(PORTA)
                .addService(new ServicoFila(fila))
                .build()
                .start();
        System.out.println("Servidor RPC ouvindo em 127.0.0.1:" + PORTA);
        System.out.println("Pressione Ctrl+C para encerrar.");
        servidor.awaitTermination();
    }

    private static final class ServicoFila extends FilaAtendimentoServiceGrpc.FilaAtendimentoServiceImplBase {
        private final FilaAtendimento fila;

        private ServicoFila(FilaAtendimento fila) {
            this.fila = fila;
        }

        @Override
        public void entrarNaFila(EntrarNaFilaRequest request, StreamObserver<ConsultaResponse> resposta) {
            executar(resposta, () -> resposta.onNext(paraResposta(fila.entrarNaFila(request.getNome()))));
        }

        @Override
        public void consultarPosicao(ConsultarPosicaoRequest request, StreamObserver<ConsultaResponse> resposta) {
            executar(resposta, () -> resposta.onNext(paraResposta(fila.consultarPosicao(request.getSenha()))));
        }

        @Override
        public void chamarProximo(ChamarProximoRequest request, StreamObserver<ConsultaResponse> resposta) {
            executar(resposta, () -> {
                FilaAtendimento.Chamada chamada = fila.chamarProximo();
                resposta.onNext(paraResposta(fila.consultarPosicao(chamada.senha())));
            });
        }

        @Override
        public void finalizarAtendimento(FinalizarAtendimentoRequest request, StreamObserver<ConsultaResponse> resposta) {
            executar(resposta, () -> {
                fila.finalizarAtendimento(request.getSenha());
                resposta.onNext(paraResposta(fila.consultarPosicao(request.getSenha())));
            });
        }

        private static void executar(StreamObserver<ConsultaResponse> resposta, Runnable operacao) {
            try {
                operacao.run();
                resposta.onCompleted();
            } catch (IllegalArgumentException erro) {
                resposta.onError(Status.INVALID_ARGUMENT.withDescription(erro.getMessage()).asRuntimeException());
            } catch (NoSuchElementException erro) {
                resposta.onError(Status.NOT_FOUND.withDescription(erro.getMessage()).asRuntimeException());
            } catch (IllegalStateException erro) {
                resposta.onError(Status.FAILED_PRECONDITION.withDescription(erro.getMessage()).asRuntimeException());
            }
        }

        private static ConsultaResponse paraResposta(FilaAtendimento.Consulta consulta) {
            ConsultaResponse.Builder resposta = ConsultaResponse.newBuilder()
                    .setSenha(consulta.senha())
                    .setNome(consulta.nome())
                    .setEstado(switch (consulta.estado()) {
                        case AGUARDANDO -> Estado.AGUARDANDO;
                        case EM_ATENDIMENTO -> Estado.EM_ATENDIMENTO;
                        case FINALIZADO -> Estado.FINALIZADO;
                    });
            if (consulta.posicao() != null) {
                resposta.setPosicao(consulta.posicao());
            }
            return resposta.build();
        }
    }
}
