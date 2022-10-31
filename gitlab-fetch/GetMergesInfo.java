import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Participant;

import java.io.*;
import java.util.List;

// Скрипт для сбора данных о мердж-реквестах
public class GetMergesInfo {

    // Метод, сопоставляющий каждому человеку имя для сохранения в датасет (в целях анонимизации)
    public static String getUserName(String name) {
        // Тут скрыты фамилии, но при сборе были взяты реальные данные
        List<String> names = List.of("Igor SURNAME", "HSE Apps", "Igor SURNAME2",
                "SURNAME Matvey", "Mikhail", "a SURNAME", "korolevsolutions", "MY_NAME SURNAME", "Mikhail SURNAME",
                "Dmitriy SURNAME", "MY_NAME", "MY_NAME SURNAME", "Andrei SURNAME", "Matvey SURNAME", "Dmitry SURNAME");

        if (names.contains(name)) {
            if (name.equals("Igor SURNAME") || name.equals("Igor SURNAME2")) {
                return "Team Lead";
            } else if (name.equals("HSE Apps")) {
                return "HSE Apps";
            } else if (name.equals("SURNAME Matvey") || name.equals("Matvey SURNAME")) {
                return "iOS Lead";
            } else if (name.equals("Dmitry SURNAME") || name.equals("Dmitriy SURNAME")) {
                return "iOS Middle";
            } else if (name.equals("Andrei SURNAME") || name.equals("korolevsolutions") || name.equals("a SURNAME")) {
                return "iOS Junior (left)";
            } else if (name.equals("Mikhail") || name.equals("Mikhail SURNAME")) {
                return "iOS Middle (left)";
            } else if (name.equals("MY_NAME") || name.equals("MY_NAME SURNAME") || name.equals("MY_NAME SURNAME")) {
                return "iOS Junior";
            }
        }

        return "Developer";
    }

    // Метод для сбора имен участников мердж-реквеста
    public static String getParticipants(Long uuid) throws GitLabApiException {
        var key = "MY_KEY";
        GitLabApi gitLabApi = new GitLabApi("https://g.hse.ru", key);

        String result = "";
        List<Participant> participants = gitLabApi.getMergeRequestApi().getParticipants("68", uuid);
        for (Participant participant:
                participants) {
            result += getUserName(participant.getName()) + ",";
        }
        return result;

    }

    public void getMergesInfo() {
        var key = "MY_KEY";
        GitLabApi gitLabApi = new GitLabApi("https://g.hse.ru", key);

        try {

            List<MergeRequest> requests = gitLabApi.getMergeRequestApi().getMergeRequests("68");
            System.out.println(requests.size());

            OutputStream os = new FileOutputStream("/Users/liuda/Desktop/gitlab/merge-requests.csv");
            os.write(239);
            os.write(187);
            os.write(191);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.append("created at;id;author;title;description;merge status;state;source branch;target branch;references;" +
                    "should remove source branch;merge when pipeline succeeds;has conflicts;diverged commits count;" +
                    "approvals before merge;allow collaboration;approvals required;reviewers;upvotes;downvotes;" +
                    "user notes count;approvals left;participants;approved by;blocking discussions resolved;updated at;" +
                    "merged by;closed at;closed by\n");

            for (MergeRequest req :
                    requests) {

                String name = req.getAuthor().getName();
                String output = req.getCreatedAt() + ";" + req.getId() + ";" + getUserName(name) + ";" +
                        req.getTitle() + ";" + req.getDescription().replace("\n", " ").replace(";", " ") + ";" +
                        req.getMergeStatus() + ";" + req.getState() + ";" + req.getSourceBranch() + ";" +
                        req.getTargetBranch() + ";" + req.getReferences().getRelative() + ";" + req.getShouldRemoveSourceBranch() +
                        ";" + req.getMergeWhenPipelineSucceeds() + ";" + req.getHasConflicts() + ";" +
                        req.getDivergedCommitsCount() + ";" +  req.getApprovalsBeforeMerge() + ";" +
                        req.getAllowCollaboration() + ";" + req.getApprovalsRequired() + ";" +
                        ((req.getReviewers() != null &&  req.getReviewers().size() > 0) ? getUserName(req.getReviewers().get(0).getName()) : "") + ";" +
                        req.getUpvotes() + ";" + req.getDownvotes() + ";" + req.getUserNotesCount() + ";" +
                        req.getApprovalsLeft() + ";" +
                        getParticipants(req.getIid()) + ";" +
                        ((req.getApprovedBy() != null && req.getApprovedBy().size() > 0) ? getUserName(req.getApprovedBy().get(0).getName()) : "") + ";" +
                        req.getBlockingDiscussionsResolved() + ";" + req.getUpdatedAt() + ";" +
                        (req.getMergedBy() == null ? "" : getUserName(req.getMergedBy().getName())) + ";" + req.getClosedAt() + ";" +
                        (req.getClosedBy() == null ? "" : getUserName(req.getClosedBy().getName()));

                output += "\n";

                writer.append(output);
            }
            writer.close();

        } catch (GitLabApiException | IOException e) {
            e.printStackTrace();
        }
    }

}
