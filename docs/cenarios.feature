# language: pt
# Fonte: docs/diario-de-comportamento.md (observações de 2026-11-24, instância pública)
# Origem de cada expectativa:
#   [documentado] a documentação promete
#   [observado]   a API faz assim, e o teste detecta se mudar
#   [regra]       comportamento correto que defendo, mesmo que a API falhe nele

Funcionalidade: Reservas do Restful-booker
  Como QA responsável pela API de reservas
  Quero automatizar o comportamento observado e as regras esperadas
  Para detectar mudanças e documentar divergências

  Contexto:
    Dado que a API está disponível em "https://restful-booker.herokuapp.com"

  # ---------------------------------------------------------------
  # Saúde e autenticação (diário: itens 1 e 2)
  # ---------------------------------------------------------------

  @smoke
  Cenário: Health check responde
    Quando faço GET em "/ping"
    Então o status deve ser 201 [observado; a convenção seria 200]
    E o corpo deve ser "Created" [observado]

  @smoke
  Cenário: Credencial válida gera token
    Quando faço POST em "/auth" com usuário "admin" e a senha configurada
    Então o status deve ser 200 [documentado]
    E a resposta traz um campo "token" não vazio [documentado]

  # ---------------------------------------------------------------
  # Consulta (diário: itens 3 e 4)
  # ---------------------------------------------------------------

  @contrato
  Cenário: Listar ids de reservas
    Quando faço GET em "/booking" sem filtros
    Então o status deve ser 200 [documentado]
    E o corpo é uma lista não vazia de objetos com a chave "bookingid" [documentado]

  @contrato
  Cenário: Consultar uma reserva existente com o Accept padrão
    Dado que existe uma reserva criada por mim
    Quando faço GET nela com o header "Accept" igual a "*/*"
    Então o status deve ser 200 [observado; relatos de terceiros indicavam 418]
    E a resposta traz os campos firstname, lastname, totalprice, depositpaid, bookingdates e additionalneeds [documentado]

  # ---------------------------------------------------------------
  # Criação (diário: item 5)
  # ---------------------------------------------------------------

  @smoke
  Cenário: Criar reserva válida
    Quando crio uma reserva com dados válidos
    Então o status deve ser 200 [observado; a convenção seria 201]
    E a resposta não traz o header "Location" [observado; a convenção REST traria]
    E a resposta traz um "bookingid" numérico [documentado]
    E a reserva devolvida tem os mesmos dados enviados [regra]

  # ---------------------------------------------------------------
  # Alteração (diário: itens 6 e 7)
  # ---------------------------------------------------------------

  @regressao
  Cenário: Alterar a reserva inteira com autenticação Basic
    Dado que existe uma reserva criada por mim
    Quando faço PUT nela com autenticação Basic do usuário "admin" e um corpo completo
    Então o status deve ser 200 [observado]
    E os campos da resposta correspondem exatamente ao corpo enviado [regra]

  # A observação de 2026-11-24 (403) foi invalidada: a causa foi o script da coleção,
  # que sobrescreveu o token com "undefined". Refazer a observação com o script corrigido
  # antes de automatizar este cenário.
  @regressao
  Cenário: Alterar parcialmente uma reserva com token válido
    Dado que existe uma reserva criada por mim
    E que tenho um token válido
    Quando faço PATCH nela com firstname "Jim" e lastname "Brown"
    Então o status deve ser 200 [documentado; observação pendente]
    E firstname e lastname devem ser alterados [documentado]
    E os demais campos devem permanecer iguais [regra]

  # ---------------------------------------------------------------
  # Exclusão (diário: item 8 e tabela de erros)
  # ---------------------------------------------------------------

  @regressao
  Cenário: Excluir reserva existente
    Dado que existe uma reserva criada por mim
    E que tenho um token válido
    Quando faço DELETE nela
    Então o status deve ser 201 [observado; a convenção seria 200 ou 204]
    E o corpo deve ser "Created" [observado]

  @regressao
  Cenário: Excluir reserva com token vazio
    Dado que existe uma reserva criada por mim
    Quando faço DELETE nela com o cookie "token" vazio
    Então o status deve ser 403 [observado]

  @regressao
  Cenário: Excluir reserva inexistente
    Dado que tenho um token válido
    Quando faço DELETE na reserva de id 999999999
    Então o status deve ser 405 [observado; a convenção seria 404]