# Checklist para fechar a entrega

Este checklist foi atualizado após a leitura visual do PDF oficial `Lucas Santos Ribeiro lsr21 - Atividade 3 - Socket, RPC e RMI (1).pdf` e cruzado com o estado atual do código e das evidências. O PDF não define prazo, formato do relatório, limite de páginas, normas de citação ou slides.

Legenda: `[x]` concluído e verificado; `[ ]` pendente; `[?]` depende de confirmação externa. Nas seções de relatório, **Exigência do PDF** separa o obrigatório do que é complemento recomendado.

## Etapa 1 — Requisitos e escopo

- [x] Aplicação escolhida: fila operacional de atendimento da clínica, sem prontuário, diagnóstico ou prescrição.
- [x] Mesmas quatro operações nos três mecanismos: entrar, consultar posição, chamar próximo e finalizar atendimento.
- [x] Regra de negócio compartilhada entre Socket, RPC e RMI.
- [x] **Exigência do PDF:** entregar o código-fonte das três versões e o relatório comparativo.
- [x] **Exigência do PDF:** Socket foi testado em duas portas distintas (`5000` e `5001`) no mesmo computador; os registros estão na pasta de evidências da rodada.
- [x] O PDF oficial foi conferido; prazo, formato do relatório, limite de páginas, normas de citação e forma de envio não aparecem nele.
- [?] A apresentação foi desejada no contexto inicial do projeto, mas não é exigida pelo PDF fornecido. Confirmar se o professor a pediu separadamente.
- [?] Confirmar identificação de todos os integrantes da dupla e eventuais regras de envio anunciadas fora deste PDF.

## Etapa 2 — Aplicação e arquitetura

- [x] Cliente envia pedidos; servidor mantém a fila em memória e aplica as regras.
- [x] Senhas numéricas sequenciais; fila FIFO; estados `AGUARDANDO`, `EM_ATENDIMENTO` e `FINALIZADO`.
- [x] No máximo um atendimento ativo; pacientes finalizados continuam consultáveis.
- [x] Regra implementada em `src/FilaAtendimento.java` e protegida para chamadas concorrentes.
- [x] Interface Swing comum conectada aos três clientes; funcionamento confirmado manualmente pelo usuário.
- [ ] Guardar capturas das três telas para a apresentação, caso seja permitido ou solicitado.

## Etapa 3 — Mecanismos de comunicação

### Socket

- [x] Cliente/servidor TCP implementados em `ClienteSocket` e `ServidorSocket`.
- [x] Protocolo textual próprio com operações e respostas documentadas.
- [x] Atalho CMD para compilar, iniciar servidor, executar pedidos e abrir a tela.
- [x] Porta configurável no servidor e no cliente Socket, mantendo `5000` como padrão; teste em `5000` e `5001` concluído. O teste entre máquinas não foi feito porque a alternativa de portas distintas já atende ao PDF.

### RPC (gRPC)

- [x] Contrato definido em `src/main/proto/fila.proto`; classes geradas pelo Maven.
- [x] Servidor e cliente RPC implementados; respostas de erro usam estados gRPC.
- [x] Build e execução documentados pelos comandos CMD.

### RMI

- [x] Interface remota, implementação, DTO serializável, registro e cliente implementados.
- [x] Registro e objeto remoto documentados com as portas usadas.
- [x] Build e execução documentados pelos comandos CMD.

## Etapa 4 — Testes e evidências

- [x] Teste local da regra da fila em `verificacao/VerificarFila.java` executado com sucesso.
- [x] Compilação Socket, RPC e RMI executada com sucesso.
- [x] Rodada comparável: 14 verificações por mecanismo, 42 no total, com filas reiniciadas e mesmo fluxo na porta padrão.
- [x] Prova complementar Socket em portas distintas executada: 10 operações passaram entre as portas `5000` e `5001`; evidência em `docs/evidencias/validacao-final-2026-10-01/validacao-socket-duas-portas.md`.
- [x] Cobertos: fila vazia, entradas, posições, senha inexistente, chamada durante atendimento, finalizações e ordem FIFO.
- [x] Saídas, códigos de retorno e resultados estruturados guardados em `docs/evidencias/validacao-final-2026-10-01/`.
- [x] Diferenças observadas nos erros registradas: texto no Socket, status `NOT_FOUND`/`FAILED_PRECONDITION` no gRPC e exceções Java no RMI.
- [ ] Capturas das interfaces são opcionais segundo o PDF; guardar se forem úteis para a apresentação ou se houver solicitação separada.
- [x] Latência foi adicionada como complemento: 5.000 consultas por mecanismo, após aquecimento, sem medir compilação ou inicialização e com logs assimétricos do Socket desativados; CSV, resumo, metadados e gráfico salvos em `docs/evidencias/metricas-2026-10-01/`. A primeira tentativa com logs está arquivada como descartada.
- [ ] Teste de carga concorrente não é pedido pelo PDF e permanece fora do escopo desta comparação.

