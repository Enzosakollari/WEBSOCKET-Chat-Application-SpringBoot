let stomp;
let me = {};
let activeReceiver = null;
let roomSubscriptions = {}; // Store all room subscriptions

document.addEventListener("DOMContentLoaded", () => {
    // Get DOM elements
    const usernameForm = document.getElementById("usernameForm");
    const loginScreen = document.getElementById("login-screen");
    const chatScreen = document.getElementById("chat-screen");
    const nicknameInput = document.getElementById("nickname");
    const fullnameInput = document.getElementById("fullname");
    const enterBtn = document.querySelector("#usernameForm button[type='submit']");

    // Ensure chat screen is hidden initially
    if (chatScreen) {
        chatScreen.style.display = 'none';
        chatScreen.style.opacity = '0';
    }

    // Handle login function
    const handleLogin = (e) => {
        if (e) {
            e.preventDefault();
            e.stopPropagation();
        }

        me.nickname = nicknameInput.value.trim();
        me.fullname = fullnameInput.value.trim();

        if (!me.nickname || !me.fullname) {
            alert("Please enter both nickname and full name.");
            return false;
        }

        // Disable button during transition
        if (enterBtn) enterBtn.disabled = true;

        // Start transition
        loginScreen.style.opacity = '0';

        setTimeout(() => {
            loginScreen.classList.add("hidden");
            chatScreen.classList.remove("hidden");
            chatScreen.style.display = 'block';

            // Force reflow
            void chatScreen.offsetHeight;

            // Start fade-in
            chatScreen.style.opacity = '1';

            // Update UI
            const meElement = document.getElementById("connected-user-fullname");
            if (meElement) {
                meElement.textContent = `Logged in as ${me.nickname} (${me.fullname})`;
            }

            // Connect to WebSocket
            try {
                connect();
            } catch (error) {
                console.error("Connection failed:", error);
                alert("Connection error. Please refresh and try again.");
                if (enterBtn) enterBtn.disabled = false;
                return;
            }

            // Focus on message input
            setTimeout(() => {
                const messageInput = document.getElementById("message");
                if (messageInput) messageInput.focus();
            }, 300);
        }, 200);

        return false;
    };

    // Event listeners
    if (usernameForm) {
        usernameForm.addEventListener("submit", handleLogin);
    }

    // Logout handler
    const logoutBtn = document.getElementById("logout");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            if (stomp && stomp.connected) {
                stomp.send("/app/user.disconnectUser", {}, JSON.stringify({
                    nickname: me.nickname,
                    fullname: me.fullname,
                    status: "OFFLINE"
                }));
                stomp.disconnect();
            }
            location.reload();
        });
    }

    // Message handlers
    const messageForm = document.getElementById("messageForm");
    const messageInput = document.getElementById("message");

    if (messageForm) {
        messageForm.addEventListener("submit", (e) => {
            e.preventDefault();
            sendMessage();
        });
    }

    if (messageInput) {
        messageInput.addEventListener("keydown", e => {
            if (e.key === "Enter") {
                e.preventDefault();
                sendMessage();
            }
        });
    }
});

function connect() {
    const sock = new SockJS("/ws");
    stomp = Stomp.over(sock);

    stomp.connect({}, () => {
        // Private messages
        stomp.subscribe("/user/queue/messages", frame => {
            const msg = JSON.parse(frame.body);
            console.log("Received private message:", msg); // Debug log

            // Only show message if it's from someone else or if we're the sender
            if (msg.senderNickname !== me.nickname || msg.receiverNickname === activeReceiver) {
                appendMsg(
                    msg.senderNickname === me.nickname ? "me" : "other",
                    `${msg.senderNickname}: ${msg.content}`
                );
            }
        });

        // User list updates
        stomp.subscribe("/topic/users", () => {
            loadUsers();
        });

        // Register user
        stomp.send("/app/user.addUser", {}, JSON.stringify({
            nickname: me.nickname,
            fullname: me.fullname,
            status: "ONLINE"
        }));

        // Initial load
        loadUsers();

        // Subscribe to all existing chat rooms
        subscribeToAllChatRooms();
    }, (error) => {
        console.error("WebSocket connection error:", error);
        alert("Failed to connect. Please refresh and try again.");
    });
}

function loadUsers() {
    fetch("/user/connectedUsers")
        .then(response => {
            if (!response.ok) throw new Error("Network error");
            return response.json();
        })
        .then(users => {
            const list = document.getElementById("connectedUsers");
            if (!list) {
                console.error("connectedUsers element not found!");
                return;
            }

            list.innerHTML = "";
            users
                .filter(u => u.nickname !== me.nickname)
                .forEach(u => {
                    const li = document.createElement("li");
                    li.className = `user${u.nickname === activeReceiver ? " active" : ""}`;
                    li.textContent = u.nickname;
                    li.onclick = () => selectReceiver(u.nickname);
                    list.appendChild(li);
                });
        })
        .catch(error => {
            console.error("Error loading users:", error);
        });
}

