# Realtime/WebSocket (STOMP)

## Cấu hình
- Endpoint: `/ws` (cho phép mọi origin, hỗ trợ SockJS)
- Application prefix: `/app`
- Broker: `/topic`, `/queue`
- User prefix: `/user`

```javascript
// Ví dụ client (SockJS + StompJS)
const socket = new SockJS('/ws');
const stomp = Stomp.over(socket);
stomp.connect({}, () => {
  // subscribe topic chung
  stomp.subscribe('/topic/room-123', (msg) => console.log('topic', msg.body));
  // subscribe hàng đợi riêng user
  stomp.subscribe('/user/queue/notifications', (msg) => console.log('notify', msg.body));

  // gửi message
  stomp.send('/app/chat.sendMessage', {}, JSON.stringify({ text: 'Hello' }));
});
```

## Mapping tiêu biểu (từ Controllers)
- Gửi: `/app/chat.sendMessage`, `/app/chat.addUser`, `/app/chat.typing`, `/app/chat.stopTyping`
- Nhận:
  - Broadcast: `/topic/...`
  - Riêng tư: `/user/{id}/queue/...`

## Tích hợp bình luận realtime
- `CommentController` có `@MessageMapping("/comments.addComment")` để thêm bình luận realtime.

## Bảo mật
- Kết nối WS được phép công khai tới `/ws`, nhưng xử lý ứng dụng vẫn dựa trên xác thực phiên/JWT ở tầng REST khi cần.