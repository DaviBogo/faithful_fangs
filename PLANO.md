# Faithful Fangs — Plano do mod

> Documento vivo. Registra as ideias, as decisões tomadas e a ordem de implementação.
> Em novas sessões com o Claude: "leia o PLANO.md e continue de onde paramos".

## Visão geral

Incentivar o jogador a levar o lobo para as aventuras: morrer não é o fim (respawn na cama),
o lobo fica mais forte com o tempo e ajuda a encontrar itens. Em casa, o lobo pode andar
livremente em vez de ficar sentado num canto.

Minecraft 26.3 · Forge 66.0.13 · Java 25 · modid `faithful_fangs` · pacote `com.davibogo.faithfulfangs`

## Sistemas

### 1. Cama, plaquinha e vínculo — ✅ DEFINIDO (casos de borda confirmados)
- **Cama de cachorro** (bloco), fabricada com lã de qualquer cor. A cor da cama segue a lã.
- **Plaquinha de identificação** (*Dog Tag*, item). Substitui a ideia de "coleira", porque o lobo vanilla já tem coleira colorida.
- **Vínculo:**
  1. Clique com a plaquinha em branco num lobo seu → abre uma tela para digitar o nome → a plaquinha fica gravada com aquele lobo.
  2. Clique com a plaquinha gravada numa cama → lobo vinculado, recebe o nome e a plaquinha é **consumida**.
- **Um lobo por cama, uma cama por lobo.**
- **Trocar o nome:** faça outra plaquinha e use no lobo. A nova sobrescreve a anterior.
- **Cama quebrada:** o lobo perde o vínculo **e o nome**, mas **mantém as melhorias**. A cama dropa uma **plaquinha nova (em branco)**, e o jogador precisa refazer o vínculo. O dono recebe um aviso no chat.
- **Casos de borda:**
  - Trocar o nome de um lobo já vinculado: usar a plaquinha em branco no lobo renomeia na hora, sem precisar ir até a cama.
  - Plaquinha gravada de um lobo que já tem cama, usada em outra cama vazia: o vínculo **muda** para a cama nova, e a antiga fica livre.
  - Plaquinha usada numa cama que já pertence a outro lobo: **recusa** com mensagem ("Esta cama já pertence a Rex").

### 2. Modos de comportamento — ✅ DEFINIDO
**Modos de movimento:** Seguir, Em casa e Sentado.
- **Trocar de modo:** pelo **Painel do lobo** (Shift + clique direito), botões "Senta", "Fica em casa" e "Me siga". O clique direito normal continua vanilla (sentar/levantar, alimentar).
- **Lobo sem cama:** o botão "Fica em casa" fica desativado.
- **Voltar para casa:** ao entrar em "Em casa" longe da cama, o lobo **caminha um trecho** e depois **teleporta** para a cama. Fica **invulnerável** durante o retorno. Funciona **de qualquer lugar**, inclusive de outra dimensão.
- **Modo "Em casa":**
  - Passeia num raio de **10 blocos** ao redor da cama (configurável no futuro).
  - **Defende a casa** de monstros, conforme a postura. Como não abre portas, ignora monstros que não consegue alcançar.
  - **Dorme na cama** à noite.
  - **Não abre portas.**
- **Modo "Seguir" (melhorias):**
  - Não pula em lava, fogo ou abismos e não teleporta para lugares perigosos.
  - Fica menos travado em escadas, água e cercas.
  - **Medo de creeper:** sempre se afasta deles, mesmo que precise se afastar do jogador por um momento.

**Posturas de combate:** Passivo, Neutro e Agressivo.
- **Passivo:** nunca ataca; só foge quando é alvo.
- **Neutro:** igual ao vanilla: ataca o que o jogador atacar e defende o jogador quando ele é atacado.
- **Agressivo:** ataca qualquer monstro que aparecer, antes mesmo do jogador.
- A postura é escolhida no **Painel do lobo** e vale também no modo "Em casa".

### 3. Respawn — ✅ DEFINIDO
- Lobo **vinculado** que morre renasce na cama. Lobo sem cama morre de vez (vanilla).
- Renasce com **meio coração** de vida e **não regenera sozinho**; precisa ser alimentado.
- **Sem mudança visual** no lobo. A vida aparece **em corações no Painel do lobo**. O mod não desenha barra de vida sobre o lobo, para não conflitar com outros mods.
- Volta **imediatamente** depois da morte (mudado em 20/10/2026; antes eram 5 minutos ou quando o dono dormisse, mas assim valia mais a pena quebrar a cama e refazer o vínculo).
- **Mensagem:** "Rex se machucou feio e está de volta na cama".
- **Armadura:** continua no lobo, sem perda extra de durabilidade; só o desgaste normal pelos golpes (ver sistema 6).
- **Técnico:** a área da cama é carregada só por um instante, no momento do respawn. Fora isso, nada fica carregado à toa; os dados do lobo ficam salvos com o mundo enquanto ele espera.

### Painel do lobo (transversal) — ✅ DEFINIDO
Aberto com **Shift + clique direito** no próprio lobo. Reúne:
- **Vida** atual em corações.
- **Comandos:** Senta / Fica em casa / Me siga.
- **Postura:** Passivo / Neutro / Agressivo.
- **Melhorias** com ossos especiais (sistema 5).
- **Nome** do lobo e **localização da cama** (coordenadas e dimensão).
- **Espaço de armadura** (como no inventário do cavalo).

### Nome visível (transversal) — ✅ DEFINIDO
- O lobo mostra o nome como um mob com name tag (vanilla).
- A **cama** também mostra o nome do lobo flutuando quando o jogador olha para ela, igual à name tag.
- Plano B, se o nome flutuante no bloco der problema: escrever o nome numa plaquinha simples desenhada na cama.

