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
