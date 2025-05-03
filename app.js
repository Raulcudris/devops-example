const express = require('express');
const app = express();
const PORT = 8080;

app.get('/', (req, res) => {
  res.send('¡Hola desde nuestro pipeline DevOps Actualizacion!');
});

app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
});