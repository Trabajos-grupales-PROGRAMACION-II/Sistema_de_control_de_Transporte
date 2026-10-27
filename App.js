/* ---- Administradores (reemplazar por backend/API) ---- */
const ADMINS = [{ username: "admin", password: "admin123", name: "Administrador", email: "admin@ejemplo.com", role: "admin" }];

const TITLES = { dashboard: "Inicio", users: "Usuarios", drivers: "Choferes", schedules: "Horarios", profile: "Mi perfil", settings: "Configuración" };
const TYPE_LABEL = { admin: "Administrador", transport: "Unidad de transporte", student: "Estudiante" };
const DAYS = ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"];

const $ = id => document.getElementById(id);
const esc = s => String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const uid = () => Math.random().toString(36).slice(2, 9);
const mins = t => { const [h, m] = t.split(":").map(Number); return h * 60 + m; };

/* ---- Datos (localStorage; con backend esto pasa a la API) ---- */
const load = k => { try { return JSON.parse(localStorage.getItem(k)) || []; } catch (e) { return []; } };
let profiles = load("profiles"), drivers = load("drivers");
/* Migración: turnos antiguos con minutos sueltos pasan a ventanas fijas (vacías) */
profiles.forEach(p => (p.shifts || []).forEach(s => {
  if (typeof s.rest === "number") { Object.assign(s, { restStart: "", restDur: 0, maintStart: "", maintDur: 0 }); delete s.rest; delete s.maint; }
}));
function save() {
  try { localStorage.setItem("profiles", JSON.stringify(profiles)); localStorage.setItem("drivers", JSON.stringify(drivers)); }
  catch (e) { alert("No se pudo guardar (almacenamiento lleno). Prueba con fotos más pequeñas."); }
}
const units = () => profiles.filter(p => p.role === "transport");
const allAccounts = () => [...ADMINS, ...profiles];
const driverById = id => drivers.find(d => d.id === id);
const photo = d => d && d.photo ? `<img class="photo" src="${d.photo}" alt="">` : `<div class="photo"></div>`;
let currentUser = null;

/* ---- Sesión ---- */
function saveSession(u) { try { sessionStorage.setItem("session", u ? u.username : ""); } catch (e) {} }
function loadSession() {
  try { const n = sessionStorage.getItem("session"); return allAccounts().find(u => u.username === n) || null; } catch (e) { return null; }
}

/* ---- Reglas de turnos ---- */
function checkShift(unit, s) {
  const d = driverById(s.driverId);
  if (!d || !d.active) return "Elige un chofer activo y disponible.";
  if (!s.start || !s.end || mins(s.end) <= mins(s.start)) return "La hora de fin debe ser posterior a la de inicio.";
  const covered = d.availability.some(a => a.day === s.day && mins(a.start) <= mins(s.start) && mins(a.end) >= mins(s.end));
  if (!covered) return `${d.name} no está disponible ese día en ese horario.`;
  for (const u of units()) for (const o of u.shifts || []) {
    if (o.id === s.id || o.day !== s.day) continue;
    const sameUnit = u.username === unit.username, sameDriver = o.driverId === s.driverId;
    if (!sameUnit && !sameDriver) continue;
    if (mins(s.start) < mins(o.end) && mins(o.start) < mins(s.end))
      return sameUnit ? "Choca con otro turno de esta unidad." : `${d.name} ya tiene un turno en la unidad ${u.plate} a esa hora.`;
  }
  return "";
}

/* Ventanas fijas de descanso y mantenimiento, en minutos desde medianoche */
function breaks(s) {
  return [["Descanso", s.restStart, s.restDur], ["Mantenimiento", s.maintStart, s.maintDur]]
    .filter(([, st, du]) => st && du > 0).map(([name, st, du]) => ({ name, from: mins(st), to: mins(st) + du }));
}
function checkBreaks(s) {
  const b = breaks(s);
  for (const w of b) if (w.from < mins(s.start) || w.to > mins(s.end)) return `${w.name} debe quedar dentro del turno (${s.start}–${s.end}).`;
  if (b.length === 2 && b[0].from < b[1].to && b[1].from < b[0].to) return "Descanso y mantenimiento no pueden cruzarse.";
  return "";
}

