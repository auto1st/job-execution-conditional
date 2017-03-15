/*
 * Copyright 2017 Automation First, Inc. (https://github.com/auto1st)
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

import java.util.Iterator;
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
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;

import grails.util.Holders;


@Plugin(name = JobExecutionConditional.PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
public class JobExecutionConditional implements StepPlugin {
	
	public static final String PROVIDER_NAME = "job-execution-conditional";
	
	@PluginProperty(title = "Job UUID", description = "UUID for the Job.")
	String jobUUID;

	private void poc(final PluginStepContext context)  throws Exception {
		
		final JobService jobService = context.getExecutionContext().getJobService();
		final JobReference job = jobService.jobForID(jobUUID, context.getFrameworkProject());
		context.getLogger().log(4, "JOB ID: " + job.getId());
		
		final Class<?> Execution = Class.forName("rundeck.Execution");
		context.getLogger().log(4, "EXECUTION CLASS: " + Execution);
		
		final GrailsApplication grails = Holders.getGrailsApplication();
		context.getLogger().log(4, "GRAILS APPL: " + grails);
				
		final ApplicationContext grailsContext = grails.getMainContext();
		context.getLogger().log(4, "GRAILS CONTEXT: " + grailsContext);
		
		context.getLogger().log(4, "SESSION FACTORY: " + grailsContext.getBean("sessionFactory"));
		
		final SessionFactory factory = (SessionFactory)grailsContext.getBean("sessionFactory");
		
		final Session session = factory.openSession();
		context.getLogger().log(4, "NEW SESSION: " + session);
		
		final Query query = session.createQuery("from Execution e where e.scheduledExecution.uuid = :uuid");
		query.setParameter("uuid", job.getId());
		
		final List<?> _executions = query.list();
		context.getLogger().log(4, "EXECUTIONS: " + _executions);
		for(int _index = 0; _index < _executions.size(); _index++){
			Object _execution = _executions.get(_index);
			context.getLogger().log(4, _execution.getClass().toString());
			
			Object _argStringObj = MethodUtils.invokeMethod(_execution, "getArgString");
			context.getLogger().log(4, "ARGS: " + _argStringObj);
			
			Object _scheduled = MethodUtils.invokeMethod(_execution, "getScheduledExecution");
			context.getLogger().log(4, "" + _scheduled);
			
			Object _optionsObj = MethodUtils.invokeMethod(_scheduled, "getOptions");
			
			if(_optionsObj instanceof Set<?>){
				Set<?> _optionsSet = (Set<?>)_optionsObj;
				Iterator<?> _options = _optionsSet.iterator();
				while(_options.hasNext()){
					Object _option = _options.next();
					context.getLogger().log(4, "" + _option);
					
					Object _valuesObj = MethodUtils.invokeMethod(_option, "getValues");
					if(_valuesObj instanceof Set<?>){
						Set<?> _valuesSet = (Set<?>)_valuesObj;
						Iterator<?> _values = _valuesSet.iterator();
						while(_values.hasNext()){
							Object _value = _values.next();
							context.getLogger().log(4, "THE VALUE: " + _value);
						}
					}
				}
			}
		}
		
		session.close();
		
	}

	@Override
	public void executeStep(PluginStepContext context, Map<String, Object> map) throws StepException {
		
		try{
			poc(context);
		}catch(Exception e){
			throw new StepException(e.getMessage(), new FailureReason() {
			});
		}
	}

}

