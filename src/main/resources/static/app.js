// Same-origin by default. The separate development server uses the configured CORS origins.
const API = location.port === '5173' ? 'http://localhost:8080/api' : '/api';
const $ = id => document.getElementById(id);
const fmt = n => n.toLocaleString('es-EC', {maximumFractionDigits: 4});
const buttons = [...document.querySelectorAll('button')];
async function request(path, method = 'GET') {
  const response = await fetch(API + path, {method});
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || `HTTP ${response.status}`);
  }
  return response.json();
}
async function action(operation) {
  buttons.forEach(b => b.disabled = true);
  $('status').textContent = 'Ejecutando en el servidor…';
  try { await operation(); }
  catch (error) { $('status').textContent = `No se pudo completar: ${error.message}. Verifica el backend en localhost:8080 y el origen CORS.`; }
  finally { buttons.forEach(b => b.disabled = false); }
}
$('compare').onclick = () => action(async () => {
  const data = await request(`/benchmark?limit=${$('limit').value}&runs=${$('runs').value}`, 'POST');
  const a = data.withoutCache, b = data.withCache;
  $('plain').textContent = `${fmt(a.meanMs)} ms`;
  $('cached').textContent = `${fmt(b.meanMs)} ms`;
  $('speed').textContent = `${fmt(data.speedup)} ×`;
  $('plainQueries').textContent = `${a.sqlQueries} consultas SQL en ${data.runs} lecturas`;
  $('hitQueries').textContent = `${b.sqlQueries} consultas SQL en ${data.runs} lecturas`;
  $('saved').textContent = `${fmt(data.savedPercent)} % de reducción de tiempo`;
  const max = Math.max(a.meanMs, b.meanMs, .000001);
  $('plainBar').style.width = `${a.meanMs / max * 100}%`;
  $('cachedBar').style.width = `${b.meanMs / max * 100}%`;
  $('plainLabel').textContent = `${fmt(a.meanMs)} ms`;
  $('cachedLabel').textContent = `${fmt(b.meanMs)} ms`;
  $('cold').textContent = `Carga inicial con caché vacía: ${fmt(data.cold.elapsedMs)} ms · ${data.cold.sqlQueries} consulta SQL · ${data.cold.records.toLocaleString('es-EC')} registros.`;
  $('samples').textContent = JSON.stringify({sinCache: a, conCache: b}, null, 2);
  $('status').textContent = `Experimento completado · ${data.limit.toLocaleString('es-EC')} registros · ${data.runs} repeticiones por modalidad.`;
});
$('clear').onclick = () => action(async () => {
  await request('/cache', 'DELETE');
  $('status').textContent = 'Caché vaciada. La próxima lectura con caché consultará SQL.';
});
for (const [id, mode] of [['readPlain', 'uncached'], ['readCached', 'cached']]) {
  $(id).onclick = () => action(async () => {
    const data = await request(`/products/${mode}?limit=${$('limit').value}`);
    $('rows').replaceChildren(...data.sample.map(product => {
      const row = document.createElement('tr');
      for (const value of [product.id, product.name, product.category, Number(product.price).toFixed(2)]) {
        const cell = document.createElement('td'); cell.textContent = value; row.append(cell);
      }
      return row;
    }));
    $('status').textContent = `${data.records.toLocaleString('es-EC')} registros leídos en ${fmt(data.elapsedMs)} ms · ${data.sqlQueries} consultas SQL · muestra de ${data.sample.length}.`;
  });
}
request('/info').then(info => { $('connection').textContent = `● ${info.records.toLocaleString('es-EC')} registros disponibles`; })
  .catch(() => { $('connection').textContent = '○ Backend desconectado'; });