/* ---- Render ---- */
function detailRows(u) {
  const rows = [["Usuario", u.username]];
  if (u.role === "transport") {
    const d = driverById(u.driverId);
    rows.push(["Matrícula", u.plate], ["Modelo", u.model], ["Color", u.color], ["Teléfono", u.phone], ["Chofer principal", d ? d.name : "Sin chofer"]);
  } else { rows.push(["Correo", u.email]); if (u.role === "student") rows.push(["Identificación", u.studentId]); }
  return rows;
}

function profileExtra(u) {
  if (u.role !== "transport") return "";
  const d = driverById(u.driverId);
  const drv = d ? `<div class="profile" style="margin-top:16px">${photo(d)}<div><b>${esc(d.name)}</b><br><span class="empty">Cédula ${esc(d.cedula)} · Tel. ${esc(d.phone)}</span></div></div>` : "";
  const list = [...(u.shifts || [])].sort((a, b) => a.day - b.day || mins(a.start) - mins(b.start));
  const sh = list.length
    ? `<h3 style="margin-top:20px">Horarios</h3><ul>${list.map(s => `<li>${DAYS[s.day]} ${s.start}–${s.end} · ${esc((driverById(s.driverId) || {}).name || "—")}</li>`).join("")}</ul>`
    : `<p class="empty">Sin horarios asignados.</p>`;
  return drv + sh;
}

function renderUsers() {
  $("users-body").innerHTML = profiles.length ? profiles.map(u => {
    const detail = u.role === "transport"
      ? `${u.plate} · ${u.model} · ${u.color} · ${u.phone} · ${(driverById(u.driverId) || {}).name || "Sin chofer"}`
      : `${u.studentId} · ${u.email}`;
    return `<tr><td>${esc(u.name)}</td><td><span class="badge">${TYPE_LABEL[u.role]}</span></td><td>${esc(detail)}</td>
      <td><button class="btn ghost small" data-del="${esc(u.username)}">Eliminar</button></td></tr>`;
  }).join("") : `<tr><td colspan="4" class="empty">Aún no hay usuarios. Crea el primero con "Nuevo usuario".</td></tr>`;
}

function renderDrivers() {
  $("drivers-body").innerHTML = drivers.length ? drivers.map(d => {
    const days = [...new Set(d.availability.map(a => DAYS[a.day]))].join(", ");
    const a = d.availability[0];
    return `<tr><td>${photo(d)}</td><td>${esc(d.name)}</td><td>${esc(d.cedula)}</td><td>${esc(d.phone)}</td>
      <td>${days} ${a ? a.start + "–" + a.end : ""}</td>
      <td><span class="badge">${d.active ? "Activo" : "Inactivo"}</span></td>
      <td><button class="btn ghost small" data-toggle="${d.id}">${d.active ? "Desactivar" : "Activar"}</button>
          <button class="btn ghost small" data-ddel="${d.id}">Eliminar</button></td></tr>`;
  }).join("") : `<tr><td colspan="7" class="empty">Aún no hay choferes. Agrega el primero con "Nuevo chofer".</td></tr>`;
}

function fillSelects() {
  const act = drivers.filter(d => d.active);
  $("f-driver").innerHTML = `<option value="">Sin chofer por ahora</option>` + act.map(d => `<option value="${d.id}">${esc(d.name)}</option>`).join("");
  const prev = $("s-unit").value;
  $("s-unit").innerHTML = units().map(u => `<option value="${esc(u.username)}">${esc(u.plate)} · ${esc(u.model)}</option>`).join("");
  if (prev) $("s-unit").value = prev;
}

const curUnit = () => units().find(u => u.username === $("s-unit").value);

function renderShifts() {
  const u = curUnit();
  if (!u) { $("shifts-body").innerHTML = `<tr><td colspan="6" class="empty">Crea primero una unidad de transporte.</td></tr>`; return; }
  const list = [...(u.shifts || [])].sort((a, b) => a.day - b.day || mins(a.start) - mins(b.start));
  const win = (s, k) => `<input type="time" value="${s[k + "Start"] || ""}" data-sid="${s.id}" data-f="${k}Start"> <input type="number" min="0" value="${s[k + "Dur"] || 0}" data-sid="${s.id}" data-f="${k}Dur">`;
  $("shifts-body").innerHTML = list.length ? list.map(s => `<tr><td>${DAYS[s.day]}</td><td>${s.start}–${s.end}</td>
    <td>${esc((driverById(s.driverId) || {}).name || "—")}</td>
    <td>${win(s, "rest")}</td><td>${win(s, "maint")}</td>
    <td><button class="btn ghost small" data-sdel="${s.id}">Quitar</button></td></tr>`).join("")
    : `<tr><td colspan="6" class="empty">Esta unidad no tiene turnos todavía.</td></tr>`;
}

