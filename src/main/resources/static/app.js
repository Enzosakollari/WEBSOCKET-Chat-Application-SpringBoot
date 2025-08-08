let stompClient = null;
let currentUser = null;

function enterChatroom() {
    const nickname = document.getElementById('nickname').value;
    const fullname = document.getElementById('fullname').value;

    if (!nickname || !fullname) return alert("Fill all fields!");

    currentUser = { nickname, name: fullname, status: 'ONLINE' };

    // Connect WebSocket
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        stompClient.subscribe('/topic/user', onUserUpdate);
        stompClient.subscribe('/user/' + currentUser.nickname + '/queue/messages', onPrivateMessage);

        // Send JOIN
        stompClient.send("/app/user.addUser", {}, JSON.stringify(currentUser));

        // Show chat UI
        document.getElementById('loginPage').classList.add('hidden');
        document.getElementById('chatPage').classList.remove('hidden');
        document.getElementById('currentUser').innerText = "Connected as: " + currentUser.nickname;

        fetch('/user/connectedUsers')
            .then(res => res.json())
            .then(users => updateUserList(users));
    });
}

function onUserUpdate(message) {
    const user = JSON.parse(message.body);
    fetch('/user/connectedUsers')
        .then(res => res.json())
        .then(users => updateUserList(users));
}

function updateUserList(users) {
    const list = document.getElementById('userList');
    list.innerHTML = '';
    users.forEach(user => {
        if (user.nickname !== currentUser.nickname) {
            const li = document.createElement('li');
            li.innerText = user.nickname;
            li.onclick = () => openPrivateChat(user.nickname);
            list.appendChild(li);
        }
    });
}

let selectedReceiver = null;

function openPrivateChat(receiverNickname) {
    selectedReceiver = receiverNickname;

    fetch(`/messages/${currentUser.nickname}/${receiverNickname}`)
        .then(res => res.json())
        .then(messages => {
            const container = document.getElementById('messageContainer');
            container.innerHTML = '';
            messages.forEach(msg => {
                const div = document.createElement('div');
                div.textContent = msg.sender.nickname + ": " + msg.content;
                container.appendChild(div);
            });
        });
}

function sendMessage() {
    const content = document.getElementById('messageInput').value;
    if (!content || !selectedReceiver) return;

    const message = {
        sender: currentUser,
        receiver: { nickname: selectedReceiver },
        content: content,
        chatId: currentUser.nickname + "_" + selectedReceiver
    };

    stompClient.send("/app/chat", {}, JSON.stringify(message));
    document.getElementById('messageInput').value = '';
}

function onPrivateMessage(message) {
    const msg = JSON.parse(message.body);
    const container = document.getElementById('messageContainer');
    const div = document.createElement('div');
    div.textContent = msg.senderNickname + ": " + msg.content;
    container.appendChild(div);
}

function logout() {
    if (stompClient) {
        stompClient.send("/app/user.disconnectUser", {}, JSON.stringify(currentUser));
        stompClient.disconnect();
    }
    location.reload();
}
