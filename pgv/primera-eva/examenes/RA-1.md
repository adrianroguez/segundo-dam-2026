# Examen RA1 - Programación de Procesos

---

### Pregunta 1
**¿Qué afirmación es correcta tras un fork() satisfactorio?**

- [x] **a.** Padre e hijo tienen espacios de direcciones virtuales separados; modificar una variable ordinaria en uno no modifica la del otro.
- [ ] **b.** El hijo empieza a ejecutar siempre desde la primera línea del archivo.
- [ ] **c.** El padre queda bloqueado obligatoriamente hasta que termine el hijo.
- [ ] **d.** Padre e hijo comparten necesariamente la misma variable Python modificable como una única ubicación lógica.

**Respuesta correcta:** **a**  
**Explicación:** Tras un `fork()` exitoso, el proceso hijo es una copia del padre, pero con su propio espacio de direcciones virtuales independiente (utilizando técnicas como *copy-on-write* a nivel de kernel). Por ello, las modificaciones en las variables ordinarias de uno no afectan al otro.

---

### Pregunta 2
**Suponiendo que todas las llamadas a fork tienen éxito, ¿cuántos procesos alcanzan la llamada a registrar(), incluido el original?**

```python
import os
os.fork()
os.fork()
registrar()
```

- [x] **a.** 4
- [ ] **b.** 5
- [ ] **c.** 6
- [ ] **d.** 3

**Respuesta correcta:** **a**  
**Explicación:** Cada llamada incondicional a `fork()` duplica el número de procesos existentes. Partiendo de 1 proceso: el primer `fork()` genera 2 procesos, y el segundo `fork()` es ejecutado por ambos, resultando en $2^2 = 4$ procesos que llegan a ejecutar `registrar()`.

---

### Pregunta 3
**¿Qué diferencia hay entre el cambio de modo usuario a núcleo y el cambio de contexto entre procesos?**

- [ ] **a.** El cambio de contexto solo puede realizarse en modo usuario.
- [x] **b.** Un cambio de modo no implica necesariamente cambiar el proceso en ejecución.
- [ ] **c.** El cambio de modo obliga siempre a crear un proceso nuevo.
- [ ] **d.** Son siempre la misma operación.

**Respuesta correcta:** **b**  
**Explicación:** Un cambio de modo ocurre cuando la CPU pasa a modo núcleo (kernel mode) para atender una llamada al sistema o una interrupción, y puede retornar inmediatamente al mismo proceso sin necesidad de conmutar de tarea (cambio de contexto).

---

### Pregunta 4
**¿Qué afirmación sobre los PID después de fork() es correcta?**

- [ ] **a.** El PID del hijo es siempre el PID del padre más uno.
- [ ] **b.** Padre e hijo tienen el mismo PID.
- [x] **c.** Padre e hijo tienen PID distintos.
- [ ] **d.** El PID del hijo es siempre cero.

**Respuesta correcta:** **c**  
**Explicación:** Cada proceso en el sistema operativo posee un identificador único (PID). El valor 0 es el valor retornado por la función `fork()` en el proceso hijo, pero no es su PID real en el sistema.

---

### Pregunta 5
**En planificación Round Robin, ¿qué ocurre normalmente al agotarse el cuanto si existen otros procesos preparados?**

- [x] **a.** El proceso puede ser expropiado y volver a la cola de preparados.
- [ ] **b.** El proceso conserva la CPU hasta terminar toda su ejecución.
- [ ] **c.** El proceso pasa siempre a bloqueado.
- [ ] **d.** El proceso termina obligatoriamente.

**Respuesta correcta:** **a**  
**Explicación:** El algoritmo Round Robin es expropiativo (*preemptive*). Si un proceso agota su cuanto (*quantum*) de CPU y no ha terminado ni se ha bloqueado, el planificador le retira la CPU y lo devuelve al final de la cola de preparados.

---

### Pregunta 6
**Una interrupción de reloj llega mientras A está ejecutándose. ¿Cuál de las siguientes situaciones es posible?**

- [x] **a.** El núcleo atiende la interrupción y A continúa sin cambiar de proceso.
- [ ] **b.** A debe bloquearse hasta la siguiente interrupción.
- [ ] **c.** A debe terminar porque se ha agotado el reloj.
- [ ] **d.** El dispatcher debe crear un nuevo proceso antes de volver al modo usuario.

