# Kubernetes (kind) for WP-09

```bash
kind create cluster --name jvm-lab --config platform/k8s/kind-config.yaml
./gradlew :reference-service:order-service:bootJar
docker build -f platform/docker/spring-boot.Dockerfile -t lab/order-service:0.1.0 reference-service/order-service
kind load docker-image lab/order-service:0.1.0 --name jvm-lab
kubectl apply -k platform/k8s/base
curl localhost:30080/actuator/health
```

`overlays/ci` is what the CI/CD pipeline deploys (image from the kind registry, telemetry off, 1 replica).

## Measurement in the cluster (WP-09 setup step)

Pod-level resource signals are what the WP-09 labs measure. None of them exist in a bare kind cluster:

| Signal | Why it matters | Source |
|---|---|---|
| `container_cpu_cfs_throttled_periods_total` / `_periods_total` | CPU throttling raises p99 while CPU usage looks below the limit | cAdvisor (kubelet) |
| `container_memory_working_set_bytes` vs limit | How close the pod is to OOMKilled (heap + non-heap + native) | cAdvisor |
| `kube_pod_container_status_last_terminated_reason` | Distinguish OOMKilled from JVM OutOfMemoryError | kube-state-metrics |
| `kubectl top pod` | Quick check during experiments | metrics-server |

Install them when you start WP-09 (versions to pin in ADR-000 at that point):

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace \
  --set grafana.enabled=false --set alertmanager.enabled=false
# kind needs metrics-server with --kubelet-insecure-tls for `kubectl top`
```

Then either point the lab Grafana at that Prometheus or remote-write it into the lab Prometheus, and add the
throttling and memory panels to the dashboard.
