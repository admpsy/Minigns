// ============================================================
// Crypts & Dungeons — Web Recreation
// Translated from Kotlin/Compose to vanilla JS
// ============================================================

// =================== GAME DATA ===================

const HERO_CLASSES = {
  WARRIOR: { name:'Guerreiro', title:'Guardião de Ferro', role:'Tanque & Controle', color:'#ef4444',
    hp:120, mp:25, atk:18, def:14, mag:4, spd:9,
    desc:'Mestre da espada e escudo. Protege aliados e desfere golpes devastadores.',
    skills:[
      {id:'war_strike',name:'Golpe Poderoso',desc:'Golpe pesado com a lâmina.',mp:0,ap:1,power:24,range:1,type:'SINGLE_ENEMY',dmg:'PHYSICAL'},
      {id:'war_cleave',name:'Clivar',desc:'Ataca todos os inimigos adjacentes.',mp:8,ap:1,power:18,range:1,type:'ALL_ENEMIES',dmg:'PHYSICAL'},
      {id:'war_taunt',name:'Provocação',desc:'Atrai a fúria inimiga e ganha armadura.',mp:10,ap:1,power:0,range:3,type:'SINGLE_ENEMY',dmg:'PHYSICAL'},
      {id:'war_execute',name:'Execução',desc:'Dano dobrado se alvo abaixo de 50% HP.',mp:15,ap:1,power:45,range:1,type:'SINGLE_ENEMY',dmg:'PHYSICAL'},
    ]},
  MAGE: { name:'Mago', title:'Arcanista das Sombras', role:'Dano em Área', color:'#3b82f6',
    hp:70, mp:100, atk:6, def:5, mag:22, spd:10,
    desc:'Canaliza correntes místicas para incinerar e congelar.',
    skills:[
      {id:'mag_bolt',name:'Projétil Arcano',desc:'Disparo veloz de energia mística.',mp:6,ap:1,power:20,range:4,type:'SINGLE_ENEMY',dmg:'ARCANE'},
      {id:'mag_fireball',name:'Bola de Fogo',desc:'Detonação flamejante + queimadura.',mp:18,ap:1,power:36,range:4,type:'SINGLE_ENEMY',dmg:'FIRE'},
      {id:'mag_frost',name:'Nova de Gelo',desc:'Congela inimigos por 1 turno.',mp:16,ap:1,power:22,range:3,type:'ALL_ENEMIES',dmg:'FROST'},
      {id:'mag_lightning',name:'Relâmpagos',desc:'Raio que salta entre inimigos.',mp:25,ap:1,power:50,range:4,type:'ALL_ENEMIES',dmg:'ARCANE'},
    ]},
  CLERIC: { name:'Clérico', title:'Sacerdote da Luz', role:'Cura & Dano Sagrado', color:'#fbbf24',
    hp:95, mp:80, atk:12, def:10, mag:16, spd:8,
    desc:'Empunha a bênção solar. Cura ferimentos e ergue escudos divinos.',
    skills:[
      {id:'cle_smite',name:'Golpe Sagrado',desc:'Dano bônus contra mortos-vivos.',mp:8,ap:1,power:22,range:1,type:'SINGLE_ENEMY',dmg:'HOLY'},
      {id:'cle_heal',name:'Prece de Cura',desc:'Restaura a vida de um aliado.',mp:14,ap:1,power:35,range:3,type:'SINGLE_ALLY',dmg:'HEAL'},
      {id:'cle_radiance',name:'Radiância Divina',desc:'Cura aliados e fere trevas.',mp:22,ap:1,power:25,range:3,type:'ALL_ALLIES',dmg:'HOLY'},
      {id:'cle_sanctuary',name:'Santuário',desc:'Barreira que absorve dano.',mp:20,ap:1,power:30,range:3,type:'ALL_ALLIES',dmg:'HEAL'},
    ]},
  ROGUE: { name:'Ladino', title:'Assassino das Sombras', role:'Furtividade & Críticos', color:'#8b5cf6',
    hp:80, mp:40, atk:20, def:7, mag:6, spd:15,
    desc:'Move-se invisível, desarma armadilhas e desfere golpes fatais.',
    skills:[
      {id:'rog_stab',name:'Punhalada Rápida',desc:'Ataque veloz com chance de crítico.',mp:4,ap:1,power:26,range:1,type:'SINGLE_ENEMY',dmg:'PHYSICAL'},
      {id:'rog_poison',name:'Lâmina Envenenada',desc:'Aplica veneno contínuo.',mp:10,ap:1,power:20,range:1,type:'SINGLE_ENEMY',dmg:'POISON'},
      {id:'rog_stealth',name:'Passo das Sombras',desc:'Fica invisível. Crítico garantido.',mp:12,ap:1,power:0,range:0,type:'SELF',dmg:'SHADOW'},
      {id:'rog_assassinate',name:'Assassinar',desc:'80% chance de crítico.',mp:18,ap:1,power:55,range:1,type:'SINGLE_ENEMY',dmg:'PHYSICAL'},
    ]},
};

const THEMES = {
  FORGOTTEN_CATACOMBS: { title:'Catacumbas Esquecidas', sub:'Ossuários & Nichos Mortuários', danger:'Iniciante • Nível 1-4', color:'#78716C',
    desc:'Corredores estreitos com pilhas de crânios, sarcófagos rachados e portas secretas.' },
  ROYAL_CRYPT: { title:'Cripta Real', sub:'Tumbas de Reis & Vitrais', danger:'Intermediário • Nível 4-7', color:'#ca8a04',
    desc:'Salões grandiosos com colunas ornamentadas e sarcófagos reais guardados por armadilhas.' },
  ABANDONED_DUNGEON: { title:'Masmorra Abandonada', sub:'Celas Enferrujadas & Poços', danger:'Perigoso • Nível 7-10', color:'#475569',
    desc:'Antigo cárcere com grades de ferro oxidado, poços e marcas de garras.' },
  NATURAL_CAVERNS: { title:'Cavernas Naturais', sub:'Fungos Bioluminescentes', danger:'Avançado • Nível 10-13', color:'#0284c7',
    desc:'Lagos subterrâneos, cogumelos gigantes azulados e fendas de gás tóxico.' },
  SUBTERRANEAN_TEMPLE: { title:'Templo Subterrâneo', sub:'Estátuas Colossais & Runas', danger:'Mortal • Nível 13-17', color:'#9333ea',
    desc:'Átrios com estátuas de divindades esquecidas, altares de sacrifício e runas brilhantes.' },
  ANCIENT_RUINS: { title:'Ruínas Antigas', sub:'Arcos Quebrados & Maquinário', danger:'Lendário • Nível 17-20', color:'#d97706',
    desc:'Civilização pré-humana com maquinário antigo e bibliotecas proibidas.' },
};

const ENEMIES = {
  FORGOTTEN_CATACOMBS: {
    normal: [
      {name:'Esqueleto Guerreiro',title:'Sentinela Fúnebre',hp:45,atk:14,def:6,spd:7,xp:40,gold:20},
      {name:'Arqueiro dos Ossos',title:'Atirador Cadavérico',hp:35,atk:15,def:4,spd:11,xp:45,gold:25},
      {name:'Zumbi Pútrido',title:'Corpo Decomposto',hp:60,atk:16,def:8,spd:5,xp:50,gold:22},
    ],
    boss: [
      {name:'Rei Esqueleto Eterno',title:'Senhor dos Ossuários',hp:220,atk:24,def:14,spd:9,xp:250,gold:180,isBoss:true},
      {name:'Guarda de Elite',title:'Protetor Fúnebre',hp:65,atk:16,def:10,spd:8,xp:60,gold:30},
    ]
  },
  ROYAL_CRYPT: {
    normal: [
      {name:'Cavaleiro Espectral',title:'Guardião da Cripta',hp:110,atk:22,def:16,spd:10,xp:95,gold:70},
      {name:'Aparição dos Vitrais',title:'Espírito Vingativo',hp:70,atk:12,def:6,spd:13,xp:85,gold:60},
    ],
    boss: [{name:'Cavaleiro da Coroa',title:'Guardião Real',hp:180,atk:26,def:18,spd:11,xp:200,gold:150,isBoss:true}]
  },
  ABANDONED_DUNGEON: {
    normal: [
      {name:'Torturador Cego',title:'Executor',hp:130,atk:26,def:12,spd:8,xp:110,gold:85},
      {name:'Gárgula de Correntes',title:'Sentinela Animada',hp:95,atk:20,def:18,spd:9,xp:100,gold:75},
    ],
    boss: [{name:'Carrasco',title:'Executor das Masmorras',hp:200,atk:30,def:16,spd:10,xp:220,gold:160,isBoss:true}]
  },
  NATURAL_CAVERNS: {
    normal: [
      {name:'Aranha Gigante',title:'Predadora das Teias',hp:140,atk:25,def:10,spd:16,xp:140,gold:95},
      {name:'Verme de Pedra',title:'Horror das Fendas',hp:180,atk:28,def:20,spd:7,xp:160,gold:110},
    ],
    boss: [{name:'Matriarca Peçonhenta',title:'Rainha das Teias',hp:250,atk:32,def:14,spd:18,xp:300,gold:200,isBoss:true}]
  },
  SUBTERRANEAN_TEMPLE: {
    normal: [
      {name:'Sacerdote das Runas',title:'Invocador do Abismo',hp:160,atk:18,def:14,spd:12,xp:220,gold:160},
      {name:'Demônio Invocado',title:'Criatura Profana',hp:210,atk:35,def:18,spd:11,xp:280,gold:190},
    ],
    boss: [{name:'Grão-Sacerdote',title:'Demoníaco',hp:300,atk:38,def:20,spd:14,xp:400,gold:300,isBoss:true}]
  },
  ANCIENT_RUINS: {
    normal: [
      {name:'Guardião Automato',title:'Máquina Antiga',hp:200,atk:30,def:24,spd:10,xp:250,gold:180},
    ],
    boss: [{name:'Titã Golem de Pedra',title:'Colosso Protetor',hp:380,atk:42,def:30,spd:6,xp:500,gold:400,isBoss:true}]
  }
};

