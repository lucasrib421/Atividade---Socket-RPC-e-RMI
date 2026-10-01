# Plano de métricas e entrega

Para acompanhar todas as etapas, veja também o [checklist de entrega](checklist-entrega.md).

## O que o enunciado pede

O PDF oficial pede o código-fonte das três versões e um relatório comparativo. O relatório deve explicar a aplicação e a arquitetura de cada versão, registrar dificuldades, vantagens e desvantagens, e comparar diferenças de implementação, abstração, facilidade/complexidade e casos de uso. Não especifica métricas numéricas, quantidade de amostras, formato do relatório ou apresentação. O teste Socket em máquinas diferentes ou portas distintas é um requisito funcional separado, já demonstrado com duas portas configuráveis.

## Comparação que já podemos registrar

| Aspecto | Socket | RPC (gRPC) | RMI |
|---|---|---|---|
| Contrato das operações | Protocolo textual definido pelo projeto: `ENTRAR`, `CONSULTAR`, `CHAMAR`, `FINALIZAR` | Contrato em `src/main/proto/fila.proto`; classes Java geradas pelo Maven | Interface Java `FilaAtendimentoRemota` |
| Dados na rede | Texto UTF-8, delimitado por linha | Mensagens Protocol Buffers sobre gRPC/HTTP/2 | Objetos Java serializados pelo RMI |
| Localização do serviço | `127.0.0.1:5000` por padrão; porta pode ser configurada no servidor e no cliente | IP e porta 50051 configurados no cliente | Registro RMI em 1099, serviço na porta 1100 e nome `FilaAtendimento` |
| Dependências de compilação | JDK 21 | JDK 21, Maven e bibliotecas gRPC | JDK 21; RMI faz parte do Java |
| Interface gráfica | Tela comum com adaptador `ClienteSocket` | A mesma tela com adaptador `ClienteRpc` | A mesma tela com adaptador `ClienteRmi` |
| Tratamento de falhas | Resposta textual `ERRO|...` | Códigos gRPC, como `NOT_FOUND` e `FAILED_PRECONDITION` | Exceções Java e `RemoteException` para falhas de comunicação |

Todas as versões delegam à mesma classe `FilaAtendimento`. Cada servidor mantém a própria fila em memória; para comparar, cada rodada precisa começar com o servidor reiniciado e vazio.

## Contagem de código específica por mecanismo

Contagem atualizada em 01/10/2026. Conta linhas não vazias que não são comentários de linha inteira. Inclui as classes de cliente/servidor e o contrato próprio de cada mecanismo; exclui regra de negócio compartilhada, interface gráfica compartilhada, scripts `.bat`, configuração Maven, código gerado e o coletor de métricas. É uma medida de volume, não de complexidade.

| Mecanismo | Arquivos específicos contados | Linhas físicas | Linhas não vazias sem comentários |
|---|---|---:|---:|
| Socket | `ServidorSocket.java`, `ClienteSocket.java` | 292 | 256 |
| RPC | `ServidorRpc.java`, `ClienteRpc.java`, `fila.proto` | 243 | 211 |
| RMI | `FilaAtendimentoRemota.java`, `ServicoFilaRmi.java`, `ServidorRmi.java`, `ClienteRmi.java`, `ConsultaRemota.java` | 164 | 137 |

Essa contagem deixa visível o volume das partes específicas, mas não deve ser interpretada sozinha: o RPC também exige configuração Maven e geração de código, enquanto o RMI separa interface remota, implementação e DTO em mais arquivos. O aumento do Socket em relação à medição preliminar decorre da configuração opcional de porta, necessária para testar a alternativa pedida no PDF.

## Métricas recomendadas

| Métrica | Como medir | Como apresentar |
|---|---|---|
| Linhas de código do mecanismo | Contar apenas classes específicas de transporte e seus comandos; excluir `FilaAtendimento`, `ClienteFila`, interface gráfica e código gerado | Tabela com Socket, RPC e RMI; explicar o que foi excluído |
| Latência por chamada | Benchmark em processo persistente; aquecer cliente; medir a mesma consulta sem incluir compilação/inicialização | Mediana e p95 em milissegundos; registrar ambiente, operação e mecanismo |
| Vazão sequencial | Medir o tempo de cada bloco de 1.000 consultas síncronas | Operações por segundo; não representa carga concorrente |
| Complexidade e abstração | Revisão do caminho da chamada: o que o programa precisa codificar e o que a tecnologia gera ou esconde | Comparação descritiva apoiada por trechos pequenos de código |
| Acoplamento e configuração | Registrar contratos, bibliotecas, portas, serviço de nomes e comandos necessários para compilar e iniciar | Tabela de componentes e passos de execução |
| Dados transportados | Comparar a representação e o contrato: texto, Protocol Buffers ou serialização Java. Não tratar bytes da mensagem como tráfego total, pois os cabeçalhos dos transportes diferem | Comparação qualitativa; só incluir tamanhos se medirmos a camada e declararmos exatamente o que foi contado |
| Erros | Repetir os mesmos pedidos inválidos: senha inexistente, fila vazia, segunda chamada enquanto há atendimento e finalização de senha errada | Tabela com resultado observado e forma como cada mecanismo comunica o erro |

