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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
	  
	  final Pattern _pattern = Pattern.compile(Utils.PROPERTIES.getProperty("regex.condition"));
	  final Matcher _matcher = _pattern.matcher(value);
	  
	  int _length = 0;
	  
	  /*
	   * For each match
	   */
	  while(_matcher.find()){
	    /*
	     * Get the length of group and accumulate
	     */
	    _length+=_matcher.group(1).length();
	  }
	  
	  /*
	   * false: invalid because there are unmatched characters
	   * true.: valid
	   */
	  return value.length() == _length;
	}

}
