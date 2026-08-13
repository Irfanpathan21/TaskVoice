package com.taskvoice.ai;

import com.taskvoice.model.Appraisal;
import com.taskvoice.model.Project;
import com.taskvoice.model.Task;
import com.taskvoice.model.TaskUpdate;
import com.taskvoice.model.TimesheetEntry;

import java.util.List;

/**
 * GeminiPrompts — five distinct, purpose-built prompt templates.
 * Never reused across purposes. Each method returns the full prompt string.
 */
public final class GeminiPrompts {

    private GeminiPrompts() {}

    /**
     * Prompt 1: VOICE SEGMENTATION
     * Transcript → structured JSON array of work entries.
     * Output shape: [{title, category, durationHours, description}, ...]
     */
    public static String voiceSegmentation(String transcript, List<String> assignedTaskTitles) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional work-log parser for an enterprise time-tracking system called TaskVoice.\n\n");
        sb.append("TASK: Parse the following spoken daily work recap into a structured JSON array of individual work entries.\n\n");
        sb.append("RULES:\n");
        sb.append("1. Split the recap into separate entries — one per distinct activity.\n");
        sb.append("2. Each entry must have: title (string), category (string), durationHours (number, e.g. 1.5), description (string).\n");
        sb.append("3. durationHours must be a positive number. If unclear, estimate based on context.\n");
        sb.append("4. Total hours should not exceed 10 per day unless clearly stated.\n");
        sb.append("5. Category must be one of: Development, Design, Meetings, Documentation, Testing, Research, Deployment, Administration.\n");
        sb.append("6. Description should be a clean, professional one-sentence summary (not the employee's raw words).\n");
        sb.append("7. If the spoken text matches any of the employee's assigned tasks, set matchedTaskTitle to that task title; otherwise omit it.\n");
        sb.append("8. Return ONLY a valid JSON array — no markdown, no explanation, no ```json wrapper.\n\n");

        if (assignedTaskTitles != null && !assignedTaskTitles.isEmpty()) {
            sb.append("EMPLOYEE'S ASSIGNED TASKS (for fuzzy matching):\n");
            for (String t : assignedTaskTitles) sb.append("- ").append(t).append("\n");
            sb.append("\n");
        }