/* Solo muestra choferes activos que pueden cubrir el turno del formulario */
function refreshShiftDrivers() {
  const u = curUnit(), prev = $("s-driver").value;
  const base = { id: "", day: +$("s-day").value, start: $("s-start").value, end: $("s-end").value };
  const ok = u ? drivers.filter(d => d.active && !checkShift(u, { ...base, driverId: d.id })) : [];
  $("s-driver").innerHTML = ok.length ? ok.map(d => `<option value="${d.id}">${esc(d.name)}</option>`).join("") : `<option value="">Ningún chofer disponible</option>`;
  if (ok.some(d => d.id === prev)) $("s-driver").value = prev;
}

function renderAll() { renderUsers(); renderDrivers(); fillSelects(); renderShifts(); refreshShiftDrivers(); renderMap(); }

function showApp(user) {
  currentUser = user;
  const isAdmin = user.role === "admin";
  $("view-login").hidden = true; $("view-app").hidden = false;
  document.querySelectorAll(".nav[data-admin]").forEach(b => b.hidden = !isAdmin);
  $("top-user").textContent = user.name;
  $("avatar").textContent = user.name.charAt(0).toUpperCase();
  $("p-name").textContent = user.name;
  $("p-role").textContent = TYPE_LABEL[user.role];
  $("p-details").innerHTML = detailRows(user).map(([k, v]) => `<dt>${k}</dt><dd>${esc(v)}</dd>`).join("");
  $("p-extra").innerHTML = profileExtra(user);
  renderAll();
  goTo(isAdmin ? "dashboard" : "profile");
}

function showLogin() {
  currentUser = null;
  $("view-app").hidden = true; $("view-login").hidden = false;
  $("pass").value = ""; $("login-error").textContent = "";
}

function goTo(section) {
  document.querySelectorAll("[data-sec]").forEach(s => { s.hidden = s.id !== "sec-" + section; });
  document.querySelectorAll(".nav[data-section]").forEach(b => {
    if (b.dataset.section === section) b.setAttribute("aria-current", "page"); else b.removeAttribute("aria-current");
  });
  $("section-title").textContent = TITLES[section];
  if (section === "schedules") { renderShifts(); refreshShiftDrivers(); }
  if (section === "dashboard") { initMap(); renderMap(); }
}

/* ---- Login ---- */
function login() {
  const found = allAccounts().find(u => u.username === $("user").value.trim() && u.password === $("pass").value);
  if (!found) { $("login-error").textContent = "Usuario o contraseña incorrectos. Revisa los datos e inténtalo de nuevo."; return; }
  saveSession(found); showApp(found);
}

/* ---- Nuevo usuario (unidad o estudiante) ---- */
function toggleFields() {
  const t = $("f-type").value;
  $("fields-transport").hidden = t !== "transport";
  $("fields-student").hidden = t !== "student";
}
function openForm() {
  document.querySelectorAll("#form-panel input").forEach(i => i.value = "");
  $("f-type").value = "transport"; $("form-error").textContent = "";
  fillSelects(); toggleFields(); $("form-panel").hidden = false; $("f-plate").focus();
}
function saveProfile() {
  const role = $("f-type").value, v = id => $(id).value.trim();
  const username = v("f-user"), password = $("f-pass").value;
  const fail = m => { $("form-error").textContent = m; };
  let p;
  if (role === "transport") {
    p = { role, plate: v("f-plate"), model: v("f-model"), color: v("f-color"), phone: v("f-uphone"), driverId: $("f-driver").value, shifts: [], email: "" };
    p.name = "Unidad " + p.plate;
    if (!p.plate || !p.model || !p.color || !p.phone) return fail("Completa matrícula, modelo, color y teléfono de la unidad.");
    if (units().some(u => u.plate.toLowerCase() === p.plate.toLowerCase())) return fail("Ya existe una unidad con esa matrícula.");
  } else {
    p = { role, name: v("f-sname"), studentId: v("f-sid"), email: v("f-semail") };
    if (!p.name || !p.studentId || !p.email) return fail("Completa nombre, identificación y correo.");
  }
  if (!username || password.length < 6) return fail("Define un usuario y una contraseña de al menos 6 caracteres.");
  if (allAccounts().some(u => u.username === username)) return fail("Ese usuario ya existe. Elige otro nombre de acceso.");
  profiles.push({ ...p, username, password });
  save(); renderAll(); $("form-panel").hidden = true;
}

