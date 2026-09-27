#!/usr/bin/env bash
# Demo scenario: run it once both services are started.
API=${API:-http://localhost:8080}
set -x

# 1. Create two users (REST -> chat-api database)
curl -s -X POST $API/users -H 'Content-Type: application/json' -d '{"username":"alice","displayName":"Alice"}'; echo
curl -s -X POST $API/users -H 'Content-Type: application/json' -d '{"username":"bob","displayName":"Bob"}'; echo

# 2. Create a conversation between them
curl -s -X POST $API/conversations -H 'Content-Type: application/json' -d '{"name":"Projet OO","participantIds":[1,2]}'; echo

# 3. Send messages (REST -> chat-api -> gRPC SendMessage -> message-server database)
curl -s -X POST $API/conversations/1/messages -H 'Content-Type: application/json' -d '{"senderId":1,"content":"Salut Bob !"}'; echo
curl -s -X POST $API/conversations/1/messages -H 'Content-Type: application/json' -d '{"senderId":2,"content":"Salut Alice, on finit le projet ?"}'; echo

# 4. Read the history (REST -> chat-api -> gRPC GetHistory stream -> message-server)
curl -s $API/conversations/1/messages; echo

# 5. Error cases handled by the exception handler
curl -s $API/users/999; echo                                                         # 404
curl -s -X POST $API/users -H 'Content-Type: application/json' -d '{"username":"alice","displayName":"X"}'; echo   # 409
curl -s -X POST $API/conversations/1/messages -H 'Content-Type: application/json' -d '{"senderId":1,"content":""}'; echo  # 400
