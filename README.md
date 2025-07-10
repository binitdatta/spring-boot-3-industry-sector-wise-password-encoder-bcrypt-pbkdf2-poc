# Industry-Sector Wise Password Encoding Demo (Spring Boot 3)

This project demonstrates a secure, flexible user authentication system in **Spring Boot 3.5.3** that applies **industry-specific password encoding strategies** based on customer type:

- **Commercial users**: passwords are encoded using **BCrypt**
- **Government users**: passwords are encoded using **PBKDF2WithHmacSHA256** (FIPS-aligned)

It features:
- A login and registration UI built with **Thymeleaf + Bootstrap 5**
- Role-based user handling and secure authentication
- Passwords stored with Spring Security prefixes (`{bcrypt}` / `{pbkdf2}`) for portability
- A **MySQL 8** database with JPA-based `AppUser` and `Customer` entities
- An admin-initialized customer directory rendered dynamically in Thymeleaf
- **Quarkus-ready password verifier** using only `{prefix}` and raw Java crypto — no Spring Security required on consumer side

This architecture allows Spring Boot to serve as the authoritative encoder, while enabling downstream systems (like Quarkus microservices) to validate passwords without pulling in the full Spring stack.

# The Build File 

## Build Overview

Here is the `## Build Overview` section in **Markdown format** for your `README.md`:

---

# Password Encoder Prefix

<table>
  <thead>
    <tr>
      <th>Password Hash</th>
      <th>Role</th>
      <th>Username</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>{bcrypt}$2a$10$53K3YRUwSxc8m/nDP.LNm.q9m/SvExpmFQjrjG0UYypqaoJ54nOVK</td>
      <td>USER</td>
      <td>alice_global</td>
    </tr>
    <tr>
      <td>{bcrypt}$2a$10$cDPlllfsmXqBbj4BMHMpW.xS/r0.igIzEqQ.0mGzl.sbBYPh7DGE6</td>
      <td>ADMIN</td>
      <td>bob_global</td>
    </tr>
  </tbody>
</table>

# How Prefixes Are Handled by Spring :

Spring Security uses **prefixes in encoded passwords** to indicate which algorithm (encoder) was used to hash a password. These prefixes are critical for **delegated password validation** using `DelegatingPasswordEncoder`. Here's a detailed explanation:

---

## 🔐 What Are These Prefixes?

When Spring Security encodes a password using `DelegatingPasswordEncoder`, it prepends a **prefix** to the resulting hash to indicate the algorithm used:

| Encoder                   | Prefix     | Example Prefix in Hash                    |
| ------------------------- | ---------- | ----------------------------------------- |
| `BCryptPasswordEncoder`   | `{bcrypt}` | `{bcrypt}$2a$10$...`                      |
| `Pbkdf2PasswordEncoder`   | `{pbkdf2}` | `{pbkdf2}e0e88fef...`                     |
| `SCryptPasswordEncoder`   | `{scrypt}` | `{scrypt}$e0801$...`                      |
| `Argon2PasswordEncoder`   | `{argon2}` | `{argon2}$argon2id$v=19$m=...`            |
| `NoOpPasswordEncoder`     | `{noop}`   | `{noop}plaintextPassword`                 |
| `SHA256PasswordEncoder`\* | `{sha256}` | `{sha256}...` (only if custom-registered) |

> ✅ These prefixes are automatically added when encoding with `DelegatingPasswordEncoder`.

---

## 🛡️ Why Are Prefixes Useful?

### ✅ 1. **Dynamic Decoder Lookup**

During login, Spring Security doesn't need to know in advance what algorithm was used. It parses the prefix from the stored hash and dynamically delegates the matching to the correct encoder.

```
{bcrypt}$2a$10$X... ⇒ BCryptPasswordEncoder
{pbkdf2}X...         ⇒ Pbkdf2PasswordEncoder
```

### ✅ 2. **Seamless Algorithm Migration**

You can support **multiple password encoders simultaneously**, allowing:

* New users to get stronger hashes (e.g., `PBKDF2`)
* Old users to keep logging in with legacy hashes (e.g., `bcrypt`)
* Auto-upgrade their password hash on successful login

---

## ⚙️ How Matching Works

```
DelegatingPasswordEncoder.matches(rawPassword, storedHash);
```

Internally:

1. Extracts prefix from `storedHash`
2. Looks up matching `PasswordEncoder` from internal map
3. Calls `.matches(raw, storedWithoutPrefix)`

---

## 💡 Example

### Stored password:

```
{pbkdf2}c756f5cb0bc1... (PBKDF2 hash)
```

### Login flow:

* User enters `password123`
* DelegatingPasswordEncoder:

    * Sees `{pbkdf2}`
    * Uses `Pbkdf2PasswordEncoder` to hash `password123`
    * Compares with the stored hash

---

## 🧰 Registering Multiple Encoders

```
Map<String, PasswordEncoder> encoders = Map.of(
    "bcrypt", new BCryptPasswordEncoder(),
    "pbkdf2", new Pbkdf2PasswordEncoder(...),
    "noop", NoOpPasswordEncoder.getInstance()
);

PasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
```

---

## ✅ Summary

* **Prefix = key to decoder** for `DelegatingPasswordEncoder`
* **Supports algorithm agility** and security upgrades
* **Enables backward compatibility** for legacy hashes



```markdown
## 🛠 Build Overview

This project uses **Gradle** (Groovy DSL) with **Spring Boot 3.5.3** and **Java 21**. The build is modular, security-focused, and production-ready.

### 🔧 Plugins

| Plugin                                | Purpose                                                  |
|---------------------------------------|----------------------------------------------------------|
| `org.springframework.boot`            | Enables Spring Boot tasks like `bootRun`, `bootJar`      |
| `io.spring.dependency-management`     | Manages transitive dependency versions via BOM           |
| `java`                                | Applies Java compilation and testing support             |

---

### 📦 Dependencies

#### 🔐 Security

| Dependency                                      | Purpose                                                                 |
|--------------------------------------------------|-------------------------------------------------------------------------|
| `spring-boot-starter-security`                  | Provides full Spring Security support (auth, filters, CSRF, sessions)  |
| `spring-security-crypto`                        | Standalone encoders: `BCrypt`, `PBKDF2`, `DelegatingPasswordEncoder`   |
| `org.bouncycastle:bcprov-jdk18on:1.78`          | FIPS-compliant algorithms (PBKDF2, AES, etc.); 140-3 focus after 2026  |

#### 🌐 Web & MVC

| Dependency                           | Purpose                                       |
|--------------------------------------|-----------------------------------------------|
| `spring-boot-starter-web`           | Spring MVC + embedded Tomcat + Jackson        |
| `spring-boot-starter-thymeleaf`     | Server-side HTML rendering using Thymeleaf    |

#### 🗃 Persistence & DB

| Dependency                           | Purpose                                           |
|--------------------------------------|---------------------------------------------------|
| `spring-boot-starter-data-jpa`      | JPA & Hibernate ORM layer                         |
| `com.mysql:mysql-connector-j`       | MySQL 8+ JDBC driver (runtime only)              |

#### 📈 Observability

| Dependency                          | Purpose                                             |
|-------------------------------------|-----------------------------------------------------|
| `spring-boot-starter-actuator`     | Production metrics, health checks (`/actuator/*`)  |

#### 🧪 Testing

| Dependency                             | Purpose                               |
|----------------------------------------|----------------------------------------|
| `spring-boot-starter-test`             | JUnit 5, Mockito, Spring Boot Test     |

---

### ⚙ Java & Tooling

- **Java Version**: 21
- **Gradle**: Groovy DSL
- **Source Compatibility**: `Java 21`
- **Test Engine**: `JUnit Platform (JUnit 5)`

---

```

## Details

Here's a detailed breakdown of your `build.gradle` file, explaining each **plugin**, **dependency**, and what it contributes to the application:

---

## 🛠️ `plugins { ... }`

```groovy
id 'org.springframework.boot' version '3.5.3'
```

* Applies the Spring Boot Gradle plugin (v3.5.3)
* Enables `bootRun`, `bootJar`, and Spring-specific build enhancements

```groovy
id 'io.spring.dependency-management' version '1.1.4'
```

* Enables Bill of Materials (BOM) support
* Ensures consistent versions of Spring-managed dependencies

```groovy
id 'java'
```

* Applies the standard Java plugin
* Enables Java compilation, JAR packaging, and test support

---

## 📦 Project Metadata

```
group = 'com.rollingstone'
version = '0.0.1-SNAPSHOT'
description = 'Demo project for Spring Boot'
sourceCompatibility = '21'
```

* Uses **Java 21**
* Defines group and version for artifact publishing or classpath resolution

---

## 📡 `repositories { ... }`

```groovy
mavenCentral()
```

* Declares **Maven Central** as the source for dependencies

---

## 📦 `dependencies { ... }`

### ✅ Core Runtime Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

* Enables health checks, metrics, and application monitoring endpoints under `/actuator/*`
* Useful for production monitoring and observability tools (Prometheus, Datadog, etc.)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

* Provides JPA and Hibernate support
* Enables `@Entity`, `JpaRepository`, and auto schema generation (`ddl-auto`)
* Manages DB transactions, queries, etc.

```groovy
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
```

* Integrates **Thymeleaf** as the server-side HTML rendering engine
* Enables `@Controller` → `return "view"` → `templates/*.html`
* Works well with Spring Security and Bootstrap

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
```

* Adds Spring MVC (`@RestController`, `@RequestMapping`, etc.)
* Includes embedded **Tomcat**, Jackson (for JSON), and Spring's web server bootstrap logic

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
```

* Brings full **Spring Security** support: login, sessions, authorization, CSRF, password hashing, etc.
* Supports `@EnableWebSecurity`, `SecurityFilterChain`, and `UserDetailsService`

```groovy
implementation 'org.springframework.security:spring-security-crypto'
```

* Contains standalone password encoders: `BCryptPasswordEncoder`, `Pbkdf2PasswordEncoder`, `DelegatingPasswordEncoder`
* **Does not require Spring Boot** — safe for Quarkus or CLI use
* Used to hash & verify passwords

```groovy
implementation 'org.bouncycastle:bcprov-jdk18on:1.78'
```

* BouncyCastle cryptographic provider
* Adds support for **FIPS 140-2/140-3 algorithms**, e.g., SHA-512, PBKDF2 variants, AES/GCM, etc.
* Can be used to customize or harden PBKDF2 and JWT signing
* 🚨 After **Sept 22, 2026**, FIPS 140-2 will sunset, so track migration to **FIPS 140-3**

---

### 🗃️ Database Driver

```groovy
runtimeOnly 'com.mysql:mysql-connector-j'
```

* MySQL JDBC driver used at runtime for DB access
* Required by JPA to connect to `jdbc:mysql://...` URLs

---

### ✅ Test Dependencies

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

* Brings in JUnit 5, AssertJ, Hamcrest, Mockito, Spring Boot Test context, and web test clients
* Enables `@SpringBootTest`, `@WebMvcTest`, `MockMvc`, etc.

---

## ✅ `test { useJUnitPlatform() }`

* Configures Gradle to use **JUnit 5** platform for all test executions

---

## 📌 Summary Table

| Dependency / Plugin             | Purpose                                                    |
| ------------------------------- | ---------------------------------------------------------- |
| `spring-boot-starter-web`       | Web APIs, MVC controllers, embedded Tomcat                 |
| `spring-boot-starter-security`  | Security filters, password hashing, login form             |
| `spring-security-crypto`        | Password encoders (BCrypt, PBKDF2), safe for Quarkus reuse |
| `spring-boot-starter-thymeleaf` | HTML templating with Bootstrap UI                          |
| `spring-boot-starter-data-jpa`  | JPA/Hibernate ORM layer                                    |
| `spring-boot-starter-actuator`  | Monitoring and observability endpoints                     |
| `mysql-connector-j`             | MySQL JDBC driver                                          |
| `bcprov-jdk18on`                | Advanced cryptography, FIPS-compliant algorithms           |
| `spring-boot-starter-test`      | JUnit 5, Mockito, test framework support                   |

---

# Java Code

## AppUserDetailsService

This class defines a **custom `UserDetailsService` implementation** in Spring Security. It tells Spring **how to load user credentials from your application's database** (in this case, from a `UserRepository` backed by `AppUser` entities).

---

## 🔍 File: `AppUserDetailsService.java`

```
@Service
public class AppUserDetailsService implements UserDetailsService {
```

* Annotated with `@Service` so it's picked up by Spring's component scan
* Implements `UserDetailsService`, which is a **core interface used by Spring Security**
* This is the key hook for **custom user authentication logic**

---

## 🧱 Fields

```
@Autowired
private UserRepository userRepository;
```

* Injects the Spring Data JPA repository (`UserRepository`) that knows how to load users from your database
* Typically backed by a `users` table (`AppUser` entity)

---

## 🔁 Method: `loadUserByUsername(String username)`

```
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
```

* Called automatically by Spring Security during login
* The `username` is whatever the user enters in the login form
* If the username is found, we return a Spring Security-compatible `UserDetails` object

---

## 🔄 Step-by-step Logic

### 1. Find user from DB

```
AppUser user = userRepository.findByUsername(username)
    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
```

* Looks up the user from the database via Spring Data JPA
* If the user doesn't exist, a `UsernameNotFoundException` is thrown → Spring handles this gracefully (login fails)

---

### 2. Convert your domain `AppUser` to Spring's `UserDetails`

```
return User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .roles(user.getRoles()) // COMMERCIAL or GOVERNMENT
        .build();
```

* Spring Security requires a `UserDetails` object
* This builds one using the static builder from `org.springframework.security.core.userdetails.User`
* The `password` must already be hashed (e.g., using `BCrypt` or `PBKDF2`)
* The `roles()` call expects a **comma-separated list of roles** (e.g., `"USER"` or `"ADMIN"`)
* Note: `roles()` will automatically prefix each with `ROLE_` → e.g., `"USER"` → `"ROLE_USER"`

---

## ✅ Why You Need This

Spring Security does **not know how to load users from your database out of the box**.

You must provide a `UserDetailsService` that:

* Knows how to get a user by username
* Returns a `UserDetails` object with username, password, and authorities

This class fulfills that contract.

---

## 📦 Integration Flow

1. User submits login form (`/login`)
2. Spring Security calls `loadUserByUsername(...)`
3. Your DB is queried for the user via `UserRepository`
4. Password is checked by Spring using the configured `PasswordEncoder`
5. If valid, user is authenticated and stored in `SecurityContext`

---

## 🔐 Real-World Enhancements

You could enhance this class to:

* Load roles from a separate table (`UserRole`)
* Support account status (locked, disabled)
* Return custom `UserDetails` objects (with `tenantType`, etc.)

---

## DataInitializer

This class `DataInitializer` is a **Spring Boot startup hook** that pre-loads test/demo users into your database when the application starts.

Let’s break it down for clarity:

---

## 🔍 Class Purpose

```
@Component
public class DataInitializer implements CommandLineRunner
```

* Annotated with `@Component` so Spring bootstraps it automatically
* Implements `CommandLineRunner` → `run(...)` is executed **once on application startup**
* It's typically used for:

    * Initial demo/test data
    * Setup logic
    * Admin/bootstrap accounts

---

## 🧩 Injected Dependencies

```
@Autowired
private UserRepository userRepository;

@Autowired
private TenantPasswordEncoderFactory encoderFactory;
```

* `UserRepository`: Spring Data JPA interface for working with the `AppUser` entity (CRUD + custom queries)
* `TenantPasswordEncoderFactory`: Factory that returns a configured `DelegatingPasswordEncoder`, based on tenant type or global settings (`bcrypt` or `pbkdf2`)

---

## 🔁 Method: `run(...)`

```
public void run(String... args) {
    addUser("alice_global", "password123", "USER");
    addUser("bob_global", "securePass456", "ADMIN");
}
```

* This is executed after the Spring context loads
* Calls `addUser(...)` twice to create two test users: `"alice_global"` and `"bob_global"`

---

## 🔐 Method: `addUser(...)`

```
private void addUser(String username, String rawPassword, String role)
```

This method:

1. Gets the global password encoder via:

   ```
   PasswordEncoder encoder = encoderFactory.getEncoder();
   ```

   This should return a `DelegatingPasswordEncoder` that prefixes the hashed password with `{bcrypt}` or `{pbkdf2}`.

2. Checks if the user already exists:

   ```
   if (userRepository.findByUsername(username).isEmpty()) { ... }
   ```

3. Creates a new `AppUser`, sets its fields:

   ```
   user.setUsername(username);
   user.setPassword(encoder.encode(rawPassword));  // Prefixed-hash like {bcrypt}$2a$...
   user.setRoles(role);
   ```

4. Saves the user to the database:

   ```
   userRepository.save(user);
   ```

---

## ✅ Expected Result

After app startup, your `app_user` table will contain:

| username       | password (hashed)                                         | roles |
| -------------- | --------------------------------------------------------- | ----- |
| `alice_global` | `{bcrypt}$2a$10$...`                                      | USER  |
| `bob_global`   | `{bcrypt}$2a$10$...` or `{pbkdf2}...` depending on config | ADMIN |

These users are now **valid for login** through Spring Security.

---

## 📌 Notes

* Passwords are encoded with a `{prefix}` → required for `DelegatingPasswordEncoder.matches(...)` to succeed.
* If the prefix is missing, Spring Security will throw:

  ```
  IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"
  ```

---

## 🧠 Best Practices

* Wrap this logic in `@Profile("dev")` to avoid inserting test users in production.
* Consider extracting passwords into `application.yml` or `.env` for demo secrets.
* Replace with Flyway/Liquibase for long-term data seeding in production environments.

---

## TenantPasswordEncoderFactory

Here's a clear explanation of your **`TenantPasswordEncoderFactory`** class — a Spring-managed factory that dynamically returns a `DelegatingPasswordEncoder` based on the tenant type (e.g., *commercial* or *government*), ensuring correct password hashing strategy per industry sector.

---

## 🔍 **Purpose**

This factory class allows your Spring Boot application to:

* Automatically pick the correct password encoder (`bcrypt` or `pbkdf2`) based on the tenant type defined in `application.properties` or `application.yml`.
* Return a `DelegatingPasswordEncoder` that properly prefixes hashes with `{bcrypt}` or `{pbkdf2}`, so they can be verified even if encoded by different strategies.

---

## ✅ **Breakdown of the Code**

### 🔐 `GLOBAL_SECRET`

```
private static final String GLOBAL_SECRET = "StrongPepperUsedAcrossAllPBKDF2Hashes";
```

* A **pepper** used in PBKDF2 to strengthen password hashes.
* Acts as an extra secret mixed into the hashing process.
* Best practice in FIPS-compliant environments.

---

### 🧩 Injected Configuration

```
@Value("${app.security.tenant-type:commercial}")
private String tenantType;
```

* Pulls the tenant type from application config (`application.yml` or `.properties`).
* Defaults to `commercial` if not specified.

**Example:**

```yaml
app:
  security:
    tenant-type: government
```

---

### 🏗️ `@PostConstruct` Initialization

```
@PostConstruct
public void init() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();
```

* Initializes a map of encoders *after* the Spring container injects the `tenantType` value.

---

### 🔐 Encoder Configuration

```
encoders.put("bcrypt", new BCryptPasswordEncoder());
encoders.put("pbkdf2", new Pbkdf2PasswordEncoder(
    GLOBAL_SECRET,
    16,
    310000,
    Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
));
```

* **`bcrypt`**: Default encoder for most commercial SaaS systems.
* **`pbkdf2`**:

    * Used by government customers.
    * **FIPS 140-2/3 compliant**, high iteration count (310,000), secure HMAC-SHA-256.
    * NIST-approved: Suitable for FedRAMP, DoD, and similar regulatory needs.

---

### 🎯 Set Default Encoder Dynamically

```
String defaultId = tenantType.equalsIgnoreCase("government") ? "pbkdf2" : "bcrypt";
this.delegatingEncoder = new DelegatingPasswordEncoder(defaultId, encoders);
```

* Picks the default encoder ID based on tenant type.
* `DelegatingPasswordEncoder` prefixes encoded hashes:

    * `{bcrypt}$2a$...`
    * `{pbkdf2}3c0b0e...`

This ensures compatibility during password validation — Spring Security can resolve the right encoder at runtime.

---

### 🔁 Public API

```
public PasswordEncoder getEncoder() {
    return this.delegatingEncoder;
}
```

* Called from services (e.g. during user registration, password reset, etc.).
* You always get the correct encoder for the tenant, without hardcoding any strategy.

---

## 📌 Sample Encoded Passwords

| Tenant Type | Algorithm | Encoded Password Format                         |
| ----------- | --------- | ----------------------------------------------- |
| commercial  | bcrypt    | `{bcrypt}$2a$10$zOgl5FgdcVKzRyCpDH...`          |
| government  | pbkdf2    | `{pbkdf2}KJsd9fk34Jkd...` (base64-encoded hash) |

---

## ✅ Best Practices Followed

* Uses `DelegatingPasswordEncoder` → supports prefix-based decoding.
* NIST/FIPS compliant algorithm (`PBKDF2WithHmacSHA256`).
* Keeps encoding logic tenant-aware but decoupled.
* Centralized password encoding configuration.

---

## 🛡️ Why This Matters

| Feature                            | Value                                      |
| ---------------------------------- | ------------------------------------------ |
| **Multi-Tenant Security Strategy** | Supports custom encoding per tenant type   |
| **Regulatory Compliance**          | Satisfies FIPS 140-2 / FedRAMP for PBKDF2  |
| **Flexibility**                    | Easily extendable with more encoders       |
| **Spring Native Support**          | Integrates smoothly with Spring Security 6 |

---

## SecurityConfig

Here’s a breakdown of your `SecurityConfig` class in Spring Security 6 for a **multi-tenant password encoding** scenario using a custom `TenantPasswordEncoderFactory`:

---

## 🔐 `SecurityConfig` – Overview

This class configures:

1. **Request authorization rules**
2. **Custom login/logout behavior**
3. **DAO-based authentication using tenant-aware encoders**
4. **Disabling CSRF** (useful for form testing; not recommended for production without proper protection)

---

## 🧩 Class Components Explained

### 🔧 `@Configuration` + `@EnableWebSecurity`

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {
```

* Declares this as a Spring Security configuration class.
* Enables Spring Security filter chain auto-wiring.

---

### 🧱 Dependencies Injected

```
@Autowired
TenantPasswordEncoderFactory encoderFactory;

@Autowired
AppUserDetailsService userDetailsService;
```

* `TenantPasswordEncoderFactory` provides a **DelegatingPasswordEncoder** with tenant-specific strategies (`bcrypt`, `pbkdf2`, etc.).
* `AppUserDetailsService` loads user credentials from DB (implements `UserDetailsService`).

---

### 🔐 Security Filter Chain

```
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
```

Defines the entire **HTTP security** behavior.

#### ✅ Request Authorization

```
.authorizeHttpRequests(authz -> authz
        .requestMatchers("/login", "/error").permitAll()
        .anyRequest().authenticated()
)
```

* Allows anonymous access to login and error pages.
* Requires authentication for everything else.

#### 🔓 Form-Based Login

```
.formLogin(form -> form
        .loginPage("/login")
        .defaultSuccessUrl("/home", true)
)
```

* Custom login page at `/login`.
* Upon successful login, always redirects to `/home`.

#### 🔚 Logout Behavior

```
.logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login")
        .permitAll()
)
```

* Allows anyone to hit `/logout`.
* Redirects to `/login` after logout.

#### 🚫 CSRF Disabled

```
.csrf(csrf -> csrf.disable()); // ❗ Dev only
```

* **Important**: Disables CSRF protection — only safe for local dev/testing.
* You should **enable CSRF for forms** in production or use CSRF tokens in AJAX requests.

---

### 🔐 Authentication Manager Configuration

```
@Bean
public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(encoderFactory.getEncoder());
    return new ProviderManager(provider);
}
```

* Creates a `DaoAuthenticationProvider` that:

    * Looks up users via `AppUserDetailsService`
    * Validates passwords using the correct encoder (`bcrypt` or `pbkdf2`) selected from the factory
* `ProviderManager` is the main `AuthenticationManager` for this configuration.

---

## ✅ Example Password Handling

* A user registered under **commercial**:

  ```plaintext
  password = {bcrypt}$2a$10$...
  ```
* A user under **government**:

  ```plaintext
  password = {pbkdf2}abcd...xyz
  ```

The prefix `{bcrypt}` or `{pbkdf2}` is automatically added during registration and respected by Spring during login via the `DelegatingPasswordEncoder`.

---

## 🛡️ Security Best Practices

| Practice              | Status                                     |
| --------------------- | ------------------------------------------ |
| CSRF Enabled          | ❌ (You should enable for prod)             |
| Passwords Encrypted   | ✅                                          |
| Role-Based Access     | ⚠️ Not configured yet (optional next step) |
| Logout Path           | ✅ Custom `/logout`                         |
| Tenant-Specific Logic | ✅ Done via factory                         |

---

## 🚀 Want to Improve Further?

* Enable CSRF properly:

  ```
  .csrf(Customizer.withDefaults())
  ```
* Add role-based access:

  ```
  .authorizeHttpRequests(authz -> authz
      .requestMatchers("/admin/**").hasRole("ADMIN")
      .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
      .anyRequest().authenticated()
  )
  ```
* Add `rememberMe()` config if needed.

---

## JPA (Does not Much Explanation)

``` 
package com.rollingstone.model;

import jakarta.persistence.*;

@Entity
@Table(name="APP_USER")
public class AppUser {
    @Id
    private String username;
    private String password;
    private String roles; // comma-separated (e.g., "USER,ADMIN")

    public AppUser() {
    }

    public AppUser(String username, String password, String roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                '}';
    }
}


```

## Customer

``` 
package com.rollingstone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String country;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // No-arg constructor
    public Customer() {}

    public Customer(Long id, String name, String email, String phone, String country, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```

## 

``` 
package com.rollingstone.repository;

import com.rollingstone.model.AppUser;
import com.rollingstone.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);  // ✅ valid
    List<Customer> findByName(String name);        // ✅ valid
}


```

## 

``` 
package com.rollingstone.repository;

import com.rollingstone.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByUsername(String username);

}

```

## LoginService

``` 
package com.rollingstone.service;

import com.rollingstone.config.TenantPasswordEncoderFactory;
import com.rollingstone.model.AppUser;
import com.rollingstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantPasswordEncoderFactory encoderFactory;

    public AppUser register(String username, String rawPassword) {
        PasswordEncoder encoder = encoderFactory.getEncoder();  // Uses globally-configured encoder
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setRoles("ROLE_USER");

        return userRepository.save(user);
    }
}

```

# HTML Thymeleaf

## login.html (under src/main/resources/templates)

``` 
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body, html {
            height: 100%;
        }
        .login-wrapper {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
    </style>
</head>
<body class="bg-light">
<div class="container login-wrapper">
    <div class="card shadow-lg border-0" style="width: 100%; max-width: 420px;">
        <div class="card-header bg-primary text-white text-center">
            <h4 class="my-1">🔐 Secure Login</h4>
        </div>
        <div class="card-body p-4">
            <form th:action="@{/login}" method="post">
                <div class="mb-3">
                    <label for="username" class="form-label">👤 Username</label>
                    <input type="text" class="form-control" name="username" placeholder="Enter username" required />
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label">🔑 Password</label>
                    <input type="password" class="form-control" name="password" placeholder="Enter password" required />
                </div>
                <button type="submit" class="btn btn-success w-100">🚀 Login</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>

```

## home.html

``` 
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Home | Welcome</title>
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .card-hover:hover {
            transform: translateY(-2px);
            transition: all 0.2s ease-in-out;
            box-shadow: 0 0 15px rgba(0, 123, 255, 0.2);
        }
    </style>
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="text-center mb-5">
        <h2 class="text-primary fw-bold">👋 Welcome, <span th:text="${username}">User</span>!</h2>
        <p class="text-muted">Glad to see you back. Choose what you’d like to do:</p>
    </div>

    <div class="row justify-content-center g-4">
        <div class="col-md-4">
            <a href="/customers" class="text-decoration-none">
                <div class="card card-hover border-0 shadow-sm h-100">
                    <div class="card-body text-center">
                        <div class="display-4 text-info">📋</div>
                        <h5 class="card-title mt-3">View Customers</h5>
                        <p class="card-text text-muted">Access your full customer directory</p>
                    </div>
                </div>
            </a>
        </div>

        <div class="col-md-4">
            <a href="/encryption" class="text-decoration-none">
                <div class="card card-hover border-0 shadow-sm h-100">
                    <div class="card-body text-center">
                        <div class="display-4 text-success">🔐</div>
                        <h5 class="card-title mt-3">Encryption Tool</h5>
                        <p class="card-text text-muted">Encrypt and validate your secure texts</p>
                    </div>
                </div>
            </a>
        </div>

        <div class="col-md-4">
            <a href="/logout" class="text-decoration-none">
                <div class="card card-hover border-0 shadow-sm h-100">
                    <div class="card-body text-center">
                        <div class="display-4 text-danger">🚪</div>
                        <h5 class="card-title mt-3">Logout</h5>
                        <p class="card-text text-muted">Securely exit your session</p>
                    </div>
                </div>
            </a>
        </div>
    </div>
</div>
</body>
</html>

```

## customers.html

``` 
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Customers</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="text-primary">📋 Customer List</h3>
        <a href="/home" class="btn btn-outline-secondary">🏠 Back to Home</a>
    </div>
    <div class="card shadow-sm border-0">
        <div class="card-body p-4">
            <table class="table table-hover align-middle" id="customer-table">
                <thead class="table-light">
                <tr>
                    <th scope="col">🆔 ID</th>
                    <th scope="col">👤 Name</th>
                    <th scope="col">📧 Email</th>
                    <th scope="col">📱 Phone</th>
                    <th scope="col">🌐 Country</th>
                    <th scope="col">🕓 Created At</th>
                </tr>
                </thead>
                <tbody class="table-group-divider">
                <tr th:each="cust : ${customers}">
                    <td th:text="${cust.id}">1</td>
                    <td th:text="${cust.name}">Alice</td>
                    <td th:text="${cust.email}">alice@example.com</td>
                    <td th:text="${cust.phone}">123-456-7890</td>
                    <td th:text="${cust.country}">US</td>
                    <td th:text="${#temporals.format(cust.createdAt, 'yyyy-MM-dd HH:mm')}">2025-07-01</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>

```

# VIP The Other Side of Midnight (Sydney Sheldon) : How to do it in KeyCloak Quarkus

To support validating **Spring Security-prefixed passwords** in Keycloak via a custom `RemoteUserStorageProvider`, you can follow **two different strategies** depending on whether you want to:

* ✅ #1 Use Spring Security's `DelegatingPasswordEncoder` via Spring dependencies
* ✅ #2 Use **pure Quarkus Java crypto** to manually handle decoding

This enables **Keycloak**, running in **Quarkus (Docker mode)**, to remotely authenticate users stored in a MySQL database **with prefixed hashed passwords like `{bcrypt}...` or `{pbkdf2}...`** generated by a Spring Boot application.

---

## ✅ **Context Recap**

Your MySQL table (e.g., `users`) has:

| username      | password                           | roles |
| ------------- | ---------------------------------- | ----- |
| alice\_global | `{bcrypt}$2a$10$53K3YRUwSxc8m/...` | USER  |
| bob\_global   | `{pbkdf2}e4cfe4...`                | ADMIN |

---

# 🔐 Keycloak: Custom RemoteUserStorageProvider

Keycloak lets you write a **custom SPI (`UserStorageProvider`)** to delegate user lookup and password validation to external systems like your Spring Boot DB.

Create:

```
public class RemoteUserStorageProvider implements UserStorageProvider, CredentialInputValidator {
    ...
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        ...
    }
}
```

---

# ⚙️ Approach #1: Use Spring Security inside Keycloak SPI (with Spring Dependencies)

> Use Spring’s native `DelegatingPasswordEncoder` inside your `isValid` method.

### ✅ Dependencies (in your Keycloak SPI module):

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.security:spring-security-crypto'
```

### ✅ Code:

```
public class RemoteUserStorageProvider implements UserStorageProvider, CredentialInputValidator {

    private final Map<String, PasswordEncoder> encoders;
    private final DelegatingPasswordEncoder delegating;

    public RemoteUserStorageProvider(...) {
        encoders = Map.of(
            "bcrypt", new BCryptPasswordEncoder(),
            "pbkdf2", new Pbkdf2PasswordEncoder("StrongPepperUsedAcrossAllPBKDF2Hashes", 16, 310000,
                            Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256)
        );
        delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) return false;

        UserCredentialModel credential = (UserCredentialModel) input;
        String username = user.getUsername();
        String rawPassword = credential.getChallengeResponse();

        // Fetch user and hashed password from external DB
        String hashedPassword = userService.fetchHashedPasswordByUsername(username); // e.g. "{pbkdf2}..."

        return delegating.matches(rawPassword, hashedPassword);
    }
}
```

### ✅ Benefits:

* 1:1 compatibility with Spring Boot-hashed passwords
* Minimal logic to maintain

---

# 🧼 Approach #2: Pure Quarkus, No Spring Dependencies

> Parse the `{prefix}` manually and dispatch to a native encoder.

### 🔨 Required dependencies:

```groovy
implementation 'org.bouncycastle:bcprov-jdk18on:1.78' // For PBKDF2
```

### ✅ Code:

```
@Override
public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
    String rawPassword = input.getChallengeResponse();
    String hashedPassword = userService.fetchHashedPasswordByUsername(user.getUsername());

    if (hashedPassword.startsWith("{bcrypt}")) {
        return BCrypt.checkpw(rawPassword, hashedPassword.substring("{bcrypt}".length()));
    } else if (hashedPassword.startsWith("{pbkdf2}")) {
        return Pbkdf2Util.verifyPasswordPBKDF2(rawPassword, hashedPassword.substring("{pbkdf2}".length()));
    }

    return false;
}
```

### Utility for PBKDF2:

```
public class Pbkdf2Util {

    private static final String SECRET = "StrongPepperUsedAcrossAllPBKDF2Hashes";
    private static final int ITERATIONS = 310_000;
    private static final int KEY_LENGTH = 256;

    public static boolean verifyPasswordPBKDF2(String rawPassword, String encodedPassword) {
        // Implement your own PBKDF2 decoding logic using javax.crypto or Bouncy Castle
        // You need to store the salt and parameters in encodedPassword or in DB
        // Use Mac.getInstance("PBKDF2WithHmacSHA256") etc.
        return false; // stub
    }
}
```

### ✅ Benefits:

* Zero Spring dependencies
* Fully native to Quarkus/Keycloak

### ⚠️ Challenges:

* PBKDF2 manual implementation is **error-prone** if the hash format is custom
* You must **store salt separately** or embed it (Spring encodes salt internally)

---

## 🆚 Comparison

| Aspect                   | Spring-based Approach              | Pure Quarkus (No Spring)        |
| ------------------------ | ---------------------------------- | ------------------------------- |
| ✅ Compatibility          | 100% with Spring Boot              | Requires exact hash match logic |
| 🔄 Password Upgrade Path | Easy via Spring Security utilities | Manual                          |
| 🧩 Dependencies          | `spring-security-crypto`, etc.     | BouncyCastle or JDK crypto APIs |
| 🧠 Complexity            | Low                                | Moderate to high                |
| 🚀 Runtime size (Docker) | Slightly larger due to Spring jars | Smaller with only BouncyCastle  |

---

## 🧪 Testing Tips

* ✅ Use Spring Boot to insert both `{bcrypt}` and `{pbkdf2}` users
* 🔒 Enable SPI logging: `log.level.org.keycloak.storage=DEBUG`
* 📦 Package as a JAR and mount it under `$KEYCLOAK_HOME/providers`

---

