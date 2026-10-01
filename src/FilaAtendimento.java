import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class FilaAtendimento {
    public enum Estado {
        AGUARDANDO,
        EM_ATENDIMENTO,
        FINALIZADO
    }

    public record Consulta(int senha, String nome, Estado estado, Integer posicao) {}

    public record Chamada(int senha, String nome) {}

    private final Queue<Integer> senhasAguardando = new ArrayDeque<>();
    private final Map<Integer, String> nomesPorSenha = new HashMap<>();
    private final Map<Integer, Estado> estadosPorSenha = new HashMap<>();
    private int proximaSenha = 1;
    private Integer senhaEmAtendimento;

    public synchronized Consulta entrarNaFila(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome nao pode estar vazio.");
        }

        int senha = proximaSenha++;
        nomesPorSenha.put(senha, nome.trim());
        estadosPorSenha.put(senha, Estado.AGUARDANDO);
        senhasAguardando.add(senha);
        return new Consulta(senha, nome.trim(), Estado.AGUARDANDO, senhasAguardando.size());
    }

    public synchronized Consulta consultarPosicao(int senha) {
        if (!nomesPorSenha.containsKey(senha)) {
            throw new NoSuchElementException("Senha nao encontrada: " + senha);
        }

        Estado estado = estadosPorSenha.get(senha);
        Integer posicao = null;
        if (estado == Estado.AGUARDANDO) {
            int indice = 1;
            for (int senhaAguardando : senhasAguardando) {
                if (senhaAguardando == senha) {
                    posicao = indice;
                    break;
                }
                indice++;
            }
            if (posicao == null) {
                throw new IllegalStateException("A senha esta aguardando, mas nao consta na fila.");
            }
        }

        return new Consulta(senha, nomesPorSenha.get(senha), estado, posicao);
    }

    public synchronized Chamada chamarProximo() {
        if (senhaEmAtendimento != null) {
            throw new IllegalStateException("Ja existe um atendimento em andamento.");
        }

        Integer senha = senhasAguardando.poll();
        if (senha == null) {
            throw new NoSuchElementException("Nao ha pacientes aguardando.");
        }

        senhaEmAtendimento = senha;
        estadosPorSenha.put(senha, Estado.EM_ATENDIMENTO);
        return new Chamada(senha, nomesPorSenha.get(senha));
    }

    public synchronized void finalizarAtendimento(int senha) {
        if (!nomesPorSenha.containsKey(senha)) {
            throw new NoSuchElementException("Senha nao encontrada: " + senha);
        }
        if (estadosPorSenha.get(senha) == Estado.FINALIZADO) {
            throw new IllegalStateException("Atendimento ja finalizado.");
        }
        if (senhaEmAtendimento == null || senhaEmAtendimento != senha) {
            throw new IllegalStateException("Esta senha nao esta em atendimento.");
        }

        estadosPorSenha.put(senha, Estado.FINALIZADO);
        senhaEmAtendimento = null;
    }
}
