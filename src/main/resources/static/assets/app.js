"use strict";

const TOKEN_KEY = "cheemsquest.token";
const CHARACTER_KEY = "cheemsquest.character";
const SLOT_ORDER = ["WEAPON", "HEAD", "NECK", "BODY"];
const KEY_DIRECTIONS = {
    ArrowUp: "NORTH", w: "NORTH",
    ArrowDown: "SOUTH", s: "SOUTH",
    ArrowLeft: "WEST", a: "WEST",
    ArrowRight: "EAST", d: "EAST"
};
const KEY_SKILLS = {1: "BONK", 2: "BITE", 3: "BONKING_SPIN"};
const OBJECTIVE_VERBS = {DEFEAT_ENEMY: "Defeat", VISIT_LOCATION: "Visit", COLLECT_ITEM: "Find"};
const MAP_CELL = {width: 96, height: 50, gapX: 26, gapY: 22, padding: 4, stub: 12};
const SVG_NS = "http://www.w3.org/2000/svg";

const state = {
    token: null,
    username: null,
    authMode: "login",
    character: null,
    map: null,
    encounter: null,
    target: null,
    shop: null,
    inventory: null,
    busy: false
};

const $ = (selector) => document.querySelector(selector);

function el(tag, props = {}, ...children) {
    const node = document.createElement(tag);
    Object.assign(node, props);
    node.append(...children.filter((child) => child !== null && child !== undefined));
    return node;
}

const storage = {
    get(key) {
        try {
            return localStorage.getItem(key);
        } catch {
            return null;
        }
    },
    set(key, value) {
        try {
            localStorage.setItem(key, value);
        } catch {
        }
    },
    remove(key) {
        try {
            localStorage.removeItem(key);
        } catch {
        }
    }
};

class ApiError extends Error {
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}

async function api(method, path, body) {
    const headers = {};
    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }
    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }
    const response = await fetch(`/api${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    if (response.status === 401 && state.token) {
        logout("Session expired. Much sad. Please log in again.");
        throw new ApiError(401, "Session expired");
    }
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
        const fieldErrors = data && data.errors
            ? Object.entries(data.errors).map(([field, message]) => `${field} ${message}`).join(", ")
            : "";
        throw new ApiError(response.status, fieldErrors || (data && data.detail) || `Request failed (${response.status})`);
    }
    return data;
}

function decodeToken(token) {
    try {
        const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        return JSON.parse(atob(payload));
    } catch {
        return null;
    }
}

function show(screen) {
    for (const id of ["auth", "characters", "game"]) {
        $(`#${id}`).hidden = id !== screen;
    }
    $("#session").hidden = screen === "auth";
    $("#session-user").textContent = state.username ? `@${state.username}` : "";
}

async function guarded(action, onError = (error) => log(error.message, "error")) {
    if (state.busy) {
        return;
    }
    state.busy = true;
    try {
        await action();
    } catch (error) {
        if (error.status !== 401 || !state.token) {
            onError(error);
        }
    } finally {
        state.busy = false;
    }
}

function setAuthMode(mode) {
    state.authMode = mode;
    for (const tab of document.querySelectorAll("#auth .tab")) {
        tab.classList.toggle("active", tab.dataset.mode === mode);
    }
    $("#auth-submit").textContent = mode === "login" ? "Log in" : "Register";
    $("#auth-form").password.autocomplete = mode === "login" ? "current-password" : "new-password";
    $("#auth-form").password.minLength = mode === "login" ? 1 : 8;
    $("#auth-error").textContent = "";
}

function startSession(token) {
    const claims = decodeToken(token);
    if (!claims || claims.exp * 1000 <= Date.now()) {
        return false;
    }
    state.token = token;
    state.username = claims.username;
    storage.set(TOKEN_KEY, token);
    return true;
}

function logout(message = "") {
    state.token = null;
    state.username = null;
    state.character = null;
    state.encounter = null;
    storage.remove(TOKEN_KEY);
    storage.remove(CHARACTER_KEY);
    setAuthMode("login");
    $("#auth-error").textContent = message;
    show("auth");
}

