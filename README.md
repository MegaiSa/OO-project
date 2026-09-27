# Messaging App

Une messagerie en deux microservices Java / Spring Boot :

- **chat-api** : service REST (port 8080). Gère les utilisateurs et les conversations, et sert l'interface web.
- **message-server** : serveur gRPC (port 9090). Enregistre et renvoie les messages.

Chaque service a sa propre base de données (H2 en mémoire).

## Comment ça marche

```
Navigateur ──REST──► chat-api ──gRPC──► message-server
                        │                     │
                   base chat            base messages
```

1. Le navigateur envoie une requête HTTP à `chat-api` (par exemple : envoyer un message).
2. `chat-api` vérifie dans sa base que l'utilisateur et la conversation existent.
3. `chat-api` transmet le message à `message-server` en gRPC.
4. `message-server` l'enregistre dans sa base et renvoie le message créé.

Le contrat gRPC entre les deux services est dans `messaging-proto/src/main/proto/message_service.proto`.

## Lancer le projet

Prérequis : **JDK 21**.

Dans un premier terminal :
```bash
./gradlew :message-server:bootRun       # Windows : gradlew.bat :message-server:bootRun
```

Dans un second terminal :
```bash
./gradlew :chat-api:bootRun             # Windows : gradlew.bat :chat-api:bootRun
```

Puis ouvrir :
- **http://localhost:8080** : l'interface web (créer des utilisateurs, une conversation, envoyer des messages) ;
- **http://localhost:8080/swagger-ui.html** : la liste des routes de l'API, testables depuis le navigateur.

Avec Docker, une seule commande suffit (une base PostgreSQL par service) :
```bash
docker compose up --build
```

## Routes principales

| Verbe | URL | Action |
|---|---|---|
| POST | `/users` | Créer un utilisateur |
| GET | `/users/{id}/conversations` | Conversations d'un utilisateur |
| POST | `/conversations` | Créer une conversation |
| POST | `/conversations/{id}/messages` | Envoyer un message |
| GET | `/conversations/{id}/messages` | Lire l'historique |
