# Relatório de Observação — Restful Booker API

**Coleção:** Booker | **Workspace:** Prática APIs | **Data de observação:** 2026-11-24

---

### 1\. GET /ping — Ping / HealthCheck

- **Requisição:** GET `https://restful-booker.herokuapp.com/ping` — sem headers especiais
    
- **Esperado (convenção HTTP):** `200 OK` para health check
    
- **Observado em 2026-11-24:** `201 Created`, corpo: `"Created"`
    
- **Diverge?** Diverge da convenção HTTP — health check retorna 201 em vez de 200. Comportamento documentado pela própria API (Restful Booker).
    
- **Decisão para o teste:** Afirmar `201` (comportamento observado/documentado pela API).
    

---

### 2\. POST /auth — Auth / CreateToken

- **Requisição:** POST `https://restful-booker.herokuapp.com/auth` — body JSON: `{"username":"admin","password":"password123"}`
    
- **Esperado (fonte):** `200 OK` com campo `token`
    
- **Observado em 2026-11-24:** `200 OK`, corpo: `{"token":"7826f725e921390"}`
    
- **Diverge?** Não diverge. Comportamento esperado.
    
- **Decisão para o teste:** Afirmar `200` e presença do campo `token` não vazio.
    

> ⚠ **Atenção:** o script `afterResponse` na coleção (`pm.collectionVariables.set("token", pm.response.json().token)`) roda após **toda** requisição — se outra request for disparada depois, o token pode ser sobrescrito com `undefined`. Ver item 7. 
  

---

### 3\. GET /booking — Booking / GetBookingIds

- **Requisição:** GET `https://restful-booker.herokuapp.com/booking` — sem filtros ou headers especiais
    
- **Esperado (fonte):** `200 OK` com array de objetos `{"bookingid": N}`
    
- **Observado em 2026-11-24:** `200 OK`, array de **3.323 IDs** — ex.: `[{"bookingid":1},{"bookingid":2},...]`
    
- **Diverge?** Não diverge. Comportamento esperado.
    
- **Decisão para o teste:** Afirmar `200` e que o corpo é um array não vazio de objetos com a chave `bookingid`.
    

---

### 4\. GET /booking/{id} — Booking / GetBooking

- **Requisição:** GET `https://restful-booker.herokuapp.com/booking/1` — header padrão do Postman (`Accept: \\*/\\*`)
    
- **Esperado (fonte):** `200 OK` com objeto de booking completo
    
- **Observado em 2026-11-24:** `200 OK`, corpo:
    
    ``` json
    {
      "firstname": "James",
      "lastname": "Brown",
      "totalprice": 111,
      "depositpaid": true,
      "bookingdates": { "checkin": "2018-01-01", "checkout": "2019-01-01" },
      "additionalneeds": "Breakfast"
    }
    
     ```
    
- **Diverge?** Não diverge. Retorna JSON mesmo sem `Accept: application/json` explícito.
    
- **Decisão para o teste:** Afirmar `200` e presença de todos os campos obrigatórios do schema de booking.
    

---

### 5\. POST /booking — Booking / CreateBooking

- **Requisição:** POST `https://restful-booker.herokuapp.com/booking` — `Content-Type: application/json`  
    body:
    
    ``` json
    {
      "firstname": "Eric",
      "lastname": "Foreman",
      "totalprice": 422,
      "depositpaid": true,
      "bookingdates": { "checkin": "2025-01-01", "checkout": "2026-01-01" },
      "additionalneeds": "Breakfast"
    }
    
     ```
    
- **Esperado (convenção REST):** `201 Created` com o recurso criado e header `Location`
    
- **Observado em 2026-11-24:** `200 OK`, corpo: `{"bookingid":4914,"booking":{...}}`
    
- **Diverge?** Diverge da convenção HTTP — criação de recurso deveria retornar 201, não 200. Sem header `Location`.
    
- **Decisão para o teste:** Afirmar `200` (comportamento real da API). Capturar `bookingid` para reuso nos testes subsequentes.
    

> **Nota no nome do teste:** "CreateBooking retorna 200 (deveria ser 201 pela convenção REST)" 
  

