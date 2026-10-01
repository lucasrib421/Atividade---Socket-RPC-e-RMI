import javax.swing.SwingUtilities;

public final class TelaFilaRpc {
    private TelaFilaRpc() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaFila(new ClienteRpc(), "RPC (gRPC)").setVisible(true));
    }
}
