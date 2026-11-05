# Examen RA2 - Programación Multihilo y Mecanismos de Sincronización

---

### Pregunta 1
**En un productor-consumidor con búfer compartido, un consumidor adquiere primero un Lock del búfer y después espera mediante acquire() sobre un semáforo elementos inicialmente a cero. El productor necesita ese mismo Lock para insertar el primer elemento. ¿Qué riesgo aparece?**

- [x] **a.** Interbloqueo: el consumidor retiene el Lock mientras espera algo que necesita producir quien requiere ese Lock.
- [ ] **b.** El semáforo incrementa automáticamente su contador cuando se adquiere el Lock.
- [ ] **c.** El consumidor consume un elemento inexistente sin bloquearse.
- [ ] **d.** El productor puede entrar en la sección protegida porque el consumidor está bloqueado.

**Respuesta correcta:** **a**  
**Explicación:** Se produce un interbloqueo (*deadlock*): el consumidor retiene el `Lock` del búfer mientras espera que el semáforo le dé paso; por su parte, el productor no puede insertar elementos ni liberar el semáforo porque no puede obtener el `Lock` que retiene el consumidor.

---

### Pregunta 2
**Después de llamar a evento.set(), otro hilo invoca evento.wait() sin que se haya ejecutado clear(). ¿Qué ocurre?**

- [ ] **a.** wait() desactiva el evento antes de retornar.
- [ ] **b.** wait() bloquea hasta un segundo set().
- [x] **c.** wait() retorna sin bloquear por estar el evento activado.
- [ ] **d.** wait() lanza RuntimeError por reutilizar el evento.

**Respuesta correcta:** **c**  
**Explicación:** En `threading.Event`, el indicador interno permanece en estado verdadero (`True`) una vez invocado `set()`. Cualquier hilo que llame a `wait()` mientras el evento permanezca activado continuará de inmediato sin bloquearse, hasta que se invoque explícitamente `clear()`.

---

### Pregunta 3
**En este fragmento, ¿qué afirmación es necesariamente cierta cuando se llama a registrar()?**

```python
import threading

t = threading.Thread(target=tarea)
t.start()
t.join()
registrar()
```

- [ ] **a.** La tarea de t puede seguir ejecutándose.
- [x] **b.** La tarea de t ha terminado.
- [ ] **c.** El hilo principal ha sido sustituido por t.
- [ ] **d.** t no ha empezado a ejecutarse.

**Respuesta correcta:** **b**  
**Explicación:** La llamada `t.join()` sin argumento de tiempo límite bloquea el hilo llamante (el hilo principal) hasta que el hilo `t` finaliza por completo la ejecución de su función objetivo. Por tanto, al llegar a `registrar()`, `t` ha terminado con seguridad.

---

### Pregunta 4
**Se supone que marcar(etiqueta) registra una etiqueta en una lista compartida de forma indivisible. Cada hilo conserva el orden de sus dos llamadas. ¿Qué secuencia es imposible?**

```python
import threading

def a():
    marcar("A1")
    marcar("A2")

def b():
    marcar("B1")
    marcar("B2")

a = threading.Thread(target=a)
a.start()
b = threading.Thread(target=b)
b.start()
```

- [ ] **a.** B1, B2, A1, A2.
- [ ] **b.** B1, A1, B2, A2.
- [ ] **c.** A1, B1, A2, B2.
- [x] **d.** A2, A1, B1, B2.

**Respuesta correcta:** **d**  
**Explicación:** Dentro de un mismo hilo el flujo de instrucciones es secuencial; por tanto, el hilo `a` siempre ejecutará `marcar("A1")` antes que `marcar("A2")`. La secuencia `A2, A1, B1, B2` viola este orden relativo, siendo imposible.

---

### Pregunta 5
**Dos hilos de un mismo proceso modifican una lista global. ¿Qué comparten necesariamente?**

- [ ] **a.** El estado de ejecución y todos los registros.
- [ ] **b.** El contador de programa y la pila.
- [x] **c.** El espacio de direcciones del proceso.
- [ ] **d.** Sus identificadores de hilo.

**Respuesta correcta:** **c**  
**Explicación:** Todos los hilos creados dentro de un mismo proceso comparten el mismo espacio de direcciones virtuales, memoria dinámica (*heap*) y variables globales, aunque cada hilo posee su propia pila (*stack*), registros y contador de programa propios.

