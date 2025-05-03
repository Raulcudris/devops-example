# Usa una imagen más reciente y específica de Node.js
FROM node:20-alpine

# Establece el directorio de trabajo
WORKDIR /usr/src/app

# Copia solo los archivos necesarios para instalar dependencias primero
COPY package.json package-lock.json ./

# Instala las dependencias de producción solamente
RUN npm ci --only=production

# Copia el resto de los archivos
COPY . .

# Expone el puerto que usa la aplicación
EXPOSE 8080

# Define el usuario no root para mayor seguridad
USER node

# Comando para ejecutar la aplicación
CMD ["node", "app.js"]