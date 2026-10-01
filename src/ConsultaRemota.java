import java.io.Serializable;

public record ConsultaRemota(
        int senha,
        String nome,
        FilaAtendimento.Estado estado,
        Integer posicao) implements Serializable {
    private static final long serialVersionUID = 1L;
}
