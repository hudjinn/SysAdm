## Área Pública
### 1. Cadastro de Agendamento
- **Variáveis**: id, nomePaciente, email, status (agendado, cancelado, realizado), clínica, médico, dataHoraAgendamento, dataCadastro (gerada automaticamente).
- **Regras**:
  - Agendamentos são criados com o status "agendado".
  - Não permitir cadastro duplicado no mesmo horário para a mesma clínica e médico.
  - Informar o paciente com o id do agendamento ou número de protocolo após cadastro.

### 2. Relacionamento de Entidades
- A entidade agendamento deve ter um relacionamento com a entidade médico.

## Área Administrativa (Usuário Logado)
### 1. CRUD de Médico
- Funções básicas: criar, ler, atualizar e deletar médicos (id e nome).

### 2. Gerenciamento de Agendamentos
- Listar, excluir, alterar e cancelar agendamentos.
- Enviar e-mail ao paciente quando um agendamento for cancelado (ponto extra).

## Documentação e Outras Requisitos
- Desenvolver um relatório das tecnologias usadas, incluir um diagrama de classes, e descrever a contribuição de cada membro da equipe.
- Persistir todos os dados em um banco de dados.
- O projeto deve ser colaborativo e todos os membros devem entender todas as partes do código.
- O back-end deve ser desenvolvido em Java, com liberdade para escolher as tecnologias de front-end e o sistema de gerenciamento de banco de dados (SGBD).

## Entrega e Avaliação
- O código deve estar no GitHub e todos os participantes devem fazer commits.
- Criar uma pasta no Google Drive e compartilhar o link no ambiente educacional online.
- Apresentação do trabalho é obrigatória para aceitação.

## Penalizações
- Explicações incorretas ou falta de participação ativa podem resultar em perda de pontos.
