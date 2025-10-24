--Link del video--

https://drive.google.com/file/d/1UUJtY3489ohAmhL0hLU_ulHNcaPOkJz2/view?usp=sharing


--Descripcion del proyecto--

Monedix es una aplicación móvil desarrollada en Java para Android que permite gestionar de forma sencilla y organizada las finanzas personales del usuario. Ofrece registro de gastos diarios, control de cuentas, planificación de ahorros y seguimiento de metas financieras. Además, integra un sistema de reportes conectado con n8n, que permite enviar al correo del usuario un resumen completo de su situación financiera con solo un botón. Monedix busca brindar mayor claridad, control y motivación en la administración del dinero día a día

--Instrucciones de instalacion--

Se puede abrir y usar directamente desde Android Studio. Solo hace falta descargar el proyecto desde Github, se abre en el programa y se espera a que se carguen los archivos necesarios.
Luego se conecta un ceular Android a la computadora o se puede usar un emular del programa y se ejcuta la app. Despues de esto queda lista para comenzar a usarse.

En caso de que se quiera instalar sin Android Studio, también es posible generar la APK del proyecto. Esta APK se puede pasar a cualquier teléfono Android y abrirla para que el dispositivo instale la aplicación como cualquier otra. Es una forma rápida de compartir Monedix o usarla sin necesidad de compilar el proyecto cada vez. 

--Integracion con n8n--

Monedix integra un sistema de envío de reportes financieros por email utilizando n8n. Cuando el usuario pulsa “Enviar Reporte”, la app genera un JSON con todos los datos financieros relevantes: cuentas, gastos, ahorros, metas, totales y el correo del destinatario. Ese contenido se envía mediante una petición POST al Webhook configurado en n8n.

El flujo en n8n utiliza tres nodos: el Webhook como disparador del flujo, un nodo de procesamiento para organizar los datos y el nodo Gmail que construye y envía el correo usando una plantilla HTML. Gracias a expresiones como {{ $json.email }} y {{ $json.fechaGeneracion }}, cada reporte se personaliza según el usuario y la fecha.

Durante la implementación se resolvieron tres situaciones principales: un error de compilación por una variable fuera de alcance, el envío de correos vacíos debido a un mal uso del JSON en el flujo de n8n y errores en el destinatario al no enviar correctamente el email desde la app. Todas estas incidencias se corrigieron ajustando el código en Android y la configuración en los nodos de n8n.

--Imagenes de la App--

<img width="1918" height="921" alt="imagen" src="https://github.com/user-attachments/assets/6cb44fd2-fa6e-4a50-b0ea-6a75b4c6caf3" />

