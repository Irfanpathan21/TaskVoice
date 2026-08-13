package com.taskvoice.controller.manager;

import com.taskvoice.model.Appraisal;
import com.taskvoice.model.AppraisalPeriod;
import com.taskvoice.model.User;
import com.taskvoice.service.AppraisalService;
import com.taskvoice.service.EmployeeService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/manager/appraisals")
public class AppraisalServlet extends HttpServlet {

    private final AppraisalService appraisalService = new AppraisalService();
    private final EmployeeService  empService       = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String periodIdStr = req.getParameter("periodId");
        String appraisalIdStr = req.getParameter("id");

        if (appraisalIdStr != null && !appraisalIdStr.isBlank()) {
            int appraisalId = Integer.parseInt(appraisalIdStr);
            Optional<Appraisal> opt = appraisalService.findById(appraisalId);
            if (opt.isEmpty() || opt.get().getManagerId() != manager.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            req.setAttribute("appraisal", opt.get());
            req.getRequestDispatcher("/WEB-INF/views/manager/appraisal-review.jsp").forward(req, resp);
            return;
        }

        if (periodIdStr != null && !periodIdStr.isBlank()) {
            int periodId = Integer.parseInt(periodIdStr);
            Optional<AppraisalPeriod> periodOpt = appraisalService.findPeriodById(periodId);
            if (periodOpt.isEmpty()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

            req.setAttribute("period", periodOpt.get());
            req.setAttribute("appraisals", appraisalService.findByPeriod(periodId));
            req.setAttribute("team", empService.findTeam(manager.getId()));
            req.getRequestDispatcher("/WEB-INF/views/manager/appraisal-period-detail.jsp").forward(req, resp);
            return;
        }

        List<AppraisalPeriod> periods = appraisalService.getPeriodsForManager(manager.getId());
        req.setAttribute("periods", periods);
        req.setAttribute("team", empService.findTeam(manager.getId()));
        req.getRequestDispatcher("/WEB-INF/views/manager/appraisal-periods.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");

        try {
            if ("createPeriod".equals(action)) {
                String title = req.getParameter("title");
                String periodType = req.getParameter("periodType");
                String startDate = req.getParameter("startDate");
                String endDate = req.getParameter("endDate");

                appraisalService.createPeriod(title, periodType, startDate, endDate, manager.getId(),
                        manager.getId(), manager.getName(), req.getRemoteAddr());

                req.getSession().setAttribute("flashMessage", "Appraisal period created.");

            } else if ("triggerAi".equals(action)) {
                int periodId = Integer.parseInt(req.getParameter("periodId"));
                int employeeId = Integer.parseInt(req.getParameter("employeeId"));

                Appraisal a = appraisalService.triggerAiAnalysis(periodId, employeeId, manager.getId(),
                        manager.getId(), manager.getName(), req.getRemoteAddr());

                req.getSession().setAttribute("flashMessage", "AI appraisal analysis complete. Please review.");
                resp.sendRedirect(req.getContextPath() + "/manager/appraisals?id=" + a.getId());
                return;

            } else if ("finalize".equals(action)) {
                int appraisalId = Integer.parseInt(req.getParameter("appraisalId"));
                double score = Double.parseDouble(req.getParameter("managerScore"));
                String grade = req.getParameter("managerGrade");
                String decision = req.getParameter("managerDecision");
                String remark = req.getParameter("managerRemark");

                appraisalService.finalizeAppraisal(appraisalId, score, grade, decision, remark,
                        manager.getId(), manager.getName(), req.getRemoteAddr());

                req.getSession().setAttribute("flashMessage", "Appraisal finalized successfully.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/manager/appraisals");
    }
}
