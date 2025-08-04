# Desktop Software

**_Descripción del Proyecto:_**

CentauroSoft es una aplicación de escritorio diseñada para la gestión integral de un negocio, enfocada en el control de clientes, proveedores, y la administración de sus respectivas cuentas corrientes y movimientos. La aplicación permite registrar remitos, gestionar productos y generar reportes detallados en formato PDF para un seguimiento eficiente de las operaciones comerciales.

### Características Principales

* **_Gestión de Clientes y Proveedores:_** 
Funcionalidades completas para la carga, modificación y eliminación 
de clientes y proveedores.


* **_Control de Cuentas Corrientes:_** Registro y actualización de movimientos
de cuenta corriente para cada cliente y proveedor.


* **_Gestión de Comprobantes_:** Creación de comprobantes (remitos) asociados 
a las cuentas corrientes, incluyendo los productos correspondientes.


* **_Catálogo de Productos:_** Administración de un catálogo de productos con
código, nombre y precio.


* **_Generación de Reportes PDF:_** Creación de reportes detallados de las
cuentas corrientes de clientes y reportes de facturas de proveedores,
que se guardan y se abren automáticamente en el sistema del usuario.

### Tecnologías Utilizadas

* **_Lenguaje de Programación:_** Java 21


* **_Interfaz de Usuario:_** JavaFX 21


* **_Gestión de Dependencias:_** Apache Maven


* **_Base de Datos:_** MySQL Connector/J 8.0.33 para la conexión con una base de datos MySQL.


* **_Generación de PDF:_** iText7

##   Configuración e Instalación

  Para configurar y ejecutar este proyecto, sigue estos pasos:

#### 1 - Clona el repositorio:

git clone https://github.com/JoaquinMart/CentauroProyect.git

#### 2 - Configura la Base de Datos:

El proyecto utiliza una base de datos MySQL. La estructura de las tablas se crea automáticamente al iniciar la aplicación si no existen.

Crea una base de datos con el nombre centauro.
La aplicación se encargará de crear las tablas clientes, proveedores, cuenta_corriente, comprobantes y productos la primera vez que se ejecute.

#### 3 - Configura las Credenciales de la Base de Datos:

Por motivos de seguridad, las credenciales de la base de datos se manejan a través de un archivo de variables de entorno.

En la raíz del proyecto, crea un archivo llamado .env (asegúrate de que no tenga ninguna extensión adicional).

### Cómo Ejecutar la Aplicación

Para ejecutar la aplicación, puedes hacerlo directamente desde tu IDE (como IntelliJ o Visual Studio Code) ejecutando la clase principal main.Main.java.

### Estructura del Proyecto.

├── src/

│   ├── main/

│   │   ├── java/

│   │   │   ├── controllers/

│   │   │   ├── dao/

│   │   │   ├── main/

│   │   │   ├── model/

│   │   │   └── services/

│   │   └── resources/

│   │       ├── stylePDF.css

│   │       └── icono.png

├── .gitignore

├── .env

├── pom.xml

└── README.md

### Contribuciones

Si bien este proyecto fue desarrollado para un propósito específico, las sugerencias de mejora son bienvenidas.