const LOOT = [
  {name:'Lâmina da Cripta Profana',type:'WEAPON',atk:22,rarity:'Épico',val:240},
  {name:'Cajado das Estrelas Caídas',type:'WEAPON',mag:26,rarity:'Épico',val:260},
  {name:'Arco do Caçador Abissal',type:'WEAPON',atk:18,spd:4,rarity:'Raro',val:190},
  {name:'Armadura de Placas de Titânio',type:'ARMOR',def:18,hp:40,rarity:'Épico',val:300},
  {name:'Anel do Olho do Dragão',type:'ACCESSORY',atk:8,crit:20,spd:5,rarity:'Lendário',val:500},
  {name:'Amuleto da Luz Eterna',type:'ACCESSORY',hp:35,mag:12,rarity:'Épico',val:350},
  {name:'Gema de Rubi Flamejante',type:'GEM',atk:6,rarity:'Raro',val:150},
  {name:'Gema de Safira Gélida',type:'GEM',mag:8,rarity:'Raro',val:150},
];

const TILE = { FLOOR:'.', WALL:'#', DOOR:'+', SAC:'S', CHEST:'C', ALTAR:'A', TRAP_S:'^', TRAP_P:'~', SECRET:'?', FOUNTAIN:'F', TORCH:'T', EXIT:'>', RUBBLE:'R' };
const TILE_INFO = {
  '.':{cls:'floor',walk:true}, '#':{cls:'wall',walk:false}, '+':{cls:'door',walk:true},
  'S':{cls:'sarcophagus',walk:false}, 'C':{cls:'chest',walk:true}, 'A':{cls:'altar',walk:false},
  '^':{cls:'trap-spike',walk:true}, '~':{cls:'trap-poison',walk:true}, '?':{cls:'secret-door',walk:true},
  'F':{cls:'fountain',walk:false}, 'T':{cls:'torch-stand',walk:false}, '>':{cls:'exit',walk:true},
  'R':{cls:'rubble',walk:true}
};

// =================== GAME STATE ===================

let S = {};

function newGame() {
  S = {
    screen: 'HOME',
    theme: 'FORGOTTEN_CATACOMBS',
    party: createParty(),
    activeHero: 0,
    resources: { gold:120, torches:4, rations:6, potions:3 },
    torchRadius: 3.5,
    steps: 0,
    map: null,
    mapW: 18, mapH: 14,
    px: 2, py: 2,
    turn: 1,
    patrols: [],
    banner: null,
    logs: [],
    inventory: LOOT.slice(0,4),
    // Combat
    enemies: [], initiative: [], turnIdx: 0, round: 1,
    selEnemy: 0, selSkill: null, enemyTurn: false, diceRoll: null,
    arena: makeArena(),
    // Victory
    gainXp: 0, gainGold: 0, gainLoot: [],
  };
}

function createParty() {
  const defs = [
    {cls:'WARRIOR', name:'Sir Valerius', wpn:{name:'Espada Longa de Ferro',atk:6}, arm:{name:'Cota de Malha',def:8}, acc:{name:'Anel do Guardião',hp:20,def:2}},
    {cls:'MAGE', name:'Eldrin Sombrastral', wpn:{name:'Cajado das Runas Gélidas',mag:12}, arm:{name:'Túnica do Místico',def:3,mag:4}, acc:{name:'Amuleto de Lápis-Lazúli',mag:6}},
    {cls:'CLERIC', name:'Irmã Lysandra', wpn:{name:'Maça da Aurora',atk:8,mag:6}, arm:{name:'Escamas Solenes',def:6}, acc:{name:'Relicário de Prata',hp:15,mag:5}},
    {cls:'ROGUE', name:'Vesper Sombra-Ágil', wpn:{name:'Adagas de Obsidiana',atk:14,crit:15}, arm:{name:'Gibão Furtivo',def:5,spd:3}, acc:{name:'Capa do Notívago',spd:4,crit:5}},
  ];
  return defs.map(d => {
    const c = HERO_CLASSES[d.cls];
    return {
      id: crypto.randomUUID(), name: d.name, cls: d.cls, class: c,
      level: 1, xp: 0, maxXp: 100,
      hp: c.hp, maxHp: c.hp, mp: c.mp, maxMp: c.mp, ap: 2, maxAp: 2,
      atk: c.atk, def: c.def, mag: c.mag, spd: c.spd, crit: 10,
      skills: c.skills, wpn: d.wpn, arm: d.arm, acc: d.acc,
      gx: 0, gy: 0, guard: false, alive: true, moved: false,
      statuses: [],
    };
  });
}

function heroTotal(hero, stat) {
  let v = hero[stat];
  if (hero.wpn) {
    if (stat==='atk') v += hero.wpn.atk||0;
    if (stat==='mag') v += hero.wpn.mag||0;
    if (stat==='crit') v += hero.wpn.crit||0;
  }
  if (hero.arm) {
    if (stat==='def') v += hero.arm.def||0;
    if (stat==='mag') v += hero.arm.mag||0;
    if (stat==='spd') v += hero.arm.spd||0;
  }
  if (hero.acc) {
    if (stat==='atk') v += hero.acc.atk||0;
    if (stat==='def') v += hero.acc.def||0;
    if (stat==='mag') v += hero.acc.mag||0;
    if (stat==='spd') v += hero.acc.spd||0;
    if (stat==='crit') v += hero.acc.crit||0;
    if (stat==='hp') v += hero.acc.hp||0;
  }
  if (stat==='def' && hero.guard) v += 6;
  return v;
}

function makeArena() {
  const tiles = [];
  for (let x=0;x<6;x++) for (let y=0;y<4;y++) {
    tiles.push({x,y,cover:(x===2&&y===1)||(x===3&&y===2), trap:(x===2&&y===2)||(x===3&&y===1)});
  }
  return tiles;
}

// =================== DUNGEON GENERATION ===================