### 4. Faro: o lobo encontra itens — ✅ DEFINIDO
- Só no modo **Seguir** e só com o **jogador se deslocando**. A chance é calculada pela **distância percorrida**, o que evita farm AFK.
- **Frequência inicial** (a ajustar com testes): achado comum a cada ~5 min de exploração; osso especial a cada ~40 min.
- **Combate aumenta a chance de itens raros:** cada monstro que o lobo mata dá um bônus temporário para o próximo achado raro.
- **Entrega:** o item **cai onde o lobo está** e ele **late** (latido vanilla) para chamar o jogador.
- **Itens variam por bioma.** As listas ficam em **arquivos JSON editáveis** (data pack), separados por bioma e dimensão, por exemplo:
  - Floresta: flores, sementes, gravetos, maçãs.
  - Deserto: cactos, areia, ossos.
  - Selva: cacau, bambu.
  - Cavernas: minérios.
  - Nether: pepitas e lingotes de ouro, quartzo, ossos melhores.
  - End: ender pearls.
- **Drops de mobs** entram onde aqueles mobs podem nascer: ender pearl onde há endermen, blaze rod no Nether (talvez só perto de fortalezas) etc.
- **Ossos especiais:** **Osso Ancestral** (Overworld) · **Osso Ígneo** (Nether) · **Osso Etéreo** (End).
- O lobo late algumas vezes e **para quando o jogador chega perto** ou pega o item.
- As listas também variam por **estrutura** (fortaleza do Nether, bastião, cidade do End).
- O **bônus de combate** zera depois do achado raro.

### 5. Melhorias de atributos — ✅ DEFINIDO
- Feitas no **Painel do lobo**, consumindo ossos especiais do inventário do jogador.
- **Atributos** (5 níveis cada). O "Couro" (defesa) foi **removido**, porque a armadura já cumpre esse papel:

| Atributo | Efeito | Por nível |
|---|---|---|
| Vida | Vida máxima | +2 corações |
| Força | Dano do ataque | +1 de dano |
| Agilidade | Velocidade | +10% (era +5%; mudado em 21/10/2026) |
| Faro | Chance de achados (sistema 4) | +15% |

- **Custo por nível:**
  - Nv 1: 1 Osso Ancestral
  - Nv 2: 2 Ossos Ancestrais
  - Nv 3: 1 Osso Ígneo
  - Nv 4: 2 Ossos Ígneos
  - Nv 5: 1 Osso Etéreo (eram 2; mudado em 21/10/2026)
- **Sem limite total:** o lobo pode maximizar todos os atributos.
- **Não dá para desfazer.**
- As melhorias **são mantidas** no respawn e quando a cama é quebrada.

### 6. Armadura — ✅ DEFINIDO (refeito em 21/10/2026)
O desenho original (armadura que reduz o dano como a do jogador, receitas na mesa de trabalho e todos os encantamentos de armadura) foi trocado depois de analisar a armadura de tatu do vanilla.
- **Funciona como a armadura de tatu do vanilla:** enquanto existe, **absorve 100% de cada golpe**; cada ponto de dano vira 1 ponto de desgaste, e o golpe que a quebra também é absorvido inteiro. Não protege do que a de tatu também não protege: afogamento, sufocar dentro de bloco, congelamento, fome, magia (poção de dano), efeito wither, espinhos, aperto de mobs, borda do mundo, vazio e `/kill`. Fogo, lava, queda, explosão e ataques ela absorve.
- Mostra rachaduras em 3 estágios com som, tem som próprio ao quebrar e sai com tesoura (só o dono).
- **Obtenção** (mudada em 21/10/2026 para ficar mais perto do vanilla; antes era tudo na mesa de ferraria com 1 lingote): **cobre, ferro, ouro e diamante** na **mesa de trabalho**, com a armadura de tatu no centro e **8 do material em volta**; a armadura sai nova (sem desgaste nem cor). **Netherite** na **mesa de ferraria**, como o equipamento do jogador: **molde de melhoria de netherite + armadura de diamante + lingote de netherite**; desgaste, cor e nome passam para a de netherite.
- **Tiers** (absorção em pontos de dano; um zumbi tira uns 3):

| Tier | Receita | Absorve | Observação |
|---|---|---|---|
| Tatu | — (vanilla, 6 escamas) | 64 | Base |
| Cobre | Tatu + 8 lingotes de cobre | 96 | |
| Ouro | Tatu + 8 lingotes de ouro | 112 | **Protege o dono dos piglins**: com o lobo de armadura de ouro a até 16 blocos, os piglins tratam o dono como se ele usasse ouro. Brutos continuam hostis e piglins irritados continuam atacando, igual ao vanilla. (Piglins nunca atacam lobos por conta própria, só revidam.) |
| Ferro | Tatu + 8 lingotes de ferro | 128 | |
| Diamante | Tatu + 8 diamantes | 256 | |
| Netherite | Ferraria: molde + diamante + lingote de netherite | 384 | O **item** não queima no fogo nem na lava |

- **Conserto:** clique direito no lobo sentado com o material do tier (escama de tatu, lingote de cobre, de ferro, de ouro, diamante ou lingote de netherite) conserta 12,5%, igual à escama no vanilla. Também na bigorna.
- **Tingimento:** a listra tingível da armadura de tatu continua em todos os tiers.
- **Encantamentos: nenhum.** A armadura de tatu do vanilla não aceita encantamento (nem na mesa nem com livro na bigorna), então os outros tiers também não.
- Colocada pelo **espaço de armadura do Painel do lobo**; o clique direito vanilla continua funcionando.
- **Morte:** a armadura continua no lobo após o respawn.

## Ordem de implementação proposta
1. Cama + plaquinha + nome + modos (base de tudo).
2. Respawn.
3. Faro (encontrar itens, incluindo ossos).
4. Melhorias de atributos.
5. Armadura.

## Decisões tomadas
- Sistema 1 (cama, plaquinha e vínculo) definido em 19/10/2026.
- Sistema 2 (modos e posturas) definido em 19/10/2026.
- Sistema 3 (respawn) e Painel do lobo definidos em 19/10/2026.
- Sistema 4 (faro) e Nome visível definidos em 19/10/2026.
- Sistema 5 (melhorias) definido em 19/10/2026.
- Sistema 6 (armadura) definido em 19/10/2026; atributo Couro removido.
- **Design completo.** Próximo passo: implementar a Fase 1.
- Detalhes da Fase 1 definidos em 19/10/2026: receita da Dog Tag (1 pepita de ferro + 1 linha, sem formato fixo); receita da Dog Bed (5 lãs da mesma cor em formato de almofada `W_W` / `WWW`, 16 cores como as camas vanilla); renomear um lobo já vinculado consome a plaquinha em branco.
- Idioma: o mod é 100% em inglês (`en_us.json` é o oficial). Nomes: Dog Tag, Dog Bed, Ancient Bone, Blazing Bone, Ethereal Bone; modos Follow / Stay Home / Sit; posturas Passive / Neutral / Aggressive; atributos Health / Strength / Agility / Scent.

