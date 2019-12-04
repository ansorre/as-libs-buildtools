/*
 * Copyright 2019 Antonio Sorrentini
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package me.as.lib.buildtools.io;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.as.lib.core.lang.StringExtras.getQuickHashedUniqueKey;


public class JavaBundleSpecs
{

 public final String path;

 /** Glob Patterns of packages which are allowed */
 public final List<String> allowedPackages;

 /** Glob Patterns of packages which are to be escluded */
 public final List<String> excludedPackages;

 /** Glob Patterns of resource files which are allowed */
 public final List<String> allowedResources;

 public final List<String> options;

 public final String uniqueId;


 public JavaBundleSpecs(String path)
 {
  this(path, null, null, null, null);
 }

 public JavaBundleSpecs(String path, List<String> allowedPackages, List<String> excludedPackages, List<String> allowedResources, List<String> options)
 {
  this.path=path;
  this.allowedPackages=allowedPackages;
  this.excludedPackages=excludedPackages;
  this.allowedResources=allowedResources==null ? new ArrayList<>(Collections.singletonList("*")) : allowedResources;
  this.options=options;
  uniqueId=getQuickHashedUniqueKey(path, allowedPackages, excludedPackages, allowedResources, options);
 }



}
