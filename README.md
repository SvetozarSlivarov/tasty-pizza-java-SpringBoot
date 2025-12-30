# 🍕 Tasty Pizza – Spring Boot Backend

A **production-grade RESTful backend** for an online pizza ordering platform, built with **Spring Boot** and designed following real-world backend best practices.

This project focuses on **security, scalability, and clean architecture**, supporting both **authenticated users** and **guest users**, with robust cart handling, JWT authentication, and role-based access control.

---

## 📖 Project Overview

Tasty Pizza is a backend service that powers an online food ordering system.  
It exposes a REST API consumed by a frontend application (web or mobile).

Key goals of the project:
- Clean separation of concerns
- Secure authentication and authorization
- Realistic e-commerce cart & order logic
- Production-style configuration and logging

---

## ✨ Core Features

- User registration & login
- JWT authentication (Access + Refresh tokens)
- Refresh token stored in **HttpOnly cookie**
- Stateless security with Spring Security
- Role-based authorization (USER / ADMIN)
- Guest shopping cart via `cart_token`
- Cart persistence for logged-in users
- Guest → User cart merge after login
- Order creation & order history
- Product & category management
- Image upload via Cloudinary
- Centralized exception handling
- CORS configuration for SPA frontends
- Structured logging with trace identifiers

---

## 🛠️ Technology Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)

### Database
- MySQL

### Security
- JWT (HS256)
- BCrypt password hashing
- HttpOnly & Secure cookies

### Tooling
- Maven
- Lombok
- Cloudinary (media storage)

---

## 🧱 Architecture Overview

```
Client
  ↓
Controller Layer
  ↓
Service Layer
  ↓
Repository Layer
  ↓
Database
```

---

## 🔐 Authentication & Authorization

Uses **Access Token + Refresh Token** strategy.
Refresh token is stored in an **HttpOnly cookie** to prevent XSS attacks.

---

## 🛒 Shopping Cart Logic

Supports both **guest carts** (via `cart_token`) and **user carts**.
Guest carts are merged into user carts upon login.

---

## 🗃️ Data Models (Entities & Relationships)

Below is a clearer, entity-based overview of the main domain models (based on the actual JPA entities).

> Notes
> - Most “catalog” entities support **soft delete** using `deleted` + `deletedAt`.
> - Some fields reference enums (e.g. `ProductType`, `OrderStatus`, `UserRole`, `PizzaSize`, `DoughType`, `SpicyLevel`).

### User
Represents an account that can place orders.

**Key fields**
- `id: Long`
- `fullname: String`
- `username: String` (login identifier, depending on frontend usage)
- `password: String` (BCrypt-hashed)
- `role: UserRole` (e.g. USER / ADMIN)
- `tokenVersion: int` (commonly used to invalidate refresh tokens)
- `createdAt: LocalDateTime`
- `deleted: boolean`, `deletedAt: LocalDateTime`

**Relationships**
- `User 1 → * Order` (`orders`)

---

### Product (base catalog item)
A generic sellable item. Specialized products (Pizza, Drink) are linked via composition.

**Key fields**
- `id: Long`
- `type: ProductType` (e.g. PIZZA / DRINK / etc.)
- `name: String`
- `description: String`
- `basePrice: BigDecimal`
- `imageUrl: String`
- `createdAt: LocalDateTime`
- `deleted: boolean`, `deletedAt: LocalDateTime`

**Used by**
- `Pizza.product`
- `Drink.product`
- `OrderItem.product` (snapshot/reference for what was ordered)

---

### Pizza (product specialization)
Stores pizza-specific attributes and ingredient configuration.

**Key fields**
- `id: Long`
- `spicyLevel: SpicyLevel` (enum)
- `product: Product`

**Relationships**
- `Pizza 1 → * PizzaVariant` (different size/dough combinations)
- `Pizza 1 → * PizzaIngredient` (default ingredients on the pizza)
- `Pizza 1 → * PizzaAllowedIngredient` (what can be added/removed + extra price rules)

---

### PizzaVariant
Defines purchasable variants of a pizza (size + dough) and price adjustments.

**Key fields**
- `id: Long`
- `size: PizzaSize`
- `dough: DoughType`
- `extraPrice: BigDecimal` (added on top of `Product.basePrice`)
- `pizza: Pizza`

---

### IngredientType
Groups ingredients (e.g. cheese, meat, veggies, sauces).

**Key fields**
- `id: Long`
- `name: String`

**Relationships**
- `IngredientType 1 → * Ingredient`

---

### Ingredient
Represents a single ingredient option that can appear on pizzas or be used in customizations.

**Key fields**
- `id: Long`
- `name: String`
- `type: IngredientType`
- `deleted: boolean`, `deletedAt: LocalDateTime`

---

### PizzaIngredient (default pizza ingredients)
Defines which ingredients are included by default in a given pizza and whether they are removable.

**Key fields**
- `id: Long`
- `pizza: Pizza`
- `ingredient: Ingredient`
- `removable: boolean`