## Progresso da implementação

### Fase 1 — Cama, plaquinha, vínculo e modos (concluída em 20/10/2026)
- [x] **1. Textos em inglês:** descrição do `mods.toml` e mensagem de log traduzidas; faixa de versão do Minecraft corrigida no `mods.toml` (`[26.3,26.4)`).
- [x] **2. Dog Tag** (`faithful_fangs:dog_tag`): item com dois estados, em branco e gravada. O estado gravado fica no componente de dados `faithful_fangs:engraved_tag` (UUID do lobo + nome). Textura própria para cada estado, tooltip explicando o uso, receita (desbloqueada no livro de receitas ao pegar uma pepita de ferro) e entrada na aba Ferramentas e Utilidades, logo depois da Name Tag.
- [x] **3. Dog Bed** (`faithful_fangs:<cor>_dog_bed`, 16 cores): cama de 1 bloco, baixa e larga (redesenhada em 19/10/2026 para o lobo deitar enrolado): almofada de 2 px, bordas de 2 px de espessura no fundo (4 px de altura) e nas laterais (3 px), frente aberta; 12 × 14 px livres por dentro. Usa a textura da lã da mesma cor. O lado aberto fica virado para quem coloca. Receita `W_W` / `WWW` com lã da mesma cor (livro de receitas desbloqueia ao pegar a lã), dropa a si mesma, empilha até 1 como as camas vanilla e aparece em Blocos Funcionais logo depois das camas vanilla. A block entity `faithful_fangs:dog_bed` já guarda o UUID e o nome do lobo e sincroniza com o cliente (ainda sem uso).
- [x] **4. Dados do vínculo:**
  - **Registro global** (`BondRegistry`, salvo em `data/faithful_fangs/bonds.dat` do mundo): para cada lobo vinculado guarda o UUID do lobo, o dono, o nome e a cama (dimensão + posição). É a fonte da verdade; também indexa cama → lobo (uma cama por lobo, um lobo por cama).
  - **Cópias:** a cama guarda o lobo e o nome (para exibir); o lobo guarda o nome que o vínculo deu a ele (`bond_name`, nos dados persistentes do Forge). Quando a cama ou o lobo carregam, comparam a cópia com o registro e se corrigem. Assim, mudanças feitas com eles descarregados (em outra dimensão, longe) são aplicadas depois.
  - **Regras do nome:** lobo vinculado recebe o nome do vínculo. Se o vínculo acaba, o lobo perde o nome. Exceção: se o lobo foi renomeado com Name Tag vanilla enquanto **não** tinha vínculo aplicado, esse nome é mantido.
  - **Cama removida** (quebrada, explodida ou trocada por comando): o vínculo é desfeito. O aviso ao dono e a plaquinha nova que cai ficam para o passo 5.
  - **Lobo vinculado que morre:** por enquanto o vínculo continua (a cama fica reservada); o respawn da Fase 2 vai usar isso.
  - **Comando de teste** `/faithfulfangs debug` (só operador; textos em inglês sem tradução): sem argumentos lista os vínculos e descreve o lobo ou a cama para onde você olha; `bind <nome>` vincula o lobo para onde você olha à Dog Bed livre mais próxima (16 blocos); `unbind` desfaz o vínculo do lobo ou da cama para onde você olha.
- [x] **5. Fluxo de vínculo:**
  - **Gravar:** plaquinha em branco + clique direito no seu lobo → tela "Engrave Dog Tag" para digitar o nome (até 50 caracteres; já vem preenchido com o nome atual do lobo, se houver) → a plaquinha vira gravada. O lobo não senta (a interação vanilla é cancelada). O servidor confere tudo de novo ao confirmar (dono, distância, plaquinha na mão).
  - **Vincular:** plaquinha gravada + clique direito numa Dog Bed → lobo vinculado e nomeado, plaquinha consumida, mensagem "Rex now has a home!".
  - **Renomear lobo já vinculado:** plaquinha em branco no lobo abre a tela "Rename Rex"; o nome muda na hora no lobo e na cama, e a plaquinha é consumida.
  - **Casos de borda:** cama de outro lobo → recusa ("This bed already belongs to Rex."), plaquinha não é consumida; lobo que já tem cama + plaquinha gravada numa cama vazia → muda de cama e a antiga fica livre; plaquinha gravada usada no lobo → dica ("Use it on a Dog Bed" ou "pertence a outro lobo"); plaquinha em branco na cama → dica para gravar primeiro; lobo que não é seu → recusa.
  - **Cama removida:** além de desfazer o vínculo, dropa uma plaquinha em branco e o dono (se online) recebe aviso no chat.
  - **Name Tag vanilla num lobo vinculado:** o vínculo e a cama passam a usar o novo nome (a Name Tag continua funcionando normalmente).
  - Rede: canal `faithful_fangs:main` com dois pacotes (abrir a tela no cliente; confirmar o nome no servidor).