---

### Pregunta 6
**Un Event está inicialmente desactivado. Un hilo llama a wait() y otro ejecuta set(). ¿Qué sucede?**

- [x] **a.** set() activa el evento y permite continuar a los hilos que esperan.
- [ ] **b.** wait() reinicia automáticamente el Event.
- [ ] **c.** wait() libera un permiso de semáforo.
- [ ] **d.** set() solo despierta a un hilo.

**Respuesta correcta:** **a**  
**Explicación:** `Event.set()` cambia la bandera interna a activada y notifica/despierta a **todos** los hilos que se encuentren bloqueados en `wait()`, permitiéndoles continuar la ejecución.

---

### Pregunta 7
**Un Semaphore(0) es compartido por un hilo que ejecuta acquire() y otro que ejecuta release(). ¿Qué es correcto?**

- [ ] **a.** release() reduce el contador del semáforo.
- [ ] **b.** El semáforo solo puede utilizarse entre procesos distintos.
- [x] **c.** acquire() espera hasta que exista un permiso disponible.
- [ ] **d.** acquire() puede pasar sin esperar aunque no haya liberaciones.

**Respuesta correcta:** **c**  
**Explicación:** Un semáforo con contador inicial en 0 no tiene permisos disponibles. Cualquier hilo que invoque `acquire()` quedará bloqueado hasta que otro hilo ejecute `release()` e incremente el contador.

---

### Pregunta 8
**Un Semaphore(2) recibe dos acquire() completados, sin release(). Un tercer hilo llama a acquire(). ¿Qué sucede?**

- [x] **a.** Queda bloqueado hasta que haya un release().
- [ ] **b.** El contador pasa a -1 y el hilo continúa.
- [ ] **c.** Se crea automáticamente otro semáforo.
- [ ] **d.** Obtiene el permiso porque el contador es inicialmente 2.

**Respuesta correcta:** **a**  
**Explicación:** Tras dos llamadas exitosas a `acquire()`, el contador interno disminuye de 2 a 0. Al intentar una tercera adquisición sin permisos disponibles, el hilo queda suspendido a la espera de un `release()`.

---

### Pregunta 9
**Dos hilos ejecutan una sección crítica protegida por el mismo Lock. ¿Qué se garantiza?**

- [ ] **a.** El Lock impide cualquier cambio de contexto.
- [x] **b.** Como máximo uno está dentro de esa sección protegida a la vez.
- [ ] **c.** El hilo que lleva más tiempo esperando adquiere el Lock primero.
- [ ] **d.** Los hilos alternan estrictamente una entrada cada uno.

**Respuesta correcta:** **b**  
**Explicación:** Un cerrojo (`Lock`) garantiza el principio de exclusión mutua: únicamente un hilo puede poseer el cerrojo en un instante dado, impidiendo accesos concurrentes a la sección crítica, sin garantizar orden ni equidad (*fairness*).

---

### Pregunta 10
**En el código, cada hilo modifica una variable local x y llama a registrar(x). ¿Qué afirmación es correcta?**

```python
import threading

def tarea():
    x = 0
    x += 1
    registrar(x)

a = threading.Thread(target=tarea)
b = threading.Thread(target=tarea)
a.start()
b.start()
a.join()
b.join()
```

- [ ] **a.** join() convierte x en una variable compartida.
- [ ] **b.** Los dos hilos comparten obligatoriamente la misma variable local x.
- [ ] **c.** x se almacena siempre en una variable global.
- [x] **d.** Cada invocación de tarea dispone de su propia variable local x.

**Respuesta correcta:** **d**  
**Explicación:** Las variables locales definidas dentro de una función se asignan en la pila (*stack*) de ejecución correspondiente a cada hilo. Cada hilo posee su propia pila, por lo que cada llamada a `tarea()` tiene una variable `x` independiente.

---

### Pregunta 11
**Dos hilos ejecutan cada uno una sola vez incrementar(), que lee total, suma 1 y escribe total sin sincronización. total comienza en 0. ¿Qué valores finales son posibles para la variable total?**

