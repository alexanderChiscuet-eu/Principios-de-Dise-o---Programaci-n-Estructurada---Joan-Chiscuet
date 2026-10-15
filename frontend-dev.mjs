import {createServer} from 'node:http';
import {readFile} from 'node:fs/promises';
const root = new URL('./src/main/resources/static/', import.meta.url);
const files = {'/':'index.html', '/index.html':'index.html', '/style.css':'style.css', '/app.js':'app.js'};
createServer(async (req, res) => {
  const file = files[new URL(req.url, 'http://localhost').pathname];
  if (!file) { res.writeHead(404).end('No encontrado'); return; }
  try {
    const body = await readFile(new URL(file,root));
    res.writeHead(200, {'Content-Type':file.endsWith('.css')?'text/css':file.endsWith('.js')?'text/javascript':'text/html; charset=utf-8'}).end(body);
  } catch { res.writeHead(500).end('No se pudo leer el archivo'); }
}).listen(5173, '127.0.0.1', () => console.log('Frontend: http://127.0.0.1:5173 (backend: http://localhost:8080)'));