**Respuesta correcta:** **a**  
**Explicación:** El núcleo atiende la rutina de servicio de interrupción (ISR) del reloj; si el proceso A aún dispone de tiempo de CPU o no hay otro proceso prioritario, el control se le devuelve directamente a A sin realizar cambio de contexto.

---

### Pregunta 7
**¿Cuál es una responsabilidad característica del dispatcher?**

- [ ] **a.** Establecer la política de prioridades que empleará el planificador.
- [x] **b.** Efectuar la transferencia de ejecución hacia la tarea seleccionada.
- [ ] **c.** Crear un nuevo proceso al realizar un cambio de contexto.
- [ ] **d.** Decidir qué proceso debe desbloquearse cuando finaliza una E/S.

**Respuesta correcta:** **b**  
**Explicación:** El *scheduler* (planificador) decide **qué** proceso se ejecuta según las políticas del sistema, mientras que el *dispatcher* (despachador) es el componente encargado de realizar el cambio de contexto mecánico y ceder el control de la CPU a dicho proceso.

---

### Pregunta 8
**Un proceso espera a que termine una operación de E/S. ¿Cuál es su estado habitual?**

- [x] **a.** Bloqueado.
- [ ] **b.** En ejecución.
- [ ] **c.** Finalizado.
- [ ] **d.** Preparado.

**Respuesta correcta:** **a**  
**Explicación:** Un proceso pasa al estado bloqueado (*blocked* o *waiting*) cuando no puede continuar su ejecución hasta que ocurra un evento externo (como la finalización de una operación de lectura/escritura en disco o red).

---

### Pregunta 9
**Un proceso A entra al núcleo por una llamada al sistema y vuelve al mismo proceso A. ¿Qué conclusión puede extraerse?**

- [ ] **a.** El PCB de A ha dejado de existir mientras se ejecutaba el núcleo.
- [x] **b.** Se ha producido un cambio de modo, pero no necesariamente un cambio de proceso.
- [ ] **c.** El scheduler ha tenido que seleccionar un proceso distinto de A.
- [ ] **d.** Ha tenido que producirse un cambio de contexto de A a otro proceso.

**Respuesta correcta:** **b**  
**Explicación:** Entrar al núcleo mediante una llamada al sistema (syscall) implica una transición de modo usuario a modo núcleo (cambio de privilegio/modo), pero al resolver la petición el sistema puede retornar la ejecución directamente a A sin alternar de proceso.

---

### Pregunta 10
**Un proceso A está bloqueado esperando E/S; B está preparado y C se ejecuta. Finaliza la E/S de A y el planificador es expropiativo. ¿Qué secuencia es posible?**

- [ ] **a.** La finalización de E/S obliga a C a pasar a bloqueado.
- [ ] **b.** B pasa a finalizado porque A ha completado la E/S.
- [x] **c.** A pasa a preparado, C continúa ejecutándose y B sigue preparado.
- [ ] **d.** A pasa directamente de bloqueado a ejecución sin estar preparado.

**Respuesta correcta:** **c**  
**Explicación:** Al completarse la E/S, el proceso A transiciona obligatoriamente de bloqueado a preparado. Aunque el planificador sea expropiativo, si la prioridad de A no supera a la de C, C continúa en ejecución y A se suma a la cola de preparados junto a B.

---

### Pregunta 11
**En un planificador expropiativo, ¿qué puede provocar que un proceso en ejecución pase a preparado?**

- [x] **a.** El agotamiento de su cuanto de CPU.
- [ ] **b.** Únicamente una llamada de E/S bloqueante.
- [ ] **c.** Únicamente su finalización.
- [ ] **d.** La liberación de toda su memoria.

**Respuesta correcta:** **a**  
**Explicación:** En esquemas expropiativos, el agotamiento del tiempo asignado (*quantum*) o la llegada de un proceso de mayor prioridad hace que el proceso en ejecución sea desalojado y regrese al estado preparado.

---

### Pregunta 12
**¿Qué caracteriza a la concurrencia?**

- [x] **a.** El progreso de varias tareas durante intervalos de tiempo solapados, sin exigir simultaneidad.
- [ ] **b.** La ejecución secuencial de un único programa sin interrupciones.
- [ ] **c.** La ejecución simultánea obligatoria en varios núcleos.
- [ ] **d.** La ejecución exclusiva de tareas en equipos diferentes.

**Respuesta correcta:** **a**  
**Explicación:** La concurrencia consiste en gestionar múltiples tareas que progresan de forma intercalada o solapada en el tiempo. No requiere paralelismo real (ejecución simultánea en hardware con varios núcleos).