function genMap(theme) {
  const W=18,H=14;
  const grid = Array(H).fill().map(()=>Array(W).fill('#'));
  const rooms = [[1,1,5,5],[8,1,8,4],[2,8,5,4],[9,7,7,5]];
  for (const [rx,ry,rw,rh] of rooms)
    for (let y=ry;y<ry+rh;y++) for (let x=rx;x<rx+rw;x++)
      if (y>=0&&y<H&&x>=0&&x<W) grid[y][x]='.';
  for (let x=5;x<=8;x++) grid[3][x]='.';
  for (let y=5;y<=8;y++) grid[y][3]='.';
  for (let y=4;y<=7;y++) grid[y][12]='.';
  for (let x=6;x<=9;x++) grid[9][x]='.';

  const props = {
    FORGOTTEN_CATACOMBS: [[2,10,'S'],[2,14,'S'],[2,12,'C'],[9,4,'A'],[3,7,'^'],[9,8,'~'],[10,14,'>'],[1,1,'T'],[8,1,'T'],[7,9,'T'],[5,3,'?']],
    ROYAL_CRYPT: [[2,11,'S'],[2,13,'S'],[3,12,'A'],[9,3,'F'],[10,13,'C'],[10,14,'>'],[3,6,'^'],[9,7,'^'],[1,4,'T'],[7,15,'T']],
    ABANDONED_DUNGEON: [[3,6,'+'],[6,3,'+'],[9,7,'+'],[2,12,'R'],[10,11,'C'],[9,4,'X'],[10,14,'>'],[2,3,'T'],[8,10,'T']],
    NATURAL_CAVERNS: [[2,12,'F'],[3,10,'~'],[9,4,'~'],[10,12,'C'],[9,8,'X'],[10,14,'>']],
    SUBTERRANEAN_TEMPLE: [[2,12,'A'],[9,12,'A'],[9,3,'F'],[2,14,'C'],[10,14,'>'],[3,7,'?']],
    ANCIENT_RUINS: [[2,10,'R'],[3,14,'C'],[9,3,'A'],[8,12,'R'],[10,14,'>'],[3,6,'^']],
  };
  const p = props[theme]||[];
  for (const [y,x,t] of p) { if (t==='X') grid[y]===undefined||x===undefined||(grid[y][x]='.'); else grid[y]&&(grid[y][x]=t); }
  // Fix: props format is [x,y,type] actually [y,x,type] based on grid[y][x]
  // Let me redo this properly
  const grid2 = Array(H).fill().map(()=>Array(W).fill('#'));
  for (const [rx,ry,rw,rh] of rooms)
    for (let y=ry;y<ry+rh;y++) for (let x=rx;x<rx+rw;x++)
      if (y>=0&&y<H&&x>=0&&x<W) grid2[y][x]='.';
  for (let x=5;x<=8;x++) grid2[3][x]='.';
  for (let y=5;y<=8;y++) grid2[y][3]='.';
  for (let y=4;y<=7;y++) grid2[y][12]='.';
  for (let x=6;x<=9;x++) grid2[9][x]='.';

  // Apply props as [row, col, type]
  for (const [r,c,t] of p) {
    if (r>=0&&r<H&&c>=0&&c<W) {
      if (t==='X') grid2[r][c]='.'; // fissure = passable floor visually
      else grid2[r][c]=t;
    }
  }

  const tiles = [];
  for (let y=0;y<H;y++) for (let x=0;x<W;x++) {
    const dist = Math.hypot(x-2, y-2);
    tiles.push({ x, y, type: grid2[y][x], explored: dist<=3.5, visible: dist<=3.5, triggered: false, looted: false });
  }
  return { tiles, W, H, theme };
}

function updateVision() {
  for (const t of S.map.tiles) {
    const d = Math.hypot(t.x - S.px, t.y - S.py);
    t.visible = d <= S.torchRadius;
    if (t.visible) t.explored = true;
  }
}

// =================== EXPLORATION ===================

function movePlayer(dx, dy) {
  const nx = S.px + dx, ny = S.py + dy;
  if (nx<0||nx>=S.map.W||ny<0||ny>=S.map.H) return;
  const tile = S.map.tiles.find(t=>t.x===nx&&t.y===ny);
  if (!tile) return;
  const info = TILE_INFO[tile.type];
  if (!info.walk && tile.type!=='+' && tile.type!=='?') return;

  // Traps
  if (tile.type==='^' && !tile.triggered) {
    S.party.forEach(h => h.hp = Math.max(1, h.hp - 12));
    tile.triggered = true;
    addLog('Armadilha de Espinhos! -12 HP no grupo.', 'attack');
  }
  if (tile.type==='~' && !tile.triggered) {
    S.party.forEach(h => h.hp = Math.max(1, h.hp - 8));
    tile.triggered = true;
    addLog('Nuvem Tóxica! -8 HP no grupo.', 'status');
  }

  // Open doors
  if (tile.type==='+' || tile.type==='?') { tile.type='.'; tile.triggered=true; addLog('Porta aberta.', 'system'); }

  S.px = nx; S.py = ny;
  advanceTurn();

  // Check encounter zones
  if (tile.type==='>') startCombat(true);
  else if ((nx===7&&ny===4)||(nx===10&&ny===5)) startCombat(false);

  render();
}

function interact() {
  const tile = S.map.tiles.find(t=>t.x===S.px&&t.y===S.py);
  if (!tile) return;
  switch(tile.type) {
    case 'C':
      if (!tile.looted) {
        const gold = Math.floor(40+Math.random()*50);
        const loot = LOOT[Math.floor(Math.random()*LOOT.length)];
        S.resources.gold += gold;
        S.inventory.push({...loot, id: crypto.randomUUID()});
        tile.looted = true; tile.type='.';
        setBanner(`Baú Aberto! +${gold} Ouro, ${loot.name}!`);
        addLog(`Baú: +${gold} ouro, ${loot.name}!`, 'loot');
      } break;
    case 'S':
      if (!tile.looted) {
        if (Math.random()<0.5) { startCombat(false); return; }
        const gold = Math.floor(30+Math.random()*40);
        S.resources.gold += gold;
        tile.looted = true;
        setBanner(`Sarcófago: +${gold} Ouro`);
        addLog(`Sarcófago saqueado: +${gold} ouro`, 'loot');
      } break;
    case 'A':
      S.party.forEach(h => { h.hp=Math.min(h.maxHp,h.hp+40); h.mp=Math.min(h.maxMp,h.mp+30); });
      setBanner('Bênção Ancestral! Vida, Mana e Sanidade restaurados!');
      addLog('Altar ativado! Graça Divina no grupo!', 'heal');
      break;
    case 'F':
      S.party.forEach(h => { h.hp=h.maxHp; h.mp=h.maxMp; });
      setBanner('Fonte Luminescente! Vida e Mana totalmente restaurados!');
      addLog('Fonte purifica todas as feridas!', 'heal');
      break;
    case 'T':
      S.resources.torches++;
      addLog('Tocha coletada do suporte.', 'loot');
      break;
  }
  render();
}

function rest() {
  if (S.resources.rations<=0) { setBanner('Sem rações para descansar!'); render(); return; }
  S.resources.rations--;
  S.party.forEach(h => {
    h.hp = Math.min(h.maxHp, h.hp + Math.floor(h.maxHp*0.4));
    h.mp = Math.min(h.maxMp, h.mp + Math.floor(h.maxMp*0.3));
  });
  setBanner('Descanso Curto (-1 Ração)');
  addLog('Descanso realizado. Vida e mana revigorados.', 'system');
  render();
}

function useTorch() {
  if (S.resources.torches<=0) { setBanner('Sem tochas!'); render(); return; }
  S.resources.torches--;
  S.torchRadius = 4.5;
  S.steps = 0;
  setBanner('Nova Tocha Acesa! Visão restaurada.');
  updateVision();
  render();
}

function advanceTurn() {
  S.turn++;
  S.steps++;
  if (S.steps>=10) {
    S.steps=0;
    if (S.resources.torches>0) { S.resources.torches--; S.torchRadius=3.5; }
    else { S.torchRadius=1.8; S.party.forEach(h=>h.hp=Math.max(1,h.hp-2)); }
  }
  // Patrol movement
  S.patrols = S.patrols.filter(p => {
    const d = Math.abs(p.gx-S.px)+Math.abs(p.gy-S.py);
    if (d<=1) { startCombat(false, [p]); return false; }
    const dirs = [[1,0],[-1,0],[0,1],[0,-1]].filter(([dx,dy])=>{
      const t = S.map.tiles.find(tt=>tt.x===p.gx+dx&&tt.y===p.gy+dy);
      return t && TILE_INFO[t.type].walk;
    });
    if (dirs.length) {
      const best = d<=6 ? dirs.reduce((a,b)=>{
        const da=Math.abs(p.gx+a[0]-S.px)+Math.abs(p.gy+a[1]-S.py);
        const db=Math.abs(p.gx+b[0]-S.px)+Math.abs(p.gy+b[1]-S.py);
        return da<=db?a:b;
      }) : dirs[Math.floor(Math.random()*dirs.length)];
      p.gx += best[0]; p.gy += best[1];
    }
    return true;
  });
  updateVision();
}

// =================== COMBAT ===================

