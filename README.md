# Pix Service - Sistema de Transferências PIX

Sistema de gerenciamento de carteiras digitais e transferências PIX construído com Spring Boot e MongoDB.

## 📋 Índice

- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Executando o Projeto](#executando-o-projeto)
- [Testando](#testando)
- [Arquitetura](#arquitetura)
- [Endpoints da API](#endpoints-da-api)
- [Decisões de Design](#decisões-de-design)
- [Trade-offs e Limitações](#trade-offs-e-limitações)

## 🔧 Requisitos

- **Java 21** ou superior
- **Docker** e **Docker Compose**
- **Gradle 8.x** (wrapper incluído)

## 📦 Instalação

### 1. Clone o repositório

```bash
git clone <repository-url>
cd pixservice
```

### 2. Inicie os serviços (MongoDB e RabbitMQ) via Docker Compose

```bash
docker compose up -d
```

Isso iniciará:
- MongoDB na porta `27017` com as credenciais:
- Usuário: `root`
- Senha: `root`
- Database: `pixservicedb`

- RabbitMQ na porta `5672` e console de gerenciamento em `http://localhost:15672`
  - Usuário: `admin`
  - Senha: `admin`

### 3. Build do projeto

```bash
./gradlew clean build
# Windows (PowerShell)
./gradlew.bat clean build
```

## 🚀 Executando o Projeto

### Modo desenvolvimento

```bash
./gradlew :main:bootRun
# Windows (PowerShell)
./gradlew.bat :main:bootRun
```

A aplicação estará disponível em: `http://localhost:8080`

### Modo produção

```bash
./gradlew :main:bootJar
# Windows (PowerShell)
./gradlew.bat :main:bootJar

java -jar main/build/libs/main-0.0.1-SNAPSHOT.jar
```

### Verificando a saúde da aplicação

```bash
curl http://localhost:8080/actuator/health
```

## 🧪 Testando

### Executar todos os testes

```bash
./gradlew test
# Windows (PowerShell)
./gradlew.bat test
```

### Executar testes com relatório de cobertura

```bash
./gradlew test jacocoTestReport
# Windows (PowerShell)
./gradlew.bat test jacocoTestReport
```

O relatório HTML estará disponível em: `build/reports/jacoco/test/html/index.html`

### Executar testes de um módulo específico

```bash
./gradlew :application:test
./gradlew :infrastructure:test
# Windows (PowerShell)
./gradlew.bat :application:test
./gradlew.bat :infrastructure:test
```

## 🌐 Documentação e Observabilidade

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Actuator Health: `http://localhost:8080/actuator/health`
- Actuator Prometheus: `http://localhost:8080/actuator/prometheus`
- RabbitMQ Console: `http://localhost:15672` (admin/admin)

Se estiver usando as credenciais do RabbitMQ do Docker Compose (`admin/admin`), defina as variáveis de ambiente antes de iniciar a aplicação:

```bash
set RABBITMQ_USER=admin && set RABBITMQ_PASS=admin   # Windows (PowerShell)
export RABBITMQ_USER=admin RABBITMQ_PASS=admin       # Linux/macOS
```

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas baseada em **Clean Architecture**:

```
pixservice/
├── domain/           # Entidades, interfaces de repositórios, regras de negócio
├── application/      # Casos de uso (use cases)
├── infrastructure/   # Implementações concretas (BD, REST, configurações)
└── main/            # Ponto de entrada da aplicação
```

### Módulos

- **domain**: Núcleo da aplicação, livre de frameworks
- **application**: Orquestração de casos de uso
- **infrastructure**: Detalhes técnicos (MongoDB, REST controllers, RabbitMQ, OpenAPI)
- **main**: Configuração Spring Boot e inicialização

## 📡 Endpoints da API

### Wallets

#### Criar Carteira
```http
POST /wallets
Content-Type: application/json

{
  "ownerName": "João Silva",
  "documentNumber": "12345678909"
}
```

#### Consultar Saldo
```http
GET /wallets/{id}/balance
GET /wallets/{id}/balance?at=2025-01-15T10:00:00Z
```

#### Depositar
```http
POST /wallets/{id}/deposit
Content-Type: application/json

{
  "amount": 100.00,
  "source": "Transferência bancária"
}
```

#### Sacar
```http
POST /wallets/{id}/withdraw
Content-Type: application/json

{
  "amount": 50.00,
  "reason": "Saque ATM"
}
```

#### Registrar Chave PIX
```http
POST /wallets/{id}/pix-keys
Content-Type: application/json

{
  "keyType": "CPF",
  "keyValue": "12345678909"
}
```

### PIX

#### Transferência PIX
```http
POST /pix/transfers
Idempotency-Key: unique-request-id-123
Content-Type: application/json

{
  "fromWalletId": "wallet-id-123",
  "toPixKey": "98765432100",
  "amount": 250.00
}
```

#### Webhook (Simulação de confirmação/rejeição)
```http
POST /pix/webhook
Content-Type: application/json

{
  "endToEndId": "e2e-id-456",
  "eventId": "event-789",
  "eventType": "CONFIRMED",
  "occurredAt": "2025-10-19T14:30:00Z"
}
```

Obs.: Alguns endpoints podem ser exibidos e testados via Swagger UI.

## 🎯 Decisões de Design

### 1. Arquitetura em Camadas (Clean Architecture)

**Motivação**: Separar regras de negócio de detalhes de implementação, facilitando manutenção e testes.

**Benefícios**:
- Testabilidade: Domain layer independente de frameworks
- Flexibilidade: Troca de tecnologias sem afetar lógica de negócio
- Manutenibilidade: Responsabilidades bem definidas

### 2. Idempotência via PixRecordService

**Motivação**: Garantir que requisições duplicadas (por falha de rede, retry) não causem efeitos colaterais.

**Implementação**:
- Cache de resultados por `scope + key`
- Lock por chave para evitar race conditions
- Persistência assíncrona em MongoDB

**Trade-off**: Aumento de complexidade vs. robustez em cenários distribuídos.

### 3. Controle de Concorrência Otimista (Versioning)

**Motivação**: Evitar lost updates em operações concorrentes no saldo da carteira.

**Implementação**:
- Campo `version` na entidade `Wallet`
- Update condicional via query: `WHERE version = expectedVersion`
- Incremento automático de versão

**Benefício**: Performance superior ao lock pessimista em cenários de baixa contenção.

### 4. Ledger Imutável

**Motivação**: Auditoria completa de todas as transações financeiras.

**Implementação**:
- Todas as movimentações registradas em `ledger_entries`
- Apenas INSERT, nunca UPDATE/DELETE
- Saldo histórico calculável via agregação

**Benefício**: Rastreabilidade e conformidade regulatória.

### 5. Simulação Assíncrona do BACEN

**Motivação**: Simular o fluxo real onde a confirmação PIX vem via webhook.

**Implementação**:
- Thread separada com delay aleatório (2-5s)
- 90% de sucesso, 10% de rejeição
- Estado inicial: `PENDING` → `CONFIRMED/REJECTED`

## ⚖️ Trade-offs e Limitações

### Limitações de Tempo

Devido ao prazo limitado, as seguintes decisões foram tomadas:

#### 1. **Simulação do BACEN Simplificada**

**Decisão**: Thread.sleep() dentro do use case ao invés de sistema de mensageria.

**Trade-off**:
- ✅ Implementação rápida
- ❌ Não é escalável (bloqueia threads)
- ❌ Difícil de testar

**Melhoria futura**:
- RabbitMQ/Kafka para processamento assíncrono
- Dead Letter Queue para falhas
- Retry exponencial com backoff

#### 2. **Lock por WalletId sem Cleanup Automático Robusto**

**Decisão**: ConcurrentHashMap de locks com cleanup básico.

**Trade-off**:
- ✅ Sincronização efetiva
- ⚠️ Potencial memory leak em sistemas com muitas carteiras

**Melhoria futura**:
- Guava Cache com eviction policy
- Redis Distributed Lock (RedLock)
- Weak References para GC automático

#### 3. **Ausência de Transações Distribuídas**

**Decisão**: Transações locais do MongoDB.

**Trade-off**:
- ✅ Simplicidade de implementação
- ❌ Sem garantia ACID entre múltiplos serviços

**Melhoria futura**:
- Saga Pattern (Choreography ou Orchestration)
- Outbox Pattern para consistência eventual
- Event Sourcing



#### 4. **Ausência de Validações Avançadas**

**Decisão**: Validações básicas via Bean Validation.

**Limitações**:
- CPF/CNPJ: apenas validação de tamanho, sem dígito verificador
- Chaves PIX: sem validação de formato específico
- Limites de transferência: não implementados

**Melhoria futura**:
- Biblioteca de validação de documentos brasileiros
- Regras de negócio por tipo de chave PIX
- Rate limiting e limites transacionais

#### 5. **Testes de Integração Ausentes**

**Decisão**: Apenas testes unitários com mocks.

**Trade-off**:
- ✅ Feedback rápido
- ❌ Não valida integrações reais

**Melhoria futura**:
- Testcontainers para MongoDB
- Testes E2E com RestAssured
- Contract Testing

#### 6. **Observabilidade Básica**

**Decisão**: Apenas logs e Actuator health.

**Limitações**:
- Sem métricas customizadas (Micrometer)
- Sem distributed tracing (Sleuth/Zipkin)
- Sem alertas

**Melhoria futura**:
- Prometheus + Grafana
- ELK Stack
- APM (Application Performance Monitoring)

### Justificativas de Priorização

**Foco em**:
1. ✅ Fluxo completo de transferência PIX
2. ✅ Controle de concorrência
3. ✅ Idempotência
4. ✅ Estrutura arquitetural sólida

**Deixado para depois**:
1. ⏳ Mensageria assíncrona real
2. ⏳ Validações avançadas
3. ⏳ Testes de integração
4. ⏳ Observabilidade completa

## 📚 Tecnologias Utilizadas

- **Spring Boot 3.5.6**
- **Spring Web, Validation, Cache, Actuator**
- **Spring Data MongoDB**
- **RabbitMQ (Spring AMQP)**
- **Springdoc OpenAPI**
- **Micrometer + Prometheus**
- **MongoDB 7.x**
- **Lombok**
- **JUnit 5 / Mockito**
- **Gradle 8.x**

Notas adicionais:
-

## 📄 Licença

Este projeto é um desafio técnico e não possui licença específica.

## 👤 Autor

Matheus L. Barbosa

---