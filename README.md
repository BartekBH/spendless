## SPENDLESS

Backend for home budget manager application based on:
- pekko-http
- slick
- cats
- sealed monad
- macwire
- circe

Authentication with JWT Tokens

### Run application:
```
sbt run
````

### Run tests:
```
sbt test
```

### Endpoints examples:

#### Create new Budget:
```
POST /budgets
Authorization: Bearer YOUR_API_TOKEN
Content-Type: application/json
{
  "name": "Example Budget name"
}
```
Response details:

| Status Code | Description            |
|-------------|------------------------|
| 200         | Success                |
| 400         | Bad Request            |
| 500         | Internal server error  |

#### Update Budget
```
PATCH /budgets/{budget_id}
Authorization: Bearer YOUR_API_TOKEN
Content-Type: application/json
{
  "name": "New Budget name"
}
```
Response details:

| Status Code | Description           |
|-------------|-----------------------|
| 200         | Success               |
| 400         | Bad Request           |
| 403         | Forbidden             |
| 404         | Not Found             |
| 500         | Internal server error |
