# 🖥️ PDV Posto de Combustível (Frontend)

Este é o módulo de **Frontend** do projeto de PDV. É um cliente desktop construído em **Java Swing** que consome a API REST do backend, simulando a operação das bombas e os painéis de gerenciamento.

![Status: Concluído](https://img.shields.io/badge/status-concluído-brightgreen)

## ✨ Funcionalidades

* **Interface Gráfica (GUI):** Construído 100% em Java Swing.
* **Tema Moderno:** Utiliza a biblioteca **FlatLaf** (Dark) para um visual moderno.
* **Consumo de API:** Usa `RestTemplate` (com `httpclient5`) para se comunicar com o backend via REST, realizando operações `GET`, `POST`, `PUT`, `DELETE` e `PATCH`.
* **Programação Concorrente:** Utiliza `SwingWorker` para todas as chamadas de API, garantindo que a interface (UI) nunca trave.
* **Gerenciamento Completo:** Possui telas CRUD para todas as 7 entidades do backend (Produtos, Estoque, Preços, Custos, Pessoas, Acessos, Contatos).
* **Lógica de Negócio:** Conecta as telas de Custo e Preço para "Calcular o Preço Ideal" de venda.

## 🛠️ Tecnologias Utilizadas

* **Java 17** (Swing)
* **Spring Boot 3** (para Injeção de Dependência e `RestTemplate`)
* **FlatLaf** (para o Look & Feel)
* **Apache HttpClient5** (para requisições `PATCH`)
* **Maven**

## 🚀 Como Rodar o Frontend

### Pré-requisitos
* **O Backend deve estar rodando!** (Na porta `http://localhost:8080`)
* Java 17 (JDK)
* Maven

### Executar
Execute o método `main` da classe `FrontendApplication.java`.
A tela de login aparecerá.

---

## 📖 Guia de Uso Completo

Para um guia passo a passo sobre como **configurar o posto** (cadastrar produtos, custos, preços) e **operar as bombas** (realizar vendas), veja o nosso:

### ➡️ **[Manual do Usuário (GUIA_DO_USUARIO.md)](GUIA_DO_USUARIO.md)**