# Multiplayer Quiz Roadmap

## Architecture

Use HTTP and STOMP WebSocket for different responsibilities.

- HTTP REST handles room creation, room lookup, quiz assignment, time-limit configuration, and room deletion.
- STOMP WebSocket handles live room events, quiz start, question broadcast, submissions, score updates, and final ranking.
- Database stores durable state: rooms, participants, submissions, and final rankings.
- In-memory state can store active game progress at first: current question index, deadline, and per-question submissions. If the app is scaled to multiple servers, move this state to Redis or a broker-backed design.

## Commit Plan

1. `feat: 퀴즈룸 대기방 REST API 구현`
   - Host creates a room.
   - Room response includes invite code and invite path.
   - Anonymous participants can join with a nickname or server-generated nickname.
   - Room has question time limit, defaulting to 20 seconds.

2. `feat: 퀴즈룸 STOMP 입장 이벤트 구현`
   - Add WebSocket principal handling.
   - Broadcast room snapshots to `/topic/room/{roomId}`.
   - Send joined participant ID to `/user/queue/room.joined`.

3. `feat: 일반 퀴즈 진행 이벤트 구현`
   - Broadcast same question and deadline to everyone.
   - Collect submissions per participant.
   - Broadcast question end and answer reveal.

4. `feat: 퀴즈 결과 제출과 순위판 구현`
   - Clients submit calculated final scores.
   - Server stores and broadcasts one shared ranking.
   - Add tie-breaking rules.

5. `feat: 스피드 게임 모드 구현`
   - Add game mode.
   - Server judges answers immediately.
   - Only the first correct participant earns score for each question.
   - Broadcast live scoreboard during the game.

## STOMP Route Plan

Client to server:

- `/app/room.join`
- `/app/room.leave`
- `/app/room.start`
- `/app/quiz.submit`

Server to client:

- `/user/queue/room.joined`
- `/topic/room/{roomId}`
- `/topic/quiz/{roomId}`

## Important Rules

- Speed game must use server receive time, not client time.
- Client-calculated scores are convenient for the normal mode, but server should still store enough submission data to audit or recompute results later.
- SockJS with cookies works for local development, but production must restrict allowed origins.
