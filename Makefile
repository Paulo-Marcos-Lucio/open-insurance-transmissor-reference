.PHONY: help up down logs build test it clean image ps run load certs

MVN := ./mvnw -B -ntp

help:
	@echo "Targets:"
	@echo "  up         - sobe stack de observabilidade (otel + prometheus + tempo + loki + grafana)"
	@echo "  down       - derruba a stack (preserva volumes)"
	@echo "  logs       - tail dos logs da stack"
	@echo "  build      - mvn clean package -DskipTests"
	@echo "  test       - mvn test (unit + ArchUnit)"
	@echo "  it         - mvn verify (unit + integration)"
	@echo "  run        - roda app no profile local (sem mTLS, seed populando 50 apólices)"
	@echo "  load       - dispara tráfego sintético GET /policies pra alimentar dashboards"
	@echo "  image      - build da imagem OCI via Spring Boot Buildpacks"
	@echo "  certs      - gera trust store + key store de teste pra mTLS local"
	@echo "  clean      - mvn clean + derruba volumes"

up:
	docker compose up -d
	@echo "Grafana: http://localhost:3000 | Swagger: http://localhost:8083/swagger-ui.html"

down:
	docker compose down

logs:
	docker compose logs -f --tail 200

build:
	$(MVN) clean package -DskipTests

test:
	$(MVN) test

it:
	$(MVN) verify

run:
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=local

image:
	$(MVN) spring-boot:build-image -DskipTests

load:
	@./scripts/load.sh

ps:
	docker compose ps

certs:
	@./scripts/generate-test-certs.sh

clean:
	$(MVN) clean
	docker compose down -v
