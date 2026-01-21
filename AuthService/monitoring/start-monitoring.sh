cd monitoring
docker-compose up -d
echo "Мониторинг запущен:"
echo "   Grafana:      http://localhost:3001 (admin/adminadmin)"
echo "   Prometheus:   http://localhost:9091"
echo "   Loki:         http://localhost:3101"