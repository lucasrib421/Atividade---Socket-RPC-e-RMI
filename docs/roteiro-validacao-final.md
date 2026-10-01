# Rodada final de validação

Este roteiro repete os mesmos pedidos nos três mecanismos e registra evidências para a comparação. Faça Socket, RPC e RMI separadamente. Cada servidor guarda sua fila em memória; encerre e reinicie o servidor antes de cada mecanismo para começar com senhas novas e estado vazio.

## Preparação

1. Abra um CMD na pasta do projeto.
2. Compile os três mecanismos:

```bat
comandos\socket.bat compilar
comandos\rpc.bat compilar
comandos\rmi.bat compilar
```

3. Inicie cada servidor em seu próprio CMD e mantenha as janelas abertas. Os três podem ficar ligados ao mesmo tempo porque usam portas diferentes e mantêm filas independentes:

| Mecanismo | Comando para iniciar o servidor |
|---|---|
| Socket | `comandos\socket.bat servidor` |
| RPC | `comandos\rpc.bat servidor` |
| RMI | `comandos\rmi.bat servidor` |

Use um quarto CMD na mesma pasta para os pedidos. Rode uma sequência completa por mecanismo. Não digite `MECANISMO` literalmente; use um dos nomes reais: `socket`, `rpc` ou `rmi`.

## Pedidos para cada mecanismo

Execute uma sequência completa por mecanismo. Como as filas são independentes, cada uma começa vazia se os servidores foram iniciados agora.

Nos comandos de teste, o prefixo concreto é:

| Mecanismo | Prefixo que deve ser digitado |
|---|---|
| Socket | `comandos\socket.bat` |
| RPC | `comandos\rpc.bat` |
| RMI | `comandos\rmi.bat` |

Por exemplo, para o primeiro teste de fila vazia, rode estes comandos, um por vez, no CMD de operações:

```bat
comandos\socket.bat chamar
comandos\rpc.bat chamar
comandos\rmi.bat chamar
```

Cada um deve informar que não há pacientes aguardando. Para os demais testes, use o prefixo do mecanismo escolhido antes da operação; por exemplo, a entrada de Ana no Socket é `comandos\socket.bat entrar "Ana Maria"`.

### A. Erro com fila vazia

Logo após iniciar o servidor vazio:

```bat
comandos\MECANISMO.bat chamar
```

Resultado esperado: erro informando que não há pacientes aguardando. O processo cliente pode terminar com código diferente de zero; isso é esperado nesse caso.

### B. Entrada e posição na fila

```bat
comandos\MECANISMO.bat entrar "Ana Maria"
comandos\MECANISMO.bat entrar "Bruno"
comandos\MECANISMO.bat consultar 1
comandos\MECANISMO.bat consultar 2
```

Resultados esperados: Ana recebe senha 1 e posição 1; Bruno recebe senha 2 e posição 2; as consultas mostram ambos aguardando nas posições correspondentes.

### C. Erros de senha e concorrência de atendimento

```bat
comandos\MECANISMO.bat consultar 99
comandos\MECANISMO.bat chamar
comandos\MECANISMO.bat chamar
comandos\MECANISMO.bat finalizar 99
```

Resultados esperados: senha 99 não encontrada; a primeira chamada inicia Ana (senha 1); a segunda chamada é recusada porque já há atendimento em andamento; finalizar senha 99 é recusado porque não existe.

### D. Finalização e ordem FIFO

```bat
comandos\MECANISMO.bat finalizar 1
comandos\MECANISMO.bat consultar 1
comandos\MECANISMO.bat chamar
comandos\MECANISMO.bat finalizar 2
comandos\MECANISMO.bat consultar 2
```

Resultados esperados: Ana fica FINALIZADO, sem posição; Bruno é chamado em seguida, confirmando a ordem FIFO, e também termina FINALIZADO, sem posição.

## Registro dos resultados

Preencha uma cópia desta tabela para cada mecanismo. Anote a mensagem essencial recebida e marque se correspondeu ao resultado esperado. Capture as janelas do cliente e do servidor se a entrega pedir evidência visual.

| Mecanismo | Teste | Resultado observado | Conforme esperado? |
|---|---|---|---|
|  | Fila vazia |  |  |
|  | Entrada e posições 1 e 2 |  |  |
|  | Consulta de senha inexistente |  |  |
|  | Chamada de Ana |  |  |
|  | Segunda chamada durante atendimento |  |  |
|  | Finalização de senha inexistente |  |  |
|  | Finalização e consulta de Ana |  |  |
|  | Chamada de Bruno e ordem FIFO |  |  |
|  | Finalização e consulta de Bruno |  |  |

## Limites desta validação

Esta rodada compara comportamento funcional e tratamento de erros. Ela não mede velocidade: iniciar servidores e clientes em processos separados acrescentaria tempos de inicialização à medição. O enunciado não exige latência numérica; primeiro concluiremos e registraremos a comparação qualitativa e a contagem de código já iniciada.

## Exigência adicional do PDF: Socket em portas distintas

O PDF permite demonstrar Socket em máquinas diferentes **ou** em portas distintas. Para validar a alternativa local, abra dois CMDs de servidor:

```bat
comandos\socket.bat servidor
comandos\socket.bat servidor 5001
```

Em outro CMD, envie uma operação para cada porta:

```bat
comandos\socket.bat entrar "Ana Maria"
comandos\socket.bat entrar "Bia" 5001
comandos\socket.bat consultar 1
comandos\socket.bat consultar 1 5001
```

Cada servidor deve devolver senha 1 para sua própria paciente. A chamada e finalização também aceitam porta opcional, por exemplo `comandos\socket.bat chamar 5001` e `comandos\socket.bat finalizar 1 5001`. A evidência já obtida está em `evidencias/validacao-final-2026-10-01/validacao-socket-duas-portas.md`.
