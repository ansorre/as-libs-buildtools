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

package me.as.lib.buildtools.engine;


import me.as.lib.core.system.FileInfo;

import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;


public class CustomJavaFileObject<I extends FileInfo> extends ForwardingJavaFileObject
{
 public I eFileInfo;


 protected CustomJavaFileObject(JavaFileObject fileObject, I eFileInfo)
 {
  super(fileObject);

  if (eFileInfo==null)
   throw new RuntimeException("eFileInfo should not be null!");

  this.eFileInfo=eFileInfo;
 }




}
