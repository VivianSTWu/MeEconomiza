# MeEconomiza

MeEconomiza é um aplicativo Android desenvolvido para auxiliar usuários na avaliação da viabilidade e custo-benefício de instalar fontes de energia sustentável, como energia solar e eólica, em suas residências. 

O aplicativo utiliza a localização dada pelo usuário para coletar dados meteorológicos para estimar potenciais economias em relação ao seu gasto usual com energia.

---


## Desenvolvido por

Grupo KCIAO

Integrantes:
- RM 553471 - Gustavo Vieira Bargas
- RM 553791 - Jhonatan Sampaio Ferreira
- RM 553169 - Vivian Sy Ting Wu

Como solução proposta para a Global Solution do final de 2024, desafio lançado pela FIAP.

---

## Funcionalidades

- **Gerenciamento de Endereços**: Os usuários podem adicionar endereços de suas propriedades, onde os cálculos de energia sustentável serão realizados.
- **Cálculos de Sustentabilidade**: Utiliza dados meteorológicos e parâmetros do local para calcular:
  - Energia solar estimada gerada por placas solares.
  - Energia eólica estimada gerada por microturbinas eólicas.
  - Quanto o usuário economizaria por mês.

---

## Tecnologias Utilizadas

### Aplicação Android
- **Linguagem**: Kotlin.
- **Arquitetura**: MVVM (Model-View-ViewModel).
- **UI**: View Binding, RecyclerView.
- **Consumo de APIs**: Retrofit para chamadas REST.

### APIs Integradas
1. **OpenWeather**  
   - Utilizada para obter dados meteorológicos, como índice de radiação solar e vento.  
   - **Autenticação**: Chave de API  
   - **Formato de Resposta**: JSON com dados estruturados como parâmetros e coordenadas.
  
2. **Weatherbit**  
   - Utilizada para obter outros dados meteorológicos, como a média de radiação solar no mês ou velocidade do vento média no local.  
   - **Autenticação**: Chave de API  
   - **Formato de Resposta**: JSON com dados estruturados como parâmetros e coordenadas.

2. **Distance Matrix API (Google Maps)**  
   - **Função**: Converter o CEP indicado pelo usuário em coordenadas geográficas.  
   - **Autenticação**: Chave de API.  
   - **Formato de Resposta**: JSON.  

4. **API Interna do Backend**  
   - **Propósito**: Gerenciar informações de usuários e endereços.
   - **Formato de Resposta**: JSON com detalhes de usuários e cálculos realizados.

---
## Fluxo de Uso

1. **Cadastro do Usuário:**
   - O usuário insere nome e email no aplicativo.
   - Os dados são enviados para a API, utilizando o método POST, e o ID do usuário é armazenado localmente.

2. **Cadastro de Endereços:**
   - O usuário informa o endereço e fornece informações como consumo mensal de energia e tarifa.
   - A API armazena essas informações no banco de dados, utilizando os métodos GET, POST e PATCH no processo. 

3. **Consulta de Dados:**
   - O aplicativo consulta APIs de terceiros para buscar dados meteorológicos e realizar cálculos de viabilidade.

4. **Exibição de Resultados:**
   - O aplicativo exibe gráficos e estimativas para cada endereço, utilizando o método GET.