        sb.append("TRANSCRIPT TO PARSE:\n\"").append(transcript).append("\"\n\n");
        sb.append("REQUIRED OUTPUT (JSON array only):\n");
        sb.append("[{\"title\":\"...\",\"category\":\"...\",\"durationHours\":0.0,\"description\":\"...\",\"matchedTaskTitle\":\"...\"}]");
        return sb.toString();
    }

    /**
     * Prompt 2: REPHRASING
     * Raw spoken/typed text → professional one-line description.
     * Original always preserved alongside it in the DB.
     */
    public static String rephrase(String rawText) {
        return "You are a professional business writing assistant for an enterprise task management system.\n\n" +
               "TASK: Rephrase the following work update into a single, professional, concise sentence suitable for a corporate progress log.\n\n" +
               "RULES:\n" +
               "1. Keep the same meaning and facts — do not add, remove, or invent information.\n" +
               "2. Write in past tense, third-person-neutral or first-person (match the original intent).\n" +
               "3. Remove filler words, slang, repetition.\n" +
               "4. Return ONLY the rephrased sentence — no explanation, no quotes, no markdown.\n\n" +
               "RAW TEXT:\n\"" + rawText + "\"\n\n" +
               "REPHRASED SENTENCE:";
    }

    /**
     * Prompt 3: PROJECT SUMMARY
     * Completed project history → short factual narrative summary.
     */
    public static String projectSummary(Project project, List<Task> tasks, List<TaskUpdate> allUpdates) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a technical project analyst. Write a concise factual summary of a completed software project.\n\n");
        sb.append("PROJECT: ").append(project.getTitle()).append("\n");
        sb.append("Description: ").append(project.getDescription()).append("\n");
        sb.append("Duration: ").append(project.getStartDate()).append(" to ").append(project.getEndDate()).append("\n");
        sb.append("Status: ").append(project.getStatus()).append("\n\n");
        sb.append("TASKS (").append(tasks.size()).append(" total):\n");
        for (Task t : tasks) {
            sb.append("- [").append(t.getStatus()).append("] ").append(t.getTitle());
            if (t.getManagerGrade() != null) sb.append(" (Grade: ").append(t.getManagerGrade()).append(")");
            sb.append("\n");
        }
        sb.append("\nKEY UPDATES:\n");
        int shown = 0;
        for (TaskUpdate u : allUpdates) {
            if (shown >= 10) break;
            sb.append("- ").append(u.getRawText()).append("\n");
            shown++;
        }
        sb.append("\nRULES:\n");
        sb.append("1. Write 2-3 sentences maximum.\n");
        sb.append("2. Be factual — cover what was built, how it progressed, and the final outcome.\n");
        sb.append("3. No opinions, no recommendations, no marketing language.\n");
        sb.append("4. Return ONLY the summary text — no heading, no markdown.\n\n");
        sb.append("PROJECT SUMMARY:");
        return sb.toString();
    }

    /**
     * Prompt 4: PROJECT SENTIMENT (fires ONCE per project, on completion only)
     * Full project history → {sentiment, confidence, explanation}
     */
    public static String projectSentiment(Project project, List<Task> tasks,
                                           List<TaskUpdate> allUpdates, List<String> managerRemarks) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a neutral project health analyst. Assess the overall sentiment of a completed project.\n\n");
        sb.append("PROJECT: ").append(project.getTitle()).append("\n");
        sb.append("Duration: ").append(project.getStartDate()).append(" to ").append(project.getEndDate()).append("\n\n");
        sb.append("TASK SUMMARY:\n");
        int completed = 0, total = tasks.size();
        for (Task t : tasks) {
            if ("COMPLETED".equals(t.getStatus())) completed++;
            sb.append("- [").append(t.getStatus()).append("] ").append(t.getTitle());
            if (t.getManagerGrade() != null) sb.append(" Grade:").append(t.getManagerGrade());
            sb.append("\n");
        }
        sb.append("Completion rate: ").append(completed).append("/").append(total).append("\n\n");
        sb.append("PROBLEMS RAISED:\n");
        for (TaskUpdate u : allUpdates) {
            if (u.getProblemsFaced() != null && !u.getProblemsFaced().isBlank()) {
                sb.append("- ").append(u.getProblemsFaced()).append("\n");
            }
        }
        sb.append("\nMANAGER REMARKS:\n");
        for (String r : managerRemarks) sb.append("- ").append(r).append("\n");
        sb.append("\nRULES:\n");
        sb.append("1. Assess overall project sentiment: POSITIVE, NEUTRAL, or NEGATIVE.\n");
        sb.append("2. Provide a confidence percentage (0-100).\n");
        sb.append("3. Write a 1-2 sentence factual explanation.\n");
        sb.append("4. Return ONLY valid JSON — no markdown, no extra text.\n\n");
        sb.append("REQUIRED OUTPUT:\n");
        sb.append("{\"sentiment\":\"POSITIVE|NEUTRAL|NEGATIVE\",\"confidence\":0-100,\"explanation\":\"...\"}");
        return sb.toString();
    }

    /**
     * Prompt 5: APPRAISAL ANALYSIS
     * Real period data → full performance analysis JSON.
     * AI NEVER auto-decides — only suggests. Manager must Accept/Modify/Reject.
     */
    public static String appraisalAnalysis(
            com.taskvoice.model.User employee,
            com.taskvoice.model.AppraisalPeriod period,
            List<Task> tasks,
            List<TaskUpdate> allUpdates,
            List<TimesheetEntry> timesheetEntries,
            double totalLoggedHours,
            double totalExpectedHours) {

        StringBuilder sb = new StringBuilder();
        sb.append("You are an objective, data-driven performance analyst for an enterprise HR system.\n");
        sb.append("Produce a comprehensive appraisal analysis based ONLY on the factual data provided below.\n");
        sb.append("IMPORTANT: This is a recommendation only. All final decisions rest with the human manager.\n\n");

        sb.append("EMPLOYEE: ").append(employee.getName()).append(" (").append(employee.getEmployeeNo()).append(")\n");
        sb.append("APPRAISAL PERIOD: ").append(period.getTitle()).append(" (").append(period.getStartDate()).append(" to ").append(period.getEndDate()).append(")\n\n");

        // Task performance data
        sb.append("TASKS (").append(tasks.size()).append(" total):\n");
        int completedOnTime = 0, completedLate = 0, incomplete = 0;
        for (Task t : tasks) {
            sb.append("- [").append(t.getStatus()).append("] ").append(t.getTitle());
            sb.append(" | Due: ").append(t.getDueDate());
            sb.append(" | Completion: ").append(t.getCompletionPct()).append("%");
            sb.append(" | Expected: ").append(t.getExpectedHours()).append("h");
            sb.append(" | Actual: ").append(t.getActualHours()).append("h");
            if (t.getManagerGrade() != null) sb.append(" | Grade: ").append(t.getManagerGrade());
            if (t.getManagerRemark() != null) sb.append(" | Remark: ").append(t.getManagerRemark());
            sb.append("\n");
            if ("COMPLETED".equals(t.getStatus())) completedOnTime++;
            else if (!"CANCELLED".equals(t.getStatus())) incomplete++;
        }
        sb.append("\nHOURS: Logged ").append(String.format("%.1f", totalLoggedHours))
          .append("h vs Expected ").append(String.format("%.1f", totalExpectedHours)).append("h\n\n");

        sb.append("PROGRESS UPDATES (sample of problems faced):\n");
        int shown = 0;
        for (TaskUpdate u : allUpdates) {
            if (shown >= 8) break;
            if (u.getProblemsFaced() != null && !u.getProblemsFaced().isBlank()) {
                sb.append("- ").append(u.getProblemsFaced()).append("\n");
                shown++;
            }
        }

        sb.append("\nRULES:\n");
        sb.append("1. Base analysis ONLY on the data provided — do not invent or assume.\n");
        sb.append("2. overallScore must be 0-100.\n");
        sb.append("3. suggestedGrade must be one of: OUTSTANDING, EXCELLENT, VERY_GOOD, GOOD, AVERAGE, NEEDS_IMPROVEMENT.\n");
        sb.append("4. promotionRecommendation must be one of: STRONGLY_RECOMMEND, RECOMMEND, CONSIDER, NOT_RECOMMENDED.\n");
        sb.append("5. incrementRange must include label 'AI RECOMMENDATION — MANAGER DECISION REQUIRED'.\n");
        sb.append("6. Return ONLY valid JSON — no markdown, no explanation outside the JSON.\n\n");
        sb.append("REQUIRED JSON OUTPUT:\n");
        sb.append("{\"summary\":\"...\",\"strengths\":\"...\",\"improvements\":\"...\",\"productivityAnalysis\":\"...\",");
        sb.append("\"reliabilityAnalysis\":\"...\",\"consistencyAnalysis\":\"...\",\"problemSolvingAssessment\":\"...\",");
        sb.append("\"overallScore\":0,\"suggestedGrade\":\"...\",\"promotionRecommendation\":\"...\",");
        sb.append("\"incrementRange\":\"AI RECOMMENDATION — MANAGER DECISION REQUIRED: ...\",");
        sb.append("\"disclaimer\":\"AI-generated analysis. Final decision rests with manager.\"}");
        return sb.toString();
    }
}
