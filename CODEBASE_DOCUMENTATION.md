# Todo-App Codebase Documentation

This document contains a complete, detailed snapshot of the codebase for the `todo-app` project. It includes all important source files, configuration, build instructions, database schema, and notes necessary to prepare slides for a presentation. No details have been omitted.

---

## Project Overview

- **Artifact**: todo-app (WAR)
- **Language**: Java (Jakarta Servlet/JSP)
- **Build tool**: Maven
- **Purpose**: Simple Todo web application demonstrating CRUD-like operations (add, list, toggle complete, delete) using a MySQL backend and JSP UI.

---

## File List (key files)

- `pom.xml` - Maven project file and dependencies.
- `src/main/java/com/todolist/DBConnection.java` - DB connection helper.
- `src/main/java/com/todolist/Todo.java` - Model representing a todo item.
- `src/main/java/com/todolist/TodoDAO.java` - Data access object with CRUD operations.
- `src/main/java/com/todolist/TodoServlet.java` - Servlet handling HTTP requests.
- `src/main/webapp/index.jsp` - Redirects to servlet URL.
- `src/main/webapp/list.jsp` - JSP page rendering the todo list and add form.
- `src/main/webapp/WEB-INF/web.xml` - Servlet mapping and web app descriptor.

---

## Build & Run Instructions

1. Build with Maven:

```bash
mvn clean package
```

2. The build produces `target/todo-app.war`. Deploy this WAR to a Jakarta EE / Tomcat (or compatible) servlet container by copying it to the `webapps/` directory or using your container's deployment UI.

3. Application URL (after deploying to Tomcat at default context):

```
http://localhost:8080/todo-app/
```

4. The app expects a MySQL database available at `jdbc:mysql://localhost:3306/todo_db` with credentials configured in `DBConnection.java` (default `root` / `password`). Update `DBConnection.java` or provide environment-specific overrides before deploying.

Optional: Run a local MySQL (Docker):

```bash
docker run --name todo-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=todo_db -p 3306:3306 -d mysql:8
# then create table (see schema below)
```

---

## Database Schema

Create the `todos` table used by the application:

```sql
CREATE TABLE todos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  is_completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Notes:
- Column names used in code: `id`, `title`, `is_completed`, `created_at`.

---

## Full Source Files

Below are the full contents of each primary source and configuration file in the project.

### `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.todo</groupId>
  <artifactId>todo-app</artifactId>
  <packaging>war</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>todo-app Maven Webapp</name>
  <url>http://maven.apache.org</url>
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <version>6.0.0</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>jakarta.servlet.jsp</groupId>
      <artifactId>jakarta.servlet.jsp-api</artifactId>
      <version>3.1.0</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>jakarta.servlet.jsp.jstl</groupId>
      <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
      <version>3.0.0</version>
    </dependency>
    <!-- JSTL implementation (required at runtime for JSP taglibs) -->
    <dependency>
      <groupId>org.glassfish.web</groupId>
      <artifactId>jakarta.servlet.jsp.jstl</artifactId>
      <version>3.0.0</version>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.4.0</version>
    </dependency>
  </dependencies>
  <build>
    <finalName>todo-app</finalName>
  </build>
</project>
```

---

### `src/main/java/com/todolist/DBConnection.java`

```java
package com.todolist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Match these to your Docker container settings!
    private static final String URL = "jdbc:mysql://localhost:3306/todo_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
```

Notes:
- Update `URL`, `USER`, `PASS` to match your environment. The code loads the MySQL driver explicitly.

---

### `src/main/java/com/todolist/Todo.java`

```java
package com.todolist;

import java.sql.Timestamp;

