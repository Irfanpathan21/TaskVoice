package com.taskvoice.controller.admin;

import com.taskvoice.dao.CategoryDAO;
import com.taskvoice.dao.impl.CategoryDAOImpl;
import com.taskvoice.model.Category;
import com.taskvoice.model.User;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/categories")
public class CategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Category> categories = categoryDAO.findAll();
        req.setAttribute("categories", categories);
        req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                String name = req.getParameter("name");
                String description = req.getParameter("description");
                Category c = new Category();
                c.setName(name.trim());
                c.setDescription(description);
                c.setDefault(false);
                c.setCreatedBy(user.getId());
                categoryDAO.insert(c);
                req.getSession().setAttribute("flashMessage", "Category created.");
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                String name = req.getParameter("name");
                String description = req.getParameter("description");
                Category c = new Category();
                c.setId(id);
                c.setName(name.trim());
                c.setDescription(description);
                categoryDAO.update(c);
                req.getSession().setAttribute("flashMessage", "Category updated.");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                categoryDAO.delete(id);
                req.getSession().setAttribute("flashMessage", "Category deleted.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
}