- [x] **6. Nome flutuante** sobre a cama: quando a mira está na cama vinculada (alcance normal de interação), o nome do lobo aparece acima dela no mesmo estilo da Name Tag vanilla (visível através de blocos). Feito com um block entity renderer (`client/DogBedRenderer`); o modelo da cama continua sendo o modelo de bloco. O plano B (plaquinha desenhada na cama) não foi necessário.
- [x] **7. Painel do lobo** (Shift + clique direito no seu lobo; com a Dog Tag na mão vale a plaquinha): menu de container (`faithful_fangs:wolf_panel`), pronto para receber o espaço de armadura e o inventário na Fase 5. Mostra o lobo girando (como no inventário do cavalo), a vida em corações (atualiza ao vivo), a cama (dimensão + coordenadas) ou "No bed", e os botões de modo e postura. O selecionado fica com o texto amarelo; cada botão tem tooltip. "Stay Home" fica desativado sem cama. Os botões usam o pacote vanilla de clique em botão de container; o servidor confere dono e distância (8 blocos).
- [x] **8a. Modos Sit e Follow** pelo painel. Modelo de dados: "Sit" é o próprio estado sentado vanilla; o mod guarda à parte o **modo de pé** (Follow ou Stay Home). Assim o clique direito vanilla continua sentando/levantando, e ao levantar o lobo **volta ao modo anterior** (decidido em 19/10/2026). Se o lobo perde a cama, o modo de pé volta para Follow. A postura (padrão Neutral) também fica guardada no lobo.
  - Por enquanto "Stay Home" e as posturas só ficam salvos; o comportamento vem nos passos 8b e 8c (até lá o lobo age como no vanilla).
- [x] **8b. Posturas** (objetivos de IA adicionados a todo lobo ao entrar no mundo; só agem em lobos domesticados):
  - **Passive:** um objetivo de alvo com prioridade máxima impede todos os ataques vanilla (defender o dono, revidar, caçar esqueletos) e zera alvo e raiva. O lobo foge **enquanto estiver sendo caçado**: ameaça é quem bateu nele nos últimos 5 s ou um mob que está mirando nele (detectado num raio de 24 blocos). Continua fugindo enquanto a ameaça o perseguir (até 32 blocos) e, no mínimo, até ficar a 16 blocos; troca de ameaça se outro mob atacar; encurralado, corre para qualquer lugar. No modo Follow, quando fica a mais de 12 blocos do dono, teleporta até ele (como o pânico vanilla) — única forma de escapar de endermen. Golpe do próprio dono não conta. (Ajustado em 19/10/2026: antes o lobo parava de fugir cedo demais.)
  - **Neutral:** vanilla, sem mudanças.
  - **Aggressive:** ataca qualquer mob hostil (interface `Enemy`) ao alcance, mesmo antes do dono, desde que consiga ver e chegar até ele. Ignora creepers (foge deles, ver 8d), endermen e zombified piglins calmos, piglins que não estão hostis, warden, wither e dragão. Lobo sentado não ataca.
  - Trocar para Passive no meio de uma briga faz o lobo parar na hora.
- [x] **8c. Stay Home:**
  - **Área de casa:** usa a restrição vanilla de "home" dos mobs, centrada na cama com raio 10. Com ela, o passeio aleatório vanilla fica dentro da área e o lobo só escolhe/briga com alvos dentro dela (é assim que defende a casa sem sair correndo atrás de monstros). Alvos que saem da área (+2 blocos) são largados. Fora da área (até 24 blocos) ele volta andando.
  - **Retorno:** mais longe que 24 blocos, ou em outra dimensão: anda ~5 s (para a cama se houver caminho, senão para longe do dono) e teleporta para cima da cama, em qualquer dimensão. Fica invulnerável durante o retorno (invulnerabilidade temporária renovada a cada tick, então some sozinha se algo interromper). A área de chegada fica carregada por alguns segundos (ticket de portal vanilla) e depois descarrega normalmente com o lobo salvo nela. Nuvenzinha de partículas na saída e na chegada.
  - **Dormir:** à noite (Overworld) o lobo vai até a cama, se ajeita no meio dela virado para o lado aberto e **dorme enrolado**, **de olhos fechados**: peito na frente com o pescoço virado para o lado aberto, traseiro na diagonal até o canto do fundo, patas de trás e rabo dando a volta pelo outro lado, cabeça presa ao pescoço (a coleira fica no pescoço) e apoiada nas patas da frente, virada para as patas de trás. O **filhote** também dorme deitado e de olhos fechados: cabeça de frente para o lado aberto, corpo na diagonal atrás dela, patas da frente ao lado da cabeça (sem atravessar o rosto) e rabo dando a volta pelo outro lado. Levanta de manhã, se tiver alvo para brigar ou se apanhar. Se não alcançar a cama em 30 s, tenta de novo 1 min depois. O Painel mostra "Sleeping".
    - Técnica: o estado "dormindo" é a pose de entidade `SITTING` (a que o camelo usa para deitar; sincronizada pelo vanilla e sem mudar a hitbox — a pose `SLEEPING` encolheria o lobo para 0,2 bloco). O renderizador do lobo foi substituído por uma cópia do vanilla (`client/wolf/CurlingWolfRenderer`) cujos modelos (`CurlingAdultWolfModel` e `CurlingBabyWolfModel`) têm a pose deitada; a camada de armadura também acompanha. O lado da barriga do adulto que encosta no peito não tem textura no vanilla (fica sempre escondido dentro do peito); na pose enrolada a ponta da barriga continua dentro do peito, então esse lado nunca aparece (conferido em 48 ângulos de câmera). Olhos fechados (`client/wolf/ClosedEyesTextures`): o mod gera em tempo de execução uma cópia da textura do lobo com as pálpebras fechadas (adulto: cada olho vira a cor do pelo em cima e uma linha na cor da pupila embaixo; filhote: o olho de 1 pixel vira uma linha de 2 pixels na cor da pupila), então funciona com todas as variantes, pacotes de recursos (inclusive em resolução maior) e variantes de outros mods, sem copiar texturas da Mojang; as cópias são refeitas a cada recarga de recursos (F3 + T). Ressalva: outro mod que também troque o renderizador do lobo vai conflitar com este.
  - **Objetivos vanilla desligados no Stay Home:** seguir o dono (que também teleporta até ele), o pânico com fogo/lava que teleporta até o dono (vira um pânico comum) e o "sentar" automático que o vanilla faz quando o dono está offline ou em outra dimensão (no Stay Home só senta se mandado).
  - Não abre portas (lobos vanilla não abrem).