---

### PizzaAllowedIngredient (customization rules)
Defines what ingredients are allowed to be added to a specific pizza and the extra price for adding them.

**Key fields**
- `id: Long`
- `pizza: Pizza`
- `ingredient: Ingredient`
- `extraPrice: BigDecimal`

---

### Drink (product specialization)
A simple specialization for drinks.

**Key fields**
- `id: Long`
- `product: Product`

---

### Order
Represents a completed checkout. Supports both authenticated and guest checkouts.

**Key fields**
- `id: Long`
- `user: User` *(nullable if guest checkout)*
- `guestToken: String` *(used when not logged in)*
- `status: OrderStatus`
- `deliveryPhone: String`
- `deliveryAddress: String`
- `createdAt: LocalDateTime`

**Relationships**
- `Order 1 → * OrderItem` (`items`)
- `Order 1 → * OrderStatusChange` (`statusChanges`)

---

### OrderItem
A single line item inside an order (product + quantity), including optional customizations.

**Key fields**
- `id: Long`
- `order: Order`
- `product: Product`
- `pizzaVariantId: Long` *(if the item is a pizza variant; stored as id reference)*
- `quantity: int`
- `price: BigDecimal` *(unit or line price depending on service logic)*
- `comment: String` *(free-text note, if supported)*

**Relationships**
- `OrderItem 1 → * OrderItemCustomization` (`customizations`)

---

### OrderItemCustomization
Captures per-item ingredient actions (e.g. remove an ingredient, add extra ingredient).

**Key fields**
- `id: Long`
- `orderItem: OrderItem`
- `ingredient: Ingredient`
- `action: OrderItemCustomizationAction` (enum, ADD / REMOVE)

---

### OrderStatusChange
Keeps a history of order status transitions.

**Key fields**
- `id: Long`
- `order: Order`
- `status: OrderStatus`
- `changedAt: LocalDateTime`

---

## 🌐 API Overview

Below is a controller-derived overview of the available REST endpoints. Paths are listed as they appear in the code.

**Authentication:** typically via `Authorization: Bearer <access_token>` header. Refresh token is handled via HttpOnly cookie.

### Authentication
Login/registration and token lifecycle (access + refresh).

- `POST` **/api/auth/login**   — Body: `LoginRequest request`
- `POST` **/api/auth/logout**
- `POST` **/api/auth/refresh**
- `POST` **/api/auth/register**  — Body: `RegisterRequest request`

### Users
- `GET` **/api/users/me** 
- `PUT` **/api/users/me**   — Body: `UpdateUserRequest request`
- `PATCH` **/api/users/me/fullname**   — Body: `UpdateFullNameRequest req`
- `PATCH` **/api/users/me/password**   — Body: `ChangePasswordRequest req`
- `PATCH` **/api/users/me/username**   — Body: `UpdateUsernameRequest req`

### Cart
Guest cart is tracked by `cart_token` (HttpOnly cookie). Many endpoints implicitly create the cookie if missing.

- `GET` **/api/cart** 
- `POST` **/api/cart/checkout**   — Body: `CheckoutRequest request`
- `POST` **/api/cart/items/drink**   — Body: `AddDrinkToCartRequest request`
- `POST` **/api/cart/items/pizza**   — Body: `AddPizzaToCartRequest request`
- `DELETE` **/api/cart/items/{itemId}**   — Path: `Long itemId`
- `PATCH` **/api/cart/items/{itemId}**   — Path: `Long itemId` | Body: `UpdateCartItemRequest request`

### Orders (User)
User-facing order endpoints (reorder, own orders, status history).

- `GET` **/api/orders/my** 
- `POST` **/api/orders/{id}/reorder**   — Path: `Long id`
- `GET` **/api/orders/{id}/statusHistory**   — Path: `Long id`

### Orders (Admin Actions)
Administrative actions for changing order status and workflow transitions.

- `POST` **/api/orders/{id}/cancel** _Access:_ **ADMIN**  — Path: `Long id`
- `POST` **/api/orders/{id}/deliver** _Access:_ **ADMIN**  — Path: `Long id`
- `POST` **/api/orders/{id}/out-for-delivery** _Access:_ **ADMIN**  — Path: `Long id`
- `POST` **/api/orders/{id}/start-preparing** _Access:_ **ADMIN**  — Path: `Long id`

### Admin Orders
Admin listing and inspection of all orders with filtering and pagination.

- `GET` **/api/admin/orders** _Access:_ **ADMIN**  — Query: `String status`, `String q`, `Long userId`
- `GET` **/api/admin/orders/{id}** _Access:_ **ADMIN**  — Path: `Long id`

### Admin Users
Admin moderation and user management.

- `GET` **/api/admin/users** _Access:_ **ADMIN**  — Query: `String q`, `String show`, `int page`, `int size`
- `DELETE` **/api/admin/users/{id}** _Access:_ **ADMIN**  — Path: `Long id`
- `POST` **/api/admin/users/{id}/restore** _Access:_ **ADMIN**  — Path: `Long id`
- `PATCH` **/api/admin/users/{id}/role** _Access:_ **ADMIN**  — Path: `Long id` | Body: `UpdateUserRoleRequest req`

