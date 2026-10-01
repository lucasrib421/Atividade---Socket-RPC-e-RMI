import java.io.IOException;

public interface ClienteFila extends AutoCloseable {
    FilaAtendimento.Consulta entrarNaFila(String nome) throws IOException;

    FilaAtendimento.Consulta consultarPosicao(int senha) throws IOException;

    FilaAtendimento.Consulta chamarProximo() throws IOException;

    FilaAtendimento.Consulta finalizarAtendimento(int senha) throws IOException;

    @Override
    void close();
}
