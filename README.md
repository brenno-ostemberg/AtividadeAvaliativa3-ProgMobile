# Atividade Avaliativa 3 - ProgMobile

## Alunos do grupo:

* Brenno Ostemberg de Oliveira
* Júlia Campos Nery

## Visão Geral do Software:

O **Controle de Campeonatos** é um aplicativo Android projetado para simplificar o gerenciamento de torneios e competições amadoras, como campeonatos de videogame (ex: FIFA, Pro Evolution Soccer) entre amigos. O sistema permite que um organizador (o usuário principal) mantenha um registro centralizado e de fácil acesso de todos os participantes e dos resultados dos jogos.

O objetivo principal é substituir anotações manuais em papel ou planilhas, oferecendo uma solução móvel, rápida e offline. O aplicativo armazena todos os dados localmente no dispositivo, garantindo que as informações estejam sempre disponíveis para consulta e gerenciamento.

## Usuários do Sistema:

No estágio atual, o software foi projetado para um único tipo de usuário, que possui controle total sobre todas as funcionalidades do aplicativo.

* **Administrador / Organizador do Campeonato:**
    * **Descrição:** É o usuário-chave do sistema, responsável por gerenciar todo o ciclo de vida do campeonato, desde a inscrição dos jogadores até o registro dos resultados das partidas.
    * **Permissões:**
        * Cadastrar, visualizar, editar e remover jogadores.
        * Registrar, visualizar e remover partidas.
        * Consultar o histórico de partidas de um usuario específico através de um filtro.

## Requisitos Funcionais:

| ID | Requisito Funcional | Usuário Envolvido | Entradas Necessárias | Processamento Realizado | Saídas e Relatórios Gerados |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RF01** | Cadastrar um novo usuario | Administrador | Nome completo, Nickname (apelido), E-mail e Data de Nascimento. | O sistema valida se o `Nickname` já está em uso para garantir que seja único. Se não estiver, os dados são salvos na tabela de jogadores. | O novo usuario é exibido na lista de jogadores cadastrados. Uma mensagem de sucesso ("Jogador salvo!") é mostrada. |
| **RF02** | Editar dados de um usuario | Administrador | Seleção de um usuario na lista e a submissão de novos dados para os campos desejados. | O sistema carrega os dados atuais do usuario no formulário de edição e salva as alterações no banco de dados. | A lista de jogadores é atualizada com as novas informações. |
| **RF03** | Excluir um usuario | Administrador | Seleção de um usuario na lista (através de um clique longo) e confirmação na caixa de diálogo. | O sistema remove o usuario do banco de dados. Graças à configuração de chave estrangeira (`onDelete = CASCADE`), todas as partidas associadas a esse usuario também são excluídas automaticamente. | O usuario é removido da lista. Uma mensagem de sucesso ("Jogador excluído!") é exibida. |
| **RF04** | Registrar uma nova partida | Administrador | Data da partida, seleção do Jogador 1 e Jogador 2 (de uma lista de jogadores cadastrados) e os respectivos placares. | O sistema valida se os dois jogadores selecionados são diferentes e se todos os campos foram preenchidos. Em seguida, salva a partida no banco de dados. | A nova partida é exibida na lista de partidas. Uma mensagem de sucesso ("Partida salva!") é mostrada. |
| **RF05** | Excluir uma partida | Administrador | Seleção de uma partida na lista (clique longo) e confirmação na caixa de diálogo. | O sistema solicita confirmação e, se afirmativo, remove o registro da partida do banco de dados. | A partida é removida da lista. Uma mensagem de sucesso ("Partida excluída!") é exibida. |
| **RF06** | Listar todas as partidas | Administrador | Acesso à tela "Controle de Partidas". | O sistema busca todas as partidas registradas e os nicknames dos jogadores envolvidos para compor a visualização. | É exibida uma lista contendo a data, os nomes dos jogadores e os placares de cada partida. |
| **RF07** | Filtrar partidas por usuario | Administrador | Digitação do `Nickname` de um usuario e o acionamento do botão "Filtrar". | O sistema busca o ID do usuario correspondente ao `Nickname` e, em seguida, busca todas as partidas em que esse usuario participou (seja como Jogador 1 ou Jogador 2). | A lista de partidas é atualizada, mostrando apenas os jogos que incluem o usuario filtrado. |

