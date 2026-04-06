#!/bin/bash
# setup-volumes.sh

# Create directory structure
mkdir -p volumes/{kafka-data,prometheus-data,grafana-data,traffic-state,sms-state,call-state,internet-state,mobility-state}
mkdir -p monitoring

# Download JMX exporter (if not already present)
if [ ! -f monitoring/jmx_prometheus_javaagent-1.0.1.jar ]; then
  echo "Downloading JMX exporter..."
  wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/1.0.1/jmx_prometheus_javaagent-1.0.1.jar \
    -O monitoring/jmx_prometheus_javaagent-1.0.1.jar
fi

# Fix Kafka permissions (critical for Linux hosts)
# Kafka inside container runs as user UID 1000 (confluent)
sudo chown -R 1000:1000 volumes/kafka-data
chmod -R 755 volumes/kafka-data

# Prometheus runs as nobody (UID/GID 65534) in the official image
sudo chown -R 65534:65534 volumes/prometheus-data
chmod -R 775 volumes/prometheus-data

# Grafana runs as the host user in rootless Docker
sudo chown -R $(id -u):$(id -g) volumes/grafana-data

# Ensure Kafka Streams state directories are writable by the host user (since Spring Boot apps run as root in simple setups)
sudo chmod -R 777 volumes/*-state

echo "✓ Directories created and permissions fixed"
echo "✓ Kafka data:   $(pwd)/volumes/kafka-data"
echo "✓ Prometheus:   $(pwd)/volumes/prometheus-data"
echo "✓ Grafana:      $(pwd)/volumes/grafana-data"
echo "✓ Streams state: $(pwd)/volumes/*-state"
