import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public final class ServicoFilaRmi extends UnicastRemoteObject implements FilaAtendimentoRemota {
    private static final int PORTA_SERVICO = 1100;
    private final FilaAtendimento fila;

    public ServicoFilaRmi(FilaAtendimento fila) throws RemoteException {
        super(PORTA_SERVICO);
        this.fila = fila;
    }

    @Override
    public ConsultaRemota entrarNaFila(String nome) throws RemoteException {
        return converter(fila.entrarNaFila(nome));
    }

    @Override
    public ConsultaRemota consultarPosicao(int senha) throws RemoteException {
        return converter(fila.consultarPosicao(senha));
    }

    @Override
    public ConsultaRemota chamarProximo() throws RemoteException {
        FilaAtendimento.Chamada chamada = fila.chamarProximo();
        return converter(fila.consultarPosicao(chamada.senha()));
    }

    @Override
    public ConsultaRemota finalizarAtendimento(int senha) throws RemoteException {
        fila.finalizarAtendimento(senha);
        return converter(fila.consultarPosicao(senha));
    }

    private static ConsultaRemota converter(FilaAtendimento.Consulta consulta) {
        return new ConsultaRemota(consulta.senha(), consulta.nome(), consulta.estado(), consulta.posicao());
    }
}