/* ---- Choferes ---- */
function readPhoto(file) {
  return new Promise(res => {
    if (!file) return res("");
    const r = new FileReader();
    r.onload = () => {
      const img = new Image();
      img.onload = () => {
        const sc = Math.min(1, 160 / Math.max(img.width, img.height)), c = document.createElement("canvas");
        c.width = img.width * sc; c.height = img.height * sc;
        c.getContext("2d").drawImage(img, 0, 0, c.width, c.height);
        res(c.toDataURL("image/jpeg", 0.8));
      };
      img.onerror = () => res(""); img.src = r.result;
    };
    r.readAsDataURL(file);
  });
}
function openDriverForm() {
  document.querySelectorAll("#driver-form input[type=text],#driver-form input[type=tel],#driver-form input[type=file]").forEach(i => i.value = "");
  document.querySelectorAll("#d-days input").forEach(i => i.checked = false);
  $("d-start").value = "06:00"; $("d-end").value = "18:00"; $("driver-error").textContent = "";
  $("driver-form").hidden = false; $("d-name").focus();
}
async function saveDriver() {
  const name = $("d-name").value.trim(), cedula = $("d-cedula").value.trim(), phone = $("d-phone").value.trim();
  const days = [...document.querySelectorAll("#d-days input:checked")].map(i => +i.value);
  const start = $("d-start").value, end = $("d-end").value;
  const fail = m => { $("driver-error").textContent = m; };
  if (!name || !cedula || !phone) return fail("Completa nombre, cédula y teléfono.");
  if (drivers.some(d => d.cedula === cedula)) return fail("Ya existe un chofer con esa cédula.");
  if (!days.length) return fail("Marca al menos un día de disponibilidad.");
  if (!start || !end || mins(end) <= mins(start)) return fail("La hora final debe ser posterior a la inicial.");
  const img = await readPhoto($("d-photo").files[0]);
  drivers.push({ id: uid(), name, cedula, phone, photo: img, active: true, availability: days.map(day => ({ day, start, end })) });
  save(); renderAll(); $("driver-form").hidden = true;
}
const driverInUse = id => units().some(u => u.driverId === id || (u.shifts || []).some(s => s.driverId === id));

/* ---- Turnos ---- */
function addShift() {
  const u = curUnit(), err = $("shift-error");
  if (!u) { err.textContent = "Crea primero una unidad de transporte."; return; }
  const s = { id: uid(), day: +$("s-day").value, start: $("s-start").value, end: $("s-end").value, driverId: $("s-driver").value,
              restStart: $("s-rstart").value, restDur: +$("s-rdur").value || 0, maintStart: $("s-mstart").value, maintDur: +$("s-mdur").value || 0 };
  const msg = checkShift(u, s) || checkBreaks(s);
  if (msg) { err.textContent = msg; return; }
  u.shifts = u.shifts || []; u.shifts.push(s);
  err.textContent = ""; save(); renderShifts(); refreshShiftDrivers(); renderMap();
}

/* ---- Eventos ---- */
$("btn-login").addEventListener("click", login);
["user", "pass"].forEach(id => $(id).addEventListener("keydown", e => { if (e.key === "Enter") login(); }));
$("btn-logout").addEventListener("click", () => { saveSession(null); showLogin(); });
document.querySelectorAll(".nav[data-section]").forEach(b => b.addEventListener("click", () => goTo(b.dataset.section)));

$("btn-new").addEventListener("click", openForm);
$("btn-cancel").addEventListener("click", () => { $("form-panel").hidden = true; });
$("btn-save").addEventListener("click", saveProfile);
$("f-type").addEventListener("change", toggleFields);
$("users-body").addEventListener("click", e => {
  const name = e.target.dataset.del;
  if (!name || !confirm("¿Eliminar este usuario?")) return;
  profiles = profiles.filter(u => u.username !== name); save(); renderAll();
});