## Etapa 5 — Conteúdo do relatório comparativo

O rascunho revisável está em `docs/relatorio-comparativo.md`; métricas e evidências ficam vinculadas nos documentos abaixo.

- [x] **Exigência do PDF:** explicar a aplicação escolhida.
- [x] **Exigência do PDF:** explicar a arquitetura de cada versão (Socket, RPC e RMI).
- [x] **Exigência do PDF:** registrar dificuldades encontradas e como foram resolvidas.
- [x] **Exigência do PDF:** comparar vantagens e desvantagens percebidas.
- [x] **Exigência do PDF:** comparar diferenças de implementação, nível de abstração, facilidade/complexidade e casos de uso práticos.
- [x] **Complemento recomendado:** incluir objetivo, regras, resultados dos testes, conclusão e limitações.
- [ ] **Complemento recomendado:** acrescentar diagrama geral ao relatório ou à apresentação.
- [ ] **Complemento recomendado:** comparar quantidade de código, acoplamento, configuração, dados transportados, tratamento de erros e experiência do desenvolvedor. Explicar o recorte da contagem atual, que exclui código compartilhado, interface gráfica, scripts, Maven e código gerado.
- [x] **Complemento recomendado:** incluir referências técnicas para Socket TCP, gRPC e RMI; o PDF não especifica formato de referências.
- [?] Confirmar fora deste PDF se são exigidos capa, resumo, número de páginas, padrão ABNT, referências, anexos ou benchmark.

## Etapa 6 — Apresentação (objetivo adicional do projeto, ausente no PDF)

- [ ] Mostrar problema e objetivo em um slide inicial.
- [ ] Usar diagrama único para contrastar a regra compartilhada com os três caminhos de comunicação.
- [ ] Mostrar um fluxo curto da fila e trechos pequenos que evidenciem a diferença entre protocolo textual, RPC tipado e chamada remota Java.
- [ ] Apresentar a tabela comparativa e os 42 resultados funcionais, com limitações declaradas.
- [ ] Demonstrar as três telas ou incluir capturas, se a apresentação exigir demonstração.
- [ ] Fechar com aprendizados, vantagens/desvantagens e conclusão.

## Etapa 7 — Empacotamento e entrega

- [ ] Testar uma compilação limpa a partir do pacote final, em uma cópia sem `build/` e `target/`.
- [ ] Definir como o avaliador obterá JDK 21 e Maven: os comandos atuais esperam as ferramentas em `.tools/`, mas `.tools/`, `build/` e `target/` estão ignorados pelo Git e não aparecem em uma cópia limpa do código.
- [ ] Incluir README com pré-requisitos e comandos para compilar, iniciar servidores, testar clientes e abrir telas.
- [ ] Conferir nomes e extensões dos arquivos finais, abrir o relatório e a apresentação em outro computador/perfil e validar os links/figuras.
- [?] Confirmar forma de envio, prazo, convenção de nomes e se deve ser entregue arquivo compactado, repositório ou ambos.
- [ ] Encerrar servidores antes da apresentação e iniciar cada mecanismo com fila vazia.

## Próxima sequência recomendada

1. Revisar o rascunho do relatório comparativo e ajustar identificação, formatação e diagramas conforme as regras do professor.
2. Confirmar se há requisitos adicionais além do PDF, como formato, prazo ou apresentação obrigatória.
3. Montar a apresentação como material adicional solicitado no contexto inicial.
4. Fazer uma compilação limpa do pacote final e conferir os requisitos de envio.




