/*-
 * #%L
 * xlator
 * %%
 * Copyright (C) 2016 - 2018 FNS
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.fns.xlator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
// @see https://github.com/spring-projects/spring-boot/issues/11950
public class GitLoggingListener implements ApplicationListener<ApplicationStartedEvent> {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(GitLoggingListener.class);

	@Override
	public void onApplicationEvent(final ApplicationStartedEvent event) {
		try {
			GitProperties git = event.getApplicationContext().getBean(GitProperties.class);
			LOGGER.info("This application was built from git commit id: {}", git.getShortCommitId());
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("This application was not built in a git repository.");
		}
	}
}
