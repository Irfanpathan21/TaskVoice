package com.taskvoice.controller.admin;

import com.taskvoice.dao.DepartmentDAO;
import com.taskvoice.dao.impl.DepartmentDAOImpl;
import com.taskvoice.model.Department;
import com.taskvoice.model.User;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/departments")
public class DepartmentServlet extends HttpServlet {

    private final DepartmentDAO deptDAO = new DepartmentDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Department> departments = deptDAO.findAll();
        req.setAttribute("departments", departments);
        req.getRequestDispatcher("/WEB-INF/views/admin/departments.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                String name = req.getParameter("name");
                String description = req.getParameter("description");
                Department d = new Department();
                d.setName(name.trim());
                d.setDescription(description);
                deptDAO.insert(d);
                req.getSession().setAttribute("flashMessage", "Department created successfully.");
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                String name = req.getParameter("name");
                String description = req.getParameter("description");
                Department d = new Department();
                d.setId(id);
                d.setName(name.trim());
                d.setDescription(description);
                deptDAO.update(d);
                req.getSession().setAttribute("flashMessage", "Department updated.");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                if (deptDAO.countEmployees(id) > 0) {
                    req.getSession().setAttribute("flashError", "Cannot delete department with active employees.");
                } else {
                    deptDAO.delete(id);
                    req.getSession().setAttribute("flashMessage", "Department deleted.");
                }
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/departments");
    }
}
