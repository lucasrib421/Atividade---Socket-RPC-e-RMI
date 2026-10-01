# Relatório comparativo: Socket, RPC (gRPC) e RMI

## 1. Objetivo e escopo

Este trabalho implementa uma aplicação simples de fila de atendimento em três versões distribuídas: Socket TCP, RPC com gRPC e Java RMI. O objetivo é observar como a mesma regra de negócio é oferecida ao cliente por mecanismos de comunicação diferentes. A comparação considera arquitetura, contrato, esforço de implementação, tratamento de falhas, evidências funcionais e uma medição exploratória de latência.

O enunciado solicita o código-fonte das três versões e um relatório que explique a aplicação, a arquitetura de cada mecanismo, as dificuldades, vantagens, desvantagens, diferenças de implementação, nível de abstração, facilidade/complexidade e casos de uso. O teste de Socket em portas distintas também é requisito da atividade. A medição numérica é um complemento acadêmico, não um requisito do enunciado.

## 2. Aplicação e regra de negócio

A aplicação simula uma fila clínica. O paciente entra e recebe uma senha crescente. O cliente pode consultar o estado e a posição, chamar a próxima pessoa e finalizar o atendimento atual. A fila segue ordem FIFO; apenas um atendimento pode estar ativo. Senhas finalizadas continuam consultáveis durante a execução do servidor. A implementação mantém os dados em memória, portanto reiniciar um servidor reinicia sua fila.

A classe `FilaAtendimento` concentra as regras compartilhadas. Cada mecanismo possui cliente e servidor próprios, mas chama a mesma lógica de negócio. A interface Swing também é reaproveitada por adaptadores de cliente, permitindo testar os transportes sem duplicar a tela.

## 3. Arquiteturas

### 3.1 Socket TCP

O cliente conecta-se ao servidor TCP, envia um pedido textual UTF-8 em uma linha e lê uma resposta textual. Os pedidos são `ENTRAR`, `CONSULTAR`, `CHAMAR` e `FINALIZAR`; a resposta contém senha, estado, posição e nome, ou uma mensagem de erro. O servidor escuta em `127.0.0.1:5000` por padrão. A porta pode ser escolhida tanto na inicialização do servidor quanto no comando do cliente.

Essa versão deixa explícitos os detalhes do intercâmbio: endereço, porta, conexão, escrita, leitura, separação dos campos e interpretação das respostas. Isso facilita observar o protocolo e reduz dependências externas, mas exige que cliente e servidor mantenham o mesmo formato textual.

### 3.2 RPC com gRPC

O contrato de serviço e as mensagens estão declarados em `src/main/proto/fila.proto`. O Maven executa os geradores Protocol Buffers/gRPC, que produzem classes Java de cliente e servidor. O cliente chama métodos tipados no stub e o servidor implementa esses métodos, convertendo erros da regra de negócio em estados gRPC. O serviço usa a porta 50051.

A abstração é maior que em Socket: o código trabalha com métodos e mensagens tipadas, enquanto o framework cuida do transporte e da serialização. Em contrapartida, é necessário manter o `.proto`, as dependências Maven e o processo de geração de fontes alinhados. A documentação do gRPC descreve esse modelo de serviço, stub e geração a partir de `.proto` [1].

### 3.3 Java RMI

O servidor implementa a interface remota `FilaAtendimentoRemota` e registra o objeto como `FilaAtendimento` no RMI Registry. O registro usa a porta 1099 e o serviço exportado usa a porta 1100. O cliente consulta o registro, obtém a referência remota e chama métodos Java. `ConsultaRemota` transporta os dados do resultado.

RMI oferece a chamada mais parecida com uma invocação local e integra-se diretamente ao ecossistema Java. A interface remota define o contrato e os valores retornados precisam ser apropriados para transporte remoto. A comunicação entre objetos Java em JVMs diferentes e o uso de objetos serializáveis fazem parte do modelo documentado pela Oracle [2].

## 4. Comparação qualitativa

| Critério | Socket TCP | RPC (gRPC) | Java RMI |
|---|---|---|---|
| Contrato | Protocolo textual criado no projeto | Contrato tipado em `.proto` | Interface Java remota |
| Abstração | Baixa; conexão e mensagens explícitas | Alta; stub e mensagens geradas | Alta; métodos de objeto remoto |
| Representação dos dados | Texto UTF-8 delimitado por linha | Mensagens Protocol Buffers | Objetos/valores Java serializáveis |
| Dependências | JDK | JDK, Maven e bibliotecas gRPC | JDK e infraestrutura RMI |
| Localização | Host e porta conhecidos pelo cliente | Endpoint gRPC configurado | Registry e nome do serviço |
| Falhas | Resposta textual `ERRO|...` | Status gRPC, como `NOT_FOUND` e `FAILED_PRECONDITION` | Exceções Java e `RemoteException` |
| Principal vantagem | Simplicidade de dependências e controle do protocolo | Contrato explícito, tipado e geração de stubs | Integração natural com aplicações Java |
| Principal custo | Parsing e manutenção manual do protocolo | Configuração, geração e dependências adicionais | Acoplamento ao ecossistema Java e ao registry |
| Uso adequado | Protocolos pequenos, didáticos ou integração de baixo nível | Serviços com contrato explícito e clientes em diferentes linguagens | Sistemas Java distribuídos que se beneficiam de chamadas remotas tipadas |