---

### Pregunta 13
**Un proceso A se bloquea esperando datos y B está preparado. ¿Qué afirmación describe mejor la planificación?**

- [ ] **a.** A permanece ejecutándose hasta que termine la E/S.
- [x] **b.** B puede ser seleccionado porque A ya no está en condiciones de continuar.
- [ ] **c.** La E/S obliga a ejecutar B en modo usuario dentro de la ISR.
- [ ] **d.** El dispatcher convierte la espera de E/S en una finalización de A.

**Respuesta correcta:** **b**  
**Explicación:** Al bloquearse A, este deja de ser elegible para la CPU. El planificador inspecciona la cola de preparados y puede seleccionar a B para aprovechar el tiempo de procesador.

---

### Pregunta 14
**¿Qué hace normalmente subprocess.run(["echo", "hola"]) si el programa externo se ejecuta correctamente?**

- [ ] **a.** Ejecuta el comando únicamente si existe una shell interactiva.
- [ ] **b.** Sustituye permanentemente el proceso Python por echo.
- [x] **c.** Ejecuta el programa indicado y espera a que termine antes de devolver el control.
- [ ] **d.** Crea un hilo Python y retorna inmediatamente.

**Respuesta correcta:** **c**  
**Explicación:** `subprocess.run()` ejecuta el comando especificado como un subproceso hijo síncrono y bloquea la ejecución del proceso Python llamante hasta que el subproceso finaliza.

---

### Pregunta 15
**Un proceso A ejecuta una llamada al sistema que termina sin bloquearlo. Durante esa llamada se produce una interrupción de reloj. ¿Qué afirmación es necesariamente cierta?**

- [ ] **a.** A debe pasar por el estado bloqueado antes de volver al modo usuario.
- [ ] **b.** La interrupción provoca la selección de otro proceso.
- [x] **c.** El procesador puede regresar a A sin que haya habido cambio de proceso.
- [ ] **d.** La llamada al sistema y la interrupción implican dos cambios de contexto entre procesos.

**Respuesta correcta:** **c**  
**Explicación:** La atención de llamadas al sistema e interrupciones ocurre dentro del contexto del núcleo sin forzar una conmutación de procesos; si el planificador lo determina, el control puede volver inmediatamente a A.

---

### Pregunta 16
**La función registrar(x) no está definida y no modifica el flujo antes de su llamada. ¿Cuántos procesos llegan a registrar() con su variable local x igual a 7?**

```python
import os
x = 7
pid = os.fork()
if pid == 0:
    x = 9
registrar(x)
```

- [x] **a.** 1
- [ ] **b.** 2
- [ ] **c.** 3
- [ ] **d.** 0

**Respuesta correcta:** **a**  
**Explicación:** Tras `fork()`, el proceso hijo recibe `pid == 0` y actualiza su propia copia de `x` a `9`. El proceso padre recibe el PID del hijo (distinto de 0), no entra en el bloque condicional y conserva `x = 7`. Solo 1 proceso (el padre) llega con `x == 7`.

---

### Pregunta 17
**¿Cuántos procesos alcanzan procesar() si todas las llamadas a fork tienen éxito?**

```python
import os
os.fork()
if os.fork() == 0:
    os.fork()
procesar()
```

- [ ] **a.** 7
- [ ] **b.** 5
- [x] **c.** 6
- [ ] **d.** 4

**Respuesta correcta:** **c**  
**Explicación:** 
1. El primer `os.fork()` crea 2 procesos.
2. Ambos procesos ejecutan el segundo `os.fork()`, resultando en 4 procesos (2 padres y 2 hijos).
3. Solo los 2 hijos (donde el retorno es `0`) ejecutan el tercer `os.fork()`, duplicándose y generando 2 procesos adicionales ($2 + 2 = 4$).
4. En total, los 2 padres que no entraron al `if` más los 4 procesos resultantes del bloque suman $2 + 4 = 6$ procesos.

---

### Pregunta 18
**Un proceso A bloqueado termina su operación de E/S mientras B está ejecutándose. ¿Cuál es la transición de estado habitual de A?**

- [ ] **a.** Bloqueado a nuevo.
- [ ] **b.** Bloqueado a finalizado.
- [x] **c.** Bloqueado a preparado.
- [ ] **d.** Bloqueado a ejecución sin intervención del planificador.