async function submitAuth(event) {
    event.preventDefault();
    const form = event.target;
    await guarded(async () => {
        const path = state.authMode === "login" ? "/auth/login" : "/auth/register";
        const result = await api("POST", path, {username: form.username.value.trim(), password: form.password.value});
        form.password.value = "";
        startSession(result.accessToken);
        await openCharacters();
    }, (error) => {
        $("#auth-error").textContent = error.message;
    });
}

async function openCharacters() {
    storage.remove(CHARACTER_KEY);
    state.character = null;
    state.encounter = null;
    $("#characters-error").textContent = "";
    show("characters");
    const characters = await api("GET", "/characters");
    $("#no-characters").hidden = characters.length > 0;
    $("#character-list").replaceChildren(...characters.map((character) => el("li", {},
        el("button", {type: "button", onclick: () => guarded(() => enterGame(character.id), showCharactersError)},
            el("span", {}, el("strong", {textContent: character.name}), ` · level ${character.level}`),
            el("span", {className: "muted", textContent: character.location.name})))));
}

function showCharactersError(error) {
    $("#characters-error").textContent = error.message;
}

async function createCharacter(event) {
    event.preventDefault();
    const name = event.target.name.value.trim();
    await guarded(async () => {
        const character = await api("POST", "/characters", {name});
        await enterGame(character.id);
    }, showCharactersError);
}

async function enterGame(id) {
    const [character, inventory, quests, encounter, map] = await Promise.all([
        api("GET", `/characters/${id}`),
        api("GET", `/characters/${id}/inventory`),
        api("GET", `/characters/${id}/quests`),
        api("GET", `/characters/${id}/encounter`).catch((error) => {
            if (error.status === 404) {
                return null;
            }
            throw error;
        }),
        api("GET", `/characters/${id}/map`)
    ]);
    storage.set(CHARACTER_KEY, id);
    state.map = map;
    state.character = character;
    $("#log").replaceChildren();
    show("game");
    renderCharacter();
    renderLocation();
    log(`${character.name} is in ${character.location.name}.`, "system turn");
    log(character.location.description, "muted");
    setEncounter(encounter);
    if (encounter) {
        log(`The fight is still on! ${enemyNames(encounter)} won't let you go.`, "bad");
    }
    renderInventory(inventory);
    renderQuests(quests);
}

async function refreshGame() {
    const id = state.character.id;
    const [character, inventory, quests, map] = await Promise.all([
        api("GET", `/characters/${id}`),
        api("GET", `/characters/${id}/inventory`),
        api("GET", `/characters/${id}/quests`),
        api("GET", `/characters/${id}/map`)
    ]);
    state.character = character;
    state.map = map;
    renderCharacter();
    renderLocation();
    renderInventory(inventory);
    renderQuests(quests);
}

function experienceForLevel(level) {
    return 25 * level * (level - 1);
}

function setBar(selector, value, max) {
    const percent = max > 0 ? Math.max(0, Math.min(100, (value / max) * 100)) : 0;
    $(selector).style.width = `${percent}%`;
}

function renderCharacter() {
    const c = state.character;
    $("#char-name").textContent = c.name;
    $("#char-level").textContent = `Level ${c.level}`;
    $("#hp-text").textContent = `${c.hp} / ${c.maxHp}`;
    setBar("#hp-bar", c.hp, c.maxHp);
    const floor = experienceForLevel(c.level);
    $("#exp-text").textContent = `${c.experience} / ${c.experienceForNextLevel}`;
    setBar("#exp-bar", c.experience - floor, c.experienceForNextLevel - floor);
    $("#atk").textContent = c.attack;
    $("#def").textContent = c.defense;
    $("#gold").textContent = c.gold;
}

function renderLocation() {
    const location = state.character.location;
    $("#loc-name").textContent = location.name;
    $("#loc-desc").textContent = location.description;
    for (const button of document.querySelectorAll("#compass button")) {
        button.disabled = Boolean(state.encounter) || !location.exits.includes(button.dataset.dir);
    }
    $("#shop-tab").hidden = !location.shop;
    if (!location.shop && !$("#shop").hidden) {
        showPanel("inventory");
    }
    renderMap();
}

function svg(tag, attributes = {}, ...children) {
    const node = document.createElementNS(SVG_NS, tag);
    for (const [name, value] of Object.entries(attributes)) {
        node.setAttribute(name, value);
    }
    node.append(...children);
    return node;
}