---

### 6\. PUT /booking/{id} — Booking / UpdateBooking

- **Requisição:** PUT `https://restful-booker.herokuapp.com/booking/1` — autenticação Basic (`admin`), body JSON completo com os dados do booking
    
- **Esperado (fonte):** `200 OK` com objeto atualizado
    
- **Observado em 2026-11-24:** `200 OK`, corpo:
    
    ``` json
    {
      "firstname": "James",
      "lastname": "Brown",
      "totalprice": 111,
      "depositpaid": true,
      "bookingdates": { "checkin": "2018-01-01", "checkout": "2019-01-01" },
      "additionalneeds": "Breakfast"
    }
    
     ```
    
- **Diverge?** Não diverge. A autenticação Basic funcionou e o booking foi atualizado.
    
- **Decisão para o teste:** Afirmar `200` e que os campos da resposta correspondem exatamente ao payload enviado.
    

---

### 7\. PATCH /booking/{id} — Booking / PartialUpdateBooking

- **Requisição:** PATCH `https://restful-booker.herokuapp.com/booking/3`  
    headers: `Content-Type: application/json`, `Accept: application/json`, `Cookie: token={{token}}`  
    body: `{"firstname":"Jim","lastname":"Brown"}`
    
- **Esperado (fonte):** `200 OK` com objeto parcialmente atualizado
    
- **Observado em 2026-11-24:** `403 Forbidden`, corpo: `"Forbidden"`
    
- **Diverge?** Diverge do esperado. A requisição foi rejeitada por falta de autorização válida.
    
- **Causa identificada:** O script `afterResponse` na coleção executa após **todas** as requisições. Requests sem o campo `token` na resposta (ex.: GetBookingIds retorna array; GetBooking retorna objeto sem token) sobrescrevem `{{token}}` com `undefined`, invalidando o Cookie.
    
- **Decisão para o teste:** Corrigir o script de coleção (adicionar guard: `if (json && json.token)`) e refazer a observação. Assegurar que Auth precede o PATCH na ordem de execução.
    

> **Nota:** o erro 403 é consequência de configuração de script, não de comportamento da API em condições normais. Registrar como "bug de script de coleção". 
  

---

## Script afterResponse da coleção — versão corrigida

``` javascript
const json = pm.response.json();
if (json && json.token) {
    pm.collectionVariables.set("token", json.token);
}

 ```

---

### 8\. DELETE /booking/{id} — Booking / DeleteBooking

- **Requisição:** DELETE `https://restful-booker.herokuapp.com/booking/{{bookingId}}` — header: `Cookie: token={{token}}`
    
- **Esperado (convenção REST):** `200 OK` ou `204 No Content` para deleção bem-sucedida
    
- **Observado em 2026-11-24:** `201 Created`, corpo: `"Created"`
    
- **Diverge?** Diverge da convenção HTTP — deleção de recurso deveria retornar 200 ou 204, não 201.
    
- **Decisão para o teste:** Afirmar `201` (comportamento real da API). Garantir que `{{token}}` esteja válido e que `{{bookingId}}` aponte para um booking existente antes de executar.
    

#### Erros encontrados durante a observação

| Sintoma | Causa | Correção aplicada |
| --- | --- | --- |
| `403 Forbidden` | `{{token}}` estava vazio — `Cookie: token=` enviado sem valor | Executar `Auth - CreateToken` para gerar token novo; atualizar variável `{{token}}` |
| `405 Method Not Allowed` | Booking ID `2564` não existe na API | Substituir ID hardcoded por `{{bookingId}}` (atualmente `1`) |
| URL malformada | Presença de `\n` (newline) no final da URL | URL corrigida para `https://restful-booker.herokuapp.com/booking/{{bookingId}}` |

> ⚠ **Atenção:** o mesmo problema de script identificado no item 7 (sobrescrita de `{{token}}`) afeta o DELETE. Garantir que o guard `if (json && json.token)` esteja aplicado no script `afterResponse` da coleção (ver seção ao final do relatório) e que `Auth - CreateToken` seja executado imediatamente antes do DELETE na ordem do collection run.