function startCombat(isBoss, specificEnemies) {
  const theme = S.theme;
  const pool = ENEMIES[theme] || ENEMIES.FORGOTTEN_CATACOMBS;
  const src = isBoss ? pool.boss : pool.normal;
  const enemyDefs = specificEnemies || src;

  S.enemies = enemyDefs.map((e,i) => ({
    ...e, id: crypto.randomUUID(),
    gx: i%2===0?4:5, gy: i%4, alive: true, guard: false,
    intent: null, statuses: [],
  }));

  // Position heroes
  S.party.forEach((h,i) => { h.gx = i%2===0?1:0; h.gy = i%4; h.ap=h.maxAp; h.guard=false; h.moved=false; });

  // Generate intents
  S.enemies.forEach(e => e.intent = genEnemyIntent(e));

  // Initiative
  S.initiative = [];
  S.party.filter(h=>h.alive).forEach(h => S.initiative.push({id:h.id,name:h.name,hero:true,speed:Math.floor(Math.random()*20+1)+heroTotal(h,'spd'),alive:true}));
  S.enemies.forEach(e => S.initiative.push({id:e.id,name:e.name,hero:false,speed:Math.floor(Math.random()*20+1)+e.spd,alive:true}));
  S.initiative.sort((a,b)=>b.speed-a.speed);

  S.round = 1; S.turnIdx = 0; S.screen = 'COMBAT'; S.enemyTurn = false;
  const first = S.initiative[0];
  S.activeHero = first.hero ? S.party.findIndex(h=>h.id===first.id) : 0;
  S.selEnemy = 0;
  S.selSkill = S.party[S.activeHero]?.skills[0] || null;
  S.diceRoll = null;

  addLog(`⚔ Combate Iniciado! Iniciativas: ${S.initiative.map(i=>i.name.split(' ')[0]+':'+i.speed).join(', ')}`, 'system');
  addLog(`Rodada 1 • Turno de ${first.name}!`, 'system');
  setBanner(first.hero ? `Sua vez! Turno de ${first.name}.` : `Turno de ${first.name}...`);

  if (!first.hero) {
    S.enemyTurn = true;
    setTimeout(()=>enemyTurn(first), 800);
  }
  render();
}

function genEnemyIntent(enemy) {
  if (enemy.isBoss) {
    const r = Math.random();
    if (r<0.45) return {type:'HEAVY_SMASH',desc:'Golpe Esmagador',dmg:Math.floor(enemy.atk*1.5)};
    if (r<0.75) return {type:'ARCANE_SPELL',desc:'Magia Sombria em Área',dmg:Math.floor(enemy.atk*0.8)};
    return {type:'DEFENSIVE_GUARD',desc:'Barreira Óssea',dmg:0};
  }
  const r = Math.random();
  if (r<0.6) return {type:'ATTACK',desc:'Ataque direto',dmg:Math.floor(enemy.atk*0.9)};
  if (r<0.85) return {type:'HEAVY_SMASH',desc:'Golpe forte',dmg:Math.floor(enemy.atk*1.3)};
  return {type:'DEFENSIVE_GUARD',desc:'Guarda defensiva',dmg:0};
}

function rollD20(adv=false, dis=false) {
  const r1 = Math.floor(Math.random()*20+1);
  if (adv && !dis) return Math.max(r1, Math.floor(Math.random()*20+1));
  if (dis && !adv) return Math.min(r1, Math.floor(Math.random()*20+1));
  return r1;
}

function heroAttack() {
  const hero = S.party[S.activeHero];
  if (!hero || !hero.alive || hero.ap<=0 || S.enemyTurn) return;
  const skill = S.selSkill || hero.skills[0];
  if (hero.mp < skill.mp) { setBanner(`Mana insuficiente para ${skill.name}!`); render(); return; }

  hero.mp -= skill.mp;
  hero.ap -= skill.ap || 1;

  // Healing skills
  if (skill.type==='SINGLE_ALLY' || skill.type==='ALL_ALLIES') {
    S.party.forEach(h => {
      if (skill.type==='ALL_ALLIES' || h.id===hero.id) {
        h.hp = Math.min(h.maxHp, h.hp + skill.power);
        h.statuses.push({type:'BLESSED',dur:2});
      }
    });
    addLog(`${hero.name} canalizou ${skill.name}! Grupo curado!`, 'heal');
    checkEndTurn();
    render();
    return;
  }

  // Self skills (stealth)
  if (skill.type==='SELF') {
    hero.statuses.push({type:'STEALTHED',dur:2});
    addLog(`${hero.name} entrou em Furtividade!`, 'status');
    checkEndTurn();
    render();
    return;
  }

  // Offensive
  const enemy = S.enemies[S.selEnemy];
  if (!enemy || !enemy.alive) {
    // Find first alive enemy
    const idx = S.enemies.findIndex(e=>e.alive);
    if (idx===-1) { endCombat(true); return; }
    S.selEnemy = idx;
  }
  const target = S.enemies[S.selEnemy];
  if (!target || !target.alive) return;

  const stealthed = hero.statuses.some(s=>s.type==='STEALTHED');
  const dist = Math.abs(hero.gx-target.gx)+Math.abs(hero.gy-target.gy);
  const isMelee = dist<=1;
  const flanking = isMelee && S.party.some(a => a.id!==hero.id && a.alive &&
    Math.abs(a.gx-target.gx)+Math.abs(a.gy-target.gy)<=1);

  const hasAdv = stealthed || flanking || hero.cls==='ROGUE';
  const d20 = rollD20(hasAdv, hero.hp < hero.maxHp*0.2);
  const isCrit = d20>=19 || (stealthed && hero.cls==='ROGUE');
  const isMiss = d20===1;

  if (isMiss) {
    addLog(`${hero.name} usou ${skill.name}! D20:${d20} — Falha Crítica!`, 'attack');
    S.diceRoll = {d20, total:d20, isCrit:false, isMiss:true, name:skill.name, actor:hero.name, target:target.name, dmg:0};
    checkEndTurn(); render(); return;
  }

  let comboName = null, comboMult = 1;
  if (flanking) { comboName='Flanqueamento (+35%)'; comboMult += 0.35; }
  if (hero.cls==='ROGUE' && stealthed) { comboName='Golpe Furtivo (+60%)'; comboMult += 0.6; }

  const statPower = ['FIRE','FROST','ARCANE','HOLY'].includes(skill.dmg) ? heroTotal(hero,'mag') : heroTotal(hero,'atk');
  let rawDmg = (statPower*0.8 + skill.power + d20*0.5) * comboMult;
  if (isCrit) rawDmg *= 1.8;
  let defRed = target.def * 0.4 + (target.guard ? 8 : 0);
  const dmg = Math.max(4, Math.floor(rawDmg - defRed));

  target.hp -= dmg;
  if (target.hp<=0) { target.hp=0; target.alive=false; addLog(`${target.name} foi derrotado!`, 'loot'); }

  // Status effects
  if (skill.id==='mag_frost') { target.statuses.push({type:'FROZEN',dur:1}); addLog(`${target.name} congelado!`, 'status'); }
  if (skill.id==='mag_fireball') { target.statuses.push({type:'BURNING',dur:2,mag:8}); addLog(`${target.name} queimando!`, 'status'); }
  if (skill.id==='rog_poison') { target.statuses.push({type:'POISONED',dur:3,mag:10}); addLog(`${target.name} envenenado!`, 'status'); }

  // Remove stealth
  hero.statuses = hero.statuses.filter(s=>s.type!=='STEALTHED');

  const critTxt = isCrit ? ' ★ CRÍTICO!' : '';
  const comboTxt = comboName ? ` [${comboName}]` : '';
  addLog(`${hero.name} → ${skill.name} em ${target.name}! D20:${d20} → ${dmg} dano${critTxt}${comboTxt}`, isCrit?'crit':'attack');
  S.diceRoll = {d20, total:d20, isCrit, isMiss:false, name:skill.name, actor:hero.name, target:target.name, dmg, combo:comboName};

  if (S.enemies.every(e=>!e.alive)) { endCombat(true); return; }
  checkEndTurn();
  render();
}

function heroGuard() {
  const hero = S.party[S.activeHero];
  if (!hero||!hero.alive||hero.ap<=0||S.enemyTurn) return;
  hero.ap--; hero.guard=true;
  hero.statuses.push({type:'SHIELDED',dur:1});
  addLog(`${hero.name} assumiu Postura Defensiva!`, 'status');
  checkEndTurn(); render();
}

function heroMove(gx, gy) {
  const hero = S.party[S.activeHero];
  if (!hero||!hero.alive||S.enemyTurn) return;
  if (hero.ap<=0 && hero.moved) { setBanner('Sem AP para mover!'); render(); return; }
  if (gx<0||gx>5||gy<0||gy>3) return;
  const occupied = S.party.some(h=>h.id!==hero.id&&h.alive&&h.gx===gx&&h.gy===gy) ||
    S.enemies.some(e=>e.alive&&e.gx===gx&&e.gy===gy);
  if (occupied) { setBanner('Posição ocupada!'); render(); return; }
  const dist = Math.abs(hero.gx-gx)+Math.abs(hero.gy-gy);
  if (dist>2) { setBanner('Máximo: 2 quadros!'); render(); return; }
  const apCost = hero.moved ? 1 : 0;
  hero.gx=gx; hero.gy=gy; hero.moved=true; hero.ap=Math.max(0,hero.ap-apCost);
  addLog(`${hero.name} moveu-se para (${gx},${gy}).`, 'system');
  render();
}