function mapLabel(name) {
    const words = name.split(" ");
    if (name.length <= 12 || words.length < 2) {
        return [name];
    }
    let best = 1;
    for (let i = 1; i < words.length; i++) {
        const diff = (a) => Math.abs(words.slice(0, a).join(" ").length - words.slice(a).join(" ").length);
        if (diff(i) < diff(best)) {
            best = i;
        }
    }
    return [words.slice(0, best).join(" "), words.slice(best).join(" ")];
}

function renderMap() {
    const map = state.map;
    if (!map || !state.character) {
        return;
    }
    const current = map.locations.find((location) => location.code === state.character.location.code);
    const whole = {x: 0, y: 0, cols: map.width, rows: map.height};
    drawMap($("#map"), current ? {x: current.x - 1, y: current.y - 1, cols: 3, rows: 3} : whole);
    const dialog = $("#map-dialog");
    if (dialog.open && state.encounter) {
        dialog.close();
    } else if (dialog.open) {
        drawMap($("#full-map"), whole);
    }
}

function openFullMap() {
    if (!state.map) {
        return;
    }
    $("#map-dialog").showModal();
    renderMap();
}

function drawMap(element, view) {
    const map = state.map;
    const {width, height, gapX, gapY, stub} = MAP_CELL;
    const padding = MAP_CELL.padding + stub;
    const here = state.character.location.code;
    const current = map.locations.find((location) => location.code === here);
    const walkable = new Map(state.encounter || !current
        ? []
        : Object.entries(current.exits).map(([direction, code]) => [code, direction]));
    const byCode = new Map(map.locations.map((location) => [location.code, location]));
    const visible = (location) => location.x >= view.x && location.x < view.x + view.cols
        && location.y >= view.y && location.y < view.y + view.rows;
    const center = (location) => ({
        x: padding + (location.x - view.x) * (width + gapX) + width / 2,
        y: padding + (location.y - view.y) * (height + gapY) + height / 2
    });
    const gateOf = (location, code) => {
        const exit = Object.entries(location.exits).find(([, target]) => target === code);
        return exit ? location.gates[exit[0]] : undefined;
    };
    const shown = map.locations.filter(visible);

    const edges = [];
    const locks = [];
    for (const location of shown) {
        for (const code of Object.values(location.exits)) {
            const target = byCode.get(code);
            if (!target) {
                continue;
            }
            const from = center(location);
            const to = center(target);
            const gate = gateOf(location, code) || gateOf(target, location.code);
            const classes = ["edge"];
            if (gate) classes.push("gated");
            if (!visible(target)) {
                const dx = Math.sign(to.x - from.x);
                const dy = Math.sign(to.y - from.y);
                const reach = dx ? width / 2 + stub : height / 2 + stub;
                classes.push("stub");
                edges.push(svg("line", {
                    x1: from.x, y1: from.y, x2: from.x + dx * reach, y2: from.y + dy * reach,
                    class: classes.join(" ")
                }, svg("title", {}, `Leads to ${target.name}`)));
                continue;
            }
            if (location.code > code) {
                continue;
            }
            const open = (location.code === here && walkable.has(code)) || (code === here && walkable.has(location.code));
            if (open) classes.push("open");
            edges.push(svg("line", {x1: from.x, y1: from.y, x2: to.x, y2: to.y, class: classes.join(" ")}));
            if (gate) {
                locks.push(svg("text", {x: (from.x + to.x) / 2, y: (from.y + to.y) / 2, class: "lock"},
                    svg("title", {}, `Requires ${gate.join(", ")}`), "🔒"));
            }
        }
    }

    const nodes = shown.map((location) => {
        const {x, y} = center(location);
        const direction = walkable.get(location.code);
        const classes = ["node"];
        if (!location.discovered) classes.push("unknown");
        if (location.safe) classes.push("safe");
        if (location.code === here) classes.push("current");
        if (direction) classes.push("reachable");
        const lines = mapLabel(location.name);
        const text = svg("text", {x, y: y - (lines.length - 1) * 7},
            ...lines.map((line, index) => svg("tspan", {x, dy: index === 0 ? 0 : 14}, line)));
        const gate = direction && current.gates[direction];
        const name = location.discovered ? location.name : "somewhere unexplored";
        const title = location.code === here
            ? `${location.name} (you are here)`
            : direction
                ? `Walk ${direction.toLowerCase()} to ${name}${gate ? ` (requires ${gate.join(", ")})` : ""}`
                : location.discovered ? location.name : "Unexplored";
        const node = svg("g", {class: classes.join(" ")},
            svg("title", {}, title),
            svg("rect", {x: x - width / 2, y: y - height / 2, width, height, rx: 8}),
            text);
        if (direction) {
            node.setAttribute("tabindex", "0");
            node.setAttribute("role", "button");
            node.addEventListener("click", () => move(direction));
            node.addEventListener("keydown", (event) => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    move(direction);
                }
            });
        }
        return node;
    });

    const svgWidth = padding * 2 + view.cols * (width + gapX) - gapX;
    const svgHeight = padding * 2 + view.rows * (height + gapY) - gapY;
    element.setAttribute("viewBox", `0 0 ${svgWidth} ${svgHeight}`);
    element.replaceChildren(...edges, ...nodes, ...locks);
}