- [x] **Follow entre dimensões** (pedido em 19/10/2026): quando o dono troca de dimensão (Nether, End ou qualquer dimensão de outro mod), os lobos que o seguem vão junto e aparecem ao lado dele. Vale para lobos em Follow, de pé, sem guia nem montaria, a até 24 blocos do dono no momento da viagem. Cobre portais, teletransporte por comando e a primeira saída do End (créditos).
- [x] **8d. Melhorias do Follow e segurança:**
  - **Seguir mais seguro** (`wolf/ai/SafeFollowOwnerGoal`, substitui o "seguir o dono" vanilla no Follow): mesmas distâncias do vanilla (começa a seguir a 10 blocos, para a 2, teleporta a partir de 12). O teleporte só vai para lugares seguros: os mesmos testes do vanilla (chão firme, sem fogo ou lava do lado, sem folhas, espaço livre) e nunca a menos de 8 blocos de um creeper. Se não achar lugar seguro, continua andando até o dono.
  - **Trava menos:** com o dono a mais de 5 blocos, o lobo que quase não se mexeu por 3 s (cerca, escada, beirada de água) ou que está sem caminho/nadando e não chegou mais perto em 5 s teleporta até o dono, sem esperar os 12 blocos do vanilla.
  - **Não pula de abismos** (`wolf/ai/LedgeGuardGoal`): no vanilla, um lobo brigando aceita quedas de 20+ blocos para alcançar o alvo. Agora, se os próximos passos do caminho têm uma queda que machuca (mais de 3 blocos, salvo se cair na água), ele para na beirada; se o mesmo alvo continuar fora de alcance por 5 s, desiste dele. O Aggressive também ignora monstros mais de 3 blocos abaixo dele.
  - **Sem pulo de ataque** (mudado em 21/10/2026, na fase 4): o lobo domesticado não dá mais o pulo de ataque vanilla; ele corre até o alvo e morde. O pulo tem velocidade fixa, então um lobo pulando atrás da presa não ficava mais rápido com a melhoria Agility. Antes, ele só pulava se o chão até o alvo fosse seguro. O lobo selvagem continua pulando como no vanilla.
  - **Fogo e lava:** nunca anda por fogo, blocos de magma ou fogueiras acesas, e evita passar colado em fogo ou lava quando há outro caminho. Última defesa (`wolf/ai/StepGuardGoal`, a cada tick): se os próximos passos do lobo (pelo caminho ou pelo embalo, inclusive pulo ou empurrão) terminam em fogo ou lava, ou acima de lava/fogo onde ele cairia, ele para na hora. O teleporte também recusa lugares com fogo ou lava a 1 bloco (o teste vanilla falha quando há água do lado).
  - **Mob no fogo depende da postura** (`wolf/ai/HazardTargetGoal`, definido em 20/10/2026): no **Neutral**, o lobo larga o alvo que está pegando fogo ou em cima/ao lado de fogo ou lava, e fica 3 s sem escolher alvo (não defende o dono de um zumbi queimando no sol). No **Aggressive**, ele aceita o risco de se queimar: ataca esses mobs (inclusive escolhe sozinho), morde da beirada sem nunca pisar em fogo ou lava e, se pegar fogo no meio da briga, continua brigando (só foge se estiver dentro do fogo ou colado nele). Em qualquer postura, se o lobo não chega mais perto do alvo por 5 s (por exemplo, alvo dentro da lava), desiste dele e fica 3 s sem escolher alvo.
  - **Sai do fogo com calma** (`wolf/ai/FireEscapeGoal`, todos os lobos, substitui o pânico vanilla que corria para lugares aleatórios, às vezes de volta para o fogo): no Follow, com o dono a 12+ blocos, teleporta até ele (lugar seguro); pegando fogo e com água a até 8 blocos, vai para a água; se está no perigo ou colado nele (fogo, lava, magma, neve fofa, cacto), anda até o lugar mais perto a pelo menos 2 blocos de fogo ou lava e fica lá até o fogo apagar.
  - **Medo de creeper:** todo lobo de pé, selvagem ou domesticado (Follow ou Stay Home, até acordando se estiver dormindo), se afasta de creepers que estejam a menos de 8 blocos e à vista, correndo quando estão a menos de 7. Depois volta a seguir o dono normalmente.
  - Lobo sentado (Sit) continua parado, como no vanilla.

### Fase 2 — Respawn (concluída em 20/10/2026)
Detalhes definidos em 20/10/2026: volta **imediatamente** depois da morte (os 5 minutos foram descartados após os testes); ao voltar, o lobo fica em **Stay Home** (ferido, em casa, sem teleportar até o dono); se a cama for quebrada enquanto ele espera, ele **volta na hora** no lugar da cama, sem vínculo e sem nome (regra normal de cama quebrada), para nunca se perder um lobo por isso.
- [x] **1. Morte e volta para a cama** (`wolf/WolfRecovery`, dados em `bond/Recovery`):
  - Lobo domesticado **com vínculo** que morre (qualquer causa, inclusive cair no vazio ou `/kill`) não morre: some numa nuvenzinha com um choro (o som do próprio lobo), sem drops nem a mensagem de morte vanilla. Lobo sem vínculo morre normalmente.
  - **Volta na hora** para a cama. Se a área da cama não está carregada, ela é carregada por alguns segundos (o mesmo ticket curto que um portal usa); enquanto isso o lobo inteiro (variante, coleira, armadura, nome, postura...) fica guardado no `bonds.dat`, então ele nunca se perde, nem se o jogo fechar nesse meio-tempo. Quando os mobs daquela área estão carregados, o lobo aparece em cima da cama, virado para a frente dela, com **meio coração** e em **Stay Home**, numa nuvenzinha com choro. O dono recebe "Rex was badly hurt and is back in its bed.". Depois a área descarrega normalmente com o lobo salvo nela.
  - Vale para cama em qualquer dimensão e com o dono longe, em outra dimensão ou offline (a mensagem só chega se ele estiver online).
  - Se a cama sumiu nesse meio-tempo (quebrada enquanto a área carregava), ele volta no lugar dela ("Rex was badly hurt and is back where its bed used to be.").
  - Vanilla já não regenera vida de lobo sozinho (só comendo), então "não regenera sozinho" já vale.
  - **Comando de teste:** `/faithfulfangs debug` lista os lobos que ainda esperam a área da cama carregar (normalmente nenhum).

