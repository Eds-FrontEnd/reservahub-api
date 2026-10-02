# ReservaHub API

API REST do **ReservaHub**, uma plataforma de assinatura de smartphones desenvolvida com **Java 21 e Spring Boot**, responsável pela autenticação de usuários, gerenciamento de smartphones, planos de assinatura, controle de estoque e integração com o Front-End.

O projeto foi desenvolvido com foco em **arquitetura em camadas, segurança, separação de responsabilidades, boas práticas de desenvolvimento e integração com aplicações web modernas**.

## 🚀 Tecnologias

| Tecnologia          | Utilização                             |
| ------------------- | -------------------------------------- |
| **Java 21**         | Linguagem principal                    |
| **Spring Boot**     | Desenvolvimento da API REST            |
| **Spring Security** | Segurança e autenticação               |
| **JWT**             | Autenticação baseada em tokens         |
| **BCrypt**          | Criptografia de senhas                 |
| **Spring Data JPA** | Persistência de dados                  |
| **Hibernate**       | Mapeamento objeto-relacional           |
| **PostgreSQL**      | Banco de dados relacional              |
| **Maven**           | Gerenciamento de dependências          |
| **REST API**        | Comunicação entre Front-End e Back-End |

## 🏗️ Arquitetura

O Back-End utiliza uma arquitetura em camadas, separando responsabilidades e facilitando a manutenção e evolução da aplicação.

```text
┌──────────────────────┐
│      Controller      │
│     API REST / HTTP  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│       Service        │
│    Regras de negócio │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│      Repository      │
│      Persistência    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│      PostgreSQL      │
│      Banco de dados  │
└──────────────────────┘
```

### Responsabilidades

| Camada       | Responsabilidade                                            |
| ------------ | ----------------------------------------------------------- |
| `controller` | Recebimento das requisições e construção das respostas HTTP |
| `service`    | Regras e operações de negócio                               |
| `repository` | Acesso e persistência dos dados                             |
| `dto`        | Contratos de entrada e saída da API                         |
| `model`      | Entidades e enums da aplicação                              |
| `security`   | Autenticação, JWT e proteção de recursos                    |
| `config`     | Configurações e componentes da aplicação                    |

---

## 🔐 Segurança

A autenticação utiliza **Spring Security** e **JWT**, protegendo os recursos que exigem autenticação.

### Recursos

- Cadastro de usuários
- Login
- Geração de JWT
- Validação de tokens
- Expiração de tokens
- Proteção de endpoints
- Hash de senhas com BCrypt
- Logout autenticado
- Revogação de tokens
- Controle de acesso

As senhas não são armazenadas em texto puro.

A chave JWT e demais credenciais devem permanecer fora do código-fonte versionado.

---

## 📱 Funcionalidades

### Usuários

- Cadastro
- Autenticação
- Geração de token
- Logout
- Revogação de token

### Smartphones

- Listagem
- Consulta por identificador
- Busca por nome
- Controle de estoque
- Paginação
- Associação com planos

### Planos

- Plano mensal
- Plano anual
- Plano de 24 meses

---

## 🌐 Endpoints

### Autenticação

| Método | Endpoint             | Descrição                   | Auth |
| ------ | -------------------- | --------------------------- | ---- |
| `POST` | `/api/auth/cadastro` | Cadastro de usuário         | Não  |
| `POST` | `/api/auth/login`    | Autenticação                | Não  |
| `POST` | `/api/auth/logout`   | Logout e revogação do token | Sim  |

### Smartphones

| Método | Endpoint                           | Descrição                  |
| ------ | ---------------------------------- | -------------------------- |
| `GET`  | `/api/smartphones`                 | Lista smartphones          |
| `GET`  | `/api/smartphones/{id}`            | Consulta smartphone por ID |
| `GET`  | `/api/smartphones/buscar?nome=...` | Pesquisa por nome          |

---

## 📊 Paginação

A listagem utiliza paginação do **Spring Data**, evitando o carregamento de todos os produtos em uma única requisição.

### Requisição

```http
GET /api/smartphones?page=0&size=4
```

### Parâmetros

| Parâmetro | Descrição                          |
| --------- | ---------------------------------- |
| `page`    | Página atual, iniciando em `0`     |
| `size`    | Quantidade de registros por página |

### Exemplo de Resposta

```json
{
  "content": [],
  "number": 0,
  "totalElements": 12,
  "totalPages": 3,
  "size": 4
}
```

---

## 🔎 Busca

A API permite pesquisar smartphones pelo nome por meio de parâmetros de consulta.

```http
GET /api/smartphones/buscar?nome=iPhone
```

---

## 🔑 Fluxo de Autenticação

```text
Cliente
   │
   │ POST /api/auth/login
   │ e-mail + senha
   ▼
Spring Security
   │
   │ valida credenciais
   ▼
BCrypt
   │
   │ senha válida
   ▼
JWT
   │
   │ token gerado
   ▼
Cliente
   │
   │ Authorization: Bearer TOKEN
   ▼
Endpoint protegido
   │
   │ valida JWT
   ▼
Recurso solicitado
```

### Header de Autenticação

```http
Authorization: Bearer SEU_TOKEN_JWT
```

---

## 🚪 Logout

O logout utiliza o token enviado pelo cliente e realiza sua revogação.

```text
POST /api/auth/logout
        │
        ▼
AuthController
        │
        ▼
LogoutService
        │
        ▼
JwtService
        │
        ▼
Token revogado
```