function enemyNames(encounter) {
    return encounter.enemies.filter((enemy) => enemy.hp > 0).map((enemy) => enemy.name).join(" and ");
}

function setEncounter(encounter) {
    state.encounter = encounter && encounter.status === "PLAYER_TURN" ? encounter : null;
    $("#combat").hidden = !state.encounter;
    if (state.encounter) {
        const alive = state.encounter.enemies.filter((enemy) => enemy.hp > 0);
        if (!alive.some((enemy) => enemy.position === state.target)) {
            state.target = alive.length ? alive[0].position : null;
        }
        renderEnemies();
        renderCombatItems();
    } else {
        state.target = null;
    }
    renderLocation();
    for (const button of document.querySelectorAll("#inventory button")) {
        button.disabled = Boolean(state.encounter);
    }
}

function renderEnemies() {
    $("#enemies").replaceChildren(...state.encounter.enemies.map((enemy) => {
        const down = enemy.hp <= 0;
        const bar = el("div", {className: "bar"}, el("div", {className: "fill hp"}));
        bar.firstChild.style.width = `${(enemy.hp / enemy.maxHp) * 100}%`;
        const button = el("button", {
                type: "button",
                className: `enemy${enemy.position === state.target ? " selected" : ""}${down ? " down" : ""}`,
                disabled: down,
                title: down ? "Already bonked" : "Click to target",
                onclick: () => {
                    state.target = enemy.position;
                    renderEnemies();
                }
            },
            el("div", {className: "row"},
                el("strong", {textContent: enemy.name}),
                el("span", {className: "muted", textContent: down ? "down" : `${enemy.hp} / ${enemy.maxHp} HP`})),
            bar);
        return el("li", {}, button);
    }));
}

function lineTone(line) {
    const name = state.character ? state.character.name : "";
    if (/VICTORY|QUEST COMPLETE/.test(line)) return "good";
    if (/LEVEL UP| feels lucky/.test(line)) return "level";
    if (/ heals \d+ HP/.test(line)) return "good";
    if (/^Loot!| receives | finds \d+ gold/.test(line)) return "loot";
    if (/faints|DEFEAT/.test(line)) return "bad";
    if (line.replace(/^CRITICAL! /, "").startsWith(`${name} loses`)) return "hurt";
    if (/ loses \d+ HP| is down!/.test(line)) return "hit";
    if (/ gains \d+ EXP|Quest progress/.test(line)) return "system";
    if (/ dodges/.test(line)) return "muted";
    return "";
}

function log(text, tone = lineTone(text)) {
    const list = $("#log");
    list.append(el("li", {className: tone, textContent: text}));
    while (list.children.length > 300) {
        list.firstChild.remove();
    }
    list.scrollTop = list.scrollHeight;
}

async function move(direction) {
    if (!state.character || state.encounter) {
        return;
    }
    await guarded(async () => {
        const result = await api("POST", `/characters/${state.character.id}/move`, {direction});
        state.map = await api("GET", `/characters/${state.character.id}/map`);
        Object.assign(state.character, {location: result.location, hp: result.hp, maxHp: result.maxHp});
        log(result.message, result.encounter ? "bad turn" : "system turn");
        if (!result.encounter) {
            log(result.location.description, "muted");
        }
        result.log.forEach((line) => log(line, lineTone(line)));
        if (result.log.length) {
            await refreshGame();
        }
        renderCharacter();
        setEncounter(result.encounter);
    });
}

