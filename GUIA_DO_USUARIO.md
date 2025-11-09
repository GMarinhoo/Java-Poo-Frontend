# 📖 Manual do Usuário - PDV Posto de Combustível

Este guia detalha o fluxo de trabalho completo para configurar e operar o sistema PDV.

---

## 1. 🔑 Primeiro Acesso (Admin)

O sistema de backend cria um usuário administrador padrão na primeira vez que é executado com um banco vazio.

* **Usuário:** `admin`
* **Senha:** `123456`

---

## 2. ⚙️ Guia do Gerente (Configurando o Posto)

Este é o fluxo obrigatório para configurar o sistema antes da primeira venda.

### 2.1. Cadastrar Produtos
Primeiro, cadastre os combustíveis que o posto irá vender.

1.  Faça login como `admin`.
2.  Vá ao menu `Sistema -> Produtos`.
3.  Preencha os dados (Ex: "Gasolina Comum", "Etanol") e clique em **Salvar**.
4.  **IMPORTANTE:** Anote o **ID** do produto que você acabou de criar (ele aparece na primeira coluna da tabela).

### 2.2. Definindo a Política de Custo
Agora, defina a regra de negócio global para cálculo de preços.

1.  Vá em `Sistema -> Custos`.
2.  Cadastre **um registro** com a política de preços do posto.
    * **Exemplo:**
        * Imposto: `1.20` (R$ 1,20)
        * Custo Fixo: `0.15` (R$ 0,15)
        * Margem Lucro: `0.20` (20%)
        * Data: (Data atual)

### 2.3. Definindo o Preço de Venda (Usando o Cálculo Ideal)
Com o produto e a regra de custo criados, vamos definir o preço que vai para a bomba.

1.  Vá em `Sistema -> Definir Preços`.
2.  Digite o **ID do Produto** (que você anotou no passo 2.1).
3.  Clique no botão **[Calcular Preço Ideal]**.
4.  Uma caixa de diálogo perguntará o "Custo de Compra" (preço da distribuidora). Digite-o (Ex: `4.10`).
5.  O sistema usará a regra do passo 2.2 para calcular o preço final (Ex: `R$ 6.54`) e preencherá o campo **Valor (R$)** automaticamente.
6.  Você pode "arredondar" o valor se desejar (Ex: `6.59`) e clique em **Salvar**.

### 2.4. Cadastrando o Estoque
1.  Vá em `Sistema -> Estoque`.
2.  Cadastre o estoque para o **ID do Produto**.

### 2.5. Cadastrando Frentistas
1.  Vá em `Sistema -> Cadastrar Novo Usuário`.
2.  Preencha todos os dados (Pessoais e de Acesso).
3.  Defina o perfil como `OPERADOR_CAIXA`.
4.  Clique em **Registrar**.

O sistema está pronto para operar!

---

## 3. 🧭 Operação do Dia a Dia (Guia do Frentista)

1.  O frentista faz login com a conta criada (Ex: "frentista1", "senha123").
2.  Na `TelaPrincipal`, ele escolhe o **Combustível** na bomba desejada. O preço carregará automaticamente.
3.  O frentista digita o **Valor (R$)"** (Ex: 50,00) OU os **Litros** (Ex: 10). O outro campo é calculado sozinho.
4.  Ele seleciona a **Forma de Pagamento**.
5.  Clica em **[Registrar Venda]**.
6.  O sistema pedirá para imprimir o cupom fiscal. A venda é registrada e o estoque é atualizado automaticamente.
7.  Para deslogar, o frentista clica em `Sair -> Logout`.

---

## 4. 🔐 Gerenciamento Avançado (Admin)

### 4.1. Promover ou Rebaixar um Usuário
1.  Vá em `Sistema -> Gerenciar Acessos (Logins)`.
2.  Clique no usuário que deseja alterar (Ex: "frentista1").
3.  Na caixa de seleção **"Novo Perfil"**, escolha o novo perfil (Ex: `GERENTE`).
4.  Clique em **[Atualizar Perfil]**. A mudança é instantânea.

### 4.2. Excluir um Funcionário (Fluxo de 2 Etapas)
Para manter a integridade do banco de dados, você não pode excluir uma `Pessoa` que possui um `Acesso` (login).

1.  **Etapa 1: Excluir o Acesso**
    * Vá em `Sistema -> Gerenciar Acessos (Logins)`.
    * Selecione o usuário (Ex: "frentista1").
    * Clique em **[Excluir Login]**.
2.  **Etapa 2: Excluir a Pessoa**
    * Agora que o login foi removido, vá em `Sistema -> Pessoas`.
    * Selecione o funcionário.
    * Clique em **[Excluir]**.