# PESocial Backend Demo Guide (For Presentation)

This README is focused on backend architecture, structure, design principles, and design patterns.

---

## 1) Direct Answer: Does this backend follow design principles and patterns?

### Verdict

Yes, mostly.

### What is clearly good

- Clean layered architecture: Controller -> Service -> Repository -> Model.
- Strong use of interfaces for services (`*Service` + `*ServiceImpl`).
- Constructor injection is used in most major services and controllers.
- Centralized exception handling with `GlobalExceptionHandler`.
- Security and cross-cutting concerns are separated (`config`, `security`, AOP).
- Design patterns are present and demonstrable (listed below).

### What is not ideal (be honest in demo)

- A few files still use field injection (`@Autowired`) instead of constructor injection.
- Some story operations return `null` instead of throwing explicit exceptions.
- A few controllers contain extra orchestration logic that could move to services.

---

## 2) Design Principles Mapping (Demo Talking Points)

### SRP (Single Responsibility)

- Good in `AuthController`, `PostController`, `PostServiceImpl`, `GlobalExceptionHandler`.
- Weaker in `StoryController` and `MediaController` (multiple responsibilities in one class).

### OCP (Open/Closed)

- Good in notification strategy setup (`NotificationStrategy`, `NotificationContext`) and feed observer flow (`FollowerObserver`, `FeedPublisher`).

### LSP (Liskov Substitution)

- Service interfaces are substitutable with implementations (`UserService`/`UserServiceImpl`, `PostService`/`PostServiceImpl`, etc.).

### ISP (Interface Segregation)

- Service interfaces are split by feature (auth/user/post/message/monetization/chat, etc.), avoiding one giant interface.

### DIP (Dependency Inversion)

- Mostly followed via constructor-injected dependencies.
- Exceptions: legacy field injection still exists in story/media area.

---

## 3) Design Patterns Present in Backend

1. **MVC Architecture Pattern**
   - **Model**: Domain entities in `model/` package (User, Post, Story, Message, ChatRoom, Notification, CreatorAnalytics, etc.).
   - **View**: Response DTOs in `dto/` package (PostResponseDto, UserProfileDto, MessageDTO, etc.) + JSON serialization via Spring.
   - **Controller**: REST controllers in `controller/` package handle HTTP requests and delegate to services.
   - **Flow**: HTTP Request → Controller → Service (business logic) → Repository (data access) → Model → DTO → Response.

2. **Layered Architecture**
   - Packages: `controller`, `service`, `repository`, `model`, `dto`.
   - Clean separation: Presentation (Controller) → Business Logic (Service) → Data Access (Repository) → Domain (Model).

3. **Repository Pattern**
   - Spring Data repositories + custom query repository (`PostQueryRepositoryImpl`).

4. **Service Layer Pattern**
   - Business logic concentrated in `service/impl` classes.

5. **Strategy Pattern**
   - `NotificationStrategy` + `InAppNotificationStrategy` + `EmailNotificationStrategy` + `NotificationContext`.

6. **Observer Pattern**
   - `FeedPublisher` + `FollowerObserver` + `FeedNotificationObserver`.

7. **AOP Pattern (Cross-cutting Security Rule)**
   - `@CheckVisibility` + `PostVisibilityAspect`.

8. **Creational Pattern #1: Factory Class**
   - `PostFactory` creates normalized feed posts and exclusive creator posts.
   - `NotificationFactory` creates type-safe follow/message/like/comment notifications.
   - Service layer depends on these factory classes instead of directly constructing domain objects.
   - Why this fits a social app: social events are high-volume and semantic (feed post, exclusive post, like, comment, follow, message). Factory classes centralize creation intent and prevent invalid object combinations at creation time.

9. **Creational Pattern #2: Builder**
   - `Post.builder(authorId)...build()` handles optional media, text, visibility, timestamps, and payload validation.
   - `Notification.builder(recipientId, senderHandle, type)...build()` handles optional post/comment context while enforcing type constraints.
   - Why this fits a social app: post and notification payloads have many optional fields and evolving attributes. Builder keeps object creation readable and safe without large telescoping constructors.