function useItem(type) {
  const hero = S.party[S.activeHero];
  if (!hero||!hero.alive||hero.ap<=0||S.enemyTurn) return;
  if (type==='potion') {
    if (S.resources.potions<=0) { setBanner('Sem poções!'); render(); return; }
    S.resources.potions--; hero.hp=Math.min(hero.maxHp,hero.hp+60); hero.ap--;
    addLog(`${hero.name} usou Poção (+60 HP)`, 'heal');
  } else if (type==='fire') {
    const e = S.enemies[S.selEnemy]; if (!e||!e.alive) return;
    e.hp -= 26; e.alive = e.hp>0; e.statuses.push({type:'BURNING',dur:2,mag:8}); hero.ap--;
    addLog(`${hero.name} jogou Fogo Alquímico! -26 dano flamejante`, 'crit');
    if (S.enemies.every(en=>!en.alive)) { endCombat(true); return; }
  } else if (type==='smoke') {
    hero.statuses.push({type:'STEALTHED',dur:2}); hero.ap--;
    addLog(`${hero.name} detonou Bomba de Fumaça! Furtivo!`, 'status');
  }
  checkEndTurn(); render();
}

function checkEndTurn() {
  const hero = S.party[S.activeHero];
  if (hero && hero.ap<=0) {
    setBanner(`${hero.name} sem AP. Passando turno...`);
    setTimeout(advanceTurn, 700);
  }
}

function endPlayerTurn() { if (!S.enemyTurn) advanceTurn(); }

function advanceTurn() {
  if (S.initiative.length===0) return;
  // Update liveness
  S.initiative.forEach(e => {
    if (e.hero) e.alive = S.party.some(h=>h.id===e.id&&h.alive);
    else e.alive = S.enemies.some(en=>en.id===e.id&&en.alive);
  });

  const aliveH = S.party.filter(h=>h.alive);
  const aliveE = S.enemies.filter(e=>e.alive);
  if (aliveH.length===0) { S.screen='DEFEAT'; S.enemyTurn=false; render(); return; }
  if (aliveE.length===0) { endCombat(true); return; }

  let next = (S.turnIdx+1) % S.initiative.length;
  let newRound = S.round;
  if (next===0) newRound++;
  let attempts=0;
  while (!S.initiative[next].alive && attempts<S.initiative.length) {
    next = (next+1) % S.initiative.length;
    if (next===0) newRound++;
    attempts++;
  }
  const entity = S.initiative[next];
  const wrapped = newRound > S.round;
  S.turnIdx = next; S.round = newRound;

  if (wrapped) {
    S.party.forEach(h=>{h.ap=h.maxAp;h.guard=false;h.moved=false;});
    S.enemies.forEach(e=>{ if(e.alive) e.intent=genEnemyIntent(e); e.guard=false; });
    addLog(`--- Rodada ${newRound} ---`, 'system');
  }

  if (entity.hero) {
    const idx = S.party.findIndex(h=>h.id===entity.id);
    S.activeHero = Math.max(0, idx);
    S.selSkill = S.party[S.activeHero]?.skills[0] || null;
    S.enemyTurn = false;
    S.diceRoll = null;
    setBanner(`Sua vez! Turno de ${entity.name}.`);
    addLog(`Turno de ${entity.name}`, 'system');
  } else {
    S.enemyTurn = true;
    S.diceRoll = null;
    setBanner(`Turno de ${entity.name}...`);
    addLog(`Turno de ${entity.name}`, 'system');
    setTimeout(()=>enemyTurn(entity), 800);
  }
  render();
}

function enemyTurn(entity) {
  const enemy = S.enemies.find(e=>e.id===entity.id);
  if (!enemy || !enemy.alive) { advanceTurn(); return; }

  // Stun check
  if (enemy.statuses.some(s=>s.type==='FROZEN'||s.type==='STUNNED')) {
    addLog(`${enemy.name} está congelado! Turno perdido.`, 'status');
    advanceTurn(); return;
  }

  // DoT
  enemy.statuses = enemy.statuses.filter(s => {
    if (s.type==='BURNING') { enemy.hp -= s.mag; addLog(`${enemy.name} sofre ${s.mag} de queimadura!`, 'status'); }
    if (s.type==='POISONED') { enemy.hp -= s.mag; addLog(`${enemy.name} sofre ${s.mag} de veneno!`, 'status'); }
    s.dur--; return s.dur>0;
  });
  if (enemy.hp<=0) { enemy.alive=false; addLog(`${enemy.name} morreu por efeitos!`, 'loot');
    if (S.enemies.every(e=>!e.alive)) { endCombat(true); return; }
    advanceTurn(); return; }

  // Attack
  const targets = S.party.filter(h=>h.alive);
  if (targets.length===0) { S.screen='DEFEAT'; render(); return; }
  const target = targets[Math.floor(Math.random()*targets.length)];

  const d20 = rollD20();
  if (d20===1) { addLog(`${enemy.name} errou ${target.name}!`, 'attack'); advanceTurn(); return; }
  const isCrit = d20>=20;
  let raw = enemy.atk*0.9 + d20*0.4;
  if (isCrit) raw *= 1.7;
  let defRed = heroTotal(target,'def') * 0.5 + (target.guard ? 12 : 0);
  const dmg = Math.max(3, Math.floor(raw - defRed));
  target.hp -= dmg;
  if (target.hp<=0) { target.hp=0; target.alive=false; addLog(`${target.name} caiu em combate!`, 'attack'); }

  addLog(`${enemy.name} → ${target.name}! D20:${d20} → ${dmg} dano${isCrit?' ★ CRÍTICO!':''}`, isCrit?'crit':'attack');
  S.diceRoll = {d20, total:d20, isCrit, isMiss:false, name:'Ataque Inimigo', actor:enemy.name, target:target.name, dmg};

  if (S.party.every(h=>!h.alive)) { S.screen='DEFEAT'; S.enemyTurn=false; render(); return; }
  setTimeout(advanceTurn, 900);
  render();
}

function endCombat(victory) {
  if (!victory) { S.screen='DEFEAT'; render(); return; }
  const xp = S.enemies.reduce((s,e)=>s+e.xp,0);
  const gold = S.enemies.reduce((s,e)=>s+e.gold,0);
  const drop = Math.random()>0.4 ? LOOT[Math.floor(Math.random()*LOOT.length)] : null;

  S.party.forEach(h => {
    if (!h.alive) return;
    h.xp += xp;
    while (h.xp >= h.maxXp && h.level<20) {
      h.xp -= h.maxXp; h.level++;
      h.maxXp = Math.floor(h.maxXp*1.5);
      h.maxHp += 15; h.hp += 15;
      h.atk += 3; h.def += 2; h.mag += 3;
      addLog(`${h.name} subiu para nível ${h.level}!`, 'loot');
    }
  });
  S.resources.gold += gold;
  if (drop) S.inventory.push({...drop, id: crypto.randomUUID()});
  S.gainXp = xp; S.gainGold = gold; S.gainLoot = drop ? [drop] : [];
  S.screen = 'VICTORY';
  S.enemyTurn = false;
  addLog(`★ Vitória! +${xp} XP, +${gold} Ouro`, 'loot');
  render();
}

function continueAfterVictory() {
  S.screen = 'EXPLORATION';
  S.diceRoll = null;
  render();
}

function retry() {
  S.party = createParty();
  S.screen = 'HOME';
  render();
}

// =================== HELPERS ===================

function addLog(text, type='system') { S.logs.push({text, type, id: crypto.randomUUID()}); if (S.logs.length>50) S.logs.shift(); }
function setBanner(msg) { S.banner = msg; }
function dismissBanner() { S.banner = null; render(); }
function navigate(screen) { S.screen = screen; render(); }

function selectScenario(theme) {
  S.theme = theme;
  S.map = genMap(theme);
  S.px = 2; S.py = 2; S.turn = 1; S.steps = 0; S.torchRadius = 3.5;
  S.patrols = (ENEMIES[theme]?.normal||[]).slice(0,2).map((e,i)=>({...e,id:crypto.randomUUID(),gx:9+i*4,gy:3+i*5,alive:true}));
  S.screen = 'EXPLORATION';
  S.logs = [];
  addLog(`O grupo adentrou ${THEMES[theme].title}. Exploração iniciada!`, 'system');
  updateVision();
  render();
}

function selHero(i) { S.activeHero = i; S.selSkill = S.party[i]?.skills[0] || null; render(); }
function selEnemy(i) { S.selEnemy = i; render(); }
function selSkill(s) { S.selSkill = s; render(); }
function dismissDice() { S.diceRoll = null; render(); }

// =================== RENDERING ===================