async function act(skill) {
    if (!state.encounter) {
        return;
    }
    const body = {skill};
    if (skill !== "BONKING_SPIN" && state.target !== null) {
        body.target = state.target;
    }
    await guarded(() => playAction(body));
}

async function actWithItem(item) {
    if (!state.encounter) {
        return;
    }
    await guarded(async () => {
        const status = await playAction({item: item.code});
        if (status === "PLAYER_TURN") {
            renderInventory(await api("GET", `/characters/${state.character.id}/inventory`));
        }
    });
}

async function playAction(body) {
    const result = await api("POST", `/characters/${state.character.id}/encounter/action`, body);
    result.log.forEach((line, index) => log(line, index === 0 ? `${lineTone(line)} turn` : lineTone(line)));
    state.character.hp = result.hp;
    state.character.maxHp = result.maxHp;
    renderCharacter();
    setEncounter(result.encounter);
    if (result.status !== "PLAYER_TURN") {
        await refreshGame();
    }
    return result.status;
}

function combatItems() {
    const items = state.inventory ? state.inventory.items : [];
    return items.filter((item) => item.effect === "HEAL" || item.effect === "GUARANTEED_CRIT");
}

function renderCombatItems() {
    if (!state.encounter) {
        return;
    }
    $("#lucky").hidden = !state.encounter.lucky;
    const items = combatItems();
    $("#combat-items").hidden = items.length === 0;
    $("#combat-items").replaceChildren(...items.map((item, index) => {
        const fullHp = item.effect === "HEAL" && state.character.hp >= state.character.maxHp;
        const alreadyLucky = item.effect === "GUARANTEED_CRIT" && state.encounter.lucky;
        return el("button", {
                type: "button",
                className: "combat-item",
                disabled: fullHp || alreadyLucky,
                title: fullHp ? "Already at full HP" : alreadyLucky ? "Already feeling lucky" : "Uses your turn",
                onclick: () => actWithItem(item)
            },
            index < 6 ? el("kbd", {textContent: String(index + 4)}) : null,
            ` ${item.name} `,
            el("span", {className: "quantity", textContent: `×${item.quantity}`}),
            el("small", {textContent: effectText(item)}));
    }));
}

function bonusText(bonus) {
    const parts = [];
    if (bonus.maxHp) parts.push(`+${bonus.maxHp} HP`);
    if (bonus.attack) parts.push(`+${bonus.attack} ATK`);
    if (bonus.defense) parts.push(`+${bonus.defense} DEF`);
    return parts.join(" ");
}

function renderInventory(inventory) {
    state.inventory = inventory;
    Object.assign(state.character, {
        hp: inventory.hp, maxHp: inventory.maxHp, attack: inventory.attack, defense: inventory.defense,
        gold: inventory.gold
    });
    renderCharacter();
    const order = (item) => (item.slot ? SLOT_ORDER.indexOf(item.slot) : SLOT_ORDER.length);
    const items = [...inventory.items].sort((a, b) => (b.equipped - a.equipped) || (order(a) - order(b)));
    const fighting = Boolean(state.encounter);
    const button = (className, textContent, onclick) =>
        el("button", {type: "button", className, textContent, disabled: fighting, onclick});
    $("#inventory").replaceChildren(...items.map((item) => el("li", {className: `item${item.equipped ? " equipped" : ""}`},
        el("div", {className: "row"},
            el("span", {className: `name rarity-${item.rarity}`, textContent: item.name},
                item.quantity > 1 ? el("span", {className: "quantity", textContent: ` ×${item.quantity}`}) : null),
            el("span", {className: "actions"},
                item.effect
                    ? button("primary", "Use", () => useItem(item))
                    : button(item.equipped ? "secondary" : "primary", item.equipped ? "Unequip" : "Equip", () => toggleItem(item)),
                item.equipped ? null : button("secondary", "Sell", () => sellItem(item, 1)),
                item.quantity > 1 ? button("secondary", "Sell all", () => sellItem(item, item.quantity)) : null)),
        el("p", {textContent: item.description}),
        el("div", {className: "meta", textContent: `${itemMeta(item)} · sells for ${item.sellValue} gold`}))));
    renderCombatItems();
    if (state.shop && !$("#shop").hidden) {
        renderShop(state.shop);
    }
}

