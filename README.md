Plan of file

## 1. Project Description

Proof of concept for a sensor tracking system.

- **Sensors:** two sensors (temperature and humidity) send UDP datagrams in the format `sensor_id,value` (e.g. `t1,30`).
- **warehouse-service:** reactively receives signals from all registered sensors and publishes the measurements to a message broker.
- **central-monitoring-service:** subscribes to the message broker and checks whether the limit for a given sensor is exceeded. If it is, it prints an `ALERT` log message.

### Solution Assumptions

- **Plain Java:** no dependency injection framework.
- **Intentional complexity:** some constructs are more elaborate than necessary, to demonstrate the approach in this POC.
- **No authentication:** sensors are only checked for registration; they do not authenticate.
- **No encryption:** signals are sent in plain text.
- **At-most-once delivery:** loss of signals is acceptable (guaranteed delivery would require NATS JetStream).
- **Bounded queue:** when the publish queue is full, old messages are dropped.
- **NATS:** scaling and availability are out of scope; NATS is assumed to be always available.
- **Load balancing:** there is no feedback from the load balancer; simple round-robin is used.
- **Alerting:** monitoring and alerts are simplified to a log message for demonstration purposes.
- **Tests:** coverage is limited to basic scenarios for demonstration purposes.



## 2. Core Technologies

- **Java 21** – language and runtime for both services.
- **Reactor Netty** – non-blocking UDP server in `warehouse-service` that receives sensor datagrams reactively.
- **Docker / Docker Compose** – builds and runs all services together (`docker compose up --build`).
- **Maven** – multi-module build for `warehouse-service` and `central-monitoring-service`.
- **JUnit** – unit tests (UDP server, alert monitor).
- **NATS** – message broker: `warehouse-service` publishes measurements, `central-monitoring-service` subscribes to them.
- **Nginx** – UDP gateway on ports 3344 (humidity) and 3355 (temperature) that round-robins datagrams across `warehouse-service` replicas.

## 3. How to Run

1. Start all services from the project root:

   ```bash
   docker compose up --build
   ```

2. Send a UDP datagram:

   - **PowerShell:**

     ```powershell
     echo "t1,20" | ncat -u -w 1 127.0.0.1 3355
     ```

   - **macOS / Linux:**

     ```bash
     echo "t1,20" | nc -u -w 1 127.0.0.1 3355
     ```

3. View current measurements at <http://localhost:8080/measurements>.

### Alert limits

Limits are set in `.env` (defaults shown):

| Sensor      | Variable            | Default |
|-------------|---------------------|---------|
| Temperature | `TEMPERATURE_LIMIT` | 35      |
| Humidity    | `HUMIDITY_LIMIT`    | 50      |

To trigger an alert, send a value above the limit and check the `central-monitoring-service` logs for an `ALERT` message.

## 4. warehouse-service High Availability

`warehouse-service` can run as several instances. Set the number of instances in `.env`:

```env
INSTANCES=3
```

The Nginx gateway finds the instances through Docker's built-in DNS and distributes incoming UDP datagrams between them using round-robin. Instances that are added or recreated are picked up automatically, without reloading Nginx. 


