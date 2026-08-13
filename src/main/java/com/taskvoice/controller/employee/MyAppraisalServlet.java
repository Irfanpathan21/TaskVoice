package com.taskvoice.controller.employee;

import com.taskvoice.model.Appraisal;
import com.taskvoice.model.User;
import com.taskvoice.service.AppraisalService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/employee/appraisal")
public class MyAppraisalServlet extends HttpServlet {

    private final AppraisalService appraisalService = new AppraisalService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            int appraisalId = Integer.parseInt(idStr);
            Optional<Appraisal> opt = appraisalService.findById(appraisalId);
            if (opt.isEmpty() || opt.get().getEmployeeId() != employee.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            req.setAttribute("appraisal", opt.get());
            req.getRequestDispatcher("/WEB-INF/views/employee/appraisal-detail.jsp").forward(req, resp);
            return;
        }

        List<Appraisal> appraisals = appraisalService.findByEmployee(employee.getId());
        req.setAttribute("appraisals", appraisals);
        req.getRequestDispatcher("/WEB-INF/views/employee/appraisal.jsp").forward(req, resp);
    }
}