public class Todo {
    private int id;
    private String title;
    private boolean isCompleted;
    private Timestamp createdAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
```

---

### `src/main/java/com/todolist/TodoDAO.java`

```java
package com.todolist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO {
    
    // Get all todos
    public List<Todo> getAllTodos() {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todos ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Todo todo = new Todo();
                todo.setId(rs.getInt("id"));
                todo.setTitle(rs.getString("title"));
                todo.setCompleted(rs.getBoolean("is_completed"));
                todo.setCreatedAt(rs.getTimestamp("created_at"));
                todos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todos;
    }

    // Add new todo
    public void addTodo(String title) {
        String sql = "INSERT INTO todos (title) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Toggle completion status
    public void toggleTodo(int id, boolean isCompleted) {
        String sql = "UPDATE todos SET is_completed = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isCompleted);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete todo
    public void deleteTodo(int id) {
        String sql = "DELETE FROM todos WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

### `src/main/java/com/todolist/TodoServlet.java`

```java
package com.todolist;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TodoServlet extends HttpServlet {
    private TodoDAO todoDAO = new TodoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            // Default: Show all todos
            List<Todo> todos = todoDAO.getAllTodos();
            request.setAttribute("todos", todos);
            request.getRequestDispatcher("/list.jsp").forward(request, response);
            
        } else if (action.equals("delete")) {
            int id = Integer.parseInt(request.getParameter("id"));
            todoDAO.deleteTodo(id);
            response.sendRedirect(request.getContextPath() + "/app");
            
        } else if (action.equals("toggle")) {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean isCompleted = Boolean.parseBoolean(request.getParameter("isCompleted"));
            todoDAO.toggleTodo(id, !isCompleted); // Toggle the current state
            response.sendRedirect(request.getContextPath() + "/app");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String title = request.getParameter("title");
        if (title != null && !title.trim().isEmpty()) {
            todoDAO.addTodo(title);
        }

        // Keep the user on the same page and render the refreshed list.
        List<Todo> todos = todoDAO.getAllTodos();
        request.setAttribute("todos", todos);
        request.getRequestDispatcher("/list.jsp").forward(request, response);
    }
}
```

---

### `src/main/webapp/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

  <display-name>todo-app</display-name>

  <servlet>
    <servlet-name>TodoServlet</servlet-name>
    <servlet-class>com.todolist.TodoServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>TodoServlet</servlet-name>
    <url-pattern>/app</url-pattern>
  </servlet-mapping>

  <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
```

---

### `src/main/webapp/index.jsp`

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    response.sendRedirect(request.getContextPath() + "/app");
%>
```

---

### `src/main/webapp/list.jsp`

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Todo List</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <h2 class="text-center mb-4">My Todo List</h2>
    
    <!-- Form to add new todo -->
    <div class="card mb-4 shadow-sm">
        <div class="card-body">
            <form action="<%= request.getContextPath() %>/app" method="post" class="d-flex">
                <input type="text" name="title" class="form-control me-2" placeholder="What do you need to do?" required>
                <button type="submit" class="btn btn-primary">Add</button>
            </form>
        </div>
    </div>

    <!-- List of todos -->
    <div class="card shadow-sm">
        <ul class="list-group list-group-flush">
            <c:choose>
                <c:when test="${not empty todos}">
                    <c:forEach var="todo" items="${todos}">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span style="${todo.completed ? 'text-decoration: line-through; color: gray;' : ''}">
                                ${todo.title}
                            </span>
                            <div>
                                <!-- Toggle Link -->
                                          <a href="<%= request.getContextPath() %>/app?action=toggle&id=${todo.id}&isCompleted=${todo.completed}" 
                                   class="btn btn-sm ${todo.completed ? 'btn-warning' : 'btn-success'}">
                                    ${todo.completed ? 'Undo' : 'Complete'}
                                </a>
                                <!-- Delete Link -->
                                          <a href="<%= request.getContextPath() %>/app?action=delete&id=${todo.id}" 
                                   class="btn btn-sm btn-danger"
                                   onclick="return confirm('Are you sure you want to delete this task?');">Delete</a>
                            </div>
                        </li>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <li class="list-group-item text-center text-muted">No todos yet! Add one above.</li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</div>
</body>
</html>
```

---

## Notes & Presentation Tips

- Architecture: Simple 3-layer pattern — Servlet (controller), DAO (data access), Model (Todo), JSP (view).
- Transactionality: Each DAO method uses its own connection; there is no transaction spanning multiple operations.
- Error handling: Exceptions are printed via `e.printStackTrace()`; consider logging for production.
- Security: No input sanitization beyond required fields; be cautious about XSS in JSP output (escape if exposing raw input).

Suggested PPT slide breakdown:
1. Project overview & architecture diagram (one slide)
2. Build & deployment steps (one slide)
3. Database schema & Docker quickstart (one slide)
4. Walkthrough of `TodoServlet` (one slide) with sequence: add -> update -> delete
5. Show `TodoDAO` SQL operations and code snippets (one slide)
6. UI screenshots / JSP structure (one slide)
7. Known limitations & improvements (one slide)

---

If you want, I can also generate a condensed set of PPT slides (PowerPoint) from this documentation. Would you like me to produce a slide deck draft next?