```python
import threading

total = 0

def incrementar():
    global total
    suma = total
    suma += 1
    total = suma

for i in range(2):
    t = threading.Thread(target=incrementar)
    t.start()
```

- [ ] **a.** Solo 0.
- [ ] **b.** 0 o 2.
- [x] **c.** 1 o 2.
- [ ] **d.** Solo 2.

**Respuesta correcta:** **c**  
**Explicación:** Debido a la condición de carrera (*race condition*), si ambos hilos leen `total = 0` concurrentemente antes de que el otro escriba, ambos calcularán `suma = 1` y escribirán `total = 1` (pérdida de actualización). Si se ejecutan secuencialmente sin solape, el valor final será `2`. Por tanto, los valores posibles son `1` o `2`.

---

### Pregunta 12
**El hilo principal crea un daemon y ejecuta join() sin tiempo límite sobre él. ¿Qué afirmación es correcta?**

- [ ] **a.** join() provoca necesariamente una excepción.
- [ ] **b.** join() no espera a los hilos daemon.
- [x] **c.** join() espera a que ese hilo termine, aunque sea daemon.
- [ ] **d.** join() convierte el daemon en no daemon.

**Respuesta correcta:** **c**  
**Explicación:** Los hilos *daemon* finalizan abruptamente si todos los hilos no-*daemon* acaban. Sin embargo, si un hilo invoca explícitamente `join()` sobre un hilo *daemon*, el hilo llamante esperará hasta la terminación de este con normalidad.

---

### Pregunta 13
**Un hilo daemon está escribiendo datos y termina el hilo principal, sin otros hilos no daemon. ¿Cuál es el riesgo?**

- [ ] **a.** El daemon pasa a ejecutarse en otro proceso.
- [x] **b.** La escritura puede quedar incompleta al finalizar el intérprete.
- [ ] **c.** La escritura queda garantizada por el GIL.
- [ ] **d.** El daemon se reinicia automáticamente.

**Respuesta correcta:** **b**  
**Explicación:** Cuando el último hilo no-*daemon* finaliza, el proceso de Python termina de forma inmediata sin esperar a los hilos *daemon*. Cualquier operación en curso (como escritura de ficheros o red) se interrumpirá bruscamente pudiendo corromper datos.

---

### Pregunta 14
**Un Semaphore(3) protege una sección de trabajo. ¿Cuántos hilos como máximo pueden encontrarse simultáneamente dentro, suponiendo que nadie modifica el semáforo fuera de esas secciones?**

- [ ] **a.** No hay límite.
- [ ] **b.** 1.
- [x] **c.** 3.
- [ ] **d.** 2.

**Respuesta correcta:** **c**  
**Explicación:** El contador inicial de un semáforo representa el número de permisos disponibles. Un valor inicial de 3 permite hasta 3 llamadas a `acquire()` simultáneas antes de bloquearse.

---

### Pregunta 15
**Un hilo daemon permanece ejecutándose cuando finaliza el último hilo no daemon. ¿Qué es correcto?**

- [ ] **a.** El daemon termina su tarea y después finaliza el proceso.
- [x] **b.** El intérprete puede finalizar sin esperar a que el daemon termine.
- [ ] **c.** El daemon se convierte automáticamente en no daemon.
- [ ] **d.** El intérprete espera siempre a que termine el daemon.

**Respuesta correcta:** **b**  
**Explicación:** Por definición en Python y la mayoría de entornos de hilos, los hilos marcados como *daemon* son tareas de segundo plano que no impiden que el intérprete de Python finalice cuando ya no quedan hilos principales activos.

---

### Pregunta 16
**Un hilo adquiere un Lock y nunca lo libera. Otro hilo intenta adquirir ese mismo Lock. ¿Qué es posible?**

- [ ] **a.** El segundo hilo adquiere el Lock al agotarse su quantum.
- [ ] **b.** El segundo hilo entra en la sección crítica sin adquirirlo.
- [x] **c.** El segundo hilo permanece bloqueado indefinidamente si no existe otra vía de liberación.
- [ ] **d.** El planificador libera automáticamente el Lock al cambiar de hilo.

**Respuesta correcta:** **c**  
**Explicación:** El sistema operativo o el runtime no libera automáticamente los bloqueos a nivel de aplicación cuando hay cambios de contexto. Si el poseedor del `Lock` no ejecuta `release()`, cualquier otro hilo que intente adquirirlo quedará bloqueado indefinidamente (*deadlock* / inanición).