### Fase 3 — Faro (concluída em 20/10/2026)
Etapas: (1) os três ossos especiais; (2) as listas de achados em JSON (data pack); (3) o faro em si (o lobo encontrar, entregar e latir).
- [x] **1. Ossos especiais** (`item/SpecialBoneItem`, registrados em `registry/ModItems`):
  - **Ancient Bone** (Overworld, raridade incomum, nome amarelo), **Blazing Bone** (Nether, rara, nome azul-claro, não queima no fogo nem na lava, como o netherite) e **Ethereal Bone** (End, épica, nome roxo).
  - Texturas próprias 16x16 (`textures/item/*_bone.png`): osso envelhecido com musgo, osso incandescente com rachaduras brilhando e osso lilás com brilhos ciano. Seguram na mão como o osso vanilla.
  - Tooltip cinza dizendo onde o osso é encontrado (por exemplo "Sniffed out by wolves in the Overworld"). O uso (melhorias, sistema 5) entra no tooltip na fase 4.
  - Ficam na aba criativa **Ingredientes**, logo depois do osso vanilla. Ainda não têm receita nem outra forma de obter; virão do faro.
- [x] **2. Listas de achados** (loot tables em `data/faithful_fangs/loot_table/scent/`, do tipo `minecraft:gift`, o mesmo do presente matinal do gato; editáveis por data pack):
  - Entradas: `faithful_fangs:scent/common` (achado comum) e `faithful_fangs:scent/rare` (osso especial: Blazing Bone no Nether, Ethereal Bone no End, Ancient Bone no Overworld e em qualquer outra dimensão).
  - O achado comum usa a **primeira lista que servir** para o lugar onde o lobo está: dimensão → estrutura → bioma de caverna → subterrâneo (sem céu e abaixo de y 40; abaixo de y 0 é o "profundo") → bioma de superfície → lista geral da dimensão.
  - **Overworld:** estruturas (cidade antiga, câmaras do desafio, mina abandonada, vila); cavernas (deep dark, lush caves, dripstone caves, sulfur caves, subterrâneo, subterrâneo profundo); biomas (campos de cogumelo, cerejeiras, jardim pálido, floresta escura, pântano, selva, badlands, deserto, savana, neve, taiga, floresta, planícies e prado, montanhas, praia, rio, oceano); geral.
  - **Nether:** fortaleza, bastião, floresta carmesim, floresta distorcida, vale das almas, deltas de basalto, geral (inclui Nether Wastes).
  - **End:** cidade do End, geral.
  - A lista geral do Overworld tem drops dos mobs que nascem em quase todo lugar (osso, linha, pena, carne podre, pólvora, flecha, olho de aranha e, raramente, ender pearl). Quase toda lista de bioma também puxa dela; campos de cogumelo e deep dark não, porque lá não nascem monstros. As listas de caverna puxam da lista do subterrâneo (minérios e drops de monstros).
  - Os biomas são reconhecidos por tags (`#minecraft:is_forest`, `#c:is_snowy`...), então biomas de outros mods que usam essas tags caem na lista certa.
  - Exemplos de chance por achado: diamante 1,6% no subterrâneo profundo; ender pearl 1,3% na lista geral do Overworld; blaze rod 13% na fortaleza; chave do desafio 3,2% nas câmaras do desafio; esmeralda 4,1% na vila.
  - **Teste:** `/loot give @s loot faithful_fangs:scent/common` sorteia um achado para o lugar onde você está (`.../rare` sorteia o osso especial; também dá para sortear uma lista direto, como `faithful_fangs:scent/overworld/desert`).
  - Observação: o `/loot give` põe o item direto no inventário; no faro o item cai no chão, onde o lobo está (passo 3).
- [x] **3. O faro em si** (`wolf/WolfScent`, `wolf/ai/ScentGoal`, `wolf/ai/ScentAlertGoal`, `wolf/WolfSounds`; definido em 20/10/2026):
  - **Quem fareja:** só lobo domesticado **com vínculo** (cama e Dog Tag), em **Follow**, de pé, com o dono vivo, jogando (não espectador) e a até **24 blocos**, na mesma dimensão. Com vários lobos, **cada um faz os próprios achados**.
  - **Pago em distância:** a cada 5 s mede quanto o dono avançou em linha reta, sem contar a altura. Menos de 2 blocos (construindo, parado, AFK, andando em círculo pequeno) não conta; mais de 30 blocos (6 blocos/s, uma corrida) conta como 30, então cavalo, elytra ou teleporte não dão achados a mais.
  - **Contagem regressiva por lobo**, salva no próprio lobo (continua depois de sair do jogo e no respawn): achado comum a cada **900 blocos** em média e osso especial a cada **7200 blocos** em média (cerca de 5 e 40 minutos andando a 3 blocos/s); cada achado vem entre 50% e 150% da média.
  - **Bônus de combate:** cada monstro que o lobo mata faz a contagem do próximo osso especial andar **25% mais rápido** (até 3 vezes mais rápido). O bônus some 10 minutos depois da última morte ou quando o osso especial é encontrado.
  - **Achado:** quando a contagem chega a zero e o lobo está calmo (no chão, fora da água e da lava, sem fogo, sem alvo e fora de perigo), ele para, cava (voa um pouco de terra do bloco em que ele está) e o item **cai no chão aos pés dele**. O osso especial tem prioridade e solta brilhos verdes. Se o lobo não pode cavar agora, o achado espera até ele poder.
  - **Chamado** (ajustado em 20/10/2026, o latido único não chamava atenção): o lobo fica junto do item, com a cabeça inclinada e olhando para o dono, e chama com **sequências de 2 ou 3 latidos rápidos** (o latido do próprio lobo) a cada 2 a 2,5 s, bem diferente do latido solto que ele dá à toa. A primeira sequência (3 latidos) sempre toca inteira. O dono também recebe uma **mensagem acima da barra de itens**: "Rex found something!" ou, no osso especial, "Rex found something special!". O chamado para quando o dono chega a 3 blocos do lobo ou do item, quando alguém pega o item, depois de **20 segundos**, se o dono se afasta mais de 48 blocos ou se acontece algo mais importante (briga, perigo, ordem de sentar ou de ficar em casa). Depois o lobo volta a seguir o dono normalmente (teleportando se ele estiver longe).
  - **Comandos de teste:** `/faithfulfangs debug` olhando para o lobo mostra quantos blocos faltam para o próximo achado e para o osso especial, o bônus de combate e se ele está farejando agora. `/faithfulfangs debug find` (ou `find rare`) deixa um achado (ou o osso especial) pronto para o lobo que você está olhando ou para todos os seus lobos por perto; ele cava em até 5 s.