### Sugestões de Novos Requisitos Funcionais

| ID | Requisito Funcional | Usuário Envolvido | Entradas Necessárias | Processamento Sugerido | Saídas e Relatórios Gerados |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RF08** | **Cadastro de novos usuários com foto** | Qualquer pessoa (não autenticada) | Nome, Nickname, E-mail, Senha e uma foto (opcional, via câmera ou galeria). | O sistema deve validar os dados (ex: campos vazios, formato de e-mail). **A senha deve ser armazenada em formato de hash** (ex: SHA-256 com "salt"). O caminho da foto do usuário é salvo. | Mensagem de sucesso e redirecionamento para a tela de login. Em caso de erro (ex: nickname já existe), exibe uma mensagem clara. |
| **RF09** | **Autenticação de usuário (Login)** | Usuário cadastrado | E-mail (ou nickname) e senha. | O sistema busca o usuário pelo e-mail/nickname, aplica o mesmo algoritmo de hash à senha fornecida e compara com o hash armazenado no banco de dados. | Se a autenticação for bem-sucedida, o usuário ganha acesso à área principal do aplicativo. Caso contrário, uma mensagem de "Usuário ou senha inválidos" é exibida. |
| **RF10** | **Validação de entradas e tratamento de erros** | Administrador / Usuário Cadastrado | Preenchimento de qualquer formulário (cadastro, login, registro de partida). | O sistema verifica se campos obrigatórios estão vazios e se os tipos de dados estão corretos (ex: impede texto em campos numéricos como placar). | Exibição de mensagens de erro específicas e amigáveis ao lado dos campos inválidos (ex: "Este campo é obrigatório" ou "Placar deve ser um número"). |
| **RF11** | **Associação de partidas aos usuários** | Usuário Cadastrado | Ao registrar uma partida, o sistema automaticamente preenche o "Jogador 1" com o usuário logado e permite a seleção do "Jogador 2" de uma lista de outros usuários cadastrados. | A partida registrada no banco de dados fica diretamente vinculada aos IDs dos dois usuários envolvidos. | O placar e o resultado da partida são associados ao histórico e às estatísticas de ambos os jogadores (usuários). |
| **RF12** | **Utilização de câmera para foto de perfil** | Usuário Cadastrado | Acesso à tela de edição de perfil e acionamento do botão "Tirar Foto". | O aplicativo deve solicitar permissão para usar a câmera, iniciar a câmera do dispositivo, permitir que o usuário tire uma foto e associá-la ao seu perfil. | A nova foto é exibida no perfil do usuário e em outras áreas do app (como na lista de jogadores/ranking). |
| **RF13** | **Notificar usuário sobre um resultado** | Usuário Cadastrado | Conclusão do registro de uma partida. | Após uma partida ser salva, o sistema pode gerar uma **notificação** no dispositivo do "Jogador 2" informando sobre o novo resultado registrado (ex: "Maria registrou o resultado da partida: Você 2 x 3 Maria"). | Uma notificação do sistema aparece na barra de status do dispositivo do oponente. |
| **RF14** | **Menu de navegação principal** | Usuário Cadastrado | Acesso à tela principal após o login. | Implementar um menu de navegação (ex: **Navigation Drawer** ou **Bottom Navigation**) para facilitar o acesso às diferentes seções: Partidas, Ranking, Jogadores e Perfil. | Uma interface mais organizada e intuitiva, melhorando a **usabilidade** do aplicativo. |
| **RF15** | **Acessibilidade para leitores de tela** | Usuário com deficiência visual | Navegação pelo aplicativo com um leitor de tela (ex: TalkBack) ativado. | Todos os componentes visuais não-textuais (ImageButtons, FABs, fotos de perfil) devem ter o atributo `contentDescription` ("descrição de conteúdo") preenchido. | O leitor de tela descreve verbalmente a função de cada botão e imagem, permitindo que o usuário navegue pelo app. |