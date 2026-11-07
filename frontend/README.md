# Frontend — Angular 22

SPA de Gestión de Mancomunidad. Usa componentes standalone, SCSS y Vitest.

## Arranque

El backend debe estar en `http://localhost:8080`:

```bash
npm install        # solo la primera vez
npm start          # http://localhost:4200
```

El servidor de desarrollo aplica `proxy.conf.json`: `/api` y `/actuator` se reenvían al backend, así que no hace falta configurar CORS.

## Comandos

```bash
npm test -- --watch=false   # tests con Vitest
npm run build               # build de producción en dist/
npm run watch               # build incremental
```

## Estructura

```text
src/app/
├── core/           # servicios y modelos transversales
├── features/       # pantallas por funcionalidad
├── app.config.ts   # providers globales
└── app.routes.ts   # rutas lazy
```

La home (`features/inicio`) consulta `GET /actuator/health` y muestra si el backend está disponible.
