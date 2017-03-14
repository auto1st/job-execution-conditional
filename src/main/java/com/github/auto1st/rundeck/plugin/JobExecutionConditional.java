package com.github.auto1st.rundeck.plugin;

import com.dtolabs.rundeck.core.execution.ExecutionService;
import com.dtolabs.rundeck.core.execution.ExecutionServiceFactory;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.jobs.JobNotFound;
import com.dtolabs.rundeck.core.jobs.JobReference;
import com.dtolabs.rundeck.core.jobs.JobService;
import com.dtolabs.rundeck.core.jobs.JobState;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.google.common.util.concurrent.ExecutionList;

import java.util.Map;


@Plugin(name=JobExecutionConditional.SERVICE_PROVIDER, service= ServiceNameConstants.WorkflowStep)
@PluginDescription(
        title="Job Execution Conditional",
        description = "Responsible to validate whether a package was deployed in previously environments."
)
public class JobExecutionConditional implements StepPlugin {

    // define global variables need to rundeck configuration and documentation page
    public final static String SERVICE_PROVIDER = "job-execution-conditional";


    // JOB parameter to receive the configuration system.
    @PluginProperty(
            title="Job UUID",
            description="Define the JOB UUID Reference to found Rundeck executions"
    )
    public String jobUUID;

    // JOB parameters to receive the JOB Name instead of JOB UUID.
    @PluginProperty(
            title = "Job Name",
            description = "Job Name Reference (when using folders \"path/to/myJobName\""
    )
    public String jobName;

    // n(th)-last execution numbers
    @PluginProperty(
            title="Max Execution Evaluation",
            description="The n-Last execution that will be check on the Plugin. \n" +
                        "Set this to 0 to read all executions registered on Rundeck server",
            defaultValue="25",
            required = true
    )
    public int nLast;

    // also analyse jobs failures.
    @PluginProperty(
            title="Analyse Failure",
            description="Also Analise Executions was finished with FAILURE",
            defaultValue="false"
    )
    private boolean alsoFailureExectuion;

    // define the strings
    private String[] options;

    // Define the class Constants
    public static final String EXEC_STATE_FAILED = "Failed";
    public static final String EXEC_STATE_ABORTED = "Aborted";
    public static final String EXEC_STATE_TIMED_OUT = "TimedOut";
    public static final String EXEC_STATE_NEVER = "Never";
    public static final String EXEC_STATE_SUCCEEDED = "Succeeded";
    public static final String EXEC_STATE_FAILED_WITH_RETRY = "Failed-with-retry";

    /**
     *  Function responsible to execute the information.
     */
    public void executeStep(final PluginStepContext context, final Map<String, Object> configuration)
            throws StepException {

        // Validate whether a JobUUID or JobName was not passed to the execution.
        if (jobUUID == null && jobName == null)
            throw new StepException(
                    "You should define or a JobUUID or a JobName to use as reference.",
                    StepFailureReason.ConfigurationFailure
            );

        // Return the JOBS State.
        JobService jobService = context.getExecutionContext().getJobService();
        final JobState jobState;
        final JobReference jobReference;
        final ExecutionService execService;
        try {

            if (null != jobUUID) {
                jobReference = jobService.jobForID(jobUUID, context.getFrameworkProject());
            }
            else {
                jobReference = jobService.jobForName(jobName, context.getFrameworkProject());
            }

            jobState = jobService.getJobState(jobReference);

            execService = ExecutionServiceFactory.getInstanceForFramework(
                        context.getFramework()
            );

            System.out.println(execService);

            System.out.println("JobUUID: " + jobUUID);
            System.out.println("JObName: " + jobName);
            System.out.println("Reference: " + jobState.getPreviousExecutionStatusString());

        } catch (JobNotFound jobNotFound) {
            throw new StepException(
                    "Job was not found: " + jobNotFound.getMessage(),
                    StepFailureReason.ConfigurationFailure
            );
        }
    }

    /**
     * Responsible to find the Given option and return
     * true, when the matches is founded on the project execution.
     *
     * @option String with the context to be searched
     * @Execution the execution that will be searched
     * @return true, whether found the option
     */
    private boolean findOptionAt(String option, String Exectution) {
        // TODO: Implemnets the function responsible to analyse the execution looking for the given Option.

        return false;
    }

}

