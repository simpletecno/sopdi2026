# SOPDI
Sistema ERP para Control de Operaciones Comerciales

Descripción
-----------
SOPDI es una aplicación Java/Maven basada en Vaadin para la gestión contable, compras, ventas, inventario y recursos humanos.

Contenido del repositorio
-------------------------
- Código fuente Java en `src/main/java/`.
- Recursos web y assets en `src/main/webapp/`.
- Configuración de build en `pom.xml`.

Requisitos
----------
- JDK 8 o superior (se recomienda JDK 11+).
- Apache Maven 3.6+.
- Git.
- (Opcional) Apache Tomcat u otro contenedor para desplegar el WAR.

Compilación
-----------
Desde la raíz del proyecto ejecuta:

```bash
mvn -DskipTests package
```

El artefacto resultante se generará en `target/`.

Desarrollo y ejecución
----------------------
- Importa el proyecto en tu IDE (IntelliJ IDEA, Eclipse).
- Ejecuta la clase de arranque o despliega el WAR en Tomcat.

Buenas prácticas del repo
-------------------------
- He añadido reglas en `.gitignore` para evitar subir artefactos y archivos locales.
- Las carpetas `src/main/webapp/projectfiles/` y `src/main/webapp/pdfreceipts/` se han añadido al `.gitignore` y sus contenidos han sido removidos del índice (siguen presentes en tu copia local).
- Para archivos binarios grandes considera usar Git LFS.

Contribuciones
--------------
1. Crea una rama para tu funcionalidad: `git checkout -b feat/descripcion`.
2. Haz commits pequeños y descriptivos.
3. Abre un pull request para revisión.

Comandos útiles
---------------
```bash
git status
git log --oneline --graph -n 5
```

Licencia
--------
Este repositorio incluye un archivo `LICENSE` con la licencia MIT por defecto.

Contacto
--------
Si quieres que actualice la licencia o el contenido del README (por ejemplo, añadir instrucciones específicas para despliegue, variables de entorno o configuraciones), indícalo y lo modifico.
