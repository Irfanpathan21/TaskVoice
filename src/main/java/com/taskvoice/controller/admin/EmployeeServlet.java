package com.taskvoice.controller.admin;

import com.taskvoice.model.User;
import com.taskvoice.service.EmployeeService;
import com.taskvoice.service.AuthenticationService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/employees")
public class EmployeeServlet extends HttpServlet {

    private final EmployeeService empService = new EmployeeService();
    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = 1;
        String pStr = req.getParameter("page");
        if (pStr != null && !pStr.isBlank()) {
            try { page = Math.max(1, Integer.parseInt(pStr)); } catch (NumberFormatException ignored) {}
        }
        int pageSize = 25;
        List<User> employees = empService.findAll(page, pageSize);
        int total = empService.countAll();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("employees", employees);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("departments", empService.findAllDepartments());
        req.setAttribute("managers", empService.findByRole("MANAGER"));

        req.getRequestDispatcher("/WEB-INF/views/admin/employees.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User actor = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String name = req.getParameter("name");
                String email = req.getParameter("email");
                String roleName = req.getParameter("roleName");
                int roleId = "ADMIN".equals(roleName) ? 1 : ("MANAGER".equals(roleName) ? 2 : 3);
                Integer deptId = parseInteger(req.getParameter("departmentId"));
                Integer managerId = parseInteger(req.getParameter("managerId"));
                String joiningDate = req.getParameter("joiningDate");

                empService.createUser(name, email, roleName, roleId, deptId, managerId, joiningDate,
                        actor.getId(), actor.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Employee account created successfully.");

            } else if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                String name = req.getParameter("name");
                String email = req.getParameter("email");
                String roleName = req.getParameter("roleName");
                int roleId = "ADMIN".equals(roleName) ? 1 : ("MANAGER".equals(roleName) ? 2 : 3);
                Integer deptId = parseInteger(req.getParameter("departmentId"));
                String joiningDate = req.getParameter("joiningDate");

                empService.updateUser(id, name, email, roleId, deptId, joiningDate,
                        actor.getId(), actor.getName(), req.getRemoteAddr());

                Integer managerId = parseInteger(req.getParameter("managerId"));
                if (managerId != null && "EMPLOYEE".equals(roleName)) {
                    empService.assignManager(id, managerId, actor.getId(), actor.getName(), req.getRemoteAddr());
                }

                req.getSession().setAttribute("flashMessage", "User updated successfully.");

            } else if ("disable".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                empService.disableUser(id, actor.getId(), actor.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Account disabled.");

            } else if ("enable".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                empService.enableUser(id, actor.getId(), actor.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Account enabled.");

            } else if ("resetPassword".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                String tempPw = authService.resetPassword(actor.getId(), actor.getName(), id);
                req.getSession().setAttribute("flashMessage", "Password reset. Temporary password: " + tempPw);

            } else if ("assignManager".equals(action)) {
                int empId = Integer.parseInt(req.getParameter("employeeId"));
                int mgrId = Integer.parseInt(req.getParameter("managerId"));
                empService.assignManager(empId, mgrId, actor.getId(), actor.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Manager assigned successfully.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/employees");
    }

    private Integer parseInteger(String str) {
        if (str == null || str.isBlank()) return null;
        try { return Integer.parseInt(str); } catch (NumberFormatException e) { return null; }
    }
}