10. **Centralized Exception Handling**

- `GlobalExceptionHandler` with domain exceptions.

Creational pattern scope note:

- We intentionally keep creational patterns limited to exactly these two (`Factory Class` and `Builder`) and avoid introducing other creational patterns.

---

## 4) Suggested 10-Minute Demo Flow (Backend Only)

1. Start with package structure and layered architecture.
2. Show security flow:
   - `SecurityConfig` -> `JwtAuthenticationFilter` -> protected endpoints.
3. Show one feature end-to-end (Post lifecycle):
   - `PostController` -> `PostServiceImpl` -> `PostRepository`.
4. Show design pattern demos:
   - Strategy (`notification` package),
   - Observer (`feed` package),
   - AOP (`PostVisibilityAspect`).
5. Show exception consistency via `GlobalExceptionHandler`.
6. End with honest refactor points (field injection and null-return cleanup).

---

## 5) Backend Files One-by-One (Complete List + Purpose)

### A) Application Entry

- `src/main/java/com/pesocial/PeSocialApplication.java` - Spring Boot entry point.

### B) Config Package

- `src/main/java/com/pesocial/config/JwtAuthenticationFilter.java` - Validates JWT per request and populates Spring Security context.
- `src/main/java/com/pesocial/config/JwtLogoutHandler.java` - Blacklists access token and revokes refresh token on logout.
- `src/main/java/com/pesocial/config/OpenApiConfig.java` - Swagger/OpenAPI configuration with bearer auth schema.
- `src/main/java/com/pesocial/config/SecurityConfig.java` - HTTP security rules, endpoint authorization, CORS, stateless session.
- `src/main/java/com/pesocial/config/WebSocketConfig.java` - STOMP endpoint and broker config, JWT auth for websocket CONNECT.
- `src/main/java/com/pesocial/config/WebSocketHandler.java` - Raw websocket handler for realtime events on `/ws/realtime`.
- `src/main/java/com/pesocial/config/WebSocketHandlerConfig.java` - Registers raw websocket handler endpoint.

### C) Controller Package

- `src/main/java/com/pesocial/controller/AdminController.java` - Admin APIs: moderation, reports, announcements.
- `src/main/java/com/pesocial/controller/AuthController.java` - Auth APIs: register, login, refresh, logout-service.
- `src/main/java/com/pesocial/controller/ChatRoomController.java` - Chat room APIs: create/get room, history, unread, archive/pin.
- `src/main/java/com/pesocial/controller/ChatWebSocketController.java` - WebSocket message mappings for DM, typing, seen, call signaling.
- `src/main/java/com/pesocial/controller/CreatorController.java` - Creator APIs: posts, exclusive posts, monetization toggle, analytics.
- `src/main/java/com/pesocial/controller/GuestController.java` - Public/guest read-only APIs.
- `src/main/java/com/pesocial/controller/HealthController.java` - Health check endpoint.
- `src/main/java/com/pesocial/controller/MediaController.java` - Media upload and media streaming from GridFS.
- `src/main/java/com/pesocial/controller/MessageController.java` - Direct message APIs and attachment upload.
- `src/main/java/com/pesocial/controller/MonetizationController.java` - Monetization APIs: eligibility, revenue, payout, balance.
- `src/main/java/com/pesocial/controller/NotificationController.java` - Notification CRUD/read-status APIs.
- `src/main/java/com/pesocial/controller/PostController.java` - Feed/post APIs: create, edit, delete, like, comment, share, by author.
- `src/main/java/com/pesocial/controller/StoryController.java` - Story APIs: create/view/like/unlike/analytics/delete.
- `src/main/java/com/pesocial/controller/UserController.java` - User profile/follow/search/feed APIs and become-creator endpoint.

### D) DTO Package

