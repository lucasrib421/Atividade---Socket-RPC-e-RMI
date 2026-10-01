# Fila de Atendimento da Clinica

Aplicacao academica cliente/servidor em Java com tres mecanismos de comunicacao: Socket TCP, RPC com gRPC e Java RMI. As versoes compartilham a regra de negocio e a interface Swing.

## Documentacao e resultados

- [Relatorio comparativo](docs/relatorio-comparativo.md)
- [Checklist de entrega](docs/checklist-entrega.md)
- [Metricas e metodologia](docs/metricas-e-entrega.md)

O relatorio apresenta os resultados dos testes e da medicao de latencia. Os logs, CSVs e demais evidencias brutas permanecem locais em `docs/evidencias/`, fora do repositorio publicado.

## Antes de executar uma copia do repositorio

O repositorio contem fontes, contratos, comandos, relatorio e documentacao. As pastas `.tools/`, `build/`, `target/` e `docs/evidencias/` sao locais e nao sao enviadas ao GitHub. Os atalhos CMD atuais esperam JDK 21 em `.tools/jdk-21` e Maven em `.tools/apache-maven-3.9.16`; essas ferramentas precisam ser preparadas na maquina que executar o projeto. A compilacao gera os arquivos de `build/` e `target/`.

## Passo 1: preparar e verificar o Java

Este projeto usa o Eclipse Temurin JDK 21 em `.tools/jdk-21`. O JDK fica nesta pasta e nao altera a instalacao global do Windows. O script confere o SHA-256 publicado pelo fornecedor antes de extrair o arquivo. `.tools/` nao deve ser versionada.

O JDK ja foi baixado nesta maquina. Para conferir a versao, abra o CMD nesta pasta e rode:

```bat
.\.tools\jdk-21\bin\java.exe -version
.\.tools\jdk-21\bin\javac.exe -version
```

Para testar a compilacao e a execucao com o programa pequeno de verificacao:

```bat
if not exist build mkdir build
.\.tools\jdk-21\bin\javac.exe -d .\build .\verificacao\VerificarJava.java
.\.tools\jdk-21\bin\java.exe -cp .\build VerificarJava
```

Resultado esperado na ultima linha: `Compilacao e execucao Java funcionando.`

`javac.exe` transforma o arquivo `.java` em um arquivo `.class` dentro de `build`. `java.exe` executa a classe compilada. A pasta `build` pode ser apagada e sera recriada na proxima compilacao.

Os scripts em `scripts/` sao opcionais. Nenhum comando acima modifica variaveis globais do Windows.

## Regra da fila: primeiras operacoes

No CMD, a partir desta pasta:

```bat
if not exist build mkdir build
.\.tools\jdk-21\bin\javac.exe -d .\build .\src\FilaAtendimento.java .\verificacao\VerificarFila.java
.\.tools\jdk-21\bin\java.exe -cp .\build VerificarFila
```

Resultado esperado:

```text
Ana: senha 1, posicao 1
Bruno: senha 2, posicao 2
Entradas e consultas invalidas foram recusadas.
Ana foi chamada e finalizada; depois Bruno foi chamado e finalizado.
Chamadas e finalizacoes invalidas foram recusadas.
```

As quatro operacoes da regra de negocio estao implementadas. `consultarPosicao` informa estado e posicao atual; a posicao fica vazia quando o paciente esta em atendimento ou ja finalizou. A mesma regra e usada pelos servidores Socket, RPC e RMI.

## Tela cliente Socket

A tela usa Swing para enviar as operacoes ao servidor Socket. Inicie primeiro o servidor em um CMD; em outro CMD execute:

```bat
comandos\socket.bat tela
```

A tela compila as classes e abre o cliente grafico. Digite Ana e Bruno, depois use os botoes para consultar, chamar e finalizar. O quadro atualiza consultando o servidor para as senhas criadas por esta janela. Para uma demonstracao limpa, reinicie o servidor antes de abrir a tela e faca todas as entradas por ela.

## Tela cliente RPC

A mesma tela tambem pode se conectar por gRPC, usando a interface `ClienteFila` com implementacoes Socket e RPC. Inicie o servidor com `comandos\rpc.bat servidor` e, em outro CMD, execute `comandos\rpc.bat tela`. A interface mostra no titulo o mecanismo ativo; use a tela para inserir e acompanhar os pacientes. Para uma demonstracao limpa, reinicie o servidor RPC antes de abrir a tela e faca todas as entradas por ela.

## Socket: operacoes pela rede

O servidor aguarda clientes TCP em `127.0.0.1:5000` por padrao. Uma porta diferente pode ser escolhida ao iniciar o servidor e informada nos comandos do cliente. Cada conexao envia uma linha de pedido e recebe uma linha de resposta. Estao disponiveis `ENTRAR|nome`, `CONSULTAR|senha`, `CHAMAR|` e `FINALIZAR|senha`. A resposta segue `OK|senha|estado|posicao|nome`; o hifen representa uma posicao inexistente quando a pessoa ja esta em atendimento ou finalizou.

