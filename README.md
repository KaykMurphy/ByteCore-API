# ByteCore API

API REST para marketplace de produtos digitais com entrega automatizada, fluxo de verificação de vendedores e integração com PIX via Mercado Pago.

> Reescrita da [bytemarket v1](https://github.com/KaykMurphy/bytemarket). Mesmo domínio, arquitetura melhor.

&nbsp;

## Tecnologias

`Java 17` `Spring Boot 3.4` `Spring Security` `JWT` `Spring Data JPA` `PostgreSQL` `MapStruct` `Mercado Pago SDK` `JavaMailSender` `JUnit 5` `Mockito`

H2 em memória para desenvolvimento local. PostgreSQL em produção.

&nbsp;

## Como funciona

O fluxo principal é direto: o comprador cria um pedido, gera um pagamento PIX e, após a confirmação do webhook do Mercado Pago, o sistema entrega automaticamente o conteúdo do produto (keys, contas) por e-mail.

O saldo do vendedor fica retido em estado pendente por 7 a 14 dias dependendo da avaliação dele, antes de um job agendado liberá-lo. Se a entrega automática falhar no meio da transação, um job de retry reprocessa a cada 30 minutos.

Vendedores passam por um fluxo de verificação de documentos antes de poderem listar produtos. Documentos fraudulentos são identificados por SHA-256 e bloqueados permanentemente — reenviar o mesmo documento com outra conta não vai funcionar.

&nbsp;

## Rodando localmente

```bash
git clone https://github.com/KaykMurphy/bytemarket-v2
cd bytemarket-v2
./mvnw spring-boot:run
```

Swagger UI → `http://localhost:8080/swagger-ui.html`  
Console H2 → `http://localhost:8080/h2-console`

&nbsp;

**Variáveis de ambiente necessárias:**

```properties
# Auth
JWT_SECRET=          # Base64-encoded, mínimo 256 bits
JWT_EXPIRATION=

# Mercado Pago
MERCADOPAGO_ACCESS_TOKEN=
MERCADOPAGO_WEBHOOK_SECRET=

# E-mail
EMAIL_USER=
EMAIL_PASSWORD=

# Admin seed (criado automaticamente ao subir)
ADMIN_EMAIL=
ADMIN_PASSWORD=

# App
APP_BASE_URL=
```

&nbsp;

## Estrutura do projeto

```
config/         Spring Security, CORS, OpenAPI, admin seeder
controller/     Camada HTTP — validação, extração de auth, mapeamento de resposta
service/        Regras de negócio. Tudo vive aqui.
domain/         Entidades JPA com métodos de negócio (não são modelos anêmicos)
dto/            Records de request/response + mappers MapStruct
repository/     Interfaces Spring Data JPA
security/       Filtro JWT, CustomUserDetails
exceptions/     Handler global de exceções (@ControllerAdvice)
enums/          Roles, status, tipos de produto e pagamento
validation/     Validators customizados
```

Vale notar: as entidades carregam seu próprio comportamento — `order.markAsPaid()`, `product.decrementStock()`, `verification.approve(admin)`. As regras de negócio não ficam espalhadas pelos métodos de serviço.

&nbsp;

## Endpoints

```
POST   /auth/register
POST   /auth/login
GET    /auth/me

GET    /api/products              catálogo paginado
GET    /api/products/{id}
GET    /api/products/search
POST   /api/orders
POST   /api/payments/pix/{orderId}
POST   /api/reviews
PUT    /api/reviews/{id}
GET    /api/reviews/user/{userId}
POST   /api/withdrawals
GET    /api/withdrawals/me
GET    /api/withdrawals/balance

POST   /seller/verifications
GET    /seller/verifications/me

POST   /admin/products
PUT    /admin/products/{id}
DELETE /admin/products/{id}
GET    /admin/verifications/pending
POST   /admin/verifications/{id}/approve
POST   /admin/verifications/{id}/reject
POST   /admin/verifications/{id}/ban

POST   /api/webhooks/mercadopago
```

&nbsp;

## Testes

```bash
./mvnw test
```

Testes unitários com JUnit 5 + Mockito. Testes de integração ainda não escritos.

| Service | Cobertura |
|---|---|
| `AdminProductService` | CRUD + cenários de não encontrado |
| `AdminVerificationService` | Aprovação, rejeição, ban, documento já banido |
| `AuthService` | Registro, login, e-mail duplicado, saldo nulo |
| `OrderService` | Criação de pedido + todas as validações de negócio |
| `PixPaymentService` | Criação do pagamento, erros do gateway, consulta de status |
| `ReviewService` | Criação, edição, ownership, limite de edições |
| `ScheduledPaymentReleaseJob` | Liberação em lote, isolamento de falha por item |
| `SellerVerificationService` | Submissão, guard de verificação pendente duplicada |
| `WithdrawalService` | Criação de saque, saldo insuficiente, limite diário |
| `DigitalProductDeliveryService` | Entrega automática, log de falha por estoque insuficiente |

&nbsp;

## Status

- [x] Core da API
- [x] Autenticação JWT
- [x] Pagamentos PIX + webhook
- [x] Entrega digital automatizada + job de retry
- [x] Fluxo de verificação de vendedor
- [x] Job de liberação de saldo
- [x] Testes unitários — em andamento
- [ ] Testes de integração
- [ ] Validação de assinatura do webhook reativada para produção

&nbsp;

---

Desenvolvido por [Kayk Edmar](https://github.com/KaykMurphy)
