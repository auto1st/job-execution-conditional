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

import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;

/**
 * Validates the option condition property.
 * 
 * @author fabiojose
 *
 */
public class OptionConditionValidator implements PropertyValidator {

	@Override
	public boolean isValid(final String value) throws ValidationException {
		

	  /*
		LineNumberReader _reader = new LineNumberReader(new StringReader(value));
		try{
			while( _reader.readLine() != null);
		}catch(IOException e){}

		final Pattern pattern = Pattern.compile(Utils.PROPERTIES.getProperty("regex.option"));
		final Matcher matcher = pattern.matcher(value);
		int _counter = 0;
		while(matcher.find()){
			_counter++;
		}
		
		return (_reader.getLineNumber() == _counter);
		*/
	  
	  return true;
	}

}
