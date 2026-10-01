import java.util.NoSuchElementException;

public class VerificarFila {
    public static void main(String[] args) {
        FilaAtendimento fila = new FilaAtendimento();
        FilaAtendimento.Consulta entradaAna = fila.entrarNaFila("Ana");
        FilaAtendimento.Consulta entradaBruno = fila.entrarNaFila("Bruno");
        int senhaAna = entradaAna.senha();
        int senhaBruno = entradaBruno.senha();

        if (senhaAna != 1 || senhaBruno != 2 ||
                entradaAna.posicao() != 1 || entradaBruno.posicao() != 2) {
            throw new AssertionError("As senhas deveriam seguir a ordem de entrada.");
        }
        if (fila.consultarPosicao(senhaAna).posicao() != 1 ||
                fila.consultarPosicao(senhaBruno).posicao() != 2) {
            throw new AssertionError("As posicoes deveriam seguir a ordem de entrada.");
        }

        try {
            fila.entrarNaFila("  ");
            throw new AssertionError("Nome vazio deveria ser recusado.");
        } catch (IllegalArgumentException esperado) {
            // A entrada invalida foi recusada.
        }

        try {
            fila.consultarPosicao(999);
            throw new AssertionError("Senha desconhecida deveria ser recusada.");
        } catch (NoSuchElementException esperado) {
            // A consulta invalida foi recusada.
        }

        if (fila.consultarPosicao(senhaBruno).posicao() != 2) {
            throw new AssertionError("Pedidos invalidos nao deveriam alterar a fila.");
        }

        System.out.println("Ana: senha 1, posicao 1");
        System.out.println("Bruno: senha 2, posicao 2");
        System.out.println("Entradas e consultas invalidas foram recusadas.");

        FilaAtendimento.Chamada primeiraChamada = fila.chamarProximo();
        if (primeiraChamada.senha() != senhaAna || !primeiraChamada.nome().equals("Ana")) {
            throw new AssertionError("Ana deveria ser chamada primeiro.");
        }
        if (fila.consultarPosicao(senhaAna).estado() != FilaAtendimento.Estado.EM_ATENDIMENTO ||
                fila.consultarPosicao(senhaAna).posicao() != null ||
                fila.consultarPosicao(senhaBruno).posicao() != 1) {
            throw new AssertionError("As situacoes apos a chamada estao incorretas.");
        }

        try {
            fila.chamarProximo();
            throw new AssertionError("Nao deveria haver dois atendimentos simultaneos.");
        } catch (IllegalStateException esperado) {
            // Ana continua em atendimento.
        }
        try {
            fila.finalizarAtendimento(senhaBruno);
            throw new AssertionError("Bruno ainda nao foi chamado.");
        } catch (IllegalStateException esperado) {
            // A senha em atendimento continua sendo a de Ana.
        }

        fila.finalizarAtendimento(senhaAna);
        if (fila.consultarPosicao(senhaAna).estado() != FilaAtendimento.Estado.FINALIZADO) {
            throw new AssertionError("Ana deveria estar com atendimento finalizado.");
        }
        try {
            fila.finalizarAtendimento(senhaAna);
            throw new AssertionError("Finalizar duas vezes deveria ser recusado.");
        } catch (IllegalStateException esperado) {
            // O atendimento continua finalizado.
        }

        FilaAtendimento.Chamada segundaChamada = fila.chamarProximo();
        if (segundaChamada.senha() != senhaBruno) {
            throw new AssertionError("Bruno deveria ser chamado depois de Ana.");
        }
        fila.finalizarAtendimento(senhaBruno);
        try {
            fila.chamarProximo();
            throw new AssertionError("A fila vazia deveria ser informada.");
        } catch (NoSuchElementException esperado) {
            // Ninguem ficou aguardando.
        }

        System.out.println("Ana foi chamada e finalizada; depois Bruno foi chamado e finalizado.");
        System.out.println("Chamadas e finalizacoes invalidas foram recusadas.");
    }
}