Para latência, não usar o tempo total do `.bat`, do Maven, da compilação ou da inicialização da JVM. Esses tempos mediriam a ferramenta/processo junto com a comunicação. O coletor `src/BenchmarkLatencia.java` usa clientes já iniciados, uma máquina local e a mesma consulta em todas as versões; faz 200 chamadas de aquecimento e cinco rodadas de 1.000 chamadas medidas por mecanismo. A conexão TCP nova por operação no Socket e o canal reutilizado pelo gRPC fazem parte das implementações atuais e devem ser descritos como tais.

Uma rodada exploratória foi executada em 01/10/2026. Os valores abaixo descrevem este computador e estas implementações; não demonstram que um protocolo seja universalmente mais rápido.

| Mecanismo | Amostras | Mediana (ms) | P95 (ms) | Vazão sequencial (ops/s) |
|---|---:|---:|---:|---:|
| Socket | 5.000 | 0,336900 | 0,452600 | 2.802,81 |
| RPC (gRPC) | 5.000 | 0,321800 | 0,729401 | 3.018,58 |
| RMI | 5.000 | 0,126400 | 0,210001 | 7.594,99 |

O RMI apresentou menor mediana e p95 nesta rodada local; o p95 do RPC foi maior que sua mediana, indicando cauda mais lenta nesta amostra. Repetir em outro equipamento pode alterar os valores e a ordenação. A vazão é síncrona e sequencial, sem clientes simultâneos. Os logs por requisição do servidor Socket foram desativados (`--quiet`) para não acrescentar escrita no console a esse mecanismo enquanto RPC e RMI não faziam o mesmo. A tentativa anterior, com os logs ativos, foi arquivada em `preliminar-com-log/` e não é usada na comparação.

Os dados completos estão em `docs/evidencias/metricas-2026-10-01/latencias.csv`; o resumo e os parâmetros estão em `resumo.csv` e `metadados.txt`. A figura `latencia-mediana-p95.png` mostra a mediana e o p95. O script `gerar-grafico.py` recria a figura com Python 3 e Pillow; a coleta dos dados não depende de Python.

## Evidências já obtidas

- Socket: entrada, consulta, chamada e finalização validadas pelo cliente e pela tela.
- RPC: contrato gerado, compilação, operações de entrada/consulta/chamada/finalização e tela validados.
- RMI: comandos, operações de entrada/consulta/chamada/finalização e botões da tela validados pelo usuário.
- Rodada final em 01/10/2026: 14 testes por mecanismo, 42 no total; filas limpas, ordem FIFO e erros esperados confirmados. Saídas e códigos de retorno estão em `docs/evidencias/validacao-final-2026-10-01/relatorio-de-execucao.md`.
- Socket foi validado em portas distintas (`5000` e `5001`) no mesmo computador, cumprindo a alternativa prevista no PDF. A evidência e os registros estão em `docs/evidencias/validacao-final-2026-10-01/validacao-socket-duas-portas.md`.
- Os três mecanismos usam servidores separados e armazenam a fila somente em memória.

## Checklist da entrega

- [x] Código-fonte das três versões.
- [x] Comandos CMD para compilar, iniciar servidor, operar a fila e abrir a interface.
- [x] Mesma regra da fila nas três versões.
- [x] Validar os botões da interface RMI.
- [x] Testar as operações principais nos três mecanismos.
- [x] Executar e registrar uma contagem inicial de código.
- [x] Executar o [roteiro de validação final](roteiro-validacao-final.md) nos três mecanismos e registrar saídas e códigos de retorno como evidência.
- [x] Incluir latência numérica como complemento opcional, com metodologia e limitações registradas.
- [x] Coletar e visualizar mediana, p95 e vazão sequencial no mesmo ambiente para as três implementações.
- [x] Produzir rascunho comparativo com arquitetura, diferenças, dificuldades, vantagens, limitações, evidências e referências.
- [ ] Produzir apresentação com diagrama de arquitetura, fluxo de chamada e resultados como material adicional pedido no contexto inicial; ela não consta no PDF oficial recebido.
- [ ] Confirmar fora do PDF se há formato, capa, identificação da dupla, prazo ou itens de avaliação adicionais.

## Métrica de desempenho opcional

O enunciado não pede latência ou vazão numéricas. Se decidirmos incluí-las como complemento, a recomendação é medir o comportamento das implementações atuais, sem incluir inicialização da JVM nem compilação: Socket abre uma conexão TCP por pedido, enquanto o cliente gRPC mantém um canal e o cliente RMI reutiliza a referência remota. O resultado deve ser descrito como comparação destas implementações locais, não como uma medição isolada dos protocolos. A contagem de código e a comparação qualitativa continuam cobrindo os critérios explicitamente listados no enunciado.

