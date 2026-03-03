# ByteCore API

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

| Módulo | Descrição |
|---|---|
| Auth | Registro, login com JWT, endpoint `/me` |
| Produtos | CRUD público (paginado + busca), aprovação por admin |
| Pedidos | Criação com validação de estoque e status do vendedor |
| Pagamentos | PIX via Mercado Pago, webhook com validação de assinatura HMAC-SHA256 |
| Entrega digital | Entrega automática de keys/contas por e-mail após confirmação do pagamento |
| Verificação de vendedor | Fluxo de submissão → análise → aprovação/rejeição/ban |
| Saques | Solicitação via PIX com limite diário de R$ 500 |
| Reviews | Avaliação de vendedores pós-entrega (máx. 2 edições) |
| Blacklist | Documentos banidos por hash SHA-256 |
| Jobs | Liberação automática de saldo (cron diário às 03h) |

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

Testes unitários com **JUnit 5** + **Mockito** cobrindo `ProductService` e `AdminProductService`.

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
Made with ☕, discipline, and attention to detail.

Developed by Kayk Murphy
Backend Developer focused on scalable systems and clean architecture.