---

### Pregunta 17
**Se crean dos hilos y se invoca start() sobre ambos antes de hacer join(). ¿Qué garantiza el programa?**

```python
import threading

a = threading.Thread(target=tarea_a)
b = threading.Thread(target=tarea_b)
a.start()
b.start()
a.join()
b.join()
registrar()
```

- [ ] **a.** Que B termine antes que A.
- [x] **b.** Que ambos terminen antes de registrar(), pero no su orden de finalización.
- [ ] **c.** Que ambos ejecuten simultáneamente cada instrucción.
- [ ] **d.** Que A termine antes de empezar B.

**Respuesta correcta:** **b**  
**Explicación:** Al llamar a `a.join()` y `b.join()`, el hilo principal asegura que tanto `a` como `b` habrán concluido antes de llegar a `registrar()`. Sin embargo, el orden relativo en que `a` y `b` finalizan entre sí es indeterminado.

---

### Pregunta 18
**Dos hilos ejecutan la misma función sobre una variable compartida contador, inicialmente igual a 0.**

```python
import threading

contador = 0

def tarea():
    global contador
    auxiliar = contador
    procesar()
    contador = auxiliar + 1

h1 = threading.Thread(target=tarea)
h2 = threading.Thread(target=tarea)

h1.start()
h2.start()

h1.join()
h2.join()
```

*La función procesar() no está definida y no modifica ninguna variable. Su única finalidad es representar una operación durante la cual el planificador puede suspender el hilo en ejecución.*

**¿Qué valores puede tener contador cuando finalizan ambos hilos?**

- [ ] **a.** Únicamente 2.
- [ ] **b.** 0, 1 ó 2, dependiendo del entrelazado de ejecución.
- [x] **c.** 1 ó 2, dependiendo del entrelazado de ejecución.
- [ ] **d.** Únicamente 1.

**Respuesta correcta:** **c**  
**Explicación:** Si un hilo lee `contador = 0` y es suspendido durante `procesar()`, el otro hilo puede ejecutarse completo dejando `contador = 1`. Cuando el primer hilo reanuda, usa su `auxiliar = 0` y asigna `contador = 1` (perdiendo el incremento del otro). Si no hay solapamiento destructivo, el resultado es `2`. Como cada hilo al menos suma 1 sobre lo que leyó, nunca puede quedar en 0.

---

### Pregunta 19
**Dos hilos incrementan total dentro de la región crítica y cada uno lo hace cinco veces. Se espera a ambos con join(). ¿Qué valor final tendrá total, cuyo valor inicial es 0?**

```python
import threading

candado = threading.Lock()
total = 0

def tarea():
    global total, candado
    for _ in range(5):
       candado.acquire()        
       total += 1
       candado.release()

a = threading.Thread(target=tarea)
b = threading.Thread(target=tarea)
a.start()
b.start()
a.join()
b.join()
registrar(total)
```

- [ ] **a.** Depende del orden del planificador y puede ser 5 o 10.
- [ ] **b.** Puede ser cualquier valor de 0 a 10.
- [x] **c.** Siempre 10.
- [ ] **d.** Siempre 5.

**Respuesta correcta:** **c**  
**Explicación:** La modificación de la variable `total` está debidamente sincronizada mediante un `Lock`, garantizando exclusión mutua en cada uno de los $5 + 5 = 10$ incrementos. No hay condiciones de carrera y ambos hilos son esperados con `join()`, por lo que el resultado es determinista: exactamente `10`.

---

### Pregunta 20
**¿Cuál es la diferencia esencial entre invocar tarea() y crear un Thread(target=tarea) seguido de start()?**

- [ ] **a.** start() ejecuta tarea antes de retornar obligatoriamente.
- [ ] **b.** La llamada directa crea un hilo daemon.
- [x] **c.** La llamada directa ejecuta tarea en el hilo llamante; start() inicia otro hilo.
- [ ] **d.** Ambas opciones crean un nuevo hilo.

**Respuesta correcta:** **c**  
**Explicación:** Llamar a `tarea()` es una simple invocación síncrona de función que se ejecuta en el mismo hilo que realiza la llamada (bloqueando su avance). En cambio, `Thread.start()` crea y pone en marcha un nuevo hilo de ejecución gestionado por el sistema operativo.

