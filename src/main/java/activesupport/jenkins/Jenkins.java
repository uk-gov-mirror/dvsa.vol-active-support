package activesupport.jenkins;

import activesupport.jenkins.exceptions.JenkinsBuildFailed;
import activesupport.system.Properties;
import activesupport.system.out.Output;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class Jenkins {

    public enum Job {
        NI_EXPORT("Batch_data-gov-NI-export"),
        BATCH_PROCESS_QUEQUE("Batch_Process_Queue"),
        BATCH_RUN_CLI("Batch_Run_Cli");

        private final String name;

        Job(@NotNull String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static void trigger(@NotNull Job job, @NotNull HashMap<String, String> params) throws Exception {
        String username = Properties.get("JENKINS_USERNAME", true);
        String password = Properties.get("JENKINS_PASSWORD", true);

        JenkinsServer batchProcessJobs = new JenkinsServer(new URI("http://olcsci.shd.ci.nonprod.dvsa.aws:8080/"), username, password);
        JobWithDetails details = batchProcessJobs.getJob(job.toString());
        int lastSuccessfulBuild = details.getNextBuildNumber();
        details.build(params,true);
        List<Build> builds = details.getAllBuilds(Range.build().from(lastSuccessfulBuild).build());

        for (Build build : builds) {
            BuildWithDetails buildInfo = build.details();

            while (buildInfo.isBuilding()) {
                if (buildInfo.getResult() == BuildResult.SUCCESS) {
                    break;
                } else if (buildInfo.getResult() == BuildResult.FAILURE) {
                    throw new JenkinsBuildFailed();
                } else {
                    throw new Exception(Output.printColoredLog("[ERROR] Jenkins job was not successfully completed"));
                }
            }
        }
    }

}