- `src/main/java/com/pesocial/dto/StoryDetailDTO.java` - Story view/analytics response payload.
- `src/main/java/com/pesocial/dto/auth/AuthResponse.java` - Auth response containing tokens and user identity fields.
- `src/main/java/com/pesocial/dto/auth/LoginRequest.java` - Login request payload.
- `src/main/java/com/pesocial/dto/auth/LogoutRequest.java` - Logout request payload (refresh token).
- `src/main/java/com/pesocial/dto/auth/RefreshTokenRequest.java` - Access-token refresh request payload.
- `src/main/java/com/pesocial/dto/auth/RegisterRequest.java` - User registration request payload.
- `src/main/java/com/pesocial/dto/error/ErrorResponse.java` - Unified error response structure.
- `src/main/java/com/pesocial/dto/message/ChatRoomDTO.java` - Chat room response with other-user details and unread count.
- `src/main/java/com/pesocial/dto/message/MessageDTO.java` - Message response object for chat history.
- `src/main/java/com/pesocial/dto/message/SendMessageRequest.java` - Send-message request payload.
- `src/main/java/com/pesocial/dto/monetization/MonetizationResult.java` - Revenue computation result payload.
- `src/main/java/com/pesocial/dto/monetization/PayoutResponse.java` - Payout operation result payload.
- `src/main/java/com/pesocial/dto/notification/CreateNotificationRequest.java` - Notification creation request payload.
- `src/main/java/com/pesocial/dto/post/CreatePostRequest.java` - Create-post request payload.
- `src/main/java/com/pesocial/dto/post/EditPostRequest.java` - Edit-post request payload.
- `src/main/java/com/pesocial/dto/post/PostResponseDto.java` - Post response payload for feed/profile APIs.
- `src/main/java/com/pesocial/dto/user/MyProfileDto.java` - Logged-in user profile response with posts.
- `src/main/java/com/pesocial/dto/user/UpdateMyProfileRequest.java` - Self-profile update payload.
- `src/main/java/com/pesocial/dto/user/UserProfileDto.java` - Public profile response object.
- `src/main/java/com/pesocial/dto/user/UserSummaryDto.java` - Compact user response (id/handle/profilePhoto).

### E) Exception Package

- `src/main/java/com/pesocial/exception/AccessDeniedException.java` - Domain-level forbidden access exception.
- `src/main/java/com/pesocial/exception/EntityNotFoundException.java` - Domain-level missing-resource exception.
- `src/main/java/com/pesocial/exception/GlobalExceptionHandler.java` - Maps exceptions to consistent HTTP error responses.
- `src/main/java/com/pesocial/exception/InsufficientFundsException.java` - Domain exception for paywall/monetization access checks.

### F) Model Package

- `src/main/java/com/pesocial/model/Story.java` - Story aggregate with timestamp, viewers, likes, and TTL behavior.
- `src/main/java/com/pesocial/model/analytics/CreatorAnalytics.java` - Analytics aggregate and engagement computation.
- `src/main/java/com/pesocial/model/guest/Guest.java` - Guest domain model.
- `src/main/java/com/pesocial/model/message/ChatRoom.java` - Chat room aggregate with unread counters and room metadata.
- `src/main/java/com/pesocial/model/message/Message.java` - Message aggregate with status/read/reaction/attachment fields.
- `src/main/java/com/pesocial/model/notification/Notification.java` - Notification aggregate.
- `src/main/java/com/pesocial/model/notification/NotificationType.java` - Notification type enum.
- `src/main/java/com/pesocial/model/post/MediaStorageType.java` - Media storage enum (`GRID_FS` / `EXTERNAL_URL`).
- `src/main/java/com/pesocial/model/post/MediaUrl.java` - Embedded media value object and static factory helpers.
- `src/main/java/com/pesocial/model/post/Post.java` - Post aggregate and domain behaviors (edit/like/comment/share).
- `src/main/java/com/pesocial/model/security/RefreshToken.java` - Persisted refresh token model.
- `src/main/java/com/pesocial/model/security/TokenBlocklist.java` - Blacklisted token model (JWT jti + expiry).
- `src/main/java/com/pesocial/model/subscription/Purchase.java` - Purchase model for post/creator paid access.
- `src/main/java/com/pesocial/model/subscription/Subscription.java` - Creator subscription model with active window.
- `src/main/java/com/pesocial/model/system/SystemService.java` - System-level hooks (logging/recommendation/guest limit).
- `src/main/java/com/pesocial/model/user/Admin.java` - Admin user subtype.
- `src/main/java/com/pesocial/model/user/Creator.java` - Creator user subtype with monetization fields.
- `src/main/java/com/pesocial/model/user/RegularUser.java` - Regular user subtype.
- `src/main/java/com/pesocial/model/user/User.java` - Abstract base user model.
- `src/main/java/com/pesocial/model/user/UserRole.java` - User role enum.