---

### Pregunta 21
**En este código, ¿cuál es la relación garantizada entre registrar("B") y registrar("A")?**

```python
import threading

evento = threading.Event()

def a():
    evento.wait()
    registrar("A")

def b():
    registrar("B")
    evento.set()

a = threading.Thread(target=a)
a.start()
b = threading.Thread(target=b)
b.start()
```

- [x] **a.** B se registra antes que A.
- [ ] **b.** Se registran necesariamente a la vez.
- [ ] **c.** A se registra antes que B.
- [ ] **d.** Su orden depende únicamente de qué hilo se creó primero.

**Respuesta correcta:** **a**  
**Explicación:** El hilo `a` se bloquea inmediatamente en `evento.wait()`. No puede continuar hasta que el hilo `b` ejecute `evento.set()`, lo cual sucede estrictamente **después** de llamar a `registrar("B")`. Por consiguiente, `registrar("B")` siempre ocurre antes que `registrar("A")`.

---

### Pregunta 22
**¿Qué sucede al llamar a start() sobre un objeto Thread creado pero aún no iniciado?**

- [ ] **a.** La función objetivo se ejecuta en el hilo que llamó a start().
- [x] **b.** Se solicita el inicio de la ejecución de su función objetivo en otro hilo.
- [ ] **c.** Se crea otro proceso con memoria independiente.
- [ ] **d.** La llamada espera a que termine la función objetivo.

**Respuesta correcta:** **b**  
**Explicación:** El método `start()` solicita al sistema operativo y al runtime la creación del hilo nativo y arranca la ejecución del método `run()` (o la función asignada en `target`) de forma asíncrona y concurrente.

---

### Pregunta 23
**En el código, ¿qué riesgo presenta la función tarea() si el hilo llega a la segunda adquisición?**

```python
import threading

candado = threading.Lock()

def tarea():
    candado.acquire()
    candado.acquire()
    registrar()
    candado.release()
    candado.release()
```

- [ ] **a.** La segunda adquisición convierte Lock en Semaphore.
- [x] **b.** El hilo puede bloquearse esperando un bloqueo que él mismo mantiene.
- [ ] **c.** La segunda adquisición libera automáticamente el bloqueo.
- [ ] **d.** La segunda adquisición permite continuar porque ya ha sido adquirido el cerrojo por el mismo hilo.

**Respuesta correcta:** **b**  
**Explicación:** En Python, `threading.Lock` es un cerrojo **no reentrante**. Si el mismo hilo intenta adquirirlo por segunda vez sin haberlo liberado antes, se bloquea a sí mismo indefinidamente esperando su propia liberación (*self-deadlock*). Para permitir adquisiciones recursivas se requeriría un `RLock`.

---

### Pregunta 24
**¿Qué ocurre si se invoca start() dos veces sobre el mismo objeto Thread?**

- [x] **a.** La segunda llamada produce RuntimeError.
- [ ] **b.** La segunda llamada se ignora.
- [ ] **c.** La segunda llamada reinicia la tarea.
- [ ] **d.** Se crean dos hilos independientes.

**Respuesta correcta:** **a**  
**Explicación:** El ciclo de vida de un objeto `Thread` en Python solo permite ser iniciado una única vez. Intentar llamar a `start()` más de una vez sobre la misma instancia lanza una excepción `RuntimeError`.

---

### Pregunta 25
**Un hilo A espera una operación de entrada/salida mientras B está preparado. ¿Qué puede ocurrir?**

- [ ] **a.** B debe esperar a que A termine.
- [ ] **b.** El bloqueo de A bloquea obligatoriamente el proceso completo.
- [ ] **c.** A conserva la CPU hasta que finaliza la entrada/salida.
- [x] **d.** B puede ejecutarse mientras A permanece bloqueado.

**Respuesta correcta:** **d**  
**Explicación:** Uno de los mayores beneficios de los hilos es que, al realizar un hilo una operación de E/S bloqueante, el sistema operativo (y en Python liberando el GIL durante llamadas de E/S del sistema) suspende al hilo A y cede el procesador a otros hilos preparados como B.

