import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;

public final class ServidorRmi {
    private static final int PORTA_REGISTRO = 1099;
    private static final String NOME_SERVICO = "FilaAtendimento";

    private ServidorRmi() {}

    public static void main(String[] args) throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        Registry registro = LocateRegistry.createRegistry(PORTA_REGISTRO);
        ServicoFilaRmi servico = new ServicoFilaRmi(new FilaAtendimento());
        registro.rebind(NOME_SERVICO, servico);

        System.out.println("Servidor RMI pronto em 127.0.0.1:" + PORTA_REGISTRO);
        System.out.println("Servico " + NOME_SERVICO + " exportado na porta 1100.");
        System.out.println("Pressione Ctrl+C para encerrar.");
        new CountDownLatch(1).await();
    }
}
