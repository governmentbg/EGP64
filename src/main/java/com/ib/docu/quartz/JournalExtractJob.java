package com.ib.docu.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class JournalExtractJob  implements Job {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JournalExtractJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO
	}

}