### G) Repository Package

- `src/main/java/com/pesocial/repository/ChatRoomRepository.java` - Chat room persistence and custom room lookup methods.
- `src/main/java/com/pesocial/repository/CreatorAnalyticsRepository.java` - Creator analytics persistence.
- `src/main/java/com/pesocial/repository/MessageRepository.java` - Message persistence and unread/history queries.
- `src/main/java/com/pesocial/repository/NotificationRepository.java` - Notification persistence and unread/recent query methods.
- `src/main/java/com/pesocial/repository/PostQueryRepository.java` - Custom post-visibility query abstraction.
- `src/main/java/com/pesocial/repository/PostQueryRepositoryImpl.java` - MongoTemplate implementation of complex visibility queries.
- `src/main/java/com/pesocial/repository/PostRepository.java` - Main post repository (extends custom query repository).
- `src/main/java/com/pesocial/repository/PurchaseRepository.java` - Purchase access query repository.
- `src/main/java/com/pesocial/repository/RefreshTokenRepository.java` - Refresh token persistence and revocation queries.
- `src/main/java/com/pesocial/repository/StoryRepository.java` - Story persistence and active/story-list queries.
- `src/main/java/com/pesocial/repository/SubscriptionRepository.java` - Subscription status/access queries.
- `src/main/java/com/pesocial/repository/TokenBlocklistRepository.java` - Token-blocklist persistence.
- `src/main/java/com/pesocial/repository/UserRepository.java` - User lookup/search repository.

### H) Security Package

- `src/main/java/com/pesocial/security/CheckVisibility.java` - Custom annotation to trigger post-visibility checks.
- `src/main/java/com/pesocial/security/PostVisibilityAspect.java` - AOP aspect enforcing visibility before annotated methods.
- `src/main/java/com/pesocial/security/SecurityUtils.java` - Helpers for current user and role checks.
- `src/main/java/com/pesocial/security/TargetEntity.java` - Enum used by ownership checks.

### I) Service Interfaces

- `src/main/java/com/pesocial/service/AdminService.java` - Admin use-case contract.
- `src/main/java/com/pesocial/service/AuthService.java` - Auth use-case contract.
- `src/main/java/com/pesocial/service/ChatRoomService.java` - Chat room use-case contract.
- `src/main/java/com/pesocial/service/CreatorService.java` - Creator use-case contract.
- `src/main/java/com/pesocial/service/GuestService.java` - Guest use-case contract.
- `src/main/java/com/pesocial/service/MessageService.java` - Messaging use-case contract.
- `src/main/java/com/pesocial/service/MonetizationService.java` - Monetization use-case contract.
- `src/main/java/com/pesocial/service/NotificationService.java` - Notification use-case contract.
- `src/main/java/com/pesocial/service/OwnershipService.java` - Resource ownership/authorization contract.
- `src/main/java/com/pesocial/service/PostService.java` - Post/feed use-case contract.
- `src/main/java/com/pesocial/service/PostVisibilityService.java` - Visibility decision contract.
- `src/main/java/com/pesocial/service/StoryService.java` - Story use-case contract.
- `src/main/java/com/pesocial/service/SubscriptionService.java` - Subscription access-check contract.
- `src/main/java/com/pesocial/service/UserService.java` - User/social graph/profile contract.

### J) Service Pattern Subpackages

