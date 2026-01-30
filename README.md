API REST para um e-commerce de produtos digitais, desenvolvida com Spring Boot, focada em organização, manutenibilidade e evolução contínua.

Este projeto nasce como a segunda geração de uma API que já está em produção, incorporando aprendizados práticos adquiridos no desenvolvimento e manutenção do primeiro sistema.

🎯 Objetivo do Projeto

A ByteCore API tem como objetivo fornecer uma base sólida para um e-commerce moderno, permitindo:

Listagem e busca de produtos

Operações administrativas (criação, atualização e desativação de produtos)

Evolução futura para:

autenticação e autorização

pagamentos (PIX e cartão de crédito via Mercado Pago)

controle de estoque

pedidos e usuários

🤔 Por que criar uma nova API em vez de refatorar a antiga?

A primeira versão do e-commerce foi meu primeiro projeto completo, hoje está em produção e funcionando corretamente.
No entanto, ao longo do tempo, alguns pontos ficaram claros:

1. Projeto em produção ≠ laboratório de refatoração

Refatorações profundas em um sistema ativo aumentam significativamente o risco de regressões e instabilidades.

2. Decisões iniciais de aprendizado

Como projeto inicial, muitas decisões foram tomadas com foco em fazer funcionar, e não necessariamente em:

separação clara de responsabilidades

escalabilidade

padronização de DTOs

organização de pacotes

evolução a longo prazo

Essas decisões funcionaram, mas tornaram o código mais difícil de manter.

3. Nova arquitetura, não apenas “código melhor”

Este projeto não é apenas uma refatoração estética. Ele nasce com:

arquitetura mais clara desde o primeiro commit

camadas bem definidas (controller, service, repository, dto, mapper)

uso de MapStruct para conversão de DTOs

tratamento centralizado de exceções

serviços com responsabilidades explícitas (ex: ProductService vs AdminProductService)

Essas mudanças seriam complexas e arriscadas de aplicar diretamente no projeto antigo.

4. Aprendizado aplicado corretamente

Criar uma nova API permite aplicar boas práticas desde o início, mantendo:

histórico de commits limpo

evolução incremental

código mais legível e previsível

Isso reflete melhor minha evolução como desenvolvedor.

🧱 Tecnologias Utilizadas

Java 21

Spring Boot

Spring Data JPA

Hibernate

MapStruct

Lombok

PostgreSQL / H2 (ambiente de desenvolvimento)

Maven

📁 Organização do Projeto
controller/   -> Camada de entrada da API (REST)
service/      -> Regras de negócio
repository/   -> Acesso a dados
domain/       -> Entidades
dto/
 ├─ request   -> DTOs de entrada
 ├─ response  -> DTOs de saída
 └─ mapper    -> Conversão entre entidades e DTOs
exceptions/   -> Tratamento global de erros

🚀 Funcionalidades Atuais

Listagem paginada de produtos

Busca de produtos por título

Consulta de produto por ID

Criação, atualização e desativação de produtos (soft delete)

Tratamento padronizado de erros

🔮 Próximos Passos

Autenticação e autorização (Spring Security)

Integração com Mercado Pago (PIX e cartão de crédito)

Gestão de usuários

Controle de estoque

Pedidos e pagamentos

Testes automatizados mais abrangentes

📌 Considerações Finais

Este projeto representa não apenas um novo sistema, mas a consolidação de aprendizados reais adquiridos com um produto em produção.

Ele foi pensado para ser mais fácil de manter, evoluir e escalar, refletindo uma abordagem mais madura de desenvolvimento backend.
