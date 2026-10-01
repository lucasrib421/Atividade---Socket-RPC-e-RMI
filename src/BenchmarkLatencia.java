import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class BenchmarkLatencia {
    private static final int AQUECIMENTO = 200;
    private static final int RODADAS = 5;
    private static final int AMOSTRAS_POR_RODADA = 1000;
    private static final int TOTAL_AMOSTRAS = RODADAS * AMOSTRAS_POR_RODADA;

    private record Alvo(String nome, ClienteFila cliente, int senha) {}
    private record Resultados(long[][] latencias, long[][] temposDeRodada) {}

    private BenchmarkLatencia() {}

    public static void main(String[] args) throws Exception {
        Path arquivoCsv = args.length == 0
                ? Path.of("docs", "evidencias", "metricas-2026-10-01", "latencias.csv")
                : Path.of(args[0]);
        Path arquivoResumo = arquivoCsv.resolveSibling("resumo.csv");
        Path arquivoMetadados = arquivoCsv.resolveSibling("metadados.txt");
        Path pastaSaida = arquivoCsv.toAbsolutePath().getParent();
        Files.createDirectories(pastaSaida);

        try (ClienteSocket socket = new ClienteSocket();
             ClienteRpc rpc = new ClienteRpc();
             ClienteRmi rmi = new ClienteRmi()) {
            List<Alvo> alvos = List.of(
                    prepararAlvo("Socket", socket),
                    prepararAlvo("RPC", rpc),
                    prepararAlvo("RMI", rmi));
            Resultados resultados = medir(alvos);
            salvarCsv(arquivoCsv, alvos, resultados.latencias());
            salvarResumo(arquivoResumo, alvos, resultados.latencias(), resultados.temposDeRodada());
            salvarMetadados(arquivoMetadados, arquivoCsv);
            imprimirResumo(alvos, resultados.latencias(), resultados.temposDeRodada());
        }
    }

    private static Alvo prepararAlvo(String nome, ClienteFila cliente) throws IOException {
        FilaAtendimento.Consulta entrada = cliente.entrarNaFila("PacienteMetricas");
        if (entrada.posicao() == null || entrada.posicao() != 1) {
            throw new IllegalStateException("A fila " + nome
                    + " nao estava vazia. Reinicie os tres servidores e rode o benchmark novamente.");
        }
        return new Alvo(nome, cliente, entrada.senha());
    }

    private static Resultados medir(List<Alvo> alvos) throws IOException {
        long[][] latencias = new long[alvos.size()][TOTAL_AMOSTRAS];
        long[][] temposDeRodada = new long[alvos.size()][RODADAS];

        for (Alvo alvo : alvos) {
            for (int i = 0; i < AQUECIMENTO; i++) {
                validarResposta(alvo, alvo.cliente().consultarPosicao(alvo.senha()));
            }
        }

        for (int rodada = 0; rodada < RODADAS; rodada++) {
            for (int deslocamento = 0; deslocamento < alvos.size(); deslocamento++) {
                int indiceAlvo = (rodada + deslocamento) % alvos.size();
                Alvo alvo = alvos.get(indiceAlvo);
                long inicioRodada = System.nanoTime();
                for (int amostra = 0; amostra < AMOSTRAS_POR_RODADA; amostra++) {
                    long inicioChamada = System.nanoTime();
                    FilaAtendimento.Consulta resultado = alvo.cliente()
                            .consultarPosicao(alvo.senha());
                    long decorrido = System.nanoTime() - inicioChamada;
                    validarResposta(alvo, resultado);
                    int indiceAmostra = rodada * AMOSTRAS_POR_RODADA + amostra;
                    latencias[indiceAlvo][indiceAmostra] = decorrido;
                }
                temposDeRodada[indiceAlvo][rodada] = System.nanoTime() - inicioRodada;
            }
        }
        return new Resultados(latencias, temposDeRodada);
    }

    private static void validarResposta(Alvo alvo, FilaAtendimento.Consulta resposta) {
        if (resposta.senha() != alvo.senha()
                || resposta.estado() != FilaAtendimento.Estado.AGUARDANDO) {
            throw new IllegalStateException("Resposta inesperada ao consultar a fila " + alvo.nome() + ".");
        }
    }

    private static void salvarCsv(Path arquivo, List<Alvo> alvos, long[][] dados) throws IOException {
        StringBuilder csv = new StringBuilder("mecanismo,rodada,amostra,latencia_ns\n");
        for (int indiceAlvo = 0; indiceAlvo < alvos.size(); indiceAlvo++) {
            for (int indice = 0; indice < TOTAL_AMOSTRAS; indice++) {
                int rodada = indice / AMOSTRAS_POR_RODADA + 1;
                int amostra = indice % AMOSTRAS_POR_RODADA + 1;
                csv.append(alvos.get(indiceAlvo).nome()).append(',')
                        .append(rodada).append(',').append(amostra).append(',')
                        .append(dados[indiceAlvo][indice]).append('\n');
            }
        }
        Files.writeString(arquivo, csv, StandardCharsets.UTF_8);
    }

    private static void salvarResumo(Path arquivo, List<Alvo> alvos, long[][] dados,
                                     long[][] temposDeRodada) throws IOException {
        StringBuilder csv = new StringBuilder(
                "mecanismo,amostras,mediana_ms,p95_ms,vazao_sequencial_ops_s\n");
        for (int indiceAlvo = 0; indiceAlvo < alvos.size(); indiceAlvo++) {
            long[] latencias = Arrays.copyOf(dados[indiceAlvo], TOTAL_AMOSTRAS);
            double medianaMs = mediana(latencias) / 1_000_000.0;
            double p95Ms = percentil(latencias, 0.95) / 1_000_000.0;
            double[] vazoes = new double[RODADAS];
            for (int rodada = 0; rodada < RODADAS; rodada++) {
                vazoes[rodada] = AMOSTRAS_POR_RODADA
                        / (temposDeRodada[indiceAlvo][rodada] / 1_000_000_000.0);
            }
            csv.append(alvos.get(indiceAlvo).nome()).append(',')
                    .append(TOTAL_AMOSTRAS).append(',')
                    .append(String.format(Locale.ROOT, "%.6f", medianaMs)).append(',')
                    .append(String.format(Locale.ROOT, "%.6f", p95Ms)).append(',')
                    .append(String.format(Locale.ROOT, "%.2f", mediana(vazoes))).append('\n');
        }
        Files.writeString(arquivo, csv, StandardCharsets.UTF_8);
    }

    private static long percentil(long[] valores, double quantil) {
        Arrays.sort(valores);
        int indice = (int) Math.ceil(quantil * valores.length) - 1;
        return valores[Math.max(0, indice)];
    }

    private static double mediana(long[] valores) {
        Arrays.sort(valores);
        int meio = valores.length / 2;
        return valores.length % 2 == 0
                ? (valores[meio - 1] + (double) valores[meio]) / 2.0
                : valores[meio];
    }

    private static double mediana(double[] valores) {
        Arrays.sort(valores);
        int meio = valores.length / 2;
        return valores.length % 2 == 0
                ? (valores[meio - 1] + valores[meio]) / 2.0
                : valores[meio];
    }

    private static void salvarMetadados(Path arquivo, Path csv) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("Data: " + java.time.LocalDateTime.now());
        linhas.add("Sistema: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        linhas.add("Java: " + System.getProperty("java.version"));
        linhas.add("Processadores visiveis: " + Runtime.getRuntime().availableProcessors());
        linhas.add("Operacao: consultarPosicao de uma senha em espera");
        linhas.add("Aquecimento por mecanismo: " + AQUECIMENTO);
        linhas.add("Rodadas: " + RODADAS + "; amostras por rodada: " + AMOSTRAS_POR_RODADA);
        linhas.add("CSV bruto: " + csv.toAbsolutePath());
        linhas.add("Compilacao e inicializacao dos servidores/clientes nao entram no cronometro.");
        linhas.add("Socket abre uma conexao TCP por pedido; gRPC mantem canal; RMI reutiliza referencia remota.");
        linhas.add("Logs por requisicao do servidor Socket foram desativados (--quiet) para evitar I/O de console assimetrico.");
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    private static void imprimirResumo(List<Alvo> alvos, long[][] dados,
                                       long[][] temposDeRodada) {
        System.out.println("Resultado da consulta repetida; latencias em milissegundos:");
        System.out.println("Mecanismo | amostras | mediana | p95 | vazao sequencial (ops/s)");
        for (int indiceAlvo = 0; indiceAlvo < alvos.size(); indiceAlvo++) {
            long[] latencias = Arrays.copyOf(dados[indiceAlvo], TOTAL_AMOSTRAS);
            double medianaMs = mediana(latencias) / 1_000_000.0;
            double p95Ms = percentil(latencias, 0.95) / 1_000_000.0;
            double[] vazoes = new double[RODADAS];
            for (int rodada = 0; rodada < RODADAS; rodada++) {
                vazoes[rodada] = AMOSTRAS_POR_RODADA
                        / (temposDeRodada[indiceAlvo][rodada] / 1_000_000_000.0);
            }
            System.out.printf(Locale.ROOT, "%s | %d | %.6f | %.6f | %.2f%n",
                    alvos.get(indiceAlvo).nome(), TOTAL_AMOSTRAS, medianaMs,
                    p95Ms, mediana(vazoes));
        }
    }
}
