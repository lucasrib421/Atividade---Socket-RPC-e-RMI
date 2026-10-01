import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public final class TelaFilaRmi {
    private TelaFilaRmi() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TelaFila(new ClienteRmi(), "RMI").setVisible(true);
            } catch (IOException erro) {
                JOptionPane.showMessageDialog(null,
                        "Nao foi possivel conectar ao servidor RMI: " + erro.getMessage(),
                        "Falha de conexao", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