function render() {
  const app = document.getElementById('app');
  switch (S.screen) {
    case 'HOME': app.innerHTML = renderHome(); break;
    case 'SCENARIO': app.innerHTML = renderScenario(); break;
    case 'EXPLORATION': app.innerHTML = renderExploration(); break;
    case 'COMBAT': app.innerHTML = renderCombat(); break;
    case 'VICTORY': app.innerHTML = renderVictory(); break;
    case 'DEFEAT': app.innerHTML = renderDefeat(); break;
    case 'PARTY': app.innerHTML = renderParty(); break;
  }
  attachEvents();
}

function resBar() {
  const r = S.resources;
  return `<div class="res-bar">
    <span class="res-item">💰 ${r.gold}</span>
    <span class="res-item" style="color:var(--orange)">🔥 ${r.torches}</span>
    <span class="res-item" style="color:var(--green)">🍖 ${r.rations}</span>
    <span class="res-item" style="color:var(--crimson)">🧪 ${r.potions}</span>
    <div class="spacer"></div>
    <button class="btn btn-sm btn-outline" data-act="party">Grupo & Itens</button>
  </div>`;
}

function heroMiniCard(h) {
  const clsColor = h.class.color;
  const hpRatio = (h.hp/h.maxHp)*100;
  const hpColor = hpRatio>50?'var(--green)':hpRatio>20?'var(--orange)':'var(--crimson)';
  return `<div class="hero-mini ${h.alive?'':'dead'}" style="border-color:${h.alive?'var(--border)':'var(--crimson)'}">
    <div class="hero-name" style="color:${h.alive?'#fff':'#666'}">${h.name.split(' ')[0]}</div>
    <div class="hero-class" style="color:${clsColor}">${h.class.name}</div>
    <div class="hero-hp">HP ${h.hp}/${h.maxHp}</div>
    <div class="bar"><div class="bar-fill" style="width:${hpRatio}%;background:${hpColor}"></div></div>
  </div>`;
}

function renderHome() {
  const alive = S.party.filter(h=>h.alive).length;
  return `
    <h1 class="game-title">⚔ Crypts & Dungeons</h1>
    <p class="game-sub">RPG Tático de Mesa • Exploração de Masmorras • Combate por Turnos em Grid</p>
    ${resBar()}
    <div class="flex-col" style="margin-top:12px">
      <button class="btn btn-gold" data-act="scenario" style="height:56px;font-size:16px">🏰 CAMPANHA & CENÁRIOS — Explore as 6 Grandes Criptas</button>
      <button class="btn btn-outline" data-act="explore" style="height:48px"> Entrar na Masmorra Atual (${THEMES[S.theme].title})</button>
      <div class="grid-2">
        <button class="btn btn-outline" data-act="party">🛡 Grupo & Itens</button>
        <button class="btn btn-outline" data-act="quick-combat">⚔ Combate Rápido (Teste)</button>
      </div>
      <div class="card" style="margin-top:8px">
        <div class="flex" style="justify-content:space-between;align-items:center">
          <strong style="color:var(--gold-light)">Grupo de Aventureiros</strong>
          <span style="color:var(--green);font-size:13px">${alive}/4 Vivos</span>
        </div>
        <div class="flex" style="margin-top:8px">${S.party.map(heroMiniCard).join('')}</div>
      </div>
    </div>`;
}

function renderScenario() {
  const cards = Object.entries(THEMES).map(([key,t],i) => `
    <div class="scenario-card" data-act="select-scenario" data-theme="${key}">
      <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
        <div style="width:12px;height:12px;border-radius:3px;background:${t.color}"></div>
        <strong style="color:var(--gold-light);font-size:16px">${t.title}</strong>
      </div>
      <div style="font-size:12px;color:var(--text-dim);margin-bottom:4px">${t.sub}</div>
      <div style="font-size:13px;margin-bottom:6px">${t.desc}</div>
      <div style="font-size:11px;color:var(--crimson)">⚠ ${t.danger}</div>
    </div>`).join('');
  return `
    <h1 class="game-title">🏰 Seleção de Cenário</h1>
    <button class="btn btn-sm btn-outline" data-act="home" style="margin-bottom:12px">← Voltar</button>
    <div class="grid-2">${cards}</div>`;
}

function renderExploration() {
  const tiles = S.map.tiles;
  const cols = S.map.W;
  let grid = `<div class="dungeon-grid" style="grid-template-columns:repeat(${cols},32px)">`;
  for (let y=0;y<S.map.H;y++) {
    for (let x=0;x<S.map.W;x++) {
      const t = tiles.find(tt=>tt.x===x&&tt.y===y);
      const isPlayer = x===S.px && y===S.py;
      const info = TILE_INFO[t.type] || {cls:'floor'};
      let cls = info.cls;
      if (!t.explored) cls = 'unexplored';
      else if (!t.visible) cls += ' not-visible';
      if (isPlayer) cls += ' player';
      let icon = '';
      if (t.explored) {
        if (t.type==='C') icon='💰';
        else if (t.type==='S') icon='⚰️';
        else if (t.type==='A') icon='⚡';
        else if (t.type==='F') icon='💧';
        else if (t.type==='T') icon='🔥';
        else if (t.type==='>') icon='⬇️';
        else if (t.type==='+') icon='🚪';
        else if (t.type==='?') icon='❓';
        else if (t.type==='^') icon='⚠️';
      }
      grid += `<div class="tile ${cls}">${icon}</div>`;
    }
  }
  grid += '</div>';

  const patrol = S.patrols.map(p=> {
    const t = tiles.find(tt=>tt.x===p.gx&&tt.y===p.gy);
    if (t && t.visible) return `<div class="tile" style="position:absolute;pointer-events:none"></div>`;
    return '';
  }).join('');

  return `
    <h1 class="game-title">🗺️ ${THEMES[S.theme].title}</h1>
    <p class="game-sub">${THEMES[S.theme].sub} — Turno #${S.turn}</p>
    ${resBar()}
    ${S.banner?`<div class="banner">${S.banner} <button class="btn btn-sm btn-outline" data-act="dismiss-banner" style="float:right">✕</button></div>`:''}
    <div style="position:relative">${grid}
      ${S.patrols.filter(p=>{const t=tiles.find(tt=>tt.x===p.gx&&tt.y===p.gy);return t&&t.visible;}).map(p=>
        `<div style="position:absolute;width:32px;height:32px;display:flex;align-items:center;justify-content:center;font-size:14px;pointer-events:none;color:var(--crimson)">👹</div>`
      ).join('')}
    </div>
    <div class="grid-2" style="max-width:400px;margin:12px auto">
      <div class="move-pad">
        <div class="move-btn empty"></div>
        <div class="move-btn" data-act="move" data-dx="0" data-dy="-1">↑</div>
        <div class="move-btn empty"></div>
        <div class="move-btn" data-act="move" data-dx="-1" data-dy="0">←</div>
        <div class="move-btn empty"></div>
        <div class="move-btn" data-act="move" data-dx="1" data-dy="0">→</div>
        <div class="move-btn empty"></div>
        <div class="move-btn" data-act="move" data-dx="0" data-dy="1">↓</div>
        <div class="move-btn empty"></div>
      </div>
      <div class="flex-col" style="justify-content:center">
        <button class="btn btn-cyan btn-sm" data-act="interact">✋ Interagir</button>
        <button class="btn btn-outline btn-sm" data-act="wait">⏳ Aguardar Turno</button>
        <button class="btn btn-sm" style="background:var(--orange);color:#000" data-act="torch">🔥 Usar Tocha</button>
        <button class="btn btn-outline btn-sm" data-act="rest">🍖 Descansar</button>
        <button class="btn btn-sm btn-outline" data-act="home">← Voltar</button>
      </div>
    </div>
    <div class="flex">${S.party.map(heroMiniCard).join('')}</div>
    <div class="combat-log" style="margin-top:8px">${S.logs.slice(-10).map(l=>`<div class="log-entry log-${l.type}">${l.text}</div>`).join('')}</div>`;
}

