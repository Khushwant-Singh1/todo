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
