import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Job;

import java.io.*;
import java.util.List;

// Скрипт для сбора данных о сборках через CI
public class GetJobsInfo {

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

    public void getJobsInfo() {
        var key = "MY_KEY";

        GitLabApi gitLabApi = new GitLabApi("https://g.hse.ru", key);

        try {
            List<Job> requests = gitLabApi.getJobApi().getJobs("68");

            OutputStream os = new FileOutputStream("/Users/liuda/Desktop/gitlab/jobs.csv");
            os.write(239);
            os.write(187);
            os.write(191);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.append("created at;id;user;name;ref;tag;stage;status;when;manual;" +
                    "allow failure;coverage;commit_authoredDate;commit_id;" +
                    "commit_authorName;commit_title;commit_message;pipeline_createdAt;pipeline_id;pipeline_tag;" +
                    "pipeline_status;pipeline_ref;pipeline_updatedAt;pipeline_startedAt;pipeline_finishedAt;pipeline_duration;" +
                    "artifact_fileType;artifact_fileName;started at;finished at;duration\n");

            for (Job req : requests) {
                String name = req.getUser().getName();
                String output = req.getCreatedAt() + ";" + req.getId() + ";" + getUserName(name) + ";" + req.getName() +
                        ";" + req.getRef() + ";" + req.getTag() + ";" + req.getStage() + ";" + req.getStatus() + ";" +
                        req.getWhen() + ";" + req.getManual() + ";" + req.getAllowFailure() + ";" + req.getCoverage() +
                        ";" + req.getCommit().getAuthoredDate() + ";" + req.getCommit().getId() + ";" +
                        getUserName(req.getCommit().getAuthorName()) + ";" + req.getCommit().getTitle() + ";" +
                        req.getCommit().getMessage().replace("\n", " ").replace(";", " ") +
                        ";" + req.getPipeline().getCreatedAt() + ";" + req.getPipeline().getId() + ";" +
                        req.getPipeline().getTag() + ";" + req.getPipeline().getStatus() + ";" + req.getPipeline().getRef() +
                        ";" + req.getPipeline().getUpdatedAt() + ";" + req.getPipeline().getStartedAt() + ";" +
                        req.getPipeline().getFinishedAt() + ";" + req.getPipeline().getDuration() + ";" +
                        (req.getArtifacts().size() > 0 ? req.getArtifacts().get(0).getFileType() : "") + ";" +
                        (req.getArtifacts().size() > 0 ? req.getArtifacts().get(0).getFilename() : "") + ";" +
                        req.getStartedAt() + ";" + req.getFinishedAt() + ";" + req.getDuration();

                output += "\n";
                writer.append(output);
            }

            writer.close();

        } catch (GitLabApiException | IOException e) {
            e.printStackTrace();
        }
    }

}