**Respuesta correcta:** **c**  
**Explicación:** Un proceso nunca pasa directamente de bloqueado a ejecución. Al terminar el evento que esperaba (E/S), transiciona a preparado y queda a la espera de que el planificador le asigne CPU.

---

### Pregunta 19
**En un sistema expropiativo, el proceso A es interrumpido y después reanuda exactamente la instrucción que correspondía. ¿Qué información resulta esencial conservar para esa reanudación?**

- [ ] **a.** Únicamente el PID y el nombre del ejecutable.
- [ ] **b.** Una copia de todos los archivos abiertos en memoria del proceso.
- [x] **c.** El estado de CPU necesario, incluido el punto de reanudación, además de mantener su contexto de memoria.
- [ ] **d.** Solo el valor del contador de instrucciones, sin ningún otro registro.

**Respuesta correcta:** **c**  
**Explicación:** Para poder restaurar fielmente un proceso tras una interrupción, el sistema operativo debe guardar en su Bloque de Control de Proceso (PCB) los registros de CPU, el contador de programa (PC), el puntero de pila y conservar su mapa de memoria intacto.

---

### Pregunta 20
**El proceso A se ejecuta y B está preparado. Se agota el cuanto de A y se decide ejecutar B. ¿Qué descripción distingue correctamente scheduler y dispatcher?**

- [ ] **a.** El scheduler guarda el PCB en disco y el dispatcher determina la prioridad de B.
- [x] **b.** El scheduler elige B y el dispatcher realiza la transferencia efectiva de la CPU.
- [ ] **c.** El dispatcher elige B según la política y el scheduler restaura los registros.
- [ ] **d.** Ambos realizan exclusivamente la misma operación: atender la interrupción del reloj.

**Respuesta correcta:** **b**  
**Explicación:** El *scheduler* es el algoritmo decisorio encargado de seleccionar qué proceso pasa a ejecutarse según las políticas de planificación; el *dispatcher* es el ejecutor a bajo nivel encargado de guardar el contexto de A, cargar el de B y conmutar el control de la CPU.

---

### Pregunta 21
**El proceso A es expropiado y pasa a preparado; B pasa a ejecución. ¿Qué afirmación describe mejor el cambio de contexto?**

- [ ] **a.** Solo cambia el valor del PID visible al usuario; los registros permanecen intactos.
- [ ] **b.** A pierde definitivamente su espacio de direcciones al abandonar la CPU.
- [x] **c.** Debe preservarse el contexto necesario de A y establecerse el de B para continuar su ejecución.
- [ ] **d.** B tiene que empezar a ejecutar desde la primera instrucción de su programa.

**Respuesta correcta:** **c**  
**Explicación:** El cambio de contexto (*context switch*) requiere almacenar el estado actual del procesador para el proceso saliente A (registros, contador de programa, etc.) y cargar el estado previamente guardado de B para reanudar su ejecución donde se quedó.

---

### Pregunta 22
**¿Qué condición permite paralelismo real entre dos procesos que consumen CPU?**

- [x] **a.** Que puedan ejecutarse simultáneamente en dos unidades de ejecución disponibles.
- [ ] **b.** Que se alternen en una única CPU.
- [ ] **c.** Que compartan el mismo PID.
- [ ] **d.** Que ambos estén en estado preparado.

**Respuesta correcta:** **a**  
**Explicación:** El paralelismo real exige soporte hardware con múltiples unidades de procesamiento (núcleos o procesadores físicos distintos) ejecutando instrucciones exactamente en el mismo instante temporal.

---

### Pregunta 23
**En un sistema mononúcleo, A se ejecuta y B está preparado. Se produce una interrupción de reloj. ¿Qué afirmación es correcta?**

- [ ] **a.** El núcleo debe ejecutar B inmediatamente.
- [x] **b.** La ISR puede terminar y devolver el control a A sin cambiar de proceso.
- [ ] **c.** La interrupción hace que B pase de preparado a finalizado.
- [ ] **d.** A debe pasar a bloqueado al producirse cualquier interrupción.

**Respuesta correcta:** **b**  
**Explicación:** Una interrupción de reloj no obliga necesariamente a expropiar la CPU; si el proceso A no ha consumido la totalidad de su cuanto de tiempo, la rutina de interrupción finaliza y devuelve el control al proceso A.

---

### Pregunta 24
**En una CPU con un único núcleo y sin SMT, dos procesos A y B están preparados y consumen CPU. ¿Cuál de las siguientes afirmaciones es correcta?**