Para iniciar o servidor, abra o primeiro CMD na pasta do projeto e execute:

```bat
comandos\socket.bat servidor
```

O atalho compila e inicia o servidor. Mantenha esse CMD aberto. Em outro CMD, envie dois pedidos de entrada, chame e finalize Ana e consulte o resultado:

```bat
comandos\socket.bat entrar "Ana Maria"
comandos\socket.bat entrar "Bruno"
comandos\socket.bat chamar
comandos\socket.bat finalizar 1
comandos\socket.bat consultar 1
```

Ana deve passar para `FINALIZADO`; sua consulta deve mostrar esse estado e nenhuma posicao de espera. O servidor tambem pode ser compilado sem iniciar usando `comandos\socket.bat compilar`.

Para iniciar o Socket em uma porta alternativa, passe o numero da porta ao servidor e ao cliente:

```bat
comandos\socket.bat servidor 5001
comandos\socket.bat entrar "Ana Maria" 5001
comandos\socket.bat consultar 1 5001
comandos\socket.bat chamar 5001
comandos\socket.bat finalizar 1 5001
```

O PDF da atividade pede testar Socket em maquinas diferentes ou portas distintas. A segunda opcao pode ser demonstrada localmente iniciando dois servidores, um na porta padrao `5000` e outro em `5001`, e passando a porta correspondente nos comandos do cliente. As filas ficam independentes por serem processos de servidor separados. A evidencia desta validacao esta em `docs/evidencias/validacao-final-2026-10-01/validacao-socket-duas-portas.md`.

## RPC: gerar e compilar o contrato gRPC

O contrato esta em `src/main/proto/fila.proto`. O Maven e as dependencias ficam dentro de `.tools`, sem alterar a instalacao global do Windows. No CMD, execute:

```bat
comandos\rpc.bat compilar
```

Na primeira compilacao, o Maven baixa as bibliotecas necessarias para `.tools\m2` e gera as classes Java do Protocol Buffers e do gRPC em `target\generated-sources`. Os comandos de servidor e cliente compilam automaticamente apenas se as classes ainda nao estiverem geradas. Depois de alterar arquivos Java ou `.proto`, rode `comandos\rpc.bat compilar` uma vez para atualizar o build; as chamadas seguintes reutilizam as classes.

Para iniciar o servidor, use `comandos\rpc.bat servidor`; em outro CMD teste as operacoes:

```bat
comandos\rpc.bat entrar "Ana Maria"
comandos\rpc.bat entrar "Bruno"
comandos\rpc.bat consultar 2
comandos\rpc.bat chamar
comandos\rpc.bat finalizar 1
comandos\rpc.bat consultar 1
comandos\rpc.bat chamar
comandos\rpc.bat finalizar 2
```

O servidor RPC usa a porta 50051 e mantem sua propria fila em memoria. O comando `chamar` com a fila vazia deve responder com erro `NOT_FOUND`.

## RMI

O RMI usa uma interface Java remota, um registro para encontrar o servico e objetos serializaveis para transportar os resultados. O servidor inicia o registro na porta 1099 e publica o servico `FilaAtendimento` na porta 1100. No CMD, compile e inicie o servidor:

```bat
comandos\rmi.bat compilar
comandos\rmi.bat servidor
```

Em outro CMD, teste as operacoes:

```bat
comandos\rmi.bat entrar "Ana Maria"
comandos\rmi.bat entrar "Bruno"
comandos\rmi.bat consultar 2
comandos\rmi.bat chamar
comandos\rmi.bat finalizar 1
comandos\rmi.bat consultar 1
comandos\rmi.bat chamar
comandos\rmi.bat finalizar 2
```

Para abrir a interface grafica, execute `comandos\rmi.bat tela` com o servidor RMI ativo. Depois de alterar arquivos Java, rode `comandos\rmi.bat compilar` para atualizar as classes.

## Metricas comparativas de latencia

Para uma rodada de metricas, primeiro reinicie os tres servidores para que as filas estejam vazias. Inicie o Socket com `comandos\socket.bat servidor 5000 --quiet` para desativar os logs por requisicao durante a medicao; inicie RPC e RMI normalmente. Deixe os tres ativos em seus CMDs e, em outro CMD, execute:

```bat
comandos\metricas.bat
```

O coletor insere um paciente de teste em cada fila, verifica que as tres estavam vazias e consulta repetidamente esse paciente. Faz 200 chamadas de aquecimento e mede cinco rodadas de 1.000 chamadas por mecanismo. O cronometro nao inclui compilacao, inicializacao dos servidores nem preparacao dos clientes. A conexao por chamada no Socket, o canal gRPC e a referencia RMI reutilizada sao partes das implementacoes medidas.

O comando cria `latencias.csv`, `resumo.csv` e `metadados.txt` em `docs/evidencias/metricas-2026-10-01/`. O arquivo `latencia-mediana-p95.png` e o grafico desta rodada, gerado a partir do resumo; o `.bat` nao precisa de Python para coletar os dados. Esta e uma medicao local exploratoria, nao um benchmark isolado dos protocolos.