- `src/main/java/com/pesocial/service/feed/FollowerObserver.java` - Observer interface for feed publishing events.
- `src/main/java/com/pesocial/service/feed/FeedPublisher.java` - Subject/publisher for new post events.
- `src/main/java/com/pesocial/service/feed/FeedNotificationObserver.java` - Observer implementation that sends notifications on new posts.
- `src/main/java/com/pesocial/service/notification/NotificationStrategy.java` - Strategy interface for notification channel behavior.
- `src/main/java/com/pesocial/service/notification/InAppNotificationStrategy.java` - In-app notification strategy.
- `src/main/java/com/pesocial/service/notification/EmailNotificationStrategy.java` - Email notification strategy.
- `src/main/java/com/pesocial/service/notification/NotificationContext.java` - Strategy selector/context wrapper.
- `src/main/java/com/pesocial/service/security/JwtService.java` - JWT generation/parsing/validation logic.
- `src/main/java/com/pesocial/service/security/RefreshTokenService.java` - Refresh-token lifecycle contract.
- `src/main/java/com/pesocial/service/security/TokenBlocklistService.java` - Access-token blocklist contract.

### K) Service Implementations (`service/impl`)

- `src/main/java/com/pesocial/service/impl/AdminServiceImpl.java` - Admin business logic implementation.
- `src/main/java/com/pesocial/service/impl/AuthServiceImpl.java` - Register/login/refresh/logout implementation.
- `src/main/java/com/pesocial/service/impl/ChatRoomServiceImpl.java` - Chat room orchestration and unread/history logic.
- `src/main/java/com/pesocial/service/impl/CreatorServiceImpl.java` - Creator analytics/monetization/post operations.
- `src/main/java/com/pesocial/service/impl/GuestServiceImpl.java` - Guest access and public content logic.
- `src/main/java/com/pesocial/service/impl/MessageServiceImpl.java` - Message send/read/delete and realtime push logic.
- `src/main/java/com/pesocial/service/impl/MonetizationServiceImpl.java` - Commission/revenue/payout logic.
- `src/main/java/com/pesocial/service/impl/NotificationServiceImpl.java` - Notification persistence + realtime dispatch.
- `src/main/java/com/pesocial/service/impl/OwnershipServiceImpl.java` - Ownership enforcement implementation.
- `src/main/java/com/pesocial/service/impl/PostServiceImpl.java` - Post/feed core implementation.
- `src/main/java/com/pesocial/service/impl/PostVisibilityServiceImpl.java` - Post visibility rules and paywall enforcement.
- `src/main/java/com/pesocial/service/impl/StoryServiceImpl.java` - Story lifecycle and analytics implementation.
- `src/main/java/com/pesocial/service/impl/SubscriptionServiceImpl.java` - Purchase/subscription access checks.
- `src/main/java/com/pesocial/service/impl/UserServiceImpl.java` - User profile, follow graph, creator conversion logic.

### L) Backend Resources and Tests

- `src/main/resources/application.properties` - Runtime config (Mongo URI, JWT settings, monetization settings, OpenAPI paths).
- `src/test/java/com/pesocial/PeSocialApplicationTests.java` - Spring Boot context-load test.

---

## 6) Fast Q&A for Presentation

### Q: Which patterns can you demo live quickly?

1. Strategy pattern (`service/notification`).
2. Observer pattern (`service/feed`).
3. AOP visibility enforcement (`security/PostVisibilityAspect`).
4. Repository pattern with custom implementation (`PostQueryRepositoryImpl`).

### Q: Best files to open first in demo?

1. `src/main/java/com/pesocial/config/SecurityConfig.java`
2. `src/main/java/com/pesocial/controller/PostController.java`
3. `src/main/java/com/pesocial/service/impl/PostServiceImpl.java`
4. `src/main/java/com/pesocial/repository/PostRepository.java`
5. `src/main/java/com/pesocial/repository/PostQueryRepositoryImpl.java`
6. `src/main/java/com/pesocial/security/PostVisibilityAspect.java`
7. `src/main/java/com/pesocial/service/notification/NotificationContext.java`
8. `src/main/java/com/pesocial/service/feed/FeedPublisher.java`

### Q: Where to improve after demo?

- Replace remaining field injection with constructor injection (`StoryController`, `StoryServiceImpl`, `MediaController`).
- Replace `null` returns in story flows with explicit exceptions/results.
- Move remaining heavy controller logic into service classes.