$("d-days").innerHTML = DAYS.map((n, i) => `<label><input type="checkbox" value="${i}">${n}</label>`).join("");
$("s-day").innerHTML = DAYS.map((n, i) => `<option value="${i}">${n}</option>`).join("");
$("btn-new-driver").addEventListener("click", openDriverForm);
$("btn-cancel-driver").addEventListener("click", () => { $("driver-form").hidden = true; });
$("btn-save-driver").addEventListener("click", saveDriver);
$("drivers-body").addEventListener("click", e => {
  const t = e.target.dataset;
  if (t.toggle) {
    const d = driverById(t.toggle);
    if (d.active && driverInUse(d.id)) return alert("Este chofer tiene unidades o turnos asignados. Quítalos antes de desactivarlo.");
    d.active = !d.active; save(); renderAll();
  } else if (t.ddel) {
    if (driverInUse(t.ddel)) return alert("Este chofer tiene unidades o turnos asignados. Quítalos antes de eliminarlo.");
    if (!confirm("¿Eliminar este chofer?")) return;
    drivers = drivers.filter(d => d.id !== t.ddel); save(); renderAll();
  }
});

$("s-unit").addEventListener("change", () => { renderShifts(); refreshShiftDrivers(); });
["s-day", "s-start", "s-end"].forEach(id => $(id).addEventListener("change", refreshShiftDrivers));
$("btn-add-shift").addEventListener("click", addShift);
$("shifts-body").addEventListener("click", e => {
  const id = e.target.dataset.sdel, u = curUnit();
  if (!id) return;
  u.shifts = u.shifts.filter(s => s.id !== id); save(); renderShifts(); refreshShiftDrivers();
});
$("shifts-body").addEventListener("change", e => {
  const t = e.target, u = curUnit();
  if (!t.dataset.sid) return;
  const s = u.shifts.find(x => x.id === t.dataset.sid), f = t.dataset.f, old = s[f];
  s[f] = f.endsWith("Dur") ? Math.max(0, +t.value || 0) : t.value;
  const msg = checkBreaks(s);
  if (msg) { s[f] = old; alert(msg); } else save();
  renderShifts(); renderMap();
});

/* ---- Mapa en vivo (posiciones simuladas según horarios; sustituir por GPS real) ---- */
const MAP_CENTER = [-0.1807, -78.4678]; // centro inicial del mapa: cámbialo por tu ciudad
const STATUS = { route: ["En ruta", "#1f8a5b"], rest: ["En descanso", "#d98e04"], maint: ["En mantenimiento", "#c2410c"], off: ["Fuera de turno", "#7b8794"] };
const DEFAULT_STATIONS = [
  { name: "Estación Central", lat: -0.1807, lng: -78.4678, travel: 6, stop: 2 },
  { name: "Terminal Norte", lat: -0.1650, lng: -78.4700, travel: 8, stop: 2 },
  { name: "Campus", lat: -0.1700, lng: -78.4900, travel: 7, stop: 2 },
  { name: "Plaza Sur", lat: -0.1950, lng: -78.4800, travel: 7, stop: 2 }
];
let stations;
try { stations = JSON.parse(localStorage.getItem("stations")); } catch (e) {}
if (!Array.isArray(stations)) stations = DEFAULT_STATIONS.map(s => ({ ...s }));
const saveStations = () => { try { localStorage.setItem("stations", JSON.stringify(stations)); } catch (e) {} };
const fmt = m => { m = Math.round(m); return String(Math.floor(m / 60) % 24).padStart(2, "0") + ":" + String(m % 60).padStart(2, "0"); };
const routeTotal = () => stations.reduce((a, s) => a + s.stop + s.travel, 0);
let map, stationLayer, unitLayer, sim = null, addingStation = false;
const markers = {};

function nowInfo() {
  if (sim) return sim;
  const d = new Date();
  return { day: (d.getDay() + 6) % 7, min: d.getHours() * 60 + d.getMinutes() };
}