### Pizzas
- `GET` **/api/pizzas**   — Query: `boolean withVariants`
- `GET` **/api/pizzas/{id}**  — Path: `Long id`
- `POST` **/api/pizzas** _Access:_ **ADMIN**  — Body: `PizzaRequest request`
- `GET` **/api/pizzas/deleted** _Access:_ **ADMIN**  — Query: `boolean withVariants`
- `DELETE` **/api/pizzas/{id}** _Access:_ **ADMIN**  — Path: `Long id`
- `GET` **/api/pizzas/{id}** _Access:_ **ADMIN**  — Path: `Long id`
- `PUT` **/api/pizzas/{id}** _Access:_ **ADMIN**  — Path: `Long id` | Body: `PizzaRequest request`
- `POST` **/api/pizzas/{id}/restore** _Access:_ **ADMIN**  — Path: `Long id`

### Pizza Ingredients
- `GET` **/api/pizzas/{pizzaId}/ingredients**  — Path: `Long pizzaId`
- `POST` **/api/pizzas/{pizzaId}/ingredients** _Access:_ **ADMIN**  — Path: `Long pizzaId` | Body: `PizzaIngredientRequest request`
- `DELETE` **/api/pizzas/{pizzaId}/ingredients/{id}** _Access:_ **ADMIN**  — Path: `Long pizzaId`, `Long id`
- `PUT` **/api/pizzas/{pizzaId}/ingredients/{id}** _Access:_ **ADMIN**  — Path: `Long pizzaId`, `Long id` | Body: `PizzaIngredientRequest request`

### Pizza Allowed Ingredients
- `GET` **/api/pizzas/{pizzaId}/allowed-ingredients**   — Path: `Long pizzaId`
- `POST` **/api/pizzas/{pizzaId}/allowed-ingredients** _Access:_ **ADMIN**  — Path: `Long pizzaId` | Body: `PizzaAllowedIngredientRequest request`
- `DELETE` **/api/pizzas/{pizzaId}/allowed-ingredients/{id}** _Access:_ **ADMIN**  — Path: `Long pizzaId`, `Long id`
- `PUT` **/api/pizzas/{pizzaId}/allowed-ingredients/{id}** _Access:_ **ADMIN**  — Path: `Long pizzaId`, `Long id` | Body: `PizzaAllowedIngredientRequest request`

### Drinks
- `GET` **/api/drinks** 
- `GET` **/api/drinks/{id}** — Path: `Long id`
- `POST` **/api/drinks** _Access:_ **ADMIN**  — Body: `DrinkRequest request`
- `GET` **/api/drinks/deleted** _Access:_ **ADMIN**
- `DELETE` **/api/drinks/{id}** _Access:_ **ADMIN**
- `GET` **/api/drinks/{id}** _Access:_ **ADMIN**
- `PUT` **/api/drinks/{id}** _Access:_ **ADMIN**  — Body: `DrinkRequest request`
- `POST` **/api/drinks/{id}/restore** _Access:_ **ADMIN**

### Ingredients
- `GET` **/api/ingredients**   — Query: `String show`
- `POST` **/api/ingredients** _Access:_ **ADMIN**  — Body: `IngredientRequest dto`
- `GET` **/api/ingredients/with-type**   — Query: `String show`
- `DELETE` **/api/ingredients/{id}** _Access:_ **ADMIN**  — Path: `Long id`
- `GET` **/api/ingredients/{id}**   — Path: `Long id`
- `PUT` **/api/ingredients/{id}** _Access:_ **ADMIN**  — Path: `Long id` | Body: `IngredientRequest dto`
- `POST` **/api/ingredients/{id}/restore** _Access:_ **ADMIN**  — Path: `Long id`

### Ingredient Types
- `DELETE` **/api/ingredient-type** _Access:_ **ADMIN**  — Body: `IngredientTypeRequest dto`
- `GET` **/api/ingredient-type**
- `POST` **/api/ingredient-type** _Access:_ **ADMIN**  — Body: `IngredientTypeRequest dto`
- `DELETE` **/api/ingredient-type/{id}** _Access:_ **ADMIN**  — Path: `Long id`
- `GET` **/api/ingredient-type/{id}**  — Path: `Long id`
- `PUT` **/api/ingredient-type/{id}** _Access:_ **ADMIN**  — Path: `Long id` | Body: `IngredientTypeRequest dto`


---

## ▶️ Running the Application

```
mvn clean install
mvn spring-boot:run
```

## 👤 Default Administrator Account

On application startup, the system checks whether the users table contains any records.

If the table is empty, a default administrator account is automatically created.

If at least one user already exists, this step is skipped.

### Default admin credentials:

- **Username: admin**

- **Password: admin123**

---

## 📄 License

MIT License