As contagens específicas foram: Socket, 256 linhas não vazias sem comentários; gRPC, 211; RMI, 137. Essa contagem exclui a regra compartilhada, interface, scripts, configuração Maven, código gerado e coletor. Ela mede volume de fonte específico, não complexidade. O gRPC tem ainda configuração e código gerado; portanto a contagem isolada favorece uma leitura incompleta. O Socket foi ampliado para permitir parametrizar a porta e satisfazer a alternativa de teste indicada no enunciado.

## 5. Testes funcionais e dificuldades

A validação final executou 14 verificações para cada implementação, totalizando 42/42 resultados esperados. Foram cobertos fila vazia, entradas e consultas, posição FIFO, chamada, tentativa de chamada enquanto existe atendimento, finalização inválida, finalização válida e consulta de senha finalizada. Os erros foram comunicados no formato próprio de cada tecnologia: texto no Socket, códigos gRPC e exceções no RMI. Os detalhes e saídas estão no diretório `docs/evidencias/validacao-final-2026-10-01/`.

O teste de portas distintas iniciou dois servidores Socket independentes, nas portas 5000 e 5001. Operações completas retornaram sucesso em ambos, e cada servidor manteve sua própria fila. Isso demonstra a comunicação em portas diferentes no mesmo computador, alternativa expressamente admitida no enunciado; não foi executado teste entre máquinas distintas.

As principais dificuldades práticas foram manter o contrato de cada transporte coerente com a mesma regra de negócio, configurar a geração de código no gRPC e organizar os comandos de compilação/execução no CMD. Também foi necessário separar os clientes por adaptadores para reutilizar a interface gráfica. Para medir latência, o registro de cada pedido no console do Socket foi desativado apenas no modo de benchmark, evitando contabilizar escrita de log que os outros servidores não realizavam.

## 6. Medição exploratória de latência

Foi feita uma única rodada local no Windows, com JDK 21. Cada mecanismo executou 200 chamadas de aquecimento e 5.000 chamadas medidas de `consultarPosicao` para uma senha em espera. A compilação, inicialização dos servidores e preparação dos clientes ficaram fora do cronômetro. O cliente Socket abre uma conexão TCP por pedido; o gRPC mantém o canal; o RMI reutiliza a referência remota. Essas diferenças são características das implementações avaliadas e participam do resultado.

| Mecanismo | Amostras | Mediana (ms) | P95 (ms) | Vazão sequencial (ops/s) |
|---|---:|---:|---:|---:|
| Socket | 5.000 | 0,336900 | 0,452600 | 2.802,81 |
| RPC (gRPC) | 5.000 | 0,321800 | 0,729401 | 3.018,58 |
| RMI | 5.000 | 0,126400 | 0,210001 | 7.594,99 |

Nesta rodada, RMI apresentou a menor mediana e o menor p95. O p95 do gRPC foi superior aos valores de Socket e RMI, embora a mediana do gRPC tenha ficado próxima à do Socket. A vazão medida é sequencial e síncrona, em uma única máquina; não representa múltiplos clientes concorrentes. Como houve uma só rodada exploratória, os números não provam uma superioridade geral de protocolo. Uma nova execução, outro equipamento, carga concorrente ou alteração no ciclo de conexão pode mudar a ordenação.

O CSV bruto, resumo e parâmetros estão em `docs/evidencias/metricas-2026-10-01/`. O gráfico resume mediana e p95. A medição deve ser lida como comparação destas implementações e configurações locais, e não como teste isolado dos protocolos.

## 7. Conclusão

Os três mecanismos atenderam ao mesmo conjunto de operações sobre a fila compartilhada e passaram pelos testes funcionais registrados. Socket expôs mais detalhes de comunicação e deixou o protocolo sob controle do projeto. gRPC trouxe contrato tipado e geração de stubs, ao custo de configuração e dependências. RMI ofereceu a chamada remota mais integrada ao modelo de objetos Java, com dependência do Registry e do ecossistema Java.

A rodada de latência favoreceu RMI nesta configuração local, mas seu alcance é limitado ao cenário sequencial medido. A conclusão principal da atividade é a diferença de abstração e de responsabilidades que cada mecanismo apresenta ao construir a mesma aplicação distribuída.

## Referências técnicas

1. gRPC. [Introduction to gRPC](https://grpc.io/docs/what-is-grpc/introduction/).
2. Oracle. [Java Remote Method Invocation (RMI) — The Java Tutorials](https://docs.oracle.com/javase/tutorial/rmi/index.html).
3. Oracle. [ServerSocket — Java SE 21 API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/ServerSocket.html).
4. Enunciado da atividade: “Atividade 3 - Socket, RPC e RMI”, PDF fornecido para este trabalho.

## Evidências do projeto

- Testes funcionais: `docs/evidencias/validacao-final-2026-10-01/relatorio-de-execucao.md`.
- Teste Socket em duas portas: `docs/evidencias/validacao-final-2026-10-01/validacao-socket-duas-portas.md`.
- Metodologia e checklist: `docs/metricas-e-entrega.md` e `docs/checklist-entrega.md`.
- Dados e gráfico: `docs/evidencias/metricas-2026-10-01/latencias.csv`, `resumo.csv`, `metadados.txt` e `latencia-mediana-p95.png`.
