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
- [x] **3. Dog Bed** (`faithful_fangs:<cor>_dog_bed`, 16 cores): almofada de 1 bloco com borda (encosto alto atrás, laterais e borda baixa na frente), usando a textura da lã da mesma cor. O lado aberto fica virado para quem coloca. Receita `W_W` / `WWW` com lã da mesma cor (livro de receitas desbloqueia ao pegar a lã), dropa a si mesma, empilha até 1 como as camas vanilla e aparece em Blocos Funcionais logo depois das camas vanilla. A block entity `faithful_fangs:dog_bed` já guarda o UUID e o nome do lobo e sincroniza com o cliente (ainda sem uso).
- [ ] **4. Dados do vínculo:** registro global salvo com o mundo (lobo → cama, cama → lobo, pendências) + cópia dos dados no lobo e na cama.
- [ ] **5. Fluxo de vínculo:** tela para digitar o nome, plaquinha gravada, uso na cama e casos de borda.
- [ ] **6. Nome flutuante** sobre a cama.
- [ ] **7. Painel do lobo** (Shift + clique direito), já como menu de inventário para receber o espaço de armadura na Fase 5.
- [ ] **8a. Modos Sit e Follow** pelo painel.
- [ ] **8b. Posturas** Passive / Neutral / Aggressive.
- [ ] **8c. Stay Home:** passeio no raio, defesa, dormir à noite, retorno com teleporte e invulnerabilidade.
- [ ] **8d. Melhorias do Follow:** evitar perigos, travar menos, medo de creeper.

### Estrutura do código
- `FaithfulFangs` — classe principal; registra os `DeferredRegister` e a aba criativa.
- `registry/ModBlocks`, `registry/ModItems`, `registry/ModBlockEntities`, `registry/ModDataComponents` — registros.
- `item/DogTagItem`, `item/EngravedTag` — plaquinha e o componente de gravação.
- `block/DogBedBlock`, `block/DogBedBlockEntity` — cama e os dados do vínculo guardados nela.