- [ ] **a.** La concurrencia exige que ambos tengan el mismo espacio de direcciones.
- [ ] **b.** Si A agota su cuanto, B pasa directamente de bloqueado a ejecución.
- [ ] **c.** Ambos pueden ejecutar instrucciones simultáneamente si tienen la misma prioridad.
- [x] **d.** El sistema puede alternar sus ejecuciones, pero no ejecutarlos simultáneamente en ese núcleo.

**Respuesta correcta:** **d**  
**Explicación:** En un único núcleo físico sin multihilo simultáneo (SMT/HyperThreading), solo una instrucción puede ejecutarse a la vez; la concurrencia se logra intercalando el tiempo de procesador entre A y B mediante multiplexación en el tiempo.

---

### Pregunta 25
**En Linux, ¿qué devuelve os.fork() al proceso hijo cuando la llamada tiene éxito?**

- [x] **a.** 0.
- [ ] **b.** El PID del padre.
- [ ] **c.** El PID del hijo.
- [ ] **d.** Siempre -1.

**Respuesta correcta:** **a**  
**Explicación:** En los sistemas POSIX/Linux, `fork()` devuelve `0` al proceso hijo recién creado para que pueda identificarse a sí mismo como tal, mientras que al proceso padre le devuelve el PID del hijo.

---

### Pregunta 26
**Un proceso padre y su hijo, creado mediante fork(), modifican una variable ordinaria que existía antes de la llamada. ¿Qué resultado es posible?**

- [ ] **a.** La modificación del hijo se refleja necesariamente en la variable del padre.
- [ ] **b.** El padre solo puede acceder a la variable después de terminar el hijo.
- [x] **c.** Cada uno modifica su propia copia lógica de la variable.
- [ ] **d.** Ambos comparten siempre el mismo PID mientras existan.

**Respuesta correcta:** **c**  
**Explicación:** Los procesos no comparten memoria por defecto; padre e hijo tienen espacios de memoria virtuales independientes, por lo que cualquier modificación que uno realice sobre variables se produce sobre su propia copia privada.

---

### Pregunta 27
**En Linux, ¿qué devuelve os.fork() al proceso padre cuando tiene éxito?**

- [ ] **a.** El PID del proceso padre.
- [ ] **b.** 0.
- [x] **c.** El PID del proceso hijo.
- [ ] **d.** El número de procesos del sistema.

**Respuesta correcta:** **c**  
**Explicación:** Al proceso padre, `os.fork()` le devuelve el identificador de proceso (PID) del hijo recién creado, lo que le permite controlarlo, sincronizarse o esperar su finalización mediante llamadas como `os.waitpid()`.

---

### Pregunta 28
**La función emitir() no está definida. Suponiendo que ambas llamadas a fork tienen éxito, ¿qué afirmación describe los posibles órdenes en que los procesos alcanzan emitir()?**

```python
import os
os.fork()
os.fork()
emitir()
```

- [ ] **a.** El último hijo creado necesariamente llega el último.
- [ ] **b.** El proceso original debe llegar antes que todos sus hijos.
- [ ] **c.** Los hijos siempre alcanzan emitir() antes que el padre.
- [x] **d.** El planificador puede hacer que distintos procesos lleguen en órdenes diferentes; no hay un orden total garantizado.

**Respuesta correcta:** **d**  
**Explicación:** Una vez creados los procesos, su velocidad y orden de avance dependen enteramente de las decisiones de asignación de CPU del planificador del sistema operativo, produciéndose un comportamiento no determinista.

---

### Pregunta 29
**¿Cuántos procesos alcanzan finalizar(), incluido el original, si todas las llamadas a fork tienen éxito?**

```python
import os
pid=os.fork()
if pid == 0:
   os.fork()
finalizar()
```

- [ ] **a.** 4
- [ ] **b.** 2
- [ ] **c.** 5
- [x] **d.** 3

**Respuesta correcta:** **d**  
**Explicación:** 
1. El primer `os.fork()` crea un proceso hijo (tenemos 2 procesos: padre e hijo 1).
2. En el padre, `pid > 0`, por lo que salta el bloque condicional y llega a `finalizar()` (1 proceso).
3. En el hijo 1, `pid == 0`, entra en el `if` y ejecuta un segundo `os.fork()`, creando al hijo 2.
4. Ambos (hijo 1 e hijo 2) continúan y alcanzan `finalizar()`. En total llegan $1 + 2 = 3$ procesos.