async function sellItem(item, quantity) {
    const precious = item.rarity === "EPIC" || item.rarity === "LEGENDARY";
    if (precious && !confirm(`Sell ${quantity > 1 ? `${quantity}× ` : ""}${item.name}? Very rare. Much regret?`)) {
        return;
    }
    await guarded(async () => {
        const inventory = await api("POST", `/characters/${state.character.id}/inventory/sell`, {code: item.code, quantity});
        renderInventory(inventory);
        const label = quantity > 1 ? `${quantity}× ${item.name}` : item.name;
        log(`${state.character.name} sells ${label} for ${item.sellValue * quantity} gold. Stonks.`, "loot");
    });
}

async function toggleItem(item) {
    await guarded(async () => {
        const action = item.equipped ? "unequip" : "equip";
        const inventory = await api("POST", `/characters/${state.character.id}/inventory/${item.id}/${action}`);
        renderInventory(inventory);
        log(`${state.character.name} ${item.equipped ? "takes off" : "puts on"} ${item.name}.`, "loot");
    });
}

function renderQuests(quests) {
    $("#quests").replaceChildren(...quests.map((quest) => {
        const progress = el("div", {className: "bar"}, el("div", {className: "fill exp"}));
        progress.firstChild.style.width = `${(quest.progress / quest.requiredAmount) * 100}%`;
        const reward = [`${quest.rewardExp} EXP`, quest.rewardItem].filter(Boolean).join(" + ");
        const started = quest.status === "ACTIVE" || quest.status === "COMPLETED";
        const objective = `${OBJECTIVE_VERBS[quest.objectiveType]} ${quest.target}: ${quest.progress}/${quest.requiredAmount}`;
        const meta = quest.status === "LOCKED"
            ? `Requires: ${quest.requires} · Reward: ${reward}`
            : `${objective} · Reward: ${reward}`;
        return el("li", {className: `item${quest.status === "LOCKED" ? " locked" : ""}`},
            el("div", {className: "row"},
                el("span", {className: "name", textContent: quest.name}),
                el("span", {className: `badge ${quest.status}`, textContent: quest.status})),
            el("p", {textContent: quest.description}),
            started ? progress : null,
            el("div", {className: "row meta"},
                el("span", {textContent: meta}),
                quest.status === "AVAILABLE"
                    ? el("button", {type: "button", className: "primary", textContent: "Accept", onclick: () => acceptQuest(quest)})
                    : null));
    }));
}

async function acceptQuest(quest) {
    await guarded(async () => {
        await api("POST", `/characters/${state.character.id}/quests/${quest.code}/accept`);
        renderQuests(await api("GET", `/characters/${state.character.id}/quests`));
        log(`Quest accepted: ${quest.name}. Much determination.`, "level");
    });
}

function showPanel(panel) {
    for (const tab of document.querySelectorAll("#game .tab")) {
        tab.classList.toggle("active", tab.dataset.panel === panel);
    }
    $("#inventory").hidden = panel !== "inventory";
    $("#quests").hidden = panel !== "quests";
    $("#shop").hidden = panel !== "shop";
    if (panel === "shop") {
        guarded(async () => renderShop(await api("GET", `/characters/${state.character.id}/shop`)));
    }
}

function effectText(item) {
    switch (item.effect) {
        case "HEAL": return `heals ${item.effectPower} HP`;
        case "PERMANENT_ATTACK": return `+${item.effectPower} ATK forever`;
        case "GUARANTEED_CRIT": return "next attack crits";
        default: return "";
    }
}

function itemMeta(item) {
    return item.effect
        ? `CONSUMABLE · ${item.rarity} · ${effectText(item)}`
        : `${item.slot} · ${item.rarity} · ${bonusText(item.bonus)}`;
}