function renderCombat() {
  const hero = S.party[S.activeHero];
  // Initiative tracker
  const initChips = S.initiative.map((e,i) => {
    const cls = [i===S.turnIdx?'current':'', e.hero?'hero':'enemy', e.alive?'':'dead'].filter(Boolean).join(' ');
    return `<div class="init-chip ${cls}">${i===S.turnIdx?'▶ ':''}${e.name.split(' ')[0]} (${e.speed})</div>`;
  }).join('');

  // Combat grid
  let grid = '<div class="combat-grid">';
  for (let y=0;y<4;y++) {
    for (let x=0;x<6;x++) {
      const tile = S.arena.find(t=>t.x===x&&t.y===y);
      let cls = x<=1?'hero-zone':x>=4?'enemy-zone':'neutral';
      if (tile?.cover) cls += ' cover';
      if (tile?.trap) cls += ' trap';
      const heroT = S.party.find(h=>h.alive&&h.gx===x&&h.gy===y);
      const enemyT = S.enemies.find(e=>e.alive&&e.gx===x&&e.gy===y);
      let token = '';
      if (heroT) {
        const isActive = S.activeHero===S.party.indexOf(heroT) && !S.enemyTurn;
        token = `<div class="combat-token hero ${isActive?'active':''}">${heroT.name[0]}</div>`;
      } else if (enemyT) {
        const isTarget = S.enemies.indexOf(enemyT)===S.selEnemy;
        token = `<div class="combat-token enemy ${enemyT.isBoss?'boss':''} ${isTarget?'targeted':''}" data-act="sel-enemy" data-idx="${S.enemies.indexOf(enemyT)}">${enemyT.isBoss?'☠':'👹'}</div>`;
      } else if (hero && !S.enemyTurn) {
        const dist = Math.abs(hero.gx-x)+Math.abs(hero.gy-y);
        if (dist>0 && dist<=2 && hero.ap>0) cls += ' reachable';
      }
      grid += `<div class="combat-tile ${cls}" data-act="move-hero" data-gx="${x}" data-gy="${y}">${token}</div>`;
    }
  }
  grid += '</div>';

  // Enemy cards
  const enemyCards = S.enemies.map((e,i) => {
    if (!e.alive) return '';
    const hpR = (e.hp/ Math.max(e.hp,e.hp)) *100;
    const hpPct = Math.max(0, (e.hp / (e.maxHp||e.hp)) * 100);
    const intent = e.intent;
    return `<div class="card ${i===S.selEnemy?'card-elev':''}" style="border-color:${i===S.selEnemy?'var(--crimson)':'var(--border)'}" data-act="sel-enemy" data-idx="${i}">
      <div class="flex" style="align-items:center">
        <div style="width:36px;height:36px;border-radius:50%;background:${e.isBoss?'var(--crimson)':'#4a1e24'};display:flex;align-items:center;justify-content:center;font-size:18px">${e.isBoss?'☠':'👹'}</div>
        <div class="spacer">
          <strong style="font-size:13px">${e.name}</strong>
          <div style="font-size:11px;color:var(--text-dim)">${e.title}</div>
          <div class="bar" style="margin-top:2px"><div class="bar-fill" style="width:${hpPct}%;background:${e.isBoss?'var(--gold)':'var(--crimson)'}"></div></div>
          <div style="font-size:10px">${e.hp} HP • Nv.${e.level||1} • (${e.gx},${e.gy})</div>
        </div>
      </div>
      ${intent?`<div class="card card-elev" style="margin-top:6px;padding:4px 8px;font-size:11px">🎯 ${intent.desc}${intent.dmg?` (~${intent.dmg} dano)`:''}</div>`:''}
    </div>`;
  }).join('');

  // Hero cards
  const heroCards = S.party.map((h,i) => {
    if (!h.alive) return '';
    const hpR = (h.hp/h.maxHp)*100;
    const mpR = (h.mp/h.maxMp)*100;
    const ap = '●'.repeat(h.ap) + '○'.repeat(h.maxAp-h.ap);
    return `<div class="card ${i===S.activeHero?'card-elev':''}" style="cursor:pointer;flex:1;min-width:130px;border-color:${i===S.activeHero?'var(--gold)':'var(--border)'}" data-act="sel-hero" data-idx="${i}">
      <div class="flex" style="justify-content:space-between">
        <strong style="font-size:12px">${h.name.split(' ')[0]}</strong>
        <span style="font-size:10px;color:var(--gold-light)">Nv.${h.level}</span>
      </div>
      <div style="font-size:10px;color:${h.class.color}">${h.class.name}</div>
      <div class="bar"><div class="bar-fill" style="width:${hpR}%;background:${hpR>50?'var(--green)':hpR>20?'var(--orange)':'var(--crimson)'}"></div></div>
      <div style="font-size:10px">${h.hp}/${h.maxHp} HP</div>
      <div class="bar"><div class="bar-fill" style="width:${mpR}%;background:var(--cyan)"></div></div>
      <div style="font-size:10px">${h.mp}/${h.maxMp} MP • AP: ${ap}</div>
      ${h.guard?'<span class="status-badge" style="background:rgba(6,182,212,.2);color:var(--cyan)">🛡 GUARD</span>':''}
    </div>`;
  }).join('');

  // Skills
  const skills = hero ? hero.skills.map(s => {
    const enough = hero.mp >= s.mp && hero.ap >= (s.ap||1);
    return `<div class="skill-btn ${S.selSkill?.id===s.id?'selected':''} ${!enough?'disabled':''}" data-act="sel-skill" data-skill="${s.id}">
      <div style="font-weight:700;font-size:12px">${s.name}</div>
      <div style="font-size:10px;color:var(--text-dim)">P:${s.power} ${s.mp?`• ${s.mp}MP`:''} • ${s.ap||1}AP</div>
    </div>`;
  }).join('') : '';

  // Dice roll modal
  const dice = S.diceRoll ? `<div class="card card-elev" style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:100;max-width:400px;text-align:center;border:2px solid ${S.diceRoll.isCrit?'var(--gold)':S.diceRoll.isMiss?'var(--crimson)':'var(--cyan)'}">
    <h2 style="color:${S.diceRoll.isCrit?'var(--gold-light)':S.diceRoll.isMiss?'var(--crimson)':'#fff'};font-size:18px">${S.diceRoll.name}</h2>
    <div style="font-size:13px;color:var(--text-dim);margin-bottom:8px">${S.diceRoll.actor} → ${S.diceRoll.target}</div>
    <div style="font-size:48px;font-weight:900;color:${S.diceRoll.isCrit?'var(--gold-light)':S.diceRoll.isMiss?'var(--crimson)':'#fff'}">${S.diceRoll.d20}</div>
    <div style="font-size:12px;color:var(--text-dim)">d20</div>
    ${S.diceRoll.isCrit?'<div class="status-badge" style="background:rgba(212,175,55,.2);color:var(--gold);margin:8px 0">★ CRÍTICO!</div>':''}
    ${S.diceRoll.isMiss?'<div class="status-badge" style="background:rgba(220,38,38,.2);color:var(--crimson);margin:8px 0">☠ ERROU!</div>':''}
    ${S.diceRoll.dmg?`<div style="font-size:20px;font-weight:700;color:var(--crimson);margin:8px 0">-${S.diceRoll.dmg} Dano</div>`:''}
    ${S.diceRoll.combo?`<div style="font-size:12px;color:var(--cyan)">${S.diceRoll.combo}</div>`:''}
    <button class="btn btn-gold btn-sm" data-act="dismiss-dice" style="margin-top:12px">Continuar</button>
  </div>` : '';

  return `
    <div style="display:flex;justify-content:space-between;align-items:center">
      <div><h1 class="game-title" style="font-size:20px">⚔ Combate Tático</h1>
      <p style="font-size:12px;color:${S.enemyTurn?'var(--crimson)':'var(--green)'}">${S.enemyTurn?'Turno dos Inimigos...':`Turno do Jogador: ${hero?.name||''}`}</p></div>
      <div class="flex"><span class="status-badge" style="background:rgba(212,175,55,.2);color:var(--gold)">R${S.round}</span>
      <button class="btn btn-sm btn-outline" data-act="retreat">Recuar</button></div>
    </div>
    <div class="initiative">${initChips}</div>
    ${grid}
    ${S.banner?`<div class="banner">${S.banner}</div>`:''}
    <div class="grid-2" style="align-items:start">
      <div><strong style="color:var(--crimson);font-size:12px">Inimigos:</strong>${enemyCards}</div>
      <div><strong style="color:var(--gold-light);font-size:12px">Heróis:</strong><div class="flex" style="overflow-x:auto">${heroCards}</div></div>
    </div>
    ${hero && !S.enemyTurn ? `<div class="card" style="margin-top:8px">
      <div class="flex" style="justify-content:space-between;align-items:center">
        <strong style="color:var(--gold-light)">Ações de ${hero.name}:</strong>
        <span style="color:var(--cyan);font-size:12px">AP: ${hero.ap}/${hero.maxAp} • MP: ${hero.mp}/${hero.maxMp}</span>
      </div>
      <div class="flex" style="overflow-x:auto;margin-top:6px">${skills}</div>
      ${S.selSkill?`<div class="card card-elev" style="margin-top:6px"><div style="font-size:11px">${S.selSkill.desc}</div>
        <div style="font-size:10px;color:var(--gold-dark)">Tipo: ${S.selSkill.dmg} • Alcance: ${S.selSkill.range} • Alvo: ${S.selSkill.type}</div></div>`:''}
      <div class="flex" style="margin-top:8px;flex-wrap:wrap">
        <button class="btn btn-red btn-sm" data-act="attack" ${hero.ap<=0?'disabled':''}>⚔ Executar</button>
        <button class="btn btn-sm" style="background:#233b53" data-act="guard" ${hero.ap<=0||hero.guard?'disabled':''}>🛡 Guarda</button>
        <button class="btn btn-sm" style="background:#4a3820" data-act="shove" ${hero.ap<=0?'disabled':''}>🤜 Empurrar</button>
        <button class="btn btn-sm btn-outline" style="border-color:var(--green);color:var(--green)" data-act="item-potion" ${S.resources.potions<=0||hero.ap<=0?'disabled':''}>🧪 Cura (${S.resources.potions})</button>
        <button class="btn btn-sm btn-outline" style="border-color:var(--orange);color:var(--orange)" data-act="item-fire" ${hero.ap<=0?'disabled':''}>🔥 Fogo</button>
        <button class="btn btn-sm btn-outline" data-act="item-smoke" ${hero.ap<=0?'disabled':''}>💨 Fumaça</button>
        <div class="spacer"></div>
        <button class="btn btn-sm btn-outline" data-act="end-turn">⏭ Passar Turno</button>
      </div>
    </div>` : ''}
    <strong style="font-size:12px;color:var(--gold-light)">Registro de Combate:</strong>
    <div class="combat-log">${S.logs.slice(-15).map(l=>`<div class="log-entry log-${l.type}">${l.text}</div>`).join('')}</div>
    ${dice}`;
}

