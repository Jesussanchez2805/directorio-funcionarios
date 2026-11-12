# Directorio de funcionarios

Aplicación MVC construida con Java 21, Spring Boot, Spring Data JPA y MySQL. La interfaz estática usa HTML, CSS y JavaScript vanilla; consume la API REST bajo `/api/v1/funcionarios`.

El acceso a la API está protegido con Spring Security y HTTP Basic. Al abrir la aplicación, la ruta principal presenta la pantalla de inicio de sesión; el formulario valida las credenciales antes de abrir el directorio.

El acceso se bloquea durante 30 segundos después de tres contraseñas incorrectas consecutivas. El bloqueo se aplica en el servidor, se muestra una cuenta regresiva en el formulario y un inicio de sesión exitoso reinicia el contador. Los intentos se mantienen en memoria y se reinician al reiniciar la aplicación.

## Requisitos

- Java 21
- Maven 3.9+
- MySQL 8+

## Configuración y ejecución

La configuración predeterminada se conecta a MySQL en Aiven por TLS, usando la base de datos `defaultdb` y el usuario `avnadmin`. La contraseña es obligatoria y se obtiene de la variable de entorno `DB_PASSWORD`; no se guarda en el código. Si el servicio usa otro nombre de base de datos o endpoint, configura también `DB_URL`.

Configura las variables de entorno antes de iniciar la aplicación:

```bash
export DB_PASSWORD='tu-contraseña-de-MySQL'
mvn spring-boot:run
```

Abre <http://localhost:8080> para usar el directorio.

El usuario de desarrollo predeterminado es `admin` y la contraseña es `admin123`. Cámbialos antes de iniciar la aplicación en cualquier entorno compartido o desplegado, definiendo `APP_SECURITY_USERNAME` y `APP_SECURITY_PASSWORD`, por ejemplo:

```bash
export APP_SECURITY_USERNAME=mi_usuario
export APP_SECURITY_PASSWORD='una-clave-segura'
mvn spring-boot:run
```

La aplicación conserva el encabezado HTTP Basic en `sessionStorage` para autorizar las llamadas de la interfaz durante la pestaña actual; al cerrar sesión o la pestaña se elimina. HTTP Basic no cifra las credenciales, por lo que fuera del desarrollo local debe servirse exclusivamente mediante HTTPS.

Hibernate crea o actualiza la tabla `funcionarios` en la base configurada al iniciar; los datos permanecen guardados en MySQL al reiniciar la aplicación. La cuenta debe tener permisos para crear o actualizar esa tabla.

## API

| Método | Ruta | Comportamiento |
|---|---|---|
| GET | `/api/v1/funcionarios` | Lista; acepta `nombre` parcial o `departamento` exacto |
| GET | `/api/v1/funcionarios/{id}` | Consulta por ID |
| POST | `/api/v1/funcionarios` | Registra; devuelve `201 Created` |
| PUT | `/api/v1/funcionarios/{id}` | Actualiza los campos del funcionario |
| DELETE | `/api/v1/funcionarios/{id}` | Elimina; devuelve `204 No Content` |
| POST | `/api/v1/auth/validar` | Valida credenciales HTTP Basic; devuelve `204 No Content` |

Las búsquedas no distinguen mayúsculas de minúsculas. Cuando ambos filtros tienen valor, prevalece `nombre`. Las listas vacías y búsquedas sin coincidencias devuelven `204`.

Ejemplo de cuerpo para crear o actualizar:

```json
{
  "nombre": "Ana Pérez",
  "cargo": "Analista",
  "departamento": "Contabilidad",
  "telefono": "555-0101"
}
```

En una solicitud de creación también puede enviarse `"id": null`; un ID no nulo se rechaza. Los campos `nombre`, `cargo` y `departamento` son obligatorios.

## Pruebas

```bash
mvn test
```
