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

/**
 * Exception to use when try to load the resources/plugin.properties
 * 
 * @author fabiojose
 *
 */
public final class PropertiesNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PropertiesNotFoundException(){
    
  }
  
  public PropertiesNotFoundException(final String message){
    super(message);
  }
  
  public PropertiesNotFoundException(final Throwable cause){
    super(cause);
  }
  
  public PropertiesNotFoundException(final String message, final Throwable cause){
    super(message, cause);
  }
}