function renderShop(shop) {
    state.shop = shop;
    const gold = state.character.gold;
    $("#shop").replaceChildren(
        el("li", {className: "shop-header muted small", textContent: `${shop.location} merchant. Such deals. You have ${gold} gold.`}),
        ...shop.offers.map((offer) => el("li", {className: "item"},
            el("div", {className: "row"},
                el("span", {className: `name rarity-${offer.rarity}`, textContent: offer.name}),
                el("span", {className: "actions"},
                    el("span", {className: "price", textContent: `${offer.price} gold`}),
                    el("button", {
                        type: "button",
                        className: "primary",
                        textContent: "Buy",
                        disabled: gold < offer.price,
                        title: gold < offer.price ? "Much poor" : "",
                        onclick: () => buyItem(offer)
                    }))),
            el("p", {textContent: offer.description}),
            el("div", {className: "meta", textContent: itemMeta(offer)}))));
}

async function buyItem(offer) {
    await guarded(async () => {
        const inventory = await api("POST", `/characters/${state.character.id}/shop/buy`, {code: offer.code, quantity: 1});
        renderInventory(inventory);
        log(`${state.character.name} buys ${offer.name} for ${offer.price} gold.`, "loot");
    });
}

async function useItem(item) {
    await guarded(async () => {
        const before = {hp: state.character.hp, attack: state.character.attack};
        const inventory = await api("POST", `/characters/${state.character.id}/inventory/${item.id}/use`);
        renderInventory(inventory);
        const name = state.character.name;
        if (item.effect === "HEAL") {
            log(`${name} eats ${item.name} and heals ${inventory.hp - before.hp} HP. Much yum.`, "good");
        } else if (item.effect === "PERMANENT_ATTACK") {
            log(`${name} invests in ${item.name}. ATK ${before.attack} → ${inventory.attack}. To the moon!`, "level");
        } else {
            log(`${name} uses ${item.name}.`, "loot");
        }
    });
}

function onKey(event) {
    if ($("#game").hidden || event.ctrlKey || event.metaKey || event.altKey || event.target.matches("input")) {
        return;
    }
    const direction = KEY_DIRECTIONS[event.key];
    const skill = KEY_SKILLS[event.key];
    if (event.key === "m" || event.key === "M") {
        event.preventDefault();
        if ($("#map-dialog").open) {
            $("#map-dialog").close();
        } else if (!state.encounter) {
            openFullMap();
        }
    } else if (direction) {
        event.preventDefault();
        move(direction);
    } else if (skill) {
        event.preventDefault();
        act(skill);
    } else if (state.encounter && /^[4-9]$/.test(event.key)) {
        const button = $("#combat-items").children[Number(event.key) - 4];
        if (button) {
            event.preventDefault();
            if (!button.disabled) {
                button.click();
            }
        }
    }
}

async function init() {
    for (const tab of document.querySelectorAll("#auth .tab")) {
        tab.addEventListener("click", () => setAuthMode(tab.dataset.mode));
    }
    for (const tab of document.querySelectorAll("#game .tab")) {
        tab.addEventListener("click", () => showPanel(tab.dataset.panel));
    }
    for (const button of document.querySelectorAll("#compass button")) {
        button.addEventListener("click", () => move(button.dataset.dir));
    }
    $("#full-map-button").addEventListener("click", openFullMap);
    $("#map-dialog-close").addEventListener("click", () => $("#map-dialog").close());
    $("#map-dialog").addEventListener("click", (event) => {
        if (event.target === event.currentTarget) {
            event.currentTarget.close();
        }
    });
    for (const button of document.querySelectorAll(".skill")) {
        button.addEventListener("click", () => act(button.dataset.skill));
    }
    $("#auth-form").addEventListener("submit", submitAuth);
    $("#create-form").addEventListener("submit", createCharacter);
    $("#logout").addEventListener("click", () => logout());
    $("#to-characters").addEventListener("click", () => guarded(openCharacters, showCharactersError));
    document.addEventListener("keydown", onKey);

    const token = storage.get(TOKEN_KEY);
    if (!token || !startSession(token)) {
        logout();
        return;
    }
    const characterId = storage.get(CHARACTER_KEY);
    await guarded(async () => {
        if (characterId) {
            try {
                await enterGame(characterId);
                return;
            } catch (error) {
                if (error.status === 401) {
                    throw error;
                }
            }
        }
        await openCharacters();
    }, showCharactersError);
}

init();
