// init-mongo.js
db = db.getSiblingDB('chat_app');

// Create app user with simple password
db.createUser({
    user: 'appuser',
    pwd: 'apppass',
    roles: [{ role: 'readWrite', db: 'chat_app' }]
});

// Create a test collection and document
db.createCollection('users')
;

db.users.insertOne({
    name: "Test User",
    email: "test@example.com",
    status: "online",
    createdAt: new Date()
}
)
;