/* Posición dentro de la vuelta completa; "held" = unidad detenida (descanso/mantenimiento) */
function cycleAt(t, held) {
  if (!stations.length) return null;
  const total = routeTotal();
  if (stations.length < 2 || total <= 0) return { pos: [stations[0].lat, stations[0].lng], text: "En " + stations[0].name };
  let m = ((t % total) + total) % total;
  for (let i = 0; i < stations.length; i++) {
    const a = stations[i], b = stations[(i + 1) % stations.length];
    if (m < a.stop) return { pos: [a.lat, a.lng], text: held ? `Detenida en ${a.name}` : `En parada: ${a.name} (sale en ${Math.ceil(a.stop - m)} min)` };
    m -= a.stop;
    if (m < a.travel) {
      const f = m / a.travel;
      return { pos: [a.lat + (b.lat - a.lat) * f, a.lng + (b.lng - a.lng) * f],
               text: held ? `Detenida entre ${a.name} y ${b.name}` : `Hacia ${b.name} (llega en ${Math.ceil(a.travel - m)} min)` };
    }
    m -= a.travel;
  }
}

/* Minutos efectivos de marcha: el tiempo en descanso/mantenimiento no avanza la unidad */
const effMinutes = (s, t) => t - mins(s.start) - breaks(s).reduce((a, w) => a + Math.max(0, Math.min(t, w.to) - w.from), 0);

function unitState(u, now) {
  const s = (u.shifts || []).find(x => x.day === now.day && mins(x.start) <= now.min && now.min < mins(x.end));
  if (!s) return { key: "off", note: "Sin turno en este momento" };
  const w = breaks(s).find(w => w.from <= now.min && now.min < w.to);
  if (w) return { key: w.name === "Descanso" ? "rest" : "maint", shift: s, note: `${w.name} hasta las ${fmt(w.to)}; se reactiva después` };
  return { key: "route", shift: s, note: "" };
}

function unitView(u, i, now) {
  const st = unitState(u, now), n = units().length;
  let loc = null;
  if (stations.length && st.shift) loc = cycleAt(effMinutes(st.shift, now.min) + i * routeTotal() / n, st.key !== "route");
  else if (stations.length) loc = { pos: [stations[0].lat, stations[0].lng], text: "En la estación inicial" };
  return { st, loc };
}

function initMap() {
  if (map) { map.invalidateSize(); return; }
  if (typeof L === "undefined") { $("map").innerHTML = `<p class="empty" style="padding:16px">No se pudo cargar el mapa. Revisa tu conexión a internet.</p>`; return; }
  map = L.map("map").setView(MAP_CENTER, 13);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", { maxZoom: 19, attribution: "&copy; OpenStreetMap contributors" }).addTo(map);
  stationLayer = L.layerGroup().addTo(map);
  unitLayer = L.layerGroup().addTo(map);
  if (stations.length) map.fitBounds(stations.map(s => [s.lat, s.lng]), { padding: [30, 30] });
  map.on("click", e => {
    if (!addingStation) return;
    const name = prompt("Nombre de la estación:");
    addingStation = false; $("btn-add-station").textContent = "Agregar estación en el mapa";
    if (!name || !name.trim()) return;
    stations.push({ name: name.trim(), lat: e.latlng.lat, lng: e.latlng.lng, travel: 5, stop: 2 });
    saveStations(); renderMap();
  });
}

