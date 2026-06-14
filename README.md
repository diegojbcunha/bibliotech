# BiblioTech - Trabalho Final de Testes de Sistemas

**Disciplina:** Testes de Sistemas  
**Período:** 2026.2  
**Curso:** Desenvolvimento de Sistemas

## Descrição

Este repositório contém os testes automatizados desenvolvidos para o sistema **BiblioTech**, uma aplicação web de gerenciamento de bibliotecas. O trabalho simula um cenário real de Quality Assurance (QA), incluindo planejamento, implementação de testes unitários e de interface, execução, identificação de bugs e elaboração de relatório profissional.

## Como Executar os Testes

### Pré‑requisitos

- Java JDK 17 ou superior
- Maven 3.6+ (ou use os wrappers `mvnw` / `mvnw.cmd`)
- Google Chrome instalado (para os testes Selenium)
- IDE (Eclipse, IntelliJ) ou terminal

### 1. Clonar o repositório

```bash
git clone https://github.com/diegojbcunha/bibliotech
cd bibliotech
```

### 2. Executar a aplicação (necessária apenas para os testes Selenium)

```bash
mvn spring-boot:run
```
A aplicação estará disponível em http://localhost:8080.

### 3. Executar os testes

**Todos os testes (unitários + Selenium)**
```bash
mvn clean test
```

**Apenas os testes unitários (não precisam do sistema rodando)**
```bash
mvn test -Dtest="com.bibliotech.unit.*"
```

**Apenas os testes Selenium (exigem que o sistema esteja rodando)**
```bash
mvn test -Dtest="com.bibliotech.selenium.*"
```

### 4. Gerar o relatório HTML de resultados

```bash
mvn surefire-report:report
```
O relatório será gerado em `target/site/surefire-report.html`.

---

## Resultados Obtidos

### Resumo Geral

| Métrica | Valor |
| :--- | :--- |
| **Total de testes implementados** | 32 (20 unitários + 12 Selenium) |
| **Testes aprovados** | 23 (71,9%) |
| **Testes reprovados** | 9 (28,1%) |
| **Bugs identificados e documentados** | 6 |
| **Requisitos funcionais testados** | 15 de 15 (100%) |
| **Regras de negócio testadas** | 10 de 12 (83,3%) |

### Bugs Encontrados

| ID | Descrição | Severidade | Módulo |
| :--- | :--- | :--- | :--- |
| **BUG-001** | ISBN duplicado permitido (não valida unicidade) | ALTA | Livros |
| **BUG-002** | Multa calculada com multiplicador 3.0 em vez de 2.0 | ALTA | Empréstimos |
| **BUG-003** | Prazo de devolução de 7 dias em vez de 14 | ALTA | Empréstimos |
| **BUG-004** | Empréstimo permitido sem verificar disponibilidade | ALTA | Empréstimos |
| **BUG-005** | Exclusão de usuário com empréstimos ativos não é impedida | ALTA | Usuários |
| **BUG-006** | Autenticação quebrada (comparação de senha com ==) | CRÍTICA | Autenticação |

---

## Estrutura do Projeto

```text
bibliotech/
├── src/
│   ├── main/java/com/bibliotech/          # Código do sistema (NÃO MODIFICADO)
│   └── test/java/com/bibliotech/
│       ├── unit/                          # Testes unitários JUnit 5 + Mockito
│       │   ├── LivroServiceTest.java
│       │   ├── UsuarioServiceTest.java
│       │   ├── EmprestimoServiceTest.java
│       │   └── DashboardServiceTest.java
│       └── selenium/                      # Testes de interface Selenium WebDriver
│           ├── BaseSeleniumTest.java
│           ├── LoginSeleniumTest.java
│           ├── LivroSeleniumTest.java
│           ├── UsuarioSeleniumTest.java
│           ├── EmprestimoSeleniumTest.java
│           └── NavegacaoSeleniumTest.java
├── Evidencias_GrupoX/                     # Evidências coletadas
│   ├── screenshots/                       # Screenshots dos bugs e falhas
│   ├── relatorios/                        # Relatório HTML (surefire-report.html)
│   └── logs/                              # Logs de execução (opcional)
├── pom.xml
└── README.md
```

## Relatório Final

O relatório técnico completo em PDF está disponível neste repositório:

📎 [Relatorio_Testes_BiblioTech_GrupoX.pdf](#)

## Tecnologias Utilizadas

* Java 17
* Spring Boot 3.2.0
* JUnit 5 (com Mockito)
* Selenium WebDriver 4.15.0
* WebDriverManager 5.6.2
* Maven Surefire Report Plugin

## Licença

Este projeto é parte de um trabalho acadêmico da disciplina de Testes de Sistemas. Todos os direitos reservados aos integrantes do grupo.

*Desenvolvido por Diego Cunha – 2026*