function selectReceiver(nick) {
    activeReceiver = nick;
    const chatWithElement = document.getElementById("chatWith");
    if (chatWithElement) {
        chatWithElement.textContent = `Chatting with ${nick}`;
    }
    highlightActive(nick);

    const chatId = computeChatId(me.nickname, nick);

    // Subscribe to the chat room if not already subscribed
    if (!roomSubscriptions[chatId]) {
        roomSubscriptions[chatId] = stomp.subscribe(`/topic/chat/${chatId}`, frame => {
            const msg = JSON.parse(frame.body);
            console.log("Received chat room message:", msg); // Debug log

            // Always show message if it's from the active chat or if we're the sender
            if (msg.senderNickname === me.nickname || 
                msg.receiverNickname === me.nickname) {

                // Only append to chat if this is the active conversation or a notification should be shown
                if (activeReceiver === msg.senderNickname || 
                    activeReceiver === msg.receiverNickname || 
                    msg.senderNickname === me.nickname) {

                    appendMsg(
                        msg.senderNickname === me.nickname ? "me" : "other",
                        `${msg.senderNickname}: ${msg.content}`
                    );
                } else {
                    // This is a message from another conversation - could add notification here
                    console.log("Message received in background chat:", msg);
                    // TODO: Add notification for background messages
                }
            }
        });
    }

    fetch(`/messages/${me.nickname}/${nick}`)
        .then(response => {
            if (!response.ok) throw new Error("History load failed");
            return response.json();
        })
        .then(history => {
            const box = document.getElementById("chat-messages");
            if (box) {
                box.innerHTML = "";
                history.forEach(m => {
                    appendMsg(
                        m.sender.nickname === me.nickname ? "me" : "other",
                        `${m.sender.nickname}: ${m.content}`
                    );
                });
            }
        })
        .catch(error => {
            console.error("Error loading history:", error);
        });

    // Show message form
    const messageForm = document.getElementById("messageForm");
    if (messageForm) {
        messageForm.classList.remove("hidden");
    }
}

function sendMessage() {
    const contentInput = document.getElementById("message");
    if (!contentInput || !activeReceiver) return;

    const content = contentInput.value.trim();
    if (!content) return;

    const chatId = computeChatId(me.nickname, activeReceiver);

    // Immediately show the message for the sender
    appendMsg("me", `${me.nickname}: ${content}`);

    // Send via WebSocket with proper structure
    const messageData = {
        content: content,
        chatId: chatId,
        sender: { 
            nickname: me.nickname,
            fullname: me.fullname 
        },
        receiver: { 
            nickname: activeReceiver,
            fullname: activeReceiver // We'll get the real fullname from backend
        }
    };

    console.log("Sending message:", messageData); // Debug log
    stomp.send("/app/chat", {}, JSON.stringify(messageData));

    contentInput.value = "";
    contentInput.focus();
}

function appendMsg(kind, text) {
    const m = document.createElement("div");
    m.className = `msg ${kind}`;
    m.textContent = text;

    const box = document.getElementById("chat-messages");
    if (box) {
        box.appendChild(m);
        box.scrollTop = box.scrollHeight;
        console.log("Message appended:", text); // Debug log
    } else {
        console.error("Chat messages box not found!");
    }
}

function highlightActive(nick) {
    document.querySelectorAll(".user").forEach(el => {
        el.classList.toggle("active", el.textContent === nick);
    });
}

function computeChatId(a, b) {
    return a.localeCompare(b) < 0 ? `${a}_${b}` : `${b}_${a}`;
}

function subscribeToAllChatRooms() {
    // Fetch all chat rooms for the current user
    fetch(`/chatrooms/${me.nickname}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch chat rooms");
            return response.json();
        })
        .then(chatRooms => {
            console.log("Found chat rooms:", chatRooms);

            // Subscribe to each chat room
            chatRooms.forEach(room => {
                const chatId = room.chatId;

                // Only subscribe if not already subscribed
                if (!roomSubscriptions[chatId]) {
                    console.log(`Subscribing to chat room: ${chatId}`);

                    roomSubscriptions[chatId] = stomp.subscribe(`/topic/chat/${chatId}`, frame => {
                        const msg = JSON.parse(frame.body);
                        console.log("Received chat room message:", msg);

                        // Always show message if it's from the active chat or if we're the sender
                        if (msg.senderNickname === me.nickname || 
                            msg.receiverNickname === me.nickname) {

                            // Only append to chat if this is the active conversation or a notification should be shown
                            if (activeReceiver === msg.senderNickname || 
                                activeReceiver === msg.receiverNickname || 
                                msg.senderNickname === me.nickname) {

                                appendMsg(
                                    msg.senderNickname === me.nickname ? "me" : "other",
                                    `${msg.senderNickname}: ${msg.content}`
                                );
                            } else {
                                // This is a message from another conversation - could add notification here
                                console.log("Message received in background chat:", msg);
                                // TODO: Add notification for background messages
                            }
                        }
                    });
                }
            });
        })
        .catch(error => {
            console.error("Error subscribing to chat rooms:", error);
        });
}
