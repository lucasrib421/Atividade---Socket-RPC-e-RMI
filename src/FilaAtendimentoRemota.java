import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FilaAtendimentoRemota extends Remote {
    ConsultaRemota entrarNaFila(String nome) throws RemoteException;

    ConsultaRemota consultarPosicao(int senha) throws RemoteException;

    ConsultaRemota chamarProximo() throws RemoteException;

    ConsultaRemota finalizarAtendimento(int senha) throws RemoteException;
}