function renderVictory() {
  return `<div class="victory-box">
    <div style="font-size:48px">🏆</div>
    <h2 style="color:var(--gold-light);font-size:24px;margin:8px 0">VITÓRIA TÁTICA!</h2>
    <p style="color:var(--text-dim);margin-bottom:16px">Os horrores foram subjugados.</p>
    <div class="flex" style="gap:10px;margin-bottom:12px">
      <div class="card card-elev" style="flex:1"><div style="font-size:11px;color:var(--cyan)">Experiência</div><div style="font-size:20px;font-weight:700">+${S.gainXp} XP</div></div>
      <div class="card card-elev" style="flex:1"><div style="font-size:11px;color:var(--gold)">Tesouro</div><div style="font-size:20px;font-weight:700;color:var(--gold-light)">+${S.gainGold} Ouro</div></div>
    </div>
    ${S.gainLoot.length?`<div style="margin-bottom:12px"><strong style="font-size:12px;color:var(--gold-light)">Recompensas:</strong>${S.gainLoot.map(l=>`<div class="card card-elev" style="margin-top:4px;padding:6px 10px">🎁 ${l.name} <span style="font-size:11px;color:var(--text-dim)">(${l.rarity})</span></div>`).join('')}</div>`:''}
    <button class="btn btn-gold" data-act="continue" style="width:100%;height:46px">Continuar Exploração</button>
  </div>`;
}

function renderDefeat() {
  return `<div class="defeat-box">
    <div style="font-size:48px">💀</div>
    <h2 style="color:var(--crimson);font-size:22px;margin:8px 0">O GRUPO FOI DERROTADO</h2>
    <p style="color:var(--text-dim);margin-bottom:18px">A escuridão das catacumbas consumiu seus passos.</p>
    <button class="btn btn-red" data-act="retry" style="width:100%;height:46px">Reagrupar no Acampamento</button>
  </div>`;
}

function renderParty() {
  const heroDetail = (h,i) => {
    if (!h.alive) return `<div class="card" style="opacity:.4"><strong>${h.name}</strong> — Caído</div>`;
    return `<div class="card" style="border-left:3px solid ${h.class.color}">
      <div class="flex" style="justify-content:space-between">
        <div><strong style="font-size:15px">${h.name}</strong> <span style="font-size:12px;color:${h.class.color}">${h.class.name}</span></div>
        <span style="color:var(--gold-light);font-weight:700">Nv.${h.level}</span>
      </div>
      <div style="font-size:12px;color:var(--text-dim);margin:4px 0">${h.class.title} — ${h.class.role}</div>
      <div style="font-size:11px">${h.class.desc}</div>
      <div class="flex" style="margin-top:8px;font-size:12px;gap:12px">
        <span>❤️ ${h.hp}/${h.maxHp}</span>
        <span>💧 ${h.mp}/${h.maxMp}</span>
        <span>⚔ ${heroTotal(h,'atk')}</span>
        <span>🛡 ${heroTotal(h,'def')}</span>
        <span>✨ ${heroTotal(h,'mag')}</span>
        <span>💨 ${heroTotal(h,'spd')}</span>
      </div>
      <div style="margin-top:6px"><strong style="font-size:12px">Habilidades:</strong>
        ${h.skills.map(s=>`<div class="card card-elev" style="margin-top:4px;padding:6px"><strong style="font-size:12px">${s.name}</strong> <span style="font-size:10px;color:var(--text-dim)">P:${s.power} ${s.mp?`MP:${s.mp} `:''}AP:${s.ap||1}</span><div style="font-size:11px">${s.desc}</div></div>`).join('')}
      </div>
      <div style="margin-top:6px"><strong style="font-size:12px">Equipamento:</strong>
        <div style="font-size:11px">🗡 ${h.wpn?.name||'—'}</div>
        <div style="font-size:11px">🛡 ${h.arm?.name||'—'}</div>
        <div style="font-size:11px">💍 ${h.acc?.name||'—'}</div>
      </div>
    </div>`;
  };
  return `
    <h1 class="game-title">🛡 Grupo & Itens</h1>
    <button class="btn btn-sm btn-outline" data-act="home" style="margin-bottom:12px">← Voltar</button>
    <div class="flex-col">${S.party.map(heroDetail).join('')}</div>
    <div class="card" style="margin-top:12px"><strong style="color:var(--gold-light)">Inventário (${S.inventory.length}):</strong>
      ${S.inventory.map(it=>`<div class="card card-elev" style="margin-top:4px;padding:6px 10px">📦 ${it.name} <span style="font-size:11px;color:var(--text-dim)">${it.type} • ${it.rarity} • ${it.val} moedas</span></div>`).join('')}
    </div>`;
}

// =================== EVENT HANDLING ===================

function attachEvents() {
  document.querySelectorAll('[data-act]').forEach(el => {
    el.addEventListener('click', (e) => {
      e.preventDefault();
      const act = el.dataset.act;
      switch(act) {
        case 'home': navigate('HOME'); break;
        case 'scenario': navigate('SCENARIO'); break;
        case 'explore': navigate('EXPLORATION'); break;
        case 'party': navigate('PARTY'); break;
        case 'quick-combat': startCombat(false); break;
        case 'select-scenario': selectScenario(el.dataset.theme); break;
        case 'move': movePlayer(+el.dataset.dx, +el.dataset.dy); break;
        case 'interact': interact(); break;
        case 'wait': advanceTurn(); setBanner(`Turno #${S.turn}: Grupo aguardou.`); render(); break;
        case 'torch': useTorch(); break;
        case 'rest': rest(); break;
        case 'dismiss-banner': dismissBanner(); break;
        case 'sel-hero': selHero(+el.dataset.idx); break;
        case 'sel-enemy': selEnemy(+el.dataset.idx); break;
        case 'sel-skill': { const h=S.party[S.activeHero]; const sk=h?.skills.find(s=>s.id===el.dataset.skill); if(sk) selSkill(sk); break; }
        case 'attack': heroAttack(); break;
        case 'guard': heroGuard(); break;
        case 'shove': { const e=S.enemies[S.selEnemy]; const h=S.party[S.activeHero]; if(h&&e&&h.ap>0){h.ap--;e.gx=Math.min(5,e.gx+1);e.hp-=8;addLog(`${h.name} empurrou ${e.name}!`,'attack');if(S.enemies.every(en=>!en.alive)){endCombat(true);return;}checkEndTurn();render();} break; }
        case 'item-potion': useItem('potion'); break;
        case 'item-fire': useItem('fire'); break;
        case 'item-smoke': useItem('smoke'); break;
        case 'end-turn': endPlayerTurn(); break;
        case 'move-hero': heroMove(+el.dataset.gx, +el.dataset.gy); break;
        case 'dismiss-dice': dismissDice(); break;
        case 'continue': continueAfterVictory(); break;
        case 'retry': retry(); break;
        case 'retreat': navigate('EXPLORATION'); break;
      }
    });
  });
}

// =================== INIT ===================

newGame();
render();