### Fase 4 — Melhorias de atributos (concluída em 21/10/2026)
Detalhes definidos em 21/10/2026: layout "coluna ao lado" no Painel do lobo (opção A do esboço, para sobrar espaço embaixo para a armadura e o inventário da fase 5); os corações novos de um nível de Health já vêm cheios.
- [x] **1. Melhorias no Painel do lobo** (`wolf/WolfUpgrade`, `wolf/WolfUpgrades`, tela em `client/WolfPanelScreen`):
  - Para cada atributo: ícone, nome, **5 quadradinhos de nível** coloridos pelo osso que cada nível custou (bege = Ancient, laranja = Blazing, lilás = Ethereal) e um **botão com o osso e a quantidade** do próximo nível. O botão fica cinza se faltar osso e vira "MAX" no nível 5. Desde o painel compacto da fase 5, as melhorias abrem pelo botão "Upgrades..." (veja a fase 5, passo 3); o "You have:" com a contagem de ossos saiu, porque o inventário aparece no painel.
  - Dica do botão: "Upgrade Strength to level 2" / "+1 attack damage, total +2" / "Costs 2 Ancient Bones" (em vermelho "Needs ..., you have ..." quando falta osso).
  - Efeitos por nível: **Health** +2 corações (+4 de vida máxima), **Strength** +1 de dano, **Agility** +10% de velocidade (era +5%, pouco perceptível nos testes), **Scent** +15% (a exploração do dono conta 15% a mais para os achados, comuns e especiais).
  - Custo por nível (igual para todos): 1 Ancient, 2 Ancient, 1 Blazing, 2 Blazing, 1 Ethereal (o último nível custava 2 Ethereal; mudado para 1 em 21/10/2026). Os ossos saem de qualquer lugar do inventário (inclusive a mão secundária). No **criativo é de graça**, como a mesa de encantamento.
  - Ao comprar: som de subir de nível e brilhos verdes no lobo; num nível de Health os corações novos já vêm cheios.
  - Vale para qualquer lobo seu (com ou sem cama). Não dá para desfazer. Os níveis ficam salvos no lobo, então continuam no respawn e quando a cama é quebrada; os modificadores de atributo também são salvos com o lobo (a vida extra está lá assim que ele carrega).
  - Com Health alto, os corações ocupam até 3 linhas e a cama desce um pouco no painel.
  - Para a Agility valer também na caça, o lobo domesticado não dá mais o pulo de ataque (veja a seção de segurança da fase 1).
  - Os ossos ganharam a dica "Shift + right click your wolf to upgrade it".
  - **Comandos de teste:** `/faithfulfangs debug` olhando para o lobo mostra os níveis, a vida máxima, o dano e a velocidade; `/faithfulfangs debug reset_upgrades` zera as melhorias do lobo que você está olhando.

### Fase 5 — Armadura (concluída em 21/10/2026)
Desenho refeito em 21/10/2026 (veja o sistema 6): absorve como a armadura de tatu, sem encantamentos; o ouro protege o dono dos piglins.
- [x] **1. Tiers de armadura** (`item/WolfArmorTier`, `wolf/WolfArmor`, registrados em `registry/ModItems`):
  - Itens **Copper, Iron, Golden, Diamond e Netherite Wolf Armor**, na aba criativa Combate logo depois da armadura de tatu. A de netherite não queima no fogo nem na lava.
  - **Receitas** (refeitas em 21/10/2026; a primeira versão usava a mesa de ferraria sem molde para todos): cobre, ferro, ouro e diamante na **mesa de trabalho**, armadura de tatu no centro e 8 lingotes (ou diamantes) em volta (`recipe/<tier>_wolf_armor.json`); aparecem no livro de receitas quando o jogador tem uma armadura de tatu. Netherite na **mesa de ferraria**: molde de melhoria de netherite + Diamond Wolf Armor + lingote de netherite (`recipe/netherite_wolf_armor_smithing.json`), levando desgaste, cor e nome.
  - **Absorção** igual à de tatu (o jogo só faz isso para o item dele, então o mod repete a regra em `WolfArmor`): absorvem 96 (cobre), 112 (ouro), 128 (ferro), 256 (diamante) e 384 (netherite); rachaduras em 3 estágios com som e pedacinhos do material; som próprio ao quebrar; mesmas exceções (afogamento, fome, magia, wither, espinhos, vazio...).
  - **Conserto:** com o lobo sentado, clique direito com o material do tier conserta 12,5% (regra vanilla, que já vale para qualquer armadura de lobo); bigorna também.
  - **Tingimento:** mesma listra tingível da de tatu, com receita de tingir na mesa de trabalho e lavagem no caldeirão.
  - **Ouro:** com um lobo seu de armadura de ouro a até 16 blocos, piglins não começam a te atacar (como se você usasse ouro). Piglins irritados com você (por exemplo, porque você bateu num deles ou abriu um baú do bastião) ainda atacam; brutos continuam hostis. Piglins também adoram o item (pegam do chão), como qualquer item de ouro.
  - Texturas: as placas da armadura de tatu recoloridas com a cor de cada metal (as tiras de couro continuam iguais).
  - Sem encantamentos (nem a de tatu aceita).
