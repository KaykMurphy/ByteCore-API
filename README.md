# ByteCore API

> **⚠️ PROJETO EM DESENVOLVIMENTO ATIVO**
>
> Este projeto está em construção. Algumas funcionalidades podem estar incompletas, sujeitas a mudanças ou ainda não totalmente testadas. A cobertura de testes está sendo expandida progressivamente.

API REST para marketplace de produtos digitais com entrega automática, sistema de verificação de vendedores e integração com Mercado Pago (PIX).

> Segunda versão da [bytemarket](https://github.com/KaykMurphy/bytemarket) — reescrita do zero com arquitetura mais sólida, boas práticas e maior facilidade de manutenção.

---

## Stack

- **Java 17** + **Spring Boot 3.4**
- **Spring Security** + **JWT** (jjwt 0.12)
- **Spring Data JPA** + **PostgreSQL** (H2 para testes)
- **MapStruct** + **Lombok**
- **Mercado Pago SDK**
- **JavaMailSender** + **Thymeleaf** (e-mails HTML)
- **SpringDoc OpenAPI** (Swagger UI)

---

## Funcionalidades

| Módulo | Descrição | Status |
|---|---|---|
| Auth | Registro, login com JWT, endpoint `/me` | ✅ Implementado |
| Produtos | CRUD público (paginado + busca), aprovação por admin | ✅ Implementado |
| Pedidos | Criação com validação de estoque e status do vendedor | ✅ Implementado |
| Pagamentos | PIX via Mercado Pago, webhook com validação de assinatura HMAC-SHA256 | ✅ Implementado |
| Entrega digital | Entrega automática de keys/contas por e-mail após confirmação do pagamento | ✅ Implementado |
| Verificação de vendedor | Fluxo de submissão → análise → aprovação/rejeição/ban | ✅ Implementado |
| Saques | Solicitação via PIX com limite diário de R$ 500 | ✅ Implementado |
| Reviews | Avaliação de vendedores pós-entrega (máx. 2 edições) | ✅ Implementado |
| Blacklist | Documentos banidos por hash SHA-256 | ✅ Implementado |
| Jobs | Liberação automática de saldo (cron diário às 03h) | ✅ Implementado |

---

## Segurança

- Autenticação stateless com JWT
- Roles: `USER`, `PENDING_SELLER`, `VERIFIED_SELLER`, `ADMIN`
- Validação de assinatura HMAC-SHA256 nos webhooks do Mercado Pago
- Controle de concorrência com `@Version` (Optimistic Lock) no estoque
- Hash SHA-256 em documentos para blacklist

---

## Endpoints principais

```
POST   /auth/register
POST   /auth/login
GET    /auth/me

GET    /api/products          (público, paginado)
GET    /api/products/{id}     (público)
GET    /api/products/search   (público)

POST   /api/orders
POST   /api/payments/pix/{orderId}

POST   /seller/verifications
GET    /seller/verifications/me

POST   /api/reviews
PUT    /api/reviews/{id}

POST   /api/withdrawals
GET    /api/withdrawals/me
GET    /api/withdrawals/balance

POST   /admin/products
PUT    /admin/products/{id}
DELETE /admin/products/{id}
GET    /admin/verifications/pending
POST   /admin/verifications/{id}/approve
POST   /admin/verifications/{id}/reject
POST   /admin/verifications/{id}/ban

POST   /api/webhooks/mercadopago
```

Documentação interativa disponível em `/swagger-ui.html`.

---

## Configuração

As variáveis abaixo devem estar definidas no `application.properties` ou como variáveis de ambiente:

```properties
# Banco de dados
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

# JWT
jwt.secret=           # Base64 encoded, mínimo 256 bits
jwt.expirationMs=

# Mercado Pago
mercadopago.access_token=
mercadopago.webhook.secret=

# E-mail
spring.mail.host=
spring.mail.port=
spring.mail.username=
spring.mail.password=
bytemarket.email.from=

# Admin seed
admin_email=
admin_password=

# App
app.base-url=
app.cors.allowed-origins=
```

---

## Rodando localmente

```bash
# Clone
git clone https://github.com/KaykMurphy/bytemarket-v2
cd bytemarket-v2

# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

Por padrão usa H2 em memória. Para PostgreSQL, configure o `application.properties` adequadamente.

---

## Testes

```bash
./mvnw test
```

> **🚧 Em expansão** — A cobertura de testes está sendo incrementada gradualmente.
> Atualmente cobertos com **JUnit 5** + **Mockito**:

| Classe | Testes | Cobertura |
|---|---|---|
| `AdminProductService` | CRUD completo + exceções | ✅ |
| `OrderService` | Criação de pedidos + validações | ✅ |
| Demais serviços | — | 🔄 Em andamento |

---

## Arquitetura

```
controller/     → entrada HTTP, validação de request
service/        → regras de negócio
domain/         → entidades JPA
dto/            → request/response + mappers (MapStruct)
repository/     → Spring Data JPA
security/       → JWT filter, UserDetails
config/         → Security, CORS, OpenAPI, seeds
exceptions/     → handlers globais
enums/          → roles, status, tipos
validation/     → validators customizados
```

---

## Status do Projeto

```
🟢 Core da API         → Funcional
🟢 Autenticação JWT    → Funcional
🟢 Pagamentos PIX      → Funcional
🟢 Entrega digital     → Funcional
🟡 Testes unitários    → Em expansão
🟡 Validação webhook   → Temporariamente desabilitada (dev)
🔴 Testes integração   → Não iniciados
```

---

---

Built with ☕, discipline, and attention to detail.

Developed by **[Kayk Edmar](https://github.com/KaykMurphy)** — Backend Developer focused on scalable systems and clean architecture.