function renderMap() {
  const now = nowInfo(), us = units();
  if (!sim) { $("m-day").value = now.day; $("m-time").value = fmt(now.min); }
  const rows = us.map((u, i) => ({ u, ...unitView(u, i, now) }));
  $("st-units").textContent = us.length;
  $("st-drivers").textContent = drivers.filter(d => d.active).length;
  $("st-route").textContent = rows.filter(r => r.st.key === "route").length;

  $("units-live").innerHTML = rows.length ? rows.map(({ u, st, loc }) => {
    const d = driverById(st.shift ? st.shift.driverId : u.driverId), [label, color] = STATUS[st.key];
    const win = st.shift ? (breaks(st.shift).map(w => `${w.name} ${fmt(w.from)}–${fmt(w.to)}`).join("<br>") || "—") : "—";
    return `<tr><td>${esc(u.plate)}<br><span class="empty">${esc(u.model)} · ${esc(u.color)}<br>Tel. ${esc(u.phone)}</span></td>
      <td><span class="badge" style="border-color:${color};color:${color}">${label}</span><br><span class="empty">${esc(st.note)}</span></td>
      <td>${d ? `<div class="profile">${photo(d)}<div>${esc(d.name)}<br><span class="empty">Cédula ${esc(d.cedula)} · ${esc(d.phone)}</span></div></div>` : "Sin chofer"}</td>
      <td>${esc(loc ? loc.text : "Sin estaciones")}</td><td>${win}</td></tr>`;
  }).join("") : `<tr><td colspan="5" class="empty">Aún no hay unidades de transporte.</td></tr>`;

  $("stations-body").innerHTML = stations.length ? stations.map((s, i) => `<tr><td>${esc(s.name)}</td>
    <td><input type="number" min="0" value="${s.travel}" data-si="${i}" data-f="travel"></td>
    <td><input type="number" min="0" value="${s.stop}" data-si="${i}" data-f="stop"></td>
    <td><button class="btn ghost small" data-sidel="${i}">Quitar</button></td></tr>`).join("")
    : `<tr><td colspan="4" class="empty">Aún no hay estaciones. Usa "Agregar estación en el mapa".</td></tr>`;
  $("cycle-total").textContent = stations.length > 1
    ? `Vuelta completa: ${routeTotal()} min (el orden de las estaciones es el de creación).` : "Agrega al menos dos estaciones para definir una ruta.";

  if (!map) return;
  stationLayer.clearLayers();
  if (stations.length > 1) L.polyline([...stations, stations[0]].map(s => [s.lat, s.lng]), { color: "#7b8794", weight: 3, dashArray: "6 6" }).addTo(stationLayer);
  stations.forEach(s => L.circleMarker([s.lat, s.lng], { radius: 6, color: "#18221f", weight: 2, fillColor: "#fff", fillOpacity: 1 }).bindTooltip(s.name).addTo(stationLayer));
  rows.forEach(({ u, st, loc }) => {
    if (!loc) return;
    const d = driverById(st.shift ? st.shift.driverId : u.driverId), [label, color] = STATUS[st.key];
    const html = `<b>${esc(u.plate)}</b> · ${label}<br>${esc(u.model)} · ${esc(u.color)} · Tel. ${esc(u.phone)}<br>${d ? "Conductor: " + esc(d.name) + " · " + esc(d.phone) : "Sin chofer"}<br>${esc(loc.text)}${st.note ? "<br>" + esc(st.note) : ""}`;
    let m = markers[u.username];
    if (!m) m = markers[u.username] = L.circleMarker(loc.pos, { radius: 10, color: "#fff", weight: 2, fillOpacity: 1 }).addTo(unitLayer).bindPopup(html);
    m.setLatLng(loc.pos); m.setStyle({ fillColor: color }); m.setPopupContent(html);
  });
  Object.keys(markers).forEach(k => { if (!us.some(u => u.username === k)) { unitLayer.removeLayer(markers[k]); delete markers[k]; } });
}

$("m-day").innerHTML = DAYS.map((n, i) => `<option value="${i}">${n}</option>`).join("");
const setSim = () => { sim = { day: +$("m-day").value, min: mins($("m-time").value || "00:00") }; renderMap(); };
$("m-day").addEventListener("change", setSim);
$("m-time").addEventListener("change", setSim);
$("btn-now").addEventListener("click", () => { sim = null; renderMap(); });
$("btn-add-station").addEventListener("click", () => {
  addingStation = !addingStation;
  $("btn-add-station").textContent = addingStation ? "Toca el mapa para ubicarla…" : "Agregar estación en el mapa";
});
$("stations-body").addEventListener("change", e => {
  const t = e.target;
  if (t.dataset.si === undefined) return;
  stations[+t.dataset.si][t.dataset.f] = Math.max(0, +t.value || 0);
  saveStations(); renderMap();
});
$("stations-body").addEventListener("click", e => {
  const i = e.target.dataset.sidel;
  if (i === undefined || !confirm("¿Quitar esta estación de la ruta?")) return;
  stations.splice(+i, 1); saveStations(); renderMap();
});
setInterval(() => { if (!sim && currentUser && !$("sec-dashboard").hidden) renderMap(); }, 20000);

/* ---- Restaurar sesión ---- */
const existing = loadSession();
if (existing) showApp(existing);