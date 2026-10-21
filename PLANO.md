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
  - **Pulo no alvo seguro:** o lobo domesticado só dá o pulo de ataque vanilla se o alvo não estiver mais baixo e o chão até ele for seguro (sem buraco, lava, fogo ou neve fofa).
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

### Fase 3 — Faro (em andamento)
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
- `wolf/WolfSafety`, `wolf/ai/SafeFollowOwnerGoal`, `wolf/ai/LedgeGuardGoal`, `wolf/ai/StepGuardGoal`, `wolf/ai/HazardTargetGoal`, `wolf/ai/FireEscapeGoal` — segurança: seguir e teleportar com segurança, destravar, não pular de abismos, pulo de ataque seguro, fogo, lava e creepers.
- `wolf/WolfSleep`, `client/wolf/*` — estado de sono, o renderizador e os modelos do lobo deitado (adulto e filhote) e as texturas de olhos fechados.
- `wolf/WolfRecovery`, `bond/Recovery` — respawn: lobo vinculado que morre se recupera e volta para a cama.
- `command/FaithfulFangsCommand` — `/faithfulfangs debug`.