- [x] **2. Espaço de armadura no Painel do lobo** (opção B do esboço, escolhida em 21/10/2026; `menu/WolfPanelMenu`, `client/WolfPanelScreen`):
  - O **espaço de armadura** fica **ao lado do retrato do lobo**, como na tela do cavalo, e o **inventário do jogador** aparece no painel. (A primeira versão deixou o painel com 346x246 e espaço sobrando; foi reorganizada no passo 3.)
  - Coloca e tira arrastando ou com **Shift + clique** (armadura do inventário vai para o lobo; a do lobo volta para o inventário; o resto troca entre inventário e barra rápida). Aceita a armadura de tatu e os tiers do mod. O espaço vazio mostra o contorno de uma armadura de lobo (`textures/gui/sprites/container/slot/wolf_armor.png`) e a barra de durabilidade aparece no ícone.
  - **Filhotes** não usam armadura (regra vanilla), então o espaço some para eles.
  - O clique direito vanilla com a armadura no lobo e a tesoura continuam funcionando.
- [x] **3. Painel compacto** (pedido em 21/10/2026: o painel ocupava tela demais e tinha espaço em branco; opção A do esboço):
  - **262x170**, mais ou menos o tamanho da tela do cavalo.
  - **Esquerda:** espaço de armadura, retrato, corações e cama em cima; o **inventário logo abaixo**, perto do espaço de armadura.
  - **Direita:** **Modo** e **Postura** em destaque, como colunas de botões grandes (um por linha), para usar rápido. O atual fica com o texto amarelo.
  - Botão **"Upgrades..."** embaixo da coluna: troca Modo e Postura pelas melhorias (ícone e nome; embaixo, os quadradinhos de nível e o botão com o preço); **"Back"** volta. O painel sempre abre mostrando Modo e Postura.
  - O "You have:" saiu, porque a quantidade de ossos aparece no próprio inventário.

### Descrição e versões (21/10/2026)
- Descrição do mod no `mods.toml` (em inglês), resumindo os cinco sistemas, com os créditos: **muitas das ideias vieram de Érica Scherner Ferreira**, e o mod foi **desenvolvido com a ajuda do Claude Opus 5.5 (Anthropic)**. Autor: "Davi Bogo".
- Versões conferidas: mod **1.0.0** (igual no `build.gradle` e no `mods.toml`; mudar nos dois juntos), Minecraft **26.3** (aceita `[26.3,26.4)`), Forge **26.3-66.0.13** (o `mods.toml` agora exige a partir de 66.0.13, a versão usada no desenvolvimento), Java **25**, ForgeGradle 7, EventBus 7.0.6.
- `pack.mcmeta`: a descrição mostrava `${mod_id}` literal (o build não substitui variáveis); agora é "Faithful Fangs resources". O formato 121 é o mesmo que o próprio Forge 66 usa.

### Estrutura do código
- `FaithfulFangs` — classe principal; registra os `DeferredRegister` e a aba criativa.
- `registry/ModBlocks`, `registry/ModItems`, `registry/ModBlockEntities`, `registry/ModDataComponents` — registros.
- `item/DogTagItem`, `item/EngravedTag` — plaquinha e o componente de gravação.
- `item/SpecialBoneItem` — ossos especiais (Ancient, Blazing e Ethereal Bone).
- `block/DogBedBlock`, `block/DogBedBlockEntity` — cama e os dados do vínculo guardados nela.
- `bond/Bond`, `bond/BondRegistry`, `bond/BondManager` — vínculo, registro global e todas as operações (vincular, desvincular, renomear, sincronizar cópias).
- `wolf/WolfData` — dados do mod guardados no lobo.
- `event/ModEvents` — listeners (lobo entrando no mundo, clique direito em lobo com Dog Tag/Name Tag, registro de comandos).
- `network/ModNetwork`, `network/OpenTagScreenPacket`, `network/EngraveTagPacket` — canal de rede e pacotes.
- `client/DogTagScreen`, `client/ClientPacketHandlers` — tela do nome (só no cliente).
- `client/DogBedRenderer`, `client/ClientSetup` — nome flutuante sobre a cama e registros só do cliente.
- `menu/WolfPanelMenu`, `client/WolfPanelScreen`, `registry/ModMenuTypes` — Painel do lobo (textura em `textures/gui/wolf_panel.png`).
- `wolf/WolfMode`, `wolf/WolfStance`, `wolf/WolfCommands` — modos, posturas e a aplicação deles no lobo.
- `wolf/ai/WolfGoals` — instala a IA do mod em todo lobo e condiciona objetivos vanilla ao modo (`wolf/ai/ModeGatedGoal`).
- `wolf/ai/PassiveNoTargetGoal`, `wolf/ai/PassiveFleeGoal` — IA das posturas.
- `wolf/WolfHome`, `wolf/ai/HomeStateGoal`, `wolf/ai/ReturnHomeGoal`, `wolf/ai/SleepInBedGoal` — Stay Home (área de casa, retorno com teleporte, dormir).
- `wolf/FollowAcrossDimensions` — lobos em Follow acompanham o dono entre dimensões.
- `wolf/WolfSafety`, `wolf/ai/SafeFollowOwnerGoal`, `wolf/ai/LedgeGuardGoal`, `wolf/ai/StepGuardGoal`, `wolf/ai/HazardTargetGoal`, `wolf/ai/FireEscapeGoal` — segurança: seguir e teleportar com segurança, destravar, não pular de abismos, fogo, lava e creepers.
- `wolf/WolfSleep`, `client/wolf/*` — estado de sono, o renderizador e os modelos do lobo deitado (adulto e filhote) e as texturas de olhos fechados.
- `wolf/WolfRecovery`, `bond/Recovery` — respawn: lobo vinculado que morre se recupera e volta para a cama.
- `wolf/WolfScent`, `wolf/ai/ScentGoal`, `wolf/ai/ScentAlertGoal` — faro: contagem por distância, achado aos pés do lobo e o chamado com latidos. Listas em `data/faithful_fangs/loot_table/scent/`.
- `wolf/WolfSounds` — latido e choro do próprio lobo.
- `wolf/WolfUpgrade`, `wolf/WolfUpgrades` — melhorias de atributos: níveis, custos, modificadores e a compra pelo painel.
- `item/WolfArmorTier`, `wolf/WolfArmor` — tiers de armadura (material, absorção do dano, efeito do ouro nos piglins). Texturas em `textures/entity/equipment/wolf_body/` e `equipment/`.
- `command/FaithfulFangsCommand` — `/faithfulfangs debug` (inclui `bind`, `unbind`, `find` e `reset_upgrades`).
