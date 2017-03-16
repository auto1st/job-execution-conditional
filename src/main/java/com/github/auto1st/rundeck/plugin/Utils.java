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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities.
 * 
 * @author fabiojose
 *
 */
public final class Utils {
	
	protected static final Properties PROPERTIES = new Properties();
	static {
		try{
			PROPERTIES.load(JobExecutionConditional.class.getResourceAsStream("/resources/plugin.properties"));
		}catch(IOException e){
			throw new RuntimeException(e.getMessage(), e);
		}
	};

	private Utils() {
		
	}
	
	/**
   * Extract the pairs from <code>-VAR=${option.VAR3}</code>, where <code>"VAR"</code> is the key and <code>"${option.VAR3}"</code> is the value
   * @param value The text with one or more pairs
   * @return <code>java.util.Map</code>
   */
	public static Map<String, String> conditionsOf(final String value) {
    final Map<String, String> _result = new HashMap<String, String>();
    
    final Pattern pattern = Pattern.compile(Utils.PROPERTIES.getProperty("regex.option"));
    final Matcher matcher = pattern.matcher(value);
    while(matcher.find()){
      String _key = matcher.group(1);
      
      //remove the hyphen symbol
      _key = _key.substring(1);
      
      //remove the equals symbol
      _key = _key.substring(0, _key.length() - 1);
      
      //put the _key and the value in the map
      _result.put(_key, matcher.group(2));
    }
    
    return _result;
  }
	
	/**
	 * Extract the pairs from <code>-VAR0 value -VAR1 value</code>, where <code>"VAR0"</code> is the key and <code>"value"</code> is the value.
	 * @param text The text with one or more pairs
	 * @return <code>java.util.Map</code>
	 */
	public static Map<String, String> optionsOf(final String text) {
	  final Map<String, String> _result = new HashMap<String, String>();
	  
	  final Pattern pattern = Pattern.compile(Utils.PROPERTIES.getProperty("regex.args"));
	  final Matcher matcher = pattern.matcher(text);
	  
	  while(matcher.find()){
	    
	    /*
	     * Part one: execution key
	     */
	    String _key = matcher.group(1);
	    
      //remove the hyphen and get VAR0
      _key = _key.substring(1);
      
      //remove spaces
      _key = _key.trim();
      
      /*
       * Part two: execution value
       */
      String _value = matcher.group(2);
      
      _result.put(_key, _value);
	  }
	  
	  return _result;
	}
	
	/**
	 * Expand the expression equals this <code>${option.VAR3}</code>, getting the value from data context.<br/>
	 * 
	 * @param expr Expression
	 * @param dataContext Rundeck data context
	 * @return Value found in data context or <code>null</code>
	 */
	public static String expand(final String expr, final Map<String, Map<String, String>> dataContext) {
		
		final Pattern pattern = Pattern.compile(Utils.PROPERTIES.getProperty("regex.option.group"));
		final Matcher matcher = pattern.matcher(expr);
		
		matcher.find();
		if(matcher.groupCount() >= 1){
			String group = matcher.group(1);
			
			if(group.startsWith("option.")){
				
				String var = group.split("\\.")[1];
				Map<String, String> _options = dataContext.get("option");
				if(null!= _options && _options.containsKey(var)){
					return _options.get(var);
				}
				
			}
		}
		
		return null;
	}
	
	/**
	 * Check if a value is an expression like this <code>${option.VAR3}</code>
	 * @param value
	 * @return <code>true</code> if value is an expression
	 */
	public static boolean isExpr(final String value) {
	  
	  final Pattern pattern = Pattern.compile(Utils.PROPERTIES.getProperty("regex.option.group"));
	  final Matcher matcher = pattern.matcher(value);
	  
	  return matcher.matches();
	}
}
