/*
 * Copyright 2017 Automation First, Open Source Co. (https://github.com/auto1st)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.auto1st.rundeck.plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.jobs.JobReference;
import com.dtolabs.rundeck.core.jobs.JobService;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;

import grails.util.Holders;

/**
 * Job Execution Conditional plugin impl.
 * 
 * @author fabiojose
 *
 */
@Plugin(name = JobExecutionConditional.PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Job Execution Conditional", description = "Evaluate executions options of another Job")
public class JobExecutionConditional implements StepPlugin {

  private static final String SESSION_FACTORY                         = "sessionFactory";
  private static final String EXECUTIONS_QUERY_ALL_SUCCEEDED          = "from Execution e where e.dateCompleted is not null AND e.scheduledExecution.uuid = :uuid AND e.status = 'succeeded' ORDER BY e.dateCompleted DESC";
  private static final String EXECUTIONS_QUERY_ALL_SUCCEEDED_OR_OTHER = "from Execution e where e.dateCompleted is not null AND e.scheduledExecution.uuid = :uuid AND (e.status = 'succeeded' OR e.status = :status) ORDER BY e.dateCompleted DESC";
  private static final String EXECUTIONS_QUERY_ALL_PARAM0             = "uuid";
  private static final String EXECUTIONS_QUERY_ALL_PARAM1             = "status";
  
  private static final String STATUS_FAILED = "failed";

  private static final String METHOD_GET_ARGSTRING = "getArgString";

  public static final String PROVIDER_NAME = "job-execution-conditional";

  @PluginProperty(title = "Job UUID",
                  description = "UUID for the Job", 
                  required = true)
  protected String jobUUID;
  
  @PluginProperty(title = "Custom message",
                  description = "Configure a custom message to show when fail",
                  defaultValue = "No true comparison found")
  protected String message;

  @PluginProperty(title = "Maximum executions", 
                  description = "Maximum number of latest executions to analyze"
                                + "\n\n"
                                + "`0` for all (should affect the performance)", 
                  defaultValue = "10")
  protected Integer maximumExecutions;

  @PluginProperty(title = "Analyze failed", 
                  description = "Analyze the execution, even if it was failed")
  protected Boolean analyzeFailed;

  @PluginProperty(title = "Option condition", 
                  description = "Use options values to get a true comparison" 
                                + "\n\n\n"
                                + "*Use the following syntax*"
                                + "\n\n" 
                                + "Example:" + "\n"
                                + "`-OPTION_NAME_A=${option.OPTION_0} -OPTION_NAME_B=${option.OPTION_1} -OPTION_NAME_C=literal`",
                  validatorClass = OptionConditionValidator.class, 
                  required = true)
  @TextArea
  protected String optionCondition;

  private static enum Failures implements FailureReason {
    GeneralFailure
  }

  @Override
  public void executeStep(final PluginStepContext context, final Map<String, Object> map) throws StepException {

    /*
     * Get the job service from context.
     */
    final JobService jobService = context.getExecutionContext().getJobService();

    /*
     * Extract options from optionCondition
     */
    final Map<String, String> _conditions = Utils.conditionsOf(optionCondition);
    final Set<String> _conditionsKeys = _conditions.keySet();

    /*
     * Hack to get the hibernate session from Grails APP
     */
    final GrailsApplication grails = Holders.getGrailsApplication();
    final ApplicationContext mainContext = grails.getMainContext();
    final SessionFactory factory = (SessionFactory) mainContext.getBean(SESSION_FACTORY);
    Session session = null;

    try {
      /*
       * Get the job reference by UUID
       */
      final JobReference job = jobService.jobForID(jobUUID, context.getFrameworkProject());
      
      /*
       * Log the job name.
       */
      context.getLogger().log(2, "Job Name: " + job.getJobAndGroup());

      /*
       * Start a new hibernate session.
       */
      session = factory.openSession();
      
      /*
       * Query.
       */
      Query query = null;
      
      /*
       * Analyzed failed?
       */
      if(Boolean.TRUE.equals(analyzeFailed)){
        
        //yes, query the succeeded or failed
        query = session.createQuery(EXECUTIONS_QUERY_ALL_SUCCEEDED_OR_OTHER);
        query.setParameter(EXECUTIONS_QUERY_ALL_PARAM1, STATUS_FAILED);
        
      } else {
        
        //no, select just the succeeded
        query = session.createQuery(EXECUTIONS_QUERY_ALL_SUCCEEDED);
        
      }

      //set the job uuid
      query.setParameter(EXECUTIONS_QUERY_ALL_PARAM0, job.getId());
           
      //is there maximum executions?
      if(maximumExecutions > 0){
        query.setMaxResults(maximumExecutions);
      }
      
      // execute the query
      final List<?> _executions = query.list();

      /*
       * At then end if it has true, conditions are met
       */
      boolean _continue = _executions.size() > 0;

      /*
       * For each execution.
       */
      for (int _index = 0; _index < _executions.size(); _index++) {
        Object _execution = _executions.get(_index);

        // local boolean accumulator
        boolean _local = true;

        /*
         * Get the argString value. The options list used by the execution.
         */
        Object _argStringObj = MethodUtils.invokeMethod(_execution, METHOD_GET_ARGSTRING);
        
        if (_argStringObj instanceof String) {

          /*
           * Extract the pairs from argString used by the Execution
           */
          Map<String, String> _options = Utils.optionsOf((String) _argStringObj);

          /*
           * For each condition
           */
          for (String _key : _conditionsKeys) {
            // compare the condition value to execution option value
            _local = _local && _conditions.get(_key).equals(_options.get(_key));
          }
        }

        // compare holder
        _continue = _local;

        /*
         * When the first true compare is found, breaks the loop.
         */
        if (_local) {
          break;
        }
      }

      if (_continue) {
        // conditions are met
        context.getLogger().log(2, "Conditions are met: " + _conditions);
      } else {
        // conditions aren't met
        context.getLogger().log(0, "Conditions aren't met: " + _conditions);
        context.getFlowControl().Halt(message);
      }

    } catch (Exception e) {
      throw new StepException(e.getMessage(), e, Failures.GeneralFailure);
    } finally {
      if (null != session) {
        // close the hibernate session
        session.close();
      }
    }
  }

}