Tokens revogados deixam de ser considerados válidos pela aplicação.

---

## 🗄️ Banco de Dados

A persistência utiliza **PostgreSQL**, com **Spring Data JPA** e **Hibernate**.

### Estrutura

```text
PostgreSQL
└── reservahub
```

### Recursos Utilizados

- Entidades JPA
- Relacionamentos entre entidades
- Enums
- Consultas personalizadas
- Paginação
- Persistência de usuários e smartphones
- Relacionamento entre produtos e planos

### Configuração Local (`application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservahub
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA

jwt.secret=SUA_CHAVE_SECRETA
jwt.expiracao-minutos=60
```

> **Importante:** Não versione senhas, tokens, chaves JWT ou outras credenciais reais.

---

## ⚙️ Execução

### Pré-requisitos

- Java 21 ou superior
- PostgreSQL
- Maven
- Banco `reservahub` criado

### Passos

1. Clonar o repositório:

   ```bash
   git clone URL_DO_REPOSITORIO
   ```

2. Acessar o projeto:

   ```bash
   cd backend/api
   ```

3. Executar o projeto:
   - **Windows:**
     ```cmd
     mvnw.cmd spring-boot:run
     ```
   - **Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```

A API será disponibilizada em: `http://localhost:8080`

---

## 📂 Estrutura de Diretórios

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── reservahub/
    │           └── api/
    │               ├── config/
    │               ├── controller/
    │               ├── dto/
    │               ├── model/
    │               ├── repository/
    │               ├── security/
    │               └── service/
    │
    └── resources/
        └── application.properties
```

### Organização

| Pacote        | Função                     |
| ------------- | -------------------------- |
| `config/`     | Configurações do sistema   |
| `controller/` | Endpoints REST             |
| `dto/`        | Objetos de entrada e saída |
| `model/`      | Entidades e enums          |
| `repository/` | Camada de persistência     |
| `security/`   | Autenticação e JWT         |
| `service/`    | Regras de negócio          |

---

## 🔄 Integração

O Back-End é consumido pelo **ReservaHub Front-End**, desenvolvido com **React** e **TypeScript**.

```text
┌────────────────────────────┐
│      React + TypeScript    │
│         ReservaHub         │
└─────────────┬──────────────┘
              │
              │ HTTP / JSON
              ▼
┌────────────────────────────┐
│      Spring Boot API       │
│           Java 21          │
│     Spring Security + JWT  │
└─────────────┬──────────────┘
              │
              │ JPA / Hibernate
              ▼
┌────────────────────────────┐
│         PostgreSQL         │
│         reservahub         │
└────────────────────────────┘
```

A comunicação utiliza HTTP e JSON. Recursos protegidos recebem o JWT por meio do header `Authorization`.

---

## 🧩 DTOs

Os **Data Transfer Objects (DTOs)** controlam os contratos de comunicação da API, reduzindo o acoplamento entre os dados expostos externamente e as entidades de persistência.

São utilizados em operações como:

- Cadastro
- Login
- Resposta de autenticação
- Smartphones
- Produtos
- Planos

---

## 🧪 Boas Práticas

O projeto considera práticas voltadas à qualidade, segurança e manutenção do código:

- Separação de responsabilidades
- Clean Code
- Injeção de dependências
- Arquitetura em camadas
- DTOs
- API REST
- Validação de dados
- Códigos HTTP apropriados
- BCrypt
- JWT
- Paginação
- JPA/Hibernate
- Configuração por ambiente
- Proteção de credenciais
- Baixo acoplamento entre Front-End e Back-End

---

## 📈 Performance

- A paginação reduz o volume de dados transferidos em cada requisição e evita o carregamento simultâneo de todos os produtos.
- A separação das responsabilidades permite evoluir consultas, regras de negócio e mecanismos de persistência sem concentrar diferentes responsabilidades em um único componente.

---

## 🔒 Configuração Segura

Credenciais e informações sensíveis devem ser fornecidas por configuração de ambiente e não armazenadas no Git.

Não devem ser versionados:

- Senhas
- Chaves JWT
- Tokens
- Credenciais de banco
- Credenciais de APIs externas
- Segredos específicos de produção

Para ambientes produtivos, recomenda-se utilizar variáveis de ambiente ou um serviço de gerenciamento de segredos.

---

## 🌎 Ambientes

As configurações podem variar conforme o ambiente de execução. Entre os parâmetros que podem ser separados estão:

- Banco de dados
- Credenciais
- Chave JWT
- Expiração do token
- Logging
- URLs de serviços externos

---

## 🔗 ReservaHub Ecosystem

O projeto completo é composto por Front-End e Back-End.

### Front-End

- React, TypeScript, Vite, React Router, Material UI, HTML5, CSS/SCSS, API REST, Autenticação JWT.

### Back-End

- Java 21, Spring Boot, Spring Security, JWT, BCrypt, Spring Data JPA, Hibernate, PostgreSQL, Maven.

---

## 📌 Status

**Em desenvolvimento** 🚧

O projeto continua recebendo melhorias em:

- Funcionalidades
- Segurança
- Testes automatizados
- Performance
- Arquitetura
- Integração entre aplicações

---

## 👨‍💻 Autor

**Edson Alves**  
Desenvolvedor Front-End Pleno em evolução para atuação Full Stack, com experiência no desenvolvimento de aplicações web e utilização de tecnologias modernas de Front-End e Back-End.
