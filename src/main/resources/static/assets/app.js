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
const MAP_CELL = {width: 96, height: 50, gapX: 26, gapY: 22, padding: 4};
const SVG_NS = "http://www.w3.org/2000/svg";

const state = {
    token: null,
    username: null,
    authMode: "login",
    character: null,
    map: null,
    encounter: null,
    target: null,
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
        state.map ? Promise.resolve(state.map) : api("GET", "/world/map")
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
    const [character, inventory, quests] = await Promise.all([
        api("GET", `/characters/${id}`),
        api("GET", `/characters/${id}/inventory`),
        api("GET", `/characters/${id}/quests`)
    ]);
    state.character = character;
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
}

function renderLocation() {
    const location = state.character.location;
    $("#loc-name").textContent = location.name;
    $("#loc-desc").textContent = location.description;
    for (const button of document.querySelectorAll("#compass button")) {
        button.disabled = Boolean(state.encounter) || !location.exits.includes(button.dataset.dir);
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
    const {width, height, gapX, gapY, padding} = MAP_CELL;
    const here = state.character.location.code;
    const current = map.locations.find((location) => location.code === here);
    const walkable = new Map(state.encounter || !current
        ? []
        : Object.entries(current.exits).map(([direction, code]) => [code, direction]));
    const byCode = new Map(map.locations.map((location) => [location.code, location]));
    const center = (location) => ({
        x: padding + location.x * (width + gapX) + width / 2,
        y: padding + location.y * (height + gapY) + height / 2
    });

    const edges = [];
    for (const location of map.locations) {
        for (const code of Object.values(location.exits)) {
            const target = byCode.get(code);
            if (!target || location.code > code) {
                continue;
            }
            const from = center(location);
            const to = center(target);
            const open = (location.code === here && walkable.has(code)) || (code === here && walkable.has(location.code));
            edges.push(svg("line", {x1: from.x, y1: from.y, x2: to.x, y2: to.y, class: `edge${open ? " open" : ""}`}));
        }
    }

    const nodes = map.locations.map((location) => {
        const {x, y} = center(location);
        const direction = walkable.get(location.code);
        const classes = ["node"];
        if (location.safe) classes.push("safe");
        if (location.code === here) classes.push("current");
        if (direction) classes.push("reachable");
        const lines = mapLabel(location.name);
        const text = svg("text", {x, y: y - (lines.length - 1) * 7},
            ...lines.map((line, index) => svg("tspan", {x, dy: index === 0 ? 0 : 14}, line)));
        const title = location.code === here
            ? `${location.name} (you are here)`
            : direction ? `Walk ${direction.toLowerCase()} to ${location.name}` : location.name;
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

    const svgWidth = padding * 2 + map.width * (width + gapX) - gapX;
    const svgHeight = padding * 2 + map.height * (height + gapY) - gapY;
    const element = $("#map");
    element.setAttribute("viewBox", `0 0 ${svgWidth} ${svgHeight}`);
    element.replaceChildren(...edges, ...nodes);
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
    if (/LEVEL UP/.test(line)) return "level";
    if (/^Loot!| receives /.test(line)) return "loot";
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
        Object.assign(state.character, {location: result.location, hp: result.hp, maxHp: result.maxHp});
        log(result.message, result.encounter ? "bad turn" : "system turn");
        if (!result.encounter) {
            log(result.location.description, "muted");
        }
        renderCharacter();
        setEncounter(result.encounter);
    });
}

async function act(skill) {
    if (!state.encounter) {
        return;
    }
    await guarded(async () => {
        const body = {skill};
        if (skill !== "BONKING_SPIN" && state.target !== null) {
            body.target = state.target;
        }
        const result = await api("POST", `/characters/${state.character.id}/encounter/action`, body);
        result.log.forEach((line, index) => log(line, index === 0 ? `${lineTone(line)} turn` : lineTone(line)));
        state.character.hp = result.hp;
        state.character.maxHp = result.maxHp;
        renderCharacter();
        setEncounter(result.encounter);
        if (result.status !== "PLAYER_TURN") {
            await refreshGame();
        }
    });
}

function bonusText(bonus) {
    const parts = [];
    if (bonus.maxHp) parts.push(`+${bonus.maxHp} HP`);
    if (bonus.attack) parts.push(`+${bonus.attack} ATK`);
    if (bonus.defense) parts.push(`+${bonus.defense} DEF`);
    return parts.join(" ");
}

function renderInventory(inventory) {
    Object.assign(state.character, {
        hp: inventory.hp, maxHp: inventory.maxHp, attack: inventory.attack, defense: inventory.defense
    });
    renderCharacter();
    const items = [...inventory.items].sort((a, b) =>
        (b.equipped - a.equipped) || (SLOT_ORDER.indexOf(a.slot) - SLOT_ORDER.indexOf(b.slot)));
    $("#inventory").replaceChildren(...items.map((item) => el("li", {className: `item${item.equipped ? " equipped" : ""}`},
        el("div", {className: "row"},
            el("span", {className: `name rarity-${item.rarity}`, textContent: item.name}),
            el("button", {
                type: "button",
                className: item.equipped ? "secondary" : "primary",
                textContent: item.equipped ? "Unequip" : "Equip",
                disabled: Boolean(state.encounter),
                onclick: () => toggleItem(item)
            })),
        el("p", {textContent: item.description}),
        el("div", {className: "meta", textContent: `${item.slot} · ${item.rarity} · ${bonusText(item.bonus)}`}))));
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
        progress.firstChild.style.width = `${(quest.kills / quest.requiredKills) * 100}%`;
        const reward = [`${quest.rewardExp} EXP`, quest.rewardItem].filter(Boolean).join(" + ");
        return el("li", {className: "item"},
            el("div", {className: "row"},
                el("span", {className: "name", textContent: quest.name}),
                el("span", {className: `badge ${quest.status}`, textContent: quest.status})),
            el("p", {textContent: quest.description}),
            quest.status === "AVAILABLE" ? null : progress,
            el("div", {className: "row meta"},
                el("span", {textContent: `${quest.targetEnemy}: ${quest.kills}/${quest.requiredKills} · Reward: ${reward}`}),
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
}

function onKey(event) {
    if ($("#game").hidden || event.ctrlKey || event.metaKey || event.altKey || event.target.matches("input")) {
        return;
    }
    const direction = KEY_DIRECTIONS[event.key];
    const skill = KEY_SKILLS[event.key];
    if (direction) {
        event.preventDefault();
        move(direction);
    } else if (skill) {
        event.preventDefault();
        act(skill);
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
