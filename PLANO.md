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
- Volta depois de **5 minutos** ou quando o **dono dorme**, o que vier primeiro.
- **Mensagens:** "Rex está ferido e voltará para casa em 5 minutos" e "Rex está de volta na cama".
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
| Agilidade | Velocidade | +5% |
| Faro | Chance de achados (sistema 4) | +15% |

- **Custo por nível:**
  - Nv 1: 1 Osso Ancestral
  - Nv 2: 2 Ossos Ancestrais
  - Nv 3: 1 Osso Ígneo
  - Nv 4: 2 Ossos Ígneos
  - Nv 5: 2 Ossos Etéreos
- **Sem limite total:** o lobo pode maximizar todos os atributos.
- **Não dá para desfazer.**
- As melhorias **são mantidas** no respawn e quando a cama é quebrada.

### 6. Armadura — ✅ DEFINIDO
- Colocada pelo **espaço de armadura do Painel do lobo**. O clique direito vanilla continua funcionando.
- **Tiers**, do mais fraco ao mais forte:

| Tier | Obtenção | Observação |
|---|---|---|
| Escamas de tatu | Vanilla | Base |
| Cobre | Receita com lingotes de cobre | Melhor que tatu, pior que ferro |
| Ferro | Receita com lingotes de ferro | |
| Ouro | Receita com lingotes de ouro | Fraca, mas **piglins ignoram o lobo** |
| Diamante | Receita com diamantes | Alta |
| Netherite | Diamante + lingote de netherite na mesa de ferraria | A melhor. O **item** não queima no fogo ou na lava (como todo item de netherite), mas **não protege o lobo** do fogo |

- **Encantamentos:** aceita **todos** os encantamentos de armadura do jogador (capacete, peitoral, calças e botas). Cada um funciona no lobo como funcionaria no jogador, por exemplo Queda de Pena reduz dano de queda e Passos Profundos acelera na água.
- **Durabilidade:** gasta quando o lobo é atingido, como a armadura do jogador. Tiers melhores duram mais.
- **Morte:** funciona **igual ao jogador**. Não há perda extra de durabilidade ao morrer; a armadura só se desgasta pelos golpes recebidos, inclusive o golpe fatal. Ela continua no lobo após o respawn.

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

### Fase 1 — Cama, plaquinha, vínculo e modos (em andamento)
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
  - **Aggressive:** ataca qualquer mob hostil (interface `Enemy`) ao alcance, mesmo antes do dono, desde que consiga ver e chegar até ele. Ignora creepers (medo de creeper vem no 8d), endermen e zombified piglins calmos, piglins que não estão hostis, warden, wither e dragão. Lobo sentado não ataca.
  - Trocar para Passive no meio de uma briga faz o lobo parar na hora.
- [x] **8c. Stay Home:**
  - **Área de casa:** usa a restrição vanilla de "home" dos mobs, centrada na cama com raio 10. Com ela, o passeio aleatório vanilla fica dentro da área e o lobo só escolhe/briga com alvos dentro dela (é assim que defende a casa sem sair correndo atrás de monstros). Alvos que saem da área (+2 blocos) são largados. Fora da área (até 24 blocos) ele volta andando.
  - **Retorno:** mais longe que 24 blocos, ou em outra dimensão: anda ~5 s (para a cama se houver caminho, senão para longe do dono) e teleporta para cima da cama, em qualquer dimensão. Fica invulnerável durante o retorno (invulnerabilidade temporária renovada a cada tick, então some sozinha se algo interromper). A área de chegada fica carregada por alguns segundos (ticket de portal vanilla) e depois descarrega normalmente com o lobo salvo nela. Nuvenzinha de partículas na saída e na chegada.
  - **Dormir:** à noite (Overworld) o lobo vai até a cama, se ajeita no meio dela virado para o lado aberto e **dorme enrolado**: peito de um lado, traseiro encostado no fundo, patas de trás e rabo dando a volta pelo outro lado, cabeça apoiada nas patas da frente com o focinho virado para o rabo. Levanta de manhã, se tiver alvo para brigar ou se apanhar. Se não alcançar a cama em 30 s, tenta de novo 1 min depois. O Painel mostra "Sleeping".
    - Técnica: o estado "dormindo" é a pose de entidade `SITTING` (a que o camelo usa para deitar; sincronizada pelo vanilla e sem mudar a hitbox — a pose `SLEEPING` encolheria o lobo para 0,2 bloco). O renderizador do lobo foi substituído por uma cópia do vanilla (`client/wolf/CurlingWolfRenderer`) cujo modelo tem a pose enrolada; a camada de armadura também acompanha. Filhotes dormem sentados. Ressalva: outro mod que também troque o renderizador do lobo vai conflitar com este.
  - **Objetivos vanilla desligados no Stay Home:** seguir o dono (que também teleporta até ele), o pânico com fogo/lava que teleporta até o dono (vira um pânico comum) e o "sentar" automático que o vanilla faz quando o dono está offline ou em outra dimensão (no Stay Home só senta se mandado).
  - Não abre portas (lobos vanilla não abrem).
- [x] **Follow entre dimensões** (pedido em 19/10/2026): quando o dono troca de dimensão (Nether, End ou qualquer dimensão de outro mod), os lobos que o seguem vão junto e aparecem ao lado dele. Vale para lobos em Follow, de pé, sem guia nem montaria, a até 24 blocos do dono no momento da viagem. Cobre portais, teletransporte por comando e a primeira saída do End (créditos).
- [ ] **8d. Melhorias do Follow:** evitar perigos, travar menos, medo de creeper.

### Estrutura do código
- `FaithfulFangs` — classe principal; registra os `DeferredRegister` e a aba criativa.
- `registry/ModBlocks`, `registry/ModItems`, `registry/ModBlockEntities`, `registry/ModDataComponents` — registros.
- `item/DogTagItem`, `item/EngravedTag` — plaquinha e o componente de gravação.
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
- `wolf/WolfSleep`, `client/wolf/*` — estado de sono e o renderizador/modelo do lobo enrolado.
- `command/FaithfulFangsCommand` — `/faithfulfangs debug`.
